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
public class TObjectSetGPSModuleStatusSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,2);
    
    private static final int StatusesCapacity = 50;
    private TComponentTimestampedInt16Value[] Statuses = new TComponentTimestampedInt16Value[StatusesCapacity];
    private short Status_Count = 0;
    
    public TObjectSetGPSModuleStatusSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Status";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        Statuses[0] = _Value;
        Status_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Status_Count == 0)
            return null; //. ->
        return Statuses[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Status_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        if ((Status_Count > 0) && (Statuses[Status_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (Status_Count >= StatusesCapacity)
            return false; //. ->            
        Statuses[Status_Count] = _Value;
        Status_Count++;
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
        if (ValuesCount > StatusesCapacity)
            ValuesCount = StatusesCapacity;
        Status_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(BA,/*ref*/ Idx);
            Statuses[Status_Count] = Value;
            Status_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Status_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Status_Count*TComponentTimestampedInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Status_Count; I++)
        {
            BA = Statuses[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}