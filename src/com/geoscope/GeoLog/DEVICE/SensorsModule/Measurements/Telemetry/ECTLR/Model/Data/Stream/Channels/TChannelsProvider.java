package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public TChannelsProvider() {
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TTLRChannel.TypeID.equals(pTypeID))
			return (new TTLRChannel()); // =>
		else
			return null; //. ->
	}	
}
