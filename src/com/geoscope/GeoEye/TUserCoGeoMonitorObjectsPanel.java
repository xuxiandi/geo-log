package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjects;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TUserCoGeoMonitorObjectsPanel extends Activity {

	private long UserID;
	private TReflectorComponent Component;
	//.
	private ListView 	lvObjects;
	private Button 		btnAddCheckedObjects;
	//.
	private TAsyncProcessing Updating = null;
	//.
	private TCoGeoMonitorObjects.TDescriptors ResultItems;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			UserID = extras.getLong("UserID");
			ComponentID = extras.getInt("ComponentID");
		}
		Component = TReflectorComponent.GetComponent(ComponentID);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.user_gmos_panel);
        //.
        lvObjects = (ListView)findViewById(R.id.lvObjects);
		lvObjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvObjects.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });         
        lvObjects.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	Intent intent = new Intent(TUserCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectPanel.class);
            	if (Component != null)
            		intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("ParametersType", TReflectorCoGeoMonitorObjectPanel.PARAMETERS_TYPE_OID);
            	intent.putExtra("ObjectID", ResultItems.Items.get(arg2).idComponent);
            	startActivity(intent);
            	//.
            	return true; 
			}
		}); 
        btnAddCheckedObjects = (Button)findViewById(R.id.btnAddCheckedObjects);
        btnAddCheckedObjects.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		AddCheckedObjects();
        		//.
            	setResult(RESULT_OK);
        		TUserCoGeoMonitorObjectsPanel.this.finish();
            }
        });
        //.
        this.setResult(RESULT_CANCELED);
        //.
        StartUpdating();
    }

	@Override
	protected void onDestroy() {
		if (Updating != null) 
			try {
				Updating.Destroy();
				Updating = null;
			} catch (InterruptedException E) {
			}
		//.
		super.onDestroy();
	}
    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	//.
		Updating = new TAsyncProcessing(TUserCoGeoMonitorObjectsPanel.this,getString(R.string.SWaitAMoment)) {
			
			private TCoGeoMonitorObjects.TDescriptors ResultItems;
			
			@Override
			public void Process() throws Exception {
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				ResultItems = UserAgent.User().GetUserCoGeoMonitorObjects(UserID);
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				TUserCoGeoMonitorObjectsPanel.this.ResultItems = ResultItems;
				//.
				lvObjects_UpdateByResultItems();
			}
			
			@Override
			public void DoOnCancelIsOccured() {
				TUserCoGeoMonitorObjectsPanel.this.finish();
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserCoGeoMonitorObjectsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Updating.Start();
    }
    
	private void lvObjects_UpdateByResultItems() {
		int Cnt = ResultItems.Items.size();
		String[] lvObjectsItems = new String[Cnt];
		for (int I = 0; I < Cnt; I++)
			lvObjectsItems[I] = ResultItems.Items.get(I).Text1();
		ArrayAdapter<String> lvObjectsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvObjectsItems);             
		lvObjects.setAdapter(lvObjectsAdapter);
		for (int I = 0; I < Cnt; I++)
			lvObjects.setItemChecked(I,false);
	}	
	
	private void AddCheckedObjects() {
		if (Component == null)
			return; //. ->
		int Cnt = ResultItems.Items.size();
		for (int I = 0; I < Cnt; I++)
			if (lvObjects.isItemChecked(I)) {
				TCoGeoMonitorObject.TDescriptor Item = ResultItems.Items.get(I);
				if (Item.IsValid()) 
					Component.CoGeoMonitorObjects.AddItem(Item.ID(),Item.Text(),true);
			}
	}
}