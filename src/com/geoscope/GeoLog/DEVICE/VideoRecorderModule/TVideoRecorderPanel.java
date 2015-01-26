package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorder.IVideoRecorderPanel;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TVideoRecorderPanel extends Activity implements IVideoRecorderPanel {

	public static boolean flStarting = false;
    public static boolean flHidden = false;
    
	private static TVideoRecorderPanel _VideoRecorderPanel = null;
	
	public static synchronized TVideoRecorderPanel GetVideoRecorderPanel() {
		return _VideoRecorderPanel;
	}
	
	public static synchronized void SetVideoRecorderPanel(TVideoRecorderPanel Panel) {
		_VideoRecorderPanel = Panel;
	}
	
	public static synchronized void ClearVideoRecorderPanel(TVideoRecorderPanel Panel) {
		if (_VideoRecorderPanel == Panel)
			_VideoRecorderPanel = null;
	}
	
	public static synchronized boolean IsVideoRecorderPanelNull() {
		return (_VideoRecorderPanel == null);
	}
	
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Surface = holder;
			DoOnSurfaceIsChanged(holder, format,width,height);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Surface = holder;
			DoOnSurfaceIsCreated(holder);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Surface = null;
			DoOnSurfaceIsDestroyed(holder);
		}
	}
	
	public class TVideoRecorderSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			VideoRecorder.FinalizeRecorder();
			VideoRecorder.camera_Surface_Clear(holder);
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (VideoRecorder.camera_Surface_Get() != holder) {
				VideoRecorder.FinalizeRecorder();
				//.
				VideoRecorder.camera_Surface_Set(holder);
				VideoRecorder.InitializeRecorder();
			}
		}
	}
	
	protected boolean							SurfaceIsVisible = false;
	protected FrameLayout						SurfaceLayout;
	protected SurfaceView 						svSurface;
	protected SurfaceHolder 					Surface = null;
	private TSurfaceHolderCallbackHandler		Surface_HolderCallbackHandler;
	protected TextView 							lbStatus;
    //.
	private TVideoRecorder								VideoRecorder;
	private	FrameLayout									VideoRecorder_SurfaceLayout;
	private SurfaceView 								VideoRecorder_svSurface;
	private TVideoRecorderSurfaceHolderCallbackHandler	VideoRecorder_Surface_HolderCallbackHandler;
	private TextView 									VideoRecorder_lbStatus;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	flStarting = true;
    	try {
            super.onCreate(savedInstanceState);
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    		//.
            setContentView(R.layout.video_recorder_panel);
            //.
            SurfaceLayout = (FrameLayout)findViewById(R.id.VideoRecorderPanelSurfaceLayout);
        	svSurface = (SurfaceView)findViewById(R.id.VideoRecorderPanelSurface);
            lbStatus = (TextView)findViewById(R.id.lbVideoRecorderPanelSurface);
    		//.
    		Surface_HolderCallbackHandler = new TSurfaceHolderCallbackHandler();
            //.
            VideoRecorder_SurfaceLayout = (FrameLayout)findViewById(R.id.VideoRecorderPanelCameraSurfaceLayout);
        	VideoRecorder_svSurface = (SurfaceView)findViewById(R.id.VideoRecorderPanelCameraSurface);
            VideoRecorder_lbStatus = (TextView)findViewById(R.id.lbVideoRecorderPanelCameraStatus);
    		//.
            try {
        		TTracker Tracker = TTracker.GetTracker();
        		if (Tracker == null)
        			throw new Exception("Tracker is null"); //. =>
        		VideoRecorder = new TVideoRecorder(this, Tracker.GeoLog.VideoRecorderModule, VideoRecorder_lbStatus, flHidden);
            }
            catch (Exception E) {
            	Toast.makeText(this, getString(R.string.SVideoRecorderInitializationError)+E.getMessage(), Toast.LENGTH_LONG).show();
            	//.
            	finish();
            	//.
            	return; //. ->
            }
            //.
    		VideoRecorder_Surface_HolderCallbackHandler = new TVideoRecorderSurfaceHolderCallbackHandler();
    		//.
    		SetVideoRecorderPanel(TVideoRecorderPanel.this);
        	//.
            /*///- if (!flHidden)
            	Toast.makeText(this, getString(R.string.SVideoRegistratorIsOn), Toast.LENGTH_LONG).show();*/
    		SetSurface(false);
    	}
    	finally {
    		flStarting = false;
    	}
    }
	
    @Override
    public void onDestroy() {
    	ClearVideoRecorderPanel(this);
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
		super.onStop();
	}

	@SuppressWarnings("deprecation")
	public void onStart() {
    	super.onStart();
		//.
		SurfaceHolder sh;
		//.
    	sh = svSurface.getHolder();
    	sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	sh.addCallback(Surface_HolderCallbackHandler);
    	//.
    	sh = VideoRecorder_svSurface.getHolder();
    	sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	sh.addCallback(VideoRecorder_Surface_HolderCallbackHandler);
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
    
    @Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SConfirmation)
        .setMessage(R.string.SFinishRecording)
	    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null)
					Tracker.GeoLog.VideoRecorderModule.CancelRecording();
				//.
	    		TVideoRecorderPanel.this.finish();
	    	}
	    })
	    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	    	}
	    })
	    .show();
	}	
	
	public void DoOnSurfaceIsCreated(SurfaceHolder SH) {
	}
	
	public void DoOnSurfaceIsChanged(SurfaceHolder SH, int Format, int Width, int Height) {
	}
	
	public void DoOnSurfaceIsDestroyed(SurfaceHolder SH) {
	}
	
    public void SetSurface(boolean flVisible) {
    	if (flVisible) {
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(256,256);
    		VideoRecorder_SurfaceLayout.setLayoutParams(params);    	
    		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    		SurfaceLayout.setLayoutParams(params);
    		SurfaceLayout.setVisibility(View.VISIBLE);
    	}
    	else {
    		SurfaceLayout.setVisibility(View.GONE);
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    		VideoRecorder_SurfaceLayout.setLayoutParams(params);    	
    	}
    	SurfaceIsVisible = flVisible;
    	//.
    	VideoRecorder.Status_SetVisibility(!SurfaceIsVisible);
    }

    @Override
	public boolean RestartRecording(TReceiverDescriptor RD, short pMode, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo) {
    	if (flHidden) {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);	
    		VideoRecorder_lbStatus.setVisibility(View.GONE);
    		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(1,1);
    		VideoRecorder_svSurface.setLayoutParams(params);    	
    	}
    	else {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);	
    		android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT,android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
    		VideoRecorder_svSurface.setLayoutParams(params);    	
    		VideoRecorder_lbStatus.setVisibility(View.VISIBLE);
    	}
    	//.
		return VideoRecorder.RestartRecording(RD, pMode, pflTransmitting, pflSaving, pflAudio, pflVideo);
	}

    @Override
	public void StopRecording() {
		VideoRecorder.StopRecording();
	}
	
    @Override
	public boolean IsRecording() {
		return VideoRecorder.IsRecording();
	}
	
    @Override
	public short GetMode() {
		return VideoRecorder.Mode;
	}
	
    @Override
	public boolean IsAudio() {
		return VideoRecorder.flAudio;
	}
	
    @Override
	public boolean IsVideo() {
		return VideoRecorder.flVideo;
	}
	
    @Override
	public boolean IsTransmitting() {
		return VideoRecorder.flTransmitting;
	}
	
    @Override
	public boolean IsSaving() {
		return VideoRecorder.flSaving;
	}
	
    @Override
	public void StartTransmitting(int pidGeographServerObject) {
		VideoRecorder.StartTransmitting(pidGeographServerObject);
	}
	
    @Override
	public void FinishTransmitting() {
		VideoRecorder.FinishTransmitting();
	}
	
    @Override
	public TMeasurementDescriptor Recording_GetMeasurementDescriptor() throws Exception {
		return Camera.CurrentCamera_GetMeasurementDescriptor();
	}
}
