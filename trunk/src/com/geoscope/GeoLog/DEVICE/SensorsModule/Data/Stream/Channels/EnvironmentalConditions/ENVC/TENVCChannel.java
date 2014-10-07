package com.geoscope.GeoLog.DEVICE.SensorsModule.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TENVCChannel extends TChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";
	
	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 		= 1;
	public static final short TemperatureTag	= 2;
	public static final short PressureTag		= 3;
	public static final short HumidityTag		= 4;
	
	public byte[] Timestamp_ToByteArray(double Timestamp) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(TimestampTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public byte[] Temperature_ToByteArray(double Temperature) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(TemperatureTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Temperature);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public byte[] Pressure_ToByteArray(double Pressure) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(PressureTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Pressure);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public byte[] Humidity_ToByteArray(double Humidity) throws IOException {
		byte[] Result = new byte[2/*SizeOf(SizeDescriptor)*/+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx +=2;
		TDataConverter.ConvertInt16ToLEByteArray(HumidityTag, Result, Idx); Idx +=2;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Humidity);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
