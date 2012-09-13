/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TComponentByteValue extends TComponentValue
{
    public static final int ValueSize = 1;
    
    public byte Value;
    
	public TComponentByteValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentByteValue()
    {
        Value = 0;
    }
    
    public TComponentByteValue(byte pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentByteValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(byte pValue)
    {
        Value = pValue;
        flSet = true;
    }
       
    public synchronized byte GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        Value = ((TComponentByteValue)pValue.getValue()).Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentByteValue(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentByteValue V = (TComponentByteValue)AValue.getValue();
        return (V.Value == Value);
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Value = BA[Idx.Value]; Idx.Value+=1;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] R = {Value};
        return R;
    }
}
