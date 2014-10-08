package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";

	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 		= 1;
	public static final short TemperatureTag	= 2;
	public static final short PressureTag		= 3;
	public static final short HumidityTag		= 4;
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller) throws IOException {
		TStreamChannel.TPacketSubscriber PacketSubscriber  = new TStreamChannel.TPacketSubscriber() {
    		@Override
    		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
    			try {
        			pOutputStream.write(Packet, 0,PacketSize);		
    			}
    			catch (Exception E) {
    				Canceller.Cancel();
    			}
    		}
    	};
    	PacketSubscribers.Subscribe(PacketSubscriber);
    	try {
    		try {
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

	private byte[] Timestamp_ToByteArray(double Timestamp) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(TimestampTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Temperature_ToByteArray(double Temperature) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(TemperatureTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Temperature);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Pressure_ToByteArray(double Pressure) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(PressureTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Pressure);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Humidity_ToByteArray(double Humidity) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(HumidityTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Humidity);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public void DoOnTimestamp(double Timestamp) throws IOException {
		byte[] BA = Timestamp_ToByteArray(Timestamp);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnTemperature(double Temperature) throws IOException {
		byte[] BA = Temperature_ToByteArray(Temperature);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnPressure(double Pressure) throws IOException {
		byte[] BA = Pressure_ToByteArray(Pressure);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnHumidity(double Humidity) throws IOException {
		byte[] BA = Humidity_ToByteArray(Humidity);
		PacketSubscribers.DoOnPacket(BA);
	}
}
