package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.TFileSystemFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.TTrackerPanel.TCurrentFixObtaining;
import com.geoscope.GeoEye.Space.Defines.TGeoLocation;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserDataFile;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserSession;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskIsOriginatedHandler;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TMyUserPanel extends Activity {

	private static final int REQUEST_SETUSERACTIVITY 	= 1;
	private static final int REQUEST_SHOWONREFLECTOR 	= 2;
	private static final int REQUEST_TEXTEDITOR			= 3;
	private static final int REQUEST_CAMERA 			= 4;
	private static final int REQUEST_VIDEOCAMERA		= 5;
	private static final int REQUEST_DRAWINGEDITOR		= 6;
	
	private static final int ACTIVITY_DATAFILE_TYPE_TEXT 	= 1;
	private static final int ACTIVITY_DATAFILE_TYPE_IMAGE 	= 2;
	private static final int ACTIVITY_DATAFILE_TYPE_VIDEO 	= 3;
	private static final int ACTIVITY_DATAFILE_TYPE_DRAWING = 4;
	private static final int ACTIVITY_DATAFILE_TYPE_FILE 	= 5;
	
	public static final int DataNameMaxSize = 100;
		
    private static TUserDescriptor 	UserInfo = null; 
    //.
    public static void ResetUserInfo() {
    	UserInfo = null;
    }
    //.
    private static TActivity UserCurrentActivity = null;
    //.
    public static boolean UserActivityIsAvailable() {
    	return (UserCurrentActivity != null);
    }
    //.
    public static void SetUserActivity(TActivity Activity) {
    	UserCurrentActivity = Activity;
    }
    //.
    public static TActivity GetUserActivity() {
    	return UserCurrentActivity;
    }
    //.
    public static void ResetUserCurrentActivity() {
    	UserCurrentActivity = null;
    }
    //.
    private static int UserDafaultActivityID = 0;
    //.
    public static boolean UserActivityIsDefault() {
    	return ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0) && (UserCurrentActivity.ID == UserDafaultActivityID));
    }
    
    public static void Reset() {
    	ResetUserInfo();
    	ResetUserCurrentActivity();
    }
    
    public boolean flExists = false;
    //.
	private EditText edUserName;
	private EditText edUserFullName;
	private EditText edUserContactInfo;
	private EditText edUserDomains;
	private CheckBox cbUserTaskEnabled;
	private EditText edUserConnectionState;
	private EditText edUserSessionState;
	private EditText edUserLocation;
	private Button btnGetUserLocation;
	private Button btnUserLocation;
	//.
	private Button btnUserCurrentActivity;
	private CheckBox cbDataName;
	private Button btnUserCurrentActivityAddDataFile;
	private Button btnUserCurrentActivityAddTextDataFile;
	private Button btnUserCurrentActivityAddImageDataFile;
	private Button btnUserCurrentActivityAddVideoDataFile;
	private Button btnUserCurrentActivityAddDrawingDataFile;
	private Button btnUserCurrentActivityAddFileDataFile;
	private Button btnUserCurrentActivityComponentList;
	private Button btnUserLastActivities;
	//.
	private LinearLayout 	llUserTasks;
	private Button 			btnOriginateUserTask;
	private Button			btnUserOriginateedTasks;
	private Button 			btnUserTasks;
    //.
    private boolean flVisible = false;
    //.
	private TReflectorComponent Component = null;
	//.
	private TUpdating Updating = null;
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	//.
	private Timer StatusUpdater = null;
	//.
	private TComponentServiceOperation ServiceOperation = null;
    //.
    private ProgressDialog progressDialog = null;
    //.
    private boolean flNamedData = false;
	
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
        setContentView(R.layout.myuser_panel);
        //.
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
					User_SetTaskEnabled(checked);
		    	}
		    	catch (Exception E) {
		            Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        edUserConnectionState = (EditText)findViewById(R.id.edUserConnectionState);
        edUserSessionState = (EditText)findViewById(R.id.edUserSessionState);
        edUserLocation = (EditText)findViewById(R.id.edUserLocation);
        btnGetUserLocation = (Button)findViewById(R.id.btnGetUserLocation);
        btnGetUserLocation.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
        			User_GetLocation();
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserLocation = (Button)findViewById(R.id.btnUserLocationTracker);
        btnUserLocation.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TMyUserPanel.this, TTrackerPanel.class);
				intent.putExtra("ComponentID", TMyUserPanel.this.Component.ID);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityPanel.class);
            	startActivityForResult(intent,REQUEST_SETUSERACTIVITY);
            }
        });
        cbDataName = (CheckBox)findViewById(R.id.cbDataName);
        cbDataName.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
                //.
                flNamedData = checked; 
            }
        });        
        btnUserCurrentActivityAddDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddDataFile);
        btnUserCurrentActivityAddDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		AddDataFile();
            }
        });
        btnUserCurrentActivityAddTextDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddTextDataFile);
        btnUserCurrentActivityAddTextDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_TEXT);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddImageDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddImageDataFile);
        btnUserCurrentActivityAddImageDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddVideoDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddVideoDataFile);
        btnUserCurrentActivityAddVideoDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddDrawingDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddDrawingDataFile);
        btnUserCurrentActivityAddDrawingDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_DRAWING);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddFileDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddFileDataFile);
        btnUserCurrentActivityAddFileDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_FILE);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityComponentList = (Button)findViewById(R.id.btnUserCurrentActivityComponentList);
        btnUserCurrentActivityComponentList.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityComponentListPanel.class);
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
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityListPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",UserInfo.UserID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        //.
        llUserTasks = (LinearLayout)findViewById(R.id.llUserTasks);
    	btnOriginateUserTask = (Button)findViewById(R.id.btnOriginateUserTask);
    	btnOriginateUserTask.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
    		    new AlertDialog.Builder(TMyUserPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_info)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SDoYouWantToOriginateANewTask)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	@Override
    		    	public void onClick(DialogInterface dialog, int id) {
    	        		try {
        		        	TTracker Tracker = TTracker.GetTracker();
        		        	if (Tracker == null)
        		        		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
        		    		boolean flActivityDataIsNotReady = ((Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.GetComponentFileStreamCount() > 0) || (Tracker.GeoLog.ComponentFileStreaming.GetItemsCount() > 0));
        		    		if (!flActivityDataIsNotReady)
        	            		Tasks_OriginateNewTask(UserCurrentActivity,TTaskDataValue.MODELUSER_TASK_PRIORITY_Normal);
        		    		else {
        		    		    new AlertDialog.Builder(TMyUserPanel.this)
        		    	        .setIcon(android.R.drawable.ic_dialog_alert)
        		    	        .setTitle(R.string.SConfirmation)
        		    	        .setMessage(R.string.SActivityDataTransmissionIsNotFinished)
        		    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
        		    		    	@Override
        		    		    	public void onClick(DialogInterface dialog, int id) {
        		    	        		try {
        	        	            		Tasks_OriginateNewTask(UserCurrentActivity,TTaskDataValue.MODELUSER_TASK_PRIORITY_Normal);
        		    					}
        		    					catch (Exception E) {
        		    	        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
        		    					}
        		    		    	}
        		    		    })
        		    		    .setNegativeButton(R.string.SNo, null)
        		    		    .show();
        		    		}
    					}
    					catch (Exception E) {
    	        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
    					}
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
            }
        });
    	btnUserOriginateedTasks = (Button)findViewById(R.id.btnUserOriginatedTasks);
    	btnUserOriginateedTasks.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		Intent intent = new Intent(TMyUserPanel.this, TMyUserTaskListPanel.class);
				intent.putExtra("ComponentID", TMyUserPanel.this.Component.ID);
            	intent.putExtra("flOriginator",true);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
    	btnUserTasks = (Button)findViewById(R.id.btnUserTasks);
    	btnUserTasks.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		Intent intent = new Intent(TMyUserPanel.this, TMyUserTaskListPanel.class);
				intent.putExtra("ComponentID", TMyUserPanel.this.Component.ID);
            	intent.putExtra("flOriginator",false);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        //.
        flExists = true;
        //.
        StatusUpdater = new Timer();
        StatusUpdater.schedule(new TUpdaterTask(this),100,1000);
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
        if (StatusUpdater != null) {
        	StatusUpdater.cancel();
        	StatusUpdater = null;
        }
		//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
		//.
    	ServiceOperation_Cancel();
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
    	//.
        flVisible = true;
        //.
		Update();
    	if ((UserInfo == null) || (UserCurrentActivity == null))
    		StartUpdating();
        //. start tracker position fixing immediately if it is in impulse mode
        TTracker Tracker = TTracker.GetTracker();
    	if ((Tracker != null) && (Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode) 
			Tracker.GeoLog.GPSModule.ProcessImmediately();
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	//.
    	flVisible = false;
    }    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SETUSERACTIVITY: 
        	if (resultCode == RESULT_OK) {
        		ResetUserCurrentActivity();
        	}
            break; //. >

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) 
        		finish();
            break; //. >

        case REQUEST_TEXTEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String POIText = extras.getString("Text");
                	try {
                    	if (flNamedData) 
                    		DataFileName_Dialog(DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_TEXT, POIText,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_TEXT, POIText,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case REQUEST_CAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = getImageTempFile(this);
				if (F.exists()) {
					try {
		            	if (flNamedData) 
		            		DataFileName_Dialog(DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE, F,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE, F,null);
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(this, S, Toast.LENGTH_LONG).show();  						
					}
				}
				else
        			Toast.makeText(this, R.string.SImageWasNotPrepared, Toast.LENGTH_SHORT).show();  
        	}  
            break; //. >

        case REQUEST_VIDEOCAMERA: 
        	if (resultCode == RESULT_OK) {  
            	try {
    				final File F = getVideoTempFile(this);
    				if (F.exists()) {
		            	if (flNamedData) 
		            		DataFileName_Dialog(DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO, F,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO, F,null);
    				}
    				else
            			Toast.makeText(this, R.string.SVideoWasNotPrepared, Toast.LENGTH_SHORT).show();  
				}
				catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
			}
            break; //. >

        case REQUEST_DRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String DrawingFileName = extras.getString("FileName");
                	try {
		            	if (flNamedData) 
		            		DataFileName_Dialog(DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_DRAWING, DrawingFileName,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_DRAWING, DrawingFileName,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void User_SetTaskEnabled(boolean _Value) {
    	final boolean Value = _Value;
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				UserAgent.Server.User.SetTaskEnabled(Value);
			}
			@Override
			public void DoOnCompleted() throws Exception {
				if (UserInfo != null)
					UserInfo.UserIsTaskEnabled = Value;
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
    
    private void User_GetLocation() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	//.
    	new TCurrentFixObtaining(this, Tracker.GeoLog.GPSModule, new TCurrentFixObtaining.TDoOnFixIsObtainedHandler() {
    		
    		@Override
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    			TGeoLocation GeoLocation = new TGeoLocation(TGPSModule.DatumID, Fix.TimeStamp, Fix.Latitude,Fix.Longitude,Fix.Altitude);
    			//.
        		try {
        			Intent intent = new Intent(TMyUserPanel.this, TReflector.class);
        			intent.putExtra("Reason", TReflectorComponent.REASON_SHOWGEOLOCATION1);
        			intent.putExtra("GeoLocation", GeoLocation.ToByteArray());
        			TMyUserPanel.this.startActivity(intent);
        		} catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(),Toast.LENGTH_LONG).show();
        		}
    		}
    	});
    }
    
    private void AddDataFile() {
		final CharSequence[] _items = new CharSequence[3];
		_items[0] = getString(R.string.SText); 
		_items[1] = getString(R.string.SImage); 
		_items[2] = getString(R.string.SVideo1); 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SCreateDataFile);
		builder.setNegativeButton(getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							switch (arg1) {
							case 0:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_TEXT);
								break; //. >

							case 1:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE);
								break; //. >

							case 2:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO);
								break; //. >
							}
							arg0.dismiss();
						} catch (Exception E) {
							Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    private void AddDataFile(final int DataFileType) throws IOException {
    	DoAddDataFile(DataFileType);
    }
    
    private void DoAddDataFile(int DataFileType) throws IOException {
		if (!TTracker.TrackerIsEnabled()) 
			throw new IOException(getString(R.string.STrackerIsNotActive)); //. =>
		switch (DataFileType) {
		
		case ACTIVITY_DATAFILE_TYPE_TEXT:
    		Intent intent = new Intent(TMyUserPanel.this, TTrackerPOITextPanel.class);
            startActivityForResult(intent,REQUEST_TEXTEDITOR);
			break; //. >
			
		case ACTIVITY_DATAFILE_TYPE_IMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TMyUserPanel.this.getImageTempFile(TMyUserPanel.this))); 
  		    startActivityForResult(intent, REQUEST_CAMERA);    		
			break; //. >
			
		case ACTIVITY_DATAFILE_TYPE_VIDEO:
  		    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TMyUserPanel.this.getVideoTempFile(TMyUserPanel.this))); 
  		    startActivityForResult(intent, REQUEST_VIDEOCAMERA);    		
			break; //. >

		case ACTIVITY_DATAFILE_TYPE_DRAWING:
    		intent = new Intent(TMyUserPanel.this, TDrawingEditor.class);
    		File F = getDrawingTempFile(this);
    		F.delete();
  		    intent.putExtra("FileName", F.getAbsolutePath()); 
  		    intent.putExtra("ReadOnly", false); 
  	    	intent.putExtra("SpaceContainersAvailable", true); 
  		    startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
			break; //. >
			
		case ACTIVITY_DATAFILE_TYPE_FILE:
	    	TFileSystemFileSelector FileSelector = new TFileSystemFileSelector(this)
	        .setFilter(".*")
	        .setOpenDialogListener(new TFileSystemFileSelector.OpenDialogListener() {

	        	@Override
	            public void OnSelectedFile(String fileName) {
	        		final String SelectedFileName = fileName; 
                    //.
					try {
		            	if (flNamedData) 
		            		DataFileName_Dialog(DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_FILE, SelectedFileName,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(ACTIVITY_DATAFILE_TYPE_FILE, SelectedFileName,null);
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(TMyUserPanel.this, S, Toast.LENGTH_SHORT).show();  						
					}
	            }

				@Override
				public void OnCancel() {
				}
	        });
	    	FileSelector.show();    	
			break; //. >
		}
    }
    
    private void EnqueueDataFile(int DataFileType, Object Data, String DataName) throws Exception {
    	if ((DataName != null) && (DataName.length() > 0))
    		DataName = "@"+TComponentFileStreaming.EncodeFileNameString(DataName);
    	else
    		DataName = "";
    	//.
		switch (DataFileType) {
		
		case ACTIVITY_DATAFILE_TYPE_TEXT: {
			String POIText = (String)Data;
    		if (POIText.equals(""))
    			throw new Exception(getString(R.string.STextIsNull)); //. =>
    		//.
    		byte[] TextBA = POIText.getBytes("windows-1251");
    		//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+".txt";
    		File NF = new File(NFN);
    		FileOutputStream FOS = new FileOutputStream(NF);
    		try {
    			FOS.write(TextBA);
    		}
    		finally {
    			FOS.close();
    		}
    		//. prepare and send datafile
    		final String 	DataFileName = NFN;
    		final int 		DataFileSize = TextBA.length;
    		/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
    			}
    			@Override 
    			public void DoOnCompleted() throws Exception {
            		Toast.makeText(TMyUserPanel.this, getString(R.string.STextIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
    			}
    			@Override
    			public void DoOnException(Exception E) {
    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			}
    		};
    		Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.SendViaDevice(Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.STextIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case ACTIVITY_DATAFILE_TYPE_IMAGE: {
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
			//.
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	//.
			File F = (File)Data;
			FileInputStream fs = new FileInputStream(F);
			try
			{
				byte[] PictureBA;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither=false;
				options.inPurgeable=true;
				options.inInputShareable=true;
				options.inTempStorage=new byte[1024*1024*3]; 							
				Rect rect = new Rect();
				Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), rect, options);
				try {
					int ImageMaxSize = options.outWidth;
					if (options.outHeight > ImageMaxSize)
						ImageMaxSize = options.outHeight;
					float MaxSize = Tracker.GeoLog.GPSModule.MapPOIConfiguration.Image_ResX;
					float Scale = MaxSize/ImageMaxSize; 
					Matrix matrix = new Matrix();     
					matrix.postScale(Scale,Scale);
					//.
					Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						try {
							if (!resizedBitmap.compress(CompressFormat.JPEG, Tracker.GeoLog.GPSModule.MapPOIConfiguration.Image_Quality, bos)) 
								throw new Exception(getString(R.string.SErrorOfSavingJPEG)); //. =>
							PictureBA = bos.toByteArray();
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
				//.
            	double Timestamp = OleDate.UTCCurrentTimestamp();
        		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+".jpg";
        		File NF = new File(NFN);
        		FileOutputStream FOS = new FileOutputStream(NF);
        		try {
        			FOS.write(PictureBA);
        		}
        		finally {
        			FOS.close();
        		}
        		//. prepare and send datafile
        		final String 	DataFileName = NFN;
        		final int 		DataFileSize = PictureBA.length;
	    		/* TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
	    			@Override
	    			public void Process() throws Exception {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				    	TTracker Tracker = TTracker.GetTracker();
				    	if (Tracker == null)
				    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
				    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
				    	DataFile.SendViaDevice(Tracker.GeoLog);
	    			}
	    			@Override 
	    			public void DoOnCompleted() throws Exception {
			        	Toast.makeText(TMyUserPanel.this, getString(R.string.SImageIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
	    			}
	    			@Override
	    			public void DoOnException(Exception E) {
	    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    			}
	    		};
	    		Processing.Start();*/
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    	Tracker = TTracker.GetTracker();
		    	if (Tracker == null)
		    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
		    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
		    	DataFile.SendViaDevice(Tracker.GeoLog);
		    	//.
	        	Toast.makeText(this, getString(R.string.SImageIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
			}
			finally
			{
				fs.close();
			}
			break; //. >
		}
			
		case ACTIVITY_DATAFILE_TYPE_VIDEO: {
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
			//.
			File F = (File)Data;
			//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(F.getName());
    		File NF = new File(NFN);
    		F.renameTo(NF);
    		String FileName = NFN;
    		//. prepare and send datafile
    		final String 	DataFileName = FileName;
    		final long 		DataFileSize;
    		F = new File(FileName);
    		if (F.exists())
    			DataFileSize = F.length();
    		else
    			DataFileSize = 0;
    		/* TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
    			}
    			@Override 
    			public void DoOnCompleted() throws Exception {
            		Toast.makeText(TMyUserPanel.this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataFileSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
    			}
    			@Override
    			public void DoOnException(Exception E) {
    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			}
    		};
    		Processing.Start(); */
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.SendViaDevice(Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataFileSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
			break; //. >
		}

		case ACTIVITY_DATAFILE_TYPE_DRAWING: {
			String DrawingFileName = (String)Data;
			//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TDrawingDefines.FileExtension;
    		File NF = new File(NFN);
    		if (!(new File(DrawingFileName)).renameTo(NF))
    			throw new IOException("could not rename file: "+DrawingFileName); //. =>
    		//. prepare and send datafile
    		final String 	DataFileName = NFN;
    		final long		DataFileSize = NF.length();
    		/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
    			}
    			@Override 
    			public void DoOnCompleted() throws Exception {
            		Toast.makeText(TMyUserPanel.this, getString(R.string.SDrawingIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
    			}
    			@Override
    			public void DoOnException(Exception E) {
    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			}
    		};
    		Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.SendViaDevice(Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.SDrawingIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case ACTIVITY_DATAFILE_TYPE_FILE: {
			String FileName = (String)Data;
			//.
	    	double Timestamp = OleDate.UTCCurrentTimestamp();
			String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(FileName);
			File NF = new File(NFN);
			TFileSystem.CopyFile(new File(FileName), NF);
			//. prepare and send datafile
			final String 	DataFileName = NFN;
			final long		DataFileSize = NF.length();
			/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
				@Override
				public void Process() throws Exception {
					TUserAgent UserAgent = TUserAgent.GetUserAgent();
					if (UserAgent == null)
						throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
				}
				@Override 
				public void DoOnCompleted() throws Exception {
	        		Toast.makeText(TMyUserPanel.this, getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
				}
				@Override
				public void DoOnException(Exception E) {
					Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			};
			Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.SendViaDevice(Tracker.GeoLog);
	    	//.
			Toast.makeText(this, getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
		}
    }
    
    protected File getImageTempFile(Context context) {
  	  	return new File(TGeoLogApplication.GetTempFolder(),"Image.jpg");
    }
  
    protected File getVideoTempFile(Context context) {
    	return new File(TGeoLogApplication.GetTempFolder(),"Video.3gp");
    }

    protected File getDrawingTempFile(Context context) {
    	return new File(TGeoLogApplication.GetTempFolder(),"Drawing"+"."+TDrawingDefines.FileExtension);
    }

    private static class TOnDataFileNameHandler {
    	
    	public void DoOnDataFileNameHandler(String Name) throws Exception {
    	}
    }
    
    private void DataFileName_Dialog(final int DataNameMaxSize, final TOnDataFileNameHandler OnDataFileNameHandler) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		// .
		alert.setTitle(R.string.SDataName);
		alert.setMessage(R.string.SEnterName);
		// .
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);
		// .
		alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						//. hide keyboard
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						//.
						try {
							String Name = input.getText().toString();
            				if (Name.length() > DataNameMaxSize)
            					Name = Name.substring(0,DataNameMaxSize);
            				//.
							OnDataFileNameHandler.DoOnDataFileNameHandler(Name);
						} catch (Exception E) {
							Toast.makeText(TMyUserPanel.this, E.getMessage(),	Toast.LENGTH_LONG).show();
						}
					}
				});
		// .
		alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// . hide keyboard
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				});
		// .
		alert.show();
    }
    
	private void ServiceOperation_Cancel() {
		if (ServiceOperation != null) {
			ServiceOperation.Cancel();
			ServiceOperation = null;
		}
	}
	
    private void Tasks_OriginateNewTask(TActivity Activity, int TaskPriority) throws Exception {
    	int TaskType = 2; 		//. MODELUSER_TASK_TYPE_UserTask
    	int TaskService = 0; 	//. MODELUSER_TASK_TYPE_UserTask_SERVICE_Any
    	//.
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	String Comment;
    	if (Activity.Info != null)
    		Comment = Activity.Name+" ("+Activity.Info+")";
    	else
    		Comment = Activity.Name;
    	@SuppressWarnings("unused")
		TComponentServiceOperation SO = Tracker.GeoLog.TaskModule.OriginateNewTask(Tracker.GeoLog.UserID, Activity.ID, TaskPriority, TaskType, TaskService, Comment, new TTaskIsOriginatedHandler() {
    		@Override
    		public void DoOnTaskIsOriginated(int idTask) {
    			Tasks_DoOnTaskIsOriginated(idTask);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Tasks_DoOnException(E);  						
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Tasks_DoOnTaskIsOriginated(int idTask) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
    	MessageHandler.obtainMessage(MESSAGE_ONTASKISORIGINATED,idTask).sendToTarget();
    }
    
    private void Tasks_DoOnException(Exception E) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private void Tasks_OpenTaskPanel(int TaskID) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskData(Tracker.GeoLog.UserID, TaskID, new TTaskDataValue.TTaskDataIsReceivedHandler() {
    		@Override
    		public void DoOnTaskDataIsReceived(byte[] TaskData) {
    			Task_OnDataIsReceivedForOpening(TaskData);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_OnDataIsReceivedForOpening(byte[] TaskData) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_OPENTASKPANEL,TaskData).sendToTarget();
    }
        
    private void Task_DoOnException(Exception E){
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_STARTED = 0;
    	private static final int MESSAGE_COMPLETED = 1;
    	private static final int MESSAGE_FINISHED = 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 5;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TUserDescriptor UserInfo = null; 
        private TActivity 		UserCurrentActivity = null;
        private int				UserDafaultActivityID = 0;
        
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
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
					MessageHandler.obtainMessage(MESSAGE_STARTED).sendToTarget();
					//.
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				UserInfo = UserAgent.Server.User.GetUserInfo();
	    				//.
	    				UserCurrentActivity = UserAgent.Server.User.GetUserCurrentActivity();
	    				UserDafaultActivityID = UserAgent.Server.User.GetUserDefaultActivityID();
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
		            	if (TMyUserPanel.UserCurrentActivity == null)
		            		TMyUserPanel.this.btnUserCurrentActivity.setText("?");
		            	//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_STARTED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	if (TMyUserPanel.UserCurrentActivity == null)
		            		TMyUserPanel.this.btnUserCurrentActivity.setText(R.string.SUpdating);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	TMyUserPanel.UserInfo = UserInfo;
		            	//.
		            	if ((TMyUserPanel.UserCurrentActivity == null) || (!TMyUserPanel.UserCurrentActivity.IsUnknown()))
		            		TMyUserPanel.UserCurrentActivity = UserCurrentActivity;
		            	//.
		            	TMyUserPanel.UserDafaultActivityID = UserDafaultActivityID;
	           		 	//.
	           		 	TMyUserPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TMyUserPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TMyUserPanel.this);    
		            	progressDialog.setMessage(TMyUserPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
				            	if (TMyUserPanel.UserCurrentActivity == null)
				            		TMyUserPanel.this.btnUserCurrentActivity.setText("?");
								//.
								if (flClosePanelOnCancel)
									TMyUserPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TMyUserPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
				            	if (TMyUserPanel.UserCurrentActivity == null)
				            		TMyUserPanel.this.btnUserCurrentActivity.setText("?");
								//.
								if (flClosePanelOnCancel)
									TMyUserPanel.this.finish();
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
    		TReflectorComponent _Reflector = Component;
    		//.
    		btnUserLocation.setEnabled(!((_Reflector != null) && (_Reflector.Configuration.GeoLog_flHide)));
    		//.
        	if (UserInfo != null) {
        		edUserName.setText(UserInfo.UserName);
        		edUserFullName.setText(UserInfo.UserFullName);
        		edUserContactInfo.setText(UserInfo.UserContactInfo);
        		edUserDomains.setText(UserInfo.UserDomains);
        		cbUserTaskEnabled.setVisibility(UserInfo.UserDomainsAreSpecified() ? View.VISIBLE : View.GONE);
        		cbUserTaskEnabled.setChecked(UserInfo.UserIsTaskEnabled);
        		boolean flTMSOption = UserInfo.UserDomainsAreSpecified();
        		if (flTMSOption) {
            		if (_Reflector != null)
            			flTMSOption = _Reflector.Configuration.ReflectionWindow_flTMSOption;
        		}
        		llUserTasks.setVisibility(flTMSOption ? View.VISIBLE : View.GONE);
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
        	if (UserCurrentActivity != null) { 
        		if (!UserCurrentActivity.IsNone()) {
            		btnUserCurrentActivity.setText(UserCurrentActivity.GetInfo(this,false));
            		btnUserCurrentActivityComponentList.setEnabled(true);
            		//.
            		btnOriginateUserTask.setEnabled(UserCurrentActivity.ID != UserDafaultActivityID);
        		}
        		else {  
        			btnUserCurrentActivity.setText(R.string.SNone1);
            		btnUserCurrentActivityComponentList.setEnabled(false);
            		btnOriginateUserTask.setEnabled(false);
        		}
        	}
        	else {
        		btnUserCurrentActivity.setText("?");
        		btnUserCurrentActivityComponentList.setEnabled(false);
        		btnOriginateUserTask.setEnabled(false);
        	}
    	}
    	finally {
    		flUpdate = false;
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
    }
    
	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
	private static final int MESSAGE_UPDATESTATUS 			= 4;
	private static final int MESSAGE_ONTASKISORIGINATED 	= 5;
	private static final int MESSAGE_OPENTASKPANEL 			= 6;
	
    private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_EXCEPTION:
    				if (!flExists)
    	            	break; //. >
                	Exception E = (Exception)msg.obj;
                    Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	//.
                	break; //. >
                	
                case MESSAGE_UPDATESTATUS:
                	if (flVisible)
                		UpdateStatus();  
                	break; //. >
                	
                case MESSAGE_ONTASKISORIGINATED:
                	if (!flExists)
                		break; //. >
                	int idTask = (Integer)msg.obj;
                	//.
                	TMyUserPanel.ResetUserCurrentActivity();
                	//.
            		StartUpdating();
            		//.
            		Toast.makeText(TMyUserPanel.this, getString(R.string.SANewTaskHasBeenOriginated)+Integer.toString(idTask), Toast.LENGTH_LONG).show();
            		//.
            		try {
    					Tasks_OpenTaskPanel(idTask);
    				} catch (Exception Ex) {
    					Tasks_DoOnException(Ex);
    				}
                	break; //. >

                case MESSAGE_OPENTASKPANEL:
                	if (!flExists)
                		break; //. >
                	byte[] TaskData = (byte[])msg.obj;
                	//.
    				try {
    			    	TTracker Tracker = TTracker.GetTracker();
    			    	if (Tracker == null)
    			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	            	Intent intent = new Intent(TMyUserPanel.this, TUserTaskPanel.class);
    					intent.putExtra("ComponentID", TMyUserPanel.this.Component.ID);
    	            	intent.putExtra("UserID",(long)Tracker.GeoLog.UserID);
    	            	intent.putExtra("flOriginator",true);
    	            	intent.putExtra("TaskData",TaskData);
    	            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
    				}
    				catch (Exception Ex) {
    					Toast.makeText(TMyUserPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
    					finish();
    				}
                	break; //. >

                case MESSAGE_PROGRESSBAR_SHOW:
                	progressDialog = new ProgressDialog(TMyUserPanel.this);    
                	progressDialog.setMessage(TMyUserPanel.this.getString(R.string.SLoading));    
                	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
                	progressDialog.setIndeterminate(true); 
                	progressDialog.setCancelable(true);
                	progressDialog.setOnCancelListener( new OnCancelListener() {
            			@Override
            			public void onCancel(DialogInterface arg0) {
            				TMyUserPanel.this.finish();
            			}
            		});
                	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TMyUserPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
                		@Override 
                		public void onClick(DialogInterface dialog, int which) { 
            				TMyUserPanel.this.finish();
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

    private class TUpdaterTask extends TimerTask
    {
        private TMyUserPanel Panel;
        
        public TUpdaterTask(TMyUserPanel pPanel)
        {
            Panel = pPanel;
        }
        
        public void run() {
        	try {
            	Panel.MessageHandler.obtainMessage(TMyUserPanel.MESSAGE_UPDATESTATUS).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }
    
    private void UpdateStatus() {
    	if (UserInfo != null) {
        	if (UserInfo.UserIsOnline) {
        		String S = getString(R.string.SOnline);
            	if (TServerConnection.flSecureConnection)
            		S += "("+getString(R.string.SSecure)+")";
        		edUserConnectionState.setText(S);
        		edUserConnectionState.setTextColor(Color.GREEN);
        	}
        	else {
        		edUserConnectionState.setText(getString(R.string.SOffline));
        		edUserConnectionState.setTextColor(Color.RED);
        	}
    	}
    	else {
    		edUserConnectionState.setText("?");
    		edUserConnectionState.setTextColor(Color.GRAY);
    	}
    	//.
		TUserAgent UserAgent = TUserAgent.GetUserAgent();
		if (UserAgent != null) {
			TGeoScopeServerUser User = UserAgent.User();
			if (User.InSession()) {
				edUserSessionState.setText(R.string.SSessionIsActive);
				edUserSessionState.setTextColor(Color.GREEN);
			}
			else {
				edUserSessionState.setText(R.string.SSessionIsNone);
				edUserSessionState.setTextColor(Color.GRAY);
				//.
				TGeoScopeServerUserSession Session = User.GetSession();
				if (Session != null)
					Session.Reconnect();
			}
		}
		else {
			edUserSessionState.setText(R.string.SSessionIsNone);
			edUserSessionState.setTextColor(Color.GRAY);
		}
    	//.
    	TTracker Tracker = TTracker.GetTracker(); 
    	if ((Tracker != null) && Tracker.GeoLog.IsEnabled()) {
            //. GPS module info
            if (Tracker.GeoLog.GPSModule.flProcessing) {
                btnGetUserLocation.setEnabled(true);
                if (Tracker.GeoLog.GPSModule.flGPSFixing) {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    String S;
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecsAgo);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecsAgo);
                    //.
                    edUserLocation.setText(getString(R.string.SAvailable1)+", "+S);
                    edUserLocation.setTextColor(Color.GREEN);
                }
                else {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    String S;
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecs);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecs);
                    //.
                    edUserLocation.setText(getString(R.string.SUnknown)+", "+S);
                    edUserLocation.setTextColor(Color.RED);
                }
            }
            else {
                btnGetUserLocation.setEnabled(false);
            	edUserLocation.setText("?");
                edUserLocation.setTextColor(Color.RED);
            }
    	}
    	else {
        	edUserLocation.setText(getString(R.string.SDisabled1));
        	edUserLocation.setTextColor(Color.GRAY);
    	}
    }
}
