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
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskResultValue;
/**
 * @author ALXPONOM
 */
public class TObjectSetGetTaskModuleTaskResultSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,16,1002);
    
    private static final int 	TaskResultValuesCapacity = 1;
    private TTaskResultValue[] 	TaskResultValues = new TTaskResultValue[TaskResultValuesCapacity];
    private short 				TaskResultValues_Count = 0;
    
    public TObjectSetGetTaskModuleTaskResultSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        flParseResult = true;
        Name = "Set TaskModule TaskResult";
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
    
    @Override
    public synchronized void setValue(TComponentValue Value) {
        TTaskResultValue value = (TTaskResultValue)Value;
        TaskResultValues[0] = value;
        TaskResultValues_Count = 1;
    }
        
    @Override
    public synchronized TComponentValue getValue() {
        if (TaskResultValues_Count == 0)
            return null; //. ->
        return TaskResultValues[0];
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
        return TaskResultValues_Count;
    }
    
    @Override
    public synchronized int BatchSize() throws Exception {
        int DataSize = 0;
        for (int I = 0; I < TaskResultValues_Count; I++)
            DataSize += TaskResultValues[I].ByteArraySize();
        return (DataSize);
    }
        
    @Override
    public synchronized boolean AddNewValue(TComponentValue Value) {
        TTaskResultValue ResultValue = (TTaskResultValue)Value;
        if ((TaskResultValues_Count > 0) && (TaskResultValues[TaskResultValues_Count-1].IsValueTheSame(ResultValue)))
            return true; //. ->
        if (TaskResultValues_Count >= TaskResultValuesCapacity)
            return false; //. ->            
        TaskResultValues[TaskResultValues_Count] = ResultValue;
        TaskResultValues_Count++;
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
        if (ValuesCount > TaskResultValuesCapacity)
            ValuesCount = TaskResultValuesCapacity;
        TaskResultValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++) {
            TTaskResultValue Value = new TTaskResultValue(BA,/*ref*/ Idx);
            TaskResultValues[TaskResultValues_Count] = Value;
            TaskResultValues_Count++;
        }
    }
    
    @Override
    protected synchronized byte[] PrepareData() throws Exception {
        if (TaskResultValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < TaskResultValues_Count; I++) {
            BA = TaskResultValues[I].ToByteArray();
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
    	TTaskResultValue TaskResultValue = ((TTaskResultValue)getValue());
    	if (TaskResultValue.ExceptionHandler != null)
    		TaskResultValue.ExceptionHandler.DoOnException(OperationException.GetParsedException(E,Connector.Device.context));
    }
}