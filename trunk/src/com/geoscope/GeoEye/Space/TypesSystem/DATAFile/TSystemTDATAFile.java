package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import java.io.File;

import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TSystemTDATAFile extends TTypeSystem {

	public static String ContextFolder = TReflector.TypesSystemContextFolder+"/"+"DATAFile";
	
	@Override
	public String GetContextFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
