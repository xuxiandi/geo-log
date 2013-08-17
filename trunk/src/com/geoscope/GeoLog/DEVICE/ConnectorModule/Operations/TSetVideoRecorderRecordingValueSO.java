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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

/**
 *
 * @author ALXPONOM
 */
public class TSetVideoRecorderRecordingValueSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,3);
    
    public TSetVideoRecorderRecordingValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue() throws OperationException
    {
    	if (Connector.Device.VideoRecorderModule.Security_UserAccessCodeIsActive())
    		throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
        return Connector.Device.VideoRecorderModule.Recording;
    }
    
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException
    {
        if (Connector.Device.VideoRecorderModule.CompletionHandler != null) 
        	Connector.Device.VideoRecorderModule.CompletionHandler.obtainMessage(TVideoRecorderModule.MESSAGE_OPERATION_COMPLETED,Connector.Device.VideoRecorderModule.Recording).sendToTarget();        	
    	return SuccessCode_OK;
    }

    public synchronized void DoOnOperationException(OperationException E) 
    {
        if (Connector.Device.VideoRecorderModule.CompletionHandler != null) 
        	Connector.Device.VideoRecorderModule.CompletionHandler.obtainMessage(TVideoRecorderModule.MESSAGE_OPERATION_ERROR,E).sendToTarget();        	
    }
}
