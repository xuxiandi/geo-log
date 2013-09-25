package com.geoscope.GeoEye.Space;

import android.os.Environment;
import android.os.StatFs;

public class TSpaceContextStorage {

	public static final double MaxDeviceFillFactor = 0.9;
	public static final double MaxDeviceFillFactorForRemovingOldItems = 0.7;
	
	public static String DevicePath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();	
	}
	
	public double DeviceFillFactor() {
	    StatFs SDstat = new StatFs(Environment.getExternalStorageDirectory().getPath());
	    long BC = (long)SDstat.getBlockCount();
	    long ABC = (long)SDstat.getAvailableBlocks();
	    return (0.0+(BC-ABC))/BC;
	}
	
	public boolean CheckDeviceFillFactor() {
		return (DeviceFillFactor() <= MaxDeviceFillFactor);
	}

	public boolean CheckDeviceFillFactorForRemovingOldItems() {
		return (DeviceFillFactor() <= MaxDeviceFillFactorForRemovingOldItems);
	}
}
