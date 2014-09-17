package com.geoscope.Classes.IO.Protocols.RTP;

import java.io.IOException;
import java.net.UnknownHostException;

public class TRTPEncoder {

	public TRTPPacket 	RTPPacket;
	private byte[] 		RTPPacketBuffer;
	private int			RTPPacketBufferOffset;
	
	public TRTPEncoder(int RTPPacketOffet) throws UnknownHostException {
		RTPPacket = new TRTPPacket(RTPPacketOffet);
		//.
		RTPPacketBuffer = RTPPacket.buffer;
		RTPPacketBufferOffset = RTPPacket.buffer_offset; 
	}
	
	public TRTPEncoder() throws UnknownHostException {
		this(0);
	}
	
	public void DoOnInput(byte[] InputBuffer, int InputBufferSize, int RtpTimestamp) throws IOException {
		RTPPacket.updateTimestamp(RtpTimestamp);
		//.
		int Offset = 0;
		while (InputBufferSize > 0) {
			int Portion = InputBufferSize;
			if (Portion > TRTPPacket.MAXDATASIZE)
				Portion = TRTPPacket.MAXDATASIZE;
			//.
			System.arraycopy(InputBuffer,Offset, RTPPacketBuffer,RTPPacketBufferOffset+TRTPPacket.RTP_HEADER_LENGTH, Portion);
			RTPPacket.setBufferLength(RTPPacketBufferOffset+TRTPPacket.RTP_HEADER_LENGTH+Portion);
			//.
			DoOnOutput(RTPPacket);
			//.
			InputBufferSize -= Portion;
			Offset += Portion;
		}
	}
	
	public void DoOnOutput(TRTPPacket OutputPacket) throws IOException {		
	}
}
