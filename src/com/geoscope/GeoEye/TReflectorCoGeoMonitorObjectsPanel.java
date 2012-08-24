package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TReflectorCoGeoMonitorObjectsPanel extends Activity  {

	public static final int REQUEST_ADDNEWOBJECT = 1;
	
	private TReflector Reflector;
	private TReflectorCoGeoMonitorObjects CoGeoMonitorObjects;
	
	private Button btnNewObject;
	private Button btnClose;
	private ListView lvObjects;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        CoGeoMonitorObjects = Reflector.CoGeoMonitorObjects;
        //.
        setContentView(R.layout.reflector_gmos_panel);
        //.
        btnNewObject = (Button)findViewById(R.id.btnNewObject);
        btnNewObject.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewObject();
            }
        });
        //.
        btnClose = (Button)findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        //.
        lvObjects = (ListView)findViewById(R.id.lvObjects);
		lvObjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvObjects.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ID = CoGeoMonitorObjects.Items[arg2].ID;
				CoGeoMonitorObjects.EnableDisableItem(ID,!CoGeoMonitorObjects.Items[arg2].flEnabled);
        	}              
        });         
        lvObjects.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	/*///? Intent intent = new Intent(TReflectorCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectPanel.class);
            	intent.putExtra("Index", arg2);
            	startActivity(intent);*/
				Object_ShowCurrentPosition(arg2);
            	//.
            	finish();
            	//.
            	return true; 
			}
		}); 
        //.
        lvObjects_Update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        
        case REQUEST_ADDNEWOBJECT:
            if (resultCode == Activity.RESULT_OK) 
                lvObjects_Update();
            break; //. >
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reflector_gmos_panel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.miRemoveDisabledObjects:
        	RemoveSelectedObject();
        	//.
            return true; //. >
            
        case R.id.miGMOsConfiguration:
        	Intent intent = new Intent(TReflectorCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectsConfigurationPanel.class);
        	startActivity(intent);
        	//.
            return true; //. >
    	}
    
        return false;
    }

	private void lvObjects_Update() {
		String[] lvObjectsItems = new String[CoGeoMonitorObjects.Items.length];
		for (int I = 0; I < CoGeoMonitorObjects.Items.length; I++)
			lvObjectsItems[I] = CoGeoMonitorObjects.Items[I].LabelText;
		ArrayAdapter<String> lvObjectsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvObjectsItems);             
		lvObjects.setAdapter(lvObjectsAdapter);
		for (int I = 0; I < CoGeoMonitorObjects.Items.length; I++)
			lvObjects.setItemChecked(I,CoGeoMonitorObjects.Items[I].flEnabled);
	}
	
	private void AddNewObject() {
    	Intent intent = new Intent(this, TReflectorNewCoGeoMonitorObjectPanel.class);
    	startActivityForResult(intent,REQUEST_ADDNEWOBJECT);
	}
	
	private void RemoveSelectedObject() {
		CoGeoMonitorObjects.RemoveDisabledItems();
		lvObjects_Update();
	}
	
	public void Object_ShowCurrentPosition(int idxObject) {
		try {
			CoGeoMonitorObjects.Items[idxObject].UpdateVisualizationLocation();
			Reflector.MoveReflectionWindow(CoGeoMonitorObjects.Items[idxObject].VisualizationLocation);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, "Ошибка установки текущей позиции, "+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}	
}
