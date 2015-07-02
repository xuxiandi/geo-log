package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

@SuppressLint("HandlerLeak")
public class TMeasurementProcessorPanel extends Activity {
    
	@SuppressWarnings("unused")
	private boolean flExists = false;
	//.
	@SuppressWarnings("unused")
	private boolean IsInFront = false;
	//.
	private String 				MeasurementDatabaseFolder;
	private String 				MeasurementID;
	private String 				MeasurementDataFile = null;
	private TSensorMeasurement 	Measurement = null;
	private TAsyncProcessing 	MeasurementPreprocessing = null; 
	private double 				MeasurementStartPosition = 0.0;
	//.
	private TMeasurementProcessor 	Processor = null;
	private LinearLayout 			ProcessorLayout;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        try {
            Bundle extras = getIntent().getExtras(); 
        	MeasurementDatabaseFolder = extras.getString("MeasurementDatabaseFolder");
        	MeasurementID = extras.getString("MeasurementID");
        	MeasurementDataFile = extras.getString("MeasurementDataFile");
        	MeasurementStartPosition = extras.getDouble("MeasurementStartPosition");
        	//.
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		//.
            setContentView(R.layout.measurement_processor_panel);
            //.
            ProcessorLayout = (LinearLayout)findViewById(R.id.ProcessorLayout);
            //
    		MeasurementPreprocessing = new TAsyncProcessing(this) {

    			private TSensorMeasurement _Measurement = null;
    			
    			@Override
    			public void Process() throws Exception {
    	        	if (MeasurementDataFile == null)
    	        		_Measurement = new TSensorMeasurement(MeasurementDatabaseFolder, MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
    	        	else
    	        		_Measurement = TSensorMeasurement.FromDataFile(MeasurementDataFile, MeasurementDatabaseFolder, MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance, Canceller);
    			}

    			@Override
    			public void DoOnCompleted() throws Exception {
    				if (Canceller.flCancel)
    					return; //. ->
    	            //.
    				Measurement = _Measurement;
    				//.
    	            Processor = TMeasurementProcessor.GetProcessor(Measurement.Descriptor);
    	            if (Processor == null)
    	            	throw new Exception("there is no handler for a type: "+Measurement.Descriptor.TypeID()); //. ->
    	            //.
    	            Processor.flStandalone = true;
    	            //.
    	            Processor.SetLayout(TMeasurementProcessorPanel.this, ProcessorLayout);
    	            //.
    	            Processor.Start();
    	            //.
    	            Processor.Setup(Measurement);
    	            //.
    	            Processor.SetPosition(MeasurementStartPosition, 100, false);
    			}
    			
    			@Override
    			public void DoOnCancelIsOccured() {
    				TMeasurementProcessorPanel.this.finish();
    			}
    			
    			@Override
    			public void DoOnException(Exception E) {
    				String S = E.getMessage();
    				if (S == null)
    					S = E.getClass().getName();
    				Toast.makeText(TMeasurementProcessorPanel.this, S, Toast.LENGTH_LONG).show();
    				//.
    				finish();
    			}
    		};
    		MeasurementPreprocessing.Start();
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
    		if (MeasurementPreprocessing != null) {
    			MeasurementPreprocessing.Cancel();
    			MeasurementPreprocessing = null;
    		}
    		if (Processor != null) {
    			Processor.Destroy();
    			Processor = null;
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
		//.
		if (Processor != null)
			Processor.Resume();
		//.
		IsInFront = true;
	}

    @Override
	protected void onPause() {
		IsInFront = false;
		//.
		if (Processor != null)
			Processor.Pause();
		//.
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onStart() {
    	super.onStart();
    	//.
		if (Processor != null)
			try {
				Processor.Start();
			} catch (Exception E) {
			}
    }
	
	@Override
	protected void onStop() {
		if (Processor != null)
			try {
				Processor.Stop();
			} catch (Exception E) {
			}
		//.
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		DoOnExit();
		//.
		super.onBackPressed();
	}
	
	private void DoOnExit() {
		if ((Processor != null) && Processor.flInitialized) {
			double MeasurementCurrentPosition = Processor.Measurement.Descriptor.StartTimestamp+Processor.GetPosition();
	    	Intent intent = getIntent();
	    	intent.putExtra("MeasurementCurrentPosition",MeasurementCurrentPosition);
	        //.
	    	setResult(RESULT_OK,intent);
		}
	}
}
