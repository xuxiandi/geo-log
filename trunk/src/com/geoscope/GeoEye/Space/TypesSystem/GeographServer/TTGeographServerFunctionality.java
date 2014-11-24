package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeographServerFunctionality extends TTypeFunctionality {

	public TTGeographServerFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTGeographServerFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return null;
	}
}
