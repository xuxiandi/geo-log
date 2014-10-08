package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TStreamChannel extends TChannel {

	public static class TPacketSubscriber {
		
		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {			
		}
	}
	
	public static class TPacketSubscribers {
	
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
		}

		public synchronized void Unsubscribe(TPacketSubscriber Subscriber) {
			Items.remove(Subscriber);
		}

		public synchronized void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				Items.get(I).DoOnPacket(Packet,PacketSize);
		}

		public synchronized void DoOnPacket(byte[] Packet) throws IOException {
			DoOnPacket(Packet,0);
		}
	}


	public TPacketSubscribers PacketSubscribers = new TPacketSubscribers();
	
	public void DoStreaming(final OutputStream pOutputStream, TCanceller Canceller) throws IOException {
	}	
}
