package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model;

import org.w3c.dom.Node;

import android.content.Context;
import android.graphics.Color;

public class TModel extends com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel {

	public static final String ModelTypeID = "Audio";
	//.
	private static final String ModelTypeName = "Audio";
	private static final int 	ModelTypeColor = Color.YELLOW;
	//.
	public static TTypeInfo 	GetTypeInfo(Context context) {
		return new TTypeInfo(ModelTypeName, ModelTypeColor);
	}
	
	public TModel() {
		super();
		//.
    	TypeID = ModelTypeID;
    	ContainerTypeID = "Audio.AAC";
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
