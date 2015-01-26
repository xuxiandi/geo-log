package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TEventRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoLog.COMPONENT.TComponentElement;

public class TGMO1GeoLogAndroidBusinessModel extends TObjectBusinessModel {
	
	public static final int 	ID = 2; 
	public static final String	Name = "Geo.Log.Android";

	public TGMO1GeoLogAndroidBusinessModel(TGeoMonitoredObject1Model pGeoMonitoredObject1Model) {
		super(pGeoMonitoredObject1Model);
	}
	
	@Override
	public int GetID() {
		return ID;
	}
	
	@Override
	public String GetName() {
		return Name;
	}	

	@Override
	public THistoryRecord GetBusinessHistoryRecord(TComponentElement ObjectModelElement, double pTimestamp, long UserID, boolean flSetOperation, Context context) {
		TGeoMonitoredObject1DeviceComponent DeviceComponent = (TGeoMonitoredObject1DeviceComponent)ObjectModel.ObjectDeviceSchema.RootComponent;
		if (ObjectModelElement == DeviceComponent.BatteryModule.Charge) {
			int Severity = TEventRecord.SEVERITY_INFO;
			if (DeviceComponent.BatteryModule.Charge.Value < 20)
				Severity = TEventRecord.SEVERITY_MINOR;
			else
				if (DeviceComponent.BatteryModule.Charge.Value < 10)
					Severity = TEventRecord.SEVERITY_MAJOR;
				else
					if (DeviceComponent.BatteryModule.Charge.Value < 5)
						Severity = TEventRecord.SEVERITY_CRITICAL;
			String Message = context.getString(R.string.SBatteryCharge)+Short.toString(DeviceComponent.BatteryModule.Charge.Value)+"%";
			TEventRecord EventRecord = new TEventRecord(pTimestamp, Severity, Message);
			return EventRecord; //. ->
		};
		if (ObjectModelElement == DeviceComponent.ConnectorModule.ServiceProvider.Signal) {
			int Severity = TEventRecord.SEVERITY_INFO;
			if (DeviceComponent.ConnectorModule.ServiceProvider.Signal.Value < 20)
				Severity = TEventRecord.SEVERITY_MINOR;
			else
				if (DeviceComponent.ConnectorModule.ServiceProvider.Signal.Value < 10)
					Severity = TEventRecord.SEVERITY_MAJOR;
				else
					if (DeviceComponent.ConnectorModule.ServiceProvider.Signal.Value < 5)
						Severity = TEventRecord.SEVERITY_CRITICAL;
			String Message = context.getString(R.string.SCellularSignal1)+Short.toString(DeviceComponent.ConnectorModule.ServiceProvider.Signal.Value)+"%";
			TEventRecord EventRecord = new TEventRecord(pTimestamp, Severity, Message);
			return EventRecord; //. ->
		}
		return null; //. ->
	}
}
