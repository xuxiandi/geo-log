package com.geoscope.Classes.IO.File.FileSelector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.geoscope.GeoEye.R;

public class TFileSystemPreviewFileSelectorPanel extends Activity {

	public boolean flExists = false;
	//.
	private String Folder;	
	private String FileFilter;	
	//.
	private TFileSystemPreviewFileSelectorComponent ListComponent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
			Folder = extras.getString("Folder");
			FileFilter = extras.getString("FileFilter");
        }
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //. 
        setContentView(R.layout.previewfileselector); 
        //.
        LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
        //.
        ListComponent = new TFileSystemPreviewFileSelectorComponent(this, ParentLayout, Folder, FileFilter, TFileSystemPreviewFileSelectorComponent.LIST_ROW_SIZE_SMALL_ID, null);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ListComponent.onActivityResult(requestCode, resultCode, data);
	}
}
