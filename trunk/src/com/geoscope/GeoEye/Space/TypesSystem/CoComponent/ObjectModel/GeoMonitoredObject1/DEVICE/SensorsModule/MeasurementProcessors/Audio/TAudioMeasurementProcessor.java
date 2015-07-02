package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Audio;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerMyPlayerComponent;

public class TAudioMeasurementProcessor extends TVideoRecorderServerMyPlayerComponent {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.TModel.ModelTypeID;
	

	public TAudioMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	
}
