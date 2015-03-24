package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model;

import org.w3c.dom.Node;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel;

public class TModel extends TSensorMeasurementModel {

	public TModel() {
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
