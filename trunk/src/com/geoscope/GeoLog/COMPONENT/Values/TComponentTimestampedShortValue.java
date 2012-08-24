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

public class TComponentTimestampedShortValue extends TComponentValue
{
    public static final int ValueSize = 10;
    
    public double 	Timestamp;
    public short 	Value;
    
    public TComponentTimestampedShortValue()
    {
    	Timestamp = 0.0;
        Value = 0;
    }
    
    public TComponentTimestampedShortValue(double pTimestamp, short pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedShortValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, short pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double GetTimestamp()
    {
        return Timestamp;
    }
       
    public synchronized short GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedShortValue Src = (TComponentTimestampedShortValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedShortValue(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedShortValue V = (TComponentTimestampedShortValue)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
    	Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	byte[] Result = new byte[ValueSize];
    	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,0, BA.length); 
    	BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Value);
    	System.arraycopy(BA,0, Result,8, BA.length); 
        return Result;
    }
}
