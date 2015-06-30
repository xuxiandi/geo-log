package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTDATAFile extends TTypeSystem {

	public static final String 	FolderName = "DATAFile";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTDATAFile(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTDATAFile,SpaceDefines.nmTDATAFile);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTDATAFileFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
