package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC;

import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TXENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.XENVC";
	
	public TXENVCChannel(TInternalSensorsModule pInternalSensorsModule) {
		super(pInternalSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
