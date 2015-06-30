package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeographServerObject extends TTypeSystem {

	public static final String 	FolderName = "GeographServerObject";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTGeographServerObject(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTGeoGraphServerObject,SpaceDefines.nmTGeoGraphServerObject);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTGeographServerObjectFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
