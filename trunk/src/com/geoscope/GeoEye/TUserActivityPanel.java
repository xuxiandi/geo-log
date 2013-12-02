package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TUserActivityPanel extends Activity {

	private static final int MESSAGE_LOADCURRENTACTIVITY	= 1;
	private static final int MESSAGE_SETCURRENTACTIVITY 	= 2;
	private static final int MESSAGE_SELECTCURRENTACTIVITY 	= 3;
	
	private EditText edCurrentUserActivityName;
	private EditText edCurrentUserActivityInfo;
	private Button btnSetCurrentUserActivity;
	private Button btnSelectCurrentUserActivity;
	private Button btnClearCurrentUserActivity;
	//.
	private TCurrentActivityLoading CurrentActivityLoading = null;
	//.
	private TCurrentActivitySetting CurrentActivitySetting = null;
	//.
	private TCurrentActivitySelecting CurrentActivitySelecting = null;
	//.
	private TGeoScopeServerUser.TUserDescriptor.TActivity CurrentActivity = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activity_panel);
        //.
        edCurrentUserActivityName = (EditText)findViewById(R.id.edCurrentUserActivityName);
        edCurrentUserActivityInfo = (EditText)findViewById(R.id.edCurrentUserActivityInfo);
        btnSetCurrentUserActivity = (Button)findViewById(R.id.btnSetCurrentUserActivity);
        btnSetCurrentUserActivity.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
    			TActivity NewActivity = new TActivity();
    			NewActivity.Name = edCurrentUserActivityName.getText().toString();
    			NewActivity.Info = edCurrentUserActivityInfo.getText().toString();
    			if ((CurrentActivity != null) && NewActivity.Name.equals(CurrentActivity.Name))
    				return; //. ->
            	SetCurrentActivity(NewActivity);
            }
        });
        btnSelectCurrentUserActivity = (Button)findViewById(R.id.btnSelectCurrentUserActivity);
        btnSelectCurrentUserActivity.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SelectCurrentActivity();
            }
        });
        btnClearCurrentUserActivity = (Button)findViewById(R.id.btnClearCurrentUserActivity);
        btnClearCurrentUserActivity.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SetCurrentActivity(null);
            }
        });
        //.
        setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		if (CurrentActivitySelecting != null) {
			CurrentActivitySelecting.CancelAndWait();
			CurrentActivitySelecting = null;
		}
		if (CurrentActivitySetting != null) {
			CurrentActivitySetting.CancelAndWait();
			CurrentActivitySetting = null;
		}
		if (CurrentActivityLoading != null) {
			CurrentActivityLoading.CancelAndWait();
			CurrentActivityLoading = null;
		}
		//.
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        //.
        LoadCurrentActivity();
	}

	private void LoadCurrentActivity() {
		if (CurrentActivityLoading != null) {
			CurrentActivityLoading.CancelAndWait();
			CurrentActivityLoading = null;
		}
		CurrentActivityLoading = new TCurrentActivityLoading();
	}
	
	private void SetCurrentActivity(TActivity NewActivity) {
		CurrentActivitySetting = new TCurrentActivitySetting(NewActivity);
	}
	
	private void SelectCurrentActivity() {
		CurrentActivitySelecting = new TCurrentActivitySelecting();
	}
	
	private class TCurrentActivityLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivityLoading() {
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
		
    private class TCurrentActivitySetting extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TGeoScopeServerUser.TUserDescriptor.TActivity NewActivity;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivitySetting(TGeoScopeServerUser.TUserDescriptor.TActivity pNewActivity) {
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
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
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
	                	progressDialog.dismiss(); 
	                //.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
		
	private class TCurrentActivitySelecting extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
        private ProgressDialog progressDialog; 
    	
    	public TCurrentActivitySelecting() {
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
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
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
	                	progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_LOADCURRENTACTIVITY: 
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
            	catch (Exception E) {
            		Toast.makeText(TUserActivityPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            	break; //. >
                
            case MESSAGE_SETCURRENTACTIVITY:
            	setResult(RESULT_OK);
            	finish();
            	break; //. >

            case MESSAGE_SELECTCURRENTACTIVITY:
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
        }
    };	
}
