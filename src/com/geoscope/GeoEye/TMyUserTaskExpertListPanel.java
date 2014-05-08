package com.geoscope.GeoEye;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue.TExpertDescriptorV1;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue.TExpertDescriptorsV1;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue.TExpertsIsReceivedHandler;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TMyUserTaskExpertListPanel extends Activity {

	public boolean flExists = false;
	//. 
	@SuppressWarnings("unused")
	private TextView lbUserTaskExpertList;
	private CheckBox cbActiveExpertsOnly;	
	private ListView lvUserTaskExpertList;
	//.
	private boolean flActiveExpertsOnly = true;
	//.
	private TComponentServiceOperation ServiceOperation = null;
	//.
    private TExpertDescriptorsV1 Experts = null;
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.myuser_taskexpertlist_panel);
        //.
        lbUserTaskExpertList = (TextView)findViewById(R.id.lbUserTaskExpertList);
        cbActiveExpertsOnly = (CheckBox)findViewById(R.id.cbActiveExpertsOnly);
        cbActiveExpertsOnly.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				try {
					flActiveExpertsOnly = ((CheckBox)v).isChecked();
					//.
			        Tasks_GetExperts(flActiveExpertsOnly);
				}
				catch (Exception E) {
					Toast.makeText(TMyUserTaskExpertListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				}
            }
        });        
        lvUserTaskExpertList = (ListView)findViewById(R.id.lvUserTaskExpertList);
        lvUserTaskExpertList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserTaskExpertList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (Experts == null)
					return; //. ->
		    	Intent intent = getIntent();
		    	intent.putExtra("UserID",Experts.Items[arg2].ID);
		        //.
		    	setResult(Activity.RESULT_OK,intent);
		    	//.
		    	TMyUserTaskExpertListPanel.this.finish();
        	}              
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
		try {
	        Tasks_GetExperts(flActiveExpertsOnly);
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
    	ServiceOperation_Cancel();
    	//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void ServiceOperation_Cancel() {
		if (ServiceOperation != null) {
			ServiceOperation.Cancel();
			ServiceOperation = null;
		}
	}
	
    private void Tasks_GetExperts(boolean flActiveExpertsOnly) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetExperts(flActiveExpertsOnly, new TExpertsIsReceivedHandler() {
    		@Override
    		public void DoOnExpertsIsReceived(TExpertDescriptorsV1 Experts) {
    			Tasks_DoOnExpertsIsReceived(Experts);
    		}
    	}, new TExpertsValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Tasks_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Tasks_DoOnExpertsIsReceived(TExpertDescriptorsV1 Experts) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONEXPERTSARERECEIVED,Experts).sendToTarget();
    }
        
    private void Tasks_DoOnException(Exception E) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private void Update() {
    	if (Experts == null) {
    		lvUserTaskExpertList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[Experts.Items.length];
		for (int I = 0; I < Experts.Items.length; I++) {
			TExpertDescriptorV1 Expert = Experts.Items[I]; 
			lvItems[I] = Expert.Name+" ["+Expert.Domains+"]";
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserTaskExpertList.setAdapter(lvAdapter);
    }

	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_ONEXPERTSARERECEIVED 	= 0;
	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;

	private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
					if (!flExists)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TMyUserTaskExpertListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONEXPERTSARERECEIVED:
					if (!flExists)
		            	break; //. >
			    	Experts = (TExpertDescriptorsV1)msg.obj;
			    	Update();    	
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TMyUserTaskExpertListPanel.this);    
	            	progressDialog.setMessage(TMyUserTaskExpertListPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TMyUserTaskExpertListPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TMyUserTaskExpertListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	        				TMyUserTaskExpertListPanel.this.finish();
	            		} 
	            	}); 
	            	//.
	            	progressDialog.show(); 	            	
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_HIDE:
	                if ((!isFinishing()) && progressDialog.isShowing()) 
	                	progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
        	}
        	catch (Exception E) {
        	}
        }
    };
}
