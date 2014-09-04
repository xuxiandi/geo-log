package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TComponentDescriptor {

	public int 	idTComponent;
	public long idComponent;
	
	public TComponentDescriptor() {
		idTComponent = 0;
		idComponent = 0;
	}
	
	public TComponentDescriptor(int pidTComponent, long pidComponent) {
		idTComponent = pidTComponent;
		idComponent = pidComponent;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		idTComponent = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		idComponent = TDataConverter.ConvertLEByteArrayToInt64(BA,Idx); Idx += 8;
		return Idx;
	}

	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[4/*SizeOf(idTComponent)*/+8/*SizeOf(idComponent)*/];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(idTComponent); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt64ToLEByteArray(idComponent); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		return Result;
	}	

}
