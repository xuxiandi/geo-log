package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Video;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TVideoMeter extends TSensorMeter {

	public static final String TypeID = "Video";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Video recorder";
	public static final String Info = "camera";
	
	public static class TMyProfile extends TProfile {
	}
	

	private TH264IChannel SourceChannel;
	
	public TVideoMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected TStreamChannel[] GetSourceChannels() throws Exception {
		if (SensorsModule.InternalSensorsModule.H264IChannel == null)
			throw new IOException("no origin channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.H264IChannel.Enabled)
			throw new IOException("the origin channel is disabled"); //. =>
		SourceChannel = (TH264IChannel)SensorsModule.InternalSensorsModule.H264IChannel.DestinationChannel_Get(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		return (new TStreamChannel[] {SourceChannel}); 	
	}
	
	@Override
	protected void DoProcess() throws Exception {
		GetSourceChannels();
		//.
		SourceChannel.Suspend();
		try {
			SourceChannel.SourceChannel_Start();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					Measurement.H264IChannel.Assign(SourceChannel);
					Measurement.H264IChannel.FrameRate = SensorsModule.InternalSensorsModule.H264IChannel.GetFrameRate();
					//.
					Measurement.Start();
					try {
						SourceChannel.DoStreaming(Measurement.H264IChannel.DestinationStream, Canceller, MeasurementMaxDuration);
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
