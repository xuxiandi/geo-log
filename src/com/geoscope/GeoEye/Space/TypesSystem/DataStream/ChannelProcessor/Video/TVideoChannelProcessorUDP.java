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

import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPDecoder;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPPacket;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor.TChannel.TConfigurationParser;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessorUDP;

public class TVideoChannelProcessorUDP extends TStreamChannelProcessorUDP {

	public static TStreamChannelProcessorUDP GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
		if (pTypeID.equals(TMediaFrameServerVideoH264UDPRTPClient.TypeID)) 
			return (new TVideoChannelProcessorUDP(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler)); //. ->
		else 
			return null; //. ->
	}
	
    public static final int DefaultReadingTimeout = 1000; //. ms
    
	public static abstract class TMediaFrameServerVideoClient {
	    
		protected TStreamChannelProcessorUDP Processor;
		//.
		protected byte[] 	Buffer = new byte[65535];
		//.
		protected String ExceptionMessage;
		//.
		protected Surface 	TheSurface;
		protected int 		TheSurface_Width;
		protected int 		TheSurface_Height;
		
		public TMediaFrameServerVideoClient(TStreamChannelProcessorUDP pProcessor) {
			Processor = pProcessor;
		}
		
		public abstract void Open() throws Exception;
		public abstract void Close() throws Exception;
		public abstract void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller);
		public abstract void DoOnException(Exception E);
	}
	
	public static class TMediaFrameServerVideoH264UDPRTPClient extends TMediaFrameServerVideoClient {
		
		public static final String TypeID = "Video.H264UDPRTP";
		//.
		private static final String CodecTypeName = "video/avc";
		private static final int 	CodecLatency = 1000; //. microseconds
		private static final int 	CodecWaitInterval = 1000000; //. microseconds
		//.
		private static int MaxUnderlyingStreamSize = 1024*1024*10;

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
							//. no need for buffer render it on surface ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
							//.
							Codec.releaseOutputBuffer(outputBufferIndex, true);
							outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
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
		
		
		private TRTPDecoder RTPDecoder;
		private TRTPPacket	RTPPacket;
		//.
		private MediaCodec 			Codec;
		private ByteBuffer[] 		InputBuffers;
		@SuppressWarnings("unused")
		private ByteBuffer[] 		OutputBuffers;
		private TOutputProcessing 	OutputProcessing = null;
		//.
		private boolean flConfigIsProcessed;
		
		public TMediaFrameServerVideoH264UDPRTPClient(TStreamChannelProcessorUDP pProcessor) throws IOException {
			super(pProcessor);
		}

		@Override
		public void Open() throws Exception {
			RTPDecoder = new TRTPDecoder() {
				@Override
				public void DoOnOutput(byte[] OutputBuffer, int OutputBufferSize, int RtpTimestamp) throws IOException {
	  	    	      try {
	  	    	    	  if (!flConfigIsProcessed) {
	  	    	    		  flConfigIsProcessed = true;
	  	    	    		  //.
	  	    	    		  DecodeInputBuffer(((TVideoChannelProcessorUDP)Processor).ConfigurationBuffer,((TVideoChannelProcessorUDP)Processor).ConfigurationBuffer.length);
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
			MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, TheSurface_Width,TheSurface_Height);
			Codec.configure(format, TheSurface, null, 0);
			Codec.start();
			//.
			InputBuffers = Codec.getInputBuffers();
			OutputBuffers = Codec.getOutputBuffers();
			OutputProcessing = new TOutputProcessing();
    		//.
    		flConfigIsProcessed = false;
		}
			    		
		@Override
		public void Close() throws Exception {
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
			int inputBufferIndex = Codec.dequeueInputBuffer(CodecWaitInterval);
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
	public TMediaFrameServerVideoClient VideoClient = null;
	
	public TVideoChannelProcessorUDP(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
		//.
		ReadingTimeout = DefaultReadingTimeout;
		//.
		if (TypeID.equals(TMediaFrameServerVideoH264UDPRTPClient.TypeID)) 
			VideoClient = new TMediaFrameServerVideoH264UDPRTPClient(this);
		else
			VideoClient = null;
	}	
	
	@Override
	public void ParseConfiguration() throws Exception {
		TConfigurationParser CP = new TConfigurationParser(Configuration);
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
    public void Close() throws Exception {
		VideoClient.Close();
    }
        
	@Override
    public boolean IsVisual() {
    	return true;
    }

	@Override
	public void VisualSurface_Set(SurfaceHolder SH, int Width, int Height) {
		VideoClient.TheSurface = SH.getSurface();
		VideoClient.TheSurface_Width = Width;
		VideoClient.TheSurface_Height = Height;
	}
	
	@Override
	public void VisualSurface_Clear(SurfaceHolder SH) {
	}
	
	@Override
    public void DoOnStreamChannelRead(TStream Stream, int ReadSize, TCanceller Canceller) {
    	VideoClient.DoOnRead(Stream,ReadSize, Canceller);
    }	
}
