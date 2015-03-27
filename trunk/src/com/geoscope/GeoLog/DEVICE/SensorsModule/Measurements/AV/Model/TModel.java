package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model;

import org.w3c.dom.Node;

import android.content.Context;

public class TModel extends com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel {

	public static final String ModelTypeID = "AV.FRAME";
	//.
	private static final String ModelTypeName = "Video";
	public static String		GetTypeName(Context context) {
		return ModelTypeName;
	}
	
	public TModel() {
		super();
		//.
    	TypeID = ModelTypeID;
    	ContainerTypeID = "Audio.AAC-Video.H264I";
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
