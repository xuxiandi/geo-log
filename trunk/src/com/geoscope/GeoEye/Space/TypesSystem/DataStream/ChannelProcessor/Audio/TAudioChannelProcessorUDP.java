package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Audio;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Base64;

import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPDecoder;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPPacket;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor.TChannel.TConfigurationParcer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessorUDP;

public class TAudioChannelProcessorUDP extends TStreamChannelProcessorUDP {

	public static TStreamChannelProcessorUDP GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
		if (pTypeID.equals(TMediaFrameServerAudioAACUDPRTPClient.TypeID)) 
			return (new TAudioChannelProcessorUDP(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler)); //. ->
		else 
			return null; //. ->
	}
	
    public static final int DefaultReadingTimeout = 1000; //. ms
    
	public static abstract class TMediaFrameServerAudioClient {
	    
		protected TStreamChannelProcessorUDP Processor;
		//.
		protected byte[] 	Buffer = new byte[1024];
		//.
		protected String ExceptionMessage;
		
		protected class TAudioBufferPlaying extends TCancelableThread {
			
			private static final int BufferSize = 4096*10;
			private static final int BufferPlayPortion = 16;
			
			private AudioTrack AudioPlayer;
			//.
			private Object 	BufferLock = new Object();
			private byte[] 	Buffer = new byte[BufferSize];
			private int		BufferHead = 0;
			private int		BufferTile = 0;
			private boolean Buffer_flEmpty = true;
			
			private TAutoResetEvent	PlaySignal = new TAutoResetEvent();
			
			public TAudioBufferPlaying(AudioTrack pAudioPlayer) {
				AudioPlayer = pAudioPlayer;
				//.
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
		
		public TMediaFrameServerAudioClient(TStreamChannelProcessorUDP pProcessor) {
			Processor = pProcessor;
		}
		
		public abstract void Open() throws Exception;
		public abstract void Close() throws Exception;
		public abstract void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller);
		public abstract void DoOnException(Exception E);
	}
	
	public static class TMediaFrameServerAudioAACUDPRTPClient extends TMediaFrameServerAudioClient {
		
		public static final String TypeID = "Audio.AACUDPRTP";
		//.
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. microseconds
		//.
		private static int[] SamplingFrequencies = new int[] {96000,88200,64000,48000,44100,32000,24000,22050,16000,12000,11025,8000,7350};
		//.
		private static int MaxUnderlyingStreamSize = 1024*100;
		
		private class TOutputProcessing extends TCancelableThread {
			
			private IOException _Exception = null;
			
			public TOutputProcessing() {
				_Thread = new Thread(this);
				_Thread.start();
			}
			
			public void Destroy() throws InterruptedException {
				CancelAndWait();
			}
			
			@Override
			public void run()  {
				try {
					_Thread.setPriority(Thread.NORM_PRIORITY);
					//.
					MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
					while (!Canceller.flCancel) {
						int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
						while (outputBufferIndex >= 0) {
							ByteBuffer outputBuffer = OutputBuffers[outputBufferIndex];
							if (OutData.length < bufferInfo.size)
								OutData = new byte[bufferInfo.size];
							outputBuffer.rewind(); //. reset position to 0
							outputBuffer.get(OutData, 0,bufferInfo.size);
							//. process output
							AudioBufferPlaying.PlayBuffer(OutData, bufferInfo.size);
							//.
							Codec.releaseOutputBuffer(outputBufferIndex, false);
							outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
						}
						if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
							OutputBuffers = Codec.getOutputBuffers();
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
					    	//.
					    	if (AudioPlayer != null) 
					    		AudioPlayer.stop();
							AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, ChannelConfig, AudioFormat.ENCODING_PCM_16BIT, BufferSize*10, AudioTrack.MODE_STREAM);
					    	AudioPlayer.setStereoVolume(1.0F,1.0F);
					    	AudioPlayer.play();
					    	//.
					    	if (AudioBufferPlaying != null) {
					    		AudioBufferPlaying.Destroy();
					    		AudioBufferPlaying = null;
					    	}
				    		AudioBufferPlaying = new TAudioBufferPlaying(AudioPlayer);
						}
					}
				}
				catch (Throwable T) {
					synchronized (this) {
						_Exception = new IOException(T.getMessage());
					}
				}
			}
			
			public synchronized IOException GetException() {
				return _Exception;
			}
		}
		
		
		private int SampleRate;
		//.
		private TRTPDecoder RTPDecoder;
		private TRTPPacket	RTPPacket;
		//.
		private MediaCodec 			Codec;
		private ByteBuffer[] 		InputBuffers;
		private ByteBuffer[] 		OutputBuffers;
		private byte[]				OutData;
		private TOutputProcessing 	OutputProcessing = null;
		//.
		private AudioTrack 			AudioPlayer;
		private TAudioBufferPlaying	AudioBufferPlaying;
		//.
		private boolean flConfigIsProcessed;
		
		public TMediaFrameServerAudioAACUDPRTPClient(TStreamChannelProcessorUDP pProcessor) throws IOException {
			super(pProcessor);
		}

		@Override
		public void Open() throws Exception {
			byte[] Config = ((TAudioChannelProcessorUDP)Processor).ConfigurationBuffer;
			//.
			@SuppressWarnings("unused")
		    byte ObjectType = (byte)(Config[0] >> 3);
			byte FrequencyIndex = (byte)(((Config[0] & 7) << 1) | ((Config[1] >> 7) & 0x01));
			byte ChannelCount = (byte)((Config[1] >> 3) & 0x0F);
			SampleRate = SamplingFrequencies[FrequencyIndex];
			//.
			RTPDecoder = new TRTPDecoder() {
				@Override
				public void DoOnOutput(byte[] OutputBuffer, int OutputBufferSize, int RtpTimestamp) throws IOException {
	  	    	      try {
	  	    	    	  if (!flConfigIsProcessed) {
	  	    	    		  flConfigIsProcessed = true;
	  	    	    		  //.
	  	    	    		  DecodeInputBuffer(((TAudioChannelProcessorUDP)Processor).ConfigurationBuffer,((TAudioChannelProcessorUDP)Processor).ConfigurationBuffer.length);
	  	    	    	  }
	  	    	    	  DecodeInputBuffer(OutputBuffer,OutputBufferSize);
	  	    	      } catch (IOException IOE) {
							DoOnException(IOE);
	  	    	      }
				}
			};
			RTPPacket = new TRTPPacket(null,0);
			//.
			Codec = MediaCodec.createDecoderByType(CodecTypeName);
			//.
			MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName,SampleRate,ChannelCount);
			Codec.configure(format, null, null, 0);
			Codec.start();
			//.
			InputBuffers = Codec.getInputBuffers();
			OutputBuffers = Codec.getOutputBuffers();
			OutData = new byte[0];
			OutputProcessing = new TOutputProcessing();
			//.
			AudioPlayer = null;
			AudioBufferPlaying = null;
    		//.
    		flConfigIsProcessed = false;
		}
			    		
		@Override
		public void Close() throws Exception {
			if (OutputProcessing != null) {
				OutputProcessing.Destroy();
				OutputProcessing = null;
			}
			if (AudioBufferPlaying != null) {
				AudioBufferPlaying.Destroy();
				AudioBufferPlaying = null;
			}
    		if (AudioPlayer != null) {
	    		AudioPlayer.stop();
	    		AudioPlayer = null;
    		}
			if (Codec != null) {
				Codec.stop();
				Codec.release();
				Codec = null;
			}
		}
		
		@Override
	    public void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller) {
	    	try {
	    		try {
	    			if (ReadSize > Buffer.length)
	    				Buffer = new byte[ReadSize];
	    			Stream.Read(Buffer,ReadSize);
	    			//. process UDP buffer
	  	    	    RTPPacket.SetBuffer(Buffer,ReadSize);
	  	    	    RTPDecoder.DoOnInput(RTPPacket);
	    		}
		    	finally {
			    	if ((Stream.Size > MaxUnderlyingStreamSize) && (Stream.Size == Stream.Position))
			    		Stream.Clear();
			    }
			} catch (Exception E) {
				DoOnException(E);
			}
	    }

		public void DecodeInputBuffer(byte[] input, int input_size) throws IOException {
			int inputBufferIndex = Codec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = InputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input, 0,input_size);
				Codec.queueInputBuffer(inputBufferIndex, 0, input_size, SystemClock.elapsedRealtime(), 0);
			}
			//.
			IOException E = OutputProcessing.GetException();
			if (E != null)
				throw E; //. =>
		}

		@Override
		public void DoOnException(Exception E) {
			Processor.DoOnStreamChannelException(E);
		}
	}

	
	private byte[] ConfigurationBuffer;
	//.
	public TMediaFrameServerAudioClient AudioClient = null;
	
	public TAudioChannelProcessorUDP(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
		//.
		ReadingTimeout = DefaultReadingTimeout;
		//.
		if (TypeID.equals(TMediaFrameServerAudioAACUDPRTPClient.TypeID)) 
			AudioClient = new TMediaFrameServerAudioAACUDPRTPClient(this);
		else
			AudioClient = null;
	}	
	
	@Override
	public void ParseConfiguration() throws Exception {
		TConfigurationParcer CP = new TConfigurationParcer(Configuration);
		int Version = Integer.parseInt(CP.DecoderConfiguration[0]);
		if (Version != 1)
			throw new Exception("unknown configuration version"); //. =>
		ConfigurationBuffer = Base64.decode(CP.DecoderConfiguration[1], Base64.NO_WRAP);		
	}
	
	@Override
    protected void Open() throws Exception {
		AudioClient.Open();
    }
        
	@Override
    protected void Close() throws Exception {
		AudioClient.Close();
    }
        
	@Override
    public boolean IsVisual() {
    	return false;
    }

	@Override
    public void DoOnStreamChannelRead(TStream Stream, int ReadSize, TCanceller Canceller) {
    	AudioClient.DoOnRead(Stream,ReadSize, Canceller);
    }
}
