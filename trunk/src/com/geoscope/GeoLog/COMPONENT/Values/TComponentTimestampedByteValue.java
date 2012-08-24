/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TComponentTimestampedByteValue extends TComponentValue
{
    public static final int ValueSize = 9;
    
    public double 	Timestamp;
    public byte 	Value;
    
    public TComponentTimestampedByteValue()
    {
    	Timestamp = 0.0;
        Value = 0;
    }
    
    public TComponentTimestampedByteValue(double pTimestamp, byte pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedByteValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, byte pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double GetTimestamp()
    {
        return Timestamp;
    }
       
    public synchronized byte GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedByteValue Src = (TComponentTimestampedByteValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedByteValue(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedByteValue V = (TComponentTimestampedByteValue)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
        Value = BA[Idx.Value]; Idx.Value+=1;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	byte[] Result = new byte[ValueSize];
    	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,0, BA.length); 
        Result[8] = Value;
        return Result;
    }
}
