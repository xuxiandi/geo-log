package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Meters;

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
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;

@SuppressLint("HandlerLeak")
public class TSensorsMetersPanel extends Activity {

	private long 			ObjectID = -1;
    private TObjectModel 	ObjectModel = null; 
	//.
	private TSensorMeterInfo[] MetersInfo = null;
	//.
	private ListView lvMeters;	
	private Button btnUpdate;
	private Button btnApplyChanges;
	//.
	private TUpdating	Updating = null;
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	ObjectID = extras.getLong("ObjectID");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.sensorsmodule_meters_panel);
        //.
        lvMeters = (ListView)findViewById(R.id.lvMeters);
        lvMeters.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvMeters.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				boolean flChecked = lvMeters.isItemChecked(arg2);
				if (flChecked) { //. clear checking for all items with the same LocationID
					TSensorMeterInfo CheckedMeter = MetersInfo[arg2];
					if (CheckedMeter.Descriptor.LocationID.length() > 0) {
						int Cnt = MetersInfo.length;
						for (int I =0; I < Cnt; I++)
							if (I != arg2) {
								TSensorMeterInfo Meter = MetersInfo[I];
						    	if (Meter.Descriptor.LocationID.equals(CheckedMeter.Descriptor.LocationID))
						    		lvMeters.setItemChecked(I, false);
							}
					}
				}
			}
		});
        lvMeters.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (MetersInfo == null)
					return false; //. ->
				Intent intent = new Intent(TSensorsMetersPanel.this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Meter.TSensorsMeterPanel.class);
				intent.putExtra("ObjectID", ObjectID);
				intent.putExtra("MeterID", MetersInfo[arg2].Descriptor.ID);
				intent.putExtra("MeterName", MetersInfo[arg2].Descriptor.Name);
		        startActivity(intent);
				return true;
			}
		});
        //.
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		StartUpdating();
            }
        });
        //.
        btnApplyChanges = (Button)findViewById(R.id.btnApplyChanges);
        btnApplyChanges.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
            		ApplyChanges();
				} catch (Exception E) {
					Toast.makeText(TSensorsMetersPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        final int HintID = THintManager.HINT__Long_click_to_edit_item_properties;
        final TextView lbListHint = (TextView)findViewById(R.id.lbListHint);
        String Hint = THintManager.GetHint(HintID, this);
        if (Hint != null) {
        	lbListHint.setText(Hint);
            lbListHint.setOnLongClickListener(new OnLongClickListener() {
            	
    			@Override
    			public boolean onLongClick(View v) {
    				THintManager.SetHintAsDisabled(HintID);
    	        	lbListHint.setVisibility(View.GONE);
    	        	//.
    				return true;
    			}
    		});
            //.
        	lbListHint.setVisibility(View.VISIBLE);
        }
        else
        	lbListHint.setVisibility(View.GONE);
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
        private TSensorMeterInfo[] MetersInfo = null;
    	
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
		    				TUserAgent UserAgent = TUserAgent.GetUserAgent(TSensorsMetersPanel.this.getApplicationContext());
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
		    							MetersInfo = ObjectModel.Sensors_Meters_GetList(); 
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
		                Toast.makeText(TSensorsMetersPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
						if (TSensorsMetersPanel.this.ObjectModel != null)
							TSensorsMetersPanel.this.ObjectModel.Destroy();
		            	TSensorsMetersPanel.this.ObjectModel = ObjectModel;
		            	//.
		            	TSensorsMetersPanel.this.MetersInfo = MetersInfo;
	           		 	//.
		            	TSensorsMetersPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TSensorsMetersPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TSensorsMetersPanel.this);    
		            	progressDialog.setMessage(TSensorsMetersPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TSensorsMetersPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TSensorsMetersPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TSensorsMetersPanel.this.finish();
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
    		if (MetersInfo != null) {
    			int SaveIndex = lvMeters.getFirstVisiblePosition();
    			View V = lvMeters.getChildAt(0);
    			int SaveTop = (V == null) ? 0 : V.getTop();
    			//.
    			String[] lvItems = new String[MetersInfo.length];
    			for (int I = 0; I < MetersInfo.length; I++) {
    				lvItems[I] = MetersInfo[I].Descriptor.Name;
    				if (MetersInfo[I].Descriptor.Info.length() > 0)
    					lvItems[I] += " "+"/"+MetersInfo[I].Descriptor.Info+"/"; 
    				switch (MetersInfo[I].Status) {

    				case TSensorMeter.STATUS_ERROR:
    					lvItems[I] += "   "+"["+TSensorMeter.STATUS_GetString(MetersInfo[I].Status, this)+((MetersInfo[I].StatusString != null) ? (", "+MetersInfo[I].StatusString) : "")+"]"; 
    					break; //. >
    				
    				case TSensorMeter.STATUS_NOTRUNNING:
    					break; //. >
    					
    				case TSensorMeter.STATUS_RUNNING:
    					lvItems[I] += "   "+"["+TSensorMeter.STATUS_GetString(MetersInfo[I].Status, this)+((MetersInfo[I].StatusTimestamp > 0.0) ? (" "+Integer.toString((int)((OleDate.UTCCurrentTimestamp()-MetersInfo[I].StatusTimestamp)*24.0*60.0))+" min") : "")+"]"; 
    					break; //. >
    				}
    			}
    			ArrayAdapter<String> lvItemsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvItems);             
    			lvMeters.setAdapter(lvItemsAdapter);
    			for (int I = 0; I < MetersInfo.length; I++)
    				lvMeters.setItemChecked(I,MetersInfo[I].flActive);
    			//.
    			lvMeters.setSelectionFromTop(SaveIndex, SaveTop);
    		}
    		else {
    			lvMeters.setAdapter(null);
    		}
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
    
    private void ApplyChanges() {
    	if ((MetersInfo == null) || (ObjectModel == null))
    		return; //. ->
    	StringBuilder SB = new StringBuilder();
		for (int I = 0; I < MetersInfo.length; I++)
			if (lvMeters.isItemChecked(I)) {
				if (I != 0)
					SB.append(","+MetersInfo[I].Descriptor.ID);
				else
					SB.append(MetersInfo[I].Descriptor.ID);
			}
		final String MeterIDs = SB.toString();
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TObjectModel _ObjectModel = TSensorsMetersPanel.this.ObjectModel;
			
			@Override
			public void Process() throws Exception {
				_ObjectModel.Sensors_Meters_ValidateActivity(MeterIDs);
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				StartUpdating();
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TSensorsMetersPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
}
