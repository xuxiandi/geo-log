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
		
		public TStreamChannel Channel;
		
		public TPacketSubscriber() {
		}
		
		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {			
		}

		protected void DoOnPacket(byte[] Packet) throws IOException {
			DoOnPacket(Packet,Packet.length);
		}
	}
	
	public static class TPacketSubscribers {
	
		public static class TItemsNotifier {
			
			protected void DoOnSubscribe(TPacketSubscriber Subscriber) {			
			}

			protected void DoOnSubscribed(TPacketSubscriber Subscriber) {			
			}

			protected void DoOnUnsubscribe(TPacketSubscriber Subscriber) {			
			}

			protected void DoOnUnsubscribed(TPacketSubscriber Subscriber) {			
			}
		}
		
		private TStreamChannel Channel;
		//.
		private ArrayList<TPacketSubscriber> 	Items = new ArrayList<TPacketSubscriber>();
		private TItemsNotifier 					ItemsNotifier = null;
		
		public TPacketSubscribers(TStreamChannel pChannel) {
			Channel = pChannel;
		}
		
		public void Destroy() {
			ClearSubscribers();
		}

		public void SetItemsNotifier(TItemsNotifier pNotifier) {
			synchronized (Items) {
				ItemsNotifier = pNotifier;
			}
		}
		
		private synchronized void ClearSubscribers() {
			Items.clear();
		}
		
		public void Subscribe(TPacketSubscriber Subscriber) {
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnSubscribe(Subscriber);
			}
			//.
			boolean flStartSources;
			synchronized (this) {
				flStartSources = (Items.size() == 0);
				//.
				Items.add(Subscriber);
				Subscriber.Channel = Channel;
			}
			//.
			if (flStartSources)
				Channel.SourceChannels_Start();
			//.
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnSubscribed(Subscriber);
			}
		}

		public void Unsubscribe(TPacketSubscriber Subscriber) {
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnUnsubscribe(Subscriber);
			}
			//.
			boolean flStopSources;
			synchronized (this) {
				Subscriber.Channel = null;
				//.
				Items.remove(Subscriber);
				//.
				flStopSources = (Items.size() == 0); 
			}
			//.
			if (flStopSources)
				Channel.SourceChannels_Stop();
			//.
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnUnsubscribed(Subscriber);
			}
		}

		public synchronized int Count() {
			return Items.size();
		}
		
		public synchronized void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				Items.get(I).DoOnPacket(Packet,PacketSize);
		}

		public void DoOnPacket(byte[] Packet) throws IOException {
			DoOnPacket(Packet,Packet.length);
		}
	}


	public TSensorsModule SensorsModule;
	
	public ArrayList<TChannel> SourceChannels = new ArrayList<TChannel>();
	//.
	public TPacketSubscribers PacketSubscribers = new TPacketSubscribers(this);
	
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
	
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws IOException {
	}	
	
	public void DoStreaming(OutputStream pOutputStream, TCanceller Canceller) throws IOException {
		DoStreaming(pOutputStream, Canceller, -1);
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

	public void DoOnData(TDataType DataType, TPacketSubscriber Subscriber) throws IOException {
		byte[] BA = DataType_ToByteArray(DataType);
		synchronized (PacketSubscribers) {
			Subscriber.DoOnPacket(BA);
		}
	}
}
