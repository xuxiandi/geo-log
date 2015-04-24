package com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeodesyPointFunctionality extends TTypeFunctionality {

	public TTGeodesyPointFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TGeodesyPointFunctionality(this,idComponent));
	}
}
