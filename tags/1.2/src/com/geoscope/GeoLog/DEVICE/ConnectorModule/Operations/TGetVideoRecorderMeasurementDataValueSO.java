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
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurementDataValue;

/**
 *
 * @author ALXPONOM
 */
public class TGetVideoRecorderMeasurementDataValueSO extends TDeviceGetComponentDataByAddressDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,1101);
    
	public static final int OperationErrorCode_DataIsNotFound 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-1;
	public static final int OperationErrorCode_DataIsTooBig 	= TGeographServerServiceOperation.ErrorCode_CustomOperationError-2;
    
    public TGetVideoRecorderMeasurementDataValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress,pAddressData);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
    	TVideoRecorderMeasurementDataValue Value = new TVideoRecorderMeasurementDataValue(Connector.Device.VideoRecorderModule);
        return Value;
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
