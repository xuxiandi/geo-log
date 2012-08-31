/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetConnectorServiceProviderSignalValueSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,3,1,4);
    
    private static final int ConnectorServiceProviderSignalValuesCapacity = 50;
    private TComponentTimestampedInt16Value[] ConnectorServiceProviderSignalValues = new TComponentTimestampedInt16Value[ConnectorServiceProviderSignalValuesCapacity];
    private short ConnectorServiceProviderSignalValues_Count = 0;
    
    public TObjectSetConnectorServiceProviderSignalValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set ConnectorServiceProviderSignal value";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        ConnectorServiceProviderSignalValues[0] = _Value;
        ConnectorServiceProviderSignalValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (ConnectorServiceProviderSignalValues_Count == 0)
            return null; //. ->
        return ConnectorServiceProviderSignalValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return ConnectorServiceProviderSignalValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        if ((ConnectorServiceProviderSignalValues_Count > 0) && (ConnectorServiceProviderSignalValues[ConnectorServiceProviderSignalValues_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (ConnectorServiceProviderSignalValues_Count >= ConnectorServiceProviderSignalValuesCapacity)
            return false; //. ->            
        ConnectorServiceProviderSignalValues[ConnectorServiceProviderSignalValues_Count] = _Value;
        ConnectorServiceProviderSignalValues_Count++;
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
        if (ValuesCount > ConnectorServiceProviderSignalValuesCapacity)
            ValuesCount = ConnectorServiceProviderSignalValuesCapacity;
        ConnectorServiceProviderSignalValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(BA,/*ref*/ Idx);
            ConnectorServiceProviderSignalValues[ConnectorServiceProviderSignalValues_Count] = Value;
            ConnectorServiceProviderSignalValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (ConnectorServiceProviderSignalValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[ConnectorServiceProviderSignalValues_Count*TComponentTimestampedInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < ConnectorServiceProviderSignalValues_Count; I++)
        {
            BA = ConnectorServiceProviderSignalValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}