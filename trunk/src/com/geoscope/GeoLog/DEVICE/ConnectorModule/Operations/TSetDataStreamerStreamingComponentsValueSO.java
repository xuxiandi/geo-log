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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;

/**
 *
 * @author ALXPONOM
 */
public class TSetDataStreamerStreamingComponentsValueSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,17,1);
    
	public static final int OperationErrorCode_DataStreamerIsDisabled = TGeographServerServiceOperation.ErrorCode_CustomOperationError-1;
	
    public TSetDataStreamerStreamingComponentsValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue() throws OperationException
    {
        return Connector.Device.DataStreamerModule.StreamingComponentsValue;
    }
    
    public synchronized void DoOnOperationException(OperationException E) 
    {
    }
}
