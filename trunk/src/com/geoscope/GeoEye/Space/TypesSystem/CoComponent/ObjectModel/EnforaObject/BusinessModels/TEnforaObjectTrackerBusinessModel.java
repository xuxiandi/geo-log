package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.BusinessModels;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.TEnforaObjectModel;

public class TEnforaObjectTrackerBusinessModel extends TObjectBusinessModel { 
	
	public static final int 	ID = 1;
	public static final String	Name = "Enfora-Object.Tracker";

	public TEnforaObjectTrackerBusinessModel(TEnforaObjectModel pEnforaObjectModel) {
		super(pEnforaObjectModel);
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
