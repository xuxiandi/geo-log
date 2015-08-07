package com.geoscope.GeoEye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.TTrackerPanel.TConfiguration;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TTrackerConfigurationPanel extends Activity {

	public static final int REASON_SELECT_METERS = 1;
	
	
	private TTracker Tracker;
	//.
    private TTrackerPanel.TConfiguration Configuration;
    //.
    @SuppressWarnings("unused")
	private TSensorMeterInfo[] 	SensorsModuleMeters;
	@SuppressWarnings("unused")
	private Spinner 			spMeterToControl;
	private EditText 			edMetersToControl;
	private Button				btnSelectMetersToControl;
	private Spinner 			spMeterControl;
	private Spinner 			spControlNotifications;
	private Spinner 			spRecordingNotification;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        try {
        	Tracker = TTracker.GetTracker(this.getApplicationContext());
        	if (Tracker == null)
        		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    		//.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		if ((android.os.Build.VERSION.SDK_INT >= 14) && (!ViewConfiguration.get(this).hasPermanentMenuKey())) 
    			requestWindowFeature(Window.FEATURE_ACTION_BAR);
            //.
            setContentView(R.layout.tracker_configuration_panel);
            //.
            Configuration = new TConfiguration();
    		Configuration.Load();
            //.
            /* spMeterToControl = (Spinner)findViewById(R.id.spMeterToControl);
            SensorsModuleMeters = Tracker.GeoLog.SensorsModule.Meters.Items_GetList();
            int Cnt = SensorsModuleMeters.length;
            String[] MeterItems = new String[Cnt+1]; 
            for (int I = 0; I < Cnt; I++) {
				String S = SensorsModuleMeters[I].Descriptor.Name;
				if (SensorsModuleMeters[I].Descriptor.Info.length() > 0)
					S += " "+"/"+SensorsModuleMeters[I].Descriptor.Info+"/";
				//.
            	MeterItems[I] = S;
            }
        	MeterItems[Cnt] = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.VideoRecorderModuleMeterID;
            ArrayAdapter<String> saMeterToControlControl = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MeterItems);
            saMeterToControlControl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMeterToControl.setAdapter(saMeterToControlControl);
            spMeterToControl.setOnItemSelectedListener(new OnItemSelectedListener() {
            	
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                	try {
                		if (position < SensorsModuleMeters.length) 
                    		Configuration.SensorsModuleConfiguration.MeterToControl = SensorsModuleMeters[position].Descriptor.ID;
                		else
                			Configuration.SensorsModuleConfiguration.MeterToControl = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.VideoRecorderModuleMeterID;
                		//.
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                	}
    		    	catch (Exception E) {
    		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });*/
    		//.
            edMetersToControl = (EditText)findViewById(R.id.edMetersToControl);
            edMetersToControl.setKeyListener(null);
            //.
            btnSelectMetersToControl = (Button)findViewById(R.id.btnSelectMetersToControl);
            btnSelectMetersToControl.setOnClickListener(new OnClickListener() {
            	
    			@Override
                public void onClick(View v) {
            		Intent intent = new Intent(TTrackerConfigurationPanel.this, com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.TSensorsMetersPanel.class);
			    	intent.putExtra("Mode", com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.TSensorsMetersPanel.MODE_SELECTOR); 
			    	intent.putExtra("IDs", Configuration.SensorsModuleConfiguration.MeterToControl); 
                    startActivityForResult(intent, REASON_SELECT_METERS);
                }
            });
            //.
            spMeterControl = (Spinner)findViewById(R.id.spMeterControl);
            String[] ControlItems = new String[3]; 
            ControlItems[0] = getString(R.string.SNone2);
            ControlItems[1] = getString(R.string.S2TapsStartManualStop);
            ControlItems[2] = getString(R.string.S2TapsStart3TapsStop);
            ArrayAdapter<String> saVideoRecorderModuleControl = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ControlItems);
            saVideoRecorderModuleControl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMeterControl.setAdapter(saVideoRecorderModuleControl);
            spMeterControl.setOnItemSelectedListener(new OnItemSelectedListener() {
            	
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                	try {
                    	switch (position) {
                    	
                    	case 0:
                    		Configuration.SensorsModuleConfiguration.MeterControl = TTrackerPanel.TConfiguration.CONTROL_NONE;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case 1: 
                    		Configuration.SensorsModuleConfiguration.MeterControl = TTrackerPanel.TConfiguration.CONTROL_2TAPSSTARTMANUALSTOP;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case 2: 
                    		Configuration.SensorsModuleConfiguration.MeterControl = TTrackerPanel.TConfiguration.CONTROL_2TAPSSTART3TAPSSTOP;
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
                		Configuration.SensorsModuleConfiguration.MeterControl = TTrackerPanel.TConfiguration.CONTROL_NONE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                	}
    		    	catch (Exception E) {
    		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
                }
            });        
            //.
            spControlNotifications = (Spinner)findViewById(R.id.spControlNotifications);
            ControlItems = new String[4]; 
            ControlItems[0] = getString(R.string.SNone2);
            ControlItems[1] = getString(R.string.SVibration2Beats);
            ControlItems[2] = getString(R.string.SVoice);
            ControlItems[3] = getString(R.string.SVibration2BeatsAndVoice);
            ArrayAdapter<String> saVideoRecorderModuleNotifications = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ControlItems);
            saVideoRecorderModuleNotifications.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spControlNotifications.setAdapter(saVideoRecorderModuleNotifications);
            spControlNotifications.setOnItemSelectedListener(new OnItemSelectedListener() {
            	
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                	try {
                    	switch (position) {
                    	
                    	case 0:
                    		Configuration.SensorsModuleConfiguration.MeterControlNotifications = TTrackerPanel.TConfiguration.NOTIFICATIONS_NONE;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case 1: 
                    		Configuration.SensorsModuleConfiguration.MeterControlNotifications = TTrackerPanel.TConfiguration.NOTIFICATIONS_VIBRATION2BITS;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case 2: 
                    		Configuration.SensorsModuleConfiguration.MeterControlNotifications = TTrackerPanel.TConfiguration.NOTIFICATIONS_VOICE;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case 3: 
                    		Configuration.SensorsModuleConfiguration.MeterControlNotifications = TTrackerPanel.TConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE;
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
                		Configuration.SensorsModuleConfiguration.MeterControlNotifications = TTrackerPanel.TConfiguration.NOTIFICATIONS_NONE;
                		Configuration.Save();
                        setResult(Activity.RESULT_OK);
                	}
    		    	catch (Exception E) {
    		            Toast.makeText(TTrackerConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
                }
            });    
            //.
            spRecordingNotification = (Spinner)findViewById(R.id.spRecordingNotification);
            String[] RecordingNotificationItems = new String[3]; 
            RecordingNotificationItems[TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE] = getString(R.string.SNone);
            RecordingNotificationItems[TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING] = getString(R.string.SBeeping);
            RecordingNotificationItems[TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING] = getString(R.string.SVibrating);
            ArrayAdapter<String> saRecordingNotification = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, RecordingNotificationItems);
            saRecordingNotification.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRecordingNotification.setAdapter(saRecordingNotification);
            spRecordingNotification.setOnItemSelectedListener(new OnItemSelectedListener() {
            	
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                	try {
                    	switch (position) {
                    	
                    	case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE:
        	        		Configuration.SensorsModuleConfiguration.MeterRecordingNotification = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING: 
        	        		Configuration.SensorsModuleConfiguration.MeterRecordingNotification = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING;
                    		Configuration.Save();
                            setResult(Activity.RESULT_OK);
                    		break; //. >
                    		
                    	case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING: 
        	        		Configuration.SensorsModuleConfiguration.MeterRecordingNotification = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING;
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
    	        		Configuration.SensorsModuleConfiguration.MeterRecordingNotification = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE;
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
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			//.
			finish();
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	try {
            switch (requestCode) {        

            case REASON_SELECT_METERS:
            	if (resultCode == RESULT_OK) {  
                    Bundle extras = data.getExtras(); 
                    if (extras != null) {
                    	String MetersToControl = extras.getString("IDs");
                    	//.
            			Configuration.SensorsModuleConfiguration.MeterToControl = MetersToControl;
                		Configuration.Save();
                		//.
                		Update();
                    }
            	}
            	break; //. >
            }
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void Update() {
    	/*TSensorMeter MeterToControl = Tracker.GeoLog.SensorsModule.Meters.Items_GetItem(Configuration.SensorsModuleConfiguration.MeterToControl);
		int Cnt = SensorsModuleMeters.length;
    	int SelectedIdx = Cnt;
    	if (MeterToControl != null) 
    		for (int I = 0; I < Cnt; I++)
    			if (SensorsModuleMeters[I].Descriptor.ID.equals(MeterToControl.Descriptor.ID)) {
    				SelectedIdx = I;
    				break; //. >
    			}
    	if (SelectedIdx >= 0)
            spMeterToControl.setSelection(SelectedIdx);*/
    	//.
    	String MetersToControl = "";
    	String[] _MetersToControl = Configuration.SensorsModuleConfiguration.MeterToControl.split(",");
		int CntI = _MetersToControl.length;
    	for (int I = 0; I < CntI; I++) {
        	TSensorMeter MeterToControl = Tracker.GeoLog.SensorsModule.Meters.Items_GetItem(_MetersToControl[I]);
        	if (MeterToControl != null) { 
				String S = MeterToControl.Descriptor.GetText();
				//.
				if (MetersToControl.length() == 0)
					MetersToControl = S;
				else
					MetersToControl += "\n"+S;
        	}
    	}
    	if (MetersToControl.length() == 0)
    		MetersToControl = TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.VideoRecorderModuleMeterID;
    	edMetersToControl.setText(MetersToControl);
    	//.
        switch (Configuration.SensorsModuleConfiguration.MeterControl) {
        
        case TTrackerPanel.TConfiguration.CONTROL_NONE:
            spMeterControl.setSelection(0);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.CONTROL_2TAPSSTARTMANUALSTOP:
            spMeterControl.setSelection(1);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.CONTROL_2TAPSSTART3TAPSSTOP:
            spMeterControl.setSelection(2);
        	break; //. >
        }
        //.
        switch (Configuration.SensorsModuleConfiguration.MeterControlNotifications) {
        
        case TTrackerPanel.TConfiguration.NOTIFICATIONS_NONE:
            spControlNotifications.setSelection(0);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.NOTIFICATIONS_VIBRATION2BITS:
            spControlNotifications.setSelection(1);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.NOTIFICATIONS_VOICE:
            spControlNotifications.setSelection(2);
        	break; //. >

        case TTrackerPanel.TConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE:
            spControlNotifications.setSelection(3);
        	break; //. >
        }
        //.
        switch (Configuration.SensorsModuleConfiguration.MeterRecordingNotification) {
        
        case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE:
            spRecordingNotification.setSelection(TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING:
            spRecordingNotification.setSelection(TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING);
        	break; //. >
            
        case TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING:
            spRecordingNotification.setSelection(TTrackerPanel.TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING);
        	break; //. >
        }
    }
}
