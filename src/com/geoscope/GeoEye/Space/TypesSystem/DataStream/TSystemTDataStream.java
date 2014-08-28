package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.io.File;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTDataStream extends TTypeSystem {
	
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+"DataStream";
	
	public TSystemTDataStream(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return (new TTDataStreamFunctionality(this, pServer)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
