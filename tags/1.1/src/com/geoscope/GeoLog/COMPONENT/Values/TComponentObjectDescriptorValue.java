package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponentObjectDescriptorValue extends TComponentValue
{
	public int idTObj;
	public int idObj;
	
	public TComponentObjectDescriptorValue(TComponent pOwner, int pID, String pName) {
		super(pOwner,pID,pName);
	}

	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
	{
		if ((Idx.Value+8) > BA.length) return; //. -> 
		idTObj = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
		idObj = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        super.FromByteArray(BA,/*ref*/ Idx);
	}

	@Override
    public synchronized byte[] ToByteArray() throws IOException
	{
		byte[] Result = new byte[8];
		int Idx = 0;
		byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(idTObj);
		System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
		BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(idObj);
		System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
		//.
		return Result;
	}
}
