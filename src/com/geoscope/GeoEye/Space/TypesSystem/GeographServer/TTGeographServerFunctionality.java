package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;

public class TTGeographServerFunctionality extends TTypeFunctionality {

	public TTGeographServerFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTGeoGraphServer);
	}

	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return null;
	}
}
