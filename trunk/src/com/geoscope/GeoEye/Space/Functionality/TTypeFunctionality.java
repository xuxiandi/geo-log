package com.geoscope.GeoEye.Space.Functionality;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TTPositionerFunctionality;

public class TTypeFunctionality extends TFunctionality {
	
	public static TTypeFunctionality Create(TGeoScopeServer pServer, int idTComponent) {
		switch (idTComponent) {
		
		case SpaceDefines.idTPositioner: 
			return (new TTPositionerFunctionality(pServer)); //. ->
		
		default:
			return null; //. ->
		}
	}
	
	public TTypeSystem TypeSystem;
	//.
	public TGeoScopeServer Server;
	//.
	public int idType;
	
	public TTypeFunctionality(TTypeSystem pTypeSystem, TGeoScopeServer pServer, int pidType) {
		TypeSystem = pTypeSystem;
		Server = pServer;
		idType = pidType;
	}
	
	public TTypeFunctionality(TTypeSystem pTypeSystem, int pidType) {
		this(pTypeSystem, null, pidType);
	}
	
	public TTypeFunctionality(TGeoScopeServer pServer, int pidType) {
		this(null, pServer, pidType);
	}
	
	public TTypesSystem TypesSystem() {
		return TypeSystem.TypesSystem;
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		return null;
	}
}
