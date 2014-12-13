package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TStreamChannel extends TChannel {

    public static abstract class TOnProgressHandler {
    	
    	public abstract void DoOnProgress(int ReadSize, TCanceller Canceller);
    }
    
    public static abstract class TOnIdleHandler {
    	
    	public abstract void DoOnIdle(TCanceller Canceller);
    }
    
	public static class TDoOnDataHandler {
		
		public void DoOnData(TDataType DataType) {
		}
	}
	

	public void DoStreaming(Socket Connection, InputStream pInputStream, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
	}	

	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		return Idx;
	}
}
