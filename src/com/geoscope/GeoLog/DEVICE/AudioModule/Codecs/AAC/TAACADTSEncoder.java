package com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC;

import java.io.IOException;

public class TAACADTSEncoder extends TAACEncoder {
	
	private static final int ADTSHeaderLength = 7;
	 
	private boolean flConfigIsArrived = false;
	private byte ObjectType;
	private byte FrequencyIndex;
	private byte ChannelConfiguration;
	//.
	private byte[] ADTSHeader = new byte[ADTSHeaderLength];
	private byte[] ADTSPacket = new byte[1024];
	
	public TAACADTSEncoder(int BitRate, int SampleRate) {
		super(BitRate, SampleRate);
	}
	
	@Override
	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws Exception {
		if (!flConfigIsArrived) {
			flConfigIsArrived = true;
			//. 
			if (BufferSize < 2) 
				throw new IOException("invalid AAC codec configuration data"); //. =>
	        ObjectType = (byte)(Buffer[0] >> 3);
        	FrequencyIndex = (byte)(((Buffer[0] & 7) << 1) | ((Buffer[1] >> 7) & 0x01));
        	ChannelConfiguration = (byte)((Buffer[1] >> 3) & 0x0F);
			return; //. ->
		}
		//.
		int ADTSPacketSize = ADTSHeader.length+BufferSize;
		ADTSHeader[0] = (byte)0xFF/*SyncWord*/;
		ADTSHeader[1] = (byte)((0x0F << 4)/*SyncWord*/ | (0 << 3)/*MPEG-4*/ | (0 << 1)/*Layer*/ | 1/*ProtectionAbsent*/);
		ADTSHeader[2] = (byte)(((ObjectType-1) << 6)/*Profile*/ | ((FrequencyIndex & 0x0F) << 2)/*SamplingFrequencyIndex*/ | (0 << 1)/*PrivateStream*/ | ((ChannelConfiguration >> 2) & 0x01)/*ChannelConfiguration*/);
		ADTSHeader[3] = (byte)(((ChannelConfiguration & 3) << 6)/*ChannelConfiguration*/ | (0 << 5)/*Originality*/ | (0 << 4)/*Home*/ | (0 << 3)/*CopyrightedStream*/ | (0 << 2)/*CopyrightStart*/ | ((ADTSPacketSize >> 11) & 3)/*FrameLength*/);
		ADTSHeader[4] = (byte)((ADTSPacketSize >> 3) & 0xFF)/*FrameLength*/;
		ADTSHeader[5] = (byte)((ADTSPacketSize & 7) << 5)/*FrameLength*//*5 bits of BufferFullness*/;
		ADTSHeader[6] = (byte)/*6 bits of BufferFullness*/+(0)/*Number of AAC frames - 1*/;
		//.
		if (ADTSPacketSize > ADTSPacket.length)
			ADTSPacket = new byte[ADTSPacketSize];
		System.arraycopy(ADTSHeader,0, ADTSPacket,0, ADTSHeaderLength);
		System.arraycopy(Buffer,0, ADTSPacket,ADTSHeaderLength, BufferSize);
		//.
		DoOnOutputPacket(ADTSPacket, ADTSPacketSize);
	}
	
	public void DoOnOutputPacket(byte[] Packet, int PacketSize) throws Exception {
	}
}
 
