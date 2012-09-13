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
public class TObjectSetVideoRecorderTransmittingFlagSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,6);
    
    private static final int TransmittingFlagsCapacity = 5;
    private TComponentTimestampedByteValue[] TransmittingFlags = new TComponentTimestampedByteValue[TransmittingFlagsCapacity];
    private short TransmittingFlags_Count = 0;
    
    public TObjectSetVideoRecorderTransmittingFlagSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Transmitting flag";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        TransmittingFlags[0] = _Value;
        TransmittingFlags_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (TransmittingFlags_Count == 0)
            return null; //. ->
        return TransmittingFlags[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedByteValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return TransmittingFlags_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        if ((TransmittingFlags_Count > 0) && (TransmittingFlags[TransmittingFlags_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (TransmittingFlags_Count >= TransmittingFlagsCapacity)
            return false; //. ->            
        TransmittingFlags[TransmittingFlags_Count] = _Value;
        TransmittingFlags_Count++;
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
        if (ValuesCount > TransmittingFlagsCapacity)
            ValuesCount = TransmittingFlagsCapacity;
        TransmittingFlags_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedByteValue Value = new TComponentTimestampedByteValue(BA,/*ref*/ Idx);
            TransmittingFlags[TransmittingFlags_Count] = Value;
            TransmittingFlags_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (TransmittingFlags_Count == 0)
            return null; //. =>
        byte[] Result = new byte[TransmittingFlags_Count*TComponentTimestampedByteValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < TransmittingFlags_Count; I++)
        {
            BA = TransmittingFlags[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}