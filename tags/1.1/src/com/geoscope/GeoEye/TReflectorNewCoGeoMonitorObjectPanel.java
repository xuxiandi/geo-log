package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TReflectorNewCoGeoMonitorObjectPanel extends Activity {

	private TReflector Reflector;
	
	private EditText edNewGMOID;
	private EditText edNewGMOName;
	private Button btnNewObject;
	private Button btnClosePanel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        //.
        setContentView(R.layout.reflector_new_gmo_panel);
        //.
        edNewGMOID = (EditText)findViewById(R.id.edNewGMOID);
        edNewGMOName = (EditText)findViewById(R.id.edNewGMOName);
        //.
        btnNewObject = (Button)findViewById(R.id.btnNewObject);
        btnNewObject.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewObject();
            	setResult(RESULT_OK);
            	finish();
            }
        });
        //.
        btnClosePanel = (Button)findViewById(R.id.btnClosePanel);
        btnClosePanel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        this.setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void AddNewObject() {
		Reflector.CoGeoMonitorObjects.AddItem(Integer.parseInt(edNewGMOID.getText().toString()),edNewGMOName.getText().toString(),true);
	}
}
