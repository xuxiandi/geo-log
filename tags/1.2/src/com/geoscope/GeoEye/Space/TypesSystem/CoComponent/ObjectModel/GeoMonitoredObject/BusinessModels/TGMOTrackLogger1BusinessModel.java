package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.BusinessModels;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.TGeoMonitoredObjectModel;

public class TGMOTrackLogger1BusinessModel extends TObjectBusinessModel {
	
	public static final int 	ID = 1; 
	public static final String	Name = "Pulsar";

	public TGMOTrackLogger1BusinessModel(TGeoMonitoredObjectModel pGeoMonitoredObjectModel) {
		super(pGeoMonitoredObjectModel);
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
