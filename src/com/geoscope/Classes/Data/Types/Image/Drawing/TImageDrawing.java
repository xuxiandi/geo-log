package com.geoscope.Classes.Data.Types.Image.Drawing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TImageDrawing extends TDrawing {
	
	public static final short TYPE_ID = 3;
	
	private static Paint paint = new Paint();
	
	public Bitmap Image;
	public TDrawingNode Node0;
	public TDrawingNode Node1;
	
	public TImageDrawing(Bitmap pImage, float pX0, float pY0, float pX1, float pY1) {
		Image = pImage;
		//.
		Node0 = new TDrawingNode(pX0,pY0);
		//.
		Node1 = new TDrawingNode(pX1,pY1);
	}

	public TImageDrawing() {
	}

	@Override
	public void Destroy() {
		if (Image != null) {
			Image.recycle();
			Image = null;
		}
	}

	@Override
	public short TypeID() {
		return TYPE_ID;
	}
	
	public void Paint(Canvas pCanvas) {
		if (Image != null) {
			Rect SrcRect = new Rect(0,0, Image.getWidth(),Image.getHeight());
			RectF DestRect = new RectF(Node0.X,Node0.Y, Node1.X,Node1.Y);
			pCanvas.drawBitmap(Image, SrcRect, DestRect, paint);
		}
	}
	
	public void Translate(float dX, float dY) {
		Node0.X += dX;
		Node0.Y += dY;
		//.
		Node1.X += dX;
		Node1.Y += dY;
	}
	
	@Override
	public void Scale(float X0, float Y0, float Scale) {
		Node0.X = X0+(Node0.X-X0)*Scale;
		Node0.Y = Y0+(Node0.Y-Y0)*Scale;
		//.
		Node1.X = X0+(Node1.X-X0)*Scale;
		Node1.Y = Y0+(Node1.Y-Y0)*Scale;
	}

	@Override
	public TDrawingNode GetAveragePosition() {
		TDrawingNode Result = new TDrawingNode((Node0.X+Node1.X)/2.0F,(Node0.Y+Node1.Y)/2.0F);
		return Result;
	}
	
	@Override
	public TRectangle GetRectangle() {
		TRectangle Result = new TRectangle(Node0.X,Node0.Y, Node1.X,Node1.Y);
		return Result;
	}
	
	@Override
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA = Node0.ToByteArray();
			BOS.write(BA);
			BA = Node1.ToByteArray();
			BOS.write(BA);
			ByteArrayOutputStream BOS1 = new ByteArrayOutputStream();
			try {
				int PictureSize;
				if (Image != null) {
					Image.compress(CompressFormat.PNG, 100/* % */, BOS1);
					BA = BOS1.toByteArray();
					PictureSize = BA.length;
				}
				else
					PictureSize = 0;
				byte[] BA1 = TDataConverter.ConvertInt32ToLEByteArray(PictureSize);
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
		Node0 = new TDrawingNode();
		Idx = Node0.FromByteArray(BA, Idx);
		Node1 = new TDrawingNode();
		Idx = Node1.FromByteArray(BA, Idx);
		int ImageSize = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		if (ImageSize > 0) {
			Image = BitmapFactory.decodeByteArray(BA,Idx,ImageSize,TBitmapDecodingOptions.GetBitmapFactoryOptions()); Idx += ImageSize;
		}
		else 
			Image = null;
		return Idx;
	}
}

