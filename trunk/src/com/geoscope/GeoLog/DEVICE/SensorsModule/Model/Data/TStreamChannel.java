package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

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

		public void ProcessPacket(byte[] Packet, int PacketSize) throws IOException {
			synchronized (this) {
				DoOnPacket(Packet, PacketSize);
			}
		}

		public void ProcessPacket(byte[] Packet) throws IOException {
			ProcessPacket(Packet, Packet.length);
		}
	}
	
	public static class TPacketSubscribers {
	
		public static class TItemsNotifier {
			
			protected void DoOnSubscribe(TPacketSubscriber Subscriber) throws Exception {			
			}

			protected void DoOnSubscribed(TPacketSubscriber Subscriber) {			
			}

			protected void DoOnUnsubscribe(TPacketSubscriber Subscriber) throws Exception {			
			}

			protected void DoOnUnsubscribed(TPacketSubscriber Subscriber) {			
			}
		}
		
		private TStreamChannel Channel;
		//.
		protected ReentrantLock Lock = new ReentrantLock();
		//.
		private ArrayList<TPacketSubscriber> 	Items = new ArrayList<TPacketSubscriber>();
		private TItemsNotifier 					ItemsNotifier = null;
		//.
		public int SuspendCounter = 0;
		
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
		
		public void ClearItemsNotifier() {
			SetItemsNotifier(null);
		}
		
		private void ClearSubscribers() {
			Lock.lock();
			try {
				Items.clear();
			}
			finally {
				Lock.unlock();
			}
		}
		
		public void Subscribe(TPacketSubscriber Subscriber) throws Exception {
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnSubscribe(Subscriber);
			}
			//.
			boolean flStartSources;
			Lock.lock();
			try {
				flStartSources = (Items.size() == 0);
				//.
				Items.add(Subscriber);
				Subscriber.Channel = Channel;
			}
			finally {
				Lock.unlock();
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

		public void Unsubscribe(TPacketSubscriber Subscriber) throws Exception {
			synchronized (Items) {
				if (ItemsNotifier != null)
					ItemsNotifier.DoOnUnsubscribe(Subscriber);
			}
			//.
			boolean flStopSources;
			Lock.lock();
			try {
				Subscriber.Channel = null;
				//.
				Items.remove(Subscriber);
				//.
				flStopSources = (Items.size() == 0); 
			}
			finally {
				Lock.unlock();
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

		public int Count() {
			Lock.lock();
			try {
				return Items.size(); //. ->
			}
			finally {
				Lock.unlock();
			}
		}
		
		public int Suspend() {
			Lock.lock();
			try {
				SuspendCounter++;
				return SuspendCounter; //. ->  
			}
			finally {
				Lock.unlock();
			}
		}
		
		public int Resume() {
			Lock.lock();
			try {
				SuspendCounter--;
				return SuspendCounter; //. ->  
			}
			finally {
				Lock.unlock();
			}
		}
		
		public boolean IsSuspended() {
			Lock.lock();
			try {
				return (SuspendCounter > 0); //. ->  
			}
			finally {
				Lock.unlock();
			}
		}
		
		private void WaitForResume() throws InterruptedException {
			while (SuspendCounter > 0) {
				Lock.unlock();
				try {
					Thread.sleep(10);
				}
				finally {
					Lock.lock();
				}
			}
		}
		
		public void ProcessPacket(byte[] Packet) throws Exception {
			Lock.lock();
			try {
				WaitForResume();
				//.
				int Cnt = Items.size();
				int PacketSize = Packet.length;
				for (int I = 0; I < Cnt; I++)
					Items.get(I).ProcessPacket(Packet,PacketSize);
			}
			finally {
				Lock.unlock();
			}
		}

		public void ProcessPacket(byte[] Packet, int PacketSize) throws Exception {
			Lock.lock();
			try {
				WaitForResume();
				//.
				int Cnt = Items.size();
				for (int I = 0; I < Cnt; I++)
					Items.get(I).ProcessPacket(Packet,PacketSize);
			}
			finally {
				Lock.unlock();
			}
		}

		public void ProcessPacket(byte[] Packet, TPacketSubscriber Subscriber) throws Exception {
			Lock.lock();
			try {
				WaitForResume();
				//.
				Subscriber.ProcessPacket(Packet,Packet.length);
			}
			finally {
				Lock.unlock();
			}
		}

		public void ProcessPacket(byte[] Packet, int PacketSize, TPacketSubscriber Subscriber) throws Exception {
			Lock.lock();
			try {
				WaitForResume();
				//.
				Subscriber.ProcessPacket(Packet,PacketSize);
			}
			finally {
				Lock.unlock();
			}
		}
	}


	public TSensorsModule SensorsModule;
	
	public ArrayList<TChannel> 	SourceChannels = new ArrayList<TChannel>();
	protected int 				SourceChannels_StartCounter = 0;
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
			SourceChannels_StartCounter++;
			if (SourceChannels_StartCounter == 1) {
				int Cnt = SourceChannels.size();
				for (int I = 0; I < Cnt; I++)
					SourceChannels.get(I).StartSource();
			}
		}
	}
	
	public void SourceChannels_Stop() {
		synchronized (SourceChannels) {
			SourceChannels_StartCounter--;
			if (SourceChannels_StartCounter == 0) {
				int Cnt = SourceChannels.size();
				for (int I = 0; I < Cnt; I++)
					SourceChannels.get(I).StopSource();
			}
		}
	}
	
	@Override
	public boolean DestinationIsConnected() {
		return (PacketSubscribers.Count() > 0);
	}
	
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws Exception {
	}	
	
	public void DoStreaming(OutputStream pOutputStream, TCanceller Canceller) throws Exception {
		DoStreaming(pOutputStream, Canceller, -1);
	}	
	
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return null;
	}

	public int Suspend() {
		return PacketSubscribers.Suspend();
	}
	
	public int Resume() {
		return PacketSubscribers.Resume();
	}
	
	public boolean IsSuspended() {
		return PacketSubscribers.IsSuspended();
	}
	
	public void ProcessPacket(byte[] Packet, int PacketSize) throws Exception {
		PacketSubscribers.ProcessPacket(Packet, PacketSize);
	}

	public void ProcessPacket(byte[] Packet) throws Exception {
		ProcessPacket(Packet, Packet.length);
	}

	public void ProcessPacket(byte[] Packet, int PacketSize, TPacketSubscriber Subscriber) throws Exception {
		PacketSubscribers.ProcessPacket(Packet, PacketSize, Subscriber);
	}
	
	public void ProcessPacket(byte[] Packet, TPacketSubscriber Subscriber) throws Exception {
		PacketSubscribers.ProcessPacket(Packet, Packet.length, Subscriber);
	}
	
	protected byte[] DataType_ToByteArray(TDataType DataType) throws IOException {
		return null;
	}
	
	public void DoOnData(TDataType DataType) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		PacketSubscribers.ProcessPacket(BA);
	}

	public void DoOnData(TDataType DataType, TPacketSubscriber Subscriber) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		PacketSubscribers.ProcessPacket(BA, Subscriber);
	}
}
