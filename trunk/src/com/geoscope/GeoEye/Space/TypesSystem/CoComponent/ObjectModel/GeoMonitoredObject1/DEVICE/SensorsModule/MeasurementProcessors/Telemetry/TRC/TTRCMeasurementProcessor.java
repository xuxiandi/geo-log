package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.TRC;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.Telemetry.TLR.TTLRMeasurementProcessor;

public class TTRCMeasurementProcessor extends TTLRMeasurementProcessor {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.TRC.Model.TModel.ModelTypeID;
	

	public TTRCMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
