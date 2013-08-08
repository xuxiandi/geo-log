package com.geoscope.GeoEye.Space.TypesSystem;

import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.TSystemTDATAFile;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;

public class TTypesSystem {

	public static TTypesSystem TypesSystem = new TTypesSystem();
	
	//. type systems
	public TSystemTDATAFile SystemTDATAFile;
	public TSystemTGeographServerObject SystemTGeographServerObject;
	
	public TTypesSystem() {
		SystemTDATAFile 			= new TSystemTDATAFile();
		SystemTGeographServerObject = new TSystemTGeographServerObject();
	}
	
	public void Destroy() {
		if (SystemTGeographServerObject != null) {
			SystemTGeographServerObject.Destroy();
			SystemTGeographServerObject = null;
		}
		if (SystemTDATAFile != null) {
			SystemTDATAFile.Destroy();
			SystemTDATAFile = null;
		}
	}
	
	public void ClearContext() {
		SystemTGeographServerObject.ClearContext();
	}
}
