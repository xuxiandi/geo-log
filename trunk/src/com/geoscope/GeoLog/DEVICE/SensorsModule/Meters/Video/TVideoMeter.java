package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Video;

import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TVideoMeter extends TSensorMeter {

	public static final String TypeID = "Video";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Video recorder";
	
	public static class TMyProfile extends TProfile {
	}
	

	protected com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel SourceChannel;
	//.
	protected TH264IChannel Channel;
	
	public TVideoMeter(TSensorsModule pSensorsModule, String pID, String pLocationID, String pInfo, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, pLocationID, Name,pInfo), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		GetChannels();
		//.
		Channel.SourceChannel_Start();
		try {
			Channel.Suspend();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					//.
					TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(Measurement.Domain, Measurement.Descriptor.ID); //. lock new measurement
					try {
						//. setup measurement
						Measurement.H264IChannel.Assign(Channel);
						Measurement.H264IChannel.FrameRate = SourceChannel.GetFrameRate();
						//.
						Measurement.Start();
						try {
							Channel.DoStreaming(TUserAccessKey.GenerateValue(), Measurement.H264IChannel.DestinationStream, Canceller, MeasurementMaxDuration);
						}
						finally {
							Measurement.Finish();
						}
					}
					finally {
						MeasurementLock.WriteUnLock();
					}
					//.
					DoOnMeasurementFinish(Measurement);
				}
			}
			finally {
				Channel.Resume();
			}
		}
		finally {
			Channel.SourceChannel_Stop();
		}
	}
}
