package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) throws Exception {
		if (TADSChannel.TypeID.equals(pTypeID))
			return (new TADSChannel()); //. ->
		if (TENVCChannel.TypeID.equals(pTypeID))
			return (new TENVCChannel()); //. ->
		if (TXENVCChannel.TypeID.equals(pTypeID))
			return (new TXENVCChannel()); //. ->
		if (TChannel.TypeIsTypeOfChannel(pTypeID, TTLRChannel.TypeID))
			return (new TTLRChannel()); //. ->
		if (TLUMChannel.TypeID.equals(pTypeID))
			return (new TLUMChannel()); //. ->
		if (TGPSChannel.TypeID.equals(pTypeID))
			return (new TGPSChannel()); //. ->
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel()); //. ->
		if (TH264IChannel.TypeID.equals(pTypeID))
			return (new TH264IChannel()); //. ->
		else
			return null; //. ->
	}	
}
