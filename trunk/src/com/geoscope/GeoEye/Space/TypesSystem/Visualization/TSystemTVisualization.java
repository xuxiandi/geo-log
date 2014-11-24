package com.geoscope.GeoEye.Space.TypesSystem.Visualization;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTVisualization extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"Visualizations";
	
	public TSystemTVisualization(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTVisualization,SpaceDefines.nmTVisualization);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTVisualizationFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	

	@Override
	public void Context_ClearItems(long ToTime) {
	}	
}
