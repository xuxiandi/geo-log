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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskResultValue;

/**
 * @author ALXPONOM
 */

public class TObjectGetTaskModuleTaskResultSO extends TObjectGetComponentDataServiceOperation {
	
    public static TElementAddress _Address = new TElementAddress(2,16,1002);
    
    private TComponentValue _Value;
    
    public TObjectGetTaskModuleTaskResultSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
        
    @Override
    public synchronized void setValue(TComponentValue Value)
    {
    	_Value = Value;
    }
    
    @Override
    public synchronized TComponentValue getValue() throws Exception
    {
        return _Value;
    }
    
    @Override
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException {
    	return SuccessCode_OK;
    }

    @Override
    public synchronized void DoOnOperationException(OperationException E) {
    	try {
        	TTaskResultValue TaskResultValue = ((TTaskResultValue)getValue());
        	if (TaskResultValue.ExceptionHandler != null)
        		TaskResultValue.ExceptionHandler.DoOnException(OperationException.GetParsedException(E,Connector.Device.context));
    	}
    	catch (Exception Ex) {};
    }
}
    
