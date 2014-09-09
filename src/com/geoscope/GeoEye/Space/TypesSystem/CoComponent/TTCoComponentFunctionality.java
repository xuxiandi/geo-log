package com.geoscope.GeoEye.Space.TypesSystem.CoComponent;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTCoComponentFunctionality extends TTypeFunctionality {

	public TTCoComponentFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTCoComponentFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TCoComponentFunctionality(this,idComponent));
	}
}
