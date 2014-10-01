package com.geoscope.GeoLog.DEVICE.PluginsModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;

public class TPluginsDataValue extends TComponentTimestampedDataValue {

	protected TPluginsModule PluginsModule;
	
	public TPluginsDataValue(TPluginsModule pPluginsModule) {
		PluginsModule = pPluginsModule;
	}
}
