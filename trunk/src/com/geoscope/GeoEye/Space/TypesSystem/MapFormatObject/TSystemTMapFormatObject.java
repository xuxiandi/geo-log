package com.geoscope.GeoEye.Space.TypesSystem.MapFormatObject;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTMapFormatObject extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"MapFormatObject";
	
	public TSystemTMapFormatObject(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTMapFormatObject,SpaceDefines.nmTMapFormatObject);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTMapFormatObjectFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
