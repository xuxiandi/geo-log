package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.AudioModule.TMicrophoneCapturingServer;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC.TAACEncoder;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264Encoder;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264.TH264EncoderServer;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.PacketTimeBase;

@SuppressLint("NewApi")
public class CameraStreamerFRAME extends Camera {
	
	public static final int AUDIO_SAMPLE_FILE_FORMAT_PCMPACKETS 		= 1;
	public static final int AUDIO_SAMPLE_FILE_FORMAT_ZIPPEDPCMPACKETS 	= 2;
	public static final int AUDIO_SAMPLE_FILE_FORMAT_ADTSAACPACKETS 	= 3;
	//.
	public static final int VIDEO_FRAME_FILE_FORMAT_JPEGPACKETS 		= 1;
	public static final int VIDEO_FRAME_FILE_FORMAT_ZIPPEDJPEGPACKETS 	= 2;
	public static final int VIDEO_FRAME_FILE_FORMAT_H264PACKETS 		= 3;
	
	public static boolean 			flSaveAudioCodecConfig = false;
	public static boolean 			flSaveVideoCodecConfig = false;
	
	private static void SaveCodecConfig(String FN, byte[] ConfigData, int ConfigDataSize) throws IOException {
		FileOutputStream FOS = new FileOutputStream(FN);
		try {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.NO_WRAP);
				try {
					B64S.write(ConfigData, 0,ConfigDataSize);
				}
				finally {
					B64S.close();
				}
				FOS.write(BOS.toByteArray());
			}
			finally {
				BOS.close();
			}
		}
		finally {
			FOS.close();
		}
	}
	
	public class TAudioSampleSource extends TCancelableThread {
		
		//.
		private AudioRecord Microphone_Recorder = null; 
		public int 			Microphone_Source = MediaRecorder.AudioSource.DEFAULT;
		public int 			Microphone_SamplePerSec = 8000;
        private int 		Microphone_BufferSize;

		public TAudioSampleSource() {
    		super();
		}
		
		public void Release() throws IOException, InterruptedException {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
		}
		
		public void SetSource(int pSource) {
			Microphone_Source = pSource;
		}
		
		public void SetSampleRate(int SPS) {
			Microphone_SamplePerSec = SPS;
		}
		
		public void Start() {
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Stop() throws InterruptedException {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
		}
		
	    private void Microphone_Initialize() throws IOException {
	    	Microphone_BufferSize = AudioRecord.getMinBufferSize(Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	        if (Microphone_BufferSize != AudioRecord.ERROR_BAD_VALUE && Microphone_BufferSize != AudioRecord.ERROR) {
	            Microphone_Recorder = new AudioRecord(Microphone_Source, Microphone_SamplePerSec, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Microphone_BufferSize*10);
	            if (Microphone_Recorder != null && Microphone_Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
	            	Microphone_Recorder.startRecording();
	            else 
	            	throw new IOException("unable to initialize audio-recorder"); //. =>

	        } else 
	        	throw new IOException("AudioRecord.getMinBufferSize() error"); //. =>
	    }
	    
	    private void Microphone_Finalize() {
	        if (Microphone_Recorder != null) {
	            if (Microphone_Recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
	            	Microphone_Recorder.stop();
	            if (Microphone_Recorder.getState() == AudioRecord.STATE_INITIALIZED) 
	            	Microphone_Recorder.release();
	            Microphone_Recorder = null;
	        }
	    }
	    
		@Override
		public void run() {
			try {
				//. try to connect to an AudioModule.MicrophoneCapturingServer
				TMicrophoneCapturingServer.TConfiguration Configuration = new TMicrophoneCapturingServer.TConfiguration(Microphone_SamplePerSec);
				TMicrophoneCapturingServer.TPacketSubscriber PacketSubscriber = new TMicrophoneCapturingServer.TPacketSubscriber() {
					@Override
					protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
						DoOnAudioPacket(Packet,PacketSize);
					}
				};  
				if (
						(VideoRecorderModule.Device.AudioModule.MicrophoneCapturingServer != null) &&
						(VideoRecorderModule.Device.AudioModule.MicrophoneCapturingServer.Connect(Configuration, PacketSubscriber)) 
				) {
					try {
						while (!Canceller.flCancel) 
							Thread.sleep(1000*60);
					}
					finally {
						VideoRecorderModule.Device.AudioModule.MicrophoneCapturingServer.Disconnect(PacketSubscriber);
					}
				}
				else {
	    			VideoRecorderModule.Device.Log.WriteWarning("CameraFRAMEStreamer","unable to connect to the MicrophoneCapturingServer (the configuration is differ with a current one), using default method");
	    			//.
					Microphone_Initialize();
					try {
				        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
				        byte[] TransferBuffer = new byte[Microphone_BufferSize];
				        int Size;
						while (!Canceller.flCancel) {
				            Size = Microphone_Recorder.read(TransferBuffer, 0,TransferBuffer.length);     
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
			long Timestamp = System.nanoTime()/1000;
			//.
	        VideoRecorderModule.MediaFrameServer.CurrentSamplePacket.Set(Packet,PacketSize, Timestamp);
	        //.
	        try {
	        	VideoRecorderModule.MediaFrameServer.CurrentSamplePacketSubscribers.DoOnPacket(Packet,PacketSize, Timestamp);
			} 
			catch (IOException IOE) {
			}
			catch (Exception E) {
			}
			//. saving the AAC sample packet
			if (AudioSampleFileStream != null) 
				try {
					AudioSampleEncoder.EncodeInputBuffer(Packet,PacketSize,((System.nanoTime()/1000)-PacketTimeBase.TimeBase)/1000);
			        //.
					camera_parameters_Audio_SampleCount++;
				} 
				catch (IOException IOE) {
				}
				catch (Exception E) {
				}
		}
	}
	
	private static class TAudioSampleEncoder extends TAACEncoder {

		protected CameraStreamerFRAME Streamer;
		//.
		private OutputStream MyOutputStream;
		//.
		private boolean flConfigIsArrived = false;
		private byte ObjectType;
		private byte FrequencyIndex;
		private byte ChannelConfiguration;
		private byte[] ADTSHeader = new byte[7];
		public int Packets = 0;
		
		public TAudioSampleEncoder(CameraStreamerFRAME pStreamer, int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
			Streamer = pStreamer;
			MyOutputStream = pOutputStream;
		}

		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			if (!flConfigIsArrived) {
				flConfigIsArrived = true;
				//. 
				if (BufferSize < 2) 
					throw new IOException("invalid AAC codec configuration data"); //. =>
		        ObjectType = (byte)(Buffer[0] >> 3);
	        	FrequencyIndex = (byte)(((Buffer[0] & 7) << 1) | ((Buffer[1] >> 7) & 0x01));
	        	ChannelConfiguration = (byte)((Buffer[1] >> 3) & 0x0F);
				//.
				if (flSaveAudioCodecConfig) {
					flSaveAudioCodecConfig = false;
					//.
			        if (Streamer.MeasurementID != null) 
			        	try {
			        		String FN = Streamer.MeasurementFolder+"/"+TMeasurementDescriptor.AudioAACADTSFileName+"."+"Config"+"."+"txt";
			        		SaveCodecConfig(FN, Buffer,BufferSize);
						} catch (Exception E) {
						}
				}
				return; //. ->
			}
			//.
			int AudioBufferSize = ADTSHeader.length+BufferSize;
			ADTSHeader[0] = (byte)0xFF/*SyncWord*/;
			ADTSHeader[1] = (byte)((0x0F << 4)/*SyncWord*/ | (0 << 3)/*MPEG-4*/ | (0 << 1)/*Layer*/ | 1/*ProtectionAbsent*/);
			ADTSHeader[2] = (byte)(((ObjectType-1) << 6)/*Profile*/ | ((FrequencyIndex & 0x0F) << 2)/*SamplingFrequencyIndex*/ | (0 << 1)/*PrivateStream*/ | ((ChannelConfiguration >> 2) & 0x01)/*ChannelConfiguration*/);
			ADTSHeader[3] = (byte)(((ChannelConfiguration & 3) << 6)/*ChannelConfiguration*/ | (0 << 5)/*Originality*/ | (0 << 4)/*Home*/ | (0 << 3)/*CopyrightedStream*/ | (0 << 2)/*CopyrightStart*/ | ((AudioBufferSize >> 11) & 3)/*FrameLength*/);
			ADTSHeader[4] = (byte)((AudioBufferSize >> 3) & 0xFF)/*FrameLength*/;
			ADTSHeader[5] = (byte)((AudioBufferSize & 7) << 5)/*FrameLength*//*5 bits of BufferFullness*/;
			ADTSHeader[6] = (byte)/*6 bits of BufferFullness*/+(0)/*Number of AAC frames - 1*/;
			//.
			MyOutputStream.write(ADTSHeader);
			MyOutputStream.write(Buffer, 0,BufferSize);
			Packets++;
		}
	}
	 
	public class TVideoFrameCaptureCallback implements PreviewCallback {

		public TVideoFrameCaptureCallback() {
		}
		
		public void Release() throws IOException {
		}
		
		@Override        
		public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
			try {
				long Timestamp = System.nanoTime()/1000;
				//.
				VideoRecorderModule.MediaFrameServer.CurrentFrame.Set(camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, data,data.length, Timestamp);
				//.
				try {
					VideoRecorderModule.MediaFrameServer.CurrentFrameSubscribers.DoOnPacket(data,data.length, Timestamp);
				} 
				catch (IOException IOE) {
					String S = IOE.getMessage();
					if (S == null)
						S = IOE.getClass().getName();
					Log.e("VideoRecorderSaving", S);
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Log.e("VideoRecorderSaving", S);
				}
				//. saving the H264 frame
				if ((VideoFrameFileStream != null) && (VideoFrameEncoder != null)) 
					try {
						VideoFrameEncoder.EncodeInputBuffer(data,data.length,((System.nanoTime()/1000)-PacketTimeBase.TimeBase)/1000);
						//.
						camera_parameters_Video_FrameCount++;
					} 
					catch (IOException IOE) {
						String S = IOE.getMessage();
						if (S == null)
							S = IOE.getClass().getName();
						Log.e("VideoRecorderSaving", S);
					}
					catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
						Log.e("VideoRecorderSaving", S);
					}
			}
			finally {
				camera.addCallbackBuffer(data);			
			}
        }
	}

	private static class TVideoFrameEncoder extends TH264Encoder {

		protected CameraStreamerFRAME Streamer;
		//.
		protected OutputStream 	MyOutputStream = null;
		protected int			MyOutputStreamPosition = 0;
		protected OutputStream 	MyIndexOutputStream = null;
		protected OutputStream 	MyTimestampOutputStream = null;
		//.
		public int Packets = 0;
		
		public TVideoFrameEncoder(CameraStreamerFRAME pStreamer, int FrameWidth, int FrameHeight, int BitRate, int FrameRate, int pInputBufferPixelFormat, OutputStream pOutputStream, OutputStream pIndexOutputStream, OutputStream pTimestampOutputStream) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate, pInputBufferPixelFormat);
			Streamer = pStreamer;
			MyOutputStream = pOutputStream;
			MyIndexOutputStream = pIndexOutputStream;
			MyTimestampOutputStream = pTimestampOutputStream;
		}

		private byte[] Descriptor32 = new byte[4];
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			if (flSaveVideoCodecConfig) {
				flSaveVideoCodecConfig = false;
				//.
		        if (Streamer.MeasurementID != null) 
		        	try {
		        		String FN = Streamer.MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName+"."+"Config"+"."+"txt";
		        		SaveCodecConfig(FN, Buffer,BufferSize);
					} catch (Exception E) {
					}
			}
			//. saving ...
			MyOutputStream.write(Buffer, 0,BufferSize);
			//.
			if (flSyncFrame) {
				if (MyIndexOutputStream != null) {
					Descriptor32[0] = (byte)(MyOutputStreamPosition & 0xff);
					Descriptor32[1] = (byte)(MyOutputStreamPosition >> 8 & 0xff);
					Descriptor32[2] = (byte)(MyOutputStreamPosition >> 16 & 0xff);
					Descriptor32[3] = (byte)(MyOutputStreamPosition >>> 24);
					MyIndexOutputStream.write(Descriptor32);
				}
				if (MyTimestampOutputStream != null) {
					Descriptor32[0] = (byte)(Timestamp & 0xff);
					Descriptor32[1] = (byte)(Timestamp >> 8 & 0xff);
					Descriptor32[2] = (byte)(Timestamp >> 16 & 0xff);
					Descriptor32[3] = (byte)(Timestamp >>> 24);
					MyTimestampOutputStream.write(Descriptor32);
				}
			}
			//.
			MyOutputStreamPosition += BufferSize;
			//.
			Packets++;
		}
	}
	
	private static class TVideoFrameEncoderServerClient extends TH264EncoderServer.TClient {

		protected CameraStreamerFRAME Streamer;
		//.
		protected OutputStream 	MyOutputStream = null;
		protected int			MyOutputStreamPosition = 0;
		protected OutputStream 	MyIndexOutputStream = null;
		protected OutputStream 	MyTimestampOutputStream = null;
		//.
		public int Packets = 0;
		
		public TVideoFrameEncoderServerClient(CameraStreamerFRAME pStreamer, OutputStream pOutputStream, OutputStream pIndexOutputStream, OutputStream pTimestampOutputStream) {
			super(true);
			Streamer = pStreamer;
			MyOutputStream = pOutputStream;
			MyIndexOutputStream = pIndexOutputStream;
			MyTimestampOutputStream = pTimestampOutputStream;
		}

		private byte[] Descriptor32 = new byte[4];
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws IOException {
			if (flSaveVideoCodecConfig) {
				flSaveVideoCodecConfig = false;
				//.
		        if (Streamer.MeasurementID != null) 
		        	try {
		        		String FN = Streamer.MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName+"."+"Config"+"."+"txt";
		        		SaveCodecConfig(FN, Buffer,BufferSize);
					} catch (Exception E) {
					}
			}
			//. saving ...
			Timestamp = (Timestamp-PacketTimeBase.TimeBase)/1000; //. convert to zero-based timestamp
			//.
			MyOutputStream.write(Buffer, 0,BufferSize);
			//.
			if (flSyncFrame) {
				if (MyIndexOutputStream != null) {
					Descriptor32[0] = (byte)(MyOutputStreamPosition & 0xff);
					Descriptor32[1] = (byte)(MyOutputStreamPosition >> 8 & 0xff);
					Descriptor32[2] = (byte)(MyOutputStreamPosition >> 16 & 0xff);
					Descriptor32[3] = (byte)(MyOutputStreamPosition >>> 24);
					MyIndexOutputStream.write(Descriptor32);
				}
				if (MyTimestampOutputStream != null) {
					Descriptor32[0] = (byte)(Timestamp & 0xff);
					Descriptor32[1] = (byte)(Timestamp >> 8 & 0xff);
					Descriptor32[2] = (byte)(Timestamp >> 16 & 0xff);
					Descriptor32[3] = (byte)(Timestamp >>> 24);
					MyTimestampOutputStream.write(Descriptor32);
				}
			}
			//.
			MyOutputStreamPosition += BufferSize;
			//.
			Packets++;
		}
	}
	
	private android.hardware.Camera 			camera;
	private android.hardware.Camera.Parameters 	camera_parameters;
	private int 								camera_parameters_Audio_SampleRate = -1;
	private int 								camera_parameters_Audio_SampleCount = -1;
	private int 								camera_parameters_Video_FrameRate = -1;
	private Size 								camera_parameters_Video_FrameSize;
	private int									camera_parameters_Video_FramePixelFormat;
	private int 								camera_parameters_Video_FrameCount = -1;
	//.
	private TAudioSampleSource			AudioSampleSource;
	private TAudioSampleEncoder			AudioSampleEncoder;	
	private FileOutputStream 			AudioSampleFileStream = null;
	private BufferedOutputStream		AudioSampleBufferedStream = null;
	private TAACChannel 				AACChannel = null; 
	//.
	private TVideoFrameCaptureCallback 		VideoFrameCaptureCallback;
	private TVideoFrameEncoder				VideoFrameEncoder;	
	private TVideoFrameEncoderServerClient	VideoFrameEncoderServerClient;	
	private FileOutputStream 				VideoFrameFileStream = null;
	private BufferedOutputStream			VideoFrameBufferedStream = null;
	private FileOutputStream 				VideoFrameIndexFileStream = null;
	private BufferedOutputStream			VideoFrameIndexBufferedStream = null;
	private FileOutputStream 				VideoFrameTimestampFileStream = null;
	private BufferedOutputStream			VideoFrameTimestampBufferedStream = null;
	private TH264IChannel 					H264IChannel = null; 
	
	public CameraStreamerFRAME(TVideoRecorderModule pVideoRecorderModule) {
		super(pVideoRecorderModule);
		//.
		camera = null;
		VideoFrameCaptureCallback = new TVideoFrameCaptureCallback();
		//.
		SetCurrentCamera(this);
	}
	
	@Override
	public void Destroy() throws Exception {
		SetCurrentCamera(null);
		//.
		Finalize();
	}
	
	 private byte[] CreateCallbackBuffer() {
	        int bufferSize;
	        byte buffer[];
	        int bitsPerPixel;
	        //.
	        android.hardware.Camera.Parameters mParams = camera.getParameters();
	        Size mSize = mParams.getPreviewSize();
	        int mImageFormat = mParams.getPreviewFormat();
	        if (mImageFormat == ImageFormat.YV12) {
	            int yStride   = (int) Math.ceil(mSize.width / 16.0) * 16;
	            int uvStride  = (int) Math.ceil( (yStride / 2) / 16.0) * 16;
	            int ySize     = yStride * mSize.height;
	            int uvSize    = uvStride * mSize.height / 2;
	            bufferSize    = ySize + uvSize * 2;
	            buffer = new byte[bufferSize];
	            return buffer; //. ->
	        }
	        //.
	        bitsPerPixel = ImageFormat.getBitsPerPixel(mImageFormat);
	        bufferSize = (int)(mSize.height*mSize.width*((bitsPerPixel/(float)8)));
	        buffer = new byte[bufferSize];
	        return buffer;
	}
	 
	@SuppressWarnings("deprecation")
	@Override
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, long UserID, String UserPassword, long pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview, double MaxMeasurementDuration) throws Exception {
		flAudio = pflAudio;
		flVideo = pflVideo;
		flTransmitting = pflTransmitting;
		flSaving = pflSaving;
		//.
		synchronized (this) {
			if (flSaving) {
				MeasurementDescriptor = new TMeasurementDescriptor();
				MeasurementID = TSensorsModuleMeasurements.CreateNewMeasurement(MeasurementDescriptor); 
				MeasurementFolder = TSensorsModuleMeasurements.DataBaseFolder+"/"+MeasurementID;
			}
			else {  
				MeasurementID = null;
				MeasurementDescriptor = null;
				MeasurementFolder = "";
			}
		}
		//. AUDIO
		if (flAudio) {
			AudioSampleSource = new TAudioSampleSource();
			if (asrc > 0)
				AudioSampleSource.SetSource(asrc);
			if (sps > 0)
				AudioSampleSource.SetSampleRate(sps);
	        camera_parameters_Audio_SampleRate = AudioSampleSource.Microphone_SamplePerSec;
			//.
	        synchronized (VideoRecorderModule.MediaFrameServer.CurrentSamplePacket) {
	        	VideoRecorderModule.MediaFrameServer.SampleRate = AudioSampleSource.Microphone_SamplePerSec;
	        	VideoRecorderModule.MediaFrameServer.SamplePacketInterval = 10;
	        	VideoRecorderModule.MediaFrameServer.SampleBitRate = abr;
			}
			camera_parameters_Audio_SampleCount = 0;
	        //.
	        if (MeasurementID != null) {
				AudioSampleFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.AudioAACADTSFileName);
				AudioSampleBufferedStream = new BufferedOutputStream(AudioSampleFileStream, 65535);
				AudioSampleEncoder = new TAudioSampleEncoder(this, abr, AudioSampleSource.Microphone_SamplePerSec, AudioSampleBufferedStream);
	        }
		}
		else {
			camera_parameters_Audio_SampleCount = -1;
			AudioSampleFileStream = null;
		}
		//. VIDEO
		if (flVideo) {
			if ((vsrc > 0) && (android.hardware.Camera.getNumberOfCameras() > vsrc)) 
				camera = android.hardware.Camera.open(vsrc);
			else
				camera = android.hardware.Camera.open();
	        camera_parameters = camera.getParameters();
	        camera_parameters.setPreviewSize(resX,resY);
	        if (fps > 0)
	        	camera_parameters.setPreviewFrameRate(fps);
	        //.
	        camera_parameters.setPreviewFormat(ImageFormat.NV21);
			//.
	        camera_parameters.set("orientation", "landscape");
	        camera.setParameters(camera_parameters);
	        camera_parameters = camera.getParameters();
	        camera_parameters_Video_FrameSize = camera_parameters.getPreviewSize();
	        camera_parameters_Video_FrameRate = camera_parameters.getPreviewFrameRate();
	        camera_parameters_Video_FramePixelFormat = camera_parameters.getPreviewFormat();
	        //.
	        if (VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable()) {
	        	Surface 	Preview = null;
	        	Rect 		PreviewFrame = null;
	        	if (pflPreview) {
	        		Preview = holder.getSurface();
	        		PreviewFrame = holder.getSurfaceFrame();
	        	}
	        	VideoRecorderModule.MediaFrameServer.H264EncoderServer_Start(camera, camera_parameters_Video_FrameSize.width, camera_parameters_Video_FrameSize.height, br, camera_parameters_Video_FrameRate, Preview,PreviewFrame);
	        }
	        else {
		        for (int I = 0; I < 4; I++) 
		        	camera.addCallbackBuffer(CreateCallbackBuffer());
		        //.
		        camera.setPreviewCallbackWithBuffer(VideoFrameCaptureCallback);
		        //.
	        	camera.setPreviewDisplay(holder);
	        }
	        //. 
	        camera_parameters_Video_FrameCount = 0;
	        //. setting FrameServer
	        synchronized (VideoRecorderModule.MediaFrameServer.CurrentFrame) {
	        	VideoRecorderModule.MediaFrameServer.FrameSize = camera_parameters_Video_FrameSize;
	        	VideoRecorderModule.MediaFrameServer.FrameRate = camera_parameters_Video_FrameRate;
	        	VideoRecorderModule.MediaFrameServer.FrameInterval = (int)(1000/camera_parameters_Video_FrameRate);
	        	VideoRecorderModule.MediaFrameServer.FrameBitRate = br;
	        	VideoRecorderModule.MediaFrameServer.FramePixelFormat = camera_parameters_Video_FramePixelFormat; 
			}
	        //.
	        if (MeasurementID != null) { 
				VideoFrameFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName);
				VideoFrameBufferedStream = new BufferedOutputStream(VideoFrameFileStream, 256*1024);
				VideoFrameIndexFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoIndex32FileName);
				VideoFrameIndexBufferedStream = new BufferedOutputStream(VideoFrameIndexFileStream, 65535);
				VideoFrameTimestampFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoTS32FileName);
				VideoFrameTimestampBufferedStream = new BufferedOutputStream(VideoFrameTimestampFileStream, 65535);
				if (VideoRecorderModule.MediaFrameServer.H264EncoderServer_Exists()) {
					VideoFrameEncoderServerClient = new TVideoFrameEncoderServerClient(this, VideoFrameBufferedStream,VideoFrameIndexBufferedStream,VideoFrameTimestampBufferedStream);
					//.
					VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Register(VideoFrameEncoderServerClient);
				}
				else
					VideoFrameEncoder = new TVideoFrameEncoder(this, camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, br, camera_parameters_Video_FrameRate, camera_parameters_Video_FramePixelFormat, VideoFrameBufferedStream,VideoFrameIndexBufferedStream,VideoFrameTimestampBufferedStream);
	        }
		}
		else {
	        camera_parameters_Video_FrameCount = -1;
			VideoFrameFileStream = null;
			VideoFrameBufferedStream = null;
			VideoFrameIndexFileStream = null;
			VideoFrameIndexBufferedStream = null;
			VideoFrameTimestampFileStream = null;
			VideoFrameTimestampBufferedStream = null;
		}
		//.
		synchronized (this) {
	        if (MeasurementDescriptor != null) {
	        	if (flAudio) {
	        		AACChannel = new TAACChannel();
	        		AACChannel.ID = TChannel.GetNextID();
	        		AACChannel.Enabled = true;
	        		AACChannel.DataFormat = 0;
	        		AACChannel.Name = "Audio channel";
	        		AACChannel.Info = "AAC channel";
	        		AACChannel.Size = 0;
	        		AACChannel.Configuration = "";
	        		AACChannel.Parameters = "";
	        		AACChannel.SampleRate = camera_parameters_Audio_SampleRate;
	        		//.
	        		MeasurementDescriptor.Model.Stream.Channels.add(AACChannel);
	        		//. 
	            	MeasurementDescriptor.AudioFormat = AUDIO_SAMPLE_FILE_FORMAT_ADTSAACPACKETS;
	            	MeasurementDescriptor.AudioSPS = camera_parameters_Audio_SampleRate;
	        	}
	        	if (flVideo) {
	        		H264IChannel = new TH264IChannel();
	        		H264IChannel.ID = TChannel.GetNextID();
	        		H264IChannel.Enabled = true;
	        		H264IChannel.DataFormat = 0;
	        		H264IChannel.Name = "Video channel";
	        		H264IChannel.Info = "H264(Indexed) channel";
	        		H264IChannel.Size = 0;
	        		H264IChannel.Configuration = "";
	        		H264IChannel.Parameters = "";
	        		H264IChannel.FrameRate = camera_parameters_Video_FrameRate;
	        		//.
	        		MeasurementDescriptor.Model.Stream.Channels.add(H264IChannel);
	        		//. 
	            	MeasurementDescriptor.VideoFormat = VIDEO_FRAME_FILE_FORMAT_H264PACKETS;
	            	MeasurementDescriptor.VideoFPS = camera_parameters_Video_FrameRate;
	        	}
	        	TSensorsModuleMeasurements.SetMeasurementDescriptor(MeasurementID, MeasurementDescriptor);
	        }
		}
	}
	
	@Override
	public void Finalize() throws Exception {
		super.Finalize();
		//.
		synchronized (this) {
			if (camera != null) {
				 camera.stopPreview();
		         camera.setPreviewDisplay(null);
		         camera.setPreviewCallback(null);
		         camera.release();
		         camera = null;
		    }
			if (VideoFrameCaptureCallback != null) {
				VideoFrameCaptureCallback.Release();
				VideoFrameCaptureCallback = null;
			}
			//.
			if (VideoRecorderModule.MediaFrameServer.H264EncoderServer_Exists()) 
				VideoRecorderModule.MediaFrameServer.H264EncoderServer_Stop();
		}
		//.
		if (AudioSampleSource != null) {
			AudioSampleSource.Release();
			AudioSampleSource = null;
		}
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//. Start audio streaming
		if (flAudio) {
			AudioSampleSource.Start();
	        //.
			VideoRecorderModule.MediaFrameServer.flAudioActive = true;
		}
		//. Start video streaming
		if (flVideo) {
	        camera.startPreview();
	        //.
	        VideoRecorderModule.MediaFrameServer.flVideoActive = true;
		}
	}
	
	@Override
	public void Stop() throws Exception {
		double FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//. Stop video streaming
		if (flVideo) {
			VideoRecorderModule.MediaFrameServer.flVideoActive = false;
			//.
			camera.stopPreview();
		}
		//. Stop audio streaming
		if (flAudio) {
			VideoRecorderModule.MediaFrameServer.flAudioActive = false;
			//.
			AudioSampleSource.Stop();
		}
		//.
		if (MeasurementID != null) {
			int AudioSampleEncoderPackets = 0;
			if (AudioSampleEncoder != null) {
				AudioSampleEncoderPackets = AudioSampleEncoder.Packets;
				AudioSampleEncoder.Destroy();
				AudioSampleEncoder = null;
			}
			if (AudioSampleBufferedStream != null) {
				AudioSampleBufferedStream.close();
				AudioSampleBufferedStream = null;
			}
			//.
			if (AudioSampleFileStream != null) {
				AudioSampleFileStream.close();
				AudioSampleFileStream = null;
			}
			//.
			int VideoFrameEncoderPackets = 0;
			if (VideoRecorderModule.MediaFrameServer.H264EncoderServer_Exists()) {
				if (VideoFrameEncoderServerClient != null) {
					VideoRecorderModule.MediaFrameServer.H264EncoderServer_Clients_Unregister(VideoFrameEncoderServerClient);
					//.
					VideoFrameEncoderPackets = VideoFrameEncoderServerClient.Packets;
					//.
					VideoFrameEncoderServerClient.Destroy();
					VideoFrameEncoderServerClient = null;
				}
				
			}
			else
				if (VideoFrameEncoder != null) {
					VideoFrameEncoderPackets = VideoFrameEncoder.Packets;
					//.
					VideoFrameEncoder.Destroy();
					VideoFrameEncoder = null;
				}
			//.
			if (VideoFrameTimestampBufferedStream != null) {
				VideoFrameTimestampBufferedStream.close();
				VideoFrameTimestampBufferedStream = null;
			}
			if (VideoFrameTimestampFileStream != null) {
				VideoFrameTimestampFileStream.close();
				VideoFrameTimestampFileStream = null;
			}
			if (VideoFrameIndexBufferedStream != null) {
				VideoFrameIndexBufferedStream.close();
				VideoFrameIndexBufferedStream = null;
			}
			if (VideoFrameIndexFileStream != null) {
				VideoFrameIndexFileStream.close();
				VideoFrameIndexFileStream = null;
			}
			if (VideoFrameBufferedStream != null) {
				VideoFrameBufferedStream.close();
				VideoFrameBufferedStream = null;
			}
			if (VideoFrameFileStream != null) {
				VideoFrameFileStream.close();
				VideoFrameFileStream = null;
			}
			//.
			synchronized (this) {
				if (MeasurementDescriptor != null) {
					MeasurementDescriptor.FinishTimestamp = FinishTimestamp;
					//.
					if (AACChannel != null)
						AACChannel.Packets = AudioSampleEncoderPackets;
					if (H264IChannel != null)
						H264IChannel.Packets = VideoFrameEncoderPackets;
					//. 
					MeasurementDescriptor.AudioPackets = AudioSampleEncoderPackets;
					MeasurementDescriptor.VideoPackets = VideoFrameEncoderPackets;
					//.
		        	TSensorsModuleMeasurements.SetMeasurementDescriptor(MeasurementID, MeasurementDescriptor);
				}
			}
			//.
			MeasurementID = null;
		}
	}
	
	@Override
	public void StartTransmitting(long pidGeographServerObject) {
		//.
		flTransmitting = true;
	}
	
	@Override
	public void FinishTransmitting() {
		//.
		flTransmitting = false;
	}
	
	@Override
	public synchronized TMeasurementDescriptor GetMeasurementCurrentDescriptor() throws Exception {
		if (MeasurementDescriptor == null)
			return null; //. ->
		//.
		int AudioSampleEncoderPackets = 0;
		if (AudioSampleEncoder != null)
			AudioSampleEncoderPackets = AudioSampleEncoder.Packets;
		//.
		int VideoFrameEncoderPackets = 0;
		if (VideoFrameEncoder != null)
			VideoFrameEncoderPackets = VideoFrameEncoder.Packets;
		//.
		MeasurementDescriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//.
		if (AACChannel != null)
			AACChannel.Packets = AudioSampleEncoderPackets;
		if (H264IChannel != null)
			H264IChannel.Packets = VideoFrameEncoderPackets;
		//. 
		MeasurementDescriptor.AudioPackets = AudioSampleEncoderPackets;
		MeasurementDescriptor.VideoPackets = VideoFrameEncoderPackets;
		//.
		return MeasurementDescriptor;
	}
}
