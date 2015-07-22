package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.GeoLocation.GPS;

import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TGPSChannel extends TTLRChannel {

	public static final String TypeID = "GeoLocation.GPS";

	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
