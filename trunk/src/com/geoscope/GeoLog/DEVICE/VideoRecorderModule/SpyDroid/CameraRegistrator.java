package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

public class CameraRegistrator extends Camera {

    public static final String LOG_TAG = "SPYDROID";
    
	private MediaRecorder media = null;
	
	public CameraRegistrator(TVideoRecorderModule pVideoRecorderModule) {
		super(pVideoRecorderModule);
		//.
		media = new MediaRecorder();
	}
	
	@Override
	public void Destroy() throws Exception {
		Finalize();
		//.
		if (media != null) {
			media.release();
			media = null;
		}
	}
	
	@Override
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, long UserID, String UserPassword, long pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview, double MaxMeasurementDuration) throws Exception {
		flAudio = pflAudio;
		flVideo = pflVideo;
		flTransmitting = pflTransmitting;
		flSaving = pflSaving;
		//.
		if ((!(flAudio || flVideo)) || !flSaving)
			return; //. ->
		//.
		synchronized (this) {
			if (flSaving) {
				MeasurementID = TSensorMeasurement.GetNewID();
				MeasurementDescriptor = new TMeasurementDescriptor();
				MeasurementDescriptor.Mode = (short)Mode; 
				TSensorsModuleMeasurements.CreateNewMeasurement(TSensorsModuleMeasurements.DataBaseFolder,MeasurementID,MeasurementDescriptor); 
				MeasurementFolder = TSensorsModuleMeasurements.DataBaseFolder+"/"+MeasurementID;
			}
			else { 
				MeasurementID = null;
				MeasurementDescriptor = null;
				MeasurementFolder = "";
			}
		}
		//.
    	media.reset();
    	if (flAudio)
    		media.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
    	if (flVideo)
    		media.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
    	switch (Mode) {
    	case TVideoRecorderModule.MODE_MPEG4: 
    		media.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    		break; //. >

    	case TVideoRecorderModule.MODE_3GP: 
    		media.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    		break; //. >
    	}
    	if (flAudio) {
    		if (sps > 0)
    			media.setAudioSamplingRate(sps);
    		if (abr > 0)
    			media.setAudioEncodingBitRate(abr);
    		media.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    	}
    	if (flVideo) {
			if (holder == null)
				throw new Exception("surface isn't set"); //. ->
			//.
    		if (fps > 0)
    			media.setVideoFrameRate(fps);
        	media.setVideoSize(resX, resY);
			if (br > 0)
				media.setVideoEncodingBitRate(br);
	    	switch (Mode) {
	    	case TVideoRecorderModule.MODE_MPEG4: 
	        	media.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
	    		break; //. >

	    	case TVideoRecorderModule.MODE_3GP: 
	        	media.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    		break; //. >
	    	}
    		media.setPreviewDisplay(holder.getSurface());
    	}
    	media.setMaxDuration((int)(MaxMeasurementDuration*24.0*3600.0*1000.0)+10000/*additive*/);
    	String OutputFileName = "";
    	switch (Mode) {
    	case TVideoRecorderModule.MODE_MPEG4: 
    		OutputFileName = MeasurementFolder+"/"+TMeasurementDescriptor.MediaMPEG4FileName;
    		break; //. >

    	case TVideoRecorderModule.MODE_3GP: 
    		OutputFileName = MeasurementFolder+"/"+TMeasurementDescriptor.Media3GPFileName;
    		break; //. >
    	}
		media.setOutputFile(OutputFileName);
		//.
		media.prepare();
	}
	
	@Override
	public void Finalize() throws Exception {
		super.Finalize();
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//. starting
		if (flAudio || flVideo)
			media.start();
	}
	
	@Override
	public void Stop() throws Exception {
		//. stopping
		if (flAudio || flVideo)
			media.stop();
		//.
		if (MeasurementID != null) {
			int AudioPackets = 0;
			if (flAudio)
				AudioPackets = -1;
			int VideoPackets = 0;
			if (flVideo)
				VideoPackets = -1;
			//.
			MeasurementDescriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
			MeasurementDescriptor.GeographServerObjectID = VideoRecorderModule.Device.idGeographServerObject;
			MeasurementDescriptor.GUID = TUIDGenerator.GenerateWithTimestamp();
			//. 
			MeasurementDescriptor.AudioPackets = AudioPackets;
			MeasurementDescriptor.VideoPackets = VideoPackets;
			//.
        	TSensorsModuleMeasurements.SetMeasurementDescriptor(MeasurementID, MeasurementDescriptor);
			//.
			MeasurementID = null;
		}
	}
	
	@Override
	public void StartTransmitting(long pidGeographServerObject) {
		flTransmitting = false;
	}
	
	@Override
	public void FinishTransmitting() {
		flTransmitting = false;
	}
	
	@Override
	public synchronized TMeasurementDescriptor GetMeasurementCurrentDescriptor() throws Exception {
		if (MeasurementDescriptor == null)
			return null; //. ->
		//.
		int AudioPackets = 0;
		if (flAudio)
			AudioPackets = -1;
		int VideoPackets = 0;
		if (flVideo)
			VideoPackets = -1;
		//.
		MeasurementDescriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//. 
		MeasurementDescriptor.AudioPackets = AudioPackets;
		MeasurementDescriptor.VideoPackets = VideoPackets;
		//.
		return MeasurementDescriptor;
	}
}
