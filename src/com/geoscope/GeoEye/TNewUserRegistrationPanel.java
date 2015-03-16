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
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TNewUserRegistrationPanel extends Activity {

	private static final int MESSAGE_LOADCAPTCHA 		= 1;
	private static final int MESSAGE_USERISREGISTERED 	= 2;
	
	public static class TNewUserDescriptor {
		public long 	ID;
		public String 	Name;
		public String 	Password;
		public String 	FullName;
		public String 	ContactInfo;
	}
	
	private TReflectorComponent Component;
	//.
	private EditText edNewUserName;
	private EditText edNewUserPassword;
	private EditText edNewUserPasswordConfirmation;
	private EditText edNewUserFullName;
	private EditText edNewUserContactInfo;
	private ImageView ivCaptcha;
	private Button btnLoadNewUserCaptcha;
	private EditText edNewUserCaptcha;
	private Button btnRegister;
	//.
	private TNewUserCaptchaLoading CaptchaLoading = null;
	//.
	private TNewUserRegistering UserRegistering = null;
	
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.newuserregistration_panel);
        //.
        edNewUserName = (EditText)findViewById(R.id.edNewUserName);
        edNewUserPassword = (EditText)findViewById(R.id.edNewUserPassword);
        edNewUserPasswordConfirmation = (EditText)findViewById(R.id.edNewUserPasswordConfirmation);
        edNewUserFullName = (EditText)findViewById(R.id.edNewUserFullName);
        edNewUserContactInfo = (EditText)findViewById(R.id.edNewUserContactInfo);
        edNewUserCaptcha = (EditText)findViewById(R.id.edNewUserCaptcha);
        ivCaptcha = (ImageView)findViewById(R.id.ivCaptcha);
        btnLoadNewUserCaptcha = (Button)findViewById(R.id.btnLoadNewUserCaptcha);
        btnLoadNewUserCaptcha.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	LoadCaptcha();
            }
        });
        btnRegister = (Button)findViewById(R.id.btnRegisterNewUser);
        btnRegister.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Register();
            }
        });
        //.
        this.setResult(RESULT_CANCELED);
        //.
        LoadCaptcha();
	}

	@Override
	protected void onDestroy() {
		try {
			if (UserRegistering != null) {
				UserRegistering.CancelAndWait();
				UserRegistering = null;
			}
			if (CaptchaLoading != null) {
				CaptchaLoading.CancelAndWait();
				CaptchaLoading = null;
			}
		} catch (InterruptedException E) {
		}
		//.
		super.onDestroy();
	}

	private void LoadCaptcha() {
		if (CaptchaLoading != null) {
			CaptchaLoading.Cancel();
			CaptchaLoading = null;
		}
		CaptchaLoading = new TNewUserCaptchaLoading(MESSAGE_LOADCAPTCHA);
	}
	
	private void Validate() throws Exception {
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
	}
	
	private void Register() {
		try {
			Validate();
			//.
			TNewUserDescriptor NewUserDescriptor = new TNewUserDescriptor();
			NewUserDescriptor.Name = edNewUserName.getText().toString().trim();
			NewUserDescriptor.Password = edNewUserPassword.getText().toString();
			NewUserDescriptor.FullName = edNewUserFullName.getText().toString();
			NewUserDescriptor.ContactInfo = edNewUserContactInfo.getText().toString();
			String Captcha = edNewUserCaptcha.getText().toString();
			//.
			String Signature = "CPT"+Captcha;
			if (UserRegistering != null) {
				UserRegistering.CancelAndWait();
				UserRegistering = null;
			}
			UserRegistering = new TNewUserRegistering(NewUserDescriptor, Signature, MESSAGE_USERISREGISTERED);
		}
		catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void DoOnUserIsRegistered(TNewUserDescriptor pNewUserDescriptor) {
		final TNewUserDescriptor NewUserDescriptor = pNewUserDescriptor;
	    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SRegistration)
        .setMessage(getString(R.string.SANewUserHasBeenRegisteredSuccessfully)+"\n"+getString(R.string.SName)+pNewUserDescriptor.Name+"\n"+"ID: "+Long.toString(pNewUserDescriptor.ID)+"\n"+getString(R.string.SProgramConfigurationWillBeChangedToUseNewUser))
	    .setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
	    	
	    	public void onClick(DialogInterface dialog, int id) {
            	Intent intent = TNewUserRegistrationPanel.this.getIntent();
            	intent.putExtra("UserID",NewUserDescriptor.ID);
            	intent.putExtra("UserName",NewUserDescriptor.Name);
            	intent.putExtra("UserPassword",NewUserDescriptor.Password);
                //.
            	TNewUserRegistrationPanel.this.setResult(Activity.RESULT_OK,intent);
	    		TNewUserRegistrationPanel.this.finish();
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
    		super();
    		//.
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
		                Toast.makeText(TNewUserRegistrationPanel.this, TNewUserRegistrationPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TNewUserRegistrationPanel.this);    
		            	progressDialog.setMessage(TNewUserRegistrationPanel.this.getString(R.string.SLoading));    
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
		
    private class TNewUserRegistering extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TNewUserDescriptor NewUserDescriptor;
		private String Signature;
		//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TNewUserRegistering(TNewUserDescriptor pNewUserDescriptor, String pSignature, int pOnCompletionMessage) {
    		super();
    		//.
    		NewUserDescriptor = pNewUserDescriptor;
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
    				NewUserDescriptor.ID = Component.Server.RegisterNewUser(Component.User, NewUserDescriptor.Name, NewUserDescriptor.Password, NewUserDescriptor.FullName, NewUserDescriptor.ContactInfo, Signature);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,NewUserDescriptor).sendToTarget();
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
		                Toast.makeText(TNewUserRegistrationPanel.this, TNewUserRegistrationPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TNewUserRegistrationPanel.this);    
		            	progressDialog.setMessage(TNewUserRegistrationPanel.this.getString(R.string.SRegisteringNewUser));    
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
                		Toast.makeText(TNewUserRegistrationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                    
                case MESSAGE_USERISREGISTERED: 
                	try {
                		TNewUserDescriptor NewUserDescriptor = (TNewUserDescriptor)msg.obj;
                		//.
                		DoOnUserIsRegistered(NewUserDescriptor);
                	}
                	catch (Exception E) {
                		Toast.makeText(TNewUserRegistrationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
