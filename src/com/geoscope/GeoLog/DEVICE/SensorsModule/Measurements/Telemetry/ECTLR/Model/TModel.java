package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model;

import org.w3c.dom.Node;

public class TModel extends com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel {

	public TModel() {
    	TypeID = "Telemetry.ECTLR";
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
