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
import android.util.Base64;
import android.util.Base64OutputStream;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectionWindow;
import com.geoscope.GeoEye.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TSpaceLays;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.jcraft.jzlib.ZInputStream;

public class TSpaceHints {
	
	public static final String HintsFolder = TReflector.TypesSystemContextFolder+"/"+"Hints"; 
	public static final String HintsFileName = HintsFolder+"/"+"Hints.dat";
	public static final int MaxHintsCount = 1000;
	public static final int HintSpacing = 4;
	
	public TReflector Reflector;
	private TSpaceHint 						Items;
	private int 							ItemsCount;
	private Hashtable<Integer, TSpaceHint> 	ItemsTable;
	private TSpaceHintImageDataFiles		ItemsImageDataFiles;
	private byte[] Buffer = new byte[8192];
	private Paint DrawPointPaint;
	private Paint DrawPointHintImagePaint;
	private Paint SelectedPaint;
	
	public TSpaceHints(TReflector pReflector) throws IOException {
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
		DrawPointHintImagePaint = new Paint();
		ColorFilter filter = new LightingColorFilter(Color.WHITE, 1); 
		DrawPointHintImagePaint.setColorFilter(filter);
		//.
		SelectedPaint = new Paint();
		SelectedPaint.setColor(Color.RED);
		SelectedPaint.setStrokeWidth(2.0F);
		//.
		Load();
	}
	
	public void Destroy() throws IOException {
		Save();
		if (ItemsImageDataFiles != null) {
			ItemsImageDataFiles.Destroy();
			ItemsImageDataFiles = null;
		}
	}
	
	public void Load() throws IOException {
		ItemsImageDataFiles.Load();
		//.
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
		    		ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(ItemsCountBA, 0);
		    		//.
		    		byte[] ItemData = new byte[1024]; //. max item data size
	        		for (int I = 0; I < ItemsCount; I++) {
	            		byte[] ItemDataSizeBA = new byte[2];
						FIS.read(ItemDataSizeBA);
						short ItemDataSize = TDataConverter.ConvertBEByteArrayToInt16(ItemDataSizeBA, 0);
						FIS.read(ItemData, 0,ItemDataSize);
						//.
						int Idx = 0;
			    		int ItemID = TDataConverter.ConvertBEByteArrayToInt32(ItemData, Idx); Idx += 8; //. Int64
	        			TSpaceHint NewItem = new TSpaceHint(ItemID,Reflector.metrics);
	        			NewItem.FromByteArray(ItemData,Idx);
	        			//. insert into queue
	        			if (LastItem != null)
	        				LastItem.Next = NewItem;
	        			else 
	        				Items = NewItem;
	        			ItemsTable.put(ItemID, NewItem);
	        			//.
	        			LastItem = NewItem;
	        		}
	    	}
			finally
			{
				FIS.close(); 
			}
		}
	}
	
	public void Save() throws IOException {
		ItemsImageDataFiles.Save();
		//.
		FileOutputStream FOS = new FileOutputStream(HintsFileName);
        try
        {
        	byte[] ItemsCountBA = TDataConverter.ConvertInt32ToBEByteArray(ItemsCount);
        	FOS.write(ItemsCountBA);
        	TSpaceHint Item = Items;
        	while (Item != null) {
        		byte[] BA = Item.ToByteArray();
        		short ItemDataSize = (short)BA.length;
        		byte[] ItemDataSizeBA = TDataConverter.ConvertInt16ToBEByteArray(ItemDataSize);
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
	
	public synchronized void Clear() throws IOException {
		Items = null;
		ItemsCount = 0;
		ItemsTable = new Hashtable<Integer, TSpaceHint>();
		ItemsImageDataFiles.Clear();
		Save();
	}
	
	public synchronized void FromByteArray(byte[] BA, TCanceller Canceller) throws IOException, CancelException {
    	RemoveOldItems();
		//.
		int Idx = 0;
    	int _ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	for (int I = 0; I < _ItemsCount; I++) {
    		int ItemID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
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
			if ((Canceller != null) && Canceller.flCancel)
    			throw new CancelException(); //. =>
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
	
	private void InputStream_ReadData(InputStream in, byte[] Data, int DataSize, TCanceller Canceller) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
			if ((Canceller != null) && Canceller.flCancel)
				throw new CancelException(); //. =>
        }
	}

	@SuppressWarnings({ "null", "unused" })
	public void GetHintsFromServer(TReflectionWindow ReflectionWindow, TCanceller Canceller) throws Exception {
		TReflectionWindowStruc RW = ReflectionWindow.GetWindow();
		TSpaceLays Lays = ReflectionWindow.getLays();
		TReflectionWindowActualityInterval ActualityInterval = ReflectionWindow.GetActualityInterval();
		//.
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
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
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TSVUserDataSize);
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
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
				byte[] HintDataSizeBA = new byte[4]; 
				InputStream_ReadData(in, HintDataSizeBA,HintDataSizeBA.length, Canceller);
				int HintDataSize = TDataConverter.ConvertBEByteArrayToInt32(HintDataSizeBA,0); 
				byte[] HintData = new byte[HintDataSize]; 
				InputStream_ReadData(in, HintData,HintDataSize, Canceller);
				HintData = UnPackByteArray(HintData);
				ReviseItemsInReflectionWindow(RW,HintData,Canceller);
				FromByteArray(HintData,Canceller);
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	private synchronized void RemoveOldItems() {
    	if (ItemsCount < (1.1*MaxHintsCount))
    		return; //. ->
		int Cnt = MaxHintsCount;
		TSpaceHint Item = Items;
		while (Item != null) {
			Cnt--;
			Item = Item.Next;
			//.
			if (Cnt == 0) {
				if (Item == null)
					return; //. ->
				TSpaceHint RemoveItem = Item.Next;
				Item.Next = null;
				ItemsCount = MaxHintsCount;
				while (RemoveItem != null) {
	    			ItemsTable.remove(RemoveItem.ID);
					//.
					RemoveItem = RemoveItem.Next;
				}
			}
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
		    	int _ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(ExistingItemsBA, Idx); Idx += 4;
		    	for (int I = 0; I < _ItemsCount; I++) {
		    		int ItemID = TDataConverter.ConvertBEByteArrayToInt32(ExistingItemsBA, Idx); Idx += 8; //. Int64
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
	    	}
	    	else 
				LastItem = Item;
	    	//.
			if ((Canceller != null) && Canceller.flCancel)
	    		throw new CancelException(); //. =>
	    	//.
			Item = Item.Next;
		}
	}
	
	private String PrepareHintImagesURL(int[] HintIDs) throws IOException {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
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
    	int _ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	for (int I = 0; I < _ItemsCount; I++) {
    		int HintID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		int ImageDataFileID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		TSpaceHintImageDataFile ItemsImageDataFiles_Item = ItemsImageDataFiles.GetItem(ImageDataFileID);
    		Idx = ItemsImageDataFiles_Item.FromByteArray(BA, Idx);
    		//.
    		TSpaceHint Hint = ItemsTable.get(HintID);
    		if (Hint != null)
    			Hint.InfoImageDATAFileID = ImageDataFileID;
    		//.
			if ((Canceller != null) && Canceller.flCancel)
    			throw new CancelException(); //. =>
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
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
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
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(url);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
				//.
				int RetSize = HttpConnection.getContentLength();
				if (RetSize == 0)
					throw new Exception(Reflector.getString(R.string.SUnknownServerResponse)); //. =>
				Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
		ItemsImageDataFiles_FromZippedByteArray(Data,Canceller);
		//. final check for supplyment
		synchronized (this) {
			TSpaceHint Item = Items;
			while (Item != null) {
				if ((Item.InfoImageDATAFileID != 0) && (ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID) == null))
					return SHWIDF_RESULT_SUPPLIEDPARTIALLY; //. ->
				//.
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
				//.
				Item = Item.Next;
			}
		}
		return SHWIDF_RESULT_SUPPLIED;
	}
	
	public synchronized void DrawOnCanvas(TReflectionWindowStruc RW, double VisibleFactor, Canvas canvas) {
		double RW_SqrScale = Math.pow(RW.Scale(),2);
		TSpaceHint Item = Items;
		while (Item != null) {
			if (RW.Container_IsNodeVisible(Item.BindingPointX,Item.BindingPointY)) {
				if ((Item.BaseSquare*RW_SqrScale) >= VisibleFactor) {
					TXYCoord P = RW.ConvertToScreen(Item.BindingPointX,Item.BindingPointY);
					if (((RW.Xmn <= P.X) && (P.X <= RW.Xmx)) && ((RW.Ymn <= P.Y) && (P.Y <= RW.Ymx))) {
						//. draw image
						float Left = (float)P.X;
						boolean flImage = false;
						synchronized (ItemsImageDataFiles) {
							TSpaceHintImageDataFile ImageDataFile = ItemsImageDataFiles.ItemsTable.get(Item.InfoImageDATAFileID);
							if ((ImageDataFile != null) && (ImageDataFile.Data != null)) {
								canvas.drawBitmap(ImageDataFile.Data, Left,(float)(P.Y-ImageDataFile.Data.getHeight()), DrawPointHintImagePaint);
								Left += ImageDataFile.Data.getWidth()+1.0F;
								flImage = true;
							}
						}
						//.
						if (!flImage)
							canvas.drawCircle((float)P.X,(float)P.Y,3.0F,DrawPointPaint);
						//. draw text
						Paint ShadowPaint = new Paint(Item.paint);
						ShadowPaint.setColor(Color.BLACK);
                        canvas.drawText(Item.InfoString, Left+1,(float)P.Y+1, ShadowPaint);
                        canvas.drawText(Item.InfoString, Left,(float)P.Y, Item.paint);
                        if (Item.flSelected) {
                        	Rect TR = new Rect();
                        	Item.paint.getTextBounds(Item.InfoString, 0,Item.InfoString.length(), TR);
                        	float X0,Y0,X1,Y1;
                        	X0 = Left-HintSpacing; Y0 = (float)P.Y-(TR.bottom-TR.top)-HintSpacing;
                        	X1 = Left+(TR.right-TR.left)+HintSpacing; Y1 = (float)P.Y+HintSpacing;
                        	float[] Points = {X0,Y0,X1,Y0, X1,Y0,X1,Y1, X1,Y1,X0,Y1, X0,Y1,X0,Y0};
                        	canvas.drawLines(Points,SelectedPaint);
                        }
					}
				}
			}
			//.
			Item = Item.Next;
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
								W = ImageDataFile.Data.getWidth()+1.0F;
								H = ImageDataFile.Data.getHeight();
							}
						}
						//. 
                    	Rect TR = new Rect();
                    	Item.paint.getTextBounds(Item.InfoString, 0,Item.InfoString.length(), TR);
                        W += (TR.right-TR.left);
                        if (Item.InfoStringFontSize > H)
                        	H = (TR.bottom-TR.top);
                        //.
                        if ((((P.X-HintSpacing) <= pX) && (pX <= (P.X+W+HintSpacing))) && ((((P.Y-H-HintSpacing) <= pY) && (pY <= (P.Y+HintSpacing))))) {
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
