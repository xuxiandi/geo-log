package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model;

import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel;

public class TModel extends TSensorMeasurementModel {

	public static TTypeInfo GetTypeInfo(String TypeID, Context context) {
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.GetTypeInfo(context); //. -> 
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.Model.TModel.GetTypeInfo(context); //. -> 
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.Model.TModel.GetTypeInfo(context); //. ->
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.GPSTLR.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.GPSTLR.Model.TModel.GetTypeInfo(context); //. ->
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.TModel.GetTypeInfo(context); //. -> 
		if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.TModel.ModelTypeID.equals(TypeID))
			return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.TModel.GetTypeInfo(context); //. -> 
		else
			return null; //. ->
	}

	public TModel() {
	}

	public TModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		super(ANode, ChannelProvider);
	}
}
