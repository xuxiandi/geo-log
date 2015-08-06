package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";

	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 		= 1;
	public static final short TemperatureTag	= 2;
	public static final short PressureTag		= 3;
	public static final short HumidityTag		= 4;
	
	public TENVCChannel(TSensorsModule pSensorsModule) {
		super(pSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(String UserAccessKey, final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws Exception {
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
    		}
    	};
    	try {
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
    	finally {
    		PacketSubscriber.Destroy();
    	}
	}

	@Override
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return (new TENVCChannelStreamer(this, pidTComponent,pidComponent, pConfiguration,pParameters));
	}
	
	private byte[] Timestamp_ToByteArray(double Timestamp) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(TimestampTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Temperature_ToByteArray(double Temperature) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(TemperatureTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Temperature);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Pressure_ToByteArray(double Pressure) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(PressureTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Pressure);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Humidity_ToByteArray(double Humidity) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(HumidityTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Humidity);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public void DoOnTimestamp(double Timestamp) throws Exception {
		byte[] BA = Timestamp_ToByteArray(Timestamp);
		PacketSubscribers.EnqueuePacket(BA);
	}

	public void DoOnTemperature(double Temperature) throws Exception {
		byte[] BA = Temperature_ToByteArray(Temperature);
		PacketSubscribers.EnqueuePacket(BA);
	}

	public void DoOnPressure(double Pressure) throws Exception {
		byte[] BA = Pressure_ToByteArray(Pressure);
		PacketSubscribers.EnqueuePacket(BA);
	}

	public void DoOnHumidity(double Humidity) throws Exception {
		byte[] BA = Humidity_ToByteArray(Humidity);
		PacketSubscribers.EnqueuePacket(BA);
	}
}
