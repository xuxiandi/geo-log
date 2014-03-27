package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;

public class TTPositionerFunctionality extends TTypeFunctionality {
	
	public TTPositionerFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTPositioner);
	}

	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new TPositionerFunctionality(this,idComponent));
	}
}
