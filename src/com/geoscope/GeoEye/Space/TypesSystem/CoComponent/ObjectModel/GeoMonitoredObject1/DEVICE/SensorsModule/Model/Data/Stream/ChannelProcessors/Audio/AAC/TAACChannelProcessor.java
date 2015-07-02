package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Audio.AAC;

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;

public class TAACChannelProcessor extends TChannelProcessor {

	public static class TStatisticHandler {
		
		public void DoOnAudioBuffer(int AudioBuffersCount) {
			
		}
	}
	
	
	private class TAudioBufferPlaying extends TCancelableThread {
		
		private static final int BufferSize = 4096*10;
		private static final int BufferPlayPortion = 16;
		
		private Object 	BufferLock = new Object();
		private byte[] 	Buffer = new byte[BufferSize];
		private int		BufferHead = 0;
		private int		BufferTile = 0;
		private boolean Buffer_flEmpty = true;
		
		private TAutoResetEvent	PlaySignal = new TAutoResetEvent();
		
		public TAudioBufferPlaying() {
    		super();
    		//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws InterruptedException {
			Cancel();
			PlaySignal.Set();
			Wait();
		}
		
		@Override
		public void run()  {
			try {
		        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
				byte[] Portion = new byte[BufferPlayPortion];
				int Portion_Size;
				while (!Canceller.flCancel) {
					PlaySignal.WaitOne(1000);
					//.
					while (!Canceller.flCancel) {
						synchronized (BufferLock) {
							if (!Buffer_flEmpty) {
								int Size = BufferTile-BufferHead;
								if (Size > 0) {
									if (Size > BufferPlayPortion)
										Size = BufferPlayPortion;
									System.arraycopy(Buffer,BufferHead, Portion,0, Size);
									Portion_Size = Size;
									BufferHead += Size;
								}
								else {
									int Delta = BufferSize-BufferHead;
									if (Delta > BufferPlayPortion) {
										System.arraycopy(Buffer,BufferHead, Portion,0, BufferPlayPortion);
										Portion_Size = BufferPlayPortion;
										BufferHead += BufferPlayPortion;
									}
									else {
										System.arraycopy(Buffer,BufferHead, Portion,0, Delta);
										Portion_Size = Delta;
										Size = BufferPlayPortion-Delta;
										if (Size > 0) {
											if (Size > BufferTile)
												Size = BufferTile;
											System.arraycopy(Buffer,0, Portion,Delta, Size);
											Portion_Size += Size;
										}
										BufferHead = Size;
									}
								}
								Buffer_flEmpty = (BufferHead == BufferTile); 
							}
							else 
								Portion_Size = 0;
						}
						if (Portion_Size > 0)
							AudioPlayer.write(Portion, 0,Portion_Size);
						else
							break; //. >
					}
				}
			}
			catch (InterruptedException IE) {
			}
			catch (Throwable T) {
			}
		}
		
		public void PlayBuffer(byte[] pBuffer, int pBufferSize) {
			if (AudioPlayer == null)
				return; //. ->
			synchronized (BufferLock) {
				int Delta = BufferSize-BufferTile;
				if (Delta > pBufferSize) {
					System.arraycopy(pBuffer,0, Buffer,BufferTile, pBufferSize);
					if (BufferTile < BufferHead) {
						BufferTile += pBufferSize;
						if (BufferHead < BufferTile)
							BufferHead = BufferTile; 
					}
					else
						BufferTile += pBufferSize;
				}
				else {
					if (Delta > 0) {
						System.arraycopy(pBuffer,0, Buffer,BufferTile, Delta);
						if ((BufferTile < BufferHead) && (BufferHead < BufferSize))
								BufferHead = 0; 
					}
					pBufferSize = pBufferSize-Delta;
					if (pBufferSize > 0)
						System.arraycopy(pBuffer,Delta, Buffer,0, pBufferSize);
					BufferTile = pBufferSize;
					if (BufferHead < BufferTile)
						BufferHead = BufferTile; 
				}
				Buffer_flEmpty = false;				
			}
			PlaySignal.Set();
		}
	}
	
	private TAACChannel AACChannel;
	//.
	private AudioTrack 	AudioPlayer = null;
	private TAudioBufferPlaying AudioBufferPlaying;
	//.
	private int AudioBuffersCount = 0;
	//.
	public volatile TStatisticHandler StatisticHandler = null;
	
	public TAACChannelProcessor(TStreamChannel pChannel) {
		super(pChannel);
		//.
		AACChannel = (TAACChannel)Channel;
		//.
		AACChannel.OnSamplesHandler = new TAACChannel.TH264Channel() {
			
			@Override
			public void DoOnConfiguration(int SampleRate, int ChannelCount) throws java.io.IOException {
			    int ChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
			    int BufferSize = -1;
			    switch (ChannelCount) {
			     
			    case 1:
			    	ChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
				    BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			    	break; //. >

			    case 2:
			    	ChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
				    BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
			    	break; //. >
			    }
		    	if (BufferSize <= 0)
	    			throw new IOException("error of AudioRecord.getMinBufferSize, RC: "+Integer.toString(BufferSize)); //. =>
		    	if (AudioPlayer != null) 
		    		AudioPlayer.stop();
		    	BufferSize = TAudioBufferPlaying.BufferPlayPortion*2;
		    	//.
				AudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, SampleRate, ChannelConfig, AudioFormat.ENCODING_PCM_16BIT, BufferSize*10, AudioTrack.MODE_STREAM);
		    	AudioPlayer.setStereoVolume(1.0F,1.0F);
		    	AudioPlayer.play();
				//.
				AudioBufferPlaying = new TAudioBufferPlaying();
			}
			
			@Override
			public void DoOnSamplesPacket(byte[] Packet, int PacketSize) {
				AudioBufferPlaying.PlayBuffer(Packet, PacketSize);
				//.
				AudioBuffersCount++;
				//.
				TStatisticHandler _StatisticHandler = StatisticHandler;
				if (_StatisticHandler != null)
					_StatisticHandler.DoOnAudioBuffer(AudioBuffersCount);
			}
		};
	}

	@Override
	public void Destroy() throws Exception {
		AACChannel.OnSamplesHandler = null;
		//.
		if (AudioBufferPlaying != null) {
			AudioBufferPlaying.Destroy();
			AudioBufferPlaying = null;
		}
		//.
		if (AudioPlayer != null) {
    		AudioPlayer.stop();
    		AudioPlayer = null;
		}
		//.
		super.Destroy();
	}
}
