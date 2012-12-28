package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TReflectorConfigurationPanel extends Activity {

	public static final int REQUEST_REGISTERNEWUSER 			= 1;
	public static final int REQUEST_CONSTRUCTNEWTRACKEROBJECT 	= 2;
	
	private TReflector Reflector;
	private TableLayout _TableLayout;
	private TextView edServerAddress;
	private TextView edUserID;
	private TextView edUserPassword;
	private TextView edGeoSpaceID;
	private Button btnRegisterNewUser;
	private Button btnSpaceLays;
	private Button btnClearReflections;
	private CheckBox cbUseTrackerService;
	private CheckBox cbTrackerServerConnection;
	private TextView edTrackerServerAddress;
	private TextView edTrackerServerPort;
	private TextView edTrackerServerObjectID;
	private TextView edTrackerOpQueueTransmitInterval;
	private TextView edTrackerPositionReadInterval;
	private TextView edTrackerPOIMapID;
	private CheckBox cbTrackerSaveOpQueue;
	private CheckBox cbTrackerVideoModuleEnabled;
	private Button btnConstructNewTrackerObject;
	private Button btnTrackerVideoModulePropsPanel;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.GetReflector();
        // Setup the window
        setContentView(R.layout.reflector_configuration_panel);
        _TableLayout = (TableLayout)findViewById(R.id.ReflectorConfigurationTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edServerAddress = (TextView)findViewById(R.id.edServerAddress);
        edUserID = (TextView)findViewById(R.id.edUserID); 
        edUserPassword = (TextView)findViewById(R.id.edUserPassword);
        edGeoSpaceID = (TextView)findViewById(R.id.edGeoSpaceID);
        btnRegisterNewUser = (Button)findViewById(R.id.btnRegisterNewUser);
        btnRegisterNewUser.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	RegisterNewUser();
            }
        });
        btnSpaceLays = (Button)findViewById(R.id.btnSpaceLays);
        btnSpaceLays.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ShowSpaceLays();
            }
        });
        btnClearReflections = (Button)findViewById(R.id.btnClearReflections);
        btnClearReflections.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	new TVisualizationsClearing(true);            
            }
        });
        cbUseTrackerService = (CheckBox)findViewById(R.id.cbUseTrackerService);
        cbUseTrackerService.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
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
    	cbTrackerServerConnection.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	    		cbTrackerSaveOpQueue.setEnabled(!arg1);
			}
        });        
    	edTrackerServerObjectID = (TextView)findViewById(R.id.edTrackerServerObjectID);
        btnConstructNewTrackerObject = (Button)findViewById(R.id.btnConstructNewTrackerObject);
        btnConstructNewTrackerObject.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ConstructNewTrackerObject();
            }
        });
        edTrackerServerAddress = (TextView)findViewById(R.id.edTrackerServerAddress);
        edTrackerServerPort = (TextView)findViewById(R.id.edTrackerServerPort);
    	edTrackerOpQueueTransmitInterval = (TextView)findViewById(R.id.edTrackerOpQueueTransmitInterval);
    	edTrackerPositionReadInterval = (TextView)findViewById(R.id.edTrackerPositionReadInterval);
    	edTrackerPOIMapID = (TextView)findViewById(R.id.edTrackerPOIMapID);
    	cbTrackerSaveOpQueue = (CheckBox)findViewById(R.id.cbTrackerSaveOpQueue);
    	cbTrackerVideoModuleEnabled = (CheckBox)findViewById(R.id.cbTrackerVideoModuleEnabled);
    	cbTrackerVideoModuleEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		    	///- btnTrackerVideoModulePropsPanel.setEnabled(arg1);
			}
        });        
    	btnTrackerVideoModulePropsPanel = (Button)findViewById(R.id.btnTrackerVideoModulePropsPanel);
    	btnTrackerVideoModulePropsPanel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
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
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
        //.
        Update();
    }

    @Override
    protected void onDestroy() {
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
            		String UserPassword = extras.getString("UserPassword");
            		//.
            		edUserID.setText(Integer.toString(UserID));
            		edUserPassword.setText(UserPassword);
            		//.
                	Save();
                	//.
                    setResult(Activity.RESULT_OK);
                    finish();
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
                	edTrackerServerObjectID.setText(Integer.toString(GeographServerObjectID));
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
	
    private void Update() {
    	edServerAddress.setText(Reflector.Configuration.ServerAddress);
    	if (Reflector.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) {
    		edUserID.setText(Integer.toString(Reflector.Configuration.UserID));
    		btnRegisterNewUser.setEnabled(false);
    	}
    	else { 
    		edUserID.setText(R.string.SAnonymouse);
    		btnRegisterNewUser.setEnabled(true);
    	}
    	edUserPassword.setText(Reflector.Configuration.UserPassword);
    	edGeoSpaceID.setText(Integer.toString(Reflector.Configuration.GeoSpaceID));
    	//.
    	cbUseTrackerService.setChecked(Reflector.Configuration.GeoLog_flEnabled);
    	cbTrackerServerConnection.setChecked(Reflector.Configuration.GeoLog_flServerConnection);
    	edTrackerServerAddress.setText(Reflector.Configuration.GeoLog_ServerAddress);
    	edTrackerServerPort.setText(Integer.toString(Reflector.Configuration.GeoLog_ServerPort));
    	edTrackerServerObjectID.setText(Integer.toString(Reflector.Configuration.GeoLog_ObjectID));
    	btnConstructNewTrackerObject.setEnabled((Reflector.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) && (Reflector.Configuration.GeoLog_ObjectID == 0));
    	edTrackerOpQueueTransmitInterval.setText(Integer.toString(Reflector.Configuration.GeoLog_QueueTransmitInterval));
    	edTrackerPositionReadInterval.setText(Integer.toString(Reflector.Configuration.GeoLog_GPSModuleProviderReadInterval));
    	edTrackerPOIMapID.setText(Integer.toString(Reflector.Configuration.GeoLog_GPSModuleMapID));
    	cbTrackerSaveOpQueue.setChecked(Reflector.Configuration.GeoLog_flSaveQueue);
    	cbTrackerVideoModuleEnabled.setChecked(Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled);
    	EnableDisableTrackerItems(Reflector.Configuration.GeoLog_flEnabled);
    }
    
    private void EnableDisableTrackerItems(boolean flEnable) {
    	if (flEnable) {
        	cbTrackerServerConnection.setEnabled(true);
        	edTrackerServerAddress.setEnabled(true);
        	edTrackerServerPort.setEnabled(true);
        	edTrackerServerObjectID.setEnabled(true);
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
    	Reflector.Configuration.UserID = Integer.parseInt(edUserID.getText().toString());
    	Reflector.Configuration.UserPassword = edUserPassword.getText().toString();
    	Reflector.Configuration.GeoSpaceID = Integer.parseInt(edGeoSpaceID.getText().toString());
    	//.
    	Reflector.Configuration.GeoLog_flEnabled = cbUseTrackerService.isChecked();
    	Reflector.Configuration.GeoLog_flServerConnection = cbTrackerServerConnection.isChecked();
    	Reflector.Configuration.GeoLog_ServerAddress = edTrackerServerAddress.getText().toString();
    	Reflector.Configuration.GeoLog_ServerPort = Integer.parseInt(edTrackerServerPort.getText().toString());
    	Reflector.Configuration.GeoLog_ObjectID = Integer.parseInt(edTrackerServerObjectID.getText().toString());
    	Reflector.Configuration.GeoLog_QueueTransmitInterval = Integer.parseInt(edTrackerOpQueueTransmitInterval.getText().toString());
    	Reflector.Configuration.GeoLog_GPSModuleProviderReadInterval = Integer.parseInt(edTrackerPositionReadInterval.getText().toString());
    	Reflector.Configuration.GeoLog_GPSModuleMapID = Integer.parseInt(edTrackerPOIMapID.getText().toString());
    	Reflector.Configuration.GeoLog_flSaveQueue = cbTrackerSaveOpQueue.isChecked();
    	Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled = cbTrackerVideoModuleEnabled.isChecked();
    	//.
    	try {
    		Reflector.Configuration.Save();
    		Reflector.Configuration.Validate();
    	}
    	catch (Exception E) {
            Toast.makeText(this, getString(R.string.SErrorOfSavingConfiguration)+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
}
