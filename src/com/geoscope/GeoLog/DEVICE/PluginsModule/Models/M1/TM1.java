package com.geoscope.GeoLog.DEVICE.PluginsModule.Models.M1;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TMODELCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.TModel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.TPluginModel;

public class TM1 extends TPluginModel {

	public TModel Model = null;
	
	public TM1(TPluginsModule pPluginsModule) {
		super(pPluginsModule,null);
	}

	public void DoOnCommandResponse(TCommand Command) throws Exception {
		if (Command instanceof TMODELCommand) {
			TMODELCommand MODELCommand = (TMODELCommand)Command;
			Model = MODELCommand.Value;
			return; //. ->
		}
		//.
		if (Model != null)
			Model.DoOnCommandResponse(Command);
	}
}
