package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Rect;
import android.view.Surface;

import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264EncoderServer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

public class TMediaFrameServer {

	private static boolean flUseH264EncoderServer = true;
	
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
		public byte[] 	Data = new byte[0];
		public int		DataSize = 0;
		
		public synchronized void Set(int pWidth, int pHeight, byte[] pData, int pDataSize, long pTimestamp) {
			Timestamp = pTimestamp;
			//.
			Width = pWidth;
			Height = pHeight;
			//.
			DataSize = pDataSize;
			if (DataSize > Data.length)
				Data = new byte[DataSize]; 
			System.arraycopy(pData,0, Data,0, DataSize);
			//.
			this.notifyAll();
		}
	}
	
	
	public TVideoRecorderModule VideoRecorderModule;
	//. audio sample output
	public boolean 	flAudioActive = false;
	public int 		SampleRate = 0;
	public int 		SamplePacketInterval = 0;
	public int 		SampleBitRate = 0;
	//.
	public TSamplePacket 		CurrentSamplePacket = new TSamplePacket();
	public TPacketSubscribers 	CurrentSamplePacketSubscribers = new TPacketSubscribers();
	
	//. video frame output
	public boolean 	flVideoActive = false;
	public Rect 	FrameSize = null;
	public int 		FrameRate = 0;
	public int 		FrameInterval = 0;
	public int 		FrameBitRate = 0;
	public int 		FramePixelFormat = 0;
	//.
	public TFrame 				CurrentFrame = new TFrame();
	public TPacketSubscribers 	CurrentFrameSubscribers = new TPacketSubscribers();
	//.
	public TH264EncoderServer 						H264EncoderServer = null;
	private ArrayList<TH264EncoderServer.TClient> 	H264EncoderServer_Clients = new ArrayList<TH264EncoderServer.TClient>(); 
	
	public TMediaFrameServer(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}
	
	public void Destroy() throws IOException, InterruptedException {
		H264EncoderServer_Stop();
	}
	
	public boolean H264EncoderServer_IsAvailable() {
		return (flUseH264EncoderServer && TH264EncoderServer.IsSupported()); 
	}
	
	public synchronized TH264EncoderServer H264EncoderServer_Start(android.hardware.Camera pSourceCamera, int pFrameWidth, int pFrameHeight, int pBitRate, int pFrameRate, Surface pPreviewSurface, Rect pPreviewSurfaceRect) throws IOException, InterruptedException {
		H264EncoderServer_Stop();
		//.
		H264EncoderServer = new TH264EncoderServer(VideoRecorderModule.Device.VideoModule, pSourceCamera, pFrameWidth, pFrameHeight, pBitRate, pFrameRate, H264EncoderServer_Clients, pPreviewSurface,pPreviewSurfaceRect);
		H264EncoderServer.WaitForInitialization();
		return H264EncoderServer;
	}
	
	public synchronized void H264EncoderServer_Stop() throws IOException, InterruptedException {
		if (H264EncoderServer != null) {
			H264EncoderServer.Destroy();
			H264EncoderServer = null;
		}
	}

	public synchronized boolean H264EncoderServer_Exists() {
		return (H264EncoderServer != null);
	}

	public synchronized void H264EncoderServer_Clients_Register(TH264EncoderServer.TClient Client) throws Exception {
		synchronized (H264EncoderServer_Clients) {
			H264EncoderServer_Clients.add(Client);
			//. send codec Parameters
			if ((H264EncoderServer != null) && (H264EncoderServer.Parameters != null) && Client.flApplyParameters)
				Client.DoOnConfiguration(H264EncoderServer.Parameters,H264EncoderServer.Parameters.length);
		}
	}

	public synchronized void H264EncoderServer_Clients_Unregister(TH264EncoderServer.TClient Client) throws Exception {
		synchronized (H264EncoderServer_Clients) {
			H264EncoderServer_Clients.remove(Client);
		}
	}

	public int H264EncoderServer_Clients_Count() throws IOException {
		synchronized (H264EncoderServer_Clients) {
			return H264EncoderServer_Clients.size();
		}
	}
}
