/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.VideoModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.SystemClock;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264Encoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer.TPacketSubscriber;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpEncoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpPacket;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TVideoModule extends TModule 
{
	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"VideoModule";
	}
	//.
	public static final int VideoFrameServer_Service_JPEGFrames 	= 1;
	public static final int VideoFrameServer_Service_H264Frames 	= 2;
	public static final int VideoFrameServer_Service_H264Frames1 	= 3;
	//.
	public static final int VideoFrameServer_Initialization_Code_Ok 							= 0;
	public static final int VideoFrameServer_Initialization_Code_Error 							= -1;
	public static final int VideoFrameServer_Initialization_Code_UnknownServiceError 			= -2;
	public static final int VideoFrameServer_Initialization_Code_ServiceIsNotActiveError 		= -3;
	public static final int VideoFrameServer_Initialization_Code_ServiceAccessIsDeniedError		= -4;
	public static final int VideoFrameServer_Initialization_Code_ServiceAccessIsDisabledError	= -5;
	
	private static class TMyH264Encoder extends H264Encoder {

		protected OutputStream 	MyOutputStream = null;
		
		public TMyH264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream, boolean pflParseParameters) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pflParseParameters);
			MyOutputStream = pOutputStream;
		}

		private byte[] DataDescriptor = new byte[4];
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			//.
			DataDescriptor[0] = (byte)(BufferSize & 0xff);
			DataDescriptor[1] = (byte)(BufferSize >> 8 & 0xff);
			DataDescriptor[2] = (byte)(BufferSize >> 16 & 0xff);
			DataDescriptor[3] = (byte)(BufferSize >>> 24);
			//.
			MyOutputStream.write(DataDescriptor);
			MyOutputStream.write(Buffer, 0,BufferSize);
			MyOutputStream.flush();
		}
		
		private void SendBuffer(byte[] Buffer) throws IOException {
			SendBuffer(Buffer,Buffer.length);
		}
		
		@Override
		public void DoOnParameters(byte[] Buffer, int BufferSize) throws IOException {
		}
		
		@Override
		public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
			SendBuffer(pSPS);
			SendBuffer(pPPS);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyH264Encoder1 extends H264Encoder {

		protected OutputStream 	MyOutputStream = null;
	 
		public TMyH264Encoder1(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate);
			MyOutputStream = pOutputStream;
		}

		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			//.
			MyOutputStream.write(Buffer, 0,BufferSize);
			MyOutputStream.flush();
		}
		
		private void SendBuffer(byte[] Buffer) throws IOException {
			SendBuffer(Buffer,Buffer.length);
		}
		
		@Override
		public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
			SendBuffer(pSPS);
			SendBuffer(pPPS);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyH264EncoderUDPRTP extends H264Encoder {

		private DatagramSocket OutputSocket;
		private String Address;
		private int Port;
		//.
		private int Timestamp = 0;
		//.
		private TRtpEncoder RtpEncoder;
		
		public TMyH264EncoderUDPRTP(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, DatagramSocket pOutputSocket, String pAddress, int pPort) throws UnknownHostException {
			super(FrameWidth, FrameHeight, BitRate, FrameRate);
			OutputSocket = pOutputSocket;
			Address = pAddress;
			Port = pPort;
			//.
			RtpEncoder = new TRtpEncoder(Address,Port) {  
				
				private int PacketIndex = 0;
				
				@Override
				public void DoOnOutput(TRtpPacket OutputPacket) throws IOException {
					OutputPacket.sendTo(OutputSocket);
					if (PacketIndex < 2) { //. re-send first configuration packets 
						for (int I = 0; I < 4; I++)
							OutputPacket.sendToAgain(OutputSocket);
					}
					//.
					PacketIndex++;
				}
			};
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RtpEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		private void SendBuffer(byte[] Buffer) throws IOException {
			SendBuffer(Buffer,Buffer.length);
		}
		
		@Override
		public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
			SendBuffer(pSPS);
			SendBuffer(pPPS);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyH264EncoderProxyUDPRTP extends H264Encoder {

		private DatagramSocket OutputSocket;
		//.
		private String 	ProxyServerAddress;
		private int 	ProxyServerPort;
		//.
		private String 	Address;
		private int 	Port;
		//.
		private int Timestamp = 0;
		//.
		private TRtpEncoder RtpEncoder;
		
		public TMyH264EncoderProxyUDPRTP(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, DatagramSocket pOutputSocket, String pProxyServerAddress, int pProxyServerPort, String pAddress, int pPort) throws UnknownHostException {
			super(FrameWidth, FrameHeight, BitRate, FrameRate);
			OutputSocket = pOutputSocket;
			ProxyServerAddress = pProxyServerAddress;
			ProxyServerPort = pProxyServerPort;
			Address = pAddress;
			Port = pPort;
			//.
			RtpEncoder = new TRtpEncoder(ProxyServerAddress,ProxyServerPort, Address,Port) {
				
				private int PacketIndex = 0;
				
				@Override
				public void DoOnOutput(TRtpPacket OutputPacket) throws IOException {
					OutputPacket.sendTo(OutputSocket);
					if (PacketIndex < 2) { //. re-send first configuration packets 
						for (int I = 0; I < 4; I++)
							OutputPacket.sendToAgain(OutputSocket);
					}
					//.
					PacketIndex++;
				}
			};
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RtpEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		private void SendBuffer(byte[] Buffer) throws IOException {
			SendBuffer(Buffer,Buffer.length);
		}
		
		@Override
		public void DoOnParameters(byte[] pSPS, byte[] pPPS) throws IOException {
			SendBuffer(pSPS);
			SendBuffer(pPPS);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	public static TComponentDataStreaming.TStreamer GetStreamer(String TypeID, TDEVICEModule Device, int idTComponent, long idComponent, int ChannelID, String Configuration, String Parameters) {
		if (TH264VideoStreamer.TypeID().equals(TypeID))
			return new TH264VideoStreamer(Device, idTComponent,idComponent, ChannelID, Configuration, Parameters, Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResX,Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResY,Device.VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate,Device.VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate); //. ->
		else
			return null; //. ->
	}
	
	public static class TH264VideoStreamer extends TDEVICEModule.TComponentDataStreaming.TStreamer {

		public static String TypeID() {
			return "Video.H264";
		}
		
		private class TProcessing extends TCancelableThread {
			
			public TProcessing() {
			}
			
			public void Destroy() {
				Stop();
			}
			
	    	public void Start() {
        		_Thread = new Thread(this);
        		_Thread.start();
	    	}
	    	
	    	public void Stop() {
	    		CancelAndWait();
	    	}
	    	
			@Override
			public void run() {
				try {
					final H264Encoder Encoder = new TMyH264Encoder(FrameWidth,FrameHeight, FrameBitRate, FrameRate, StreamingBuffer_OutputStream, true); 
					try {
						try {
				        	TPacketSubscriber PacketSubscriber  = new TPacketSubscriber() {
				        		@Override
				        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
					            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
				        		}
				        	};
				        	MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
				        	try {
								while (!Canceller.flCancel) {
									Thread.sleep(1000);
								}
				        	}
				        	finally {
					        	MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
				        	}
						}
						catch (InterruptedException IE) {
						}
					}
					finally {
						Encoder.Destroy();
					}
				}
	        	catch (Throwable E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Device.Log.WriteError("Streamer.Processing",S);
	        	}
			}

		}
		
		private TDEVICEModule Device;
		//.
		private int FrameWidth;
		private int FrameHeight;
		private int FrameBitRate;
		private int FrameRate;
		//.
		private TProcessing Processing = null;
		//.
		private TDEVICEModule.TComponentDataStreaming DataStreaming = null;
		
		public TH264VideoStreamer(TDEVICEModule pDevice, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters, int pFrameWidth, int pFrameHeight, int pFrameBitRate, int pFrameRate) {
			super(pidTComponent,pidComponent, pChannelID, pConfiguration, pParameters, 4, 8192);
			//.
			Device = pDevice;
			//.
			FrameWidth = pFrameWidth;
			FrameHeight = pFrameHeight;
			FrameBitRate = pFrameBitRate;
			FrameRate = pFrameRate;
			//.
			Processing = new TProcessing();
			DataStreaming = Device.TComponentDataStreaming_Create(this);
		}
		
		@Override
		public void Start() {
			Processing.Start();
			DataStreaming.Start();
		}

		@Override
		public void Stop() {
			if (DataStreaming != null) {
				DataStreaming.Destroy();
				DataStreaming = null;
			}
			if (Processing != null) {
				Processing.Destroy();
				Processing = null;
			}
		}
	}
	
	public TUserAccessKey UserAccessKey;
	
    public TVideoModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
		//.
		UserAccessKey = new TUserAccessKey();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() {
    }
    
    public void VideoFrameServer_Connect() {
    	
    }
    
    public void VideoFrameServer_Disconnect() {
    	
    }
    
    public void VideoFrameServer_Capturing(InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
    	int InitializationCode = VideoFrameServer_Initialization_Code_Ok;
    	byte[] DataDescriptor = new byte[4];
        int Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
  		if (Size != DataDescriptor.length)
  			throw new IOException("wrong service data"); //. =>
		int Service = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
		//.
		int FrameRate = 20;
		int FrameQuality = 100;
		switch (Service) {
		
		case VideoFrameServer_Service_JPEGFrames: 
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong frame rate data"); //. =>
			int _FrameRate = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((_FrameRate < 0) || (_FrameRate <= 60))
				FrameRate = _FrameRate;
			//.
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong frame quality data"); //. =>
			int _FrameQuality = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 <= _FrameQuality) && (_FrameQuality <= 100))
				FrameQuality = _FrameQuality;
			if (!Device.VideoRecorderModule.Transmitting.BooleanValue())
				InitializationCode = VideoFrameServer_Initialization_Code_ServiceAccessIsDisabledError;
			break; //. >
			
		case VideoFrameServer_Service_H264Frames: 
		case VideoFrameServer_Service_H264Frames1: 
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong frame rate data"); //. =>
			_FrameRate = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 <= _FrameRate) && (_FrameRate <= 50))
				FrameRate = _FrameRate;
			//.
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong frame quality data"); //. =>
			_FrameQuality = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 <= _FrameQuality) && (_FrameQuality <= 100))
				FrameQuality = _FrameQuality;
			//.
			if (Device.VideoRecorderModule.Transmitting.BooleanValue()) {
				if (!TMyH264Encoder.IsSupported())
					InitializationCode = VideoFrameServer_Initialization_Code_ServiceIsNotActiveError;
			}
			else
				InitializationCode = VideoFrameServer_Initialization_Code_ServiceAccessIsDisabledError;
			break; //. >
			
		default:
			InitializationCode = VideoFrameServer_Initialization_Code_UnknownServiceError;			
		}
		//. if ((InitializationCode >= 0) && (!MediaFrameServer.flVideoActive)) 
		//. 	InitializationCode = VideoFrameServer_Initialization_Code_ServiceIsNotActiveError;
		//.
		DataDescriptor[0] = (byte)(InitializationCode & 0xff);
		DataDescriptor[1] = (byte)(InitializationCode >> 8 & 0xff);
		DataDescriptor[2] = (byte)(InitializationCode >> 16 & 0xff);
		DataDescriptor[3] = (byte)(InitializationCode >>> 24);
		DestinationConnectionOutputStream.write(DataDescriptor);		
		//.
		if (InitializationCode < 0)
			return; //. ->
		//. send frame rate
		DataDescriptor[0] = (byte)(MediaFrameServer.FrameRate & 0xff);
		DataDescriptor[1] = (byte)(MediaFrameServer.FrameRate >> 8 & 0xff);
		DataDescriptor[2] = (byte)(MediaFrameServer.FrameRate >> 16 & 0xff);
		DataDescriptor[3] = (byte)(MediaFrameServer.FrameRate >>> 24);
		DestinationConnectionOutputStream.write(DataDescriptor);		
		//. capturing
        byte[] 	FrameBuffer = new byte[0];
        int 	FrameBufferSize = 0;
        long	FrameTimestamp = 0;
        byte[] 	FrameTimestampBA = new byte[8];
		int		FrameWidth = 0;
		int		FrameHeight = 0;
		Rect	FrameRect = new Rect();
		int 	FrameFormat = 0;
		boolean flProcessFrame;
		switch (Service) {
		case VideoFrameServer_Service_JPEGFrames: 
			ByteArrayOutputStream FrameStream = new ByteArrayOutputStream();
			try {
				try {
					if (FrameRate >= 0) {
						while (!Canceller.flCancel) {
							if (MediaFrameServer.flVideoActive) {
								synchronized (MediaFrameServer.CurrentFrame) {
									MediaFrameServer.CurrentFrame.wait(MediaFrameServer.FrameInterval);
									//.
									if (MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
										FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameFormat = MediaFrameServer.CurrentFrame.Format;
										FrameBufferSize = MediaFrameServer.CurrentFrame.DataSize;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
										//.
										flProcessFrame = true;
									}
									else flProcessFrame = false;
								}
								if (flProcessFrame) {
									FrameStream.reset();
									//. write frame timestamp
									TDataConverter.ConvertDoubleToLEByteArray(FrameTimestamp,FrameTimestampBA);
									FrameStream.write(FrameTimestampBA);
									//.
									switch (FrameFormat) {
									
						            case ImageFormat.NV16:
						            case ImageFormat.NV21:
						            case ImageFormat.YUY2:
						            case ImageFormat.YV12:
						                new YuvImage(FrameBuffer, FrameFormat, FrameWidth,FrameHeight, null).compressToJpeg(FrameRect, FrameQuality, FrameStream);
						                break; //. >

						            default:
						                throw new IOException("unsupported image format"); //. =>
									}
									//.
									Size = FrameStream.size();
									DataDescriptor[0] = (byte)(Size & 0xff);
									DataDescriptor[1] = (byte)(Size >> 8 & 0xff);
									DataDescriptor[2] = (byte)(Size >> 16 & 0xff);
									DataDescriptor[3] = (byte)(Size >>> 24);
									//.
									DestinationConnectionOutputStream.write(DataDescriptor);
									FrameStream.writeTo(DestinationConnectionOutputStream);
								}
							}
							else
								Thread.sleep(1000);
				        }
					}
					else {
						int FrameInterval = -FrameRate;
						while (!Canceller.flCancel) {
							if (MediaFrameServer.flVideoActive) {
								Thread.sleep(FrameInterval);
								//.
								synchronized (MediaFrameServer.CurrentFrame) {
									if (MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
										FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameFormat = MediaFrameServer.CurrentFrame.Format;
										FrameBufferSize = MediaFrameServer.CurrentFrame.DataSize;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
										//.
										flProcessFrame = true;
									}
									else flProcessFrame = false;
								}
								if (flProcessFrame) {
									FrameStream.reset();
									//. write frame timestamp
									TDataConverter.ConvertDoubleToLEByteArray(FrameTimestamp,FrameTimestampBA);
									FrameStream.write(FrameTimestampBA);
									//.
									switch (FrameFormat) {
									
						            case ImageFormat.NV16:
						            case ImageFormat.NV21:
						            case ImageFormat.YUY2:
						            case ImageFormat.YV12:
						                new YuvImage(FrameBuffer, FrameFormat, FrameWidth,FrameHeight, null).compressToJpeg(FrameRect, FrameQuality, FrameStream);
						                break; //. >

						            default:
						                throw new IOException("unsupported image format"); //. =>
									}
									//.
									Size = FrameStream.size();
									DataDescriptor[0] = (byte)(Size & 0xff);
									DataDescriptor[1] = (byte)(Size >> 8 & 0xff);
									DataDescriptor[2] = (byte)(Size >> 16 & 0xff);
									DataDescriptor[3] = (byte)(Size >>> 24);
									//.
									DestinationConnectionOutputStream.write(DataDescriptor);
									FrameStream.writeTo(DestinationConnectionOutputStream);
								}
							}
							else
								Thread.sleep(1000);
				        }
					}
			        //. send disconnect message (Descriptor = 0)
					DataDescriptor[0] = 0;
					DataDescriptor[1] = 0;
					DataDescriptor[2] = 0;
					DataDescriptor[3] = 0;
					DestinationConnectionOutputStream.write(DataDescriptor);
				}
				catch (InterruptedException IE) {
				}
			}
			finally {
				FrameStream.close();
			}
			break; //. >
			
		case VideoFrameServer_Service_H264Frames:
			TMyH264Encoder MyH264Encoder = new TMyH264Encoder(MediaFrameServer.FrameSize.width,MediaFrameServer.FrameSize.height, MediaFrameServer.FrameBitRate, MediaFrameServer.FrameRate, DestinationConnectionOutputStream, false);
			try {
				try {
					long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flVideoActive) {
							synchronized (MediaFrameServer.CurrentFrame) {
								MediaFrameServer.CurrentFrame.wait(MediaFrameServer.FrameInterval);
								//.
								if (MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
									FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
									FrameWidth = MediaFrameServer.CurrentFrame.Width;
									if (FrameWidth != FrameRect.right) 
										FrameRect.right = FrameWidth;
									FrameHeight = MediaFrameServer.CurrentFrame.Height;
									if (FrameHeight != FrameRect.bottom) 
										FrameRect.bottom = FrameHeight;
									FrameFormat = MediaFrameServer.CurrentFrame.Format;
									FrameBufferSize = MediaFrameServer.CurrentFrame.DataSize;
									if (FrameBuffer.length != FrameBufferSize)
										FrameBuffer = new byte[FrameBufferSize];
									System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
									//.
									flProcessFrame = true;
								}
								else flProcessFrame = false;
							}
							if (flProcessFrame) {
				            	MyH264Encoder.EncodeInputBuffer(FrameBuffer,FrameBufferSize,SystemClock.elapsedRealtime()-TimestampBase);
							}
						}
						else
							Thread.sleep(1000);
			        }
			        //. send disconnect message (Descriptor = 0)
					DataDescriptor[0] = 0;
					DataDescriptor[1] = 0;
					DataDescriptor[2] = 0;
					DataDescriptor[3] = 0;
					DestinationConnectionOutputStream.write(DataDescriptor);
				}
				catch (InterruptedException IE) {
				}
			}
			finally {
				MyH264Encoder.Destroy();
			}
			break; //. >

		case VideoFrameServer_Service_H264Frames1:
			TMyH264Encoder1 MyH264Encoder1 = new TMyH264Encoder1(MediaFrameServer.FrameSize.width,MediaFrameServer.FrameSize.height, MediaFrameServer.FrameBitRate, MediaFrameServer.FrameRate, DestinationConnectionOutputStream);
			try {
				try {
					long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flVideoActive) {
							synchronized (MediaFrameServer.CurrentFrame) {
								MediaFrameServer.CurrentFrame.wait(MediaFrameServer.FrameInterval);
								//.
								if (MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
									FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
									FrameWidth = MediaFrameServer.CurrentFrame.Width;
									if (FrameWidth != FrameRect.right) 
										FrameRect.right = FrameWidth;
									FrameHeight = MediaFrameServer.CurrentFrame.Height;
									if (FrameHeight != FrameRect.bottom) 
										FrameRect.bottom = FrameHeight;
									FrameFormat = MediaFrameServer.CurrentFrame.Format;
									FrameBufferSize = MediaFrameServer.CurrentFrame.DataSize;
									if (FrameBuffer.length != FrameBufferSize)
										FrameBuffer = new byte[FrameBufferSize];
									System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
									//.
									flProcessFrame = true;
								}
								else flProcessFrame = false;
							}
							if (flProcessFrame) {
				            	MyH264Encoder1.EncodeInputBuffer(FrameBuffer,FrameBufferSize,SystemClock.elapsedRealtime()-TimestampBase);
							}
						}
						else
							Thread.sleep(1000);
			        }
				}
				catch (InterruptedException IE) {
				}
			}
			finally {
				MyH264Encoder1.Destroy();
			}
			break; //. >
		}
    }
    
    public void VideoFrameServer_Capturing(String Configuration, DatagramSocket IOSocket, String OutputAddress, int OutputPort, int OutputProxyType, String ProxyServerAddress, int ProxyServerPort, TCanceller Canceller) throws IOException {
		//. capturing
        @SuppressWarnings("unused")
        byte[] 	FrameBuffer = new byte[0];
        @SuppressWarnings("unused")
        int 	FrameBufferSize = 0;
        @SuppressWarnings("unused")
		long	FrameTimestamp = 0;
        @SuppressWarnings("unused")
        byte[] 	FrameTimestampBA = new byte[8];
        @SuppressWarnings("unused")
		int		FrameWidth = 0;
        @SuppressWarnings("unused")
		int		FrameHeight = 0;
        @SuppressWarnings("unused")
		Rect	FrameRect = new Rect();
        @SuppressWarnings("unused")
		int 	FrameFormat = 0;
        @SuppressWarnings("unused")
		boolean flProcessFrame;
		final H264Encoder Encoder;
		switch (OutputProxyType) {
		
		case TUDPEchoServerClient.PROXY_TYPE_NATIVE:
			Encoder = new TMyH264EncoderProxyUDPRTP(MediaFrameServer.FrameSize.width,MediaFrameServer.FrameSize.height, MediaFrameServer.FrameBitRate, MediaFrameServer.FrameRate, IOSocket, ProxyServerAddress,ProxyServerPort, OutputAddress,OutputPort);
			break; //. >
			
		default:
			Encoder = new TMyH264EncoderUDPRTP(MediaFrameServer.FrameSize.width,MediaFrameServer.FrameSize.height, MediaFrameServer.FrameBitRate, MediaFrameServer.FrameRate, IOSocket, OutputAddress,OutputPort);
			break; //. >
		}
		try {
			try {
	        	TPacketSubscriber PacketSubscriber  = new TPacketSubscriber() {
	        		@Override
	        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
		            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
	        		}
	        	};
	        	MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
	        	try {
					//. long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						Thread.sleep(1000);
						/*//. last version if (MediaFrameServer.flVideoActive) {
							synchronized (MediaFrameServer.CurrentFrame) {
								MediaFrameServer.CurrentFrame.wait(MediaFrameServer.FrameInterval);
								//.
								if (MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
									FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
									FrameWidth = MediaFrameServer.CurrentFrame.Width;
									if (FrameWidth != FrameRect.right) 
										FrameRect.right = FrameWidth;
									FrameHeight = MediaFrameServer.CurrentFrame.Height;
									if (FrameHeight != FrameRect.bottom) 
										FrameRect.bottom = FrameHeight;
									FrameFormat = MediaFrameServer.CurrentFrame.Format;
									FrameBufferSize = MediaFrameServer.CurrentFrame.DataSize;
									if (FrameBuffer.length != FrameBufferSize)
										FrameBuffer = new byte[FrameBufferSize];
									System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
									//.
									flProcessFrame = true;
								}
								else flProcessFrame = false;
							}
							if (flProcessFrame) {
				            	Encoder.EncodeInputBuffer(FrameBuffer,FrameBufferSize,SystemClock.elapsedRealtime()-TimestampBase);
							}
						}
						else
							Thread.sleep(1000);*/
					}
	        	}
	        	finally {
		        	MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
	        	}
			}
			catch (InterruptedException IE) {
			}
		}
		finally {
			Encoder.Destroy();
		}
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		@SuppressWarnings("unused")
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("VideoModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
	    SaveConfigurationLocally();
		//.
		int Version = 1;
        Serializer.startTag("", "VideoModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.endTag("", "VideoModule");
    }
    
    public void SaveConfigurationLocally() throws IOException {
	}    
}
