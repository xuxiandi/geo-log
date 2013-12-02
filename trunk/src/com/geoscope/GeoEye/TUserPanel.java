package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TCanceller;

@SuppressLint("HandlerLeak")
public class TUserPanel extends Activity {

	public static final int REQUEST_SETUSERACTIVITY = 1;
	public static final int REQUEST_SHOWONREFLECTOR = 2;
	
	private EditText edUserName;
	private EditText edUserFullName;
	private EditText edUserContactInfo;
	private EditText edUserConnectionState;
	private Button btnUserLocation;
	private Button btnUserMessaging;
	private Button btnUserCurrentActivity;
	private Button btnUserCurrentActivityComponentList;
	private Button btnUserLastActivities;
	//.
	private int 			UserID = 0;	
    private TUserDescriptor UserInfo = null; 
    private TActivity 		UserCurrentActivity = null;
	//.
	private TUpdating	Updating = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) 
        	UserID = extras.getInt("UserID");
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_panel);
        //.
        edUserName = (EditText)findViewById(R.id.edUserName);
        edUserFullName = (EditText)findViewById(R.id.edUserFullName);
        edUserContactInfo = (EditText)findViewById(R.id.edUserContactInfo);
        edUserConnectionState = (EditText)findViewById(R.id.edUserConnectionState);
        btnUserLocation = (Button)findViewById(R.id.btnUserLocation);
        btnUserLocation.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
        		GetUserLocation(UserInfo);
            }
        });
        btnUserMessaging = (Button)findViewById(R.id.btnUserMessaging);
        btnUserMessaging.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
				//.
        		OpenUserMessaging(UserInfo);
            	//.
            	finish();
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivityComponentList = (Button)findViewById(R.id.btnUserCurrentActivityComponentList);
        btnUserCurrentActivityComponentList.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
            	Intent intent = new Intent(TUserPanel.this, TUserActivityComponentListPanel.class);
            	intent.putExtra("UserID",UserInfo.UserID);
            	intent.putExtra("ActivityID",UserCurrentActivity.ID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        btnUserLastActivities = (Button)findViewById(R.id.btnUserLastActivities);
        btnUserLastActivities.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
            	Intent intent = new Intent(TUserPanel.this, TUserActivityListPanel.class);
            	intent.putExtra("UserID",UserInfo.UserID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
	}

	@Override
	protected void onDestroy() {
		if (Updating != null) {
			Updating.CancelAndWait();
			Updating = null;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SETUSERACTIVITY: 
        	if (resultCode == RESULT_OK) 
        		StartUpdating();
            break; //. >

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) 
        		finish();
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        private TUserDescriptor UserInfo = null; 
        private TActivity 		UserCurrentActivity = null;
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
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
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				UserInfo = UserAgent.Server.User.GetUserInfo(UserID);
	    				try {
		    				UserCurrentActivity = UserAgent.Server.User.GetUserCurrentActivity(UserID);
	    				}
	    				catch (Exception E) {
	    					UserCurrentActivity = null;
	    					//.
	    	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
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
		                Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	TUserPanel.this.UserInfo = UserInfo;
		            	TUserPanel.this.UserCurrentActivity = UserCurrentActivity;
	           		 	//.
	           		 	TUserPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	TUserPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserPanel.this);    
		            	progressDialog.setMessage(TUserPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserPanel.this.finish();
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
	
    private void Update() {
    	if (UserInfo != null) {
    		edUserName.setText(UserInfo.UserName);
    		edUserFullName.setText(UserInfo.UserFullName);
    		edUserContactInfo.setText(UserInfo.UserContactInfo);
    	}
    	else {
    		edUserName.setText("");
    		edUserFullName.setText("");
    		edUserContactInfo.setText("");
    	}
    	//.
    	if (UserInfo.UserIsOnline) {
    		edUserConnectionState.setText(getString(R.string.SOnline));
    		edUserConnectionState.setTextColor(Color.GREEN);
    	}
    	else {
    		edUserConnectionState.setText(getString(R.string.SOffline));
    		edUserConnectionState.setTextColor(Color.RED);
    	}
    	//.
    	if ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0)) { 
    		btnUserCurrentActivity.setText(UserCurrentActivity.GetInfo(this,false));
    		btnUserCurrentActivityComponentList.setEnabled(true);
    	}
    	else {
    		btnUserCurrentActivity.setText(R.string.SNone1);
    		btnUserCurrentActivityComponentList.setEnabled(false);
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }    

    private class TUserLocationGetting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private TGeoScopeServerUser.TUserDescriptor User;
    	private boolean flCloseAfterDone;
    	
        private ProgressDialog progressDialog; 
    	
    	public TUserLocationGetting(TGeoScopeServerUser.TUserDescriptor pUser, boolean pflCloseAfterDone) {
    		User = pUser;
    		flCloseAfterDone = pflCloseAfterDone;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				TXYCoord Location;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				Location = _GetUserLocation(User, UserAgent.Server.User, Canceller);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE,Location).sendToTarget();
        	}
			catch (InterruptedException IE) {
			}
			catch (CancelException CE) {
			}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	            	//.
	    		    new AlertDialog.Builder(TUserPanel.this)
	    	        .setIcon(android.R.drawable.ic_dialog_alert)
	    	        .setTitle(R.string.SError)
	    	        .setMessage(TUserPanel.this.getString(R.string.SErrorOfGettingCurrentLocationOfUser)+User.UserName+" ("+User.UserFullName+")"+", "+E.getMessage())
	    		    .setPositiveButton(R.string.SOk, null)
	    		    .show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DONE:
	            	TXYCoord Location = (TXYCoord)msg.obj;
	        		//.
	        		TReflector Reflector = TReflector.GetReflector();
	        		if (Reflector != null) {
		            	Reflector.MoveReflectionWindow(Location);
		            	//.
		            	if (flCloseAfterDone)
		            		finish();
	            		//.
	                    Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SCurrentLocationOfUser)+User.UserName+" ("+User.UserFullName+")", Toast.LENGTH_SHORT).show();
	        		}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserPanel.this);    
	            	progressDialog.setMessage(TUserPanel.this.getString(R.string.SGettingUserLocation));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
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
	
    private TXYCoord _GetUserLocation(TGeoScopeServerUser.TUserDescriptor User, TGeoScopeServerUser MyUser, TCanceller Canceller) throws Exception {
    	TGeoScopeServerUser.TUserLocation UserLocation = MyUser.IncomingMessages_Command_GetUserLocation(User.UserID, TGeoScopeServerUser.TGetUserLocationCommandMessage.Version_ObtainFix, Integer.MAX_VALUE, Canceller);
		switch (UserLocation.Status) {
		
		case TGPSModule.GPSMODULESTATUS_AVAILABLE:
			break; //. >
			
		case TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE:
			throw new Exception(getString(R.string.SLocationIsPermanentlyUnavailable)); //. =>
			
		case TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE:
			throw new Exception(getString(R.string.SLocationIsTemporarilyUnavailable)); //. =>

		case TGPSModule.GPSMODULESTATUS_UNKNOWN:
			throw new Exception(getString(R.string.SLocationIsUnavailableForUnknownReason)); //. =>
		}
		if (!UserLocation.IsAvailable())
			throw new Exception(getString(R.string.SLocationIsNotAvailable)); //. =>
		if (UserLocation.IsNull())
			throw new Exception(getString(R.string.SLocationIsUnknown)); //. =>
		//.
		TReflector Reflector = TReflector.GetReflector();
		if (Reflector == null) 
			throw new Exception(TUserPanel.this.getString(R.string.SReflectorIsNull)); //. =>
		return Reflector.ConvertGeoCoordinatesToXY(UserLocation.Datum,UserLocation.Latitude,UserLocation.Longitude);
    }
    
    private void GetUserLocation(TGeoScopeServerUser.TUserDescriptor User) {
    	new TUserLocationGetting(User,true);
    }    
    
    private void OpenUserMessaging(TGeoScopeServerUser.TUserDescriptor ContactUser) {
		TUserChatPanel UCP = TUserChatPanel.Panels.get(ContactUser.UserID);
		if (UCP != null)
			UCP.finish();
    	Intent intent = new Intent(this, TUserChatPanel.class);
    	intent.putExtra("UserID",ContactUser.UserID);
    	intent.putExtra("UserIsDisabled",ContactUser.UserIsDisabled);
    	intent.putExtra("UserIsOnline",ContactUser.UserIsOnline);
    	intent.putExtra("UserName",ContactUser.UserName);
    	intent.putExtra("UserFullName",ContactUser.UserFullName);
    	intent.putExtra("UserContactInfo",ContactUser.UserContactInfo);
    	startActivity(intent);
    }
}
