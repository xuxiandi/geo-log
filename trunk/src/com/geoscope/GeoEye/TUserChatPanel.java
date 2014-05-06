package com.geoscope.GeoEye;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

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
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TUserChatPanel extends Activity {

	public static Hashtable<Integer, TUserChatPanel> Panels = new Hashtable<Integer, TUserChatPanel>();

	public static final int ContactUserInfoUpdateInterval = 1000*30; //. seconds
	public static final int MessageIsProcessedDelay = 1000*3; //. seconds
	
	private static final int MESSAGE_SENT 				= 1;
	private static final int MESSAGE_RECEIVED 			= 2;
	private static final int MESSAGE_UPDATECONTACTUSER 	= 3;
	
	private static class TMessageAsProcessedMarking extends TCancelableThread {
		
		private TIncomingMessage Message;
		private int Delay;
		
		public TMessageAsProcessedMarking(TIncomingMessage pMessage, int pDelay) {
			Message = pMessage;
			Delay = pDelay;
			//.
			_Thread = new Thread(this);
			_Thread.setPriority(Thread.MIN_PRIORITY);
		}
		
		public void Start() {
			_Thread.start();
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(Delay);
				//.
				Message.SetAsProcessed();
			} catch (InterruptedException E) {
			} catch (Throwable E) {
			}
		}
	}
	
	private boolean flExists = false;
	//.
	private TGeoScopeServerUser.TUserDescriptor 	ContactUser = new TGeoScopeServerUser.TUserDescriptor();
	private TContactUserUpdating    				ContactUserUpdating;
	//.
	private int UserIncomingMessages_LastCheckInterval = -1;
	private ArrayList<TMessageAsProcessedMarking> MessageAsProcessedMarkingList = new ArrayList<TUserChatPanel.TMessageAsProcessedMarking>();
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
    	TReflector Reflector;
		try {
			Reflector = Reflector();
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return; //. ->
		}
    	TIncomingMessage Message = null;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ContactUser.UserID = extras.getInt("UserID");
        	ContactUser.UserIsDisabled = extras.getBoolean("UserIsDisabled");
        	ContactUser.UserIsOnline = extras.getBoolean("UserIsOnline");
        	ContactUser.UserName = extras.getString("UserName");
        	ContactUser.UserFullName = extras.getString("UserFullName");
        	ContactUser.UserContactInfo = extras.getString("UserContactInfo");
        	//.
        	int MessageID = extras.getInt("MessageID");
        	if ((Reflector != null) && (Reflector.User != null) && (Reflector.User.IncomingMessages != null))
        		Message = Reflector.User.IncomingMessages.GetMessageByID(MessageID);
        }
        //.
        if (Message != null) {
    		TUserChatPanel UCP = Panels.get(Message.SenderID);
    		if (UCP != null) {
    			UCP.PublishMessage(Message);
    			//.
    			finish();
    			return; //. ->
    		}
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        UpdateContactUserInfo();
        ContactUserUpdating = new TContactUserUpdating(MESSAGE_UPDATECONTACTUSER);
        //.
        if (Message != null) 
        	PublishMessage(Message);
        //.
    	if ((Reflector != null) && (Reflector.User != null) && (Reflector.User.IncomingMessages != null))
    		UserIncomingMessages_LastCheckInterval = Reflector.User.IncomingMessages.SetFastCheckInterval(); //. speed up messages updating
        //.
        Panels.put(ContactUser.UserID, this);
        //.
        flExists = true;
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	Panels.remove(ContactUser.UserID);
    	//.
    	if (MessageAsProcessedMarkingList != null) {
    		for (int I = 0; I < MessageAsProcessedMarkingList.size(); I++)
    			MessageAsProcessedMarkingList.get(I).Cancel();
    		//.
    		MessageAsProcessedMarkingList = null;
    	}
    	//.
    	if (UserIncomingMessages_LastCheckInterval >= 0) {
    		try {
    			TReflector Reflector = Reflector();
            	if ((Reflector != null) && (Reflector.User != null) && (Reflector.User.IncomingMessages != null))
            		Reflector.User.IncomingMessages.RestoreCheckInterval(UserIncomingMessages_LastCheckInterval);
    		} catch (Exception E) {
    		}
    		//.
    		UserIncomingMessages_LastCheckInterval = -1;
    	}
        //.
    	if (ContactUserUpdating != null) {
    		ContactUserUpdating.Cancel();
    		ContactUserUpdating = null;
    	}
    	//.
		super.onDestroy();
	}

    private TReflector Reflector() throws Exception {
    	TReflector Reflector = TReflector.GetReflector();
    	if (Reflector == null)
    		throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
		return Reflector;
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
    
    private void PublishMessage(TIncomingMessage Message) {
		ChatArea_AddMessage(ContactUser.UserName, Message.Timestamp, Message.Message, true);
		//.
		TMessageAsProcessedMarking MessageAsProcessedMarking = new TMessageAsProcessedMarking(Message, MessageIsProcessedDelay);
		MessageAsProcessedMarkingList.add(MessageAsProcessedMarking);
		MessageAsProcessedMarking.Start();
    }
    
    private void ChatArea_AddMessage(String SenderName, double Timestamp, String Message, boolean flContactUser) {
    	TextView tvMessage = new TextView(this);
    	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(Timestamp)).GetDateTime())+" "+SenderName+": "+Message);
    	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	tvMessage.setLayoutParams(LP);
    	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
    	if (flContactUser)
    		tvMessage.setTextColor(Color.BLACK);
    	else
    		tvMessage.setTextColor(Color.LTGRAY);
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
    	        	TReflector Reflector = Reflector();
    	        	if ((Reflector != null) && (Reflector.User != null))
    	        		Reflector.User.IncomingMessages_SendNewMessage(ContactUser.UserID, Message);
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
					if (Canceller.flCancel)
		            	break; //. >
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
						TGeoScopeServerUser.TUserDescriptor User = null;
	    	        	TReflector Reflector = Reflector();
	    	        	if ((Reflector != null) && (Reflector.User != null))
	    	        		User = Reflector.User.GetUserInfo(ContactUser.UserID); 
						//.
						if (Canceller.flCancel)
							return; //. ->
			    		//.
						if (User != null)
							PanelHandler.obtainMessage(OnCompletionMessage,User).sendToTarget();
		        	}
		        	catch (InterruptedException E) {
		        	}
		        	catch (NullPointerException NPE) { 
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
					if (Canceller.flCancel)
		            	break; //. >
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
		
	public final Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MESSAGE_SENT: 
				if (!flExists)
	            	break; //. >
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
				if (!flExists)
	            	break; //. >
            	try {
            		TIncomingMessage Message = (TIncomingMessage)msg.obj;
            		PublishMessage(Message);
            	}
            	catch (Exception E) {
            		Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            	break; //. >
            	
            case MESSAGE_UPDATECONTACTUSER:
				if (!flExists)
	            	break; //. >
        		TGeoScopeServerUser.TUserDescriptor User = (TGeoScopeServerUser.TUserDescriptor)msg.obj;
        		ContactUser.Assign(User);
        		UpdateContactUserInfo();
            	break; //. >
            }
        }
    };
}
