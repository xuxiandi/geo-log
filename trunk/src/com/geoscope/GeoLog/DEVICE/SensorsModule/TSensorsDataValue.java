package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TSensorsDataValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsDataValue(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}
}
