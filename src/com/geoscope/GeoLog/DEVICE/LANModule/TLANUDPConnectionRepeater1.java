package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class TLANUDPConnectionRepeater1 extends TUDPConnectionRepeater {

	public static final int SourceConnectionTimeout = 1000*30; //. seconds
	public static final int MaxTransferPacketSize = 1024*1024; 

	private int ReceivingPort;
	private int ReceivingPacketSize;
	//.
	private String 	SourceAddress;
	private int 	SourceTransmittingPort;
	private int		SourceTransmittingPacketSize;
    
	public TLANUDPConnectionRepeater1(TLANModule pLANModule, int pReceivingPort, int pReceivingPacketSize, String pSourceAddress, int pSourceTransmittingPort, int pSourceTransmittingPacketSize, String pDestinationAddress, int pDestinationPort, int pConnectionID) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
		//.
		ReceivingPort = pReceivingPort;
		ReceivingPacketSize = pReceivingPacketSize;
		//.
		SourceAddress = pSourceAddress;
		SourceTransmittingPort = pSourceTransmittingPort;
		SourceTransmittingPacketSize = pSourceTransmittingPacketSize;
		//. cancel the same repeaters
    	ArrayList<TLANUDPConnectionRepeater1> RepeatersToCancel = new ArrayList<TLANUDPConnectionRepeater1>(1);
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++) {
        		TUDPConnectionRepeater CR = TUDPConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TLANUDPConnectionRepeater1) && ((((TLANUDPConnectionRepeater1)CR).GetReceivingPort() == ReceivingPort)))
        			RepeatersToCancel.add(((TLANUDPConnectionRepeater1)CR));
        	}
		}
    	for (int I = 0; I < RepeatersToCancel.size(); I++)
    		RepeatersToCancel.get(I).CancelAndWait();
		//.
		Start();
	}
	
	public int GetReceivingPort() {
		return ReceivingPort;
	}
	
	@Override
	protected void ConnectSource() throws IOException {
	}
	
	@Override
	protected void DisconnectSource() throws IOException {
	}	
	
	@Override
	public boolean ReceivingIsAvaiable() {
		return (ReceivingPort > 0);
	}
	
	@Override
	public boolean TransmittingIsAvaiable() {
		return (SourceTransmittingPort > 0);
	}
	
	@Override
	public void DoReceiving() throws IOException {
		int BufferSize = TransferBufferSize;
		if ((ReceivingPacketSize > 0) && (ReceivingPacketSize <= MaxTransferPacketSize))
			BufferSize = ReceivingPacketSize;
		byte[] TransferBuffer = new byte[BufferSize];
		byte[] DataDescriptor = new byte[4];
		int Size;
		DatagramSocket UDPSocket = new DatagramSocket(ReceivingPort);
		try {
			UDPSocket.setSoTimeout(100);
			//.
			DatagramPacket UDPPacket = new DatagramPacket(TransferBuffer,TransferBuffer.length);
			while (!Canceller.flCancel) {
				try {
					UDPSocket.receive(UDPPacket);
					Size = UDPPacket.getLength();
					//. 
					//. Log.d("Geo.Log", "Receive UDP ("+Integer.toString(ReceivingPort)+"), Packet: "+Integer.toString(Size));
				}
				catch (SocketTimeoutException E) {
					Size = 0;
				}
				if (Size > 0) {
					DataDescriptor[0] = (byte)(Size & 0xff);
					DataDescriptor[1] = (byte)(Size >> 8 & 0xff);
					DataDescriptor[2] = (byte)(Size >> 16 & 0xff);
					DataDescriptor[3] = (byte)(Size >>> 24);
					//.
					DestinationConnectionOutputStream.write(DataDescriptor,0,DataDescriptor.length);
					DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
				}
			}
		}
		finally {
			UDPSocket.close();
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
	
	@Override
	public void DoTransmitting() throws IOException {
		int BufferSize = TransferBufferSize;
		if ((SourceTransmittingPacketSize > 0) && (SourceTransmittingPacketSize <= MaxTransferPacketSize))
			BufferSize = SourceTransmittingPacketSize;
		byte[] TransferBuffer = new byte[BufferSize];
		int Size;
		DatagramSocket UDPSocket = new DatagramSocket();
		try {
			DatagramPacket UDPPacket = new DatagramPacket(TransferBuffer,1,InetAddress.getByName(SourceAddress),SourceTransmittingPort);
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
					throw new IOException("wrong data descriptor"); //. =>
				Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
				if (Size > 0) {
					if (Size > TransferBuffer.length)
						TransferBuffer = new byte[Size];
					Size = InputStream_Read(DestinationConnectionInputStream,TransferBuffer,Size);	
	                if (Size <= 0) 
	                	break; //. >
					UDPPacket.setLength(Size);
					UDPSocket.send(UDPPacket);				
				}
			}
		}
		finally {
			UDPSocket.close();
		}
	}	
}
