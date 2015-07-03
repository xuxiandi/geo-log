package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerMyPlayerComponent extends TMeasurementProcessor implements SurfaceHolder.Callback {
    
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID;
	
	public static class TChannelProcessor extends TCancelableThread {
		
		protected TVideoRecorderServerMyPlayerComponent Player;
		//.
		protected int Packets;
		protected int PositionInMs =0;
		//.
		protected MediaCodec Codec;
		protected ByteBuffer[] 	inputBuffers;
		protected ByteBuffer[] 	outputBuffers;
		protected byte[]		outData;
		//.
		public boolean flReady = false;
		protected TAutoResetEvent StartSignal = new TAutoResetEvent();
		public boolean flInitialized = false;
		public boolean flSetPosition = false;
		public boolean flPause = true;
		public boolean flRunning = false;
		public boolean flPlaying = false;
		//.
		protected int PlayedBuffersCount = 0;
		//.
		protected long CurrentPosition_InMs = 0;
		
		public TChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, int pPackets, int pPositionInMs) {
			super();
			//.
			Player = pPlayer;
			Packets = pPackets;
			PositionInMs = pPositionInMs;
			//.
			_Thread = new Thread(this);
		}
		
		public void Destroy(boolean flWaitForTermination) throws InterruptedException {
			if (flWaitForTermination)
				CancelAndWait();
			else
				Cancel();
		}
		
		public void Destroy() throws InterruptedException {
			Destroy(true);
		}
		
		public void Start() {
			StartSignal.Set();
		}
		
		public void Pause() {
			flPause = true;
		}
		
		public void Resume() {
			flPause = false;
		}
		
		public void Set(int pPositionInMs) {
			synchronized (this) {
				PositionInMs = pPositionInMs;
			}
			flSetPosition = true;
		}
		
		public void Set(int pPositionInMs, boolean pflPause) {
			synchronized (this) {
				PositionInMs = pPositionInMs;
			}
			flPause = pflPause;
			//.
			flSetPosition = true;
		}
		
		public synchronized void CurrentPosition_SetInMs(long Value) {
			CurrentPosition_InMs = Value;
		}
		
		public synchronized long CurrentPosition_GetInMs() {
			return CurrentPosition_InMs;
		}
	}
	
	public static class TAudioAACChannelProcessor extends TChannelProcessor {
		
		public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel.TypeID;
		
		public static class TAudioChannelParams {
		
			public int SampleRate;
			public int Packets;
			public String FileName;
			
			public TAudioChannelParams(int pSampleRate, int pPackets, String pFileName) {
				SampleRate = pSampleRate;
				Packets = pPackets;
				FileName = pFileName;
			}
		}
		
		public static TAudioChannelParams GetAudioChannelParams(TChannel Channel) {
			if (Channel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel) {
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel AACChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel)Channel;
				if (AACChannel.Packets > 0)
					return (new TAudioChannelParams(AACChannel.SampleRate, AACChannel.Packets, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.AudioAACADTSFileName)); //. ->
				else
					return null; //. -> 
			}
			else
				if (Channel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC.TAACChannel) {
					com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC.TAACChannel AACChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC.TAACChannel)Channel;
					if (AACChannel.Packets > 0)
						return (new TAudioChannelParams(AACChannel.SampleRate, AACChannel.Packets, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.TMeasurementDescriptor.AudioAACADTSFileName)); //. ->
					else
						return null; //. -> 
				}
				else
					return null; //. -> 
		}
		
		public static boolean ChannelIsMine(TChannel Channel) {
			return (GetAudioChannelParams(Channel) != null);
		}
		
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. microseconds
		private static final int 	CodecWaitInterval = 1000000; //. microseconds

		private static final int 	MinimumOfPlayedBufferCountBeforePausing = 1; 

		
		private String AudioFileName;
		//.
		public int SampleRate;
		//.
		private AudioTrack 	AudioPlayer;

		public TAudioAACChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pAudioFileName, int pPackets, int pSampleRate, int pPositionInMs) {
    		super(pPlayer, pPackets, pPositionInMs);
    		//.
			AudioFileName = pAudioFileName;
			SampleRate = pSampleRate;
			//.
			_Thread.start();
		}
		
		public TAudioAACChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pAudioFileName, int pPackets, int pSampleRate) {
			this(pPlayer, pAudioFileName, pPackets, pSampleRate, 0);
		}
		
		public TAudioAACChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pFolder, TAudioChannelParams AudioChannelParams) {
			this(pPlayer, pFolder+"/"+AudioChannelParams.FileName, AudioChannelParams.Packets, AudioChannelParams.SampleRate, 0);
		}
		
		@Override
		public void run() {
			try {
				RandomAccessFile AudioFile = new RandomAccessFile(AudioFileName, "r");
				try {
					FileChannel AudioFileChannel = AudioFile.getChannel();
					try {
						MappedByteBuffer AudioFileBuffer = AudioFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, AudioFileChannel.size());
						try {
							AudioFileBuffer.load();
							AudioFileBuffer.position(0);
							//. prepare codec configuration data
							byte[] ADTSHeader = new byte[7];
							if (AudioFileBuffer.remaining() < ADTSHeader.length)
								return; //. ->
							AudioFileBuffer.get(ADTSHeader);
							int AudioBufferSize = (((ADTSHeader[3] & 0x3) << 10) | ((ADTSHeader[4] & 0xFF) << 3) | ((ADTSHeader[5] & 0xE0) >> 5)); 
							//.
							byte ObjectType = (byte)(((ADTSHeader[2] & 0xFF) >> 6)+1); 
							byte FrequencyIndex = (byte)((ADTSHeader[2] & 0x3C) >> 2);
							byte ChannelConfiguration = (byte)(((ADTSHeader[2] & 0x01) << 2) | ((ADTSHeader[3] & 0xC0) >> 6));
							//.
							byte[] ConfigWordBA = new byte[2];
							ConfigWordBA[0] = (byte)(((ObjectType << 3) & 0xF8) | ((FrequencyIndex >> 1) & 0x07));
							ConfigWordBA[1] = (byte)(((FrequencyIndex & 0x01) << 7) | ((ChannelConfiguration & 0x0F) << 3));
							//.
							int IdxBase = AudioBufferSize;
							//. prepare file indexes
							ByteBuffer AudioFileIndexes;
							int AudioFileIndexesCount;
							ByteArrayOutputStream BOS = new ByteArrayOutputStream((int)(AudioFileChannel.size() >> 9));
							try {
								DataOutputStream DOS = new DataOutputStream(BOS);
								try {
									AudioFileBuffer.position(IdxBase);
									while (AudioFileBuffer.remaining() > ADTSHeader.length) {
										int Pos = AudioFileBuffer.position();
										DOS.writeInt(Pos);
										AudioFileBuffer.get(ADTSHeader);
										AudioBufferSize = (((ADTSHeader[3] & 0x3) << 10) | ((ADTSHeader[4] & 0xFF) << 3) | ((ADTSHeader[5] & 0xE0) >> 5));
										//.
										Pos += AudioBufferSize;
										AudioFileBuffer.position(Pos);
										//.
										if (Canceller.flCancel)
											return; //. >
									}
								}
								finally {
									DOS.close();
								}
								AudioFileIndexes = ByteBuffer.wrap(BOS.toByteArray());
								AudioFileIndexesCount = (AudioFileIndexes.limit() >> 2/*SizeOf(Index)*/); 
								if (AudioFileIndexesCount == 0)
									return; //. >
							}
							finally {
								BOS.close();
							}
							//.
							Codec = MediaCodec.createDecoderByType(CodecTypeName);
							try {
								MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName,SampleRate,1);
								Codec.configure(format, null, null, 0);
								Codec.start();
								try {
									inputBuffers = Codec.getInputBuffers();
									outputBuffers = Codec.getOutputBuffers();
									outData = new byte[0];
									byte[] Buffer = new byte[0];
									//.
									if (android.os.Build.VERSION.SDK_INT <= 16) 
										SampleRate*=2; //. workaround for a bug
									int SampleInterval = 20; //. ms
									int SampleSize = 2;
									int BufferSize = (SampleSize*SampleRate/1000)*SampleInterval;							    	
									AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize, AudioTrack.MODE_STREAM);
							    	AudioPlayer.setStereoVolume(1.0F,1.0F);
							    	AudioPlayer.play();
							    	try {
										Canceller.Check();
										//.
										DecodeInputBuffer(ConfigWordBA,0,ConfigWordBA.length,0,1);
										//.
										while (!Canceller.flCancel) {
											int StartIndex;
											int PositionIndex = 0;
											if (flInitialized) {
												synchronized (this) {
													PositionIndex = (int)(AudioFileIndexesCount*(PositionInMs+0.0)/Player.Measurement.Descriptor.DurationInMs());
												}
												if (PositionIndex >= AudioFileIndexesCount) 
													PositionIndex = AudioFileIndexesCount-1;
												//.
												StartIndex = AudioFileIndexes.getInt(PositionIndex << 2);
											}
											else {
												StartIndex = 0;
												//.
												flInitialized = true;
											}
											//.
											flReady = true;
											Player.MessageHandler.obtainMessage(MESSAGE_AUDIOCHANNELPROCESSOR_ISREADY).sendToTarget();
											//.
											StartSignal.WaitOne();
											//.
											PositionIndex++;
											PlayedBuffersCount = 0;
											for (int I = PositionIndex; I < AudioFileIndexesCount; I++) {
												int FinishIndex = AudioFileIndexes.getInt(I << 2);
												//.
												BufferSize = (FinishIndex-StartIndex);
												if (BufferSize > Buffer.length)
													Buffer = new byte[BufferSize];
												AudioFileBuffer.position(StartIndex);
												AudioFileBuffer.get(Buffer, 0,BufferSize);
												//.
												boolean flProtectionAbsent = ((Buffer[1] & 1) != 0);
												AudioBufferSize = (((Buffer[3] & 0x3) << 10) | ((Buffer[4] & 0xFF) << 3) | ((Buffer[5] & 0xE0) >> 5)); 
												int ADTSHeaderSize;
												if (flProtectionAbsent) 
													ADTSHeaderSize = 7;
												else
													ADTSHeaderSize = 9;
												//.
										    	DecodeInputBuffer(Buffer,  ADTSHeaderSize,BufferSize-ADTSHeaderSize, I-1,AudioFileIndexesCount);
												//.
										    	Player.MessageHandler.obtainMessage(MESSAGE_AUDIOPLAYING_PROGRESS,(double)((I-1.0)/AudioFileIndexesCount)).sendToTarget();
												//.
										    	if (flRunning && (PlayedBuffersCount >= MinimumOfPlayedBufferCountBeforePausing))
													while (flPause) {
														Thread.sleep(10);
														if (Canceller.flCancel | flSetPosition)
															break; //. >
													}
												//.
												if (Canceller.flCancel | flSetPosition)
													break; //. >
												//.
												StartIndex = FinishIndex;
											}
											//.
											Codec.flush();
											//. wait for re-position
									    	if (flRunning) {
												while (!flSetPosition) {
													Canceller.Check();
													//.
													Thread.sleep(10);
												}
												flSetPosition = false;
												flPlaying = true;
									    	}
										}
							    	}
							    	finally {
							    		AudioPlayer.stop();
							    	}
								}
								finally {
									Codec.stop();
								}
							}
							finally {
								Codec.release();
							}
						}
						finally {
							AudioFileBuffer = null;
							//.
							System.gc();
						}
					}
					finally {
						AudioFileChannel.close();
					}
				}
				finally {
					AudioFile.close();
				}
			}
			catch (InterruptedException IE) {
			}
			catch (CancelException CE) {
			}
			catch (Throwable T) {
				if (!Canceller.flCancel)
					Player.DoOnException(T);
			}
		}
		
		public void DecodeInputBuffer(byte[] input, int input_offset, int input_size, int Index, int Count) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, input_offset,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0,input_size, (long)(1.0*Player.Measurement.Descriptor.DurationInMs()*Index/Count), 0);
			}
			//.
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			while (outputBufferIndex >= 0) {
				//.
				flRunning = true;
				//.
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				if (outData.length < bufferInfo.size)
					outData = new byte[bufferInfo.size];
				outputBuffer.rewind(); //. reset position to 0
				outputBuffer.get(outData, 0,bufferInfo.size);
				//. process output
				if (flPlaying) {
					AudioPlayer.write(outData, 0,bufferInfo.size);
					PlayedBuffersCount++;
				}
				//.
				CurrentPosition_SetInMs(bufferInfo.presentationTimeUs);
				//.
				Codec.releaseOutputBuffer(outputBufferIndex, false);
				//.
				outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			}
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
			     outputBuffers = Codec.getOutputBuffers();
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			     // Subsequent data will conform to new format.
			     ///? MediaFormat format = codec.getOutputFormat();
			}
		}		
	}
	
	public static class TVideoH264IChannelProcessor extends TChannelProcessor {
		
		public static final String TypeID = TH264IChannel.TypeID;
		
		public static class TVideoChannelParams {
			
			public int FrameRate;
			public int Packets;
			public String FileName;
			public String IndexFileName;
			public String TimestampFileName;
			
			public TVideoChannelParams(int pFrameRate, int pPackets, String pFileName, String pIndexFileName, String pTimestampFileName) {
				FrameRate = pFrameRate;
				Packets = pPackets;
				FileName = pFileName;
				IndexFileName = pIndexFileName;
				TimestampFileName = pTimestampFileName;
			}
		}
		
		public static TVideoChannelParams GetVideoChannelParams(TChannel Channel) {
			if (Channel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I.TH264IChannel) {
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I.TH264IChannel H264IChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)Channel;
				if (H264IChannel.Packets > 0)
					return (new TVideoChannelParams(H264IChannel.FrameRate, H264IChannel.Packets, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurementDescriptor.VideoH264FileName,com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurementDescriptor.VideoIndex32FileName,com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurementDescriptor.VideoTS32FileName)); //. ->
				else
					return null; //. -> 
			}
			else
				if (Channel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel) {
					com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel H264IChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)Channel;
					if (H264IChannel.Packets > 0)
						return (new TVideoChannelParams(H264IChannel.FrameRate, H264IChannel.Packets, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.VideoH264FileName,com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.VideoIndex32FileName,com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.VideoTS32FileName)); //. ->
					else
						return null; //. -> 
				}
				else
					return null; //. -> 
		}
		
		public static boolean ChannelIsMine(TChannel Channel) {
			return (GetVideoChannelParams(Channel) != null);
		}
		
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 1000; //. microseconds
		private static final int 	CodecWaitInterval = 1000000; //. microseconds

		private static final int MinimumOfPlayedBufferCountBeforePausing = 1;
		
		private String VideoFileName;
		private String VideoIndexFileName;
		private String VideoTimestampFileName;
		//.
		@SuppressWarnings("unused")
		private int 	FrameRate;
		//.
		private SurfaceHolder surface;
		//.
		private int 	Width;
		private int 	Height;
		//.
		private long TimestampBase = 0;		
		
		public TVideoH264IChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pVideoFileName, String pVideoIndexFileName, String pVideoTimestampFileName, int pPackets, int pFrameRate, SurfaceHolder pSurface, int pWidth, int pHeight, int pPositionInMs) {
    		super(pPlayer, pPackets, pPositionInMs);
    		//.
			VideoFileName = pVideoFileName;
			VideoIndexFileName = pVideoIndexFileName;
			VideoTimestampFileName = pVideoTimestampFileName;
			FrameRate = pFrameRate;
			surface = pSurface;
			Width = pWidth;
			Height = pHeight;
			//.
			_Thread.start();
		}
		
		public TVideoH264IChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pVideoFileName, String pVideoIndexFileName, String pVideoTimestampFileName, int pPackets, int pFrameRate, SurfaceHolder pSurface, int pWidth, int pHeight) {
			this(pPlayer, pVideoFileName,pVideoIndexFileName,pVideoTimestampFileName, pPackets, pFrameRate, pSurface, pWidth,pHeight, 0);
		}
		
		public TVideoH264IChannelProcessor(TVideoRecorderServerMyPlayerComponent pPlayer, String pFolder, TVideoChannelParams VideoChannelParams, SurfaceHolder pSurface, int pWidth, int pHeight) {
			this(pPlayer, pFolder+"/"+VideoChannelParams.FileName,pFolder+"/"+VideoChannelParams.IndexFileName,pFolder+"/"+VideoChannelParams.TimestampFileName, VideoChannelParams.Packets, VideoChannelParams.FrameRate, pSurface, pWidth,pHeight);		
		}
		
		@SuppressWarnings("unused")
		private void Background_Draw() throws IllegalArgumentException, OutOfResourcesException {
			if (surface != null) {
				Surface sf = surface.getSurface();
				Canvas canvas = sf.lockCanvas(null);
				try {
					canvas.drawColor(Color.BLACK);
				}
				finally {
					sf.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		@Override
		public void run() {
			try {
				RandomAccessFile VideoFile = new RandomAccessFile(VideoFileName, "r");
				try {
					FileChannel VideoFileChannel = VideoFile.getChannel();
					try {
						long VideoFileBufferSize = VideoFileChannel.size(); 
						MappedByteBuffer VideoFileBuffer = VideoFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, VideoFileBufferSize);
						try {
							VideoFileBuffer.load();
							//. 
							RandomAccessFile VideoIndexFile = null;
							try {
								if (VideoIndexFileName != null) {
									File VIF = new File(VideoIndexFileName); 
									if (VIF.exists())
										VideoIndexFile = new RandomAccessFile(VIF, "r");
								}
								FileChannel VideoIndexFileChannel = null;
								try {
									if (VideoIndexFile != null)
										VideoIndexFileChannel = VideoIndexFile.getChannel();
									ByteBuffer VideoFileIndexes = null;
									if (VideoIndexFileChannel != null) {
										VideoFileIndexes = VideoIndexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, VideoIndexFileChannel.size());
										((MappedByteBuffer)VideoFileIndexes).load();
										VideoFileIndexes.order(ByteOrder.LITTLE_ENDIAN);
									}
									RandomAccessFile VideoTimestampFile = null;
									try {
										if (VideoTimestampFileName != null) {
											File VTF = new File(VideoTimestampFileName); 
											if (VTF.exists())
												VideoTimestampFile = new RandomAccessFile(VTF, "r");
										}
										FileChannel VideoTimestampFileChannel = null;
										try {
											if (VideoTimestampFile != null)
												VideoTimestampFileChannel = VideoTimestampFile.getChannel();
											//.
											ByteBuffer VideoFileTimestamps = null;
											if (VideoTimestampFileChannel != null) {
												VideoFileTimestamps = VideoTimestampFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, VideoTimestampFileChannel.size());
												((MappedByteBuffer)VideoFileTimestamps).load();
												VideoFileTimestamps.order(ByteOrder.LITTLE_ENDIAN);
											}
											else
												return; //. ->
											//.
											int VideoFileIndexesCount = (VideoFileIndexes.limit() >> 2/*SizeOf(Index)*/); 
											if (VideoFileIndexesCount == 0)
												return; //. >
											/* processing */
											Codec = MediaCodec.createDecoderByType(CodecTypeName);
											try {
												MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, Width,Height);
												Codec.configure(format, surface.getSurface(), null, 0);
												Codec.start();
												try {
													inputBuffers = Codec.getInputBuffers();
													outputBuffers = Codec.getOutputBuffers();
													byte[] Buffer = new byte[0];
													//.
													byte[] 	PacketSignature = new byte[] {0x00,0x00,0x00,0x01};
													int 	PacketSignatureSize = PacketSignature.length; 
													int[] 	IntervalIndexes = new int[1000];
													int		IntervalIndexesCount = 0;
													//.
													while (!Canceller.flCancel) {
														int StartIndex;
														long _PositionInMs = 0;
														int PositionIndex = 0;
														if (flInitialized) {
															if (VideoFileTimestamps != null) {
																synchronized (this) {
																	_PositionInMs = PositionInMs;
																}
																int Limit = VideoFileTimestamps.limit() >> 2;
																while (true) {
																	int NewPositionIndex = ((PositionIndex+Limit) >> 1);
																	if (NewPositionIndex == PositionIndex)
																		break; //. >
																	long TS = VideoFileTimestamps.getInt(NewPositionIndex << 2);
																	if (_PositionInMs < TS) {
																		if (Limit > PositionIndex)
																			Limit = PositionIndex;
																	}
																	else 
																		if (_PositionInMs > TS) {
																			if (Limit < PositionIndex)
																				Limit = PositionIndex;
																		}
																		else
																			break; //. >
																	PositionIndex = NewPositionIndex;
																	//.
																	if (Canceller.flCancel)
																		return; //. >
																}
															}
															if (PositionIndex >= VideoFileIndexesCount)
																PositionIndex = VideoFileIndexesCount-1;
															//.
															StartIndex = VideoFileIndexes.getInt(PositionIndex << 2);
															//.
															TimestampBase = SystemClock.elapsedRealtime()-_PositionInMs;
														}
														else {
															StartIndex = 0;
															//.
															flInitialized = true;
														}
														//.
														flReady = true;
														Player.MessageHandler.obtainMessage(MESSAGE_VIDEOCHANNELPROCESSOR_ISREADY).sendToTarget();
														//.
														StartSignal.WaitOne();
														//.
														PositionIndex++;
														PlayedBuffersCount = 0;
														for (int I = PositionIndex; I < VideoFileIndexesCount; I++) {
															int FinishIndex = VideoFileIndexes.getInt(I << 2);
															//.
															int BufferSize = (FinishIndex-StartIndex);
															if (BufferSize > Buffer.length)
																Buffer = new byte[BufferSize];
															VideoFileBuffer.position(StartIndex);
															VideoFileBuffer.get(Buffer, 0,BufferSize);
															//.
															IntervalIndexesCount = 0;
															int Idx = PacketSignatureSize;
															while (Idx < BufferSize) {
																//. search for a next index
																int PacketSignatureIdx = 0;
																int _FinishIndex = -1;
																while (Idx < BufferSize) {
																	if (Buffer[Idx] == PacketSignature[PacketSignatureIdx]) {
																		Idx++;
																		PacketSignatureIdx++;
																		if (PacketSignatureIdx == PacketSignatureSize) {
																			_FinishIndex = (Idx-PacketSignatureIdx);
																			//.
																			break; //. >
																		}
																	}
																	else { 
																		Idx++;
																		//.
																		PacketSignatureIdx = 0;
																	}
																}
																if (_FinishIndex < 0)
																	break; //. >
																//.
																IntervalIndexes[IntervalIndexesCount] = _FinishIndex;
																IntervalIndexesCount++;
																if (IntervalIndexesCount > IntervalIndexes.length)
																	break; //. >
															}
															if (IntervalIndexesCount < IntervalIndexes.length) {
																IntervalIndexes[IntervalIndexesCount] = BufferSize;
																IntervalIndexesCount++;
															}
															//.
															int TS = 0;
															int dTS = 0;
															if (VideoFileTimestamps != null) { 
																TS = VideoFileTimestamps.getInt((I-1) << 2);
																dTS = (VideoFileTimestamps.getInt(I << 2)-TS)/IntervalIndexesCount;
															}
															//.
															int _StartIndex = 0;
															for (int J = 0; J < IntervalIndexesCount; J++) {
																int _FinishIndex = IntervalIndexes[J];
																//.
														    	DecodeInputBuffer(Codec, Buffer, _StartIndex,(_FinishIndex-_StartIndex), TS);
														    	TS += dTS;
																//.
																if (Player.AudioChannelProcessor == null) 
																	Player.MessageHandler.obtainMessage(MESSAGE_VIDEOPLAYING_PROGRESS,(double)((I-1.0)/VideoFileIndexesCount)).sendToTarget();
																//.
														    	if (flRunning && (PlayedBuffersCount >= MinimumOfPlayedBufferCountBeforePausing))
																	while (flPause) {
																		Thread.sleep(10);
																		Canceller.Check();
																		if (flSetPosition)
																			break; //. >
																	}
																//.
																Canceller.Check();
																if (flRunning && flSetPosition)
																	break; //. >
																//.
																_StartIndex = _FinishIndex;
																
															}
															//.
															if (flSetPosition)
																break; //. >
															//.
															StartIndex = FinishIndex;
														}
														//.
														Codec.flush();
														//. wait for re-position
												    	if (flRunning) {
															while (!flSetPosition) {
																Canceller.Check();
																//.
																Thread.sleep(10);
															}
															flSetPosition = false;
															flPlaying = true;
												    	}
													}
												}
												finally {
													Codec.stop();
												}
											}
											finally {
												Codec.release();
											}
										}
										finally {
											if (VideoTimestampFileChannel != null)
												VideoTimestampFileChannel.close();
										}
									}
									finally {
										if (VideoTimestampFile != null)
											VideoTimestampFile.close();
									}
								}
								finally {
									if (VideoIndexFileChannel != null)
										VideoIndexFileChannel.close();
								}
							}
							finally {
								if (VideoIndexFile != null)
									VideoIndexFile.close();
							}
						}
						finally {
							VideoFileBuffer = null;
							//.
							System.gc();
						}
					}
					finally {
						VideoFileChannel.close();
					}
				}
				finally {
					VideoFile.close();
					//.
					//. Background_Draw();
				}
			}
			catch (InterruptedException IE) {
			}
			catch (CancelException CE) {
			}
			catch (Throwable T) {
				if (!Canceller.flCancel)
					Player.DoOnException(T);
			}
		}
		
		public void DecodeInputBuffer(MediaCodec Codec, byte[] input, int input_offset, int input_size, long Timestamp) throws IOException, InterruptedException {
			int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, input_offset,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0,input_size, Timestamp, 0);
			}
			//.
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			while (outputBufferIndex >= 0) {
				//.
				flRunning = true;
				//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				//. synchronizing video
				if (flPlaying)
					if (Player.AudioChannelProcessor != null) {
						while (true) {
							long Delta = bufferInfo.presentationTimeUs-Player.AudioChannelProcessor.CurrentPosition_GetInMs();
							if (Delta <= 0)
								break; //. >
							Thread.sleep(Delta % 100);
							//.
							if (Canceller.flCancel | flSetPosition)
								return; //. >
							//.
							if (flPause)
								break; //. >
						}
					} 
					else {
						while (true) {
							long Delta = bufferInfo.presentationTimeUs-(SystemClock.elapsedRealtime()-TimestampBase);
							if (Delta <= 0)
								break; //. >
							Thread.sleep(Delta % 100);
							//.
							if (Canceller.flCancel | flSetPosition)
								return; //. >
							//.
							if (flPause)
								break; //. >
						}
					}
				//. process output
				if (flPlaying) {
					Codec.releaseOutputBuffer(outputBufferIndex, true);
					PlayedBuffersCount++;
				}
				//.
				outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			}
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
			     outputBuffers = Codec.getOutputBuffers();
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			     // Subsequent data will conform to new format.
			     ///? MediaFormat format = codec.getOutputFormat();
			}
		}		
	}
	
	private static final int MESSAGE_SHOWEXCEPTION 					= 1;
	private static final int MESSAGE_AUDIOCHANNELPROCESSOR_ISREADY 	= 2;
	private static final int MESSAGE_VIDEOCHANNELPROCESSOR_ISREADY 	= 3;
	private static final int MESSAGE_AUDIOPLAYING_PROGRESS 			= 4;
	private static final int MESSAGE_VIDEOPLAYING_PROGRESS 			= 5;

	
	public Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(ParentActivity,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >
    				
    			case MESSAGE_AUDIOCHANNELPROCESSOR_ISREADY:
    				if (VideoChannelProcessor != null) {
    					if (VideoChannelProcessor.flReady) {
    						if (AudioChannelProcessor != null)
    							AudioChannelProcessor.Start();
    						VideoChannelProcessor.Start();
    					}
    				}
    				else 
    					if (AudioChannelProcessor != null)
    						AudioChannelProcessor.Start();
    				break; // . >

    			case MESSAGE_VIDEOCHANNELPROCESSOR_ISREADY:
    				if (AudioChannelProcessor != null) {
    					if (AudioChannelProcessor.flReady) {
    						AudioChannelProcessor.Start();
    						if (VideoChannelProcessor != null)
    							VideoChannelProcessor.Start();
    					}
    				}
    				else 
    					if (VideoChannelProcessor != null)
    						VideoChannelProcessor.Start();
    				break; // . >

    			case MESSAGE_AUDIOPLAYING_PROGRESS:
    				if ((AudioChannelProcessor != null) && AudioChannelProcessor.flPlaying && !AudioChannelProcessor.Canceller.flCancel) {
        				double ProgressFactor = (Double)msg.obj;
        				DoOnPlayingProgress(ProgressFactor);
    				}
    				//.
    				break; // . >
    				
    			case MESSAGE_VIDEOPLAYING_PROGRESS:
    				if ((VideoChannelProcessor != null) && VideoChannelProcessor.flPlaying && !VideoChannelProcessor.Canceller.flCancel) {
        				double ProgressFactor = (Double)msg.obj;
        				DoOnPlayingProgress(ProgressFactor);
    				}
    				//.
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	public double 			MeasurementCurrentPositionFactor = 0.0;
	//.
	private SurfaceView 	svVideoRecorderServerMyPlayer;
	public TextView 		lbVideoRecorderServerMyPlayer;
	public SeekBar 			sbVideoRecorderServerMyPlayer;
	public CheckBox			cbVideoRecorderServerMyPlayerPause;
	private ImageView		ivVideoRecorderServerMyPlayerAudioOnly;
	//.
	private boolean 			flAudio = false;
	private TChannelProcessor	AudioChannelProcessor = null;
	//.
	private boolean 			flVideo = false;
	private TChannelProcessor	VideoChannelProcessor = null;
	//.
	private TAsyncProcessing Positioning = null;
	
	public TVideoRecorderServerMyPlayerComponent() {
		super();
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) throws Exception {
		super.SetLayout(pParentActivity, pParentLayout);
		//.
		LayoutInflater inflater = (LayoutInflater)ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.video_recorder_server_myplayer_layout, ParentLayout);
        //.
        svVideoRecorderServerMyPlayer = (SurfaceView)ParentLayout.findViewById(R.id.svVideoRecorderServerMyPlayer);
        svVideoRecorderServerMyPlayer.getHolder().addCallback(this);
        svVideoRecorderServerMyPlayer.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (!IsSetup())
					return false; //. ->
				//.
	    		final CharSequence[] _items;
	    		int SelectedIdx = -1;
	    		_items = new CharSequence[1];	    		
	    		_items[0] = ParentActivity.getString(R.string.SExportToFile); 
	    		//.
	    		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
	    		builder.setTitle(R.string.SSelect);
	    		builder.setNegativeButton(R.string.SClose,null);
	    		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
	    			
	    			@Override
	    			public void onClick(DialogInterface arg0, int arg1) {
	    		    	try {
	    		    		switch (arg1) {
	    		    		
	    		    		case 0: //. export to a file
	    		    			TAsyncProcessing Exporting = new TAsyncProcessing(ParentActivity) {

	    		    				private static final String ExpertFileName = "Clip.mp4";
	    		    				
	    		    				private String ExportFile;
	    		    				
	    		    				@Override
	    		    				public void Process() throws Exception {
	    		    					ExportFile = TGeoLogApplication.TempFolder+"/"+ExpertFileName;
	    		    					//.
	    		    					com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.ExportMeasurementToMP4File(Measurement.DatabaseFolder, Measurement.DatabaseFolder, Measurement.Descriptor.ID, ExportFile);
	    		    				}

	    		    				@Override
	    		    				public void DoOnCompleted() throws Exception {
	    		    					if (!Canceller.flCancel) {
	    		    					    new AlertDialog.Builder(ParentActivity)
	    		    				        .setIcon(android.R.drawable.ic_dialog_info)
	    		    				        .setTitle(R.string.SOperationIsDone)
	    		    				        .setMessage(ParentActivity.getString(R.string.SAVDataHasBeenExportedToFile)+ExportFile)
	    		    					    .setPositiveButton(R.string.SOpen, new DialogInterface.OnClickListener() {

	    		    					    	@Override
	    		    					    	public void onClick(DialogInterface dialog, int id) {
	    		    								try {
	    		    									Intent intent = new Intent();
	    		    									intent.setDataAndType(Uri.fromFile(new File(ExportFile)), "video/*");
	    		    									intent.setAction(android.content.Intent.ACTION_VIEW);
	    		    									ParentActivity.startActivity(intent);
	    		    								} catch (Exception E) {
	    		    									Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
	    		    									return; // . ->
	    		    								}
	    		    					    	}
	    		    					    })
	    		    					    .setNegativeButton(R.string.SClose, null)
	    		    					    .show();
	    		    					}
	    		    				}
	    		    			};
	    		    			Exporting.Start();
	    						//.
	        		    		arg0.dismiss();
	        		    		//.
	    		    			break; //. >
	    		    		}
	    		    	}
	    		    	catch (Exception E) {
	    		    		Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
	    		    		//.
	    		    		arg0.dismiss();
	    		    	}
	    			}
	    		});
	    		AlertDialog alert = builder.create();
	    		alert.show();
				return true;
			}
		});
        //.
        lbVideoRecorderServerMyPlayer = (TextView)ParentLayout.findViewById(R.id.lbVideoRecorderServerMyPlayer);
        //.
        cbVideoRecorderServerMyPlayerPause = (CheckBox)ParentLayout.findViewById(R.id.cbVideoRecorderServerMyPlayerPause);
        //.
        sbVideoRecorderServerMyPlayer = (SeekBar)ParentLayout.findViewById(R.id.sbVideoRecorderServerMyPlayer);
        sbVideoRecorderServerMyPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        	
        	@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
        	@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
        	@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					if (Measurement != null) {
						double Position = (Measurement.Descriptor.Duration()*progress/100.0);
						DoSetPosition(Position);
					}
				}
			}
		});
		sbVideoRecorderServerMyPlayer.setVisibility(View.GONE);
        //.
        cbVideoRecorderServerMyPlayerPause = (CheckBox)ParentLayout.findViewById(R.id.cbVideoRecorderServerMyPlayerPause);
        cbVideoRecorderServerMyPlayerPause.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
                //.
                if (checked)
                	Pause();
                else
                	Resume();
            }
        });
        //.
        ivVideoRecorderServerMyPlayerAudioOnly = (ImageView)ParentLayout.findViewById(R.id.ivVideoRecorderServerMyPlayerAudioOnly);
		//.
        if (flStandalone)
        	cbVideoRecorderServerMyPlayerPause.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}
	
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		surface = null;
		//.
		synchronized (this) {
			if (VideoChannelProcessor != null) {
				try {
					VideoChannelProcessor.Destroy();
				} catch (InterruptedException E) {
				}
				VideoChannelProcessor = null;
			}
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		surface = arg0;
		Width = arg2;
		Height = arg3;
		//. re-initialize video
		if ((VideoChannelProcessor == null) && ((Measurement != null) && (Measurement.Descriptor.Model != null))) {
			int Cnt = Measurement.Descriptor.Model.Stream.Channels.size();
			for (int C = 0; C < Cnt; C++) {
				TChannel Channel = Measurement.Descriptor.Model.Stream.Channels.get(C);
				//.
				if (TVideoH264IChannelProcessor.ChannelIsMine(Channel)) {
					flVideo = true;
					//.
					synchronized (this) {
						VideoChannelProcessor = new TVideoH264IChannelProcessor(this, Measurement.Folder(), TVideoH264IChannelProcessor.GetVideoChannelParams(Channel), surface, Width,Height);
					}
				}
			}
		}
		//.
		if (OnVideoSurfaceChangedHandler != null) 
			OnVideoSurfaceChangedHandler.DoOnSurfaceChanged(surface);
		//.
		ShowInfo();
	}
	
	@Override
	protected void Initialize(TSensorMeasurement pMeasurement) throws Exception {
		super.Initialize(pMeasurement);
		//.
		if (Measurement.Descriptor.Model != null) {
			int Cnt = Measurement.Descriptor.Model.Stream.Channels.size();
			for (int C = 0; C < Cnt; C++) {
				TChannel Channel = Measurement.Descriptor.Model.Stream.Channels.get(C);
				//.
				if (TAudioAACChannelProcessor.ChannelIsMine(Channel)) {
					flAudio = true;
					//.
					synchronized (this) {
						if (AudioChannelProcessor != null) { 
							AudioChannelProcessor.Destroy();
							AudioChannelProcessor = null;
						}
						AudioChannelProcessor = new TAudioAACChannelProcessor(this, Measurement.Folder(), TAudioAACChannelProcessor.GetAudioChannelParams(Channel));
					}
				}
				//.
				if ((surface != null) && TVideoH264IChannelProcessor.ChannelIsMine(Channel)) {
					flVideo = true;
					//.
					synchronized (this) {
						if (VideoChannelProcessor != null) {
							VideoChannelProcessor.Destroy();
							VideoChannelProcessor = null;
						}
						VideoChannelProcessor = new TVideoH264IChannelProcessor(this, Measurement.Folder(), TVideoH264IChannelProcessor.GetVideoChannelParams(Channel), surface, Width,Height);
					}
				}
			}
		}
		//.
		sbVideoRecorderServerMyPlayer.setVisibility(View.VISIBLE);
		//.
		ShowInfo();
		//.
		flInitialized = true;
	}

	@Override
	protected void Finalize() throws IOException, InterruptedException {
		flInitialized = false;
		//.
		if (Positioning != null) {
			Positioning.Cancel();
			Positioning = null;
		}
		//.
		synchronized (this) {
			if (VideoChannelProcessor != null) {
				VideoChannelProcessor.Destroy();
				VideoChannelProcessor = null;
			}
		}
		//.
		synchronized (this) {
			if (AudioChannelProcessor != null) {
				AudioChannelProcessor.Destroy();
				AudioChannelProcessor = null;
			}
		}
		//.
		sbVideoRecorderServerMyPlayer.setVisibility(View.GONE);
	}
	
	@Override
	public void Start() throws Exception {
		SetPosition(0.0, 0);
	}
	
	@Override
	public void Stop() throws Exception {
		flInitialized = false;
		//.
		if (Positioning != null) {
			Positioning.Cancel();
			Positioning = null;
		}
		//.
		synchronized (this) {
			if (VideoChannelProcessor != null) {
				VideoChannelProcessor.Destroy(false);
				VideoChannelProcessor = null;
			}
		}
		//.
		synchronized (this) {
			if (AudioChannelProcessor != null) {
				AudioChannelProcessor.Destroy(false);
				AudioChannelProcessor = null;
			}
		}
		//.
		sbVideoRecorderServerMyPlayer.setVisibility(View.GONE);
	}
	
	@Override
	public void Pause() {
		if (AudioChannelProcessor != null)
			AudioChannelProcessor.Pause();
		if (VideoChannelProcessor != null)
			VideoChannelProcessor.Pause();
	}
	
	@Override
	public void Resume() {
		if (AudioChannelProcessor != null)
			AudioChannelProcessor.Resume();
		if (VideoChannelProcessor != null)
			VideoChannelProcessor.Resume();
	}
	
	@Override
	public void Show() {
		ParentLayout.setVisibility(View.VISIBLE);
		svVideoRecorderServerMyPlayer.setVisibility(View.VISIBLE);
		//.
		super.Show();
	}
	
	@Override
	public void Hide() {
		super.Hide();
		//.
		svVideoRecorderServerMyPlayer.setVisibility(View.GONE);
		ParentLayout.setVisibility(View.GONE);
	}
	
	private void ShowInfo() {
		StringBuilder SB = new StringBuilder(ParentActivity.getString(R.string.SViewer1));
		if (flVideo)
			SB.append(ParentActivity.getString(R.string.SVideo));
        else
        	SB.append(ParentActivity.getString(R.string.SNoVideo));
        if (flAudio)
        	SB.append(","+ParentActivity.getString(R.string.SAudio));
        else
        	SB.append(","+ParentActivity.getString(R.string.SNoAudio));
        int Secs = (int)(MeasurementCurrentPositionFactor*Measurement.Descriptor.DurationInMs()/1000);
        SB.append(": "+Integer.toString(Secs)+" "+ParentActivity.getString(R.string.SSec1));
        //.
        lbVideoRecorderServerMyPlayer.setText(SB.toString());
		//.
		ivVideoRecorderServerMyPlayerAudioOnly.setVisibility((flAudio && !flVideo) ? View.VISIBLE : View.GONE);
	}
	
	private synchronized boolean IsRunning() {
		return ((!flVideo || ((VideoChannelProcessor != null) && VideoChannelProcessor.flRunning)) && (!flAudio || ((AudioChannelProcessor != null) && AudioChannelProcessor.flRunning)));
	}
	
	public synchronized boolean IsPlaying() {
		return ((!flVideo || ((VideoChannelProcessor != null) && VideoChannelProcessor.flPlaying)) && (!flAudio || ((AudioChannelProcessor != null) && AudioChannelProcessor.flPlaying)));
	}
	
	private void DoSetPosition(double Position) {
		if (Position < 0.0)
			return; //. ->
		//.
		int PositionInMs = (int)(Position*(24.0*3600.0*1000.0));
		//.
		if (AudioChannelProcessor != null)
			AudioChannelProcessor.Set(PositionInMs);
		if (VideoChannelProcessor != null)
			VideoChannelProcessor.Set(PositionInMs);
	}
	
	private void DoSetPosition(double Position, boolean pflPause) {
		if (Position < 0.0)
			return; //. ->
		//.
		int PositionInMs = (int)(Position*(24.0*3600.0*1000.0));
		//.
		if (AudioChannelProcessor != null)
			AudioChannelProcessor.Set(PositionInMs, pflPause);
		if (VideoChannelProcessor != null)
			VideoChannelProcessor.Set(PositionInMs, pflPause);
	}
	
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
		if (flPaused)
			Pause();
		//.
		if (Positioning != null) { 
			Positioning.Cancel();
			Positioning = null;
		}
		//.
		if ((Delay > 0) || !IsRunning()) {
			Positioning = new TAsyncProcessing() {

				@Override
				public void Process() throws Exception {
					if (Delay > 0)
						Thread.sleep(Delay);
					while (!Canceller.flCancel) {
						if (IsRunning())
							break; //. >
						Thread.sleep(10); 
					}
				}

				@Override
				public void DoOnCompleted() throws Exception {
					if (!Canceller.flCancel) 
						DoSetPosition(Position, flPaused);
				}
				
				@Override
				public void DoOnFinished() throws Exception {
					if (Positioning == this)
						Positioning = null;
				}
			};
			Positioning.Start();
		}
		else
			DoSetPosition(Position, flPaused);
	}
	
	public void SetPosition(double Position, int Delay) throws InterruptedException {
		SetPosition(Position, Delay, false);
	}
	
	public void StopPositioning() throws InterruptedException {
		if (Positioning != null) {
			Positioning.CancelAndWait();
			Positioning = null;
		}
	}
	
	private void DoOnPlayingProgress(double ProgressFactor) {
		if (!flExists)
			return; //. ->
		//.
		synchronized (this) {
			MeasurementCurrentPositionFactor = ProgressFactor;
		}
		//.
		sbVideoRecorderServerMyPlayer.setProgress((int)(100.0*ProgressFactor));
		//.
		ShowInfo();
		//.
		if (OnProgressHandler != null) 
			OnProgressHandler.DoOnProgress(ProgressFactor);
	}
	
	private void DoOnException(Throwable E) {
		if (flExists)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
