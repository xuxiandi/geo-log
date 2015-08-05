package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TChannelsProvider() {
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) throws Exception {
		if (TChannel.TypeIsTypeOfChannel(pTypeID, TTLRChannel.TypeID))
			return (new TTLRChannel()); //. ->
		//.
		com.geoscope.Classes.Data.Stream.Channel.TChannelProvider CP;
		//.
		CP = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.TChannelsProvider.Instance;
		TChannel Result = CP.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		CP = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.TChannelsProvider.Instance;
		Result = CP.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		CP = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.TChannelsProvider.Instance;
		Result = CP.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		return Result; //. ->
	}	
}
