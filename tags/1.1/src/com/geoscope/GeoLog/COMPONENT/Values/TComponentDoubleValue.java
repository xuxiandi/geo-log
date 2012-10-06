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

public class TComponentDoubleValue extends TComponentValue
{
    public static final int ValueSize = 8;
    
    public double Value;
    
	public TComponentDoubleValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentDoubleValue()
    {
        Value = 0;
    }
    
    public TComponentDoubleValue(double pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentDoubleValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pValue)
    {
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        Value = ((TComponentDoubleValue)pValue.getValue()).Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentDoubleValue(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentDoubleValue V = (TComponentDoubleValue)AValue.getValue();
        return (V.Value == Value);
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Value = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        return TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Value);
    }
}
    
