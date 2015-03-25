package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public static final String TypeIDPrefix = "AV";
	
	public static final String AudioFileName = "Audio.rtp";
	public static final String VideoFileName = "Video.rtp";
	public static final String MediaMPEG4FileName = "Data.mp4";
	public static final String Media3GPFileName = "Data.3gp";
	public static final String AudioAACADTSFileName = "Audio.aac";
	public static final String VideoH264FileName = "Video.h264";
	public static final String VideoIndex32FileName = "VideoIDX.idx32";
	public static final String VideoTS32FileName = "VideoTS.ts32";

	
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
