package com.geoscope.GeoEye.Space.TypesSystem.HINTVisualization;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTHintVisualizationFunctionality extends TTypeFunctionality {

	public TTHintVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTHintVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new THintVisualizationFunctionality(this,idComponent));
	}
}
