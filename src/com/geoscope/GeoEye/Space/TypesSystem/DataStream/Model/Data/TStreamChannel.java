package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessor.TChannelProcessor;

public class TStreamChannel extends TChannel {

    public static abstract class TOnProgressHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnProgressHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnProgress(int ReadSize, TCanceller Canceller);
    }
    
    public static abstract class TOnIdleHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnIdleHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnIdle(TCanceller Canceller);
    }
    
    public static abstract class TOnExceptionHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnExceptionHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnException(Exception E);
    }
    
	public static class TDoOnDataHandler {
		
		public void DoOnData(TDataType DataType) {
		}
	}
	

	protected boolean 	BufferSize_flRead = false;
	
	public TStreamChannel() {
		super();
	}
	
	@Override
	public void Close() throws Exception {
		FreeProcessor();
		//.
		super.Close();
	}
	
    public void DoOnRead(TStream Stream, int ReadSize, TOnProgressHandler OnProgressHandler, TOnIdleHandler OnIdleHandler, TOnExceptionHandler OnExceptionHandler, TCanceller Canceller) {
	}

	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		return Idx;
	}
    
	protected TChannelProcessor Processor = null;
	
	public TChannelProcessor GetProcessor() throws Exception {
		return Processor;
	}

	public void FreeProcessor() throws Exception {
		if (Processor != null) {
			Processor.Destroy();
			Processor = null;
		}
	}
}
