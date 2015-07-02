package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.AudioModule.TMicrophoneCapturingServer;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC.TAACADTSEncoder;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

@SuppressLint("HandlerLeak")
public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";
	
	public class TAudioSampleSource extends TCancelableThread {
		
		private AudioRecord Recorder = null; 
		public int 			Source = TMicrophoneCapturingServer.TConfiguration.SOURCE_ANY;
		public int 			SampleRate = 16000;
		public int			BitRate = 16000;
        private int 		BufferSize;
        //.
        private TAACADTSEncoder AACADTSEncoder;

		public TAudioSampleSource() {
    		super();
		}
		
		public void Release() throws Exception {
			Stop();
		}
		
		public void Start() {
    		AACADTSEncoder = new TAACADTSEncoder(BitRate, SampleRate) {
    			
    			@Override
    			public void DoOnOutputPacket(byte[] Packet, int PacketSize) throws Exception {
    				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = DestinationChannel_Get();
    				if (DestinationChannel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel)
    					((com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel)DestinationChannel).DoOnAACPacket(Packet, PacketSize);
    			};
    		};
			//.
			Canceller.Reset();
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Stop() throws Exception {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
			//.
			if (AACADTSEncoder != null) {
				AACADTSEncoder.Destroy();
				AACADTSEncoder = null;
			}
		}
		
	    private void Microphone_Initialize() throws IOException {
	    	BufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	        if (BufferSize != AudioRecord.ERROR_BAD_VALUE && BufferSize != AudioRecord.ERROR) {
	            Recorder = new AudioRecord(Source, SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize);
	            if (Recorder != null && Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
	            	Recorder.startRecording();
	            else 
	            	throw new IOException("unable to initialize audio-recorder"); //. =>

	        } else 
	        	throw new IOException("AudioRecord.getMinBufferSize() error"); //. =>
	    }
	    
	    private void Microphone_Finalize() {
	        if (Recorder != null) {
	            if (Recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
	            	Recorder.stop();
	            if (Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
	            	Recorder.release();
	            Recorder = null;
	        }
	    }
	    
		@Override
		public void run() {
			try {
				//. try to connect to an AudioModule.MicrophoneCapturingServer
				TMicrophoneCapturingServer.TConfiguration Configuration = new TMicrophoneCapturingServer.TConfiguration(Source, SampleRate);
				TMicrophoneCapturingServer.TPacketSubscriber PacketSubscriber = new TMicrophoneCapturingServer.TPacketSubscriber() {
					
					@Override
					protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
						DoOnAudioPacket(Packet,PacketSize);
					}
				};  
				if (
						(InternalSensorsModule.Device.AudioModule.MicrophoneCapturingServer != null) &&
						(InternalSensorsModule.Device.AudioModule.MicrophoneCapturingServer.Connect(Configuration, PacketSubscriber)) 
				) {
					try {
						while (!Canceller.flCancel) 
							Thread.sleep(1000*60);
					}
					finally {
						InternalSensorsModule.Device.AudioModule.MicrophoneCapturingServer.Disconnect(PacketSubscriber);
					}
				}
				else {
					InternalSensorsModule.Device.Log.WriteWarning("AACChannel.AudioSampleSource","unable to connect to the MicrophoneCapturingServer (the configuration is differ with a current one), using default method");
	    			//.
					Microphone_Initialize();
					try {
				        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
				        byte[] TransferBuffer = new byte[BufferSize];
				        int Size;
						while (!Canceller.flCancel) {
				            Size = Recorder.read(TransferBuffer, 0,TransferBuffer.length);     
							if (Size > 0)  
								DoOnAudioPacket(TransferBuffer,Size);
				        }
					}
					finally {
						Microphone_Finalize();
					}
				}
			}
			catch (Throwable T) {
			}
		}
		
		private void DoOnAudioPacket(byte[] Packet, int PacketSize) throws IOException {
			try {
				AACADTSEncoder.EncodeInputBuffer(Packet, PacketSize, (System.nanoTime()/1000)/1000);
			} 
			catch (IOException IOE) {
			}
			catch (Exception E) {
			}
		}
	}
	
	private TAudioSampleSource AudioSampleSource;
	
	public TAACChannel(TInternalSensorsModule pInternalSensorsModule) {
		super(pInternalSensorsModule);
		//.
		Enabled = true;
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "Audio";
		Info = "AAC channel";
		Size = 0;
		Configuration = "";
		Parameters = "";
		//.
		AudioSampleSource = new TAudioSampleSource();
	}
	
	@Override
	public void Close() throws Exception {
		if (AudioSampleSource != null) {
			AudioSampleSource.Release();
			AudioSampleSource = null;
		}
		//.
		super.Close();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	public int GetSampleRate() {
		return AudioSampleSource.SampleRate;
	}
	
	@Override
	public void StartSource() {
		PostStart();
	}

	@Override
	public void StopSource() {
		PostStop();
	}
	
    public void PostStart() {
		MessageHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }
    
    public void PostStop() {
		MessageHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
    }
    
	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;
	
	public Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_START: 
                	try {
            			AudioSampleSource.Start();
                	}
                	catch (Exception E) {
                		Toast.makeText(InternalSensorsModule.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
            			AudioSampleSource.Stop();
                	}
                	catch (Exception E) {
                		Toast.makeText(InternalSensorsModule.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
