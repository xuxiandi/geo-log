package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels;

import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	public static final TChannelsProvider Instance = new TChannelsProvider();
	
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) throws Exception {
		if (TAACChannel.TypeID.equals(pTypeID))
			return (new TAACChannel()); // =>
		if (TH264IChannel.TypeID.equals(pTypeID))
			return (new TH264IChannel()); // =>
		else
			return null;
	}	
}
