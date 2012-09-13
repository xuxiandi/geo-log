package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import android.os.SystemClock;

public class PacketTimeBase {

	public static long TimeBase = 0;
	
	public static void Set() {
		TimeBase = SystemClock.elapsedRealtime();
	}
}
