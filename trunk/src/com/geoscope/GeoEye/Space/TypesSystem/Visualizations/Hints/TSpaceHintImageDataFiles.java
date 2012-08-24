package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.jcraft.jzlib.ZInputStream;

public class TSpaceHintImageDataFiles {

	public static final String DataFilesFileName = TSpaceHints.HintsFolder+"/"+"HintImageDataFiles.dat";
	public static final int MaxHintsCount = 100;
	
	public TSpaceHints SpaceHints;
	private TSpaceHintImageDataFile 					Items;
	private int 										ItemsCount;
	public Hashtable<Integer, TSpaceHintImageDataFile> 	ItemsTable;
	private byte[] Buffer = new byte[8192];
	
	public TSpaceHintImageDataFiles(TSpaceHints pSpaceHints) {
		SpaceHints = pSpaceHints;
		//.
		Items = null;
		ItemsCount = 0;
		ItemsTable = new Hashtable<Integer, TSpaceHintImageDataFile>();
	}
	
	public void Destroy() {
    	TSpaceHintImageDataFile Item = Items;
    	while (Item != null) {
    		Item.Destroy();
			//.
			Item = Item.Next;
    	}
		Items = null;
		ItemsCount = 0;
		ItemsTable = null;
	}
	
	public void Load() throws IOException {
		Items = null;
		ItemsCount = 0;
		TSpaceHintImageDataFile LastItem = null;
		//.
		File F = new File(DataFilesFileName);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(DataFilesFileName);
	    	try {
	    			byte[] ItemsCountBA = new byte[4];
	    			FIS.read(ItemsCountBA);
		    		ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(ItemsCountBA, 0);
		    		//.
		    		byte[] ItemData = new byte[10*1024]; //. max item data size
	        		for (int I = 0; I < ItemsCount; I++) {
	            		byte[] ItemDataSizeBA = new byte[4];
						FIS.read(ItemDataSizeBA);
						int ItemDataSize = TDataConverter.ConvertBEByteArrayToInt32(ItemDataSizeBA, 0);
						if (ItemDataSize > 0) {
							if (ItemDataSize > ItemData.length)
								ItemData = new byte[ItemDataSize];
							FIS.read(ItemData, 0,ItemDataSize);
						}
						//.
						int Idx = 0;
			    		int ItemID = TDataConverter.ConvertBEByteArrayToInt32(ItemData, Idx); Idx += 8; //. Int64
			    		TSpaceHintImageDataFile NewItem = new TSpaceHintImageDataFile(ItemID);
						if (ItemDataSize > 0) 
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
		FileOutputStream FOS = new FileOutputStream(DataFilesFileName);
        try
        {
        	byte[] ItemsCountBA = TDataConverter.ConvertInt32ToBEByteArray(ItemsCount);
        	FOS.write(ItemsCountBA);
        	TSpaceHintImageDataFile Item = Items;
        	while (Item != null) {
        		byte[] BA = Item.ToByteArray();
        		int ItemDataSize = BA.length;
        		byte[] ItemDataSizeBA = TDataConverter.ConvertInt32ToBEByteArray(ItemDataSize);
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
	
	public synchronized void Clear() {
    	TSpaceHintImageDataFile Item = Items;
    	while (Item != null) {
    		Item.Destroy();
			//.
			Item = Item.Next;
    	}
		Items = null;
		ItemsCount = 0;
		ItemsTable = new Hashtable<Integer, TSpaceHintImageDataFile>();
	}
	
	private synchronized void FromByteArray(byte[] BA) throws IOException {
		int Idx = 0;
    	int _ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
    	for (int I = 0; I < _ItemsCount; I++) {
    		int ItemID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
    		TSpaceHintImageDataFile Item = ItemsTable.get(ItemID);
    		if (Item == null) { 
    			Item = new TSpaceHintImageDataFile(ItemID);
    			//.
    			Item.Next = Items;
    			Items = Item;
    			ItemsCount++;
    			//.
    			ItemsTable.put(ItemID, Item);
    		}
    		Idx = Item.FromByteArray(BA, Idx);
    	}
	}
	
	public synchronized void FromZippedByteArray(byte[] BA) throws IOException {
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
					FromByteArray(BOS.toByteArray());
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
	
	public synchronized TSpaceHintImageDataFile GetItem(int ItemID) {
		TSpaceHintImageDataFile Item = ItemsTable.get(ItemID);
		if (Item == null) { 
			Item = new TSpaceHintImageDataFile(ItemID);
			//.
			Item.Next = Items;
			Items = Item;
			ItemsCount++;
			//.
			ItemsTable.put(ItemID, Item);
		}
		return Item;
	}
	
	public synchronized void RemoveOldItems() {
    	if (ItemsCount < (1.1*MaxHintsCount))
    		return; //. ->
		int Cnt = MaxHintsCount;
		TSpaceHintImageDataFile Item = Items;
		while (Item != null) {
			Cnt--;
			Item = Item.Next;
			//.
			if (Cnt == 0) {
				if (Item == null)
					return; //. ->
				TSpaceHintImageDataFile RemoveItem = Item.Next;
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
	
	
}
