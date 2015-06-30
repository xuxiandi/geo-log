package com.geoscope.GeoEye.Space.TypesSystem.SecurityFile;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTSecurityFile extends TTypeSystem {

	public static final String 	FolderName = "SecurityFile";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTSecurityFile(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTSecurityFile,SpaceDefines.nmTSecurityFile);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTSecurityFileFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
