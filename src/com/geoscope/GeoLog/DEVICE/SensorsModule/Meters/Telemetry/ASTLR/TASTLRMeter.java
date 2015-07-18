package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ASTLR;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.Telemetry.TLR.TTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TASTLRMeter extends TTLRMeter {

	public static final String TypeID = "Telemetry.ASTLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Android state";
	public static final String Info = "Telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	
	public TASTLRMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected TStreamChannel[] GetSourceChannels() throws Exception {
		if (SensorsModule.InternalSensorsModule.ASTLRChannel == null)
			throw new IOException("no origin channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.ASTLRChannel.Enabled)
			throw new IOException("the origin channel is disabled"); //. =>
		SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.ASTLRChannel.DestinationChannel_Get(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		return (new TStreamChannel[] {SourceChannel}); 	
	}
	
	@Override
	protected TMeasurement CreateMeasurement() throws Exception {
		return new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
	}
}
