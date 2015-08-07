package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.SecurityFile.TSecurityFileInstanceListPanel;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TMyUserNewDatafilePanel extends Activity {

	public static final long DEFAULT_SECURITY_FILE_ID = 0;
	public static final long PRIVATE_SECURITY_FILE_ID = -1;
	
	public static final int DEFAULT_SECURITY_FILE_INDEX = 0;
	public static final int PRIVATE_SECURITY_FILE_INDEX = 1;
	public static final int OTHER_SECURITY_FILE_INDEX 	= 2;

	public static final int REQUEST_COMPONENT_CHANGESECURITY = 1;

	
	private int		DataFileType;
	private String 	DataFile = "";
	//.
	private EditText 	edName;
	private String 		Name = ""; 
	//.
	private long 		SecurityFileID = DEFAULT_SECURITY_FILE_ID;
	private Spinner 	spSecurity;
	private TextView 	tvSecurity;
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
        String[] SecuritySA = new String[3];
        SecuritySA[DEFAULT_SECURITY_FILE_INDEX] = getString(R.string.SDefault);
        SecuritySA[PRIVATE_SECURITY_FILE_INDEX] = getString(R.string.SPrivate);
        SecuritySA[OTHER_SECURITY_FILE_INDEX] = getString(R.string.SOther);
        spSecurity = (Spinner)findViewById(R.id.spSecurity);
        ArrayAdapter<String> saSecurity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SecuritySA);
        saSecurity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecurity.setAdapter(saSecurity);
        spSecurity.setSelection(DEFAULT_SECURITY_FILE_INDEX);
        spSecurity.setOnItemSelectedListener(new OnItemSelectedListener() {
        	
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	switch (position) {
            	
            	case DEFAULT_SECURITY_FILE_INDEX:
        			SecurityFileID = DEFAULT_SECURITY_FILE_ID;
        			tvSecurity.setVisibility(View.GONE);
            		break; //. >
            		
            	case PRIVATE_SECURITY_FILE_INDEX:
        			SecurityFileID = PRIVATE_SECURITY_FILE_ID;
        			tvSecurity.setVisibility(View.GONE);
            		break; //. >
            		
            	case OTHER_SECURITY_FILE_INDEX:
        			TAsyncProcessing Processing = new TAsyncProcessing(TMyUserNewDatafilePanel.this,TMyUserNewDatafilePanel.this.getString(R.string.SWaitAMoment)) {
        				
        				private TGeoScopeServerUser.TUserDescriptor UserDescriptor;
        				
        				@Override
        				public void Process() throws Exception {
        					TUserAgent UserAgent = TUserAgent.GetUserAgent(context.getApplicationContext());
        					if (UserAgent == null)
        						throw new Exception(TMyUserNewDatafilePanel.this.getString(R.string.SUserAgentIsNotInitialized)); //. =>
        					UserDescriptor = UserAgent.User().GetUserInfo();
        					//.
        					Thread.sleep(100);
        				}
        				
        				@Override 
        				public void DoOnCompleted() throws Exception {
							Intent intent = new Intent(TMyUserNewDatafilePanel.this,TSecurityFileInstanceListPanel.class);
							intent.putExtra("Context", UserDescriptor.UserName);
							TMyUserNewDatafilePanel.this.startActivityForResult(intent, REQUEST_COMPONENT_CHANGESECURITY);
        				}
        				
        				@Override
        				public void DoOnException(Exception E) {
        					Toast.makeText(TMyUserNewDatafilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        				}
        			};
        			Processing.Start();
            		break; //. >
            	}
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });  
        tvSecurity = (TextView)findViewById(R.id.tvSecurity);
        //.
        btnCreate = (Button)findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		Name = edName.getText().toString();
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
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_COMPONENT_CHANGESECURITY: 
        	if (resultCode == Activity.RESULT_OK) { 
        		SecurityFileID = data.getExtras().getLong("SecurityFileID");
        		String SecurityFileName = data.getExtras().getString("SecurityFileName");  
        		//.
        		tvSecurity.setText(SecurityFileName);
    			tvSecurity.setVisibility(View.VISIBLE);
        	}
        	break; //. >
        }
        //.
    	super.onActivityResult(requestCode, resultCode, data);
    }
}
