/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;

/**
 *
 * @author ALXPONOM
 */
public class TGetConnectorConfigurationDataValueSO extends TDeviceGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,3,1001);
    
    public TGetConnectorConfigurationDataValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Connector.Device.ConnectorModule.ConfigurationDataValue;
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
