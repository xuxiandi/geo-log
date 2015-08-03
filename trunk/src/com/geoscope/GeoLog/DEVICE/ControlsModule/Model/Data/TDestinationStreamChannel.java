package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TDestinationStreamChannel extends TChannel {

	public static final int NextID = 4; //. a next unique channel ID

	
	public TDestinationStreamChannel() {
		super();
	}
	
	public TDestinationStreamChannel(int pID, String pLocationID, String pProfilesFolder, Class<?> ProfileClass) throws Exception {
		super(pID, pLocationID, pProfilesFolder, ProfileClass);
	}
	
	@Override
	public boolean IsActive() { 
		return false;
	}
}
