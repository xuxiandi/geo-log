package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TGeoLocation {

	
	public int Datum;
	//.
	public double Timestamp;
	public double Latitude;
	public double Longitude;
	public double Altitude;

	public TGeoLocation(int pDatum, double pTimestamp, double pLatitude, double pLongitude, double pAltitude) {
		Datum = pDatum;
		//.
		Timestamp = pTimestamp;
		Latitude = pLatitude;
		Longitude = pLongitude;
		Altitude = pAltitude;
	}
	
	public TGeoLocation(int pDatum) {
		this(pDatum,0.0,0.0,0.0,0.0);
	}
	
	public TGeoLocation() {
		this(0,0.0,0.0,0.0,0.0);
	}
	
	public boolean IsTheSame(TGeoLocation A) {
		return ((A.Timestamp == Timestamp) && (A.Latitude == Latitude) && (A.Longitude == Longitude) && (A.Altitude == Altitude));
	}
	
	public TGeoLocation Clone() {
		return (new TGeoLocation(Datum, Timestamp,Latitude,Longitude,Altitude));
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Datum = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
		//.
		Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
		Latitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Longitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    Altitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    //.
	    return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[4+8+8+8+8];
		//.
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(Datum);
		System.arraycopy(BA,0, Result,0, BA.length);
		//.
		BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,4, BA.length);
		BA = TDataConverter.ConvertDoubleToLEByteArray(Latitude);
		System.arraycopy(BA,0, Result,12, BA.length);
		BA = TDataConverter.ConvertDoubleToLEByteArray(Longitude);
		System.arraycopy(BA,0, Result,20, BA.length);
		BA = TDataConverter.ConvertDoubleToLEByteArray(Altitude);
		System.arraycopy(BA,0, Result,28, BA.length);
		//.
		return Result;
	}
}
