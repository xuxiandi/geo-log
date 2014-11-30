package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.BaseVisualizationFunctionality;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;

public class TBaseVisualizationFunctionality extends TComponentFunctionality {

	public long Ptr = SpaceDefines.nilPtr;
	
	public TBaseVisualizationFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality, pidComponent);
	}
}
