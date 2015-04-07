package com.geoscope.GeoEye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class TUserListPanel extends Activity {

	@SuppressWarnings("unused")
	private boolean flExists = false;
	//.
	private TReflectorComponent Component;
	//.
	private int Mode;
	//.
	private TUserListComponent ListComponent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
        int ComponentID = 0;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
			ComponentID = extras.getInt("ComponentID");
        	Mode = extras.getInt("Mode");
        }
		Component = TReflectorComponent.GetComponent(ComponentID);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //.
        setContentView(R.layout.userlist_panel);
        //.
        LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
        //.
        ListComponent = new TUserListComponent(this, ParentLayout, TUserListComponent.LIST_ROW_SIZE_SMALL_ID, Mode, Component, null);
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
		try {
			ListComponent.Start();
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
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
}
