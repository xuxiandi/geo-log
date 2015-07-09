package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;

public class TRtpPacket extends TRtpBuffer {

    private DatagramPacket upack;

    public TRtpPacket(InetAddress dest, int dport) {
		super();
        //.
		upack = new DatagramPacket(buffer,1,dest,dport);
		//.
		SupplyWithRTPHeader();
    }

    public TRtpPacket(String ProxyServerAddress, int ProxyServerPort, String DestinationAddress, int DestinationPort) throws UnknownHostException {
    	super();
        //.
		buffer_offset = TUDPEchoServerClient.PROXY_IPV4_SupplyPacketWithHeader(buffer, DestinationAddress, DestinationPort);
		upack = new DatagramPacket(buffer,1,InetAddress.getByName(ProxyServerAddress),ProxyServerPort);
		//.
		SupplyWithRTPHeader();
    }

	/* Send RTP packet over the network */
	public void sendTo(DatagramSocket Socket) throws IOException {
		
		updateSequence();
		upack.setLength(buffer_length);
		
		Socket.send(upack);
		
		if (upts) {
			upts = false;
			buffer[buffer_offset+1] -= 0x80;
		}
		
	}
	
	public void sendToAgain(DatagramSocket Socket) throws IOException {
		Socket.send(upack);
	}
}
