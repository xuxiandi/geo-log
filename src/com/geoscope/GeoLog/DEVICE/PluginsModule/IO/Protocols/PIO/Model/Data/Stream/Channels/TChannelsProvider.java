package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TPluginModule PluginModule;
	
	public TChannelsProvider() {
		PluginModule = null;
	}
	
	public TChannelsProvider(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel(PluginModule)); //. ->
		if (TTLRChannel.TypeID.equals(pTypeID))
			return (new TTLRChannel(PluginModule)); //. ->
		else
			return null; //. ->
	}	
}
