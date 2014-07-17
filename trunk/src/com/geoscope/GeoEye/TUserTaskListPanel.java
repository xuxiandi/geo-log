package com.geoscope.GeoEye;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDescriptorV1V2;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDescriptorsV1V2;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserTasksAreReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;

@SuppressLint("HandlerLeak")
public class TUserTaskListPanel extends Activity {

	private static final int REQUEST_SHOWONREFLECTOR = 1;
	
	public boolean flExists = false;
	//. 
	private TextView lbUserTaskList;
	private ListView lvUserTaskList;
	private CheckBox cbActiveTasksOnly;
	//.
	private TComponentServiceOperation ServiceOperation = null;
	//.
	private int UserID = 0;
	private boolean flOriginator = false;	
    private TTaskDescriptorsV1V2 UserTasks = null;
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	UserID = extras.getInt("UserID");
        	flOriginator = extras.getBoolean("flOriginator");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.myuser_tasklist_panel);
        //.
        lbUserTaskList = (TextView)findViewById(R.id.lbUserTaskList);
        if (flOriginator)
        	lbUserTaskList.setText(getString(R.string.SMyOriginatedTasks));
        else
        	lbUserTaskList.setText(getString(R.string.SMyTasks));
        lvUserTaskList = (ListView)findViewById(R.id.lvUserTaskList);
        lvUserTaskList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserTaskList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (UserTasks == null)
					return; //. ->
				try {
					//. Tasks_OpenTaskPanel(UserTasks.Items[arg2].ID);
					TTaskDescriptorV1V2 Task = UserTasks.Items[arg2];
	            	Intent intent = new Intent(TUserTaskListPanel.this, TUserTaskPanel.class);
	            	intent.putExtra("UserID",UserID);
	            	intent.putExtra("flOriginator",flOriginator);
	            	intent.putExtra("TaskData",Task.ToByteArrayV1());
	            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
				}
				catch (Exception E) {
					Toast.makeText(TUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				}
        	}              
        });
        cbActiveTasksOnly = (CheckBox)findViewById(R.id.cbActiveTasksOnly);
        cbActiveTasksOnly.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
				try {
			        Tasks_GetData(checked);
				}
				catch (Exception E) {
					Toast.makeText(TUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				}
            }
        });        
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
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
        //.
		try {
	        Tasks_GetData(cbActiveTasksOnly.isChecked());
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
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
	
    private void Tasks_GetData(boolean flOnlyActive) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetUserTasks(UserID, flOriginator,flOnlyActive, new TUserTasksAreReceivedHandler() {
    		@Override
    		public void DoOnUserTasksAreReceived(TTaskDescriptorsV1V2 Tasks) {
    			Tasks_DoOnUserTasksAreReceived(Tasks);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Tasks_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Tasks_DoOnUserTasksAreReceived(TTaskDescriptorsV1V2 Tasks) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONUSERTASKSARERECEIVED,Tasks).sendToTarget();
    }
        
    @SuppressWarnings("unused")
	private void Tasks_OpenTaskPanel(int TaskID) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskData(UserID, TaskID, new TTaskDataValue.TTaskDataIsReceivedHandler() {
    		@Override
    		public void DoOnTaskDataIsReceived(byte[] TaskData) {
    			Tasks_OnDataIsReceivedForOpening(TaskData);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Tasks_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Tasks_OnDataIsReceivedForOpening(byte[] TaskData) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_OPENTASKPANEL,TaskData).sendToTarget();
    }
        
    private void Tasks_DoOnException(Exception E) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private void Update() {
    	if (UserTasks == null) {
    		lvUserTaskList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[UserTasks.Items.length];
		for (int I = 0; I < UserTasks.Items.length; I++) {
			TTaskDescriptorV1V2 Task = UserTasks.Items[I]; 
			lvItems[I] = Task.Comment+":  "+TTaskStatusValue.Status_String(Task.Status,this)+" ("+(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Task.StatusTimestamp)).GetDateTime())+")";
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserTaskList.setAdapter(lvAdapter);
    }

	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
	private static final int MESSAGE_ONUSERTASKSARERECEIVED = 4;
	private static final int MESSAGE_OPENTASKPANEL			= 5;

	private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
					if (!flExists)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONUSERTASKSARERECEIVED:
					if (!flExists)
		            	break; //. >
			    	UserTasks = (TTaskDescriptorsV1V2)msg.obj;
			    	Update();    	
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_OPENTASKPANEL:
	            	if (!flExists)
	            		break; //. >
	            	byte[] TaskData = (byte[])msg.obj;
	            	//.
	            	Intent intent = new Intent(TUserTaskListPanel.this, TUserTaskPanel.class);
	            	intent.putExtra("flOriginator",flOriginator);
	            	intent.putExtra("TaskData",TaskData);
	            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserTaskListPanel.this);    
	            	progressDialog.setMessage(TUserTaskListPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TUserTaskListPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserTaskListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	        				TUserTaskListPanel.this.finish();
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
