package com.geoscope.GeoEye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.TTrackerPanel.TConfiguration;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TTrackerConfigurationPanel extends Activity {

    private TTrackerPanel.TConfiguration Configuration;
    //.
	private Spinner spVideoRecorderModuleControl;
	private Spinner spVideoRecorderModuleNotifications;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if ((android.os.Build.VERSION.SDK_INT >= 14) && (!ViewConfiguration.get(this).hasPermanentMenuKey())) 
			requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //.
        setContentView(R.layout.tracker_configuration_panel);
        //.
        Configuration = new TConfiguration(TReflector.ProfileFolder()+"/"+TTrackerPanel.ConfigurationFileName);
        try {
			Configuration.Load();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
		}
        //.
        spVideoRecorderModuleControl = (Spinner)findViewById(R.id.spVideoRecorderModuleControl);
        String[] Items = new String[3]; 
        Items[0] = getString(R.string.SNone2);
        Items[1] = getString(R.string.S2TapsStartManualStop);
        Items[2] = getString(R.string.S2TapsStart3TapsStop);
        ArrayAdapter<String> saVideoRecorderModuleControl = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Items);
        saVideoRecorderModuleControl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVideoRecorderModuleControl.setAdapter(saVideoRecorderModuleControl);
        spVideoRecorderModuleControl.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	try {
                	switch (position) {
                	
                	case 0:
                		Configuration.VideoRecorderModuleConfiguration.Control = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_NONE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                		
                	case 1: 
                		Configuration.VideoRecorderModuleConfiguration.Control = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_2TAPSSTARTMANUALSTOP;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                		
                	case 2: 
                		Configuration.VideoRecorderModuleConfiguration.Control = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_2TAPSSTART3TAPSSTOP;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                	}
            	}
		    	catch (Exception E) {
		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	try {
            		Configuration.VideoRecorderModuleConfiguration.Control = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_NONE;
            		Configuration.Save();
                    setResult(Activity.RESULT_OK);
            	}
		    	catch (Exception E) {
		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        //.
        spVideoRecorderModuleNotifications = (Spinner)findViewById(R.id.spVideoRecorderModuleNotifications);
        Items = new String[4]; 
        Items[0] = getString(R.string.SNone2);
        Items[1] = getString(R.string.SVibration2Beats);
        Items[2] = getString(R.string.SVoice);
        Items[3] = getString(R.string.SVibration2BeatsAndVoice);
        ArrayAdapter<String> saVideoRecorderModuleNotifications = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Items);
        saVideoRecorderModuleNotifications.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVideoRecorderModuleNotifications.setAdapter(saVideoRecorderModuleNotifications);
        spVideoRecorderModuleNotifications.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	try {
                	switch (position) {
                	
                	case 0:
                		Configuration.VideoRecorderModuleConfiguration.Notifications = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_NONE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                		
                	case 1: 
                		Configuration.VideoRecorderModuleConfiguration.Notifications = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VIBRATION2BITS;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                		
                	case 2: 
                		Configuration.VideoRecorderModuleConfiguration.Notifications = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VOICE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                		
                	case 3: 
                		Configuration.VideoRecorderModuleConfiguration.Notifications = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                		break; //. >
                	}
            	}
		    	catch (Exception E) {
		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	try {
            		Configuration.VideoRecorderModuleConfiguration.Notifications = TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_NONE;
            		Configuration.Save();
                    setResult(Activity.RESULT_OK);
            	}
		    	catch (Exception E) {
		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        //.
        Update();
        //.
        setResult(Activity.RESULT_CANCELED);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    
    private void Update() {
        TTracker Tracker = TTracker.GetTracker();
    	if (Tracker != null) {
            switch (Configuration.VideoRecorderModuleConfiguration.Control) {
            
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_NONE:
                spVideoRecorderModuleControl.setSelection(0);
            	break; //. >
                
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_2TAPSSTARTMANUALSTOP:
                spVideoRecorderModuleControl.setSelection(1);
            	break; //. >
                
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.CONTROL_2TAPSSTART3TAPSSTOP:
                spVideoRecorderModuleControl.setSelection(2);
            	break; //. >
            }
            //.
            switch (Configuration.VideoRecorderModuleConfiguration.Notifications) {
            
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_NONE:
                spVideoRecorderModuleNotifications.setSelection(0);
            	break; //. >
                
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VIBRATION2BITS:
                spVideoRecorderModuleNotifications.setSelection(1);
            	break; //. >
                
            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VOICE:
                spVideoRecorderModuleNotifications.setSelection(2);
            	break; //. >

            case TTrackerPanel.TConfiguration.TVideoRecorderModuleConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE:
                spVideoRecorderModuleNotifications.setSelection(3);
            	break; //. >
            }
    	}
    }
}
