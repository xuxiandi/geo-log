package com.geoscope.GeoLog.DEVICE.PluginsModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TCommand;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TPluginModule extends TModule {

	public TPluginsModule PluginsModule;
	
	public TPluginModule(TPluginsModule pPluginsModule) {
		super(pPluginsModule);
		//.
		PluginsModule = pPluginsModule;
	}
	
	public void OutgoingCommands_ProcessCommand(TCommand Command) throws Exception {
	}	
}
