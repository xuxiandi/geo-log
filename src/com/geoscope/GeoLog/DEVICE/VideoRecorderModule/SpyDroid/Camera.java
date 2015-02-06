package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import android.view.SurfaceHolder;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.PacketTimeBase;

public class Camera {

    private static Camera CurrentCamera = null;

	public static synchronized void SetCurrentCamera(Camera CS) {
		CurrentCamera = CS;
	}
	
	public static synchronized boolean CurrentCamera_IsSaving() {
		if (CurrentCamera == null)
			return false; //. ->
		return CurrentCamera.flSaving; 
	}	
		
	public static synchronized TMeasurementDescriptor CurrentCamera_GetMeasurementDescriptor() throws Exception {
		if (CurrentCamera == null)
			return null; //. ->
		return CurrentCamera.GetMeasurementCurrentDescriptor(); 
	}	
		
	public static synchronized void CurrentCamera_FlashMeasurement() throws Exception {
		if (CurrentCamera == null)
			return; //. ->
		CurrentCamera.UpdateMeasurementCurrentDescriptor(); 
	}	
	
	public static class AudioSetupError extends Exception {

		private static final long serialVersionUID = 1L;

		public AudioSetupError() {}
	}
	
	public static class VideoSetupError extends Exception {

		private static final long serialVersionUID = 1L;

		public VideoSetupError() {}
	}
	
	protected TVideoRecorderModule VideoRecorderModule;
	//.
	public boolean flAudio = true;
	public boolean flVideo = true;
	public boolean flTransmitting = false;
	public boolean flSaving = true;
	//.
	public String 		MeasurementID = null;
	protected String 	MeasurementFolder = "";
	
	public Camera(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}
	
	public void Destroy() throws Exception {
	}
	
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, int UserID, String UserPassword, int pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, double MaxMeasurementDuration) throws Exception {
	}
	
	public void Finalize() throws Exception {
		synchronized (this) {
			if (MeasurementID != null) {
				MeasurementID = null;
				MeasurementFolder = "";
			}
		}
	}
	
	public void Start() throws Exception {
		if (MeasurementID != null)
			TVideoRecorderMeasurements.SetMeasurementStartTimestamp(MeasurementID);
		//. set packet's timestamp base
		PacketTimeBase.Set();
	}
	
	public void Stop() throws Exception {
	}
	
	public void StartTransmitting(int pidGeographServerObject) {
	}
	
	public void FinishTransmitting() {
	}
	
	public synchronized TMeasurementDescriptor GetMeasurementCurrentDescriptor() throws Exception {
		return null;
	}
	
	public void UpdateMeasurementCurrentDescriptor() throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementCurrentDescriptor();
		if (Descriptor != null) 
			TVideoRecorderMeasurements.SetMeasurementDescriptor(Descriptor.ID, Descriptor);
	}
}
