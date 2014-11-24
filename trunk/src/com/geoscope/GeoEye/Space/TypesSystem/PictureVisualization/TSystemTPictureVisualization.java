package com.geoscope.GeoEye.Space.TypesSystem.PictureVisualization;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTPictureVisualization extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"PictureVisualizations";
	
	public TSystemTPictureVisualization(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTPictureVisualization,SpaceDefines.nmTPictureVisualization);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTPictureVisualizationFunctionality(this, pServer)); 
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
