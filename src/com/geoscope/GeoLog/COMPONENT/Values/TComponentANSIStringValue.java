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
public class TComponentANSIStringValue extends TComponentValue
{
    public String Value = "";

	public TComponentANSIStringValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentANSIStringValue()
    {
    }
    
    public TComponentANSIStringValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TComponentANSIStringValue(String pValue)
    {
        Value = pValue;
        //.
        flSet = true;
    }
    
    public synchronized String GetValue()
    {
        return Value;
    }
    
    public synchronized void SetValue(String pValue) {
    	Value = pValue;
        //.
        flSet = true;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentANSIStringValue Src = (TComponentANSIStringValue)pValue.getValue();
        Value = Src.Value;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TComponentANSIStringValue(Value);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentANSIStringValue S = (TComponentANSIStringValue)AValue.getValue();
        return (Value.equals(S.Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int SS = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        byte[] SA = new byte[SS];
        System.arraycopy(BA,Idx.Value, SA,0, SS); Idx.Value += SS;
        Value = new String(SA,"windows-1251");
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	int DataSize = 0;
        int SS = 0;
        byte[] SA = null;
        if (Value != null) {
        	SA = Value.getBytes("windows-1251");
        	SS = SA.length;
        }
        DataSize += SS; 
        byte[] Result = new byte[4/*SizeOf(DataSize)*/+DataSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (SS > 0) {            
            System.arraycopy(SA,0,Result,Idx,SS); Idx+=SS;
        }
        return Result;
    }

    public int ByteArraySize()
    {
        int SS = 0;
        if (Value != null) 
        	SS = Value.length()*2;
        return (4/*SizeOf(DataSize)*/+SS);
    }
}
