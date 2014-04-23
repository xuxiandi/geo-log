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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
/**
 * @author ALXPONOM
 */
public class TObjectSetGetTaskModuleTaskStatusSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,16,1001);
    
    private static final int 	TaskStatusValuesCapacity = 1;
    private TTaskStatusValue[] 	TaskStatusValues = new TTaskStatusValue[TaskStatusValuesCapacity];
    private short 				TaskStatusValues_Count = 0;
    
    public TObjectSetGetTaskModuleTaskStatusSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        flParseResult = true;
        Name = "Set TaskModule TaskStatus";
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
    
    @Override
    public synchronized void setValue(TComponentValue Value) {
        TTaskStatusValue value = (TTaskStatusValue)Value;
        TaskStatusValues[0] = value;
        TaskStatusValues_Count = 1;
    }
        
    @Override
    public synchronized TComponentValue getValue() {
        if (TaskStatusValues_Count == 0)
            return null; //. ->
        return TaskStatusValues[0];
    }
        
    @Override
    protected synchronized boolean ValueIsVariableSized() {
    	return true;
    }
    
    @Override
    protected synchronized int ValueSize() {
        return 0;
    }
    
    @Override
    public synchronized int ValueCount() {
        return TaskStatusValues_Count;
    }
    
    @Override
    public synchronized int BatchSize() throws Exception {
        int DataSize = 0;
        for (int I = 0; I < TaskStatusValues_Count; I++)
            DataSize += TaskStatusValues[I].ByteArraySize();
        return (DataSize);
    }
        
    @Override
    public synchronized boolean AddNewValue(TComponentValue Value) {
        TTaskStatusValue StatusValue = (TTaskStatusValue)Value;
        if ((TaskStatusValues_Count > 0) && (TaskStatusValues[TaskStatusValues_Count-1].IsValueTheSame(StatusValue)))
            return true; //. ->
        if (TaskStatusValues_Count >= TaskStatusValuesCapacity)
            return false; //. ->            
        TaskStatusValues[TaskStatusValues_Count] = StatusValue;
        TaskStatusValues_Count++;
        return true;
    }
    
    @Override
    protected int TimeForCompletion(int Mult) {
        double MaxSecondsPerOperation = 10.0;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > TaskStatusValuesCapacity)
            ValuesCount = TaskStatusValuesCapacity;
        TaskStatusValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++) {
            TTaskStatusValue Value = new TTaskStatusValue(BA,/*ref*/ Idx);
            TaskStatusValues[TaskStatusValues_Count] = Value;
            TaskStatusValues_Count++;
        }
    }
    
    @Override
    protected synchronized byte[] PrepareData() throws Exception {
        if (TaskStatusValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < TaskStatusValues_Count; I++) {
            BA = TaskStatusValues[I].ToByteArray();
            if (BA != null) {
            	System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
            }
        }
        return Result;
    }

    @Override
    public synchronized int DoOnOperationCompletion() throws Exception {
		return TGeographServerServiceOperation.SuccessCode_OK;
    }

    @Override
    public synchronized void DoOnOperationException(OperationException E) {
    	TTaskStatusValue TaskStatusValue = ((TTaskStatusValue)getValue());
    	if (TaskStatusValue.ExceptionHandler != null)
    		TaskStatusValue.ExceptionHandler.DoOnException(E);
    }
}