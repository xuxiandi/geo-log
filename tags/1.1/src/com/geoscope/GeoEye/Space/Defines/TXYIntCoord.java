package com.geoscope.GeoEye.Space.Defines;

public class TXYIntCoord {

	public int X;
	public int Y;
	
	public boolean IsTheSame(TXYIntCoord A) {
		return ((A.X == X) && (A.Y == Y));
	}
	
	public TXYIntCoord Clone() {
		TXYIntCoord Result = new TXYIntCoord();
		Result.X = X;
		Result.Y = Y;
		return Result;
	}
}
