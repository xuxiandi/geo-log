package com.geoscope.GeoEye.Space.TypesSystem.HINTVisualization;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTHintVisualization extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"Hints";
	
	public TSystemTHintVisualization(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTHINTVisualization,SpaceDefines.nmTHINTVisualization);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTHintVisualizationFunctionality(this)); 
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
