package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TSystemTGeoSpace;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

@SuppressLint("HandlerLeak")
public class TNewClientRegistrationPanel extends Activity {

	private static final int MESSAGE_LOADCAPTCHA 			= 1;
	private static final int MESSAGE_CLIENTISREGISTERED 	= 2;
	
	public static class TNewClientDescriptor {
		
		public TNewUserRegistrationPanel.TNewUserDescriptor 					User = new TNewUserRegistrationPanel.TNewUserDescriptor();
		public TNewTrackerObjectConstructionPanel.TNewTrackerObjectDescriptor 	Tracker = new TNewTrackerObjectConstructionPanel.TNewTrackerObjectDescriptor(); 
	}
	
	private TReflectorComponent Component;
	//. User
	private EditText edNewUserName;
	private EditText edNewUserPassword;
	private EditText edNewUserPasswordConfirmation;
	private EditText edNewUserFullName;
	private EditText edNewUserContactInfo;
	private ImageView ivCaptcha;
	private Button btnLoadNewUserCaptcha;
	private EditText edNewUserCaptcha;
	private Button btnRegister;
	//. Tracker
	private EditText edNewTrackerObjectName;
	private CheckBox cbNewTrackerObjectPrivateAccess;
	private Spinner spNewTrackerObjectGeoSpace;
	private EditText edNewTrackerObjectMapID;
	//.
	private TNewUserCaptchaLoading CaptchaLoading = null;
	//.
	private TNewClientRegistering ClientRegistering = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
        //. 
        setContentView(R.layout.newclientregistration_panel);
        //.
        edNewUserName = (EditText)findViewById(R.id.edNewUserName);
        edNewUserName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            	edNewTrackerObjectName.setText(s+"Tracker");
            }
        });        
        edNewUserPassword = (EditText)findViewById(R.id.edNewUserPassword);
        edNewUserPasswordConfirmation = (EditText)findViewById(R.id.edNewUserPasswordConfirmation);
        edNewUserFullName = (EditText)findViewById(R.id.edNewUserFullName);
        edNewUserContactInfo = (EditText)findViewById(R.id.edNewUserContactInfo);
        edNewUserCaptcha = (EditText)findViewById(R.id.edNewUserCaptcha);
        ivCaptcha = (ImageView)findViewById(R.id.ivCaptcha);
        btnLoadNewUserCaptcha = (Button)findViewById(R.id.btnLoadNewUserCaptcha);
        btnLoadNewUserCaptcha.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	LoadCaptcha();
            }
        });
        //.
        edNewTrackerObjectName = (EditText)findViewById(R.id.edNewTrackerObjectName);
        cbNewTrackerObjectPrivateAccess = (CheckBox)findViewById(R.id.cbNewTrackerObjectPrivateAccess);
        spNewTrackerObjectGeoSpace = (Spinner)findViewById(R.id.spNewTrackerObjectGeoSpace);
        String[] GeoSpaceNames = TSystemTGeoSpace.WellKnownGeoSpaces_GetNames();
        ArrayAdapter<String> saNewTrackerObjectGeoSpace = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, GeoSpaceNames);
        saNewTrackerObjectGeoSpace.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNewTrackerObjectGeoSpace.setAdapter(saNewTrackerObjectGeoSpace);
        edNewTrackerObjectMapID = (EditText)findViewById(R.id.edNewTrackerObjectMapID);
        //.
        btnRegister = (Button)findViewById(R.id.btnRegisterNewUser);
        btnRegister.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	Register();
            }
        });
        //.
        this.setResult(RESULT_CANCELED);
        //.
        Update();
        //.
        LoadCaptcha();
	}

	@Override
	protected void onDestroy() {
		try {
			if (ClientRegistering != null) {
				ClientRegistering.Destroy();
				ClientRegistering = null;
			}
			if (CaptchaLoading != null) {
				CaptchaLoading.Destroy();
				CaptchaLoading = null;
			}
		} catch (Exception E) {
		}
		//.
		super.onDestroy();
	}

	private void Update() {
		cbNewTrackerObjectPrivateAccess.setChecked(false);
    	spNewTrackerObjectGeoSpace.setSelection(TSystemTGeoSpace.WellKnownGeoSpaces_GetIndexByID(Component.Configuration.GeoSpaceID));
		edNewTrackerObjectMapID.setText(Integer.toString(Component.Configuration.GeoLog_GPSModuleMapID));
	}
	
	private void LoadCaptcha() {
		if (CaptchaLoading != null) {
			CaptchaLoading.Cancel();
			CaptchaLoading = null;
		}
		CaptchaLoading = new TNewUserCaptchaLoading(MESSAGE_LOADCAPTCHA);
	}
	
	private void Validate() throws Exception {
		//. User
		if (edNewUserName.getText().toString().equals(""))
			throw new Exception(getString(R.string.SNameIsEmpty)); //. =>
		if (edNewUserName.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
		if (edNewUserPassword.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
		if (!edNewUserPassword.getText().toString().equals(edNewUserPasswordConfirmation.getText().toString()))
				throw new Exception(getString(R.string.SPasswordAndConfirmationMustMatch)); //. =>
		if (edNewUserPassword.getText().toString().equals(""))
			throw new Exception(getString(R.string.SPasswordIsNull)); //. =>
		if (edNewUserFullName.getText().toString().equals(""))
			throw new Exception(getString(R.string.SFullNameIsEmpty)); //. =>
		if (edNewUserFullName.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
		if (edNewUserContactInfo.getText().toString().equals(""))
			throw new Exception(getString(R.string.SContactInfoIsEmpty)); //. =>
		if (edNewUserContactInfo.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
		if (edNewUserCaptcha.getText().toString().equals(""))
			throw new Exception(getString(R.string.SRegistrationCodeIsEmpty)); //. =>
		if (edNewUserCaptcha.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
		//. Tracker
		if (edNewTrackerObjectName.equals(""))
			throw new Exception(getString(R.string.SNameIsEmpty)); //. =>
		if (edNewTrackerObjectName.getText().toString().contains(","))
			throw new Exception(getString(R.string.SStringContainsCommaChar)); //. =>
	}
	
	private void Register() {
		try {
			Validate();
			//.
			TNewClientDescriptor NewClientDescriptor = new TNewClientDescriptor();
			//. User
			NewClientDescriptor.User.Name = edNewUserName.getText().toString().trim();
			NewClientDescriptor.User.Password = edNewUserPassword.getText().toString();
			NewClientDescriptor.User.FullName = edNewUserFullName.getText().toString();
			NewClientDescriptor.User.ContactInfo = edNewUserContactInfo.getText().toString();
			String Captcha = edNewUserCaptcha.getText().toString();
			String Signature = "CPT"+Captcha;
			//. Tracker
			NewClientDescriptor.Tracker.Name = edNewTrackerObjectName.getText().toString();
			NewClientDescriptor.Tracker.flPrivateAccess = cbNewTrackerObjectPrivateAccess.isChecked();
	    	int Idx = spNewTrackerObjectGeoSpace.getSelectedItemPosition();
	    	if (Idx < 0)
	    		Idx = 0;
	    	NewClientDescriptor.Tracker.GeoSpaceID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].ID;
	    	NewClientDescriptor.Tracker.MapID = TSystemTGeoSpace.WellKnownGeoSpaces[Idx].POIMapID;
	    	//.
			if (ClientRegistering != null) {
				ClientRegistering.CancelAndWait();
				ClientRegistering = null;
			}
			ClientRegistering = new TNewClientRegistering(NewClientDescriptor, Signature, MESSAGE_CLIENTISREGISTERED);
		}
		catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void DoOnClientIsRegistered(TNewClientDescriptor pNewClientDescriptor) {
		final TNewClientDescriptor NewClientDescriptor = pNewClientDescriptor;
	    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SRegistration)
        .setMessage(getString(R.string.SANewUserHasBeenRegisteredSuccessfully)+"\n"+getString(R.string.SName)+pNewClientDescriptor.User.Name+"\n"+"ID: "+Long.toString(pNewClientDescriptor.User.ID)+"\n"+getString(R.string.SProgramConfigurationWillBeChangedToUseNewUser))
	    .setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
	    	
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
            	Intent intent = TNewClientRegistrationPanel.this.getIntent();
            	//. User
            	intent.putExtra("UserID",NewClientDescriptor.User.ID);
            	intent.putExtra("UserName",NewClientDescriptor.User.Name);
            	intent.putExtra("UserPassword",NewClientDescriptor.User.Password);
            	//. Tracker
            	intent.putExtra("Name",NewClientDescriptor.Tracker.Name);
            	intent.putExtra("MapID",NewClientDescriptor.Tracker.MapID);
            	intent.putExtra("ComponentID",NewClientDescriptor.Tracker.CreationInfo.ComponentID);
            	intent.putExtra("GeographServerAddress",NewClientDescriptor.Tracker.CreationInfo.GeographServerAddress);
            	intent.putExtra("GeographServerPort",NewClientDescriptor.Tracker.CreationInfo.GeographServerPort);
            	intent.putExtra("GeographServerObjectID",NewClientDescriptor.Tracker.CreationInfo.GeographServerObjectID);
                //.
            	TNewClientRegistrationPanel.this.setResult(Activity.RESULT_OK,intent);
	    		TNewClientRegistrationPanel.this.finish();
	    	}
	    })
	    .show();
	}
	
    private class TNewUserCaptchaLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TNewUserCaptchaLoading(int pOnCompletionMessage) {
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				byte[] CaptchaData;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				CaptchaData = Component.Server.GetCaptcha(Component.User);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,CaptchaData).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) { 
        		if (!isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TNewClientRegistrationPanel.this, TNewClientRegistrationPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TNewClientRegistrationPanel.this);    
		            	progressDialog.setMessage(TNewClientRegistrationPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		            	progressDialog.dismiss(); 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
    private class TNewClientRegistering extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TNewClientDescriptor NewClientDescriptor;
		private String Signature;
		//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TNewClientRegistering(TNewClientDescriptor pNewClientDescriptor, String pSignature, int pOnCompletionMessage) {
    		NewClientDescriptor = pNewClientDescriptor;
    		Signature = pSignature;
			//.
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				//. User registering
    				NewClientDescriptor.User.ID = Component.Server.RegisterNewUser(Component.User, NewClientDescriptor.User.Name, NewClientDescriptor.User.Password, NewClientDescriptor.User.FullName, NewClientDescriptor.User.ContactInfo, Signature);
    				//. Tracker creating
    				int SecurityIndex = 0;
    				if (NewClientDescriptor.Tracker.flPrivateAccess)
    					SecurityIndex = 1; //. private access
    				NewClientDescriptor.Tracker.CreationInfo = Component.Server.User.ConstructNewTrackerObject(NewClientDescriptor.User.ID,NewClientDescriptor.User.Password, TDEVICEModule.ObjectBusinessModel,NewClientDescriptor.Tracker.Name,NewClientDescriptor.Tracker.GeoSpaceID,SecurityIndex);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,NewClientDescriptor).sendToTarget();
        	}
        	catch (InterruptedException IE) {
        	}
        	catch (NullPointerException NPE) { 
        		if (!isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TNewClientRegistrationPanel.this, TNewClientRegistrationPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TNewClientRegistrationPanel.this);    
		            	progressDialog.setMessage(TNewClientRegistrationPanel.this.getString(R.string.SRegisteringNewUser));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(false);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		            	progressDialog.dismiss(); 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_LOADCAPTCHA: 
                	try {
                    	byte[] CaptchaData = (byte[])msg.obj;
                		Bitmap BMP = BitmapFactory.decodeByteArray(CaptchaData, 0,CaptchaData.length);
                		//.
                		if (BMP != null)
                			ivCaptcha.setImageBitmap(BMP);
                		edNewUserCaptcha.setText("");
                	}
                	catch (Exception E) {
                		Toast.makeText(TNewClientRegistrationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                    
                case MESSAGE_CLIENTISREGISTERED: 
                	try {
                		TNewClientDescriptor NewClientDescriptor = (TNewClientDescriptor)msg.obj;
                		//.
                		DoOnClientIsRegistered(NewClientDescriptor);
                	}
                	catch (Exception E) {
                		Toast.makeText(TNewClientRegistrationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };	
}
