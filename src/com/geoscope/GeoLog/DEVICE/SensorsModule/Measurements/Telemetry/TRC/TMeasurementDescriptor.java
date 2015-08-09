package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.TRC;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.TRC.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
