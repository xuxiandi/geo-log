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

public class TComponentTimestampedDoubleArrayValue extends TComponentTimestampedValue
{
    public static final int DoubleSize = 8;
    
    public double[]		Value = null;
    
	public TComponentTimestampedDoubleArrayValue(TComponent pOwner, int pID, String pName, int Size) {
		super(pOwner, pID, pName);
		Value  = new double[Size];
	}

    public TComponentTimestampedDoubleArrayValue()
    {
    	Timestamp = 0.0;
        Value = null;
    }
    
    public TComponentTimestampedDoubleArrayValue(double pTimestamp, double[] pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedDoubleArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, double[] pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized double[] GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedDoubleArrayValue Src = (TComponentTimestampedDoubleArrayValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedDoubleArrayValue(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedDoubleArrayValue V = (TComponentTimestampedDoubleArrayValue)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
		if ((Idx.Value+10) > BA.length) 
			return; //. -> 
		double _Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
		short Size = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2;
		if ((Idx.Value+Size*DoubleSize) > BA.length) 
			return; //. -> 
		if (_Timestamp <= Timestamp)
		{
			Idx.Value += (Size*DoubleSize);
			return; //. ->
		};
		if ((Value == null) && (Size != Value.length)) 
		{
			Idx.Value += (Size*DoubleSize);
			return; //. ->
		};
		Timestamp = _Timestamp;
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
		Result = new byte[8+2/*SizeOf(Size)*/+Size*DoubleSize];
		int Idx = 0;
		byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    	BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Size);
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
