package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;

public class TConnectionRepeater extends TCancelableThread {

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
    public static final List<TConnectionRepeater> Repeaters = Collections.synchronizedList(new ArrayList<TConnectionRepeater>());
	//.
	public TLANModule LANModule;
	//.
	private String 	DestinationAddress;
	private int 	DestinationPort;
    //.
    private Socket 			DestinationConnection;
    protected InputStream 	DestinationConnectionInputStream;
    protected OutputStream 	DestinationConnectionOutputStream;
    private Object 			DestinationConnectionResultLock = new Object();
    private Exception		DestinationConnectionResult = null;
    //.
	///? private Date LastActivityTimestamp;
    //.
	public int	ConnectionID = 0;
	
	public TConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		LANModule = pLANModule;
		//.
		ConnectionID = pConnectionID;
		//.
		DestinationAddress = pDestinationAddress;
		DestinationPort = pDestinationPort;
		//.
		Repeaters.add(this);
		//.
		_Thread = new Thread(this);
	}
	
	public void Destroy() {
		Cancel();
	}
	
	protected void Start() {
		_Thread.start();
	}
	
	protected void ConnectSource() throws IOException {		
	}

	protected void DisconnectSource() throws IOException {
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
	
	public boolean ReceivingIsAvaiable() {
		return true;
	}
	
	public boolean TransmittingIsAvaiable() {
		return true;
	}
	
	public void DoReceiving() throws IOException {
	}
	
	public void DoTransmitting() throws IOException {
	}
	
	private class TReceiving implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					DoReceiving();
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionRepeater.Receiving",S,TE.getStackTrace());
			}
		}		
	}
	
	private class TTransmitting implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					DoTransmitting();
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionRepeater.Transmitting",S,TE.getStackTrace());
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
					TReceiving Receiving = null;
					Thread ReceivingThread = null;
					if (ReceivingIsAvaiable()) {
						Receiving = new TReceiving();
						ReceivingThread = new Thread(Receiving);
					}
					try {
						if (ReceivingThread != null) {
							ReceivingThread.setPriority(Thread.MAX_PRIORITY);
							ReceivingThread.start();
						}
						//.
						TTransmitting Transmitting = null;
						Thread TransmittingThread = null;
						if (TransmittingIsAvaiable()) {
							Transmitting = new TTransmitting();
							TransmittingThread = new Thread(Transmitting);
						}
						try {
							if (TransmittingThread != null) {
								TransmittingThread.setPriority(Thread.MAX_PRIORITY);
								TransmittingThread.start();
							}
							//. working in Receiving&Transmitting threads
							while (!Canceller.flCancel) {
								Thread.sleep(100);
								if (((Receiving != null) && (!Receiving.flRunning)) || ((Transmitting != null) && (!Transmitting.flRunning)))
									break; //. >
							}
						}
						finally {
							if (TransmittingThread != null) 
								TransmittingThread.interrupt();
						}
					}
					finally {
						if (ReceivingThread != null) 
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
		Repeaters.remove(this);
	}
	
	public synchronized boolean IsIdle() {
		return false;
	}
}
