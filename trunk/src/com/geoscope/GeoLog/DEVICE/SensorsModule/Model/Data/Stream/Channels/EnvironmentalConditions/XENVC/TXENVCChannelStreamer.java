package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;

public class TXENVCChannelStreamer extends TComponentDataStreaming.TStreamer {

	private class TProcessing extends TCancelableThread {
		
		public TProcessing() {
    		super();
		}
		
		public void Destroy() throws InterruptedException {
			Stop();
		}
		
    	public void Start() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Stop() throws InterruptedException {
    		CancelAndWait();
    	}
    	
		@Override
		public void run() {
			try {
				Channel.DoStreaming(StreamingBuffer_OutputStream, Canceller);
			}
        	catch (Throwable E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Device.Log.WriteError("Streamer.Processing",S);
        	}
		}
	}
	
	private TXENVCChannel Channel;
	//.
	private TProcessing Processing = null;
	//.
	private TDEVICEModule.TComponentDataStreamingAbstract DataStreaming = null;
	
	public TXENVCChannelStreamer(TXENVCChannel pChannel, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		super(pChannel.SensorsModule.Device, pidTComponent,pidComponent, pChannelID, pConfiguration, pParameters, TXENVCChannel.DescriptorSize, 1024);
		//.
		Channel = pChannel;
		//.
		Processing = new TProcessing();
		DataStreaming = Device.TComponentDataStreaming_Create(this);
	}
	
	@Override
	public void Start() {
		Processing.Start();
		DataStreaming.Start();
	}

	@Override
	public void Stop() throws InterruptedException {
		if (DataStreaming != null) {
			DataStreaming.Destroy();
			DataStreaming = null;
		}
		if (Processing != null) {
			Processing.Destroy();
			Processing = null;
		}
	}

	@Override
	public boolean Streaming_SourceIsActive() {
		return true;
	}
}
