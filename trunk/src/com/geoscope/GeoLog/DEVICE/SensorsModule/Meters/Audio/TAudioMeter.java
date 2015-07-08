package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Audio;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

public class TAudioMeter extends TSensorMeter {

	public static final String TypeID = "Audio";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Audio recorder";
	public static final String Info = "microphone";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TAudioMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		if (SensorsModule.InternalSensorsModule.AACChannel == null)
			throw new IOException("no origin channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.AACChannel.Enabled)
			throw new IOException("the origin channel is disabled"); //. =>
		TAACChannel SourceChannel = (TAACChannel)SensorsModule.InternalSensorsModule.AACChannel.DestinationChannel_Get(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		//.
		SourceChannel.Suspend();
		try {
			SourceChannel.SourceChannel_Start();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					Measurement.AACChannel.Assign(SourceChannel);
					Measurement.AACChannel.SampleRate = SensorsModule.InternalSensorsModule.AACChannel.GetSampleRate();
					//.
					Measurement.Start();
					try {
						SourceChannel.DoStreaming(Measurement.AACChannel.DestinationStream, Canceller, MeasurementMaxDuration);
					}
					finally {
						Measurement.Finish();
					}
					//.
					DoOnMeasurementFinish(Measurement);
				}
			}
			finally {
				SourceChannel.SourceChannel_Stop();
			}
		}
		finally {
			SourceChannel.Resume();
		}
	}
}
