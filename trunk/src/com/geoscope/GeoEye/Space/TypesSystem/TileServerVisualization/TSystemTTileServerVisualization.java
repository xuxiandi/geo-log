package com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization;

import java.io.File;
import java.io.IOException;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.Utils.TFileSystem;

public class TSystemTTileServerVisualization extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"TileImagery";
	
	private static final String[] PersistContextFiles = new String[] {"DATA.XML"};
	
	public static class TThisContextCache extends TTypeSystem.TContextCache {
		
		public TThisContextCache(TTypeSystem pTypeSystem) throws IOException {
			super(pTypeSystem);
		}
		
		@Override
		protected TComponentData CreateItem() {
			return (new TTileServerVisualizationData());
		}
	}	
	
	public TSystemTTileServerVisualization(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem);
		//.
		ContextCache = new TThisContextCache(this);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTTileServerVisualizationFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	

	@Override
	public void Context_Clear() {
		if (ContextCache != null)
			ContextCache.Clear();
		TFileSystem.EmptyFolder(new File(Context_GetFolder()), PersistContextFiles);
	}
	
	@Override
	public void Context_ClearItems(long ToTime) {
		TFileSystem.RemoveFolderFiles(new File(Context_GetFolder()), ToTime, PersistContextFiles);
	}	
}
