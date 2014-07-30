package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;

import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TSpaceReflection {

	public TSpaceReflection Next = null;
	public int RefCount = 0;
	public double 					TimeStamp;
	public TReflectionWindowStruc 	Window;
	public Bitmap 					Data_Bitmap;
	
	public TSpaceReflection() {
		TimeStamp = 0.0;
		Window = new TReflectionWindowStruc();
		Data_Bitmap = null;
	}
	
	public TSpaceReflection(double pTimeStamp, TReflectionWindowStruc pWindow, Bitmap pData_Bitmap) {
		TimeStamp = pTimeStamp;
		Window = pWindow;
		Data_Bitmap = pData_Bitmap;
	}
	
	public void Destroy() {
		if (Data_Bitmap != null) {
			Data_Bitmap.recycle();
			Data_Bitmap = null;
		}
	}
	
	public String DataFileName() {
		return TSpaceReflections.Folder+"/"+Double.toString(TimeStamp)+".dat";
	}
	
	public void SaveDataFile() throws IOException {
		if (Data_Bitmap == null)
			return; //. ->
		FileOutputStream FOS = new FileOutputStream(DataFileName());
        try
        {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Data_Bitmap.compress(CompressFormat.JPEG, 90/* % */, BOS);
				FOS.write(BOS.toByteArray());
			}
			finally {
				BOS.close();
			}
        }
        finally
        {
        	FOS.close();
        }
	}
	
	private void LoadDataFile() throws IOException {
		File F = new File(DataFileName());
		if (!F.exists()) {
			Data_Bitmap = Bitmap.createBitmap(1,1,Config.RGB_565);     
			return; //. ->
		}
		//.
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(F);
    	try {
    		byte[] Data = new byte[(int)FileSize];
    		FIS.read(Data);
    		Data_Bitmap = BitmapFactory.decodeByteArray(Data,0,Data.length,TBitmapDecodingOptions.GetBitmapFactoryOptions());
    	}
    	finally {
    		FIS.close();
    	}
	}
	
	public boolean IsCached() {
		return (Data_Bitmap != null);
	}
	
	public boolean CacheDataFile() throws IOException {
		if (Data_Bitmap == null) {
			LoadDataFile();
			return true; //. ->
		}
		return false; //. ->
	}
	
	public boolean FreeDataFile() {
		if (Data_Bitmap != null) {
			Data_Bitmap.recycle();
			Data_Bitmap = null;
			return true; //. ->
		}
		return false; //. ->
	}
	
	public void DeleteDataFile() {
		File F = new File(DataFileName());
		if (F.exists())
			F.delete();
	}
	
	public void AssignData(double pTimeStamp, Bitmap pData_Bitmap) {
		DeleteDataFile();
		//.
		TimeStamp = pTimeStamp;
		//.
		if (Data_Bitmap != null) 
			Data_Bitmap.recycle();
		Data_Bitmap = pData_Bitmap;
	}
	
	public static int ByteArraySize() {
		return (8/*TimeStamp*/+TReflectionWindowStruc.ByteArraySize());
	}
	
	public int ToByteArray(byte[] Result, int Idx) throws IOException
	{
		byte[] BA;
		BA = TDataConverter.ConvertDoubleToBEByteArray(TimeStamp); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
		BA = Window.ToByteArray(); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
		return Idx;
	}

	public int FromByteArray(byte[] BA, int Idx) throws IOException
	{
		TimeStamp = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Idx = Window.FromByteArray(BA,Idx);
		return Idx;
	}
}
