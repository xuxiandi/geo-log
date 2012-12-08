package com.geoscope.GeoEye.Utils.Graphics;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TLineDrawing extends TDrawing {
	public static final int DefaultNodesCapacity = 100;
	
	public Paint 	Brush = null;
	public ArrayList<TDrawingNode> Nodes = new ArrayList<TDrawingNode>(DefaultNodesCapacity);
	
	public TLineDrawing(Paint pBrush) {
		Brush = pBrush;
	}
	
	public void Paint(Canvas pCanvas) {
		TDrawingNode LastNode = Nodes.get(0); 
		pCanvas.drawCircle(LastNode.X,LastNode.Y, Brush.getStrokeWidth()*0.5F, Brush);
		for (int J = 1; J < Nodes.size(); J++) {
			TDrawingNode Node = Nodes.get(J);
			pCanvas.drawLine(LastNode.X,LastNode.Y, Node.X,Node.Y, Brush);
			LastNode = Node;
		}
	}
	
	public void Translate(float dX, float dY) {
		for (int J = 0; J < Nodes.size(); J++) {
			TDrawingNode Node = Nodes.get(J);
			Node.X += dX;
			Node.Y += dY;
		}
	}
}

