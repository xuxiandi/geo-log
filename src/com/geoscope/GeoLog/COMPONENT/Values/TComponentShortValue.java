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

public class TComponentShortValue extends TComponentValue
{
    public static final int ValueSize = 2;
    
    public short Value;
    
    public TComponentShortValue()
    {
        Value = 0;
    }
    
    public TComponentShortValue(short pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentShortValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(short pValue)
    {
        Value = pValue;
        flSet = true;
    }
       
    public synchronized short GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        Value = ((TComponentShortValue)pValue.getValue()).Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentShortValue(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentShortValue V = (TComponentShortValue)AValue.getValue();
        return (V.Value == Value);
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        return TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Value);
    }
}
    
