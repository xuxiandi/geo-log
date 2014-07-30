package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;

public class TComponentData {

	public double 	Timestamp = 0.0;
	public int 		ID = 0;
	
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
    	Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
    	ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
		return Idx;
	}

	public byte[] ToByteArrayV1() throws IOException {
		byte[] Result = new byte[8/*SizeOf(ID)*/+8/*SizeOf(Timestamp)*/];
		//.
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8; //. Int64
		//.
		return Result;
	}
}
