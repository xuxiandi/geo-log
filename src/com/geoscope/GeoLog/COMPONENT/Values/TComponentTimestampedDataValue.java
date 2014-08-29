/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentTimestampedValue;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TComponentTimestampedDataValue extends TComponentTimestampedValue
{

    public byte[] Value = null;

	public TComponentTimestampedDataValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
		flDirectAccess = true;
	}

    public TComponentTimestampedDataValue()
    {
    }
    
    public TComponentTimestampedDataValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TComponentTimestampedDataValue(double pTimestamp, byte[] pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        //.
        flSet = true;
    }
    
    public synchronized byte[] GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedDataValue Src = (TComponentTimestampedDataValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedDataValue(Timestamp,Value);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedDataValue DV = (TComponentTimestampedDataValue)AValue.getValue();
        return ((Timestamp == DV.Timestamp) && ((Value == DV.Value) || ((Value != null) && Value.equals(DV.Value))));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        int DS = DataSize-8;
        Value = new byte[DS];
        System.arraycopy(BA,Idx.Value, Value,0, DS); Idx.Value += DS;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
    	int DataSize = 8;
        int DS = 0;
        if (Value != null) 
        	DS = Value.length;
        DataSize += DS; 
        byte[] Result = new byte[4/*SizeOf(DataSize)*/+DataSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (DS > 0) {            
            System.arraycopy(Value,0,Result,Idx,DS); Idx+=DS;
        }
        return Result;
    }

    public int ByteArraySize()
    {
        int DS = 0;
        if (Value != null) 
        	DS = Value.length;
        return (4/*SizeOf(DataSize)*/+8/*SizeOf(Timestamp)*/+DS);
    }
}
