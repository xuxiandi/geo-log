package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TMeasurement extends TSensorMeasurement {

	public TTLRChannel TLRChannel;
	
	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pDomain, String pMeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pDomain, pMeasurementID, DescriptorClass, ChannelProvider);
		//.
		TLRChannel = new TTLRChannel(Folder());
		Descriptor.Model.Stream.Channels.add(TLRChannel);
	}
}
