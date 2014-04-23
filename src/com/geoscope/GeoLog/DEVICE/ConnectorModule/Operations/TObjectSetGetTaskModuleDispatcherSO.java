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
import com.geoscope.GeoLog.DEVICE.TaskModule.TDispatcherValue;
/**
 * @author ALXPONOM
 */
public class TObjectSetGetTaskModuleDispatcherSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,16,1004);
    
    private static final int 	DispatcherValuesCapacity = 1;
    private TDispatcherValue[] 	DispatcherValues = new TDispatcherValue[DispatcherValuesCapacity];
    private short 				DispatcherValues_Count = 0;
    
    public TObjectSetGetTaskModuleDispatcherSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        flParseResult = true;
        Name = "Set TaskModule Dispatcher";
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
    
    @Override
    public synchronized void setValue(TComponentValue Value) {
        TDispatcherValue value = (TDispatcherValue)Value;
        DispatcherValues[0] = value;
        DispatcherValues_Count = 1;
    }
        
    @Override
    public synchronized TComponentValue getValue() {
        if (DispatcherValues_Count == 0)
            return null; //. ->
        return DispatcherValues[0];
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
        return DispatcherValues_Count;
    }
    
    @Override
    public synchronized int BatchSize() throws Exception {
        int DataSize = 0;
        for (int I = 0; I < DispatcherValues_Count; I++)
            DataSize += DispatcherValues[I].ByteArraySize();
        return (DataSize);
    }
        
    @Override
    public synchronized boolean AddNewValue(TComponentValue Value) {
    	TDispatcherValue DispatcherValue = (TDispatcherValue)Value;
        if ((DispatcherValues_Count > 0) && (DispatcherValues[DispatcherValues_Count-1].IsValueTheSame(DispatcherValue)))
            return true; //. ->
        if (DispatcherValues_Count >= DispatcherValuesCapacity)
            return false; //. ->            
        DispatcherValues[DispatcherValues_Count] = DispatcherValue;
        DispatcherValues_Count++;
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
        if (ValuesCount > DispatcherValuesCapacity)
            ValuesCount = DispatcherValuesCapacity;
        DispatcherValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++) {
        	TDispatcherValue Value = new TDispatcherValue(BA,/*ref*/ Idx);
            DispatcherValues[DispatcherValues_Count] = Value;
            DispatcherValues_Count++;
        }
    }
    
    @Override
    protected synchronized byte[] PrepareData() throws Exception {
        if (DispatcherValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < DispatcherValues_Count; I++) {
            BA = DispatcherValues[I].ToByteArray();
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
    	TDispatcherValue DispatcherValue = ((TDispatcherValue)getValue());
    	if (DispatcherValue.ExceptionHandler != null)
    		DispatcherValue.ExceptionHandler.DoOnException(E);
    }
}