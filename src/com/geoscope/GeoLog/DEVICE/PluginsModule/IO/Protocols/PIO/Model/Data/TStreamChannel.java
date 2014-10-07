package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;

public class TStreamChannel extends TChannel {

	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		return false;
	}
}
