package com.geoscope.GeoEye.Space.TypesSystem.MapFormatObject;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTMapFormatObjectFunctionality extends TTypeFunctionality {

	public TTMapFormatObjectFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TMapFormatObjectFunctionality(this,idComponent));
	}
}
