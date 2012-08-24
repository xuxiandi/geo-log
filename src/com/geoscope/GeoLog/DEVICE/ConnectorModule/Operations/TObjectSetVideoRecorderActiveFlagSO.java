/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedByteValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetVideoRecorderActiveFlagSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,2);
    
    private static final int ActiveFlagsCapacity = 50;
    private TComponentTimestampedByteValue[] ActiveFlags = new TComponentTimestampedByteValue[ActiveFlagsCapacity];
    private short ActiveFlags_Count = 0;
    
    public TObjectSetVideoRecorderActiveFlagSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress); 
        Name = "Set Active flag";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        ActiveFlags[0] = _Value;
        ActiveFlags_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (ActiveFlags_Count == 0)
            return null; //. ->
        return ActiveFlags[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedByteValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return ActiveFlags_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        if ((ActiveFlags_Count > 0) && (ActiveFlags[ActiveFlags_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (ActiveFlags_Count >= ActiveFlagsCapacity)
            return false; //. ->            
        ActiveFlags[ActiveFlags_Count] = _Value;
        ActiveFlags_Count++;
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
        if (ValuesCount > ActiveFlagsCapacity)
            ValuesCount = ActiveFlagsCapacity;
        ActiveFlags_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedByteValue Value = new TComponentTimestampedByteValue(BA,/*ref*/ Idx);
            ActiveFlags[ActiveFlags_Count] = Value;
            ActiveFlags_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (ActiveFlags_Count == 0)
            return null; //. =>
        byte[] Result = new byte[ActiveFlags_Count*TComponentTimestampedByteValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < ActiveFlags_Count; I++)
        {
            BA = ActiveFlags[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}