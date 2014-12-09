package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;

public class TStreamChannel extends TChannel {

	protected TInternalSensorsModule InternalSensorsModule;
	//.
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = null;
	
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
	
	public synchronized void DestinationChannel_Set(com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel pDestinationChannel) {
		DestinationChannel = pDestinationChannel;
	}
	
	public synchronized com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel_Get() {
		return DestinationChannel;
	}

	public synchronized boolean DestinationChannel_IsConnected() {
		return ((DestinationChannel != null) && DestinationChannel.DestinationIsConnected());
	}
}
