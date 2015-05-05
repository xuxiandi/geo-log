package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import com.geoscope.GeoEye.Space.URLs.TURLFolderListComponent;

public class TReflectorURLListPanel extends Activity {

	public boolean flExists = false;
	//.
	private TReflectorComponent Component;
	//.
	private String URLListFolder;	
	//.
	private TURLFolderListComponent ListComponent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
			ComponentID = extras.getInt("ComponentID");
			URLListFolder = extras.getString("URLListFolder");
        }
		Component = TReflectorComponent.GetComponent(ComponentID);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activitycomponentlist_panel);
        //.
        LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
        //.
        ListComponent = new TURLFolderListComponent(this, ParentLayout, URLListFolder, TUserActivityComponentListComponent.LIST_ROW_SIZE_NORMAL_ID, Component, null);
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
}
