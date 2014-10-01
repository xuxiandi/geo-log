/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ADCModule.TADCModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TObjectSetADCValueItemSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,7,1);
    
    private static final int ADCValueItemsCapacity = 50;
    
    private TElementAddress ItemAddress = null;
    private TADCModule.TADCValueItem[] ADCValueItems = new TADCModule.TADCValueItem[ADCValueItemsCapacity];
    private short ADCValueItems_Count = 0;
    
    public TObjectSetADCValueItemSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,null);
        if (pSubAddress != null)
        	ItemAddress = new TElementAddress(pSubAddress);
        Name = "Set ADC value item";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(ItemAddress);
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
    	TADCModule.TADCValueItem _Value = (TADCModule.TADCValueItem)Value;
        ADCValueItems[0] = _Value;
        ADCValueItems_Count = 1;
        //.
        ItemAddress = new TElementAddress(_Value.Address);
    }
        
    public synchronized TComponentValue getValue()
    {
        if (ADCValueItems_Count == 0)
            return null; //. ->
        return ADCValueItems[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TADCModule.TADCValueItem.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return ADCValueItems_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
    	TADCModule.TADCValueItem _Value = (TADCModule.TADCValueItem)Value;
        if ((ADCValueItems_Count > 0) && (ADCValueItems[ADCValueItems_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (ADCValueItems_Count >= ADCValueItemsCapacity)
            return false; //. ->            
        ADCValueItems[ADCValueItems_Count] = _Value;
        ADCValueItems_Count++;
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
        if (ValuesCount > ADCValueItemsCapacity)
            ValuesCount = ADCValueItemsCapacity;
        ADCValueItems_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TADCModule.TADCValueItem Value = new TADCModule.TADCValueItem(Connector.Device.ADCModule, ItemAddress.Value[0], BA,/*ref*/ Idx);
            ADCValueItems[ADCValueItems_Count] = Value;
            ADCValueItems_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (ADCValueItems_Count == 0)
            return null; //. =>
        byte[] Result = new byte[ADCValueItems_Count*TADCModule.TADCValueItem.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < ADCValueItems_Count; I++)
        {
            BA = ADCValueItems[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}