package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

public class CameraStreamerFRAME extends Camera {
	
	public static final int AUDIO_SAMPLE_FILE_FORMAT_PCMPACKETS 		= 1;
	public static final int AUDIO_SAMPLE_FILE_FORMAT_ZIPPEDPCMPACKETS 	= 2;
	//.
	public static final int VIDEO_FRAME_FILE_FORMAT_JPEGPACKETS 		= 1;
	public static final int VIDEO_FRAME_FILE_FORMAT_ZIPPEDJPEGPACKETS 	= 2;
	
	public class TAudioSampleSource extends TCancelableThread {
		
		//.
		private AudioRecord Microphone_Recorder = null; 
		private int 		Microphone_SamplePerSec = 8000;
		//.
		private ByteArrayOutputStream PacketZippingStream = new ByteArrayOutputStream();
		private byte[] Descriptor = new byte[4];
		
		public TAudioSampleSource(int pSPS) {
			if (pSPS > 0)
				Microphone_SamplePerSec = pSPS;
		}
		
		public void Release() throws IOException {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
			if (PacketZippingStream != null) {
				PacketZippingStream.close();
				PacketZippingStream = null;
			}
		}
		
		public void Start() {
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Stop() {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
		}
		
	    private void Microphone_Initialize() throws IOException {
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
			if (!flSaving)
				return; //. ->
			try {
				Microphone_Initialize();
				try {
			        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
			        byte[] TransferBuffer = new byte[2*Microphone_SamplePerSec/2];
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
			catch (Throwable T) {
			}
		}
		
		private void DoOnAudioPacket(byte[] Packet, int PacketSize) throws IOException {
			if (AudioFrameFileStream != null) {
				PacketZippingStream.reset();
				//.
	            ZOutputStream out = new ZOutputStream(PacketZippingStream,JZlib.Z_BEST_SPEED);
	            try
	            {
	                out.write(Packet,0,PacketSize);
	            }
	            finally
	            {
	                out.close();
	            }
	            //.
	            int Size = PacketZippingStream.size();
	            //.
				Descriptor[0] = (byte)(Size & 0xff);
				Descriptor[1] = (byte)(Size >> 8 & 0xff);
				Descriptor[2] = (byte)(Size >> 16 & 0xff);
				Descriptor[3] = (byte)(Size >>> 24);
				//.
				AudioFrameFileStream.write(Descriptor);
				PacketZippingStream.writeTo(AudioFrameFileStream);
			}
			camera_parameters_Audio_SampleCount++;
		}
	}
	
	public class TVideoFrameCaptureCallback implements PreviewCallback {

		public int 						SavingFrameQuality = 30; //. %
		private Rect					SavingFrameRect = new Rect();
		private byte[] 					SavingFrameDescriptor = new byte[4];
		private ByteArrayOutputStream 	SavingFrameStream = new ByteArrayOutputStream();
		
		public void Release() throws IOException {
			if (SavingFrameStream != null) {
				SavingFrameStream.close();
				SavingFrameStream = null;
			}
		}
		
		@Override        
		public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
			MediaFrameServer.CurrentFrame.Set(camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, camera_parameters_Video_FrameImageFormat, data);
			//. saving the frame
			if (VideoFrameFileStream != null) {
				SavingFrameStream.reset();
				//.
				switch (camera_parameters_Video_FrameImageFormat) {
	            case ImageFormat.NV16:
	            case ImageFormat.NV21:
	            case ImageFormat.YUY2:
	            case ImageFormat.YV12:
	            	if (camera_parameters_Video_FrameSize.width != SavingFrameRect.right)
	            		SavingFrameRect.right = camera_parameters_Video_FrameSize.width; 
	            	if (camera_parameters_Video_FrameSize.height != SavingFrameRect.bottom)
	            		SavingFrameRect.bottom = camera_parameters_Video_FrameSize.height; 
	                new YuvImage(data, camera_parameters_Video_FrameImageFormat, camera_parameters_Video_FrameSize.width,camera_parameters_Video_FrameSize.height, null).compressToJpeg(SavingFrameRect, SavingFrameQuality, SavingFrameStream);
	                break; //. >

	            default:
				}
				if (SavingFrameStream.size() > 0) {
					int SavingFrameSize = SavingFrameStream.size();
					SavingFrameDescriptor[0] = (byte)(SavingFrameSize & 0xff);
					SavingFrameDescriptor[1] = (byte)(SavingFrameSize >> 8 & 0xff);
					SavingFrameDescriptor[2] = (byte)(SavingFrameSize >> 16 & 0xff);
					SavingFrameDescriptor[3] = (byte)(SavingFrameSize >>> 24);
					//.
					try {
						VideoFrameFileStream.write(SavingFrameDescriptor);
						SavingFrameStream.writeTo(VideoFrameFileStream);
					} catch (IOException IOE) {
					}
				}
			}
			camera_parameters_Video_FrameCount++;
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
	private FileOutputStream 			AudioFrameFileStream = null;
	//.
	private TVideoFrameCaptureCallback 	VideoFrameCaptureCallback;
	private FileOutputStream 			VideoFrameFileStream = null;
	
	
	public CameraStreamerFRAME() {
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
	
	@Override
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int sps, int abr, int resX, int resY, int fps, int br, int UserID, String UserPassword, int pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, double MaxMeasurementDuration) throws Exception {
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
				TVideoRecorderMeasurements.CreateNewMeasurement(MeasurementID,TVideoRecorderModule.MODE_FRAMESTREAM1); 
				MeasurementFolder = TVideoRecorderMeasurements.MyDataBaseFolder+"/"+MeasurementID;
			}
			else  
				MeasurementID = null;
		}
		//. AUDIO
		if (flAudio) {
			AudioSampleSource = new TAudioSampleSource(sps);
			//.
			camera_parameters_Audio_SampleCount = 0;
	        //.
	        if (MeasurementID != null) 
				AudioFrameFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.AudioSampleFileName);
		}
		else {
			camera_parameters_Audio_SampleCount = -1;
			AudioFrameFileStream = null;
		}
		//. VIDEO
		if (flVideo) {
	        camera = android.hardware.Camera.open();
	        camera_parameters = camera.getParameters();
	        camera_parameters.setPreviewFrameRate(fps);
	        ///? camera_parameters.setPreviewFpsRange(15000,30000);
	        camera_parameters_Video_FrameRate = camera_parameters.getPreviewFrameRate();
	        synchronized (MediaFrameServer.CurrentFrame) {
	        	MediaFrameServer.FrameRate = camera_parameters_Video_FrameRate;
	        	MediaFrameServer.FrameInterval = (int)(1000/camera_parameters_Video_FrameRate);
			}
	        camera.setParameters(camera_parameters);
	        camera_parameters = camera.getParameters();
	        camera_parameters_Video_FrameSize = camera_parameters.getPreviewSize();
	        camera_parameters_Video_FrameImageFormat = camera_parameters.getPreviewFormat();
	        camera.setPreviewDisplay(holder);
	        camera.setPreviewCallback(VideoFrameCaptureCallback);
	        camera_parameters_Video_FrameCount = 0;
	        //.
	        if (MeasurementID != null) 
				VideoFrameFileStream = new FileOutputStream(MeasurementFolder+"/"+TVideoRecorderMeasurements.VideoFrameFileName);
		}
		else {
	        camera_parameters_Video_FrameCount = -1;
			VideoFrameFileStream = null;
		}
		//.
        if (MeasurementID != null) {
        	TMeasurementDescriptor MD = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementID);
        	if (flAudio) {
            	MD.AudioFormat = AUDIO_SAMPLE_FILE_FORMAT_ZIPPEDPCMPACKETS;
            	MD.AudioSPS = camera_parameters_Audio_SampleRate;
        	}
        	if (flVideo) {
            	MD.VideoFormat = VIDEO_FRAME_FILE_FORMAT_JPEGPACKETS;
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
		if (flAudio)
			AudioSampleSource.Start();
		//. Start video streaming
		if (flVideo) {
	        camera.startPreview();
	        //.
	        MediaFrameServer.flVideoActive = true;
		}
	}
	
	@Override
	public void Stop() throws Exception {
		MediaFrameServer.flVideoActive = false;
		//. Stop video streaming
		if (flVideo) {
			camera.stopPreview();
			//.
			if (MeasurementID != null) {
				if (VideoFrameFileStream != null) {
					VideoFrameFileStream.close();
					VideoFrameFileStream = null;
				}
				//.
				TVideoRecorderMeasurements.SetMeasurementFinish(MeasurementID,camera_parameters_Audio_SampleCount,camera_parameters_Video_FrameCount);
				//.
				MeasurementID = null;
			}
		}
		//. Stop audio streaming
		if (flAudio)
			AudioSampleSource.Stop();
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
		if (flAudio)
			Result.AudioPackets = camera_parameters_Audio_SampleCount;
		Result.VideoPackets = 0;
		if (flVideo)
			Result.VideoPackets = camera_parameters_Video_FrameCount;
		Result.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//.
		return Result;
	}
}
