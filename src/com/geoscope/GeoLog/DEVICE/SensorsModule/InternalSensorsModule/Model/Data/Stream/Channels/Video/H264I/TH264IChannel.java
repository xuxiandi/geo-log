package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264EncoderServer;

@SuppressLint("HandlerLeak")
public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";
	
	
	public class TVideoFrameSource extends TCancelableThread {
		
		private class TVideoFrameEncoderServerClient extends TH264EncoderServer.TClient {

			private int Index = 0;
			
			public TVideoFrameEncoderServerClient() {
				super(true);
			}

			@Override
			public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel DestinationChannel = DestinationChannel_Get();
				if (DestinationChannel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel) {
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel Channel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)DestinationChannel;
					//.
					Channel.DoOnH264Packet(Buffer, BufferSize);
					//.
					if (flSyncFrame) {
						Channel.DoOnH264Index(Index);
						//.
						Timestamp = Timestamp/1000; //. convert to milliseconds from microseconds
						Channel.DoOnH264Timestamp((int)Timestamp);
					}
				}
				//.
				Index += BufferSize;
			}
		}

		public int Width = 640;
		public int Height = 480;
		//.
		public int FrameRate = 15;
		public int BitRate = 1000000;
		
		public TVideoFrameSource() {
    		super();
		}
		
		public void Release() throws Exception {
			Stop();
		}
		
		public void Start() {
			Canceller.Reset();
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Stop() throws Exception {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
		}
		
		@Override
		public void run() {
			try {
		        if (!InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) 
		        	throw new IOException("Video server is not available"); //. ->
		        android.hardware.Camera camera = android.hardware.Camera.open();
		        InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Start(camera, Width,Height, BitRate, FrameRate, null,null);
		        try {
		        	TVideoFrameEncoderServerClient VideoFrameEncoderServerClient = new TVideoFrameEncoderServerClient();
		        	try {
			        	InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(VideoFrameEncoderServerClient);
			        	try {
			    	        camera.startPreview();
			    	        try {
								while (!Canceller.flCancel) 
									Thread.sleep(1000*60);
			    	        }
			        		finally {
			        			camera.stopPreview();
			        			camera.setPreviewDisplay(null);
			        			camera.setPreviewCallback(null);
			        			camera.release();
			        		}
			        	}
			        	finally {
			        		InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(VideoFrameEncoderServerClient);
			        	}
		        	}
		        	finally {
						VideoFrameEncoderServerClient.Destroy();
		        	}
		        }
		        finally {
		        	InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Stop();
		        }
			}
			catch (Throwable TE) {
				InternalSensorsModule.Device.Log.WriteWarning("H264Channel.VideoFrameSource","Exception: "+TE.getMessage());
			}
		}
	}
	
	private TVideoFrameSource VideoFrameSource;
	
	public TH264IChannel(TInternalSensorsModule pInternalSensorsModule) {
		super(pInternalSensorsModule);
		//.
		Enabled = true;
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "Video";
		Info = "H264 channel";
		Size = 0;
		Configuration = "";
		Parameters = "";
		//.
		VideoFrameSource = new TVideoFrameSource();
	}
	
	@Override
	public void Close() throws Exception {
		if (VideoFrameSource != null) {
			VideoFrameSource.Release();
			VideoFrameSource = null;
		}
		//.
		super.Close();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	public int GetFrameRate() {
		return VideoFrameSource.FrameRate;
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
            			VideoFrameSource.Start();
                	}
                	catch (Exception E) {
                		Toast.makeText(InternalSensorsModule.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
            			VideoFrameSource.Stop();
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
