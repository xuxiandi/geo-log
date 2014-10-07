package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TDVRTChannel extends TChannel {

	public static final String TypeID = "DeviceRotator.DVRT";
	

	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
