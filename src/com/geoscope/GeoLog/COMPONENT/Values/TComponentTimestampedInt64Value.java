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

public class TComponentTimestampedInt64Value extends TComponentTimestampedValue
{
    public static final int ValueSize = 16;
    
    public long Value;
    
	public TComponentTimestampedInt64Value(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentTimestampedInt64Value()
    {
    	Timestamp = 0.0;
        Value = 0;
    }
    
    public TComponentTimestampedInt64Value(double pTimestamp, long pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedInt64Value(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, long pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedInt64Value Src = (TComponentTimestampedInt64Value)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedInt64Value(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedInt64Value V = (TComponentTimestampedInt64Value)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
    	Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt64(BA, Idx.Value); Idx.Value+=8;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	byte[] Result = new byte[ValueSize];
    	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,0, BA.length); 
    	BA = TGeographServerServiceOperation.ConvertInt64ToBEByteArray(Value);
    	System.arraycopy(BA,0, Result,8, BA.length); 
        return Result;
    }
}
