package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

public class TMeasurementDescriptor {

	public String	ID = null;
	public short 	Mode = TVideoRecorderModule.MODE_UNKNOWN;
	public double 	StartTimestamp = 0.0;
	public double 	FinishTimestamp = 0.0;
	public int		AudioPackets = -1;
	public int		VideoPackets = -1;
	
	public TMeasurementDescriptor(String pID) {
		ID = pID;
	}
	
	public boolean IsStarted() {
		return (StartTimestamp != 0.0);
	}

	public boolean IsFinished() {
		return (FinishTimestamp != 0.0);
	}
}
