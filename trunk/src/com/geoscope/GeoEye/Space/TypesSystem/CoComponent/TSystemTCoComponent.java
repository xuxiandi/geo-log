package com.geoscope.GeoEye.Space.TypesSystem.CoComponent;

import java.io.File;

import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTCoComponent extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"CoComponent";
	
	public TSystemTCoComponent(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem);
	}

	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
