package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV;

import java.io.IOException;

import com.geoscope.Classes.MultiThreading.TStartableCancelableThread;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TAVMeter extends TSensorMeter {

	public static final String TypeID = "AV.FRAME";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "AudioVideo recorder";
	public static final String Info = "camera";
	
	public static class TMyProfile extends TProfile {
	}
	
	
	private TAACChannel 	AudioSourceChannel;
	private TH264IChannel 	VideoSourceChannel;
	
	public TAVMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public TStreamChannel[] GetSourceChannels() throws Exception {
		if (SensorsModule.InternalSensorsModule.AACChannel == null)
			throw new IOException("no origin audio channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.AACChannel.Enabled)
			throw new IOException("the origin audio channel is disabled"); //. =>
		AudioSourceChannel = (TAACChannel)SensorsModule.InternalSensorsModule.AACChannel.DestinationChannel_Get(); 	
		if (AudioSourceChannel == null)
			throw new IOException("no source audio channel"); //. =>
		//.
		if (SensorsModule.InternalSensorsModule.H264IChannel == null)
			throw new IOException("no origin video channel"); //. =>
		if (!SensorsModule.InternalSensorsModule.H264IChannel.Enabled)
			throw new IOException("the origin video channel is disabled"); //. =>
		VideoSourceChannel = (TH264IChannel)SensorsModule.InternalSensorsModule.H264IChannel.DestinationChannel_Get(); 	
		if (VideoSourceChannel == null)
			throw new IOException("no source video channel"); //. =>
		return (new TStreamChannel[] {AudioSourceChannel,VideoSourceChannel}); 	
	}
	
	@Override
	protected void DoProcess() throws Exception {
		GetSourceChannels();
		//.
		AudioSourceChannel.Suspend();
		try {
			AudioSourceChannel.SourceChannel_Start();
			try {
				VideoSourceChannel.Suspend();
				try {
					VideoSourceChannel.SourceChannel_Start();
					try {
						final int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
						while (!Canceller.flCancel) {
							final TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
							//. setup measurement
							Measurement.AACChannel.Assign(AudioSourceChannel);
							Measurement.AACChannel.SampleRate = SensorsModule.InternalSensorsModule.AACChannel.GetSampleRate();
							//.
							Measurement.H264IChannel.Assign(VideoSourceChannel);
							Measurement.H264IChannel.FrameRate = SensorsModule.InternalSensorsModule.H264IChannel.GetFrameRate();
							//.
							Measurement.Start();
							try {
								TStartableCancelableThread AudioRecording = new TStartableCancelableThread() {
									
									@Override
									public void run() {
										try {
											AudioSourceChannel.DoStreaming(Measurement.AACChannel.DestinationStream, Canceller, MeasurementMaxDuration);
										}
								    	catch (Throwable E) {
											SetStatus(STATUS_ERROR);
											//.
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
											SensorsModule.Device.Log.WriteError("AV recorder: "+GetTypeID()+", audio channel error",S);
								    	}
									}
								};
								TStartableCancelableThread VideoRecording = new TStartableCancelableThread() {
									
									@Override
									public void run() {
										try {
											VideoSourceChannel.DoStreaming(Measurement.H264IChannel.DestinationStream, Canceller, MeasurementMaxDuration);
										}
								    	catch (Throwable E) {
											SetStatus(STATUS_ERROR);
											//.
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
											SensorsModule.Device.Log.WriteError("AV recorder: "+GetTypeID()+", video channel error",S);
								    	}
									}
								};
								try {
									AudioRecording.Start();
									VideoRecording.Start();
									//.
									Thread.sleep(MeasurementMaxDuration);
								}
								finally {
									VideoRecording.Destroy();
									AudioRecording.Destroy();
								}
							}
							finally {
								Measurement.Finish();
							}
							//.
							DoOnMeasurementFinish(Measurement);
						}
					}
					finally {
						VideoSourceChannel.SourceChannel_Stop();
					}
				}
				finally {
					VideoSourceChannel.Resume();
				}
			}
			finally {
				AudioSourceChannel.SourceChannel_Stop();
			}
		}
		finally {
			AudioSourceChannel.Resume();
		}
	}
}
