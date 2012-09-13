package com.geoscope.GeoEye.Space.Defines;

public class TContainerCoord {

	public double Xmin;
	public double Ymin;
	public double Xmax;
	public double Ymax;

	public boolean IsObjectOutside(TContainerCoord Obj_ContainerCoord)
	{
		return (((Obj_ContainerCoord.Xmax < Xmin) || (Obj_ContainerCoord.Xmin > Xmax) || (Obj_ContainerCoord.Ymax < Ymin) || (Obj_ContainerCoord.Ymin > Ymax)));
	}
}
