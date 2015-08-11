package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Video.H264I;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.geoscope.Classes.IO.Memory.Buffering.TMemoryBuffering;
import com.geoscope.Classes.IO.Memory.Buffering.TMemoryBuffering.TBuffer;
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
	private static final int	CodecBufferingInterval = 2000; //. milliseconds

	
	private TH264IChannel H264Channel;
	//.
	public TMemoryBuffering Buffering = null;
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
		int BuffersCount = 2;
		if (H264Channel.FrameRate > 0) {
			int FrameDelay = 1000/H264Channel.FrameRate;
			BuffersCount = CodecBufferingInterval/FrameDelay;
		}
		//.
		Buffering = new TMemoryBuffering(BuffersCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
			
			private int 	ABuffer_Size = 0;
			private byte[] 	ABuffer = new byte[8192];
			//.
			private long LastFrameTimestamp = -1;
			
			@Override
			public void DoOnBufferDequeue(TBuffer Buffer) {
				try {
					long Timestamp;
					synchronized (Buffer) {
						Timestamp = Buffer.Timestamp;
						//.
						ABuffer_Size = Buffer.Size;
						if (ABuffer_Size > ABuffer.length)
							ABuffer = new byte[ABuffer_Size];
						System.arraycopy(Buffer.Data,0, ABuffer,0, ABuffer_Size);
					}
					//.
					synchronized (CodecLock) {
						if (Codec != null) {
							int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
							if (inputBufferIndex >= 0) {
								ByteBuffer inputBuffer = CodecInputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(ABuffer, 0, ABuffer_Size);
								Codec.queueInputBuffer(inputBufferIndex, 0, ABuffer_Size, Timestamp, 0);
							}
							//.
							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
							while (outputBufferIndex >= 0) {
								//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								//.
								if ((LastFrameTimestamp == -1) && (bufferInfo.presentationTimeUs != 0))
									LastFrameTimestamp = bufferInfo.presentationTimeUs;
								//.
								if (LastFrameTimestamp != -1) {
									int Delay = (int)(bufferInfo.presentationTimeUs-LastFrameTimestamp);
									LastFrameTimestamp = bufferInfo.presentationTimeUs;
									//.
									if (Delay > 0)
										Thread.sleep(Delay);
								}
								//.
								Codec.releaseOutputBuffer(outputBufferIndex, true);
								outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
								//.
								VideoBuffersCount++;
								if ((StatisticHandler != null) && ((VideoBuffersCount % 10) == 0))
									StatisticHandler.DoOnVideoBuffer(VideoBuffersCount);
							}
							if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
							     CodecOutputBuffers = Codec.getOutputBuffers();
							else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
							     // Subsequent data will conform to new format.
							     ///? MediaFormat format = _Codec.getOutputFormat();
							}
						}
					}
				} catch (Exception E) {
				}
			}
		});
		//.
		H264Channel.SetOnH264FramesHandler(new TH264IChannel.TDoOnH264FramesHandler() {
			
			@Override
			public void DoOnH264Packet(int Timestamp, byte[] Packet, int PacketOffset,	int PacketSize) throws Exception {
				//. wait for the codec initialization, it is done as soon as video surface is created
				while (true) {
					synchronized (CodecLock) {
						if (Codec != null) 
							break; //. >
					}
					//.
					Thread.sleep(10);
				}
				//.
				Buffering.EnqueueBuffer(Packet, PacketOffset, PacketSize, Timestamp);
			}
		});
	}

	@Override
	public void Destroy() throws Exception {
		Stop();
		//.
		H264Channel.SetOnH264FramesHandler(null);
		//.
		if (Buffering != null) {
			Buffering.Destroy();
			Buffering = null;
		}
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
