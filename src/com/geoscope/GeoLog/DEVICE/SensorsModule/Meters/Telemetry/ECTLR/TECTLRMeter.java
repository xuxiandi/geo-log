package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TECTLRMeter extends TSensorMeter {

	public static final String TypeID = "Telemetry.ECTLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Environmental conditions";
	public static final String Info = "Telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TECTLRMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		if (SensorsModule.InternalSensorsModule.ECTLRChannel == null)
			throw new IOException("no origin channel"); //. =>
		TTLRChannel SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.ECTLRChannel.DestinationChannel_Get(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		//.
		SourceChannel.Suspend();
		try {
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
		finally {
			SourceChannel.Resume();
		}
	}
}
