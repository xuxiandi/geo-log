package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class TReflectionWindowEditorCommittingPanel extends Activity {

	int UserSecurityFileID;
	//.
	private EditText edPlaceName;
	private CheckBox cbPrivate;
	private CheckBox cbReSet;
	private LinearLayout llReSetInterval;
	private Spinner spReSetIntervalSelector;
	private Button btnCommit;
	private Button btnCancel;
	//.
	String 	PlaceName;
	boolean flPrivate;
	boolean flReSet;
	double ReSetInterval;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
		UserSecurityFileID = 0;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	UserSecurityFileID = extras.getInt("UserSecurityFileID");
        }
        //.
        setContentView(R.layout.reflectionwindow_editor_committing_panel);
        //.
        edPlaceName = (EditText)findViewById(R.id.edRWEditorCommittingPlaceName);
        //.
        cbPrivate = (CheckBox)findViewById(R.id.cbRWEditorCommittingPrivate);
    	cbPrivate.setEnabled(UserSecurityFileID != 0);
        //.
        cbReSet = (CheckBox)findViewById(R.id.cbRWEditorCommittingReset);
        cbReSet.setChecked(true); //. default
        cbReSet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		        if (arg1)
		        	llReSetInterval.setVisibility(LinearLayout.GONE);
		        else
		        	llReSetInterval.setVisibility(LinearLayout.VISIBLE);
			}
        });      
        //.
        llReSetInterval = (LinearLayout)findViewById(R.id.llRWEditorCommittingReSetInterval);
        if (cbReSet.isChecked())
        	llReSetInterval.setVisibility(LinearLayout.GONE);
        else
        	llReSetInterval.setVisibility(LinearLayout.VISIBLE);
        //.
        spReSetIntervalSelector = (Spinner)findViewById(R.id.spRWEditorCommittingReSetIntervalSelector);
        String[] SA = new String[5];
        SA[0] = getString(R.string.SAlways);
        SA[1] = getString(R.string.S1Day);
        SA[2] = getString(R.string.S1Week);
        SA[3] = getString(R.string.S1Month);
        SA[4] = getString(R.string.S1Year);
        ArrayAdapter<String> saReSetIntervalSelector = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saReSetIntervalSelector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReSetIntervalSelector.setAdapter(saReSetIntervalSelector);
        spReSetIntervalSelector.setSelection(3);
        //.
        btnCommit = (Button)findViewById(R.id.btnRWEditorCommittingCommit);
        btnCommit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Commit();
            	//.
            	int USFID = 0;
            	if (flPrivate)
            		USFID = UserSecurityFileID;
            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("flReSet",flReSet);
            	intent.putExtra("ReSetInterval",ReSetInterval);
            	intent.putExtra("PlaceName",PlaceName);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnCancel = (Button)findViewById(R.id.btnRWEditorCommittingCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
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
	
	public void Commit() {
		try {
			PlaceName = edPlaceName.getText().toString();
			if (PlaceName.equals(""))
				PlaceName = getString(R.string.SPlace);
			//.
			flPrivate = cbPrivate.isChecked();
			//.
			flReSet = cbReSet.isChecked();
			ReSetInterval = 0.0;
			if (!flReSet) {
				switch (spReSetIntervalSelector.getSelectedItemPosition()) {

				case 1: 
					ReSetInterval = 1.0; //. 1 day
					break; //. >

				case 2: 
					ReSetInterval = 7.0; //. 1 week
					break; //. >
					
				case 3: 
					ReSetInterval = 31.0; //. 1 month
					break; //. >

				case 4: 
					ReSetInterval = 365.0; //. 1 year
					break; //. >
				}
				if (ReSetInterval > 0.0)
					flReSet = true;
			}
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    }
	}
}
