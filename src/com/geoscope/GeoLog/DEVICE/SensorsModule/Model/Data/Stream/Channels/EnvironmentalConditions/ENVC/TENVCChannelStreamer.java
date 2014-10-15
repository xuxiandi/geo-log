package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;

public class TENVCChannelStreamer extends TComponentDataStreaming.TStreamer {

	private class TProcessing extends TCancelableThread {
		
		public TProcessing() {
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
	
	private TENVCChannel Channel;
	//.
	private TDEVICEModule Device;
	//.
	private TProcessing Processing = null;
	//.
	private TDEVICEModule.TComponentDataStreamingAbstract DataStreaming = null;
	
	public TENVCChannelStreamer(TENVCChannel pChannel, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) {
		super(pidTComponent,pidComponent, pChannelID, pConfiguration, pParameters, TENVCChannel.DescriptorSize, 1024);
		//.
		Channel = pChannel;
		Device = Channel.SensorsModule.Device;
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
