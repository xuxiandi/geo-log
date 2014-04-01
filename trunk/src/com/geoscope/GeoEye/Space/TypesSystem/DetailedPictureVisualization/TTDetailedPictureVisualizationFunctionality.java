package com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTDetailedPictureVisualizationFunctionality extends TTypeFunctionality {

	public TTDetailedPictureVisualizationFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer) {
		super(pTypeSystem,pServer,SpaceDefines.idTDetailedPictureVisualization);
	}
	
	public TTDetailedPictureVisualizationFunctionality(TGeoScopeServer pServer) {
		super(pServer,SpaceDefines.idTDetailedPictureVisualization);
	}

	public TTDetailedPictureVisualizationFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem,SpaceDefines.idTDetailedPictureVisualization);
	}

	@Override
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return (new TDetailedPictureVisualizationFunctionality(this,idComponent));
	}
}
