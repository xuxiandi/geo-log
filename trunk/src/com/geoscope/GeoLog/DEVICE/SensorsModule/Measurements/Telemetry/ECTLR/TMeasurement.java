package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TMeasurement extends TSensorMeasurement {

	public TTLRChannel TLRChannel;
	
	public TMeasurement(String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
		//.
		TLRChannel = new TTLRChannel(Folder());
		Descriptor.Model.Stream.Channels.add(TLRChannel);
	}
}
