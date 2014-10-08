package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.view.Surface;
import android.widget.TextView;

import com.geoscope.Classes.Exception.TExceptionHandler;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionUDPRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionUDPStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionUDPStopHandler;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TGeographProxyServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpBuffer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpDecoder;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TVideoRecorderServerViewUDPRTP extends TVideoRecorderServerView {

	public static final int AudioServerPort = 10001;
	public static final int AudioPort = 5001;
	public static final int VideoServerPort = 10002;
	public static final int VideoPort = 5002;
	
	public static class TAudioClient extends TCancelableThread {
		
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. microseconds

		public static final int DefaultSampleRate = 8000;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private class TAudioBufferPlaying extends TCancelableThread {
			
			private static final int BufferSize = 4096*10;
			private static final int BufferPlayPortion = 128;
			
			private Object 	BufferLock = new Object();
			private byte[] 	Buffer = new byte[BufferSize];
			private int		BufferHead = 0;
			private int		BufferTile = 0;
			private boolean Buffer_flEmpty = true;
			
			private TAutoResetEvent	PlaySignal = new TAutoResetEvent();
			
			public TAudioBufferPlaying() {
				_Thread = new Thread(this);
				_Thread.start();
			}
			
			public void Destroy() throws InterruptedException {
				Cancel();
				PlaySignal.Set();
				Wait();
			}
			
			@Override
			public void run()  {
				try {
			        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
					byte[] Portion = new byte[BufferPlayPortion];
					int Portion_Size;
					while (!Canceller.flCancel) {
						PlaySignal.WaitOne(1000);
						//.
						while (!Canceller.flCancel) {
							synchronized (BufferLock) {
								if (!Buffer_flEmpty) {
									int Size = BufferTile-BufferHead;
									if (Size > 0) {
										if (Size > BufferPlayPortion)
											Size = BufferPlayPortion;
										System.arraycopy(Buffer,BufferHead, Portion,0, Size);
										Portion_Size = Size;
										BufferHead += Size;
									}
									else {
										int Delta = BufferSize-BufferHead;
										if (Delta > BufferPlayPortion) {
											System.arraycopy(Buffer,BufferHead, Portion,0, BufferPlayPortion);
											Portion_Size = BufferPlayPortion;
											BufferHead += BufferPlayPortion;
										}
										else {
											System.arraycopy(Buffer,BufferHead, Portion,0, Delta);
											Portion_Size = Delta;
											Size = BufferPlayPortion-Delta;
											if (Size > 0) {
												if (Size > BufferTile)
													Size = BufferTile;
												System.arraycopy(Buffer,0, Portion,Delta, Size);
												Portion_Size += Size;
											}
											BufferHead = Size;
										}
									}
									Buffer_flEmpty = (BufferHead == BufferTile); 
								}
								else 
									Portion_Size = 0;
							}
							if (Portion_Size > 0)
								AudioPlayer.write(Portion, 0,Portion_Size);
							else
								break; //. >
						}
					}
				}
				catch (InterruptedException IE) {
				}
				catch (Throwable T) {
				}
			}
			
			public void PlayBuffer(byte[] pBuffer, int pBufferSize) {
				if (AudioPlayer == null)
					return; //. ->
				synchronized (BufferLock) {
					int Delta = BufferSize-BufferTile;
					if (Delta > pBufferSize) {
						System.arraycopy(pBuffer,0, Buffer,BufferTile, pBufferSize);
						if (BufferTile < BufferHead) {
							BufferTile += pBufferSize;
							if (BufferHead < BufferTile)
								BufferHead = BufferTile; 
						}
						else
							BufferTile += pBufferSize;
					}
					else {
						if (Delta > 0) {
							System.arraycopy(pBuffer,0, Buffer,BufferTile, Delta);
							if ((BufferTile < BufferHead) && (BufferHead < BufferSize))
									BufferHead = 0; 
						}
						pBufferSize = pBufferSize-Delta;
						if (pBufferSize > 0)
							System.arraycopy(pBuffer,Delta, Buffer,0, pBufferSize);
						BufferTile = pBufferSize;
						if (BufferHead < BufferTile)
							BufferHead = BufferTile; 
					}
					Buffer_flEmpty = false;				
				}
				PlaySignal.Set();
			}
		}
		
		private int Port;
		//.
		private TExceptionHandler ExceptionHandler;
		//.
		private Socket socket = null;
		//.
		public int SampleRate = DefaultSampleRate;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] inputBuffers;
		private ByteBuffer[] 	outputBuffers;
		private byte[]			outData;
		//.
		private AudioTrack 			AudioPlayer;
		private TAudioBufferPlaying AudioBufferPlaying;

		public TAudioClient(int pPort, TExceptionHandler pExceptionHandler) {
			Port = pPort;
			ExceptionHandler = pExceptionHandler;
			//.
			socket = new Socket();
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws IOException, InterruptedException {
			Cancel();
			if (socket != null)
				socket.close(); //. cancel socket blocking reading			
			Wait();
		}
		
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				_Thread.setPriority(Thread.MAX_PRIORITY);
				//.
				socket.connect(new InetSocketAddress("127.0.0.1", Port), InitializationTimeout);
				try {
					socket.setSoTimeout(InitializationTimeout);
					//.
					InputStream IS = socket.getInputStream();
					try {
						OutputStream OS = socket.getOutputStream();
						try {
							int ActualSize;
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
									AudioPlayer = null;
							    	try {
							    		AudioBufferPlaying = new TAudioBufferPlaying();
							    		TRtpDecoder RtpDecoder = new TRtpDecoder() {
							    			@Override
							    			public void DoOnOutput(byte[] OutputBuffer, int OutputBufferSize, int RtpTimestamp) throws IOException {
										    	DecodeInputBuffer(OutputBuffer,OutputBufferSize);
							    			}
							    		};
							    		try {
							    			//. AudioSampleServer_Service_AACPackets1
							    			TRtpBuffer TransferBuffer = new TRtpBuffer(); 
											byte[] PacketSizeBA = new byte[4];
											int PacketSize;
											socket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
											while (!Canceller.flCancel) {
												try {
									                ActualSize = TLANConnectionRepeater.InputStream_Read(IS,PacketSizeBA,PacketSizeBA.length);
											    	if (ActualSize == 0)
											    		break; //. > connection is closed
											    		else 
													    	if (ActualSize < 0) {
								    							if (ActualSize == -1)
								    								break; //. > stream EOF, connection is closed
								    							else
												    				throw new IOException("error of reading server socket data descriptor, RC: "+Integer.toString(ActualSize)); //. =>
												    		}
												}
												catch (SocketTimeoutException E) {
													continue; //. ^
												}
												if (ActualSize != PacketSizeBA.length)
													throw new IOException("wrong data descriptor"); //. =>
												PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
												if (PacketSize > 0) {
													if (PacketSize > TRtpBuffer.MAXPACKETSIZE)
														PacketSize = TRtpBuffer.MAXPACKETSIZE;
													ActualSize = TLANConnectionRepeater.InputStream_Read(IS,TransferBuffer.buffer,PacketSize);	
											    	if (ActualSize == 0)
											    		break; //. > connection is closed
											    		else 
													    	if (ActualSize < 0) {
														    	if (ActualSize == -1)
														    		break; //. > stream EOF, connection is closed
														    	else
														    		throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
													    	}
											    	//.
													///test Log.i("UDP packet", "<= TS: "+Long.toString(System.currentTimeMillis())+", size: "+Integer.toString(ActualSize));
											    	TransferBuffer.setBufferLength(ActualSize);
											    	RtpDecoder.DoOnInput(TransferBuffer);
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
							    		if (AudioPlayer != null) {
								    		AudioPlayer.stop();
								    		AudioPlayer.release();
								    		AudioPlayer = null;
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
				if (!Canceller.flCancel) {
					if (ExceptionHandler != null)
						ExceptionHandler.DoOnException(T);
				}
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
				//. AudioPlayer.write(outData, 0,bufferInfo.size);
				//.
				Codec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
			}
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
				outputBuffers = Codec.getOutputBuffers();
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				// Subsequent data will conform to new format.
			    MediaFormat format = Codec.getOutputFormat();
			    //.
			    SampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE); 
			    int ChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
			    int BufferSize = -1;
			    switch (format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)) {
			     
			    case 1:
			    	ChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
				    BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			    	break; //. >

			    case 2:
			    	ChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
				    BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
			    	break; //. >
			    }
		    	if (BufferSize <= 0)
	    			throw new IOException("error of AudioRecord.getMinBufferSize, RC: "+Integer.toString(BufferSize)); //. =>
		    	if (AudioPlayer != null) 
		    		AudioPlayer.stop();
		    	BufferSize = TAudioBufferPlaying.BufferPlayPortion*2;
				AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, ChannelConfig, AudioFormat.ENCODING_PCM_16BIT, BufferSize, AudioTrack.MODE_STREAM);
		    	AudioPlayer.setStereoVolume(1.0F,1.0F);
		    	AudioPlayer.play();
			}
		}		
	}
	
	public static class TVideoClient extends TCancelableThread {
		
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 10000; //. microseconds

		public static final int DefaultFrameRate = 25;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private int Port;
		//.
		private Surface surface;
		private int 	Width;
		private int 	Height;
		//.
		private TExceptionHandler ExceptionHandler;
		//.
		private Socket socket = null;
		//.
		public int FrameRate = DefaultFrameRate;
		//.
		private MediaCodec Codec;
		private ByteBuffer[] inputBuffers;
		@SuppressWarnings("unused")
		private ByteBuffer[] outputBuffers;

		public TVideoClient(int pPort, Surface psurface, int pWidth, int pHeight, TExceptionHandler pExceptionHandler) {
			Port = pPort;
			surface = psurface;
			Width = pWidth;
			Height = pHeight;
			ExceptionHandler = pExceptionHandler;
			//.
			socket = new Socket();
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws IOException, InterruptedException {
			Cancel();
			if (socket != null)
				socket.close(); //. cancel socket blocking reading			
			Wait();
		}
		
		
		@SuppressLint("NewApi")
		@Override
		public void run() {
			try {
				_Thread.setPriority(Thread.MAX_PRIORITY);
				//.
				socket.connect(new InetSocketAddress("127.0.0.1", Port), InitializationTimeout);
				try {
					socket.setSoTimeout(InitializationTimeout);
					//.
					InputStream IS = socket.getInputStream();
					try {
						OutputStream OS = socket.getOutputStream();
						try {
							int ActualSize;
							Codec = MediaCodec.createDecoderByType(CodecTypeName);
							try {
								MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, Width,Height);
								Codec.configure(format, surface, null, 0);
								Codec.start();
								try {
									inputBuffers = Codec.getInputBuffers();
									outputBuffers = Codec.getOutputBuffers();
									//.
						    		TRtpDecoder RtpDecoder = new TRtpDecoder() {
						    			@Override
						    			public void DoOnOutput(byte[] OutputBuffer, int OutputBufferSize, int RtpTimestamp) throws IOException {
					    					DecodeInputBuffer(OutputBuffer,OutputBufferSize);
						    			}
						    		};
						    		//. 
						    		TRtpBuffer TransferBuffer = new TRtpBuffer(); 
									byte[] PacketSizeBA = new byte[4];
									int PacketSize;
									socket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
									while (!Canceller.flCancel) {
										try {
							                ActualSize = TLANConnectionRepeater.InputStream_Read(IS,PacketSizeBA,PacketSizeBA.length);
									    	if (ActualSize == 0)
									    		break; //. > connection is closed
									    		else 
											    	if (ActualSize < 0) {
												    	if (ActualSize == -1)
												    		break; //. > stream EOF, connection is closed
												    	else
												    		throw new IOException("error of reading server socket data descriptor, RC: "+Integer.toString(ActualSize)); //. =>
											    	}
										}
										catch (SocketTimeoutException E) {
											continue; //. ^
										}
										if (ActualSize != PacketSizeBA.length)
											throw new IOException("wrong data descriptor"); //. =>
										PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
										if (PacketSize > 0) {
											if (PacketSize > TRtpBuffer.MAXPACKETSIZE)
												PacketSize = TRtpBuffer.MAXPACKETSIZE;
											ActualSize = TLANConnectionRepeater.InputStream_Read(IS,TransferBuffer.buffer,PacketSize);	
									    	if (ActualSize == 0)
									    		break; //. > connection is closed
									    		else 
											    	if (ActualSize < 0) {
												    	if (ActualSize == -1)
												    		break; //. > stream EOF, connection is closed
												    	else
												    		throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
											    	}
									    	//.
									    	TransferBuffer.setBufferLength(ActualSize);
									    	RtpDecoder.DoOnInput(TransferBuffer);
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
				if (!Canceller.flCancel) {
					if (ExceptionHandler != null)
						ExceptionHandler.DoOnException(T);
				}
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
	
	private static Random rnd = new Random();
	
	private TLANConnectionUDPRepeater	AudioLocalServer = null;
	private TAudioClient				AudioClient = null;
	//.
	private TLANConnectionUDPRepeater	VideoLocalServer = null;
	private TVideoClient				VideoClient = null;
	
    public TVideoRecorderServerViewUDPRTP(Context pcontext, String pGeographProxyServerAddress, int pGeographProxyServerPort, int pUserID, String pUserPassword, TCoGeoMonitorObject pObject, boolean pflAudio, boolean pflVideo, String pUserAccessKey, TExceptionHandler pExceptionHandler, TextView plbVideoRecorderServer) {
    	super(pcontext, pGeographProxyServerAddress,pGeographProxyServerPort, pUserID,pUserPassword, pObject, pflAudio,pflVideo, pUserAccessKey, pExceptionHandler, plbVideoRecorderServer);
		if (GeographProxyServerPort == TUDPEchoServerClient.ServerDefaultPort) {
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker != null) {
	    		try {
					TGeographProxyServerClient.TServerInfo GPSI = Tracker.GeoLog.ConnectorModule.GetGeographProxyServerInfo();
					if (GPSI.UDPEchoServerInfo != null) { 
						GeographProxyServerAddress = GPSI.Address;
						GeographProxyServerPort = GPSI.UDPEchoServerInfo.Ports[rnd.nextInt(GPSI.UDPEchoServerInfo.Ports.length)]; //. get one of the UDP proxy ports
					}
					
				} catch (Exception E) {
				}
	    	}
		}
    }
	
    @Override
	public void AudioClient_Initialize() throws Exception {
		AudioClient_Finalize();
		if (flAudio) {
			AudioClient = new TAudioClient(AudioLocalServer.GetPort(), new TExceptionHandler() {
				@Override
				public void DoOnException(Throwable E) {
					if (!(E instanceof SocketException))
						TVideoRecorderServerViewUDPRTP.this.DoOnProcessingException(E);
				}
			});
		} 
	}
	
    @Override
	public void AudioClient_Finalize() throws Exception {
		if (AudioClient != null) {
			AudioClient.Destroy();
			AudioClient = null;
		}
	}
	
    @Override
	public void VideoClient_Initialize() throws Exception {
		VideoClient_Finalize();
		if (flVideo && (VideoSurface != null)) {
			VideoClient = new TVideoClient(VideoLocalServer.GetPort(), VideoSurface,VideoSurfaceWidth,VideoSurfaceHeight, new TExceptionHandler() {
				public void DoOnException(Throwable E) {
					if (!(E instanceof SocketException))
						TVideoRecorderServerViewUDPRTP.this.DoOnProcessingException(E);
				}
			});
		}
	}
	
    @Override
	public void VideoClient_Finalize() throws Exception {
		if (VideoClient != null) {
			VideoClient.Destroy();
			VideoClient = null;
		}
	}
	
    @Override
	public void Initialize() throws Exception {
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
		TLANConnectionExceptionHandler AudioLocalServerExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				String S = context.getString(R.string.SErrorOfAudio);
				Exception Ex = new Exception(S+", "+E.getMessage());
				TVideoRecorderServerViewUDPRTP.this.DoOnProcessingException(Ex);
			}
		};		
		TLANConnectionExceptionHandler VideoLocalServerExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				String S = context.getString(R.string.SErrorOfVideo);
				Exception Ex = new Exception(S+", "+E.getMessage());
				TVideoRecorderServerViewUDPRTP.this.DoOnProcessingException(Ex);
			}
		};		
		TLANConnectionUDPStartHandler StartHandler = ObjectModel.TLANConnectionUDPStartHandler_Create(Object);
		TLANConnectionUDPStopHandler StopHandler = ObjectModel.TLANConnectionUDPStopHandler_Create(Object); 
		//.
		if (flAudio)
			AudioLocalServer = new TLANConnectionUDPRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",AudioServerPort, AudioPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.GeographServerObjectID(), null, UserAccessKey, AudioLocalServerExceptionHandler, StartHandler,StopHandler);
		if (flVideo)
			VideoLocalServer = new TLANConnectionUDPRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",VideoServerPort, VideoPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.GeographServerObjectID(), null, UserAccessKey, VideoLocalServerExceptionHandler, StartHandler,StopHandler);
		//.
		AudioClient_Initialize();
		VideoClient_Initialize();
		//.
		flActive = true;
	}

    @Override
	public void Finalize() throws Exception {
		flActive = false;
		//.
		VideoClient_Finalize();
		AudioClient_Finalize();
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
	
    @Override
	public void Reinitialize() throws Exception {
		Finalize();
		Initialize();
	}
	
    @Override
	public void UpdateInfo() {
		if (lbVideoRecorderServer == null)
			return; //. ->
		if (!flActive) {
	        lbVideoRecorderServer.setText(R.string.SNotActiveYet);
	        return; //. ->
		}
        //.
        String S = context.getString(R.string.SViewer1);
        if (flVideo)
        	S = S+context.getString(R.string.SVideo);
        else
        	S = S+context.getString(R.string.SNoVideo);
        if (flAudio)
        	S = S+", "+context.getString(R.string.SAudio);
        else
        	S = S+", "+context.getString(R.string.SNoAudio);
        lbVideoRecorderServer.setText(S);
	}	
}
