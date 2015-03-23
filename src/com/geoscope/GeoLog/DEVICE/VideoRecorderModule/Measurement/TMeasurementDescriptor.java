package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.Measurement;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TMeasurementDescriptor extends TDEVICEModule.TSensorMeasurementDescriptor {

	public static final String TypeIDPrefix = "AV";
	
	
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
}
