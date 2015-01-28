package com.geoscope.GeoEye;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TReflectorCoGeoMonitorObjectsConfigurationPanel extends Activity  {

	private TReflector Reflector;
	private TReflectorCoGeoMonitorObjects CoGeoMonitorObjects;
	
	private EditText edGMOsUpdateInterval;
	private Button btnSaveGMOsConfiguration;
	private Button btnCloseGMOsConfigurationPanel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.GetReflector();
        CoGeoMonitorObjects = Reflector.Component.CoGeoMonitorObjects;
        //.
        setContentView(R.layout.reflector_gmos_configuration_panel);
        //.
        edGMOsUpdateInterval = (EditText)findViewById(R.id.edGMOsUpdateInterval);
        //.
        btnSaveGMOsConfiguration = (Button)findViewById(R.id.btnSaveGMOsConfiguration);
        btnSaveGMOsConfiguration.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ApplyChanges();
            	//.
            	finish();
            }
        });
        //.
        btnCloseGMOsConfigurationPanel = (Button)findViewById(R.id.btnCloseGMOsConfigurationPanel);
        btnCloseGMOsConfigurationPanel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        //.
        Update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void Update() {
		edGMOsUpdateInterval.setText(Integer.toString(CoGeoMonitorObjects.GetUpdateInterval()));
	}
	
	private void ApplyChanges() {
		CoGeoMonitorObjects.SetUpdateInterval(Integer.parseInt(edGMOsUpdateInterval.getText().toString()));
	}
}
