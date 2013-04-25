package com.geoscope.GeoLog.DEVICE.AudioModule.Codecs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

@SuppressLint({ "NewApi" })
public class AACEncoder {

	private static final String CodecTypeName = "audio/mp4a-latm";
	private static final String CodecName = "OMX.SEC.aac.enc"; 
	//.
	private MediaCodec Codec;
	//.
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	private byte[] outData;
	//.
	protected OutputStream MyOutputStream;
 
	public AACEncoder(int BitRate, int SampleRate, OutputStream pOutputStream) {
		MyOutputStream = pOutputStream; 
		//.
		Codec = MediaCodec.createByCodecName(CodecName);
		//.
		MediaFormat format = MediaFormat.createAudioFormat(CodecTypeName, SampleRate, 1);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BitRate);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		Codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		Codec.start();
		//.
		inputBuffers = Codec.getInputBuffers();
		outputBuffers = Codec.getOutputBuffers();
		outData = new byte[0];
	}
 
	public void Destroy() throws IOException {
		if (Codec != null) {
			Codec.stop();
			Codec.release();
			Codec = null;
		}
	}
 
	public void EncodeInputBuffer(byte[] input, int input_size) throws IOException {
		int inputBufferIndex = Codec.dequeueInputBuffer(-1);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(input, 0,input_size);
			Codec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
		}
		//.
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, 0);
		while (outputBufferIndex >= 0) {
			ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
			if (outData.length < bufferInfo.size)
				outData = new byte[bufferInfo.size];
			outputBuffer.rewind(); //. reset position to 0
			outputBuffer.get(outData, 0,bufferInfo.size);
			//. process output
			DoOnOutputBuffer(outData,bufferInfo.size);
			//.
			Codec.releaseOutputBuffer(outputBufferIndex, false);
			outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, 0);
		}
		if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
		     outputBuffers = Codec.getOutputBuffers();
		else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
		     // Subsequent data will conform to new format.
		     ///? MediaFormat format = codec.getOutputFormat();
		}
	}
	
	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize) throws IOException {
	}
}
