package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;

public class TGeoSpaceData extends TComponentData {

    public String 	Name;
    public int		Datum;
    public int 		Projection;
    //.
    public int[]	GeoCrdSystemList;
    public int[]	MapFormatMapList;

    @Override
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = super.FromByteArrayV1(BA, Idx);
		//.
    	byte SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		Name = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		Name = "";
		Datum = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
		Projection = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
    	int Count = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	GeoCrdSystemList = new int[Count];
		for (int I = 0; I < Count; I++) { 
			GeoCrdSystemList[I] = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
		}
    	Count = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	MapFormatMapList = new int[Count];
		for (int I = 0; I < Count; I++) { 
			MapFormatMapList[I] = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
		}
		//.
	    return Idx;
	}

	public byte[] ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			BOS.write(super.ToByteArrayV1());
			//.
			byte[] BA_SL = new byte[1];
			BA_SL[0] = (byte)Name.length();
			BOS.write(BA_SL);
			if (BA_SL[0] > 0)
				BOS.write(Name.getBytes("windows-1251"));
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(Datum);
			BOS.write(BA);
			BA = TDataConverter.ConvertInt32ToBEByteArray(Projection);
			BOS.write(BA);
			int Count = 0;
			if (GeoCrdSystemList != null)
				Count = GeoCrdSystemList.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Count);
			BOS.write(BA);
			byte[] BA_32TO64 = new byte[4];
			for (int I = 0; I < Count; I++) {
				BA = TDataConverter.ConvertInt32ToBEByteArray(GeoCrdSystemList[I]);
				BOS.write(BA);
				BOS.write(BA_32TO64);
			}
			Count = 0;
			if (MapFormatMapList != null)
				Count = MapFormatMapList.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Count);
			BOS.write(BA);
			for (int I = 0; I < Count; I++) {
				BA = TDataConverter.ConvertInt32ToBEByteArray(MapFormatMapList[I]);
				BOS.write(BA);
				BOS.write(BA_32TO64);
			}
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
	
	public int GetGeoCrdSystemByXY(double X, double Y) {
		if ((GeoCrdSystemList != null) && (GeoCrdSystemList.length > 0)) {
			return GeoCrdSystemList[0]; //. -> 
		}
		else
			return 0; //. ->
	}
	
	public int GetGeoCrdSystemByLatLong(double Latitude, double Longitude) {
		if ((GeoCrdSystemList != null) && (GeoCrdSystemList.length > 0)) {
			return GeoCrdSystemList[0]; //. -> 
		}
		else
			return 0; //. ->
	}
}
