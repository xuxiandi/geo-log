package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.Telecontrol.TLC.TTLCChannel;

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
	public TStreamChannel GetChannel(String pTypeID) throws Exception {
		if (TDVRTChannel.TypeID.equals(pTypeID))
			return (new TDVRTChannel(PluginModule)); //. ->
		if (TTLCChannel.TypeID.equals(pTypeID))
			return (new TTLCChannel(PluginModule)); //. ->
		else
			return null; //. ->
	}	
}
