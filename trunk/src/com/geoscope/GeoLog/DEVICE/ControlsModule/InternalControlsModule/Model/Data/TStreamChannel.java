package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data;

import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TDestinationStreamChannel;

public class TStreamChannel extends TDestinationStreamChannel {

	protected TInternalControlsModule InternalControlsModule;
	
	public TStreamChannel(TInternalControlsModule pInternalControlsModule, int pID, String pLocationID, Class<?> ChannelProfile) throws Exception {
		super(pID, pLocationID, TControlsModule.Channels_Folder(), ChannelProfile);
		//.
		InternalControlsModule = pInternalControlsModule;
	}
	
}
