package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.Surface;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264EncoderServer;

@SuppressLint("HandlerLeak")
public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";
	
	public static class TMyProfile extends TChannel.TProfile {
		
		public int Width 	= 640;
		public int Height 	= 480;
		//.
		public int FrameRate = 15;
		public int BitRate	 = 1000000;

		public TMyProfile() {
			super();
		}

		public TMyProfile(byte[] ProfileData) throws Exception {
			super(ProfileData);
		}

		@Override
		public void FromXMLNode(Node ANode) throws Exception {
			super.FromXMLNode(ANode);
			//. Width
			Node _Node = TMyXML.SearchNode(ANode,"Width");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					Width = Integer.parseInt(ValueNode.getNodeValue());
			}
			//. Height
			_Node = TMyXML.SearchNode(ANode,"Height");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					Height = Integer.parseInt(ValueNode.getNodeValue());
			}
			//. FrameRate
			_Node = TMyXML.SearchNode(ANode,"FrameRate");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					FrameRate = Integer.parseInt(ValueNode.getNodeValue());
			}
			//. BitRate
			_Node = TMyXML.SearchNode(ANode,"BitRate");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					BitRate = Integer.parseInt(ValueNode.getNodeValue());
			}
		}
		
		@Override
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
			super.ToXMLSerializer(Serializer);
	    	//. Width
	        Serializer.startTag("", "Width");
	        Serializer.text(Integer.toString(Width));
	        Serializer.endTag("", "Width");
	    	//. Height
	        Serializer.startTag("", "Height");
	        Serializer.text(Integer.toString(Height));
	        Serializer.endTag("", "Height");
	    	//. FrameRate
	        Serializer.startTag("", "FrameRate");
	        Serializer.text(Integer.toString(FrameRate));
	        Serializer.endTag("", "FrameRate");
	    	//. BitRate
	        Serializer.startTag("", "BitRate");
	        Serializer.text(Integer.toString(BitRate));
	        Serializer.endTag("", "BitRate");
		}
		
		@Override
		public Intent GetProfilePanel(Activity Parent) throws Exception {
			Intent Result = new Intent(Parent, TH264IChannelProfilePanel.class);
			Result.putExtra("ProfileData", ToByteArray());
			//.
			return Result;
		}
	}
	
	public static class TChannelStreamConfiguration {
	
		private int 	Session;
		private byte[] 	Data = null;
		
		public synchronized int Set(byte[] pData, int pDataSize) {
			Session++;
			//.
			if ((Data == null) || (Data.length != pDataSize))
				Data = new byte[pDataSize];
			System.arraycopy(pData,0, Data,0, pDataSize);
			//.
			return Session;
		}
		
		public synchronized TChannelStreamConfiguration Get() {
			if (Data == null)
				return null; //. ->
			//.
			TChannelStreamConfiguration Result = new TChannelStreamConfiguration();
			//.
			Result.Session = Session;
			//.
			Result.Data = new byte[Data.length];
			if (Data.length > 0)
				System.arraycopy(Data,0, Result.Data,0, Data.length);
			//.
			return Result;
		}
	}
	
	
	public class TVideoFrameSource extends TCancelableThread {
		
		private class TVideoFrameEncoderServerClient extends TH264EncoderServer.TClient {

			private int Index = 0;
			
			public TVideoFrameEncoderServerClient() {
				super(true);
			}

			@Override
			public void DoOnConfiguration(byte[] Buffer, int BufferSize) throws Exception {
				//. save a configuration for a Channel.Configuration value
				Configuration_SaveToFile(Buffer, BufferSize);
				//. start a new stream session and fill the channel configuration with data
				int StreamSession = ChannelStreamConfiguration.Set(Buffer, BufferSize);
				//. send new session packet
				DestinationChannel.DoOnChannelStreamSession(StreamSession);
				//. send configuration
				DestinationChannel.DoOnH264Packet(Buffer, BufferSize);
			}
			
			@Override
			public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
				DestinationChannel.DoOnH264Packet(Buffer, BufferSize);
				//.
				if (flSyncFrame) {
					DestinationChannel.DoOnH264Index(Index);
					//.
					Timestamp = Timestamp/1000; //. convert to milliseconds from microseconds
					DestinationChannel.DoOnH264Timestamp((int)Timestamp);
				}
				//.
				Index += BufferSize;
			}
		}

		private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel DestinationChannel;
		//.
		public boolean flStarted = false;
		//.
		private volatile TH264EncoderServer Server = null;
				
		
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
				flStarted = true;
				try {
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel _DestinationChannel = DestinationChannel_Get();
					if (!(_DestinationChannel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)) 
			        	throw new IOException("No destination channel"); //. ->
					DestinationChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)_DestinationChannel;
					DestinationChannel_PacketSubscribersItemsNotifier_Set(new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscribers.TItemsNotifier() {

						@Override
						protected void DoOnSubscribed(TPacketSubscriber Subscriber) throws Exception {
							TChannelStreamConfiguration CC = ChannelStreamConfiguration.Get();
							if (CC != null) {
								//. send new session packet
								Subscriber.EnqueuePacket(DestinationChannel.ChannelStreamSession_ToByteArray(CC.Session));
								//. send channel configuration
								Subscriber.EnqueuePacket(DestinationChannel.H264Packet_ToByteArray(CC.Data, CC.Data.length));
							}
						}
					});
					try {
				        if (!InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) 
				        	throw new IOException("Video server is not available"); //. ->
				        //.
				        android.hardware.Camera camera = android.hardware.Camera.open();
				        if (camera == null)
				        	return; //. ->
				        try {
				        	Server = InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Start(camera, MyProfile.Width,MyProfile.Height, MyProfile.BitRate, MyProfile.FrameRate, Preview,PreviewFrame);
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
					        	Server = null;
					        }
				        }
				        finally {
		        			camera.release();
				        }
					}
					finally {
						DestinationChannel_PacketSubscribersItemsNotifier_Clear();
					}
				}
				finally {
					flStarted = false;
				}
			}
        	catch (InterruptedException IE) {
        	}
			catch (Throwable TE) {
				InternalSensorsModule.Device.Log.WriteWarning("H264IChannel.VideoFrameSource","Exception: "+TE.getMessage());
			}
		}
	}
	
	private TMyProfile MyProfile;
	//.
	private TVideoFrameSource VideoFrameSource;
	//.
	private TChannelStreamConfiguration ChannelStreamConfiguration = new TChannelStreamConfiguration();
	//.
	public volatile Surface Preview = null;
	public volatile Rect 	PreviewFrame = null;
	
	public TH264IChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, "DefaultCamera", TMyProfile.class);
		MyProfile = (TMyProfile)Profile;
		//.
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "Video";
		Info = "";
		Size = 1024*1024*10;
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
	
	public Rect GetFrameSize() {
		return (new Rect(0,0, MyProfile.Width,MyProfile.Height));
	}
	
	public int GetFrameRate() {
		return MyProfile.FrameRate;
	}
	
	public int GetBitrate() {
		TH264EncoderServer _Server = VideoFrameSource.Server;
		if (_Server != null) 
			return _Server.GetBitrate(); //. ->
		else
			return -1; //. ->
	}
	
	public static final int SetBitrateResult_Ok 						= 0;
	public static final int SetBitrateResult_SourceNotAvailable 		= -1;
	public static final int SetBitrateResult_SourceNonExcusiveAccess 	= -2;
	
	public int SetBitrate(int Value) {
		TH264EncoderServer _Server = VideoFrameSource.Server;
		if (_Server != null) {
			if (_Server.Clients_Count() == 1) {
				_Server.SetBitrate(Value);
				//.
				return SetBitrateResult_Ok; //. ->
			}
			else
				return SetBitrateResult_SourceNonExcusiveAccess; //. ->
		}
		else
			return SetBitrateResult_SourceNotAvailable; //. ->
	}
	
	@Override
	public void Configuration_Check() throws Exception {
		if (Configuration.length() == 0) {
			String CFN = Folder()+"/"+TChannel.ConfigurationFileName;
			File CF = new File(CFN);
			if (!CF.exists())
				throw new ConfigurationErrorException("Video.H264I channel has no configuration. Please start that channel to create a configuration automatically."); //. =>
		}
	}
	
	private void Configuration_SaveToFile(byte[] Data, int DataSize) throws IOException {
		String CFN = Folder()+"/"+TChannel.ConfigurationFileName;
		File CF = new File(CFN);
		if (CF.exists())
			return; //. ->
		CF.getParentFile().mkdirs();
		String TCFN = Folder()+"/"+TChannel.ConfigurationFileName+".tmp";
		File TCF = new File(TCFN);
		FileOutputStream FOS = new FileOutputStream(TCF);
		try {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.NO_WRAP);
				try {
					B64S.write(Data, 0,DataSize);
				}
				finally {
					B64S.close();
				}
				String DataString = new String(BOS.toByteArray(), "utf-8");
				//.
				String ConfigurationString = "1:;1,"+DataString;
				//.
				FOS.write(ConfigurationString.getBytes("utf-8"));
			}
			finally {
				BOS.close();
			}
		}
		finally {
			FOS.close();
		}
		TCF.renameTo(CF);
	}
	
	@Override
	public synchronized boolean StreamableViaComponent() {
		return Profile.StreamableViaComponent;
	}
	
	@Override
	public void StartSource() {
		PostStart();
	}

	@Override
	public void StopSource() {
		PostStop();
	}
	
	@Override
	public boolean IsActive() {
		return VideoFrameSource.flStarted;
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
