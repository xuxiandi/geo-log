package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;

public class TSourceChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TSourceChannelsProvider Instance = new TSourceChannelsProvider();

	
	@SuppressWarnings("unused")
	private TSensorsModule SensorsModule;
	
	public TSourceChannelsProvider() {
		SensorsModule = null;
	}
	
	public TSourceChannelsProvider(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}
	
	@Override
	public TSourceStreamChannel GetChannel(String pTypeID) throws Exception {
		TSourceStreamChannel Result = com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance.GetChannel(pTypeID);
		if (Result != null)
			return Result; //. ->
		//.
		Result = com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.TChannelsProvider.Instance.GetChannel(pTypeID);
		return Result;
	}	
}
