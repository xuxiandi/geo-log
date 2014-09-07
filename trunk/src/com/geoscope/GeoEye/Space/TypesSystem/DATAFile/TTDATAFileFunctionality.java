package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTDATAFileFunctionality extends TTypeFunctionality {

	public TTDATAFileFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTDATAFileFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TDATAFileFunctionality(this,idComponent));
	}
}
