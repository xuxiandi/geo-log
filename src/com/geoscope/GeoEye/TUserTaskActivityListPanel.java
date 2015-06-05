package com.geoscope.GeoEye;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUserTaskActivityListPanel extends Activity {

	private static final int REQUEST_SHOWONREFLECTOR = 1;
	
	public boolean flExists = false;
	//. 
	private TReflectorComponent Component;
	//.
	@SuppressWarnings("unused")
	private TextView lbUserTaskActivityList;
	private ListView lvUserTaskActivityList;
	//.
	private TComponentServiceOperation ServiceOperation = null;
	//.
	private long UserID = 0;
	private long TaskID = 0;	
    private TActivities TaskActivities = null;
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        //.
        int ComponentID = 0;
        if (extras != null) {
			ComponentID = extras.getInt("ComponentID");
			//.
        	UserID = extras.getLong("UserID");
        	//.
        	TaskID = extras.getLong("TaskID");
        }
		Component = TReflectorComponent.GetComponent(ComponentID);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_taskactivitylist_panel);
        //.
        lbUserTaskActivityList = (TextView)findViewById(R.id.lbUserTaskActivityList);
        lvUserTaskActivityList = (ListView)findViewById(R.id.lvUserTaskActivityList);
        lvUserTaskActivityList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserTaskActivityList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (TaskActivities == null)
					return; //. ->
            	Intent intent = new Intent(TUserTaskActivityListPanel.this, TUserActivityComponentListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",TaskActivities.Items[arg2].idUser);
            	intent.putExtra("ActivityID",TaskActivities.Items[arg2].ID);
            	intent.putExtra("ActivityInfo",TaskActivities.Items[arg2].GetInfo(TUserTaskActivityListPanel.this));
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
        	}              
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
		try {
	        Task_GetActivities();
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
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) { 
                setResult(RESULT_OK);
                //.
        		finish();
        	}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
	private void ServiceOperation_Cancel() {
		if (ServiceOperation != null) {
			ServiceOperation.Cancel();
			ServiceOperation = null;
		}
	}
	
    private void Task_GetActivities() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskActivities(UserID, TaskID, new TTaskDataValue.TTaskActivitiesAreReceivedHandler() {
    		@Override
    		public void DoOnTaskActivitiesAreReceived(TActivities Activities) {
    			Task_OnActivitiesAreReceived(Activities);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_OnActivitiesAreReceived(TActivities Activities) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_COMPLETED,Activities).sendToTarget();
    }
        
    private void Task_DoOnException(Exception E){
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private void Update() {
    	if (TaskActivities == null) {
    		lvUserTaskActivityList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[TaskActivities.Items.length];
		for (int I = 0; I < TaskActivities.Items.length; I++) {
			TActivity Activity = TaskActivities.Items[I]; 
			String AN = Activity.Name;
			if (Activity.Info != null)
				AN += " /"+Activity.Info+"/";
			lvItems[I] = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Activity.FinishTimestamp)).GetDateTime())+": "+AN;
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserTaskActivityList.setAdapter(lvAdapter);
    }

	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_COMPLETED 				= 0;
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
	                Toast.makeText(TUserTaskActivityListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMPLETED:
					if (!flExists)
		            	break; //. >
			    	TaskActivities = (TActivities)msg.obj;
			    	Update();    	
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserTaskActivityListPanel.this);    
	            	progressDialog.setMessage(TUserTaskActivityListPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TUserTaskActivityListPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserTaskActivityListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	        				TUserTaskActivityListPanel.this.finish();
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
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
