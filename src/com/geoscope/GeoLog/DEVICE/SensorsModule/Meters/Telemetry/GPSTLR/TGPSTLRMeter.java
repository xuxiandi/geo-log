package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.GPSTLR;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.GPSTLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TGPSTLRMeter extends TSensorMeter {

	public static final String ID = "Telemetry.GPSTLR";
	//.
	public static final String TypeID = "Telemetry.GPSTLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Geo-location track";
	public static final String Info = "GPS telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TGPSTLRMeter(TSensorsModule pSensorsModule, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(ID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		if (SensorsModule.InternalSensorsModule.GPSChannel == null)
			throw new IOException("no origin channel"); //. =>
		TTLRChannel SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.GPSChannel.DestinationChannel_Get(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		//.
		SourceChannel.SourceChannels_Start();
		try {
			int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
			while (!Canceller.flCancel) {
				TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
				Measurement.TLRChannel.Assign(SourceChannel);
				//.
				Measurement.Start();
				try {
					SourceChannel.DoStreaming(Measurement.TLRChannel.DestrinationStream, Canceller, MeasurementMaxDuration);
				}
				finally {
					Measurement.Finish();
				}
				//.
				DoOnMeasurementFinish(Measurement);
			}
		}
		finally {
			SourceChannel.SourceChannels_Stop();
		}
	}
}
