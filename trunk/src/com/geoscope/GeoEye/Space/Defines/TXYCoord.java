package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Utils.TDataConverter;

public class TXYCoord {
	
	public double X;
	public double Y;
	
	public TXYCoord(double pX, double pY) {
		X = pX;
		Y = pY;
	}
	
	public TXYCoord() {
		this(0.0,0.0);
	}
	
	public boolean IsTheSame(TXYCoord A) {
		return ((A.X == X) && (A.Y == Y));
	}
	
	public TXYCoord Clone() {
		TXYCoord Result = new TXYCoord();
		Result.X = X;
		Result.Y = Y;
		return Result;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
	    X = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Y = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    //.
	    return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[8+8];
		//.
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(X);
		System.arraycopy(BA,0, Result,0, BA.length);
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y);
		System.arraycopy(BA,0, Result,8, BA.length);
		//.
		return Result;
	}
}

