package com.geoscope.GeoEye.Space.TypesSystem.TTFVisualization;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTTTFVisualizationFunctionality extends TTypeFunctionality {

	public TTTTFVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTTTFVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TTTFVisualizationFunctionality(this,idComponent));
	}
}
