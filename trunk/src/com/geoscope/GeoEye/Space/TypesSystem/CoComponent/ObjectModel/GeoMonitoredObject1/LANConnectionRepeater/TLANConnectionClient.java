package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;

public class TLANConnectionClient extends TCancelableThread {

	public static int ServerReadWriteTimeout = 1000; //. ms
	public static int TransferBufferSize = 1024*1024;
	//.
	private TLANConnectionRepeater Repeater; 
	//.
	protected Socket 			ServerSocket;
	protected InputStream 	ServerSocketInputStream;
	protected OutputStream 	ServerSocketOutputStream;
	//.
	private int ConnectionID = 0;
	//.
	private OutputStream DestinationSocketOutputStream;
	//.
	private String ExceptionMessage;

	public boolean flActive = false;
	public boolean flRunning = false;

	public TLANConnectionClient(TLANConnectionRepeater pRepeater, OutputStream pDestinationSocketOutputStream) throws Exception {
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
	
	public void Destroy() throws IOException {
		CancelAndWait();
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
	
    protected static int InputStream_Read(InputStream Connection, byte[] Data, int DataSize) throws IOException {
        int SummarySize = 0;
        int ReadSize;
        int Size;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = Connection.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	return Size; //. ->
            SummarySize += Size;
        }
        return SummarySize;
    }
	
	private void Connect() throws Exception {
		ServerSocket = new Socket(Repeater.ServerAddress,Repeater.ServerPort); 
		ServerSocket.setSoTimeout(LANConnectionRepeaterDefines.ServerReadWriteTimeout);
		ServerSocket.setKeepAlive(true);
		ServerSocket.setSendBufferSize(8192);
		ServerSocketInputStream = ServerSocket.getInputStream();
		ServerSocketOutputStream = ServerSocket.getOutputStream();
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
		Repeater.StartHandler.DoStartLANConnection(Repeater.ConnectionType, Repeater.Address,Repeater.Port, Repeater.ServerAddress,Repeater.ServerPort, ConnectionID);
		//.
		ServerSocket.setSoTimeout(ServerReadWriteTimeout);
		//.
		flActive = true;
	}
	
	private void Disconnect() throws IOException {
		if (ConnectionID > 0) 
			Repeater.StopHandler.DoStopLANConnection(ConnectionID);
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
				byte[] TransferBuffer = new byte[TransferBufferSize];
				int ActualSize;
				switch (Repeater.ConnectionType) {
				
				case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
					while (!Canceller.flCancel) {
						try {
						    ActualSize = ServerSocketInputStream.read(TransferBuffer,0,TransferBuffer.length);
					    	if (ActualSize == 0)
					    		break; //. > connection is closed
					    		else 
							    	if (ActualSize < 0)
						    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
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
					    }
					}
					break; //. >
					
				case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
					byte[] PacketSizeBA = new byte[4];
					int PacketSize;
					while (!Canceller.flCancel) {
						try {
			                ActualSize = ServerSocketInputStream.read(PacketSizeBA,0,PacketSizeBA.length);
					    	if (ActualSize == 0)
					    		break; //. > connection is closed
					    		else 
							    	if (ActualSize < 0)
						    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
						}
						catch (SocketTimeoutException E) {
							continue; //. ^
						}
						if (ActualSize != PacketSizeBA.length)
							throw new IOException("wrong data descriptor"); //. =>
						PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
						if (PacketSize > 0) {
							ActualSize = InputStream_Read(ServerSocketInputStream,TransferBuffer,PacketSize);	
					    	if (ActualSize == 0)
					    		break; //. > connection is closed
					    		else 
							    	if (ActualSize < 0)
						    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
						}
						//.
					    if (Repeater.OnSourceBytesTransmiteHandler != null)
					    	Repeater.OnSourceBytesTransmiteHandler.DoOnBytesTransmite(TransferBuffer,PacketSize);
					    //.
						DestinationSocketOutputStream.write(PacketSizeBA,0,PacketSizeBA.length);
						if (PacketSize > 0)
							DestinationSocketOutputStream.write(TransferBuffer,0,PacketSize);
					}
					break; //. >
				}
			}
			finally {
				flRunning = false;
			}
		}
		catch (Throwable T) {
		    ExceptionMessage = T.getMessage();
		    //.
		    DoOnException();
		}
	}
	
	private void DoOnException() {
		if (Repeater.ExceptionHandler != null)
			Repeater.ExceptionHandler.DoOnException(ExceptionMessage);
	}
}
