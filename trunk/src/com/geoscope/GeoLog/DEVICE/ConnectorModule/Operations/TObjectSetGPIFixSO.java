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
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIFixValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGPIFixSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,1200);
    
    private static final int GPIFixValuesCapacity = 50;
    private TGPIFixValue[] GPIFixValues = new TGPIFixValue[GPIFixValuesCapacity];
    private short GPIFixValues_Count = 0;
    
    public TObjectSetGPIFixSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set GPI fix";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TGPIFixValue fix = (TGPIFixValue)Value;
        GPIFixValues[0] = fix;
        GPIFixValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (GPIFixValues_Count == 0)
            return null; //. ->
        return GPIFixValues[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TGPIFixValue.TGPIFixValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return GPIFixValues_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TGPIFixValue fix = (TGPIFixValue)Value;
        if ((GPIFixValues_Count > 0) && (GPIFixValues[GPIFixValues_Count-1].IsValueTheSame(fix)))
            return true; //. ->
        if (GPIFixValues_Count >= GPIFixValuesCapacity)
            return false; //. ->            
        GPIFixValues[GPIFixValues_Count] = fix;
        GPIFixValues_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult) 
    {
        double MaxSecondsPerOperation = 0.030;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > GPIFixValuesCapacity)
            ValuesCount = GPIFixValuesCapacity;
        GPIFixValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TGPIFixValue Value = new TGPIFixValue(BA,/*ref*/ Idx);
            GPIFixValues[GPIFixValues_Count] = Value;
            GPIFixValues_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (GPIFixValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[GPIFixValues_Count*TGPIFixValue.TGPIFixValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < GPIFixValues_Count; I++)
        {
            BA = GPIFixValues[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}