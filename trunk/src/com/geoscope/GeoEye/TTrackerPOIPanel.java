package com.geoscope.GeoEye;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TSystemTGeoSpace;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOISO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIValue;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TTrackerPOIPanel extends Activity {

	
	public static final long DEFAULT_SECURITY_FILE_ID = 0;
	public static final long PRIVATE_SECURITY_FILE_ID = -1;
	
	public static final int DEFAULT_SECURITY_FILE_INDEX = 0;
	public static final int PRIVATE_SECURITY_FILE_INDEX = 1;
	public static final int OTHER_SECURITY_FILE_INDEX 	= 2;
	
	private final int MENU_CANCEL = 0;
	private final int MENU_CREATE = 1;
	
	private Spinner spPOIMapIDGeoSpace;
	public EditText edPOIName;
	private Spinner spSecurity;
	public CheckBox cbPOIModifyLast;
	private Button 	btnOk;
	//.
	public int 		MapID;
	public int 		POIType = 0x2c04; //. unknown POI type
	public String  	POIName = "";
	public boolean 	flPOIPrivateSecurity = false;
	public boolean 	flPOIModifyLast = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
        setContentView(R.layout.tracker_poi_panel);
    	//.
        spPOIMapIDGeoSpace = (Spinner)findViewById(R.id.spPOIMapIDGeoSpace);
        String[] GeoSpaceNames = TSystemTGeoSpace.WellKnownGeoSpaces_GetNames();
        ArrayAdapter<String> saPOIMapIDGeoSpace = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GeoSpaceNames);
        saPOIMapIDGeoSpace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPOIMapIDGeoSpace.setAdapter(saPOIMapIDGeoSpace);
        //.
        edPOIName = (EditText)findViewById(R.id.edPOIName);
        //.
    	//.
        String[] SecuritySA = new String[2];
        SecuritySA[DEFAULT_SECURITY_FILE_INDEX] = getString(R.string.SDefault);
        SecuritySA[PRIVATE_SECURITY_FILE_INDEX] = getString(R.string.SPrivate);
        //. SecuritySA[OTHER_SECURITY_FILE_INDEX] = getString(R.string.SOther);
        spSecurity = (Spinner)findViewById(R.id.spSecurity);
        ArrayAdapter<String> saSecurity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SecuritySA);
        saSecurity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecurity.setAdapter(saSecurity);
        spSecurity.setOnItemSelectedListener(new OnItemSelectedListener() {
        	
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	switch (position) {
            	
            	case OTHER_SECURITY_FILE_INDEX:
            		break; //. >
            	}
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });        
        //.
        cbPOIModifyLast = (CheckBox)findViewById(R.id.cbPOIModifyLast);
        cbPOIModifyLast.setChecked(false);
        //.
        btnOk = (Button)findViewById(R.id.TrackerPOIPanel_btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if (CreateNewPOI())
        			setResult(Activity.RESULT_OK);
        		//.
            	finish();
            }
        });
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
        Update();
	}

	@SuppressLint("SimpleDateFormat")
	public void Update() {
		if (TTracker.GetTracker() == null)
			return; //. ->
		MapID = TTracker.GetTracker().GeoLog.GPSModule.MapID;
    	spPOIMapIDGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByPOIMapID(MapID));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");      
		POIName = "POI@"+sdf.format(new Date()); 
		edPOIName.setText(POIName);
        spSecurity.setSelection(DEFAULT_SECURITY_FILE_INDEX);
		flPOIModifyLast = false;
		cbPOIModifyLast.setChecked(flPOIModifyLast);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,MENU_CREATE,0,R.string.SCreateNew);
        menu.add(1,MENU_CANCEL,0,R.string.SCancel);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CREATE:
    		if (CreateNewPOI())
    			setResult(Activity.RESULT_OK);
    		//.
    		finish();
        	//.
            return true; //. >
        
        case MENU_CANCEL:
    		finish();
    		//.
    		return true; //. >
    	}
        return false;
    }
    
    private boolean CreateNewPOI() {
    	int Idx = spPOIMapIDGeoSpace.getSelectedItemPosition();
    	if (Idx < 0)
    		Idx = 0;
    	MapID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].POIMapID;
		POIName = edPOIName.getText().toString(); 
		flPOIPrivateSecurity = (spSecurity.getSelectedItemPosition() == PRIVATE_SECURITY_FILE_INDEX);
		flPOIModifyLast = cbPOIModifyLast.isChecked();
		//.
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_SHORT).show();
			return false; //. ->
		}
        TTracker Tracker = TTracker.GetTracker(); 
        TGPSFixValue GPSFix = null;
        if (!Tracker.GeoLog.GPSModule.flProcessingIsDisabled) {
            GPSFix =  Tracker.GeoLog.GPSModule.GetCurrentFix();
            if (!GPSFix.flSet) {
    			Toast.makeText(this, R.string.SCurrentPositionIsUnavailable, Toast.LENGTH_LONG).show();
    			return false; //. ->
            }        
            if (!GPSFix.IsAvailable()) {
            	if (GPSFix.IsEmpty()) {
            		Toast.makeText(this, R.string.SCurrentPositionIsUnknown, Toast.LENGTH_LONG).show();
            		return false; //. ->
            	}
            	else
            		Toast.makeText(this, R.string.SCurrentPositionIsNotAvailableUsedLast, Toast.LENGTH_LONG).show();
            }        
        }
        try {
        	TObjectSetGPSFixSO SetGPSFixSO = null;
        	if (GPSFix != null) {
                SetGPSFixSO = new TObjectSetGPSFixSO(Tracker.GeoLog.ConnectorModule,Tracker.GeoLog.UserID,Tracker.GeoLog.UserPassword,Tracker.GeoLog.ObjectID,null);
                SetGPSFixSO.setValue(GPSFix);
        	}
            //.
            int POIID = 0; //. create new POI instance
            if (flPOIModifyLast)
                POIID = -1; //. modify last created POI
            TMapPOIValue MapPOI = new TMapPOIValue(OleDate.UTCCurrentTimestamp(),MapID,POIID,POIType,POIName,flPOIPrivateSecurity);
            TObjectSetMapPOISO SetMapPOISO = new TObjectSetMapPOISO(Tracker.GeoLog.ConnectorModule, Tracker.GeoLog.UserID, Tracker.GeoLog.UserPassword, Tracker.GeoLog.ObjectID, null);
            SetMapPOISO.setValue(MapPOI);
            //.
            if (SetGPSFixSO != null)
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SetGPSFixSO,SetMapPOISO);
            else 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SetMapPOISO);
            Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
            Tracker.GeoLog.BackupMonitor.BackupImmediate();
        	//.
        	Toast.makeText(this, R.string.SPOIIsAdded, Toast.LENGTH_SHORT).show();
        }
        catch (Exception E) {
        	Toast.makeText(this, getString(R.string.SPOICreationError)+E.getMessage(), Toast.LENGTH_SHORT).show();
        	//.
        	return false; //. ->
        }
    	//.
    	return true; //. ->
    }
}
