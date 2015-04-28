package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TComponentTypedDataFile {

	public TComponentTypedDataFiles TypedDataFiles;
	//.
	public int		DataComponentType;
	public long		DataComponentID;
	public int		DataType;
	public String 	DataParams = null;
	public String 	DataFormat = "";
	public String 	DataName = "";
	public byte[]	Data = null;
	public String 	DataFileName = null;
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles) {
		TypedDataFiles = pTypedDataFiles;
	}
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles, int	pDataType) {
		TypedDataFiles = pTypedDataFiles;
		//.
		DataType = pDataType;
	}
	
	public TComponentTypedDataFile Clone() {
		TComponentTypedDataFile _Clone = new TComponentTypedDataFile(TypedDataFiles);
		//.
		_Clone.DataComponentType = DataComponentType;
		_Clone.DataComponentID = DataComponentID;
		_Clone.DataType = DataType;
		_Clone.DataFormat = DataFormat;
		_Clone.DataName = DataName;
		_Clone.Data = Data;
		_Clone.DataFileName = DataFileName;
		//.
		return _Clone;
	}
	
	public String FileName() {
		return ("DataFile"+Long.toString(DataComponentID)+DataFormat);
	}

	public boolean DataIsNull() {
		return (Data == null);
	}
	
	public boolean DataActualityIsExpired() {
		return DataIsNull();
	}
	
	public boolean IsLoaded() {
		return ((DataFileName != null) | (Data != null));
	}
	
	public boolean IsFSFile() {
		return (DataFileName != null);
	}
	
	public int FromByteArrayV0(byte[] BA, int Idx) throws IOException {
		DataComponentType = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		DataComponentID = TDataConverter.ConvertLEByteArrayToInt64(BA,Idx); Idx += 8; //. native ComponentID is Int64
		DataType = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		byte ItemFormatSize = BA[Idx]; Idx++;
		DataFormat = (new String(BA,Idx,ItemFormatSize,"windows-1251")).toUpperCase(Locale.US); Idx += ItemFormatSize;
		short ItemNameSize = TDataConverter.ConvertLEByteArrayToInt16(BA,Idx); Idx += 2;
		DataName = new String(BA,Idx,ItemNameSize,"windows-1251"); Idx += ItemNameSize;
		int DataSize = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		if (DataSize > 0) {
			Data = new byte[DataSize];
			System.arraycopy(BA,Idx, Data,0, DataSize); Idx += DataSize;
		}
		else 
			Data = null;
		return Idx;
	}

	public void FromByteArrayV0(byte[] BA) throws Exception,IOException {
		int Idx = 0;
		int ItemsCounter = TDataConverter.ConvertLEByteArrayToInt16(BA,Idx); Idx += 2;
		if (ItemsCounter != 1)
			throw new Exception("item count is not equal to 1"); //. =>
		FromByteArrayV0(BA,Idx);
	}
	
	public byte[] ToByteArrayV0() throws IOException {
		byte[] DataFormatBA = null;
		byte DataFormatSize = 0;
		if (DataFormat != null) {
			DataFormatBA = DataFormat.getBytes("windows-1251");
			DataFormatSize = (byte)DataFormatBA.length;
		}
		byte[] DataNameBA = null;
		short DataNameSize = 0;
		if (DataName != null) {
			DataNameBA = DataName.getBytes("windows-1251");
			DataNameSize = (byte)DataNameBA.length;
		}
		int DataSize = 0;
		if (Data != null) 
			DataSize = Data.length;
		//.
		byte[] Result = new byte[4/*SizeOf(DataComponentType)*/+8/*SizeOf(DataComponentID)*/+4/*SizeOf(DataType)*/+1/*SizeOf(DataFormat)*/+DataFormatSize+2/*SizeOf(DataName)*/+DataNameSize+4/*SizeOf(DataSize)*/+DataSize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(DataComponentType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		BA = TDataConverter.ConvertInt64ToLEByteArray(DataComponentID); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; //. SizeOf(Int64)
		//.
		BA = TDataConverter.ConvertInt32ToLEByteArray(DataType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		Result[Idx] = DataFormatSize; Idx++;
		if (DataFormatSize > 0) {
			System.arraycopy(DataFormatBA,0, Result,Idx, DataFormatSize); Idx += DataFormatSize;
		}
		//.
		BA = TDataConverter.ConvertInt16ToLEByteArray(DataNameSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataNameSize > 0) {
			System.arraycopy(DataNameBA,0, Result,Idx, DataNameSize); Idx += DataNameSize;
		}
		//.
		BA = TDataConverter.ConvertInt32ToLEByteArray(DataSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataSize > 0) {
			System.arraycopy(Data,0, Result,Idx, DataSize); Idx += DataSize;
		}
		//.
		return Result;
	}	

	public void PrepareAsName() {
		//. convert to name data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_Name)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE;
	}
	
	public void PrepareAsFullFromFile(String pFileName) {
		DataFileName = pFileName;
		// . convert to full data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
	}
	
	public void PrepareForComponent(int idTComponent, long idComponent, String pDataParams, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		DataParams = pDataParams;
		//.
		int Version = 0;
		TComponentFunctionality CF = Server.User.Space.TypesSystem.TComponentFunctionality_Create(idTComponent,idComponent);
		if (CF == null)
			return; //. ->
		try {
			if (!flWithComponents) {
				byte[] DataDocument = CF.Context_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
				if (DataDocument != null) {
					FromByteArrayV0(DataDocument);
					if (DataActualityIsExpired()) 
						DataDocument = null;
				}
				if (DataDocument == null) {
					DataDocument = CF.Server_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
					if (DataDocument != null)
						FromByteArrayV0(DataDocument);
				}
				return; //. ->
			}
			else {
				byte[] DataDocument = CF.Server_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
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
	
	public File GetTempFile() {
		File TempFolder = new File(TGeoLogApplication.GetTempFolder());
		return (new File(TempFolder.getAbsolutePath()+"/"+"DataFile"+Long.toString(DataComponentID)+DataFormat));	
	}
	
	private File CreateTempFile() throws Exception {
		if (Data == null)
			throw new Exception("data file is null"); //. =>
		File TempFile = GetTempFile();
		FileOutputStream fos = new FileOutputStream(TempFile);
		try {
			fos.write(Data,0,Data.length);
		}
		finally {
			fos.close();
		}
		return TempFile;
	}
	
	public File GetFile() throws Exception {
		if (DataFileName != null)
			return new File(DataFileName); //. ->
		else
			return CreateTempFile(); //. ->
	}

	public byte[] GetFileData() throws Exception {
		if (DataFileName != null) {
			File F = new File(DataFileName);
			byte[] _Data = new byte[(int)F.length()];
			FileInputStream FIS = new FileInputStream(F);
			try {
				FIS.read(_Data);
			}
			finally {
				FIS.close();
			}
			return _Data; //. ->
		}
		else
			return Data; //. ->
	}
	
	public boolean FileIsEmpty() throws Exception {
		if (DataFileName != null) {
			File F = new File(DataFileName);
			return (!(F.exists() && (F.length() > 0))); //. ->
		}
		else
			return (!((Data != null) && (Data.length > 0))); //. ->
	}
}
