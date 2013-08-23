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

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264Encoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.Utils.TDataConverter;

/**
 *
 * @author ALXPONOM
 */
public class TVideoModule extends TModule 
{
	public static final String Folder = TDEVICEModule.DeviceFolder+"/"+"VideoModule";
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

		public TMyH264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pOutputStream,null,null);
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

		public TMyH264Encoder1(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pOutputStream,null,null);
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
	
	public TUserAccessKey UserAccessKey;
	
    public TVideoModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder);
		if (!F.exists()) 
			F.mkdirs();
		//.
		UserAccessKey = new TUserAccessKey();
        //.
    	try {
			LoadConfiguration();
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
        double LastTimestamp = 0.0;
        byte[] 	FrameBuffer = new byte[0];
        int 	FrameBufferSize = 0;
        double 	FrameTimestamp = 0.0;
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
									if (MediaFrameServer.CurrentFrame.Timestamp > LastTimestamp) {
										FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameFormat = MediaFrameServer.CurrentFrame.Format;
										FrameBufferSize = MediaFrameServer.CurrentFrame.Data.length;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
										//.
										LastTimestamp = MediaFrameServer.CurrentFrame.Timestamp; 
										//.
										flProcessFrame = true;
									}
									else flProcessFrame = false;
								}
								if (flProcessFrame) {
									FrameStream.reset();
									//. write frame timestamp
									TDataConverter.ConvertDoubleToBEByteArray(FrameTimestamp,FrameTimestampBA);
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
									if (MediaFrameServer.CurrentFrame.Timestamp > LastTimestamp) {
										FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameFormat = MediaFrameServer.CurrentFrame.Format;
										FrameBufferSize = MediaFrameServer.CurrentFrame.Data.length;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
										//.
										LastTimestamp = MediaFrameServer.CurrentFrame.Timestamp; 
										//.
										flProcessFrame = true;
									}
									else flProcessFrame = false;
								}
								if (flProcessFrame) {
									FrameStream.reset();
									//. write frame timestamp
									TDataConverter.ConvertDoubleToBEByteArray(FrameTimestamp,FrameTimestampBA);
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
			TMyH264Encoder MyH264Encoder = new TMyH264Encoder(MediaFrameServer.FrameSize.width,MediaFrameServer.FrameSize.height, MediaFrameServer.FrameBitRate, MediaFrameServer.FrameRate, DestinationConnectionOutputStream);
			try {
				try {
					long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flVideoActive) {
							synchronized (MediaFrameServer.CurrentFrame) {
								MediaFrameServer.CurrentFrame.wait(MediaFrameServer.FrameInterval);
								//.
								if (MediaFrameServer.CurrentFrame.Timestamp > LastTimestamp) {
									FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
									FrameWidth = MediaFrameServer.CurrentFrame.Width;
									if (FrameWidth != FrameRect.right) 
										FrameRect.right = FrameWidth;
									FrameHeight = MediaFrameServer.CurrentFrame.Height;
									if (FrameHeight != FrameRect.bottom) 
										FrameRect.bottom = FrameHeight;
									FrameFormat = MediaFrameServer.CurrentFrame.Format;
									FrameBufferSize = MediaFrameServer.CurrentFrame.Data.length;
									if (FrameBuffer.length != FrameBufferSize)
										FrameBuffer = new byte[FrameBufferSize];
									System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
									//.
									LastTimestamp = MediaFrameServer.CurrentFrame.Timestamp; 
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
								if (MediaFrameServer.CurrentFrame.Timestamp > LastTimestamp) {
									FrameTimestamp = MediaFrameServer.CurrentFrame.Timestamp;
									FrameWidth = MediaFrameServer.CurrentFrame.Width;
									if (FrameWidth != FrameRect.right) 
										FrameRect.right = FrameWidth;
									FrameHeight = MediaFrameServer.CurrentFrame.Height;
									if (FrameHeight != FrameRect.bottom) 
										FrameRect.bottom = FrameHeight;
									FrameFormat = MediaFrameServer.CurrentFrame.Format;
									FrameBufferSize = MediaFrameServer.CurrentFrame.Data.length;
									if (FrameBuffer.length != FrameBufferSize)
										FrameBuffer = new byte[FrameBufferSize];
									System.arraycopy(MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
									//.
									LastTimestamp = MediaFrameServer.CurrentFrame.Timestamp; 
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
    
    @Override
    public synchronized void LoadConfiguration() throws Exception {
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
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
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
