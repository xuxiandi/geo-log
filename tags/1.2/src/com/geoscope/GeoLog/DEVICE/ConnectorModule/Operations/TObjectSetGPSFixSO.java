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
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGPSFixSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,5);
    
    private static final int GPSFixValuesCapacity = 50;
    private TGPSFixValue[] GPSFixValues = new TGPSFixValue[GPSFixValuesCapacity];
    private short GPSFixValues_Count = 0;
    
    public TObjectSetGPSFixSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set location fix";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TGPSFixValue fix = (TGPSFixValue)Value;
        GPSFixValues[0] = fix;
        GPSFixValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (GPSFixValues_Count == 0)
            return null; //. ->
        return GPSFixValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TGPSFixValue.TGPSFixValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return GPSFixValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TGPSFixValue fix = (TGPSFixValue)Value;
        if ((GPSFixValues_Count > 0) && (GPSFixValues[GPSFixValues_Count-1].IsValueTheSame(fix)))
            return true; //. ->
        if (GPSFixValues_Count >= GPSFixValuesCapacity)
            return false; //. ->            
        GPSFixValues[GPSFixValues_Count] = fix;
        GPSFixValues_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult)
    {
        double MaxSecondsPerOperation = 0.030;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > GPSFixValuesCapacity)
            ValuesCount = GPSFixValuesCapacity;
        GPSFixValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TGPSFixValue Value = new TGPSFixValue(BA,/*ref*/ Idx);
            GPSFixValues[GPSFixValues_Count] = Value;
            GPSFixValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (GPSFixValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[GPSFixValues_Count*TGPSFixValue.TGPSFixValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < GPSFixValues_Count; I++)
        {
            BA = GPSFixValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}