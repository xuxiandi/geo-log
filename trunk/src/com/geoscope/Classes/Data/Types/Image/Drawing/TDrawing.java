package com.geoscope.Classes.Data.Types.Image.Drawing;

import java.io.IOException;

import android.graphics.Canvas;

public class TDrawing {

	public static class TRectangle {
		
		public float Xmn;
		public float Ymn;
		public float Xmx;
		public float Ymx;
		
		public TRectangle() {
		}
		
		public TRectangle(float pXmn, float pYmn, float pXmx, float pYmx) {
			Xmn = pXmn;
			Ymn = pYmn;
			Xmx = pXmx;
			Ymx = pYmx;
		}
		
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
			
		case TImageDrawing.TYPE_ID:
			return (new TImageDrawing()); //. ->
			
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
	
	public void Scale(float X0, float Y0, float Scale) {
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
