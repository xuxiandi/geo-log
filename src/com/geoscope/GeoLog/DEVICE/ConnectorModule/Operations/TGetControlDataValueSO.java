/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ControlModule.TControlDataValue;

/**
 *
 * @author ALXPONOM
 */
public class TGetControlDataValueSO extends TDeviceGetComponentDataByAddressDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,11,1000);
    
	public static final int OperationErrorCode_DataIsNotFound 			= TGeographServerServiceOperation.ErrorCode_CustomOperationError-1;
	public static final int OperationErrorCode_DataIsTooBig 			= TGeographServerServiceOperation.ErrorCode_CustomOperationError-2;
	public static final int OperationErrorCode_TimeoutIsExpired 		= TGeographServerServiceOperation.ErrorCode_CustomOperationError-3;
	public static final int OperationErrorCode_LANConnectionIsNotFound 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-4;
	public static final int OperationErrorCode_SourceIsUnavaiable 		= TGeographServerServiceOperation.ErrorCode_CustomOperationError-5;
	public static final int OperationErrorCode_SourceAccessIsDenied 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-6;
	public static final int OperationErrorCode_SourceIsBusy 			= TGeographServerServiceOperation.ErrorCode_CustomOperationError-7;
	public static final int OperationErrorCode_SourceIsTimedout 		= TGeographServerServiceOperation.ErrorCode_CustomOperationError-8;
    
    public TGetControlDataValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress,pAddressData);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
    	TControlDataValue Value = new TControlDataValue(Connector.Device.ControlModule);
        return Value;
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
