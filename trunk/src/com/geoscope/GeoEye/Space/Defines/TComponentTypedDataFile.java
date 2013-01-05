package com.geoscope.GeoEye.Space.Defines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.geoscope.Utils.TDataConverter;

import android.os.Environment;

public class TComponentTypedDataFile {

	public TComponentTypedDataFiles TypedDataFiles;
	//.
	public int		DataComponentType;
	public int		DataComponentID;
	public int		DataType;
	public String 	DataFormat;
	public String 	DataName;
	public byte[]	Data;
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles) {
		TypedDataFiles = pTypedDataFiles;
	}
	
	public String FileName() {
		return (DataName+DataType);
	}

	public int PrepareFromByteArrayV0(byte[] BA, int Idx) throws IOException {
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

	public void PrepareFromByteArrayV0(byte[] BA) throws Exception,IOException {
		int Idx = 0;
		int ItemsCounter = TDataConverter.ConvertBEByteArrayToInt16(BA,Idx); Idx += 2;
		if (ItemsCounter != 1)
			throw new Exception("�������� ������ ����� ������, ����� ��������� �� ����� 1"); //. =>
		PrepareFromByteArrayV0(BA,Idx);
	}
	
	public File GetTempFile() {
		File TempFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Temp");
		TempFolder.mkdirs();
		return (new File(TempFolder.getAbsolutePath()+"/"+"Data"+DataFormat));	
	}
	
	public File CreateTempFile() throws Exception {
		if (Data == null)
			throw new Exception("���� ������ ����"); //. =>
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
}
