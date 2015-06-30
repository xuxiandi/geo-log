package com.geoscope.GeoEye.Space.TypesSystem.TTFVisualization;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTTTFVisualization extends TTypeSystem {

	public static final String 	FolderName = "TTFVisualization";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTTTFVisualization(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTTTFVisualization,SpaceDefines.nmTTTFVisualization);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTTTFVisualizationFunctionality(this)); 
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
