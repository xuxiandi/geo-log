package com.geoscope.GeoEye.Space.TypesSystem.WMFVisualization;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTWMFVisualizationFunctionality extends TTypeFunctionality {

	public TTWMFVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTWMFVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TWMFVisualizationFunctionality(this,idComponent));
	}
}
