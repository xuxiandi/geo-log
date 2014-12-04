package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TLANConnectionRepeater1 extends TConnectionRepeater {

	public static final int SourceConnectionTimeout = 1000*30; //. seconds

	private String 	SourceAddress;
	private int 	SourcePort;
	//.
    private Socket 			SourceConnection;
    private InputStream 	SourceConnectionInputStream;
    private OutputStream 	SourceConnectionOutputStream;
    
	public TLANConnectionRepeater1(TLANModule pLANModule, String pSourceAddress, int pSourcePort, String pDestinationAddress, int pDestinationPort, int pConnectionID, long pUserID, String pUserAccessKey) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID, pUserID,pUserAccessKey);
		//.
		SourceAddress = pSourceAddress;
		SourcePort = pSourcePort;
		//.
		Start();
	}
	
	@Override
	protected void ConnectSource() throws IOException {
        SourceConnection = new Socket(SourceAddress,SourcePort); 
        SourceConnection.setSoTimeout(SourceConnectionTimeout);
        SourceConnection.setKeepAlive(true);
        SourceConnection.setSendBufferSize(8192);
        SourceConnectionInputStream = SourceConnection.getInputStream();
        SourceConnectionOutputStream = SourceConnection.getOutputStream();
	}
	
	@Override
	protected void DisconnectSource() throws IOException {
		SourceConnectionOutputStream.close();
		SourceConnectionInputStream.close();
		SourceConnection.close();
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
	
	@Override
	public void DoReceiving(Thread ReceivingThread) throws IOException {
		byte[] TransferBuffer = new byte[TransferBufferSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = SourceConnectionInputStream.read(TransferBuffer,0,TransferBuffer.length);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size != 4/*SizeOf(Descriptor)*/)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) {
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Size = InputStream_Read(SourceConnectionInputStream,TransferBuffer,Size);	
                if (Size <= 0) 
                	break; //. >
				DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
			}
		}
	}
	
	@Override
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
		byte[] TransferBuffer = new byte[TransferBufferSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = DestinationConnectionInputStream.read(TransferBuffer,0,4/*SizeOf(Descriptor)*/);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size != 4/*SizeOf(Descriptor)*/)
				throw new IOException("wrong data descrptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) {
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Size = InputStream_Read(DestinationConnectionInputStream,TransferBuffer,Size);	
                if (Size <= 0) 
                	break; //. >
                SourceConnectionOutputStream.write(TransferBuffer,0,Size);
			}
		}
	}	
}
