package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

public class MediaFrameServer {

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
	
	//. video frame output
	public static boolean 	flVideoActive = false;
	public static int 		FrameRate = 0;
	public static int 		FrameInterval = 0;
	public static TFrame 	CurrentFrame = new TFrame();
	
}
