package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeoSpaceFunctionality extends TTypeFunctionality {

	public TTGeoSpaceFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTGeoSpace);
	}
	
	public TTGeoSpaceFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTGeoSpace);
	}

	public TTGeoSpaceFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTGeoSpace);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TGeoSpaceFunctionality(this,idComponent));
	}
}
