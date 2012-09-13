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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

/**
 *
 * @author ALXPONOM
 */

public class TLoadVideoRecorderConfigurationSO extends TObjectGetComponentDataServiceOperation
{
    public class TConfigurationValue extends TComponentValue
    {
        public TConfigurationValue() {
        }

        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        	Connector.Device.VideoRecorderModule.Mode.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Active.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Recording.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Audio.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Video.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Transmitting.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Saving.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.SDP.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.Receivers.FromByteArray(BA, Idx);
        	Connector.Device.VideoRecorderModule.SavingServer.FromByteArray(BA, Idx);
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    }
    
    public static TElementAddress _Address = new TElementAddress(2,9,1000);
    
    public TConfigurationValue Value = new TConfigurationValue();
    
    public TLoadVideoRecorderConfigurationSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
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
    
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException
    {
        if (Connector.Device.VideoRecorderModule.CompletionHandler != null) 
        	Connector.Device.VideoRecorderModule.CompletionHandler.obtainMessage(TVideoRecorderModule.MESSAGE_CONFIGURATION_RECEIVED,Connector.Device.VideoRecorderModule.Video).sendToTarget();        	
    	return SuccessCode_OK;
    }

    public synchronized void DoOnOperationException(OperationException E) 
    {
        if (Connector.Device.VideoRecorderModule.CompletionHandler != null) 
        	Connector.Device.VideoRecorderModule.CompletionHandler.obtainMessage(TVideoRecorderModule.MESSAGE_OPERATION_ERROR,E).sendToTarget();        	
    }
}
    
