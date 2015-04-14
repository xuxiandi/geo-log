package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

public class TMeasurement extends TSensorMeasurement {

	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
	}
}
