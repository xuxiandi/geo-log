package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Video;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerMyPlayerComponent;

public class TVideoMeasurementProcessor extends TVideoRecorderServerMyPlayerComponent {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.TModel.ModelTypeID;
	

	public TVideoMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
