package com.geoscope.Classes.Data.Stream.Channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Containers.TDataConverter;

public class TChannelIDs {
	
	public ArrayList<Integer> Items = new ArrayList<Integer>();
	
	public TChannelIDs() {
	}

	public TChannelIDs(byte[] BA) throws IOException {
		FromByteArray(BA);
	}
	
	public void AddID(int ID) {
		Items.add(ID);
	}
	
	public int Count() {
		return Items.size();
	}
	
	public boolean IDExists(int ID) {
		for (int I = 0; I < Items.size(); I++) 
			if (Items.get(I) == ID)
				return true; //. ->
		return false;
	}
	
	public void FromByteArray(byte[] BA) throws IOException {
		Items.clear();
		int ItemSize = 4; //. SizeOf(Int32)
		int Cnt = (BA.length/ItemSize);
		int Idx = 0;
		for (int I = 0; I < Cnt; I++) {
			int ID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += ItemSize;
			Items.add(ID);
		}
	}
	
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream OS = new ByteArrayOutputStream();
	    try {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
    			byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(Items.get(I));
    			OS.write(BA);
    		}
    		return OS.toByteArray(); //. ->
	    }
	    finally {
	    	OS.close();	    	
	    }
	}

	public int[] ToIDArray() throws IOException {
		int Cnt = Items.size();
		int[] Result = new int[Cnt];
		for (int I = 0; I < Cnt; I++)
			Result[I] = Items.get(I);
		return Result;
	}
}
