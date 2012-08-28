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

public class TComponentInt32Value extends TComponentValue
{
    public static final int ValueSize = 4;
    
    public int Value;
    
	public TComponentInt32Value(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentInt32Value()
    {
        Value = 0;
    }
    
    public TComponentInt32Value(int pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentInt32Value(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(int pValue)
    {
        Value = pValue;
        flSet = true;
    }
       
    public synchronized int GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        Value = ((TComponentInt32Value)pValue.getValue()).Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentInt32Value(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentInt32Value V = (TComponentInt32Value)AValue.getValue();
        return (V.Value == Value);
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        return TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Value);
    }
}
    
