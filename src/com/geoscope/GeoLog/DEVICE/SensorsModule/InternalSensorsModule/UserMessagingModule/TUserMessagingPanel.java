package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.TFileSystemFileSelector;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingXMLDataMessage;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule.TUserMessagingModule.TUserMessaging;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUserMessagingPanel extends Activity {

	public static Hashtable<String, TUserMessagingPanel> Panels = new Hashtable<String, TUserMessagingPanel>();

	public static final int ContactUserInfoUpdateInterval = 1000*30; //. seconds
	public static final int MessageIsProcessedDelay = 1000*1; //. seconds
	
	private static final int MESSAGE_SENT 				= 1;
	private static final int MESSAGE_RECEIVED 			= 2;
	private static final int MESSAGE_UPDATECONTACTUSER 	= 3;
	
	private static final int REQUEST_DRAWINGEDITOR	= 1;
	
	private static class TMessageAsProcessedMarking extends TCancelableThread {
		
		private TIncomingMessage Message;
		private int Delay;
		
		public TMessageAsProcessedMarking(TIncomingMessage pMessage, int pDelay) {
			Message = pMessage;
			Delay = pDelay;
			//.
			_Thread = new Thread(this);
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
	private TUserAgent UserAgent;
	private TTracker Tracker;
	//.
	private TUserMessagingModule UserMessagingModule;
	//.
	private boolean flInitiator;
	//.
	private int 			UserMessagingID;
	private TUserMessaging 	UserMessaging = null; 
	//.
	private TGeoScopeServerUser.TUserDescriptor 	ContactUser = new TGeoScopeServerUser.TUserDescriptor();
	private TContactUserUpdating    				ContactUserUpdating;
	//.
	private ArrayList<TMessageAsProcessedMarking> MessageAsProcessedMarkingList = new ArrayList<TUserMessagingPanel.TMessageAsProcessedMarking>();
	//.
	private TextView lbUserChatContactUser;
	private ScrollView svUserChatArea;
	private LinearLayout llUserChatArea;
	private EditText edUserChatComposeMessage;
	private Button btnUserChatComposeMessageSend;
	private Button btnUserChatTextEntry;
	private Button btnUserChatDrawingSend;
	private Button btnUserChatPictureSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		try {
			UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	UserMessagingModule = Tracker.GeoLog.SensorsModule.InternalSensorsModule.UserMessagingModule; 
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return; //. ->
		}
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
            flInitiator = extras.getBoolean("Initiator");
        	UserMessagingID = extras.getInt("UserMessagingID");
        	//.
            UserMessaging = Tracker.GeoLog.SensorsModule.InternalSensorsModule.UserMessagingModule.UserMessagings.GetItemByID(UserMessagingID);
        	ContactUser.UserID = (int)UserMessaging.UserID;
        }
        //.
        if (!flInitiator) {
            TUserMessagingPanel UMP = Panels.get(UserMessaging.SessionID());
            if (UMP != null) {
            	if (!Reader_IsInitialized())
            		Reader_Initialize();
            	//.
                finish();
                return; //. ->
            }
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.usermessagingmodule_usermessaging_panel);
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
			@Override
            public void onClick(View v) {
            	String Message = edUserChatComposeMessage.getText().toString();
            	if (!Message.equals(""))
            		SendMessage(Message);
            }
        });
        //.
        btnUserChatTextEntry = (Button)findViewById(R.id.btnUserChatTextEntry);
        btnUserChatTextEntry.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edUserChatComposeMessage, InputMethodManager.SHOW_FORCED);
			}
        });
        //.
        btnUserChatDrawingSend = (Button)findViewById(R.id.btnUserChatDrawingSend);
        btnUserChatDrawingSend.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				SendDrawing();
			}
        });
        //.
        btnUserChatPictureSend = (Button)findViewById(R.id.btnUserChatPictureSend);
        btnUserChatPictureSend.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				SendPicture();
			}
        });
        //.
        UpdateContactUserInfo();
        ContactUserUpdating = new TContactUserUpdating(MESSAGE_UPDATECONTACTUSER);
        //.
        Panels.put(UserMessaging.SessionID(), this);
        //.
        flExists = true;
        //.
        if (flInitiator) 
        	Reader_Initialize();
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	Panels.remove(UserMessaging.SessionID());
    	//.
    	Reader_Finalize();
    	//.
    	if (MessageAsProcessedMarkingList != null) {
    		for (int I = 0; I < MessageAsProcessedMarkingList.size(); I++)
    			MessageAsProcessedMarkingList.get(I).Cancel();
    		//.
    		MessageAsProcessedMarkingList = null;
    	}
        //.
    	if (ContactUserUpdating != null) {
    		ContactUserUpdating.Cancel();
    		ContactUserUpdating = null;
    	}
    	//.
		super.onDestroy();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        
        case REQUEST_DRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String DrawingFileName = extras.getString("FileName");
                	File F = new File(DrawingFileName);
                	if (F.exists()) {
                    	try {
                	    	FileInputStream FIS = new FileInputStream(F);
                	    	try {
                        		byte[] DRW = new byte[(int)F.length()];
                    			FIS.read(DRW);
                    			//.
                            	TIncomingXMLDataMessage IDM = new TIncomingXMLDataMessage(TDrawingDefines.FileExtension,DRW);
                            	//.
                            	new TMessageSending(IDM,F,MESSAGE_SENT);
                	    	}
                	    	finally {
                	    		FIS.close();
                	    	}
    					}
    					catch (Exception E) {
    	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
    					}
                	}
                }
			}
            break; //. >
        }
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void Reader_Initialize() {
    	
    }
    
    private void Reader_Finalize() {
    	
    }
    
    private boolean Reader_IsInitialized() {
    	return false;
    }
    
    private void UpdateContactUserInfo() {
		String State;
		if (ContactUser.UserIsOnline)
			State = "[ONLINE]";
		else
			State = "[offline]";
        lbUserChatContactUser.setText(getString(R.string.SUser)+" "+ContactUser.UserName+" "+State+" / "+ContactUser.UserFullName);
    }
    
    private void SendMessage(String Message) {
    	TIncomingMessage IM = new TIncomingMessage();
    	IM.Message = Message;
    	new TMessageSending(IM,null,MESSAGE_SENT);
    }
    
    private void SendDrawing() {
    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"UserChatDrawing"+"."+TDrawingDefines.FileExtension;
    	//.
    	Intent intent = new Intent(TUserMessagingPanel.this, TDrawingEditor.class);
    	intent.putExtra("FileName", FileName); 
    	intent.putExtra("ReadOnly", false); 
    	intent.putExtra("SpaceContainersAvailable", false); 
    	startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
    }
    
    private void SendPicture() {
    	TFileSystemFileSelector FileSelector = new TFileSystemFileSelector(this)
        .setFilter(".*\\.bmp|.*\\.png|.*\\.gif|.*\\.jpg|.*\\.jpeg")
        .setOpenDialogListener(new TFileSystemFileSelector.OpenDialogListener() {
        	
            @Override
            public void OnSelectedFile(String fileName) {
                File ChosenFile = new File(fileName);
                //.
				try {
                	File F = new File(ChosenFile.getAbsolutePath());
                	if (F.exists()) {
                    	try {
                	    	FileInputStream FIS = new FileInputStream(F);
                	    	try {
                        		byte[] Data = new byte[(int)F.length()];
                    			FIS.read(Data);
                    			//.
                            	TIncomingXMLDataMessage IDM = new TIncomingXMLDataMessage(TFileSystem.FileName_GetExtension(ChosenFile.getAbsolutePath()),Data);
                            	//.
                            	new TMessageSending(IDM,null,MESSAGE_SENT);
                	    	}
                	    	finally {
                	    		FIS.close();
                	    	}
    					}
    					catch (Exception E) {
    	        			Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
    					}
                	}
				}
				catch (Throwable E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TUserMessagingPanel.this, S, Toast.LENGTH_SHORT).show();  						
				}
            }

			@Override
			public void OnCancel() {
			}
        });
    	FileSelector.show();    	
    }
    
    public void ReceiveMessage(TIncomingMessage Message) {
		PanelHandler.obtainMessage(MESSAGE_RECEIVED,Message).sendToTarget();
    }
    
    private void PublishMessage(TIncomingMessage Message) throws Exception {
		ChatArea_AddMessage(ContactUser.UserName, Message, true);
		//.
		TMessageAsProcessedMarking MessageAsProcessedMarking = new TMessageAsProcessedMarking(Message, MessageIsProcessedDelay);
		MessageAsProcessedMarkingList.add(MessageAsProcessedMarking);
		MessageAsProcessedMarking.Start();
    }
    
    private void ChatArea_AddMessage(String SenderName, TIncomingMessage Message, boolean flContactUser) throws Exception {
    	if (Message instanceof TIncomingXMLDataMessage) {
        	TIncomingXMLDataMessage DataMessage = (TIncomingXMLDataMessage)Message;
        	if (DataMessage.Data != null) {
            	TextView tvMessage = new TextView(this);
            	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(Message.Timestamp)).GetDateTime())+" "+SenderName+": ");
            	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            	tvMessage.setLayoutParams(LP);
            	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
            	if (flContactUser)
            		tvMessage.setTextColor(Color.BLACK);
            	else
            		tvMessage.setTextColor(Color.LTGRAY);
            	llUserChatArea.addView(tvMessage);
            	tvMessage.setVisibility(View.VISIBLE);
            	//.
            	if (DataMessage.DataType.equals(TDrawingDefines.FileExtension)) {
                	TDrawings Drawings = new TDrawings();
                	try {
                		Drawings.LoadFromByteArray(DataMessage.Data,0);
                		byte[] BMPData = Drawings.SaveAsBitmapData("png"); 
                    	Bitmap BMP = BitmapFactory.decodeByteArray(BMPData, 0,BMPData.length);
                    	ImageView ivMessage = new ImageView(this);
                    	ivMessage.setImageBitmap(BMP);
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
                	finally {
                		Drawings.Destroy();
                	}
            	}
            	else
                	if (DataMessage.DataType.equals("png") || DataMessage.DataType.equals("jpg") || DataMessage.DataType.equals("jpeg") || DataMessage.DataType.equals("gif") || DataMessage.DataType.equals("bmp")) {
                    	Bitmap BMP = BitmapFactory.decodeByteArray(DataMessage.Data, 0,DataMessage.Data.length);
                    	ImageView ivMessage = new ImageView(this);
                    	ivMessage.setImageBitmap(BMP);
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
        	}
    	}
    	else {
        	TextView tvMessage = new TextView(this);
        	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(Message.Timestamp)).GetDateTime())+" "+SenderName+": "+Message.Message);
        	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	tvMessage.setLayoutParams(LP);
        	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        	if (flContactUser)
        		tvMessage.setTextColor(Color.BLACK);
        	else
        		tvMessage.setTextColor(Color.LTGRAY);
        	llUserChatArea.addView(tvMessage);
        	tvMessage.setVisibility(View.VISIBLE);
    	}
    	svUserChatArea.postDelayed(new Runnable() {
    	    @Override
    	    public void run() {
            	try {
                    svUserChatArea.fullScroll(View.FOCUS_DOWN);
            	}
            	catch (Throwable E) {
            		TGeoLogApplication.Log_WriteError(E);
            	}
    	    }
		},100);
    }
    
    private class TMessageSending extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TIncomingMessage 	Message;
    	private File 				MessageSourceFile;
    	//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TMessageSending(TIncomingMessage pMessage, File pMessageSourceFile, int pOnCompletionMessage) {
    		Message = pMessage;
    		MessageSourceFile = pMessageSourceFile;
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
    	        	if (UserAgent.Server.User != null) {
    	        		UserAgent.Server.User.IncomingMessages_SendNewMessage(ContactUser.UserID, Message.Message);
    	        		Message.Timestamp = OleDate.UTCCurrentTimestamp();
    	        	}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
				if (MessageSourceFile != null)
					MessageSourceFile.delete();
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserMessagingPanel.this, TUserMessagingPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserMessagingPanel.this);    
		            	progressDialog.setMessage(TUserMessagingPanel.this.getString(R.string.SSending));    
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
			while (!Canceller.flCancel) {
				try {
					TGeoScopeServerUser.TUserDescriptor User = null;
    	        	if ((ContactUser.UserID != 0) && (UserAgent.Server.User != null))
    	        		User = UserAgent.Server.User.GetUserInfo(ContactUser.UserID); 
					//.
					Canceller.Check();
		    		//.
					if (User != null)
						PanelHandler.obtainMessage(OnCompletionMessage,User).sendToTarget();
					//. 
		        	Thread.sleep(ContactUserInfoUpdateInterval);
	        	}
	        	catch (InterruptedException E) {
	        		return; //. ->
	        	}
	        	catch (CancelException E) {
	        		return; //. ->
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

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserMessagingPanel.this, TUserMessagingPanel.this.getString(R.string.SUpdatingContactUser)+E.getMessage(), Toast.LENGTH_SHORT).show();
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
	public final Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_SENT: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		TIncomingMessage Message = (TIncomingMessage)msg.obj;
                		ChatArea_AddMessage(getString(R.string.SMe), Message, false);
                		//.
                		edUserChatComposeMessage.setText("");
                	}
                	catch (Exception E) {
                		Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
                		Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
