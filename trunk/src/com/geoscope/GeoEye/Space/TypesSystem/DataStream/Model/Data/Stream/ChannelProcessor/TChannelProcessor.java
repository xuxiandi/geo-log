package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.ChannelProcessor;

import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;

public class TChannelProcessor {

	protected TStreamChannel Channel;
	
	public TChannelProcessor(TStreamChannel pChannel) {
		Channel = pChannel;
	}
	
	public void Destroy() throws Exception {
	}
}
