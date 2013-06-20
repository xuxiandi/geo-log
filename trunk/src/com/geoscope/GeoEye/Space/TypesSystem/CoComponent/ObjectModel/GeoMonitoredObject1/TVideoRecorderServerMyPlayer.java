package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerMyPlayer extends Activity implements SurfaceHolder.Callback {
    
	private static final int MESSAGE_SHOWEXCEPTION = 1;
	
	public class TAudioClient extends TCancelableThread {
		
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. milliseconds

		public static final int DefaultSampleRate = 8000;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private static final int TransferBufferSize = 1024*1024;
		
		private class TAudioBufferPlaying extends TCancelableThread {
			
			private byte[] 	Buffer = new byte[0];
			private int 	BufferSize = 0;
			private Object 	BufferLock = new Object();
			private Object	PlaySignal = new Object();
			
			public TAudioBufferPlaying() {
				_Thread = new Thread(this);
				_Thread.start();
			}
			
			public void Destroy() {
				Cancel();
				synchronized (PlaySignal) {
					PlaySignal.notify();
				}
				Wait();
			}
			
			@Override
			public void run()  {
				try {
					while (!Canceller.flCancel) {
						synchronized (PlaySignal) {
							PlaySignal.wait(1000);
						}
						synchronized (BufferLock) {
							if (BufferSize > 0) 
								try {
									AudioPlayer.write(Buffer, 0,BufferSize);
								}
							finally {
								BufferSize = 0;
							}
						}
					}
				}
				catch (InterruptedException IE) {
				}
				catch (Throwable T) {
				}
			}
			
			public void PlayBuffer(byte[] pBuffer, int pBufferSize) {
	        	if (AudioPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
	        		AudioPlayer.pause();
	        		AudioPlayer.flush();
	        		AudioPlayer.play();
	        	}
				synchronized (BufferLock) {
					if (pBufferSize > Buffer.length) 
						Buffer = new byte[pBufferSize];
					System.arraycopy(pBuffer,0, Buffer,0, pBufferSize);
					BufferSize = pBufferSize;
				}
				synchronized (PlaySignal) {
					PlaySignal.notify();
				}
			}
		}
		
		private String AudioFileName;
		//.
		public int SampleRate = DefaultSampleRate;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] inputBuffers;
		private ByteBuffer[] 	outputBuffers;
		private byte[]			outData;
		//.
		private AudioTrack 	AudioPlayer;
		private TAudioBufferPlaying AudioBufferPlaying;

		public TAudioClient(String pAudioFileName) {
			AudioFileName = pAudioFileName;
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() {
			Cancel();
		}
		
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				/*////////////////////////////////Socket socket = new Socket("127.0.0.1", Port);
				try {
					socket.setSoTimeout(InitializationTimeout);
					//.
					InputStream IS = socket.getInputStream();
					try {
						OutputStream OS = socket.getOutputStream();
						try {
							int ActualSize;
							int Idx = 0;
							//. set service type
							int Service = TAudioModule.AudioSampleServer_Service_AACPackets1;
							byte[] DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Service);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set sample rate
							DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(SampleRate);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set sample packet size
							DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(0);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set frame quality
							DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(100);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//.
							OS.write(InitBuffer);
							//. get service initialization result
							DescriptorBA = new byte[4];
			                ActualSize = IS.read(DescriptorBA,0,DescriptorBA.length);
					    	if (ActualSize == 0)
				    			throw new IOException("connection is closed unexpectedly"); //. =>
					    		else 
							    	if (ActualSize < 0)
						    			throw new IOException("error of reading server socket descriptor, RC: "+Integer.toString(ActualSize)); //. =>
							if (ActualSize != DescriptorBA.length)
								throw new IOException("wrong data descriptor"); //. =>
							int RC = (DescriptorBA[3] << 24)+((DescriptorBA[2] & 0xFF) << 16)+((DescriptorBA[1] & 0xFF) << 8)+(DescriptorBA[0] & 0xFF);
							if (RC < 0) 
								switch (RC) {
								
								case TAudioModule.AudioSampleServer_Initialization_Code_Error:                   
									throw new Exception("error of initializing the server"); //. =>
									
								case TAudioModule.AudioSampleServer_Initialization_Code_UnknownServiceError:
									throw new Exception("unknown service, service: "+Integer.toString(Service)); //. =>
									
								case TAudioModule.AudioSampleServer_Initialization_Code_ServiceIsNotActiveError: 
									throw new Exception("service is not active, service: "+Integer.toString(Service)); //. =>
									
							    default:
							    	throw new Exception("error of initializing the server: "+Integer.toString(RC));
								}
							if (Canceller.flCancel)
								return; //. ->
							//. get frame rate
							DescriptorBA = new byte[4];
			                ActualSize = IS.read(DescriptorBA,0,DescriptorBA.length);
					    	if (ActualSize == 0)
				    			throw new IOException("connection is closed unexpectedly"); //. =>
					    		else 
							    	if (ActualSize < 0)
						    			throw new IOException("error of reading server socket descriptor, RC: "+Integer.toString(ActualSize)); //. =>
							if (ActualSize != DescriptorBA.length)
								throw new IOException("wrong data descriptor"); //. =>
							SampleRate = (DescriptorBA[3] << 24)+((DescriptorBA[2] & 0xFF) << 16)+((DescriptorBA[1] & 0xFF) << 8)+(DescriptorBA[0] & 0xFF);
							if (Canceller.flCancel)
								return; //. ->
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
									//.
									SampleRate*=2;
									int SampleInterval = 20; //. ms
									int SampleSize = 2;
									int BufferSize = (SampleSize*SampleRate/1000)*SampleInterval;							    	
									AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize, AudioTrack.MODE_STREAM);
							    	AudioPlayer.setStereoVolume(1.0F,1.0F);
							    	AudioPlayer.play();
							    	try {
							    		AudioBufferPlaying = new TAudioBufferPlaying();
							    		try {
											byte[] TransferBuffer = new byte[TransferBufferSize];
											byte[] PacketSizeBA = new byte[2];
											short PacketSize;
											socket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
											while (!Canceller.flCancel) {
												try {
									                ActualSize = IS.read(PacketSizeBA,0,PacketSizeBA.length);
											    	if (ActualSize == 0)
											    		break; //. > connection is closed
											    		else 
													    	if (ActualSize < 0)
												    			throw new IOException("error of reading server socket data descriptor, RC: "+Integer.toString(ActualSize)); //. =>
												}
												catch (SocketTimeoutException E) {
													continue; //. ^
												}
												if (ActualSize != PacketSizeBA.length)
													throw new IOException("wrong data descriptor"); //. =>
												PacketSize = (short)(((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF));
												if (PacketSize > 0) {
													if (PacketSize > TransferBuffer.length)
														TransferBuffer = new byte[PacketSize];
													ActualSize = TLANConnectionRepeater.InputStream_Read(IS,TransferBuffer,PacketSize);	
											    	if (ActualSize == 0)
											    		break; //. > connection is closed
											    		else 
													    	if (ActualSize < 0)
												    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
											    	//.
											    	DecodeInputBuffer(TransferBuffer,PacketSize);
												}
												else
													break; //. > disconnect
											}
							    		}
							    		finally {
							    			AudioBufferPlaying.Destroy();
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
							OS.close();
						}
					}
					finally {
						IS.close();
					}
				}
				finally {
					socket.close();
				}*/
			}
			catch (Throwable T) {
				DoOnException(T);
			}
		}
		
		@SuppressLint("NewApi")
		public void DecodeInputBuffer(byte[] input, int input_size) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, 0,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0, input_size, SystemClock.elapsedRealtime(), 0);
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
				AudioBufferPlaying.PlayBuffer(outData, bufferInfo.size);
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
	
	public class TVideoClient extends TCancelableThread {
		
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 10000; //. milliseconds

		public static final int DefaultFrameRate = 25;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private static final int TransferBufferSize = 16;
		
		private String VideoFileName;
		//.
		private Surface surface;
		private int 	Width;
		private int 	Height;
		//.
		public int FrameRate = DefaultFrameRate;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] inputBuffers;
		@SuppressWarnings("unused")
		private ByteBuffer[] outputBuffers;

		public TVideoClient(String pVideoFileName, Surface psurface, int pWidth, int pHeight) {
			VideoFileName = pVideoFileName;
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
		
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				FileInputStream IS = new FileInputStream(VideoFileName); 
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
							byte[] NAL_BA = new byte[4];
							int NAL_Size;
							byte[] TransferBuffer = new byte[0];
							int ActualSize;
							while (!Canceller.flCancel) {
								IS.read(NAL_BA);
								NAL_Size = ((NAL_BA[3]&0xFF) | (NAL_BA[2]&0xFF) << 8 | (NAL_BA[1]&0xFF) << 16 | (NAL_BA[0]&0xFF) << 24);
								//.
								if (NAL_Size > TransferBuffer.length)
									TransferBuffer = new byte[NAL_Size];
								//.
				                ActualSize = IS.read(TransferBuffer,0,TransferBuffer.length);
				                if (ActualSize > 0) 
							    	DecodeInputBuffer(TransferBuffer,ActualSize);
				                else
				                	break; //. >
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
		public void DecodeInputBuffer(byte[] input, int input_size) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, 0,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0, input_size, SystemClock.elapsedRealtime(), 0);
			}
			//.
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			while (outputBufferIndex >= 0) {
				//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				//.
				Codec.releaseOutputBuffer(outputBufferIndex, true);
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
	
	private boolean 				flAudio = false;
	private TAudioClient			AudioClient = null;
	private boolean 				flVideo = false;
	private TVideoClient			VideoClient = null;
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
			if (VideoClient != null) 
				VideoClient.Destroy();
			/////////////////
			VideoClient = new TVideoClient(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoH264FileName, arg0.getSurface(), arg2, arg3);
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
			if (AudioClient != null) 
				AudioClient.Destroy();
			///////AudioClient = new TAudioClient(AudioLocalServer.GetPort());
		}
	}

	private void Finalize() throws IOException {
		if (AudioClient != null) {
			AudioClient.Destroy();
			AudioClient = null;
		}
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
