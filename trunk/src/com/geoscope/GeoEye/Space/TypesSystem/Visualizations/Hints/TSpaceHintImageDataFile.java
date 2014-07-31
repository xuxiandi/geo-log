package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

import com.geoscope.Classes.IO.Log.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TSpaceHintImageDataFile {

	public TSpaceHintImageDataFile Next;
	public int 		ID;
	public Bitmap	Data;
	private Rect	Data_OriginalRect = null;
	
	public TSpaceHintImageDataFile(int pID) {
		ID = pID;
	}
	
	public void Destroy() {
		if (Data != null) {
			Data.recycle();
			Data = null;
		}
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
    	int DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	//.
		if (Data != null) {
			Data.recycle();
			Data = null;
		}
    	if (DataSize > 0) {
    		///copy(Bitmap.Config.ARGB_8888,true)
    		Bitmap _Data = BitmapFactory.decodeByteArray(BA, Idx,DataSize,TBitmapDecodingOptions.GetBitmapFactoryOptions()); Idx += DataSize;
    		try {
        		int width = _Data.getWidth();
        		int height = _Data.getHeight();
        		int[] pixels = new int[width*height];
        		_Data.getPixels(pixels, 0, width, 0,0, width,height);
        		int TransparentColor = pixels[0];
        		for (int Y = 0; Y < height; Y++)
        			for (int X = 0; X < width; X++) {
        				int PI = Y*width+X;
        				if (pixels[PI] == TransparentColor)
        					pixels[PI] = Color.TRANSPARENT;
        			}
        		Data = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        		Data.setPixels(pixels, 0, width, 0,0, width,height);
    		}
    		finally {
    			_Data.recycle();
    		}
    	}
    	//.
		return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		int DataSize = 0;
		byte[] DataBA = null;
		if (Data != null) {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				Data.compress(CompressFormat.PNG, 0, BOS);
				DataBA = BOS.toByteArray();
				DataSize = DataBA.length;
			}
			finally {
				BOS.close();
			}
		}
		//.
		byte[] Result = new byte[8/*SizeOf(ID)*/+4/*SizeOf(DataSize)*/+DataSize];
		//.
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 8; //. Int64
		BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += 4;
		if (DataSize > 0) { 
			System.arraycopy(DataBA,0, Result,Idx, DataSize); Idx += DataSize;
		}
		//.
		return Result;
	}
	
	public Rect Data_GetOriginalRect() {
		if (Data_OriginalRect != null)
			return Data_OriginalRect; //. ->
		Data_OriginalRect = new Rect(0,0, Data.getWidth(),Data.getHeight());
		return Data_OriginalRect;
	}
	
	public RectF Data_GetDestinationRect(float MinSize) {
		float W = Data.getWidth();
		float H = Data.getHeight();
		float Multiplier = 1.0F;
		if (W > H) {
			if (W < MinSize)
				Multiplier = MinSize/W; 
		}
		else {
			if (H < MinSize)
				Multiplier = MinSize/H; 
		}
		return new RectF(0.0F,0.0F, W*Multiplier,H*Multiplier);
	}
}
