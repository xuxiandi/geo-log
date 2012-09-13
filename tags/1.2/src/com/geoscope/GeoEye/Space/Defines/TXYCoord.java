package com.geoscope.GeoEye.Space.Defines;

public class TXYCoord {
	public double X;
	public double Y;
	
	public boolean IsTheSame(TXYCoord A) {
		return ((A.X == X) && (A.Y == Y));
	}
	
	public TXYCoord Clone() {
		TXYCoord Result = new TXYCoord();
		Result.X = X;
		Result.Y = Y;
		return Result;
	}
}

