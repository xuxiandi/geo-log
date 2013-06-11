package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerViewer extends Activity implements SurfaceHolder.Callback {
    
	public static final int AudioServerPort = 10001;
	public static final int AudioPort = 5001;
	public static final int VideoServerPort = 10002;
	public static final int VideoPort = 5002;
	
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
		
		private int Port;
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

		public TAudioClient(int pPort) {
			Port = pPort;
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
				Socket socket = new Socket("127.0.0.1", Port);
				try {
					socket.setSoTimeout(InitializationTimeout);
					//.
					InputStream IS = socket.getInputStream();
					try {
						OutputStream OS = socket.getOutputStream();
						try {
							int ActualSize;
							byte[] InitBuffer = new byte[4/*SizeOf(Service)*/+4/*SizeOf(SampleRate)*/+4/*SizeOf(SamplePacketSize)*/+4/*SizeOf(Quality)*/];
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
		
		private static final int TransferBufferSize = 1024*1024;
		
		private int Port;
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

		public TVideoClient(int pPort, Surface psurface, int pWidth, int pHeight) {
			Port = pPort;
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
				Socket socket = new Socket("127.0.0.1", Port);
				try {
					socket.setSoTimeout(InitializationTimeout);
					//.
					InputStream IS = socket.getInputStream();
					try {
						OutputStream OS = socket.getOutputStream();
						try {
							int ActualSize;
							byte[] InitBuffer = new byte[4/*SizeOf(Service)*/+4/*SizeOf(FrameRate)*/+4/*SizeOf(Quality)*/];
							int Idx = 0;
							//. set service type
							int Service = TVideoModule.VideoFrameServer_Service_H264Frames;
							byte[] DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Service);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set frame rate
							DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(FrameRate);
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
								
								case TVideoModule.VideoFrameServer_Initialization_Code_Error:                   
									throw new Exception("error of initializing the server"); //. =>
									
								case TVideoModule.VideoFrameServer_Initialization_Code_UnknownServiceError:
									throw new Exception("unknown service, service: "+Integer.toString(Service)); //. =>
									
								case TVideoModule.VideoFrameServer_Initialization_Code_ServiceIsNotActiveError: 
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
							FrameRate = (DescriptorBA[3] << 24)+((DescriptorBA[2] & 0xFF) << 16)+((DescriptorBA[1] & 0xFF) << 8)+(DescriptorBA[0] & 0xFF);
							if (Canceller.flCancel)
								return; //. ->
							//.
							Codec = MediaCodec.createDecoderByType(CodecTypeName);
							try {
								MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, Width,Height);
								Codec.configure(format, surface, null, 0);
								Codec.start();
								try {
									inputBuffers = Codec.getInputBuffers();
									outputBuffers = Codec.getOutputBuffers();
									//.
									byte[] TransferBuffer = new byte[TransferBufferSize];
									byte[] PacketSizeBA = new byte[4];
									int PacketSize;
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
										PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
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
	
	private TReflector Reflector;
	
	private String 	GeographProxyServerAddress = "";
	private int 	GeographProxyServerPort = 0;
	private int		UserID;
	private String	UserPassword;
	private int								ObjectIndex = -1;
	private TReflectorCoGeoMonitorObject 	Object;
	
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
				Toast.makeText(TVideoRecorderServerViewer.this,EM,Toast.LENGTH_LONG).show();
				// .
				break; // . >
			}
		}
	};
	
	private SurfaceView svVideoRecorderServerViewer;
	private TextView lbVideoRecorderServer;
	
	private boolean 				flAudio = false;
	private TLANConnectionRepeater 	AudioLocalServer = null;
	private TAudioClient			AudioClient = null;
	private boolean 				flVideo = false;
	private TLANConnectionRepeater 	VideoLocalServer = null;
	private TVideoClient			VideoClient = null;
	//.
	private boolean IsInFront = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.video_recorder_server_viewer);
        //.
        svVideoRecorderServerViewer = (SurfaceView)findViewById(R.id.svVideoRecorderServerViewer);
        svVideoRecorderServerViewer.getHolder().addCallback(this);
        lbVideoRecorderServer = (TextView)findViewById(R.id.lbVideoRecorderServer);
        //.
        Reflector = TReflector.GetReflector();
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	GeographProxyServerAddress = extras.getString("GeographProxyServerAddress");
        	GeographProxyServerPort = extras.getInt("GeographProxyServerPort");
        	UserID = extras.getInt("UserID");
        	UserPassword = extras.getString("UserPassword");
        	ObjectIndex = extras.getInt("ObjectIndex");
        	Object = Reflector.CoGeoMonitorObjects.Items[ObjectIndex];
        	flAudio = extras.getBoolean("flAudio");
        	flVideo = extras.getBoolean("flVideo");
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
        lbVideoRecorderServer.setText(S);
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
			VideoClient = new TVideoClient(VideoLocalServer.GetPort(), arg0.getSurface(), arg2, arg3);
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
		TLANConnectionExceptionHandler ExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				TVideoRecorderServerViewer.this.DoOnException(E);
			}
		};		
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
		TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Object);
		TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Object); 
		//.
		if (flAudio)
			AudioLocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",AudioServerPort, AudioPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.idGeographServerObject, ExceptionHandler, StartHandler,StopHandler);
		if (flVideo)
			VideoLocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",VideoServerPort, VideoPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.idGeographServerObject, ExceptionHandler, StartHandler,StopHandler);
		//.
		if (flAudio) {
			if (AudioClient != null) 
				AudioClient.Destroy();
			AudioClient = new TAudioClient(AudioLocalServer.GetPort());
		}
	}

	private void Finalize() throws IOException {
		if (AudioClient != null) {
			AudioClient.Destroy();
			AudioClient = null;
		}
		//.
		if (VideoLocalServer != null) {
			VideoLocalServer.Destroy();
			VideoLocalServer = null;
		}
		if (AudioLocalServer != null) {
			AudioLocalServer.Destroy();
			AudioLocalServer = null;
		}
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
