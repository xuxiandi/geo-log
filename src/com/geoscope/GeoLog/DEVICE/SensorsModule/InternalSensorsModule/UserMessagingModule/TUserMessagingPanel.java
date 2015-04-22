package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
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
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt32ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedDataContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedTaggedDataContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDeliveryDataType;
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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule.TUserMessagingModule.TUserMessaging;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUserMessagingPanel extends Activity {

	public static Hashtable<String, TUserMessagingPanel> Panels = new Hashtable<String, TUserMessagingPanel>();

	public static final int DefaultChannelCheckpointInterval = 1000*10; 
	public static final int ContactUserInfoUpdateInterval = 1000*30; //. seconds
	//.
	public static final int CameraImageMaxSize = 1024;
	
	public static final String MessageTextFileName 		= "MessagePicture.txt";
	public static final String MessagePictureFileName 	= "MessageText.png";
	
	private static final int REQUEST_DRAWINGEDITOR			= 1;
	private static final int REQUEST_ADDPICTUREFROMCAMERA	= 2;
	
	private static int NextMessageID = 0;
	private synchronized static int GetNextMessageID() {
		NextMessageID++;
		return NextMessageID;
	}
	
	private static int Calling_NextNotificationID = 0;
	public synchronized static int Calling_GetNextNotificationID() {
		Calling_NextNotificationID++;
		return Calling_NextNotificationID;
	}
	//.
	@SuppressWarnings("deprecation")
	public static void Calling_ShowNotification(TUserMessaging UserMessaging, Context context, Intent UserMessagingPanelIntent, int NotificationID) {
        PendingIntent ContentIntent = PendingIntent.getActivity(context, 0, UserMessagingPanelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //.
        CharSequence TickerText = context.getString(R.string.SYouHaveUnreadMessage);
        long Timestamp = System.currentTimeMillis();
        int Icon = R.drawable.icon;
		Notification notification = new Notification(Icon,TickerText,Timestamp);
        CharSequence ContentTitle = context.getString(R.string.SNewMessageFromUser);
        CharSequence ContentText = context.getString(R.string.SClickHereToSee);
        notification.setLatestEventInfo(context, ContentTitle, ContentText, ContentIntent);
        notification.defaults = (notification.defaults | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
        //.
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NotificationID, notification);
	}
	
	public static void Calling_HideNotification(Context context, int NotificationID) {
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NotificationID);
	}
	
	
	public static class TUserScreenEventReceiver extends BroadcastReceiver {
		
		private TUserMessagingPanel Panel;
		
		public TUserScreenEventReceiver(TUserMessagingPanel pPanel) {
			Panel = pPanel;
		}

		@Override
	     public void onReceive(Context context, Intent intent) {
	    	 if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
	    		 Panel.SetUserStatus(TUserStatusDataType.USERSTATUS_IDLE);
	    		 return; //. ->
	         };
	         if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
	        	 Panel.SetUserStatus(TUserStatusDataType.USERSTATUS_NOTAVAILABLE);
	    		 return; //. ->
	         };
	         if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
	        	 Panel.SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
	    		 return; //. ->
	         };
	     }
	}
	
	public static class ExtendedEditText extends EditText {
		 
	    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	 
	    }
	 
	    public ExtendedEditText(Context context, AttributeSet attrs) {
	        super(context, attrs);
	 
	    }
	 
	    public ExtendedEditText(Context context) {
	        super(context);
	 
	    }
	 
	    @Override
	    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
	        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) 
	            dispatchKeyEvent(event);
	        return super.onKeyPreIme(keyCode, event);
	    }
	 
	}	
	
	public static class TOutChannelMessageTable {
		
		public static class TItem {
			
			public short	MessageType;
			public String 	Message;
			public String 	MessagePreamble;
			public String 	MessageStatus;
			public TextView MessageView;
			
			public TItem(short pMessageType) {
				MessageType = pMessageType;
			}
			
			public TItem(short pMessageType, String pMessage, String pMessagePreamble, String pMessageStatus, TextView pMessageView) {
				MessageType = pMessageType;
				Message = pMessage;
				MessagePreamble = pMessagePreamble;
				MessageStatus = pMessageStatus;
				MessageView = pMessageView;
			}
		}
		
		
		public Hashtable<Integer, TItem> Items = new Hashtable<Integer, TItem>();
		
		public void AddItem(int MessageID, TItem Item) {
			Items.put(MessageID, Item);
		}
	}

	
	private boolean flExists = false;
	//.
	private TUserAgent 					UserAgent;
	private short						UserStatus = TUserStatusDataType.USERSTATUS_UNKNOWN;
	private TUserStatusUpdating 		UserStatusUpdating = null;
	private TUserScreenEventReceiver	UserScreenEventReceiver = null;
	//.
	private TTracker Tracker;
	//.
	private TUserMessagingModule UserMessagingModule;
	//.
	private int 			UserMessagingID;
	private TUserMessaging 	UserMessaging = null;
	//.
	private TGeoScopeServerUser.TUserDescriptor 	ContactUser = new TGeoScopeServerUser.TUserDescriptor();
	public short									ContactUserStatus = TUserStatusDataType.USERSTATUS_UNKNOWN; 
	private TContactUserUpdating    				ContactUserUpdating;
	//.
	private TUserMessagingParametersDataType.TParameters 	OutChannel_Parameters = null;
	private TAsyncProcessing 								OutChannel_Parameters_Sending = null;
	private TOutChannelMessageTable							OutChannel_MessageTable = new TOutChannelMessageTable();
	//.
	private TStreamChannelProcessorAbstract 				InChannel_Reader = null;
	private TUserMessagingParametersDataType.TParameters 	InChannel_Parameters = null;
	//.
	private TAsyncProcessing Initializing = null;
	private TAsyncProcessing ConnectDisconnectMessageSending = null;
	private TAsyncProcessing MessageDeliverySending = null;
	//.
	private boolean flConnected = false;
	//.
	private TextView lbUserChatContactUser;
	private TextView lbStatus;
	private ScrollView svUserChatArea;
	private LinearLayout llUserChatArea;
	private LinearLayout llUserChatMessageComposer;
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
	        Bundle extras = getIntent().getExtras(); 
        	UserMessagingID = extras.getInt("UserMessagingID");
        	//.
        	if (UserMessagingID == 0) {
                finish();
                return; //. ->
        	}
        	//.
			UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	UserMessagingModule = Tracker.GeoLog.SensorsModule.InternalSensorsModule.UserMessagingModule;
	    	//.
            UserMessaging = UserMessagingModule.UserMessagings.GetItemByID(UserMessagingID);
        	if (UserMessaging == null) {
                finish();
                return; //. ->
        	}
        	//.
        	ContactUser.UserID = 0;
	        //.
            TUserMessagingPanel UMP = Panels.get(UserMessaging.SessionID());
            if (UMP != null) {
            	UserMessaging = null;
                finish();
                return; //. ->
            }
	        //.
	        Panels.put(UserMessaging.SessionID(), this);
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
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
        llUserChatMessageComposer = (LinearLayout)findViewById(R.id.llUserChatMessageComposer);
        //.
        edUserChatComposeMessage = (EditText)findViewById(R.id.edUserChatComposeMessage);
        edUserChatComposeMessage.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	String Message = edUserChatComposeMessage.getText().toString();
                	if (!Message.equals(""))
                		SendUserMessage(Message);
                	//.
                	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
                }
				return false;
			}
        });
        edUserChatComposeMessage.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) 
					SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
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
            		SendUserMessage(Message);
            }
        });
        //.
        btnUserChatTextEntry = (Button)findViewById(R.id.btnUserChatTextEntry);
        btnUserChatTextEntry.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edUserChatComposeMessage, InputMethodManager.SHOW_FORCED);
		    	//.
		    	SetUserStatus(TUserStatusDataType.USERSTATUS_COMPOSING);
			}
        });
        //.
        btnUserChatDrawingSend = (Button)findViewById(R.id.btnUserChatDrawingSend);
        btnUserChatDrawingSend.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				SendUserDrawing();
			}
        });
        //.
        btnUserChatPictureSend = (Button)findViewById(R.id.btnUserChatPictureSend);
        btnUserChatPictureSend.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				SendUserPicture();
			}
        });
        //.
        ContactUser_UpdateInfo();
        //.
        flExists = true;
        //.
        StartInitialization();
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	if (UserMessaging != null)
    		Panels.remove(UserMessaging.SessionID());
    	//.
    	StopInitialization();
    	//.
    	try {
			Finalization();
		} catch (Exception E) {
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
    protected void onResume() {
    	super.onResume();
    	//.
    	if (UserMessaging.CallingNotificationID != 0) {
    		Calling_HideNotification(this.getApplicationContext(), UserMessaging.CallingNotificationID);
    		UserMessaging.CallingNotificationID = 0;
    	}
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
                            	new TUserMessageSending(TUserMessageDataType.TYPE_IMAGE_DRW,DRW,F,MESSAGE_SENT);
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
        	//.
        	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
            break; //. >
            
        case REQUEST_ADDPICTUREFROMCAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetCameraPictureTempFile(this);
				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
					
					private byte[] PictureData;
					
					@Override
					public void Process() throws Exception {
		            	if (F.exists()) {
	            			FileInputStream fs = new FileInputStream(F);
	            			try
	            			{
	            				BitmapFactory.Options options = new BitmapFactory.Options();
	            				options.inDither=false;
	            				options.inPurgeable=true;
	            				options.inInputShareable=true;
	            				options.inTempStorage=new byte[1024*256]; 							
	            				Rect rect = new Rect();
	            				Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), rect, options);
	            				try {
	            					int ImageMaxSize = options.outWidth;
	            					if (options.outHeight > ImageMaxSize)
	            						ImageMaxSize = options.outHeight;
	            					float MaxSize = CameraImageMaxSize;
	            					float Scale = MaxSize/ImageMaxSize; 
	            					Matrix matrix = new Matrix();     
	            					matrix.postScale(Scale,Scale);
	            					//.
	            					Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
	            					try {
	            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            						try {
	            							if (!resizedBitmap.compress(CompressFormat.JPEG, 100, bos)) 
	            								throw new Exception("error of commpressing the picture to JPEG format"); //. =>
	            							PictureData = bos.toByteArray();
	            						}
	            						finally {
	            							bos.close();
	            						}
	            					}
	            					finally {
	            						resizedBitmap.recycle();
	            					}
	            				}
	            				finally {
	            					bitmap.recycle();
	            				}
	            			}
	            			finally
	            			{
	            				fs.close();
	            			}
		            	}
						else
		        			throw new Exception(context.getString(R.string.SImageWasNotPrepared)); //. =>  
					}
					
					@Override
					public void DoOnCompleted() throws Exception {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
                    	//.
                    	new TUserMessageSending(TUserMessageDataType.TYPE_IMAGE_JPG,PictureData,null,MESSAGE_SENT);
					}
					
					@Override
					public void DoOnException(Exception E) {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
						//.
						Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				};
				PictureProcessing.Start();
        	}  
        	//.
        	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
            break; //. >
        }
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
        //.
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
        	SetUserStatus(TUserStatusDataType.USERSTATUS_COMPOSING);
        	return; //. ->
        };
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
        	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
        	return; //. ->
        }
    }
    
    @Override
	public void onBackPressed() {
    	SetUserStatus(TUserStatusDataType.USERSTATUS_CLOSING);
	    new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SConfirmation)
        .setMessage(R.string.SCloseTheMessaging)
	    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	        	SetUserStatus(TUserStatusDataType.USERSTATUS_NOTAVAILABLE);
	    		TUserMessagingPanel.this.finish();
	    	}
	    })
	    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	        	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
	    	}
	    })
	    .setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
	        	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
			}
		})
	    .show();
	}	
	
    private void StartInitialization() {
		OutChannel_Parameters = new TUserMessagingParametersDataType.TParameters();
		OutChannel_Parameters.UserID = UserAgent.User().UserID;
		OutChannel_Parameters.CheckpointInterval = DefaultChannelCheckpointInterval;
		//.
		llUserChatMessageComposer.setVisibility(View.GONE);
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
				//.
				DoOnInitialization();
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
        TUserMessagingPanel.this.UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SConnecting));
        //.
    	SendParameters();
    }
    
    private void Finalization() throws Exception {
    	StopMessaging();
    	//.
    	if (flConnected)
    		Disconnect();
    	//.
    	if (ConnectDisconnectMessageSending != null) {
    		ConnectDisconnectMessageSending.Cancel();
    		ConnectDisconnectMessageSending = null;
    	}
    	//.
    	if (UserMessaging != null) {
    		TAsyncProcessing UserMessageStopping = new TAsyncProcessing() {
        		
    			@Override
    			public void Process() throws Exception {
    				UserMessagingModule.StopUserMessaging(UserMessaging.Object, UserMessaging.SessionID());
    			}
    			
    			@Override
    			public void DoOnException(Exception E) {
    		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			}
    		};
    		UserMessageStopping.Start();
    	}
    	//.
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
				//. TUserMessagingPanel.this.DoOnStatusMessage("ReceivedPacket size: "+Integer.toString(ReadSize));
				TUserMessagingPanel.this.PostOnStatusMessage("");
			}
		}, new TStreamChannelProcessorAbstract.TOnIdleHandler(UserMessaging.InChannel) {
			@Override
			public void DoOnIdle(TCanceller Canceller) {
				TUserMessagingPanel.this.PostOnStatusMessage(TUserMessagingPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
			}
		}, new TStreamChannelProcessorAbstract.TOnExceptionHandler(UserMessaging.InChannel) {
			@Override
			public void DoOnException(Exception E) {
				TUserMessagingPanel.this.PostOnException(E);
			}
		});
		//.
		UserMessaging.InChannel.OnDataHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TDoOnDataHandler() {
			@Override
			public void DoOnData(TDataType DataType) {
				try {
					InChannel_Reader_DoOnData(DataType);
				} catch (Exception E) {
					PostOnException(E); 					
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
		if (DataType instanceof TUserMessageDataType) {
			TTimestampedTypedTaggedDataContainerType.TValue Value = ((TUserMessageDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
		if (DataType instanceof TUserMessageDeliveryDataType) {
			TTimestampedInt32ContainerType.TValue Value = ((TUserMessageDeliveryDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_CONFIRMATION_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
		if (DataType instanceof TUserStatusDataType) {
			TTimestampedInt16ContainerType.TValue Value = ((TUserStatusDataType)DataType).ContainerValue();
			MessageHandler.obtainMessage(MESSAGE_USERSTATUS_RECEIVED,Value).sendToTarget();
			return; //. ->
		}
	}
	
    private void SendParameters() {
    	if (OutChannel_Parameters_Sending != null) 
    		OutChannel_Parameters_Sending.Cancel();
    	//.
    	OutChannel_Parameters_Sending = new TAsyncProcessing() {
    		
    		public static final int RetryInterval = 100; //. ms

    		
			private TTimestampedTypedDataContainerType.TValue Parameters;
			
			@Override
			public void Process() throws Exception {
				Parameters = new TTimestampedTypedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), TUserMessagingParametersDataType.TYPE_XML, OutChannel_Parameters.ToByteArray());
				//.
				while (!Canceller.flCancel) {
					if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
						synchronized (UserMessaging.OutChannel) {
							UserMessaging.OutChannel.UserMessagingParameters.SetContainerTypeValue(Parameters);
							UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessagingParameters);
						}
						//.
						return; //. ->
					}
					//.
					Thread.sleep(RetryInterval);
				}
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		OutChannel_Parameters_Sending.Start();
    }
    
    private void DoOnParametersReceived(TTimestampedTypedDataContainerType.TValue Value) throws Exception {
		if (Value.ValueType == TUserMessagingParametersDataType.TYPE_XML) {
			InChannel_Parameters = new TUserMessagingParametersDataType.TParameters();
			InChannel_Parameters.FromByteArray(Value.Value);
			//.
	        ContactUserUpdating = new TContactUserUpdating((int)InChannel_Parameters.UserID, MESSAGE_UPDATECONTACTUSER);
	        //.
			//. UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SConfigurationIsReceived));
	        //.
			Connect();
		}
    }
    
    private void Connect() {
    	if (ConnectDisconnectMessageSending != null)
    		ConnectDisconnectMessageSending.Cancel();
    	//.
    	ConnectDisconnectMessageSending = new TAsyncProcessing() {
    		
    		public static final int RetryInterval = 100; //. ms

    		
			private TTimestampedTypedTaggedDataContainerType.TValue ConnectMessage;
			
			@Override
			public void Process() throws Exception {
				ConnectMessage = new TTimestampedTypedTaggedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), TUserMessageDataType.TYPE_OPENSESSION, GetNextMessageID(), null);
				//.
				while (!Canceller.flCancel) {
					if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
						synchronized (UserMessaging.OutChannel) {
							UserMessaging.OutChannel.UserMessage.SetContainerTypeValue(ConnectMessage);
							UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessage);
						}
						//.
						return; //. ->
					}
					//.
					Thread.sleep(RetryInterval);
				}
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
        		OutChannel_MessageTable.AddItem(ConnectMessage.ValueTag, new TOutChannelMessageTable.TItem(ConnectMessage.ValueType));
        		//.
				//. UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SConnecting));
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		ConnectDisconnectMessageSending.Start();
    }
    
    private void DoOnConnected() throws Exception {
		flConnected = true;
		//.
		UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SConnected1));
		//.
		StartMessaging();
    }
    
    private void Disconnect() throws InterruptedException {
    	if (ConnectDisconnectMessageSending != null)
    		ConnectDisconnectMessageSending.Cancel();
    	//.
    	ConnectDisconnectMessageSending = new TAsyncProcessing() {
    		
    		public static final int RetryInterval = 100; //. ms

    		
			TTimestampedTypedTaggedDataContainerType.TValue DisconnectMessage;
			
			@Override
			public void Process() throws Exception {
				DisconnectMessage = new TTimestampedTypedTaggedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), TUserMessageDataType.TYPE_CLOSESESSION, GetNextMessageID(), null);
				//.
				while (!Canceller.flCancel) {
					if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
						synchronized (UserMessaging.OutChannel) {
							UserMessaging.OutChannel.UserMessage.SetContainerTypeValue(DisconnectMessage);
							UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessage);
						}
						//.
						return; //. ->
					}
					//.
					Thread.sleep(RetryInterval);
				}
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
        		OutChannel_MessageTable.AddItem(DisconnectMessage.ValueTag, new TOutChannelMessageTable.TItem(DisconnectMessage.ValueType));
        		//.
				UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SDiconnecting));
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		ConnectDisconnectMessageSending.Start();
		//.
		ConnectDisconnectMessageSending.Wait(100/*ms, disconnect timeout*/);
    }
    
    private void DoOnDisconnected() throws InterruptedException {
		flConnected = false;
    }

    private void StartMessaging() {
		llUserChatMessageComposer.setVisibility(View.VISIBLE);
		//.
		if (UserStatusUpdating != null)
			UserStatusUpdating.Cancel();
		UserStatusUpdating = new TUserStatusUpdating();
		//.
    	PowerManager powerManager = (PowerManager)getSystemService(Activity.POWER_SERVICE);
    	boolean ScreenIsOn = powerManager.isScreenOn();
    	//.
    	if (ScreenIsOn)
    		SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
    	else
    		SetUserStatus(TUserStatusDataType.USERSTATUS_ONLINE);
		//.
		if (UserScreenEventReceiver != null) 
			unregisterReceiver(UserScreenEventReceiver);
		UserScreenEventReceiver = new TUserScreenEventReceiver(this);
		IntentFilter UserScreenEventReceiverFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		UserScreenEventReceiverFilter.addAction(Intent.ACTION_SCREEN_OFF);
		UserScreenEventReceiverFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(UserScreenEventReceiver, UserScreenEventReceiverFilter);
    }
    
    private void StopMessaging() {
		if (UserScreenEventReceiver != null) {
			unregisterReceiver(UserScreenEventReceiver);
			UserScreenEventReceiver = null;
		}
		//.
		if (UserStatusUpdating != null) {
			UserStatusUpdating.Cancel();
			UserStatusUpdating = null;
		}
		//.
		if (OutChannel_Parameters_Sending != null) {
			OutChannel_Parameters_Sending.Cancel();
			OutChannel_Parameters_Sending = null;
		}
		//.
    	if (MessageDeliverySending != null) {
    		MessageDeliverySending.Cancel();
    		MessageDeliverySending = null;
    	}
    }
    
	private void DoOnUserMessageSent(final TTimestampedTypedTaggedDataContainerType.TValue Message) throws Exception {
		OutChannel_MessageTable.AddItem(Message.ValueTag, new TOutChannelMessageTable.TItem(Message.ValueType));
		//. public sent message with status "sent"
		UserMessaging_View_AddMessage(getString(R.string.SMe), Message.Timestamp,Message.ValueType,Message.ValueTag,Message.Value, true);
		//.
		edUserChatComposeMessage.setText("");
	}
	
	private void DoOnMessageReceived(final TTimestampedTypedTaggedDataContainerType.TValue Message) throws Exception {
		if (TUserMessageDataType.TYPE_OPENSESSION(Message.ValueType)) {
			//. open session signal has been received: do nothing but send the message delivery signal
			//. UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, getString(R.string.SConnectSignalIsReceived));
		}
		else
			if (TUserMessageDataType.TYPE_CLOSESESSION(Message.ValueType)) { 
				UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.RED, getString(R.string.SConnectionIsClosed));
				//.
				llUserChatMessageComposer.setVisibility(View.GONE);
				//.
				if (!flConnected)
					finish();
				return; // ->
			}
			else { 
				//. public the message
				String CN;
				if (ContactUser.UserName != null)
					CN = ContactUser.UserName;
				else 
					CN = "?";
				UserMessaging_View_AddMessage(CN, Message.Timestamp,Message.ValueType,Message.ValueTag,Message.Value, false);
			}
		//.
		DoOnMessageDelivered(Message);
	}
	
	private void DoOnMessageDelivered(final TTimestampedTypedTaggedDataContainerType.TValue Message) {
		if (MessageDeliverySending != null) 
			MessageDeliverySending.Cancel();
		//.
		MessageDeliverySending = new TAsyncProcessing() {
    		
    		public static final int RetryInterval = 100; //. ms

    		
			@Override
			public void Process() throws Exception {
				TTimestampedInt32ContainerType.TValue DeliveryValue = new TTimestampedInt32ContainerType.TValue(OleDate.UTCCurrentTimestamp(), Message.ValueTag);
				//.
				while (!Canceller.flCancel) {
					if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
						synchronized (UserMessaging.OutChannel) {
							UserMessaging.OutChannel.UserMessageDelivery.SetContainerTypeValue(DeliveryValue);
							UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessageDelivery);
						}
						//.
						return; //. ->
					}
					//.
					Thread.sleep(RetryInterval);
				}
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
				//. UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, "message delivery is sent");
			} 
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		MessageDeliverySending.Start();
	}

	private void DoOnMessageDeliveryConfirmationReceived(final TTimestampedInt32ContainerType.TValue Confirmation) throws Exception {
		TOutChannelMessageTable.TItem SentMessage = OutChannel_MessageTable.Items.get(Confirmation.Value);
		if (SentMessage != null) {
			if (TUserMessageDataType.TYPE_OPENSESSION(SentMessage.MessageType)) 
				DoOnConnected();
			else
				if (TUserMessageDataType.TYPE_CLOSESESSION(SentMessage.MessageType)) 
					DoOnDisconnected();
				else {
					SentMessage.MessageStatus = " "+"["+getString(R.string.SDelivered)+"]"; //. change the message status to "delivered"
					SentMessage.MessageView.setText(SentMessage.MessagePreamble+SentMessage.MessageStatus+": "+SentMessage.Message);
				}
		}
	}

	private void PostOnStatusMessage(String S) {
		MessageHandler.obtainMessage(MESSAGE_DOONSTATUSMESSAGE,S).sendToTarget();
	}

	private void DoOnStatusMessage(String S) {
		if (S.length() > 0) {
			lbStatus.setText(S);
			lbStatus.setVisibility(View.VISIBLE);
			//.
			//. TUserMessagingPanel.this.UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.BLUE, S);
		}
		else {
			lbStatus.setText("");
			lbStatus.setVisibility(View.GONE);
		}
	}

	private void PostOnException(Throwable E) {
		MessageHandler.obtainMessage(MESSAGE_DOONEXCEPTION,E).sendToTarget();
	}
	
	private void DoOnException(Throwable E) {
		if (E instanceof OperationException) {
			OperationException OE = (OperationException)E;
			switch (OE.Code) {
			
			case TSensorsModule.SENSORSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR:
				TUserMessagingPanel.this.finish();
				return; //. ->
			}
		}
		//.
		String EM = E.getMessage();
		if (EM == null) 
			EM = E.getClass().getName();
		//.
		Toast.makeText(TUserMessagingPanel.this,EM,Toast.LENGTH_LONG).show();
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
	
    private void SendUserMessage(String Message) {
    	try {
			new TUserMessageSending(TUserMessageDataType.TYPE_TEXT_UTF8,Message.getBytes("utf-8"),null,MESSAGE_SENT);
		} catch (UnsupportedEncodingException E) {
		}
    }
    
    private void SendUserDrawing() {
    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"UserChatDrawing"+"."+TDrawingDefines.FileExtension;
    	//.
    	Intent intent = new Intent(TUserMessagingPanel.this, TDrawingEditor.class);
    	intent.putExtra("FileName", FileName); 
    	intent.putExtra("ReadOnly", false); 
    	intent.putExtra("SpaceContainersAvailable", false); 
    	startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
    	//.
    	SetUserStatus(TUserStatusDataType.USERSTATUS_COMPOSING);
    }
    
    private static File GetCameraPictureTempFile(Context context) {
    	return new File(TGeoLogApplication.TempFolder,"picture.jpg");
    }
    
    private void SendUserPicture() {
		final CharSequence[] _items;
		_items = new CharSequence[2];
		_items[0] = getString(R.string.SAddImage);
		_items[1] = getString(R.string.SAddImageFromFile);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SOperations);
		builder.setNegativeButton(getString(R.string.SCancel),null);
		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				//.
            	try {
					switch (arg1) {
					
					case 0: //. take a picture
		      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(GetCameraPictureTempFile(TUserMessagingPanel.this))); 
		      		    startActivityForResult(intent, REQUEST_ADDPICTUREFROMCAMERA);    		
						break; //. >
						
					case 1: //. take a picture form file
				    	TFileSystemFileSelector FileSelector = new TFileSystemFileSelector(TUserMessagingPanel.this)
				        //. .setFilter(".*\\.bmp|.*\\.png|.*\\.gif|.*\\.jpg|.*\\.jpeg")
				    	.setFilter(".*\\.jpg|.*\\.jpeg")        
				    	.setOpenDialogListener(new TFileSystemFileSelector.OpenDialogListener() {
				        	
				            @Override
				            public void OnSelectedFile(String fileName) {
				            	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
				            	//.
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
				                            	new TUserMessageSending(TUserMessageDataType.TYPE_IMAGE_JPG,Data,null,MESSAGE_SENT);
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
				        			Toast.makeText(TUserMessagingPanel.this, S, Toast.LENGTH_LONG).show();  						
								}
				            }

							@Override
							public void OnCancel() {
				            	SetUserStatus(TUserStatusDataType.USERSTATUS_AVAILABLE);
							}
				        });
				    	FileSelector.show();    	
			            break; //. >
					}
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TUserMessagingPanel.this, TUserMessagingPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    	//.
    	SetUserStatus(TUserStatusDataType.USERSTATUS_COMPOSING);
    }
    
    private void ContactUser_DoOnStatusChanged(short LastContactUserStatus) {
		ContactUser_UpdateInfo();
		//.
		String USC = getString(R.string.SUserStatusIsChanged);
		switch (ContactUserStatus) {
		
		case TUserStatusDataType.USERSTATUS_AVAILABLE:
			if (LastContactUserStatus != TUserStatusDataType.USERSTATUS_COMPOSING)
				UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.GREEN, USC+TUserStatusDataType.USERSTATUS(ContactUserStatus, TUserMessagingPanel.this));
			break;
			
		case TUserStatusDataType.USERSTATUS_IDLE:
			UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.YELLOW, USC+TUserStatusDataType.USERSTATUS(ContactUserStatus, TUserMessagingPanel.this));
			break;
			
		case TUserStatusDataType.USERSTATUS_NOTAVAILABLE:
		case TUserStatusDataType.USERSTATUS_IGNORING:
		case TUserStatusDataType.USERSTATUS_CLOSING:
			UserMessaging_View_AddSystemMessage(OleDate.UTCCurrentTimestamp(), Color.RED, USC+TUserStatusDataType.USERSTATUS(ContactUserStatus, TUserMessagingPanel.this));
			break;
		}
    }
    
    private void ContactUser_UpdateInfo() {
    	StringBuilder SB = new StringBuilder();
    	//.
    	SB.append(getString(R.string.SUser));
    	if (ContactUser.UserID != 0) 
    		SB.append(" "+ContactUser.UserName+" / "+ContactUser.UserFullName);
    	else
    		SB.append(" "+"?");
		SB.append("  "+"["+TUserStatusDataType.USERSTATUS(ContactUserStatus, this)+"]");
		//.
		lbUserChatContactUser.setText(SB.toString());
    }
    
    private void UserMessaging_View_AddSystemMessage(double MessageTimestamp, int MessageColor, String Message) {
    	String MessagePreamble = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(MessageTimestamp)).GetDateTime())+": ";
    	TextView tvMessage = new TextView(this);
    	String MessageStr = MessagePreamble+Message;
    	tvMessage.setText(MessageStr);
    	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	tvMessage.setLayoutParams(LP);
    	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
    	tvMessage.setTextColor(MessageColor);
    	llUserChatArea.addView(tvMessage);
    	tvMessage.setVisibility(View.VISIBLE);
    	//.
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
    
    private void UserMessaging_View_AddMessage(String SenderName, double MessageTimestamp, short MessageType, int MessageID, byte[] Message, boolean flOutMessage) throws Exception {
    	String MessageStr = "";
    	String MessagePreamble = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(MessageTimestamp)).GetDateTime())+" "+SenderName;
    	String MessageStatus = "";
    	if (flOutMessage)
    		MessageStatus = " "+"["+getString(R.string.SSent)+"]";
    	//.
    	TextView tvMessage;
    	if (MessageType != TUserMessageDataType.TYPE_TEXT_UTF8) {
        	tvMessage = new TextView(this);
        	tvMessage.setText(MessagePreamble+MessageStatus+": ");
        	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	tvMessage.setLayoutParams(LP);
        	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        	if (flOutMessage)
        		tvMessage.setTextColor(Color.LTGRAY);
        	else
        		tvMessage.setTextColor(Color.BLACK);
        	llUserChatArea.addView(tvMessage);
        	tvMessage.setVisibility(View.VISIBLE);
        	//.
        	if (Message != null) {
            	//.
            	if (MessageType == TUserMessageDataType.TYPE_IMAGE_DRW) {
                	TDrawings Drawings = new TDrawings();
                	try {
                		Drawings.LoadFromByteArray(Message,0);
                		byte[] BMPData = Drawings.SaveAsBitmapData("png"); 
                    	final Bitmap BMP = BitmapFactory.decodeByteArray(BMPData, 0,BMPData.length);
                    	ImageView ivMessage = new ImageView(this);
                    	ivMessage.setImageBitmap(BMP);
                    	ivMessage.setOnLongClickListener(new OnLongClickListener() {
							
							@Override
							public boolean onLongClick(View v) {
								try {
									String FN = SaveMessagePicture(BMP);
									//.
					                Toast.makeText(TUserMessagingPanel.this, getString(R.string.SPictureHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
								}
								catch (Exception E) {
					                Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
								return true;
							}
						});
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
                	finally {
                		Drawings.Destroy();
                	}
            	}
            	else
                	if (TUserMessageDataType.TYPE_IMAGE(MessageType)) {
                    	final Bitmap BMP = BitmapFactory.decodeByteArray(Message, 0,Message.length);
                    	ImageView ivMessage = new ImageView(this);
                    	ivMessage.setImageBitmap(BMP);
                    	ivMessage.setOnLongClickListener(new OnLongClickListener() {
							
							@Override
							public boolean onLongClick(View v) {
								try {
									String FN = SaveMessagePicture(BMP);
									//.
					                Toast.makeText(TUserMessagingPanel.this, getString(R.string.SPictureHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
								}
								catch (Exception E) {
					                Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
								return true;
							}
						});
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
        	}
    	}
    	else {
        	tvMessage = new TextView(this);
        	MessageStr = (new String(Message,"utf-8"));
        	final String Text = MessagePreamble+MessageStatus+": "+MessageStr;
        	tvMessage.setText(Text);
        	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	tvMessage.setLayoutParams(LP);
        	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        	if (flOutMessage)
        		tvMessage.setTextColor(Color.LTGRAY);
        	else
        		tvMessage.setTextColor(Color.BLACK);
        	tvMessage.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					try {
						String FN = SaveMessageText(Text);
						//.
		                Toast.makeText(TUserMessagingPanel.this, getString(R.string.STextHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
		                Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
					return true;
				}
			});
        	llUserChatArea.addView(tvMessage);
        	tvMessage.setVisibility(View.VISIBLE);
    	}
    	//.
    	if (flOutMessage) {
    		TOutChannelMessageTable.TItem Item = OutChannel_MessageTable.Items.get(MessageID);
    		if (Item != null) {
    			Item.Message = MessageStr;
    			Item.MessagePreamble = MessagePreamble;
    			Item.MessageStatus = MessageStatus;
    			Item.MessageView = tvMessage;
    		}
    	}
    	//.
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
    
    private String SaveMessageText(String Text) throws IOException {
		String FN = TGeoLogApplication.GetTempFolder()+"/"+MessageTextFileName;
		FileOutputStream FOS = new FileOutputStream(FN);
		try {
			FOS.write(Text.getBytes("UTF-8"));
		}
		finally {
			FOS.close();
		}
		return FN;
    }
    
    private String SaveMessagePicture(Bitmap Picture) throws IOException {
		String FN = TGeoLogApplication.GetTempFolder()+"/"+MessagePictureFileName;
		FileOutputStream FOS = new FileOutputStream(FN);
		try {
			Picture.compress(CompressFormat.PNG, 100, FOS);
		}
		finally {
			FOS.close();
		}
		return FN;
    }
    
    private class TUserMessageSending extends TCancelableThread {

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
    	
    	public TUserMessageSending(short pMessageType, byte[] pMessage, File pMessageSourceFile, int pOnCompletionMessage) {
    		super();
    		//.
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
				TTimestampedTypedTaggedDataContainerType.TValue Value;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				Value = new TTimestampedTypedTaggedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), MessageType, GetNextMessageID(), Message);
					if (!UserMessaging.OutChannel.DestinationChannel_IsConnected()) 
						throw new IOException("connection does not exist"); //. =>
					synchronized (UserMessaging.OutChannel) {
	    				UserMessaging.OutChannel.UserMessage.SetContainerTypeValue(Value);
	    				UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserMessage);
					}
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
						if (!flExists)
							return; //. ->
						//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserMessagingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
    	
    	private long UserID;
    	private int OnCompletionMessage;
    	
    	public TContactUserUpdating(long pUserID, int pOnCompletionMessage) {
    		super();
    		//.
    		UserID = pUserID;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				TGeoScopeServerUser.TUserDescriptor User = null;
	        	if ((UserID != 0) && (UserAgent.Server.User != null))
	        		User = UserAgent.Server.User.GetUserInfo(UserID); 
				//.
				Canceller.Check();
	    		//.
				if (User != null)
					TUserMessagingPanel.this.MessageHandler.obtainMessage(OnCompletionMessage,User).sendToTarget();
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

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserMessagingPanel.this, TUserMessagingPanel.this.getString(R.string.SUpdatingContactUser)+E.getMessage(), Toast.LENGTH_LONG).show();
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
    	
		public static final int RetryInterval = 100; //. ms

		
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	
    	public TUserStatusUpdating() {
    		super();
    		//.
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
					//. status sending ...
					while (!Canceller.flCancel) {
						if (UserMessaging.OutChannel.DestinationChannel_IsConnected()) {
							synchronized (UserMessaging.OutChannel) {
								UserMessaging.OutChannel.UserStatus.SetContainerTypeValue(Status);
								UserMessaging.OutChannel.DoOnData(UserMessaging.OutChannel.UserStatus);
							}
							//.
							break; //. >
						}
						//.
						Thread.sleep(RetryInterval);
					}
				}
			}
			catch (InterruptedException IE) {
			}
			catch (Throwable E) {
				PostOnException(E);
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
    
	private static final int MESSAGE_DOONSTATUSMESSAGE 		= 1;
	private static final int MESSAGE_DOONEXCEPTION 			= 2;
	private static final int MESSAGE_RECEIVED 				= 3;
	private static final int MESSAGE_CONFIRMATION_RECEIVED 	= 4;
	private static final int MESSAGE_SENT 					= 5;
	private static final int MESSAGE_UPDATECONTACTUSER 		= 6;
	private static final int MESSAGE_PARAMETERS_SEND 		= 7;
	private static final int MESSAGE_PARAMETERS_RECEIVED 	= 8;
	private static final int MESSAGE_USERSTATUS_RECEIVED 	= 9;
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_DOONEXCEPTION:
					if (!flExists)
						break; // . >
    				Throwable E = (Throwable)msg.obj;
    				//.
    				DoOnException(E);
    				//.
    				break; // . >

    			case MESSAGE_DOONSTATUSMESSAGE:
					if (!flExists)
						break; // . >
    				String S = (String)msg.obj;
    				//.
    				DoOnStatusMessage(S);
    				//.
    				break; // . >
    				
                case MESSAGE_PARAMETERS_SEND: 
    				if (!flExists)
    	            	break; //. >
    		    	SendParameters();
                	break; //. >

                case MESSAGE_PARAMETERS_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedDataContainerType.TValue Value = (TTimestampedTypedDataContainerType.TValue)msg.obj;
            			//.
        				DoOnParametersReceived(Value);
                	}
                	catch (Exception E2) {
                		Toast.makeText(TUserMessagingPanel.this, E2.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_SENT: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedTaggedDataContainerType.TValue Value = (TTimestampedTypedTaggedDataContainerType.TValue)msg.obj;
            			//.
            			DoOnUserMessageSent(Value);
                	}
                	catch (Exception E1) {
                		Toast.makeText(TUserMessagingPanel.this, E1.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                	
                case MESSAGE_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedTypedTaggedDataContainerType.TValue Value = (TTimestampedTypedTaggedDataContainerType.TValue)msg.obj;
            			//.
            			DoOnMessageReceived(Value);
                	}
                	catch (Exception E2) {
                		Toast.makeText(TUserMessagingPanel.this, E2.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                	
                case MESSAGE_CONFIRMATION_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
            			TTimestampedInt32ContainerType.TValue Value = (TTimestampedInt32ContainerType.TValue)msg.obj;
                		//.
            			DoOnMessageDeliveryConfirmationReceived(Value);
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
            		//.
            		ContactUser_UpdateInfo();
                	break; //. >

                case MESSAGE_USERSTATUS_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		TTimestampedInt16ContainerType.TValue Value = (TTimestampedInt16ContainerType.TValue)msg.obj;
                		if (ContactUserStatus != Value.Value) {
                			short LastContactUserStatus = ContactUserStatus;
                			ContactUserStatus = Value.Value;
                			//.
                			ContactUser_DoOnStatusChanged(LastContactUserStatus);
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
}
