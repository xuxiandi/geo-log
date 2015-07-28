package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TDestinationStreamChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	private TInternalControlsModule InternalControlsModule;
	
	public TChannelsProvider() {
		InternalControlsModule = null;
	}
	
	public TChannelsProvider(TInternalControlsModule pInternalControlsModule) {
		InternalControlsModule = pInternalControlsModule;
	}
	
	@Override
	public TDestinationStreamChannel GetChannel(String pTypeID) throws Exception {
		if (TVCChannel.TypeID.equals(pTypeID))
			return (new TVCChannel(InternalControlsModule, -1/*NoID*/)); //. ->
		else
			return null; //. ->
	}	
}
