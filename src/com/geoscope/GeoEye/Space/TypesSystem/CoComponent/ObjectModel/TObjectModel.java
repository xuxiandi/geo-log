package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.TEnforaObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.TGeoMonitoredObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;


public class TObjectModel {

	public static TObjectModel GetObjectModel(int pID) throws Exception {
		switch (pID)
		{
		case TGeoMonitoredObjectModel.ID: 
			return (new TGeoMonitoredObjectModel());
		
			case TGeoMonitoredObject1Model.ID: 
				return (new TGeoMonitoredObject1Model());
			
			case TEnforaObjectModel.ID: 
				return (new TEnforaObjectModel());
				
			case TEnforaMT3000ObjectModel.ID: 
				return (new TEnforaMT3000ObjectModel());
				
			default:
				return null; //. ->
		}
	}
	
	public static TObjectModel GetObjectModel(int pID, TGEOGraphServerObjectController pObjectController, boolean pflFreeObjectController) throws Exception {
		TObjectModel Result = GetObjectModel(pID);
		if (Result != null) 
			Result.SetObjectController(pObjectController,pflFreeObjectController);	
		return Result;
	}
	
	public TGEOGraphServerObjectController 	ObjectController = null;
	protected boolean 						flFreeObjectController = false;
	//.
	public TComponentSchema ObjectSchema = null;
	public TComponentSchema ObjectDeviceSchema = null;
	//.
	public TObjectBusinessModel BusinessModel = null;
	
	public TObjectModel() throws Exception {
		BusinessModel = null;
		//.
		CreateSchemas();
	}
	
	public TObjectModel(TGEOGraphServerObjectController pObjectController, boolean pflFreeObjectController) throws Exception {
		this();
		SetObjectController(pObjectController,pflFreeObjectController);	
		//.
		CreateSchemas();
	}
	
	protected void CreateSchemas() throws Exception {
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
		if (flFreeObjectController && (ObjectController != null)) {
			try {
				ObjectController.Destroy();
			} catch (IOException E) {}
			ObjectController = null;
		}
	}

	public int GetID() {
		return 0;
	}
	
	public String GetName() {
		return "";
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
	
	private void SetObjectController(TGEOGraphServerObjectController pObjectController, boolean pflFreeObjectController) {
		ObjectController = pObjectController;
		flFreeObjectController = pflFreeObjectController;
	}
}
