package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	@SuppressWarnings("unused")
	private TControlsModule ControlsModule;
	
	public TChannelsProvider(TControlsModule pControlsModule) {
		ControlsModule = pControlsModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TDVRTChannel.TypeID.equals(pTypeID))
			return (new TDVRTChannel()); // =>
		else
			return null;
	}	
}
