package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TRWLevelTileContainer {
	
	public static final int NullValue = Integer.MAX_VALUE;
	//.
	public static final int ByteArraySize = 4+4*4;
	
	public int 			Level;
	public TTileLevel 	TileLevel; 
	//.
	public int Xmn;
	public int Xmx;
	public int Ymn;
	public int Ymx;
	//. container reflection window params
	public int RW_Xmn;
	public int RW_Ymn;
	public double b;
	public double _Width;
	public double diffX1X0;
	public double diffY1Y0;
	public double diffX3X0;
	public double diffY3Y0;
	public double Xc;
	public double Yc;
	public double Rotation;
	
	public TRWLevelTileContainer() {
	}
	
	public TRWLevelTileContainer(TRWLevelTileContainer C) {
		Assign(C);
	}
	
	public void Assign(TRWLevelTileContainer C) {
		if (C == null) {
			SetAsNull();
			return; //. ->
		}
		Level = C.Level;
		TileLevel = C.TileLevel;
		//.
		Xmn = C.Xmn;
		Ymn = C.Ymn;
		Xmx = C.Xmx;
		Ymx = C.Ymx;
		//.
		RW_Xmn = C.RW_Xmn;
		RW_Ymn = C.RW_Ymn;
		b = C.b;
		_Width = C._Width;
		diffX1X0 = C.diffX1X0;
		diffY1Y0 = C.diffY1Y0;
		diffX3X0 = C.diffX3X0;
		diffY3Y0 = C.diffY3Y0;
		Xc = C.Xc;
		Yc = C.Yc;
		Rotation = C.Rotation;
	}
	
	public void AssignContainer(TRWLevelTileContainer C) {
		if (C == null) {
			SetAsNull();
			return; //. ->
		}
		Level = C.Level;
		TileLevel = C.TileLevel;
		//.
		Xmn = C.Xmn;
		Ymn = C.Ymn;
		Xmx = C.Xmx;
		Ymx = C.Ymx;
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(Level);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Xmn);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Xmx);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Ymn);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Ymx);
		System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Level = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Xmn = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Xmx = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Ymn = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Ymx = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		return Idx;
	}
	
	public boolean IsNull() {
		return (((Xmn == NullValue) && (Xmx == Xmn)) && ((Ymn == NullValue) && (Ymx == Ymn)));
	}

	public void SetAsNull() {
		Level = -1;
		TileLevel = null;
		//.
		Xmn = NullValue;
		Xmx = Xmn;
		Ymn = NullValue;
		Ymx = Ymn;
	}
	
	public int ContainerSquare() {
		return ((Xmx-Xmn+1)*(Ymx-Ymn+1));
	}
}
