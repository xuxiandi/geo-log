package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

public class TUserActivityComponentListPanel extends Activity {

	public boolean flExists = false;
	//.
	private TReflectorComponent Component;
	//.
	private long UserID = 0;	
	private long ActivityID = 0;
	//.
	private TUserActivityComponentListComponent ListComponent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
			ComponentID = extras.getInt("ComponentID");
        	UserID = extras.getInt("UserID");
        	ActivityID = extras.getInt("ActivityID");
        }
		Component = TReflectorComponent.GetComponent(ComponentID);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activitycomponentlist_panel);
        //.
        LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
        //.
        ListComponent = new TUserActivityComponentListComponent(this, ParentLayout, UserID, ActivityID, TUserActivityComponentListComponent.LIST_ROW_SIZE_NORMAL_ID, Component);
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
        ListComponent.Start();
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//. 
		if (ListComponent != null) {
			ListComponent.Destroy();
			ListComponent = null;
		}
		//.
		super.onDestroy();
	}
}
