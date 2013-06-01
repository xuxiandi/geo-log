package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TLANConnectionRepeater extends TConnectionRepeater {

	public static final int SourceConnectionTimeout = 1000*30; //. seconds

	private String 	SourceAddress;
	private int 	SourcePort;
	//.
    private Socket 			SourceConnection;
    private InputStream 	SourceConnectionInputStream;
    private OutputStream 	SourceConnectionOutputStream;
    
	public TLANConnectionRepeater(TLANModule pLANModule, String pSourceAddress, int pSourcePort, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
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
	
	@Override
	public void DoReceiving() throws IOException {
		byte[] TransferBuffer = new byte[TransferBufferSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = SourceConnectionInputStream.read(TransferBuffer,0,TransferBufferSize);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size > 0)
				DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
		}
	}
	
	@Override
	public void DoTransmitting() throws IOException {
		byte[] TransferBuffer = new byte[TransferBufferSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = DestinationConnectionInputStream.read(TransferBuffer,0,TransferBufferSize);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size > 0)
                SourceConnectionOutputStream.write(TransferBuffer,0,Size);
		}
	}	
}
