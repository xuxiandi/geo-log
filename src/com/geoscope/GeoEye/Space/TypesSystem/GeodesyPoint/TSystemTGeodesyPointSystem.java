package com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint;

import java.io.File;
import java.io.IOException;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeodesyPointSystem extends TTypeSystem {

	public static final String 	FolderName = "GeodesyPoint";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public static class TThisContextCache extends TTypeSystem.TContextCache {
		
		public TThisContextCache(TTypeSystem pTypeSystem) throws IOException {
			super(pTypeSystem);
		}
		
		@Override
		protected TComponentData CreateItem() {
			return (new TGeodesyPointData());
		}
	}	
	
	public TSystemTGeodesyPointSystem(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTGeodesyPoint,SpaceDefines.nmTGeodesyPoint);
		//.
		ContextCache = new TThisContextCache(this);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTGeodesyPointFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
