package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.Network.TServerConnection;

@SuppressLint("HandlerLeak")
public class TGeoScopeServerUserSession extends TCancelableThread {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static final int WaitForInternetConnectionInterval = 1000*30; //. seconds
	//.
	public static final int ServerDefaultPort = 8888;
	public static final int ServerReadWriteTimeout 				= 1000*60; //. Seconds
	public static final int ServerDefaultCheckpointInterval 	= 1000*600; //. Seconds
	public static final int ServerReconnectInterval 			= 1000*1; //. Seconds
	public static final int ServerReconnectMultiplier 			= 3600;
	public static final int ServerConnectErrorDisplayingCounter = 25;
	public static final int ServerErrorDisplayingCounter 		= 10;
	//.
	public static final int ConnectionMinCheckpointInterval 	= 1000*300; //. Seconds	
	
	public static final short SERVICE_NONE          = 0;
	public static final short SERVICE_MESSAGING    	= 1;
	//.
	public static final short SERVICE_MESSAGING_VERSION_1	= 1;
	public static final short SERVICE_MESSAGING_VERSION_2	= 2;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	public static final int MESSAGE_CHECKPOINT = 1;
	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_NOTFOUND              = -2;
	public static final int MESSAGE_UNKNOWNSERVICE        = -10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
	public static final int MESSAGE_WRONGPARAMETERS       = -15;
	//.
	public static final int SERVICE_MESSAGING_CLIENTMESSAGE_SPACEWINDOWUPDATING_SUBSCRIBE         		= 1001;
	public static final int SERVICE_MESSAGING_CLIENTMESSAGE_SPACEWINDOWUPDATING_UNSUBSCRIBE       		= 1002;
	public static final int SERVICE_MESSAGING_CLIENTMESSAGE_TILESERVERSPACEWINDOWUPDATING_SUBSCRIBE   	= 1003;
	public static final int SERVICE_MESSAGING_CLIENTMESSAGE_TILESERVERSPACEWINDOWUPDATING_UNSUBSCRIBE 	= 1004;
	//.
	public static final int SERVICE_MESSAGING_SERVERMESSAGE_NEWUSERMESSAGE        		= 1;
	public static final int SERVICE_MESSAGING_SERVERMESSAGE_SPACEWINDOWUPDATE     		= 2;
	public static final int SERVICE_MESSAGING_SERVERMESSAGE_TILESERVERSPACEWINDOWUPDATE = 3;
	
	public static void CheckMessage(int Message) throws Exception {
		if (Message >= 0)
			return; //. ->
		switch (Message) {
		
		case MESSAGE_ERROR:
			throw new Exception("error"); //. =>
			
		case MESSAGE_NOTFOUND:
			throw new Exception("data is not found"); //. =>
			
		case MESSAGE_UNKNOWNSERVICE:
			throw new Exception("unknown service"); //. =>
			
		case MESSAGE_AUTHENTICATIONFAILED:
			throw new Exception("authentication is failed"); //. =>
			
		case MESSAGE_ACCESSISDENIED:
			throw new Exception("access is denied"); //. =>
			
		case MESSAGE_TOOMANYCLIENTS:
			throw new Exception("too many clients"); //. =>
			
		case MESSAGE_UNKNOWNCOMMAND:
			throw new Exception("unknown command"); //. =>
			
		case MESSAGE_WRONGPARAMETERS:
			throw new Exception("wrong parameters"); //. =>
			
		default:
			throw new Exception("unknown error, code: "+Integer.toString(Message)); //. =>
		}
	}

	private TGeoScopeServerUser User;
	//.
	private String 	ServerAddress;
	private int  	ServerPort;
	protected int 	SecureServerPortShift = 2;
    protected int	SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
	//.
	private static Random rnd = new Random();
	//.
    public int				ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
    private Socket 			Connection;
	private int 			ConnectionCheckpointInterval = ConnectionMinCheckpointInterval;
    private InputStream 	ConnectionInputStream;
    private OutputStream 	ConnectionOutputStream;
	private boolean			flConnected = false;
    //.
    public boolean flSessioning = false;
    //.
    private boolean flReconnect = false;
	
	public TGeoScopeServerUserSession(TGeoScopeServerUser pUser) {
		User = pUser;
		//.
		Initialize();
	}
	
	public void Destroy() {
		Finalize();
	}
	
	public void Initialize() {
		Finalize();
		//.
		_Thread = new Thread(this);
		_Thread.start();		
	}
	
	public void Finalize() {
		if (_Thread != null) {
			Cancel();
			_Thread = null;
		}
	}
	
	@Override 
	public void CancelAndWait() {
		if (Connection != null)
			try {
				Connection.shutdownInput();
			} catch (IOException E) {
			}
		super.CancelAndWait();
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
	
    private static void Connection_CheckReadData(Socket Connection, InputStream ConnectionInputStream, byte[] Data, int CheckInterval) throws Exception {
    	int LastTimeout = Connection.getSoTimeout();
    	try {
    		Connection.setSoTimeout(CheckInterval);
    		//.
            int Size;
            int SummarySize = 0;
            int ReadSize;
            while (SummarySize < Data.length) {
                ReadSize = Data.length-SummarySize;
                Size = ConnectionInputStream.read(Data,SummarySize,ReadSize);
                if (Size <= 0) 
                	throw new Exception("Connection_CheckReadData: connection is closed unexpectedly"); //. =>
                SummarySize += Size;
            }
    	}
    	finally {
    		Connection.setSoTimeout(LastTimeout);
    	}
    }
	
    private static void Connection_ReadData(Socket Connection, InputStream ConnectionInputStream, byte[] Data) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < Data.length) {
            ReadSize = Data.length-SummarySize;
            Size = ConnectionInputStream.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception("Connection_ReadData: connection is closed unexpectedly"); //. =>
            SummarySize += Size;
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
			            Connection = new Socket(ServerAddress,ServerPort); 
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
			    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
			    		break; //. >
			    		
			    	default:
			    		throw new Exception("unknown connection type"); //. =>
			    	}
			        Connection.setSoTimeout(ServerReadWriteTimeout);
			        Connection.setKeepAlive(true);
			        ConnectionInputStream = Connection.getInputStream();
			        ConnectionOutputStream = Connection.getOutputStream();
					break; //. >
				} catch (SocketTimeoutException STE) {
					throw new IOException(User.Server.context.getString(R.string.SConnectionTimeoutError)); //. =>
				} catch (ConnectException CE) {
					throw new ConnectException(User.Server.context.getString(R.string.SNoServerConnection)); //. =>
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.toString();
					throw new Exception(User.Server.context.getString(R.string.SHTTPConnectionError)+S); //. =>
				}
			}
			catch (Exception E) {
				TryCounter--;
				if (TryCounter == 0)
					throw E; //. =>
			}
		}
        //. send login info
        String UserIDStr = Integer.toString(User.UserID);
        int UserIDStrSize = 2*UserIDStr.length(); //. UCS2(UTF-16) size
        int UserIDStr1Length = 16;
        StringBuilder SB = new StringBuilder(UserIDStr1Length);
        SB.append(User.UserID);
        final char[] CharSet = new char[] {'!','@','#','$','%','^','&','*','(',')'};
        while (SB.length() < UserIDStr1Length) 
        	SB.append(CharSet[rnd.nextInt(CharSet.length)]);
        String UserIDStr1 = SB.toString();
        int UserIDStr1Size = 2*UserIDStr1.length(); //. UCS2(UTF-16) size
    	byte[] LoginBuffer = new byte[2/*SizeOf(Service)*/+2/*SizeOf(Version)*/+4/*SizeOf(UserIDStrSize)*/+UserIDStrSize+4/*SizeOf(UserIDStr1Size)*/+UserIDStr1Size];
    	int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_MESSAGING);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_MESSAGING_VERSION_2);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStrSize);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr.getBytes("UTF-16LE");
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStr1Size);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr1.getBytes("UTF-16LE");
		Buffer_Encrypt(BA,0,BA.length,User.UserPassword);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		//.
		ConnectionOutputStream.write(LoginBuffer);
		//. check login
		byte[] DecriptorBA = new byte[4];
		ConnectionInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
		//. get checkpoint interval
		ConnectionInputStream.read(DecriptorBA);
		ConnectionCheckpointInterval = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		if (ConnectionCheckpointInterval > ConnectionMinCheckpointInterval)
			ConnectionCheckpointInterval = ConnectionMinCheckpointInterval;
		//.
		flConnected = true;
	}

	private void Disconnect() throws IOException {
		flConnected = false;
        //. close connection gracefully
		try {
	        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_DISCONNECT);
	        ConnectionOutputStream.write(BA);
	        ConnectionOutputStream.flush();
		}
		catch (Exception E) {}
        //.
        ConnectionOutputStream.close();
        ConnectionInputStream.close();
        Connection.close();
	}
	
	public boolean IsOnline() {
		return flSessioning;
	}
	
	private void ValidateOnConnect() {
		//. retransmit possible missed messages
		MessageHandler.obtainMessage(HANDLER_MESSAGE_NEWUSERMESSAGE).sendToTarget();
		//. validate space window's subscription
		TReflector Reflector = TReflector.GetReflector();
		if (Reflector != null)
			Reflector.ReflectionWindow.UpdateSubscription_ResubscribeIfValid();
	}
	
	public void run() {
		int ErrorDisplayingCount = 0;
		try {
			int ReconnectMultiplier = 1;
			while (!Canceller.flCancel) {
				try {
    				//. waiting for Internet connection
    				/*///? while (!User.Server.IsNetworkAvailable()) 
    					Thread.sleep(WaitForInternetConnectionInterval);*/
					//. establishing user session 
					TGeoScopeServerInfo.TInfo SI = User.Server.Info.GetInfo();
					if (!SI.IsSpaceUserSessionServerValid())
						return; //. ->
					ServerAddress = SI.SpaceUserSessionServerAddress;
					ServerPort = SI.SpaceUserSessionServerPort;
					if (ServerPort == 0)
						ServerPort = ServerDefaultPort;
					//.
					Connect();
					try {
						ReconnectMultiplier = 1;
						//.
						flSessioning = true;
						try {
							ValidateOnConnect();
			    			//. processing
							byte[] CheckpointMessageBA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_CHECKPOINT);
							byte[] MessageBA = new byte[4];
							int Message;
							while (!Canceller.flCancel) {
						        try {
						        	Connection_CheckReadData(Connection, ConnectionInputStream, MessageBA, ConnectionCheckpointInterval);
						        }
						        catch (InterruptedIOException IIOE) { //. timeout
						        	//. send checkpoint message
						        	synchronized (ConnectionOutputStream) {
								        ConnectionOutputStream.write(CheckpointMessageBA);
								        ConnectionOutputStream.flush();
									}
							        //.
							        continue; //. ^
						        }					
					        	//. process message
					        	Message = TDataConverter.ConvertBEByteArrayToInt32(MessageBA,0);
					        	switch (Message) {
					        	
					        	case SERVICE_MESSAGING_SERVERMESSAGE_NEWUSERMESSAGE:
					    			MessageHandler.obtainMessage(HANDLER_MESSAGE_NEWUSERMESSAGE).sendToTarget();
					        		break; //. >

					        	case SERVICE_MESSAGING_SERVERMESSAGE_SPACEWINDOWUPDATE:
					        		byte[] BA = new byte[2];
						        	Connection_ReadData(Connection, ConnectionInputStream, BA);
						        	short WindowID = TDataConverter.ConvertBEByteArrayToInt16(BA,0);
						        	//.
					    			MessageHandler.obtainMessage(HANDLER_MESSAGE_SPACEWINDOWUPDATE,WindowID).sendToTarget();
					        		break; //. >
					        		
					        	case SERVICE_MESSAGING_SERVERMESSAGE_TILESERVERSPACEWINDOWUPDATE:
					        		BA = new byte[2];
						        	Connection_ReadData(Connection, ConnectionInputStream, BA);
						        	WindowID = TDataConverter.ConvertBEByteArrayToInt16(BA,0);
						        	@SuppressWarnings("unused")
									String Compilations = "";
					        		BA = new byte[4];
						        	Connection_ReadData(Connection, ConnectionInputStream, BA);
						        	int CompilationsSize = TDataConverter.ConvertBEByteArrayToInt32(BA,0);
						        	if (CompilationsSize > 0) {
						        		BA = new byte[CompilationsSize];
							        	Connection_ReadData(Connection, ConnectionInputStream, BA);
							        	Compilations = new String(BA,"US-ASCII");
						        	}
						        	//.
					    			MessageHandler.obtainMessage(HANDLER_MESSAGE_TILESERVERSPACEWINDOWUPDATE,WindowID).sendToTarget();
					        		break; //. >
					        	}
							}
						}
						finally {
							flSessioning = false;						
						}
					}
					finally {
						Disconnect();
					}
				} catch (InterruptedException E) {
					return; //. ->
				} catch (CancelException CE) {
					return; //. ->
				} catch (ConnectException CE) {
					ErrorDisplayingCount++;
					if ((ErrorDisplayingCount % ServerConnectErrorDisplayingCounter) == 0) 
						MessageHandler.obtainMessage(HANDLER_MESSAGE_SHOWEXCEPTION,new Exception(User.Server.context.getString(R.string.SUserSessionError)+User.Server.context.getString(R.string.SNoServerConnection))).sendToTarget();
				} catch (Throwable E) {
					ErrorDisplayingCount++;
					if ((ErrorDisplayingCount % ServerErrorDisplayingCounter) == 0) 
						MessageHandler.obtainMessage(HANDLER_MESSAGE_SHOWEXCEPTION,new Exception(User.Server.context.getString(R.string.SUserSessionError)+E.getMessage())).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
				//. sleeping for reconnect...
				for (int I = 0; I < ReconnectMultiplier; I++) {
					Thread.sleep(ServerReconnectInterval);
					//.
					if (Canceller.flCancel)
						return; //. ->
					//.
					if (flReconnect) {
						flReconnect = false;
						break; //. >
					}
				}
				//.
				if (ReconnectMultiplier < ServerReconnectMultiplier)
					ReconnectMultiplier <<= 1;
			}
		} catch (InterruptedException E) {
		} catch (Throwable E) {
			MessageHandler.obtainMessage(HANDLER_MESSAGE_SHOWEXCEPTION,new Exception(User.Server.context.getString(R.string.SUserSessionError)+E.getMessage())).sendToTarget();
		}
	}
	
	public boolean IsConnected() {
		return flConnected;
	}
	
	public void Reconnect() {
		flReconnect = true;
	}
	
	public void SendMessage(byte[] Message) throws IOException {
		if (!flConnected)
			throw new IOException("user session is not connected"); //. =>
    	synchronized (ConnectionOutputStream) {
	        ConnectionOutputStream.write(Message);
	        ConnectionOutputStream.flush();
		}
	}
	
	private static final int HANDLER_MESSAGE_SHOWEXCEPTION 					= -1;
	private static final int HANDLER_MESSAGE_SHOWMESSAGE 					= 0;
	private static final int HANDLER_MESSAGE_NEWUSERMESSAGE 				= 1;
	private static final int HANDLER_MESSAGE_SPACEWINDOWUPDATE 				= 2;
	private static final int HANDLER_MESSAGE_TILESERVERSPACEWINDOWUPDATE	= 3;
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case HANDLER_MESSAGE_SHOWEXCEPTION:
    				if (Canceller.flCancel)
    					break; //. >
    				Exception E = (Exception)msg.obj;
    				Toast.makeText(User.Server.context,E.getMessage(), Toast.LENGTH_LONG).show();
    				// .
    				break; //. >
    				
    			case HANDLER_MESSAGE_SHOWMESSAGE:
    				if (Canceller.flCancel)
    					break; //. >
    				String Msg  = (String)msg.obj;
    				Toast.makeText(User.Server.context,Msg, Toast.LENGTH_SHORT).show();
    				// .
    				break; //. >

    			case HANDLER_MESSAGE_NEWUSERMESSAGE:
    				if (Canceller.flCancel)
    					break; //. >
    				if (User.IncomingMessages != null)
    					User.IncomingMessages.Check();
    				//.
    				break; //. >

    			case HANDLER_MESSAGE_SPACEWINDOWUPDATE:
    			case HANDLER_MESSAGE_TILESERVERSPACEWINDOWUPDATE:
    				if (Canceller.flCancel)
    					break; //. >
    				short WindowID = (Short)msg.obj;
    				TReflector Reflector = TReflector.GetReflector();
    				if ((Reflector != null) && (Reflector.ReflectionWindow.ID == WindowID))
    					Reflector.PostStartUpdatingSpaceImage();
    				//.
    				break; //. >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};	
}
