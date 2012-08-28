package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.BusinessModels.TEnforaMT3000TrackerBusinessModel;

public class TEnforaMT3000ObjectModel extends TObjectModel
{
	public static final int ID = 201;

	public TEnforaMT3000ObjectModel() throws Exception {
		super();
		ObjectSchema = new TEnforaMT3000ObjectSchema(this);
		ObjectDeviceSchema = new TEnforaMT3000ObjectDeviceSchema(this);
	}

	@Override
	public boolean SetBusinessModel(int BusinessModelID) {
		boolean Result = super.SetBusinessModel(BusinessModelID);
		switch (BusinessModelID)
		{
			case TEnforaMT3000TrackerBusinessModel.ID: 
				BusinessModel = new TEnforaMT3000TrackerBusinessModel(this);
				return true; //. ->

			default:
				return Result; //. ->
		}
	}
}
