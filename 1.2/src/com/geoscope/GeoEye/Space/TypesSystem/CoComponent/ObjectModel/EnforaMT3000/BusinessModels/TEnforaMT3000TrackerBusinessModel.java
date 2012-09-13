package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.BusinessModels;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectModel;

public class TEnforaMT3000TrackerBusinessModel extends TObjectBusinessModel { 
	
	public static final int 	ID = 1;
	public static final String	Name = "Enfora-MT3000.Tracker";

	public TEnforaMT3000TrackerBusinessModel(TEnforaMT3000ObjectModel pEnforaMT3000ObjectModel) {
		super(pEnforaMT3000ObjectModel);
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
