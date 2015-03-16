package com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

@SuppressLint({ "NewApi" })
public class TAACEncoder {

	public static boolean IsSupported() {
		return (android.os.Build.VERSION.SDK_INT >= 16); 
	}
	
	private static final String CodecTypeName = "audio/mp4a-latm";
	private static final int	CodecLatency = 10000; //. microseconds
	private static final int 	CodecWaitInterval = 1000000; //. microseconds
		
	private class TOutputProcessing extends TCancelableThread {
		
		private IOException _Exception = null;
		
		public TOutputProcessing() {
    		super();
    		//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws InterruptedException {
			CancelAndWait();
		}
		
		@Override
		public void run()  {
			try {
				_Thread.setPriority(Thread.MAX_PRIORITY);
				//.
				OutputBufferCount = 0;
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
						if (flParseParameters && (OutputBufferCount == 0))
							DoOnParameters(OutData,bufferInfo.size);
						else
							DoOnOutputBuffer(OutData,bufferInfo.size,bufferInfo.presentationTimeUs);
						OutputBufferCount++;
						//.
						Codec.releaseOutputBuffer(outputBufferIndex, false);
						outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, 0);
					}
					if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
					     OutputBuffers = Codec.getOutputBuffers();
					else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					     // Subsequent data will conform to new format.
					     ///? MediaFormat format = codec.getOutputFormat();
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

	private boolean flParseParameters;
	
	private MediaCodec 	Codec;
	//.
	private ByteBuffer[] InputBuffers;
	//.
	private TOutputProcessing 	OutputProcessing = null;
	private ByteBuffer[] 		OutputBuffers;
	private byte[] 				OutData;
	private int 				OutputBufferCount;
 
	public TAACEncoder(int BitRate, int SampleRate, boolean pflParseParameters) {
		flParseParameters = pflParseParameters;
		//.
		Codec = MediaCodec.createEncoderByType(CodecTypeName);
		//.
		MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName, SampleRate, 1);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BitRate);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_IS_ADTS, 0); 		
		Codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		Codec.start();
		//.
		InputBuffers = Codec.getInputBuffers();
		OutputBuffers = Codec.getOutputBuffers();
		OutData = new byte[0];
		OutputProcessing = new TOutputProcessing();
	}
 
	public TAACEncoder(int BitRate, int SampleRate) {
		this(BitRate, SampleRate, false);
	}
	
	public void Destroy() throws Exception {
		if (OutputProcessing != null) {
			OutputProcessing.Destroy();
			OutputProcessing = null;
		}
		if (Codec != null) {
			Codec.stop();
			Codec.release();
			Codec = null;
		}
	}
 
	public void EncodeInputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
		int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = InputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(input, 0,input_size);
			Codec.queueInputBuffer(inputBufferIndex, 0, input_size, Timestamp, 0);
		}
		//.
		IOException E = OutputProcessing.GetException();
		if (E != null)
			throw E; //. =>
	}
	
	public void DoOnParameters(byte[] Buffer, int BufferSize) throws IOException {
	}
	
	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
	}
}
