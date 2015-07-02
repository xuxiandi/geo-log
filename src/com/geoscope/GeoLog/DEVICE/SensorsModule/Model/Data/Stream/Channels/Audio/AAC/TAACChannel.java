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
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws IOException {
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
    		}
    	};
    	PacketSubscribers.Subscribe(PacketSubscriber);
    	try {
    		try {
    			if (MaxDuration > 0)
    				Thread.sleep(MaxDuration);
    			else
    				while (!Canceller.flCancel) 
    					Thread.sleep(100);
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
		int PacketSize = DescriptorSize+AACPacketSize;
		if (PacketSize > Packet.length) 
			Packet = new byte[PacketSize];
		short Descriptor = (short)AACPacketSize;
		TDataConverter.ConvertInt16ToLEByteArray(Descriptor, Packet, 0);
		System.arraycopy(AACPacket,0, Packet,DescriptorSize, AACPacketSize);
		//.
		PacketSubscribers.DoOnPacket(Packet, PacketSize);
	}

	public void DoOnAACPacket(byte[] AACPacket) throws Exception {
		DoOnAACPacket(AACPacket, AACPacket.length);
	}
}
