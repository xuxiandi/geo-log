package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TMyUserNewDatafilePanel extends Activity {


	public static final long DEFAULT_SECURITY_FILE_ID = 0;
	public static final long PRIVATE_SECURITY_FILE_ID = -1;
	
	public static final int DEFAULT_SECURITY_FILE_INDEX = 0;
	public static final int PRIVATE_SECURITY_FILE_INDEX = 1;
	public static final int OTHER_SECURITY_FILE_INDEX 	= 2;
	
	private int		DataFileType;
	private String 	DataFile = "";
	//.
	private EditText edName;
	private String Name = ""; 
	//.
	private Spinner spSecurity;
	private long SecurityFileID = DEFAULT_SECURITY_FILE_ID;
	//.
	private Button 	btnCreate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			DataFileType = extras.getInt("DataFileType");
			DataFile = extras.getString("DataFile");
		}
        //.
        setContentView(R.layout.myuser_newdatafile_panel);
        //.
        edName = (EditText)findViewById(R.id.edName);
    	//.
        String[] SecuritySA = new String[2];
        SecuritySA[DEFAULT_SECURITY_FILE_INDEX] = getString(R.string.SDefault);
        SecuritySA[PRIVATE_SECURITY_FILE_INDEX] = getString(R.string.SPrivate);
        spSecurity = (Spinner)findViewById(R.id.spSecurity);
        ArrayAdapter<String> saSecurity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SecuritySA);
        saSecurity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecurity.setAdapter(saSecurity);
        spSecurity.setSelection(DEFAULT_SECURITY_FILE_INDEX);
        //.
        btnCreate = (Button)findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		Name = edName.getText().toString();
        		//.
        		SecurityFileID = DEFAULT_SECURITY_FILE_ID;
        		switch (spSecurity.getSelectedItemPosition()) {
        			
        		case DEFAULT_SECURITY_FILE_INDEX:
        			SecurityFileID = DEFAULT_SECURITY_FILE_ID;
        			break; //. >

        		case PRIVATE_SECURITY_FILE_INDEX:
        			SecurityFileID = PRIVATE_SECURITY_FILE_ID;
        			break; //. >
        		}
        		//.
            	Intent intent = getIntent();
            	intent.putExtra("DataFileType",DataFileType);
            	intent.putExtra("DataFile",DataFile);
            	intent.putExtra("Name",Name);
            	intent.putExtra("SecurityFileID",SecurityFileID);
                //.
            	setResult(Activity.RESULT_OK,intent);
        		//.
            	finish();
            }
        });
        //.
        setResult(Activity.RESULT_CANCELED);
	}
}
