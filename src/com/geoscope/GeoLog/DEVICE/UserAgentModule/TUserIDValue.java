package com.geoscope.GeoLog.DEVICE.UserAgentModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt32Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TUserIDValue extends TComponentTimestampedInt32Value {

	private int Last_Value = 0;
	
	public TUserIDValue(TComponent pOwner, int pID) {
		super(pOwner, pID, "UserIDValue");
	}

	public TUserIDValue() {
		super();
	}

    public TUserIDValue(double pTimestamp, int pValue) {
    	super(pTimestamp,pValue);
    }

    public TUserIDValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        super(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void SetValue(double pTimestamp, int pValue) {
    	Last_Value = Value;
    	if (pValue == Value)
    		return; //. ->
    	//.
    	super.SetValue(pTimestamp, pValue);
    }
    
    public synchronized boolean IsChanged() {
    	return (Last_Value != Value);
    }
}
