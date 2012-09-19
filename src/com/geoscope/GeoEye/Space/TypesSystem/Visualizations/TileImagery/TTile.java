package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

public class TTile {
	
	public static final String TileFileType = "png";
	public static final int TileSize = 256;
	public static final int TransparentTileSize = 741;
	
	public static String TileFileName(int X, int Y) {
		return "X"+Integer.toString(X)+"Y"+Integer.toString(Y)+"."+TileFileType;
	}
	
	public static String TileHistoryFolderName(int X, int Y) {
		return "X"+Integer.toString(X)+"Y"+Integer.toString(Y);
	}
	
	public static int TileHashCode(int X, int Y) {
		return ((Y << 16) | (X & 0x0000FFFF));
	}
	
	public static boolean Data_IsTransparent(int FileDataSize, Bitmap Data) {
		return ((Data == null) || ((FileDataSize == TransparentTileSize) && (Data.getPixel(0,0) == Color.TRANSPARENT)));
	}
	
	private static int[] DataPixels = new int[TileSize*TileSize]; 
	
	public TTile Pred = null;
	public TTile Next = null;
	//.
	public int 		X;
	public int 		Y;
	public double 	Timestamp;
	public Bitmap 	Data;
	public boolean	Data_flTransparent;
	public boolean	Data_flMutable;
	private long _AccessTime = 0;
	public boolean flModified = false;
	
	public TTile(int pX, int pY, double pTimestamp, Bitmap pData, boolean pData_flTransparent) {
		X = pX;
		Y = pY;
		Timestamp = pTimestamp;
		Data = pData;
		Data_flTransparent = pData_flTransparent;
		Data_flMutable = false;
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

	public String TileHistoryFolder_Name() {
		return TileHistoryFolderName(X,Y);
	}

	public String TileHistoryFolder_TileFileName() {
		return (Double.toString(Timestamp)+"."+TileFileType);
	}

	public int TileHashCode() {
		return TileHashCode(X,Y);
	}
	
	public long DataHashCode() {
		if ((Data == null) || Data_flTransparent)
			return 0; //. ->
		long hash = 0;
		synchronized (DataPixels) {
			Data.getPixels(DataPixels, 0, Data.getWidth(), 0,0, Data.getWidth(),Data.getHeight());
			int Size = DataPixels.length;
			long V;
			for (int I = 0; I < Size; I++) {
	            V = (hash+DataPixels[I]);
	            if (V < 0)
	            	hash = ((V << 1)^V)+1;
	            else 
	            	hash = ((V << 1)^V);
			}
		}
		return hash;
	}	
	
	public synchronized long AccessTime() {
		return _AccessTime;
	}
	
	public synchronized void SetAsAccessed() {
		_AccessTime = System.currentTimeMillis();
	}
	
	public synchronized boolean IsTransparent() {
		return Data_flTransparent;
	}
	
	public synchronized void CheckTransparency() {
		if (Data == null) {
			Data_flTransparent = true;
			return; //. ->
		}
		int W = Data.getWidth();
		int H = Data.getHeight();
		for (int X = 0; X < W; X++) 
		    for (int Y = 0; Y < H; Y++)
		    	if (Data.getPixel(X,Y) != Color.TRANSPARENT) {
					Data_flTransparent = false;
					return; //. ->
				}
		Data_flTransparent = true;
	}
	
	public synchronized Bitmap CreateTransparent() {
		Bitmap Result = Bitmap.createBitmap(TileSize,TileSize,Config.ARGB_8888);
		Data = Result;
		//.
		Data_flTransparent = true;
		Data_flMutable = false;
		//.
		return Result;
	}
	
	public synchronized boolean IsMutable() {
		return Data_flMutable;
	}
	
	public synchronized void SetMutable(boolean Value) {
		if (Data_flMutable == Value) 
			return; //. ->
		if (Data != null) {
			Bitmap _Data = Data.copy(Config.ARGB_8888 ,Value);
			Bitmap _RemoveData = Data; 
			Data = _Data;
			_RemoveData.recycle();
		}
		Data_flMutable = Value;
	}
	
	public synchronized boolean IsModified() {
		return flModified;
	}
	
	public synchronized void SetModified(boolean Value) {
		flModified = Value;
	}
}

