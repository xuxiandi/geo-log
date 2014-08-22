package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;

import android.os.Environment;

public class TComponentTypedDataFile {

	public TComponentTypedDataFiles TypedDataFiles;
	//.
	public int		DataComponentType;
	public int		DataComponentID;
	public int		DataType;
	public String 	DataFormat;
	public String 	DataName;
	public byte[]	Data = null;
	public String 	DataFileName = null;
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles) {
		TypedDataFiles = pTypedDataFiles;
	}
	
	public String FileName() {
		return (DataName+DataFormat);
	}

	public boolean IsLoaded() {
		return ((DataFileName != null) | (Data != null));
	}
	
	public boolean IsFSFile() {
		return (DataFileName != null);
	}
	
	public int FromByteArrayV0(byte[] BA, int Idx) throws IOException {
		DataComponentType = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		DataComponentID = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 8; //. native ComponentID is Int64
		DataType = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		byte ItemFormatSize = BA[Idx]; Idx++;
		DataFormat = new String(BA,Idx,ItemFormatSize,"windows-1251"); Idx += ItemFormatSize;
		short ItemNameSize = TDataConverter.ConvertBEByteArrayToInt16(BA,Idx); Idx += 2;
		DataName = new String(BA,Idx,ItemNameSize,"windows-1251"); Idx += ItemNameSize;
		int DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
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
		int ItemsCounter = TDataConverter.ConvertBEByteArrayToInt16(BA,Idx); Idx += 2;
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
		
		
		byte[] Result = new byte[4/*SizeOf(DataComponentType)*/+8/*SizeOf(DataComponentID)*/+4/*SizeOf(DataType)*/+1/*SizeOf(DataFormat)*/+DataFormatSize+2/*SizeOf(DataName)*/+DataNameSize+4/*SizeOf(DataSize)*/+DataSize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(DataComponentType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		BA = TDataConverter.ConvertInt32ToBEByteArray(DataComponentID); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		//.
		BA = TDataConverter.ConvertInt32ToBEByteArray(DataType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		Result[Idx] = DataFormatSize; Idx++;
		if (DataFormatSize > 0) {
			System.arraycopy(DataFormatBA,0, Result,Idx, DataFormatSize); Idx += DataFormatSize;
		}
		//.
		BA = TDataConverter.ConvertInt16ToBEByteArray(DataNameSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataNameSize > 0) {
			System.arraycopy(DataNameBA,0, Result,Idx, DataNameSize); Idx += DataNameSize;
		}
		//.
		BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataSize > 0) {
			System.arraycopy(Data,0, Result,Idx, DataSize); Idx += DataSize;
		}
		//.
		return Result;
	}	

	public void PrepareFullFromFile(String pFileName) {
		DataFileName = pFileName;
		// . convert to full data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
	}
	
	public File GetTempFile() {
		File TempFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Temp");
		TempFolder.mkdirs();
		return (new File(TempFolder.getAbsolutePath()+"/"+"Data"+DataFormat));	
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
}
