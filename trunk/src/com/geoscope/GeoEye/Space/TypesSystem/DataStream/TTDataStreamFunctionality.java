package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTDataStreamFunctionality extends TTypeFunctionality {

	public TTDataStreamFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTDataStream);
	}
	
	public TTDataStreamFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTDataStream);
	}

	public TTDataStreamFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTDataStream);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TDataStreamFunctionality(this,idComponent));
	}
}
