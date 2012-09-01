package com.geoscope.GeoEye;

import com.geoscope.GeoLog.TrackerService.TTracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

public class TReflectorConfigurationPanel extends Activity {

	private TReflector Reflector;
	private TableLayout _TableLayout;
	private TextView edServerAddress;
	private TextView edUserID;
	private TextView edUserPassword;
	private TextView edGeoSpaceID;
	private Button btnSpaceLays;
	private Button btnClearReflections;
	private CheckBox cbUseTrackerService;
	private CheckBox cbTrackerServerConnection;
	private TextView edTrackerServerPort;
	private TextView edTrackerServerObjectID;
	private TextView edTrackerOpQueueTransmitInterval;
	private TextView edTrackerPositionReadInterval;
	private TextView edTrackerPOIMapID;
	private CheckBox cbTrackerSaveOpQueue;
	private CheckBox cbTrackerVideoModuleEnabled;
	private Button btnTrackerVideoModulePropsPanel;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        // Setup the window
        setContentView(R.layout.reflector_configuration_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.ReflectorConfigurationTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edServerAddress = (TextView)findViewById(R.id.edServerAddress);
        edUserID = (TextView)findViewById(R.id.edUserID); 
        edUserPassword = (TextView)findViewById(R.id.edUserPassword);
        edGeoSpaceID = (TextView)findViewById(R.id.edGeoSpaceID);
        btnSpaceLays = (Button)findViewById(R.id.btnSpaceLays);
        btnSpaceLays.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ShowSpaceLays();
            }
        });
        btnClearReflections = (Button)findViewById(R.id.btnClearReflections);
        btnClearReflections.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	try {
            		Reflector.ClearVisualizations();
            		//.
            		TReflectorConfigurationPanel.this.finish();
            		//.
                    Toast.makeText(Reflector, "��������� ����������� �������", Toast.LENGTH_SHORT).show();
            	}
            	catch (Exception E) {
                    Toast.makeText(TReflectorConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
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
		            Toast.makeText(Reflector, "������ �������, "+E.getMessage(), Toast.LENGTH_LONG).show();
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
    	edTrackerServerPort = (TextView)findViewById(R.id.edTrackerServerPort);
    	edTrackerServerObjectID = (TextView)findViewById(R.id.edTrackerServerObjectID);
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
        	Intent intent= new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true; //. >
            
        case R.id.Configuration_btnCancel:
            finish();
            return true; //. >        
        }
        
        return false;
    }
    
    public void ShowSpaceLays() {
    	TSpaceLays Lays = Reflector.ReflectionWindow.getLays();
    	if (Lays != null)
    		Lays.CreateLaySelectorPanel(this).show();
    }

    private void Update() {
    	edServerAddress.setText(Reflector.Configuration.ServerAddress);
    	edUserID.setText(Integer.toString(Reflector.Configuration.UserID));
    	edUserPassword.setText(Reflector.Configuration.UserPassword);
    	edGeoSpaceID.setText(Integer.toString(Reflector.Configuration.GeoSpaceID));
    	//.
    	cbUseTrackerService.setChecked(Reflector.Configuration.GeoLog_flEnabled);
    	cbTrackerServerConnection.setChecked(Reflector.Configuration.GeoLog_flServerConnection);
    	edTrackerServerPort.setText(Integer.toString(Reflector.Configuration.GeoLog_ServerPort));
    	edTrackerServerObjectID.setText(Integer.toString(Reflector.Configuration.GeoLog_ObjectID));
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
        	edTrackerServerPort.setEnabled(true);
        	edTrackerServerObjectID.setEnabled(true);
        	edTrackerOpQueueTransmitInterval.setEnabled(true);
        	edTrackerPositionReadInterval.setEnabled(true);
        	edTrackerPOIMapID.setEnabled(true);
    		cbTrackerSaveOpQueue.setEnabled(!Reflector.Configuration.GeoLog_flServerConnection);
        	cbTrackerVideoModuleEnabled.setEnabled(true);
        	btnTrackerVideoModulePropsPanel.setEnabled(Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled);
    	}
    	else {
        	cbTrackerServerConnection.setEnabled(false);
        	edTrackerServerPort.setEnabled(false);
        	edTrackerServerObjectID.setEnabled(false);
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
            Toast.makeText(this, "������ ���������� ������������, "+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
}