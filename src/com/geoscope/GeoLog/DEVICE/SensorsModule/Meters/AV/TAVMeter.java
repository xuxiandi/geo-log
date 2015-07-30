package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV;

import com.geoscope.Classes.MultiThreading.TStartableCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TAVMeter extends TSensorMeter {

	public static final String TypeID = "AV.FRAME";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "AudioVideo recorder";
	
	public static class TMyProfile extends TProfile {
	}
	
	
	protected com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel 		AudioSourceChannel;
	protected com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel 	VideoSourceChannel;
	//.
	protected TAACChannel 	AudioChannel;
	protected TH264IChannel VideoChannel;
	
	public TAVMeter(TSensorsModule pSensorsModule, String pID, String pLocationID, String pInfo, String pProfileFolder) throws Exception {
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
		AudioChannel.Suspend();
		try {
			AudioChannel.SourceChannel_Start();
			try {
				VideoChannel.Suspend();
				try {
					VideoChannel.SourceChannel_Start();
					try {
						final int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
						while (!Canceller.flCancel) {
							final TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, TSensorsModuleMeasurements.CreateNewMeasurement(), com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
							//.
							TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(Measurement.Domain, Measurement.Descriptor.ID); //. lock new measurement
							try {
								//. setup measurement
								Measurement.AACChannel.Assign(AudioChannel);
								Measurement.AACChannel.SampleRate = AudioSourceChannel.GetSampleRate();
								//.
								Measurement.H264IChannel.Assign(VideoChannel);
								Measurement.H264IChannel.FrameRate = VideoSourceChannel.GetFrameRate();
								//.
								Measurement.Start();
								try {
									TStartableCancelableThread AudioRecording = new TStartableCancelableThread() {
										
										@Override
										public void run() {
											try {
												AudioChannel.DoStreaming(Measurement.AACChannel.DestinationStream, Canceller, MeasurementMaxDuration);
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
												VideoChannel.DoStreaming(Measurement.H264IChannel.DestinationStream, Canceller, MeasurementMaxDuration);
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
							}
							finally {
								MeasurementLock.WriteUnLock();
							}
							//.
							DoOnMeasurementFinish(Measurement);
						}
					}
					finally {
						VideoChannel.SourceChannel_Stop();
					}
				}
				finally {
					VideoChannel.Resume();
				}
			}
			finally {
				AudioChannel.SourceChannel_Stop();
			}
		}
		finally {
			AudioChannel.Resume();
		}
	}
}
