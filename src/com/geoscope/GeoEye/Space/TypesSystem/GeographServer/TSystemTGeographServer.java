package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import java.io.File;

import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeographServer extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"GeographServer";
	
	public TSystemTGeographServer(TTypesSystem pTypesSystem) {
		super(pTypesSystem);
	}

	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
