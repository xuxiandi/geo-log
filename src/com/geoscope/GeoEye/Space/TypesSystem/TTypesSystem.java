package com.geoscope.GeoEye.Space.TypesSystem;

import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;

public class TTypesSystem {

	public static TTypesSystem TypesSystem = new TTypesSystem();
	
	//. type systems
	public TSystemTGeographServerObject SystemTGeographServerObject;
	
	public TTypesSystem() {
		SystemTGeographServerObject = new TSystemTGeographServerObject();
	}
	
	public void Destroy() {
		if (SystemTGeographServerObject != null) {
			SystemTGeographServerObject.Destroy();
			SystemTGeographServerObject = null;
		}
	}
	
	public void ClearContext() {
		SystemTGeographServerObject.ClearContext();
	}
}
