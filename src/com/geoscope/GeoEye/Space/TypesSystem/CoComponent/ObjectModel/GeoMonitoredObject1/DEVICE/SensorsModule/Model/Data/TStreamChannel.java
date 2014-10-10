package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TStreamChannel extends TChannel {

    public static abstract class TOnIdleHandler {
    	
    	public abstract void DoOnIdle(TCanceller Canceller);
    }
    

	public void DoStreaming(Socket Connection, InputStream pInputStream, OutputStream pOutputStream, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws IOException {
	}	

	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		return Idx;
	}
}
