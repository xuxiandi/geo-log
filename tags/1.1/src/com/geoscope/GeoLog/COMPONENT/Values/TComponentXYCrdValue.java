package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponentXYCrdValue extends TComponentValue
{
	public double X;
	public double Y;
	
	public TComponentXYCrdValue(TComponent pOwner, int pID, String pName) {
		super(pOwner,pID,pName);
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
	{
		if ((Idx.Value+16) > BA.length) return; //. -> 
		X = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
		Y = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        super.FromByteArray(BA,/*ref*/ Idx);
	}

	@Override
    public synchronized byte[] ToByteArray() throws IOException
	{
		byte[] Result = new byte[16];
		int Idx = 0;
		byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(X);
		System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
		BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Y);
		System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
		//.
		return Result;
	}
}	
