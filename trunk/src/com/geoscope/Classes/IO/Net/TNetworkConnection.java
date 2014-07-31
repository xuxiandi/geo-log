package com.geoscope.Classes.IO.Net;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;

public class TNetworkConnection {

    public static int InputStream_ReadData(InputStream Connection, byte[] Data, int DataSize) throws IOException {
        int SummarySize = 0;
        int ReadSize;
        int Size;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = Connection.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	return Size; //. ->
            SummarySize += Size;
        }
        return SummarySize;
    }

	public static void InputStream_ReadData(InputStream in, byte[] Data, int DataSize, Context context) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
        }
	}
	
	public static void InputStream_ReadData(InputStream in, byte[] Data, int DataSize, TCanceller Canceller, Context context) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
            //.
			if (Canceller != null)
				Canceller.Check();
        }
	}
}
