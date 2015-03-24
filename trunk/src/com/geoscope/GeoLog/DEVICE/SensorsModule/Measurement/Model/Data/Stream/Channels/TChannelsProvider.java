package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.TStreamChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public TChannelsProvider() {
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		TStreamChannel Result = null;
		//.
		com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.TChannelsProvider AVChannelsProvider = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.TChannelsProvider();
		Result = AVChannelsProvider.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		return Result; //. ->
	}	
}
