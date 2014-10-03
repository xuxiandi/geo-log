package com.geoscope.GeoLog.DEVICE.PluginsModule.Models;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.TCommand;

public class TPluginModel {

	protected static final boolean flDebug = true;
	
	protected TPluginsModule PluginsModule;
	
	public TPluginModel(TPluginsModule pPluginsModule, byte[] pModelData) {
		PluginsModule = pPluginsModule;
	}
	
	public TPluginModel(TPluginsModule pPluginsModule) {
		this(pPluginsModule,null);
	}	

	public void DoOnCommandResponse(TCommand Command) throws Exception {
	}
}
