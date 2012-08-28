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

public class TComponentTimestampedInt32Value extends TComponentTimestampedValue
{
    public static final int ValueSize = 12;
    
    public int		Value;
    
	public TComponentTimestampedInt32Value(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentTimestampedInt32Value()
    {
    	Timestamp = 0.0;
        Value = 0;
    }
    
    public TComponentTimestampedInt32Value(double pTimestamp, int pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedInt32Value(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, int pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized int GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedInt32Value Src = (TComponentTimestampedInt32Value)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedInt32Value(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedInt32Value V = (TComponentTimestampedInt32Value)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
    	Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA, Idx.Value); Idx.Value+=4;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	byte[] Result = new byte[ValueSize];
    	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,0, BA.length); 
    	BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Value);
    	System.arraycopy(BA,0, Result,8, BA.length); 
        return Result;
    }
}
