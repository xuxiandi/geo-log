package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.IOException;
import java.util.ArrayList;

import android.hardware.Camera.Size;

public class MediaFrameServer {

	public static class TPacketSubscriber {
		
		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {			
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

		public synchronized void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				Items.get(I).DoOnPacket(Packet,PacketSize, PacketTimestamp);
		}
	}

	
	public static class TSamplePacket {
		
		public long 	Timestamp = 0;
		public int 		Format = 0;
		public byte[] 	Data = new byte[0];
		public int		DataSize = 0;
		
		public synchronized void Set(byte[] pData, int pDataSize, long pTimestamp) {
			Timestamp = pTimestamp; 
			//.
			DataSize = pDataSize;
			if (DataSize > Data.length)
				Data = new byte[DataSize]; 
			System.arraycopy(pData,0, Data,0, DataSize);
			//.
			this.notifyAll();
		}
	}
	
	public static class TFrame {
		
		public long 	Timestamp = 0;
		public int		Width = 0;
		public int		Height = 0;
		public int 		Format = 0;
		public byte[] 	Data = new byte[0];
		public int		DataSize = 0;
		
		public synchronized void Set(int pWidth, int pHeight, int pFormat, byte[] pData, int pDataSize, long pTimestamp) {
			Timestamp = pTimestamp;
			//.
			Width = pWidth;
			Height = pHeight;
			Format = pFormat;
			//.
			DataSize = pDataSize;
			if (DataSize > Data.length)
				Data = new byte[DataSize]; 
			System.arraycopy(pData,0, Data,0, DataSize);
			//.
			this.notifyAll();
		}
	}
	
	//. audio sample output
	public static boolean 	flAudioActive = false;
	public static int 		SampleRate = 0;
	public static int 		SamplePacketInterval = 0;
	public static int 		SampleBitRate = 0;
	//.
	public static TSamplePacket 		CurrentSamplePacket = new TSamplePacket();
	public static TPacketSubscribers 	CurrentSamplePacketSubscribers = new TPacketSubscribers();
	
	//. video frame output
	public static boolean 	flVideoActive = false;
	public static Size 		FrameSize = null;
	public static int 		FrameRate = 0;
	public static int 		FrameInterval = 0;
	public static int 		FrameBitRate = 0;
	//.
	public static TFrame 				CurrentFrame = new TFrame();
	public static TPacketSubscribers 	CurrentFrameSubscribers = new TPacketSubscribers();
	
}
