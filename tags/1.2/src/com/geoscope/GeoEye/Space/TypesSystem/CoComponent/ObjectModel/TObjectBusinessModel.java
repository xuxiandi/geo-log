package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

public class TObjectBusinessModel {

	public TObjectModel ObjectModel;

	public TObjectBusinessModel(TObjectModel pObjectModel)
	{
		ObjectModel = pObjectModel;
	}

	public void Destroy() {
	}

	public int GetID() {
		return 0;
	}
	
	public String GetName() {
		return "";
	}	
}
