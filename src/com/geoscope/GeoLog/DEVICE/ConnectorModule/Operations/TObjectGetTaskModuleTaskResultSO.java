/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 * @author ALXPONOM
 */

public class TObjectGetTaskModuleTaskResultSO extends TObjectGetComponentDataServiceOperation {
	
    public static TElementAddress _Address = new TElementAddress(2,16,1002);
    
    public TObjectGetTaskModuleTaskResultSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
    }
    
    @Override
    public TElementAddress Address() {
        return _Address.AddRight(super.Address());
    }
        
    @Override
    public synchronized TComponentValue getValue() {
        return Connector.Device.TaskModule.TaskResult;
    }
    
    @Override
    protected synchronized int ParseData(byte[] Message, TIndex Origin) {
        int ResultCode = super.ParseData(Message,/*ref*/ Origin);
        if (ResultCode != SuccessCode_OK)
            return ResultCode; //. ->
        //. setting values
        return SuccessCode_OK;
    }
}
    
