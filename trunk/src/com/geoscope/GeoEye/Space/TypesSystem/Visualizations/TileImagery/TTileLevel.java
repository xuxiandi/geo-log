package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TSpaceServersInfo;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.GeoLog.Utils.TFileSystem;
import com.geoscope.GeoLog.Utils.TUpdater;

public class TTileLevel {

	public static final double TimestampToFileTimestamp = 24.0*3600*1000;
	
	public static void CheckTileHistoryFolder(String THF) {
		File F = new File(THF);
		if (!F.exists())
			F.mkdir();
	}
	
	public class TTileIndex {
		
		private Hashtable<Integer, Hashtable<Integer, TTile>> XTable;
		private TTile 	Items;
		private int		ItemsCount;
		
		public TTileIndex() {
			XTable = new Hashtable<Integer, Hashtable<Integer,TTile>>();
			//.
			Items = null;
			ItemsCount = 0;
		}
		
		public void Destroy() {
			if (Items != null) {
				TTile Item = Items;
				while (Item != null) {
					Item.Finalize();
					//.
					Item = Item.Next;
				}
				//.
				ItemsCount = 0;
				Items = null;
			}
			XTable = null;
		}
		
		public TTile GetItem(int X, int Y) {
			Hashtable<Integer, TTile> YTable = XTable.get(X);
			if (YTable == null)
				return null; //. ->
			TTile Result = YTable.get(Y);
			//.
			if (Result != null)
				Result.SetAsAccessed();
			return Result;
		}
		
		public TTile SetItem(int X, int Y, TTile Value) {
			TTile LastTile = null;
			Hashtable<Integer, TTile> YTable = XTable.get(X);
			if (YTable == null) {
				YTable = new Hashtable<Integer, TTile>();
				XTable.put(X,YTable);
			}
			else {
				LastTile = YTable.remove(Y);
				//.
				if (LastTile != null) {
					if (LastTile.Pred != null) 
						LastTile.Pred.Next = LastTile.Next;
					else 
						Items = LastTile.Next;
					if (LastTile.Next != null)
						LastTile.Next.Pred = LastTile.Pred;
					ItemsCount--;
					//.
					LastTile.Finalize();
				}
			}
			//.
			if (Value != null) {
				YTable.put(Y,Value);
				//.
				Value.Next = Items;
				Items = Value;
				if (Value.Next != null)
					Value.Next.Pred = Value;
				ItemsCount++;
				//.
				Value.SetAsAccessed();
			}
			return LastTile;
		}
		
		public void RemoveLastItems(int Count) {
			int SkipItemsCount = ItemsCount-Count;
			TTile LastItem = null;
			TTile Item = Items;
			while (SkipItemsCount > 0) {
				LastItem = Item;
				Item = Item.Next;
				SkipItemsCount--;
			}
			while (Item != null) {
				Hashtable<Integer, TTile> YTable = XTable.get(Item.X);
				if (YTable != null) 
					YTable.remove(Item.Y);
				//.
				Item.Finalize();
				//.
				Item = Item.Next;
			}
			if (Count < ItemsCount)
				ItemsCount -= Count;
			else 
				ItemsCount = 0;
			if (LastItem != null)
				LastItem.Next = null;
			else
				Items = null;
		}
	}
	
	private TTileServerProviderCompilation Compilation;
	private int Level;
	private String LevelFolder;
	private TTileIndex TileIndex = null;
	//.
	private Paint paint = new Paint();
	
	public TTileLevel(TTileServerProviderCompilation pCompilation, int pLevel) {
		Compilation = pCompilation;
		Level = pLevel;
		//.
		LevelFolder = Compilation.Folder+"/"+Integer.toString(Level);
		File F = new File(LevelFolder);
		if (!F.exists()) 
			F.mkdirs();
		//.
		TileIndex = new TTileIndex();
	}
	
	public synchronized void Destroy() {
		if (TileIndex != null) {
			TileIndex.Destroy();
			TileIndex = null;
		}
	}
	
	public synchronized int TilesCount() {
		return TileIndex.ItemsCount;
	}
	
	public synchronized TTile GetTile(int X, int Y) {
		return TileIndex.GetItem(X,Y);
	}
	
	public synchronized TTile[] GetTiles() {
		TTile[] Result = new TTile[TileIndex.ItemsCount];
		int I = 0;
		TTile Item = TileIndex.Items;
		while (Item != null) {
			Result[I] = Item;
			//.
			I++;
			Item = Item.Next;
		}
		return Result;
	}
	
	public TTile AddTile(int pX, int pY, double pTimestamp, byte[] pData) throws Exception {
		int pDataSize = 0;
		Bitmap BMP = null;
		if (pData != null) {
			pDataSize = pData.length;
			try {
				BMP = BitmapFactory.decodeByteArray(pData,0,pDataSize,TBitmapDecodingOptions.GetBitmapFactoryOptions());
			}
			catch (Exception E) {
				BMP = null;
				pDataSize = 0;
			}
		}
		TTile NewTile = new TTile(pX,pY, pTimestamp,BMP,TTile.Data_IsTransparent(BMP,pDataSize));
		//.
		synchronized (LevelFolder) {
			//. save into file
	        File TF;
	        if (Compilation.flHistoryEnabled) {
	        	String TileHistoryFolder = LevelFolder+"/"+NewTile.TileHistoryFolder_Name();
	        	CheckTileHistoryFolder(TileHistoryFolder);
	        	TF = new File(TileHistoryFolder+"/"+NewTile.TileHistoryFolder_TileFileName());
	        }
	        else
	        	TF = new File(LevelFolder+"/"+NewTile.TileFileName());
			FileOutputStream FOS = new FileOutputStream(TF);
	        try
	        {
	        	if (pData != null)
	        		FOS.write(pData);
	        }
	        finally
	        {
	        	FOS.close();
	        }
	        long FTS = (long)(NewTile.Timestamp*TimestampToFileTimestamp);
	        if (!TF.setLastModified(FTS))
	        	TTracker.Tracker_Log_WriteError("TTileLevel.AddTile()", "could not set tile file timestamp");
		}
		//. set index item
		synchronized (this) {
			if (TileIndex != null)
				TileIndex.SetItem(pX,pY, NewTile);
		}
		return NewTile;
	}
	
	public synchronized void RemoveTile(int X, int Y) {
		TileIndex.SetItem(X,Y,null);
	}
	
	public synchronized void RemoveTile(TTile Tile) {
		if (TileIndex.GetItem(Tile.X,Tile.Y) != Tile)
			return; //. ->
		TileIndex.SetItem(Tile.X,Tile.Y,null);
	}
	
	public synchronized void RemoveTiles() {
		TileIndex.Destroy();
		TileIndex = new TTileIndex();
	}
	
	public void DeleteTile(int pX, int pY) {
		TTile DeletedTile;
		synchronized (this) {
			DeletedTile = TileIndex.SetItem(pX,pY, null); 
		}
		//.
		if (DeletedTile != null) {
	        File TF;
	        if (Compilation.flHistoryEnabled) {
	        	String TileHistoryFolder = LevelFolder+"/"+DeletedTile.TileHistoryFolder_Name();
	        	TF = new File(TileHistoryFolder+"/"+DeletedTile.TileHistoryFolder_TileFileName());
	        }
	        else
	        	TF = new File(LevelFolder+"/"+DeletedTile.TileFileName());
	        synchronized (LevelFolder) {
		        TF.delete();
			}
		}
	}

	public synchronized void DeleteTiles() {
		TileIndex.Destroy();
		TileIndex = new TTileIndex();
		//. delete level folder
		synchronized (LevelFolder) {
			TFileSystem.EmptyFolder(new File(LevelFolder));
		}
	}
	
	private class TTilesData {

		public byte[] Data = null;
		public boolean flAll = false;
	}
	
	public TTilesData GetAvailableTiles(int Xmn, int Xmx, int Ymn, int Ymx) throws IOException {
		int Capacity = (Xmx-Xmn+1)*(Ymx-Ymn+1);
		int[] _AvailableTiles = new int[Capacity];
		int _AvailableTilesCount = 0;
		for (int X = Xmn; X <= Xmx; X++)
			for (int Y = Ymn; Y <= Ymx; Y++)
			{
				TTile Tile;
				synchronized (this) {
					Tile = TileIndex.GetItem(X,Y);
				}
				if (Tile != null) {
					_AvailableTiles[_AvailableTilesCount] = Tile.TileHashCode();
					_AvailableTilesCount++;
				}
			}
		TTilesData Result = new TTilesData();
		if (_AvailableTilesCount == 0)
			return Result; //. ->
		if (_AvailableTilesCount == Capacity) {
			Result.flAll = true;
			return Result; //. ->
		}
		Result.Data = new byte[_AvailableTilesCount*4/*SizeOf(HashCode)*/];
		for (int I = 0; I < _AvailableTilesCount; I++) {
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(_AvailableTiles[I]);
			System.arraycopy(BA,0, Result.Data,(I << 2), BA.length);
		}
		return Result;
	}
	
	public TTilesData GetNotAvailableTiles(int Xmn, int Xmx, int Ymn, int Ymx) throws IOException {
		int Capacity = (Xmx-Xmn+1)*(Ymx-Ymn+1);
		int[] _NotAvailableTiles = new int[Capacity];
		int _NotAvailableTilesCount = 0;
		for (int X = Xmn; X <= Xmx; X++)
			for (int Y = Ymn; Y <= Ymx; Y++)
			{
				TTile Tile;
				synchronized (this) {
					Tile = TileIndex.GetItem(X,Y);
				}
				if (Tile == null) {
					_NotAvailableTiles[_NotAvailableTilesCount] = TTile.TileHashCode(X,Y);
					_NotAvailableTilesCount++;
				}
			}
		TTilesData Result = new TTilesData();
		if (_NotAvailableTilesCount == 0)
			return Result; //. ->
		if (_NotAvailableTilesCount == Capacity) {
			Result.flAll = true;
			return Result; //. ->
		}
		Result.Data = new byte[_NotAvailableTilesCount*4/*SizeOf(HashCode)*/];
		for (int I = 0; I < _NotAvailableTilesCount; I++) {
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(_NotAvailableTiles[I]);
			System.arraycopy(BA,0, Result.Data,(I << 2), BA.length);
		}
		return Result;
	}
	
	private class TTileAvailabilityLists {
		public byte[] AvailableTiles = null;
		public byte[] NotAvailableTiles = null;
	}
	
	public TTileAvailabilityLists GetTileAvailabilityLists(int Xmn, int Xmx, int Ymn, int Ymx) throws IOException {
		int Capacity = (Xmx-Xmn+1)*(Ymx-Ymn+1);
		int[] _AvailableTiles = new int[Capacity];
		int _AvailableTilesCount = 0;
		int[] _NotAvailableTiles = new int[Capacity];
		int _NotAvailableTilesCount = 0;
		for (int X = Xmn; X <= Xmx; X++)
			for (int Y = Ymn; Y <= Ymx; Y++)
			{
				int SegmentID = TTile.TileHashCode(X,Y);
				TTile Tile;
				synchronized (this) {
					Tile = TileIndex.GetItem(X,Y);
				}
				if (Tile != null) {
					_AvailableTiles[_AvailableTilesCount] = SegmentID;
					_AvailableTilesCount++;
				}
				else {
					_NotAvailableTiles[_NotAvailableTilesCount] = SegmentID;
					_NotAvailableTilesCount++;
				}
			}
		TTileAvailabilityLists Result = new TTileAvailabilityLists();
		if (_AvailableTilesCount != 0) {
			Result.AvailableTiles = new byte[_AvailableTilesCount*4/*SizeOf(HashCode)*/];
			for (int I = 0; I < _AvailableTilesCount; I++) {
				byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(_AvailableTiles[I]);
				System.arraycopy(BA,0, Result.AvailableTiles,(I << 2), BA.length);
			}
		}
		if (_NotAvailableTilesCount != 0) {
			Result.NotAvailableTiles = new byte[_NotAvailableTilesCount*4/*SizeOf(HashCode)*/];
			for (int I = 0; I < _NotAvailableTilesCount; I++) {
				byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(_NotAvailableTiles[I]);
				System.arraycopy(BA,0, Result.NotAvailableTiles,(I << 2), BA.length);
			}
		}
		//.
		return Result;
	}
	
	public boolean RestoreTiles(int Xmn, int Xmx, int Ymn, int Ymx, TTileLimit TileLimit, TCanceller Canceller, TUpdater Updater) throws Exception {
		int Size = (Xmx-Xmn+1)*(Ymx-Ymn+1);
		int Count = 0;
		for (int X = Xmn; X <= Xmx; X++) {
			for (int Y = Ymn; Y <= Ymx; Y++) {
				TTile Item;
				synchronized (this) {
					Item = TileIndex.GetItem(X,Y);
				}
				if ((Item == null) || (Compilation.flHistoryEnabled && (Item.Timestamp > Compilation.HistoryTime()))) {
			        synchronized (LevelFolder) {
				        File TF;
				        if (Compilation.flHistoryEnabled) {
				        	String TileHistoryFolder = LevelFolder+"/"+TTile.TileHistoryFolderName(X,Y);
				        	TF = TTileHistoryFolder.GetFileToTime(TileHistoryFolder,Compilation.HistoryTime());
				        }
				        else
				        	TF = new File(LevelFolder+"/"+TTile.TileFileName(X,Y));
				        //.
				        if ((TF != null) && TF.exists()) {
				        	double Timestamp;
					        if (Compilation.flHistoryEnabled) 
					        	Timestamp = TTile.TileHistoryFolderExtractTileFileNameTimestamp(TF.getName());
					        else {
					        	long FTS = TF.lastModified();
					        	Timestamp = (FTS+1000.0/*file timestamp round error*/)/TimestampToFileTimestamp;
					        }
					    	Bitmap BMP = null;
					    	int DataSize = (int)TF.length();
					    	if (DataSize > 0) {
						    	FileInputStream FIS = new FileInputStream(TF);
						     	try {
									Rect rect = new Rect();
									BMP = BitmapFactory.decodeFileDescriptor(FIS.getFD(),rect,TBitmapDecodingOptions.GetBitmapFactoryOptions());
						    	}
								finally {
									FIS.close(); 
								}
					    	}
							TTile RestoreItem = new TTile(X,Y, Timestamp,BMP,TTile.Data_IsTransparent(BMP,DataSize));
				        	synchronized (this) {
				    			if (TileIndex != null)
				    				TileIndex.SetItem(X,Y, RestoreItem);
							}
							Count++;
							//.
							if (TileLimit != null) {
								TileLimit.Value--;
								if (TileLimit.Value <= 0)
									return (Count == Size); //. => 
							}
				        }
					}
				}
		        else
		        	Count++;
				//.
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
			}
			if (Updater != null) 
				Updater.Update();
		}
		return (Count == Size);
	}
	
	private void InputStream_ReadData(InputStream in, byte[] Data, int DataSize) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception(Compilation.Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
        }
	}

	private boolean HttpServer_RemoveTilesByTimestampsFromServer(int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller) throws Exception {
		boolean Result = false;
		//.
		String URL1 = Compilation.Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Compilation.Reflector.User.UserID);
		String URL2 = "TileServerTiles.dat";
		//. add command parameters
		if (Compilation.flHistoryEnabled)
			URL2 = URL2+"?"+"7"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(Xmn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymn)+","+Integer.toString(Ymx)+","+Double.toString(Compilation.HistoryTime())+",";
		else
			URL2 = URL2+"?"+"4"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(Xmn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymn)+","+Integer.toString(Ymx)+",";
		//. Visualization UserData
		String ExceptTilesString = "";
		if ((ExceptTiles != null) && (ExceptTiles.length > 0)) {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.URL_SAFE);
				try {
					B64S.write(ExceptTiles);
				}
				finally {
					B64S.close();
				}
				ExceptTilesString = new String(BOS.toByteArray());
			}
			finally {
				BOS.close();
			}
		}
		URL2 = URL2+ExceptTilesString;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Compilation.Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Compilation.Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
				//.
				int SummarySize = Connection.getContentLength();
				if (SummarySize == 0)
					return Result; //. ->
				byte[] Params = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/];
	            while (SummarySize > 0)
	            {
	            	InputStream_ReadData(in, Params,Params.length);
	            	int Idx = 0;
	            	int X = TDataConverter.ConvertBEByteArrayToInt32(Params,Idx); Idx += 8; //. SizeOf(Int64)
	            	int Y = TDataConverter.ConvertBEByteArrayToInt32(Params,Idx); Idx += 8; //. SizeOf(Int64)
	            	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(Params,Idx); Idx += 8; 
	            	//.
	            	TTile Tile;
	            	synchronized (this) {
						Tile = TileIndex.GetItem(X,Y);
	            	}
					if ((Tile != null) && (Tile.Timestamp < Timestamp)) {
						if (Compilation.flHistoryEnabled)
							RemoveTile(X,Y);
						else
							DeleteTile(X,Y);
						Result = true;
					}
	            	//.
					if ((Canceller != null) && Canceller.flCancel)
						throw new CancelException(); //. =>
	            	//.
	                SummarySize -= Params.length;
	            }
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	private boolean DataServer_RemoveTilesByTimestampsFromServer(int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller) throws Exception {
		boolean Result = false;
		TSpaceServersInfo.TInfo ServersInfo = Compilation.TileImagery.Reflector.ServersInfo.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Compilation.TileImagery.Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Compilation.TileImagery.Reflector.User.UserID, Compilation.TileImagery.Reflector.User.UserPassword);
		try {
			if (Compilation.flHistoryEnabled) {
				TTileImageryDataServer.TTileTimestampDescriptor[] Timestamps = IDS.GetTilesTimestampsByTimestamp(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, Xmn, Xmx, Ymn, Ymx, Compilation.HistoryTime(), ExceptTiles, Canceller);
				for (int I = 0; I < Timestamps.length; I++) {
					TTileImageryDataServer.TTileTimestampDescriptor Timestamp = Timestamps[I];
	            	TTile Tile;
	            	synchronized (this) {
						Tile = TileIndex.GetItem(Timestamp.X,Timestamp.Y);
	            	}
					if ((Tile != null) && (Tile.Timestamp < Timestamp.Timestamp)) {
						if (Compilation.flHistoryEnabled)
							RemoveTile(Timestamp.X,Timestamp.Y);
						else
							DeleteTile(Timestamp.X,Timestamp.Y);
						Result = true;
					}
	            	//.
					if ((Canceller != null) && Canceller.flCancel)
						throw new CancelException(); //. =>
				}
			}
			else {
				TTileImageryDataServer.TTileTimestampDescriptor[] Timestamps = IDS.GetTilesTimestamps(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, Xmn, Xmx, Ymn, Ymx, ExceptTiles, Canceller);
				for (int I = 0; I < Timestamps.length; I++) {
					TTileImageryDataServer.TTileTimestampDescriptor Timestamp = Timestamps[I];
	            	TTile Tile;
	            	synchronized (this) {
						Tile = TileIndex.GetItem(Timestamp.X,Timestamp.Y);
	            	}
					if ((Tile != null) && (Tile.Timestamp < Timestamp.Timestamp)) {
						if (Compilation.flHistoryEnabled)
							RemoveTile(Timestamp.X,Timestamp.Y);
						else
							DeleteTile(Timestamp.X,Timestamp.Y);
						Result = true;
					}
	            	//.
					if ((Canceller != null) && Canceller.flCancel)
						throw new CancelException(); //. =>
				}
			}
		}
		finally {
			IDS.Destroy();
		}
		return Result;
	}
	
	private void HttpServer_GetTilesFromServer(int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		String URL1 = Compilation.Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Compilation.Reflector.User.UserID);
		String URL2 = "TileServerTiles.dat";
		//. add command parameters
		if (Compilation.flHistoryEnabled)
			URL2 = URL2+"?"+"8"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(Xmn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymn)+","+Integer.toString(Ymx)+","+Double.toString(Compilation.HistoryTime())+",";
		else
			URL2 = URL2+"?"+"6"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(Xmn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymn)+","+Integer.toString(Ymx)+",";
		//. 
		String ExceptTilesString = "";
		if ((ExceptTiles != null) && (ExceptTiles.length > 0)) {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.URL_SAFE);
				try {
					B64S.write(ExceptTiles);
				}
				finally {
					B64S.close();
				}
				ExceptTilesString = new String(BOS.toByteArray());
			}
			finally {
				BOS.close();
			}
		}
		URL2 = URL2+ExceptTilesString;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Compilation.Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Compilation.Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				if ((Canceller != null) && Canceller.flCancel)
					throw new CancelException(); //. =>
				//.
				int SummarySize = Connection.getContentLength();
				if (SummarySize == 0)
					return; //. ->
				byte[] Params = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/+4/*SizeOf(TileSize)*/];
				byte[] TileData; 
	            while (SummarySize > 0)
	            {
	            	InputStream_ReadData(in, Params,Params.length);
	            	int Idx = 0;
	            	int X = TDataConverter.ConvertBEByteArrayToInt32(Params,Idx); Idx += 8; //. SizeOf(Int64)
	            	int Y = TDataConverter.ConvertBEByteArrayToInt32(Params,Idx); Idx += 8; //. SizeOf(Int64)
	            	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(Params,Idx); Idx += 8; 
	            	int TileSize = TDataConverter.ConvertBEByteArrayToInt32(Params,Idx); Idx += 4;
	            	if (TileSize > 0) {
	            		TileData = new byte[TileSize];
		            	InputStream_ReadData(in, TileData,TileData.length);
	            	}
	            	else
	            		TileData = null;
	            	//.
	            	AddTile(X,Y, Timestamp,TileData);
	            	//.
	            	if (Updater != null)
	            		Updater.Update();
	            	//.
					if ((Canceller != null) && Canceller.flCancel)
						throw new CancelException(); //. =>
	            	//.
	                SummarySize -= Params.length;
	                SummarySize -= TileSize;
	            }
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	private void DataServer_GetTilesFromServer(int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		TSpaceServersInfo.TInfo ServersInfo = Compilation.TileImagery.Reflector.ServersInfo.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Compilation.TileImagery.Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Compilation.TileImagery.Reflector.User.UserID, Compilation.TileImagery.Reflector.User.UserPassword);
		try {
			if (Compilation.flHistoryEnabled) {
				TTileImageryDataServer.TGetTilesByTimestampParams Params = IDS.GetTilesByTimestamp_Begin(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, Xmn, Xmx, Ymn, Ymx, Compilation.HistoryTime(), ExceptTiles, Canceller, Updater);
				try {
					for (int I = 0; I < Params.TilesCount; I++) {
						TTileImageryDataServer.TTileDescriptor TD = IDS.GetTilesByTimestamp_Read(Params);
		            	//.
		            	AddTile(TD.X,TD.Y, TD.Timestamp,TD.Data);
		            	//.
		            	if (Updater != null)
		            		Updater.Update();
		            	//.
						if ((Canceller != null) && Canceller.flCancel)
							throw new CancelException(); //. =>
					}
				}
				finally {
					IDS.GetTilesByTimestamp_End(Params);
				}
			}
			else {
				TTileImageryDataServer.TGetTilesParams Params = IDS.GetTiles_Begin(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, Xmn, Xmx, Ymn, Ymx, ExceptTiles, Canceller, Updater);
				try {
					for (int I = 0; I < Params.TilesCount; I++) {
						TTileImageryDataServer.TTileDescriptor TD = IDS.GetTiles_Read(Params);
		            	//.
		            	AddTile(TD.X,TD.Y, TD.Timestamp,TD.Data);
		            	//.
		            	if (Updater != null)
		            		Updater.Update();
		            	//.
						if ((Canceller != null) && Canceller.flCancel)
							throw new CancelException(); //. =>
					}
				}
				finally {
					IDS.GetTiles_End(Params);
				}
			}
		}
		finally {
			IDS.Destroy();
		}
	}
	
	public double HttpServer_SetTilesOnServer(int SecurityFileID, byte[] Tiles) throws Exception {
		String URL1 = Compilation.Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Compilation.Reflector.User.UserID);
		String URL2 = "TileServerTiles.dat";
		//. add command parameters
		URL2 = URL2+"?"+"10"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(SecurityFileID);
		//. 
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Compilation.Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		String URL = URL1+"/"+URL2+".dat";
		//.
		URL url = new URL(URL); 
        //.
		HttpURLConnection HttpConnection = (HttpURLConnection)url.openConnection();           
		try {
	        if (!(HttpConnection instanceof HttpURLConnection))                     
	            throw new IOException(Compilation.Reflector.getString(R.string.SNoHTTPConnection));
			HttpConnection.setDoOutput(true);
			HttpConnection.setDoInput(true);
			HttpConnection.setInstanceFollowRedirects(false); 
			HttpConnection.setRequestMethod("POST"); 
			HttpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			HttpConnection.setRequestProperty("Content-Length", "" + Integer.toString(Tiles.length));
			HttpConnection.setUseCaches(false);
			//. request
			DataOutputStream DOS = new DataOutputStream(HttpConnection.getOutputStream());
			try {
				DOS.write(Tiles);
				DOS.flush();
			}
			finally {
				DOS.close();			
			}
            //. response
            int response = HttpConnection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) { 
				String ErrorMessage = HttpConnection.getResponseMessage();
				byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
				ErrorMessage = new String(ErrorMessageBA,"windows-1251");
            	throw new IOException(Compilation.Reflector.getString(R.string.SServerError)+ErrorMessage); //. =>
            }
			InputStream in = HttpConnection.getInputStream();
			try {
				int Size = HttpConnection.getContentLength();
				if (Size != 8/*SizeOf(Timestamp)*/)
					throw new IOException(Compilation.Reflector.getString(R.string.SServerError)+HttpConnection.getResponseMessage());
				byte[] TimestampBA = new byte[Size];
            	InputStream_ReadData(in, TimestampBA,TimestampBA.length);
            	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
            	return Timestamp; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	public double DataServer_SetTilesOnServer(int SecurityFileID, byte[] Tiles) throws Exception {
		TSpaceServersInfo.TInfo ServersInfo = Compilation.TileImagery.Reflector.ServersInfo.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Compilation.TileImagery.Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Compilation.TileImagery.Reflector.User.UserID, Compilation.TileImagery.Reflector.User.UserPassword);
		try {
			return IDS.SetTiles(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, SecurityFileID, Tiles);
		}
		finally {
			IDS.Destroy();
		}
	}
	
	public double HttpServer_ReSetTilesOnServer(int SecurityFileID, byte[] Tiles) throws Exception {
		String URL1 = Compilation.Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Compilation.Reflector.User.UserID);
		String URL2 = "TileServerTiles.dat";
		//. add command parameters
		URL2 = URL2+"?"+"11"/*command version*/+","+Integer.toString(Compilation.Descriptor.SID)+","+Integer.toString(Compilation.Descriptor.PID)+","+Integer.toString(Compilation.Descriptor.CID)+","+Integer.toString(Level)+","+Integer.toString(SecurityFileID);
		//. 
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Compilation.Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		String URL = URL1+"/"+URL2+".dat";
		//.
		URL url = new URL(URL); 
        //.
		HttpURLConnection HttpConnection = (HttpURLConnection)url.openConnection();           
		try {
	        if (!(HttpConnection instanceof HttpURLConnection))                     
	            throw new IOException(Compilation.Reflector.getString(R.string.SNoHTTPConnection));
			HttpConnection.setDoOutput(true);
			HttpConnection.setDoInput(true);
			HttpConnection.setInstanceFollowRedirects(false); 
			HttpConnection.setRequestMethod("POST"); 
			HttpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			HttpConnection.setRequestProperty("Content-Length", "" + Integer.toString(Tiles.length));
			HttpConnection.setUseCaches(false);
			//. request
			DataOutputStream DOS = new DataOutputStream(HttpConnection.getOutputStream());
			try {
				DOS.write(Tiles);
				DOS.flush();
			}
			finally {
				DOS.close();			
			}
            //. response
            int response = HttpConnection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
				String ErrorMessage = HttpConnection.getResponseMessage();
				byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
				ErrorMessage = new String(ErrorMessageBA,"windows-1251");
            	throw new IOException(Compilation.Reflector.getString(R.string.SServerError)+ErrorMessage); //. =>
            }
			InputStream in = HttpConnection.getInputStream();
			try {
				int Size = HttpConnection.getContentLength();
				if (Size != 8/*SizeOf(Timestamp)*/)
					throw new IOException(Compilation.Reflector.getString(R.string.SServerError)+HttpConnection.getResponseMessage());
				byte[] TimestampBA = new byte[Size];
            	InputStream_ReadData(in, TimestampBA,TimestampBA.length);
            	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
            	return Timestamp; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	public double DataServer_ReSetTilesOnServer(int SecurityFileID, byte[] Tiles) throws Exception {
		if (!Compilation.flHistoryEnabled)
			throw new Exception(Compilation.Reflector.getString(R.string.STileCompilationIsNotHistoryEnabled)); //. =>
		TSpaceServersInfo.TInfo ServersInfo = Compilation.TileImagery.Reflector.ServersInfo.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Compilation.TileImagery.Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Compilation.TileImagery.Reflector.User.UserID, Compilation.TileImagery.Reflector.User.UserPassword);
		try {
			return IDS.ReSetTiles(Compilation.Descriptor.SID, Compilation.Descriptor.PID, Compilation.Descriptor.CID, Level, SecurityFileID, Tiles);
		}
		finally {
			IDS.Destroy();
		}
	}
	
	public void GetTiles(int Xmn, int Xmx, int Ymn, int Ymx, boolean flRemoveOldTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTilesData ExceptTiles = null;
		//. restore tiles from files into index
		if (RestoreTiles(Xmn,Xmx, Ymn,Ymx, null, Canceller,null)) {
			if (!flRemoveOldTiles)
				return; //. ->
			//. remove old out-of-date tiles
			switch (Compilation.TileImagery.ServerType) {
			
			case TTileImagery.SERVERTYPE_HTTPSERVER:
				if (!HttpServer_RemoveTilesByTimestampsFromServer(Xmn,Xmx, Ymn,Ymx, null, Canceller))
					return; //. ->
				break; //. >

			case TTileImagery.SERVERTYPE_DATASERVER:
				if (!DataServer_RemoveTilesByTimestampsFromServer(Xmn,Xmx, Ymn,Ymx, null, Canceller))
					return; //. ->
				break; //. >
			}
		}  
		else {
			//. remove old out-of-date tiles
			ExceptTiles = GetNotAvailableTiles(Xmn,Xmx, Ymn,Ymx);
			if ((!ExceptTiles.flAll) && flRemoveOldTiles)
				switch (Compilation.TileImagery.ServerType) {
				
				case TTileImagery.SERVERTYPE_HTTPSERVER:
					HttpServer_RemoveTilesByTimestampsFromServer(Xmn,Xmx, Ymn,Ymx, ExceptTiles.Data, Canceller);
					break; //. >

				case TTileImagery.SERVERTYPE_DATASERVER:
					DataServer_RemoveTilesByTimestampsFromServer(Xmn,Xmx, Ymn,Ymx, ExceptTiles.Data, Canceller);
					break; //. >
				}		
		}
		//. loading new tiles from server
		ExceptTiles = GetAvailableTiles(Xmn,Xmx, Ymn,Ymx);
		if (!ExceptTiles.flAll)
			switch (Compilation.TileImagery.ServerType) {
			
			case TTileImagery.SERVERTYPE_HTTPSERVER:
				HttpServer_GetTilesFromServer(Xmn,Xmx, Ymn,Ymx, ExceptTiles.Data, Canceller,Updater);
				break; //. >
				
			case TTileImagery.SERVERTYPE_DATASERVER:
				DataServer_GetTilesFromServer(Xmn,Xmx, Ymn,Ymx, ExceptTiles.Data, Canceller,Updater);
				break; //. >
			}
	}
	
	public double CommitModifiedTiles(int SecurityFileID, boolean flReset) throws Exception {
		double Result = Double.MIN_VALUE;
		ByteArrayOutputStream TilesStream = new ByteArrayOutputStream();
		try {
			ArrayList<TTile> ModifiedTiles = new ArrayList<TTile>(); 
			synchronized (this) {
				TTile Item = TileIndex.Items;
				while (Item != null) {
					if (Item.IsModified())  
						ModifiedTiles.add(Item);
					//.
					Item = Item.Next;
				}
			}
			for (int I = 0; I < ModifiedTiles.size(); I++) {
				TTile Item = ModifiedTiles.get(I);
				ByteArrayOutputStream DataStream = new ByteArrayOutputStream();
				try {
					if (Item.Data != null)
						Item.Data.compress(CompressFormat.PNG,100,DataStream);
					//.
					byte[] R64 = new byte[4];
	        		byte[] XBA = TDataConverter.ConvertInt32ToBEByteArray(Item.X);
	        		byte[] YBA = TDataConverter.ConvertInt32ToBEByteArray(Item.Y);
	        		TilesStream.write(XBA); TilesStream.write(R64); //. Int64
	        		TilesStream.write(YBA); TilesStream.write(R64); //. Int64
	        		int SegmentSize = DataStream.size();
	        		byte[] SegmentSizeBA = TDataConverter.ConvertInt32ToBEByteArray(SegmentSize);
	        		TilesStream.write(SegmentSizeBA);
	        		if (SegmentSize > 0) 
	        			DataStream.writeTo(TilesStream);
				}
				finally {
					DataStream.close();
				}
			}
			//. commiting on the server side
			if (ModifiedTiles.size() > 0)
				if (!flReset)
					switch (Compilation.TileImagery.ServerType) {
					
					case TTileImagery.SERVERTYPE_HTTPSERVER:
						Result = HttpServer_SetTilesOnServer(SecurityFileID,TilesStream.toByteArray());
						break; //. >
						
					case TTileImagery.SERVERTYPE_DATASERVER:
						Result = DataServer_SetTilesOnServer(SecurityFileID,TilesStream.toByteArray());
						break; //. >
					}
				else
					switch (Compilation.TileImagery.ServerType) {
					
					case TTileImagery.SERVERTYPE_HTTPSERVER:
						Result = HttpServer_ReSetTilesOnServer(SecurityFileID,TilesStream.toByteArray());
						break; //. >
						
					case TTileImagery.SERVERTYPE_DATASERVER:
						Result = DataServer_ReSetTilesOnServer(SecurityFileID,TilesStream.toByteArray());
						break; //. >
					}
			//. set modified tiles as unmodified
			for (int I = 0; I < ModifiedTiles.size(); I++) 
				ModifiedTiles.get(I).SetModified(false);
		}
		finally {
			TilesStream.close();			
		}
		return Result;
	}

	public boolean Container_IsFilled(TRWLevelTileContainer RWLevelTileContainer) {
		for (int X = RWLevelTileContainer.Xmn; X <= RWLevelTileContainer.Xmx; X++)
			for (int Y = RWLevelTileContainer.Ymn; Y <= RWLevelTileContainer.Ymx; Y++) {
				synchronized (this) {
					TTile Tile = TileIndex.GetItem(X,Y); 
					if (Tile == null) 
						return false; //. ->
				}
			}
		return true;
	}
	
	public int Container_DrawOnCanvas(TRWLevelTileContainer RWLevelTileContainer, Canvas canvas, TTimeLimit TimeLimit) throws TimeIsExpiredException {
		int Result = 0;
		int Div = (1 << Level);
		double SW = RWLevelTileContainer._Width/Div;
		double SH = RWLevelTileContainer.b/Div;
		double dX = (RWLevelTileContainer.Xc+RWLevelTileContainer.diffX1X0*((RWLevelTileContainer.Xmn+0.0)/Div)+RWLevelTileContainer.diffX3X0*((RWLevelTileContainer.Ymn+0.0)/Div))-RWLevelTileContainer.RW_Xmn;
		double dY = (RWLevelTileContainer.Yc+RWLevelTileContainer.diffY1Y0*((RWLevelTileContainer.Xmn+0.0)/Div)+RWLevelTileContainer.diffY3Y0*((RWLevelTileContainer.Ymn+0.0)/Div))-RWLevelTileContainer.RW_Ymn;
		//.
		Matrix CommonMatrix = new Matrix(); 
		CommonMatrix.postRotate((float)(RWLevelTileContainer.Rotation*180.0/Math.PI),0.0F,0.0F);
		CommonMatrix.postScale((float)(SW/TTile.TileSize),(float)(SH/TTile.TileSize),0.0F,0.0F);
		CommonMatrix.postTranslate((float)dX,(float)dY);
		Matrix Transformatrix = new Matrix();
		for (int X = RWLevelTileContainer.Xmn; X <= RWLevelTileContainer.Xmx; X++)
			for (int Y = RWLevelTileContainer.Ymn; Y <= RWLevelTileContainer.Ymx; Y++) {
				synchronized (this) {
					TTile Tile = TileIndex.GetItem(X,Y); 
					if (Tile != null) {
						if (!Tile.Data_flTransparent) {
							//. drawing tile ...
				    		Transformatrix.set(CommonMatrix);
				    		Transformatrix.preTranslate((float)((X-RWLevelTileContainer.Xmn)*TTile.TileSize),(float)((Y-RWLevelTileContainer.Ymn)*TTile.TileSize));
				    		//.
				    		canvas.save();
				    		try {
						    	canvas.concat(Transformatrix);
								canvas.drawBitmap(Tile.Data, 0,0, paint);
				    		}
				    		finally {
				    			canvas.restore();
				    		}
						}
						Result++;
					}
				}
				//.
				if (TimeLimit != null)
					TimeLimit.CheckTime();
			}
		return Result;
	}

	public void Container_PaintDrawings(TRWLevelTileContainer RWLevelTileContainer, List<TDrawing> Drawings, float pdX, float pdY) throws Exception {
		GetTiles(RWLevelTileContainer.Xmn,RWLevelTileContainer.Xmx, RWLevelTileContainer.Ymn,RWLevelTileContainer.Ymx, false, null, null);
		//.
		int Div = (1 << Level);
		double SW = RWLevelTileContainer._Width/Div;
		double SH = RWLevelTileContainer.b/Div;
		double dX = pdX+(RWLevelTileContainer.Xc+RWLevelTileContainer.diffX1X0*((RWLevelTileContainer.Xmn+0.0)/Div)+RWLevelTileContainer.diffX3X0*((RWLevelTileContainer.Ymn+0.0)/Div))-RWLevelTileContainer.RW_Xmn;
		double dY = pdY+(RWLevelTileContainer.Yc+RWLevelTileContainer.diffY1Y0*((RWLevelTileContainer.Xmn+0.0)/Div)+RWLevelTileContainer.diffY3Y0*((RWLevelTileContainer.Ymn+0.0)/Div))-RWLevelTileContainer.RW_Ymn;
		Matrix CommonMatrix = new Matrix(); 
		CommonMatrix.postRotate((float)(RWLevelTileContainer.Rotation*180.0/Math.PI),0.0F,0.0F);
		CommonMatrix.postScale((float)(SW/TTile.TileSize),(float)(SH/TTile.TileSize),0.0F,0.0F);
		CommonMatrix.postTranslate((float)dX,(float)dY);
		Matrix InverseTransformatrix = new Matrix();
		Matrix Transformatrix = new Matrix();
		for (int X = RWLevelTileContainer.Xmn; X <= RWLevelTileContainer.Xmx; X++)
			for (int Y = RWLevelTileContainer.Ymn; Y <= RWLevelTileContainer.Ymx; Y++) {
				synchronized (this) {
					TTile Tile = TileIndex.GetItem(X,Y); 
					if (Tile != null) {
			    		InverseTransformatrix.set(CommonMatrix);
			    		InverseTransformatrix.preTranslate((float)((X-RWLevelTileContainer.Xmn)*TTile.TileSize),(float)((Y-RWLevelTileContainer.Ymn)*TTile.TileSize));
			    		Transformatrix.reset();
			    		InverseTransformatrix.invert(Transformatrix);
			    		//.
			    		Tile.SetMutable(true);
			    		try {
				    		long TileDataHashCode = Tile.DataHashCode();
							Bitmap BMP = Tile.Data;
				    		Canvas canvas = new Canvas(BMP);
				    		canvas.concat(Transformatrix);
				    		//.
				    		for (int I = 0; I < Drawings.size(); I++) 
				    			Drawings.get(I).Paint(canvas);
				    		if (Tile.DataHashCode() != TileDataHashCode) {
				    			Tile.CheckTransparency();
				    			Tile.SetModified(true);
				    		}
			    		}
			    		finally {
				    		Tile.SetMutable(false);
			    		}
					}
				}
			}
	}

	public boolean Composition_DrawOnCanvas(TTilesCompositionLevel TilesCompositionLevel, TRWLevelTileContainer RWLevelTileContainer, Canvas canvas, TTimeLimit TimeLimit) throws TimeIsExpiredException {
		boolean Result = true;
		int Div = (1 << Level);
		double SW = RWLevelTileContainer._Width/Div;
		double SH = RWLevelTileContainer.b/Div;
		double dX = (RWLevelTileContainer.Xc+RWLevelTileContainer.diffX1X0*((TilesCompositionLevel.XIndexMin+0.0)/Div)+RWLevelTileContainer.diffX3X0*((TilesCompositionLevel.YIndexMin+0.0)/Div))-RWLevelTileContainer.RW_Xmn;
		double dY = (RWLevelTileContainer.Yc+RWLevelTileContainer.diffY1Y0*((TilesCompositionLevel.XIndexMin+0.0)/Div)+RWLevelTileContainer.diffY3Y0*((TilesCompositionLevel.YIndexMin+0.0)/Div))-RWLevelTileContainer.RW_Ymn;
		Matrix CommonMatrix = new Matrix(); 
		CommonMatrix.postRotate((float)(RWLevelTileContainer.Rotation*180.0/Math.PI),0.0F,0.0F);
		CommonMatrix.postScale((float)(SW/TTile.TileSize),(float)(SH/TTile.TileSize),0.0F,0.0F);
		CommonMatrix.postTranslate((float)dX,(float)dY);
		Matrix Transformatrix = new Matrix();
		for (int I = 0; I < TilesCompositionLevel.TilesMapSize; I++) {
			TTile Tile = TilesCompositionLevel.TilesMap[I];
			if (Tile != null) {
				synchronized (this) {
					Tile = TileIndex.GetItem(Tile.X,Tile.Y); //. reload tile if it is already removed from index 
					if (Tile != null) {
						if (!Tile.Data_flTransparent) {
							//. drawing tile ...
				    		Transformatrix.set(CommonMatrix);
				    		Transformatrix.preTranslate((float)((Tile.X-TilesCompositionLevel.XIndexMin)*TTile.TileSize),(float)((Tile.Y-TilesCompositionLevel.YIndexMin)*TTile.TileSize));
				    		//.
				    		canvas.save();
				    		try {
						    	canvas.concat(Transformatrix);
								canvas.drawBitmap(Tile.Data, 0,0, paint);
				    		}
				    		finally {
				    			canvas.restore();
				    		}
						}
					}
					else
						Result = false;
				}
				//.
				if (TimeLimit != null)
					TimeLimit.CheckTime();
			}
		}
		return Result;
	}
}
