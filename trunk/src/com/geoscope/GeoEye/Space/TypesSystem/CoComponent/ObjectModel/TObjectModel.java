package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;


public class TObjectModel {

	public static TObjectModel GetObjectModel(int pID) throws Exception {
		switch (pID)
		{
			case TGeoMonitoredObject1Model.ID: 
				return (new TGeoMonitoredObject1Model());
			
			case TEnforaMT3000ObjectModel.ID: 
				return (new TEnforaMT3000ObjectModel());
				
			default:
				return null; //. ->
		}
	}
	
	public TComponentSchema ObjectSchema = null;
	public TComponentSchema ObjectDeviceSchema = null;
	public TObjectBusinessModel BusinessModel = null;
	
	public TObjectModel() {
		BusinessModel = null;
	}
	
	public void Destroy() {
		if (BusinessModel != null)
		{
			BusinessModel.Destroy();
			BusinessModel = null;
		};
		if (ObjectDeviceSchema != null)
		{
			ObjectDeviceSchema.Destroy();
			ObjectDeviceSchema = null;
		};
		if (ObjectSchema != null)
		{
			ObjectSchema.Destroy();
			ObjectSchema = null;
		};
	}

	public boolean SetBusinessModel(int BusinessModelID)
	{
		if (BusinessModel != null)
		{
			BusinessModel.Destroy();
			BusinessModel = null;
		};
		return false;
	}	
}
