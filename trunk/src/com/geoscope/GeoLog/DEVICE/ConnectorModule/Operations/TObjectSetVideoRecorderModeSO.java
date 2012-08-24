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
public class TObjectSetVideoRecorderModeSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,1);
    
    private static final int ModesCapacity = 50;
    private TComponentTimestampedShortValue[] Modes = new TComponentTimestampedShortValue[ModesCapacity];
    private short Modes_Count = 0;
    
    public TObjectSetVideoRecorderModeSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Mode";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedShortValue _Value = (TComponentTimestampedShortValue)Value;
        Modes[0] = _Value;
        Modes_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Modes_Count == 0)
            return null; //. ->
        return Modes[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedShortValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Modes_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedShortValue _Value = (TComponentTimestampedShortValue)Value;
        if ((Modes_Count > 0) && (Modes[Modes_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (Modes_Count >= ModesCapacity)
            return false; //. ->            
        Modes[Modes_Count] = _Value;
        Modes_Count++;
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
        if (ValuesCount > ModesCapacity)
            ValuesCount = ModesCapacity;
        Modes_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedShortValue Value = new TComponentTimestampedShortValue(BA,/*ref*/ Idx);
            Modes[Modes_Count] = Value;
            Modes_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Modes_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Modes_Count*TComponentTimestampedShortValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Modes_Count; I++)
        {
            BA = Modes[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}