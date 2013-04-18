package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
 
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

@SuppressLint({ "NewApi" })
public class H264Encoder {

	private static final String CodecTypeName = "video/avc";
	private static final String CodecName = "OMX.SEC.AVC.Encoder"; //. Sumsung Galaxy S3 specific
	//.
	private MediaCodec Codec;
	//.
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	//.
	private byte[] SPS = null;
	private byte[] PPS;
	//.
	protected OutputStream MyOutputStream;
 
	public H264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream) {
		MyOutputStream = pOutputStream; 
		//.
		if (!Build.MODEL.startsWith("GT")) //. Is this Sumsung Galaxy S3?
			Codec = MediaCodec.createEncoderByType(CodecTypeName);
		else
			Codec = MediaCodec.createByCodecName(CodecName);
		//.
		MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, FrameWidth,FrameHeight);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BitRate);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 75);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
		Codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		Codec.start();
		//.
		inputBuffers = Codec.getInputBuffers();
		outputBuffers = Codec.getOutputBuffers();
	}
 
	public void Destroy() throws IOException {
		Codec.stop();
		Codec.release();
	}
 
	public void EncodeInputBuffer(byte[] input) throws IOException {
		int inputBufferIndex = Codec.dequeueInputBuffer(-1);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(input);
			Codec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
		}
		//.
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, 0);
		while (outputBufferIndex >= 0) {
			ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
			byte[] outData = new byte[bufferInfo.size];
			outputBuffer.rewind(); //. reset position to 0
			outputBuffer.get(outData);
			//. process output
			if (SPS != null) {
				ByteBuffer frameBuffer = ByteBuffer.wrap(outData);
				frameBuffer.putInt(bufferInfo.size-4);
				//.
				DoOnOutputBuffer(outData);
			} 
			else {
				ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
				if (spsPpsBuffer.getInt() == 0x00000001) 
					System.out.println("parsing sps/pps");
				int ppsIndex = 0;
				while(!(spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x01)) {
				}
				ppsIndex = spsPpsBuffer.position();
				SPS = new byte[ppsIndex-8];
				System.arraycopy(outData, 4, SPS, 0, SPS.length);
				PPS = new byte[outData.length-ppsIndex];
				System.arraycopy(outData, ppsIndex, PPS, 0, PPS.length);
				//. 
				DoOnParameters(SPS,PPS);
			}
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
	
	public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
	}
	
	public void DoOnOutputBuffer(byte[] output) throws IOException {
	}
}
