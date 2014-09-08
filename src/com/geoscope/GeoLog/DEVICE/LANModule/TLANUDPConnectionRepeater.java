package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class TLANUDPConnectionRepeater extends TUDPConnectionRepeater {

	public static final int SourceConnectionTimeout = 1000*30; //. seconds
	public static final int MaxTransferPacketSize = 1024*1024; 

	private int ReceivingPort;
	private int ReceivingPacketSize;
	//.
	private String 	SourceAddress;
	private int 	SourceTransmittingPort;
	private int		SourceTransmittingPacketSize;
    
	public TLANUDPConnectionRepeater(TLANModule pLANModule, int pReceivingPort, int pReceivingPacketSize, String pSourceAddress, int pSourceTransmittingPort, int pSourceTransmittingPacketSize, String pDestinationAddress, int pDestinationPort, int pConnectionID) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
		//.
		ReceivingPort = pReceivingPort;
		ReceivingPacketSize = pReceivingPacketSize;
		//.
		SourceAddress = pSourceAddress;
		SourceTransmittingPort = pSourceTransmittingPort;
		SourceTransmittingPacketSize = pSourceTransmittingPacketSize;
		//. cancel the same repeaters
    	ArrayList<TLANUDPConnectionRepeater> RepeatersToCancel = new ArrayList<TLANUDPConnectionRepeater>(1);
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++) {
        		TUDPConnectionRepeater CR = TUDPConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TLANUDPConnectionRepeater) && ((((TLANUDPConnectionRepeater)CR).GetReceivingPort() == ReceivingPort)))
        			RepeatersToCancel.add(((TLANUDPConnectionRepeater)CR));
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
					continue; //. ^
				}
				if (Size > 0) {
					DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
				}
			}
		}
		finally {
			UDPSocket.close();
		}
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
	                Size = DestinationConnectionInputStream.read(TransferBuffer,0,TransferBuffer.length);
	                if (Size <= 0) 
	                	break; //. >
				}
				catch (SocketTimeoutException E) {
					continue; //. ^
				}
				if (Size > 0) {
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
