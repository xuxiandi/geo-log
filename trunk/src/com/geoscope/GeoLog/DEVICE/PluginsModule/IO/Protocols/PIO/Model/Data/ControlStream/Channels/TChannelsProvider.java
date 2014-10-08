package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	private TPluginModule PluginModule;
	
	public TChannelsProvider(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TDVRTChannel.TypeID.equals(pTypeID))
			return (new TDVRTChannel(PluginModule)); // =>
		else
			return null;
	}	
}
