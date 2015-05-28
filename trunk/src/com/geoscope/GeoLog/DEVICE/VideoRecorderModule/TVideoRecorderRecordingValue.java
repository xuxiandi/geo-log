package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.IOException;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderActiveValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TVideoRecorderRecordingValue extends TComponentTimestampedBooleanValue {

	protected TVideoRecorderModule VideoRecorderModule;
	//.
	public boolean flPreview = false;
	
	public TVideoRecorderRecordingValue(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	super.FromByteArray(BA, Idx);
    	//.
    	if ((Value != 0) && (!VideoRecorderModule.flEnabled)) { 
    		Value = 0;
    		throw new OperationException(TSetVideoRecorderActiveValueSO.OperationErrorCode_VideoRecorderIsDisabled,VideoRecorderModule.Device.context.getString(R.string.SVideoRecorderIsDisabled)); //. =>
    	}
    	//.
    	flPreview = false;
    }
}
