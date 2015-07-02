package com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC;

import java.io.IOException;

public class TAACADTSDecoder extends TAACDecoder {

	private boolean flDecoderConfiguration = false;
	
	public TAACADTSDecoder(int SampleRate) throws IOException {
		super(SampleRate);
	}
	
	public void DoOnAACADTSPAcket(byte[] ADTSPacket, int ADTSPacketOffset, int ADTSPacketSize) throws IOException {
		if (!flDecoderConfiguration) {
			flDecoderConfiguration = true;
			//.
			byte ObjectType = (byte)(((ADTSPacket[ADTSPacketOffset+2] & 0xFF) >> 6)+1); 
			byte FrequencyIndex = (byte)((ADTSPacket[ADTSPacketOffset+2] & 0x3C) >> 2);
			byte ChannelConfiguration = (byte)(((ADTSPacket[ADTSPacketOffset+2] & 0x01) << 2) | ((ADTSPacket[ADTSPacketOffset+3] & 0xC0) >> 6));
			//.
			byte[] ConfigWordBA = new byte[2];
			ConfigWordBA[0] = (byte)(((ObjectType << 3) & 0xF8) | ((FrequencyIndex >> 1) & 0x07));
			ConfigWordBA[1] = (byte)(((FrequencyIndex & 0x01) << 7) | ((ChannelConfiguration & 0x0F) << 3));
			//.
			DoOnInputBuffer(ConfigWordBA, ConfigWordBA.length, 0);
			//.
			return; //.->
		}
		//.
		boolean flProtectionAbsent = ((ADTSPacket[1] & 1) != 0);
		int ADTSHeaderSize;
		if (flProtectionAbsent) 
			ADTSHeaderSize = 7;
		else
			ADTSHeaderSize = 9;
		//.
		DoOnInputBuffer(ADTSPacket, ADTSPacketOffset+ADTSHeaderSize,ADTSPacketSize-ADTSHeaderSize, 0);
	}
	
	
}
