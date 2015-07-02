package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.TModel;

public class TMeasurementDescriptor extends TSensorMeasurementDescriptor {

	public static final String VideoH264FileName 	= "Video.h264";
	public static final String VideoIndex32FileName = "VideoIDX.idx32";
	public static final String VideoTS32FileName 	= "VideoTS.ts32";

	public TMeasurementDescriptor() {
		Model = new TModel();
	}
}
