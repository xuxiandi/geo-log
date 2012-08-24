package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;

public class TSpaceHint {

	public TSpaceHint Next;
	public int 		ID;
	public short 	Level;
	public short	InfoComponent_Type;
	public int 		InfoComponent_ID;
	public double 	BindingPointX;
	public double 	BindingPointY;
	public double 	BaseSquare;
	public int 		InfoImageDATAFileID;
	public String 	InfoString;
	public int 		InfoStringFontColor;
	public byte		InfoStringFontSize;
	public String 	InfoStringFontName;
	public boolean	flSelected = false;
	//.
	public TComponentTypedDataFiles InfoComponent_TypedDataFiles;
	//.
	public Paint	paint;
	
	public TSpaceHint(int pID) {
		ID = pID;
		paint = new Paint();
	}
	
	public TSpaceHint Clone() {
		TSpaceHint Result = new TSpaceHint(ID);
		//.
		Result.InfoComponent_Type = InfoComponent_Type;
		Result.InfoComponent_ID = InfoComponent_ID;
		Result.BindingPointX = BindingPointX;
		Result.BindingPointY = BindingPointY;
		Result.BaseSquare = BaseSquare;
		Result.InfoImageDATAFileID = InfoImageDATAFileID;
		Result.InfoString = InfoString;
		Result.InfoStringFontColor = InfoStringFontColor;
		Result.InfoStringFontSize = InfoStringFontSize;
		Result.InfoStringFontName = InfoStringFontName;
		Result.flSelected = flSelected;
		//.
		return Result;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
    	Level = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
    	//.
    	InfoComponent_Type = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
    	InfoComponent_ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    	//.
    	BindingPointX = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
    	BindingPointY = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
    	BaseSquare = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
    	//.
    	InfoImageDATAFileID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    	byte SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		InfoString = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		InfoString = "";
    	InfoStringFontColor = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	InfoStringFontSize = BA[Idx]; Idx++;
    	SS = BA[Idx]; Idx++;
    	if (SS > 0) {
    		InfoStringFontName = new String(BA, Idx,SS, "windows-1251");
    		Idx += SS;
    	}
    	else
    		InfoStringFontName = "";
    	//.
		Typeface tf = Typeface.create(InfoStringFontName,Typeface.NORMAL);
   		paint.setTypeface(tf);
   		paint.setAntiAlias(true);
		paint.setTextSize(InfoStringFontSize*2.0F);
		byte R = (byte)(InfoStringFontColor & 255);
		byte G = (byte)((InfoStringFontColor >> 8) & 255);
		byte B = (byte)((InfoStringFontColor >> 16) & 255);
		paint.setColor(Color.argb(255,R,G,B));
		//.
		return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		byte[] BA;
		byte[] B1A = new byte[1];
		byte[] Int64Space = new byte[4];
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
			BOS.write(BA);
			BOS.write(Int64Space);
			//.
			BA = TDataConverter.ConvertInt16ToBEByteArray(Level);
			BOS.write(BA);
			//.
			BA = TDataConverter.ConvertInt16ToBEByteArray(InfoComponent_Type);
			BOS.write(BA);
			//.
			BA = TDataConverter.ConvertInt32ToBEByteArray(InfoComponent_ID);
			BOS.write(BA);
			BOS.write(Int64Space);
			//.
			BA = TDataConverter.ConvertDoubleToBEByteArray(BindingPointX);
			BOS.write(BA);
			//.
			BA = TDataConverter.ConvertDoubleToBEByteArray(BindingPointY);
			BOS.write(BA);
			//.
			BA = TDataConverter.ConvertDoubleToBEByteArray(BaseSquare);
			BOS.write(BA);
			//.
			BA = TDataConverter.ConvertInt32ToBEByteArray(InfoImageDATAFileID);
			BOS.write(BA);
			BOS.write(Int64Space);
			//.
			B1A[0] = (byte)InfoString.length();
			BOS.write(B1A);
			if (B1A[0] > 0)
				BOS.write(InfoString.getBytes("windows-1251"));
			//.
			BA = TDataConverter.ConvertInt32ToBEByteArray(InfoStringFontColor);
			BOS.write(BA);
			//.
			B1A[0] = InfoStringFontSize;
			BOS.write(B1A);
			//.
			B1A[0] = (byte)InfoStringFontName.length();
			BOS.write(B1A);
			if (B1A[0] > 0)
				BOS.write(InfoStringFontName.getBytes("windows-1251"));
			//.
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int ByteArraySkip(byte[] BA, int Idx) throws IOException {
    	Idx += 2;
    	//.
    	Idx += 2;
    	Idx += 8; //. Int64
    	//.
    	Idx += 8; 
    	Idx += 8; 
    	Idx += 8;
    	//.
    	Idx += 8; //. Int64
    	byte SS = BA[Idx]; Idx++;
    	if (SS > 0) 
    		Idx += SS;
    	Idx += 4;
    	Idx++;
    	SS = BA[Idx]; Idx++;
    	if (SS > 0) 
    		Idx += SS;
		return Idx;
	}
	
	public static int ByteArrayApproxSize() {
		return (48+100);
	}
}
