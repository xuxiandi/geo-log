/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TLoadConfigurationSO extends TObjectGetComponentDataServiceOperation
{
    public class TConfigurationValue extends TComponentValue
    {
    	public int		idGeographServerObject;
        public short 	CheckpointInterval;
        public short 	ThresholdValue;
        
        public TConfigurationValue()
        {
        }

        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
        {
            idGeographServerObject = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=8; //. Int64
            CheckpointInterval = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
            ThresholdValue = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    }
    
    public static TElementAddress _Address = new TElementAddress(2,1000);
    
    public TConfigurationValue Value = new TConfigurationValue();
    
    public TLoadConfigurationSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized TComponentValue getValue()
    {
        return Value;
    }
    
    protected synchronized int ParseData(byte[] Message, TIndex Origin) 
    {
        int ResultCode = super.ParseData(Message,/*ref*/ Origin);
        if (ResultCode != SuccessCode_OK)
            return ResultCode; //. ->
        //. setting values
        Connector.Device.idGeographServerObject = Value.idGeographServerObject;
        Connector.CheckpointInterval.SetValue(Value.CheckpointInterval);
        Connector.Device.GPSModule.Threshold.SetValue(Value.ThresholdValue);
        try {
			Connector.Device.SaveConfiguration();
		} catch (Exception E) {
		}
        return SuccessCode_OK;
    }
}
    
