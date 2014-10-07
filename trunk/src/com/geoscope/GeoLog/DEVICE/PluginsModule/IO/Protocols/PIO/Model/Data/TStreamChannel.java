package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TStreamChannel extends TChannel {

	protected TDEVICEModule Device = null;
	
	public TStreamChannel(TDEVICEModule pDevice) {
		Device = pDevice;
	}
	
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		return false;
	}
}
