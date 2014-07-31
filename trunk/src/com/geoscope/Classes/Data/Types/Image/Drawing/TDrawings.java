package com.geoscope.Classes.Data.Types.Image.Drawing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawing.TRectangle;
import com.geoscope.Classes.IO.Log.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TSpaceContainers;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

public class TDrawings {

	public static class TDescriptor {
		
		public double 	Timestamp = OleDate.UTCCurrentTimestamp();
		public String 	Name = "";
		public int 		BackgroundColor = Color.WHITE;
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
	    	Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Name = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Name = "";
	    	BackgroundColor = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
	    	return Idx;
		}

		public byte[] ToByteArray() throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
				BOS.write(BA);
				//.
				byte[] BA_SL = new byte[1];
				BA_SL[0] = (byte)Name.length();
				BOS.write(BA_SL);
				if (BA_SL[0] > 0)
					BOS.write(Name.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(BackgroundColor);
				BOS.write(BA);
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}
	}	
	
    private static byte[] ZipByteArray(byte[] BA) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ZOutputStream out = new ZOutputStream(bos,JZlib.Z_BEST_COMPRESSION);
            try {
                out.write(BA);
            }
            finally
            {
                out.close();
            }
            return bos.toByteArray(); //. ->
        }
        finally {
            bos.close();
        }
	}
	
	private static byte[] UnzipByteArray(byte[] BA, int Idx, int Size) throws IOException {
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA, Idx,Size);
		try {
			ZInputStream ZIS = new ZInputStream(BIS);
			try {
				byte[] Buffer = new byte[8192];
				int ReadSize;
				ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
				try {
					while ((ReadSize = ZIS.read(Buffer)) > 0) 
						BOS.write(Buffer, 0,ReadSize);
					//.
					return BOS.toByteArray();
				}
				finally {
					BOS.close();
				}
			}
			finally {
				ZIS.close();
			}
		}
		finally {
			BIS.close();
		}
	}
	
	public static TDescriptor Descriptor_FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		TDescriptor Result = new TDescriptor();
		Result.FromByteArray(BA, Idx);
		return Result;
	}
	
	public static TDescriptor Descriptor_FromByteArrayV2(byte[] BA, int Idx) throws IOException {
		TDescriptor Result = new TDescriptor();
		Result.FromByteArray(BA, Idx);
		return Result;
	}
	
	public static TDescriptor Descriptor_LoadFromByteArray(byte[] BA, int Idx) throws Exception {
		short Version = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
		switch (Version) {
		
		case 1:
			return Descriptor_FromByteArrayV1(UnzipByteArray(BA, Idx,BA.length-Idx),0); //. ->
			
		case 2:
			return Descriptor_FromByteArrayV2(UnzipByteArray(BA, Idx,BA.length-Idx),0); //. ->
			
		default:
			throw new IOException("unknown data version: "+Short.toString(Version)); //. =>
		}
	}
	
	public static TDescriptor Descriptor_LoadFromFile(String FN) throws Exception {
		File F = new File(FN);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(FN);
	    	try {
	    			byte[] BA = new byte[(int)F.length()];
	    			FIS.read(BA);
	    			//.
	    			int Idx = 0;
	    			return Descriptor_LoadFromByteArray(BA, Idx); //. ->
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return null; //. ->
	}

	public TDescriptor 		Descriptor = new TDescriptor();
	//.
	public TSpaceContainers SpaceContainers = null;
	//.
	public List<TDrawing>	Items;
	public int				Items_HistoryIndex;
	
	public TDrawings() {
		Items = new ArrayList<TDrawing>(10);
		Items_HistoryIndex = 0;
	}
	
	public void Destroy() {
		if (Items != null) {
			ClearItems();
			//.
			Items = null;
		}
	}	

	public void ClearItems() {
		for (int I = 0; I < Items.size(); I++) 
			Items.get(I).Destroy();
		Items.clear();
		Items_HistoryIndex = 0;
	}	

	public byte[] ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			BOS.write(Descriptor.ToByteArray());
			//.
			byte[] BA;
			int DrawingsCount;
			if (Items != null) 
				DrawingsCount = Items_HistoryIndex;
			else
				DrawingsCount = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DrawingsCount);
			BOS.write(BA);
			for (int I = 0; I < DrawingsCount; I++) {
				TDrawing Drawing = Items.get(I);
				short DrawingTypeID = Drawing.TypeID();
				BA = TDataConverter.ConvertInt16ToBEByteArray(DrawingTypeID);
				BOS.write(BA);
				BA = Drawing.ToByteArray();
				BOS.write(BA);
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public byte[] ToByteArrayV2() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			BOS.write(Descriptor.ToByteArray());
			//.
			byte[] BA;
			if (SpaceContainers != null) 
				BA = SpaceContainers.ToByteArray();
			else
				BA = TDataConverter.ConvertInt32ToBEByteArray(0); //. SpaceContainersCount
			BOS.write(BA);
			//.
			int DrawingsCount;
			if (Items != null) 
				DrawingsCount = Items_HistoryIndex;
			else
				DrawingsCount = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DrawingsCount);
			BOS.write(BA);
			for (int I = 0; I < DrawingsCount; I++) {
				TDrawing Drawing = Items.get(I);
				short DrawingTypeID = Drawing.TypeID();
				BA = TDataConverter.ConvertInt16ToBEByteArray(DrawingTypeID);
				BOS.write(BA);
				BA = Drawing.ToByteArray();
				BOS.write(BA);
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = Descriptor.FromByteArray(BA, Idx);
		//.
		Items.clear();
		Items_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Items.add(Drawing);
		}
		Items_HistoryIndex = DrawingsCount;
		//.
		return Idx;
	}	
	
	public int FromByteArrayV2(byte[] BA, int Idx) throws Exception {
		Idx = Descriptor.FromByteArray(BA, Idx);
		//.
		SpaceContainers = new TSpaceContainers();
		Idx = SpaceContainers.FromByteArray(BA, Idx); 
		//.
		Items.clear();
		Items_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Items.add(Drawing);
		}
		Items_HistoryIndex = DrawingsCount;
		//.
		return Idx;
	}	
	
	public byte[] SaveToByteArray() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			short Version;
			byte[] BA;
			if (SpaceContainers != null) {
				Version = 2;
				BA = TDataConverter.ConvertInt16ToBEByteArray(Version);
				BOS.write(BA);
				BOS.write(ZipByteArray(ToByteArrayV2()));
			}
			else {
				Version = 1;
				BA = TDataConverter.ConvertInt16ToBEByteArray(Version);
				BOS.write(BA);
				BOS.write(ZipByteArray(ToByteArrayV1()));
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int LoadFromByteArray(byte[] BA, int Idx) throws Exception {
		short Version = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
		switch (Version) {
		
		case 1:
			FromByteArrayV1(UnzipByteArray(BA, Idx,BA.length-Idx),0);
			Idx = BA.length;
			//.
			SpaceContainers = null;
			//.
			break; //. >
			
		case 2:
			FromByteArrayV2(UnzipByteArray(BA, Idx,BA.length-Idx),0);
			Idx = BA.length;
			break; //. >
			
		default:
			throw new IOException("unknown data version: "+Short.toString(Version)); //. =>
		}
		//.
		return Idx;
	}
	
	public void SaveToFile(String FN) throws Exception {
		File F = new File(FN);
    	String Folder = F.getParent(); File FF = new File(Folder); FF.mkdirs();
    	//.
		FileOutputStream FOS = new FileOutputStream(FN);
        try
        {
        	byte[] BA = SaveToByteArray();
        	FOS.write(BA);
        }
        finally
        {
        	FOS.close();
        }
	}	
	
	public boolean LoadFromFile(String FN) throws Exception {
		File F = new File(FN);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(FN);
	    	try {
	    			byte[] BA = new byte[(int)F.length()];
	    			FIS.read(BA);
	    			//.
	    			int Idx = 0;
	    			LoadFromByteArray(BA, Idx);
	    			//.
	    			return true; //. ->
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return false; //. ->
	}

	public boolean IsEmpty() {
		return (Items_HistoryIndex == 0);
	}
	
	public void Add(TDrawing Drawing) {
		if (Items_HistoryIndex != Items.size()) {
			if (Items_HistoryIndex > 0) {
				List<TDrawing> L = Items.subList(0,Items_HistoryIndex);
				List<TDrawing> LastDrawings = Items;
				Items = L;
				//. free forgetting items 
				for (int I = Items_HistoryIndex; I < LastDrawings.size(); I++)
					LastDrawings.get(I).Destroy();
			}
			else 
				ClearItems();
		}
		Items.add(Drawing); 
		Items_HistoryIndex++;
	}
	
	public boolean Undo() throws Exception {
		if (Items_HistoryIndex > 0) {
			Items_HistoryIndex--;
			//.
			return (Items_HistoryIndex > 0); //. ->
		}
		else
			return false;
	}
	
	public boolean UndoAll() throws Exception {
		if (Items_HistoryIndex > 0) {
			Items_HistoryIndex = 0;
			//.
			return true; //. ->
		}
		else
			return false; //. ->
	}
	
	public boolean Redo() throws Exception {
		if (Items_HistoryIndex < Items.size()) {
			Items_HistoryIndex++;
			//.
			return (Items_HistoryIndex < Items.size()); //. ->
		}
		else
			return false;
	}
	
	public void Translate(float dX, float dY) {
		if (SpaceContainers != null)
			SpaceContainers.Translate(dX,dY);
		//.
		for (int I = 0; I < Items.size(); I++) 
			Items.get(I).Translate(dX,dY);
	}
	
	public TDrawingNode GetAveragePosition() {
		TDrawingNode Result = new TDrawingNode();
		int Cnt = 0;
		for (int I = 0; I < Items.size(); I++) {
			TDrawingNode Node = Items.get(I).GetAveragePosition();
			if (Node != null) {
				Result.X += Node.X;
				Result.Y += Node.Y;
				//.
				Cnt++;
			}
		}
		if (Cnt > 0) {
			Result.X = Result.X/Cnt;
			Result.Y = Result.Y/Cnt;
		}
		return Result;
	}
	
	public TRectangle GetRectangle() {
		if (Items.size() == 0)
			return null; //. ->
		TRectangle Result = Items.get(0).GetRectangle(); 
		for (int I = 1; I < Items.size(); I++) {
			TRectangle Rectangle = Items.get(I).GetRectangle();
			if (Rectangle.Xmn < Result.Xmn)
				Result.Xmn = Rectangle.Xmn; 
			if (Rectangle.Xmx > Result.Xmx)
				Result.Xmx = Rectangle.Xmx; 
			if (Rectangle.Ymn < Result.Ymn)
				Result.Ymn = Rectangle.Ymn; 
			if (Rectangle.Ymx > Result.Ymx)
				Result.Ymx = Rectangle.Ymx; 
		}
		return Result;
	}
	
	
	public void Paint(Canvas canvas) {
		for (int I = 0; I < Items_HistoryIndex; I++) 
			Items.get(I).Paint(canvas);
	}
	
	public Bitmap ToBitmap() {
		TRectangle DrawingsRectangle = GetRectangle();
		//.
		Bitmap BMP = Bitmap.createBitmap((int)DrawingsRectangle.Width()+1,(int)DrawingsRectangle.Height()+1, Bitmap.Config.ARGB_8888);
		Canvas BMPCanvas = new Canvas(BMP);
		//.
		float dX = DrawingsRectangle.Xmn-1.0F;
		float dY = DrawingsRectangle.Ymn-1.0F;
		//.
		Translate(-dX,-dY);
		try {
			BMP.eraseColor(Descriptor.BackgroundColor);
			Paint(BMPCanvas);
		}
		finally {
			Translate(dX,dY);
		}
		//.
		return BMP;
	}
	
	public Bitmap ToBitmap(int MaxSize) {
		Bitmap BMP = ToBitmap();
		try {
			float W = BMP.getWidth();
			float H = BMP.getHeight();
			float Multiplier;
			if (W > H) 
				Multiplier = MaxSize/W; 
			else 
				Multiplier = MaxSize/H; 
			RectF ResultRectangle = new RectF(0.0F,0.0F,W*Multiplier,H*Multiplier);
			//.
			Bitmap Result = Bitmap.createBitmap(((int)ResultRectangle.width())+1,((int)ResultRectangle.height())+1, Config.ARGB_8888);
			Canvas ResultCanvas = new Canvas(Result);
			ResultCanvas.drawBitmap(BMP, new Rect(0,0,BMP.getWidth(),BMP.getHeight()), ResultRectangle, new android.graphics.Paint());
			//.
			return Result; //. ->
		}
		finally {
			BMP.recycle();
		}
	}
	
	public byte[] SaveAsBitmapData(String Format) throws IOException {
		TRectangle DrawingsRectangle = GetRectangle();
		if (DrawingsRectangle == null)
			return null; //. ->
		Bitmap BMP = Bitmap.createBitmap((int)DrawingsRectangle.Width()+1,(int)DrawingsRectangle.Height()+1, Bitmap.Config.ARGB_8888);
		try {
			Canvas BMPCanvas = new Canvas(BMP);
			//.
			float dX = DrawingsRectangle.Xmn-1.0F;
			float dY = DrawingsRectangle.Ymn-1.0F;
			//.
			Translate(-dX,-dY);
			try {
				BMP.eraseColor(Descriptor.BackgroundColor);
				Paint(BMPCanvas);
			}
			finally {
				Translate(dX,dY);
			}
			//.
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				if (Format.equals("png"))
					BMP.compress(CompressFormat.PNG, 0, BOS);
				else
					if (Format.equals("jpg"))
						BMP.compress(CompressFormat.JPEG, 100, BOS);
					else
						throw new IOException("unknown bitmap format, format: "+Format); //. =>
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}
		finally {
			BMP.recycle();
		}
	}

	public void SaveAsBitmapFile(String FN, String Format) throws Exception {
		File F = new File(FN+"."+Format);
    	String Folder = F.getParent(); File FF = new File(Folder); FF.mkdirs();
    	//.
		FileOutputStream FOS = new FileOutputStream(FN);
        try
        {
        	byte[] BA = SaveAsBitmapData(Format);
        	FOS.write(BA);
        }
        finally
        {
        	FOS.close();
        }
	}	
}
