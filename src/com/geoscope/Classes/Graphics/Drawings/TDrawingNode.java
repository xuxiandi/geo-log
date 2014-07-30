package com.geoscope.Classes.Graphics.Drawings;

import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;

public class TDrawingNode {
	 
	public float X = 0.0F;
	public float Y = 0.0F;
	
	public TDrawingNode(float pX, float pY) {
		X = pX;
		Y = pY;
	}
	
	public TDrawingNode() {
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[8/*X*/+8/*Y*/];
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(X);
		int Idx = 0;
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y);
		System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		X = (float)TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
		Y = (float)TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
		return Idx;
	}
}
