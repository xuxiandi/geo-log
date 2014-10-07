package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TDVRTChannel extends TStreamChannel {

	public static final String TypeID = "DeviceRotator.DVRT";


	public TDVRTChannel(TDEVICEModule pDevice) {
		super(pDevice);
	}	

	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
