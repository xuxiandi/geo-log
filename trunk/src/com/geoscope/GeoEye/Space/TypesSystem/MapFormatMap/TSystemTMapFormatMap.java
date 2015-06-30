package com.geoscope.GeoEye.Space.TypesSystem.MapFormatMap;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTMapFormatMap extends TTypeSystem {

	public static final String 	FolderName = "MapFormatMap";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTMapFormatMap(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTMapFormatMap,SpaceDefines.nmTMapFormatMap);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTMapFormatMapFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
