package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;

public class TECTLRMeter extends TSensorMeter {

	public static final String ID = "Telemetry.TLR.1";
	//.
	public static final String TypeID = "Telemetry.TLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Environmental conditions";
	public static final String Info = "Telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TECTLRMeter(String pProfileFolder) throws Exception {
		super(new TSensorMeterDescriptor(ID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
}
