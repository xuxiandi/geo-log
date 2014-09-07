package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTDATAFile extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"DATAFile";
	
	public TSystemTDATAFile(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTDATAFile,SpaceDefines.nmTDATAFile);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTDATAFileFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
