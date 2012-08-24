package com.geoscope.GeoEye;

import com.geoscope.GeoEye.Space.Defines.TElectedPlaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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

public class TReflectorElectedPlacesPanel extends Activity  {

	public static final int REQUEST_ADDNEWPLACE = 1;
	
	private TReflector Reflector;
	private TElectedPlaces ElectedPlaces;
	
	private Button btnNewPlace;
	private Button btnRemoveSelectedPlaces;
	private Button btnClose;
	private ListView lvPlaces;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        ElectedPlaces = Reflector.ElectedPlaces;
        //.
        setContentView(R.layout.reflector_electedplaces_panel);
        //.
        btnNewPlace = (Button)findViewById(R.id.btnNewPlace);
        btnNewPlace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewPlace();
            }
        });
        //.
        btnRemoveSelectedPlaces = (Button)findViewById(R.id.btnRemoveSelectedPlaces);
        btnRemoveSelectedPlaces.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	RemoveSelectedPlaces();
            }
        });
        //.
        btnClose = (Button)findViewById(R.id.btnCloseElectedPlacesPanel);
        btnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        //.
        lvPlaces = (ListView)findViewById(R.id.lvPlaces);
        lvPlaces.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvPlaces.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });         
        lvPlaces.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Object_ShowPlace(arg2);
            	//.
            	finish();
            	//.
            	return true; 
			}
		}); 
        //.
        lvPlaces_Update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        
        case REQUEST_ADDNEWPLACE:
            if (resultCode == Activity.RESULT_OK) 
                lvPlaces_Update();
            break; //. >
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

	private void lvPlaces_Update() {
		String[] lvPlacesItems = new String[ElectedPlaces.Items.size()];
		for (int I = 0; I < ElectedPlaces.Items.size(); I++)
			lvPlacesItems[I] = ElectedPlaces.Items.get(I).Name;
		ArrayAdapter<String> lvPlacesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvPlacesItems);             
		lvPlaces.setAdapter(lvPlacesAdapter);
		for (int I = 0; I < ElectedPlaces.Items.size(); I++)
			lvPlaces.setItemChecked(I,false);
	}
	
	private void AddNewPlace() {
    	Intent intent = new Intent(this, TReflectorNewElectedPlacePanel.class);
    	startActivityForResult(intent,REQUEST_ADDNEWPLACE);
	}
	
	private void RemoveSelectedPlaces() {
		long[] RemoveItems = lvPlaces.getCheckItemIds();
		if (RemoveItems.length > 0) {
			int AddFactor = 0;
			for (int I = 0; I < RemoveItems.length; I++) {
				ElectedPlaces.RemovePlace((int)(RemoveItems[I]+AddFactor));
				AddFactor--;
			}
			try {
				ElectedPlaces.Save();
			}
			catch (Exception E) {
				Toast.makeText(this, "Ошибка удаления мест, "+E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			//.
			lvPlaces_Update();
		}
	}
	
	public void Object_ShowPlace(int idxPlace) {
		try {
			Reflector.TransformReflectionWindow(ElectedPlaces.Items.get(idxPlace).RW);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, "Ошибка установки места, "+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}	
}
