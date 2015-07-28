package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;


public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TChannelsProvider() {
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) throws Exception {
		if (TChannel.TypeIsTypeOfChannel(pTypeID, TTLRChannel.TypeID))
			return (new TTLRChannel()); //. ->
		else
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance.GetChannel(pTypeID); //. ->
	}	
}
