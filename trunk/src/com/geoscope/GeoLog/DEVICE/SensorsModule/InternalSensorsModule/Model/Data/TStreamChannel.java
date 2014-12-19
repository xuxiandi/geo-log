package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data;

import java.io.IOException;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;

public class TStreamChannel extends TChannel {

	protected TInternalSensorsModule InternalSensorsModule;
	//.
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel 										DestinationChannel = null;
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscribers.TItemsNotifier 	DestinationChannel_PacketSubscribersItemsNotifier = null;  
	
	public TStreamChannel(TInternalSensorsModule pInternalSensorsModule) {
		InternalSensorsModule = pInternalSensorsModule;
	}
	
	@Override
	public void StartSource() {
		InternalSensorsModule.PostStart();
	}

	@Override
	public void StopSource() {
		InternalSensorsModule.PostStop();
	}
	
	public synchronized void DestinationChannel_Set(com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel pDestinationChannel) {
		DestinationChannel = pDestinationChannel;
		//.
		if ((DestinationChannel != null) && (DestinationChannel_PacketSubscribersItemsNotifier != null))
			DestinationChannel.PacketSubscribers.SetItemsNotifier(DestinationChannel_PacketSubscribersItemsNotifier);
	}
	
	public synchronized com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel_Get() {
		return DestinationChannel;
	}

	public synchronized boolean DestinationChannel_IsConnected() {
		return ((DestinationChannel != null) && DestinationChannel.DestinationIsConnected());
	}
	
	public synchronized void DestinationChannel_PacketSubscribersItemsNotifier_Set(com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscribers.TItemsNotifier Notifier) {
		DestinationChannel_PacketSubscribersItemsNotifier = Notifier;
		//.
		if ((DestinationChannel != null) && (DestinationChannel_PacketSubscribersItemsNotifier != null))
			DestinationChannel.PacketSubscribers.SetItemsNotifier(DestinationChannel_PacketSubscribersItemsNotifier);
	}

	public void DoOnData(TDataType DataType) throws IOException {
		com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = DestinationChannel_Get();
		if (DestinationChannel != null) 
			DestinationChannel.DoOnData(DataType);
		else
			throw new IOException("DoOnData() error, DestinationChannel is not set"); //. =>
	}

	public void DoOnData(TDataType DataType, com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber Subscriber) throws IOException {
		com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = DestinationChannel_Get();
		if (DestinationChannel != null) 
			DestinationChannel.DoOnData(DataType, Subscriber);
		else
			throw new IOException("DoOnData() error, DestinationChannel is not set"); //. =>
	}
}
