package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

public class TSensorMeterInfo {
	
	public TSensorMeterDescriptor Descriptor;
	//.
	public boolean 	flEnabled;
	//.
	public boolean 	flActive;
	//.
	public int 		Status;
	
	public TSensorMeterInfo(TSensorMeterDescriptor pDescriptor, boolean pflEnabled, boolean pflActive, int pStatus) {
		Descriptor = pDescriptor;
		//.
		flEnabled = pflEnabled;
		//.
		flActive = pflActive;
		//.
		Status = pStatus;
	}
}