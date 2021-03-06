package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeoCrdSystem extends TTypeSystem {

	public static final String 	FolderName = "GeoCrdSystem";
	
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
			return (new TGeoCrdSystemData());
		}
		
		public synchronized ArrayList<TGeoCrdSystemData> GetItemsByGeoSpaceID(int idGeoSpace) {
			ArrayList<TGeoCrdSystemData> Result = null;
			Collection<TComponentData> Items = ItemsTable.values();
			Iterator<TComponentData> Item = Items.iterator();
			while (Item.hasNext()) {
				TGeoCrdSystemData CD = (TGeoCrdSystemData)Item.next();
				if (CD.GeoSpaceID == idGeoSpace) {
					if (Result == null)
						Result = new ArrayList<TGeoCrdSystemData>();
					Result.add(CD);
				}
			}			
			return Result; 
		}
	}	
	
	public TSystemTGeoCrdSystem(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTGeoCrdSystem,SpaceDefines.nmTGeoCrdSystem);
		//.
		ContextCache = new TThisContextCache(this);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTGeoCrdSystemFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
