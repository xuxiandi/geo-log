package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.AOSS;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.AOSS.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
