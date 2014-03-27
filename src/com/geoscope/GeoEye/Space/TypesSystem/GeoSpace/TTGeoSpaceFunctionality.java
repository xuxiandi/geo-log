package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;

public class TTGeoSpaceFunctionality extends TTypeFunctionality {

	public TTGeoSpaceFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTGeoSpace);
	}

	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new TGeoSpaceFunctionality(this,idComponent));
	}
}
