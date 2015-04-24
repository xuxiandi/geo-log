package com.geoscope.GeoEye.Space.TypesSystem.MapFormatMap;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTMapFormatMapFunctionality extends TTypeFunctionality {

	public TTMapFormatMapFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TMapFormatMapFunctionality(this,idComponent));
	}
}
