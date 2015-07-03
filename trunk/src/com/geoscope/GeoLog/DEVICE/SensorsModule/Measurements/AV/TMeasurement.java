package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

public class TMeasurement extends TSensorMeasurement {

	public TAACChannel 		AACChannel;
	//.
	public TH264IChannel 	H264IChannel;
	
	public TMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		super(pGeographServerObjectID, pDatabaseFolder, pDomain, pMeasurementID, TMeasurementDescriptor.class, ChannelProvider);
		//.
		AACChannel = new TAACChannel(Folder());
		Descriptor.Model.Stream.Channels.add(AACChannel);
		//.
		H264IChannel = new TH264IChannel(Folder());
		Descriptor.Model.Stream.Channels.add(H264IChannel);
	}
}
