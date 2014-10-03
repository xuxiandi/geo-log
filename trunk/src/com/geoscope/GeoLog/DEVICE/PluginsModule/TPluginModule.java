package com.geoscope.GeoLog.DEVICE.PluginsModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TCommand;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TPluginModule extends TModule {

	public TPluginModule(TModule pParent) {
		super(pParent);
	}
	
	public void OutgoingCommands_ProcessCommand(TCommand Command) throws Exception {
	}	
}
