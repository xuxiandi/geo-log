package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint({ "NewApi" })
public class H264Encoder {

	public static boolean IsSupported() {
		return (android.os.Build.VERSION.SDK_INT >= 16); 
	}
	
	private static final String CodecTypeName = "video/avc";
	@SuppressWarnings("unused")
	private static final String CodecName = "OMX.SEC.avc.enc"; //. Samsung Galaxy S3 specific
	private static final int	CodecLatency = 1000; //. microseconds

	private class TOutputProcessing extends TCancelableThread {
		
		private IOException _Exception = null;
		
		public TOutputProcessing() {
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() {
			CancelAndWait();
		}
		
		@Override
		public void run()  {
			try {
				_Thread.setPriority(Thread.MAX_PRIORITY);
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
						DoOnOutputBuffer(OutData,bufferInfo.size,bufferInfo.presentationTimeUs);
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
	
	private MediaCodec Codec;
	//.
	private ByteBuffer[] InputBuffers;
	//.
	private TOutputProcessing 	OutputProcessing = null;
	private ByteBuffer[] 		OutputBuffers;
	private byte[] 				OutData;
	//.
	@SuppressWarnings("unused")
	private byte[] SPS = null;
	@SuppressWarnings("unused")
	private byte[] PPS;
	//.
	public H264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate) {
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
		InputBuffers = Codec.getInputBuffers();
		OutputBuffers = Codec.getOutputBuffers();
		OutData = new byte[0];
		OutputProcessing = new TOutputProcessing();
	}
 
	public void Destroy() throws IOException {
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
		int inputBufferIndex = Codec.dequeueInputBuffer(-1);
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
	
	public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
	}
	
	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
	}
}
