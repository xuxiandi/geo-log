package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels;

import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Telecontrol.TLC.TTLCChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel;

public class TChannelsProvider extends com.geoscope.Classes.Data.Stream.Channel.TChannelProvider {

	@SuppressWarnings("unused")
	private TControlsModule ControlsModule;
	
	public TChannelsProvider(TControlsModule pControlsModule) {
		ControlsModule = pControlsModule;
	}
	
	@Override
	public TStreamChannel GetChannel(String pTypeID) {
		if (TDVRTChannel.TypeID.equals(pTypeID))
			return (new TDVRTChannel()); //. ->
		if (TTLCChannel.TypeID.equals(pTypeID))
			return (new TTLCChannel()); //. ->
		if (TVCChannel.TypeID.equals(pTypeID))
			return (new TVCChannel()); //. ->
		if (TVCTRLChannel.TypeID.equals(pTypeID))
			return (new TVCTRLChannel()); //. ->
		else
			return null; //. ->
	}	
}
