/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import android.os.Handler;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;

/**
 *
 * @author ALXPONOM
 */

public class TObjectGetVideoRecorderReceiversValueSO extends TObjectGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,9);

    public static final int MESSAGE_OPERATION_COMPLETED = 1;
    public static final int MESSAGE_OPERATION_ERROR = 2;

    private TComponentTimestampedANSIStringValue _Value;
    
    public Handler CompletionHandler = null;
    
    public TObjectGetVideoRecorderReceiversValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value) {
    	_Value = (TComponentTimestampedANSIStringValue)Value;
    }
    
    public synchronized TComponentValue getValue()
    {
        return _Value;
    }
    
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException
    {
        if (CompletionHandler != null) 
        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_COMPLETED,_Value).sendToTarget();        	
    	return SuccessCode_OK;
    }

    public synchronized void DoOnOperationException(OperationException E) 
    {
        if (CompletionHandler != null) 
        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_ERROR,E).sendToTarget();        	
    }
}
    
