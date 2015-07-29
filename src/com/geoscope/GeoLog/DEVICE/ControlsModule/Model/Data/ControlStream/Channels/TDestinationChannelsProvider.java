package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TDestinationStreamChannel;

public class TDestinationChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TDestinationChannelsProvider Instance = new TDestinationChannelsProvider();

	
	@SuppressWarnings("unused")
	private TControlsModule ControlsModule;
	
	public TDestinationChannelsProvider() {
		ControlsModule = null;
	}
	
	public TDestinationChannelsProvider(TControlsModule pControlsModule) {
		ControlsModule = pControlsModule;
	}
	
	@Override
	public TDestinationStreamChannel GetChannel(String pTypeID) throws Exception {
		TDestinationStreamChannel Result = com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.TChannelsProvider.Instance.GetChannel(pTypeID);
		//.
		return Result;
	}	
}
