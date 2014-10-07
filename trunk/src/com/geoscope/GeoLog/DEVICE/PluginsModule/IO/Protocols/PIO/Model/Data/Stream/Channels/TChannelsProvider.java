package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	private TDEVICEModule Device;
	
	public TChannelsProvider(TDEVICEModule pDevice) {
		Device = pDevice;
	}
	
	@Override
	public TChannel GetChannel(String pTypeID) {
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel(Device)); // =>
		else
			return null;
	}	
}
