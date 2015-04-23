package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;

public class TComponentTypedDataFiles {
	
	public Context context;
	//.
	public int 		DataModel;
	public int 		DataType = SpaceDefines.TYPEDDATAFILE_TYPE_AllName;
	public String 	DataParams = null;
	//.
	public TComponentTypedDataFile[] Items = new TComponentTypedDataFile[0];
	
	public TComponentTypedDataFiles(Context pcontext, int pDataModel) {
		context = pcontext;
		DataModel = pDataModel;
	}
	
	public TComponentTypedDataFiles(Context pcontext, int pDataModel, int pDataType) {
		context = pcontext;
		DataModel = pDataModel;
		DataType = pDataType;
	}

	public int Count() {
		return Items.length;
	}

	public TComponentTypedDataFile GetRootItem() {
		if (Items.length == 0)
			return null; //. ->
		return Items[0];
	}
	
	public boolean DataIsNull() {
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].DataIsNull())
				return true; //. ->
		return false;
	}
	
	public boolean DataActualityIsExpired() {
		return DataIsNull();
	}
	
	public void FromByteArrayV0(byte[] BA, int Index) throws IOException {
		int Idx = Index;
		short ItemsCount = TDataConverter.ConvertLEByteArrayToInt16(BA,Idx); Idx += 2;
		Items = new TComponentTypedDataFile[ItemsCount];
		for (int I = 0; I < ItemsCount; I++) {
			Items[I] = new TComponentTypedDataFile(this);
			Idx = Items[I].FromByteArrayV0(BA, Idx);
		}
	}

	public void FromByteArrayV0(byte[] BA) throws IOException {
		FromByteArrayV0(BA,0);
	}
	
	public byte[] ToByteArrayV0() throws IOException {
		short ItemsCount = (short)Items.length;
		//.
		ByteArrayOutputStream Result = new ByteArrayOutputStream();
		try {
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(ItemsCount);
			Result.write(BA);
			for (int I = 0; I < ItemsCount; I++) 
				Result.write(Items[I].ToByteArrayV0());
			//.
	    	return Result.toByteArray(); //. ->
		}
		finally {
			Result.close();
		}
	}	
	
	public void PrepareAsNames() {
		int Cnt = Items.length;
		for (int I = 0; I < Cnt; I++) 
			Items[I].PrepareAsName();
	}
	
	public void PrepareForComponent(int idTComponent, long idComponent, String pDataParams, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		DataParams = pDataParams;
		//.
		int Version = 0;
		TComponentFunctionality CF = Server.User.Space.TypesSystem.TComponentFunctionality_Create(Server, idTComponent,idComponent);
		if (CF == null)
			return; //. ->
		try {
			if (!flWithComponents) {
				byte[] DataDocument = CF.Context_GetDataDocument(DataModel, DataType, DataParams, flWithComponents, Version);
				if (DataDocument != null) {
					FromByteArrayV0(DataDocument);
					if (DataActualityIsExpired()) 
						DataDocument = null;
				}
				if (DataDocument == null) {
					DataDocument = CF.Server_GetDataDocument(DataModel, DataType, DataParams, flWithComponents, Version);
					if (DataDocument != null)
						FromByteArrayV0(DataDocument);
				}
				return; //. ->
			}
			else {
				byte[] DataDocument = CF.Server_GetDataDocument(DataModel, DataType, DataParams, flWithComponents, Version);
				if (DataDocument != null)
					FromByteArrayV0(DataDocument);
			}
		}
		finally {
			CF.Release();
		}
	}

	public void PrepareForComponent(int idTComponent, long idComponent, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		PrepareForComponent(idTComponent,idComponent, null, flWithComponents, Server);	
	}
	
	public void RemoveItem(TComponentTypedDataFile Item) {
		int Cnt = Items.length;
		ArrayList<TComponentTypedDataFile> _Items = new ArrayList<TComponentTypedDataFile>(Cnt-1);
		for (int I = 0; I < Cnt; I++)
			if (Items[I] != Item)
				_Items.add(Items[I]);
		Cnt = _Items.size();
		Items = new TComponentTypedDataFile[Cnt];
		for (int I = 0; I < Cnt; I++)
			Items[I] = _Items.get(I); 
	}
}
