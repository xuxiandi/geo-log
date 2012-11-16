/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class TComponentTimestampedInt16ArrayValue extends TComponentTimestampedValue
{
    public static final int Int16Size = 2;
    
    public short[]	Value = null;
    
	public TComponentTimestampedInt16ArrayValue(TComponent pOwner, int pID, String pName, int Size) {
		super(pOwner, pID, pName);
		Value  = new short[Size];
	}

    public TComponentTimestampedInt16ArrayValue()
    {
    	Timestamp = 0.0;
        Value = null;
    }
    
	public TComponentTimestampedInt16ArrayValue(int Size) {
    	Timestamp = 0.0;
		Value  = new short[Size];
	}

    public TComponentTimestampedInt16ArrayValue(double pTimestamp, short[] pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedInt16ArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, short[] pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized short[] GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedInt16ArrayValue Src = (TComponentTimestampedInt16ArrayValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedInt16ArrayValue(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedInt16ArrayValue V = (TComponentTimestampedInt16ArrayValue)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
		if ((Idx.Value+10) > BA.length) 
			return; //. -> 
		double _Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
		short Size = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2;
		if ((Idx.Value+Size*Int16Size) > BA.length) 
			return; //. -> 
		if (_Timestamp <= Timestamp)
		{
			Idx.Value += (Size*Int16Size);
			return; //. ->
		};
		if ((Value == null) && (Size != Value.length)) 
		{
			Idx.Value += (Size*Int16Size);
			return; //. ->
		};
		Timestamp = _Timestamp;
		for (int I = 0; I < Size; I++)
		{
			Value[I] = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=Int16Size;
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
		Result = new byte[8+2/*SizeOf(Size)*/+Size*Int16Size];
		int Idx = 0;
		byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    	BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Size);
    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    	if (Size > 0) {
    		for (int I = 0; I < Value.length; I++)
    		{
    			BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Value[I]);
    	    	System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    		};
    	}
		//.
		return Result;
    }
    
    public boolean FromFile(String FileName) throws IOException, OperationException {
		File F = new File(FileName);
		if (F.exists()) {
	    	long FileSize = F.length();
	    	FileInputStream FIS = new FileInputStream(FileName);
	    	try {
	    		byte[] BA = new byte[(int)FileSize];
	    		FIS.read(BA);
	    		TIndex Idx = new TIndex();
	    		FromByteArray(BA,Idx);
	    	}
	    	finally {
	    		FIS.close();
	    	}
	    	return true; //. ->
		}
		else
			return false;
    }
    
    public void ToFile(String FileName) throws IOException {
		FileOutputStream FOS = new FileOutputStream(FileName);
		try {
			FOS.write(ToByteArray());
		}
		finally {
			FOS.close();
		}
    }
}
