package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

public class TMicrophoneCapturingServer extends TCancelableThread {

	public static class TConfiguration {
		
		public static final int SOURCE_ANY = -1;
		//.
		public static final int BUFFERSIZE_ANY = 0;
		
		public int Source = SOURCE_ANY;
		public int SamplePerSec;
		public int BufferSize = BUFFERSIZE_ANY;
		
		public TConfiguration(int pSource, int pSamplePerSec, int pBufferSize) {
			Source = pSource;
			SamplePerSec = pSamplePerSec;
			BufferSize = pBufferSize;
		}
		
		public TConfiguration(int pSamplePerSec) {
			this(SOURCE_ANY, pSamplePerSec, BUFFERSIZE_ANY);
		}
		
		public boolean Equals(TConfiguration AConfiguration) {
			return (((AConfiguration.Source == SOURCE_ANY) || (Source == AConfiguration.Source)) && (SamplePerSec == AConfiguration.SamplePerSec) && ((AConfiguration.BufferSize == BUFFERSIZE_ANY) || (BufferSize == AConfiguration.BufferSize))); 
		}
	}
	
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

		public synchronized boolean IsEmpty() {
			return (Items.size() == 0);
		}
		
		private TPacketSubscriber[] DoOnPacket_Subscribers = new TPacketSubscriber[0];
		
		public void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
			int Cnt;
			synchronized (this) {
				Cnt = Items.size();
				if (Cnt > DoOnPacket_Subscribers.length)
					DoOnPacket_Subscribers = new TPacketSubscriber[Cnt];
				for (int I = 0; I < Cnt; I++)
					DoOnPacket_Subscribers[I] = Items.get(I); 
			}
			for (int I = 0; I < Cnt; I++)
				DoOnPacket_Subscribers[I].DoOnPacket(Packet,PacketSize);
		}
	}

	
	private TAudioModule AudioModule;
	//.
	private TConfiguration Configuration = null;
	//.
	private AudioRecord Microphone = null;
	//.
	public TPacketSubscribers PacketSubscribers = new TPacketSubscribers();
	//.
	private boolean flStarted = false;
	
	public TMicrophoneCapturingServer(TAudioModule pAudioModule) {
		super();
		//.
		AudioModule = pAudioModule;
	}
	
	public void Destroy() throws InterruptedException {
		Stop();
		//.
		if (PacketSubscribers != null) {
			PacketSubscribers.Destroy();
			PacketSubscribers = null;
		}
	}
	
	public synchronized boolean Connect(TConfiguration pConfiguration, TPacketSubscriber pPacketSubscriber, boolean pflStart) {
		if (flStarted) {
			if (!Configuration.Equals(pConfiguration))
				return false; //. ->
		}
		else {
			Configuration = pConfiguration;
			//.
			if (pflStart)
				Start();
		}
		//.
		PacketSubscribers.Subscribe(pPacketSubscriber);
		//.
		return true;
	}
	
	public synchronized boolean Connect(TConfiguration pConfiguration, TPacketSubscriber pPacketSubscriber) {
		return Connect(pConfiguration, pPacketSubscriber, true);
	}
	
	public synchronized void Disconnect(TPacketSubscriber pPacketSubscriber, boolean flStopIfIdling) throws InterruptedException {
		PacketSubscribers.Unsubscribe(pPacketSubscriber);
		//.
		if (PacketSubscribers.IsEmpty() && flStopIfIdling)
			Stop();
	}
	
	public synchronized void Disconnect(TPacketSubscriber pPacketSubscriber) throws InterruptedException {
		Disconnect(pPacketSubscriber,true);
	}
	
	public synchronized void CheckForIdling() throws InterruptedException {
		if (PacketSubscribers.IsEmpty() && flStarted)
			Stop();
	}
	
	public synchronized void Start() {
		if (flStarted)
			return; //. ->
		//.
		Canceller.Reset();
		//.
		_Thread = new Thread(this);
		_Thread.start();
		//.
		flStarted = true;
	}
	
	private synchronized void Stop() throws InterruptedException {
		if (!flStarted)
			return; //. ->
		//.
		flStarted = false;
		//.
		if (_Thread != null) {
			CancelAndWait();
			_Thread = null;
		}
	}
	
    private void Microphone_Initialize() throws IOException {
    	if (Configuration.Source == TConfiguration.SOURCE_ANY)
    		Configuration.Source = MediaRecorder.AudioSource.DEFAULT;
    	if (Configuration.BufferSize == TConfiguration.BUFFERSIZE_ANY)
    		Configuration.BufferSize = AudioRecord.getMinBufferSize(Configuration.SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    	//.
        if (Configuration.BufferSize != AudioRecord.ERROR_BAD_VALUE && Configuration.BufferSize != AudioRecord.ERROR) {
            Microphone = new AudioRecord(Configuration.Source, Configuration.SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Configuration.BufferSize*2);
            if (Microphone != null && Microphone.getState() == AudioRecord.STATE_INITIALIZED) 
            	Microphone.startRecording();
            else 
            	throw new IOException("unable to initialize audio-recorder"); //. =>

        } else 
        	throw new IOException("audio-recorder buffer size error"); //. =>
    }
    
    private void Microphone_Finalize() {
        if (Microphone.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
        	Microphone.stop();
        if (Microphone.getState() == AudioRecord.STATE_INITIALIZED) 
        	Microphone.release();
    }
    
	@Override
	public void run() {
		try {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			//. 
			Microphone_Initialize();
			try {
		        byte[] TransferBuffer = new byte[Configuration.BufferSize];
		        int Size;
				while (!Canceller.flCancel) {
		            Size = Microphone.read(TransferBuffer, 0,TransferBuffer.length);     
					if (Size > 0)  
						DoOnAudioPacket(TransferBuffer,Size);
					else
						if (Size == -1)
                    		throw new RuntimeException("error of reading audio buffer"); //. =>
		        }
			}
			finally {
				Microphone_Finalize();
			}
		}
		catch (Throwable T) {
			String S = T.getMessage();
			if (S == null)
				S = T.getClass().getName();
			AudioModule.Device.Log.WriteError("MicrophoneCapturingServer",S);
		}
	}
	
	private void DoOnAudioPacket(byte[] Packet, int PacketSize) throws IOException {
        try {
        	PacketSubscribers.DoOnPacket(Packet,PacketSize);
		} 
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			AudioModule.Device.Log.WriteError("MicrophoneCapturingServer.Processing.DoOnAudioPacket()",S);
		}
	}
}
