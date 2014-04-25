package com.geoscope.GeoEye;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TDispatcherValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TDispatcherValue.TExpertIsDispatchedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDescriptorV1V2;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskResultValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskResultValue.TResultDescriptor;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TStatusDescriptor;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TUserTaskPanel extends Activity {

	private static final int REQUEST_SHOWONREFLECTOR 			= 1;
	private static final int REQUEST_NEWSTATUS 					= 2;
	private static final int REQUEST_DISPATCHTOSPECIFIEDEXPERT 	= 3;
	
	public boolean flExists = false;
	//.
	private boolean flOriginator = false;
	//.
	private TTaskDescriptorV1V2 Task = new TTaskDescriptorV1V2();
	//. 
	private TextView lbUserTask;
	//.
	private EditText edTaskComment;
	//.
	private String		TaskOriginator = ""; 
	private EditText 	edTaskOriginator;
	//.
	private String 		TaskUser = "";	
	private EditText 	edTaskUser;
	//.
	private EditText 		edTaskStatus;
	private Button 			btnTaskStatusChange;
	private EditText 		edTaskStatusTimestamp;
	private LinearLayout	llTaskStatusReason;
	private EditText 		edTaskStatusReason;
	@SuppressWarnings("unused")
	private LinearLayout	llTaskStatusComment;
	private EditText 		edTaskStatusComment;
	//.
	@SuppressWarnings("unused")
	private LinearLayout	llTaskActivities;
	private Button 			btnTaskActivities;
	private Button 			btnAssignCurrentActivity;
	//.
	private LinearLayout	llTaskDispatcher;
	private Button 			btnTaskDispatch;
	private Button 			btnTaskDispatchToSpecifiedExpert;
	//.
	private LinearLayout	llTaskResult;
	private EditText 		edTaskResultTimestamp;
	private LinearLayout	llTaskResultCode;
	private EditText 		edTaskResultCode;
	@SuppressWarnings("unused")
	private LinearLayout	llTaskResultComment;
	private EditText 		edTaskResultComment;
	//.
	private TComponentServiceOperation ServiceOperation = null;
	//.
	private TUpdating	Updating = null;
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		String Title = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	flOriginator = extras.getBoolean("flOriginator");
        	//.
        	Title = extras.getString("Title");
        	//.
        	byte[] TaskData = extras.getByteArray("TaskData");
    		try {
            	Task.FromByteArrayV1(TaskData, 0);
    		}
    		catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    			finish();
    			return; //. ->
    		}
        }
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_task_panel);
        //.
        lbUserTask = (TextView)findViewById(R.id.lbUserTask);
        if (Title != null)
        	lbUserTask.setText(Title);
        //.
        edTaskComment = (EditText)findViewById(R.id.edTaskComment);
        edTaskOriginator = (EditText)findViewById(R.id.edTaskOriginator);
        edTaskUser = (EditText)findViewById(R.id.edTaskUser);
        //.
    	edTaskStatus = (EditText)findViewById(R.id.edTaskStatus);
    	btnTaskStatusChange = (Button)findViewById(R.id.btnTaskStatusChange);
    	btnTaskStatusChange.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TUserTaskPanel.this, TUserTaskNewStatusPanel.class);
        		intent.putExtra("flOriginator", flOriginator);
				startActivityForResult(intent, REQUEST_NEWSTATUS);
            }
        });
    	edTaskStatusTimestamp = (EditText)findViewById(R.id.edTaskStatusTimestamp);
    	llTaskStatusReason = (LinearLayout)findViewById(R.id.llTaskStatusReason);
    	edTaskStatusReason = (EditText)findViewById(R.id.edTaskStatusReason);
    	llTaskStatusComment = (LinearLayout)findViewById(R.id.llTaskStatusComment);
    	edTaskStatusComment = (EditText)findViewById(R.id.edTaskStatusComment);
    	//.
    	llTaskActivities = (LinearLayout)findViewById(R.id.llTaskActivities);
    	btnTaskActivities = (Button)findViewById(R.id.btnTaskActivities);
    	btnTaskActivities.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TUserTaskPanel.this, TUserTaskActivityListPanel.class);
        		intent.putExtra("TaskID", TUserTaskPanel.this.Task.ID);
				startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
    	btnAssignCurrentActivity = (Button)findViewById(R.id.btnAssignCurrentActivity);
    	btnAssignCurrentActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
    		    new AlertDialog.Builder(TUserTaskPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SDoYouWantToAttachYourCurrentActivity)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	@Override
    		    	public void onClick(DialogInterface dialog, int id) {
    	        		try {
    	            		Task_AssignCurrentActivity();
    	        		}
    	        		catch (Exception E) {
    	        			Toast.makeText(TUserTaskPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    	        			finish();
    	        			return; //. ->
    	        		}
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
            }
        });
    	//.
    	llTaskDispatcher = (LinearLayout)findViewById(R.id.llTaskDispatcher);
    	btnTaskDispatch = (Button)findViewById(R.id.btnTaskDispatch);
    	btnTaskDispatch.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
    		    new AlertDialog.Builder(TUserTaskPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SDoYouWantToDispatchTaskToAppropriateExpert)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	@Override
    		    	public void onClick(DialogInterface dialog, int id) {
    	        		try {
    	            		Task_DispatchTask();
    	        		}
    	        		catch (Exception E) {
    	        			Toast.makeText(TUserTaskPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    	        			finish();
    	        			return; //. ->
    	        		}
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
            }
        });
    	btnTaskDispatchToSpecifiedExpert = (Button)findViewById(R.id.btnTaskDispatchToSpecifiedExpert);
    	btnTaskDispatchToSpecifiedExpert.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TUserTaskPanel.this, TMyUserTaskExpertListPanel.class);
				startActivityForResult(intent, REQUEST_DISPATCHTOSPECIFIEDEXPERT);
            }
        });
    	//.
    	llTaskResult = (LinearLayout)findViewById(R.id.llTaskResult);
    	edTaskResultTimestamp = (EditText)findViewById(R.id.edTaskResultTimestamp);
    	llTaskResultCode = (LinearLayout)findViewById(R.id.llTaskResultCode);
    	edTaskResultCode = (EditText)findViewById(R.id.edTaskResultCode);
    	llTaskResultComment = (LinearLayout)findViewById(R.id.llTaskResultComment);
    	edTaskResultComment = (EditText)findViewById(R.id.edTaskResultComment);
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
        Update();
        //.
        StartUpdating();
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
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
            
		case REQUEST_NEWSTATUS:
			if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int Status = extras.getInt("Status");
            		int StatusReason = extras.getInt("StatusReason");
            		String StatusComment = extras.getString("StatusComment");
            		//.
            		//.
            		try {
                		if (Status != TTaskStatusValue.MODELUSER_TASK_STATUS_Processed) 
                			Task_SetStatus(Status, StatusReason, StatusComment);
                		else { 
                    		int ResultCode = extras.getInt("ResultCode");
                    		String ResultComment = extras.getString("ResultComment");
                    		//.
                			Task_SetResult(StatusReason,StatusComment, ResultCode,ResultComment);
                		}
            		}
            		catch (Exception E) {
            			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            			finish();
            			return; //. ->
            		}
            	}
			}
			break; // . >

		case REQUEST_DISPATCHTOSPECIFIEDEXPERT:
			if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int UserID = extras.getInt("UserID");
            		//.
            		try {
                		Task_DispatchTaskToSpecifiedExpert(UserID);
            		}
            		catch (Exception E) {
            			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            			finish();
            			return; //. ->
            		}
            	}
			}
			break; // . >
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void ServiceOperation_Cancel() {
		if (ServiceOperation != null) {
			ServiceOperation.Cancel();
			ServiceOperation = null;
		}
	}
	
	private void Update() {
		edTaskComment.setText(Task.Comment);
		//.
		edTaskOriginator.setText(TaskOriginator);
		edTaskUser.setText(TaskUser);
		//.
		edTaskStatus.setText(TTaskStatusValue.Status_String(Task.Status,this));
		edTaskStatusTimestamp.setText((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Task.StatusTimestamp)).GetDateTime()));
		if (Task.StatusReason > 0) {
			edTaskStatusReason.setText(Integer.toString(Task.StatusReason));
			llTaskStatusReason.setVisibility(View.VISIBLE);
		}
		else
			llTaskStatusReason.setVisibility(View.GONE);
		edTaskStatusComment.setText(Task.StatusComment);
		//.
		btnAssignCurrentActivity.setEnabled((Task.Status != TTaskStatusValue.MODELUSER_TASK_STATUS_Processed) && TMyUserPanel.UserActivityIsAvailable() && (!TMyUserPanel.UserActivityIsDefault()));
		//.
		llTaskDispatcher.setVisibility(((Task.Status == TTaskStatusValue.MODELUSER_TASK_STATUS_Originated) || (Task.Status == TTaskStatusValue.MODELUSER_TASK_STATUS_Preprocessed) || (Task.Status == TTaskStatusValue.MODELUSER_TASK_STATUS_Dispatching)) ? View.VISIBLE : View.GONE);
		//.
		if (Task.Status == TTaskStatusValue.MODELUSER_TASK_STATUS_Processed) {
			edTaskResultTimestamp.setText((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Task.ResultTimestamp)).GetDateTime()));
			if (Task.ResultCode != 0) {
				edTaskResultCode.setText(Integer.toString(Task.ResultCode));
				llTaskResultCode.setVisibility(View.VISIBLE);
			}
			else
				llTaskResultCode.setVisibility(View.GONE);
			edTaskResultComment.setText(Task.ResultComment);
			llTaskResult.setVisibility(View.VISIBLE);
		}
		else 
			llTaskResult.setVisibility(View.GONE);
	}

    private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_STARTED = 0;
    	private static final int MESSAGE_COMPLETED = 1;
    	private static final int MESSAGE_FINISHED = 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 5;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
    	private TTaskDescriptorV1V2 Task;
        //.
    	private TUserDescriptor TaskOriginator = null; 
    	private TUserDescriptor TaskUser = null;
    	//.
        private ProgressDialog progressDialog;
        
    	
    	public TUpdating(TTaskDescriptorV1V2 pTask, boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		Task = pTask;
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
					MessageHandler.obtainMessage(MESSAGE_STARTED).sendToTarget();
					//.
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				TaskOriginator = UserAgent.Server.User.GetUserInfo(Task.idOwner);
	    				TaskUser = UserAgent.Server.User.GetUserInfo(Task.idUser);
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
		            	//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserTaskPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_STARTED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	TUserTaskPanel.this.TaskOriginator = TaskOriginator.UserFullName;
		            	TUserTaskPanel.this.TaskUser = TaskUser.UserFullName;
	           		 	//.
	           		 	TUserTaskPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TUserTaskPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserTaskPanel.this);    
		            	progressDialog.setMessage(TUserTaskPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserTaskPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserTaskPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserTaskPanel.this.finish();
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
	
    private void StartUpdating() {
    	if (Updating != null)
    		Updating.CancelAndWait();
    	Updating = new TUpdating(Task, true,false);
    }

    private void Task_SetStatus(int pStatus, int pStatusReason, String pStatusComment) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.SetTaskStatus(Task.ID, OleDate.UTCCurrentTimestamp(), pStatus, pStatusReason, pStatusComment, new TTaskStatusValue.TStatusIsChangedHandler() {
    		@Override
    		public void DoOnStatusIsChanged(TStatusDescriptor Status) {
    			Task_DoOnStatusIsChanged(Status);
    		}
    	}, new TTaskStatusValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_DoOnStatusIsChanged(TStatusDescriptor Status) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONSTATUSISCHANGED,Status).sendToTarget();
    }
    
    private void Task_SetResult(int pCompletedStatusReason, String pCompletedStatusComment, int pResultCode, String pResultComment) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.SetTaskResult(Task.ID, pCompletedStatusReason, pCompletedStatusComment, OleDate.UTCCurrentTimestamp(), pResultCode, pResultComment, new TTaskResultValue.TResultIsChangedHandler() {
    		@Override
    		public void DoOnResultIsChanged(TResultDescriptor Result) {
    			Task_DoOnResultIsChanged(Result);
    		}
    	}, new TTaskResultValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_DoOnResultIsChanged(TResultDescriptor Result) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONRESULTSISCHANGED,Result).sendToTarget();
    }
    
    private void Task_AssignCurrentActivity() throws Exception {
    	TActivity CurrentActivity = TMyUserPanel.GetUserActivity();
    	if (CurrentActivity == null)
    		throw new Exception("current activity is not available"); //. =>
    	if (TMyUserPanel.UserActivityIsDefault())
    		throw new Exception("current activity is default activity"); //. =>
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.AssignActivityToTask(CurrentActivity.ID, Task.ID, new TTaskDataValue.TDoneHandler() {
    		@Override
    		public void DoOnDone(double Timestamp) {
    			Task_DoOnCurrentActrivityIsAssigned(Timestamp);
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
    
    private void Task_DoOnCurrentActrivityIsAssigned(double Timestamp) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONCURRENTACTIVITYISASSIGNED,Timestamp).sendToTarget();
    }
    
    private void Task_DispatchTask() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.DispatchTask(Task.ID, TDispatcherValue.DISPATCH_POLICY_FIRSTFREE, 0, 0, new TExpertIsDispatchedHandler() {
    		@Override
    		public void DoOnExpertIsDispatched(int idUser) {
    			Task_DoOnExpertIsDispatched(idUser);
    		}
    	}, new TDispatcherValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_DispatchTaskToSpecifiedExpert(int SpecifiedExpertID) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.DispatchTaskToTheSpecifiedExpert(Task.ID, SpecifiedExpertID, 0, new TExpertIsDispatchedHandler() {
    		@Override
    		public void DoOnExpertIsDispatched(int idUser) {
    			Task_DoOnExpertIsDispatched(idUser);
    		}
    	}, new TDispatcherValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_DoOnExpertIsDispatched(int idUser) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONEXPERTISDISPATCHED,idUser).sendToTarget();
    }
    
    private void Task_DoOnException(Exception E) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
	private static final int MESSAGE_EXCEPTION 						= -1;
	private static final int MESSAGE_PROGRESSBAR_SHOW 				= 1;
	private static final int MESSAGE_PROGRESSBAR_HIDE 				= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 			= 3;
	private static final int MESSAGE_ONSTATUSISCHANGED 				= 4;
	private static final int MESSAGE_ONCURRENTACTIVITYISASSIGNED	= 5;
	private static final int MESSAGE_ONEXPERTISDISPATCHED 			= 6;
	private static final int MESSAGE_ONRESULTSISCHANGED 			= 7;

	private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
					if (!flExists)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserTaskPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserTaskPanel.this);    
	            	progressDialog.setMessage(TUserTaskPanel.this.getString(R.string.SWaitAMoment));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TUserTaskPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserTaskPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	            			TUserTaskPanel.this.finish();
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
	            	
	            case MESSAGE_ONSTATUSISCHANGED:
					if (!flExists)
		            	break; //. >
					TStatusDescriptor Status = (TStatusDescriptor)msg.obj;
					//.
					TUserTaskPanel.this.Task.StatusTimestamp = Status.Timestamp;
					TUserTaskPanel.this.Task.Status = Status.Status;
					TUserTaskPanel.this.Task.StatusReason = Status.Reason;
					TUserTaskPanel.this.Task.StatusComment = Status.Comment;
					//.
					TUserTaskPanel.this.Update();
					//.
					Toast.makeText(TUserTaskPanel.this, getString(R.string.SStatusHasBeenChanged), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >	            	

	            case MESSAGE_ONCURRENTACTIVITYISASSIGNED:
					if (!flExists)
		            	break; //. >
					//.
					btnAssignCurrentActivity.setEnabled(false);
					//.
					TMyUserPanel.ResetUserCurrentActivity();					
					//.
					Toast.makeText(TUserTaskPanel.this, getString(R.string.SCurrentActivityHasBeenAssignedToTheTask), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >	            	

	            case MESSAGE_ONEXPERTISDISPATCHED:
					if (!flExists)
		            	break; //. >
			    	final int idUser = (Integer)msg.obj;
			    	//.
			    	TUserTaskPanel.this.Task.idUser = idUser;
			    	//.
					TAsyncProcessing Notifying = new TAsyncProcessing() {
						private String UserName; 
						@Override
						public void Process() throws Exception {
		    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
		    				if (UserAgent == null)
		    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    				//.
		    				TUserDescriptor User = UserAgent.Server.User.GetUserInfo(idUser);
		    				UserName = User.UserFullName;
						}
						@Override
						public void DoOnCompleted() throws Exception {
							Toast.makeText(TUserTaskPanel.this, getString(R.string.STaskHasBeenDispatchedToTheExpert)+UserName, Toast.LENGTH_LONG).show();
						}
						public void DoOnException(Exception E) {
							Task_DoOnException(E);
						}
					};
					Notifying.Start();
					//.
					TUserTaskPanel.this.finish();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONRESULTSISCHANGED:
					if (!flExists)
		            	break; //. >
					TResultDescriptor Result = (TResultDescriptor)msg.obj;
					//.
					TUserTaskPanel.this.Task.StatusTimestamp = Result.CompletedStatus.Timestamp;
					TUserTaskPanel.this.Task.Status = Result.CompletedStatus.Status;
					TUserTaskPanel.this.Task.StatusReason = Result.CompletedStatus.Reason;
					TUserTaskPanel.this.Task.StatusComment = Result.CompletedStatus.Comment;
					//.
					TUserTaskPanel.this.Task.ResultTimestamp = Result.Timestamp;
					TUserTaskPanel.this.Task.ResultCode = Result.ResultCode;
					TUserTaskPanel.this.Task.ResultComment = Result.Comment;
					//.
					TUserTaskPanel.this.Update();
					//.
					Toast.makeText(TUserTaskPanel.this, getString(R.string.SResultHasBeenSet), Toast.LENGTH_LONG).show();
					//.
					TUserTaskPanel.this.finish();
	            	//.
	            	break; //. >	            	
	            }
        	}
        	catch (Exception E) {
        	}
        }
    };
}