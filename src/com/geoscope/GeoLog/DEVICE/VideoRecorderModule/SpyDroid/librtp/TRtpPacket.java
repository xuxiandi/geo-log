package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TRtpPacket extends TRtpBuffer {

    private DatagramPacket upack;

    public TRtpPacket(InetAddress dest, int dport) {
    		super();
            //.
    		upack = new DatagramPacket(buffer,1,dest,dport);
    }

	/* Send RTP packet over the network */
	public void sendTo(DatagramSocket Socket) throws IOException {
		
		updateSequence();
		upack.setLength(buffer_length);
		
		Socket.send(upack);
		
		if (upts) {
			upts = false;
			buffer[1] -= 0x80;
		}
		
	}
}
