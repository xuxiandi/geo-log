package com.geoscope.GeoLog.DEVICE.ControlsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TControlsDataValue extends TComponentTimestampedDataValue {

	protected TControlsModule ControlsModule;
	
	public TControlsDataValue(TControlsModule pControlsModule) {
		ControlsModule = pControlsModule;
	}
}
