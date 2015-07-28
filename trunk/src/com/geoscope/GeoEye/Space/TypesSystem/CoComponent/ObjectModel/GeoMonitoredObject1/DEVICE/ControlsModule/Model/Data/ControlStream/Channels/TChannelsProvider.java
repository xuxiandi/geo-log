package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Telecontrol.TLC.TTLCChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TDVRTChannel.TypeID.equals(pTypeID))
			return (new TDVRTChannel()); //. ->
		if (TTLCChannel.TypeID.equals(pTypeID))
			return (new TTLCChannel()); //. ->
		else
			return null;
	}	
}
