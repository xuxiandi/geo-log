package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeoSpaceFunctionality extends TTypeFunctionality {

	public TTGeoSpaceFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TGeoSpaceFunctionality(this,idComponent));
	}
}
