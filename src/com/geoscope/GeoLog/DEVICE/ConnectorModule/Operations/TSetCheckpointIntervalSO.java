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
public class TSetCheckpointIntervalSO extends TDeviceSetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,3,2);
    
    public TSetCheckpointIntervalSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Connector.CheckpointInterval;
    }

    @Override
    public TObjectComponentServiceOperation GetObjectComponentServiceOperation() throws Exception {
    	TObjectSetCheckpointSO Operation = new TObjectSetCheckpointSO(Connector, UserID,UserPassword, ObjectID, SubAddress);
    	Operation.setValue(getValue());
    	return Operation;
    }    
}
