package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";
	
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	
	public TTLRChannel(TInternalSensorsModule pInternalSensorsModule, Class<?> ChannelProfile) throws Exception {
		super(pInternalSensorsModule, ChannelProfile);
	}
	
	public TTLRChannel(TInternalSensorsModule pInternalSensorsModule) throws Exception {
		super(pInternalSensorsModule, TMyProfile.class);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
