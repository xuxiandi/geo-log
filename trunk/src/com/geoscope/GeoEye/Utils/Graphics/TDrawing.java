package com.geoscope.GeoEye.Utils.Graphics;

import java.io.IOException;

import android.graphics.Canvas;

public class TDrawing {

	public static TDrawing CreateInstance(short TypeID) {
		switch (TypeID) {
		
		case TLineDrawing.TYPE_ID:
			return (new TLineDrawing()); //. ->
			
		case TPictureDrawing.TYPE_ID:
			return (new TPictureDrawing()); //. ->
			
		default:
			return null; //. ->
		}
	}
	
	public void Destroy() {
	}

	public short TypeID() {
		return 0;
	}
	
	public void Paint(Canvas pCanvas) {
	}
	
	public void Translate(float dX, float dY) {
	}
	
	public byte[] ToByteArray() throws IOException {
		return null;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		return Idx;
	}
}
