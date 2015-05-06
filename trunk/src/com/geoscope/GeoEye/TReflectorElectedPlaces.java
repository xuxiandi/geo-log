package com.geoscope.GeoEye;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TLocation;

public class TReflectorElectedPlaces {

	public static final String ElectedPlaceFileName = TReflectorComponent.ProfileFolder()+"/"+"ElectedPlaces.dat";
	public static final int ElectedPlaceFileVersion = 3;
	
	public ArrayList<TLocation> Items = new ArrayList<TLocation>();

	public TReflectorElectedPlaces() throws IOException {
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
    			int Version = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
    			switch (Version) {
    			case 0:
    			case 1:
        			int ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
        			for (int I = 0; I < ItemsCount; I++) {
        				TLocation Item = new TLocation();
        				Idx = Item.FromByteArray(BA,Idx);
        				//.
        				Items.add(Item);
        			}
    				break; //. >
    				
    			case 3:
        			ItemsCount = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
        			for (int I = 0; I < ItemsCount; I++) {
        				TLocation Item = new TLocation();
        				Idx = Item.FromByteArrayV2(BA,Idx);
        				//.
        				Items.add(Item);
        			}
    				break; //. >
    				
    			/*///- default:
    				throw new IOException("неизвестная версия данных, версия: "+Integer.toString(Version)); //. =>*/
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
            	byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(ElectedPlaceFileVersion);
				FOS.write(BA);
				int ItemsCount = Items.size();
				BA = TDataConverter.ConvertInt32ToLEByteArray(ItemsCount);
				FOS.write(BA);
				for (int I = 0; I < ItemsCount; I++) {
					BA = Items.get(I).ToByteArray();
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
	
	public void AddPlace(TLocation Place) throws IOException {
		Items.add(Place);
		//.
		Save();
	}
	
	public void RemovePlace(int Index) {
		Items.remove(Index);
	}
}
