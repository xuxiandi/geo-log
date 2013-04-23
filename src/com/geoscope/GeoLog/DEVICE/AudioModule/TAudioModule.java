/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.widget.Toast;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16ArrayValue;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AACEncoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.MediaFrameServer;
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
	public static final int AudioSampleServer_Service_SamplePackets 		= 1;
	public static final int AudioSampleServer_Service_SampleZippedPackets 	= 2;
	public static final int AudioSampleServer_Service_AACPackets 			= 3;
	//.
	public static final int AudioSampleServer_Initialization_Code_Ok 						= 0;
	public static final int AudioSampleServer_Initialization_Code_Error 					= -1;
	public static final int AudioSampleServer_Initialization_Code_UnknownServiceError 		= -2;
	public static final int AudioSampleServer_Initialization_Code_ServiceIsNotActiveError 	= -3;
	//.
	public static final int Loudspeaker_DestinationID = 2;
	public static final int Loudspeaker_SampleRate = 8000;
	public static final int Loudspeaker_SampleInterval = 20;
	public static final int Loudspeaker_SampleSize = 2;
	public static final int Loudspeaker_BufferSize = Loudspeaker_SampleInterval*Loudspeaker_SampleInterval*Loudspeaker_SampleSize*2;

	private static class TMyAACEncoder extends AACEncoder {

		public TMyAACEncoder(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate, pOutputStream);
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
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize) throws IOException {
			SendBuffer(Buffer,BufferSize);
		}
	}
	
	public TComponentTimestampedInt16ArrayValue	SourcesSensitivitiesValue;
	public TComponentTimestampedInt16ArrayValue	DestinationsVolumesValue;
	//.
	private AudioTrack Loudspeaker_Player;
	//.
	private AudioRecord Microphone_Recorder; 
	private static int 	Microphone_SamplePerSec = 8000;
	
    public TAudioModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder);
		if (!F.exists()) 
			F.mkdirs();
        //.
    	SourcesSensitivitiesValue	= new TComponentTimestampedInt16ArrayValue(SourcesCount);
    	DestinationsVolumesValue 	= new TComponentTimestampedInt16ArrayValue(DestinationsCount);
        //.
    	try {
			LoadConfiguration();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() {
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
			break; //. >
			
		default:
			InitializationCode = AudioSampleServer_Initialization_Code_UnknownServiceError;			
		}
		if (!MediaFrameServer.flAudioActive) 
			InitializationCode = AudioSampleServer_Initialization_Code_ServiceIsNotActiveError;
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
		//. capturing
        double 	LastTimestamp = 0.0;
        byte[] 	SamplePacketBuffer = new byte[0];
        int 	SamplePacketBufferSize = 0;
        double 	SamplePacketTimestamp = 0.0;
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
							if (MediaFrameServer.CurrentSamplePacket.Timestamp > LastTimestamp) {
								SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
								SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
								if (SamplePacketBuffer.length != SamplePacketBufferSize)
									SamplePacketBuffer = new byte[SamplePacketBufferSize];
								System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
								//.
								LastTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp; 
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
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > LastTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									LastTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp; 
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
					while (!Canceller.flCancel) {
						if (MediaFrameServer.flAudioActive) {
							synchronized (MediaFrameServer.CurrentSamplePacket) {
								MediaFrameServer.CurrentSamplePacket.wait(MediaFrameServer.SamplePacketInterval);
								//.
								if (MediaFrameServer.CurrentSamplePacket.Timestamp > LastTimestamp) {
									SamplePacketTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp;
									SamplePacketBufferSize = MediaFrameServer.CurrentSamplePacket.DataSize;
									if (SamplePacketBuffer.length != SamplePacketBufferSize)
										SamplePacketBuffer = new byte[SamplePacketBufferSize];
									System.arraycopy(MediaFrameServer.CurrentSamplePacket.Data,0, SamplePacketBuffer,0, SamplePacketBufferSize);
									//.
									LastTimestamp = MediaFrameServer.CurrentSamplePacket.Timestamp; 
									//.
									flProcessSamplePacket = true;
								}
								else flProcessSamplePacket = false;
							}
							if (flProcessSamplePacket) {
				            	MyAACEncoder.EncodeInputBuffer(SamplePacketBuffer,SamplePacketBufferSize);
							}
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
	        	MyAACEncoder.Destroy();
	        }
	        break; //. >
		}
    }
    
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
		byte[] TransferBuffer = new byte[Loudspeaker_BufferSize];
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
        int BufferSize = AudioRecord.getMinBufferSize(Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (BufferSize != AudioRecord.ERROR_BAD_VALUE && BufferSize != AudioRecord.ERROR) {
            Microphone_Recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BufferSize*10); // bufferSize 10x
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
        byte[] TransferBuffer = new byte[2*Microphone_SamplePerSec/2];
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
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
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
