package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
								Subscriber.ProcessPacket(DestinationChannel.ChannelStreamSession_ToByteArray(CC.Session));
								//. send channel configuration
								Subscriber.ProcessPacket(DestinationChannel.H264Packet_ToByteArray(CC.Data, CC.Data.length));
							}
						}
					});
					try {
				        if (!InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) 
				        	throw new IOException("Video server is not available"); //. ->
				        android.hardware.Camera camera = android.hardware.Camera.open();
				        InternalSensorsModule.Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Start(camera, MyProfile.Width,MyProfile.Height, MyProfile.BitRate, MyProfile.FrameRate, null,null);
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
	
	public TH264IChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, TMyProfile.class);
		MyProfile = (TMyProfile)Profile;
		//.
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
		return MyProfile.FrameRate;
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
