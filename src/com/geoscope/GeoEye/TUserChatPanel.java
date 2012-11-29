package com.geoscope.GeoEye;

import java.io.IOException;
import java.util.Hashtable;

import com.geoscope.GeoEye.Space.Defines.TUser;
import com.geoscope.GeoEye.Space.Defines.TUser.TIncomingMessage;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint("HandlerLeak")
public class TUserChatPanel extends Activity {

	public static Hashtable<Integer, TUserChatPanel> Panels = new Hashtable<Integer, TUserChatPanel>();

	public static final int ContactUserInfoUpdateInterval = 1000*30; //. seconds
	
	private static final int MESSAGE_SENT 				= 1;
	private static final int MESSAGE_RECEIVED 			= 2;
	private static final int MESSAGE_UPDATECONTACTUSER 	= 3;
	
	private TReflector Reflector;
	//.
	private TUser.TUserDescriptor 	ContactUser = new TUser.TUserDescriptor();
	private TContactUserUpdating    ContactUserUpdating;
	@SuppressWarnings("unused")
	private TUser 					MyUser;
	//.
	private TextView lbUserChatContactUser;
	private ScrollView svUserChatArea;
	private LinearLayout llUserChatArea;
	private EditText edUserChatComposeMessage;
	private Button btnUserChatComposeMessageSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ContactUser.UserID = extras.getInt("UserID");
        	ContactUser.UserIsDisabled = extras.getBoolean("UserIsDisabled");
        	ContactUser.UserIsOnline = extras.getBoolean("UserIsOnline");
        	ContactUser.UserName = extras.getString("UserName");
        	ContactUser.UserFullName = extras.getString("UserFullName");
        	ContactUser.UserContactInfo = extras.getString("UserContactInfo");
        }
        //.
		Reflector = TReflector.MyReflector; 
		MyUser = Reflector.User;
        //.
        setContentView(R.layout.userchat_panel);
        //.
        lbUserChatContactUser = (TextView)findViewById(R.id.lbUserChatContactUser);
        //.
        svUserChatArea = (ScrollView)findViewById(R.id.svUserChatArea);
        llUserChatArea = (LinearLayout)findViewById(R.id.llUserChatArea);
        //.
        edUserChatComposeMessage = (EditText)findViewById(R.id.edUserChatComposeMessage);
        edUserChatComposeMessage.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	String Message = edUserChatComposeMessage.getText().toString();
                	if (!Message.equals(""))
                		SendMessage(Message);
                }
				return false;
			}
        });        
        //.
        btnUserChatComposeMessageSend = (Button)findViewById(R.id.btnUserChatComposeMessageSend);
        btnUserChatComposeMessageSend.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
            	String Message = edUserChatComposeMessage.getText().toString();
            	if (!Message.equals(""))
            		SendMessage(Message);
            }
        });
        //.
        Panels.put(ContactUser.UserID, this);
		//.
        extras = getIntent().getExtras(); 
        if (extras != null) {
        	String _Message = extras.getString("Message");
        	if (_Message != null) {
            	double MessageTimestamp = extras.getDouble("MessageTimestamp");
            	//.
            	TIncomingMessage Message = new TIncomingMessage();
            	Message.SenderID = ContactUser.UserID;
            	Message.Sender = ContactUser;
            	Message.Message = _Message;
            	Message.Timestamp = MessageTimestamp;
            	//.
            	ReceiveMessage(Message);
        	}
        }
        //.
        UpdateContactUserInfo();
        ContactUserUpdating = new TContactUserUpdating(MESSAGE_UPDATECONTACTUSER);
        //.
        Reflector.User.IncomingMessages.SetFastCheckInterval(); //. speed up messages updating
	}

    @Override
	protected void onDestroy() {
        Reflector.User.IncomingMessages.RestoreCheckInterval();
        //.
    	if (ContactUserUpdating != null) {
    		ContactUserUpdating.CancelAndWait();
    		ContactUserUpdating = null;
    	}
    	Panels.remove(ContactUser.UserID);
    	//.
		super.onDestroy();
	}

    private void UpdateContactUserInfo() {
		String State;
		if (ContactUser.UserIsOnline)
			State = "[ONLINE]";
		else
			State = "[offline]";
        lbUserChatContactUser.setText(getString(R.string.SUser)+" "+ContactUser.UserName+" "+State+" / "+ContactUser.UserFullName);
    }
    
    public void SendMessage(String Message) {
    	new TMessageSending(Message,MESSAGE_SENT);
    }
    
    public void ReceiveMessage(TIncomingMessage Message) {
		PanelHandler.obtainMessage(MESSAGE_RECEIVED,Message).sendToTarget();
    }
    
    private void ChatArea_AddMessage(String SenderName, double Timestamp, String Message, boolean flContactUser) {
    	TextView tvMessage = new TextView(this);
    	tvMessage.setText(SenderName+": "+Message);
    	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	tvMessage.setLayoutParams(LP);
    	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
    	if (flContactUser)
    		tvMessage.setTextColor(Color.RED);
    	else
    		tvMessage.setTextColor(Color.GREEN);
    	llUserChatArea.addView(tvMessage);
    	tvMessage.setVisibility(View.VISIBLE);
    	svUserChatArea.postDelayed(new Runnable() {
    	    @Override
    	    public void run(){
    	    	svUserChatArea.fullScroll(View.FOCUS_DOWN);
    	    }
		},100);
    }
    
    private class TMessageSending extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private String Message;
    	private int OnCompletionMessage;
    	//.
    	private Thread _Thread;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TMessageSending(String pMessage, int pOnCompletionMessage) {
    		Message = pMessage;
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
    				Reflector.User.IncomingMessages_SendNew(Reflector, ContactUser.UserID, Message);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,Message).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) { 
        		if (!Reflector.isFinishing()) 
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
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserChatPanel.this, TUserChatPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserChatPanel.this);    
	            	progressDialog.setMessage(TUserChatPanel.this.getString(R.string.SSending));    
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
	    };
    }
		
    private class TContactUserUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
    	
    	private int OnCompletionMessage;
    	//.
    	private Thread _Thread;
    	
    	public TContactUserUpdating(int pOnCompletionMessage) {
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
		        	Thread.sleep(ContactUserInfoUpdateInterval);
		        	//.
					try {
						//.
						TUser.TUserDescriptor User = Reflector.User.GetUserInfo(Reflector, ContactUser.UserID); 
						//.
						if (Canceller.flCancel)
							return; //. ->
			    		//.
			    		PanelHandler.obtainMessage(OnCompletionMessage,User).sendToTarget();
		        	}
		        	catch (InterruptedException E) {
		        	}
		        	catch (NullPointerException NPE) { 
		        		if (!Reflector.isFinishing()) 
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
		        	}
		        	catch (IOException E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
		        	}
		        	catch (Throwable E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
		        	}
				}
			}
        	catch (InterruptedException E) {
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserChatPanel.this, TUserChatPanel.this.getString(R.string.SUpdatingContactUser)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_HIDE:
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MESSAGE_SENT: 
            	try {
            		String Message = (String)msg.obj;
            		ChatArea_AddMessage(getString(R.string.SMe), OleDate.UTCCurrentTimestamp(), Message, false);
            		//.
            		edUserChatComposeMessage.setText("");
            	}
            	catch (Exception E) {
            		Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            	break; //. >
            	
            case MESSAGE_RECEIVED: 
            	try {
            		TIncomingMessage Message = (TIncomingMessage)msg.obj;
            		ChatArea_AddMessage(ContactUser.UserName, Message.Timestamp, Message.Message, true);
            		//.
            		Message.SetAsProcessed();
            	}
            	catch (Exception E) {
            		Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            	break; //. >
            	
            case MESSAGE_UPDATECONTACTUSER:
        		TUser.TUserDescriptor User = (TUser.TUserDescriptor)msg.obj;
        		ContactUser.Assign(User);
        		UpdateContactUserInfo();
            	break; //. >
            }
        }
    };
}
