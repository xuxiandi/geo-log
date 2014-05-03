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
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
/**
 * @author ALXPONOM
 */
public class TObjectSetGetTaskModuleTaskDataSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,16,1000);
    
    private static final int 	TaskDataValuesCapacity = 1;
    private TTaskDataValue[] 	TaskDataValues = new TTaskDataValue[TaskDataValuesCapacity];
    private short 				TaskDataValues_Count = 0;
    
    public TObjectSetGetTaskModuleTaskDataSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        flParseResult = true;
        Name = "Set TaskModule TaskData";
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
    
    @Override
    public synchronized void setValue(TComponentValue Value) {
        TTaskDataValue value = (TTaskDataValue)Value;
        TaskDataValues[0] = value;
        TaskDataValues_Count = 1;
    }
        
    @Override
    public synchronized TComponentValue getValue() {
        if (TaskDataValues_Count == 0)
            return null; //. ->
        return TaskDataValues[0];
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
        return TaskDataValues_Count;
    }
    
    @Override
    public synchronized int BatchSize() {
        int DataSize = 0;
        for (int I = 0; I < TaskDataValues_Count; I++)
            DataSize += TaskDataValues[I].ByteArraySize();
        return (DataSize);
    }
        
    @Override
    public synchronized boolean AddNewValue(TComponentValue Value) {
        TTaskDataValue DataValue = (TTaskDataValue)Value;
        if ((TaskDataValues_Count > 0) && (TaskDataValues[TaskDataValues_Count-1].IsValueTheSame(DataValue)))
            return true; //. ->
        if (TaskDataValues_Count >= TaskDataValuesCapacity)
            return false; //. ->            
        TaskDataValues[TaskDataValues_Count] = DataValue;
        TaskDataValues_Count++;
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
        if (ValuesCount > TaskDataValuesCapacity)
            ValuesCount = TaskDataValuesCapacity;
        TaskDataValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++) {
            TTaskDataValue Value = new TTaskDataValue(BA,/*ref*/ Idx);
            TaskDataValues[TaskDataValues_Count] = Value;
            TaskDataValues_Count++;
        }
    }
    
    @Override
    protected synchronized byte[] PrepareData() throws IOException, OperationException {
        if (TaskDataValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < TaskDataValues_Count; I++) {
            BA = TaskDataValues[I].ToByteArray();
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
    	TTaskDataValue TaskDataValue = ((TTaskDataValue)getValue());
    	if (TaskDataValue.ExceptionHandler != null)
    		TaskDataValue.ExceptionHandler.DoOnException(OperationException.GetParsedException(E,Connector.Device.context));
    }
}