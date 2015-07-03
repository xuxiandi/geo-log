package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TMeasurement extends TSensorMeasurement {

	public TH264IChannel H264IChannel;
	
	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pDomain, pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
		//.
		H264IChannel = new TH264IChannel(Folder());
		Descriptor.Model.Stream.Channels.add(H264IChannel);
	}
}
