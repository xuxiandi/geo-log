package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import android.content.Context;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoLog.COMPONENT.TComponentElement;

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
	
	public THistoryRecord GetBusinessHistoryRecord(TComponentElement ObjectModelElement, double pTimestamp, long UserID, boolean flSetOperation, Context context) {
		return null;
	}
}
