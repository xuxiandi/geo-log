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
	private volatile MediaCodec Codec = null;
	private ByteBuffer[] 		CodecInputBuffers;
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
				//. wait for the codec initialization, it is done as soon as video surface is created
				MediaCodec _Codec;
				while (true) {
					_Codec = Codec;
					if (_Codec != null)
						break; //. >
					Thread.sleep(10);
				}
				//.
				int inputBufferIndex = _Codec.dequeueInputBuffer(CodecWaitInterval);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = CodecInputBuffers[inputBufferIndex];
					inputBuffer.clear();
					inputBuffer.put(Packet, PacketOffset, PacketSize);
					_Codec.queueInputBuffer(inputBufferIndex, 0, PacketSize, SystemClock.elapsedRealtime(), 0);
				}
				//.
				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				int outputBufferIndex = _Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
				while (outputBufferIndex >= 0) {
					//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
					//.
					_Codec.releaseOutputBuffer(outputBufferIndex, true);
					outputBufferIndex = _Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
					//.
					VideoBuffersCount++;
					if (StatisticHandler != null)
						StatisticHandler.DoOnVideoBuffer(VideoBuffersCount);
				}
				if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
				     CodecOutputBuffers = _Codec.getOutputBuffers();
				else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				     // Subsequent data will conform to new format.
				     ///? MediaFormat format = _Codec.getOutputFormat();
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
		MediaCodec _Codec = MediaCodec.createDecoderByType(CodecTypeName);
		MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, pWidth,pHeight);
		_Codec.configure(format, pSurface, null, 0);
		_Codec.start();
		//.
		CodecInputBuffers = _Codec.getInputBuffers();
		CodecOutputBuffers = _Codec.getOutputBuffers();
		//.
		Codec = _Codec;
	}
	
	public void Stop() {
		MediaCodec _Codec = Codec;
		Codec = null;
		if (_Codec != null) {
			_Codec.stop();
			_Codec.release();
		}
	}
}
