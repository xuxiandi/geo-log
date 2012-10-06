package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;

public class TGMO1GeoLogAndroidBusinessModel extends TObjectBusinessModel {
	
	public static final int 	ID = 2; 
	public static final String	Name = "Geo.Log.Android";

	public TGMO1GeoLogAndroidBusinessModel(TGeoMonitoredObject1Model pGeoMonitoredObject1Model) {
		super(pGeoMonitoredObject1Model);
	}
	
	@Override
	public int GetID() {
		return ID;
	}
	
	@Override
	public String GetName() {
		return Name;
	}	
}
