package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TStreamChannel extends TChannel {

	protected String MeasurementFolder = null;
	
	public TStreamChannel() {
	}
	
	@Override
	public void Initialize(Object pParameters) throws Exception {
		MeasurementFolder = (String)pParameters;
	}
}
