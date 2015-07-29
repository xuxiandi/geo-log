package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data;

import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TDestinationStreamChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;

public class TControlStreamChannel extends TDestinationStreamChannel {

	protected TPluginModule PluginModule = null;
	
	public TControlStreamChannel(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
	}
	
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		return false;
	}
}
