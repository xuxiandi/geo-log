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
public class TObjectSetVideoRecorderSavingFlagSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,7);
    
    private static final int SavingFlagsCapacity = 5;
    private TComponentTimestampedByteValue[] SavingFlags = new TComponentTimestampedByteValue[SavingFlagsCapacity];
    private short SavingFlags_Count = 0;
    
    public TObjectSetVideoRecorderSavingFlagSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Saving flag";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        SavingFlags[0] = _Value;
        SavingFlags_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (SavingFlags_Count == 0)
            return null; //. ->
        return SavingFlags[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedByteValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return SavingFlags_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        if ((SavingFlags_Count > 0) && (SavingFlags[SavingFlags_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (SavingFlags_Count >= SavingFlagsCapacity)
            return false; //. ->            
        SavingFlags[SavingFlags_Count] = _Value;
        SavingFlags_Count++;
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
        if (ValuesCount > SavingFlagsCapacity)
            ValuesCount = SavingFlagsCapacity;
        SavingFlags_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedByteValue Value = new TComponentTimestampedByteValue(BA,/*ref*/ Idx);
            SavingFlags[SavingFlags_Count] = Value;
            SavingFlags_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (SavingFlags_Count == 0)
            return null; //. =>
        byte[] Result = new byte[SavingFlags_Count*TComponentTimestampedByteValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < SavingFlags_Count; I++)
        {
            BA = SavingFlags[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}