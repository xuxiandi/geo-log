package com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTDetailedPictureVisualizationFunctionality extends TTypeFunctionality {

	public TTDetailedPictureVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pServer,pTypeSystem);
	}
	
	public TTDetailedPictureVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}

	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TDetailedPictureVisualizationFunctionality(this,idComponent));
	}
}
