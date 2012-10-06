/*
 * Copyright (C) 2011-2012 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid;

import java.io.IOException;

import android.view.SurfaceHolder;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
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
	
	public boolean flAudio = true;
	public boolean flVideo = true;
	public boolean flTransmitting = false;
	public boolean flSaving = true;
	public String MeasurementID = null;
	
	public Camera() {
	}
	
	public void Destroy() throws Exception {
	}
	
	public void Setup(SurfaceHolder holder, String ip, int audio_port, int video_port, int Mode, int sps, int abr, int resX, int resY, int fps, int br, int UserID, String UserPassword, int pidGeographServerObject, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, double MaxMeasurementDuration) throws IOException, AudioSetupError, VideoSetupError {
	}
	
	public void Finalize() throws Exception {
		synchronized (this) {
			if (MeasurementID != null) {
				MeasurementID = null;
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
