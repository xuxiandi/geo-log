package com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTTileServerVisualizationFunctionality extends TTypeFunctionality {

	public TTTileServerVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTTileServerVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}

	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TTileServerVisualizationFunctionality(this,idComponent));
	}
}
