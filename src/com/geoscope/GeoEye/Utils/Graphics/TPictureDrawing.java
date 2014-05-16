package com.geoscope.GeoEye.Utils.Graphics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;
import com.geoscope.Utils.TDataConverter;

public class TPictureDrawing extends TDrawing {
	
	public static final short TYPE_ID = 2;
	
	private static Paint paint = new Paint();
	
	public Bitmap Picture;
	public TDrawingNode Node;
	
	public TPictureDrawing(Bitmap pPicture, float pX, float pY) {
		Picture = pPicture;
		Node = new TDrawingNode(pX,pY);
	}

	public TPictureDrawing() {
	}

	@Override
	public void Destroy() {
		if (Picture != null) {
			Picture.recycle();
			Picture = null;
		}
	}

	@Override
	public short TypeID() {
		return TYPE_ID;
	}
	
	public void Paint(Canvas pCanvas) {
		pCanvas.drawBitmap(Picture, Node.X,Node.Y, paint);
	}
	
	public void Translate(float dX, float dY) {
		Node.X += dX;
		Node.Y += dY;
	}
	
	@Override
	public TDrawingNode GetAveragePosition() {
		TDrawingNode Result = new TDrawingNode(Node.X,Node.Y);
		Result.X = Result.X+(Picture.getWidth()/2.0F);
		Result.Y = Result.Y+(Picture.getHeight()/2.0F);
		return Result;
	}
	
	@Override
	public TRectangle GetRectangle() {
		TRectangle Result = new TRectangle();
		Result.Xmn = Node.X; Result.Ymn = Node.Y;
		Result.Xmx = Result.Xmn+Picture.getWidth(); Result.Ymx = Result.Ymn+Picture.getHeight();
		return Result;
	}
	
	@Override
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA = Node.ToByteArray();
			BOS.write(BA);
			ByteArrayOutputStream BOS1 = new ByteArrayOutputStream();
			try {
				int PictureSize;
				if (Picture != null) {
					Picture.compress(CompressFormat.PNG, 100/* % */, BOS1);
					BA = BOS1.toByteArray();
					PictureSize = BA.length;
				}
				else
					PictureSize = 0;
				byte[] BA1 = TDataConverter.ConvertInt32ToBEByteArray(PictureSize);
				BOS.write(BA1);
				if (PictureSize > 0)
					BOS.write(BA);
			}
			finally {
				BOS1.close();
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Node = new TDrawingNode();
		Idx = Node.FromByteArray(BA, Idx);
		int PictureSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		if (PictureSize > 0) {
			Picture = BitmapFactory.decodeByteArray(BA,Idx,PictureSize,TBitmapDecodingOptions.GetBitmapFactoryOptions()); Idx += PictureSize;
		}
		else 
			Picture = null;
		return Idx;
	}
}

