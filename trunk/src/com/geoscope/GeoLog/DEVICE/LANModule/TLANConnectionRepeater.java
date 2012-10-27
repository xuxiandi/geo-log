package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.Utils.TCancelableThread;

public class TLANConnectionRepeater extends TCancelableThread {

	public static final int SourceConnectionTimeout = 1000*30; //. seconds
	public static final int DestinationConnectionTimeout = 1000*30; //. seconds
	public static final int IdleTimeout = 1000*60; //. seconds
	//.
	public static final short SERVICE_LANCONNECTION_SOURCE	= 5;
	public static final short SERVICE_LANCONNECTION_SOURCE_V1 = 6;
	//.
	public static final int MESSAGE_LANCONNECTIONISNOTFOUND 	= -101;
	//.
	public static final int TransferBufferSize = 8192;
	//.
    public static final ArrayList<TLANConnectionRepeater> Repeaters = new ArrayList<TLANConnectionRepeater>();
	//.
	private TLANModule LANModule;
	//.
	private String 	SourceAddress;
	private int 	SourcePort;
	//.
    private Socket 			SourceConnection;
    private InputStream 	SourceConnectionInputStream;
    private OutputStream 	SourceConnectionOutputStream;
	//.
	private String 	DestinationAddress;
	private int 	DestinationPort;
    //.
    private Socket 			DestinationConnection;
    private InputStream 	DestinationConnectionInputStream;
    private OutputStream 	DestinationConnectionOutputStream;
    private Object 			DestinationConnectionResultLock = new Object();
    private Exception		DestinationConnectionResult = null;
    //.
	///? private Date LastActivityTimestamp;
    //.
	public int	ConnectionID = 0;
	
	public TLANConnectionRepeater(TLANModule pLANModule, String pSourceAddress, int pSourcePort, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		LANModule = pLANModule;
		//.
		SourceAddress = pSourceAddress;
		SourcePort = pSourcePort;
		//.
		ConnectionID = pConnectionID;
		//.
		DestinationAddress = pDestinationAddress;
		DestinationPort = pDestinationPort;
		//.
		synchronized (Repeaters) {
			Repeaters.add(this);
		}
		//.
		_Thread = new Thread(this);
		_Thread.start();
	}
	
	public void Destroy() {
		Cancel();
	}
	
	public boolean IsSameEndpoint(String pAddress, int pPort) {
		return (SourceAddress.equals(pAddress) && (SourcePort == pPort));
	}
	
	public boolean IsSameEndpoint(TLANConnectionRepeater CR) {
		return (SourceAddress.equals(CR.SourceAddress) && (SourcePort == CR.SourcePort));
	}
	
	private void ConnectSource() throws IOException {
        SourceConnection = new Socket(SourceAddress,SourcePort); 
        SourceConnection.setSoTimeout(SourceConnectionTimeout);
        SourceConnection.setKeepAlive(true);
        SourceConnection.setSendBufferSize(8192);
        SourceConnectionInputStream = SourceConnection.getInputStream();
        SourceConnectionOutputStream = SourceConnection.getOutputStream();
	}
	
	private void DisconnectSource() throws IOException {
		SourceConnectionOutputStream.close();
		SourceConnectionInputStream.close();
		SourceConnection.close();
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
	
	@SuppressWarnings("serial")
	private static class TSuccessException extends Exception {
		public TSuccessException() {
		}
	}
	
	private static TSuccessException Success = new TSuccessException();
	
	private void ConnectDestination() throws Exception {
		Exception ConnectionResult = null;
		try {
			DestinationConnection = new Socket(DestinationAddress,DestinationPort); 
			DestinationConnection.setSoTimeout(DestinationConnectionTimeout);
			DestinationConnection.setKeepAlive(true);
			DestinationConnection.setSendBufferSize(8192);
			DestinationConnectionInputStream = DestinationConnection.getInputStream();
			DestinationConnectionOutputStream = DestinationConnection.getOutputStream();
	        //. login
	    	byte[] LoginBuffer = new byte[20];
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_LANCONNECTION_SOURCE);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(LANModule.Device.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(LANModule.Device.idGeographServerObject);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,18, BA.length);
			Buffer_Encrypt(LoginBuffer,10,10,LANModule.Device.UserPassword);
			//. send login data
			DestinationConnectionOutputStream.write(LoginBuffer);
			//. check login
			byte[] DecriptorBA = new byte[4];
			DestinationConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor < 0)
				throw new Exception("destination login error, RC: "+Integer.toString(Descriptor)); //. =>
			//. send ConnectionID
			DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(ConnectionID);
			DestinationConnectionOutputStream.write(DecriptorBA);
			//. check ConnectionID
			DestinationConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor < 0) {
				if (Descriptor == MESSAGE_LANCONNECTIONISNOTFOUND)
					throw new OperationException(TGetControlDataValueSO.OperationErrorCode_LANConnectionIsNotFound,"LANConnection is not found"); //. =>
				else
					throw new Exception("destination login error, RC: "+Integer.toString(Descriptor)); //. =>
			}
		}
		catch (Exception E) {
			ConnectionResult = E;
		}
		synchronized (DestinationConnectionResultLock) {
			if (ConnectionResult == null)
				DestinationConnectionResult = Success;
			else
				DestinationConnectionResult = ConnectionResult;
			DestinationConnectionResultLock.notify();
		}
		if (ConnectionResult != null)
			throw ConnectionResult; //. =>
	}
	
	private void DisconnectDestination() throws IOException {
		DestinationConnectionOutputStream.close();
		DestinationConnectionInputStream.close();
		DestinationConnection.close();
	}
	
	public boolean WaitForDestinationConnectionResult(int Timeout) throws Exception {
		synchronized (DestinationConnectionResultLock) {
			DestinationConnectionResultLock.wait(Timeout);
			if (DestinationConnectionResult == Success)
				return true; //. ->
			if (DestinationConnectionResult == null)
				return false; //. ->
			//.
			throw DestinationConnectionResult; //. =>
		}
	}
	
	private class TReceiving implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					byte[] TransferBuffer = new byte[TransferBufferSize];
					int Size;
					while (!Canceller.flCancel) {
						try {
			                Size = SourceConnectionInputStream.read(TransferBuffer,0,TransferBufferSize);
			                if (Size <= 0) 
			                	break; //. >
						}
						catch (SocketTimeoutException E) {
							Size = 0;
						}
						if (Size > 0)
							DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
					}
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionRepeater.Receiving",TE.getMessage());
			}
		}		
	}
	
	private class TTransmitting implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					byte[] TransferBuffer = new byte[TransferBufferSize];
					int Size;
					while (!Canceller.flCancel) {
						try {
			                Size = DestinationConnectionInputStream.read(TransferBuffer,0,TransferBufferSize);
			                if (Size <= 0) 
			                	break; //. >
						}
						catch (SocketTimeoutException E) {
							Size = 0;
						}
						if (Size > 0)
			                SourceConnectionOutputStream.write(TransferBuffer,0,Size);
					}
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionRepeater.Transmitting",TE.getMessage());
			}
		}		
	}	
	
	@Override
	public void run() {
		try {
			ConnectSource();
			try {
				ConnectDestination();
				try {
					TReceiving Receiving = new TReceiving();
					Thread ReceivingThread = new Thread(Receiving);
					try {
						ReceivingThread.setPriority(Thread.MAX_PRIORITY);
						ReceivingThread.start();
						TTransmitting Transmitting = new TTransmitting();
						Thread TransmittingThread = new Thread(Transmitting);
						try {
							TransmittingThread.setPriority(Thread.MAX_PRIORITY);
							TransmittingThread.start();
							//. working in Receiving&Transmitting threads
							while (!Canceller.flCancel) {
								Thread.sleep(100);
								if ((!Receiving.flRunning) || (!Transmitting.flRunning))
									break; //. >
							}
						}
						finally {
							TransmittingThread.interrupt();
						}
					}
					finally {
						ReceivingThread.interrupt();
					}
				}
				finally {
					DisconnectDestination();
				}
			}
			finally {
				DisconnectSource();
			}
		}
		catch (InterruptedException E) {
		}
		catch (Throwable TE) {
        	//. log errors
    		LANModule.Device.Log.WriteError("LANModule.LANConnectionRepeater",TE.getMessage());
        	if (!(TE instanceof Exception))
        		TDEVICEModule.Log_WriteCriticalError(TE);
		}
		//.
		synchronized (Repeaters) {
			Repeaters.remove(this);
		}
	}
	
	public synchronized boolean IsIdle() {
		return false;
	}
}
