package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import android.content.Context;

public class TComponentTypedDataFiles {
	
	public Context context;
	public int DataModel;
	public TComponentTypedDataFile Items[];
	
	public TComponentTypedDataFiles(Context pcontext, int pDataModel) {
		context = pcontext;
		DataModel = pDataModel;
	}
	
	public void PrepareFromByteArrayV0(byte[] BA, int Index) throws IOException {
		int Idx = Index;
		short ItemsCount = TDataConverter.ConvertBEByteArrayToInt16(BA,Idx); Idx += 2;
		Items = new TComponentTypedDataFile[ItemsCount];
		for (int I = 0; I < ItemsCount; I++) {
			Items[I] = new TComponentTypedDataFile(this);
			Idx = Items[I].PrepareFromByteArrayV0(BA, Idx);
		}
	}

	public void PrepareFromByteArrayV0(byte[] BA) throws IOException {
		PrepareFromByteArrayV0(BA,0);
	}
}
