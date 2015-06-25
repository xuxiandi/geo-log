package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjects;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TReflectorCoGeoMonitorObjectsSearchPanel extends Activity {

	private TReflectorComponent Component;
	//.
	private EditText	edContext;
	private Button 		btnSearch;
	private ListView 	lvObjects;
	private Button 		btnAddCheckedObjects;
	//.
	private TCoGeoMonitorObjects.TDescriptors ResultItems;
	
    public void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.reflector_gmos_search_panel);
        //.
        edContext = (EditText)findViewById(R.id.edContext);
        edContext.setOnEditorActionListener(new OnEditorActionListener() {
        	
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				btnSearch.callOnClick();
				return false;
			}
        });        
        //.
        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
					DoDomainSearch(edContext.getText().toString(),"1");
				} catch (Exception E) {
					Toast.makeText(TReflectorCoGeoMonitorObjectsSearchPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        lvObjects = (ListView)findViewById(R.id.lvObjects);
		lvObjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvObjects.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });         
        btnAddCheckedObjects = (Button)findViewById(R.id.btnAddCheckedObjects);
        btnAddCheckedObjects.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		AddCheckedObjects();
        		//.
            	setResult(RESULT_OK);
        		TReflectorCoGeoMonitorObjectsSearchPanel.this.finish();
            }
        });
        //.
        this.setResult(RESULT_CANCELED);
    }

    private void DoDomainSearch(final String Domains, final String Params) throws Exception {
    	if (Domains.length() < 2) {
    		Toast.makeText(TReflectorCoGeoMonitorObjectsSearchPanel.this, R.string.STooShortSearchContext, Toast.LENGTH_SHORT).show();
    		return; //. ->
    	}
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectsSearchPanel.this,getString(R.string.SWaitAMoment)) {
			
			private TCoGeoMonitorObjects.TDescriptors ResultItems;
			
			@Override
			public void Process() throws Exception {
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				ResultItems = TCoGeoMonitorObjects.GetDataForDomains(UserAgent.Server, Domains, Params);
				//.
	    		Thread.sleep(100); 
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				TReflectorCoGeoMonitorObjectsSearchPanel.this.ResultItems = ResultItems;
				//.
				lvObjects_UpdateByResultItems();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TReflectorCoGeoMonitorObjectsSearchPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
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