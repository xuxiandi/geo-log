package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TSensorsMeterPanel extends Activity {

	private TDEVICEModule Device;
	//.
	private String 					MeterID;
	private String 					MeterName;
	private TSensorMeter 			Meter;
	private TSensorMeter.TProfile 	MeterProfile = null;
	//.
	private TextView lbName;
	private CheckBox cbEnabled;
	private CheckBox cbActive;
	private CheckBox cbCreateDataFile;
	private Button btnChannels;
	private EditText edMeasurementMaxDuration;
	private EditText edMeasurementLifeTime;
	private EditText edMeasurementAutosaveInterval;
	private Button btnApplyChanges;
	//.
	private TUpdating	Updating = null;
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        Bundle extras = getIntent().getExtras(); 
    	MeterID = extras.getString("MeterID");
    	MeterName = extras.getString("MeterName");
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.sensorsmodule_meter_panel);
        //.
        lbName = (TextView)findViewById(R.id.lbName);
        lbName.setText(getString(R.string.SRecorder)+" "+MeterName);
        //.
        cbEnabled = (CheckBox)findViewById(R.id.cbEnabled);
        //.
        cbActive = (CheckBox)findViewById(R.id.cbActive);
        //.
        cbCreateDataFile = (CheckBox)findViewById(R.id.cbCreateDataFile);
        //.
        btnChannels = (Button)findViewById(R.id.btnChannels);
        btnChannels.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
            		ShowChannels();
				} catch (Exception E) {
					Toast.makeText(TSensorsMeterPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        edMeasurementMaxDuration = (EditText)findViewById(R.id.edMeasurementMaxDuration);
        //.
        edMeasurementLifeTime = (EditText)findViewById(R.id.edMeasurementLifeTime);
        //.
        edMeasurementAutosaveInterval = (EditText)findViewById(R.id.edMeasurementAutosaveInterval);
        //.
        btnApplyChanges = (Button)findViewById(R.id.btnApplyChanges);
        btnApplyChanges.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
            		ApplyChangesAndExit();
				} catch (Exception E) {
					Toast.makeText(TSensorsMeterPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

    @Override
    protected void onResume() {
    	super.onResume();
        //.
        StartUpdating();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
    }
    
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
    	private TDEVICEModule Device;
        //.
    	private TSensorMeter 			Meter;
    	private TSensorMeter.TProfile 	MeterProfile = null;
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		super();
    		//.
    		flShowProgress = pflShowProgress;
    		flClosePanelOnCancel = pflClosePanelOnCancel;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				try {
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
    		        	TTracker Tracker = TTracker.GetTracker(Device.context);
    		        	if (Tracker == null)
    		        		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    		        	Device = Tracker.GeoLog;
						//.
    		        	Meter = Device.SensorsModule.Meters.Items_GetItem(MeterID);
						MeterProfile = new TSensorMeter.TProfile();
						MeterProfile.FromByteArray(Meter.Profile.ToByteArray()); 
					}
					finally {
						if (flShowProgress)
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
					}
    				//.
	    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
	        	}
	        	catch (InterruptedException E) {
	        	}
	        	catch (IOException E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	        	}
	        	catch (Throwable E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
	        	}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TSensorsMeterPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	TSensorsMeterPanel.this.Device = Device;
		            	//.
		            	TSensorsMeterPanel.this.Meter = Meter;
		            	TSensorsMeterPanel.this.MeterProfile = MeterProfile;
	           		 	//.
		            	TSensorsMeterPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TSensorsMeterPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TSensorsMeterPanel.this);    
		            	progressDialog.setMessage(TSensorsMeterPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TSensorsMeterPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TSensorsMeterPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TSensorsMeterPanel.this.finish();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!isFinishing()) && progressDialog.isShowing()) 
		                	try {
			                	progressDialog.dismiss(); 
		                	}
		                	catch (IllegalArgumentException IAE) {} 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }   
	
    private void Update() {
    	flUpdate = true; 
    	try {
    		cbEnabled.setChecked(MeterProfile.flEnabled);
    		//.
    		cbActive.setChecked(MeterProfile.flActive);
    		//.
    		edMeasurementMaxDuration.setText(Integer.toString((int)(MeterProfile.MeasurementMaxDuration*24.0*3600.0)));
    		//.
    		edMeasurementLifeTime.setText(Integer.toString((int)MeterProfile.MeasurementLifeTime));
    		//.
    		edMeasurementAutosaveInterval.setText(Integer.toString((int)MeterProfile.MeasurementAutosaveInterval));
    		//.
    		cbCreateDataFile.setChecked(MeterProfile.flCreateDataFile);
    	}
    	finally {
    		flUpdate = false;
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }    
    
    private void ShowChannels() throws Exception {
		Intent intent = new Intent(this, TDataStreamPropsPanel.class);
		intent.putExtra("ChannelIDs", Meter.GetChannelIDs().ToByteArray());
        startActivity(intent);
    }
    
    private void ApplyChangesAndExit() {
    	if ((MeterProfile == null) || (Device == null))
    		return; //. ->
    	MeterProfile.flEnabled = cbEnabled.isChecked();
    	//.
    	MeterProfile.flActive = cbActive.isChecked();
    	//.
    	MeterProfile.MeasurementMaxDuration = Double.parseDouble(edMeasurementMaxDuration.getText().toString())/(24.0*3600.0);
    	//.
    	MeterProfile.MeasurementLifeTime = Double.parseDouble(edMeasurementLifeTime.getText().toString());
    	//.
    	MeterProfile.MeasurementAutosaveInterval = Double.parseDouble(edMeasurementAutosaveInterval.getText().toString())/(24.0*3600.0);
    	//.
    	MeterProfile.flCreateDataFile = cbCreateDataFile.isChecked();
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TDEVICEModule Device = TSensorsMeterPanel.this.Device;
			
			@Override
			public void Process() throws Exception {
		    	byte[] MeterProfileBA = MeterProfile.ToByteArray();
		    	//.
				Device.SensorsModule.Meters.Items_GetItem(MeterID).SetProfile(MeterProfileBA, true);
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
        		TSensorsMeterPanel.this.finish();
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TSensorsMeterPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
}
