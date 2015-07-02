package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TChannelsProvider() {
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TH264IChannel.TypeID.equals(pTypeID))
			return (new TH264IChannel()); // =>
		else
			return null; //. ->
	}	
}
