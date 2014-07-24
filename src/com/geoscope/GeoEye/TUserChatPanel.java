package com.geoscope.GeoEye;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingXMLDataMessage;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoEye.Utils.Graphics.TDrawings;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TFileSystem;

@SuppressLint("HandlerLeak")
public class TUserChatPanel extends Activity {

	public static Hashtable<Integer, TUserChatPanel> Panels = new Hashtable<Integer, TUserChatPanel>();

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
        	if ((MessageID != 0) && (UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
        		Message = UserAgent.Server.User.IncomingMessages.GetMessageByID(MessageID);
        }
        //.
        if (Message != null) {
    		TUserChatPanel UCP = Panels.get(Message.SenderID);
    		if (UCP != null) {
    			try {
        			UCP.PublishMessage(Message);
    			} catch (Exception E) {
    	            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
    	            finish();
    	            return; //. ->
    			}
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
			@Override
            public void onClick(View v) {
            	String Message = edUserChatComposeMessage.getText().toString();
            	if (!Message.equals(""))
            		SendMessage(Message);
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
        if (Message != null) 
			try {
	        	PublishMessage(Message);
			} catch (Exception E) {
	            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
	            finish();
	            return; //. ->
			}
        //.
    	if ((UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
    		UserIncomingMessages_LastCheckInterval = UserAgent.Server.User.IncomingMessages.SetFastCheckInterval(); //. speed up messages updating
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
            	if ((UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
            		UserAgent.Server.User.IncomingMessages.RestoreCheckInterval(UserIncomingMessages_LastCheckInterval);
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
    	String FileName = TReflector.GetTempFolder()+"/"+"UserChatDrawing"+"."+TDrawingDefines.FileExtension;
    	//.
    	Intent intent = new Intent(TUserChatPanel.this, TDrawingEditor.class);
    	intent.putExtra("FileName", FileName); 
    	intent.putExtra("ReadOnly", false); 
    	intent.putExtra("SpaceContainersAvailable", false); 
    	startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
    }
    
    private void SendPicture() {
		final String[] FileList;
		final File FileSelectorPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
	    if(FileSelectorPath.exists()) {
	        FilenameFilter filter = new FilenameFilter() {
	            public boolean accept(File dir, String filename) {
	            	if ((new File(dir.getAbsolutePath()+"/"+filename)).isDirectory())
	            		return false; //. ->
	            	String Extension = TFileSystem.FileName_GetExtension(filename);
	            	if (Extension == null)
	            		return false; //. ->
            		Extension = Extension.toLowerCase(Locale.ENGLISH);
            		return (Extension.equals("png") || Extension.equals("jpg") || Extension.equals("jpeg") || Extension.equals("bmp") || Extension.equals("gif"));
	            }
	        };
	        FileList = FileSelectorPath.list(filter);
	    }
	    else 
	        FileList= new String[0];
	    //.
	    AlertDialog.Builder builder = new AlertDialog.Builder(TUserChatPanel.this);
        builder.setTitle(R.string.SChooseFile);
        builder.setItems(FileList, new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
                File ChosenFile = new File(FileSelectorPath.getAbsolutePath()+"/"+FileList[which]);
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
    	        			Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
    					}
                	}
				}
				catch (Throwable E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TUserChatPanel.this, S, Toast.LENGTH_SHORT).show();  						
				}
            }
        });
        Dialog FileDialog = builder.create();
        FileDialog.show();	    						
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
			try {
				while (!Canceller.flCancel) {
		        	Thread.sleep(ContactUserInfoUpdateInterval);
		        	//.
					try {
						TGeoScopeServerUser.TUserDescriptor User = null;
	    	        	if (UserAgent.Server.User != null)
	    	        		User = UserAgent.Server.User.GetUserInfo(ContactUser.UserID); 
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
	        	try {
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
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
