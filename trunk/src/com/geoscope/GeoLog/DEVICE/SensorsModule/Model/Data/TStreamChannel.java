package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TStreamChannel extends TChannel {

	public static class TPacketSubscriber {
		
		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {			
		}
	}
	
	public class TPacketSubscribers {
	
		private ArrayList<TPacketSubscriber> Items = new ArrayList<TPacketSubscriber>();
		
		public TPacketSubscribers() {
		}
		
		public void Destroy() {
			ClearSubscribers();
		}
		
		private synchronized void ClearSubscribers() {
			Items = new ArrayList<TPacketSubscriber>();
		}
		
		public synchronized void Subscribe(TPacketSubscriber Subscriber) {
			Items.add(Subscriber);
			//.
			if (Items.size() == 1)
				SourceChannels_Start();
				
		}

		public synchronized void Unsubscribe(TPacketSubscriber Subscriber) {
			Items.remove(Subscriber);
			//.
			if (Items.size() == 0)
				SourceChannels_Stop();
		}

		public synchronized int Count() {
			return Items.size();
		}
		
		public synchronized void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				Items.get(I).DoOnPacket(Packet,PacketSize);
		}

		public synchronized void DoOnPacket(byte[] Packet) throws IOException {
			DoOnPacket(Packet,Packet.length);
		}
	}


	public TSensorsModule SensorsModule;
	
	public ArrayList<TChannel> SourceChannels = new ArrayList<TChannel>();
	//.
	public TPacketSubscribers PacketSubscribers = new TPacketSubscribers();
	
	public TStreamChannel(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}
	
	public void SourceChannels_Add(TChannel SC) {
		synchronized (SourceChannels) {
			SourceChannels.add(SC);
		}
	}
	
	public void SourceChannels_Remove(TChannel SC) {
		synchronized (SourceChannels) {
			SourceChannels.remove(SC);
		}
	}
	
	public void SourceChannels_Start() {
		synchronized (SourceChannels) {
			int Cnt = SourceChannels.size();
			for (int I = 0; I < Cnt; I++)
				SourceChannels.get(I).StartSource();
		}
	}
	
	public void SourceChannels_Stop() {
		synchronized (SourceChannels) {
			int Cnt = SourceChannels.size();
			for (int I = 0; I < Cnt; I++)
				SourceChannels.get(I).StopSource();
		}
	}
	
	@Override
	public boolean DestinationIsConnected() {
		return (PacketSubscribers.Count() > 0);
	}
	
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller) throws IOException {
	}	
	
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return null;
	}

	protected byte[] DataType_ToByteArray(TDataType DataType) throws IOException {
		return null;
	}
	
	public void DoOnData(TDataType DataType) throws IOException {
		byte[] BA = DataType_ToByteArray(DataType);
		PacketSubscribers.DoOnPacket(BA);
	}
}
