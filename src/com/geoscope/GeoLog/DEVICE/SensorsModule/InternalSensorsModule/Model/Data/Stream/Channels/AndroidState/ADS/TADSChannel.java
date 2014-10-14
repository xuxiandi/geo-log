package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS;

import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TADSChannel extends TStreamChannel {

	public static final String TypeID = "AndroidState.ADS";
	
	public TADSChannel(TInternalSensorsModule pInternalSensorsModule) {
		super(pInternalSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
