package com.geoscope.Classes.IO.Protocols.RTP;

import java.io.IOException;

public class TRTPDecoder {

	public static final int TheBufferInitialSize = 1024*256;
	
	private byte[] 	TheBuffer = new byte[TheBufferInitialSize];
	private int		TheBufferLength = 0;
	//.
	private int LastRtpSequence = -1;
	private int LastRtpTimestamp = 0;
	
	public void DoOnInput(TRTPPacket InputPacket) throws IOException {
		int Seq = InputPacket.getSequence();
		if (Seq == LastRtpSequence)
			return; //. ->
		LastRtpSequence = Seq;
		//.
		int RtpTimestamp = InputPacket.getTimestamp();
		if (RtpTimestamp != LastRtpTimestamp) {
			if (LastRtpTimestamp != 0)
				DoOnOutput(TheBuffer,TheBufferLength, LastRtpTimestamp);
			TheBufferLength = 0;
			LastRtpTimestamp = RtpTimestamp;
		}
		byte[] Buffer = InputPacket.buffer;
		int BufferLength = InputPacket.getBufferLength()-TRTPPacket.RTP_HEADER_LENGTH;
		if (BufferLength > 0) {
			int NewLength = TheBufferLength+BufferLength;
			if (NewLength > TheBuffer.length) {
				byte[] _TheBuffer = new byte[NewLength];
				if (TheBufferLength > 0)
					System.arraycopy(TheBuffer,0, _TheBuffer,0, TheBufferLength);
				TheBuffer = _TheBuffer;
			}
			System.arraycopy(Buffer,TRTPPacket.RTP_HEADER_LENGTH, TheBuffer,TheBufferLength, BufferLength);
			TheBufferLength = NewLength;
		}
	}
	
	public void DoOnOutput(byte[] OutputBuffer, int OutputBufferSize, int RtpTimestamp) throws IOException {
	}
}
