package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.File;

import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.PacketTimeBase;

public class Camera {

	public static class TCameraMeasurementInfo {
		
		public String DatabaseFolder;
		public String MeasurementID;
		
		public TCameraMeasurementInfo(String pDatabaseFolder, String pMeasurementID) {
			DatabaseFolder = pDatabaseFolder;
			MeasurementID = pMeasurementID;
		}
		
		public boolean Equals(TCameraMeasurementInfo AnInfo) {
			if (AnInfo == null)
				return false; //. ->
			return (DatabaseFolder.equals(AnInfo.DatabaseFolder) && MeasurementID.equals(AnInfo.MeasurementID));
		}
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
	public String 								MeasurementID = null;
	protected String 							MeasurementFolder = "";
	protected 			TMeasurementDescriptor 	MeasurementDescriptor = null;
	
	public Camera(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}
	
	public void Destroy() throws Exception {
	}
	
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int asrc, int sps, int abr, int vsrc, int resX, int resY, int fps, int br, long UserID, String UserPassword, long pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview, double MaxMeasurementDuration) throws Exception {
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
		if (MeasurementID != null) {
			if (MeasurementDescriptor != null) {
				MeasurementDescriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
	        	TSensorsModuleMeasurements.SetMeasurementDescriptor(MeasurementID, MeasurementDescriptor);
			}
			else
				TSensorsModuleMeasurements.SetMeasurementStartTimestamp(MeasurementID);
		}
		//. set packet's timestamp base
		PacketTimeBase.Set();
	}
	
	public void Stop() throws Exception {
	}
	
	public void StartTransmitting(long pidGeographServerObject) {
	}
	
	public void FinishTransmitting() {
	}
	
	public synchronized TCameraMeasurementInfo GetMeasurementInfo() {
		return (new TCameraMeasurementInfo((new File(MeasurementFolder)).getParent(),MeasurementID));
	}
	
	public synchronized TMeasurementDescriptor GetMeasurementCurrentDescriptor() throws Exception {
		return null;
	}
	
	public void UpdateMeasurementCurrentDescriptor() throws Exception {
		//. Do not update measurement while recording it. It makes the measurement invisible for clients
		/* TMeasurementDescriptor Descriptor = GetMeasurementCurrentDescriptor();
		if (Descriptor != null) 
			TSensorsModuleMeasurements.SetMeasurementDescriptor(Descriptor.ID, Descriptor); */
	}
}
