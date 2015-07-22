package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Video.H264I;

import java.io.IOException;

import android.util.Base64;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessors.Video.H264I.TH264IChannelProcessor;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	public static final int DescriptorSize = 4;
	
	public static final int TagSize = 2;
	
	public static final short ChannelStreamSessionTag 	= 0;
	public static final short DataTag 					= 1;
	public static final short IndexTag 					= 2;
	public static final short TimestampTag 				= 3;
	
	private static int MaxUnderlyingStreamSize = 1024*1024*10;
	
	public static class TDoOnH264FramesHandler {
		
		public void DoOnH264Packet(byte[] Packet, int PacketOffset, int PacketSize) throws Exception {
		}
	}	
	
	
	public byte[] ConfigurationBuffer;
	//.
	private TDoOnH264FramesHandler OnH264FramesHandler = null;
	//.
	private byte[] 	Buffer = new byte[65535];
	private int 	BufferSize = 0;
	//.
	private boolean flConfigIsProcessed = false;
	
	public TH264IChannel() throws IOException {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Parse() throws Exception {
		super.Parse();
		//.
		TConfigurationParser CP = new TConfigurationParser(Configuration);
		int Version = Integer.parseInt(CP.DecoderConfiguration[0]);
		if (Version != 1)
			throw new Exception("unknown configuration version"); //. =>
		ConfigurationBuffer = Base64.decode(CP.DecoderConfiguration[1], Base64.NO_WRAP);		
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
	}
	
	
	@Override
	public void Stop() throws Exception {
		super.Stop();
	}
	
	@Override
    public void DoOnRead(TStream Stream, int ReadSize, TOnProgressHandler OnProgressHandler, TOnIdleHandler OnIdleHandler, TOnExceptionHandler OnExceptionHandler, TCanceller Canceller) {
    	try {
    		try {
    			int SP;
    			byte[] BufferSizeBA = new byte[4];
    			while (true) {
    		    	  SP = (int)(Stream.Size-Stream.Position);
    		    	  if (!BufferSize_flRead) {
    		  	    	    if (SP >= BufferSizeBA.length) {
    			  	    	      Stream.Read(BufferSizeBA,BufferSizeBA.length);
    			  	    	      BufferSize = TDataConverter.ConvertLEByteArrayToInt32(BufferSizeBA,0);
    			  	    	      BufferSize_flRead = true;
    			  	    	      SP -= BufferSizeBA.length;
    		  	    	    }
    		  	    	    else 
    		  	    	    	return; //. ->
    		    	  }
    	  	    	  if (SP >= BufferSize) {
    	  	    		  BufferSize_flRead = false;
    	  	    		  if (BufferSize > 0) {
    	  	    			  if (BufferSize > Buffer.length)
    			    				Buffer = new byte[BufferSize];
    	  	  	    	      Stream.Read(Buffer,BufferSize);
    	  	  	    	      //. processing
    	  	  	    	      try {
    	  	  	    	    	  ParseFromByteArrayAndProcess(Buffer, 0, BufferSize);
    	  	  	    	    	  //.
    	  	  	    	    	  OnProgressHandler.DoOnProgress(BufferSize, Canceller);
    	  	  	    	      } catch (IOException IOE) {
    	  	  	    	    	  if (OnExceptionHandler != null)
    	  	  	    	    		  OnExceptionHandler.DoOnException(IOE);
    	  	  	    	      }
    	  	    		  }
    	  	    	  }
    	  	    	  else
		  	    	    	return; //. ->
    			}
    		}
	    	finally {
		    	if ((Stream.Size > MaxUnderlyingStreamSize) && (Stream.Size == Stream.Position))
		    		Stream.Clear();
		    }
		} catch (Exception E) {
  	    	  if (OnExceptionHandler != null)
    	    		  OnExceptionHandler.DoOnException(E);
		}
    }

	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short Tag = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += TagSize; Size -= TagSize;
		switch (Tag) {
		
		case DataTag:
			synchronized (this) {
				if (OnH264FramesHandler != null) {
					if (!flConfigIsProcessed) {
						flConfigIsProcessed = true;
						//.
						OnH264FramesHandler.DoOnH264Packet(ConfigurationBuffer, 0, ConfigurationBuffer.length);
					}
					OnH264FramesHandler.DoOnH264Packet(BA, Idx, Size);
				}
			}
			break; //. >
		}
		Idx += Size; 
		return Idx;
	}

	@Override
	public TChannelProcessor GetProcessor() throws Exception {
		if (Processor != null)
			Processor.Destroy();
		//.
		Processor = new TH264IChannelProcessor(this);
		//.
		return Processor;
	}
	
	public synchronized void SetOnH264FramesHandler(TDoOnH264FramesHandler pOnH264FramesHandler) {
		OnH264FramesHandler = pOnH264FramesHandler;
	}
}
