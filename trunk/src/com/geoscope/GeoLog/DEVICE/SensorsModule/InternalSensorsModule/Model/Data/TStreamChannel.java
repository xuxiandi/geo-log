package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;

public class TStreamChannel extends TChannel {

	protected TInternalSensorsModule InternalSensorsModule;
	//.
	public com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = null;
	
	public TStreamChannel(TInternalSensorsModule pInternalSensorsModule) {
		InternalSensorsModule = pInternalSensorsModule;
	}
	
	@Override
	public void StartSource() {
		InternalSensorsModule.PostStart();
	}

	@Override
	public void StopSource() {
		InternalSensorsModule.PostStop();
	}
}
