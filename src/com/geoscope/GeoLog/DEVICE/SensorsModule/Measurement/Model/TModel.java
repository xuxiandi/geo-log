package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model;

import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel;

public class TModel extends TSensorMeasurementModel {

	public static String GetModelTypeName(String TypeID, Context context) {
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.GetTypeName(context); //. -> 
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel.GetTypeName(context); //. -> 
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model.TModel.GetTypeName(context); //. ->
		else
			return ""; //. ->
	}

	public TModel() {
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
