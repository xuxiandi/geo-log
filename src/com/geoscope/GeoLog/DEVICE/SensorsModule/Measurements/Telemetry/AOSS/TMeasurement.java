package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.AOSS;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;

public class TMeasurement extends com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR.TMeasurement {

	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pDomain, pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
	}
}
