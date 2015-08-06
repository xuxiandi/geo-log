package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	private static final int SubscriberBufferSize = 25;
	
	public static final int DescriptorSize = 4;
	
	public static final int TagSize = 2;
	
	public static final short ChannelStreamSessionTag 	= 0;
	public static final short DataTag 					= 1;
	public static final short IndexTag 					= 2;
	public static final short TimestampTag 				= 3;
	
	
	public TH264IChannel(TSensorsModule pSensorsModule) {
		super(pSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(String UserAccessKey, final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws Exception {
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
		TStreamChannel.TPacketSubscriber PacketSubscriber  = new TStreamChannel.TPacketSubscriber(UserAccessKey, SubscriberBufferSize) {
			
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
    	try {
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
    	finally {
    		PacketSubscriber.Destroy();
    	}
	}

	@Override
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return (new TH264IChannelStreamer(this, pidTComponent,pidComponent, pConfiguration,pParameters));
	}
	
	private byte[] Packet = new byte[65535];
	
	public byte[] ChannelStreamSession_ToByteArray(int Session) throws IOException {
		int Descriptor = TagSize+4/*SizeOf(Session)*/;
		int PacketSize = DescriptorSize+Descriptor;
		byte[] Result = new byte[PacketSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Result, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(ChannelStreamSessionTag, Result, Idx);  
		//.
		return Result;
	}
	
	public void DoOnChannelStreamSession(int Session) throws Exception {
		int Descriptor = TagSize+4/*SizeOf(Session)*/;
		int PacketSize = DescriptorSize+Descriptor;
		if (PacketSize > Packet.length) 
			Packet = new byte[PacketSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Packet, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(ChannelStreamSessionTag, Packet, Idx);  
		//.
		EnqueuePacket(Packet, PacketSize);
	}

	public byte[] H264Packet_ToByteArray(byte[] H264Packet, int H264PacketSize) throws IOException {
		int Descriptor = TagSize+H264PacketSize;
		int PacketSize = DescriptorSize+Descriptor;
		byte[] Result = new byte[PacketSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Result, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(DataTag, Result, Idx); Idx += TagSize; 
		System.arraycopy(H264Packet,0, Result,Idx, H264PacketSize);
		//.
		return Result;
	}
	
	public void DoOnH264Packet(byte[] H264Packet, int H264PacketSize) throws Exception {
		int Descriptor = TagSize+H264PacketSize;
		int PacketSize = DescriptorSize+Descriptor;
		if (PacketSize > Packet.length) 
			Packet = new byte[PacketSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Packet, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(DataTag, Packet, Idx); Idx += TagSize; 
		System.arraycopy(H264Packet,0, Packet,Idx, H264PacketSize);
		//.
		EnqueuePacket(Packet, PacketSize);
	}

	public void DoOnH264IndexAndTimestamp(int Index, int Timestamp) throws Exception {
		int Descriptor = TagSize+4/*SizeOf(Index OR Timestamp)*/;
		int PacketSize = ((DescriptorSize+Descriptor) << 1);
		if (PacketSize > Packet.length) 
			Packet = new byte[PacketSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Packet, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(IndexTag, Packet, Idx); Idx += TagSize; 
		TDataConverter.ConvertInt32ToLEByteArray(Index, Packet, Idx); Idx += 4; //. SizeOf(Index) 
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor, Packet, Idx); Idx += DescriptorSize; 
		TDataConverter.ConvertInt16ToLEByteArray(TimestampTag, Packet, Idx); Idx += TagSize; 
		TDataConverter.ConvertInt32ToLEByteArray(Timestamp, Packet, Idx);  
		//.
		EnqueuePacket(Packet, PacketSize);
	}
}
