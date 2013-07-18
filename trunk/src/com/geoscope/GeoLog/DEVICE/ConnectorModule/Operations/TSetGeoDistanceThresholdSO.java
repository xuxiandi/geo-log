/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectComponentServiceOperation;

/**
 *
 * @author ALXPONOM
 */
public class TSetGeoDistanceThresholdSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,4);
    
    public TSetGeoDistanceThresholdSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession, pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Connector.Device.GPSModule.Threshold;
    }
    
    @Override
    public TObjectComponentServiceOperation GetObjectComponentServiceOperation() throws Exception {
    	TObjectSetGeoDistanceThresholdSO Operation = new TObjectSetGeoDistanceThresholdSO(Connector, Connector.Device.UserID,Connector.Device.UserPassword, Connector.Device.ObjectID, SubAddress);
    	Operation.setValue(getValue());
    	return Operation;
    }    
}
