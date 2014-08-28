package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TComponentData {

	public double 	Timestamp = 0.0;
	public long 	ID = 0;
	
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
    	Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
    	ID = TDataConverter.ConvertLEByteArrayToInt64(BA, Idx); Idx += 8; //. SizeOf(Int64)
		return Idx;
	}

	public byte[] ToByteArrayV1() throws IOException {
		byte[] Result = new byte[8/*SizeOf(ID)*/+8/*SizeOf(Timestamp)*/];
		//.
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8;
		BA = TDataConverter.ConvertInt64ToLEByteArray(ID);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8; //. Int64
		//.
		return Result;
	}
}
