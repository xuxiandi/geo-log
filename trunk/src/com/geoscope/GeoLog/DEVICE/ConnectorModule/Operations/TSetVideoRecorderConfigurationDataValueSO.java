/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

/**
 *
 * @author ALXPONOM
 */
public class TSetVideoRecorderConfigurationDataValueSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,1001);
    
    public TSetVideoRecorderConfigurationDataValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Connector.Device.VideoRecorderModule.ConfigurationDataValue;
    }
    
    public synchronized void DoOnOperationException(OperationException E) 
    {
        if (Connector.Device.VideoRecorderModule.CompletionHandler != null) 
        	Connector.Device.VideoRecorderModule.CompletionHandler.obtainMessage(TVideoRecorderModule.MESSAGE_OPERATION_ERROR,E).sendToTarget();        	
    }
}
