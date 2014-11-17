package com.geoscope.GeoLog.DEVICE.AlarmModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TAlarmDataValue extends TComponentTimestampedDataValue {

	protected TAlarmModule AlarmModule;
	
	public TAlarmDataValue() {
		super();
	}
	
	public TAlarmDataValue(TAlarmModule pAlarmModule) {
		this();
		//.
		AlarmModule = pAlarmModule;
	}
}
