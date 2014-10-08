package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	private TPluginModule PluginModule;
	
	public TChannelsProvider(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) {
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel(PluginModule)); // =>
		else
			return null;
	}	
}
