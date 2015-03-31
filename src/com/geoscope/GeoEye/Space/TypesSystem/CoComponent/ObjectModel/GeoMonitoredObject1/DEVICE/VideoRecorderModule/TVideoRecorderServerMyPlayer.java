package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerMyPlayer extends Activity {
    
	@SuppressWarnings("unused")
	private boolean flExists = false;
	//.
	@SuppressWarnings("unused")
	private boolean IsInFront = false;
	//.
	private double MeasurementStartPosition = 0.0;
	private String MeasurementDatabaseFolder = null;
	private String MeasurementID = null;
	//.
	private LinearLayout ProcessorLayout;
	//.
	private TVideoRecorderServerMyPlayerComponent ProcessorComponent = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	MeasurementDatabaseFolder = extras.getString("MeasurementDatabaseFolder");
        	MeasurementID = extras.getString("MeasurementID");
        	MeasurementStartPosition = extras.getDouble("MeasurementStartPosition");
        }
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//.
        setContentView(R.layout.video_recorder_server_myplayer);
        //.
        ProcessorLayout = (LinearLayout)findViewById(R.id.VideoRecorderServerMyPlayerLayout);
        //.
        try {
			ProcessorComponent = new TVideoRecorderServerMyPlayerComponent();
			ProcessorComponent.OnVideoSurfaceChangedHandler = new TVideoRecorderServerMyPlayerComponent.TOnSurfaceChangedHandler() {
				
				@Override
				public void DoOnSurfaceChanged(SurfaceHolder surface) {
					try {
						Setup();
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
						Toast.makeText(TVideoRecorderServerMyPlayer.this, S, Toast.LENGTH_LONG).show();
					}
				}
			};
			ProcessorComponent.OnProgressHandler = new TVideoRecorderServerMyPlayerComponent.TOnProgressHandler() {
				
				@Override
				public void DoOnProgress(double ProgressFactor) {
					if (ProcessorComponent != null) {
						MeasurementStartPosition = ProcessorComponent.Measurement.Descriptor.Duration()*ProgressFactor; 
					}
				}
			};
			ProcessorComponent.SetLayout(this, ProcessorLayout);
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, S, Toast.LENGTH_LONG).show();
			//.
			finish();
			//.
			return; //. ->
		}
		//.
		setResult(RESULT_CANCELED);
		//.
		flExists = true;
    }
	
    public void onDestroy() {
    	flExists = false;
    	//.
    	try {
    		if (ProcessorComponent != null) {
    			ProcessorComponent.Destroy();
    			ProcessorComponent = null;
    		}
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, S, Toast.LENGTH_LONG).show();
		}
    	//.
		super.onDestroy();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		IsInFront = true;
	}

    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onStart() {
    	super.onStart();
    }
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		DoOnExit();
		//.
		super.onBackPressed();
	}
	
	private void Setup() throws Exception {
		ProcessorComponent.Setup(new TSensorMeasurement(MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance));
		//.
		ProcessorComponent.SetPosition(MeasurementStartPosition, 0/*Delay, ms*/, false);
	}
	
	private void DoOnExit() {
		if ((ProcessorComponent != null) && ProcessorComponent.flInitialized) {
			double MeasurementCurrentPosition = ProcessorComponent.Measurement.Descriptor.StartTimestamp+ProcessorComponent.Measurement.Descriptor.Duration()*ProcessorComponent.MeasurementCurrentPositionFactor;
	    	Intent intent = getIntent();
	    	intent.putExtra("MeasurementCurrentPosition",MeasurementCurrentPosition);
	        //.
	    	setResult(RESULT_OK,intent);
		}
	}
}
