package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public static final String AudioAACADTSFileName = "Audio.aac";

	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
