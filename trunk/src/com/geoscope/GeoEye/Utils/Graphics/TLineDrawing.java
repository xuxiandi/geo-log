package com.geoscope.GeoEye.Utils.Graphics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;

import com.geoscope.Utils.TDataConverter;

public class TLineDrawing extends TDrawing {
	
	public static final short TYPE_ID = 1;
	
	public static final int DefaultNodesCapacity = 100;
	
	public static class TBrushMaskFilter {
	}
	
	public static class TBrushBlurMaskFilter extends TBrushMaskFilter {
		
		public static final Blur[] BlurValues = Blur.values();
		
		public Blur 	BlurStyle;
		public float 	BlurRadius;
		
		public TBrushBlurMaskFilter(Blur pBlurStyle, float pBlurRadius) {
			BlurStyle = pBlurStyle;
			BlurRadius = pBlurRadius;
		}
	}

	public static final Paint DefaultBrush = new Paint();
	
	public Paint 			Brush = null;
	public TBrushMaskFilter	BrushMaskFilter = null;
	//.
	public ArrayList<TDrawingNode> Nodes = new ArrayList<TDrawingNode>(DefaultNodesCapacity);
	
	public TLineDrawing(Paint pBrush, TBrushMaskFilter pBrushMaskFilter) {
		Brush = pBrush;
		BrushMaskFilter = pBrushMaskFilter;
	}
	
	public TLineDrawing() {
	}

	@Override
	public short TypeID() {
		return TYPE_ID;
	}
	
	@Override
	public void Paint(Canvas pCanvas) {
		TDrawingNode LastNode = Nodes.get(0); 
		pCanvas.drawCircle(LastNode.X,LastNode.Y, Brush.getStrokeWidth()*0.5F, Brush);
		for (int J = 1; J < Nodes.size(); J++) {
			TDrawingNode Node = Nodes.get(J);
			pCanvas.drawLine(LastNode.X,LastNode.Y, Node.X,Node.Y, Brush);
			LastNode = Node;
		}
	}
	
	@Override
	public void Translate(float dX, float dY) {
		for (int J = 0; J < Nodes.size(); J++) {
			TDrawingNode Node = Nodes.get(J);
			Node.X += dX;
			Node.Y += dY;
		}
	}

	@Override
	public TDrawingNode GetAveragePosition() {
		if (Nodes.size() == 0)
			return null; //. ->
		TDrawingNode Result = new TDrawingNode();
		for (int J = 0; J < Nodes.size(); J++) {
			TDrawingNode Node = Nodes.get(J);
			Result.X += Node.X;
			Result.Y += Node.Y;
		}
		Result.X = Result.X/Nodes.size();
		Result.Y = Result.Y/Nodes.size();
		return Result;
	}

	@Override
	public TRectangle GetRectangle() {
		TRectangle Result = new TRectangle();
		TDrawingNode Node = Nodes.get(0);
		Result.Xmn = Node.X; Result.Ymn = Node.Y;
		Result.Xmx = Result.Xmn; Result.Ymx = Result.Ymn;
		for (int J = 1; J < Nodes.size(); J++) {
			Node = Nodes.get(J);
			if (Node.X < Result.Xmn)
				Result.Xmn = Node.X;
			else
				if (Node.X > Result.Xmx)
					Result.Xmx = Node.X;
			if (Node.Y < Result.Ymn)
				Result.Ymn = Node.Y;
			else
				if (Node.Y > Result.Ymx)
					Result.Ymx = Node.Y;
		}
		//.
		float R = Brush.getStrokeWidth()/2.0F;
		if (BrushMaskFilter instanceof TBrushBlurMaskFilter) {
			float BlurR = ((TBrushBlurMaskFilter)BrushMaskFilter).BlurRadius;
			if (R < BlurR)
				R = BlurR;
		}
		R *= 2;
		Result.Xmn -= R; Result.Ymn -= R;
		Result.Xmx += R; Result.Ymx += R;
		//.
		return Result;
	}
	
	@Override
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA;
			int BrushColor = Brush.getColor();
			BA = TDataConverter.ConvertInt32ToBEByteArray(BrushColor);
			BOS.write(BA);
			double BrushWidth = Brush.getStrokeWidth();
			BA = TDataConverter.ConvertDoubleToBEByteArray(BrushWidth);
			BOS.write(BA);
			short BrushMaskFilterType = 0;
			if (BrushMaskFilter instanceof TBrushBlurMaskFilter) 
				BrushMaskFilterType = 1;
			BA = TDataConverter.ConvertInt16ToBEByteArray(BrushMaskFilterType);
			BOS.write(BA);
			switch (BrushMaskFilterType) {
			
			case 1: //. BlurMaskFilter
				TBrushBlurMaskFilter BMF = (TBrushBlurMaskFilter)BrushMaskFilter;
				BA = TDataConverter.ConvertInt16ToBEByteArray((short)BMF.BlurStyle.ordinal());
				BOS.write(BA);
				BA = TDataConverter.ConvertDoubleToBEByteArray(BMF.BlurRadius);
				BOS.write(BA);
				break; //. >
			}
			int NodesCount = Nodes.size();
			BA = TDataConverter.ConvertInt32ToBEByteArray(NodesCount);
			BOS.write(BA);
			for (int I = 0; I < NodesCount; I++) {
				BA = Nodes.get(I).ToByteArray();
				BOS.write(BA);
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Brush = new Paint();
		Brush.setAntiAlias(true);
		Brush.setStrokeCap(Cap.ROUND);
		//.
		int BrushColor = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Brush.setColor(BrushColor);
		double BrushWidth = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
		Brush.setStrokeWidth((float)BrushWidth);
		short BrushMaskFilterType = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
		switch (BrushMaskFilterType) {
		
		case 1: //. BlurMaskFilter
			short BlurStyle = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			float BlurRadius = (float)TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			BrushMaskFilter = new TBrushBlurMaskFilter(TBrushBlurMaskFilter.BlurValues[BlurStyle],BlurRadius);
			//.
			if (Brush.getColor() != Color.TRANSPARENT)
				Brush.setXfermode(DefaultBrush.getXfermode());
			else
				Brush.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OUT)); 
			if (((TBrushBlurMaskFilter)BrushMaskFilter).BlurRadius > 0.0)
				Brush.setMaskFilter(new BlurMaskFilter(((TBrushBlurMaskFilter)BrushMaskFilter).BlurRadius,((TBrushBlurMaskFilter)BrushMaskFilter).BlurStyle));
			else 
				Brush.setMaskFilter(null);
			break; //. >
			
		default:
			BrushMaskFilter = null;
			break; //. >
		}	
		Nodes.clear();
		int NodesCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < NodesCount; I++) {
			TDrawingNode Node = new TDrawingNode();
			Idx = Node.FromByteArray(BA, Idx);
			Nodes.add(Node);
		}
		return Idx;
	}
}

