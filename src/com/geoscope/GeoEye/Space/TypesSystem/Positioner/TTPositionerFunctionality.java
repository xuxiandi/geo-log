package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTPositionerFunctionality extends TTypeFunctionality {
	
	public TTPositionerFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTPositioner);
	}
	
	public TTPositionerFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTPositioner);
	}

	public TTPositionerFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTPositioner);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new TPositionerFunctionality(this,idComponent));
	}
}
