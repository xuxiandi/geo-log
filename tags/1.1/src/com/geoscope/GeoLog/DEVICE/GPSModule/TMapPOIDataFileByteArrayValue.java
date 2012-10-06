/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TMapPOIDataFileByteArrayValue extends TComponentValue
{

	public double Timestamp;
	public String FileName;
    public byte[] Data;

    public TMapPOIDataFileByteArrayValue()
    {
    }
    
    public TMapPOIDataFileByteArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TMapPOIDataFileByteArrayValue(double pTimestamp, String pFileName, byte[] pData)
    {
    	Timestamp = pTimestamp;
    	FileName = pFileName;
        Data = pData;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TMapPOIDataFileByteArrayValue Src = (TMapPOIDataFileByteArrayValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        FileName = Src.FileName;
        Data = Src.Data;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIDataFileByteArrayValue(Timestamp,FileName,Data);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIDataFileByteArrayValue MapPOIDataFile = (TMapPOIDataFileByteArrayValue)AValue.getValue();
        return ((Timestamp == MapPOIDataFile.Timestamp) && (FileName.equals(MapPOIDataFile.FileName)) && (Data == MapPOIDataFile.Data));
    }
    
    public synchronized void setValues(double pTimestamp, String pFileName, byte[] pData)
    {
    	Timestamp = pTimestamp;
    	FileName = pFileName;
        Data = pData;
        //.
        flSet = true;
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = 0.0;
    	FileName = "";
    	Data = null;
        int Size = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        if (Size > 0) {
        	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
            byte FNS = BA[Idx.Value]; Idx.Value++;
            if (FNS > 0) {
            	FileName = new String(BA, Idx.Value,FNS, "windows-1251");
            	Idx.Value+=FNS;
            }
            int DataSize = Size-8-(1+FNS);
            if (DataSize > 0) {
                Data = new byte[DataSize];
                System.arraycopy(BA,Idx.Value, Data,0, DataSize); Idx.Value += DataSize;
            }
        }
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	int Size = 8;
    	int FNS = FileName.length();
    	if (FNS > 255)
    		FNS = 255;
    	Size += 1/*SizeOf(FileNameSize)*/+FNS;
        if (Data != null)
            Size += Data.length;
        //.
        byte[] Result = new byte[4/*SizeOf(Size)*/+Size];
        //.
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Size);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        //.
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        //.
        Result[Idx] = (byte)FNS; Idx++;
        if (FNS > 0) {
        	BA = FileName.getBytes("windows-1251");
            System.arraycopy(BA,0,Result,Idx,FNS); Idx+=FNS;
        }
        //.
        if (Data != null) {            
            System.arraycopy(Data,0,Result,Idx,Data.length); Idx+=Data.length;
        }
        return Result;
    }

    @Override
    public int ByteArraySize()
    {
    	int Size = 8;
    	int FNS = FileName.length();
    	if (FNS > 255)
    		FNS = 255;
        Size += 1/*SizeOf(FileNameSize)*/+FNS;
        if (Data != null)
            Size += Data.length;
        return (4/*SizeOf(Size)*/+Size);
    }
}
