package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessor.TChannelProcessor;

public class TStreamChannel extends TChannel {

    public static abstract class TOnProgressHandler {
    	
    	public abstract void DoOnProgress(int ReadSize, TCanceller Canceller);
    }
    
    public static abstract class TOnIdleHandler {
    	
    	public abstract void DoOnIdle(TCanceller Canceller) throws Exception;
    }
    
	public static class TDoOnDataHandler {
		
		public void DoOnData(TDataType DataType) {
		}
	}
	

	public TStreamChannel() {
		super();
	}
	
	@Override
	public void Close() throws Exception {
		FreeProcessor();
		//.
		super.Close();
	}
	
	public void DoStreaming(Socket Connection, InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
	}	

	public void DoStreaming(Socket Connection, InputStream pInputStream, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		DoStreaming(Connection, pInputStream, -1, pOutputStream, OnProgressHandler, StreamingTimeout, IdleTimeoutCounter, OnIdleHandler, Canceller);
	}	

	public void DoStreaming(InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		DoStreaming(null, pInputStream, pInputStreamSize, pOutputStream, OnProgressHandler, StreamingTimeout, IdleTimeoutCounter, OnIdleHandler, Canceller);
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
