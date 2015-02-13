package com.geoscope.GeoLog.DEVICE.UserAgentModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedXMLDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TUserDataValue extends TComponentTimestampedXMLDataValue {

	public TUserDataValue(TComponent pOwner, int pID) {
		super(pOwner, pID, "UserDataValue", false);
	}

	public TUserDataValue() {
		super();
	}

    public TUserDataValue(double pTimestamp, byte[] pValue) {
    	super(pTimestamp,pValue);
    }

    public TUserDataValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        super(BA,/*ref*/ Idx);
    }
}
