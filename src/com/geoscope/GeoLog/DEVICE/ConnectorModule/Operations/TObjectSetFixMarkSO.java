/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TFixMarkValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetFixMarkSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,7,1);
    
    private static final int FixMarkValuesCapacity = 100;
    private TFixMarkValue[] FixMarkValues = new TFixMarkValue[FixMarkValuesCapacity];
    private short FixMarkValues_Count = 0;
    
    public TObjectSetFixMarkSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Fix Mark";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TFixMarkValue value = (TFixMarkValue)Value;
        FixMarkValues[0] = value;
        FixMarkValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (FixMarkValues_Count == 0)
            return null; //. ->
        return FixMarkValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TFixMarkValue.TFixMarkValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return FixMarkValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TFixMarkValue FixMark = (TFixMarkValue)Value;
        if ((FixMarkValues_Count > 0) && (FixMarkValues[FixMarkValues_Count-1].IsValueTheSame(FixMark)))
            return true; //. ->
        if (FixMarkValues_Count >= FixMarkValuesCapacity)
            return false; //. ->            
        FixMarkValues[FixMarkValues_Count] = FixMark;
        FixMarkValues_Count++;
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
        if (ValuesCount > FixMarkValuesCapacity)
            ValuesCount = FixMarkValuesCapacity;
        FixMarkValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TFixMarkValue Value = new TFixMarkValue(BA,/*ref*/ Idx);
            FixMarkValues[FixMarkValues_Count] = Value;
            FixMarkValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (FixMarkValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[FixMarkValues_Count*TFixMarkValue.TFixMarkValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < FixMarkValues_Count; I++)
        {
            BA = FixMarkValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}