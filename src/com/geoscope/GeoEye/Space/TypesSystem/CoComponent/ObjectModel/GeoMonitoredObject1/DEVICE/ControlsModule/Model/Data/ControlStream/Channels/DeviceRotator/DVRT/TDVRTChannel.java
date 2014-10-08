package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TDVRTChannel extends TStreamChannel {

	public static final String TypeID = "DeviceRotator.DVRT";
	
	public static final int DescriptorSize = 4;
	//.
	public static final int LatitudeTag		= 1;
	public static final int LongitudeTag	= 2;
	
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	private byte[] Latitude_ToByteArray(double Latitude) throws IOException {
		byte[] Result = new byte[DescriptorSize+4/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(12/*SizeOf(Data)*/); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = TDataConverter.ConvertInt32ToLEByteArray(LatitudeTag); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Latitude);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}

	private byte[] Longitude_ToByteArray(double Longitude) throws IOException {
		byte[] Result = new byte[DescriptorSize+4/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(12/*SizeOf(Data)*/); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = TDataConverter.ConvertInt32ToLEByteArray(LongitudeTag); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Longitude);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}

	public void DoOnLatitude(double Latitude) throws IOException {
		@SuppressWarnings("unused")
		byte[] BA = Latitude_ToByteArray(Latitude);
		//.
	}

	public void DoOnLongitude(double Longitude) throws IOException {
		@SuppressWarnings("unused")
		byte[] BA = Longitude_ToByteArray(Longitude);
		//.
	}
}
