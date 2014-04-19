/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import android.os.Handler;

import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TObjectGetTaskModuleExpertsSO extends TObjectGetComponentDataServiceOperation {
	
    public static TElementAddress _Address = new TElementAddress(2,16,1003);

    public static final int MESSAGE_OPERATION_COMPLETED 	= 1;
    public static final int MESSAGE_OPERATION_ERROR 		= 2;

    private byte[] ResultData;
    
    public Handler CompletionHandler = null;
    
    public TObjectGetTaskModuleExpertsSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
        
    @Override
    protected synchronized int ParseData(byte[] Message, TIndex Origin) {
        int ResultDataSize = (Message.length-Origin.Value)-MessageProtocolSuffixSize; 
        ResultData = new byte[ResultDataSize];
        System.arraycopy(Message,Origin.Value, ResultData,0, ResultDataSize); Origin.Value+=ResultDataSize;
        //.
        return SuccessCode_OK;
    }
    
    @Override
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException {
        if (CompletionHandler != null) 
        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_COMPLETED,ResultData).sendToTarget();        	
    	return SuccessCode_OK;
    }

    @Override
    public synchronized void DoOnOperationException(OperationException E) {
        if (CompletionHandler != null) 
        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_ERROR,E).sendToTarget();        	
    }
}
    
