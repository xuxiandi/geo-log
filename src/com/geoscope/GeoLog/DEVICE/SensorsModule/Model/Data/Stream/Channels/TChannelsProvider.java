package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

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
		if (TTLRChannel.TypeID.equals(pTypeID))
			return (new TTLRChannel(SensorsModule)); // =>
		if (TLUMChannel.TypeID.equals(pTypeID))
			return (new TLUMChannel(SensorsModule)); // =>
		if (TGPSChannel.TypeID.equals(pTypeID))
			return (new TGPSChannel(SensorsModule)); // =>
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel(SensorsModule)); // =>
		else
			return null;
	}	
}
