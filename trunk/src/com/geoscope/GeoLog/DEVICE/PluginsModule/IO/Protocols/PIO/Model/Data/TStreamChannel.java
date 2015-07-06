package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;

public class TStreamChannel extends TSourceStreamChannel {

	protected TPluginModule PluginModule = null;
	
	public com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = null;
	
	public TStreamChannel(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
	}
	
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		return false;
	}
}
