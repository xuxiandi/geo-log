package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Video.H264I;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TH264IChannelProcessor extends TChannelProcessor {

	public static class TStatisticHandler {
		
		public void DoOnVideoBuffer(int VideoBuffersCount) {
		}
	}
	
	private static final String CodecTypeName = "video/avc";
	private static final int 	CodecLatency = 1000; //. microseconds
	private static final int 	CodecWaitInterval = 1000000; //. microseconds

	
	private TH264IChannel H264Channel;
	//.
	private int VideoBuffersCount = 0;
	//.
	public volatile TStatisticHandler StatisticHandler = null;
	//.
	private Object 			CodecLock = new Object();
	private MediaCodec 		Codec = null;
	private ByteBuffer[]	CodecInputBuffers;
	@SuppressWarnings("unused")
	private ByteBuffer[] 	CodecOutputBuffers;
	
	public TH264IChannelProcessor(TStreamChannel pChannel) {
		super(pChannel);
		//.
		H264Channel = (TH264IChannel)Channel;
		//.
		H264Channel.SetOnH264FramesHandler(new TH264IChannel.TDoOnH264FramesHandler() {
			
			@Override
			public void DoOnH264Packet(byte[] Packet, int PacketOffset,	int PacketSize) throws Exception {
				while (true) {
					synchronized (CodecLock) {
						if (Codec != null) {
							int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
							if (inputBufferIndex >= 0) {
								ByteBuffer inputBuffer = CodecInputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(Packet, PacketOffset, PacketSize);
								Codec.queueInputBuffer(inputBufferIndex, 0, PacketSize, SystemClock.elapsedRealtime(), 0);
							}
							//.
							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
							while (outputBufferIndex >= 0) {
								//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								//.
								Codec.releaseOutputBuffer(outputBufferIndex, true);
								outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
								//.
								VideoBuffersCount++;
								if (StatisticHandler != null)
									StatisticHandler.DoOnVideoBuffer(VideoBuffersCount);
							}
							if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
							     CodecOutputBuffers = Codec.getOutputBuffers();
							else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
							     // Subsequent data will conform to new format.
							     ///? MediaFormat format = _Codec.getOutputFormat();
							}
							//.
							return; //. ->
						}
					}
					//. wait for the codec initialization, it is done as soon as video surface is created
					Thread.sleep(10);
				}
			}
		});
	}

	@Override
	public void Destroy() throws Exception {
		Stop();
		//.
		H264Channel.SetOnH264FramesHandler(null);
		//.
		super.Destroy();
	}
	
	public void Start(Surface pSurface, int pWidth, int pHeight) {
		synchronized (CodecLock) {
			Codec = MediaCodec.createDecoderByType(CodecTypeName);
			MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, pWidth,pHeight);
			Codec.configure(format, pSurface, null, 0);
			Codec.start();
			//.
			CodecInputBuffers = Codec.getInputBuffers();
			CodecOutputBuffers = Codec.getOutputBuffers();
		}
	}
	
	public void Stop() {
		synchronized (CodecLock) {
			if (Codec != null) {
				Codec.stop();
				Codec.release();
				Codec = null;
			}
		}
	}
}
