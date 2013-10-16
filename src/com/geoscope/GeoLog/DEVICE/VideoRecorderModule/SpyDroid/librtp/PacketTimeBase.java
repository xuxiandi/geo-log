package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;


public class PacketTimeBase {

	public static long TimeBase = 0; //. microseconds
	
	public static void Set() {
		TimeBase = System.nanoTime()/1000;
	}
}
