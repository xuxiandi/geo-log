package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TLUMChannel extends TStreamChannel {

	public static final String TypeID = "UserMessaging.LUM";

	public static final int DescriptorSize = 4;
	
	public TLUMChannel(TSensorsModule pSensorsModule) {
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
        			//.
        			//. Log.i("PacketSending", "packet sent, size: "+Integer.toString(PacketSize));
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

	protected byte[] DataType_ToByteArray(TDataType DataType) throws IOException {
		int DataSize = 2/*SizeOf(ID)*/+DataType.ContainerType.ByteArraySize();
		byte[] Result = new byte[DescriptorSize+DataSize];
		int Idx = 0;
		TDataConverter.ConvertInt32ToLEByteArray(DataSize, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(DataType.ID, Result, Idx); Idx += 2; //. SizeOf(DataType.ID)
		byte[] BA = DataType.ContainerType.ToByteArray();
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
