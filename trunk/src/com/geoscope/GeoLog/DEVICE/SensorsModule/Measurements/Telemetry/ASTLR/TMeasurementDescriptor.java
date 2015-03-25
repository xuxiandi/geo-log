package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public static final String TypeIDPrefix = "Telemetry.ASTLR";
	
	
	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
