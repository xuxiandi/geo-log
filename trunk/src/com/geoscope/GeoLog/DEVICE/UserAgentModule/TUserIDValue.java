package com.geoscope.GeoLog.DEVICE.UserAgentModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt32Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TUserIDValue extends TComponentTimestampedInt32Value {

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
}
