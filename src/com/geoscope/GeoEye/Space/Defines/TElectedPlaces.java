package com.geoscope.GeoEye.Space.Defines;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoEye.TReflector;

public class TElectedPlaces {

	public static final String ElectedPlaceFileName = TReflector.ProfileFolder+"/"+"ElectedPlaces.dat";
	public static final int ElectedPlaceFileVersion = 2;
	
	public ArrayList<TElectedPlace> Items = new ArrayList<TElectedPlace>();

	public TElectedPlaces() throws IOException {
		Load();
	}
	
	public void Load() throws IOException {
		File F = new File(ElectedPlaceFileName);
		Items.clear();
		if (F.exists()) {
	    	long FileSize = F.length();
	    	FileInputStream FIS = new FileInputStream(ElectedPlaceFileName);
	    	try {
        		byte[] BA = new byte[(int)FileSize];
    			FIS.read(BA);
    			int Idx = 0;
    			int Version = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
    			switch (Version) {
    			case 0:
    			case 1:
        			int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
        			for (int I = 0; I < ItemsCount; I++) {
        				TElectedPlace Item = new TElectedPlace();
        				Idx = Item.FromByteArray(BA,Idx);
        				//.
        				Items.add(Item);
        			}
    				break; //. >
    				
    			case 2:
        			ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
        			for (int I = 0; I < ItemsCount; I++) {
        				TElectedPlace Item = new TElectedPlace();
        				Idx = Item.FromByteArrayV1(BA,Idx);
        				//.
        				Items.add(Item);
        			}
    				break; //. >
    				
    			default:
    				throw new IOException("неизвестная версия данных, версия: "+Integer.toString(Version)); //. =>
    			}
	    	}
	    	finally {
	    		FIS.close();
	    	}
		}	    	
	}

	public void Save() throws IOException {
		if (Items != null)
		{
			FileOutputStream FOS = new FileOutputStream(ElectedPlaceFileName);
            try
            {
            	byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(ElectedPlaceFileVersion);
				FOS.write(BA);
				int ItemsCount = Items.size();
				BA = TDataConverter.ConvertInt32ToBEByteArray(ItemsCount);
				FOS.write(BA);
				for (int I = 0; I < ItemsCount; I++) {
					BA = Items.get(I).ToByteArrayV1();
					FOS.write(BA);
				}
            }
            finally
            {
            	FOS.close();
            }
		}
		else {
			File F = new File(ElectedPlaceFileName);
			F.delete();
		}
	}
	
	public void AddPlace(TElectedPlace Place) throws IOException {
		Items.add(Place);
		//.
		Save();
	}
	
	public void RemovePlace(int Index) {
		Items.remove(Index);
	}
}
