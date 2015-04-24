package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeographServerObjectFunctionality extends TTypeFunctionality {

	public TTGeographServerObjectFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TGeographServerObjectFunctionality(this,idComponent));
	}
}
