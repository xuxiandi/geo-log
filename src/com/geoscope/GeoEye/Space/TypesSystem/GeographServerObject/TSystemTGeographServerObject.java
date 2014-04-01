package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeographServerObject extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"GeographServerObject";
	
	public TSystemTGeographServerObject(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTGeographServerObjectFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
