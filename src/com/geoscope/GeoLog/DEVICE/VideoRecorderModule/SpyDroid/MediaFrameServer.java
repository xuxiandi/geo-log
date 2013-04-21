package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import android.hardware.Camera.Size;

public class MediaFrameServer {

	public static class TSamplePacket {
		
		public static long CurrentTimestamp = 0;
		
		public double 	Timestamp = 0.0;
		public int 		Format = 0;
		public byte[] 	Data = null;
		public int		DataSize = 0;
		
		public synchronized void Set(byte[] pData, int pDataSize) {
			Timestamp = CurrentTimestamp; 
			CurrentTimestamp++;
			//.
			Data = pData;
			DataSize = pDataSize;
			//.
			this.notifyAll();
		}
	}
	
	public static class TFrame {
		
		public static long CurrentTimestamp = 0;
		
		public double 	Timestamp = 0.0;
		public int		Width = 0;
		public int		Height = 0;
		public int 		Format = 0;
		public byte[] 	Data = null;
		
		public synchronized void Set(int pWidth, int pHeight, int pFormat, byte[] pData) {
			Timestamp = CurrentTimestamp; 
			CurrentTimestamp++;
			//.
			Width = pWidth;
			Height = pHeight;
			Format = pFormat;
			Data = pData;
			//.
			this.notifyAll();
		}
	}
	
	//. audio sample output
	public static boolean 		flAudioActive = false;
	public static int 			SampleRate = 0;
	public static int 			SamplePacketInterval = 0;
	public static int 			SampleBitRate = 0;
	public static TSamplePacket CurrentSamplePacket = new TSamplePacket();
	
	//. video frame output
	public static boolean 	flVideoActive = false;
	public static Size 		FrameSize = null;
	public static int 		FrameRate = 0;
	public static int 		FrameInterval = 0;
	public static int 		FrameBitRate = 0;
	public static TFrame 	CurrentFrame = new TFrame();
	
}
