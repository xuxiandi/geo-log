package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeographServerObjectFunctionality extends TTypeFunctionality {

	public TTGeographServerObjectFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTGeoGraphServerObject);
	}
	
	public TTGeographServerObjectFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTGeoGraphServerObject);
	}
	
	public TTGeographServerObjectFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTGeoGraphServerObject);
	}

	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return null;
	}
}
