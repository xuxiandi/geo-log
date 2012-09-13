/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;
import com.geoscope.GeoLog.Utils.OleDate;

/**
 *
 * @author ALXPONOM
 */
public class TGetVideoRecorderMeasurementsListValueSO extends TDeviceGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,9,1100);
    
    public TGetVideoRecorderMeasurementsListValueSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
    } 
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
    	String MeasurementsList = Connector.Device.VideoRecorderModule.Measurements_GetList();
    	TComponentTimestampedANSIStringValue Value = new TComponentTimestampedANSIStringValue(OleDate.UTCCurrentTimestamp(),MeasurementsList);
        return Value;
    }

    public synchronized TComponentValue getValueBySubAddress()
    {
        return null;
    }
}
