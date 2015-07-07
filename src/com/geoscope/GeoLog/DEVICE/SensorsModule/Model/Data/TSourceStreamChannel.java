package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TSourceStreamChannel extends TChannel {

	public TSourceStreamChannel() {
		super();
	}
	
	public TSourceStreamChannel(int pID, String pProfilesFolder, Class<?> ProfileClass) throws Exception {
		super(pID, pProfilesFolder, ProfileClass);
	}
}
