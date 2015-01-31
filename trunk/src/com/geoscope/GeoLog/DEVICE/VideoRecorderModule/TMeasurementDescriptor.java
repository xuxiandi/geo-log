package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TMeasurementDescriptor extends TDEVICEModule.TSensorMeasurementDescriptor {

	public short 	Mode = TVideoRecorderModule.MODE_UNKNOWN;
	//.
	public int 		AudioFormat = 0;
	public int		AudioSPS = -1;
	public int		AudioPackets = -1;
	//.
	public int 		VideoFormat = 0;
	public int		VideoFPS = -1;
	public int		VideoPackets = -1;
	
	public TMeasurementDescriptor() {
	}
	
	public TMeasurementDescriptor(String pID) {
		super(pID);
	}
	
	public boolean IsStarted() {
		return (StartTimestamp != 0.0);
	}

	public boolean IsFinished() {
		return (FinishTimestamp != 0.0);
	}
	
	public boolean IsValid() {
		return (IsStarted() && IsFinished() && (Duration() > 0.0));
	}
	
	public double Duration() {
		return (FinishTimestamp-StartTimestamp);
	}

	public int DurationInMs() {
		return (int)(Duration()*24.0*3600.0*1000.0);
	}

	public long DurationInNs() {
		return (long)(Duration()*24.0*3600.0*1000000000.0);
	}
}
