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
public class TObjectSetBatteryChargeValueSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,2,2);
    
    private static final int BatteryChargeValuesCapacity = 50;
    private TComponentTimestampedInt16Value[] BatteryChargeValues = new TComponentTimestampedInt16Value[BatteryChargeValuesCapacity];
    private short BatteryChargeValues_Count = 0;
    
    public TObjectSetBatteryChargeValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set BatteryCharge value";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        BatteryChargeValues[0] = _Value;
        BatteryChargeValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (BatteryChargeValues_Count == 0)
            return null; //. ->
        return BatteryChargeValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentTimestampedInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return BatteryChargeValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentTimestampedInt16Value _Value = (TComponentTimestampedInt16Value)Value;
        if ((BatteryChargeValues_Count > 0) && (BatteryChargeValues[BatteryChargeValues_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (BatteryChargeValues_Count >= BatteryChargeValuesCapacity)
            return false; //. ->            
        BatteryChargeValues[BatteryChargeValues_Count] = _Value;
        BatteryChargeValues_Count++;
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
        if (ValuesCount > BatteryChargeValuesCapacity)
            ValuesCount = BatteryChargeValuesCapacity;
        BatteryChargeValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(BA,/*ref*/ Idx);
            BatteryChargeValues[BatteryChargeValues_Count] = Value;
            BatteryChargeValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (BatteryChargeValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatteryChargeValues_Count*TComponentTimestampedInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < BatteryChargeValues_Count; I++)
        {
            BA = BatteryChargeValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}