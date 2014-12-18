package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TGPSChannel extends TTLRChannel {

	public static final String TypeID = "GeoLocation.GPS";

	public TGPSChannel(TSensorsModule pSensorsModule) {
		super(pSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
