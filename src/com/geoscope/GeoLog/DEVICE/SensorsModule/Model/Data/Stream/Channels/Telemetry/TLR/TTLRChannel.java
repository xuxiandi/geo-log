package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";

	public static final int DescriptorSize = 2;
	
	public TTLRChannel(TSensorsModule pSensorsModule) {
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
		TStreamChannel.TPacketSubscriber PacketSubscriber  = new TStreamChannel.TPacketSubscriber(UserAccessKey) {
			
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
		return (new TTLRChannelStreamer(this, pidTComponent,pidComponent, pConfiguration,pParameters));
	}
	
	protected byte[] DataType_ToByteArray(TDataType DataType) throws IOException {
		short DataSize = (short)(2/*SizeOf(ID)*/+DataType.ContainerType.ByteArraySize());
		byte[] Result = new byte[DescriptorSize+DataSize];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray(DataSize, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(DataType.ID, Result, Idx); Idx += 2; //. SizeOf(DataType.ID)
		byte[] BA = DataType.ContainerType.ToByteArray();
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
