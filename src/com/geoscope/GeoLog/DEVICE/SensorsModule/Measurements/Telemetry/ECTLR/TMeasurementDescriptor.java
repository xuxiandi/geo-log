package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
