package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Audio;

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
	
	public static class TMyProfile extends TProfile {
	}
	
	
	protected com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel SourceChannel;
	//.
	protected TAACChannel Channel;
	
	public TAudioMeter(TSensorsModule pSensorsModule, String pID, String pInfo, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,pInfo), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		GetChannels();
		//.
		Channel.Suspend();
		try {
			Channel.SourceChannel_Start();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					Measurement.AACChannel.Assign(Channel);
					Measurement.AACChannel.SampleRate = SourceChannel.GetSampleRate();
					//.
					Measurement.Start();
					try {
						Channel.DoStreaming(Measurement.AACChannel.DestinationStream, Canceller, MeasurementMaxDuration);
					}
					finally {
						Measurement.Finish();
					}
					//.
					DoOnMeasurementFinish(Measurement);
				}
			}
			finally {
				Channel.SourceChannel_Stop();
			}
		}
		finally {
			Channel.Resume();
		}
	}
}
