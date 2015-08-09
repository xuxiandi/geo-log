package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.TRC;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.Telemetry.TLR.TTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TTRCMeter extends TTLRMeter {

	public static final String TypeID = "Telemetry.TRC";
	public static final String ContainerTypeID = "";
	//.
	public static final String LocationID = "Default";
	//.
	public static final String Name = "Android tracing";
	
	public static class TMyProfile extends TProfile {
	}
	
	
	public TTRCMeter(TSensorsModule pSensorsModule, String pID, String pInfo, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, LocationID, Name,pInfo), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public TStreamChannel[] GetChannels() throws Exception {
		if (SensorsModule.InternalSensorsModule.TRCChannel == null)
			throw new IOException("no origin channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.TRCChannel.Enabled)
			throw new IOException("the origin channel is disabled"); //. =>
		SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.TRCChannel.DestinationChannel_Get(); 
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		return (new TStreamChannel[] {SourceChannel}); 	
	}
	
	@Override
	protected TMeasurement CreateMeasurement() throws Exception {
		return new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.TRC.TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
	}
}
