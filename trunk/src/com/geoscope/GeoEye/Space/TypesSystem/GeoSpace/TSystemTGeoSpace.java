package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import java.io.File;
import java.io.IOException;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTGeoSpace extends TTypeSystem {
	
	public static class TGeoSpaceDescriptor {

		public int 		ID;
		public String 	Name;
		public int 		POIMapID;
		
		public TGeoSpaceDescriptor(int pID, String pName, int pPOIMapID) {
			ID = pID;
			Name = pName;
			POIMapID = pPOIMapID;
		}
	}
	
	public static final TGeoSpaceDescriptor[] WellKnownGeoSpaces = new TGeoSpaceDescriptor[] {
		new TGeoSpaceDescriptor(2,"Yandex.Maps",8),
		new TGeoSpaceDescriptor(90,"Google.Maps",12),
		new TGeoSpaceDescriptor(89,"OpenStreet.Maps",11),
		new TGeoSpaceDescriptor(88,"Russia map",6)
	};
	
	public static String[] WellKnownGeoSpaces_GetNames() {
		String[] Result = new String[WellKnownGeoSpaces.length];
		for (int I = 0; I < WellKnownGeoSpaces.length; I++)
			Result[I] = WellKnownGeoSpaces[I].Name;
		return Result;
	}
	
	public static TGeoSpaceDescriptor WellKnownGeoSpaces_GetItemByID(int pID) {
		for (int I = 0; I < WellKnownGeoSpaces.length; I++)
			if (WellKnownGeoSpaces[I].ID == pID)
				return WellKnownGeoSpaces[I]; //. =>
		return null;
	}

	public static int WellKnownGeoSpaces_GetIndexByID(int pID) {
		for (int I = 0; I < WellKnownGeoSpaces.length; I++)
			if (WellKnownGeoSpaces[I].ID == pID)
				return I; //. =>
		return -1;
	}

	public static int WellKnownGeoSpaces_GetIndexByPOIMapID(int pPOIMapID) {
		for (int I = 0; I < WellKnownGeoSpaces.length; I++)
			if (WellKnownGeoSpaces[I].POIMapID == pPOIMapID)
				return I; //. =>
		return -1;
	}

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"GeoSpace";
	
	public static class TThisContextCache extends TTypeSystem.TContextCache {
		
		public TThisContextCache(TTypeSystem pTypeSystem) throws IOException {
			super(pTypeSystem);
		}
		
		@Override
		protected TComponentData CreateItem() {
			return (new TGeoSpaceData());
		}
	}	
	
	public TSystemTGeoSpace(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTGeoSpace,SpaceDefines.nmTGeoSpace);
		//.
		ContextCache = new TThisContextCache(this);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTGeoSpaceFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
