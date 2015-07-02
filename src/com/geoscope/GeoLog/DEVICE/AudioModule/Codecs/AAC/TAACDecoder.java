package com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

public class TAACDecoder {

	private static final String CodecTypeName = "audio/mp4a-latm";
	private static final int 	CodecLatency = 10000; //. microseconds
	private static final int 	CodecWaitInterval = 1000000; //. microseconds

	
	private class TOutputProcessing extends TCancelableThread {
		
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
				_Thread.setPriority(Thread.NORM_PRIORITY);
				//.
				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				byte[] outData = new byte[0];
				while (!Canceller.flCancel) {
					int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
					while (outputBufferIndex >= 0) {
						ByteBuffer outputBuffer = OutputBuffers[outputBufferIndex];
						if (outData.length < bufferInfo.size)
							outData = new byte[bufferInfo.size];
						outputBuffer.rewind(); //. reset position to 0
						outputBuffer.get(outData, 0,bufferInfo.size);
						//.
						DoOnOutputBuffer(outData,bufferInfo.size, bufferInfo.presentationTimeUs);
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
					    int SampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
					    int ChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
					    //.
					    DoOnConfiguration(SampleRate, ChannelCount);
					}
				}
			}
			catch (Throwable T) {
				/* String S = T.getMessage();
				if (S == null)
					S = T.getClass().getName(); */
			}
		}
	}
	
	private MediaCodec 			Codec;
	private ByteBuffer[] 		InputBuffers;
	private ByteBuffer[] 		OutputBuffers;
	private TOutputProcessing 	OutputProcessing = null;
	
	public TAACDecoder(int SampleRate) throws IOException {
		Codec = MediaCodec.createDecoderByType(CodecTypeName);
		//.
		MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName, SampleRate, 1);
		Codec.configure(format, null, null, 0);
		Codec.start();
		//.
		InputBuffers = Codec.getInputBuffers();
		OutputBuffers = Codec.getOutputBuffers();
		OutputProcessing = new TOutputProcessing();
	}

	public void Destroy() throws InterruptedException {
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
	
	public void DecodeInputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
		int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = InputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(input, 0,input_size);
			Codec.queueInputBuffer(inputBufferIndex, 0, input_size, Timestamp, 0);
		}
	}
	
	public void DecodeInputBuffer(byte[] input, int input_offset, int input_size, long Timestamp) throws IOException {
		int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = InputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(input, input_offset,input_size);
			Codec.queueInputBuffer(inputBufferIndex, 0, input_size, Timestamp, 0);
		}
	}
	
	public void DoOnInputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
		DecodeInputBuffer(input,input_size, Timestamp);
	}

	public void DoOnInputBuffer(byte[] input, int input_offset, int input_size, long Timestamp) throws IOException {
		DecodeInputBuffer(input, input_offset,input_size, Timestamp);
	}

	public void DoOnConfiguration(int SampleRate, int ChannelCount) throws IOException {
	}	

	public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
	}
}
