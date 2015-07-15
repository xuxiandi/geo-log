package com.geoscope.GeoEye;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;

public class TReflectorTrack {
	
	private static final int DefaultCapacity = 1000;
	private static final int TrackColor = Color.RED;
	
	
	private TReflectorComponent Reflector;
	//.
	private int Capacity;
	//.
	private TXYCoord[] Nodes;
	//.
	private int Size;
	private int Position;
	//.
	private Paint DrawPaint = new Paint();
	private float NodeRadius;
	
	public TReflectorTrack(TReflectorComponent pReflector, int pCapacity) {
		Reflector = pReflector;
		Capacity = pCapacity;
		//.
		Nodes = new TXYCoord[Capacity];
		Size = 0;
		Position = 0;
		//.
		DrawPaint.setDither(true);
		DrawPaint.setStyle(Paint.Style.STROKE);
		DrawPaint.setStrokeJoin(Paint.Join.BEVEL);
		DrawPaint.setStrokeCap(Paint.Cap.BUTT);
		DrawPaint.setStrokeWidth(3.0F*Reflector.metrics.density);
		DrawPaint.setColor(TrackColor);
		NodeRadius = 3.0F*Reflector.metrics.density;
	}
	
	public TReflectorTrack(TReflectorComponent pReflector) {
		this(pReflector, DefaultCapacity);
	}
	
	public void Nodes_Add(TXYCoord Node) {
		Nodes[Position] = Node;
		Position++;
		if (Position >= Capacity)
			Position = 0;
		//.
		if (Size < Capacity)
			Size++;
	}
	
	public void Nodes_Clear() {
		Size = 0;
	}
	
	public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
		if (Size > 0) {
			int _Position = (Position-Size);
			while (_Position < 0)
				_Position += Capacity;
			//.
			TXYCoord Node = Nodes[0];
			TXYCoord C0 = RW.ConvertToScreen(Node.X,Node.Y);
			canvas.drawCircle((float)C0.X,(float)C0.Y, NodeRadius, DrawPaint);
			//.
			for (int I = 1; I < Size; I++) {
				Node = Nodes[I];
				TXYCoord C1 = RW.ConvertToScreen(Node.X,Node.Y);
				//.
				canvas.drawLine((float)C0.X,(float)C0.Y, (float)C1.X,(float)C1.Y, DrawPaint);
				canvas.drawCircle((float)C1.X,(float)C1.Y, NodeRadius, DrawPaint);
				//.
				C0 = C1;
				_Position++;
				if (_Position >= Capacity)
					_Position = 0;
			}
		}
	}
}
