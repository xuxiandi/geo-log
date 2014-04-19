/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 * @author ALXPONOM
 */
public class TTaskDataValue extends TComponentValue
{
	private double Timestamp;
	//.
	private int 	ObjectID;
	private String	MeasurementID;
    private byte[] 	Data;

    public TTaskDataValue() {
    }
    
    public TTaskDataValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TTaskDataValue(double pTimestamp, byte[] pData) {
    	Timestamp = pTimestamp;
        Data = pData;
        //.
        flSet = true;
    }
    
    public synchronized int GetObjectID() {
    	return ObjectID;
    }
    
    public synchronized String GetObjectMeasurementID() {
    	return Integer.toString(ObjectID)+"/"+MeasurementID;
    }
    
    public synchronized void SetProperties(int pObjectID, String pMeasurementID) {
    	ObjectID = pObjectID;
    	MeasurementID = pMeasurementID;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        TTaskDataValue Src = (TTaskDataValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Data = Src.Data;
        //.
        super.Assign(pValue);
    }
    
    @Override
    public synchronized TComponentValue getValue() {
        return new TTaskDataValue(Timestamp,Data);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue) {
        TTaskDataValue MapPOI = (TTaskDataValue)AValue.getValue();
        return ((Timestamp == MapPOI.Timestamp) && (Data == MapPOI.Data));
    }
    
    public synchronized void setValues(double pTimestamp, byte[] pData) {
    	Timestamp = pTimestamp;
        Data = pData;
        //.
        flSet = true;
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int MeasurementIDSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        byte[] MeasurementIDData = new byte[MeasurementIDSize];
        if (MeasurementIDSize > 0) {
        	System.arraycopy(BA,Idx.Value, MeasurementIDData,0, MeasurementIDSize); Idx.Value += MeasurementIDSize;
        	MeasurementID = new String(MeasurementIDData, 0,MeasurementIDData.length, "windows-1251");
        }
        else
        	MeasurementID = "";
        //.
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        //.
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        Data = new byte[DataSize];
        System.arraycopy(BA,Idx.Value, Data,0, DataSize); Idx.Value += DataSize;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException
    {
        int DataSize = 0;
        if (Data != null)
            DataSize = Data.length;
        byte[] Result = new byte[8/*SizeOf(Timestamp)*/+4/*SizeOf(DataSize)*/+DataSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (DataSize > 0) {            
            System.arraycopy(Data,0,Result,Idx,DataSize); Idx+=DataSize;
        }
        return Result;
    }

    @Override
    public int ByteArraySize() {
        int DataSize = 0;
        if (Data != null)
            DataSize = Data.length;
        return (4/*SizeOf(DataSize)*/+DataSize);
    }
}
