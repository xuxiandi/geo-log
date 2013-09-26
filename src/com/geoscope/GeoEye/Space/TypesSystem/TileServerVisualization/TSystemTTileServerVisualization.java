package com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization;

import java.io.File;

import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.Utils.TFileSystem;

public class TSystemTTileServerVisualization extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"TileImagery";
	
	private static final String[] PersistContextFiles = new String[] {"DATA.XML"};
	
	public TSystemTTileServerVisualization(TTypesSystem pTypesSystem) {
		super(pTypesSystem);
	}

	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	

	@Override
	public void Context_Clear() {
		TFileSystem.EmptyFolder(new File(Context_GetFolder()), PersistContextFiles);
	}
	
	@Override
	public void Context_ClearItems(long ToTime) {
		TFileSystem.RemoveFolderFiles(new File(Context_GetFolder()), ToTime, PersistContextFiles);
	}	
}
