package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.TRC;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TTRCChannel extends TTLRChannel {

	public static final String TypeID = TTLRChannel.TypeID+"."+"AndroidState.TRC";

	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
