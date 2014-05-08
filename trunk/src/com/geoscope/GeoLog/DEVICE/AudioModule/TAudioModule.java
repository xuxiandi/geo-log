/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.widget.Toast;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16ArrayValue;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AACEncoder;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer.TPacketSubscriber;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.RtcpBuffer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.RtpBuffer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpEncoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.TRtpPacket;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.Utils.TDataConverter;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

/**
 *
 * @author ALXPONOM
 */
public class TAudioModule extends TModule 
{
	public static final int SourcesCount = 2;
	public static final int DestinationsCount = 4;
	public static final String Folder = TDEVICEModule.DeviceFolder+"/"+"AudioModule";
	public static final String SourcesSensitivitiesConfigurationFile = Folder+"/"+"SourcesSensitivities.cfg"; 
	public static final String DestinationsVolumesConfigurationFile = Folder+"/"+"DestinationsVolumes.cfg";
	//.
	public static final String AudioFileFolder = Folder+"/"+"AudioFiles";
	public static final String AudioFileType 	= ".wma";
	public static final String AudioFileType1 	= ".mp3";	
	//.
	public static final int AudioSampleServer_Service_SamplePackets 		= 1;
	public static final int AudioSampleServer_Service_SampleZippedPackets 	= 2;
	public static final int AudioSampleServer_Service_AACPackets 			= 3;
	public static final int AudioSampleServer_Service_AACRTPPackets 		= 4;
	public static final int AudioSampleServer_Service_AACPackets1 			= 5;
	public static final int AudioSampleServer_Service_AACPackets2 			= 6;
	//.
	public static final int AudioSampleServer_Initialization_Code_Ok 							= 0;
	public static final int AudioSampleServer_Initialization_Code_Error 						= -1;
	public static final int AudioSampleServer_Initialization_Code_UnknownServiceError 			= -2;
	public static final int AudioSampleServer_Initialization_Code_ServiceIsNotActiveError 		= -3;
	public static final int AudioSampleServer_Initialization_Code_ServiceAccessIsDeniedError	= -4;
	public static final int AudioSampleServer_Initialization_Code_ServiceAccessIsDisabledError	= -5;
	//.
	public static final int Loudspeaker_DestinationID = 2;
	public static final int Loudspeaker_SampleRate = 8000;
	public static final int Loudspeaker_SampleInterval = 20; //. ms
	public static final int Loudspeaker_SampleSize = 2;
	public static final int Loudspeaker_BufferSize = (Loudspeaker_SampleSize*Loudspeaker_SampleRate/1000)*Loudspeaker_SampleInterval;

	private static class TMyAACEncoder extends AACEncoder {

		private OutputStream MyOutputStream;
		
		public TMyAACEncoder(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
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
            //.
			MyOutputStream.flush();
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyAACEncoder1 extends AACEncoder {

		private OutputStream MyOutputStream;
		
		public TMyAACEncoder1(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
			MyOutputStream = pOutputStream;
		}

		private byte[] DataDescriptor = new byte[2];
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if ((BufferSize == 0) || (BufferSize >= 65535))
				return; //. ->
			//.
			DataDescriptor[0] = (byte)(BufferSize & 0xff);
			DataDescriptor[1] = (byte)(BufferSize >> 8 & 0xff);
			//.
			MyOutputStream.write(DataDescriptor);
			MyOutputStream.write(Buffer, 0,BufferSize);
            //.
			MyOutputStream.flush();
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyAACEncoder2 extends AACEncoder {

		private OutputStream MyOutputStream;
		
		public TMyAACEncoder2(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
			MyOutputStream = pOutputStream;
		}

		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if ((BufferSize == 0) || (BufferSize >= 65535))
				return; //. ->
			//.
			MyOutputStream.write(Buffer, 0,BufferSize);
            //.
			MyOutputStream.flush();
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	@SuppressWarnings("unused")
	private static class TMyAACEncoderUDP extends AACEncoder {

		private DatagramSocket OutputSocket;
		private String Address;
		private int Port;
		//.
		private byte[] PacketBuffer;
		private DatagramPacket Packet;
		
		public TMyAACEncoderUDP(int BitRate, int SampleRate, DatagramSocket pOutputSocket, String pAddress, int pPort) throws UnknownHostException {
			super(BitRate, SampleRate);
			OutputSocket = pOutputSocket;
			Address = pAddress;
			Port = pPort;
			//.
			PacketBuffer = new byte[1400];
			Packet = new DatagramPacket(PacketBuffer,1,InetAddress.getByName(Address),Port);
		}

		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if ((BufferSize == 0) || (BufferSize >= 1400))
				return; //. ->
			//.
			System.arraycopy(Buffer,0, PacketBuffer,0, BufferSize);
			Packet.setLength(BufferSize);
			//.
			OutputSocket.send(Packet);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyAACEncoderUDPRTP extends AACEncoder {

		private DatagramSocket OutputSocket;
		//.
		private String 	Address;
		private int 	Port;
		//.
		private int Timestamp = 0;
		//.
		private TRtpEncoder RtpEncoder;
		
		public TMyAACEncoderUDPRTP(int BitRate, int SampleRate, DatagramSocket pOutputSocket, String pAddress, int pPort) throws UnknownHostException {
			super(BitRate, SampleRate);
			OutputSocket = pOutputSocket;
			Address = pAddress;
			Port = pPort;
			//.
			RtpEncoder = new TRtpEncoder(Address,Port) {  
				
				private int PacketIndex = 0;
				
				@Override
				public void DoOnOutput(TRtpPacket OutputPacket) throws IOException {
					OutputPacket.sendTo(OutputSocket);
					///test Log.v("UDP packet", "-> TS: "+Long.toString(System.currentTimeMillis())+", sent: "+Integer.toString(OutputPacket.buffer_length));
					if (PacketIndex < 2) { //. re-send first configuration packets 
						for (int I = 0; I < 4; I++)
							OutputPacket.sendToAgain(OutputSocket);
					}
					//.
					PacketIndex++;
				}
			};
		}

		//. direct packet sending test
		/*/// @Override
		public void EncodeInputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
			DoOnOutputBuffer(input,input_size, Timestamp);
		}*/
		
		private void SendBuffer(byte[] Buffer, int BufferSize) throws IOException {
			if (BufferSize == 0)
				return; //. ->
			Timestamp++;
			RtpEncoder.DoOnInput(Buffer,BufferSize, Timestamp);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyAACEncoderProxyUDPRTP extends AACEncoder {

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
		
		public TMyAACEncoderProxyUDPRTP(int BitRate, int SampleRate, DatagramSocket pOutputSocket, String pProxyServerAddress, int pProxyServerPort, String pAddress, int pPort) throws UnknownHostException {
			super(BitRate, SampleRate);
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
					///test Log.v("UDP packet", "-> TS: "+Long.toString(System.currentTimeMillis())+", sent: "+Integer.toString(OutputPacket.buffer_length));
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
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	private static class TMyAACRTPEncoder extends AACEncoder {

		private static final int PACKET_TYPE_RTP 	= 0;
		private static final int PACKET_TYPE_RTCP 	= 1;
		
		private OutputStream MyOutputStream;
		
		private int		DataSize;
		private byte[] 	DataDescriptor = new byte[2];
		private byte[] 	DataType = new byte[1];
		//.
		private long 		timestamp;
		private RtpBuffer 	RtpContainer;
		private byte[]		buffer;
		private RtcpBuffer	SenderReport;
		private long		SenderReport_LastTime = 0;
		
		public TMyAACRTPEncoder(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
			MyOutputStream = pOutputStream;
			//.
			timestamp = 0;
			//.
			RtpContainer = new RtpBuffer();
			buffer = RtpContainer.getBuffer();
			//.
			SenderReport = new RtcpBuffer();
		}

		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
            timestamp += 1024; 
            RtpContainer.updateTimestamp(timestamp);
            //.
            long Now = SystemClock.elapsedRealtime();
            if ((Now - SenderReport_LastTime) >= 5000) {
            	SenderReport.setRtpTimestamp(timestamp);
            	SenderReport.setNtpTimestamp(SystemClock.elapsedRealtime());
            	//.
            	DataSize = 1+RtcpBuffer.Size;
    			DataDescriptor[0] = (byte)(DataSize & 0xff);
    			DataDescriptor[1] = (byte)(DataSize >> 8 & 0xff);
    			//.
    			MyOutputStream.write(DataDescriptor);
    			//.
    			DataType[0] = PACKET_TYPE_RTCP; //. RTCP packet
    			MyOutputStream.write(DataType);
            	SenderReport.SendTo(MyOutputStream);    
            	//.
            	SenderReport_LastTime = Now;
            }
            //.
            int sum = 0;
            int length;
            int idx = 0;
            while (sum < BufferSize) {
                // Read frame
                if (BufferSize-sum > RtpBuffer.MAXPACKETSIZE-RtpBuffer.RTP_HEADER_LENGTH-4) {
                    length = RtpBuffer.MAXPACKETSIZE-RtpBuffer.RTP_HEADER_LENGTH-4;
                }
                else {
                    length = BufferSize-sum;
                    RtpContainer.markNextPacket();
                }
                sum += length;
                //.
                System.arraycopy(Buffer,idx, buffer,RtpBuffer.RTP_HEADER_LENGTH+4, length); idx += length;
                // AU-headers-length field: contains the size in bits of a AU-header
                // 13+3 = 16 bits -> 13bits for AU-size and 3bits for AU-Index / AU-Index-delta 
                // 13 bits will be enough because ADTS uses 13 bits for frame length
                buffer[RtpBuffer.RTP_HEADER_LENGTH] = 0;
                buffer[RtpBuffer.RTP_HEADER_LENGTH+1] = 0x10; 
                // AU-size
                buffer[RtpBuffer.RTP_HEADER_LENGTH+2] = (byte)(BufferSize >> 5);
                buffer[RtpBuffer.RTP_HEADER_LENGTH+3] = (byte)(BufferSize << 3);
                // AU-Index
                buffer[RtpBuffer.RTP_HEADER_LENGTH+3] &= 0xF8;
    			//.
                DataSize = 1+RtpBuffer.RTP_HEADER_LENGTH+4+length;
    			DataDescriptor[0] = (byte)(DataSize & 0xff);
    			DataDescriptor[1] = (byte)(DataSize >> 8 & 0xff);
    			//.
    			MyOutputStream.write(DataDescriptor);
    			//.
    			DataType[0] = PACKET_TYPE_RTP; //. RTP packet
    			MyOutputStream.write(DataType);
                RtpContainer.SendTo(MyOutputStream,RtpBuffer.RTP_HEADER_LENGTH+4+length);
                //.
    			MyOutputStream.flush();
            }
		}
	}
	
	public static class TAudioFileDescriptor {
		public int		ID;
		public String	Name;
	}

	public TUserAccessKey UserAccessKey;
	//.
	public TComponentTimestampedInt16ArrayValue	SourcesSensitivitiesValue;
	public TComponentTimestampedInt16ArrayValue	DestinationsVolumesValue;
	//.
	private AudioTrack Loudspeaker_Player;
	//.
	private AudioRecord Microphone_Recorder; 
	private static int 	Microphone_SamplePerSec = 8000;
	private static int 	Microphone_BufferSize;
	//.
	public TAudioFilesValue	AudioFilesValue;
	//.
	public TAudioFileMessageValue	AudioFileMessageValue;
	private int						AudioFileMessagePlayerDestinationIndex = -1;
	private MediaPlayer				AudioFileMessagePlayer;
	
    public TAudioModule(TDEVICEModule pDevice) {
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
    	SourcesSensitivitiesValue	= new TComponentTimestampedInt16ArrayValue(SourcesCount);
    	for (int I = 0; I < SourcesCount; I++)
    		SourcesSensitivitiesValue.Value[I] = 100; //. default 100%
    	DestinationsVolumesValue 	= new TComponentTimestampedInt16ArrayValue(DestinationsCount);
    	for (int I = 0; I < DestinationsCount; I++)
    		DestinationsVolumesValue.Value[I] = 100; //. default 100%
    	//.
    	AudioFilesValue 		= new TAudioFilesValue(this);
    	AudioFileMessageValue 	= new TAudioFileMessageValue(this);
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		AudioFileMessagePlayer = new MediaPlayer();
		AudioFileMessagePlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				arg0.stop();
				arg0.reset();
			}
		});
    }
    
    public void Destroy() {
    	if (AudioFileMessagePlayer != null) {
    		AudioFileMessagePlayer.release();
    		AudioFileMessagePlayer = null;
    		AudioFileMessagePlayerDestinationIndex = -1;    	
    	}
    }
    
    public void AudioSampleServer_Connect() {
    	
    }
    
    public void AudioSampleServer_Disconnect() {
    	
    }
    
    public void AudioSampleServer_Capturing(InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
    	int InitializationCode = AudioSampleServer_Initialization_Code_Ok;
    	byte[] DataDescriptor = new byte[4];
        int Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
  		if (Size != DataDescriptor.length)
  			throw new IOException("wrong service data"); //. =>
		int Service = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
		//.
		int SampleRate = 8000;
		@SuppressWarnings("unused")
		int SamplePacketSize = 0;
		@SuppressWarnings("unused")
		int Quality = 100;
		switch (Service) {
		
		case AudioSampleServer_Service_SamplePackets: 
		case AudioSampleServer_Service_SampleZippedPackets: 
		case AudioSampleServer_Service_AACPackets: 
		case AudioSampleServer_Service_AACPackets1: 
		case AudioSampleServer_Service_AACPackets2: 
		case AudioSampleServer_Service_AACRTPPackets: 
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong sample rate data"); //. =>
			int _SampleRate = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 < _SampleRate) && (_SampleRate <= 100000))
				SampleRate = _SampleRate;
			//.
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong sample packet size"); //. =>
			int _SamplePacketSize = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 < _SamplePacketSize) && (_SamplePacketSize <= 10000))
				SamplePacketSize = _SamplePacketSize;
			//.
	        Size = DestinationConnectionInputStream.read(DataDescriptor,0,DataDescriptor.length);
			if (Size != DataDescriptor.length)
				throw new IOException("wrong sample quality data"); //. =>
			int _Quality = (DataDescriptor[3] << 24)+((DataDescriptor[2] & 0xFF) << 16)+((DataDescriptor[1] & 0xFF) << 8)+(DataDescriptor[0] & 0xFF);
			if ((0 <= _Quality) && (_Quality <= 100))
				Quality = _Quality;
			//.
			if (Device.VideoRecorderModule.Transmitting.BooleanValue()) {
				switch (Service) {
				
				case AudioSampleServer_Service_AACPackets:
				case AudioSampleServer_Service_AACPackets1:
				case AudioSampleServer_Service_AACPackets2:
					if (!TMyAACEncoder.IsSupported())
						InitializationCode = AudioSampleServer_Initialization_Code_ServiceIsNotActiveError;
					break; //. >
					
				case AudioSampleServer_Service_AACRTPPackets:
					if (!TMyAACRTPEncoder.IsSupported())
						InitializationCode = AudioSampleServer_Initialization_Code_ServiceIsNotActiveError;
					break; //. >
				}
			}
			else 
				InitializationCode = AudioSampleServer_Initialization_Code_ServiceAccessIsDisabledError;
			//.
			break; //. >
			
		default:
			InitializationCode = AudioSampleServer_Initialization_Code_UnknownServiceError;			
		}
		//. if ((InitializationCode >= 0) && (!MediaFrameServer.flAudioActive)) 
		//. 	InitializationCode = AudioSampleServer_Initialization_Code_ServiceIsNotActiveError;		
		//.
		DataDescriptor[0] = (byte)(InitializationCode & 0xff);
		DataDescriptor[1] = (byte)(InitializationCode >> 8 & 0xff);
		DataDescriptor[2] = (byte)(InitializationCode >> 16 & 0xff);
		DataDescriptor[3] = (byte)(InitializationCode >>> 24);
		DestinationConnectionOutputStream.write(DataDescriptor);		
		//.
		if (InitializationCode < 0)
			return; //. ->
		//. send sample rate
		SampleRate = MediaFrameServer.SampleRate;
		DataDescriptor[0] = (byte)(SampleRate & 0xff);
		DataDescriptor[1] = (byte)(SampleRate >> 8 & 0xff);
		DataDescriptor[2] = (byte)(SampleRate >> 16 & 0xff);
		DataDescriptor[3] = (byte)(SampleRate >>> 24);
		DestinationConnectionOutputStream.write(DataDescriptor);
		//.
		//. capturing
        byte[] 	SamplePacketBuffer = new byte[0];
        int 	SamplePacketBufferSize = 0;
        long 	SamplePacketTimestamp = 0;
        byte[] 	SamplePacketTimestampBA = new byte[8];
		boolean flProcessSamplePacket;
		switch (Service) {
		
		case AudioSampleServer_Service_SamplePackets:
	        try {
				while (!Canceller.flCancel) {
					if (MediaFrameServer.flAudioActive) {
						synchronized (MediaFrameServer.CurrentSamplePacket) {
							MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
							//.
							if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
								SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
								SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
								if (SamplePacketBuffer.length != SamplePacketBufferSize)
									SamplePacketBuffer = new byte[SamplePacketBufferSize];
								System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
								//.
								flProcessSamplePacket = true;
							}
							else flProcessSamplePacket = false;
						}
						if (flProcessSamplePacket) {
							TDataConverter.ConvertDoubleToBEByteArray(SamplePacketTimestamp,SamplePacketTimestampBA);
							//.
							int Sz = 8/*SizeOf(SamplePacketTimestamp)*/+SamplePacketBufferSize;
							DataDescriptor[0] = (byte)(Sz & 0xff);
							DataDescriptor[1] = (byte)(Sz >> 8 & 0xff);
							DataDescriptor[2] = (byte)(Sz >> 16 & 0xff);
							DataDescriptor[3] = (byte)(Sz >>> 24);
							//.
							DestinationConnectionOutputStream.write(DataDescriptor);
							DestinationConnectionOutputStream.write(SamplePacketTimestampBA);
							DestinationConnectionOutputStream.write(SamplePacketBuffer,0,SamplePacketBufferSize);
						}
					}
					else
						Thread.sleep(100);
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
			break; //. >
			
		case AudioSampleServer_Service_SampleZippedPackets:
	        ByteArrayOutputStream PacketZippingStream = new ByteArrayOutputStream();
	        try {
		        try {
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) {
								TDataConverter.ConvertDoubleToBEByteArray(SamplePacketTimestamp,SamplePacketTimestampBA);
								//.
								PacketZippingStream.reset();
								//.
					            ZOutputStream out = new ZOutputStream(PacketZippingStream,JZlib.Z_BEST_SPEED);
					            try
					            {
					            	out.write(SamplePacketTimestampBA);
					                out.write(SamplePacketBuffer,0,SamplePacketBufferSize);
					            }
					            finally
					            {
					                out.close();
					            }
					            //.
					            Size = PacketZippingStream.size();
					            //.
								DataDescriptor[0] = (byte)(Size & 0xff);
								DataDescriptor[1] = (byte)(Size >> 8 & 0xff);
								DataDescriptor[2] = (byte)(Size >> 16 & 0xff);
								DataDescriptor[3] = (byte)(Size >>> 24);
								//.
								DestinationConnectionOutputStream.write(DataDescriptor);
								PacketZippingStream.writeTo(DestinationConnectionOutputStream);
							}
						}
						else
							Thread.sleep(100);
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
	        	PacketZippingStream.close();
	        }
	        break; //. >

		case AudioSampleServer_Service_AACPackets:
	        TMyAACEncoder MyAACEncoder = new TMyAACEncoder(MediaFrameServer.SampleBitRate, SampleRate, DestinationConnectionOutputStream);
	        try {
		        try {
		        	long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) {
				            	MyAACEncoder.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize,SystemClock.elapsedRealtime()-TimestampBase);
							}
						}
						else
							Thread.sleep(100);
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
	        	MyAACEncoder.Destroy();
	        }
	        break; //. >

		case AudioSampleServer_Service_AACPackets1:
	        TMyAACEncoder1 MyAACEncoder1 = new TMyAACEncoder1(MediaFrameServer.SampleBitRate, SampleRate, DestinationConnectionOutputStream);
	        try {
		        try {
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								///? MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) 
				            	MyAACEncoder1.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize,SamplePacketTimestamp);
							else
								Thread.sleep(0);
						}
						else
							Thread.sleep(100);
			        }
			        //. send disconnect message (Descriptor = 0)
					DataDescriptor[0] = 0;
					DataDescriptor[1] = 0;
					DestinationConnectionOutputStream.write(DataDescriptor,0,2);
		        }
				catch (InterruptedException IE) {
				}
	        }
	        finally {
	        	MyAACEncoder1.Destroy();
	        }
	        break; //. >

		case AudioSampleServer_Service_AACPackets2:
	        TMyAACEncoder2 MyAACEncoder2 = new TMyAACEncoder2(MediaFrameServer.SampleBitRate, SampleRate, DestinationConnectionOutputStream);
	        try {
		        try {
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								///? MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) 
				            	MyAACEncoder2.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize,SamplePacketTimestamp);
							else
								Thread.sleep(0);
						}
						else
							Thread.sleep(100);
			        }
		        }
				catch (InterruptedException IE) {
				}
	        }
	        finally {
	        	MyAACEncoder2.Destroy();
	        }
	        break; //. >

		case AudioSampleServer_Service_AACRTPPackets:
	        TMyAACRTPEncoder MyAACRTPEncoder = new TMyAACRTPEncoder(MediaFrameServer.SampleBitRate, SampleRate, DestinationConnectionOutputStream);
	        try {
		        try {
		        	long TimestampBase = SystemClock.elapsedRealtime();
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) {
								MyAACRTPEncoder.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize,SystemClock.elapsedRealtime()-TimestampBase);
							}
						}
						else
							Thread.sleep(100);
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
	        	MyAACRTPEncoder.Destroy();
	        }
	        break; //. >
		}
    }
    
    public void AudioSampleServer_Capturing(String Configuration, DatagramSocket IOSocket, String OutputAddress, int OutputPort, int OutputProxyType, String ProxyServerAddress, int ProxyServerPort, TCanceller Canceller) throws IOException {
		//. capturing
        @SuppressWarnings("unused")
		byte[] 	SamplePacketBuffer = new byte[0];
        @SuppressWarnings("unused")
		int 	SamplePacketBufferSize = 0;
        @SuppressWarnings("unused")
		long 	SamplePacketTimestamp = 0;
        @SuppressWarnings("unused")
		byte[] 	SamplePacketTimestampBA = new byte[8];
		@SuppressWarnings("unused")
		boolean flProcessSamplePacket;
		//.
		int BitRate = 8000;
		if (MediaFrameServer.SampleBitRate > 0)
			BitRate = MediaFrameServer.SampleBitRate; 
		int SampleRate = 8000;
		//.
		final AACEncoder Encoder; 
		switch (OutputProxyType) {
		
		case TUDPEchoServerClient.PROXY_TYPE_NATIVE:
			Encoder = new TMyAACEncoderProxyUDPRTP(BitRate, SampleRate, IOSocket, ProxyServerAddress,ProxyServerPort, OutputAddress,OutputPort);
			break; //. >
			
		default:
			Encoder = new TMyAACEncoderUDPRTP(BitRate, SampleRate, IOSocket,OutputAddress,OutputPort);
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
	        	MediaFrameServer.CurrentSamplePacketSubscribers.Subscribe(PacketSubscriber);
	        	try {
					while (!Canceller.flCancel) {
						Thread.sleep(1000);
						/*//. last version if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								///? MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > SamplePacketTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) 
				            	Encoder.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize,SamplePacketTimestamp);
							else
								Thread.sleep(0);
						}
						else
							Thread.sleep(100);*/
			        }
	        	}
	        	finally {
		        	MediaFrameServer.CurrentSamplePacketSubscribers.Unsubscribe(PacketSubscriber);
	        	}
	        }
			catch (InterruptedException IE) {
			}
        }
        finally {
        	Encoder.Destroy();
        }
    }
    
    @SuppressWarnings("deprecation")
	public void Loudspeaker_Initialize() throws IOException {
		if (!IsEnabled())
			throw new IOException("audio module is disabled"); //. =>
    	short _Volume;
    	synchronized (DestinationsVolumesValue) {
    		_Volume = DestinationsVolumesValue.GetValue()[Loudspeaker_DestinationID-1];
		}
    	Loudspeaker_Player = new AudioTrack(AudioManager.STREAM_MUSIC, Loudspeaker_SampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, Loudspeaker_BufferSize, AudioTrack.MODE_STREAM);
    	Loudspeaker_SetVolume(_Volume);
    	Loudspeaker_Player.play();    	
    }
    
    public void Loudspeaker_Finalize() {
    	if (Loudspeaker_Player != null) {
    		Loudspeaker_Player.stop();
    		Loudspeaker_Player = null;
    	}
    }
    
	public void Loudspeaker_SetVolume(short Volume) {
    	Loudspeaker_Player.setStereoVolume(0.0F, Volume/100.0F);
	}
	
    protected static int InputStream_Read(InputStream Connection, byte[] Data, int DataSize) throws IOException {
        int SummarySize = 0;
        int ReadSize;
        int Size;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = Connection.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	return Size; //. ->
            SummarySize += Size;
        }
        return SummarySize;
    }
	
    public void Loudspeaker_Playing(InputStream DestinationConnectionInputStream, TCanceller Canceller) throws IOException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //.
    	short _Volume;
    	synchronized (DestinationsVolumesValue) {
    		_Volume = DestinationsVolumesValue.GetValue()[Loudspeaker_DestinationID-1];
		}
    	short _NewVolume;
    	//.
		byte[] TransferBuffer = new byte[4];
		@SuppressWarnings("unused")
		byte[] Buffer = new byte[8192];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = DestinationConnectionInputStream.read(TransferBuffer,0,4/*SizeOf(Descriptor)*/);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size != 4/*SizeOf(Descriptor)*/)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Size = InputStream_Read(DestinationConnectionInputStream,TransferBuffer,Size);	
                if (Size <= 0) 
                	break; //. >
				Loudspeaker_Player.write(TransferBuffer, 0,Size);
			}
	    	synchronized (DestinationsVolumesValue) {
	    		_NewVolume = (short)DestinationsVolumesValue.GetValue()[Loudspeaker_DestinationID-1];
			}
	    	if (_NewVolume != _Volume) { 
	    		Loudspeaker_SetVolume(_NewVolume);
	    		_Volume = _NewVolume;
	    	}
			//. decompressing
			/*///??? if (Size > 0) {
				ByteArrayInputStream BIS = new ByteArrayInputStream(TransferBuffer,0,Size);
				try {
					ZInputStream ZIS = new ZInputStream(BIS);
					try {
						int ReadSize;
						ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
						try {
							while ((ReadSize = ZIS.read(Buffer)) > 0) 
								BOS.write(Buffer, 0,ReadSize);
							//.
							byte[] Packet = BOS.toByteArray();
							Loudspeaker_Player.write(Packet, 0,Packet.length);
						}
						finally {
							BOS.close();
						}
					}
					finally {
						ZIS.close();
					}
				}
				finally {
					BIS.close();
				}
				
			}*/
		}    	
    }
    
    public void Microphone_Initialize() throws IOException {
		if (!IsEnabled())
			throw new IOException("audio module is disabled"); //. =>
		Microphone_BufferSize = AudioRecord.getMinBufferSize(Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (Microphone_BufferSize != AudioRecord.ERROR_BAD_VALUE && Microphone_BufferSize != AudioRecord.ERROR) {
            Microphone_Recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Microphone_BufferSize*10);
            if (Microphone_Recorder != null && Microphone_Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
            	Microphone_Recorder.startRecording();
            else 
            	throw new IOException("unable to initialize audio-recorder"); //. =>

        } else 
        	throw new IOException("AudioRecord.getMinBufferSize() error"); //. =>
    }
    
    public void Microphone_Finalize() {
        if (Microphone_Recorder != null) {
            if (Microphone_Recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
            	Microphone_Recorder.stop();
            if (Microphone_Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
            	Microphone_Recorder.release();
        }
    }
    
    public void Microphone_Recording(OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
        byte[] TransferBuffer = new byte[Microphone_BufferSize];
		byte[] DataDescriptor = new byte[4];
        int Size;
		while (!Canceller.flCancel) {
            Size = Microphone_Recorder.read(TransferBuffer, 0,TransferBuffer.length);     
			if (Size > 0) {
				DataDescriptor[0] = (byte)(Size & 0xff);
				DataDescriptor[1] = (byte)(Size >> 8 & 0xff);
				DataDescriptor[2] = (byte)(Size >> 16 & 0xff);
				DataDescriptor[3] = (byte)(Size >>> 24);
				//.
				DestinationConnectionOutputStream.write(DataDescriptor,0,DataDescriptor.length);
				DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
			}
        }
    }
    
	public synchronized void AudioFiles_CheckFolder() {
		File F = new File(AudioFileFolder);
		if (!F.exists()) 
			F.mkdirs();
	}

    public static ArrayList<TAudioFileDescriptor> AudioFiles_GetDescriptors() {
		File AF = new File(AudioFileFolder);
		if (!AF.exists())
			return null; //. ->
		ArrayList<TAudioFileDescriptor> Result = new ArrayList<TAudioModule.TAudioFileDescriptor>();
		File[] AFiles = AF.listFiles();
		Arrays.sort(AFiles, new Comparator<File>(){
		    public int compare(File f1, File f2) {
		    	return f1.getName().compareToIgnoreCase(f2.getName());
		    }}
		);		
    	for (int I = 0; I < AFiles.length; I++) {
    		if (AFiles[I].isFile()) {
    			int AFID;
    			try {
    				String AFN = AFiles[I].getName();
        			AFID = Integer.parseInt(AFN.substring(0,AFN.lastIndexOf('.')));
        			TAudioFileDescriptor AFD = new TAudioFileDescriptor();
        			AFD.ID = AFID;
        			AFD.Name = AFiles[I].getName();
        			Result.add(AFD);
    			}
    			catch (NumberFormatException E){
    			}
    		}
    	}
    	return Result;
    } 
    
	public synchronized void AudioFiles_FromByteArray(byte[] BA) throws IOException {
		AudioFiles_CheckFolder();
		//.
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
		try {
			ZipInputStream ZipStream = new ZipInputStream(BIS);		
			try {
				ZipEntry theEntry;
				while ((theEntry = ZipStream.getNextEntry()) != null) {
					String fileName = theEntry.getName();
					if (!fileName.equals("")) {
						File F = new File(fileName);
						FileOutputStream out = new FileOutputStream(TAudioModule.AudioFileFolder+"/"+F.getName());
						try {
							int size = 2048;
							byte[] _data = new byte[size];
							while (true) {
								size = ZipStream.read(_data, 0,_data.length);
								if (size > 0) 
									out.write(_data, 0,size);
								else 
									break; //. >
							}
						}
						finally {
							out.close();
						}
					}
				}
			}
			finally {
				ZipStream.close();
			}
		}
		finally {
			BIS.close();
		}
	}
	
	public synchronized byte[] AudioFiles_ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
		    ZipOutputStream ZOS = new ZipOutputStream(BOS);
		    try {
				File F = new File(AudioFileFolder);
		    	//.
			    File[] files = F.listFiles();
			    byte[] buffer = new byte[8192];
			    int read = 0;
			    for (int i = 0, n = files.length; i < n; i++) 
				      if (files[i].isFile()) {
					        FileInputStream in = new FileInputStream(files[i]);
					        try {
						        ZipEntry entry = new ZipEntry(files[i].getName());
						        ZOS.putNextEntry(entry);
						        while ((read = in.read(buffer)) != -1) 
						        	ZOS.write(buffer, 0, read);
					        }
					        finally {
						        in.close();
					        }
				      }
		    }
		    finally {
			      ZOS.close();
		    }
		    return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
	
    public static boolean AudioFiles_CheckFile(int ID) {
    	File AF = new File(AudioFileFolder+"/"+Integer.toString(ID)+AudioFileType);
    	if (AF.exists())
    		return true; //. ->
    	AF = new File(AudioFileFolder+"/"+Integer.toString(ID)+AudioFileType1);
    	return (AF.exists());
    }
    
	public void AudioFiles_SetPlayingVolume(short Volume) throws IOException {
    	float LV = 0.0F;
    	float RV = 0.0F;
    	switch (AudioFileMessagePlayerDestinationIndex) {
    	
    	case 0: //. left channel
    		LV = Volume/100.0F;
    		RV = 0.0F;
    		break; //. >
    		
    	case 1: //. right channel
    		LV = 0.0F;
    		RV = Volume/100.0F;
    		break; //. >
    		
    	default:
    		throw new IOException("unknown audio DestinationID"); //. =>
    	}
    	AudioFileMessagePlayer.setVolume(LV,RV);
	}
	
    public boolean AudioFiles_Play(int FileID, String FileName, short DestinationID, short Volume, boolean flWaitForFinish) throws IOException {
		AudioFileMessagePlayerDestinationIndex = DestinationID-1;
    	short _Volume;
    	if (Volume > 0)
    		_Volume = Volume;
    	else
        	synchronized (DestinationsVolumesValue) {
        		_Volume = DestinationsVolumesValue.GetValue()[AudioFileMessagePlayerDestinationIndex];
    		}
    	//.
    	String AFN;
    	File AF;
    	if (FileName == null) {
        	AFN = AudioFileFolder+"/"+Integer.toString(FileID)+AudioFileType;
        	AF = new File(AFN);
        	if (!AF.exists()) {
            	AFN = AudioFileFolder+"/"+Integer.toString(FileID)+AudioFileType1;
            	AF = new File(AFN);
        	}
    	}
    	else { 
    		if (FileName.startsWith("/"))
    			AFN = FileName;
    		else
    			AFN = AudioFileFolder+"/"+FileName;
        	AF = new File(AFN);
    	}
    	if (!AF.exists())
    		throw new IOException("no audio file found"); //. =>
    	//.
    	if (AudioFileMessagePlayer.isPlaying()) {
    		AudioFileMessagePlayer.stop();
    		AudioFileMessagePlayer.reset();
    	}
    	AudioFileMessagePlayer.setDataSource(AFN);
    	AudioFiles_SetPlayingVolume(_Volume);
    	AudioFileMessagePlayer.prepare();
    	AudioFileMessagePlayer.start();
    	//.
    	try {
    		if (flWaitForFinish)
    			AudioFiles_WaitForPlayerIsReady();
    	}
    	finally {
    		AudioFileMessagePlayerDestinationIndex = -1;
    	}
    	return true;
    }
    
    public void AudioFiles_WaitForPlayerIsReady() throws IOException {
    	short _Volume;
    	synchronized (DestinationsVolumesValue) {
    		_Volume = DestinationsVolumesValue.GetValue()[AudioFileMessagePlayerDestinationIndex];
		}
    	short _NewVolume;
    	while (AudioFileMessagePlayer.isPlaying())
			try {
				Thread.sleep(20);
				//.
		    	synchronized (DestinationsVolumesValue) {
		    		_NewVolume = (short)DestinationsVolumesValue.GetValue()[AudioFileMessagePlayerDestinationIndex];
				}
		    	if (_NewVolume != _Volume) { 
		    		AudioFiles_SetPlayingVolume(_NewVolume);
		    		_Volume = _NewVolume;
		    	}
			} catch (InterruptedException e) {
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
		//. todo: Node AudioRecorderModuleNode = RootNode.getElementsByTagName("AudioModule").item(0);
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
		//. overriding configuration by local
		SourcesSensitivitiesValue.FromFile(SourcesSensitivitiesConfigurationFile);
		DestinationsVolumesValue.FromFile(DestinationsVolumesConfigurationFile);
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
	    SaveConfigurationLocally();
		//.
		int Version = 1;
        Serializer.startTag("", "AudioModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.endTag("", "AudioModule");
    }
    
    public void SaveConfigurationLocally() throws IOException {
		//. write local configuration
    	SourcesSensitivitiesValue.ToFile(SourcesSensitivitiesConfigurationFile);
    	DestinationsVolumesValue.ToFile(DestinationsVolumesConfigurationFile);
	}    
}
