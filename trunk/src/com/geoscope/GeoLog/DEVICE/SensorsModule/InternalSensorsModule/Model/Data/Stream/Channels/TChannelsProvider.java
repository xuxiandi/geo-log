package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	private TInternalSensorsModule InternalSensorsModule;
	
	public TChannelsProvider(TInternalSensorsModule pInternalSensorsModule) {
		InternalSensorsModule = pInternalSensorsModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TADSChannel.TypeID.equals(pTypeID))
			return (new TADSChannel(InternalSensorsModule)); // =>
		if (TXENVCChannel.TypeID.equals(pTypeID))
			return (new TXENVCChannel(InternalSensorsModule)); // =>
		else
			return null;
	}	
}
