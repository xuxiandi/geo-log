package com.geoscope.GeoLog.COMPONENT;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponentElement {

	public TComponent Owner = null;
	public int ID = 0;
	public String Name = "";
	
	public TComponentElement()
	{
	}
	
	public TComponentElement(TComponent pOwner, int pID, String pName)
	{
		Owner = pOwner;
		ID = pID;
		Name = pName;
	}	
	
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    }
    
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
        return null;
    }
}
