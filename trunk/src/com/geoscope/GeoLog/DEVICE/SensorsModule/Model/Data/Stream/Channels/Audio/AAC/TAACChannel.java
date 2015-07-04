package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";

	public static final int DescriptorSize = 2;
	
	public TAACChannel(TSensorsModule pSensorsModule) {
		super(pSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws Exception {
		final long 		FinishTimestamp;
		final Object 	FinishSignal;
		if (MaxDuration > 0) { 
			FinishSignal = new Object();
			FinishTimestamp = System.currentTimeMillis()+MaxDuration;
		}
		else {
			FinishSignal = null;
			FinishTimestamp = 0;
		}
		//.
		TStreamChannel.TPacketSubscriber PacketSubscriber  = new TStreamChannel.TPacketSubscriber() {
			
    		@Override
    		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
    			try {
        			pOutputStream.write(Packet, 0,PacketSize);
        			pOutputStream.flush();
    			}
    			catch (Exception E) {
    				Canceller.Cancel();
    			}
    			//.
    			if ((FinishTimestamp > 0) && (System.currentTimeMillis() > FinishTimestamp))  
    				synchronized (FinishSignal) {
    					FinishSignal.notify();
					}
    		}
    	};
    	PacketSubscribers.Subscribe(PacketSubscriber);
    	try {
    		try {
        		boolean flSuspended = IsSuspended();
        		if (flSuspended)
        			Resume();
    			try {
        			if (FinishTimestamp > 0)
        				synchronized (FinishSignal) {
        					FinishSignal.wait();
    					}
        			else
        				while (!Canceller.flCancel) 
        					Thread.sleep(100);
    			}
    			finally {
            		if (flSuspended)
            			Suspend();
    			}
    		}
    		catch (InterruptedException IE) {
    		}
    	}
    	finally {
    		PacketSubscribers.Unsubscribe(PacketSubscriber);
    	}
	}

	@Override
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return (new TAACChannelStreamer(this, pidTComponent,pidComponent, pChannelID, pConfiguration,pParameters));
	}
	
	private byte[] Packet = new byte[1024];
	
	public void DoOnAACPacket(byte[] AACPacket, int AACPacketSize) throws Exception {
		short Descriptor = (short)AACPacketSize;
		int PacketSize = DescriptorSize+Descriptor;
		if (PacketSize > Packet.length) 
			Packet = new byte[PacketSize];
		TDataConverter.ConvertInt16ToLEByteArray(Descriptor, Packet, 0);
		System.arraycopy(AACPacket,0, Packet,DescriptorSize, AACPacketSize);
		//.
		ProcessPacket(Packet, PacketSize);
	}
}
