package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.Defines.TGeoLocation;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjects;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.Space.URL.TURL;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;

@SuppressLint("HandlerLeak")
public class TUserPanel extends Activity {

	public static final int MODE_NORMAL 			= 0;
	public static final int MODE_OPENMESSAGING 		= 1;
	public static final int MODE_OPENLIVEMESSAGING 	= 2;
	public static final int MODE_OPENVIDEOPHONE 	= 3;
	
	public static class TUserCoGeoMonitorObjects {
		
		public TCoGeoMonitorObjects.TDescriptors 	Objects;
		//.
		public TCoGeoMonitorObject.TDescriptor 		CommunicationObject;
			    
		public void Update(TUserAgent UserAgent, long UserID, TCanceller Canceller) throws Exception {
	    	Objects = UserAgent.User().GetUserCoGeoMonitorObjects(UserID);
	    	if (Canceller != null)
	    		Canceller.Check();
			//.
			CommunicationObject = GetAnObjectForCommunication();
			//.
			if (CommunicationObject != null) {
				CommunicationObject.Object = new TCoGeoMonitorObject(UserAgent.Server, CommunicationObject.idComponent, CommunicationObject.Name);
				TGeographServerObjectController GSOC = CommunicationObject.Object.GeographServerObjectController();
				synchronized (GSOC) {
					boolean flKeepConnectionLast = GSOC.KeepConnection();
					try {
						GSOC.Connect();
						try {
					    	if (Canceller != null)
					    		Canceller.Check();
		    				//.
							byte[] ObjectSchemaData = GSOC.Component_ReadAllCUAC(new int[] {1}/*object side*/);
							//.
					    	if (Canceller != null)
					    		Canceller.Check();
		    				//.
							if (ObjectSchemaData != null)
								CommunicationObject.ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
							//.
							byte[] ObjectDeviceSchemaData = GSOC.Component_ReadAllCUAC(new int[] {2/*device side*/});
							//.
					    	if (Canceller != null)
					    		Canceller.Check();
		    				//.
							if (ObjectDeviceSchemaData != null)
								CommunicationObject.ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
						}
						finally {
							GSOC.Disconnect();
						}
					}
					finally {
						GSOC.Connection_flKeepAlive = flKeepConnectionLast;
					}
				}
			}
	    }
		
		public void Update(TUserAgent UserAgent, long UserID) throws Exception {
			Update(UserAgent, UserID, null);
		}
		
	    private TCoGeoMonitorObject.TDescriptor GetAnObjectForCommunication() throws InterruptedException {
	    	if (Objects == null)
	    		return null; //. ->
	    	int Cnt = Objects.Items.size();
	    	for (int I = 0; I < Cnt; I++) {
	    		TCoGeoMonitorObject.TDescriptor Object = Objects.Items.get(I);
	    		if ((Object.ObjectModel != null) && Object.ObjectModel.UserMessaging_IsSupported() && Object.ObjectModel.UserVideoPhone_IsSupported() && Object.flOnline)
	    			return Object; //. -> 
	    	}
			return null; 
	    }
	}
	
	public static final int REQUEST_SETUSERACTIVITY = 1;
	public static final int REQUEST_SHOWONREFLECTOR = 2;
	
	public boolean flExists = false;
	//.
	private TReflectorComponent Component;
	//.
	private int Mode = MODE_NORMAL;
	//.
	private ImageView ivUser;
	private EditText edUserName;
	private EditText edUserFullName;
	private EditText edUserContactInfo;
	private EditText edUserDomains;
	private CheckBox cbUserTaskEnabled;
	private EditText edUserConnectionState;
	private ProgressBar pbCommunicationProgress;
	private Button btnUserLocation;
	private Button btnUserMessaging;
	private Button btnUserLiveMessaging;
	private Button btnUserVideoPhone;
	private Button btnUserCoGeoMonitorObjects;
	//.
	private Button btnUserCurrentActivity;
	private Button btnUserCurrentActivityComponentList;
	private Button btnUserLastActivities;
	//.
	private LinearLayout 	llUserTasks;
	private Button 			btnUserTasks;
	private Button 			btnUserOriginateedTasks;
	//.
	private long 			UserID = 0;	
    private TUserDescriptor UserInfo = null; 
    private TActivity 		UserCurrentActivity = null;
	//.
	private TUserCoGeoMonitorObjects 	UserCoGeoMonitorObjects = null;
	private TAsyncProcessing			UserCoGeoMonitorObjects_Updating = null;
	//.
	private TUpdating	Updating = null;
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
			ComponentID = extras.getInt("ComponentID");
        	UserID = extras.getLong("UserID");
        	Mode = extras.getInt("Mode");
        }
        Component = TReflectorComponent.GetComponent(ComponentID);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_panel);
        //.
        ivUser = (ImageView)findViewById(R.id.ivUser);
        ivUser.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				final CharSequence[] _items;
				_items = new CharSequence[1];
				_items[0] = getString(R.string.SGetURLFile);
				AlertDialog.Builder builder = new AlertDialog.Builder(TUserPanel.this);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: 
			            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
			            		com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL(UserID);
			            		URL.ConstructURLFile(URLFN);
			            		//.
				    		    new AlertDialog.Builder(TUserPanel.this)
				    	        .setIcon(android.R.drawable.ic_dialog_alert)
				    	        .setTitle(R.string.SInfo)
				    	        .setMessage(TUserPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TUserPanel.this.getString(R.string.SUseItForImport))
				    		    .setPositiveButton(R.string.SOk, null)
				    		    .show();
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        edUserName = (EditText)findViewById(R.id.edUserName);
        edUserFullName = (EditText)findViewById(R.id.edUserFullName);
        edUserContactInfo = (EditText)findViewById(R.id.edUserContactInfo);
        edUserDomains = (EditText)findViewById(R.id.edUserDomains);
        cbUserTaskEnabled = (CheckBox)findViewById(R.id.cbUserTaskEnabled);
        cbUserTaskEnabled.setOnClickListener(new OnClickListener(){
        	
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
				try {
	        		if (UserInfo == null)
	        			return; //. ->
					User_SetTaskEnabled(UserInfo, checked);
		    	}
		    	catch (Exception E) {
		            Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        edUserConnectionState = (EditText)findViewById(R.id.edUserConnectionState);
        pbCommunicationProgress = (ProgressBar)findViewById(R.id.pbCommunicationProgress);
        btnUserLocation = (Button)findViewById(R.id.btnUserLocation);
        btnUserLocation.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
        		User_GetLocation(UserInfo);
            }
        });
        btnUserMessaging = (Button)findViewById(R.id.btnUserMessaging);
        btnUserMessaging.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
				//.
        		User_OpenMessaging(UserInfo);
            	//.
            	finish();
            }
        });
        btnUserMessaging.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
        		if (UserInfo == null)
        			return false; //. ->
        		//.
				final CharSequence[] _items;
				_items = new CharSequence[1];
				_items[0] = getString(R.string.SGetURLFile);
				AlertDialog.Builder builder = new AlertDialog.Builder(TUserPanel.this);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: 
			            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
			            		com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Messaging.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Messaging.TURL(UserID);
			            		URL.Name = TUserPanel.this.getString(R.string.SMessaging)+": "+UserInfo.UserFullName;
			            		URL.ConstructURLFile(URLFN);
			            		//.
				    		    new AlertDialog.Builder(TUserPanel.this)
				    	        .setIcon(android.R.drawable.ic_dialog_alert)
				    	        .setTitle(R.string.SInfo)
				    	        .setMessage(TUserPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TUserPanel.this.getString(R.string.SUseItForImport))
				    		    .setPositiveButton(R.string.SOk, null)
				    		    .show();
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        btnUserLiveMessaging = (Button)findViewById(R.id.btnUserLiveMessaging);
        btnUserLiveMessaging.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if ((UserCoGeoMonitorObjects == null) || (UserCoGeoMonitorObjects.CommunicationObject == null) || !UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_IsSupported())
        			return; //. ->
        		//.
				try {
					UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, TUserPanel.this);            	
	            	//.
	            	finish();
		    	}
		    	catch (Exception E) {
		            Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });
        btnUserLiveMessaging.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
        		if (UserInfo == null)
        			return false; //. ->
        		//.
				final CharSequence[] _items;
				_items = new CharSequence[1];
				_items[0] = getString(R.string.SGetURLFile);
				AlertDialog.Builder builder = new AlertDialog.Builder(TUserPanel.this);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: 
			            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
			            		com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.LiveMessaging.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.LiveMessaging.TURL(UserID);
			            		URL.Name = TUserPanel.this.getString(R.string.SLiveMessaging)+": "+UserInfo.UserFullName;
			            		URL.ConstructURLFile(URLFN);
			            		//.
				    		    new AlertDialog.Builder(TUserPanel.this)
				    	        .setIcon(android.R.drawable.ic_dialog_alert)
				    	        .setTitle(R.string.SInfo)
				    	        .setMessage(TUserPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TUserPanel.this.getString(R.string.SUseItForImport))
				    		    .setPositiveButton(R.string.SOk, null)
				    		    .show();
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        btnUserVideoPhone = (Button)findViewById(R.id.btnUserVideoPhone);
        btnUserVideoPhone.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if ((UserCoGeoMonitorObjects == null) || (UserCoGeoMonitorObjects.CommunicationObject == null) || !UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_IsSupported())
        			return; //. ->
				//.
				try {
					UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, TUserPanel.this);            	
	            	//.
	            	finish();
		    	}
		    	catch (Exception E) {
		            Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });
        btnUserVideoPhone.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
        		if (UserInfo == null)
        			return false; //. ->
        		//.
				final CharSequence[] _items;
				_items = new CharSequence[1];
				_items[0] = getString(R.string.SGetURLFile);
				AlertDialog.Builder builder = new AlertDialog.Builder(TUserPanel.this);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: 
			            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
			            		com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.VideoPhone.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.VideoPhone.TURL(UserID);
			            		URL.Name = TUserPanel.this.getString(R.string.SVideoPhone)+": "+UserInfo.UserFullName;
			            		URL.ConstructURLFile(URLFN);
			            		//.
				    		    new AlertDialog.Builder(TUserPanel.this)
				    	        .setIcon(android.R.drawable.ic_dialog_alert)
				    	        .setTitle(R.string.SInfo)
				    	        .setMessage(TUserPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TUserPanel.this.getString(R.string.SUseItForImport))
				    		    .setPositiveButton(R.string.SOk, null)
				    		    .show();
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        btnUserCoGeoMonitorObjects = (Button)findViewById(R.id.btnUserCoGeoMonitorObjects);
        btnUserCoGeoMonitorObjects.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
				//.
        		User_ShowCoGeoMonitorObjects(UserInfo);
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivityComponentList = (Button)findViewById(R.id.btnUserCurrentActivityComponentList);
        btnUserCurrentActivityComponentList.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
            	Intent intent = new Intent(TUserPanel.this, TUserActivityComponentListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",UserInfo.UserID);
            	intent.putExtra("ActivityID",UserCurrentActivity.ID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        btnUserLastActivities = (Button)findViewById(R.id.btnUserLastActivities);
        btnUserLastActivities.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
            	Intent intent = new Intent(TUserPanel.this, TUserActivityListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",UserInfo.UserID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        llUserTasks = (LinearLayout)findViewById(R.id.llUserTasks);
    	btnUserTasks = (Button)findViewById(R.id.btnUserTasks);
    	btnUserTasks.setOnClickListener(new OnClickListener() {
    		
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
        		Intent intent = new Intent(TUserPanel.this, TUserTaskListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",UserInfo.UserID);
            	intent.putExtra("flOriginator",false);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
    	btnUserOriginateedTasks = (Button)findViewById(R.id.btnUserOriginatedTasks);
    	btnUserOriginateedTasks.setOnClickListener(new OnClickListener() {
    		
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
        		Intent intent = new Intent(TUserPanel.this, TUserTaskListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",UserInfo.UserID);
            	intent.putExtra("flOriginator",true);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        //.
        final int HintID = THintManager.HINT__User_panel_hint;
        final TextView lbHint = (TextView)findViewById(R.id.lbHint);
        String Hint = THintManager.GetHint(HintID, this);
        if (Hint != null) {
        	lbHint.setText(Hint);
            lbHint.setOnLongClickListener(new OnLongClickListener() {
            	
    			@Override
    			public boolean onLongClick(View v) {
    				THintManager.SetHintAsDisabled(HintID);
    	        	lbHint.setVisibility(View.GONE);
    	        	//.
    				return true;
    			}
    		});
            //.
        	lbHint.setVisibility(View.VISIBLE);
        }
        else
        	lbHint.setVisibility(View.GONE);
        //. UserCoGeoMonitorObjects updating
        try {
   		 	switch (Mode) {
   		 	
   		 	case MODE_NORMAL:
   				UserCoGeoMonitorObjects_StartUpdating();
   				break; //. >
   		 	}
		} catch (InterruptedException IE) {
		}
        //.
        flExists = true;
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
        try {
			UserCoGeoMonitorObjects_StopUpdating();
		} catch (InterruptedException IE) {
		}
		//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
        //.
        StartUpdating();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SETUSERACTIVITY: 
        	if (resultCode == RESULT_OK) 
        		StartUpdating();
            break; //. >

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) 
        		finish();
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TUserDescriptor UserInfo = null; 
        private TActivity 		UserCurrentActivity = null;
        //.
		private TUserCoGeoMonitorObjects _UserCoGeoMonitorObjects = null;
		
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		super();
    		//.
    		flShowProgress = pflShowProgress;
    		flClosePanelOnCancel = pflClosePanelOnCancel;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				try {
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				UserInfo = UserAgent.Server.User.GetUserInfo(UserID);
	    				try {
		    				UserCurrentActivity = UserAgent.Server.User.GetUserCurrentActivity(UserID);
	    				}
	    				catch (Exception E) {
	    					UserCurrentActivity = null;
	    					//.
	    	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	    				}
	    				//. 
	    	   		 	switch (Mode) {
	    	   		 	
	    	   		 	case MODE_OPENLIVEMESSAGING:
	    	   		 	case MODE_OPENVIDEOPHONE:
	    					_UserCoGeoMonitorObjects = new TUserCoGeoMonitorObjects();
	    					//.
	    					_UserCoGeoMonitorObjects.Update(UserAgent, UserID, Canceller);
	    	   				break; //. >
	    	   		 	}
	    				
					}
					finally {
						if (flShowProgress)
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
					}
    				//.
	    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
	        	}
	        	catch (InterruptedException E) {
	        	}
	        	catch (IOException E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	        	}
	        	catch (Throwable E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
	        	}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TUserPanel.this.UserInfo = UserInfo;
		            	TUserPanel.this.UserCurrentActivity = UserCurrentActivity;
	           		 	//.
	           		 	TUserPanel.this.Update();
		            	//.
		            	if (_UserCoGeoMonitorObjects != null) {
							UserCoGeoMonitorObjects = _UserCoGeoMonitorObjects;
							//.
							UserCoGeoMonitorObjects_UpdateControls();
		            	}
	           		 	//.
	           		 	switch (Mode) {
	           		 	
	           		 	case MODE_OPENMESSAGING:
	                		if (UserInfo != null) {
		                		User_OpenMessaging(UserInfo);
		                    	//.
		                    	finish();
	                		}
	                    	//.
	           		 		break; //. >
	           		 		
	           		 	case MODE_OPENLIVEMESSAGING:
	                		if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.CommunicationObject != null) && UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_IsSupported()) 
		        				try {
		        					UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, TUserPanel.this);            	
		        	            	//.
		        	            	finish();
		        		    	}
		        		    	catch (Exception Ex) {
		        		            Toast.makeText(TUserPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
		        		    	}
	                		//.
	           		 		break; //. >
	           		 		
	           		 	case MODE_OPENVIDEOPHONE:
	                		if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.CommunicationObject != null) && UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_IsSupported()) 
		        				try {
		        					UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, TUserPanel.this);            	
		        	            	//.
		        	            	finish();
		        		    	}
		        		    	catch (Exception Ex) {
		        		            Toast.makeText(TUserPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
		        		    	}
		        		    //.
	           		 		break; //. >
	           		 	}
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TUserPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserPanel.this);    
		            	progressDialog.setMessage(TUserPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserPanel.this.finish();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!isFinishing()) && progressDialog.isShowing()) 
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
	
    private void Update() {
    	flUpdate = true; 
    	try {
        	if (UserInfo != null) {
        		edUserName.setText(UserInfo.UserName);
        		edUserFullName.setText(UserInfo.UserFullName);
        		edUserContactInfo.setText(UserInfo.UserContactInfo);
        		edUserDomains.setText(UserInfo.UserDomains);
        		cbUserTaskEnabled.setVisibility(UserInfo.UserDomainsAreSpecified() ? View.VISIBLE : View.GONE);
        		cbUserTaskEnabled.setChecked(UserInfo.UserIsTaskEnabled);
        		llUserTasks.setVisibility(UserInfo.UserDomainsAreSpecified() ? View.VISIBLE : View.GONE);
        	}
        	else {
        		edUserName.setText(""); 
        		edUserFullName.setText("");
        		edUserContactInfo.setText(""); 
        		edUserDomains.setText("");
        		cbUserTaskEnabled.setVisibility(View.GONE);
        		llUserTasks.setVisibility(View.GONE);
        	}
        	//.
        	if (UserInfo.UserIsOnline) {
        		ivUser.setImageDrawable(getResources().getDrawable(R.drawable.onlineuser));
        		edUserConnectionState.setText(getString(R.string.SOnline));
        		edUserConnectionState.setTextColor(Color.GREEN);
        		btnUserLocation.setEnabled(true);
        	}
        	else {
        		ivUser.setImageDrawable(getResources().getDrawable(R.drawable.offlineuser));
        		edUserConnectionState.setText(getString(R.string.SOffline));
        		edUserConnectionState.setTextColor(Color.RED);
        		btnUserLocation.setEnabled(false);
        	}
        	//.
        	if ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0)) { 
        		btnUserCurrentActivity.setText(UserCurrentActivity.GetInfo(this,false));
        		btnUserCurrentActivityComponentList.setEnabled(true);
        	}
        	else {
        		btnUserCurrentActivity.setText(R.string.SNone1);
        		btnUserCurrentActivityComponentList.setEnabled(false);
        	}
    	}
    	finally {
    		flUpdate = false;
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }    

    private class TUserLocationGetting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private TGeoScopeServerUser.TUserDescriptor User;
    	private boolean flCloseAfterDone;
    	
        private ProgressDialog progressDialog; 
    	
    	public TUserLocationGetting(TGeoScopeServerUser.TUserDescriptor pUser, boolean pflCloseAfterDone) {
    		super();
    		//.
    		User = pUser;
    		flCloseAfterDone = pflCloseAfterDone;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				TGeoLocation GeoLocation;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				GeoLocation = _GetUserLocation(User, UserAgent.Server.User, Canceller);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE,GeoLocation).sendToTarget();
        	}
			catch (InterruptedException IE) {
			}
			catch (CancelException CE) {
			}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		            	//.
		    		    new AlertDialog.Builder(TUserPanel.this)
		    	        .setIcon(android.R.drawable.ic_dialog_alert)
		    	        .setTitle(R.string.SError)
		    	        .setMessage(TUserPanel.this.getString(R.string.SErrorOfGettingCurrentLocationOfUser)+User.UserName+" ("+User.UserFullName+")"+", "+E.getMessage())
		    		    .setPositiveButton(R.string.SOk, null)
		    		    .show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DONE:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TGeoLocation GeoLocation = (TGeoLocation)msg.obj;
		        		//.
		        		try {
		        			Intent intent = new Intent(TUserPanel.this, TReflector.class);
		        			intent.putExtra("Reason", TReflectorComponent.REASON_SHOWGEOLOCATION1);
		        			intent.putExtra("GeoLocation", GeoLocation.ToByteArray());
		        			TUserPanel.this.startActivity(intent);
		            		//.
		                    Toast.makeText(TUserPanel.this, TUserPanel.this.getString(R.string.SCurrentLocationOfUser)+User.UserName+" ("+User.UserFullName+")", Toast.LENGTH_SHORT).show();
			            	//.
			            	if (flCloseAfterDone)
			            		finish();
		        		} catch (Exception E1) {
		        			Toast.makeText(TUserPanel.this, E1.getMessage(),Toast.LENGTH_LONG).show();
		        		}
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserPanel.this);    
		            	progressDialog.setMessage(TUserPanel.this.getString(R.string.SGettingUserLocation));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
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
		                if ((!isFinishing()) && progressDialog.isShowing()) 
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
	
    private TGeoLocation _GetUserLocation(TGeoScopeServerUser.TUserDescriptor User, TGeoScopeServerUser MyUser, TCanceller Canceller) throws Exception {
    	TGeoScopeServerUser.TUserLocation UserLocation = MyUser.IncomingMessages_Command_GetUserLocation(User.UserID, TGeoScopeServerUser.TGetUserLocationCommandMessage.Version_ObtainFix, Integer.MAX_VALUE, Canceller);
		switch (UserLocation.Status) {
		
		case TGPSModule.GPSMODULESTATUS_AVAILABLE:
			break; //. >
			
		case TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE:
			throw new Exception(getString(R.string.SLocationIsPermanentlyUnavailable)); //. =>
			
		case TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE:
			throw new Exception(getString(R.string.SLocationIsTemporarilyUnavailable)); //. =>

		case TGPSModule.GPSMODULESTATUS_UNKNOWN:
			throw new Exception(getString(R.string.SLocationIsUnavailableForUnknownReason)); //. =>
		}
		if (!UserLocation.IsAvailable())
			throw new Exception(getString(R.string.SLocationIsNotAvailable)); //. =>
		if (UserLocation.IsNull())
			throw new Exception(getString(R.string.SLocationIsUnknown)); //. =>
		return (new TGeoLocation(UserLocation.Datum, UserLocation.Timestamp, UserLocation.Latitude,UserLocation.Longitude,UserLocation.Altitude)); 
    }
    
    private void User_GetLocation(TGeoScopeServerUser.TUserDescriptor User) {
    	new TUserLocationGetting(User,false);
    }    
    
    private void User_SetTaskEnabled(TGeoScopeServerUser.TUserDescriptor pUser, boolean _Value) {
    	final TGeoScopeServerUser.TUserDescriptor User = pUser;
    	final boolean Value = _Value;
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				UserAgent.Server.User.SetTaskEnabled(User.UserID, Value);
			}
			@Override
			public void DoOnCompleted() throws Exception {
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
    
    private void User_OpenMessaging(TGeoScopeServerUser.TUserDescriptor User) {
		TUserChatPanel UCP = TUserChatPanel.Panels.get(User.UserID);
		if (UCP != null)
			UCP.finish();
    	Intent intent = new Intent(this, TUserChatPanel.class);
    	intent.putExtra("UserID",User.UserID);
    	intent.putExtra("UserIsDisabled",User.UserIsDisabled);
    	intent.putExtra("UserIsOnline",User.UserIsOnline);
    	intent.putExtra("UserName",User.UserName);
    	intent.putExtra("UserFullName",User.UserFullName);
    	intent.putExtra("UserContactInfo",User.UserContactInfo);
    	startActivity(intent);
    }
    
    private void User_ShowCoGeoMonitorObjects(TGeoScopeServerUser.TUserDescriptor User) {
    	Intent intent = new Intent(this, TUserCoGeoMonitorObjectsPanel.class);
		intent.putExtra("UserID", User.UserID);
		intent.putExtra("ComponentID", Component.ID);
		if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.Objects != null) && (UserCoGeoMonitorObjects.Objects.OriginalData != null))
			intent.putExtra("Data", UserCoGeoMonitorObjects.Objects.OriginalData);
    	startActivity(intent);		
    }

    private void UserCoGeoMonitorObjects_StartUpdating() throws InterruptedException {
    	UserCoGeoMonitorObjects_StopUpdating();
    	//.
    	UserCoGeoMonitorObjects_Updating = new TAsyncProcessing() {
			
			private TUserCoGeoMonitorObjects _UserCoGeoMonitorObjects;
			
			@Override
			public void Process() throws Exception {
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				_UserCoGeoMonitorObjects = new TUserCoGeoMonitorObjects();
				//.
				_UserCoGeoMonitorObjects.Update(UserAgent, UserID, Canceller);
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				UserCoGeoMonitorObjects = _UserCoGeoMonitorObjects;
				//.
				UserCoGeoMonitorObjects_UpdateControls();
			}

			@Override
			public void DoOnFinished() throws Exception {
				pbCommunicationProgress.setVisibility(View.GONE);
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		UserCoGeoMonitorObjects_Updating.Start();
		//.
		pbCommunicationProgress.setVisibility(View.VISIBLE);
    }

    private void UserCoGeoMonitorObjects_StopUpdating() throws InterruptedException {
    	if (UserCoGeoMonitorObjects_Updating != null) {
    		UserCoGeoMonitorObjects_Updating.Destroy();
    		UserCoGeoMonitorObjects_Updating = null;
    	}
    }
    
    private void UserCoGeoMonitorObjects_UpdateControls() {
		btnUserCoGeoMonitorObjects.setEnabled(UserCoGeoMonitorObjects != null);
		if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.CommunicationObject != null)) {
			btnUserLiveMessaging.setText(R.string.SLiveChat);
			btnUserLiveMessaging.setEnabled(true);
			//.
			btnUserVideoPhone.setText(R.string.SVideoPhone);
			btnUserVideoPhone.setEnabled(true);
		}
		else {
			String S = getString(R.string.SLiveChat)+" "+"/"+getString(R.string.SOff)+"/";
			btnUserLiveMessaging.setText(S);
			btnUserLiveMessaging.setEnabled(false);
			//.
			S = getString(R.string.SVideoPhone)+" "+"/"+getString(R.string.SOff)+"/";
			btnUserVideoPhone.setText(S);
			btnUserVideoPhone.setEnabled(false);
		}
    }
}
