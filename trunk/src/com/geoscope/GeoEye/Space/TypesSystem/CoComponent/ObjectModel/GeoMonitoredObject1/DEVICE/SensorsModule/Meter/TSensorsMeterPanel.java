package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Meter;

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

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;

@SuppressLint("HandlerLeak")
public class TSensorsMeterPanel extends Activity {

	private long 			ObjectID = -1;
    private TObjectModel 	ObjectModel = null; 
	//.
	private String 					MeterID;
	private String 					MeterName;
	private TSensorMeter.TProfile 	MeterProfile = null;
	private TChannelIDs				MeterChannels = null;
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
    	ObjectID = extras.getLong("ObjectID");
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
		if (ObjectModel != null) {
			ObjectModel.Destroy();
			ObjectModel = null;
		}
		//.
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
        private TObjectModel ObjectModel = null;
        //.
    	private TSensorMeter.TProfile 	MeterProfile = null;
    	private TChannelIDs				MeterChannels;
    	
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
						try {
		    				TUserAgent UserAgent = TUserAgent.GetUserAgent(TSensorsMeterPanel.this.getApplicationContext());
		    				if (UserAgent == null)
		    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    				TCoGeoMonitorObject	Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
		    				byte[] ObjectModelData = Object.GetData(1000001);
		    				if (ObjectModelData != null) {
		        				Canceller.Check();
		        				//.
		    					int Idx = 0;
		    					int ObjectModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
		    					int BusinessModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
		    					//.
		    					if (ObjectModelID != 0) {
		    						ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
		    						if (ObjectModel != null) {
		    							ObjectModel.SetBusinessModel(BusinessModelID);
		    							//.
		    							TGeographServerObjectController GSOC = Object.GeographServerObjectController();
										ObjectModel.SetObjectController(GSOC, true);
		    							//.
										MeterProfile = new TSensorMeter.TProfile();
										MeterProfile.FromByteArray(ObjectModel.Sensors_Meter_GetProfile(MeterID));
										//.
										MeterChannels = ObjectModel.Sensors_Meter_GetChannels(MeterID); 
		    						}
		    					}
		    				}
						}
						catch (Exception E) {
							if (ObjectModel != null)
								ObjectModel.Destroy();
							throw E; //. =>
						}
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
						if (TSensorsMeterPanel.this.ObjectModel != null)
							TSensorsMeterPanel.this.ObjectModel.Destroy();
		            	TSensorsMeterPanel.this.ObjectModel = ObjectModel;
		            	//.
		            	TSensorsMeterPanel.this.MeterProfile = MeterProfile;
		            	TSensorsMeterPanel.this.MeterChannels = MeterChannels;
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
        Intent intent = new Intent(this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.class);
		intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.PARAMETERS_TYPE_OID);
		intent.putExtra("ObjectID", ObjectID);
		intent.putExtra("ChannelIDs", MeterChannels.ToByteArray());
        startActivity(intent);
    }
    
    private void ApplyChangesAndExit() {
    	if ((MeterProfile == null) || (ObjectModel == null))
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
			
			private TObjectModel _ObjectModel = TSensorsMeterPanel.this.ObjectModel;
			
			@Override
			public void Process() throws Exception {
		    	byte[] MeterProfileBA = MeterProfile.ToByteArray();
		    	//.
				_ObjectModel.Sensors_Meter_SetProfile(MeterID, MeterProfileBA);
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
