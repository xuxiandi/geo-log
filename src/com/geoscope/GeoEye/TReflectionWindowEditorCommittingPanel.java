package com.geoscope.GeoEye;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;

public class TReflectionWindowEditorCommittingPanel extends Activity {

	public static final int COMMITTING_RESULT_COMMIT 								= 1;
	public static final int COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES 			= 2;
	public static final int COMMITTING_RESULT_COMMIT_VISUALIZATION 					= 3;
	public static final int COMMITTING_RESULT_COMMIT_VISUALIZATION_ENQUEUEDRAWING	= 4;
	public static final int COMMITTING_RESULT_DEFER 								= 5;
	public static final int COMMITTING_RESULT_DELETE 								= 6;
	
	private String 	PlaceName = "";
	private int 	UserSecurityFileID = 0;
	private int		UserSecurityFileIDForCommit = 0;
	private boolean flPrivate = false;
	private boolean flReSet = true;
	private double ReSetInterval = 1.0;
	private String DataFileName = "";
	private String DataName = "";
	//.
	private EditText 	edPlaceName;
	private boolean 	edPlaceName_flSynchronizing = false;
	private CheckBox cbPrivate;
	private CheckBox cbReSet;
	private LinearLayout llReSetInterval;
	private Spinner spReSetIntervalSelector;
	private Button btnCommit;
	private Button btnEnqueueChangedTiles;
	private Button btnDefer;
	private Button btnDelete;
	private Button btnCancel;
	private EditText 	edVisualizationName;
	private boolean 	edVisualizationName_flSynchronizing = false;
	private EditText edAttachmentFileName;
	private Button btnAttachmentFileName;
	private CheckBox cbVisualizationPrivate;
	private Button btnVisualizationCommit;
	private Button btnVisualizationEnqueueDrawing;
	
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
        	DataFileName = extras.getString("DataFileName");
        	DataName = extras.getString("DataName");
        	//.
        	flPrivate = (UserSecurityFileIDForCommit != 0);
        }
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.reflectionwindow_editor_committing_panel);
        //.
        TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
		tabs.setup();
		TabHost.TabSpec spec = tabs.newTabSpec("tag1");
		spec.setContent(R.id.llDrawingCommitting);
		spec.setIndicator(getString(R.string.SDrawOnMap));
		tabs.addTab(spec);
		spec = tabs.newTabSpec("tag2");
		spec.setContent(R.id.llVisualizationCommitting);
		spec.setIndicator(getString(R.string.SCreateNewVisualization));
		tabs.addTab(spec);
		tabs.setCurrentTab(0);        
	    //.
        edPlaceName = (EditText)findViewById(R.id.edRWEditorCommittingPlaceName);
        edPlaceName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            	if (edVisualizationName_flSynchronizing)
            		return; //. ->
            	edPlaceName_flSynchronizing = true;
            	try {
                	edVisualizationName.setText(s);
            	}
            	finally {
                	edPlaceName_flSynchronizing = false;
            	}
            }
        });        
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
            	intent.putExtra("PlaceName",PlaceName);
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("flReSet",flReSet);
            	intent.putExtra("ReSetInterval",ReSetInterval);
            	intent.putExtra("DataFileName",DataFileName);
            	intent.putExtra("DataName",DataName);
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
            	intent.putExtra("PlaceName",PlaceName);
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("flReSet",flReSet);
            	intent.putExtra("ReSetInterval",ReSetInterval);
            	intent.putExtra("DataFileName",DataFileName);
            	intent.putExtra("DataName",DataName);
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
            	VisualizationCommit();
            	//.
            	int USFID = 0;
            	if (flPrivate)
            		USFID = UserSecurityFileID;
            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
            	intent.putExtra("PlaceName",PlaceName);
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("flReSet",flReSet);
            	intent.putExtra("ReSetInterval",ReSetInterval);
            	intent.putExtra("DataFileName",DataFileName);
            	intent.putExtra("DataName",DataName);
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
    	            	VisualizationCommit();
    	            	//.
    	            	int USFID = 0;
    	            	if (flPrivate)
    	            		USFID = UserSecurityFileID;
    	            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
    	            	intent.putExtra("PlaceName",PlaceName);
    	            	intent.putExtra("UserSecurityFileID",USFID);
    	            	intent.putExtra("flReSet",flReSet);
    	            	intent.putExtra("ReSetInterval",ReSetInterval);
    	            	intent.putExtra("DataFileName",DataFileName);
    	            	intent.putExtra("DataName",DataName);
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
	    //.
        edVisualizationName = (EditText)findViewById(R.id.edRWEditorCommittingVisualizationName);
        edVisualizationName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            	if (edPlaceName_flSynchronizing)
            		return; //. ->
            	edVisualizationName_flSynchronizing = true;
            	try {
                	edPlaceName.setText(s);
            	}
            	finally {
            		edVisualizationName_flSynchronizing = false;
            	}
            }
        });        
        //.
        edAttachmentFileName = (EditText)findViewById(R.id.edRWEditorCommittingVisualizationAttachmentFileName);
        edAttachmentFileName.setText(DataFileName);
        //.
        btnAttachmentFileName = (Button)findViewById(R.id.btnRWEditorCommittingVisualizationAttachmentFileName);
        btnAttachmentFileName.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
    			TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TReflectionWindowEditorCommittingPanel.this, null, new TFileSystemFileSelector.OpenDialogListener() {
    	        	
                    @Override
                    public void OnSelectedFile(String fileName) {
                        edAttachmentFileName.setText(fileName);
                    }

        			@Override
        			public void OnCancel() {
        			}
                });
            	FileSelector.show();    	
            }
        });
        //.
        cbVisualizationPrivate = (CheckBox)findViewById(R.id.cbRWEditorCommittingVisualizationPrivate);
    	cbVisualizationPrivate.setChecked(UserSecurityFileIDForCommit != 0);
    	cbVisualizationPrivate.setEnabled(UserSecurityFileID != 0);
        //.
        btnVisualizationCommit = (Button)findViewById(R.id.btnRWEditorCommittingVisualizationCommit);
        btnVisualizationCommit.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	VisualizationCommit();
            	//.
            	int USFID = 0;
            	if (flPrivate)
            		USFID = UserSecurityFileID;
            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
            	intent.putExtra("PlaceName",PlaceName);
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("DataFileName",DataFileName);
            	intent.putExtra("DataName",DataName);
            	intent.putExtra("ResultCode",COMMITTING_RESULT_COMMIT_VISUALIZATION);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnVisualizationEnqueueDrawing = (Button)findViewById(R.id.btnRWEditorCommittingVisualizationEnqueueDrawing);
        btnVisualizationEnqueueDrawing.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	VisualizationCommit();
            	//.
            	int USFID = 0;
            	if (flPrivate)
            		USFID = UserSecurityFileID;
            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
            	intent.putExtra("PlaceName",PlaceName);
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("DataFileName",DataFileName);
            	intent.putExtra("DataName",DataName);
            	intent.putExtra("ResultCode",COMMITTING_RESULT_COMMIT_VISUALIZATION_ENQUEUEDRAWING);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        edPlaceName.setText(PlaceName);
        //.
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
	
	public void VisualizationCommit() {
		try {
			PlaceName = edVisualizationName.getText().toString();
			if (PlaceName.equals(""))
				PlaceName = getString(R.string.SPlace);
			//.
			flPrivate = cbVisualizationPrivate.isChecked();
			DataFileName = edAttachmentFileName.getText().toString();
			DataName = PlaceName;
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    }
	}
}
