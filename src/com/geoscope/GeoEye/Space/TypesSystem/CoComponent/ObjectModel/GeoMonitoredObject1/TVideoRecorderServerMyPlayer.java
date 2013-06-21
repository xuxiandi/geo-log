package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
		private int Position =0;
		public boolean flReady = false;
		private Object StartSignal = new Object();
		public boolean flStop = false;

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
			synchronized (StartSignal) {
				StartSignal.notify();
			}
		}
		
		public void Set(int pPosition) {
			synchronized (this) {
				Position = pPosition;
			}
			flStop = true;
		}
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				File F = new File(AudioFileName);
				FileInputStream IS = new FileInputStream(F);
				try {
					Codec = MediaCodec.createDecoderByType(CodecTypeName);
					try {
						MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName,SampleRate,1);
						Codec.configure(format, null, null, 0);
						Codec.start();
						try {
							inputBuffers = Codec.getInputBuffers();
							outputBuffers = Codec.getOutputBuffers();
							outData = new byte[0];
							//.
							SampleRate*=2;
							int SampleInterval = 20; //. ms
							int SampleSize = 2;
							int BufferSize = (SampleSize*SampleRate/1000)*SampleInterval;							    	
							AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize, AudioTrack.MODE_STREAM);
					    	AudioPlayer.setStereoVolume(1.0F,1.0F);
					    	AudioPlayer.play();
					    	try {
								byte[] Buffer = new byte[(int)F.length()];
								IS.read(Buffer);
								//.
								int Limit = Buffer.length-7/*SizeOf(ADTSHeader)*/;
								int Idx = 0;
								if (Idx < Limit) {
									int AudioBufferSize = (((Buffer[Idx+3] & 0x3) << 10) | ((Buffer[Idx+4] & 0xFF) << 3) | ((Buffer[Idx+5] & 0xE0) >> 5)); 
									//.
									byte ObjectType = (byte)(((Buffer[Idx+2] & 0xFF) >> 6)+1); 
									byte FrequencyIndex = (byte)((Buffer[Idx+2] & 0x3C) >> 2);
									byte ChannelConfiguration = (byte)(((Buffer[Idx+2] & 0x01) << 2) | ((Buffer[Idx+3] & 0xC0) >> 6));
									//.
									byte[] ConfigWordBA = new byte[2];
									ConfigWordBA[0] = (byte)(((ObjectType << 3) & 0xF8) | ((FrequencyIndex >> 1) & 0x07));
									ConfigWordBA[1] = (byte)(((FrequencyIndex & 0x01) << 7) | ((ChannelConfiguration & 0x0F) << 3));
									//.
							    	DecodeInputBuffer(ConfigWordBA,0,ConfigWordBA.length);
									//.
									Idx += AudioBufferSize;
								}
								int IdxBase = Idx;
								//.
								while (!Canceller.flCancel) {
									flStop = false;
									//.
									int PositionPacket;
									synchronized (this) {
										PositionPacket = (int)(Packets*Position/100.0);
									}
									int PacketCount = 0;
									int StartIndex = 0;
									Idx = IdxBase;
									while (Idx < Limit) {
										int AudioBufferSize = (((Buffer[Idx+3] & 0x3) << 10) | ((Buffer[Idx+4] & 0xFF) << 3) | ((Buffer[Idx+5] & 0xE0) >> 5)); 
										//.
										if (PacketCount == PositionPacket) {
											StartIndex = Idx;
											break; //. >
										}
										PacketCount++;
										//.
										if (Canceller.flCancel | flStop)
											break; //. >
										//.
										Idx += AudioBufferSize;
									}
									if (Canceller.flCancel)
										break; //. >
									if (flStop)
										continue; //. >
									//.
									flReady = true;
									MessageHandler.obtainMessage(MESSAGE_AUDIOCLIENT_ISREADY).sendToTarget();
									//.
									synchronized (StartSignal) {
										StartSignal.wait();
									}
									//.
									Idx = StartIndex;
									while (Idx < Limit) {
										boolean flProtectionAbsent = ((Buffer[Idx+1] & 1) != 0);
										int AudioBufferSize = (((Buffer[Idx+3] & 0x3) << 10) | ((Buffer[Idx+4] & 0xFF) << 3) | ((Buffer[Idx+5] & 0xE0) >> 5)); 
										int ADTSHeaderSize;
										if (flProtectionAbsent) 
											ADTSHeaderSize = 7;
										else
											ADTSHeaderSize = 9;
										//.
										Idx += ADTSHeaderSize; 
										AudioBufferSize -= ADTSHeaderSize;
										//.
								    	DecodeInputBuffer(Buffer,Idx,AudioBufferSize);
										//.
										if (Canceller.flCancel)
											break; //. >
										//.
										Idx += AudioBufferSize;
										//.
										MessageHandler.obtainMessage(MESSAGE_PLAYING_PROGRESS,(int)(100*Idx/Buffer.length)).sendToTarget();
										//.
										if (flStop)
											break; //. >
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
					IS.close();
				}
			}
			catch (Throwable T) {
				DoOnException(T);
			}
		}
		
		@SuppressLint("NewApi")
		public void DecodeInputBuffer(byte[] input, int input_offset, int input_size) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, input_offset,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0,input_size, 0, 0);
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
				Codec.releaseOutputBuffer(outputBufferIndex, false);
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
		//.
		private int Packets;
		//.
		private int 	FrameRate;
		private long	FrameInterval;
		private long 	FrameNextTimestamp;
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
		public int Position = 0;
		public boolean flReady = false;
		private Object StartSignal = new Object();
		public boolean flStop = false;
		
		public TVideoH264Client(String pVideoFileName, int pPackets, int pFrameRate, Surface psurface, int pWidth, int pHeight) {
			VideoFileName = pVideoFileName;
			Packets = pPackets;
			FrameRate = pFrameRate;
			surface = psurface;
			Width = pWidth;
			Height = pHeight;
			//.
			FrameInterval = (long)(1000000000/FrameRate);
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() {
			Cancel();
		}
		
		public void Start() {
			synchronized (StartSignal) {
				StartSignal.notify();
			}
		}
		
		public void Set(int pPosition) {
			synchronized (this) {
				Position = pPosition;
			}
			flStop = true;
		}
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				File F = new File(VideoFileName);
				FileInputStream IS = new FileInputStream(F); 
				try {
					Codec = MediaCodec.createDecoderByType(CodecTypeName);
					try {
						MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, Width,Height);
						Codec.configure(format, surface, null, 0);
						Codec.start();
						try {
							inputBuffers = Codec.getInputBuffers();
							outputBuffers = Codec.getOutputBuffers();
							//.
							byte[] Buffer = new byte[(int)F.length()];
							IS.read(Buffer);
							int Limit = Buffer.length-4/*SizeOf(PacketSignature)*/;
							//.
							while (!Canceller.flCancel) {
								flStop = false;
								//.
								int PositionPacket;
								synchronized (this) {
									PositionPacket = (int)(Packets*Position/100.0);
								}
								int PacketCount = 0;
								int PacketIndex = -1;
								int StartIndex = 0;
								for (int Idx = 0; Idx < Limit; Idx++) {
									if ((Buffer[Idx] == 0x00) && (Buffer[Idx+1] == 0x00) && (Buffer[Idx+2] == 0x00) && (Buffer[Idx+3] == 0x01)) { //. check packet signature
										PacketIndex = Idx;
										if (PacketCount == PositionPacket) {
											StartIndex = PacketIndex+4/*SizeOf(PacketSignature)*/;  
											break; //. >
										}
										PacketCount++;
										//.
										if (Canceller.flCancel | flStop)
											break; //. >
									}
								}
								if (Canceller.flCancel)
									break; //. >
								if (flStop)
									continue; //. >
								//.
								flReady = true;
								MessageHandler.obtainMessage(MESSAGE_VIDEOCLIENT_ISREADY).sendToTarget();
								//.
								synchronized (StartSignal) {
									StartSignal.wait();
								}
								//.
								FrameNextTimestamp = 0;
								for (int Idx = StartIndex; Idx < Limit; Idx++) {
									if ((Buffer[Idx] == 0x00) && (Buffer[Idx+1] == 0x00) && (Buffer[Idx+2] == 0x00) && (Buffer[Idx+3] == 0x01)) { //. check packet signature
										if (PacketIndex >= 0) {
									    	DecodeInputBuffer(Buffer,PacketIndex,(Idx-PacketIndex));
											//.
											if (Canceller.flCancel)
												break; //. >
										}
										//.
										PacketIndex = Idx;
										//.
										MessageHandler.obtainMessage(MESSAGE_PLAYING_PROGRESS,(int)(100*Idx/Buffer.length)).sendToTarget();
										//.
										if (flStop)
											break; //. >
									}
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
					IS.close();
				}
			}
			catch (InterruptedException IE) {
			}
			catch (Throwable T) {
				DoOnException(T);
			}
		}
		
		@SuppressLint("NewApi")
		public void DecodeInputBuffer(byte[] input, int input_offset, int input_size) throws IOException, InterruptedException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, input_offset,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0,input_size, System.nanoTime(), 0);
			}
			//.
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			while (outputBufferIndex >= 0) {
				//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				//.
				long Now;
				while (true) {
					Now = System.nanoTime();
					long Delta = (FrameNextTimestamp-Now);
					if (Delta <= 0)
						break; //. >
					int _Delta = (int)(Delta/1000000);
					if (_Delta == 0)
						_Delta = 10;
					Thread.sleep(_Delta);
				}
				Codec.releaseOutputBuffer(outputBufferIndex, true);
				FrameNextTimestamp = Now+FrameInterval;
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
						AudioClient.Start();
						VideoClient.Start();
					}
				}
				else 
					AudioClient.Start();
				// .
				break; // . >

			case MESSAGE_VIDEOCLIENT_ISREADY:
				if (AudioClient != null) {
					if (AudioClient.flReady) {
						AudioClient.Start();
						VideoClient.Start();
					}
				}
				else 
					VideoClient.Start();
				// .
				break; // . >

			case MESSAGE_PLAYING_PROGRESS:
				int Progress = (Integer)msg.obj;
				DoOnPlayingProgress(Progress);
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
	private TVideoH264Client			VideoClient = null;
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
					if (AudioClient != null)
						AudioClient.Set(progress);
					if (VideoClient != null)
						VideoClient.Set(progress);
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
        String S = getString(R.string.SViewer1);
        if (flVideo)
        	S = S+getString(R.string.SVideo);
        else
        	S = S+getString(R.string.SNoVideo);
        if (flAudio)
        	S = S+", "+getString(R.string.SAudio);
        else
        	S = S+", "+getString(R.string.SNoAudio);
        lbVideoRecorderServerMyPlayer.setText(S);
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
				VideoClient = new TVideoH264Client(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoH264FileName, MeasurementDescriptor.VideoPackets, MeasurementDescriptor.VideoFPS, arg0.getSurface(), arg2, arg3);
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
	}

	private void Finalize() throws IOException {
		if (AudioClient != null) {
			AudioClient.Destroy();
			AudioClient = null;
		}
	}
	
	private void DoOnPlayingProgress(int Progress) {
		sbVideoRecorderServerMyPlayer.setProgress(Progress);
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
