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

import java.net.InetAddress;

import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.AMRNBPacketizerGSPS;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp.H263PacketizerGSPS;

/*
 * 
 * Instantiates two MediaStreamer objects, one for audio streaming and the other for video streaming
 * then it uses the H263Packetizer and the AMRNBPacketizer to generate two RTP streams
 * 
 */

public class CameraStreamerH263 extends Camera {

    public static final String LOG_TAG = "SPYDROID";
    
	private MediaStreamer sound = null;
	private MediaStreamer video = null;
	private AMRNBPacketizerGSPS sstream = null;
	private H263PacketizerGSPS vstream = null;
	
	public CameraStreamerH263(TVideoRecorderModule pVideoRecorderModule) {
		super(pVideoRecorderModule);
		//.
		sound = new MediaStreamer();
		video = new MediaStreamer();
		//.
		SetCurrentCamera(this);
	}
	
	@Override
	public void Destroy() throws Exception {
		SetCurrentCamera(null);
		//.
		Finalize();
		//.
		if (video != null) {
			video.Destroy();
			video = null;
		}
		if (sound != null) {
			sound.Destroy();
			sound = null;
		}
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
				MeasurementID = TVideoRecorderMeasurements.CreateNewMeasurementID();
				//.
				TVideoRecorderMeasurements.CreateNewMeasurement(MeasurementID,TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1); 
				MeasurementFolder = TVideoRecorderMeasurements.DataBaseFolder+"/"+MeasurementID;
			}
			else { 
				MeasurementID = null;
				MeasurementFolder = "";
			}
		}
		// AUDIO
		sound.reset();
		AMRNBPacketizerGSPS _sstream = null;
		if (flAudio) {
			try {
				sound.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				sound.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
	    		if (sps > 0)
	    			sound.setAudioSamplingRate(sps);
	    		if (abr > 0)
	    			sound.setAudioEncodingBitRate(abr);
				sound.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				sound.setAudioChannels(1);
				sound.prepare();
				boolean _flTransmitting = (flTransmitting && (pidGeographServerObject != 0));
				//.
				String OutputFileName = null;
				if (flSaving)
					OutputFileName = MeasurementFolder+"/"+TMeasurementDescriptor.AudioFileName;
				_sstream = new AMRNBPacketizerGSPS(sound.getInputStream(), _flTransmitting, InetAddress.getByName(ip),audio_port, (int)UserID,UserPassword, (int)pidGeographServerObject, OutputFileName);
			} catch (Exception E) {
				flAudio = false;
				//.
				throw new AudioSetupError(); //. =>
			}
		}
		// VIDEO
		video.reset();
		H263PacketizerGSPS _vstream = null;
		if (flVideo) {
			try {
				video.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
				video.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				video.setVideoFrameRate(fps);
				video.setVideoSize(resX, resY);
				if (br > 0)
					video.setVideoEncodingBitRate(br);
				video.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
				//.
				video.setPreviewDisplay(holder.getSurface());
				video.prepare();
				boolean _flTransmitting = (flTransmitting && (pidGeographServerObject != 0));
				//.
				String OutputFileName = null;
				if (flSaving)
					OutputFileName = MeasurementFolder+"/"+TMeasurementDescriptor.VideoFileName;
				//.
				_vstream = new H263PacketizerGSPS(video.getInputStream(), _flTransmitting, InetAddress.getByName(ip),video_port, (int)UserID,UserPassword, (int)pidGeographServerObject, OutputFileName);
			} catch (Exception E) {
				flVideo = false;
				//.
				throw new VideoSetupError(); //. =>
			}
		}
		//.
		synchronized (this) {
			sstream = _sstream;
			vstream = _vstream;
		}
	}
	
	@Override
	public void Finalize() throws Exception {
		super.Finalize();
		//.
		synchronized (this) {
			if (vstream != null) {
				vstream.Destroy();
				vstream = null;
			}
			if (sstream != null) {
				sstream.Destroy();
				sstream = null;
			}
		}
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		// Start sound streaming
		if (flAudio) 
			sound.start();
		if (sstream != null)
			sstream.startStreaming();
		// Start video streaming
		if (flVideo)
			video.start();
		if (vstream != null)
			vstream.startStreaming();
	}
	
	@Override
	public void Stop() throws Exception {
		// Stop sound streaming
		if (sstream != null)
			sstream.stopStreaming();
		if (flAudio) 
			sound.stop();
		// Stop video streaming
		if (vstream != null)
			vstream.stopStreaming();
		if (flVideo)
			video.stop();
		//.
		if (MeasurementID != null) {
			int AudioPackets = 0;
			if (sstream != null)
				AudioPackets = sstream.Output.GetOutputFilePackets();
			int VideoPackets = 0;
			if (vstream != null)
				VideoPackets = vstream.Output.GetOutputFilePackets();
			//.
			TVideoRecorderMeasurements.SetMeasurementFinish(MeasurementID,AudioPackets,VideoPackets);
			//.
			MeasurementID = null;
		}
	}
	
	@Override
	public void StartTransmitting(long pidGeographServerObject) {
		if (sstream != null)
			sstream.StartTransmitting((int)pidGeographServerObject);
		//.
		if (vstream != null)
			vstream.StartTransmitting((int)pidGeographServerObject);
		//.
		flTransmitting = true;
	}
	
	@Override
	public void FinishTransmitting() {
		if (sstream != null)
			sstream.FinishTransmitting();
		//.
		if (vstream != null)
			vstream.FinishTransmitting();
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
		if (sstream != null)
			Result.AudioPackets = sstream.Output.GetOutputFilePackets();
		Result.VideoPackets = 0;
		if (vstream != null)
			Result.VideoPackets = vstream.Output.GetOutputFilePackets();
		Result.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		//.
		return Result;
	}
}
