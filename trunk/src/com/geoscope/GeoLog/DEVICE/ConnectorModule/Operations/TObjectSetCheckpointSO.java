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
public class TObjectSetCheckpointSO extends TObjectSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,3,2);
    
    private static final int 					CheckpointsCapacity = 50;
    private TComponentInt16Value[] 				Checkpoints = new TComponentInt16Value[CheckpointsCapacity];
    private short 								Checkpoints_Count = 0;
    
    public TObjectSetCheckpointSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        Name = "Set Checkpoint";
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TComponentInt16Value _Value = (TComponentInt16Value)Value;
        Checkpoints[0] = _Value;
        Checkpoints_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (Checkpoints_Count == 0)
            return null; //. ->
        return Checkpoints[0];
    }
        
    protected synchronized int ValueSize()
    {
        return TComponentInt16Value.ValueSize;
    }
    
    public synchronized int ValueCount()
    {
        return Checkpoints_Count;
    }
    
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TComponentInt16Value _Value = (TComponentInt16Value)Value;
        if ((Checkpoints_Count > 0) && (Checkpoints[Checkpoints_Count-1].IsValueTheSame(_Value)))
            return true; //. ->
        if (Checkpoints_Count >= CheckpointsCapacity)
            return false; //. ->            
        Checkpoints[Checkpoints_Count] = _Value;
        Checkpoints_Count++;
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
        if (ValuesCount > CheckpointsCapacity)
            ValuesCount = CheckpointsCapacity;
        Checkpoints_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TComponentInt16Value Value = new TComponentInt16Value(BA,/*ref*/ Idx);
            Checkpoints[Checkpoints_Count] = Value;
            Checkpoints_Count++;
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (Checkpoints_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Checkpoints_Count*TComponentInt16Value.ValueSize];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < Checkpoints_Count; I++)
        {
            BA = Checkpoints[I].ToByteArray();
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
}