package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.Measurement.Model;

import org.w3c.dom.Node;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TModel extends TDEVICEModule.TSensorMeasurementDescriptor.TModel {

	public TModel() {
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
