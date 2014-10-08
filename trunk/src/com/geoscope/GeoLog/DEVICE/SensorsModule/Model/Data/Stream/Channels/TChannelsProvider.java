package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel()); // =>
		else
			return null;
	}	
}
