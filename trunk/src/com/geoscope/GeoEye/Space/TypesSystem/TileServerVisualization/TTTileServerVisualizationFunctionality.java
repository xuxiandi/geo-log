package com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTTileServerVisualizationFunctionality extends TTypeFunctionality {

	public TTTileServerVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTTileServerVisualization);
	}
	
	public TTTileServerVisualizationFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTTileServerVisualization);
	}

	public TTTileServerVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTTileServerVisualization);
	}

	@Override
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new TTileServerVisualizationFunctionality(this,idComponent));
	}
}
