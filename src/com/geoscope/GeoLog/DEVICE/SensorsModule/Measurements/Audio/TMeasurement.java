package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

public class TMeasurement extends TSensorMeasurement {

	public TAACChannel AACChannel;
	
	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
		//.
		AACChannel = new TAACChannel(Folder());
		Descriptor.Model.Stream.Channels.add(AACChannel);
	}
}
