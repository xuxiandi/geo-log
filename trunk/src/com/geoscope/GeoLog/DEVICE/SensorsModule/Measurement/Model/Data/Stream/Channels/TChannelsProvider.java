package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public TChannelsProvider() {
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) {
		if (TTLRChannel.TypeID.equals(pTypeID))
			return (new TTLRChannel()); // =>
		//.
		com.geoscope.Classes.Data.Stream.Channel.TChannelProvider CP = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.TChannelsProvider();
		TChannel Result = CP.GetChannel(pTypeID);
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
