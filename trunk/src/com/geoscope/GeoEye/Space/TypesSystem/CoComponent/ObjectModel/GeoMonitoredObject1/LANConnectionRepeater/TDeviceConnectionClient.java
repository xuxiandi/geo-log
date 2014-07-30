package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Network.TServerConnection;

public class TDeviceConnectionClient extends TCancelableThread {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	private TDeviceConnectionRepeater Repeater; 
	//.
    public int				ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
	protected Socket 		ServerSocket = null;
	protected InputStream 	ServerSocketInputStream;
	protected OutputStream 	ServerSocketOutputStream;
	//.
	private int ConnectionID = 0;
	//.
	private OutputStream DestinationSocketOutputStream;

	public boolean flActive = false;
	public boolean flRunning = false;

	public TDeviceConnectionClient(TDeviceConnectionRepeater pRepeater, OutputStream pDestinationSocketOutputStream) throws Exception {
		Repeater = pRepeater;
		//.
		DestinationSocketOutputStream = pDestinationSocketOutputStream;
		//.
		Connect();
		//.
		flRunning = true;
		//.
		_Thread = new Thread(this);
		_Thread.start();
	}
	
	public void Destroy() throws Exception {
		Cancel();
		if (ServerSocket != null)
			ServerSocket.close(); //. cancel socket blocking reading
		Wait();
		//.
		if (flActive)
			Disconnect();
	}
	
	private short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
        int CRC = 0;
        int V;
        int Idx  = Offset;
        while (Idx < (Offset+Size))
        {
            V = (int)(buffer[Idx] & 0x000000FF);
            CRC = (((CRC+V) << 1)^V);
            //.
            Idx++;
        }
        return (short)CRC;
	}
	
	private void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
        int StartIdx = Offset;
        byte[] UserPasswordArray;
        UserPasswordArray = UserPassword.getBytes("windows-1251");
        //.
        if (UserPasswordArray.length > 0)
        {
            int UserPasswordArrayIdx = 0;
            for (int I = StartIdx; I < (StartIdx+Size); I++)
            {
                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
                UserPasswordArrayIdx++;
                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
                	UserPasswordArrayIdx = 0;
            }
        }
	}
	
	private final int Connect_TryCount = 3;
	
	private void Connect() throws Exception {
		int TryCounter = Connect_TryCount;
		while (true) {
			try {
				try {
					//. connect
			    	switch (ConnectionType()) {
			    	
			    	case CONNECTION_TYPE_PLAIN:
			    		ServerSocket = new Socket(Repeater.ServerAddress,Repeater.ServerPort); 
			    		break; //. >
			    		
			    	case CONNECTION_TYPE_SECURE_SSL:
			    		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
			    	        @Override
			    	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
			    	        }
			    	        @Override
			    	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
			    	        }
			    	        @Override
			    	        public X509Certificate[] getAcceptedIssuers() {
			    	            return null;
			    	        }
			    	    } };
			    	    //. install the all-trusting trust manager
			    	    SSLContext sslContext = SSLContext.getInstance( "SSL" );
			    	    sslContext.init( null, _TrustAllCerts, new java.security.SecureRandom());
			    	    //. create a ssl socket factory with our all-trusting manager
			    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			    	    ServerSocket = (SSLSocket)sslSocketFactory.createSocket(Repeater.ServerAddress,Repeater.SecureServerPort());
			    		break; //. >
			    		
			    	default:
			    		throw new Exception("unknown connection type"); //. =>
			    	}
					ServerSocket.setSoTimeout(LANConnectionRepeaterDefines.ServerReadWriteTimeout);
					ServerSocket.setKeepAlive(true);
					ServerSocket.setSendBufferSize(8192);
					ServerSocketInputStream = ServerSocket.getInputStream();
					ServerSocketOutputStream = ServerSocket.getOutputStream();
					break; //. >
				} catch (SocketTimeoutException STE) {
					throw new IOException("server connection timeout"); //. =>
				} catch (ConnectException CE) {
					throw new ConnectException("no server connection"); //. =>
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.toString();
					throw new Exception("server connection error: "+S); //. =>
				}
			}
			catch (Exception E) {
				TryCounter--;
				if (TryCounter == 0)
					throw E; //. =>
			}
		}
        //. login
    	byte[] LoginBuffer = new byte[20];
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(LANConnectionRepeaterDefines.SERVICE_LANCONNECTION_DESTINATION_V1);
		System.arraycopy(BA,0, LoginBuffer,0, BA.length);
		BA = TDataConverter.ConvertInt32ToBEByteArray(Repeater.UserID);
		System.arraycopy(BA,0, LoginBuffer,2, BA.length);
		BA = TDataConverter.ConvertInt32ToBEByteArray(Repeater.idGeographServerObject);
		System.arraycopy(BA,0, LoginBuffer,10, BA.length);
		short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
		BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
		System.arraycopy(BA,0, LoginBuffer,18, BA.length);
		Buffer_Encrypt(LoginBuffer,10,10,Repeater.UserPassword);
		//. send login data
		ServerSocketOutputStream.write(LoginBuffer);
		//. check login
		byte[] DecriptorBA = new byte[4];
		ServerSocketInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		if (Descriptor < 0)
			throw new Exception("destination login error, RC: "+Integer.toString(Descriptor)); //. =>
		if (Descriptor == 0) 
			throw new Exception("Wrong LANConnection, CID: "+Integer.toString(Descriptor)); //. =>
		ConnectionID = Descriptor;
		//. make connection from device side
		Repeater.StartHandler.DoStartDeviceConnection(Repeater.CUAL, Repeater.ServerAddress,Repeater.ServerPort, ConnectionID);
		//.
		ServerSocket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
		//.
		flActive = true;
	}
	
	private void Disconnect() throws Exception {
		if (ConnectionID > 0) 
			Repeater.StopHandler.DoStopDeviceConnection(ConnectionID);
		//.
		ServerSocketOutputStream.close();
		ServerSocketInputStream.close();
		ServerSocket.close();
		//.
		ConnectionID = 0;
		flActive = false;
	}
	
	@Override
	public void run() {
		try {
			try {
				byte[] TransferBuffer = new byte[TLANConnectionRepeater.TransferBufferSize];
				int ActualSize;
				while (!Canceller.flCancel) {
					try {
					    ActualSize = ServerSocketInputStream.read(TransferBuffer,0,TransferBuffer.length);
				    	if (ActualSize == 0)
				    		break; //. > connection is closed
				    		else 
						    	if (ActualSize < 0) {
							    	if (ActualSize == -1)
							    		break; //. > stream EOF, connection is closed
							    	else
							    		throw new IOException("error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
						    	}
					}
					catch (SocketTimeoutException E) {
						continue; //. ^
					}
					//.
				    if (ActualSize > 0) {
				    	if (Repeater.OnSourceBytesTransmiteHandler != null)
				    		Repeater.OnSourceBytesTransmiteHandler.DoOnBytesTransmite(TransferBuffer,ActualSize);
				    	//.
				    	DestinationSocketOutputStream.write(TransferBuffer,0,ActualSize);
				    	//.
				    	DestinationSocketOutputStream.flush();
				    }
				}
			}
			finally {
				flRunning = false;
			}
		}
		catch (Throwable T) {
			if (!Canceller.flCancel) {
				if (Repeater.ExceptionHandler != null)
					Repeater.ExceptionHandler.DoOnException(T);
			}
		}
	}
}
