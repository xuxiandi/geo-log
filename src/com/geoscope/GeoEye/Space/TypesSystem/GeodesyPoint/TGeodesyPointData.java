package com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;

public class TGeodesyPointData extends TComponentData {

	public TGeodesyPointStruct Parameters = new TGeodesyPointStruct(); 

    @Override
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = super.FromByteArrayV1(BA, Idx);
		//.
		Idx = Parameters.FromByteArray(BA, Idx);
		//.
	    return Idx;
	}

    @Override
	public byte[] ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			BOS.write(super.ToByteArrayV1());
			//.
			BOS.write(Parameters.ToByteArray());
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
}
