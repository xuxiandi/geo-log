package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TSensorsDataValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsDataValue() {
		super();
	}
	
	public TSensorsDataValue(TSensorsModule pSensorsModule) {
		this();
		//.
		SensorsModule = pSensorsModule;
	}
}
