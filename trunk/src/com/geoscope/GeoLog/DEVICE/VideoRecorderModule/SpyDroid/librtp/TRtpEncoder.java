package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TRtpEncoder {

	public TRtpPacket 	RtpPacket;
	private byte[] 		RtpPacketBuffer;
	
	public TRtpEncoder(String Address, int Port) throws UnknownHostException {
		RtpPacket = new TRtpPacket(InetAddress.getByName(Address),Port);
		RtpPacketBuffer = RtpPacket.getBuffer();
	}
	
	public void DoOnInput(byte[] InputBuffer, int InputBufferSize, int RtpTimestamp) throws IOException {
		RtpPacket.updateTimestamp(RtpTimestamp);
		//.
		int Offset = 0;
		while (InputBufferSize > 0) {
			int Portion = InputBufferSize;
			if (Portion > RtpBuffer.MAXDATASIZE)
				Portion = RtpBuffer.MAXDATASIZE;
			//.
			System.arraycopy(InputBuffer,Offset, RtpPacketBuffer,TRtpPacket.RTP_HEADER_LENGTH, Portion);
			RtpPacket.setBufferLength(TRtpPacket.RTP_HEADER_LENGTH+Portion);
			//.
			DoOnOutput(RtpPacket);
			//.
			InputBufferSize -= Portion;
			Offset += Portion;
		}
	}
	
	public void DoOnOutput(TRtpPacket OutputPacket) throws IOException {		
	}
}
