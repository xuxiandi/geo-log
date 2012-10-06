/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TMapPOIValue extends TComponentValue
{

    public static final int TMapPOIValueSize = 8+4+4+4+256/*MaxSizeOf(shortstring)*/+1;
    
    public double   Timestamp;
    public int      MapID;
    public int      POIID;
    public int      POIType;
    public byte[]   POIName;
    public boolean  flPrivate;

    public TMapPOIValue()
    {
    }
    
    public TMapPOIValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TMapPOIValue(double pTimestamp, int pMapID, int pPOIID, int pPOIType, byte[] pPOIName, boolean pflPrivate)
    {
    	Timestamp = pTimestamp;
        MapID = pMapID;
        POIID = pPOIID;
        POIType = pPOIType;
        POIName = pPOIName;
        flPrivate = pflPrivate;
        //.
        flSet = true;
    }
    
    public TMapPOIValue(double pTimestamp, int pMapID, int pPOIID, int pPOIType, String pPOIName, boolean pflPrivate)
    {
    	Timestamp = pTimestamp;
        MapID = pMapID;
        POIID = pPOIID;
        POIType = pPOIType;
        try
        {
            POIName = pPOIName.getBytes("windows-1251");
        }
        catch (UnsupportedEncodingException E)
        {
            POIName = null;
        }
        flPrivate = pflPrivate;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TMapPOIValue Src = (TMapPOIValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        MapID = Src.MapID;
        POIID = Src.POIID;
        POIType = Src.POIType;
        POIName = Src.POIName;
        flPrivate = Src.flPrivate;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIValue(Timestamp,MapID,POIID,POIType,POIName,flPrivate);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIValue MapPOI = (TMapPOIValue)AValue.getValue();
        return ((Timestamp == MapPOI.Timestamp) && (MapID == MapPOI.MapID) && (POIID == MapPOI.POIID) && (POIType == MapPOI.POIType) && (POIName == MapPOI.POIName) && (flPrivate == MapPOI.flPrivate));
    }
    
    public synchronized void setValues(double pTimestamp, int pMapID, int pPOIID, int pPOIType, String pPOIName, boolean pflPrivate)
    {
    	Timestamp = pTimestamp;
        MapID = pMapID;
        POIID = pPOIID;
        POIType = pPOIType;
        try
        {
            POIName = pPOIName.getBytes("windows-1251");
        }
        catch (UnsupportedEncodingException E)
        {
            POIName = null;
        }
        flPrivate = pflPrivate;
        //.
        flSet = true;
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        MapID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        POIID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        POIType = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        byte NameLength = BA[Idx.Value]; Idx.Value+=1;
        if (NameLength > 0)
        {
            POIName = new byte[NameLength];
            System.arraycopy(BA,Idx.Value, POIName,0, POIName.length);  
        }
        else
            POIName = null;
        Idx.Value += 255;
        flPrivate = (BA[Idx.Value] != 0);
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] Result = new byte[TMapPOIValueSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(MapID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(POIID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(POIType);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        byte NameLength = 0;
        if (POIName != null)
            NameLength = (byte)POIName.length;
        Result[Idx] = NameLength; Idx+=1;
        if (NameLength > 0)
            System.arraycopy(POIName,0, Result,Idx, NameLength); 
        Idx+=255;
        byte V = 0;
        if (flPrivate)
            V = 1;
        Result[Idx] = V;
        return Result;
    }
}
