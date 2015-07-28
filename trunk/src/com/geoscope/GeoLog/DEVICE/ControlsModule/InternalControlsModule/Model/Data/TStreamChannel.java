package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data;

import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TDestinationStreamChannel;

public class TStreamChannel extends TDestinationStreamChannel {

	protected TInternalControlsModule InternalControlsModule;
	
	public TStreamChannel(TInternalControlsModule pInternalControlsModule, int pID, Class<?> ChannelProfile) throws Exception {
		super(pID, TControlsModule.Channels_Folder(), ChannelProfile);
		//.
		InternalControlsModule = pInternalControlsModule;
	}
	
}
