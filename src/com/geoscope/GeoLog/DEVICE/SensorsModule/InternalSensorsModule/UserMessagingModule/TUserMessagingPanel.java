package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
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

import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedDataContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessagingParametersDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserStatusDataType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystemFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TStreamChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TStreamChannelProcessorAbstract;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule.TUserMessagingModule.TUserMessaging;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUserMessagingPanel extends Activity {

	public static Hashtable<String, TUserMessagingPanel> Panels = new Hashtable<String, TUserMessagingPanel>();

	public static final int DefaultChannelCheckpointInterval = 1000*10; 
	public static final int ContactUserInfoUpdateInterval = 1000*30; //. seconds
	
	private static final int REQUEST_DRAWINGEDITOR	= 1;
	
	private boolean flExists = false;
	//.
	private TUserAgent 			UserAgent;
	private short				UserStatus = TUserStatusDataType.USERSTATUS_UNKNOWN;
	private TUserStatusUpdating UserStatusUpdating = null;
	//.
	private TTracker Tracker;
	//.
	private TUserMessagingModule UserMessagingModule;
	//.
	private int 			UserMessagingID;
	private TUserMessaging 	UserMessaging = null; 
	//.
	private TGeoScopeServerUser.TUserDescriptor 	ContactUser = new TGeoScopeServerUser.TUserDescriptor();
	private TContactUserUpdating    				ContactUserUpdating;
	//.
	private TUserMessagingParametersDataType.TParameters 	OutChannel_Parameters = null;
	private TAsyncProcessing 								OutChannel_Parameters_Sending = null;
	//.
	private TStreamChannelProcessorAbstract 				InChannel_Reader = null;
	private TUserMessagingParametersDataType.TParameters 	InChannel_Parameters = null;
	//.
	private TAsyncProcessing Initializing = null;
	//.
	private TextView lbUserChatContactUser;
	private TextView lbStatus;
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
	    	//.
	        Bundle extras = getIntent().getExtras(); 
        	UserMessagingID = extras.getInt("UserMessagingID");
            UserMessaging = UserMessagingModule.UserMessagings.GetItemByID(UserMessagingID);
        	ContactUser.UserID = 0;
	        //.
            TUserMessagingPanel UMP = Panels.get(UserMessaging.SessionID());
            if (UMP != null) {
                finish();
                return; //. ->
            }
	        //.
	        Panels.put(UserMessaging.SessionID(), this);
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return; //. ->
		}
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.usermessagingmodule_usermessaging_panel);
        //.
        lbUserChatContactUser = (TextView)findViewById(R.id.lbUserChatContactUser);
        //.
        lbStatus = (TextView)findViewById(R.id.lbStatus);
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
        //.
        flExists = true;
        //.
        StartInitialization();
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	Panels.remove(UserMessaging.SessionID());
    	//.
    	if (OutChannel_Parameters_Sending != null) {
        	try {
        		OutChannel_Parameters_Sending.Destroy();
    		} catch (Exception E) {
    		}
    		OutChannel_Parameters_Sending = null;
    	}	
    	//.
    	StopInitialization();
    	//.
    	try {
			Finalize();
		} catch (Exception E) {
		}
		//.
		if (UserStatusUpdating != null) {
			UserStatusUpdating.Cancel();
			UserStatusUpdating = null;
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
                            	new TMessageSending(TUserMessageDataType.TYPE_IMAGE_DRW,DRW,F,MESSAGE_SENT);
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
    
    private void StartInitialization() {
		OutChannel_Parameters = new TUserMessagingParametersDataType.TParameters();
		OutChannel_Parameters.UserID = UserAgent.User().UserID;
		OutChannel_Parameters.CheckpointInterval = DefaultChannelCheckpointInterval;
		//.
    	Initializing = new TAsyncProcessing(TUserMessagingPanel.this,getString(R.string.SWaitAMoment)) {
			
			@Override
			public void Process() throws Exception {
				if (UserMessaging.Object == null) {
					UserMessaging.Object = new TCoGeoMonitorObject(UserAgent.Server, UserMessaging.ObjectID);
					UserMessaging.Object.CheckData();
				}
				//.
				Thread.sleep(100);
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
				InChannel_Reader_Initialize();
				DoOnInitialization();
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void DoOnCancelled() {
				TUserMessagingPanel.this.finish();
			}
		};
		Initializing.Start();
    }
    
    private void StopInitialization() {
    	if (Initializing != null) {
    		Initializing.Cancel();
    		Initializing = null;
    	}
    }
    
    private void DoOnInitialization() {
    	OutChannel_Parameters_Sending = new TAsyncProcessing() {
    		
    		public static final int RetryInterval = 100; //. ms
			
			@Override
			public void Process() throws Exception {
				TTimestampedTypedDataContainerType.TValue Parameters = new TTimestampedTypedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), TUserMessagingParametersDataType.TYPE_XML, OutChannel_Parameters.ToByteArray());
				//.
				while (!Canceller.flCancel) {
					if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
						UserMessaging.OutChannel.UserMessagingParameters.SetContainerTypeValue(Parameters);
						UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessagingParameters);
						//.
						return; //. ->
					}
					//.
					Thread.sleep(RetryInterval);
				}
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
				UserStatusUpdating = new TUserStatusUpdating();
				SetUserStatus(TUserStatusDataType.USERSTATUS_ONLINE);
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		};
		OutChannel_Parameters_Sending.Start();
    }
    
    private void Finalize() throws Exception {
        InChannel_Reader_Finalize();
    }
    
    private void InChannel_Reader_Initialize() throws Exception {
    	InChannel_Reader_Finalize();
    	//.
    	TGeoScopeServerInfo.TInfo ServersInfo = UserAgent.Server.Info.GetInfo();
		if (!ServersInfo.IsSpaceDataServerValid()) 
			throw new Exception("Invalid space data server"); //. =>
		//.
		InChannel_Reader = new TStreamChannelProcessor(this, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, UserAgent.Server.User.UserID,UserAgent.Server.User.UserPassword, UserMessaging.Object, UserMessaging.InChannel, UserMessaging.SessionID(), new TStreamChannelProcessorAbstract.TOnProgressHandler(UserMessaging.InChannel) {
			@Override
			public void DoOnProgress(int ReadSize, TCanceller Canceller) {
				TUserMessagingPanel.this.DoOnStatusMessage("");
			}
		}, new TStreamChannelProcessorAbstract.TOnIdleHandler(UserMessaging.InChannel) {
			@Override
			public void DoOnIdle(TCanceller Canceller) {
				TUserMessagingPanel.this.DoOnStatusMessage(TUserMessagingPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
			}
		}, new TStreamChannelProcessorAbstract.TOnExceptionHandler(UserMessaging.InChannel) {
			@Override
			public void DoOnException(Exception E) {
				TUserMessagingPanel.this.DoOnException(E);
			}
		});
		//.
		UserMessaging.InChannel.OnDataHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TDoOnDataHandler() {
			@Override
			public void DoOnData(TDataType DataType) {
				try {
					InChannel_Reader_DoOnData(DataType);
				} catch (Exception E) {
					DoOnException(E); 					
				}
			}
		};
		//.
		InChannel_Reader.Start();
    }
    
    private void InChannel_Reader_Finalize() throws Exception {
    	if (InChannel_Reader != null) {
    		InChannel_Reader.Destroy(false);
    		InChannel_Reader = null;
    	}
    }
    
    @SuppressWarnings("unused")
	private boolean InChannel_Reader_IsInitialized() {
    	return (InChannel_Reader != null);
    }
    
	public void InChannel_Reader_DoOnData(TDataType DataType) throws Exception {
		if (DataType instanceof TUserMessagingParametersDataType) {
			TTimestampedTypedDataContainerType.TValue Value = ((TUserMessagingParametersDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_PARAMETERS_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
		if (DataType instanceof TUserStatusDataType) {
			Integer Value = ((TUserStatusDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_USERSTATUS_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
		if (DataType instanceof TUserMessageDataType) {
			TTimestampedTypedDataContainerType.TValue Value = ((TUserMessageDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
	}
	
	private synchronized void SetUserStatus(short pUserStatus) {
		UserStatus = pUserStatus;
		//.
		if (UserStatusUpdating != null) 
			UserStatusUpdating.StartUpdate();
	}
	
	private synchronized short GetUserStatus() {
		return UserStatus;
	}
	
    private void UpdateContactUserInfo() {
    	if (ContactUser.UserID != 0) {
    		String State;
    		if (ContactUser.UserIsOnline)
    			State = "[ONLINE]";
    		else
    			State = "[offline]";
            lbUserChatContactUser.setText(getString(R.string.SUser)+" "+ContactUser.UserName+" "+State+" / "+ContactUser.UserFullName);
    	}
    	else
            lbUserChatContactUser.setText(getString(R.string.SUser)+" "+"?");
    }
    
    private void SendMessage(String Message) {
    	try {
			new TMessageSending(TUserMessageDataType.TYPE_STRING_UTF8,Message.getBytes("utf-8"),null,MESSAGE_SENT);
		} catch (UnsupportedEncodingException E) {
		}
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
        //. .setFilter(".*\\.bmp|.*\\.png|.*\\.gif|.*\\.jpg|.*\\.jpeg")
    	.setFilter(".*\\.jpg|.*\\.jpeg")        
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
                            	new TMessageSending(TUserMessageDataType.TYPE_IMAGE_JPG,Data,null,MESSAGE_SENT);
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
    
    private void ChatArea_AddMessage(String SenderName, double MessageTimestamp, short MessageType, byte[] Message, boolean flContactUser) throws Exception {
    	if (MessageType != TUserMessageDataType.TYPE_STRING_UTF8) {
        	if (Message != null) {
            	TextView tvMessage = new TextView(this);
            	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(MessageTimestamp)).GetDateTime())+" "+SenderName+": ");
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
            	if (MessageType == TUserMessageDataType.TYPE_IMAGE_DRW) {
                	TDrawings Drawings = new TDrawings();
                	try {
                		Drawings.LoadFromByteArray(Message,0);
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
                	if (TUserMessageDataType.TYPE_IMAGE(MessageType)) {
                    	Bitmap BMP = BitmapFactory.decodeByteArray(Message, 0,Message.length);
                    	ImageView ivMessage = new ImageView(this);
                    	ivMessage.setImageBitmap(BMP);
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
        	}
    	}
    	else {
        	TextView tvMessage = new TextView(this);
        	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(MessageTimestamp)).GetDateTime())+" "+SenderName+": "+(new String(Message,"utf-8")));
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
    	
    	private short 	MessageType;
    	private byte[] 	Message;
    	private File 	MessageSourceFile;
    	//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TMessageSending(short pMessageType, byte[] pMessage, File pMessageSourceFile, int pOnCompletionMessage) {
    		MessageType = pMessageType;
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
				TTimestampedTypedDataContainerType.TValue Value;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				Value = new TTimestampedTypedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), MessageType, Message); 
    				UserMessaging.OutChannel.UserMessage.SetContainerTypeValue(Value);
    				UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessage);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
				if (MessageSourceFile != null)
					MessageSourceFile.delete();
				//.
	    		TUserMessagingPanel.this.MessageHandler.obtainMessage(OnCompletionMessage,Value).sendToTarget();
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
						TUserMessagingPanel.this.MessageHandler.obtainMessage(OnCompletionMessage,User).sendToTarget();
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
		
    private class TUserStatusUpdating extends TCancelableThread {
    	
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	
    	public TUserStatusUpdating() {
    		_Thread = new Thread(this);
    		Start();
    	}
    	
		public void Start() {
    		_Thread.start();
		}
		
		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					ProcessSignal.WaitOne(OutChannel_Parameters.CheckpointInterval);
					if (Canceller.flCancel)
						return; //. ->
					//.
					TTimestampedInt16ContainerType.TValue Status = new TTimestampedInt16ContainerType.TValue(OleDate.UTCCurrentTimestamp(),GetUserStatus());  
					UserMessaging.OutChannel.UserStatus.SetContainerTypeValue(Status);
					UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserStatus);
				}
			}
			catch (Throwable E) {
			}
		}
		
		public void StartUpdate() {
			ProcessSignal.Set();
		}
		
		@Override
		public void Cancel() {
			super.Cancel();
			//.
			ProcessSignal.Set();
		}

		@Override
		public void CancelAndWait() throws InterruptedException {
			Cancel();
			// .
			Wait();
		}
    }
    
	private static final int MESSAGE_SHOWSTATUSMESSAGE 		= 1;
	private static final int MESSAGE_SHOWEXCEPTION 			= 2;
	private static final int MESSAGE_RECEIVED 				= 3;
	private static final int MESSAGE_SENT 					= 4;
	private static final int MESSAGE_UPDATECONTACTUSER 		= 5;
	private static final int MESSAGE_PARAMETERS_RECEIVED 	= 6;
	private static final int MESSAGE_USERSTATUS_RECEIVED 	= 7;
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
					if (!flExists)
						break; // . >
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TUserMessagingPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >

    			case MESSAGE_SHOWSTATUSMESSAGE:
					if (!flExists)
						break; // . >
    				String S = (String)msg.obj;
    				//.
    				if (S.length() > 0) {
    					lbStatus.setText(S);
    					lbStatus.setVisibility(View.VISIBLE);
    				}
    				else {
    					lbStatus.setText("");
    					lbStatus.setVisibility(View.GONE);
    				}
    				// .
    				break; // . >
    				
                case MESSAGE_SENT: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedDataContainerType.TValue Value = (TTimestampedTypedDataContainerType.TValue)msg.obj;
                		ChatArea_AddMessage(getString(R.string.SMe), Value.Timestamp,Value.ValueType,Value.Value, false);
                		//.
                		edUserChatComposeMessage.setText("");
                	}
                	catch (Exception E1) {
                		Toast.makeText(TUserMessagingPanel.this, E1.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                	
                case MESSAGE_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedDataContainerType.TValue Value = (TTimestampedTypedDataContainerType.TValue)msg.obj;
                		ChatArea_AddMessage(getString(R.string.SMe), Value.Timestamp,Value.ValueType,Value.Value, true);
                	}
                	catch (Exception E2) {
                		Toast.makeText(TUserMessagingPanel.this, E2.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                	
                case MESSAGE_UPDATECONTACTUSER:
    				if (!flExists)
    	            	break; //. >
            		TGeoScopeServerUser.TUserDescriptor User = (TGeoScopeServerUser.TUserDescriptor)msg.obj;
            		ContactUser.Assign(User);
            		UpdateContactUserInfo();
                	break; //. >

                case MESSAGE_PARAMETERS_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedDataContainerType.TValue Value = (TTimestampedTypedDataContainerType.TValue)msg.obj;
            			if (Value.ValueType == TUserMessagingParametersDataType.TYPE_XML) {
            				InChannel_Parameters = new TUserMessagingParametersDataType.TParameters();
            				InChannel_Parameters.FromByteArray(Value.Value);
            				//.
            				ContactUser.UserID = (int)InChannel_Parameters.UserID;
            				//.
            		        ContactUserUpdating = new TContactUserUpdating(MESSAGE_UPDATECONTACTUSER);
            			}
                	}
                	catch (Exception E2) {
                		Toast.makeText(TUserMessagingPanel.this, E2.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnStatusMessage(String S) {
		if (flExists)
			MessageHandler.obtainMessage(MESSAGE_SHOWSTATUSMESSAGE,S).sendToTarget();
	}
	
	private void DoOnException(Throwable E) {
		if (flExists)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
