package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Video;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Base64;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor.TChannel.TConfigurationParcer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor;

public class TVideoChannelProcessor extends TStreamChannelProcessor {

	public static TStreamChannelProcessor GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
		if (pTypeID.equals(TMediaFrameServerVideoH264Client.TypeID)) 
			return (new TVideoChannelProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters)); //. ->
		else 
			return null; //. ->
	}
	
	public static abstract class TMediaFrameServerVideoClient {
	    
		protected TStreamChannelProcessor Processor;
		//.
		protected byte[] 	Buffer = new byte[65535];
		protected int 		BufferSize = 0;
		protected short		BufferIndex = 0;
		//.
		protected String ExceptionMessage;
		//.
		protected Surface 	TheSurface;
		
		public TMediaFrameServerVideoClient(TStreamChannelProcessor pProcessor) {
			Processor = pProcessor;
		}
		
		public abstract void Open() throws Exception;
		public abstract void Close();
		public abstract void DoOnRead(byte[] ReadBuffer, int ReadSize, TCanceller Canceller);
		public abstract void DoOnException(Exception E);
	}
	
	public static class TMediaFrameServerVideoH264Client extends TMediaFrameServerVideoClient {
		
		public static final String TypeID = "Video.H264";
		//.
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 10000; //. milliseconds

		
		private boolean BufferSize_flRead = false;
		//.
		private boolean flConfigIsProcessed;
		
		private MediaCodec 			Codec;
		private ByteBuffer[] 		inputBuffers;
		@SuppressWarnings("unused")
		private ByteBuffer[] 		outputBuffers;
		
		public TMediaFrameServerVideoH264Client(TStreamChannelProcessor pProcessor) throws IOException {
			super(pProcessor);
		}

		@Override
		public void Open() throws Exception {
			Codec = MediaCodec.createDecoderByType(CodecTypeName);
			//.
			MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, 0,0);
			Codec.configure(format, TheSurface, null, 0);
			Codec.start();
			//.
			inputBuffers = Codec.getInputBuffers();
			outputBuffers = Codec.getOutputBuffers();
    		//.
    		flConfigIsProcessed = false;
		}
			    		
		@Override
		public void Close() {
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
						BufferSize = TDataConverter.ConvertLEByteArrayToInt32(ReadBuffer, ReadBuffer_Index);
		    			ReadBuffer_Index += 4; //. SizeOf(Integer)
		    			ReadSize -= 4; //. SizeOf(Integer)
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
								DecodeInputBuffer(((TVideoChannelProcessor)Processor).ConfigurationBuffer,((TVideoChannelProcessor)Processor).ConfigurationBuffer.length);
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

		@Override
		public void DoOnException(Exception E) {
			Processor.DoOnStreamChannelException(E);
		}
	}

	private byte[] ConfigurationBuffer;
	//.
	public TMediaFrameServerVideoClient VideoClient = null;
	
	public TVideoChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters);
		if (TypeID.equals(TMediaFrameServerVideoH264Client.TypeID)) 
			VideoClient = new TMediaFrameServerVideoH264Client(this);
		else
			VideoClient = null;
	}	
	
	@Override
	public void Destroy() {
		super.Destroy();
		//.
		if (VideoClient != null) {
			VideoClient.Close();
			VideoClient = null;
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
		VideoClient.Open();
    }
        
	@Override
    public void Close() {
		VideoClient.Close();
    }
        
	@Override
    public boolean IsVisual() {
    	return true;
    }

	@Override
	public void VisualSurface_Set(SurfaceHolder SH, int Width, int Height) {
		VideoClient.TheSurface = SH.getSurface();
	}
	
	@Override
	public void VisualSurface_Clear(SurfaceHolder SH) {
	}
	
	@Override
    public void DoOnStreamChannelRead(byte[] Buffer, int BufferSize, TCanceller Canceller) {
    	VideoClient.DoOnRead(Buffer,BufferSize, Canceller);
    }	
}
