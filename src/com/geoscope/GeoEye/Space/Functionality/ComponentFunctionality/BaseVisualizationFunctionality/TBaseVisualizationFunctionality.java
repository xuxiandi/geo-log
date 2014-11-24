package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.BaseVisualizationFunctionality;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;

public class TBaseVisualizationFunctionality extends TComponentFunctionality {

	public long Ptr = SpaceDefines.nilPtr;
	//.
	public TSpaceObj Obj = null;
	
	public TBaseVisualizationFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality, pidComponent);
	}
}
