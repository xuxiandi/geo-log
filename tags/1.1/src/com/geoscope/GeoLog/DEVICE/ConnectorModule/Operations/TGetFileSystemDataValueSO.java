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
import com.geoscope.GeoLog.DEVICE.FileSystemModule.TFileSystemDataValue;

/**
 *
 * @author ALXPONOM
 */
public class TGetFileSystemDataValueSO extends TDeviceGetComponentDataByAddressDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,10,1000);
    
	public static final int OperationErrorCode_DataIsNotFound 		= TGeographServerServiceOperation.ErrorCode_CustomOperationError-1;
	public static final int OperationErrorCode_DataIsTooBig 		= TGeographServerServiceOperation.ErrorCode_CustomOperationError-2;
	public static final int OperationErrorCode_FTPTransmitterIsBusy = TGeographServerServiceOperation.ErrorCode_CustomOperationError-101;
    
    public TGetFileSystemDataValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress,pAddressData);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
    	TFileSystemDataValue Value = new TFileSystemDataValue(Connector.Device.FileSystemModule);
        return Value;
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
