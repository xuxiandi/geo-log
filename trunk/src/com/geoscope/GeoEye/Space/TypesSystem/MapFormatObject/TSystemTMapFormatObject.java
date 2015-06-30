package com.geoscope.GeoEye.Space.TypesSystem.MapFormatObject;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTMapFormatObject extends TTypeSystem {

	public static final String 	FolderName = "MapFormatObject";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTMapFormatObject(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTMapFormatObject,SpaceDefines.nmTMapFormatObject);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTMapFormatObjectFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
