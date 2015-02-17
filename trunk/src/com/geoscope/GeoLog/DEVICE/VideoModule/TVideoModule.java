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
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Memory.Buffering.TMemoryBuffering;
import com.geoscope.Classes.IO.Memory.Buffering.TMemoryBuffering.TBuffer;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPEncoder;
import com.geoscope.Classes.IO.Protocols.RTP.TRTPPacket;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264Encoder;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264EncoderServer;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264Transcoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.TMediaFrameServer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpEncoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpPacket;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;
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
	private static final int VideoStream_BufferingCount = 10;
	//.
	public static final int VideoStream_DefaultFlashInterval = 10; //. ms
	public static final int VideoStream_Streaming_DefaultFlashInterval = 20; //. ms
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
	
	private static class TMyH264Encoder extends TH264Encoder {

		protected OutputStream 	MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;
		
		public TMyH264Encoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, OutputStream pOutputStream, boolean pflParseParameters, int pFlashInterval) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat, pflParseParameters);
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							SendBuffer(Buffer.Data,Buffer.Size);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
		            //.
					try {
						if (FlashInterval >= 0) {
							if (FlashInterval > 0) {
								long Time = System.currentTimeMillis();
								if ((Time-FlashInterval_LastTime) >= FlashInterval) {
									FlashInterval_LastTime = Time;
									MyOutputStream.flush();
								}
							}
							else
								MyOutputStream.flush();
						}
					} catch (IOException IOE) {
					}
				}
			});
		}

		@Override
		public void Destroy() throws Exception {
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
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
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264EncoderServerClient extends TH264EncoderServer.TClient {

		protected OutputStream 	MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;
		//.
		protected TH264Transcoder Transcoder;
		
		public TMyH264EncoderServerClient(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, OutputStream pOutputStream, boolean pflApplyParameters, int pFlashInterval) throws IOException {
			super(true);
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							Transcoder.DoOnInputBuffer(Buffer.Data,Buffer.Size, Buffer.Timestamp);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
				}
			});
			//.
			Transcoder = new TH264Transcoder(pDevice, pInFrameWidth,pInFrameHeight, pOutFrameWidth,pOutFrameHeight, pOutBitRate, pOutFrameRate, !pflApplyParameters) {
				
				@Override
				public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp, boolean flSyncFrame) throws IOException {
					SendBuffer(input,input_size);
				}
			};
		}

		@Override
		public void Destroy() throws Exception {
			if (Transcoder != null) {
				Transcoder.Destroy();
				Transcoder = null;
			}
			//.
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
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
            //.
			if (FlashInterval >= 0) {
				if (FlashInterval > 0) {
					long Time = System.currentTimeMillis();
					if ((Time-FlashInterval_LastTime) >= FlashInterval) {
						FlashInterval_LastTime = Time;
						MyOutputStream.flush();
					}
				}
				else
					MyOutputStream.flush();
			}
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264Encoder1 extends TH264Encoder {

		protected OutputStream 	MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;

		public TMyH264Encoder1(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, OutputStream pOutputStream, int pFlashInterval) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat);
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							SendBuffer(Buffer.Data,Buffer.Size);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
		            //.
					try {
						if (FlashInterval >= 0) {
							if (FlashInterval > 0) {
								long Time = System.currentTimeMillis();
								if ((Time-FlashInterval_LastTime) >= FlashInterval) {
									FlashInterval_LastTime = Time;
									MyOutputStream.flush();
								}
							}
							else
								MyOutputStream.flush();
						}
					} catch (IOException IOE) {
					}
				}
			});
		}

		@Override
		public void Destroy() throws Exception {
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			//.
			MyOutputStream.write(Buffer, 0,BufferSize);
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
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264EncoderServerClient1 extends TH264EncoderServer.TClient {

		protected OutputStream 	MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;
		//.
		protected TH264Transcoder Transcoder;
	 
		public TMyH264EncoderServerClient1(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, OutputStream pOutputStream, boolean pflApplyParameters, int pFlashInterval) throws IOException {
			super(true);
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							Transcoder.DoOnInputBuffer(Buffer.Data,Buffer.Size, Buffer.Timestamp);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
				}
			});
			//.
			Transcoder = new TH264Transcoder(pDevice, pInFrameWidth,pInFrameHeight, pOutFrameWidth,pOutFrameHeight, pOutBitRate, pOutFrameRate, !pflApplyParameters) {
				
				@Override
				public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp, boolean flSyncFrame) throws IOException {
					SendBuffer(input,input_size);
				}
			};
		}

		@Override
		public void Destroy() throws Exception {
			if (Transcoder != null) {
				Transcoder.Destroy();
				Transcoder = null;
			}
			//.
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
		}

		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			//.
			MyOutputStream.write(Buffer, 0,BufferSize);
            //.
			if (FlashInterval >= 0) {
				if (FlashInterval > 0) {
					long Time = System.currentTimeMillis();
					if ((Time-FlashInterval_LastTime) >= FlashInterval) {
						FlashInterval_LastTime = Time;
						MyOutputStream.flush();
					}
				}
				else
					MyOutputStream.flush();
			}
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264EncoderUDPRTP extends TH264Encoder {

		private DatagramSocket OutputSocket;
		private String Address;
		private int Port;
		//.
		private int Timestamp = 0;
		//.
		private TRtpEncoder RtpEncoder;
		
		public TMyH264EncoderUDPRTP(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, DatagramSocket pOutputSocket, String pAddress, int pPort) throws UnknownHostException {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat);
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
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyH264EncoderServerClientUDPRTP extends TH264EncoderServer.TClient {

		private DatagramSocket OutputSocket;
		private String Address;
		private int Port;
		//.
		private int Timestamp = 0;
		//.
		private TRtpEncoder RtpEncoder;
		//.
		protected TH264Transcoder Transcoder;
		
		public TMyH264EncoderServerClientUDPRTP(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, DatagramSocket pOutputSocket, String pAddress, int pPort, boolean pflApplyParameters) throws IOException {
			super(true);
			OutputSocket = pOutputSocket;
			Address = pAddress;
			Port = pPort;
			//.
			Transcoder = new TH264Transcoder(pDevice, pInFrameWidth,pInFrameHeight, pOutFrameWidth,pOutFrameHeight, pOutBitRate, pOutFrameRate, !pflApplyParameters) {
				
				@Override
				public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp, boolean flSyncFrame) throws IOException {
					SendBuffer(input,input_size);
				}
			};
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
		
		@Override
		public void Destroy() throws Exception {
			if (Transcoder != null) {
				Transcoder.Destroy();
				Transcoder = null;
			}
			//.
			super.Destroy();
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RtpEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Transcoder.DoOnInputBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264EncoderProxyUDPRTP extends TH264Encoder {

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
		
		public TMyH264EncoderProxyUDPRTP(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, DatagramSocket pOutputSocket, String pProxyServerAddress, int pProxyServerPort, String pAddress, int pPort) throws UnknownHostException {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat);
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
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyH264EncoderServerClientProxyUDPRTP extends TH264EncoderServer.TClient {

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
		//.
		protected TH264Transcoder Transcoder;
		
		public TMyH264EncoderServerClientProxyUDPRTP(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, DatagramSocket pOutputSocket, String pProxyServerAddress, int pProxyServerPort, String pAddress, int pPort, boolean pflApplyParameters) throws IOException {
			super(true);
			OutputSocket = pOutputSocket;
			ProxyServerAddress = pProxyServerAddress;
			ProxyServerPort = pProxyServerPort;
			Address = pAddress;
			Port = pPort;
			//.
			Transcoder = new TH264Transcoder(pDevice, pInFrameWidth,pInFrameHeight, pOutFrameWidth,pOutFrameHeight, pOutBitRate, pOutFrameRate, !pflApplyParameters) {
				
				@Override
				public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp, boolean flSyncFrame) throws IOException {
					SendBuffer(input,input_size);
				}
			};
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
		
		@Override
		public void Destroy() throws Exception {
			if (Transcoder != null) {
				Transcoder.Destroy();
				Transcoder = null;
			}
			//.
			super.Destroy();
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RtpEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Transcoder.DoOnInputBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264UDPRTPEncoder extends TH264Encoder {

		private OutputStream MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;
		//.
		private int Timestamp = 0;
		//.
		private TRTPEncoder RTPEncoder;
		
		public TMyH264UDPRTPEncoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, OutputStream pOutputStream, boolean pflParseParameters, int pFlashInterval) throws UnknownHostException {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat, pflParseParameters);
			//.
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							SendBuffer(Buffer.Data,Buffer.Size);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
				}
			});
			//.
			RTPEncoder = new TRTPEncoder() {  
				
				private int PacketIndex = 0;
				
				@Override
				public void DoOnOutput(TRTPPacket OutputPacket) throws IOException {
					///test Log.v("UDP packet", "-> TS: "+Long.toString(System.currentTimeMillis())+", sent: "+Integer.toString(OutputPacket.buffer_length));
					if (PacketIndex > 2) { //. skip codec configuration packets  
						OutputPacket.SendToStream(MyOutputStream);
			            //.
						if (FlashInterval >= 0) {
							if (FlashInterval > 0) {
								long Time = System.currentTimeMillis();
								if ((Time-FlashInterval_LastTime) >= FlashInterval) {
									FlashInterval_LastTime = Time;
									MyOutputStream.flush();
								}
							}
							else
								MyOutputStream.flush();
						}
					}
					//.
					PacketIndex++;
				}
			};
		}
		
		@Override
		public void Destroy() throws Exception {
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RTPEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
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
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	private static class TMyH264UDPRTPEncoderServerClient extends TH264EncoderServer.TClient {

		private OutputStream MyOutputStream = null;
		//.
		private int		FlashInterval;
		private long 	FlashInterval_LastTime;
		//.
		private TMemoryBuffering Buffering;
		//.
		private int Timestamp = 0;
		//.
		private TRTPEncoder RTPEncoder;
		//.
		protected TH264Transcoder Transcoder;
		
		public TMyH264UDPRTPEncoderServerClient(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, OutputStream pOutputStream, boolean pflApplyParameters, int pFlashInterval) throws IOException {
			super(true);
			//.
			MyOutputStream = pOutputStream;
			FlashInterval = pFlashInterval;
			//.
			FlashInterval_LastTime = System.currentTimeMillis();
			//.
			Buffering = new TMemoryBuffering(VideoStream_BufferingCount, new TMemoryBuffering.TOnBufferDequeueHandler() {
				
				@Override
				public void DoOnBufferDequeue(TBuffer Buffer) {
					synchronized (Buffer) {
						try {
							Transcoder.DoOnInputBuffer(Buffer.Data,Buffer.Size, Buffer.Timestamp);
						} catch (IOException IOE) {
							return; //. ->
						}
					}
				}
			});
			//.
			Transcoder = new TH264Transcoder(pDevice, pInFrameWidth,pInFrameHeight, pOutFrameWidth,pOutFrameHeight, pOutBitRate, pOutFrameRate, !pflApplyParameters) {
				
				@Override
				public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp, boolean flSyncFrame) throws IOException {
					SendBuffer(input,input_size);
				}
			};
			//.
			RTPEncoder = new TRTPEncoder() {  
				
				private int PacketIndex = 0;
				
				@Override
				public void DoOnOutput(TRTPPacket OutputPacket) throws IOException {
					///test Log.v("UDP packet", "-> TS: "+Long.toString(System.currentTimeMillis())+", sent: "+Integer.toString(OutputPacket.buffer_length));
					if (PacketIndex > 2) { //. skip codec configuration packets  
						OutputPacket.SendToStream(MyOutputStream);
			            //.
						if (FlashInterval >= 0) {
							if (FlashInterval > 0) {
								long Time = System.currentTimeMillis();
								if ((Time-FlashInterval_LastTime) >= FlashInterval) {
									FlashInterval_LastTime = Time;
									MyOutputStream.flush();
								}
							}
							else
								MyOutputStream.flush();
						}
					}
					//.
					PacketIndex++;
				}
			};
		}
		
		@Override
		public void Destroy() throws Exception {
			if (Transcoder != null) {
				Transcoder.Destroy();
				Transcoder = null;
			}
			//.
			if (Buffering != null) {
				Buffering.Destroy();
				Buffering = null;
			}
			//.
			super.Destroy();
		}
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RTPEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			Buffering.EnqueueBuffer(Buffer,BufferSize, Timestamp);
		}
	}
	
	public static class TH264VideoStreamer extends TDEVICEModule.TComponentDataStreaming.TStreamer {

		public static String TypeID() {
			return "Video.H264";
		}
		
		private class TProcessing extends TCancelableThread {
			
			public TProcessing() {
			}
			
			public void Destroy() throws InterruptedException {
				Stop();
			}
			
	    	public void Start() {
        		_Thread = new Thread(this);
        		_Thread.start();
	    	}
	    	
	    	public void Stop() throws InterruptedException {
	    		CancelAndWait();
	    	}
	    	
			@Override
			public void run() {
				try {
					if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
						TMyH264EncoderServerClient EncoderServerClient = new TMyH264EncoderServerClient(Device, FrameWidth,FrameHeight, FrameWidth,FrameHeight, FrameBitRate, FrameRate, StreamingBuffer_OutputStream, false, VideoStream_Streaming_DefaultFlashInterval);
						try {
							try {
								Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
								try {
									while (!Canceller.flCancel) {
										Thread.sleep(1000);
									}
								}
								finally {
									Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
								}
							}
							catch (InterruptedException IE) {
							}
						}
						finally {
							EncoderServerClient.Destroy();
						}
					}
					else {
						final TH264Encoder Encoder = new TMyH264Encoder(FrameWidth,FrameHeight, FrameBitRate, FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, StreamingBuffer_OutputStream, true, VideoStream_Streaming_DefaultFlashInterval); 
						try {
							try {
					        	TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
					        		@Override
					        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
					        			try {
							            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
					        			}
					        			catch (Exception E) {
					        				Canceller.Cancel();
					        			}
					        		}
					        	};
					        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
					        	try {
									while (!Canceller.flCancel) {
										Thread.sleep(1000);
									}
					        	}
					        	finally {
					        		Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
					        	}
							}
							catch (InterruptedException IE) {
							}
						}
						finally {
							Encoder.Destroy();
						}
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
		
		private int FrameWidth;
		private int FrameHeight;
		private int FrameBitRate;
		private int FrameRate;
		//.
		private TProcessing Processing = null;
		//.
		private TDEVICEModule.TComponentDataStreamingAbstract DataStreaming = null;
		
		public TH264VideoStreamer(TDEVICEModule pDevice, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
			super(pDevice, pidTComponent,pidComponent, pChannelID, pConfiguration, pParameters, 4, 8192);
			//.
			FrameWidth = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResX;
			FrameHeight = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResY;
			FrameBitRate = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate;
			if (FrameBitRate <= 0)
				FrameBitRate = 1000000;
			FrameRate = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate;
			if (FrameRate <= 0)
				FrameRate = 15;
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
		public void Stop() throws Exception {
			if (DataStreaming != null) {
				DataStreaming.Destroy();
				DataStreaming = null;
			}
			if (Processing != null) {
				Processing.Destroy();
				Processing = null;
			}
		}
		
		@Override
		public boolean Streaming_SourceIsActive() {
			return Device.VideoRecorderModule.MediaFrameServer.flVideoActive;
		}
	}
	
	public static class TH264UDPRTPVideoStreamer extends TDEVICEModule.TComponentDataStreaming.TStreamer {

		public static String TypeID() {
			return "Video.H264UDPRTP";
		}
		
		private class TProcessing extends TCancelableThread {
			
			public TProcessing() {
			}
			
			public void Destroy() throws InterruptedException {
				Stop();
			}
			
	    	public void Start() {
        		_Thread = new Thread(this);
        		_Thread.start();
	    	}
	    	
	    	public void Stop() throws InterruptedException {
	    		CancelAndWait();
	    	}
	    	
			@Override
			public void run() {
				try {
					if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
						TMyH264UDPRTPEncoderServerClient EncoderServerClient = new TMyH264UDPRTPEncoderServerClient(Device, FrameWidth,FrameHeight, FrameWidth,FrameHeight, FrameBitRate, FrameRate, StreamingBuffer_OutputStream, false, VideoStream_Streaming_DefaultFlashInterval);
						try {
							try {
								Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
								try {
									while (!Canceller.flCancel) {
										Thread.sleep(1000);
									}
								}
								finally {
									Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
								}
							}
							catch (InterruptedException IE) {
							}
						}
						finally {
							EncoderServerClient.Destroy();
						}
					}
					else {
						final TH264Encoder Encoder = new TMyH264UDPRTPEncoder(FrameWidth,FrameHeight, FrameBitRate, FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, StreamingBuffer_OutputStream, true, VideoStream_Streaming_DefaultFlashInterval); 
						try {
							try {
								TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
					        		@Override
					        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
					        			try {
							            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
					        			}
					        			catch (Exception E) {
					        				Canceller.Cancel();
					        			}
					        		}
					        	};
					        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
					        	try {
									while (!Canceller.flCancel) {
										Thread.sleep(1000);
									}
					        	}
					        	finally {
						        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
					        	}
							}
							catch (InterruptedException IE) {
							}
						}
						finally {
							Encoder.Destroy();
						}
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
		
		private int FrameWidth;
		private int FrameHeight;
		private int FrameBitRate;
		private int FrameRate;
		//.
		private TProcessing Processing = null;
		//.
		private TDEVICEModule.TComponentDataStreamingAbstract DataStreaming = null;
		
		public TH264UDPRTPVideoStreamer(TDEVICEModule pDevice, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
			super(pDevice, pidTComponent,pidComponent, pChannelID, pConfiguration, pParameters, 2, 8192);
			//.
			FrameWidth = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResX;
			FrameHeight = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_ResY;
			FrameBitRate = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate;
			if (FrameBitRate <= 0)
				FrameBitRate = 1000000;
			FrameRate = Device.VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate;
			if (FrameRate <= 0)
				FrameRate = 15;
			//.
			Processing = new TProcessing();
			DataStreaming = Device.TComponentDataStreamingUDP_Create(this);
		}
		
		@Override
		public void Start() {
			Processing.Start();
			DataStreaming.Start();
		}

		@Override
		public void Stop() throws Exception {
			if (DataStreaming != null) {
				DataStreaming.Destroy();
				DataStreaming = null;
			}
			if (Processing != null) {
				Processing.Destroy();
				Processing = null;
			}
		}
		
		@Override
		public boolean Streaming_SourceIsActive() {
			return Device.VideoRecorderModule.MediaFrameServer.flVideoActive;
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
    
    public void VideoFrameServer_Capturing(InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, final TCanceller Canceller) throws Exception {
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
		DataDescriptor[0] = (byte)(Device.VideoRecorderModule.MediaFrameServer.FrameRate & 0xff);
		DataDescriptor[1] = (byte)(Device.VideoRecorderModule.MediaFrameServer.FrameRate >> 8 & 0xff);
		DataDescriptor[2] = (byte)(Device.VideoRecorderModule.MediaFrameServer.FrameRate >> 16 & 0xff);
		DataDescriptor[3] = (byte)(Device.VideoRecorderModule.MediaFrameServer.FrameRate >>> 24);
		DestinationConnectionOutputStream.write(DataDescriptor);		
		//. capturing
        byte[] 	FrameBuffer = new byte[0];
        int 	FrameBufferSize = 0;
        long	FrameTimestamp = 0;
        byte[] 	FrameTimestampBA = new byte[8];
		int		FrameWidth = 0;
		int		FrameHeight = 0;
		Rect	FrameRect = new Rect();
		boolean flProcessFrame;
		switch (Service) {
		case VideoFrameServer_Service_JPEGFrames: 
			ByteArrayOutputStream FrameStream = new ByteArrayOutputStream();
			try {
				try {
					if (FrameRate >= 0) {
						while (!Canceller.flCancel) {
							if (Device.VideoRecorderModule.MediaFrameServer.flVideoActive) {
								synchronized (Device.VideoRecorderModule.MediaFrameServer.CurrentFrame) {
									Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.wait(Device.VideoRecorderModule.MediaFrameServer.FrameInterval);
									//.
									if (Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
										FrameTimestamp = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameBufferSize = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.DataSize;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
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
									switch (Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat) {
									
						            case ImageFormat.NV16:
						            case ImageFormat.NV21:
						            case ImageFormat.YUY2:
						            case ImageFormat.YV12:
						                new YuvImage(FrameBuffer, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, FrameWidth,FrameHeight, null).compressToJpeg(FrameRect, FrameQuality, FrameStream);
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
							if (Device.VideoRecorderModule.MediaFrameServer.flVideoActive) {
								Thread.sleep(FrameInterval);
								//.
								synchronized (Device.VideoRecorderModule.MediaFrameServer.CurrentFrame) {
									if (Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Timestamp > FrameTimestamp) {
										FrameTimestamp = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Timestamp;
										FrameWidth = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Width;
										if (FrameWidth != FrameRect.right) 
											FrameRect.right = FrameWidth;
										FrameHeight = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Height;
										if (FrameHeight != FrameRect.bottom) 
											FrameRect.bottom = FrameHeight;
										FrameBufferSize = Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.DataSize;
										if (FrameBuffer.length != FrameBufferSize)
											FrameBuffer = new byte[FrameBufferSize];
										System.arraycopy(Device.VideoRecorderModule.MediaFrameServer.CurrentFrame.Data,0, FrameBuffer,0, FrameBufferSize);
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
									switch (Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat) {
									
						            case ImageFormat.NV16:
						            case ImageFormat.NV21:
						            case ImageFormat.YUY2:
						            case ImageFormat.YV12:
						                new YuvImage(FrameBuffer, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, FrameWidth,FrameHeight, null).compressToJpeg(FrameRect, FrameQuality, FrameStream);
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
			if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
				TMyH264EncoderServerClient EncoderServerClient = new TMyH264EncoderServerClient(Device, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, DestinationConnectionOutputStream, true, VideoStream_DefaultFlashInterval);
				try {
					try {
						Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
						try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
						}
						finally {
							Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
						}
					}
					catch (InterruptedException IE) {
					}
			        //. send disconnect message (Descriptor = 0)
					DataDescriptor[0] = 0;
					DataDescriptor[1] = 0;
					DataDescriptor[2] = 0;
					DataDescriptor[3] = 0;
					DestinationConnectionOutputStream.write(DataDescriptor);
				}
				finally {
					EncoderServerClient.Destroy();
				}
			}
			else {
				final TMyH264Encoder Encoder = new TMyH264Encoder(Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, DestinationConnectionOutputStream, false, VideoStream_DefaultFlashInterval);
				try {
					try {
			        	TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
			        		@Override
			        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
			        			try {
					            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
			        			}
			        			catch (Exception E) {
			        				Canceller.Cancel();
			        			}
			        		}
			        	};
			        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
			        	try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
			        	}
			        	finally {
			        		Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
			        	}
					}
					catch (InterruptedException IE) {
					}
			        //. send disconnect message (Descriptor = 0)
					DataDescriptor[0] = 0;
					DataDescriptor[1] = 0;
					DataDescriptor[2] = 0;
					DataDescriptor[3] = 0;
					DestinationConnectionOutputStream.write(DataDescriptor);
				}
				finally {
					Encoder.Destroy();
				}
			}
			break; //. >

		case VideoFrameServer_Service_H264Frames1:
			if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
				TMyH264EncoderServerClient1 EncoderServerClient = new TMyH264EncoderServerClient1(Device, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, DestinationConnectionOutputStream, true, VideoStream_DefaultFlashInterval);
				try {
					try {
						Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
						try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
						}
						finally {
							Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
						}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					EncoderServerClient.Destroy();
				}
			}
			else {
				final TMyH264Encoder1 Encoder = new TMyH264Encoder1(Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, DestinationConnectionOutputStream, VideoStream_DefaultFlashInterval);
				try {
					try {
			        	TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
			        		@Override
			        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
			        			try {
					            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
			        			}
			        			catch (Exception E) {
			        				Canceller.Cancel();
			        			}
			        		}
			        	};
			        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
			        	try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
			        	}
			        	finally {
			        		Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
			        	}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					Encoder.Destroy();
				}
			}
			break; //. >
		}
    }
    
    public void VideoFrameServer_Capturing(String Configuration, DatagramSocket IOSocket, String OutputAddress, int OutputPort, int OutputProxyType, String ProxyServerAddress, int ProxyServerPort, final TCanceller Canceller) throws Exception {
		//. capturing
		switch (OutputProxyType) {
		
		case TUDPEchoServerClient.PROXY_TYPE_NATIVE: {
			if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
				TMyH264EncoderServerClientProxyUDPRTP EncoderServerClient = new TMyH264EncoderServerClientProxyUDPRTP(Device, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, IOSocket, ProxyServerAddress,ProxyServerPort, OutputAddress,OutputPort, true);
				try {
					try {
						Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
						try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
						}
						finally {
							Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
						}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					EncoderServerClient.Destroy();
				}
			}
			else {
				final TMyH264EncoderProxyUDPRTP Encoder = new TMyH264EncoderProxyUDPRTP(Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, IOSocket, ProxyServerAddress,ProxyServerPort, OutputAddress,OutputPort);
				try {
					try {
						TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
			        		@Override
			        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
			        			try {
					            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
			        			}
			        			catch (Exception E) {
			        				Canceller.Cancel();
			        			}
			        		}
			        	};
			        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
			        	try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
			        	}
			        	finally {
				        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
			        	}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					Encoder.Destroy();
				}
			}
			break; //. >
		}
			
		default: {
			if (Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
				TMyH264EncoderServerClientUDPRTP EncoderServerClient = new TMyH264EncoderServerClientUDPRTP(Device, Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height,  Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, IOSocket, OutputAddress,OutputPort, true);
				try {
					try {
						Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(EncoderServerClient);
						try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
						}
						finally {
							Device.VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(EncoderServerClient);
						}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					EncoderServerClient.Destroy();
				}
			}
			else {
				final TMyH264EncoderUDPRTP Encoder = new TMyH264EncoderUDPRTP(Device.VideoRecorderModule.MediaFrameServer.FrameSize.width,Device.VideoRecorderModule.MediaFrameServer.FrameSize.height, Device.VideoRecorderModule.MediaFrameServer.FrameBitRate, Device.VideoRecorderModule.MediaFrameServer.FrameRate, Device.VideoRecorderModule.MediaFrameServer.FramePixelFormat, IOSocket, OutputAddress,OutputPort);
				try {
					try {
						TMediaFrameServer.TPacketSubscriber PacketSubscriber = new TMediaFrameServer.TPacketSubscriber() {
			        		@Override
			        		protected void DoOnPacket(byte[] Packet, int PacketSize, long PacketTimestamp) throws IOException {
			        			try {
					            	Encoder.EncodeInputBuffer(Packet,PacketSize, PacketTimestamp);
			        			}
			        			catch (Exception E) {
			        				Canceller.Cancel();
			        			}
			        		}
			        	};
			        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Subscribe(PacketSubscriber);
			        	try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
			        	}
			        	finally {
				        	Device.VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.Unsubscribe(PacketSubscriber);
			        	}
					}
					catch (InterruptedException IE) {
					}
				}
				finally {
					Encoder.Destroy();
				}
			}
			break; //. >
		}
		}
    }
    
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(String TypeID, int idTComponent, long idComponent, int ChannelID, String Configuration, String Parameters) throws Exception {
		if (TH264VideoStreamer.TypeID().equals(TypeID))
			return new TH264VideoStreamer(Device, idTComponent,idComponent, ChannelID, Configuration, Parameters); //. ->
		else
			if (TH264UDPRTPVideoStreamer.TypeID().equals(TypeID))
				return new TH264UDPRTPVideoStreamer(Device, idTComponent,idComponent, ChannelID, Configuration, Parameters); //. ->
			else
				return null; //. ->
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
