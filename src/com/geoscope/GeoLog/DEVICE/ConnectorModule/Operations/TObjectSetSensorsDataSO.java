/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedXMLDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetSensorsDataSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,19,1);
    
    private static final int DatasCapacity = 1;
    private TComponentTimestampedXMLDataValue[] Datas = new TComponentTimestampedXMLDataValue[DatasCapacity];
    private short Datas_Count = 0;
    
    public TObjectSetSensorsDataSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress); 
        Name = "Set Data";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
    	TComponentTimestampedXMLDataValue value = (TComponentTimestampedXMLDataValue)Value;
        Datas[0] = value;
        Datas_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Datas_Count == 0)
            return null; //. ->
        return Datas[0];
    }
        
    @Override
    protected synchronized boolean ValueIsVariableSized() {
    	return true;
    }
    
    protected synchronized int ValueSize()
    {
        return 0;
    }
    
    public synchronized int ValueCount()
    {
        return Datas_Count;
    }
    
    public synchronized int BatchSize() throws IOException
    {
        int DataSize = 0;
        for (int I = 0; I < Datas_Count; I++)
            DataSize += Datas[I].ByteArraySize();
        return (DataSize);
    }
        
    public synchronized int Saving_BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < Datas_Count; I++)
            DataSize += Datas[I].Saving_ByteArraySize();
        return (DataSize);
    }
        
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
    	TComponentTimestampedXMLDataValue value = (TComponentTimestampedXMLDataValue)Value;
        if ((Datas_Count > 0) && (Datas[Datas_Count-1].IsValueTheSame(value)))
            return true; //. ->
        if (Datas_Count >= DatasCapacity)
            return false; //. ->            
        Datas[Datas_Count] = value;
        Datas_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult)
    {
        double MaxSecondsPerOperation = 30.0;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > DatasCapacity)
            ValuesCount = DatasCapacity;
        Datas_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TComponentTimestampedXMLDataValue Value = new TComponentTimestampedXMLDataValue();
            Value.FromByteArray(BA,/*ref*/ Idx);
            //.
            Datas[Datas_Count] = Value;
            Datas_Count++;
        }
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > DatasCapacity)
            ValuesCount = DatasCapacity;
        Datas_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TComponentTimestampedXMLDataValue Value = new TComponentTimestampedXMLDataValue();
            Value.Saving_FromByteArray(BA,/*ref*/ Idx);
            //.
            if (Value.IsValid()) {
            	Datas[Datas_Count] = Value;
            	Datas_Count++;
            }
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException, OperationException
    {
        if (Datas_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Datas_Count; I++)
        {
            BA = Datas[I].ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }

    protected synchronized byte[] Saving_PrepareData() throws Exception
    {
        if (Datas_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Saving_BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Datas_Count; I++)
        {
            BA = Datas[I].Saving_ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}