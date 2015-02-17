package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

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

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Exception.TExceptionHandler;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStopHandler;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;

public class TVideoRecorderServerViewTCP extends TVideoRecorderServerView {

	public static final int AudioServerPort = 10001;
	public static final int AudioPort = 5001;
	public static final int VideoServerPort = 10002;
	public static final int VideoPort = 5002;
	
	public static class TAudioClient extends TCancelableThread {
		
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. microseconds
		private static final int 	CodecWaitInterval = 1000000; //. microseconds

		public static final int DefaultSampleRate = 8000;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private static final int TransferBufferSize = 1024*1024;
		
		private class TAudioBufferPlaying extends TCancelableThread {
			
			private static final int BufferSize = 4096*10;
			private static final int BufferPlayPortion = 16;
			
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
		private AudioTrack 	AudioPlayer;
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
		
		@Override
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
							byte[] InitBuffer = new byte[4/*SizeOf(Service)*/+4/*SizeOf(SampleRate)*/+4/*SizeOf(SamplePacketSize)*/+4/*SizeOf(Quality)*/];
							int Idx = 0;
							//. set service type
							int Service = TAudioModule.AudioSampleServer_Service_AACPackets1;
							byte[] DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Service);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set sample rate
							DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(SampleRate);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set sample packet size
							DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(0);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set frame quality
							DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(100);
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
									AudioPlayer = null;
							    	try {
							    		AudioBufferPlaying = new TAudioBufferPlaying();
							    		try {
							    			//. AudioSampleServer_Service_AACPackets1
							    			byte[] TransferBuffer = new byte[TransferBufferSize];
											byte[] PacketSizeBA = new byte[2];
											short PacketSize;
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
												PacketSize = (short)(((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF));
												if (PacketSize > 0) {
													if (PacketSize > TransferBuffer.length)
														TransferBuffer = new byte[PacketSize];
													ActualSize = TLANConnectionRepeater.InputStream_Read(IS,TransferBuffer,PacketSize);	
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
											    	DecodeInputBuffer(TransferBuffer,PacketSize);
												}
												else
													break; //. > disconnect
											}
							    			//. AudioSampleServer_Service_AACPackets2
											/*byte[] TransferBuffer = new byte[TransferBufferSize];
											socket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
											while (!Canceller.flCancel) {
												try {
									                ActualSize = IS.read(TransferBuffer,0,TransferBuffer.length);
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
										    	//.
										    	DecodeInputBuffer(TransferBuffer,ActualSize);
											}*/
							    		}
							    		finally {
							    			AudioBufferPlaying.Destroy();
							    		}
							    	}
							    	finally {
							    		if (AudioPlayer != null) {
								    		AudioPlayer.stop();
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
			int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
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
				AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, ChannelConfig, AudioFormat.ENCODING_PCM_16BIT, BufferSize*10, AudioTrack.MODE_STREAM);
		    	AudioPlayer.setStereoVolume(1.0F,1.0F);
		    	AudioPlayer.play();
			}
		}		
	}
	
	public static class TVideoClient extends TCancelableThread {
		
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 1000; //. microseconds
		private static final int 	CodecWaitInterval = 1000000; //. microseconds

		public static final int DefaultFrameRate = 25;
		
		public static final int InitializationTimeout = 1000*30; //. seconds
		
		private static final int TransferBufferSize = 1024*1024;
		
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
		
		@Override
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
							byte[] InitBuffer = new byte[4/*SizeOf(Service)*/+4/*SizeOf(FrameRate)*/+4/*SizeOf(Quality)*/];
							int Idx = 0;
							//. set service type
							int Service = TVideoModule.VideoFrameServer_Service_H264Frames;
							byte[] DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Service);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set frame rate
							DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(FrameRate);
							System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
							//. set frame quality
							DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(100);
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
											if (PacketSize > TransferBuffer.length)
												TransferBuffer = new byte[PacketSize];
											ActualSize = TLANConnectionRepeater.InputStream_Read(IS,TransferBuffer,PacketSize);	
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
									    	DecodeInputBuffer(TransferBuffer,PacketSize);
										}
										else
											break; //. > disconnect
									}
									/*///? VideoFrameServer_Service_H264Frames1 byte[] TransferBuffer = new byte[TransferBufferSize];
									socket.setSoTimeout(TLANConnectionRepeater.ServerReadWriteTimeout);
									while (!Canceller.flCancel) {
										try {
							                ActualSize = IS.read(TransferBuffer,0,TransferBuffer.length);
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
									    //.
									    DecodeInputBuffer(TransferBuffer,ActualSize);
									}*/
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
			int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
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
	
	private TLANConnectionRepeater 	AudioLocalServer = null;
	private TAudioClient			AudioClient = null;
	//.
	private TLANConnectionRepeater 	VideoLocalServer = null;
	private TVideoClient			VideoClient = null;
	
    public TVideoRecorderServerViewTCP(Context pcontext, String pGeographProxyServerAddress, int pGeographProxyServerPort, long pUserID, String pUserPassword, TCoGeoMonitorObject pObject, boolean pflAudio, boolean pflVideo, String pUserAccessKey, TExceptionHandler pExceptionHandler, TextView plbVideoRecorderServer) {
    	super(pcontext, pGeographProxyServerAddress,pGeographProxyServerPort, pUserID,pUserPassword, pObject, pflAudio,pflVideo, pUserAccessKey, pExceptionHandler, plbVideoRecorderServer);
    }
	
    @Override
	public void AudioClient_Initialize() throws Exception {
		AudioClient_Finalize();
		if (flAudio) {
			AudioClient = new TAudioClient(AudioLocalServer.GetPort(), new TExceptionHandler() {
				@Override
				public void DoOnException(Throwable E) {
					if (!(E instanceof SocketException))
						TVideoRecorderServerViewTCP.this.DoOnProcessingException(E);
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
						TVideoRecorderServerViewTCP.this.DoOnProcessingException(E);
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
				TVideoRecorderServerViewTCP.this.DoOnProcessingException(Ex);
			}
		};		
		TLANConnectionExceptionHandler VideoLocalServerExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				String S = context.getString(R.string.SErrorOfVideo);
				Exception Ex = new Exception(S+", "+E.getMessage());
				TVideoRecorderServerViewTCP.this.DoOnProcessingException(Ex);
			}
		};		
		TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Object);
		TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Object); 
		//.
		if (flAudio)
			AudioLocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",AudioServerPort, AudioPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.GeographServerObjectID(), UserAccessKey, AudioLocalServerExceptionHandler, StartHandler,StopHandler);
		if (flVideo)
			VideoLocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",VideoServerPort, VideoPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.GeographServerObjectID(), UserAccessKey, VideoLocalServerExceptionHandler, StartHandler,StopHandler);
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
