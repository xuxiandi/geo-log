package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.BusinessModels.TGMOTrackLogger1BusinessModel;

public class TGeoMonitoredObjectModel extends TObjectModel
{
	public static final int 	ID = 1;
	public static final String	Name = "Geo.Log";

	public TGeoMonitoredObjectModel() throws Exception {
		super();
		ObjectSchema = new TGeoMonitoredObjectSchema(this);
		ObjectDeviceSchema = new TGeoMonitoredObjectDeviceSchema(this);
	}

	@Override
	public boolean SetBusinessModel(int BusinessModelID) {
		boolean Result = super.SetBusinessModel(BusinessModelID);
		switch (BusinessModelID)
		{
			case TGMOTrackLogger1BusinessModel.ID: 
				BusinessModel = new TGMOTrackLogger1BusinessModel(this);
				return true; //. ->

			default:
				return Result; //. ->
		}
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
