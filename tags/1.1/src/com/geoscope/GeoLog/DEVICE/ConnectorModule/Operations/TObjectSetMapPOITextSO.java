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
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOITextValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetMapPOITextSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,6,1001);
    
    private static final int MapPOITextValuesCapacity = 100;
    private TMapPOITextValue[] MapPOITextValues = new TMapPOITextValue[MapPOITextValuesCapacity];
    private short MapPOITextValues_Count = 0;
    
    public TObjectSetMapPOITextSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Map POI text";
    }
    
    @Override
    public void Destroy()
    {
    	Finalize();
    }
    
    public void Finalize() {
        for (int I = 0; I < MapPOITextValues_Count; I++)
        	MapPOITextValues[I].Finalize();
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TMapPOITextValue value = (TMapPOITextValue)Value;
        MapPOITextValues[0] = value;
        MapPOITextValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (MapPOITextValues_Count == 0)
            return null; //. ->
        return MapPOITextValues[0];
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
        return MapPOITextValues_Count;
    }
    
    public synchronized int BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
            DataSize += MapPOITextValues[I].ByteArraySize();
        return (DataSize);
    }
        
    public synchronized int Saving_BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
            DataSize += MapPOITextValues[I].Saving_ByteArraySize();
        return (DataSize);
    }
        
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TMapPOITextValue MapPOIText = (TMapPOITextValue)Value;
        if ((MapPOITextValues_Count > 0) && (MapPOITextValues[MapPOITextValues_Count-1].IsValueTheSame(MapPOIText)))
            return true; //. ->
        if (MapPOITextValues_Count >= MapPOITextValuesCapacity)
            return false; //. ->            
        MapPOITextValues[MapPOITextValues_Count] = MapPOIText;
        MapPOITextValues_Count++;
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
        if (ValuesCount > MapPOITextValuesCapacity)
            ValuesCount = MapPOITextValuesCapacity;
        MapPOITextValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOITextValue Value = new TMapPOITextValue();
            Value.FromByteArray(BA,/*ref*/ Idx);
            //.
            MapPOITextValues[MapPOITextValues_Count] = Value;
            MapPOITextValues_Count++;
        }
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOITextValuesCapacity)
            ValuesCount = MapPOITextValuesCapacity;
        MapPOITextValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOITextValue Value = new TMapPOITextValue();
            Value.Saving_FromByteArray(BA,/*ref*/ Idx);
            //.
            if (Value.IsValid()) {
                MapPOITextValues[MapPOITextValues_Count] = Value;
                MapPOITextValues_Count++;
            }
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (MapPOITextValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
        {
            BA = MapPOITextValues[I].ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
    
    protected synchronized byte[] Saving_PrepareData() throws Exception
    {
        if (MapPOITextValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Saving_BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
        {
            BA = MapPOITextValues[I].Saving_ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}