/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TFixMarkValue extends TComponentValue
{

    public static final int TFixMarkValueSize = 8+4+4;
    
    public double 	Timestamp;
    public int 		ObjID;
    public int 		ID;

    public TFixMarkValue()
    {
    }
    
    public TFixMarkValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TFixMarkValue(double pTimestamp, int pObjID, int pID)
    {
    	Timestamp = pTimestamp;
        ObjID = pObjID;
        ID = pID;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TFixMarkValue Src = (TFixMarkValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        ObjID = Src.ObjID;
        ID = Src.ID;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TFixMarkValue(Timestamp,ObjID,ID);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TFixMarkValue FixMark = (TFixMarkValue)AValue.getValue();
        return ((Timestamp == FixMark.Timestamp) && (ObjID == FixMark.ObjID) && (ID == FixMark.ID));
    }
    
    public synchronized void setValues(double pTimestamp, int pObjID, int pID)
    {
    	Timestamp = pTimestamp;
        ObjID = pObjID;
        ID = pID;
        //.
        flSet = true;
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        ObjID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        ID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] Result = new byte[TFixMarkValueSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(ObjID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(ID);
        System.arraycopy(BA,0,Result,Idx,BA.length); 
        return Result;
    }
}
