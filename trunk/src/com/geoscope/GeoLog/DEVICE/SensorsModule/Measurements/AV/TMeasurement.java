package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

public class TMeasurement extends TSensorMeasurement {

	public TMeasurement(String pMeasurementID) throws Exception {
		super(pMeasurementID, TMeasurementDescriptor.class);
	}
}
