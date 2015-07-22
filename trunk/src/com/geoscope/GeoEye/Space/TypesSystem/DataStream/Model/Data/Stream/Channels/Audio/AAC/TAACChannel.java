package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Audio.AAC;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessors.Audio.AAC.TAACChannelProcessor;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC.TAACADTSDecoder;

public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";

	public static final int DescriptorSize = 2;
	//.
	private static final int DefaultSampleRate = 16000;
	//.
	private static int MaxUnderlyingStreamSize = 1024*100;
	
	public static class TDoOnAACSamplesHandler {
		
		public void DoOnConfiguration(int SampleRate, int ChannelCount) throws IOException {
		}	

		public void DoOnSamplesPacket(byte[] Packet, int PacketSize) {
		}
	}	
	
	
	private short	BufferSize = 0;
	private byte[] 	Buffer = new byte[1024];
	//.
	private TAACADTSDecoder AACADTSDecoder;
	//.
	public volatile TDoOnAACSamplesHandler OnSamplesHandler = null;
	
	public TAACChannel() throws IOException {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		AACADTSDecoder = new TAACADTSDecoder(DefaultSampleRate) {
			
			@Override
			public void DoOnConfiguration(int SampleRate, int ChannelCount) throws IOException {
				if (OnSamplesHandler != null)
					OnSamplesHandler.DoOnConfiguration(SampleRate, ChannelCount);
			}
			
			@Override
			public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
				if (OnSamplesHandler != null)
					OnSamplesHandler.DoOnSamplesPacket(input, input_size);
				
			}
		};
	}
	
	
	@Override
	public void Stop() throws Exception {
		if (AACADTSDecoder != null) {
			AACADTSDecoder.Destroy();
			AACADTSDecoder = null;
		}
		//.
		super.Stop();
	}
	
	@Override
    public void DoOnRead(TStream Stream, int ReadSize, TOnProgressHandler OnProgressHandler, TOnIdleHandler OnIdleHandler, TOnExceptionHandler OnExceptionHandler, TCanceller Canceller) {
    	try {
    		try {
    			int SP;
    			byte[] BufferSizeBA = new byte[2];
    			while (true) {
    		    	  SP = (int)(Stream.Size-Stream.Position);
    		    	  if (!BufferSize_flRead) {
    		  	    	    if (SP >= BufferSizeBA.length) {
    			  	    	      Stream.Read(BufferSizeBA,BufferSizeBA.length);
    			  	    	      BufferSize = TDataConverter.ConvertLEByteArrayToInt16(BufferSizeBA,0);
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
		AACADTSDecoder.DoOnAACADTSPAcket(BA, Idx, Size); Idx += Size; 
		return Idx;
	}

	@Override
	public TChannelProcessor GetProcessor() throws Exception {
		if (Processor != null)
			Processor.Destroy();
		//.
		Processor = new TAACChannelProcessor(this);
		//.
		return Processor;
	}
}
