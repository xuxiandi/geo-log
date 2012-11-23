package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;


public class TElectedPlace {

	public static final double NullTimestamp = Double.MIN_VALUE;
	
	public String 					Name;
	public TReflectionWindowStruc 	RW;
	public double					Timestamp;
	
	public TElectedPlace() {
		Name = "";
		RW = null;
		Timestamp = NullTimestamp;
	}
	
	public TXYCoord PlacePoint() {
		TXYCoord P = new TXYCoord();
		P.X = (RW.X0+RW.X2)/2.0;
		P.Y = (RW.Y0+RW.Y2)/2.0;
		return P;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException
	{
		int SS = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		Name = "";
		if (SS > 0) {
			Name = new String(BA, Idx,SS, "windows-1251"); Idx += SS;
		}
		RW = new TReflectionWindowStruc();
		Idx = RW.FromByteArray(BA,Idx);
		return Idx;
	}	
	
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException
	{
		int SS = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		Name = "";
		if (SS > 0) {
			Name = new String(BA, Idx,SS, "windows-1251"); Idx += SS;
		}
		RW = new TReflectionWindowStruc();
		Idx = RW.FromByteArray(BA,Idx);
		Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx += 8;
		return Idx;
	}	
	
	public byte[] ToByteArray() throws IOException
	{
		int SS = 0;
		if (Name != null)
			SS = Name.length();
		byte[] RWBA = RW.ToByteArray();
		byte[] Result = new byte[4/*SizeOf(Name)*/+SS+RWBA.length];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SS); System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		if (SS > 0) {
			BA = Name.getBytes("windows-1251");
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		}
		System.arraycopy(RWBA,0, Result,Idx, RWBA.length); Idx+=RWBA.length;
		return Result;
	}	

	public byte[] ToByteArrayV1() throws IOException
	{
		int SS = 0;
		if (Name != null)
			SS = Name.length();
		byte[] RWBA = RW.ToByteArray();
		byte[] Result = new byte[4/*SizeOf(Name)*/+SS+RWBA.length+8/*SizeOf(Timestamp)*/];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SS); System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		if (SS > 0) {
			BA = Name.getBytes("windows-1251");
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		}
		System.arraycopy(RWBA,0, Result,Idx, RWBA.length); Idx+=RWBA.length;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp); System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		return Result;
	}	
}
