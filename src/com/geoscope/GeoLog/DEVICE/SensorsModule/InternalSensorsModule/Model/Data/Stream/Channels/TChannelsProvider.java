package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels;

import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TInternalSensorsModule InternalSensorsModule;
	
	public TChannelsProvider() {
		InternalSensorsModule = null;
	}
	
	public TChannelsProvider(TInternalSensorsModule pInternalSensorsModule) {
		InternalSensorsModule = pInternalSensorsModule;
	}
	
	@Override
	public TSourceStreamChannel GetChannel(String pTypeID) throws Exception {
		if (TADSChannel.TypeID.equals(pTypeID))
			return (new TADSChannel(InternalSensorsModule)); // =>
		if (TXENVCChannel.TypeID.equals(pTypeID))
			return (new TXENVCChannel(InternalSensorsModule)); // =>
		if (TTLRChannel.TypeID.equals(pTypeID))
			return (new TTLRChannel(InternalSensorsModule)); // =>
		if (TLUMChannel.TypeID.equals(pTypeID))
			return (new TLUMChannel(InternalSensorsModule)); // =>
		if (TGPSChannel.TypeID.equals(pTypeID))
			return (new TGPSChannel(InternalSensorsModule)); // =>
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel(InternalSensorsModule)); // =>
		if (TH264IChannel.TypeID.equals(pTypeID))
			return (new TH264IChannel(InternalSensorsModule)); // =>
		else
			return null;
	}	
}
