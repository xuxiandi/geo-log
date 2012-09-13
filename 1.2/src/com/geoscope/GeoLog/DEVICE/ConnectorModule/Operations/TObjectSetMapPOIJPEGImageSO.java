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
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIImageValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetMapPOIJPEGImageSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,6,1000);
    
    private static final int MapPOIImageValuesCapacity = 1;
    private TMapPOIImageValue[] MapPOIImageValues = new TMapPOIImageValue[MapPOIImageValuesCapacity];
    private short MapPOIImageValues_Count = 0;
    
    public TObjectSetMapPOIJPEGImageSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Map POI image";
    }
    
    @Override
    public void Destroy()
    {
    	Finalize();
    }
    
    public void Finalize() {
        for (int I = 0; I < MapPOIImageValues_Count; I++)
            MapPOIImageValues[I].Finalize();
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TMapPOIImageValue value = (TMapPOIImageValue)Value;
        MapPOIImageValues[0] = value;
        MapPOIImageValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (MapPOIImageValues_Count == 0)
            return null; //. ->
        return MapPOIImageValues[0];
    }
        
    @Override
    protected synchronized boolean ValueIsVariableSized() {
    	return true;
    }
    
    protected synchronized int ValueSize()
    {
        return 0;
    }
    
    public synchronized int ValueCount()
    {
        return MapPOIImageValues_Count;
    }
    
    public synchronized int BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOIImageValues_Count; I++)
            DataSize += MapPOIImageValues[I].ByteArraySize();
        return (DataSize);
    }
        
    public synchronized int Saving_BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOIImageValues_Count; I++)
            DataSize += MapPOIImageValues[I].Saving_ByteArraySize();
        return (DataSize);
    }
        
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TMapPOIImageValue MapPOIImage = (TMapPOIImageValue)Value;
        if ((MapPOIImageValues_Count > 0) && (MapPOIImageValues[MapPOIImageValues_Count-1].IsValueTheSame(MapPOIImage)))
            return true; //. ->
        if (MapPOIImageValues_Count >= MapPOIImageValuesCapacity)
            return false; //. ->            
        MapPOIImageValues[MapPOIImageValues_Count] = MapPOIImage;
        MapPOIImageValues_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult)
    {
        double MaxSecondsPerOperation = 5.0;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOIImageValuesCapacity)
            ValuesCount = MapPOIImageValuesCapacity;
        MapPOIImageValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOIImageValue Value = new TMapPOIImageValue();
            Value.FromByteArray(BA,/*ref*/ Idx);
            //.
            MapPOIImageValues[MapPOIImageValues_Count] = Value;
            MapPOIImageValues_Count++;
        }
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOIImageValuesCapacity)
            ValuesCount = MapPOIImageValuesCapacity;
        MapPOIImageValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOIImageValue Value = new TMapPOIImageValue();
            Value.Saving_FromByteArray(BA,/*ref*/ Idx);
            //.
            if (Value.IsValid()) {
                MapPOIImageValues[MapPOIImageValues_Count] = Value;
                MapPOIImageValues_Count++;
            }
        }
    }
    
    protected synchronized byte[] PrepareData() throws Exception
    {
        if (MapPOIImageValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOIImageValues_Count; I++)
        {
            BA = MapPOIImageValues[I].ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }

    protected synchronized byte[] Saving_PrepareData() throws Exception
    {
        if (MapPOIImageValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Saving_BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOIImageValues_Count; I++)
        {
            BA = MapPOIImageValues[I].Saving_ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}