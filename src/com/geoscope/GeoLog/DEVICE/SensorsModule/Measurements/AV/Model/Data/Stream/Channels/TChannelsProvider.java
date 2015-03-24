package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public TChannelsProvider() {
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel()); // =>
		if (TH264IChannel.TypeID.equals(pTypeID))
			return (new TH264IChannel()); // =>
		else
			return null;
	}	
}
