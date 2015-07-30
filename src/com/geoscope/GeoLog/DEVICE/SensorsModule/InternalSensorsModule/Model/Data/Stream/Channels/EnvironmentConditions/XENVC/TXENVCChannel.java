package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TXENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.XENVC";
	
	public static class TMyProfile extends TChannel.TProfile {
		
	}

	
	public TXENVCChannel(TInternalSensorsModule pInternalSensorsModule, int pID, String pLocationID) throws Exception {
		super(pInternalSensorsModule, pID, pLocationID, TMyProfile.class);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
