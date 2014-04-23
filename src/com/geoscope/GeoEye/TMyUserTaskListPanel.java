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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDescriptorV1V2;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDescriptorsV1V2;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserTasksAreReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;

@SuppressLint("HandlerLeak")
public class TMyUserTaskListPanel extends Activity {

	public boolean flExists = false;
	//. 
	private TextView lbUserTaskList;
	private ListView lvUserTaskList;
	private CheckBox cbActiveTasksOnly;
	//.
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
					TTaskDescriptorV1V2 Task = UserTasks.Items[arg2];
	            	Intent intent = new Intent(TMyUserTaskListPanel.this, TUserTaskPanel.class);
	            	intent.putExtra("flOriginator",flOriginator);
	            	intent.putExtra("TaskData",Task.ToByteArrayV1());
	            	////////////////startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
	            	startActivity(intent);
				}
				catch (Exception E) {
					Toast.makeText(TMyUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				}
        	}              
        });
        cbActiveTasksOnly = (CheckBox)findViewById(R.id.cbActiveTasksOnly);
        cbActiveTasksOnly.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				try {
			        Tasks_GetData(arg1);
				}
				catch (Exception E) {
					Toast.makeText(TMyUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
	
    private void Tasks_GetData(boolean flOnlyActive) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	Tracker.GeoLog.TaskModule.GetUserTasks(flOriginator,flOnlyActive, new TUserTasksAreReceivedHandler() {
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
			lvItems[I] = Task.Comment+" {"+(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Task.StatusTimestamp)).GetDateTime())+": "+TTaskStatusValue.Status_String(Task.Status,this)+"}";
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserTaskList.setAdapter(lvAdapter);
    }

	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_ONUSERTASKSARERECEIVED = 0;
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
	                Toast.makeText(TMyUserTaskListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONUSERTASKSARERECEIVED:
					if (!flExists)
		            	break; //. >
			    	UserTasks = (TTaskDescriptorsV1V2)msg.obj;
			    	Update();    	
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TMyUserTaskListPanel.this);    
	            	progressDialog.setMessage(TMyUserTaskListPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TMyUserTaskListPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TMyUserTaskListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	        				TMyUserTaskListPanel.this.finish();
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
