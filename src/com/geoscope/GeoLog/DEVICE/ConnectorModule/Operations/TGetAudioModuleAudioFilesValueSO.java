/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;

/**
 *
 * @author ALXPONOM
 */
public class TGetAudioModuleAudioFilesValueSO extends TDeviceGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,13,1101);
    
	public static final int OperationErrorCode_DataIsNotFound 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-1;
	public static final int OperationErrorCode_DataIsTooBig 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-2;
    
    public TGetAudioModuleAudioFilesValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
    	return Connector.Device.AudioModule.AudioFilesValue;    
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
