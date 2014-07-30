/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TAudioFilesValue extends TComponentTimestampedDataValue {
	
    private TAudioModule AudioModule;

	public TAudioFilesValue(TComponent pOwner, int pID) {
    	super(pOwner, pID, "AudioFiles");
    	//.
    	flVirtualValue = true;
	}

	public TAudioFilesValue() {
	}

    public TAudioFilesValue(TAudioModule pAudioModule) {
    	AudioModule = pAudioModule;
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        super.FromByteArray(BA,/*ref*/ Idx);
        _FromByteArray(Value);
    }
    
    public synchronized void _FromByteArray(byte[] BA) throws IOException, OperationException {
    	AudioModule.AudioFiles_FromByteArray(BA);
    }
    
}
