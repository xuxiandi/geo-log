package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

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

			protected void DoOnSubscribed(TPacketSubscriber Subscriber) throws Exception {			
			}

			protected void DoOnUnsubscribe(TPacketSubscriber Subscriber) throws Exception {			
			}

			protected void DoOnUnsubscribed(TPacketSubscriber Subscriber) throws Exception {			
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
			boolean flStartSource;
			Lock.lock();
			try {
				flStartSource = (Items.size() == 0);
				//.
				Items.add(Subscriber);
				Subscriber.Channel = Channel;
			}
			finally {
				Lock.unlock();
			}
			//.
			if (flStartSource)
				Channel.SourceChannel_Start();
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
				Channel.SourceChannel_Stop();
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
	
	protected TSourceStreamChannel	SourceChannel = null;
	protected int 					SourceChannel_StartCounter = 0;
	//.
	public TPacketSubscribers PacketSubscribers = new TPacketSubscribers(this);
	
	public TStreamChannel(TSensorsModule pSensorsModule) {
		SensorsModule = pSensorsModule;
	}

	public synchronized boolean StreamableViaComponent() {
		return ((SourceChannel != null) && SourceChannel.Profile.StreamableViaComponent);
	}
	
	@Override
	public void Profile_FromByteArray(byte[] BA) throws Exception {
		SourceChannel_Profile_FromByteArray(BA);
	}
	
	@Override
	public void Profile_FromXMLNode(Node ANode) throws Exception {
		SourceChannel_Profile_FromXMLNode(ANode);
	}
	
	@Override
	public byte[] Profile_ToByteArray() throws Exception {
		return SourceChannel_Profile_ToByteArray();
	}
	
	@Override
	public void Profile_ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		SourceChannel_Profile_ToXMLSerializer(Serializer);
	}
	
	public void SourceChannel_Set(TSourceStreamChannel SC) {
		synchronized (this) {
			SourceChannel = SC;
		}
	}
	
	public void SourceChannel_Clear(TSourceStreamChannel SC) {
		synchronized (this) {
			if (SC == SourceChannel)
				SourceChannel = null;
		}
	}
	
	public TSourceStreamChannel SourceChannel_Get() {
		synchronized (this) {
			return SourceChannel; //. ->
		}
	}
	
	public void SourceChannel_Start() {
		synchronized (this) {
			SourceChannel_StartCounter++;
			if (SourceChannel_StartCounter == 1) {
				if (SourceChannel != null)
					SourceChannel.StartSource();
			}
		}
	}
	
	public void SourceChannel_Stop() {
		synchronized (this) {
			SourceChannel_StartCounter--;
			if (SourceChannel_StartCounter == 0) {
				if (SourceChannel != null)
					SourceChannel.StopSource();
			}
		}
	}
	
	public void SourceChannel_Profile_FromByteArray(byte[] BA) throws Exception {
		synchronized (this) {
			if (SourceChannel != null)
				SourceChannel.Profile_FromByteArray(BA);
		}
	}
	
	public void SourceChannel_Profile_FromXMLNode(Node ANode) throws Exception {
		synchronized (this) {
			if (SourceChannel != null)
				SourceChannel.Profile_FromXMLNode(ANode);
		}
	}
	
	public byte[] SourceChannel_Profile_ToByteArray() throws Exception {
		synchronized (this) {
			if (SourceChannel != null)
				return SourceChannel.Profile_ToByteArray(); //. ->
			else
				return null; //. ->
		}
	}
	
	public void SourceChannel_Profile_ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		synchronized (this) {
			if (SourceChannel != null)
				SourceChannel.Profile_ToXMLSerializer(Serializer);
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
