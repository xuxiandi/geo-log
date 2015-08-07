package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserActivityIsStartedHandler;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUserActivityPanel extends Activity {

	private static final int MESSAGE_EXCEPTION 					= -1;
    private static final int MESSAGE_PROGRESSBAR_SHOW 			= 1;
    private static final int MESSAGE_PROGRESSBAR_HIDE 			= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 3;
	private static final int MESSAGE_LOADCURRENTACTIVITY		= 4;
	private static final int MESSAGE_SETCURRENTACTIVITY 		= 5;
	private static final int MESSAGE_RESTARTACTIVITYFORCURRENT	= 6;
	private static final int MESSAGE_SELECTCURRENTACTIVITY 		= 7;
	private static final int MESSAGE_ONACTIVITYISSTARTED		= 8;
	
	public boolean flExists = false;
	//.
	private String NewActivityName = null;
	private String NewActivityInfo = null;
	//.
	private EditText edCurrentUserActivityName;
	private EditText edCurrentUserActivityInfo;
	private Button btnSetCurrentUserActivity;
	private Button btnRestartUserActivity;
	private Button btnSelectCurrentUserActivity;
	private Button btnClearCurrentUserActivity;
	//.
	private TCurrentActivityLoading CurrentActivityLoading = null;
	//.
	private TCurrentActivitySetting CurrentActivitySetting = null;
	//.
	@SuppressWarnings("unused")
	private TComponentServiceOperation ServiceOperation = null;
	//.
	private TCurrentActivitySelecting CurrentActivitySelecting = null;
	//.
	private TGeoScopeServerUser.TUserDescriptor.TActivity CurrentActivity = null;
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) { 
        	NewActivityName = extras.getString("NewActivityName");
        	NewActivityInfo = extras.getString("NewActivityInfo");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activity_panel);
        //.
        edCurrentUserActivityName = (EditText)findViewById(R.id.edCurrentUserActivityName);
        edCurrentUserActivityName.setOnFocusChangeListener(new OnFocusChangeListener() {
        	@Override
        	public void onFocusChange(View v, boolean hasFocus) {
        	    if (hasFocus)
        	    	edCurrentUserActivityName.setText("");
        	}
        });
        edCurrentUserActivityInfo = (EditText)findViewById(R.id.edCurrentUserActivityInfo);
        edCurrentUserActivityInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
        	@Override
        	public void onFocusChange(View v, boolean hasFocus) {
        	    if (hasFocus)
        	    	edCurrentUserActivityInfo.setText("");
        	}
        });
        btnSetCurrentUserActivity = (Button)findViewById(R.id.btnSetCurrentUserActivity);
        btnSetCurrentUserActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
    			TActivity NewActivity = new TActivity();
    			NewActivity.Name = edCurrentUserActivityName.getText().toString();
    			NewActivity.Info = edCurrentUserActivityInfo.getText().toString();
    			if ((CurrentActivity != null) && NewActivity.Name.equals(CurrentActivity.Name))
    				return; //. ->
            	SetCurrentActivity(NewActivity);
            }
        });
        btnRestartUserActivity = (Button)findViewById(R.id.btnRestartUserActivity);
        btnRestartUserActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	RestartActivityForCurrent();
            }
        });
        btnSelectCurrentUserActivity = (Button)findViewById(R.id.btnSelectCurrentUserActivity);
        btnSelectCurrentUserActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	SelectCurrentActivity();
            }
        });
        btnClearCurrentUserActivity = (Button)findViewById(R.id.btnClearCurrentUserActivity);
        btnClearCurrentUserActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	SetCurrentActivity(null);
            }
        });
        //.
        setResult(RESULT_CANCELED);
        //.
		if ((NewActivityName != null) && (!NewActivityName.equals(""))) {
			edCurrentUserActivityName.setText(NewActivityName);
			//.
			if ((NewActivityInfo != null) && (!NewActivityInfo.equals("")))
				edCurrentUserActivityInfo.setText(NewActivityInfo);
			else
				edCurrentUserActivityInfo.setText("");
		}
        //.
        flExists = true;
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		if (CurrentActivitySelecting != null) {
			CurrentActivitySelecting.Cancel();
			CurrentActivitySelecting = null;
		}
		if (CurrentActivitySetting != null) {
			CurrentActivitySetting.Cancel();
			CurrentActivitySetting = null;
		}
		if (CurrentActivityLoading != null) {
			CurrentActivityLoading.Cancel();
			CurrentActivityLoading = null;
		}
		//.
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        //.
		if ((NewActivityName == null) || NewActivityName.equals("")) 
			LoadCurrentActivity();
	}

	private void LoadCurrentActivity() {
		if (CurrentActivityLoading != null) {
			CurrentActivityLoading.Cancel();
			CurrentActivityLoading = null;
		}
		CurrentActivityLoading = new TCurrentActivityLoading();
	}
	
	private void SetCurrentActivity(TActivity NewActivity) {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
			CurrentActivitySetting = new TCurrentActivitySetting(NewActivity);
		else
			try {
				Activities_Start(NewActivity);
			} catch (Exception E) {
				Activities_DoOnException(E);
			}
	}
	
	private void RestartActivityForCurrent() {
		CurrentActivitySelecting = new TCurrentActivitySelecting(true);
	}
	
	private void SelectCurrentActivity() {
		CurrentActivitySelecting = new TCurrentActivitySelecting(false);
	}
	
	private class TCurrentActivityLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivityLoading() {
    		super();
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				TActivity CurrentActivity;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				CurrentActivity = UserAgent.Server.User.GetUserCurrentActivity();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(MESSAGE_LOADCURRENTACTIVITY,CurrentActivity).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserActivityPanel.this, TUserActivityPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserActivityPanel.this);    
		            	progressDialog.setMessage(TUserActivityPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserActivityPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
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
		
    private class TCurrentActivitySetting extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TActivity NewActivity;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivitySetting(TActivity pNewActivity) {
    		super();
    		//.
    		NewActivity = pNewActivity;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				if (NewActivity != null)
    					UserAgent.Server.User.StartUserActivity(NewActivity);
    				else
    					UserAgent.Server.User.FinishUserCurrentActivity();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(MESSAGE_SETCURRENTACTIVITY,NewActivity).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserActivityPanel.this, TUserActivityPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserActivityPanel.this);    
		            	progressDialog.setMessage(TUserActivityPanel.this.getString(R.string.SSettingACurrentActivity));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(false);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
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
		
    private void Activities_Start(TActivity pActivity) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	//.
    	final TActivity Activity = pActivity;
    	//.
    	if (!Activity.IsValid())
        	ServiceOperation = Tracker.GeoLog.TaskModule.StartUserActivity(Activity.Name,Activity.Info, new TUserActivityIsStartedHandler() {
        		@Override
        		public void DoOnUserActivityIsStarted(int idActivity) {
        			Activity.ID = idActivity;
        			Activities_DoOnUserActivityIsStarted(Activity);
        		}
        	}, new TTaskDataValue.TExceptionHandler() {
        		@Override
        		public void DoOnException(Exception E) {
        			Activities_DoOnException(E);  						
        		}
        	});
    	else
        	ServiceOperation = Tracker.GeoLog.TaskModule.RestartUserActivity(Activity.ID, new TUserActivityIsStartedHandler() {
        		@Override
        		public void DoOnUserActivityIsStarted(int idActivity) {
        			Activities_DoOnUserActivityIsStarted(Activity);
        		}
        	}, new TTaskDataValue.TExceptionHandler() {
        		@Override
        		public void DoOnException(Exception E) {
        			Activities_DoOnException(E);  						
        		}
        	});
    	//.
    	Activity.SetAsUnknown();
    	TMyUserPanel.SetUserActivity(Activity);
    	//.
		PanelHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Activities_DoOnUserActivityIsStarted(TActivity pActivity) {
		PanelHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
    	PanelHandler.obtainMessage(MESSAGE_ONACTIVITYISSTARTED,pActivity).sendToTarget();
    }
    
    private void Activities_DoOnException(Exception E) {
		PanelHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		PanelHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
	private class TCurrentActivitySelecting extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private boolean flRestart = false;
    	
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivitySelecting(boolean pflRestart) {
    		super();
    		//.
    		flRestart = pflRestart;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				TActivities UserActivities;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				UserActivities = UserAgent.Server.User.GetUserActivityList(TActivity.MinTimestamp,TActivity.MaxTimestamp);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
				if (flRestart)
		    		PanelHandler.obtainMessage(MESSAGE_RESTARTACTIVITYFORCURRENT,UserActivities).sendToTarget();
				else
		    		PanelHandler.obtainMessage(MESSAGE_SELECTCURRENTACTIVITY,UserActivities).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserActivityPanel.this, TUserActivityPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserActivityPanel.this);    
		            	progressDialog.setMessage(TUserActivityPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
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
		
	public final Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_EXCEPTION:
    				if (!flExists)
    	            	break; //. >
                	Exception E = (Exception)msg.obj;
                    Toast.makeText(TUserActivityPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	//.
                	break; //. >
                	
                case MESSAGE_PROGRESSBAR_SHOW:
                	progressDialog = new ProgressDialog(TUserActivityPanel.this);    
                	progressDialog.setMessage(TUserActivityPanel.this.getString(R.string.SLoading));    
                	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
                	progressDialog.setIndeterminate(true); 
                	progressDialog.setCancelable(true);
                	progressDialog.setOnCancelListener( new OnCancelListener() {
            			@Override
            			public void onCancel(DialogInterface arg0) {
                        	Intent intent = TUserActivityPanel.this.getIntent();
                        	intent.putExtra("ActivityID",(long)0);
                        	//.
                			setResult(RESULT_OK,intent);
            				TUserActivityPanel.this.finish();
            			}
            		});
                	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserActivityPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
                		@Override 
                		public void onClick(DialogInterface dialog, int which) { 
                        	Intent intent = TUserActivityPanel.this.getIntent();
                        	intent.putExtra("ActivityID",(long)0);
                        	//.
                			setResult(RESULT_OK,intent);
            				TUserActivityPanel.this.finish();
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
                
                case MESSAGE_LOADCURRENTACTIVITY: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		 CurrentActivity = (TActivity)msg.obj;
                		 //.
                		 if (CurrentActivity != null) {
                    		 edCurrentUserActivityName.setText(CurrentActivity.Name);
                    		 edCurrentUserActivityInfo.setText(CurrentActivity.Info);
                		 }
                		 else {
                    		 edCurrentUserActivityName.setText("");
                    		 edCurrentUserActivityInfo.setText("");
                		 }
                	}
                	catch (Exception Ex) {
                		Toast.makeText(TUserActivityPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                    
                case MESSAGE_SETCURRENTACTIVITY:
    				if (!flExists)
    	            	break; //. >
    				TActivity Activity = (TActivity)msg.obj;
    				//.
                	Intent intent = TUserActivityPanel.this.getIntent();
                	if (Activity != null)
                		intent.putExtra("ActivityID",Activity.ID);
                	else
                		intent.putExtra("ActivityID",(long)0);
                	//.
                	setResult(RESULT_OK,intent);
                	finish();
                	break; //. >

                case MESSAGE_RESTARTACTIVITYFORCURRENT: {
    				if (!flExists)
    	            	break; //. >
                	final TActivities UserActivities = (TActivities)msg.obj;
    				//.
    				final CharSequence[] _items = new CharSequence[UserActivities.Items.length];
    				int SelectedIdx = -1;
    				for (int I = 0; I < UserActivities.Items.length; I++) 
    					_items[I] = UserActivities.Items[I].GetInfo(TUserActivityPanel.this);
    				//.
    				AlertDialog.Builder builder = new AlertDialog.Builder(TUserActivityPanel.this);
    				builder.setTitle(R.string.SLastUserActivities);
    				builder.setNegativeButton(R.string.SCancel,null);
    				builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface arg0, int arg1) {
    						CurrentActivity = UserActivities.Items[arg1];
    						//.
    						SetCurrentActivity(CurrentActivity);
    						//.
    						arg0.dismiss();
    					}
    				});
    				AlertDialog alert = builder.create();
    				alert.show();
                	break; //. >
                }
                	
                case MESSAGE_SELECTCURRENTACTIVITY: {
    				if (!flExists)
    	            	break; //. >
                	final TActivities UserActivities = (TActivities)msg.obj;
    				//.
    				final CharSequence[] _items = new CharSequence[UserActivities.Items.length];
    				int SelectedIdx = -1;
    				for (int I = 0; I < UserActivities.Items.length; I++) 
    					_items[I] = UserActivities.Items[I].GetInfo(TUserActivityPanel.this);
    				//.
    				AlertDialog.Builder builder = new AlertDialog.Builder(TUserActivityPanel.this);
    				builder.setTitle(R.string.SLastUserActivities);
    				builder.setNegativeButton(R.string.SCancel,null);
    				builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface arg0, int arg1) {
    						CurrentActivity = UserActivities.Items[arg1];
    						//.
    						CurrentActivity.ID = 0;
    						SetCurrentActivity(CurrentActivity);
    						//.
    						arg0.dismiss();
    					}
    				});
    				AlertDialog alert = builder.create();
    				alert.show();
                	break; //. >
                	}
                	
                case MESSAGE_ONACTIVITYISSTARTED:
    				if (!flExists)
    	            	break; //. >
    				Activity = (TActivity)msg.obj;
    				//.
                	intent = TUserActivityPanel.this.getIntent();
                	if (Activity != null)
                		intent.putExtra("ActivityID",Activity.ID);
                	else
                		intent.putExtra("ActivityID",(long)0);
                	//.
                	setResult(RESULT_OK,intent);
                	finish();
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };	
}
