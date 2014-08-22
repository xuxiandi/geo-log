package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TGeoCoord {

	
	public int Datum;
	//.
	public double Latitude;
	public double Longitude;
	public double Altitude;

	public TGeoCoord(int pDatum, double pLatitude, double pLongitude, double pAltitude) {
		Datum = pDatum;
		//.
		Latitude = pLatitude;
		Longitude = pLongitude;
		Altitude = pAltitude;
	}
	
	public TGeoCoord(int pDatum) {
		this(pDatum,0.0,0.0,0.0);
	}
	
	public TGeoCoord() {
		this(0,0.0,0.0,0.0);
	}
	
	public boolean IsTheSame(TGeoCoord A) {
		return ((A.Latitude == Latitude) && (A.Longitude == Longitude) && (A.Altitude == Altitude));
	}
	
	public TGeoCoord Clone() {
		return (new TGeoCoord(Datum,Latitude,Longitude,Altitude));
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Datum = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
		//.
		Latitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Longitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    Altitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    //.
	    return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[4+8+8+8];
		//.
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(Datum);
		System.arraycopy(BA,0, Result,0, BA.length);
		//.
		BA = TDataConverter.ConvertDoubleToLEByteArray(Latitude);
		System.arraycopy(BA,0, Result,0, BA.length);
		BA = TDataConverter.ConvertDoubleToLEByteArray(Longitude);
		System.arraycopy(BA,0, Result,8, BA.length);
		BA = TDataConverter.ConvertDoubleToLEByteArray(Altitude);
		System.arraycopy(BA,0, Result,16, BA.length);
		//.
		return Result;
	}
}
