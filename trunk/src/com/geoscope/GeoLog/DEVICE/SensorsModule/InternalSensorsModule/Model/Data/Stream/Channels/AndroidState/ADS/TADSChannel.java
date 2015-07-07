package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TADSChannel extends TStreamChannel {

	public static final String TypeID = "AndroidState.ADS";
	
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	

	public TADSChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, TMyProfile.class);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
