package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.AudioModule.TMicrophoneCapturingServer;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AACEncoder;
import com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264Encoder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
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
	
	public class TAudioSampleSource extends TCancelableThread {
		
		//.
		private AudioRecord Microphone_Recorder = null; 
		public int 			Microphone_Source = MediaRecorder.AudioSource.DEFAULT;
		public int 			Microphone_SamplePerSec = 8000;
        private int 		Microphone_BufferSize;

		public TAudioSampleSource() {
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
	        MediaFrameServer.CurrentSamplePacket.Set(Packet,PacketSize, Timestamp);
	        //.
	        try {
	        	MediaFrameServer.CurrentSamplePacketSubscribers.DoOnPacket(Packet,PacketSize, Timestamp);
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
	
	private static class TAudioSampleEncoder extends AACEncoder {

		private OutputStream MyOutputStream;
		
		private boolean flConfigIsArrived = false;
		private byte ObjectType;
		private byte FrequencyIndex;
		private byte ChannelConfiguration;
		private byte[] ADTSHeader = new byte[7];
		public int Packets = 0;
		
		public TAudioSampleEncoder(int BitRate, int SampleRate, OutputStream pOutputStream) {
			super(BitRate, SampleRate);
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
				MediaFrameServer.CurrentFrame.Set(camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, camera_parameters_Video_FrameImageFormat, data,data.length, Timestamp);
				//.
				try {
					MediaFrameServer.CurrentFrameSubscribers.DoOnPacket(data,data.length, Timestamp);
				} 
				catch (IOException IOE) {
				}
				catch (Exception E) {
				}
				//. saving the H264 frame
				if (VideoFrameFileStream != null) 
					try {
						VideoFrameEncoder.EncodeInputBuffer(data,data.length,((System.nanoTime()/1000)-PacketTimeBase.TimeBase)/1000);
						//.
						camera_parameters_Video_FrameCount++;
					} 
					catch (IOException IOE) {
					}
					catch (Exception E) {
					}
			}
			finally {
				camera.addCallbackBuffer(data);			
			}
        }
	}

	private static class TVideoFrameEncoder extends H264Encoder {

		protected OutputStream 	MyOutputStream = null;
		protected int			MyOutputStreamPosition = 0;
		protected OutputStream 	MyIndexOutputStream = null;
		protected OutputStream 	MyTimestampOutputStream = null;
		//.
		public int Packets = 0;
		
		public TVideoFrameEncoder(int FrameWidth, int FrameHeight, int BitRate, int FrameRate, OutputStream pOutputStream, OutputStream pIndexOutputStream, OutputStream pTimestampOutputStream) {
			super(FrameWidth, FrameHeight, BitRate, FrameRate);
			MyOutputStream = pOutputStream;
			MyIndexOutputStream = pIndexOutputStream;
			MyTimestampOutputStream = pTimestampOutputStream;
		}

		private byte[] Descriptor32 = new byte[4];
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp) throws IOException {
			MyOutputStream.write(Buffer, 0,BufferSize);
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
	private int									camera_parameters_Video_FrameImageFormat;
	private int 								camera_parameters_Video_FrameCount = -1;
	//.
	private TAudioSampleSource			AudioSampleSource;
	private TAudioSampleEncoder			AudioSampleEncoder;	
	private FileOutputStream 			AudioSampleFileStream = null;
	//.
	private TVideoFrameCaptureCallback 	VideoFrameCaptureCallback;
	private TVideoFrameEncoder			VideoFrameEncoder;	
	private FileOutputStream 			VideoFrameFileStream = null;
	private BufferedOutputStream		VideoFrameBufferedStream = null;
	private FileOutputStream 			VideoFrameIndexFileStream = null;
	private BufferedOutputStream		VideoFrameIndexBufferedStream = null;
	private FileOutputStream 			VideoFrameTimestampFileStream = null;
	private BufferedOutputStream		VideoFrameTimestampBufferedStream = null;
	
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
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, int UserID, String UserPassword, int pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, double MaxMeasurementDuration) throws Exception {
		flAudio = pflAudio;
		flVideo = pflVideo;
		flTransmitting = pflTransmitting;
		flSaving = pflSaving;
		//.
		String MeasurementFolder = null;
		synchronized (this) {
			if (flSaving) {
				MeasurementID = TVideoRecorderMeasurements.CreateNewMeasurementID();
				//.
				TVideoRecorderMeasurements.CreateNewMeasurement(MeasurementID,TVideoRecorderModule.MODE_FRAMESTREAM); 
				MeasurementFolder = TVideoRecorderMeasurements.VideoRecorder0_DataBaseFolder+"/"+MeasurementID;
			}
			else  
				MeasurementID = null;
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
	        synchronized (MediaFrameServer.CurrentSamplePacket) {
	        	MediaFrameServer.SampleRate = AudioSampleSource.Microphone_SamplePerSec;
	        	MediaFrameServer.SamplePacketInterval = 10;
	        	MediaFrameServer.SampleBitRate = abr;
			}
			camera_parameters_Audio_SampleCount = 0;
	        //.
	        if (MeasurementID != null) {
				AudioSampleFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.AudioAACADTSFileName);
				AudioSampleEncoder = new TAudioSampleEncoder(abr, AudioSampleSource.Microphone_SamplePerSec, AudioSampleFileStream);
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
	        ///? camera_parameters.setPreviewFpsRange(15000,30000);
	        camera_parameters_Video_FrameRate = camera_parameters.getPreviewFrameRate();
	        camera.setParameters(camera_parameters);
	        camera_parameters = camera.getParameters();
	        camera_parameters_Video_FrameSize = camera_parameters.getPreviewSize();
	        camera_parameters_Video_FrameImageFormat = camera_parameters.getPreviewFormat();
	        camera.setPreviewDisplay(holder);
	        //.
	        for (int I = 0; I < 4; I++) 
	        	camera.addCallbackBuffer(CreateCallbackBuffer());
	        camera.setPreviewCallbackWithBuffer(VideoFrameCaptureCallback);
	        //.
	        camera_parameters_Video_FrameCount = 0;
	        //.
	        if (MeasurementID != null) { 
				VideoFrameFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoH264FileName);
				VideoFrameBufferedStream = new BufferedOutputStream(VideoFrameFileStream, 256*1024);
				VideoFrameIndexFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoIndex32FileName);
				VideoFrameIndexBufferedStream = new BufferedOutputStream(VideoFrameIndexFileStream, 65535);
				VideoFrameTimestampFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoTS32FileName);
				VideoFrameTimestampBufferedStream = new BufferedOutputStream(VideoFrameTimestampFileStream, 65535);
				VideoFrameEncoder = new TVideoFrameEncoder(camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, br, camera_parameters_Video_FrameRate, VideoFrameBufferedStream,VideoFrameIndexBufferedStream,VideoFrameTimestampBufferedStream);
	        }
	        //. setting FrameServer
	        synchronized (MediaFrameServer.CurrentFrame) {
	        	MediaFrameServer.FrameSize = camera_parameters_Video_FrameSize;
	        	MediaFrameServer.FrameRate = camera_parameters_Video_FrameRate;
	        	MediaFrameServer.FrameInterval = (int)(1000/camera_parameters_Video_FrameRate);
	        	MediaFrameServer.FrameBitRate = br;
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
        if (MeasurementID != null) {
        	TMeasurementDescriptor MD = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementID);
        	if (flAudio) {
            	MD.AudioFormat = AUDIO_SAMPLE_FILE_FORMAT_ADTSAACPACKETS;
            	MD.AudioSPS = camera_parameters_Audio_SampleRate;
        	}
        	if (flVideo) {
            	MD.VideoFormat = VIDEO_FRAME_FILE_FORMAT_H264PACKETS;
            	MD.VideoFPS = camera_parameters_Video_FrameRate;
        	}
        	TVideoRecorderMeasurements.SetMeasurementDescriptor(MeasurementID, MD);
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
	        MediaFrameServer.flAudioActive = true;
		}
		//. Start video streaming
		if (flVideo) {
	        camera.startPreview();
	        //.
	        MediaFrameServer.flVideoActive = true;
		}
	}
	
	@Override
	public void Stop() throws Exception {
		double FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//. Stop video streaming
		if (flVideo) {
			MediaFrameServer.flVideoActive = false;
			//.
			camera.stopPreview();
		}
		//. Stop audio streaming
		if (flAudio) {
			MediaFrameServer.flAudioActive = false;
			//.
			AudioSampleSource.Stop();
		}
		//.
		if (MeasurementID != null) {
			if (AudioSampleFileStream != null) {
				AudioSampleFileStream.close();
				AudioSampleFileStream = null;
			}
			int AudioSampleEncoderPackets = 0;
			if (AudioSampleEncoder != null) {
				AudioSampleEncoderPackets = AudioSampleEncoder.Packets;
				AudioSampleEncoder.Destroy();
				AudioSampleEncoder = null;
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
			int VideoFrameEncoderPackets = 0;
			if (VideoFrameEncoder != null) {
				VideoFrameEncoderPackets = VideoFrameEncoder.Packets;
				VideoFrameEncoder.Destroy();
				VideoFrameEncoder = null;
			}
			//.
			TVideoRecorderMeasurements.SetMeasurementFinish(MeasurementID,FinishTimestamp,AudioSampleEncoderPackets,VideoFrameEncoderPackets);
			//.
			MeasurementID = null;
		}
	}
	
	@Override
	public void StartTransmitting(int pidGeographServerObject) {
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
		if (MeasurementID == null)
			return null; //. ->
		TMeasurementDescriptor Result = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementID);
		if (Result == null)
			return null; //. ->
		//.
		Result.AudioPackets = 0;
		if (AudioSampleEncoder != null)
			Result.AudioPackets = AudioSampleEncoder.Packets;
		Result.VideoPackets = 0;
		if (VideoFrameEncoder != null)
			Result.VideoPackets = VideoFrameEncoder.Packets;
		Result.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//.
		return Result;
	}
}
