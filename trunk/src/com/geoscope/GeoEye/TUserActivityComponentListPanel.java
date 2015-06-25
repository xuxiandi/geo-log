package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TUserActivityComponentListPanel extends Activity {

	public boolean flExists = false;
	//.
	private TReflectorComponent Component;
	//.
	private long UserID = 0;
	//.
	private long 	ActivityID = 0;
	private String	ActivityInfo;
	//.
	private TUserActivityComponentListComponent ListComponent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        try {
            int ComponentID = 0;
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
    			ComponentID = extras.getInt("ComponentID");
            	UserID = extras.getLong("UserID");
            	ActivityID = extras.getLong("ActivityID");
            	ActivityInfo = extras.getString("ActivityInfo");
            }
    		Component = TReflectorComponent.GetComponent(ComponentID);
    		//.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //. 
            setContentView(R.layout.user_activitycomponentlist_panel);
            //.
            LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
            //.
    		ListComponent = new TUserActivityComponentListComponent(this, ParentLayout, UserID, ActivityID,ActivityInfo, TUserActivityComponentListComponent.LIST_ROW_SIZE_NORMAL_ID, Component, null);
            //.
            setResult(RESULT_CANCELED);
            //.
            flExists = true;
            //.
            ListComponent.Start();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			// .
			finish();
			return; // . ->
		}
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//. 
		try {
			if (ListComponent != null) {
				ListComponent.Destroy();
				ListComponent = null;
			}
		} catch (Exception E) {
		}
		//.
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//.
		ListComponent.DoOnResume();
	}
	
	@Override
	protected void onPause() {
		ListComponent.DoOnPause();
		//.
		super.onPause();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	ListComponent.onActivityResult(requestCode, resultCode, data);
    }
}
