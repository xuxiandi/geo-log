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
        	String MeasurementDatabaseFolder = extras.getString("MeasurementDatabaseFolder");
        	String MeasurementID = extras.getString("MeasurementID");
			Measurement = new TSensorMeasurement(MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
        	MeasurementStartPosition = extras.getDouble("MeasurementStartPosition");
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		//.
            setContentView(R.layout.measurement_processor_panel);
            //.
            ProcessorLayout = (LinearLayout)findViewById(R.id.ProcessorLayout);
            //
    		MeasurementPreprocessing = new TAsyncProcessing(this) {

    			@Override
    			public void Process() throws Exception {
    				Measurement.Descriptor.Model.Process(Canceller);
    			}

    			@Override
    			public void DoOnCompleted() throws Exception {
    				if (Canceller.flCancel)
    					return; //. ->
    	            //.
    	            Processor = TMeasurementProcessor.GetProcessor(Measurement.Descriptor);
    	            if (Processor == null)
    	            	throw new Exception("there is no handler for a type: "+Measurement.Descriptor.TypeID()); //. ->
    	            //.
    	            Processor.SetLayout(TMeasurementProcessorPanel.this, ProcessorLayout);
    	            Processor.flStandalone = true;
    	            //.
    	            Processor.Setup(Measurement);
    	            //.
    	            Processor.SetPosition(MeasurementStartPosition, 100, true);
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
