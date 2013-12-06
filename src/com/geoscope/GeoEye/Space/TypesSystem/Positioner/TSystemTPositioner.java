package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import java.io.File;

import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTPositioner extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"Positioner";
	
	public TSystemTPositioner(TTypesSystem pTypesSystem) {
		super(pTypesSystem);
	}

	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
