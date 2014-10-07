package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedXMLDataValue;

public class TSensorsDataValue extends TComponentTimestampedXMLDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsDataValue(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}
}
