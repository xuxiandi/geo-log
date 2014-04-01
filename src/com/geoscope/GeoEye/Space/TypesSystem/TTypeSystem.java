package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.TFileSystem;

public class TTypeSystem {

	public static final long Context_Item_DefaultLifeTime = (1000*3600*24)*30; //. days
	
	public static class TContextCache {
		
		public static String 	FileName = "Data.dat";
		public static int		FileVersion = 1;
		
		private TTypeSystem TypeSystem;
		//.
		protected Hashtable<Integer, TComponentData> ItemsTable;
		//.
		private boolean flChanged = false;
		
		public TContextCache(TTypeSystem pTypeSystem) throws IOException {
			TypeSystem = pTypeSystem;
			//.
			ItemsTable = new Hashtable<Integer, TComponentData>();
			//.
			Load();
		}
		
		public void Destroy() throws IOException {
			Save();
		}
		
		public synchronized void Clear() {
			ItemsTable.clear();
			//.
			flChanged = true;		
		}
		
		public synchronized void ClearToTime(double ToTime) {
			TComponentData[] Items = (TComponentData[])ItemsTable.values().toArray();
			for (int I = 0; I < Items.length; I++)
				if (Items[I].Timestamp < ToTime)
					ItemsTable.remove(Items[I].ID);
			//.
			flChanged = true;		
		}
		
		public synchronized void Add(TComponentData Item) {
			ItemsTable.put(Item.ID, Item);
			//.
			flChanged = true;		
		}
		
		public synchronized void Remove(TComponentData Item) {
			ItemsTable.remove(Item.ID);
			//.
			flChanged = true;		
		}
		
		public synchronized TComponentData GetItem(int pID) {
			return ItemsTable.get(pID);
		}
		
		public void Load() throws IOException {
			LoadFromFile();
			//.
			flChanged = false;
		}
		
		public void Save() throws IOException {
			if (flChanged) {
				SaveToFile();
				//.
				flChanged = false;			
			}
		}

		protected TComponentData CreateItem() {
			return null; 
		}
		
		public synchronized void LoadFromFile() throws IOException {
			Clear();
			String FN = TypeSystem.Context_GetFolder()+"/"+FileName;
			File F = new File(FN);
			if (F.exists()) { 
		    	FileInputStream FIS = new FileInputStream(FN);
		    	try {
	    				byte[] VersionBA = new byte[4];
	    				FIS.read(VersionBA);
	    				int Version = TDataConverter.ConvertBEByteArrayToInt32(VersionBA, 0);
	    				//.
	    				switch (Version) {
	    				
	    				case 1:
			    			byte[] ItemsCountBA = new byte[4];
			    			FIS.read(ItemsCountBA);
				    		int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(ItemsCountBA, 0);
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
								TComponentData Item = CreateItem();
								Item.FromByteArrayV1(ItemData,0);
								//.
								ItemsTable.put(Item.ID, Item);
			        		}
	    					break; //. >
	    				
	    				default:
	    					throw new IOException("unknown file version"); //. =>
	    				}
		    	}
				finally
				{
					FIS.close(); 
				}
			}
		}
		
		public synchronized void SaveToFile() throws IOException {
			String FN = TypeSystem.Context_GetFolder()+"/"+FileName;
			FileOutputStream FOS = new FileOutputStream(FN);
	        try
	        {
	        	byte[] VersionBA = TDataConverter.ConvertInt32ToBEByteArray(FileVersion);
	        	FOS.write(VersionBA);
				Collection<TComponentData> Items = ItemsTable.values();
				int IC = Items.size();
	        	byte[] ItemsCountBA = TDataConverter.ConvertInt32ToBEByteArray(IC);
	        	FOS.write(ItemsCountBA);
				Iterator<TComponentData> Item = Items.iterator();
				while (Item.hasNext()) {
					TComponentData CD = Item.next();
	        		byte[] BA = CD.ToByteArrayV1();
	        		//.
	        		int ItemDataSize = BA.length;
	        		byte[] ItemDataSizeBA = TDataConverter.ConvertInt32ToBEByteArray(ItemDataSize);
	    			FOS.write(ItemDataSizeBA);
	    			FOS.write(BA);
				}			
	        }
	        finally
	        {
	        	FOS.close();
	        }
		}
	}
	
	public TTypesSystem TypesSystem;
	//.
	public TContextCache ContextCache = null;
	
	public TTypeSystem(TTypesSystem pTypesSystem) throws Exception {
		TypesSystem = pTypesSystem;
		//.
		TypesSystem.Items.add(this);
	}
	
	public void Destroy() throws Exception {
		if (ContextCache != null) {
			ContextCache.Destroy();
			ContextCache = null;
		}
		if (TypesSystem != null)
			TypesSystem.Items.remove(this);
	}
	
	public TTypeFunctionality TTypeFunctionality_Create(TGeoScopeServer pServer) {
		return null; 
	}
	
	public TTypeFunctionality TTypeFunctionality_Create() {
		return TTypeFunctionality_Create(null); 
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(TGeoScopeServer pServer, int idComponent) {
		TTypeFunctionality TypeFunctionality = TTypeFunctionality_Create(pServer);
		return TypeFunctionality.TComponentFunctionality_Create(idComponent);
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(int idComponent) {
		TTypeFunctionality TypeFunctionality = TTypeFunctionality_Create();
		return TypeFunctionality.TComponentFunctionality_Create(idComponent);
	}
	
	public String Context_GetFolder() {
		return "";
	}
	
	public void Context_Clear() {
		if (ContextCache != null)
			ContextCache.Clear();
		TFileSystem.EmptyFolder(new File(Context_GetFolder()));
	}
	
	public void Context_ClearItems(long ToTime) {
		TFileSystem.RemoveFolderFiles(new File(Context_GetFolder()), ToTime,null);
	}	

	public void Context_ClearOldItems() {
		Context_ClearItems(Context_Item_DefaultLifeTime);
	}	
}
