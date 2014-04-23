/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGeoDistanceThresholdSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,4);
    
    private static final int 					ThresholdsCapacity = 50;
    private TComponentInt16Value[] 				Thresholds = new TComponentInt16Value[ThresholdsCapacity];
    private short 								Thresholds_Count = 0;
    
    public TObjectSetGeoDistanceThresholdSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Threshold";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentInt16Value _Value = (TComponentInt16Value)Value;
        Thresholds[0] = _Value;
        Thresholds_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Thresholds_Count == 0)
            return null; //. ->
        return Thresholds[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Thresholds_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentInt16Value _Value = (TComponentInt16Value)Value;
        if ((Thresholds_Count > 0) && (Thresholds[Thresholds_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (Thresholds_Count >= ThresholdsCapacity)
            return false; //. ->            
        Thresholds[Thresholds_Count] = _Value;
        Thresholds_Count++;
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
        if (ValuesCount > ThresholdsCapacity)
            ValuesCount = ThresholdsCapacity;
        Thresholds_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentInt16Value Value = new TComponentInt16Value(BA,/*ref*/ Idx);
            Thresholds[Thresholds_Count] = Value;
            Thresholds_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Thresholds_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Thresholds_Count*TComponentInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Thresholds_Count; I++)
        {
            BA = Thresholds[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}