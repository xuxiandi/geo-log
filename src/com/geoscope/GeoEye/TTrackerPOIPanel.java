package com.geoscope.GeoEye;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOISO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIValue;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;

public class TTrackerPOIPanel extends Activity {

	
	private final int MENU_CANCEL = 0;
	private final int MENU_CREATE = 1;
	//.
	private TableLayout _TableLayout;
	public EditText edPOIMapID;
	public EditText edPOIName;
	public CheckBox cbPOIPrivateSecurity;
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
        _TableLayout = (TableLayout)findViewById(R.id.TrackerPOIPanelTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edPOIMapID = (EditText)findViewById(R.id.edPOIMapID);
        edPOIName = (EditText)findViewById(R.id.edPOIName);
        cbPOIPrivateSecurity = (CheckBox)findViewById(R.id.cbPOIPrivateSecurity);
        cbPOIPrivateSecurity.setChecked(false);
        cbPOIModifyLast = (CheckBox)findViewById(R.id.cbPOIModifyLast);
        cbPOIModifyLast.setChecked(false);
        btnOk = (Button)findViewById(R.id.TrackerPOIPanel_btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
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

	public void Update() {
		if (TTracker.GetTracker() == null)
			return; //. ->
		MapID = TTracker.GetTracker().GeoLog.GPSModule.MapID;
		edPOIMapID.setText(Integer.toString(MapID));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");      
		POIName = "POI@"+sdf.format(new Date()); 
		edPOIName.setText(POIName);
		flPOIPrivateSecurity = false;
		cbPOIPrivateSecurity.setChecked(flPOIPrivateSecurity);
		flPOIModifyLast = false;
		cbPOIModifyLast.setChecked(flPOIModifyLast);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,MENU_CREATE,0,"Создать новую");
        menu.add(1,MENU_CANCEL,0,"Отмена");
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
		MapID = Integer.parseInt(edPOIMapID.getText().toString());
		POIName = edPOIName.getText().toString(); 
		flPOIPrivateSecurity = cbPOIPrivateSecurity.isChecked();
		flPOIModifyLast = cbPOIModifyLast.isChecked();
		//.
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, "Трекер не активный", Toast.LENGTH_SHORT).show();
			return false; //. ->
		}
        TTracker Tracker = TTracker.GetTracker(); 
        TGPSFixValue GPSFix = null;
        if (!Tracker.GeoLog.GPSModule.flProcessingIsDisabled) {
            GPSFix =  Tracker.GeoLog.GPSModule.GetCurrentFix();
            if (!GPSFix.flSet) {
    			Toast.makeText(this, "Текущая позиция не определена", Toast.LENGTH_LONG).show();
    			return false; //. ->
            }        
            if (!GPSFix.IsAvailable()) {
            	if (GPSFix.IsEmpty()) {
            		Toast.makeText(this, "Текущая позиция неизвестна", Toast.LENGTH_LONG).show();
            		return false; //. ->
            	}
            	else
            		Toast.makeText(this, "Текущая позиция не доступна, метка будет добавлена по прошлым координатам", Toast.LENGTH_LONG).show();
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
            TMapPOIValue MapPOI = new TMapPOIValue(OleDate.UTCCurrentTimestamp(),Tracker.GeoLog.GPSModule.MapID,POIID,POIType,POIName,flPOIPrivateSecurity);
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
        	Toast.makeText(this, "Метка добавлена", Toast.LENGTH_SHORT).show();
        }
        catch (Exception E) {
        	Toast.makeText(this, "Ошибка создания новой метки, "+E.getMessage(), Toast.LENGTH_SHORT).show();
        	//.
        	return false; //. ->
        }
    	//.
    	return true; //. ->
    }
}
