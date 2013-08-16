package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerFRAME;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerMyPlayer extends Activity implements SurfaceHolder.Callback {
    
	private static final int MESSAGE_SHOWEXCEPTION 			= 1;
	private static final int MESSAGE_AUDIOCLIENT_ISREADY 	= 2;
	private static final int MESSAGE_VIDEOCLIENT_ISREADY 	= 3;
	private static final int MESSAGE_PLAYING_PROGRESS 		= 4;
	
	public class TAudioAACClient extends TCancelableThread {
		
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. milliseconds

		private String AudioFileName;
		@SuppressWarnings("unused")
		private int Packets;
		//.
		public int SampleRate;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] inputBuffers;
		private ByteBuffer[] 	outputBuffers;
		private byte[]			outData;
		//.
		private AudioTrack 	AudioPlayer;
		//.
		private int PositionInMs =0;
		public boolean flReady = false;
		private TAutoResetEvent StartSignal = new TAutoResetEvent();
		public boolean flStop = false;
		public boolean flPause = false;
		//.
		private long CurrentPosition_InMs = 0;

		public TAudioAACClient(String pAudioFileName, int pPackets, int pSampleRate) {
			AudioFileName = pAudioFileName;
			Packets = pPackets;
			SampleRate = pSampleRate;
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() {
			Cancel();
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
			flStop = true;
		}
		
		public synchronized void CurrentPosition_SetInMs(long Value) {
			CurrentPosition_InMs = Value;
		}
		
		public synchronized long CurrentPosition_GetInMs() {
			return CurrentPosition_InMs;
		}
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				RandomAccessFile AudioFile = new RandomAccessFile(AudioFileName, "r");
				try {
					FileChannel AudioFileChannel = AudioFile.getChannel();
					try {
						MappedByteBuffer AudioFileBuffer = AudioFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, AudioFileChannel.size());
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
								SampleRate*=2;
								int SampleInterval = 20; //. ms
								int SampleSize = 2;
								int BufferSize = (SampleSize*SampleRate/1000)*SampleInterval;							    	
								AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize, AudioTrack.MODE_STREAM);
						    	AudioPlayer.setStereoVolume(1.0F,1.0F);
						    	AudioPlayer.play();
						    	try {
									DecodeInputBuffer(ConfigWordBA,0,ConfigWordBA.length,0,1);
									//.
									while (!Canceller.flCancel) {
										flStop = false;
										//.
										int PositionIndex;
										synchronized (this) {
											PositionIndex = (int)(AudioFileIndexesCount*(PositionInMs+0.0)/MeasurementDescriptor.DurationInMs());
										}
										if (PositionIndex >= AudioFileIndexesCount)
											PositionIndex = AudioFileIndexesCount-1;
										//.
										flReady = true;
										MessageHandler.obtainMessage(MESSAGE_AUDIOCLIENT_ISREADY).sendToTarget();
										//.
										StartSignal.WaitOne();
										//.
										int StartIndex = AudioFileIndexes.getInt(PositionIndex << 2);
										PositionIndex++;
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
											while (flPause) {
												Thread.sleep(20);
												if (Canceller.flCancel | flStop)
													break; //. >
											}
											//.
											if (Canceller.flCancel | flStop)
												break; //. >
											//.
											MessageHandler.obtainMessage(MESSAGE_PLAYING_PROGRESS,(double)((I-1.0)/AudioFileIndexesCount)).sendToTarget();
											//.
											StartIndex = FinishIndex;
										}
										Codec.flush();
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
						AudioFileChannel.close();
					}
				}
				finally {
					AudioFile.close();
				}
			}
			catch (Throwable T) {
				DoOnException(T);
			}
		}
		
		@SuppressLint("NewApi")
		public void DecodeInputBuffer(byte[] input, int input_offset, int input_size, int Index, int Count) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, input_offset,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0,input_size, (long)(1.0*MeasurementDescriptor.DurationInMs()*Index/Count), 0);
			}
			//.
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			while (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				if (outData.length < bufferInfo.size)
					outData = new byte[bufferInfo.size];
				outputBuffer.rewind(); //. reset position to 0
				outputBuffer.get(outData, 0,bufferInfo.size);
				//. process output
				AudioPlayer.write(outData, 0,bufferInfo.size);
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
	
	public class TVideoH264Client extends TCancelableThread {
		
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 10000; //. milliseconds

		private String VideoFileName;
		private String VideoIndexFileName;
		private String VideoTimestampFileName;
		//.
		@SuppressWarnings("unused")
		private int Packets;
		//.
		@SuppressWarnings("unused")
		private int 	FrameRate;
		//.
		private Surface surface;
		private int 	Width;
		private int 	Height;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] 	inputBuffers;
		@SuppressWarnings("unused")
		private ByteBuffer[] outputBuffers;
		//.
		public int PositionInMs = 0;
		public boolean flReady = false;
		private TAutoResetEvent StartSignal = new TAutoResetEvent();
		public boolean flStop = false;
		public boolean flPause = false;
		//.
		private long TimestampBase = 0;		
		
		public TVideoH264Client(String pVideoFileName, String pVideoIndexFileName, String pVideoTimestampFileName, int pPackets, int pFrameRate, Surface psurface, int pWidth, int pHeight) {
			VideoFileName = pVideoFileName;
			VideoIndexFileName = pVideoIndexFileName;
			VideoTimestampFileName = pVideoTimestampFileName;
			Packets = pPackets;
			FrameRate = pFrameRate;
			surface = psurface;
			Width = pWidth;
			Height = pHeight;
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() {
			Cancel();
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
			flStop = true;
		}
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				RandomAccessFile VideoFile = new RandomAccessFile(VideoFileName, "r");
				try {
					FileChannel VideoFileChannel = VideoFile.getChannel();
					try {
						MappedByteBuffer VideoFileBuffer = VideoFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, VideoFileChannel.size());
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
										ByteBuffer VideoFileTimestamps = null;
										if (VideoTimestampFileChannel != null) {
											VideoFileTimestamps = VideoTimestampFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, VideoTimestampFileChannel.size());
											((MappedByteBuffer)VideoFileTimestamps).load();
											VideoFileTimestamps.order(ByteOrder.LITTLE_ENDIAN);
										}
										/* processing */
										int VideoFileIndexesCount;
										if (VideoFileIndexes == null) {
											//. prepare file indexes
											byte[] PacketSignature = new byte[] {0x00,0x00,0x00,0x01};
											byte[] BA = new byte[1024*1024];
											ByteArrayOutputStream BOS = new ByteArrayOutputStream((int)(VideoFileChannel.size() >> 9));
											try {
												DataOutputStream DOS = new DataOutputStream(BOS);
												try {
													int PacketSignatureIdx = 0;
													int Idx = 0;
													VideoFileBuffer.position(Idx);
													int Size;
													while (true) {
														Size = VideoFileBuffer.remaining();
														if (Size == 0)
															break; //. >
														if (Size > BA.length)
															Size = BA.length;
														VideoFileBuffer.get(BA,0,Size);
														//. 
														for (int I = 0; I < Size; I++) {
															if (BA[I] == PacketSignature[PacketSignatureIdx]) {
																PacketSignatureIdx++;
																if (PacketSignatureIdx == PacketSignature.length) {
																	DOS.writeInt(Idx-3);
																	//.
																	PacketSignatureIdx = 0;
																}
															}
															else 
																PacketSignatureIdx = 0;
															Idx++;
														}
														//.
														if (Canceller.flCancel)
															return; //. >
													}
												}
												finally {
													DOS.close();
												}
												VideoFileIndexes = ByteBuffer.wrap(BOS.toByteArray());
											}
											finally {
												BOS.close();
											}
										}
										VideoFileIndexesCount = (VideoFileIndexes.limit() >> 2/*SizeOf(Index)*/); 
										if (VideoFileIndexesCount == 0)
											return; //. >
										//.
										Codec = MediaCodec.createDecoderByType(CodecTypeName);
										try {
											MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, Width,Height);
											Codec.configure(format, surface, null, 0);
											Codec.start();
											try {
												inputBuffers = Codec.getInputBuffers();
												outputBuffers = Codec.getOutputBuffers();
												byte[] Buffer = new byte[0];
												//.
												while (!Canceller.flCancel) {
													flStop = false;
													//.
													long _PositionInMs = 0;
													int PositionIndex = 0;
													if (VideoFileTimestamps != null) {
														synchronized (this) {
															_PositionInMs = PositionInMs;
														}
														PositionIndex = 0;
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
														}
													}
													if (PositionIndex >= VideoFileIndexesCount)
														PositionIndex = VideoFileIndexesCount-1;
													//.
													TimestampBase = SystemClock.elapsedRealtime()-_PositionInMs;
													//.
													flReady = true;
													MessageHandler.obtainMessage(MESSAGE_VIDEOCLIENT_ISREADY).sendToTarget();
													//.
													StartSignal.WaitOne();
													//.
													int StartIndex = VideoFileIndexes.getInt(PositionIndex << 2);
													PositionIndex++;
													for (int I = PositionIndex; I < VideoFileIndexesCount; I++) {
														int FinishIndex = VideoFileIndexes.getInt(I << 2);
														//.
														int BufferSize = (FinishIndex-StartIndex);
														if (BufferSize > Buffer.length)
															Buffer = new byte[BufferSize];
														VideoFileBuffer.position(StartIndex);
														VideoFileBuffer.get(Buffer, 0,BufferSize);
														//.
														int TS = 0;
														if (VideoFileTimestamps != null) 
															TS = VideoFileTimestamps.getInt((I-1) << 2);
														//.
												    	DecodeInputBuffer(Buffer, 0,BufferSize, TS);
														//.
														while (flPause) {
															Thread.sleep(20);
															if (Canceller.flCancel | flStop)
																break; //. >
														}
														//.
														if (Canceller.flCancel | flStop)
															break; //. >
														//.
														if (AudioClient == null) 
															MessageHandler.obtainMessage(MESSAGE_PLAYING_PROGRESS,(double)((I-1.0)/VideoFileIndexesCount)).sendToTarget();
														//.
														StartIndex = FinishIndex;
													}
													Codec.flush();
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
						VideoFileChannel.close();
					}
				}
				finally {
					VideoFile.close();
				}
			}
			catch (InterruptedException IE) {
			}
			catch (Throwable T) {
				DoOnException(T);
			}
		}
		
		@SuppressLint("NewApi")
		public void DecodeInputBuffer(byte[] input, int input_offset, int input_size, long Timestamp) throws IOException, InterruptedException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
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
				//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				//. synchronizing video
				if (AudioClient != null) {
					while (true) {
						long Delta = bufferInfo.presentationTimeUs-AudioClient.CurrentPosition_GetInMs();
						if (Delta <= 0)
							break; //. >
						Thread.sleep(Delta % 100);
						//.
						if (Canceller.flCancel | flStop)
							return; //. >
					}
				} 
				else {
					while (true) {
						long Delta = bufferInfo.presentationTimeUs-(SystemClock.elapsedRealtime()-TimestampBase);
						if (Delta <= 0)
							break; //. >
						Thread.sleep(Delta % 100);
						//.
						if (Canceller.flCancel | flStop)
							return; //. >
					}
				}
				Codec.releaseOutputBuffer(outputBufferIndex, true);
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
	
	public final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_SHOWEXCEPTION:
				Throwable E = (Throwable)msg.obj;
				String EM = E.getMessage();
				if (EM == null) 
					EM = E.getClass().getName();
				//.
				Toast.makeText(TVideoRecorderServerMyPlayer.this,EM,Toast.LENGTH_LONG).show();
				// .
				break; // . >
				
			case MESSAGE_AUDIOCLIENT_ISREADY:
				if (VideoClient != null) {
					if (VideoClient.flReady) {
						if (AudioClient != null)
							AudioClient.Start();
						VideoClient.Start();
					}
				}
				else 
					if (AudioClient != null)
						AudioClient.Start();
				// .
				break; // . >

			case MESSAGE_VIDEOCLIENT_ISREADY:
				if (AudioClient != null) {
					if (AudioClient.flReady) {
						AudioClient.Start();
						if (VideoClient != null)
							VideoClient.Start();
					}
				}
				else 
					if (VideoClient != null)
						VideoClient.Start();
				// .
				break; // . >

			case MESSAGE_PLAYING_PROGRESS:
				double ProgressFactor = (Double)msg.obj;
				DoOnPlayingProgress(ProgressFactor);
				// .
				break; // . >
			}
		}
	};
	
	private String 					MeasurementDatabaseFolder = null;
	private String 					MeasurementID = null;
	private String 					MeasurementFolder = null;
	//.
	private TMeasurementDescriptor 	MeasurementDescriptor = null;
	
	private SurfaceView svVideoRecorderServerMyPlayer;
	private TextView lbVideoRecorderServerMyPlayer;
	private SeekBar sbVideoRecorderServerMyPlayer;
	
	private boolean 				flAudio = false;
	private TAudioAACClient			AudioClient = null;
	private boolean 				flVideo = false;
	private TVideoH264Client		VideoClient = null;
	//.
	private double 	CurrentPosition_Factor = 0.0;
	//.
	private boolean IsInFront = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.video_recorder_server_myplayer);
        //.
        svVideoRecorderServerMyPlayer = (SurfaceView)findViewById(R.id.svVideoRecorderServerMyPlayer);
        svVideoRecorderServerMyPlayer.getHolder().addCallback(this);
        lbVideoRecorderServerMyPlayer = (TextView)findViewById(R.id.lbVideoRecorderServerMyPlayer);
        sbVideoRecorderServerMyPlayer = (SeekBar)findViewById(R.id.sbVideoRecorderServerMyPlayer);
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
					int PositionInMs = (int)(MeasurementDescriptor.DurationInMs()*progress/100.0);
					if (AudioClient != null)
						AudioClient.Set(PositionInMs);
					if (VideoClient != null)
						VideoClient.Set(PositionInMs);
				}
			}
		});
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	MeasurementDatabaseFolder = extras.getString("MeasurementDatabaseFolder");
        	MeasurementID = extras.getString("MeasurementID");
        	MeasurementFolder = MeasurementDatabaseFolder+"/"+MeasurementID; 
        	//.
    		try {
				MeasurementDescriptor = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder,MeasurementID);
			} catch (Exception E) {
				Toast.makeText(TVideoRecorderServerMyPlayer.this,E.getMessage(),Toast.LENGTH_LONG).show();
				finish();
				return; //. ->
			}
			flAudio = (MeasurementDescriptor.AudioPackets > 0);
			flVideo = (MeasurementDescriptor.VideoPackets > 0);
        }
        //.
        try {
			Initialize();
		} catch (Exception E) {
			DoOnException(E);
		}
    }
	
    public void onDestroy() {
    	try {
			Finalize();
		} catch (IOException E) {
			DoOnException(E);
		}
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IsInFront = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onStart() {
    	super.onStart();
    }
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (flVideo) {
			if (VideoClient != null) { 
				VideoClient.Destroy();
				VideoClient = null;
			}
			switch (MeasurementDescriptor.VideoFormat) {
			
			case CameraStreamerFRAME.VIDEO_FRAME_FILE_FORMAT_H264PACKETS: 
				VideoClient = new TVideoH264Client(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoH264FileName,MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoIndex32FileName,MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoTS32FileName, MeasurementDescriptor.VideoPackets, MeasurementDescriptor.VideoFPS, arg0.getSurface(), arg2, arg3);
				break; //. >
			}
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (VideoClient != null) {
			VideoClient.Destroy();
			VideoClient = null;
		}
	}
	
	private void Initialize() throws Exception {
		if (flAudio) {
			if (AudioClient != null) { 
				AudioClient.Destroy();
				AudioClient = null;
			}
			switch (MeasurementDescriptor.AudioFormat) {
			
			case CameraStreamerFRAME.AUDIO_SAMPLE_FILE_FORMAT_ADTSAACPACKETS:
				AudioClient = new TAudioAACClient(MeasurementFolder+"/"+TVideoRecorderMeasurements.AudioAACADTSFileName, MeasurementDescriptor.AudioPackets, MeasurementDescriptor.AudioSPS);
				break; //. >
			}
		}
		//.
		ShowInfo();
	}

	private void Finalize() throws IOException {
		if (AudioClient != null) {
			AudioClient.Destroy();
			AudioClient = null;
		}
	}
	
	private void ShowInfo() {
		StringBuilder SB = new StringBuilder(getString(R.string.SViewer1));
		if (flVideo)
			SB.append(getString(R.string.SVideo));
        else
        	SB.append(getString(R.string.SNoVideo));
        if (flAudio)
        	SB.append(","+getString(R.string.SAudio));
        else
        	SB.append(","+getString(R.string.SNoAudio));
        int Secs = (int)(CurrentPosition_Factor*MeasurementDescriptor.DurationInMs()/1000);
        SB.append(": "+Integer.toString(Secs)+" "+getString(R.string.SSec1));
        //.
        lbVideoRecorderServerMyPlayer.setText(SB.toString());
	}
	
	private void DoOnPlayingProgress(double ProgressFactor) {
		synchronized (this) {
			CurrentPosition_Factor = ProgressFactor;
		}
		//.
		sbVideoRecorderServerMyPlayer.setProgress((int)(100.0*ProgressFactor));
		ShowInfo();
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
