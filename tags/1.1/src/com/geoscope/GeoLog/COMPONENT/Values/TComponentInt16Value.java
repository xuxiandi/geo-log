/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TComponentInt16Value extends TComponentValue
{
    public static final int ValueSize = 2;
    
    public short Value;
    
	public TComponentInt16Value(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentInt16Value()
    {
        Value = 0;
    }
    
    public TComponentInt16Value(short pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentInt16Value(byte[] BA, TIndex Idx) throws IOException, OperationException
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
        Value = ((TComponentInt16Value)pValue.getValue()).Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentInt16Value(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentInt16Value V = (TComponentInt16Value)AValue.getValue();
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
    
