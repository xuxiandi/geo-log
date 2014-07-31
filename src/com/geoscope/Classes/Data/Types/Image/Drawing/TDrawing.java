package com.geoscope.Classes.Data.Types.Image.Drawing;

import java.io.IOException;

import android.graphics.Canvas;

public class TDrawing {

	public static class TRectangle {
		
		public float Xmn;
		public float Ymn;
		public float Xmx;
		public float Ymx;
		
		public float Width() {
			return (Xmx-Xmn);
		}

		public float Height() {
			return (Ymx-Ymn);
		}
	}
	
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
	
	public TDrawingNode GetAveragePosition() {
		return null;
	}
	
	public TRectangle GetRectangle() {
		return null;
	}
	
	public byte[] ToByteArray() throws IOException {
		return null;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		return Idx;
	}
}
