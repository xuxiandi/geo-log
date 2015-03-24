package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public TChannelsProvider() {
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) {
		TChannel Result = null;
		com.geoscope.Classes.Data.Stream.Channel.TChannelProvider CP;
		//.
		CP = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.TChannelsProvider();
		Result = CP.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		CP = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.Stream.Channels.TChannelsProvider();
		Result = CP.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		return Result; //. ->
	}	
}
