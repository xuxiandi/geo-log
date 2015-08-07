package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TSystemTGeoSpace;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

@SuppressLint("HandlerLeak")
public class TNewTrackerObjectConstructionPanel extends Activity {

	private static final int MESSAGE_OBJECTISCONSTRUCTED	= 1;
	
	public static class TNewTrackerObjectDescriptor {
		public String 	Name;
		public boolean 	flPrivateAccess;
		public int 		GeoSpaceID;
		public int 		MapID;
		public TGeoScopeServerUser.TTrackerObjectCreationInfo CreationInfo; 
	}
	
	private TReflectorComponent Component;
	//.
	private EditText edNewTrackerObjectName;
	private CheckBox cbNewTrackerObjectPrivateAccess;
	private Spinner spNewTrackerObjectGeoSpace;
	private EditText edNewTrackerObjectMapID;
	private Button btnConstruct;
	//.
	private TNewTrackerObjectConstructing TrackerObjectConstructing = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
        //. 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.newtrackobjectconstruction_panel);
        //.
        edNewTrackerObjectName = (EditText)findViewById(R.id.edNewTrackerObjectName);
        cbNewTrackerObjectPrivateAccess = (CheckBox)findViewById(R.id.cbNewTrackerObjectPrivateAccess);
        //.
        spNewTrackerObjectGeoSpace = (Spinner)findViewById(R.id.spNewTrackerObjectGeoSpace);
        String[] GeoSpaceNames = TSystemTGeoSpace.WellKnownGeoSpaces_GetNames();
        ArrayAdapter<String> saNewTrackerObjectGeoSpace = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GeoSpaceNames);
        saNewTrackerObjectGeoSpace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNewTrackerObjectGeoSpace.setAdapter(saNewTrackerObjectGeoSpace);
        //.
        edNewTrackerObjectMapID = (EditText)findViewById(R.id.edNewTrackerObjectMapID);
        btnConstruct = (Button)findViewById(R.id.btnConstructNewTrackerObject);
        btnConstruct.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Construct();
            }
        });
        //.
        Update();
        //.
        this.setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		if (TrackerObjectConstructing != null) {
			try {
				TrackerObjectConstructing.CancelAndWait();
			} catch (InterruptedException E) {
			}
			TrackerObjectConstructing = null;
		}
		//.
		super.onDestroy();
	}

	private void Update() {
		cbNewTrackerObjectPrivateAccess.setChecked(false);
    	spNewTrackerObjectGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByID(Component.Configuration.GeoSpaceID));
		edNewTrackerObjectMapID.setText(Integer.toString(Component.Configuration.GeoLog_GPSModuleMapID));
	}
	
	private void Validate() throws Exception {
		if (edNewTrackerObjectName.equals(""))
			throw new Exception(getString(R.string.SNameIsEmpty)); //. =>
		if (edNewTrackerObjectName.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
	}
	
	private void Construct() {
		try {
			Validate();
			//.
			TNewTrackerObjectDescriptor NewTrackerObjectDescriptor = new TNewTrackerObjectDescriptor();
			NewTrackerObjectDescriptor.Name = edNewTrackerObjectName.getText().toString();
			NewTrackerObjectDescriptor.flPrivateAccess = cbNewTrackerObjectPrivateAccess.isChecked();
	    	//.
	    	int Idx = spNewTrackerObjectGeoSpace.getSelectedItemPosition();
	    	if (Idx < 0)
	    		Idx = 0;
	    	NewTrackerObjectDescriptor.GeoSpaceID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].ID;
	    	//.
	    	NewTrackerObjectDescriptor.MapID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].POIMapID;
			//.
			if (TrackerObjectConstructing != null) {
				TrackerObjectConstructing.CancelAndWait();
				TrackerObjectConstructing = null;
			}
			TrackerObjectConstructing = new TNewTrackerObjectConstructing(NewTrackerObjectDescriptor, MESSAGE_OBJECTISCONSTRUCTED);
		}
		catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void DoOnObjectIsConstructed(TNewTrackerObjectDescriptor pNewTrackerObjectDescriptor) {
		final TNewTrackerObjectDescriptor NewTrackerObjectDescriptor = pNewTrackerObjectDescriptor;
	    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SObjectCreation)
        .setMessage(getString(R.string.SNewObjectHasBeenCreatedSuccessfully)+"\n"+getString(R.string.SName)+pNewTrackerObjectDescriptor.Name+"\n"+"ID: "+Integer.toString(pNewTrackerObjectDescriptor.CreationInfo.ComponentID)+"\n"+getString(R.string.SProgramConfigurationWillBeChangedForNewTrackerObject))
	    .setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
	    	
	    	public void onClick(DialogInterface dialog, int id) {
            	Intent intent = TNewTrackerObjectConstructionPanel.this.getIntent();
            	intent.putExtra("Name",NewTrackerObjectDescriptor.Name);
            	intent.putExtra("MapID",NewTrackerObjectDescriptor.MapID);
            	intent.putExtra("ComponentID",NewTrackerObjectDescriptor.CreationInfo.ComponentID);
            	intent.putExtra("GeographServerAddress",NewTrackerObjectDescriptor.CreationInfo.GeographServerAddress);
            	intent.putExtra("GeographServerPort",NewTrackerObjectDescriptor.CreationInfo.GeographServerPort);
            	intent.putExtra("GeographServerObjectID",NewTrackerObjectDescriptor.CreationInfo.GeographServerObjectID);
                //.
            	TNewTrackerObjectConstructionPanel.this.setResult(Activity.RESULT_OK,intent);
	    		TNewTrackerObjectConstructionPanel.this.finish();
	    	}
	    })
	    .show();
	}
	
    private class TNewTrackerObjectConstructing extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TNewTrackerObjectDescriptor NewTrackerObjectDescriptor;
		//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TNewTrackerObjectConstructing(TNewTrackerObjectDescriptor pNewTrackerObjectDescriptor, int pOnCompletionMessage) {
    		super();
    		//.
    		NewTrackerObjectDescriptor = pNewTrackerObjectDescriptor;
			//.
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				int SecurityIndex = 0;
    				if (NewTrackerObjectDescriptor.flPrivateAccess)
    					SecurityIndex = 1; //. private access
    				NewTrackerObjectDescriptor.CreationInfo = Component.Server.User.ConstructNewTrackerObject(TDEVICEModule.ObjectBusinessModel,NewTrackerObjectDescriptor.Name,NewTrackerObjectDescriptor.GeoSpaceID,SecurityIndex);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,NewTrackerObjectDescriptor).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) { 
        		if (!isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TNewTrackerObjectConstructionPanel.this, TNewTrackerObjectConstructionPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TNewTrackerObjectConstructionPanel.this);   
		            	progressDialog.setMessage(TNewTrackerObjectConstructionPanel.this.getString(R.string.SConstructingNewObject));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
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
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} 
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
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_OBJECTISCONSTRUCTED: 
                	try {
                		TNewTrackerObjectDescriptor NewTrackerObjectDescriptor = (TNewTrackerObjectDescriptor)msg.obj;
                		//.
                		DoOnObjectIsConstructed(NewTrackerObjectDescriptor);
                	}
                	catch (Exception E) {
                		Toast.makeText(TNewTrackerObjectConstructionPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };	
}
