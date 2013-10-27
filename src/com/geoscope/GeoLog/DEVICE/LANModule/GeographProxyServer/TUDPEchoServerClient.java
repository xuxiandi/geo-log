package com.geoscope.GeoLog.DEVICE.LANModule.GeographProxyServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater.TInternetUDPEndpoint;
import com.geoscope.Utils.TUIDGenerator;

public class TUDPEchoServerClient {

	public static final int ServerDefaultPort = 2014;
	//.
	public static final int MTU_MAX_SIZE = 1500;
	
	private String 	GeographProxyServerAddress;
	public int 		GeographProxyServerUDPEchoServerPort = 2014;
	
	public TUDPEchoServerClient(String pGeographProxyServerAddress, int pGeographProxyServerUDPEchoServerPort) {
		GeographProxyServerAddress = pGeographProxyServerAddress;
		//.
		GeographProxyServerUDPEchoServerPort = pGeographProxyServerUDPEchoServerPort;
	}
	
	public TInternetUDPEndpoint GetInternetEndpoint(int LocalPort) throws IOException {
		int TryCount = 3;
		DatagramSocket _Socket = new DatagramSocket(LocalPort,InetAddress.getByName("0.0.0.0"));
		try {
			String UID = TUIDGenerator.Generate();
			byte[] UIDBA = UID.getBytes("US-ASCII");
			//.
			byte[] SendPacketBuffer = new byte[2/*SizeOf(Version)*/+UIDBA.length];
			SendPacketBuffer[0] = 1; //. Version
			System.arraycopy(UIDBA,0, SendPacketBuffer,2, UIDBA.length);
			//.
			DatagramPacket SendPacket = new DatagramPacket(SendPacketBuffer,SendPacketBuffer.length,InetAddress.getByName(GeographProxyServerAddress),GeographProxyServerUDPEchoServerPort);
			//.
			byte[] ReceivePacketBuffer = new byte[MTU_MAX_SIZE];
			DatagramPacket ReceivePacket = new DatagramPacket(ReceivePacketBuffer,ReceivePacketBuffer.length);
			//.
			_Socket.setSoTimeout(2000);
			for (int I = 0; I < TryCount; I++) {
				_Socket.send(SendPacket); 				
				//. listen for answer
				while (true) {
					try {
						_Socket.receive(ReceivePacket);
						int ReceivePacketBufferSize = ReceivePacket.getLength();
						if (ReceivePacketBufferSize == (SendPacketBuffer.length+TInternetUDPEndpoint.ByteArraySize)) {
							boolean flMatched = true;
							for (int J = 0; J < SendPacketBuffer.length; J++)
								if (ReceivePacketBuffer[J] != SendPacketBuffer[J]) {
									flMatched = false;
									break; //. >
								}
							if (flMatched) {
								TInternetUDPEndpoint IE = new TInternetUDPEndpoint(ReceivePacketBuffer,SendPacketBuffer.length);
								IE.Socket = _Socket;
								_Socket = null;
								return IE; //. ->
							}
						}
					}
					catch (SocketTimeoutException STE) {
						break; //. >
					};
				}
			}
			return null; //. ->
		}
		finally {
			if (_Socket != null)
				_Socket.close();
		}
	}
}
