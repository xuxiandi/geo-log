package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectionWindow;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TSpaceLays;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.HINTVisualization.TSystemTHintVisualization;
import com.jcraft.jzlib.ZInputStream;

public class TSpaceHints {
	
	public static final String HintsFolder = TSystemTHintVisualization.ContextFolder; 
	public static final String HintsFileName = HintsFolder+"/"+"Hints.dat";
	public static final int 	MaxItemsCount = 1000;
	public static float 		MinItemImageSize = 20;
	public static final int 	ItemSpacing = 4;
	
	public TReflectorComponent Reflector;
	//.
	private boolean flInitialized = false;
	//.
	private TSpaceHint 						Items;
	private int 							ItemsCount;
	private Hashtable<Integer, TSpaceHint> 	ItemsTable;
	private TSpaceHintImageDataFiles		ItemsImageDataFiles;
	private float 							ItemImageMinSize;
	//.
	private byte[] Buffer = new byte[8192];
	//.
	private Paint DrawPointPaint;
	private Paint DrawPointItemImagePaint;
	private Paint SelectedPaint;
	
	public TSpaceHints(TReflectorComponent pReflector) throws IOException {
		Reflector = pReflector;
		//.
		File F = new File(HintsFolder);
		if (!F.exists()) 
			F.mkdirs();
		//.
		Items = null;
		ItemsCount = 0;
		ItemsTable = new Hashtable<Integer, TSpaceHint>();
		ItemsImageDataFiles = new TSpaceHintImageDataFiles(this);
		//.
		DrawPointPaint = new Paint();
		DrawPointPaint.setColor(Color.RED);
		//.
		DrawPointItemImagePaint = new Paint();
		ColorFilter filter = new LightingColorFilter(Color.WHITE, 1); 
		DrawPointItemImagePaint.setColorFilter(filter);
		DrawPointItemImagePaint.setFilterBitmap(true);
		//.
		SelectedPaint = new Paint();
		SelectedPaint.setStrokeWidth(2.0F);
		//.
		ItemImageMinSize = MinItemImageSize*Reflector.metrics.density;
	}
	
	public void Destroy() throws IOException {
		Save();
		if (ItemsImageDataFiles != null) {
			ItemsImageDataFiles.Destroy();
			ItemsImageDataFiles = null;
		}
	}
	
	public void CheckInitialized() throws IOException {
		if (!flInitialized)
			Initialize();
	}
	
	public void Initialize() throws IOException {
		Load();
		//.
		flInitialized = true;
	}
	
	public synchronized void LoadItems() throws IOException {
		Items = null;
		ItemsCount = 0;
		TSpaceHint LastItem = null;
		//.
		File F = new File(HintsFileName);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(HintsFileName);
	    	try {
	    			byte[] ItemsCountBA = new byte[4];
	    			FIS.read(ItemsCountBA);
		    		int _ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(ItemsCountBA, 0);
		    		//.
		    		byte[] ItemData = new byte[1024]; //. max item data size
	        		for (int I = 0; I < _ItemsCount; I++) {
	            		byte[] ItemDataSizeBA = new byte[2];
						FIS.read(ItemDataSizeBA);
						short ItemDataSize = TDataConverter.ConvertLEByteArrayToInt16(ItemDataSizeBA, 0);
						FIS.read(ItemData, 0,ItemDataSize);
						//.
						int Idx = 0;
			    		int ItemID = TDataConverter.ConvertLEByteArrayToInt32(ItemData, Idx); Idx += 8; //. Int64
	        			TSpaceHint NewItem = new TSpaceHint(ItemID,Reflector.metrics);
	        			NewItem.FromByteArray(ItemData,Idx);
	        			if (ItemsTable.get(ItemID) == null) {
		        			//. insert into queue
		        			if (LastItem != null)
		        				LastItem.Next = NewItem;
		        			else 
		        				Items = NewItem;
		        			ItemsTable.put(ItemID, NewItem);
		        			ItemsCount++;
		        			//.
		        			LastItem = NewItem;
	        			}
	        		}
	    	}
			finally
			{
				FIS.close(); 
			}
		}
	}
	
	public void Load() throws IOException {
		ItemsImageDataFiles.Load();
		//.
		LoadItems();
	}
	
	public synchronized void SaveItems() throws IOException {
		FileOutputStream FOS = new FileOutputStream(HintsFileName);
        try
        {
        	byte[] ItemsCountBA = TDataConverter.ConvertInt32ToLEByteArray(ItemsCount);
        	FOS.write(ItemsCountBA);
        	TSpaceHint Item = Items;
        	while (Item != null) {
        		byte[] BA = Item.ToByteArray();
        		short ItemDataSize = (short)BA.length;
        		byte[] ItemDataSizeBA = TDataConverter.ConvertInt16ToLEByteArray(ItemDataSize);
    			FOS.write(ItemDataSizeBA);
    			FOS.write(BA);
    			//.
    			Item = Item.Next;
        	}
        }
        finally
        {
        	FOS.close();
        }
	}
	
	public void Save() throws IOException {
		ItemsImageDataFiles.Save();
		//.
		SaveItems();
	}
	
	public synchronized void ClearItems() throws IOException {
		Items = null;
		ItemsCount = 0;
		ItemsTable = new Hashtable<Integer, TSpaceHint>();
		//.
		SaveItems();
	}
	
	public synchronized void Clear() throws IOException {
		ItemsImageDataFiles.Clear();
		ItemsImageDataFiles.Save();
		//.
		ClearItems();
	}
	
	public synchronized void FromByteArray(byte[] BA, TCanceller Canceller) throws IOException, CancelException {
    	RemoveOldItems();
		//.
		int Idx = 0;
    	int _ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
    	for (int I = 0; I < _ItemsCount; I++) {
    		int ItemID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		TSpaceHint Item = ItemsTable.get(ItemID);
    		if (Item == null) { 
    			Item = new TSpaceHint(ItemID,Reflector.metrics);
    			//.
    			Item.Next = Items;
    			Items = Item;
    			ItemsCount++;
    			//.
    			ItemsTable.put(ItemID, Item);
    		}
    		Idx = Item.FromByteArray(BA, Idx);
    		//.
			if (Canceller != null)
				Canceller.Check();
    	}
	}
	
	public byte[] UnPackByteArray(byte[] BA) throws IOException {
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
		try {
			ZInputStream ZIS = new ZInputStream(BIS);
			try {
				int ReadSize;
				ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
				try {
					while ((ReadSize = ZIS.read(Buffer)) > 0) 
						BOS.write(Buffer, 0,ReadSize);
					//.
					return BOS.toByteArray(); //. =>
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
	
	public synchronized void FromZippedByteArray(byte[] BA, TCanceller Canceller) throws IOException, CancelException {
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
		try {
			ZInputStream ZIS = new ZInputStream(BIS);
			try {
				int ReadSize;
				ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
				try {
					while ((ReadSize = ZIS.read(Buffer)) > 0) 
						BOS.write(Buffer, 0,ReadSize);
					//.
					FromByteArray(BOS.toByteArray(),Canceller);
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
	
	@SuppressWarnings({ "null", "unused" })
	public void GetHintsFromServer(TReflectionWindow ReflectionWindow, TCanceller Canceller) throws Exception {
		if (Reflector.flOffline)
			return; //. ->
		TReflectionWindowStruc RW = ReflectionWindow.GetWindow();
		TSpaceLays Lays = ReflectionWindow.getLays();
		TReflectionWindowActualityInterval ActualityInterval = ReflectionWindow.GetActualityInterval();
		//.
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Reflector.User.UserID);
		String URL2 = "SpaceWindow.png";
		//. add command parameters
		URL2 = URL2+"?"+"5"/*command version*/+","+Double.toString(RW.X0)+","+Double.toString(RW.Y0)+","+Double.toString(RW.X1)+","+Double.toString(RW.Y1)+","+Double.toString(RW.X2)+","+Double.toString(RW.Y2)+","+Double.toString(RW.X3)+","+Double.toString(RW.Y3)+",";
		short[] InvisibleLays = Lays.GetDisabledLaysIndexes();
		short InvisibleLaysCount;
		if (InvisibleLays != null)
			InvisibleLaysCount = (short)InvisibleLays.length;
		else 
			InvisibleLaysCount = 0;
		URL2 = URL2+Integer.toString(InvisibleLaysCount)+",";
		for (int I = 0; I < InvisibleLaysCount; I++) 
			URL2 = URL2+Integer.toString(InvisibleLays[I])+',';
		URL2 = URL2+Integer.toString(Reflector.VisibleFactor)+",";
		URL2 = URL2+"1"/*Dynamic hint data version*/+",";
		//. Visualization UserData
		int TSVUserDataSize = 0; 
		byte[] TSVUserData = null; 
		if (TSVUserData != null)
			TSVUserDataSize = TSVUserData.length;
		int Idx = 0;
		byte[] UserData = new byte[4/*SizeOf(TSVUserDataSize)*/+TSVUserDataSize];
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(TSVUserDataSize);
		System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
		if (TSVUserDataSize > 0) {
		  System.arraycopy(TSVUserData,0, UserData,Idx, TSVUserData.length); 
		  Idx += TSVUserData.length;
		}
		String UserDataString;
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.URL_SAFE);
			try {
				B64S.write(UserData);
			}
			finally {
				B64S.close();
			}
			UserDataString = new String(BOS.toByteArray());
		}
		finally {
			BOS.close();
		}
		URL2 = URL2+UserDataString+",";
		//.
		URL2 = URL2+Double.toString(ActualityInterval.GetBeginTimestamp())+","+Double.toString(ActualityInterval.EndTimestamp)+",";
		//.
		URL2 = URL2+Integer.toString(RW.Xmn)+","+Integer.toString(RW.Ymn)+","+Integer.toString(RW.Xmx)+","+Integer.toString(RW.Ymx);
		boolean flUpdateProxySpace = false;
		if (flUpdateProxySpace)
			URL2 = URL2+","+"1"/*flUpdateProxySpace = true*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".png";
		//.
		HttpURLConnection Connection = Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				if (Canceller != null)
					Canceller.Check();
				//.
				byte[] HintDataSizeBA = new byte[4]; 
				TNetworkConnection.InputStream_ReadData(in, HintDataSizeBA,HintDataSizeBA.length, Canceller, Reflector.context);
				int HintDataSize = TDataConverter.ConvertLEByteArrayToInt32(HintDataSizeBA,0); 
				byte[] HintData = new byte[HintDataSize]; 
				TNetworkConnection.InputStream_ReadData(in, HintData,HintDataSize, Canceller, Reflector.context);
				HintData = UnPackByteArray(HintData);
				ReviseItemsInReflectionWindow(RW,HintData,Canceller);
				FromByteArray(HintData,Canceller);
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	private synchronized void RemoveOldItems() {
    	if (ItemsCount < (1.1*MaxItemsCount))
    		return; //. ->
		TSpaceHint RemoveItem = null;
    	ItemsCount = 0;
		TSpaceHint Item = Items;
		while (Item != null) {
			ItemsCount++;
			if (ItemsCount >= MaxItemsCount) {
				RemoveItem = Item.Next;
				Item.Next = null;
				//.
				break; //. >
			}
			//.
			Item = Item.Next;
		}
		while (RemoveItem != null) {
	    	ItemsTable.remove(RemoveItem.ID);
			//.
			RemoveItem = RemoveItem.Next;
		}
	}
	
	public synchronized void ReviseItemsInReflectionWindow(TReflectionWindowStruc RW, byte[] ExistingItemsBA, TCanceller Canceller) throws IOException, CancelException {
		TSpaceHint LastItem = null;
		TSpaceHint Item = Items;
		while (Item != null) {
			boolean flRemove = false;
			if (RW.Container_IsNodeVisible(Item.BindingPointX,Item.BindingPointY)) {
    			flRemove = true;
				int Idx = 0;
		    	int _ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(ExistingItemsBA, Idx); Idx += 4;
		    	for (int I = 0; I < _ItemsCount; I++) {
		    		int ItemID = TDataConverter.ConvertLEByteArrayToInt32(ExistingItemsBA, Idx); Idx += 8; //. Int64
		    		if (ItemID == Item.ID) {
		    			flRemove = false;
		    			break; //. >
		    		}
		    		Idx = Item.ByteArraySkip(ExistingItemsBA, Idx);
		    	}
			}
	    	if (flRemove) {
	    		if (LastItem != null)
	    			LastItem.Next = Item.Next;
	    		else
	    			Items = Item.Next;
    			ItemsTable.remove(Item.ID);
    			ItemsCount--;
	    	}
	    	else 
				LastItem = Item;
	    	//.
			if (Canceller != null)
				Canceller.Check();
	    	//.
			Item = Item.Next;
		}
	}
	
	private String PrepareHintImagesURL(int[] HintIDs) throws IOException {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTHINTVisualization)+"/"+"DataFiles.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
		StringBuilder SB = new StringBuilder();
		for (int I = 0; I < HintIDs.length; I++)
			if (I < (HintIDs.length-1))
				SB.append(Integer.toString(HintIDs[I])+";");
			else
				SB.append(Integer.toString(HintIDs[I]));
		URL2 = URL2+","+SB.toString();
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	private synchronized void ItemsImageDataFiles_FromByteArray(byte[] BA, TCanceller Canceller) throws IOException, CancelException {
		RemoveOldItems();
		//.
		int Idx = 0;
    	int _ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
    	for (int I = 0; I < _ItemsCount; I++) {
    		int HintID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		int ImageDataFileID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		TSpaceHintImageDataFile ItemsImageDataFiles_Item = ItemsImageDataFiles.GetItem(ImageDataFileID);
    		Idx = ItemsImageDataFiles_Item.FromByteArray(BA, Idx);
    		//.
    		TSpaceHint Hint = ItemsTable.get(HintID);
    		if (Hint != null)
    			Hint.InfoImageDATAFileID = ImageDataFileID;
    		//.
			if (Canceller != null)
				Canceller.Check();
    	}
	}
	
	public void ItemsImageDataFiles_FromZippedByteArray(byte[] BA, TCanceller Canceller) throws IOException, CancelException {
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
		try {
			ZInputStream ZIS = new ZInputStream(BIS);
			try {
				int ReadSize;
				ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
				try {
					while ((ReadSize = ZIS.read(Buffer)) > 0) 
						BOS.write(Buffer, 0,ReadSize);
					//.
					ItemsImageDataFiles_FromByteArray(BOS.toByteArray(),Canceller);
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
	
	public static final int SHWIDF_MAX_ITEMS_TO_PROCESS = 10;
	public static final int SHWIDF_RESULT_NOITEMTOSUPPLY = 0;
	public static final int SHWIDF_RESULT_SUPPLIED = 1;
	public static final int SHWIDF_RESULT_SUPPLIEDPARTIALLY = 2;
	
	public int SupplyHintsWithImageDataFiles(TCanceller Canceller) throws Exception {
		ArrayList<TSpaceHint> _Hints = new ArrayList<TSpaceHint>();
		synchronized (this) {
			TSpaceHint Item = Items;
			while (Item != null) {
				if ((Item.InfoImageDATAFileID != 0) && (ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID) == null)) {
					boolean flDataFileIsFound = false;
					int Sz = _Hints.size();
					for (int I = 0; I < Sz; I++)
						if (_Hints.get(I).InfoImageDATAFileID == Item.InfoImageDATAFileID) {
							flDataFileIsFound = true;
							break; //. >
						}
					if (!flDataFileIsFound) { 
						_Hints.add(Item);
						if (_Hints.size() >= SHWIDF_MAX_ITEMS_TO_PROCESS)
							break; //. >
					}
				}
				//.
				if (Canceller != null)
					Canceller.Check();
				//.
				Item = Item.Next;
			}
		}
		if (_Hints.size() == 0)
			return SHWIDF_RESULT_NOITEMTOSUPPLY; //. ->
		int Sz = _Hints.size();
		int[] _HintIDs = new int[Sz];
		for (int I = 0; I < Sz; I++)
			_HintIDs[I] = _Hints.get(I).ID;
		//.
		byte[] Data = null;
		String url = PrepareHintImagesURL(_HintIDs);
		HttpURLConnection Connection = Reflector.Server.OpenConnection(url);
		try {
			InputStream in = Connection.getInputStream();
			try {
				if (Canceller != null)
					Canceller.Check();
				//.
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					throw new Exception(Reflector.context.getString(R.string.SUnknownServerResponse)); //. =>
				Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(Reflector.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		ItemsImageDataFiles_FromZippedByteArray(Data,Canceller);
		//. final check for supply
		synchronized (this) {
			TSpaceHint Item = Items;
			while (Item != null) {
				if ((Item.InfoImageDATAFileID != 0) && (ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID) == null))
					return SHWIDF_RESULT_SUPPLIEDPARTIALLY; //. ->
				//.
				if (Canceller != null)
					Canceller.Check();
				//.
				Item = Item.Next;
			}
		}
		return SHWIDF_RESULT_SUPPLIED;
	}
	
	private static class TSpaceHintItem {
		
		public TSpaceHintItem 	Next = null;
		public TSpaceHint 		Item;
		
		public TSpaceHintItem(TSpaceHint pItem, TSpaceHintItem pNext) {
			Next = pNext;
			Item = pItem;
		}
	}
	
	public synchronized void DrawOnCanvas(TReflectionWindowStruc RW, double VisibleFactor, Canvas canvas) {
		double RW_SqrScale = Math.pow(RW.Scale(),2);
		//. calculate max item number depends on image square
		int SquareOrderHintList_Size = 5*4;
		//. prepare square ordered item list
		TSpaceHintItem SquareOrderHintList = null;
		TSpaceHint Item = Items;
		while (Item != null) {
			if (RW.Container_IsNodeVisible(Item.BindingPointX,Item.BindingPointY)) {
				if ((Item.BaseSquare*RW_SqrScale) >= VisibleFactor) {
					TXYCoord P = RW.ConvertToScreen(Item.BindingPointX,Item.BindingPointY);
					if (((RW.Xmn <= P.X) && (P.X <= RW.Xmx)) && ((RW.Ymn <= P.Y) && (P.Y <= RW.Ymx))) {
						int Size = SquareOrderHintList_Size;
						TSpaceHintItem LastHintItem = null;
						TSpaceHintItem HintItem = SquareOrderHintList;
						while (HintItem != null) {
							if (HintItem.Item.BaseSquare < Item.BaseSquare)
								break; //. >
							//.
							Size--;
							if (Size <= 0)
								break; //. >
							//.
							LastHintItem = HintItem;
							HintItem = HintItem.Next;
						}
						if (Size > 0) 
							if (LastHintItem != null)
								LastHintItem.Next = new TSpaceHintItem(Item,LastHintItem.Next);
							else
								SquareOrderHintList = new TSpaceHintItem(Item,SquareOrderHintList);
					}
				}
			}
			//.
			Item = Item.Next;
		}
		//. re-order item list and prepare a draw list
		TSpaceHint[] DrawItemList = new TSpaceHint[SquareOrderHintList_Size];
		TSpaceHintItem HintItem = SquareOrderHintList;
		for (int I = SquareOrderHintList_Size-1; I >= 0; I--) 
			if (HintItem != null) {
				DrawItemList[I] = HintItem.Item;
				HintItem = HintItem.Next;
			}
			else
				break; //. >
		//. draw list
		for (int I = 0; I < DrawItemList.length; I++) {
			Item = DrawItemList[I];
			if (Item != null) {
				TXYCoord P = RW.ConvertToScreen(Item.BindingPointX,Item.BindingPointY);
				//. draw image
				float Left = (float)P.X;
				boolean flImage = false;
				synchronized (ItemsImageDataFiles) {
					TSpaceHintImageDataFile ImageDataFile = ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID);
					if ((ImageDataFile != null) && (ImageDataFile.Data != null)) {
						RectF ImageRect = ImageDataFile.Data_GetDestinationRect(ItemImageMinSize);
						ImageRect.offset(Left,(float)(P.Y-ImageRect.height()));
						//.
						canvas.drawBitmap(ImageDataFile.Data, ImageDataFile.Data_GetOriginalRect(), ImageRect, DrawPointItemImagePaint);
						//.
						Left += ImageRect.width()+1.0F;
						flImage = true;
					}
				}
				//. draw selection if it exists
                if (Item.flSelected) {
                	Rect TR = new Rect();
                	Item.paint.getTextBounds(Item.InfoString, 0,Item.InfoString.length(), TR);
                	float X0,Y0,X1,Y1;
                	X0 = Left-ItemSpacing; Y0 = (float)P.Y-(TR.bottom-TR.top)-ItemSpacing;
                	X1 = Left+(TR.right-TR.left)+ItemSpacing; Y1 = (float)P.Y+ItemSpacing;
                	//.
            		SelectedPaint.setColor(Color.argb(127, 255,0,0));
    				canvas.drawRect(X0,Y0, X1,Y1, SelectedPaint);
                	float[] Points = {X0,Y0,X1,Y0, X1,Y0,X1,Y1, X1,Y1,X0,Y1, X0,Y1,X0,Y0};
            		SelectedPaint.setColor(Color.argb(255, 255,0,0));
                	canvas.drawLines(Points,SelectedPaint);
                }
				//. draw image
				if (!flImage)
					canvas.drawCircle((float)P.X,(float)P.Y,3.0F,DrawPointPaint);
				//. draw text
				Paint ShadowPaint = new Paint(Item.paint);
				ShadowPaint.setColor(Color.BLACK);
                canvas.drawText(Item.InfoString, Left+1,(float)P.Y+1, ShadowPaint);
                canvas.drawText(Item.InfoString, Left,(float)P.Y, Item.paint);
			}
		}
	}
	
	public TSpaceHint Select(TReflectionWindowStruc RW, double VisibleFactor, float pX, float pY) {
		double RW_SqrScale = Math.pow(RW.Scale(),2);
		TSpaceHint Item = Items;
		while (Item != null) {
			if (RW.Container_IsNodeVisible(Item.BindingPointX,Item.BindingPointY)) {
				if ((Item.BaseSquare*RW_SqrScale) >= VisibleFactor) {
					TXYCoord P = RW.ConvertToScreen(Item.BindingPointX,Item.BindingPointY);
					if (((RW.Xmn <= P.X) && (P.X <= RW.Xmx)) && ((RW.Ymn <= P.Y) && (P.Y <= RW.Ymx))) {
						float W = 0;
						float H = 0;
						//. 
						synchronized (ItemsImageDataFiles) {
							TSpaceHintImageDataFile ImageDataFile = ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID);
							if ((ImageDataFile != null) && (ImageDataFile.Data != null)) {
								RectF ImageRect = ImageDataFile.Data_GetDestinationRect(ItemImageMinSize);
								W = ImageRect.width()+1.0F;
								H = ImageRect.height();
							}
						}
						//. 
                    	Rect TR = new Rect();
                    	Item.paint.getTextBounds(Item.InfoString, 0,Item.InfoString.length(), TR);
                        W += (TR.right-TR.left);
                        if (Item.InfoStringFontSize > H)
                        	H = (TR.bottom-TR.top);
                        //.
                        if ((((P.X-ItemSpacing) <= pX) && (pX <= (P.X+W+ItemSpacing))) && ((((P.Y-H-ItemSpacing) <= pY) && (pY <= (P.Y+ItemSpacing))))) {
                        	Item.flSelected = true;
                        	return Item.Clone(); //. ->
                        }
					}
				}
			}
        	Item.flSelected = false;
			//.
			Item = Item.Next;
		}
		return null;
	}		

	public void UnSelectAll() {
		TSpaceHint Item = Items;
		while (Item != null) {
        	Item.flSelected = false;
			//.
			Item = Item.Next;
		}
	}		
}
