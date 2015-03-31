package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ECTLR;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.Telemetry.TLR.TTLRMeasurementProcessor;

public class TECTLRMeasurementProcessor extends TTLRMeasurementProcessor {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel.ModelTypeID;
	

	public TECTLRMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
