/*
 * Copyright (C) 2011 GUIGUI Simon, fyhertz@gmail.com, modified by PAV
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

package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraRegistrator;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerH263;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerH264;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TVideoRecorderPanel extends Activity {

	public static TVideoRecorderPanel VideoRecorderPanel = null;
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Surface = holder;
			//.
			InitializeRecorder();
			//.
			wl.acquire();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			wl.release();
			//.
			FinalizeRecorder();
		}
	}
	
    private Camera     				camera;
    private boolean 				camera_flStarted;
    //.
    public short 				Mode;
    public boolean 				flAudio = false;
    public boolean 				flVideo = false;
    public boolean 				flTransmitting = false;
    public boolean 				flSaving = false;
    
	private TextView lbVideoRecorderStatus;
	private TSurfaceHolderCallbackHandler 		SurfaceHolderCallbackHandler;
	private SurfaceHolder Surface = null;
	private PowerManager.WakeLock wl;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        setContentView(R.layout.video_recorder_panel);
        lbVideoRecorderStatus = (TextView)findViewById(R.id.lbVideoRecorderStatus);
    	//.
    	Toast.makeText(this, getString(R.string.SVideoRegistratorIsOn), Toast.LENGTH_LONG).show();
		//.
		System.gc();
		//.
		camera_flStarted = false;
		camera = null;
		//.
		SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
		//.
		VideoRecorderPanel = TVideoRecorderPanel.this;
    }
	
    public void onDestroy() {
    	if (VideoRecorderPanel == this)
    		VideoRecorderPanel = null;
    	//.
    	if (camera != null) {
    		try {
    			camera.Destroy();
    		}
    		catch (Exception E) {
            	Toast.makeText(this, getString(R.string.SVideoRegistratorFinalizationError)+E.getMessage(), Toast.LENGTH_LONG).show();
    		}
    		camera = null;
    	}
		camera_flStarted = false;
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		FinalizeRecorder();
		//.
		super.onStop();
	}

	public void onStart() {
    	super.onStart();
    	//.
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SpydroidWakeLock");
		//.
		SurfaceView cv;
		SurfaceHolder sh;
    	cv = (SurfaceView) findViewById(R.id.VideoRecorderPanelSurfaceView);
    	sh = cv.getHolder();
    	sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	sh.addCallback(SurfaceHolderCallbackHandler);
		//.
		UpdateStatus();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_recorder_panel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.miCloseVideoRecorder:
        	finish();
            return true; //. >
            
        case R.id.miCancelVideoRecording:
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker != null)
				Tracker.GeoLog.VideoRecorderModule.CancelRecording();
            return true; //. >
    	}
    
        return false;
    }

	public boolean RestartRecording(TReceiverDescriptor RD, short pMode, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo) {
		boolean Result = false;
		if (camera_flStarted)
			StopRecording();
		try {
			if (Surface == null)
				throw new Exception("surface is empty"); //. =>
			//.
			String Address = "";
			int AudioPort = 0;
			int VideoPort = 0;
			if (RD != null) {
				if (RD.ReceiverType != TReceiverDescriptor.RECEIVER_NATIVE)
					throw new Exception("unknown receiver."); //. =>
				Address = RD.Address;
				AudioPort = RD.AudioPort;
				VideoPort = RD.VideoPort;
			}
			//.
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker == null)
				throw new Exception("Tracker is null"); //. =>
			TVideoRecorderModule VRM = Tracker.GeoLog.VideoRecorderModule;
			//.
			Mode = pMode;
			flAudio = pflAudio;
			flVideo = pflVideo;
			flTransmitting = (pflTransmitting && (Tracker.GeoLog.idGeographServerObject != 0));
			flSaving = pflSaving;
			//.
			try {
				switch (Mode) {
				
				case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
					camera = new CameraStreamerH263();
					break; //. >
					
				case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
					camera = new CameraStreamerH264();
					break; //. >
					
				case TVideoRecorderModule.MODE_MPEG4:
				case TVideoRecorderModule.MODE_3GP:
					camera = new CameraRegistrator();
					break; //. >
					
				default:
					throw new Exception("Unknown camera mode, Mode: "+Short.toString(Mode)); //. =>
				}
				//.
				camera.Setup(Surface, Address, AudioPort,VideoPort, Mode, VRM.CameraConfiguration.Camera_Audio_SampleRate,VRM.CameraConfiguration.Camera_Audio_BitRate, VRM.CameraConfiguration.Camera_Video_ResX,VRM.CameraConfiguration.Camera_Video_ResY,VRM.CameraConfiguration.Camera_Video_FrameRate,VRM.CameraConfiguration.Camera_Video_BitRate, Tracker.GeoLog.UserID,Tracker.GeoLog.UserPassword, Tracker.GeoLog.idGeographServerObject, flTransmitting, flSaving, flAudio,flVideo, VRM.MeasurementConfiguration.MaxDuration);
			}
			catch (Camera.AudioSetupError AE) {
				Tracker.GeoLog.VideoRecorderModule.SetAudio(false);
				Tracker.GeoLog.VideoRecorderModule.PostUpdateRecorderState();
			}
			catch (Camera.VideoSetupError VE) {
				Tracker.GeoLog.VideoRecorderModule.SetVideo(false);
				Tracker.GeoLog.VideoRecorderModule.PostUpdateRecorderState();
			}
			// Streaming starts as soon as the surface for the MediaRecorder is created
			camera.Start();
			//.
			Result = true;
		} 
		catch (Exception E) {
        	Toast.makeText(this, getString(R.string.SVideoRecorderInitializationError)+E.getMessage(), Toast.LENGTH_LONG).show();
        	return Result; //. ->
		}
		//.
		camera_flStarted = true;
		UpdateStatus();
		//.
		return Result;
	}
	
	public void StopRecording() {
		if (!camera_flStarted)
			return; //. ->
		//.
		try {
			camera.Stop();
			camera.Finalize();
			//.
    		camera.Destroy();
    		camera = null;
		}
		catch (Exception E) {
        	Toast.makeText(this, R.string.SVideoRegistratorFinalizationError+E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		camera_flStarted = false;
		UpdateStatus();
		//.
		System.gc();
	}
	
	public boolean IsRecording() {
		return camera_flStarted;
	}
	
	public void StartTransmitting(int pidGeographServerObject) {
		if (camera != null)
			camera.StartTransmitting(pidGeographServerObject);
		//.
		flTransmitting = true;
	}
	
	public void FinishTransmitting() {
		if (camera != null)
			camera.FinishTransmitting();
		//.
		flTransmitting = false;
	}
	
	public TMeasurementDescriptor Recording_GetMeasurementDescriptor() throws Exception {
		return Camera.CurrentCamera_GetMeasurementDescriptor();
	}
	
	private void InitializeRecorder() {
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null)
			return; //. ->
		//. set status of VideoRecorder as active
		/*///? Tracker.GeoLog.VideoRecorderModule.Activate();*/
		//.
		if (Tracker.GeoLog.VideoRecorderModule.Recording.BooleanValue()) {
			TReceiverDescriptor RD = Tracker.GeoLog.VideoRecorderModule.GetReceiverDescriptor();
			if (RD != null) 
    			RestartRecording(RD, Tracker.GeoLog.VideoRecorderModule.Mode.GetValue(), Tracker.GeoLog.VideoRecorderModule.Transmitting.BooleanValue(),Tracker.GeoLog.VideoRecorderModule.Saving.BooleanValue(), Tracker.GeoLog.VideoRecorderModule.Audio.BooleanValue(),Tracker.GeoLog.VideoRecorderModule.Video.BooleanValue());
		}
		else {
			if (IsRecording()) 
				StopRecording();
			Toast.makeText(TVideoRecorderPanel.this, getString(R.string.SRecordingIsStopped), Toast.LENGTH_LONG).show();
		}
	}
	
	private void FinalizeRecorder() {
		//. set status of VideoRecorder as inactive
		/*///? TTracker Tracker = TTracker.GetTracker();
		Tracker.GeoLog.VideoRecorderModule.DeActivate();*/
		//.
		if (IsRecording())
			StopRecording();
	}
	
	private void UpdateStatus() {
		if (camera_flStarted) {
			String S = getString(R.string.SCameraIsOn);
			//.
			S = S+getString(R.string.SCameraMode);
			switch (Mode) {
			
			case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
				S = S+getString(R.string.SCameraStreamH263);
				break; //. >
				
			case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
				S = S+getString(R.string.SCameraStreamH264);
				break; //. >
				
			case TVideoRecorderModule.MODE_MPEG4:
				S = S+getString(R.string.SCameraMPEG4);
				break; //. >
				
			case TVideoRecorderModule.MODE_3GP:
				S = S+getString(R.string.SCamera3GP);
				break; //. >
				
			default:
				S = S+"?";
				break; //. >
			}
			if (flAudio) 
				S = S+getString(R.string.SCameraAudioChannel);
			if (flVideo) 
				S = S+getString(R.string.SCameraVideoChannel);
			if (flTransmitting) 
				S = S+getString(R.string.SCameraTransmitting);
			if (flSaving) 
				S = S+getString(R.string.SCameraSaving);
			lbVideoRecorderStatus.setText(S);
		}
		else {
			lbVideoRecorderStatus.setText(R.string.SCameraOff);
		}
	}
}
