package com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.Identification.TUIDGenerator;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater.TInternetUDPEndpoint;

public class TUDPEchoServerClient {

	public static final int ServerDefaultPort = 2014;
	//.
	public static final int MTU_MAX_SIZE = 1500;
	//.
	public static final int ECHO_TYPE_ASYMMETRIC 	= 0;
	public static final int ECHO_TYPE_SYMMETRIC 	= 1;
	//.
	public static final int PROXY_TYPE_UNKNOWN 	= -1;
	public static final int PROXY_TYPE_NONE 	= 0;
	public static final int PROXY_TYPE_NATIVE 	= 1;
	//.
	public static final int PROXY_PACKET_HEADER_SIZE_IPV4 = 8;
	
	public static int PROXY_IPV4_SupplyPacketWithHeader(byte[] Packet, String DestinationAddress, int DestinationPort) {
		String[] SA = DestinationAddress.split("\\.");
		int Idx = 0;
		//. version of service (PROXY)
		Packet[Idx] = 3; Idx++;
		Packet[Idx] = 0; Idx++;
		//. destination address
		Packet[Idx] = (byte)(Integer.parseInt(SA[0]) & 0xFF); Idx++;
		Packet[Idx] = (byte)(Integer.parseInt(SA[1]) & 0xFF); Idx++;
		Packet[Idx] = (byte)(Integer.parseInt(SA[2]) & 0xFF); Idx++;
		Packet[Idx] = (byte)(Integer.parseInt(SA[3]) & 0xFF); Idx++;
		//. destination port
		Packet[Idx] = (byte)((DestinationPort >>> 8) & 0xFF); Idx++;
		Packet[Idx] = (byte)(DestinationPort & 0xFF); Idx++;
		return Idx;
	}
	
	private String 	GeographProxyServerAddress;
	public int 		GeographProxyServerUDPEchoServerPort = 2014;
	
	public TUDPEchoServerClient(String pGeographProxyServerAddress, int pGeographProxyServerUDPEchoServerPort) {
		GeographProxyServerAddress = pGeographProxyServerAddress;
		//.
		GeographProxyServerUDPEchoServerPort = pGeographProxyServerUDPEchoServerPort;
	}
	
	public static class TGetInternetEndpointResult {
		
		public TInternetUDPEndpoint 	Endpoint;
		public int						ProxyType;
		
		public TGetInternetEndpointResult(TInternetUDPEndpoint pEndpoint, int pProxyType) {
			Endpoint = pEndpoint;
			ProxyType = pProxyType;
		}
	}
	
	public TGetInternetEndpointResult GetInternetEndpoint() throws IOException {
		int TryCount = 3;
		//.
		String UID = TUIDGenerator.Generate();
		byte[] UIDBA = UID.getBytes("US-ASCII");
		//.
		byte[] SendPacketBuffer = new byte[2/*SizeOf(Version)*/+UIDBA.length];
		System.arraycopy(UIDBA,0, SendPacketBuffer,2, UIDBA.length);
		//.
		DatagramPacket SendPacket = new DatagramPacket(SendPacketBuffer,SendPacketBuffer.length,InetAddress.getByName(GeographProxyServerAddress),GeographProxyServerUDPEchoServerPort);
		//.
		byte[] ReceivePacketBuffer = new byte[MTU_MAX_SIZE];
		DatagramPacket ReceivePacket = new DatagramPacket(ReceivePacketBuffer,ReceivePacketBuffer.length);
		//.
		for (int I = 0; I < TryCount; I++) {
			int LocalPort = TConnectionUDPRepeater.GetUDPLocalPort();
			DatagramSocket _Socket = new DatagramSocket(LocalPort,InetAddress.getByName("0.0.0.0"));
			try {
				_Socket.setSoTimeout(2000);
				//. send ECHO_TYPE_ASYMMETRIC
				SendPacketBuffer[0] = 1; //. Version
				_Socket.send(SendPacket); 				
				//. send ECHO_TYPE_SYMMETRIC
				SendPacketBuffer[0] = 2; //. Version
				_Socket.send(SendPacket); 				
				//. listen for answer
				while (true) {
					try {
						_Socket.receive(ReceivePacket);
						int ReceivePacketBufferSize = ReceivePacket.getLength();
						if (ReceivePacketBufferSize == (SendPacketBuffer.length+TInternetUDPEndpoint.ByteArraySize)) {
							boolean flMatched = true;
							for (int J = 2/*skip Version*/; J < SendPacketBuffer.length; J++)
								if (ReceivePacketBuffer[J] != SendPacketBuffer[J]) {
									flMatched = false;
									break; //. >
								}
							if (flMatched) {
								TInternetUDPEndpoint Endpoint = new TInternetUDPEndpoint(LocalPort,ReceivePacketBuffer,SendPacketBuffer.length);
								Endpoint.Socket = _Socket;
								_Socket = null;
								//.
								int	ProxyType;
								switch (ReceivePacketBuffer[0]/*Version*/) {
								
								case 1:
									ProxyType = PROXY_TYPE_NONE;
									break; //. >
								
								case 2:
									ProxyType = PROXY_TYPE_NATIVE;
									break; //. >
								
								default:
									return null; //. ->
								}
								//. skip unused echo packet
								try {
									Endpoint.Socket.receive(ReceivePacket);
								}
								catch (SocketTimeoutException STE) {
								}
								//.
								return (new TGetInternetEndpointResult(Endpoint,ProxyType)); //. ->
							}
						}
					}
					catch (SocketTimeoutException STE) {
						break; //. >
					};
				}
			}
			finally {
				if (_Socket != null)
					_Socket.close();
			}
		}
		return null; //. ->
	}
}
