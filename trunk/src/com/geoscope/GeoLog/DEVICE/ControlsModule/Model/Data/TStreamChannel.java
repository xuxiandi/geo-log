package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TStreamChannel extends TChannel {

	public com.geoscope.Classes.Data.Stream.Channel.TChannel DestinationChannel = null;
	
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TCanceller Canceller) throws IOException {
	}	
}
