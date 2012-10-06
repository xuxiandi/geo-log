package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import android.graphics.Bitmap;
import android.graphics.Color;

public class TTile {
	
	public static final int TileSize = 256;
	public static final int TransparentPNGSize = 741;
	
	public static String TileFileName(int X, int Y) {
		return "X"+Integer.toString(X)+"Y"+Integer.toString(Y)+".png";
	}
	
	public static int TileHashCode(int X, int Y) {
		return ((Y << 16) | (X & 0x0000FFFF));
	}
	
	public static boolean Data_IsTransparent(int PNGSize, Bitmap Data) {
		return ((Data == null) || ((PNGSize == TransparentPNGSize) && (Data.getPixel(0,0) == Color.TRANSPARENT)));
	}
	
	public TTile Pred = null;
	public TTile Next = null;
	//.
	public int 		X;
	public int 		Y;
	public double 	Timestamp;
	public Bitmap 	Data;
	public boolean	Data_flTransparent;
	private long _AccessTime = 0;
	
	public TTile(int pX, int pY, double pTimestamp, Bitmap pData, boolean pData_flTransparent) {
		X = pX;
		Y = pY;
		Timestamp = pTimestamp;
		Data = pData;
		Data_flTransparent = pData_flTransparent;
	}
	
	public void Finalize() {
		if (Data != null) {
			Data.recycle();
			Data = null;
		}
	}
	
	public String TileFileName() {
		return TileFileName(X,Y);
	}

	public int TileHashCode() {
		return TileHashCode(X,Y);
	}
	
	public synchronized long AccessTime() {
		return _AccessTime;
	}
	
	public synchronized void SetAsAccessed() {
		_AccessTime = System.currentTimeMillis();
	}
}

