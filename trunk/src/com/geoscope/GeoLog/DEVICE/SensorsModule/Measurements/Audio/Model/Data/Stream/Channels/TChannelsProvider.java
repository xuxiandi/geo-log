package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TChannelsProvider() {
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel()); // =>
		else
			return null; //. ->
	}	
}
