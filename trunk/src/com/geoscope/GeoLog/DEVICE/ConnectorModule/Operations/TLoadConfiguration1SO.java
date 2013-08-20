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

public class TLoadConfiguration1SO extends TObjectGetComponentDataServiceOperation
{
    public class TConfigurationValue extends TComponentValue
    {
    	public int 		idTOwnerComponent;
    	public int 		idOwnerComponent;
    	public int		idGeographServerObject;
        public short 	CheckpointInterval;
        public short 	ThresholdValue;
        //.
        public String 		GeographProxyServerAddress;
        public int 			GeographProxyServerPort;
        //.
        public String 		GeographDataServerAddress;
        public int 			GeographDataServerPort;
        
        public TConfigurationValue()
        {
        }

        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
        {
            idTOwnerComponent = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4; 
            idOwnerComponent = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=8; //. Int64
            idGeographServerObject = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=8; //. Int64
            CheckpointInterval = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
            ThresholdValue = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
            byte GeographProxyServerAddressSize = BA[Idx.Value]; Idx.Value++;
            GeographProxyServerAddress = null;
            if (GeographProxyServerAddressSize > 0) {
            	GeographProxyServerAddress = new String(BA,Idx.Value,GeographProxyServerAddressSize,"windows-1251");
            	Idx.Value += GeographProxyServerAddressSize; 
            }
            GeographProxyServerPort = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
            byte GeographDataServerAddressSize = BA[Idx.Value]; Idx.Value++;
            GeographDataServerAddress = null;
            if (GeographDataServerAddressSize > 0) {
            	GeographDataServerAddress = new String(BA,Idx.Value,GeographDataServerAddressSize,"windows-1251");
            	Idx.Value += GeographDataServerAddressSize; 
            }
            GeographDataServerPort = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    }
    
    public static TElementAddress _Address = new TElementAddress(2,1002);
    
    public TConfigurationValue Value = new TConfigurationValue();
    
    public TLoadConfiguration1SO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
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
        Connector.Device.idTOwnerComponent = Value.idTOwnerComponent;
        Connector.Device.idOwnerComponent = Value.idOwnerComponent;
        Connector.Device.idGeographServerObject = Value.idGeographServerObject;
        //.
        Connector.CheckpointInterval.SetValue(Value.CheckpointInterval);
        Connector.Device.GPSModule.Threshold.SetValue(Value.ThresholdValue);
        if (Value.GeographProxyServerAddress != null)
            Connector.Device.ConnectorModule.GeographProxyServerAddress = Value.GeographProxyServerAddress;
        Connector.Device.ConnectorModule.GeographProxyServerPort = Value.GeographProxyServerPort;
        if (Value.GeographDataServerAddress != null)
            Connector.Device.ConnectorModule.GeographDataServerAddress = Value.GeographDataServerAddress;
        Connector.Device.ConnectorModule.GeographDataServerPort = Value.GeographDataServerPort;
        try {
			Connector.Device.SaveConfiguration();
		} catch (Exception E) {
		}
        return SuccessCode_OK;
    }
}
    
