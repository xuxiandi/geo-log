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
public class TObjectSetVideoRecorderAudioFlagSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,4);
    
    private static final int AudioFlagsCapacity = 5;
    private TComponentTimestampedByteValue[] AudioFlags = new TComponentTimestampedByteValue[AudioFlagsCapacity];
    private short AudioFlags_Count = 0;
    
    public TObjectSetVideoRecorderAudioFlagSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Audio flag";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        AudioFlags[0] = _Value;
        AudioFlags_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (AudioFlags_Count == 0)
            return null; //. ->
        return AudioFlags[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedByteValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return AudioFlags_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedByteValue _Value = (TComponentTimestampedByteValue)Value;
        if ((AudioFlags_Count > 0) && (AudioFlags[AudioFlags_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (AudioFlags_Count >= AudioFlagsCapacity)
            return false; //. ->            
        AudioFlags[AudioFlags_Count] = _Value;
        AudioFlags_Count++;
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
        if (ValuesCount > AudioFlagsCapacity)
            ValuesCount = AudioFlagsCapacity;
        AudioFlags_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedByteValue Value = new TComponentTimestampedByteValue(BA,/*ref*/ Idx);
            AudioFlags[AudioFlags_Count] = Value;
            AudioFlags_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (AudioFlags_Count == 0)
            return null; //. =>
        byte[] Result = new byte[AudioFlags_Count*TComponentTimestampedByteValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < AudioFlags_Count; I++)
        {
            BA = AudioFlags[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}