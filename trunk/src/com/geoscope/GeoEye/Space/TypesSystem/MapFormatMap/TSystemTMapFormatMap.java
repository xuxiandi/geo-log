package com.geoscope.GeoEye.Space.TypesSystem.MapFormatMap;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTMapFormatMap extends TTypeSystem {

	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"MapFormatMap";
	
	public TSystemTMapFormatMap(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTMapFormatMap,SpaceDefines.nmTMapFormatMap);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTMapFormatMapFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
