package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;

@SuppressLint({ "NewApi" })
public class H264Encoder {

	private static final String CodecTypeName = "video/avc";
	@SuppressWarnings("unused")
	private static final String CodecName = "OMX.SEC.avc.enc"; //. Samsung Galaxy S3 specific
	private static final int	CodecLatency = 10000; //. milliseconds
	//.
	private MediaCodec Codec;
	//.
	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	private byte[] outData;
	//.
	@SuppressWarnings("unused")
	private byte[] SPS = null;
	@SuppressWarnings("unused")
	private byte[] PPS;
	//.
	protected OutputStream MyOutputStream;
 
	public H264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream) {
		MyOutputStream = pOutputStream; 
		//.
		Codec = MediaCodec.createEncoderByType(CodecTypeName);
		//.
		MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, FrameWidth,FrameHeight);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BitRate);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
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
			Codec.queueInputBuffer(inputBufferIndex, 0, input.length, SystemClock.elapsedRealtime(), 0);
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
			DoOnOutputBuffer(outData,bufferInfo.size);
			/*///? if (SPS != null) {
				ByteBuffer frameBuffer = ByteBuffer.wrap(outData);
				frameBuffer.putInt(bufferInfo.size-4);
				//.
				DoOnOutputBuffer(outData,bufferInfo.size);
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
			}*/
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
	
	public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
	}
	
	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize) throws IOException {
	}
}
