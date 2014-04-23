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
public class TObjectSetGPSModuleModeSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,1);
    
    private static final int ModesCapacity = 50;
    private TComponentTimestampedInt16Value[] Modes = new TComponentTimestampedInt16Value[ModesCapacity];
    private short Modes_Count = 0;
    
    public TObjectSetGPSModuleModeSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
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
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
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
        return TComponentTimestampedInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Modes_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
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
            TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(BA,/*ref*/ Idx);
            Modes[Modes_Count] = Value;
            Modes_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Modes_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Modes_Count*TComponentTimestampedInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Modes_Count; I++)
        {
            BA = Modes[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}