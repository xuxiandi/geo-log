package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.File;

import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeographServerObject extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"GeographServerObject";
	
	public TSystemTGeographServerObject(TTypesSystem pTypesSystem) {
		super(pTypesSystem);
	}

	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
