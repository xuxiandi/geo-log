package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

public class TSensorMeterInfo {
	
	public TSensorMeterDescriptor Descriptor;
	//.
	public boolean 	flEnabled;
	//.
	public boolean 	flActive;
	//.
	public int 		Status;
	public double 	StatusTimestamp;
	public String	StatusString;
	
	public TSensorMeterInfo(TSensorMeterDescriptor pDescriptor, boolean pflEnabled, boolean pflActive, int pStatus, double pStatusTimestamp, String pStatusString) {
		Descriptor = pDescriptor;
		//.
		flEnabled = pflEnabled;
		//.
		flActive = pflActive;
		//.
		Status = pStatus;
		StatusTimestamp = pStatusTimestamp;
		StatusString = pStatusString;
	}
}