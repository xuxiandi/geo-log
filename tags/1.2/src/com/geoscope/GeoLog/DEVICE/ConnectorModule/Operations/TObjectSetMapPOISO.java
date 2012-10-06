/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetMapPOISO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,6,1);
    
    private static final int MapPOIValuesCapacity = 32;
    private TMapPOIValue[] MapPOIValues = new TMapPOIValue[MapPOIValuesCapacity];
    private short MapPOIValues_Count = 0;
    
    public TObjectSetMapPOISO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Map POI";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TMapPOIValue value = (TMapPOIValue)Value;
        MapPOIValues[0] = value;
        MapPOIValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (MapPOIValues_Count == 0)
            return null; //. ->
        return MapPOIValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TMapPOIValue.TMapPOIValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return MapPOIValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TMapPOIValue MapPOI = (TMapPOIValue)Value;
        if ((MapPOIValues_Count > 0) && (MapPOIValues[MapPOIValues_Count-1].IsValueTheSame(MapPOI)))
            return true; //. ->
        if (MapPOIValues_Count >= MapPOIValuesCapacity)
            return false; //. ->            
        MapPOIValues[MapPOIValues_Count] = MapPOI;
        MapPOIValues_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult)
    {
        double MaxSecondsPerOperation = 0.100;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOIValuesCapacity)
            ValuesCount = MapPOIValuesCapacity;
        MapPOIValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOIValue Value = new TMapPOIValue(BA,/*ref*/ Idx);
            MapPOIValues[MapPOIValues_Count] = Value;
            MapPOIValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (MapPOIValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[MapPOIValues_Count*TMapPOIValue.TMapPOIValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOIValues_Count; I++)
        {
            BA = MapPOIValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}