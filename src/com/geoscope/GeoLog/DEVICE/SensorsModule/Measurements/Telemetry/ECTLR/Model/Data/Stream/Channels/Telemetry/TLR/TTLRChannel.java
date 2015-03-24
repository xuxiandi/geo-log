package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.Stream.Channels.Telemetry.TLR;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.Data.TStreamChannel;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";

	
	public TTLRChannel() {
		Kind = TChannel.CHANNEL_KIND_OUT;
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
