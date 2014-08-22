package com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TGeodesyPointStruct {

	public static final int Size = 32;
	
    public double X;
    public double Y;
    //.
    public double Latitude;
    public double Longitude;
    
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
	    X = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Y = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Latitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Longitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; 
		//.
	    return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(X);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToLEByteArray(Y);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToLEByteArray(Latitude);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToLEByteArray(Longitude);
			BOS.write(BA);
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
}
