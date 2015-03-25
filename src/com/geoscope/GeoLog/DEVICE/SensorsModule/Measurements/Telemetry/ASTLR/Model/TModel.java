package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model;

import org.w3c.dom.Node;

public class TModel extends com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel {

	public TModel() {
    	TypeID = "Telemetry.ASTLR";
    	ContainerTypeID = "Telemetry.TLR";
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
