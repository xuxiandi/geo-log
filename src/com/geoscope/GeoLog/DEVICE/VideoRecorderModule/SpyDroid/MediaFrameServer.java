package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import android.hardware.Camera.Size;

public class MediaFrameServer {

	public static class TSamplePacket {
		
		public long 	Timestamp = 0;
		public int 		Format = 0;
		public byte[] 	Data = new byte[0];
		public int		DataSize = 0;
		
		public synchronized void Set(byte[] pData, int pDataSize) {
			Timestamp = System.nanoTime()/1000; 
			//.
			DataSize = pDataSize;
			if (DataSize > Data.length)
				Data = new byte[DataSize]; 
			System.arraycopy(pData,0, Data,0, DataSize);
			//.
			this.notifyAll();
		}
	}
	
	public static class TFrame {
		
		public long 	Timestamp = 0;
		public int		Width = 0;
		public int		Height = 0;
		public int 		Format = 0;
		public byte[] 	Data = new byte[0];
		public int		DataSize = 0;
		
		public synchronized void Set(int pWidth, int pHeight, int pFormat, byte[] pData, int pDataSize) {
			Timestamp = System.nanoTime()/1000; 
			//.
			Width = pWidth;
			Height = pHeight;
			Format = pFormat;
			//.
			DataSize = pDataSize;
			if (DataSize > Data.length)
				Data = new byte[DataSize]; 
			System.arraycopy(pData,0, Data,0, DataSize);
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
