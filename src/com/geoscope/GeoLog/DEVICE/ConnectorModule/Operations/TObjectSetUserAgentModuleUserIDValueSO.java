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
import com.geoscope.GeoLog.DEVICE.UserAgentModule.TUserIDValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetUserAgentModuleUserIDValueSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,22,1);
    
    private static final int 	ValuesCapacity = 1;
    private TUserIDValue[] 		Values = new TUserIDValue[ValuesCapacity];
    private short 				Values_Count = 0;
    
    public TObjectSetUserAgentModuleUserIDValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set UserAgentModule UserID";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    @Override
    public int GetQueueMaxDelay() {
    	return QueueMaxDelay;
    }
    
    public synchronized void setValue(TComponentValue Value)
    {
    	TUserIDValue _Value = (TUserIDValue)Value;
        Values[0] = _Value;
        Values_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Values_Count == 0)
            return null; //. ->
        return Values[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TUserIDValue.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Values_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
    	TUserIDValue _Value = (TUserIDValue)Value;
        if ((Values_Count > 0) && (Values[Values_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (Values_Count >= ValuesCapacity)
            return false; //. ->            
        Values[Values_Count] = _Value;
        Values_Count++;
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
        if (ValuesCount > ValuesCapacity)
            ValuesCount = ValuesCapacity;
        Values_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TUserIDValue Value = new TUserIDValue(BA,/*ref*/ Idx);
            Values[Values_Count] = Value;
            Values_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Values_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Values_Count*TUserIDValue.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Values_Count; I++)
        {
            BA = Values[I].ToByteArray();
            if (BA != null) {
                System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
            }
        }
        return Result;
    }
}