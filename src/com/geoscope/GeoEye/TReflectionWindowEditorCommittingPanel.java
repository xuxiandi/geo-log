package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class TReflectionWindowEditorCommittingPanel extends Activity {

	int UserSecurityFileID;
	//.
	private EditText edPlaceName;
	private CheckBox cbPrivate;
	private CheckBox cbReset;
	private Button btnCommit;
	private Button btnCancel;
	//.
	String 	PlaceName;
	boolean flPrivate;
	boolean flReset;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
		UserSecurityFileID = 0;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	UserSecurityFileID = extras.getInt("UserSecurityFileID");
        }
        //.
        setContentView(R.layout.reflectionwindow_editor_committing_panel);
        //.
        edPlaceName = (EditText)findViewById(R.id.edRWEditorCommittingPlaceName);
        //.
        cbPrivate = (CheckBox)findViewById(R.id.cbRWEditorCommittingPrivate);
    	cbPrivate.setEnabled(UserSecurityFileID != 0);
        //.
        cbReset = (CheckBox)findViewById(R.id.cbRWEditorCommittingReset);
        //.
        btnCommit = (Button)findViewById(R.id.btnRWEditorCommittingCommit);
        btnCommit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Commit();
            	//.
            	int USFID = 0;
            	if (flPrivate)
            		USFID = UserSecurityFileID;
            	Intent intent = TReflectionWindowEditorCommittingPanel.this.getIntent();
            	intent.putExtra("UserSecurityFileID",USFID);
            	intent.putExtra("flReset",flReset);
            	intent.putExtra("PlaceName",PlaceName);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	finish();
            }
        });
        //.
        btnCancel = (Button)findViewById(R.id.btnRWEditorCommittingCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        this.setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void Commit() {
		try {
			PlaceName = edPlaceName.getText().toString();
			if (PlaceName.equals(""))
				PlaceName = getString(R.string.SPlace);
			//.
			flPrivate = cbPrivate.isChecked();
			flReset = cbReset.isChecked();
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    }
	}
}
