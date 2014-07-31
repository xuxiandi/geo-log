package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeographServerFunctionality extends TTypeFunctionality {

	public TTGeographServerFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTGeoGraphServer);
	}
	
	public TTGeographServerFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTGeoGraphServer);
	}
	
	public TTGeographServerFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTGeoGraphServer);
	}

	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return null;
	}
}
