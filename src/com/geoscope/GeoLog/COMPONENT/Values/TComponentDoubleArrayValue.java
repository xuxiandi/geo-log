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

public class TComponentDoubleArrayValue extends TComponentValue
{
    public static final int DoubleSize = 8;
    
    public double[]		Value = null;
    
	public TComponentDoubleArrayValue(TComponent pOwner, int pID, String pName, int Size) {
		super(pOwner, pID, pName);
		Value  = new double[Size];
	}

    public TComponentDoubleArrayValue()
    {
        Value = null;
    }
    
    public TComponentDoubleArrayValue(double[] pValue)
    {
        Value = pValue;
        flSet = true;
    }

    public TComponentDoubleArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double[] pValue)
    {
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double[] GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentDoubleArrayValue Src = (TComponentDoubleArrayValue)pValue.getValue();
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentDoubleArrayValue(Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentDoubleArrayValue V = (TComponentDoubleArrayValue)AValue.getValue();
        return (V.Value == Value);
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
		if ((Idx.Value+2) > BA.length) 
			return; //. -> 
		short Size = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2;
		if ((Idx.Value+Size*DoubleSize) > BA.length) 
			return; //. -> 
		if ((Value == null) && (Size != Value.length)) 
		{
			Idx.Value += (Size*DoubleSize);
			return; //. ->
		};
		for (int I = 0; I < Size; I++)
		{
			Value[I] = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=DoubleSize;
		};
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException
    {
		byte[] Result;
		short Size = 0;
		if (Value != null)
			Size = (short)Value.length;
		Result = new byte[2/*SizeOf(Size)*/+Size*DoubleSize];
		int Idx = 0;
    	byte[] BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Size);
    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    	if (Size > 0) {
    		for (int I = 0; I < Value.length; I++)
    		{
    			BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Value[I]);
    	    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    		};
    	}
		//.
		return Result;
    }
}
