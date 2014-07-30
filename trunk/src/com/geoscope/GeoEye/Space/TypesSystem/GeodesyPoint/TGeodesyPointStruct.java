package com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;

public class TGeodesyPointStruct {

	public static final int Size = 32;
	
    public double X;
    public double Y;
    //.
    public double Latitude;
    public double Longitude;
    
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
	    X = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Y = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Latitude = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
	    Longitude = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
		//.
	    return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(X);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToBEByteArray(Y);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToBEByteArray(Latitude);
			BOS.write(BA);
			BA = TDataConverter.ConvertDoubleToBEByteArray(Longitude);
			BOS.write(BA);
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
}
