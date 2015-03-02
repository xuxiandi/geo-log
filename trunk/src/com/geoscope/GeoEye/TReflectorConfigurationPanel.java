package com.geoscope.GeoEye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TSystemTGeoSpace;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TReflectorConfigurationPanel extends Activity {

	public static final int REQUEST_REGISTERNEWUSER 			= 1;
	public static final int REQUEST_CONSTRUCTNEWTRACKEROBJECT 	= 2;
	public static final int REQUEST_SETUSERACTIVITY			 	= 3;
	
	private TReflectorComponent Component;
	//.
	private TableLayout _TableLayout;
	private TextView edCurrentProfileName;
	private Button btnChangeCurrentProfile;
	private Button btnCloneCurrentProfile;
	private TextView edServerAddress;
	private TextView edUserID;
	private TextView edUserName;
	private TextView edUserPassword;
	private CheckBox cbUserSession;
	private CheckBox cbSecureConnections;
	private Spinner spGeoSpace;
	private CheckBox 	cbVoiceCommands;
	private CheckBox 	cbHitCommands;
	private Button	 	btnHitCommandsSensitivity;
	private CheckBox 	cbAudioNotifications;
	private CheckBox 	cbTMSOption;
	private CheckBox 	cbLargeControlButtons;
	private boolean 	cbLargeControlButtons_flChanged = false;
	private CheckBox 	cbTrackerHide;
	private boolean 	cbTrackerHide_flChanged = false;
	private CheckBox 	cbApplicationQuit;
	private boolean 	cbApplicationQuit_flChanged = false;
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
	private Spinner spPOIMapIDGeoSpace;
	private CheckBox cbTrackerVideoModuleEnabled;
	private Button btnConstructNewTrackerObject;
	private Button btnTrackerVideoModulePropsPanel;
	private Button btnTrackerDataStreamerPropsPanel;
	//.
	private TGeoScopeServerUser.TUserDescriptor.TActivity UserCurrentActivity = null;
	//.
	private TUpdating	Updating = null;
    private boolean 	flUpdating = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
		if (Component == null) {
			finish();
			return; //. ->
		}
		//.
		if ((android.os.Build.VERSION.SDK_INT >= 14) && (!ViewConfiguration.get(this).hasPermanentMenuKey())) 
			requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //.
        setContentView(R.layout.reflector_configuration_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.ReflectorConfigurationTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
    	edCurrentProfileName = (TextView)findViewById(R.id.edCurrentProfileName);
    	btnChangeCurrentProfile = (Button)findViewById(R.id.btnChangeCurrentProfile);
    	btnChangeCurrentProfile.setOnClickListener(new OnClickListener() {
    		@Override
            public void onClick(View v) {
            	ChangeCurrentProfile();
            }
        });
    	btnCloneCurrentProfile = (Button)findViewById(R.id.btnCloneCurrentProfile);
    	btnCloneCurrentProfile.setOnClickListener(new OnClickListener() {
    		@Override
            public void onClick(View v) {
            	CloneCurrentProfile();
            }
        });
        edServerAddress = (TextView)findViewById(R.id.edServerAddress);
        edUserID = (TextView)findViewById(R.id.edUserID); 
        edUserName = (TextView)findViewById(R.id.edUserName); 
        edUserPassword = (TextView)findViewById(R.id.edUserPassword);
        cbUserSession = (CheckBox)findViewById(R.id.cbUserSession);
        cbSecureConnections = (CheckBox)findViewById(R.id.cbSecureConnections);
        //.
        spGeoSpace = (Spinner)findViewById(R.id.spGeoSpace);
        String[] GeoSpaceNames = TSystemTGeoSpace.WellKnownGeoSpaces_GetNames();
        ArrayAdapter<String> saGeoSpace = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GeoSpaceNames);
        saGeoSpace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGeoSpace.setAdapter(saGeoSpace);
        //.
    	cbVoiceCommands = (CheckBox)findViewById(R.id.cbVoiceCommands);
    	cbVoiceCommands.setVisibility((TGeoLogApplication.VoiceRecognizer_GetFolder() != null) ? View.VISIBLE : View.GONE);
    	cbHitCommands = (CheckBox)findViewById(R.id.cbHitCommands);
    	cbHitCommands.setOnClickListener(new OnClickListener(){
    		
            @Override
            public void onClick(View v) {
            	btnHitCommandsSensitivity.setEnabled(cbHitCommands.isChecked());
            }
        });        
    	btnHitCommandsSensitivity = (Button)findViewById(R.id.btnHitCommandsSensitivity);
    	btnHitCommandsSensitivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		HitCommands_ShowSensitivityPanel();
            }
        });
    	cbAudioNotifications = (CheckBox)findViewById(R.id.cbAudioNotifications);
        cbTMSOption = (CheckBox)findViewById(R.id.cbTMSOption);
    	cbLargeControlButtons = (CheckBox)findViewById(R.id.cbLargeControlButtons);
    	cbLargeControlButtons.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				cbLargeControlButtons_flChanged = true;
            }
        });        
    	cbTrackerHide = (CheckBox)findViewById(R.id.cbTrackerHide);
    	cbTrackerHide.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				cbTrackerHide_flChanged = true;
            }
        });        
    	cbApplicationQuit = (CheckBox)findViewById(R.id.cbApplicationQuit);
    	cbApplicationQuit.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				cbApplicationQuit_flChanged = true;
            }
        });        
        //.
        btnRegisterNewUser = (Button)findViewById(R.id.btnRegisterNewUser);
        btnRegisterNewUser.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	RegisterNewUser();
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	Intent intent = new Intent(TReflectorConfigurationPanel.this, TUserActivityPanel.class);
            	startActivityForResult(intent,REQUEST_SETUSERACTIVITY);
            }
        });
        lbContext = (TextView)findViewById(R.id.lbContext);
        btnClearContext = (Button)findViewById(R.id.btnClearContext);
        btnClearContext.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	new TContextClearing(true);            
            }
        });
        btnClearReflections = (Button)findViewById(R.id.btnClearReflections);
        btnClearReflections.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	new TVisualizationsClearing(true);            
            }
        });
        btnSpaceLays = (Button)findViewById(R.id.btnSpaceLays);
        btnSpaceLays.setVisibility((Component.Configuration.UserID == TGeoScopeServerUser.RootUserID) ? View.VISIBLE : View.GONE);
        btnSpaceLays.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	ShowSpaceLays();
            }
        });
        cbUseTrackerService = (CheckBox)findViewById(R.id.cbUseTrackerService);
        cbUseTrackerService.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
				try {
					EnableDisableTrackerItems(checked);
		    	}
		    	catch (Exception E) {
		            Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
    	cbTrackerServerConnection = (CheckBox)findViewById(R.id.cbTrackerServerConnection);
    	cbTrackerServerConnection.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
	    		cbTrackerSaveOpQueue.setEnabled(!checked);
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
    	//.
        spPOIMapIDGeoSpace = (Spinner)findViewById(R.id.spPOIMapIDGeoSpace);
        ArrayAdapter<String> saPOIMapIDGeoSpace = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GeoSpaceNames);
        saPOIMapIDGeoSpace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPOIMapIDGeoSpace.setAdapter(saPOIMapIDGeoSpace);
        //.
    	cbTrackerVideoModuleEnabled = (CheckBox)findViewById(R.id.cbTrackerVideoModuleEnabled);
    	cbTrackerVideoModuleEnabled.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
		    	btnTrackerVideoModulePropsPanel.setEnabled(checked);
            }
        });        
    	btnTrackerVideoModulePropsPanel = (Button)findViewById(R.id.btnTrackerVideoModulePropsPanel);
    	btnTrackerVideoModulePropsPanel.setOnClickListener(new OnClickListener() {
    		@Override
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
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
                    Toast.makeText(TReflectorConfigurationPanel.this, S, Toast.LENGTH_LONG).show();
            	}
            }
        });
    	btnTrackerDataStreamerPropsPanel = (Button)findViewById(R.id.btnTrackerDataStreamerPropsPanel);
    	btnTrackerDataStreamerPropsPanel.setOnClickListener(new OnClickListener() {
    		@Override
            public void onClick(View v) {
            	if (flUpdating) 
            		return; //. ->
            	try {
            		TTracker Tracker = TTracker.GetTracker();
            		if (Tracker != null)
            			Tracker.GeoLog.DataStreamerModule.ShowPropsPanel(TReflectorConfigurationPanel.this);
            	}
            	catch (Exception E) {
                    Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            }
        });
    	cbTrackerSaveOpQueue = (CheckBox)findViewById(R.id.cbTrackerSaveOpQueue);
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
            		long UserID = extras.getLong("UserID");
            		String UserName = extras.getString("UserName");
            		String UserPassword = extras.getString("UserPassword");
            		//.
            		edUserID.setText(Long.toString(UserID));
            		if (!UserName.equals("")) {
                		edUserName.setText(UserName);
                		edUserName.setVisibility(View.VISIBLE);
            		}
            		else {
                		edUserName.setText("");
                		edUserName.setVisibility(View.GONE);
            		}
            		edUserPassword.setText(UserPassword);
            		cbUserSession.setChecked(true);
            		cbSecureConnections.setChecked(false);
            		cbTMSOption.setChecked(false);
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
					int		MapID = extras.getInt("MapID");
					int 	ComponentID = extras.getInt("ComponentID"); 
                	String 	GeographServerAddress = extras.getString("GeographServerAddress");
                	int 	GeographServerPort = extras.getInt("GeographServerPort");
                	int 	GeographServerObjectID = extras.getInt("GeographServerObjectID");
                	//.
                	TReflectorConfigurationPanel.this.Component.CoGeoMonitorObjects.AddItem(ComponentID, Name, false);
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
                	spPOIMapIDGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByPOIMapID(MapID));
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
    
    private void ChangeCurrentProfile() {
		final ArrayList<String> Profiles = TGeoLogApplication.Profiles_GetNames();
		Collections.sort(Profiles, String.CASE_INSENSITIVE_ORDER);
    	final String _CurrentProfileName = TGeoLogApplication.ProfileName();
    	//.
		final CharSequence[] _items;
		int SelectedIdx = -1;
		_items = new CharSequence[Profiles.size()];
		for (int I = 0; I < Profiles.size(); I++) 
			_items[I] = Profiles.get(I);
		for (int I = 0; I < Profiles.size(); I++)
			if (Profiles.get(I).equals(_CurrentProfileName)) {
				SelectedIdx = I;
				break; //. >
			}
		//.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(com.geoscope.GeoEye.R.string.SSelectTheProfile);
		builder.setNegativeButton(R.string.SClose,null);
		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
		    	try {
		        	final String TheCurrentProfileName = Profiles.get(arg1);
		        	//.
		        	if (!TheCurrentProfileName.equals(_CurrentProfileName)) {
		        		final DialogInterface AlertDialog = arg0;
			    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectorConfigurationPanel.this,getString(R.string.SWaitAMoment)) {
			    			@Override
			    			public void Process() throws Exception {
			    				Component.Configuration.Save();
					    		Thread.sleep(100); 
			    			}
			    			@Override 
			    			public void DoOnCompleted() throws Exception {
			    				TReflector.GetReflector().finish();
			    				//.
					        	TGeoLogApplication.SetProfileName(TheCurrentProfileName, getApplicationContext());
					    		//.
					        	AlertDialog.dismiss();
					    		//.
					    		Toast.makeText(TReflectorConfigurationPanel.this, TReflectorConfigurationPanel.this.getString(R.string.SCurrentProfileHasBeenSet)+TheCurrentProfileName, Toast.LENGTH_LONG).show();
					    		//.
					    		TReflectorConfigurationPanel.this.finish();
			    			}
			    			@Override
			    			public void DoOnException(Exception E) {
			    				Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			    			}
			    		};
			    		Processing.Start();
		        	}
		        	else
			    		arg0.dismiss();
		    	}
		    	catch (Exception E) {
		    		Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    		//.
		    		arg0.dismiss();
		    	}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    private void CloneCurrentProfile() {
    	final String _CurrentProfileName = TGeoLogApplication.ProfileName();
    	//.
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	//.
    	alert.setTitle(com.geoscope.GeoEye.R.string.SNewProfile);
    	alert.setMessage(com.geoscope.GeoEye.R.string.SEnterNameForNewProfile);
    	//.
    	final EditText input = new EditText(this);
    	alert.setView(input);
    	//.
    	alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
    		@Override
        	public void onClick(DialogInterface dialog, int whichButton) {
    			//. hide keyboard
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        		//.
        		final String ANewProfileName = input.getText().toString();
        		try {
		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectorConfigurationPanel.this,getString(R.string.SWaitAMoment)) {
		    			
		    			private String NewProfileName;
		    			
		    			@Override
		    			public void Process() throws Exception {
		    				NewProfileName = TGeoLogApplication.Profiles_Clone(_CurrentProfileName, ANewProfileName);
		    				Component.Configuration.Save();
				    		Thread.sleep(100); 
		    			}
		    			@Override 
		    			public void DoOnCompleted() throws Exception {
		    				TReflector.GetReflector().finish();
		        			//. set as current
				        	TGeoLogApplication.SetProfileName(NewProfileName, getApplicationContext());
				    		//.
				    		Toast.makeText(TReflectorConfigurationPanel.this, TReflectorConfigurationPanel.this.getString(R.string.SCurrentProfileHasBeenSet)+NewProfileName, Toast.LENGTH_LONG).show();
				    		//.
				    		TReflectorConfigurationPanel.this.finish();
		    			}
		    			@Override
		    			public void DoOnException(Exception E) {
		    				Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    		};
		    		Processing.Start();
        		}
        		catch (Exception E) {
        			Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        			return; //. ->
    		    }
          	}
    	});
    	//.
    	alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int whichButton) {
    			//. hide keyboard
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    		}
    	});
    	//.
    	alert.show();    
    }
    
    private void HitCommands_ShowSensitivityPanel() {
		final CharSequence[] _items;
		double 	MinDistance = Double.MAX_VALUE;
		int 	MinDistanceThresholdIndex = -1;
		double 	Distance = Math.abs(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold-TMovementDetectorModule.THittingDetector.Threshold_VerySensitive);
		if (Distance < MinDistance) {
			MinDistance = Distance;
			MinDistanceThresholdIndex = 0;
		}
		Distance = Math.abs(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold-TMovementDetectorModule.THittingDetector.Threshold_Sensitive);
		if (Distance < MinDistance) {
			MinDistance = Distance;
			MinDistanceThresholdIndex = 1;
		}
		Distance = Math.abs(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold-TMovementDetectorModule.THittingDetector.Threshold_Moderate);
		if (Distance < MinDistance) {
			MinDistance = Distance;
			MinDistanceThresholdIndex = 2;
		}
		Distance = Math.abs(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold-TMovementDetectorModule.THittingDetector.Threshold_Hard);
		if (Distance < MinDistance) {
			MinDistance = Distance;
			MinDistanceThresholdIndex = 3;
		}
		Distance = Math.abs(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold-TMovementDetectorModule.THittingDetector.Threshold_VeryHard);
		if (Distance < MinDistance) {
			MinDistance = Distance;
			MinDistanceThresholdIndex = 4;
		}
		int SelectedIdx = MinDistanceThresholdIndex;
		_items = new CharSequence[5];
		_items[0] = getString(R.string.SVerySensitive); 
		_items[1] = getString(R.string.SSensitiveLightTap); 
		_items[2] = getString(R.string.SModerateTap); 
		_items[3] = getString(R.string.SHardTap); 
		_items[4] = getString(R.string.SVeryHardTap); 
		//.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SSelectTapSensitivity);
		builder.setNegativeButton(R.string.SClose,null);
		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
		    	try {
		    		switch (arg1) {
		    		
		    		case 0: //. very sensitive
		    			Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_VerySensitive;
		    			//.
    		    		arg0.dismiss();
    		    		//.
    		    		Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.SSensitivityIsChangedTo)+_items[arg1], Toast.LENGTH_LONG).show();
    		    		//.
		    			break; //. >
		    			
		    		case 1: //. sensitive
		    			Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_Sensitive;
		    			//.
    		    		arg0.dismiss();
    		    		//.
    		    		Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.SSensitivityIsChangedTo)+_items[arg1], Toast.LENGTH_LONG).show();
    		    		//.
		    			break; //. >

		    		case 2: //. moderate
		    			Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_Moderate;
		    			//.
    		    		arg0.dismiss();
    		    		//.
    		    		Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.SSensitivityIsChangedTo)+_items[arg1], Toast.LENGTH_LONG).show();
    		    		//.
		    			break; //. >
		    			
		    		case 3: //. hard
		    			Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_Hard;
		    			//.
    		    		arg0.dismiss();
    		    		//.
    		    		Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.SSensitivityIsChangedTo)+_items[arg1], Toast.LENGTH_LONG).show();
    		    		//.
		    			break; //. >

		    		case 4: //. very hard
		    			Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_VeryHard;
		    			//.
    		    		arg0.dismiss();
    		    		//.
    		    		Toast.makeText(TReflectorConfigurationPanel.this, getString(R.string.SSensitivityIsChangedTo)+_items[arg1], Toast.LENGTH_LONG).show();
    		    		//.
		    			break; //. >
		    		}
		    	}
		    	catch (Exception E) {
		    		Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    		//.
		    		arg0.dismiss();
		    	}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    private void RegisterNewUser() {
    	Intent intent = new Intent(this, TNewUserRegistrationPanel.class);
		intent.putExtra("ComponentID", Component.ID);
    	startActivityForResult(intent,REQUEST_REGISTERNEWUSER);
    }
    
    private void ConstructNewTrackerObject() {
    	Intent intent = new Intent(this, TNewTrackerObjectConstructionPanel.class);
		intent.putExtra("ComponentID", Component.ID);
    	startActivityForResult(intent,REQUEST_CONSTRUCTNEWTRACKEROBJECT);
    }
    
    public void ShowSpaceLays() {
    	TSpaceLays Lays = Component.ReflectionWindow.getLays();
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DONE:
		            	Component.StartUpdatingCurrentSpaceImage();
		            	//.
		            	if (flCloseAfterDone)
		            		finish();
	            		//.
	                    Toast.makeText(Component.context, R.string.SContextIsCleared, Toast.LENGTH_SHORT).show();
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
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
    				Component.ClearVisualizations(false);
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DONE:
		            	Component.StartUpdatingCurrentSpaceImage();
		            	//.
		            	if (flCloseAfterDone)
		            		finish();
	            		//.
	                    Toast.makeText(Component.context, R.string.SImageStorageIsCleared, Toast.LENGTH_SHORT).show();
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
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
	        	try {
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
        
    private void Update() {
    	flUpdating = true;
    	try {
    		edCurrentProfileName.setText(TGeoLogApplication.ProfileName());
    		//.
        	edServerAddress.setText(Component.Configuration.ServerAddress);
        	if (Component.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) {
        		edUserID.setText(Long.toString(Component.Configuration.UserID));
        		edUserID.setHint("");
        		btnRegisterNewUser.setEnabled(false);
        	}
        	else { 
        		edUserID.setText("");
        		edUserID.setHint(R.string.SAnonymouse);
        		btnRegisterNewUser.setEnabled(true);
        	}
    		if (!Component.Configuration.UserName.equals("")) {
        		edUserName.setText(Component.Configuration.UserName);
        		edUserName.setVisibility(View.VISIBLE);
    		}
    		else {
        		edUserName.setText("");
        		edUserName.setVisibility(View.GONE);
    		}
        	edUserPassword.setText(Component.Configuration.UserPassword);
    		cbUserSession.setChecked(Component.Configuration.flUserSession);
    		cbSecureConnections.setChecked(Component.Configuration.flSecureConnections);
        	spGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByID(Component.Configuration.GeoSpaceID));
        	//.
        	if ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0)) 
        		btnUserCurrentActivity.setText(UserCurrentActivity.Name); 
        	else
        		btnUserCurrentActivity.setText(R.string.SNone1);
        	//.
        	cbVoiceCommands.setChecked(Component.Configuration.GeoLog_VoiceCommandModuleEnabled);
        	cbHitCommands.setChecked(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorEnabled);
        	btnHitCommandsSensitivity.setEnabled(Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorEnabled);
        	cbAudioNotifications.setChecked(Component.Configuration.GeoLog_flAudioNotifications);
    		cbTMSOption.setChecked(Component.Configuration.ReflectionWindow_flTMSOption);
        	cbLargeControlButtons.setChecked(Component.Configuration.ReflectionWindow_flLargeControlButtons);
        	cbTrackerHide.setChecked(Component.Configuration.GeoLog_flHide);
        	cbApplicationQuit.setChecked(Component.Configuration.Application_flQuitAbility);
        	//.
        	lbContext.setText(getString(R.string.SContext)+" "+"("+getString(R.string.SFilling)+": "+Integer.toString((int)(100.0*TSpace.Space.Context.Storage.DeviceFillFactor()))+" %"+")");
        	//.
        	cbUseTrackerService.setChecked(Component.Configuration.GeoLog_flEnabled);
        	cbTrackerServerConnection.setChecked(Component.Configuration.GeoLog_flServerConnection);
        	edTrackerServerAddress.setText(Component.Configuration.GeoLog_ServerAddress);
        	edTrackerServerPort.setText(Integer.toString(Component.Configuration.GeoLog_ServerPort));
        	edTrackerServerObjectID.setText(Integer.toString(Component.Configuration.GeoLog_ObjectID));
        	if (!Component.Configuration.GeoLog_ObjectName.equals("")) {
        		edTrackerServerObjectName.setText(Component.Configuration.GeoLog_ObjectName);
        		edTrackerServerObjectName.setVisibility(View.VISIBLE);
        	}
        	else {
        		edTrackerServerObjectName.setText("");
        		edTrackerServerObjectName.setVisibility(View.GONE);
        	}
        	btnConstructNewTrackerObject.setEnabled((Component.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) && (Component.Configuration.GeoLog_ObjectID == 0));
        	edTrackerOpQueueTransmitInterval.setText(Integer.toString(Component.Configuration.GeoLog_QueueTransmitInterval));
        	cbTrackerSaveOpQueue.setChecked(Component.Configuration.GeoLog_flSaveQueue);
        	edTrackerPositionReadInterval.setText(Integer.toString(Component.Configuration.GeoLog_GPSModuleProviderReadInterval));
        	spPOIMapIDGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByPOIMapID(Component.Configuration.GeoLog_GPSModuleMapID));
        	cbTrackerVideoModuleEnabled.setChecked(Component.Configuration.GeoLog_VideoRecorderModuleEnabled);
        	//.
        	EnableDisableTrackerItems(Component.Configuration.GeoLog_flEnabled);
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
        	btnConstructNewTrackerObject.setEnabled((Component.Configuration.UserID != TGeoScopeServerUser.AnonymouseUserID) && (Component.Configuration.GeoLog_ObjectID == 0));
        	edTrackerOpQueueTransmitInterval.setEnabled(true); 
        	edTrackerPositionReadInterval.setEnabled(true);
        	spPOIMapIDGeoSpace.setEnabled(true);
    		cbTrackerSaveOpQueue.setEnabled(!Component.Configuration.GeoLog_flServerConnection);
        	cbTrackerVideoModuleEnabled.setEnabled(true);
        	btnTrackerVideoModulePropsPanel.setEnabled(Component.Configuration.GeoLog_VideoRecorderModuleEnabled);
        	btnTrackerDataStreamerPropsPanel.setEnabled(true);
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
        	spPOIMapIDGeoSpace.setEnabled(false);
    		cbTrackerSaveOpQueue.setEnabled(false);
        	cbTrackerVideoModuleEnabled.setEnabled(false);
        	btnTrackerVideoModulePropsPanel.setEnabled(false);
        	btnTrackerDataStreamerPropsPanel.setEnabled(false);
    	}
    }
    
    private void Save() {
    	Component.Configuration.ServerAddress = edServerAddress.getText().toString();
    	try {
        	Component.Configuration.UserID = Long.parseLong(edUserID.getText().toString());
    	}
    	catch (NumberFormatException NFE) {
        	Component.Configuration.UserID = TGeoScopeServerUser.AnonymouseUserID;    		
    	}
    	Component.Configuration.UserName = edUserName.getText().toString();
    	Component.Configuration.UserPassword = edUserPassword.getText().toString();
    	Component.Configuration.flUserSession = cbUserSession.isChecked();
    	Component.Configuration.flSecureConnections = cbSecureConnections.isChecked();
    	int Idx = spGeoSpace.getSelectedItemPosition();
    	if (Idx < 0)
    		Idx = 0;
    	Component.Configuration.GeoSpaceID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].ID;
    	Component.Configuration.GeoLog_VoiceCommandModuleEnabled = cbVoiceCommands.isChecked();
    	Component.Configuration.GeoLog_MovementDetectorModuleHitDetectorEnabled = cbHitCommands.isChecked();
    	Component.Configuration.GeoLog_flAudioNotifications = cbAudioNotifications.isChecked();
    	Component.Configuration.ReflectionWindow_flTMSOption = cbTMSOption.isChecked();
    	Component.Configuration.ReflectionWindow_flLargeControlButtons = cbLargeControlButtons.isChecked();
    	Component.Configuration.GeoLog_flHide = cbTrackerHide.isChecked();
    	Component.Configuration.Application_flQuitAbility = cbApplicationQuit.isChecked();
    	//.
    	Component.Configuration.GeoLog_flEnabled = cbUseTrackerService.isChecked();
    	Component.Configuration.GeoLog_flServerConnection = cbTrackerServerConnection.isChecked();
    	Component.Configuration.GeoLog_ServerAddress = edTrackerServerAddress.getText().toString();
    	Component.Configuration.GeoLog_ServerPort = Integer.parseInt(edTrackerServerPort.getText().toString());
    	Component.Configuration.GeoLog_ObjectID = Integer.parseInt(edTrackerServerObjectID.getText().toString());
    	Component.Configuration.GeoLog_ObjectName = edTrackerServerObjectName.getText().toString();
    	Component.Configuration.GeoLog_QueueTransmitInterval = Integer.parseInt(edTrackerOpQueueTransmitInterval.getText().toString());
    	Component.Configuration.GeoLog_GPSModuleProviderReadInterval = Integer.parseInt(edTrackerPositionReadInterval.getText().toString());
    	Idx = spPOIMapIDGeoSpace.getSelectedItemPosition();
    	if (Idx < 0)
    		Idx = 0;
    	Component.Configuration.GeoLog_GPSModuleMapID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].POIMapID;
    	Component.Configuration.GeoLog_flSaveQueue = cbTrackerSaveOpQueue.isChecked();
    	Component.Configuration.GeoLog_VideoRecorderModuleEnabled = cbTrackerVideoModuleEnabled.isChecked();
    	//.
    	try {
    		Component.Configuration.flChanged = true;
    		Component.Configuration.Save();
    		//.
    		Component.Configuration.Validate((cbLargeControlButtons_flChanged || cbTrackerHide_flChanged),cbApplicationQuit_flChanged);
    		//.
    		cbLargeControlButtons_flChanged = false;
    		cbTrackerHide_flChanged = false;
    		cbApplicationQuit_flChanged = false;
    	}
    	catch (Exception E) {
            Toast.makeText(this, getString(R.string.SErrorOfSavingConfiguration)+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
}
