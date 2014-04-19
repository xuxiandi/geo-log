/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataByAddressDataServiceOperation;

/**
 * @author ALXPONOM
 */
public class TSetTaskModuleTaskStatusSO extends TDeviceSetComponentDataByAddressDataServiceOperation {
	
    public static TElementAddress _Address = new TElementAddress(2,16,1001);
    
    public TSetTaskModuleTaskStatusSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData) {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress,pAddressData);
    }
    
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue() {
        return Connector.Device.TaskModule.TaskStatus;
    }
}
