package com.geoscope.GeoEye.Utils.Graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class TPictureDrawing extends TDrawing {
	
	private static Paint paint = new Paint();
	
	public Bitmap Picture;
	public TDrawingNode Node;
	
	public TPictureDrawing(Bitmap pPicture, float pX, float pY) {
		Picture = pPicture;
		Node = new TDrawingNode(pX,pY);
	}

	public void Destroy() {
		if (Picture != null) {
			Picture.recycle();
			Picture = null;
		}
	}

	public void Paint(Canvas pCanvas) {
		pCanvas.drawBitmap(Picture, Node.X,Node.Y, paint);
	}
	
	public void Translate(float dX, float dY) {
		Node.X += dX;
		Node.Y += dY;
	}
}

