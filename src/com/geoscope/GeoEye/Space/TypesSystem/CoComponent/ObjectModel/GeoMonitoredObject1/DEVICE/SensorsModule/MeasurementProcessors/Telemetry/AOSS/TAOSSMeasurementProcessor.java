package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.AOSS;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.Telemetry.TLR.TTLRMeasurementProcessor;

public class TAOSSMeasurementProcessor extends TTLRMeasurementProcessor {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.AOSS.Model.TModel.ModelTypeID;
	

	public TAOSSMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
