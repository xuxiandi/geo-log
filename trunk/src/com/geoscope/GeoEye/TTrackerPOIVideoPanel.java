package com.geoscope.GeoEye;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("DefaultLocale")
public class TTrackerPOIVideoPanel extends Activity implements OnClickListener, SurfaceHolder.Callback {

	public static final int Video_MaxDuration = 1000*3600/*seconds*/;
	public static final int Video_MaxFileSize = 1024*1024*500/*megabytes*/;
	
	private MediaRecorder recorder;     
    private SurfaceHolder holder;     
    private boolean recording = false;
    private String VideoFileName;
    
    @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
        requestWindowFeature(Window.FEATURE_NO_TITLE);     
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);     
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);      
        recorder = new MediaRecorder();     
    	try {         
            InitRecorder();     
    	} catch (Exception E) {         
    		Toast.makeText(this, R.string.SErrorOfInitializingVideorecorder, Toast.LENGTH_LONG).show();
    		finish();
    		return; //. ->
    	} 
        setContentView(R.layout.tracker_poi_video_panel);
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.TrackerPOIVideoPanelSurfaceView);     
        holder = cameraView.getHolder();     
        holder.addCallback(this);     
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);      
        cameraView.setClickable(true);     
        cameraView.setOnClickListener(this);
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
		Toast.makeText(this, R.string.SClickToScreenForVideorecording, Toast.LENGTH_LONG).show();
	}
    
	private void InitRecorder() throws Exception {
    	TTracker Tracker = TTracker.GetTracker(TTrackerPOIVideoPanel.this.getApplicationContext());
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
        //. prepare for output file
        String FN = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Temp";
		File TempFolder = new File(FN);
		TempFolder.mkdirs();
		FN = FN+"/"+"DataFile."+Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Format;
		VideoFileName = FN;
		//.
    	recorder.reset();
    	recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    	//.
    	if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Format.toUpperCase().equals("MP4"))
    		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    	else
    		if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Format.toUpperCase().equals("3GP"))
        		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	//.
		if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Audio_SampleRate > 0)
			recorder.setAudioSamplingRate(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Audio_SampleRate);
		if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Audio_BitRate > 0)
			recorder.setAudioEncodingBitRate(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Audio_BitRate);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);    	             
    	recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    	if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_FrameRate > 0)
    		recorder.setVideoFrameRate(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_FrameRate);             
		if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_BitRate > 0)
			recorder.setVideoEncodingBitRate(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_BitRate);
    	recorder.setVideoSize(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_ResX,Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_Video_ResY); 
    	if (Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_MaxDuration > 0)
    		recorder.setMaxDuration(Tracker.GeoLog.GPSModule.MapPOIConfiguration.MediaFragment_MaxDuration);      
    	recorder.setMaxFileSize(Video_MaxFileSize);  
    	recorder.setOutputFile(VideoFileName);
    }  
    
    private void PrepareRecorder() {     
    	recorder.setPreviewDisplay(holder.getSurface());
    	try {         
    		recorder.prepare();     
    	} catch (Exception E) {         
    		Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	} 
    }  
    
    public void onClick(View v) {     
    	if (recording) {         
    		recorder.stop();         
    		recording = false;          
    		//.          
    		///? initRecorder();          
    		///? prepareRecorder();     
    		//.
        	Intent intent = TTrackerPOIVideoPanel.this.getIntent();
        	intent.putExtra("FileName",VideoFileName);
            //.
        	setResult(Activity.RESULT_OK,intent);
    		//.
        	finish();
    	} 
    	else {         
    		recording = true;         
    		recorder.start();         
    	} 
    }  

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { 
    	PrepareRecorder(); 
    }  
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {     
    }  
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {     
    	if (recording) {         
    		recorder.stop();         
    		recording = false;     
    	}     
    	recorder.release();     
    	finish(); 
    }
}
