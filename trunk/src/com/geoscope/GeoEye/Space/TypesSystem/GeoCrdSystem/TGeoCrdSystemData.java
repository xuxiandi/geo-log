package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint.TGeodesyPointStruct;

public class TGeoCrdSystemData extends TComponentData {

    public int idTOwner;
    public int idOwner;
    
    public int GeoSpaceID;
    public String Name;
    public String Datum;
    public String Projection;
    public byte[] ProjectionDATA;
    public TGeodesyPointStruct[] GeodesyPoints;
    
    public int Bounds_idTVisualization;
    public int Bounds_idVisualization;
    //.
    public byte[] Bounds;

    @Override
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = super.FromByteArrayV1(BA, Idx);
		//.
	    idTOwner = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
	    idOwner = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
	    //.
	    GeoSpaceID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
    	byte SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		Name = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		Name = "";
    	SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		Datum = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		Datum = "";
    	SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		Projection = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		Projection = "";
    	int DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	if (DataSize > 0) {
    		ProjectionDATA = new byte[DataSize];
    		System.arraycopy(BA,Idx, ProjectionDATA,0, DataSize);
    		Idx += DataSize;
    	}
    	else 
    		ProjectionDATA = null;
    	DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	if (DataSize > 0) {
    		byte[] Data = new byte[DataSize];
    		System.arraycopy(BA,Idx, Data,0, DataSize);
    		Idx += DataSize;
    		//.
    		int Idx1 = 0;
        	int Cnt = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx1); Idx1 += 4;
    		GeodesyPoints = new TGeodesyPointStruct[Cnt];
    		for (int I = 0; I < GeodesyPoints.length; I++) {
    			TGeodesyPointStruct GeodesyPoint = new TGeodesyPointStruct();
    			Idx1 = GeodesyPoint.FromByteArray(Data, Idx1);
    			//.
    			GeodesyPoints[I] = GeodesyPoint; 
    		}
    	}
    	else 
    		GeodesyPoints = null;
	    //.
	    Bounds_idTVisualization = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
	    Bounds_idVisualization = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
	    //.
    	DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	if (DataSize > 0) {
    		Bounds = new byte[DataSize];
    		System.arraycopy(BA,Idx, Bounds,0, DataSize);
    		Idx += DataSize;
    	}
    	else 
    		Bounds = null;
		//.
	    return Idx;
	}

	public byte[] ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			BOS.write(super.ToByteArrayV1());
			//.
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray((short)idTOwner);
			BOS.write(BA);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idOwner);
			byte[] BA_32TO64 = new byte[4];
			BOS.write(BA);
			BOS.write(BA_32TO64);
		    //.
			BA = TDataConverter.ConvertInt32ToBEByteArray(GeoSpaceID);
			BOS.write(BA);
			BOS.write(BA_32TO64);
			byte[] BA_SL = new byte[1];
			BA_SL[0] = (byte)Name.length();
			BOS.write(BA_SL);
			if (BA_SL[0] > 0)
				BOS.write(Name.getBytes("windows-1251"));
			BA_SL[0] = (byte)Datum.length();
			BOS.write(BA_SL);
			if (BA_SL[0] > 0)
				BOS.write(Datum.getBytes("windows-1251"));
			BA_SL[0] = (byte)Projection.length();
			BOS.write(BA_SL);
			if (BA_SL[0] > 0)
				BOS.write(Projection.getBytes("windows-1251"));
			int DataSize = 0;
			if (ProjectionDATA != null)
				DataSize = ProjectionDATA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
			BOS.write(BA);
			if (DataSize > 0) 
				BOS.write(ProjectionDATA);
			DataSize = 0;
			if (GeodesyPoints != null)
				DataSize = 4/*SizeOf(PointsCount)*/+GeodesyPoints.length*TGeodesyPointStruct.Size;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
			BOS.write(BA);
			if (DataSize > 0) {
				BA = TDataConverter.ConvertInt32ToBEByteArray(GeodesyPoints.length);
				BOS.write(BA);
				for (int I = 0; I < GeodesyPoints.length; I++)
					BOS.write(GeodesyPoints[I].ToByteArray());
			}
		    //.
			BA = TDataConverter.ConvertInt16ToBEByteArray((short)Bounds_idTVisualization);
			BOS.write(BA);
			BA = TDataConverter.ConvertInt32ToBEByteArray(Bounds_idVisualization);
			BOS.write(BA);
			BOS.write(BA_32TO64);
		    //.
			DataSize = 0;
			if (Bounds != null)
				DataSize = Bounds.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
			BOS.write(BA);
			if (DataSize > 0) 
				BOS.write(Bounds);
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
}
