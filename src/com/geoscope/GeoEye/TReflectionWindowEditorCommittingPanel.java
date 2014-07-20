package com.geoscope.GeoEye;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class TReflectionWindowEditorCommittingPanel extends Activity {

	public static final int COMMITTING_RESULT_COMMIT 						= 1;
	public static final int COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES 	= 2;
	public static final int COMMITTING_RESULT_DEFER 						= 3;
	public static final int COMMITTING_RESULT_DELETE 						= 4;
	
	private String 	PlaceName = "";
	private int 	UserSecurityFileID = 0;
	private int		UserSecurityFileIDForCommit = 0;
	private boolean flPrivate = false;
	private boolean flReSet = true;
	private double ReSetInterval = 1.0;
	//.
	private EditText edPlaceName;
	private CheckBox cbPrivate;
	private CheckBox cbReSet;
	private LinearLayout llReSetInterval;
	private Spinner spReSetIntervalSelector;
	private Button btnCommit;
	private Button btnEnqueueChangedTiles;
	private Button btnDefer;
	private Button btnDelete;
	private Button btnCancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	UserSecurityFileID = extras.getInt("UserSecurityFileID");
        	//.
        	PlaceName = extras.getString("PlaceName");
        	UserSecurityFileIDForCommit = extras.getInt("UserSecurityFileIDForCommit"); 
        	ReSetInterval = extras.getDouble("ReSetInterval");
        	flPrivate = (UserSecurityFileIDForCommit != 0);
        }
        //.
        setContentView(R.layout.reflectionwindow_editor_committing_panel);
        //.
        edPlaceName = (EditText)findViewById(R.id.edRWEditorCommittingPlaceName);
        edPlaceName.setText(PlaceName);
        //.
        cbPrivate = (CheckBox)findViewById(R.id.cbRWEditorCommittingPrivate);
    	cbPrivate.setChecked(UserSecurityFileIDForCommit != 0);
    	cbPrivate.setEnabled(UserSecurityFileID != 0);
        //.
        cbReSet = (CheckBox)findViewById(R.id.cbRWEditorCommittingReset);
        boolean flReset = (ReSetInterval == 0.0);
        cbReSet.setChecked(flReset); 
        cbReSet.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
		        if (checked)
		        	llReSetInterval.setVisibility(LinearLayout.GONE);
		        else
		        	llReSetInterval.setVisibility(LinearLayout.VISIBLE);
            }
        });        
        llReSetInterval = (LinearLayout)findViewById(R.id.llRWEditorCommittingReSetInterval);
        if (cbReSet.isChecked())
        	llReSetInterval.setVisibility(LinearLayout.GONE);
        else
        	llReSetInterval.setVisibility(LinearLayout.VISIBLE);
        //.
        spReSetIntervalSelector = (Spinner)findViewById(R.id.spRWEditorCommittingReSetIntervalSelector);
        String[] SA = new String[3];
        SA[0] = getString(R.string.S1Day);
        SA[1] = getString(R.string.S1Week);
        SA[2] = getString(R.string.S1Month);
        ///? SA[3] = getString(R.string.S1Year);
        ///? SA[4] = getString(R.string.SAlways);
        ArrayAdapter<String> saReSetIntervalSelector = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saReSetIntervalSelector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReSetIntervalSelector.setAdapter(saReSetIntervalSelector);
        spReSetIntervalSelector.setSelection(2);
        //.
        btnCommit = (Button)findViewById(R.id.btnRWEditorCommittingCommit);
        btnCommit.setOnClickListener(new OnClickListener() {
        	@Override
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
            	intent.putExtra("ResultCode",COMMITTING_RESULT_COMMIT);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnEnqueueChangedTiles = (Button)findViewById(R.id.btnRWEditorCommittingEnqueueChangedTiles);
        btnEnqueueChangedTiles.setOnClickListener(new OnClickListener() {
        	@Override
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
            	intent.putExtra("ResultCode",COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnDefer = (Button)findViewById(R.id.btnRWEditorCommittingDefer);
        btnDefer.setOnClickListener(new OnClickListener() {
        	@Override
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
            	intent.putExtra("ResultCode",COMMITTING_RESULT_DEFER);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnDelete = (Button)findViewById(R.id.btnRWEditorCommittingDelete);
        btnDelete.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
    		    new AlertDialog.Builder(TReflectionWindowEditorCommittingPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SDeleteThisDrawings)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
        			@Override
    		    	public void onClick(DialogInterface dialog, int id) {
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
    	            	intent.putExtra("ResultCode",COMMITTING_RESULT_DELETE);
    	                //.
    	            	setResult(Activity.RESULT_OK,intent);
    	            	finish();
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
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
			ReSetInterval = 0.0; //. no interval => always
			if (!flReSet) {
				switch (spReSetIntervalSelector.getSelectedItemPosition()) {

				case 0: 
					ReSetInterval = 1.0; //. 1 day
					break; //. >

				case 1: 
					ReSetInterval = 7.0; //. 1 week
					break; //. >
					
				case 2: 
					ReSetInterval = 31.0; //. 1 month
					break; //. >

				case 3: 
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
