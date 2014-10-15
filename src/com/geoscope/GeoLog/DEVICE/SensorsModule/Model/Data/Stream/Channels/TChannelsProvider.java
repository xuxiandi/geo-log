package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {


	private TSensorsModule SensorsModule;
	
	public TChannelsProvider(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TADSChannel.TypeID.equals(pTypeID))
			return (new TADSChannel(SensorsModule)); // =>
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel(SensorsModule)); // =>
		if (TXENVCChannel.TypeID.equals(pTypeID))
			return (new TXENVCChannel(SensorsModule)); // =>
		else
			return null;
	}	
}
