package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Utils.TDataConverter;

public class TContainerCoord {

	public static final int ByteArraySize = 4*8;
	
	public double Xmin;
	public double Ymin;
	public double Xmax;
	public double Ymax;

	public boolean Equals(TContainerCoord ContainerCoord) {
		return ((Xmin == ContainerCoord.Xmin) && (Ymin == ContainerCoord.Ymin) && (Xmax == ContainerCoord.Xmax) && (Ymax == ContainerCoord.Ymax));
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(Xmin);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Ymin);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Xmax);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Ymax);
		System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
	
	public int ToByteArray(byte[] ToBA, int Idx) throws IOException {
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(Xmin);
		System.arraycopy(BA,0, ToBA,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Ymin);
		System.arraycopy(BA,0, ToBA,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Xmax);
		System.arraycopy(BA,0, ToBA,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToBEByteArray(Ymax);
		System.arraycopy(BA,0, ToBA,Idx, BA.length); Idx += BA.length;
		return Idx;
	}
	
	public boolean IsObjectOutside(TContainerCoord Obj_ContainerCoord)
	{
		return (((Obj_ContainerCoord.Xmax < Xmin) || (Obj_ContainerCoord.Xmin > Xmax) || (Obj_ContainerCoord.Ymax < Ymin) || (Obj_ContainerCoord.Ymin > Ymax)));
	}
}
