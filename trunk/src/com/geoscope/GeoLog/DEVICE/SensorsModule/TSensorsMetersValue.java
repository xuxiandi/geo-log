package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TSensorsMetersValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsMetersValue() {
		super();
	}
	
	public TSensorsMetersValue(TSensorsModule pSensorsModule) {
		this();
		//.
		SensorsModule = pSensorsModule;
	}
}
