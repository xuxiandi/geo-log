package com.geoscope.GeoEye;

public class TNavigationItem {

	public final static int NAVIGATIONTYPE_NONE = 1;
	public final static int NAVIGATIONTYPE_MOVING = 2;
	public final static int NAVIGATIONTYPE_SCALING = 3;
	public final static int NAVIGATIONTYPE_ROTATING = 4;
	
	public int Type;
	public double dX;
	public double dY;
}
