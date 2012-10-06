package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

public class TRWLevelTileContainer {
	
	public int 			Level;
	public TTileLevel 	TileLevel; 
	//.
	public int Xmn;
	public int Xmx;
	public int Ymn;
	public int Ymx;
	//. container reflection window params
	public int RW_Xmn;
	public int RW_Ymn;
	public double b;
	public double _Width;
	public double diffX1X0;
	public double diffY1Y0;
	public double diffX3X0;
	public double diffY3Y0;
	public double Xc;
	public double Yc;
	public double Rotation;
	
	public TRWLevelTileContainer() {
	}
	
	public TRWLevelTileContainer(TRWLevelTileContainer C) {
		AssignContainer(C);
	}
	
	public void AssignContainer(TRWLevelTileContainer C) {
		Level = C.Level;
		Xmn = C.Xmn;
		Ymn = C.Ymn;
		Xmx = C.Xmx;
		Ymx = C.Ymx;
	}
	
	public int ContainerSquare() {
		return ((Xmx-Xmn+1)*(Ymx-Ymn+1));
	}
}
