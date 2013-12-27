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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TReflectorConfigurationPanel extends Activity {

	public static final int REQUEST_REGISTERNEWUSER 			= 1;
	public static final int REQUEST_CONSTRUCTNEWTRACKEROBJECT 	= 2;
	public static final int REQUEST_SETUSERACTIVITY			 	= 3;
	
	private TReflector Reflector;
	private TableLayout _TableLayout;
	private TextView edServerAddress;
	private TextView edUserID;
	private TextView edUserName;
	private TextView edUserPassword;
	private TextView edGeoSpaceID;
	private Button btnRegisterNewUser;
	private Button btnUserCurrentActivity;
	private TextView 	lbContext;
	private Button 		btnClearContext;
	private Button 		btnClearReflections;
	private Button 		btnSpaceLays;
	private CheckBox cbUseTrackerService;
	private CheckBox cbTrackerServerConnection;
	private TextView edTrackerServerAddress;
	private TextView edTrackerServerPort;
	private TextView edTrackerServerObjectID;
	private TextView edTrackerServerObjectName;
	private TextView edTrackerOpQueueTransmitInterval;
	private CheckBox cbTrackerSaveOpQueue;
	private TextView edTrackerPositionReadInterval;
	private TextView edTrackerPOIMapID;
	private CheckBox cbTrackerVideoModuleEnabled;
	private Button btnConstructNewTrackerObject;
	private Button btnTrackerVideoModulePropsPanel;
	private CheckBox 	cbTrackerHide;
	private boolean 	cbTrackerHide_flChanged = false;
	//.
	private TGeoScopeServerUser.TUserDescriptor.TActivity UserCurrentActivity = null;
	//.
	private TUpdating	Updating = null;
    private boolean 	flUpdating = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.GetReflector();
        //.
        setContentView(R.layout.reflector_configuration_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.ReflectorConfigurationTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edServerAddress = (TextView)findViewById(R.id.edServerAddress);
        edUserID = (TextView)findViewById(R.id.edUserID); 
        edUserName = (TextView)findViewById(R.id.edUserName); 
        edUserPassword = (TextView)findViewById(R.id.edUserPassword);
        edGeoSpaceID = (TextView)findViewById(R.id.edGeoSpaceID);
        btnRegisterNewUser = (Button)findViewById(R.id.btnRegisterNewUser);
        btnRegisterNewUser.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	RegisterNewUser();
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivity.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(TReflectorConfigurationPanel.this, TUserActivityPanel.class);
            	startActivityForResult(intent,REQUEST_SETUSERACTIVITY);
            }
        });
        lbContext = (TextView)findViewById(R.id.lbContext);
        btnClearContext = (Button)findViewById(R.id.btnClearContext);
        btnClearContext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	new TContextClearing(true);            
            }
        });
        btnClearReflections = (Button)findViewById(R.id.btnClearReflections);
        btnClearReflections.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	new TVisualizationsClearing(true);            
            }
        });
        btnSpaceLays = (Button)findViewById(R.id.btnSpaceLays);
        btnSpaceLays.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ShowSpaceLays();
            }
        });
        cbUseTrackerService = (CheckBox)findViewById(R.id.cbUseTrackerService);
        cbUseTrackerService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				try {
					EnableDisableTrackerItems(arg1);
		    	}
		    	catch (Exception E) {
		            Toast.makeText(Reflector, getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
			}
        });        
    	cbTrackerServerConnection = (CheckBox)findViewById(R.id.cbTrackerServerConnection);
    	cbTrackerServerConnection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	    		cbTrackerSaveOpQueue.setEnabled(!arg1);
			}
        });        
    	edTrackerServerObjectID = (TextView)findViewById(R.id.edTrackerServerObjectID);
    	edTrackerServerObjectName = (TextView)findViewById(R.id.edTrackerServerObjectName);
        btnConstructNewTrackerObject = (Button)findViewById(R.id.btnConstructNewTrackerObject);
        btnConstructNewTrackerObject.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	ConstructNewTrackerObject();
            }
        });
        edTrackerServerAddress = (TextView)findViewById(R.id.edTrackerServerAddress);
        edTrackerServerPort = (TextView)findViewById(R.id.edTrackerServerPort);
    	edTrackerOpQueueTransmitInterval = (TextView)findViewById(R.id.edTrackerOpQueueTransmitInterval);
    	edTrackerPositionReadInterval = (TextView)findViewById(R.id.edTrackerPositionReadInterval);
    	edTrackerPOIMapID = (TextView)findViewById(R.id.edTrackerPOIMapID);
    	cbTrackerVideoModuleEnabled = (CheckBox)findViewById(R.id.cbTrackerVideoModuleEnabled);
    	cbTrackerVideoModuleEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		    	btnTrackerVideoModulePropsPanel.setEnabled(arg1);
			}
        });        
    	btnTrackerVideoModulePropsPanel = (Button)findViewById(R.id.btnTrackerVideoModulePropsPanel);
    	btnTrackerVideoModulePropsPanel.setOnClickListener(new OnClickListener() {
    		
            public void onClick(View v) {
            	if (flUpdating) 
            		return; //. ->
            	try {
            		TReflectorConfigurationPanel.this.finish();
            		//.
            		TTracker Tracker = TTracker.GetTracker();
            		if (Tracker != null)
            			Tracker.GeoLog.VideoRecorderModule.ShowPropsPanel(TReflectorConfigurationPanel.this);
            	}
            	catch (Exception E) {
                    Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            }
        });
    	cbTrackerSaveOpQueue = (CheckBox)findViewById(R.id.cbTrackerSaveOpQueue);
    	cbTrackerHide = (CheckBox)findViewById(R.id.cbTrackerHide);
    	cbTrackerHide.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				cbTrackerHide_flChanged = true;
			}
        });        
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
        //. StartUpdating();
        Update();
	}

    @Override
    protected void onDestroy() {
		if (Updating != null)
			Updating.Cancel();
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reflector_configuration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.Configuration_btnOk:
        	Save();
        	//.
            setResult(Activity.RESULT_OK);
            finish();
            return true; //. >
            
        case R.id.Configuration_btnCancel:
            finish();
            return true; //. >        
        }
        
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_REGISTERNEWUSER: 
        	if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int UserID = extras.getInt("UserID");
            		String UserName = extras.getString("UserName");
            		String UserPassword = extras.getString("UserPassword");
            		//.
            		edUserID.setText(Integer.toString(UserID));
            		if (!UserName.equals("")) {
                		edUserName.setText(UserName);
                		edUserName.setVisibility(View.VISIBLE);
            		}
            		else {
                		edUserName.setText("");
                		edUserName.setVisibility(View.GONE);
            		}
            		edUserPassword.setText(UserPassword);
            		//.
                	Save();
                    setResult(Activity.RESULT_OK);
                	//.
        		    new AlertDialog.Builder(TReflectorConfigurationPanel.this)
        	        .setIcon(android.R.drawable.ic_dialog_alert)
        	        .setTitle(R.string.SConfirmation)
        	        .setMessage(R.string.SDoYouWantToCreateTrackerObjectForThisDevice)
        		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
        		    	public void onClick(DialogInterface dialog, int id) {
        	            	ConstructNewTrackerObject();
        		    	}
        		    })
        		    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
        		    	public void onClick(DialogInterface dialog, int id) {
                            finish();
        		    	}
        		    })
        		    .show();
                }
        	}  
            break; //. >
            
        case REQUEST_CONSTRUCTNEWTRACKEROBJECT: 
        	if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
					String 	Name = extras.getString("Name");
					int 	ComponentID = extras.getInt("ComponentID"); 
                	String 	GeographServerAddress = extras.getString("GeographServerAddress");
                	int 	GeographServerPort = extras.getInt("GeographServerPort");
                	int 	GeographServerObjectID = extras.getInt("GeographServerObjectID");
                	//.
                	Reflector.CoGeoMonitorObjects.AddItem(ComponentID, Name, false);
                	//.
                	cbUseTrackerService.setChecked(true);
                	cbTrackerServerConnection.setChecked(true);
                	edTrackerServerObjectID.setText(Integer.toString(GeographServerObjectID));
                	if (!Name.equals("")) {
                		edTrackerServerObjectName.setText(Name);
                		edTrackerServerObjectName.setVisibility(View.VISIBLE);
                	}
                	else {
                		edTrackerServerObjectName.setText("");
                		edTrackerServerObjectName.setVisibility(View.GONE);
                	}
                	edTrackerServerAddress.setText(GeographServerAddress);
                	edTrackerServerPort.setText(Integer.toString(GeographServerPort));
            		//.
                	Save();
                	//.
                    setResult(Activity.RESULT_OK);
                    finish();
                }
        	}  
            break; //. >

        case REQUEST_SETUSERACTIVITY: 
        	if (resultCode == RESULT_OK) 
        		StartUpdating();
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void RegisterNewUser() {
    	Intent intent = new Intent(this, TNewUserRegistrationPanel.class);
    	startActivityForResult(intent,REQUEST_REGISTERNEWUSER);
    }
    
    private void ConstructNewTrackerObject() {
    	Intent intent = new Intent(this, TNewTrackerObjectConstructionPanel.class);
    	startActivityForResult(intent,REQUEST_CONSTRUCTNEWTRACKEROBJECT);
    }
    
    public void ShowSpaceLays() {
    	TSpaceLays Lays = Reflector.ReflectionWindow.getLays();
    	if (Lays != null)
    		Lays.CreateLaySelectorPanel(this).show();
    }

    private class TContextClearing extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private boolean flCloseAfterDone;
    	
        private ProgressDialog progressDialog; 
    	
    	public TContextClearing(boolean pflCloseAfterDone) {
    		flCloseAfterDone = pflCloseAfterDone;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
            		TTypesSystem.TypesSystem.Context_Clear();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
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
	                Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DONE:
	            	Reflector.StartUpdatingCurrentSpaceImage();
	            	//.
	            	if (flCloseAfterDone)
	            		finish();
            		//.
                    Toast.makeText(Reflector, R.string.SContextIsCleared, Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorConfigurationPanel.this);    
	            	progressDialog.setMessage(TReflectorConfigurationPanel.this.getString(R.string.SClearingContext));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
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
	
    private class TVisualizationsClearing extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private boolean flCloseAfterDone;
    	
        private ProgressDialog progressDialog; 
    	
    	public TVisualizationsClearing(boolean pflCloseAfterDone) {
    		flCloseAfterDone = pflCloseAfterDone;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
            		Reflector.ClearVisualizations(false);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
        	}
        	catch (IOException E) {
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
	                Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DONE:
	            	Reflector.StartUpdatingCurrentSpaceImage();
	            	//.
	            	if (flCloseAfterDone)
	            		finish();
            		//.
                    Toast.makeText(Reflector, R.string.SImageStorageIsCleared, Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorConfigurationPanel.this);    
	            	progressDialog.setMessage(TReflectorConfigurationPanel.this.getString(R.string.SClearingImageStorage));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
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
    				TGeoScopeServerUser.TUserDescriptor.TActivity UserCurrentActivity;
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				UserCurrentActivity = UserAgent.Server.User.GetUserCurrentActivity();
					}
					finally {
						if (flShowProgress)
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
					}
    				//.
	    			MessageHandler.obtainMessage(MESSAGE_COMPLETED,UserCurrentActivity).sendToTarget();
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
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	if (Canceller.flCancel)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMPLETED:
           		 	UserCurrentActivity = (TGeoScopeServerUser.TUserDescriptor.TActivity)msg.obj;
           		 	//.
	            	TReflectorConfigurationPanel.this.Update();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_FINISHED:
	            	TReflectorConfigurationPanel.this.Updating = null;
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorConfigurationPanel.this);    
	            	progressDialog.setMessage(TReflectorConfigurationPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
							//.
							if (flClosePanelOnCancel)
								TReflectorConfigurationPanel.this.finish();
						}
					});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TReflectorConfigurationPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
							Cancel();
							//.
							if (flClosePanelOnCancel)
								TReflectorConfigurationPanel.this.finish();
	            		} 
	            	}); 
	            	//.
	            	progressDialog.show(); 	            	
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_HIDE:
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
        
    private void Update() {
    	flUpdating = true;
    	try {
        	edServerAddress.setText(Reflector.Configuration.ServerAddress);
        	if (Reflector.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) {
        		edUserID.setText(Integer.toString(Reflector.Configuration.UserID));
        		btnRegisterNewUser.setEnabled(false);
        	}
        	else { 
        		edUserID.setText(R.string.SAnonymouse);
        		btnRegisterNewUser.setEnabled(true);
        	}
    		if (!Reflector.Configuration.UserName.equals("")) {
        		edUserName.setText(Reflector.Configuration.UserName);
        		edUserName.setVisibility(View.VISIBLE);
    		}
    		else {
        		edUserName.setText("");
        		edUserName.setVisibility(View.GONE);
    		}
        	edUserPassword.setText(Reflector.Configuration.UserPassword);
        	edGeoSpaceID.setText(Integer.toString(Reflector.Configuration.GeoSpaceID));
        	//.
        	if ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0)) 
        		btnUserCurrentActivity.setText(UserCurrentActivity.Name); 
        	else
        		btnUserCurrentActivity.setText(R.string.SNone1); 
        	//.
        	lbContext.setText(getString(R.string.SContext)+" "+"("+getString(R.string.SFilling)+": "+Integer.toString((int)(100.0*TSpace.Space.Context.Storage.DeviceFillFactor()))+" %"+")");
        	//.
        	cbUseTrackerService.setChecked(Reflector.Configuration.GeoLog_flEnabled);
        	cbTrackerServerConnection.setChecked(Reflector.Configuration.GeoLog_flServerConnection);
        	edTrackerServerAddress.setText(Reflector.Configuration.GeoLog_ServerAddress);
        	edTrackerServerPort.setText(Integer.toString(Reflector.Configuration.GeoLog_ServerPort));
        	edTrackerServerObjectID.setText(Integer.toString(Reflector.Configuration.GeoLog_ObjectID));
        	if (!Reflector.Configuration.GeoLog_ObjectName.equals("")) {
        		edTrackerServerObjectName.setText(Reflector.Configuration.GeoLog_ObjectName);
        		edTrackerServerObjectName.setVisibility(View.VISIBLE);
        	}
        	else {
        		edTrackerServerObjectName.setText("");
        		edTrackerServerObjectName.setVisibility(View.GONE);
        	}
        	btnConstructNewTrackerObject.setEnabled((Reflector.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) && (Reflector.Configuration.GeoLog_ObjectID == 0));
        	edTrackerOpQueueTransmitInterval.setText(Integer.toString(Reflector.Configuration.GeoLog_QueueTransmitInterval));
        	cbTrackerSaveOpQueue.setChecked(Reflector.Configuration.GeoLog_flSaveQueue);
        	edTrackerPositionReadInterval.setText(Integer.toString(Reflector.Configuration.GeoLog_GPSModuleProviderReadInterval));
        	edTrackerPOIMapID.setText(Integer.toString(Reflector.Configuration.GeoLog_GPSModuleMapID));
        	cbTrackerVideoModuleEnabled.setChecked(Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled);
        	cbTrackerHide.setChecked(Reflector.Configuration.GeoLog_flHide);
        	//.
        	EnableDisableTrackerItems(Reflector.Configuration.GeoLog_flEnabled);
    	}
    	finally {
    		flUpdating = false;
    	}
    }
   
    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }
    
    private void EnableDisableTrackerItems(boolean flEnable) {
    	if (flEnable) {
        	cbTrackerServerConnection.setEnabled(true);
        	edTrackerServerAddress.setEnabled(true);
        	edTrackerServerPort.setEnabled(true);
        	edTrackerServerObjectID.setEnabled(true);
        	edTrackerServerObjectName.setEnabled(true);
        	btnConstructNewTrackerObject.setEnabled((Reflector.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) && (Reflector.Configuration.GeoLog_ObjectID == 0));
        	edTrackerOpQueueTransmitInterval.setEnabled(true); 
        	edTrackerPositionReadInterval.setEnabled(true);
        	edTrackerPOIMapID.setEnabled(true);
    		cbTrackerSaveOpQueue.setEnabled(!Reflector.Configuration.GeoLog_flServerConnection);
        	cbTrackerVideoModuleEnabled.setEnabled(true);
        	btnTrackerVideoModulePropsPanel.setEnabled(Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled);
    	}
    	else {
        	cbTrackerServerConnection.setEnabled(false);
        	edTrackerServerAddress.setEnabled(false);
        	edTrackerServerPort.setEnabled(false);
        	edTrackerServerObjectID.setEnabled(false);
        	edTrackerServerObjectName.setEnabled(false);
        	btnConstructNewTrackerObject.setEnabled(false);
        	edTrackerOpQueueTransmitInterval.setEnabled(false);
        	edTrackerPositionReadInterval.setEnabled(false);
        	edTrackerPOIMapID.setEnabled(false);
    		cbTrackerSaveOpQueue.setEnabled(false);
        	cbTrackerVideoModuleEnabled.setEnabled(false);
        	btnTrackerVideoModulePropsPanel.setEnabled(false);
    	}
    }
    
    private void Save() {
    	Reflector.Configuration.ServerAddress = edServerAddress.getText().toString();
    	try {
        	Reflector.Configuration.UserID = Integer.parseInt(edUserID.getText().toString());
    	}
    	catch (NumberFormatException NFE) {
        	Reflector.Configuration.UserID = TGeoScopeServerUser.AnonymouseUserID;    		
    	}
    	Reflector.Configuration.UserName = edUserName.getText().toString();
    	Reflector.Configuration.UserPassword = edUserPassword.getText().toString();
    	Reflector.Configuration.GeoSpaceID = Integer.parseInt(edGeoSpaceID.getText().toString());
    	//.
    	Reflector.Configuration.GeoLog_flEnabled = cbUseTrackerService.isChecked();
    	Reflector.Configuration.GeoLog_flServerConnection = cbTrackerServerConnection.isChecked();
    	Reflector.Configuration.GeoLog_ServerAddress = edTrackerServerAddress.getText().toString();
    	Reflector.Configuration.GeoLog_ServerPort = Integer.parseInt(edTrackerServerPort.getText().toString());
    	Reflector.Configuration.GeoLog_ObjectID = Integer.parseInt(edTrackerServerObjectID.getText().toString());
    	Reflector.Configuration.GeoLog_ObjectName = edTrackerServerObjectName.getText().toString();
    	Reflector.Configuration.GeoLog_QueueTransmitInterval = Integer.parseInt(edTrackerOpQueueTransmitInterval.getText().toString());
    	Reflector.Configuration.GeoLog_GPSModuleProviderReadInterval = Integer.parseInt(edTrackerPositionReadInterval.getText().toString());
    	Reflector.Configuration.GeoLog_GPSModuleMapID = Integer.parseInt(edTrackerPOIMapID.getText().toString());
    	Reflector.Configuration.GeoLog_flSaveQueue = cbTrackerSaveOpQueue.isChecked();
    	Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled = cbTrackerVideoModuleEnabled.isChecked();
    	Reflector.Configuration.GeoLog_flHide = cbTrackerHide.isChecked();
    	//.
    	try {
    		Reflector.Configuration.flChanged = true;
    		Reflector.Configuration.Save();
    		//.
    		if (cbTrackerHide_flChanged)
    			Reflector.WorkSpace_Buttons_Recreate(true);
    		//.
    		Reflector.Configuration.Validate();
    		//.
    		cbTrackerHide_flChanged = false;
    	}
    	catch (Exception E) {
            Toast.makeText(this, getString(R.string.SErrorOfSavingConfiguration)+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
}
