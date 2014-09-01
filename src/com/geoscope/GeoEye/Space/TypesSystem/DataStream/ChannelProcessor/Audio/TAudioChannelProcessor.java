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

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor.TChannel.TConfigurationParcer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor;

public class TAudioChannelProcessor extends TStreamChannelProcessor {

	public static TStreamChannelProcessor GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
		if (pTypeID.equals(TMediaFrameServerAudioAACClient.TypeID)) 
			return (new TAudioChannelProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters)); //. ->
		else 
			return null; //. ->
	}
	
	public static abstract class TMediaFrameServerAudioClient {
	    
		protected TStreamChannelProcessor Processor;
		//.
		protected byte[] 	Buffer = new byte[1024];
		protected short		BufferSize = 0;
		protected short		BufferIndex = 0;
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
			
			public void Destroy() {
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
		
		public TMediaFrameServerAudioClient(TStreamChannelProcessor pProcessor) {
			Processor = pProcessor;
		}
		
		public abstract void Open() throws Exception;
		public abstract void Close();
		public abstract void DoOnRead(byte[] ReadBuffer, int ReadSize, TCanceller Canceller);
		public abstract void DoOnException(Exception E);
	}
	
	public static class TMediaFrameServerAudioAACClient extends TMediaFrameServerAudioClient {
		
		public static final String TypeID = "Audio.AAC";
		//.
		private static final String CodecTypeName = "audio/mp4a-latm";
		private static final int 	CodecLatency = 10000; //. milliseconds
		//.
		private static int[] SamplingFrequencies = new int[] {96000,88200,64000,48000,44100,32000,24000,22050,16000,12000,11025,8000,7350};
		

		private boolean BufferSize_flRead = false;
		//.
		private boolean flConfigIsProcessed;
		
		private MediaCodec 			Codec;
		private ByteBuffer[] 		inputBuffers;
		private ByteBuffer[] 		outputBuffers;
		private byte[]				outData;
		//.
		private AudioTrack 			AudioPlayer;
		private TAudioBufferPlaying	AudioBufferPlaying;
		
		public TMediaFrameServerAudioAACClient(TStreamChannelProcessor pProcessor) throws IOException {
			super(pProcessor);
		}

		@Override
		public void Open() throws Exception {
			byte[] Config = ((TAudioChannelProcessor)Processor).ConfigurationBuffer;
			//.
			@SuppressWarnings("unused")
		    byte ObjectType = (byte)(Config[0] >> 3);
			byte FrequencyIndex = (byte)(((Config[0] & 7) << 1) | ((Config[1] >> 7) & 0x01));
			byte ChannelCount = (byte)((Config[1] >> 3) & 0x0F);
			int SampleRate = SamplingFrequencies[FrequencyIndex];
			//.
			Codec = MediaCodec.createDecoderByType(CodecTypeName);
			//.
			MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName,SampleRate,ChannelCount);
			Codec.configure(format, null, null, 0);
			Codec.start();
			//.
			inputBuffers = Codec.getInputBuffers();
			outputBuffers = Codec.getOutputBuffers();
			outData = new byte[0];
			//.
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
			AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, ChannelConfig, AudioFormat.ENCODING_PCM_16BIT, BufferSize*10, AudioTrack.MODE_STREAM);
	    	AudioPlayer.setStereoVolume(1.0F,1.0F);
	    	AudioPlayer.play();
			//.
    		AudioBufferPlaying = new TAudioBufferPlaying(AudioPlayer);
    		//.
    		flConfigIsProcessed = false;
		}
			    		
		@Override
		public void Close() {
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
	    public void DoOnRead(byte[] ReadBuffer, int ReadSize, TCanceller Canceller) {
			try {
		    	int ReadBuffer_Index = 0;
		    	while (ReadSize > 0) {
		    		if (!BufferSize_flRead) {
						BufferSize = TDataConverter.ConvertLEByteArrayToInt16(ReadBuffer, ReadBuffer_Index);
		    			ReadBuffer_Index += 2; //. SizeOf(Short)
		    			ReadSize -= 2; //. SizeOf(Short)
		    			//.
		    			if (BufferSize > Buffer.length)
		    				Buffer = new byte[BufferSize];
		    			BufferIndex = 0;
		    			//.
			    		if (ReadSize == 0) 
			    			return; //. ->
		    		}
		    		int Delta = (BufferSize-BufferIndex);
		    		if (Delta <= ReadSize) {
		    			System.arraycopy(ReadBuffer,ReadBuffer_Index, Buffer,BufferIndex, Delta);
		    			//.
		    			BufferSize_flRead = false;
		    			BufferIndex = 0;
		    			//.
		    			ReadBuffer_Index += Delta;
		    			ReadSize -= Delta; 
		    			//.
		    			try {
		    				if (!flConfigIsProcessed) {
		    					flConfigIsProcessed = true;
		    					//.
								DecodeInputBuffer(((TAudioChannelProcessor)Processor).ConfigurationBuffer,((TAudioChannelProcessor)Processor).ConfigurationBuffer.length);
		    				}
							DecodeInputBuffer(Buffer,BufferSize);
						} catch (IOException IOE) {
							DoOnException(IOE);
						}
		    		}
		    		else {
		    			System.arraycopy(ReadBuffer,ReadBuffer_Index, Buffer,BufferIndex, (int)ReadSize);
		    			//.
		    			BufferIndex += ReadSize;
		    			ReadBuffer_Index += ReadSize;
		    			//.
		    			return; //. ->
		    		}
		    	}
			} catch (Exception E) {
				DoOnException(E);
			}
	    }

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
				//. Subsequent data will conform to new format.
				//. should not be here
			}
		}

		@Override
		public void DoOnException(Exception E) {
			Processor.DoOnStreamChannelException(E);
		}
	}

	private byte[] ConfigurationBuffer;
	//.
	public TMediaFrameServerAudioClient AudioClient = null;
	
	public TAudioChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters);
		if (TypeID.equals(TMediaFrameServerAudioAACClient.TypeID)) 
			AudioClient = new TMediaFrameServerAudioAACClient(this);
		else
			AudioClient = null;
	}	
	
	@Override
	public void Destroy() {
		super.Destroy();
		//.
		if (AudioClient != null) {
			AudioClient.Close();
			AudioClient = null;
		}
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
    public void Open() throws Exception {
		AudioClient.Open();
    }
        
	@Override
    public void Close() {
		AudioClient.Close();
    }
        
	@Override
    public boolean IsVisual() {
    	return false;
    }

	@Override
    public void DoOnStreamChannelRead(byte[] Buffer, int BufferSize, TCanceller Canceller) {
    	AudioClient.DoOnRead(Buffer,BufferSize, Canceller);
    }	
}
