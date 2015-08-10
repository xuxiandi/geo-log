package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;

public class TComponentTypedDataFiles {
	
	public static TThumbnailImageComposition GetImageComposition(ArrayList<TComponentTypedDataFile> ImageDataFiles, int Size) throws Exception {
		float Padding = 4.0F;
		int ImageCount = ImageDataFiles.size();
		switch (ImageCount) {
		
		case 1:
			Bitmap Result;
			TComponentTypedDataFile ImageDataFile = ImageDataFiles.get(0);
			if (ImageDataFile.Data != null) {
				Result = ImageDataFile.AsImageBitmap(Size);
				if (Result == null)
					return null; //. ->
			}
			else
				Result = Bitmap.createBitmap(Size,Size, Bitmap.Config.ARGB_8888);
			RectF DestRect = new RectF(0,0, Result.getWidth(),Result.getHeight()); 
			//.
			return (new TThumbnailImageComposition(Result, ImageDataFile)); //. ->
			
		case 2:
			float Step = Size/2.0F;
			//.
			Result = Bitmap.createBitmap(Size,Size, Bitmap.Config.ARGB_8888);
			Result.eraseColor(Color.WHITE);
			//.
			TThumbnailImageComposition Composition = new TThumbnailImageComposition(Result);
			//.
			Canvas ResultCanvas = new Canvas(Result);
			Paint DrawPaint = new Paint();
			Rect SrcRect = new Rect(); 
			DestRect = new RectF(); 
			//. 0;0
			int Y = 0;
			ImageDataFile = ImageDataFiles.get(0);
			if (ImageDataFile.Data != null) {
				Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
				if (TileImage != null)
					try {
						SrcRect.right = TileImage.getWidth();
						SrcRect.bottom = TileImage.getHeight();
						//.
						DestRect.left = 0;
						DestRect.right = Size;
						DestRect.top = Y*Step;
						DestRect.bottom = (Y+1)*Step-Padding;
						//.
						ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
						//.
						Composition.Map.AddItem(DestRect, ImageDataFile);
					}
					finally {
						TileImage.recycle();
					}
			}
			//. 0;1
			Y = 1;
			ImageDataFile = ImageDataFiles.get(1);
			if (ImageDataFile.Data != null) {
				Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
				if (TileImage != null)
					try {
						SrcRect.right = TileImage.getWidth();
						SrcRect.bottom = TileImage.getHeight();
						//.
						DestRect.left = 0;
						DestRect.right = Size;
						DestRect.top = Y*Step+Padding;
						DestRect.bottom = (Y+1)*Step;
						//.
						ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
						//.
						Composition.Map.AddItem(DestRect, ImageDataFile);
					}
					finally {
						TileImage.recycle();
					}
			}
			return Composition; //. ->
			
		case 3:
			Step = Size/2.0F;
			//.
			Result = Bitmap.createBitmap(Size,Size, Bitmap.Config.ARGB_8888);
			Result.eraseColor(Color.TRANSPARENT);
			//.
			Composition = new TThumbnailImageComposition(Result);
			//.
			ResultCanvas = new Canvas(Result);
			DrawPaint = new Paint();
			SrcRect = new Rect(); 
			DestRect = new RectF(); 
			//. 0;0
			int X = 0;
			Y = 0;
			ImageDataFile = ImageDataFiles.get(0);
			if (ImageDataFile.Data != null) {
				Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
				if (TileImage != null)
					try {
						SrcRect.right = TileImage.getWidth();
						SrcRect.bottom = TileImage.getHeight();
						//.
						DestRect.left = X*Step;
						DestRect.right = (X+1)*Step-Padding;
						DestRect.top = Y*Step;
						DestRect.bottom = (Y+1)*Step-Padding;
						//.
						ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
						//.
						Composition.Map.AddItem(DestRect, ImageDataFile);
					}
					finally {
						TileImage.recycle();
					}
			}
			//. 1;0
			X = 1;
			Y = 0;
			ImageDataFile = ImageDataFiles.get(1);
			if (ImageDataFile.Data != null) {
				Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
				if (TileImage != null)
					try {
						SrcRect.right = TileImage.getWidth();
						SrcRect.bottom = TileImage.getHeight();
						//.
						DestRect.left = X*Step+Padding;
						DestRect.right = (X+1)*Step;
						DestRect.top = Y*Step;
						DestRect.bottom = (Y+1)*Step-Padding;
						//.
						ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
						//.
						Composition.Map.AddItem(DestRect, ImageDataFile);
					}
					finally {
						TileImage.recycle();
					}
			}
			//. 0;1
			X = 0;
			Y = 1;
			ImageDataFile = ImageDataFiles.get(2);
			if (ImageDataFile.Data != null) {
				Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
				if (TileImage != null)
					try {
						SrcRect.right = TileImage.getWidth();
						SrcRect.bottom = TileImage.getHeight();
						//.
						DestRect.left = X*Step;
						DestRect.right = (X+1)*Step-Padding;
						DestRect.top = Y*Step+Padding;
						DestRect.bottom = (Y+1)*Step;
						//.
						ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
						//.
						Composition.Map.AddItem(DestRect, ImageDataFile);
					}
					finally {
						TileImage.recycle();
					}
			}
			return Composition; //. ->
		
		default: 
			if (ImageCount >= 4) {
				Step = Size/2.0F;
				//.
				Result = Bitmap.createBitmap(Size,Size, Bitmap.Config.ARGB_8888);
				Result.eraseColor(Color.TRANSPARENT);
				//.
				Composition = new TThumbnailImageComposition(Result);
				//.
				ResultCanvas = new Canvas(Result);
				DrawPaint = new Paint();
				SrcRect = new Rect(); 
				DestRect = new RectF(); 
				//. 0;0
				X = 0;
				Y = 0;
				ImageDataFile = ImageDataFiles.get(0);
				if (ImageDataFile.Data != null) {
					Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
					if (TileImage != null)
						try {
							SrcRect.right = TileImage.getWidth();
							SrcRect.bottom = TileImage.getHeight();
							//.
							DestRect.left = X*Step;
							DestRect.right = (X+1)*Step-Padding;
							DestRect.top = Y*Step;
							DestRect.bottom = (Y+1)*Step-Padding;
							//.
							ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
							//.
							Composition.Map.AddItem(DestRect, ImageDataFile);
						}
						finally {
							TileImage.recycle();
						}
				}
				//. 1;0
				X = 1;
				Y = 0;
				ImageDataFile = ImageDataFiles.get(1);
				if (ImageDataFile.Data != null) {
					Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
					if (TileImage != null)
						try {
							SrcRect.right = TileImage.getWidth();
							SrcRect.bottom = TileImage.getHeight();
							//.
							DestRect.left = X*Step+Padding;
							DestRect.right = (X+1)*Step;
							DestRect.top = Y*Step;
							DestRect.bottom = (Y+1)*Step-Padding;
							//.
							ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
							//.
							Composition.Map.AddItem(DestRect, ImageDataFile);
						}
						finally {
							TileImage.recycle();
						}
				}
				//. 0;1
				X = 0;
				Y = 1;
				ImageDataFile = ImageDataFiles.get(2);
				if (ImageDataFile.Data != null) {
					Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
					if (TileImage != null)
						try {
							SrcRect.right = TileImage.getWidth();
							SrcRect.bottom = TileImage.getHeight();
							//.
							DestRect.left = X*Step;
							DestRect.right = (X+1)*Step-Padding;
							DestRect.top = Y*Step+Padding;
							DestRect.bottom = (Y+1)*Step;
							//.
							ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
							//.
							Composition.Map.AddItem(DestRect, ImageDataFile);
						}
						finally {
							TileImage.recycle();
						}
				}
				//. 1;1
				X = 1;
				Y = 1;
				ImageDataFile = ImageDataFiles.get(3);
				if (ImageDataFile.Data != null) {
					Bitmap TileImage = ImageDataFile.AsImageBitmap(Size);
					if (TileImage != null)
						try {
							SrcRect.right = TileImage.getWidth();
							SrcRect.bottom = TileImage.getHeight();
							//.
							DestRect.left = X*Step+Padding;
							DestRect.right = (X+1)*Step;
							DestRect.top = Y*Step+Padding;
							DestRect.bottom = (Y+1)*Step;
							//.
							ResultCanvas.drawBitmap(TileImage, SrcRect, DestRect, DrawPaint);
							//.
							Composition.Map.AddItem(DestRect, ImageDataFile);
						}
						finally {
							TileImage.recycle();
						}
				}
				return Composition; //. ->
			}
			else
				return null; //. ->
		}
	}
	
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

	public String GetName() {
		TComponentTypedDataFile RootItem = GetRootItem();
		if (RootItem != null)
			return RootItem.DataName; //. ->
		else
			return ""; //. ->
	}
	
	public TComponentTypedDataFiles Clone() {
		return (new TComponentTypedDataFiles(context, DataModel, DataType));
	}
	
	public int Count() {
		return Items.length;
	}

	public TComponentTypedDataFile GetRootItem() {
		if (Items.length > 0)
			return Items[0]; //. ->
		else
			return null; //. ->
	}
	
	public TComponentTypedDataFile GetAnItemByDataType(int pDataType) {
		int Cnt = Items.length;
		for (int I = 0; I < Cnt; I++) {
			TComponentTypedDataFile Item = Items[I]; 
			if (Item.DataType == pDataType)
				return Item; //. ->
		}
		return null;
	}
	
	public ArrayList<TComponentTypedDataFile> GetItemsByDataType(int pDataType) {
		ArrayList<TComponentTypedDataFile> Result = new ArrayList<TComponentTypedDataFile>(); 
		int Cnt = Items.length;
		for (int I = 0; I < Cnt; I++) {
			TComponentTypedDataFile Item = Items[I]; 
			if (Item.DataType == pDataType)
				Result.add(Item); //. ->
		}
		return Result;
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
	
	public void PrepareAsFull() {
		int Cnt = Items.length;
		for (int I = 0; I < Cnt; I++) 
			Items[I].PrepareAsFull();
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
