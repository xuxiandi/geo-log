package com.geoscope.GeoEye.Utils.Graphics;

import java.util.ArrayList;

import android.graphics.Paint;

public class TLineDrawing extends TDrawing {
	public static final int DefaultNodesCapacity = 100;
	
	public Paint 	Brush = null;
	public ArrayList<TDrawingNode> Nodes = new ArrayList<TDrawingNode>(DefaultNodesCapacity);
	
	public TLineDrawing(Paint pBrush) {
		Brush = pBrush;
	}
}

