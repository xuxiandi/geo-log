/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedShortValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGPIValueSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,5,1);
    
    private static final int GPIValuesCapacity = 50;
    private TComponentTimestampedShortValue[] GPIValues = new TComponentTimestampedShortValue[GPIValuesCapacity];
    private short GPIValues_Count = 0;
    
    public TObjectSetGPIValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set GPI value";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedShortValue _Value = (TComponentTimestampedShortValue)Value;
        GPIValues[0] = _Value;
        GPIValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (GPIValues_Count == 0)
            return null; //. ->
        return GPIValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedShortValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return GPIValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedShortValue _Value = (TComponentTimestampedShortValue)Value;
        if ((GPIValues_Count > 0) && (GPIValues[GPIValues_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (GPIValues_Count >= GPIValuesCapacity)
            return false; //. ->            
        GPIValues[GPIValues_Count] = _Value;
        GPIValues_Count++;
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
        if (ValuesCount > GPIValuesCapacity)
            ValuesCount = GPIValuesCapacity;
        GPIValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedShortValue Value = new TComponentTimestampedShortValue(BA,/*ref*/ Idx);
            GPIValues[GPIValues_Count] = Value;
            GPIValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (GPIValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[GPIValues_Count*TComponentTimestampedShortValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < GPIValues_Count; I++)
        {
            BA = GPIValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}