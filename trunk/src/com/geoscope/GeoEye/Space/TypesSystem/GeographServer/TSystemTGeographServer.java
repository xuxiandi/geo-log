package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeographServer extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"GeographServer";
	
	public TSystemTGeographServer(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTGeographServerFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
