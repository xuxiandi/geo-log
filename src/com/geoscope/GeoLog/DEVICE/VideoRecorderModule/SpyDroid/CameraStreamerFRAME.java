package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

@SuppressLint("NewApi")
public class CameraStreamerFRAME extends Camera {
	
	private com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel	AACSourceChannel = null;
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel 						AACChannel = null;
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber AACChannelSubscriber = new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber() {
		
		@Override
		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			if (Measurement != null)
				Measurement.AACChannel.DestinationStream.write(Packet, 0, PacketSize);
		}
	};
	//.
	private com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel	H264ISourceChannel = null;
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel 						H264IChannel = null;
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber H264IChannelSubscriber = new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber() {
		
		@Override
		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			if (Measurement != null)
				Measurement.H264IChannel.DestinationStream.write(Packet, 0, PacketSize);
		}
	};
	//.
	private TMeasurement Measurement = null;
	
	public CameraStreamerFRAME(TVideoRecorderModule pVideoRecorderModule) {
		super(pVideoRecorderModule);
	}
	
	@Override
	public void Destroy() throws Exception {
		Finalize();
	}
	 
	@Override
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, long UserID, String UserPassword, long pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview, double MaxMeasurementDuration) throws Exception {
		flAudio = pflAudio;
		flVideo = pflVideo;
		flTransmitting = pflTransmitting;
		flSaving = pflSaving;
		//.
		synchronized (this) {
			if (flSaving) {
				MeasurementID = TSensorsModuleMeasurements.CreateNewMeasurement();
				MeasurementLock = TNamedReadWriteLock.WriteLock(TSensorsModuleMeasurements.Domain, MeasurementID); //. lock new measurement
				//.
				Measurement = new TMeasurement(VideoRecorderModule.Device.SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, TSensorsModuleMeasurements.Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
				if (!flAudio) {
					Measurement.Descriptor.Model.Stream.Channels_Remove(Measurement.AACChannel);
					Measurement.AACChannel = null;
				}
				if (!flVideo) { 
					Measurement.Descriptor.Model.Stream.Channels_Remove(Measurement.H264IChannel);
					Measurement.H264IChannel = null;
				}
				//.
				MeasurementDescriptor = (TMeasurementDescriptor)Measurement.Descriptor;
				MeasurementFolder = Measurement.Folder();
			}
			else {  
				MeasurementID = null;
				MeasurementDescriptor = null;
				MeasurementFolder = "";
				Measurement = null;
			}
		}
		try {
			//. AUDIO
			if (flAudio) {
				AACSourceChannel = VideoRecorderModule.Device.SensorsModule.InternalSensorsModule.AACChannel;
				//.
				AACChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel)AACSourceChannel.DestinationChannel_Get();
		        //. setting FrameServer
		        synchronized (VideoRecorderModule.MediaFrameServer.CurrentSamplePacket) {
		        	VideoRecorderModule.MediaFrameServer.SampleRate = AACSourceChannel.GetSampleRate();
		        	VideoRecorderModule.MediaFrameServer.SamplePacketInterval = 10;
		        	VideoRecorderModule.MediaFrameServer.SampleBitRate = abr;
				}
				//. setup measurement
		        if (Measurement != null) {
					Measurement.AACChannel.Assign(AACSourceChannel);
					Measurement.AACChannel.SampleRate = AACSourceChannel.GetSampleRate();
		        }
			}
			//. VIDEO
			if (flVideo) {
				H264ISourceChannel = VideoRecorderModule.Device.SensorsModule.InternalSensorsModule.H264IChannel;
				//.
				H264IChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)H264ISourceChannel.DestinationChannel_Get();
		        //. setting FrameServer
		        synchronized (VideoRecorderModule.MediaFrameServer.CurrentFrame) {
		        	VideoRecorderModule.MediaFrameServer.FrameSize = H264ISourceChannel.GetFrameSize();
		        	VideoRecorderModule.MediaFrameServer.FrameRate = H264ISourceChannel.GetFrameRate();
		        	VideoRecorderModule.MediaFrameServer.FrameInterval = (int)(1000/H264ISourceChannel.GetFrameRate());
		        	VideoRecorderModule.MediaFrameServer.FrameBitRate = br;
		        	VideoRecorderModule.MediaFrameServer.FramePixelFormat = 0; 
				}
				//. setup measurement
		        if (Measurement != null) {
					Measurement.H264IChannel.Assign(H264ISourceChannel);
					Measurement.H264IChannel.FrameRate = H264ISourceChannel.GetFrameRate();
		        }
				//. setup preview
		        if (!H264ISourceChannel.IsActive()) {
		        	Surface 	Preview = null;
		        	Rect 		PreviewFrame = null;
		        	if (pflPreview && (holder != null)) {
		        		Preview = holder.getSurface();
		        		PreviewFrame = holder.getSurfaceFrame();
		        	}
		        	H264ISourceChannel.Preview = Preview;
		        	H264ISourceChannel.PreviewFrame = PreviewFrame;
		        }
			}
		}
		catch (Exception E) {
			if (MeasurementID != null) {
				try {
					if (flVideo) {
						if (H264IChannel.PacketSubscribers.SubscriberExists(H264IChannelSubscriber)) 
							H264IChannel.PacketSubscribers.Unsubscribe(H264IChannelSubscriber);
						//.
			        	H264ISourceChannel.Preview = null;
			        	H264ISourceChannel.PreviewFrame = null;
					}
					//.
					if (flAudio) {
						if (AACChannel.PacketSubscribers.SubscriberExists(AACChannelSubscriber)) 
							AACChannel.PacketSubscribers.Unsubscribe(AACChannelSubscriber);
						AACSourceChannel.AudioSampleSource.OnAudiaPacketHandler = null;
					}
				}
				finally {
					String _MeasurementID = MeasurementID;
					//.
					synchronized (this) {
						MeasurementID = null;
						MeasurementDescriptor = null;
						MeasurementFolder = "";
					}
					//.
					TSensorsModuleMeasurements.DeleteMeasurement(_MeasurementID);
				}
				//.
				throw E; //. =>
			}
		}
	}
	
	@Override
	public void Finalize() throws Exception {
		super.Finalize();
		//.
		if (flVideo) {
			if (H264IChannel.PacketSubscribers.SubscriberExists(H264IChannelSubscriber)) 
				H264IChannel.PacketSubscribers.Unsubscribe(H264IChannelSubscriber);
			//.
        	H264ISourceChannel.Preview = null;
        	H264ISourceChannel.PreviewFrame = null;
		}
		//.
		if (flAudio) {
			if (AACChannel.PacketSubscribers.SubscriberExists(AACChannelSubscriber)) 
				AACChannel.PacketSubscribers.Unsubscribe(AACChannelSubscriber);
			AACSourceChannel.AudioSampleSource.OnAudiaPacketHandler = null;
		}
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		if (Measurement != null)
			Measurement.Start();
		//. Start audio streaming
		if (flAudio) {
			//.
			AACSourceChannel.AudioSampleSource.OnAudiaPacketHandler = new com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel.TOnAudiaPacketHandler() {
				
				@Override
				protected void DoOnAudioPacket(byte[] Packet, int PacketSize) {
					long Timestamp = System.nanoTime()/1000;
					//.
			        VideoRecorderModule.MediaFrameServer.CurrentSamplePacket.Set(Packet,PacketSize, Timestamp);
			        //.
			        try {
			        	VideoRecorderModule.MediaFrameServer.CurrentSamplePacketSubscribers.DoOnPacket(Packet,PacketSize, Timestamp);
					} 
					catch (IOException IOE) {
					}
					catch (Exception E) {
					}
				}
			};
			if (!AACChannel.PacketSubscribers.SubscriberExists(AACChannelSubscriber))
				AACChannel.PacketSubscribers.Subscribe(AACChannelSubscriber);
	        //.
			VideoRecorderModule.MediaFrameServer.flAudioActive = true;
		}
		//. Start video streaming
		if (flVideo) {
			if (!H264IChannel.PacketSubscribers.SubscriberExists(H264IChannelSubscriber))
				H264IChannel.PacketSubscribers.Subscribe(H264IChannelSubscriber);
	        //.
	        VideoRecorderModule.MediaFrameServer.flVideoActive = true;
		}
	}
	
	@Override
	public void Stop() throws Exception {
		//. Stop video streaming
		if (flVideo) {
			VideoRecorderModule.MediaFrameServer.flVideoActive = false;
			//.
			if (H264IChannel.PacketSubscribers.SubscriberExists(H264IChannelSubscriber)) 
				H264IChannel.PacketSubscribers.Unsubscribe(H264IChannelSubscriber);
			//.
        	H264ISourceChannel.Preview = null;
        	H264ISourceChannel.PreviewFrame = null;
		}
		//. Stop audio streaming
		if (flAudio) {
			VideoRecorderModule.MediaFrameServer.flAudioActive = false;
			//.
			if (AACChannel.PacketSubscribers.SubscriberExists(AACChannelSubscriber)) 
				AACChannel.PacketSubscribers.Unsubscribe(AACChannelSubscriber);
			AACSourceChannel.AudioSampleSource.OnAudiaPacketHandler = null;
		}
		//. Finish the measurement
		if (MeasurementID != null) {
			//.
			Measurement.Finish();
			//.
			MeasurementLock.WriteUnLock();
			//.
			MeasurementID = null;
		}
	}
	
	@Override
	public void StartTransmitting(long pidGeographServerObject) {
		flTransmitting = true;
	}
	
	@Override
	public void FinishTransmitting() {
		flTransmitting = false;
	}
	
	@Override
	public synchronized TMeasurementDescriptor GetMeasurementCurrentDescriptor() throws Exception {
		if (Measurement == null)
			return null; //. ->
		//.
		int AudioSampleEncoderPackets = 0;
		if (Measurement.AACChannel != null)
			AudioSampleEncoderPackets = Measurement.AACChannel.Packets;
		//.
		int VideoFrameEncoderPackets = 0;
		if (Measurement.H264IChannel != null)
			VideoFrameEncoderPackets = Measurement.H264IChannel.Packets;
		//.
		MeasurementDescriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//. 
		MeasurementDescriptor.AudioPackets = AudioSampleEncoderPackets;
		MeasurementDescriptor.VideoPackets = VideoFrameEncoderPackets;
		//.
		return MeasurementDescriptor;
	}
}
