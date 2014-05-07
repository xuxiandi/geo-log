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

/**
 *
 * @author ALXPONOM
 */
public class TSetAudioModuleDestinationsVolumesValueSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,13,1002);
    
    public TSetAudioModuleDestinationsVolumesValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Connector.Device.AudioModule.DestinationsVolumesValue;
    }

    @Override
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException
    {
    	try {
    		Connector.Device.AudioModule.SaveConfigurationLocally();
    	}
    	catch (Throwable T) {
    		Connector.Device.Log.WriteError("AudioModule.SaveConfigurationLocally()",T.getMessage());
    	}
    	return SuccessCode_OK;
    }    
}
