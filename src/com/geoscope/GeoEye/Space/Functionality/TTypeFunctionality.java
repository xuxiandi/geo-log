package com.geoscope.GeoEye.Space.Functionality;

import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TTypeFunctionality extends TFunctionality {
	
	public TGeoScopeServer Server;
	//.
	public TTypeSystem TypeSystem;
	//.
	public int idType;
	
	public TTypeFunctionality(TGeoScopeServer pServer, TTypeSystem pTypeSystem) {
		Server = pServer;
		//.
		TypeSystem = pTypeSystem;
		//.
		idType = TypeSystem.idType;
	}
	
	public TTypeFunctionality(TTypeSystem pTypeSystem) {
		this(null, pTypeSystem);
	}
	
	public TTypesSystem TypesSystem() {
		return TypeSystem.TypesSystem;
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return null;
	}
}
