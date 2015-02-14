package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

@SuppressLint({ "NewApi" })
public class TH264Encoder extends TH264EncoderAbstract {

	public static boolean IsSupported() {
		return (android.os.Build.VERSION.SDK_INT >= 16); 
	}
	
	private static final String CodecTypeName = "video/avc";
	@SuppressWarnings("unused")
	private static final String CodecName = "OMX.SEC.avc.enc"; //. Samsung Galaxy S3 specific
	private static final int	CodecLatency = 100000; //. microseconds
	private static final int 	CodecWaitInterval = 10000; //. microseconds
	//.
	private static final int 	Encoding_IFRAMEInterval = 5; //. seconds

	public static class TPixelFormatConvertor {
		
		protected int FrameWidth;
		protected int FrameHeight;
		
	    protected byte[] OutputBuffer = new byte[0];
	    
	    public TPixelFormatConvertor(int pFrameWidth, int pFrameHeight) {
	    	FrameWidth = pFrameWidth;
	    	FrameHeight = pFrameHeight;
	    }
	    
		public byte[] Convert(byte[] InputBuffer, int InputBufferSize) {
			return InputBuffer;
		}
	}
	
	public static class TYV12toI420PixelFormatConvertor extends TPixelFormatConvertor {
		
	    public TYV12toI420PixelFormatConvertor(int pFrameWidth, int pFrameHeight) {
	    	super(pFrameWidth,pFrameHeight);
	    }
	    
		@Override
		public byte[] Convert(byte[] InputBuffer, int InputBufferSize) {
			if (OutputBuffer.length != InputBuffer.length)
				OutputBuffer = new byte[InputBuffer.length];
			int whc = FrameWidth*FrameHeight;
		    for (int i = 0; i < whc; i++)
		        OutputBuffer[i] = InputBuffer[i];
		    int whcd4 = (whc >> 2); 
		    int cnt = whc+whcd4; 
		    for (int i = whc; i < cnt; i++)
		        OutputBuffer[i] = InputBuffer[i+whcd4];
		    int cnt1 = whc+(whc >> 1); 
		    for (int i = cnt; i < cnt1; i++)
		        OutputBuffer[i] = InputBuffer[i-whcd4];
		    return OutputBuffer;
		}		
	}
	
	
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
							DoOnOutputBuffer(OutData,bufferInfo.size, bufferInfo.presentationTimeUs,((bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0));
						OutputBufferCount++;
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
	
	private boolean flParseParameters;
	
	private MediaCodec Codec;
	//.
	private int			 			InputBufferPixelFormat;
	private TPixelFormatConvertor 	InputBufferPixelFormatConvertor;
	private ByteBuffer[] 			InputBuffers;
	//.
	private TOutputProcessing 	OutputProcessing = null;
	private ByteBuffer[] 		OutputBuffers;
	private byte[] 				OutData;
	private int 				OutputBufferCount;
	//.
	@SuppressWarnings("unused")
	private byte[] SPS = null;
	@SuppressWarnings("unused")
	private byte[] PPS;
	
	public TH264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, boolean pflParseParameters) {
		InputBufferPixelFormat = pInputBufferPixelFormat;
		flParseParameters = pflParseParameters;
		//.
		Codec = MediaCodec.createEncoderByType(CodecTypeName);
		//.
		MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, FrameWidth,FrameHeight);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BitRate);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Encoding_IFRAMEInterval);
		//.
		switch (InputBufferPixelFormat) {
		
		case ImageFormat.NV21:
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
			InputBufferPixelFormatConvertor = null;
			break; //. >

		case ImageFormat.YV12:
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
			InputBufferPixelFormatConvertor = new TYV12toI420PixelFormatConvertor(FrameWidth,FrameHeight);
			break; //. >
			
		case ImageFormat.UNKNOWN:
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
			InputBufferPixelFormatConvertor = null;
			break; //. >
		}
		//.
		Codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		Codec.start();
		//.
		InputBuffers = Codec.getInputBuffers();
		OutputBuffers = Codec.getOutputBuffers();
		OutData = new byte[0];
		OutputProcessing = new TOutputProcessing();
	}
 
	public TH264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat) {
		this(FrameWidth,FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat, false);
	}
	
	@Override
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
		if (InputBufferPixelFormatConvertor != null)
			input = InputBufferPixelFormatConvertor.Convert(input, input_size);
		//.
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
	
	public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
	}
}
