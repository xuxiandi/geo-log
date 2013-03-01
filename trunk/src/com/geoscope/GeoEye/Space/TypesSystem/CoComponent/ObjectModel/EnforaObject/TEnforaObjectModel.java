package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.BusinessModels.TEnforaObjectTrackerBusinessModel;

public class TEnforaObjectModel extends TObjectModel
{
	public static final int 	ID = 2;
	public static final String	Name = "Enfora-Object";

	public TEnforaObjectModel() throws Exception {
		super();
		ObjectSchema = new TEnforaObjectSchema(this);
		ObjectDeviceSchema = new TEnforaObjectDeviceSchema(this);
	}

	@Override
	public boolean SetBusinessModel(int BusinessModelID) {
		boolean Result = super.SetBusinessModel(BusinessModelID);
		switch (BusinessModelID)
		{
			case TEnforaObjectTrackerBusinessModel.ID: 
				BusinessModel = new TEnforaObjectTrackerBusinessModel(this);
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
