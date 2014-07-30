/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TMapPOIDataFileValue extends TComponentValue
{

	public static Object Lock = new Object();
	//.
	public String FileName;

    public TMapPOIDataFileValue()
    {
    }
    
    public TMapPOIDataFileValue(double pTimestamp, String pFileName) throws IOException
    {
    	FileName = pFileName;
    	//.
    	File F = new File(FileName);
    	byte[] _Data = FileName.getBytes("windows-1251");
    	setValues(pTimestamp,F.getName(),_Data);
    }
    
    private TMapPOIDataFileValue(String pDataFileName, String pFileName) {
    	DataFileName = pDataFileName;
    	FileName = pFileName;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
    	TMapPOIDataFileValue Src = (TMapPOIDataFileValue)pValue.getValue();
        DataFileName = Src.DataFileName;
        FileName = Src.FileName;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIDataFileValue(DataFileName,FileName);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIDataFileValue MapPOIDataFile = (TMapPOIDataFileValue)AValue.getValue();
        return (DataFileName.equals(MapPOIDataFile.DataFileName));
    }
    
    private synchronized void setValues(double pTimestamp, String pFileName, byte[] pData) throws IOException
    {
    	synchronized (Lock) {
        	DataFileName_Assign(TConnectorModule.OutgoingSetOperationsQueueDataFolderName(), pTimestamp);
        	FileOutputStream FOS = new FileOutputStream(DataFileName);
        	try {
            	int Size = 8;
            	int FNS = pFileName.length();
            	if (FNS > 255)
            		FNS = 255;
            	Size += 1/*SizeOf(FileNameSize)*/+FNS;
                if (pData != null)
                    Size += pData.length;
                //.
                byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Size);
                FOS.write(BA);
                //.
                BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(pTimestamp);
                FOS.write(BA);
                //.
                BA = new byte[1];
                BA[0] = (byte)FNS; 
                FOS.write(BA);
                if (FNS > 0) {
                	BA = pFileName.getBytes("windows-1251");
                    FOS.write(BA);
                }
                //.
                if (pData != null)             
                    FOS.write(pData);
        	}
        	finally {
        		FOS.close();
        	}
		}
        //.
        flSet = true;
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	DataFileName_FromByteArray(BA, Idx);
    	byte[] DFBA = DataFile_ToByteArray();
    	if (DFBA != null) {
        	int DFBA_Idx = 0;
        	DFBA_Idx += 4/*SizeOf(Size)*/;
        	DFBA_Idx += 8/*SizeOf(Timestamp)*/;
        	int FNS = (int)(DFBA[DFBA_Idx] & 0xFF); DFBA_Idx++;
        	DFBA_Idx += FNS/*SizeOf(FileName)*/;
        	FileName = new String(DFBA,DFBA_Idx,DFBA.length-DFBA_Idx,"windows-1251");
    	}
    	else
    		FileName = null;
        //.
        flSet = true;
    }

    public byte[] AllData_ToByteArray() throws IOException
    {
    	synchronized (Lock) {
			return DataFile_ToByteArray(); 
		}
    }

    public byte[] BriefData_ToByteArray() throws IOException
    {
    	byte[] Result = null;
    	synchronized (Lock) {
        	byte[] DFBA = DataFile_ToByteArray();
        	if (DFBA != null) {
            	int ResultSize = 4/*SizeOf(Size)*/+8/*SizeOf(Timestamp)*/;
            	int FNS = (int)(DFBA[ResultSize] & 0xFF); ResultSize++;
            	ResultSize += FNS/*SizeOf(FileName)*/;
            	//.
            	Result = new byte[ResultSize];
            	int Idx = 0;
            	int DataSize = ResultSize-4/*SizeOf(Size)*/;
            	byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
            	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            	System.arraycopy(DFBA,Idx, Result,Idx, DataSize); 
        	}
		}
    	return Result;
    }

    @Override
    public synchronized byte[] ToByteArray() throws IOException
    {
    	return BriefData_ToByteArray();
    }

    public int AllData_ByteArraySize()
    {
    	synchronized (Lock) {
			return DataFile_Size(); 
		}
    }
    
    public int BriefData_ByteArraySize() throws IOException
    {
		int Result = 0;
    	synchronized (Lock) {
        	byte[] DFBA = DataFile_ToByteArray();
        	if (DFBA != null) {
        		Result = 4/*SizeOf(Size)*/+8/*SizeOf(Timestamp)*/;
            	int FNS = (int)(DFBA[Result] & 0xFF); Result++;
            	Result += FNS; 
        	}
		}
    	return Result;
    }
    
    @Override
    public int ByteArraySize() throws IOException
    {
    	return BriefData_ByteArraySize();
    }
}
