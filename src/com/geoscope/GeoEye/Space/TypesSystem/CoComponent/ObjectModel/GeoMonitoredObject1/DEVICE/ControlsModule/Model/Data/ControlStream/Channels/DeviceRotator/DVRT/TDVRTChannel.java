package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TDVRTChannel extends TStreamChannel {

	public static final String TypeID = "DeviceRotator.DVRT";
	
	public static final int DescriptorSize = 4;
	//.
	public static final int LatitudeTag		= 1;
	public static final int LongitudeTag	= 2;
	
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
