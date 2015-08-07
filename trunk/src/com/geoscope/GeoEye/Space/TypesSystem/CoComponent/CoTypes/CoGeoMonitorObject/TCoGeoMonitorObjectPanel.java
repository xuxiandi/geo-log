package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.Color.ColorPicker;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentDescriptor;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModelHistoryPanel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectDeviceSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.BusinessModels.TEnforaMT3000TrackerBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.TEnforaObjectDeviceSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.TEnforaObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.BusinessModels.TEnforaObjectTrackerBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.TGeoMonitoredObjectDeviceSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.TGeoMonitoredObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.BusinessModels.TGMOTrackLogger1BusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent.TAlarmModule;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent.TAlarmModule.TAlarms;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels.TGMO1GeoLogAndroidBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerArchive;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerVideoPhoneCallPanel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerViewer;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.Space.URL.TURL;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioFileMessageValue;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioFilesValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetDataStreamerActiveValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TCoGeoMonitorObjectPanel extends Activity {

	private static final int DefaultUpdatingInterval = 1000*10; //. seconds
	
	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;
	
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private int UpdateInterval;
    	private boolean flShowProgress;
    	private boolean flClosePanelOnCancel = false;
    	//.
    	private TAutoResetEvent UpdateSignal = new TAutoResetEvent();
    	//.
    	private byte[] 			ObjectData = null;
    	private TObjectModel	ObjectModel = null;
    	
    	private TCanceller ProcessingCanceller = new TCanceller();
        private ProgressDialog progressDialog; 
    	
    	public TUpdating(int pUpdateInterval) {
    		super();
    		//.
    		UpdateInterval = pUpdateInterval;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

    	public void Destroy(boolean flWaitForTermination) throws InterruptedException {
			Cancel();
			//.
			StartUpdate();
			//.
			if (flWaitForTermination)
				Wait();
    	}
    	
		@Override
		public void run() {
			try {
				try {
					int UpdateCount = 0;
					while (!Canceller.flCancel) {
						//. wait for an update signal
						boolean flStartedByUser = UpdateSignal.WaitOne(UpdateInterval);
						Canceller.Check();
						//.
						try {
							flShowProgress = flStartedByUser; 
							flClosePanelOnCancel = (UpdateCount == 0);
							//.
							ProcessingCanceller.Reset();
			    			try {
								if (flShowProgress)
									MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
								//.
			    				ObjectData = TCoGeoMonitorObjectPanel.this.Object.GetData(0);
			    				//.
			    				Canceller.Check();
			    				ProcessingCanceller.Check();
			    				//.
			    				byte[] ObjectModelData = TCoGeoMonitorObjectPanel.this.Object.GetData(1000001);
			    				if (ObjectModelData != null) {
				    				Canceller.Check();
				    				ProcessingCanceller.Check();
				    				//.
			    					int Idx = 0;
			    					int ObjectModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
			    					int BusinessModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
			    					//.
			    					if (ObjectModelID != 0) {
			    						ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
			    						if (ObjectModel != null) {
			    							ObjectModel.SetBusinessModel(BusinessModelID);
			    							//.
			    							TGeographServerObjectController GSOC = TCoGeoMonitorObjectPanel.this.Object.GeographServerObjectController();
			    							synchronized (GSOC) {
			    								boolean flKeepConnectionLast = GSOC.KeepConnection();
			    								try {
				    								GSOC.Connect();
				    								try {
				    				    				Canceller.Check();
				    				    				ProcessingCanceller.Check();
				    				    				//.
				    									byte[] ObjectSchemaData = GSOC.Component_ReadAllCUAC(new int[] {1}/*object side*/);
				    									//.
				    				    				Canceller.Check();
				    				    				ProcessingCanceller.Check();
				    				    				//.
				    									if (ObjectSchemaData != null)
				    										ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
				    									//.
				    									byte[] ObjectDeviceSchemaData = GSOC.Component_ReadAllCUAC(new int[] {2/*device side*/});
				    									//.
				    				    				Canceller.Check();
				    				    				ProcessingCanceller.Check();
				    				    				//.
				    									if (ObjectDeviceSchemaData != null)
				    										ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
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
			    				}
							}
							finally {
								if (flShowProgress)
									MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
							}
		    				//.
			    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
			    			UpdateCount++;
						}
			        	catch (InterruptedException E) {
			        		return; //. ->
			        	}
						catch (CancelException CE) {
							if (CE.Canceller != ProcessingCanceller)
								throw CE; //. =>
						}
			        	catch (IOException E) {
			    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
			        	}
			        	catch (Throwable E) {
			    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
			        	}
					}
	        	}
	        	catch (InterruptedException E) {
	        	}
				catch (CancelException CE) {
				}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		public void StartUpdate() {
			ProcessingCanceller.Cancel();
			//.
			UpdateSignal.Set();
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
		                Toast.makeText(TCoGeoMonitorObjectPanel.this, TCoGeoMonitorObjectPanel.this.getString(R.string.SErrorOfLoadingObjectData)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TCoGeoMonitorObjectPanel.this.ObjectData = ObjectData;
		            	//.
	    				if (TCoGeoMonitorObjectPanel.this.ObjectModel != null) {
	    					TCoGeoMonitorObjectPanel.this.ObjectModel.Destroy();
	    					TCoGeoMonitorObjectPanel.this.ObjectModel = null;
	    				}
		            	TCoGeoMonitorObjectPanel.this.ObjectModel = ObjectModel;
		            	//.
		            	TCoGeoMonitorObjectPanel.this._Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TCoGeoMonitorObjectPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TCoGeoMonitorObjectPanel.this);    
		            	progressDialog.setMessage(TCoGeoMonitorObjectPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								ProcessingCanceller.Cancel();
								//.
								if (flClosePanelOnCancel)
									TCoGeoMonitorObjectPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TCoGeoMonitorObjectPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
		            			ProcessingCanceller.Cancel();
								//.
								if (flClosePanelOnCancel)
									TCoGeoMonitorObjectPanel.this.finish();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} 
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
    
	public boolean flExists = false;
	//.
	private TGeoScopeServer Server = null;
	//.
	private TReflectorComponent Component = null;
	//.
	private int ParametersType;
	//.
	private long				ObjectID = -1;
	private int					ObjectIndex = -1;
	private TCoGeoMonitorObject Object = null;
	private byte[] 				ObjectData = null;
	private TObjectModel		ObjectModel = null;
	
	private TUpdating 			Updating = null;

	public boolean 	flVisible = false;
	
	private ScrollView svRoot;
	//.
	private EditText edGMOName;
	private EditText edGMOConnectionState;
	private EditText edGMOLocationState;
	private EditText edGMOAlertState;
	private Button btnGMOUpdateInfo;
	private Button btnGMOShowPosition;
	private Button btnGMOShowUser;
	private Button btnGMOShowHistory;
	private Button btnGMOAddTrack;
	private Button btnGMODataStream;
	//.
	private int AddTrack_Date_Year;
	private int AddTrack_Date_Month;
	private int AddTrack_Date_Day;
	private int AddTrack_Color;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
		try {
			TUserAgent UserAgent = TUserAgent.GetUserAgent(this.getApplicationContext());
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			Server = UserAgent.Server;
			//.
	        Bundle extras = getIntent().getExtras(); 
	        if (extras != null) {
				int ComponentID = extras.getInt("ComponentID");
				Component = TReflectorComponent.GetComponent(ComponentID);
				//.
            	ParametersType = extras.getInt("ParametersType");
            	switch (ParametersType) {
            	
            	case PARAMETERS_TYPE_OID:
                	ObjectID = extras.getLong("ObjectID");
                	Object = new TCoGeoMonitorObject(Server, ObjectID);
                	Object.Name = extras.getString("ObjectName");
            		break; //. >
            		
            	case PARAMETERS_TYPE_OIDX:
            		if ((Component == null) || (Component.CoGeoMonitorObjects == null)) {
            			finish();
            			return; //. ->
            		}
            		//.
                	ObjectIndex = extras.getInt("ObjectIndex");
    	        	Object = Component.CoGeoMonitorObjects.Items[ObjectIndex]; 
            		break; //. >
            	}
	        }
			//.
			requestWindowFeature(Window.FEATURE_NO_TITLE);
	        //.
	        setContentView(R.layout.reflector_gmo_panel);
	        //.
	        svRoot = (ScrollView)findViewById(R.id.svRoot);
	        //.
	        edGMOName = (EditText)findViewById(R.id.edGMOName);
	        edGMOName.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
	            	try {
	            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
	            		com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel.TURL(Object.ID,Object.Name);
	            		if (Object != null)
	            			URL.Name = Object.LabelText();
	            		URL.ConstructURLFile(URLFN);
	            		//.
		    		    new AlertDialog.Builder(TCoGeoMonitorObjectPanel.this)
		    	        .setIcon(android.R.drawable.ic_dialog_alert)
		    	        .setTitle(R.string.SInfo)
		    	        .setMessage(TCoGeoMonitorObjectPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TCoGeoMonitorObjectPanel.this.getString(R.string.SUseItForImport))
		    		    .setPositiveButton(R.string.SOk, null)
		    		    .show();
	            	}
	            	catch (Exception E) {
	            		Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	}
					return true;
				}
			});
	        edGMOConnectionState = (EditText)findViewById(R.id.edGMOConnectionState);
	        edGMOLocationState = (EditText)findViewById(R.id.edGMOLocationState);
	        edGMOAlertState = (EditText)findViewById(R.id.edGMOAlertState);
	        //.
	        btnGMOUpdateInfo = (Button)findViewById(R.id.btnGMOUpdateInfo);
	        btnGMOUpdateInfo.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	            	Updating.StartUpdate();
	            }
	        });
	        //.
	        btnGMOShowPosition = (Button)findViewById(R.id.btnGMOShowPosition);
	        btnGMOShowPosition.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	            	ShowCurrentLocation();
	            }
	        });
	        btnGMOShowPosition.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
	            	try {
	            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
	            		com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.Location.TURL URL = new com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.Location.TURL(Object.ID);
	            		if (Object != null)
	            			URL.Name = getString(R.string.SLocation)+Object.LabelText();
	            		URL.ConstructURLFile(URLFN);
	            		//.
		    		    new AlertDialog.Builder(TCoGeoMonitorObjectPanel.this)
		    	        .setIcon(android.R.drawable.ic_dialog_alert)
		    	        .setTitle(R.string.SInfo)
		    	        .setMessage(TCoGeoMonitorObjectPanel.this.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+TCoGeoMonitorObjectPanel.this.getString(R.string.SUseItForImport))
		    		    .setPositiveButton(R.string.SOk, null)
		    		    .show();
	            	}
	            	catch (Exception E) {
	            		Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	}
					return true;
				}
			});
	        //.
	        btnGMOShowUser = (Button)findViewById(R.id.btnGMOShowUser);
	        btnGMOShowUser.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	        		ShowUser();
	            }
	        });
	        //.
	        btnGMOShowHistory = (Button)findViewById(R.id.btnGMOShowHistory);
	        btnGMOShowHistory.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	        		ShowHistory();
	            }
	        });
	        //.
	        btnGMOAddTrack = (Button)findViewById(R.id.btnGMOAddTrack);
	        btnGMOAddTrack.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	            	AddTrack();
	            }
	        });
	        //.
	        btnGMODataStream = (Button)findViewById(R.id.btnGMODataStream);
	        btnGMODataStream.setOnClickListener(new OnClickListener() {
	        	@Override
	            public void onClick(View v) {
	            	try {
						ShowDataStream();
	            	}
	            	catch (Exception E) {
	            		Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	}
	            }
	        });
	        //.
	        flExists = true;
	        //.
	        int UpdateInterval = DefaultUpdatingInterval;
	        if ((Component != null) && (Component.CoGeoMonitorObjects != null))
	        	UpdateInterval = Component.CoGeoMonitorObjects.GetUpdateInterval()*1000;
    		Updating = new TUpdating(UpdateInterval);
    		//.
    		_Update();
        	Update(); //. start updating
    	}
    	catch (Exception E) {
    		Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		finish();
    	}
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		if (ObjectModel != null) {
			ObjectModel.Destroy();
			ObjectModel = null;
		}
		if (Updating != null) {
			try {
				Updating.Destroy(false);
			} catch (InterruptedException IE) {
			}
			Updating = null;
		}
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		flVisible = true;
	}

	public void onPause() {
	    flVisible = false;
		super.onPause();
	}
	
	private void Update() {
		Updating.StartUpdate();
	}
	
	private void _Update() {
		try {
			edGMOName.setText(Object.LabelText());
			if (ObjectData != null) {
				boolean IsOnline = (ObjectData[0] > 0);
				boolean FixIsAvailable = (ObjectData[1] > 0);
				int UserAlert = TDataConverter.ConvertLEByteArrayToInt32(ObjectData,2);
				//.
				if (IsOnline) {
					edGMOConnectionState.setText(R.string.SOnline);
					edGMOConnectionState.setTextColor(Color.GREEN);
				}
				else {
					edGMOConnectionState.setText(R.string.SOffline);
					edGMOConnectionState.setTextColor(Color.RED);
				}
				if (FixIsAvailable) {
					edGMOLocationState.setText(R.string.SAvailable);
					edGMOLocationState.setTextColor(Color.GREEN);
				}
				else {
					edGMOLocationState.setText(R.string.SNotAvailable);
					edGMOLocationState.setTextColor(Color.RED);
				}
				switch (UserAlert) {

				case 0: 
					edGMOAlertState.setText(R.string.SNone);
					edGMOAlertState.setTextColor(Color.GREEN);
					break; //. >

				case 1: 
					edGMOAlertState.setText(R.string.SMinor);
					edGMOAlertState.setTextColor(Color.YELLOW);
					break; //. >
					
				case 2: 
					edGMOAlertState.setText(R.string.SMajor);
					edGMOAlertState.setTextColor(Color.MAGENTA);
					break; //. >
					
				case 3: 
					edGMOAlertState.setText(R.string.SCritical);
					edGMOAlertState.setTextColor(Color.RED);
					break; //. >
				}
			}
			else {
				edGMOConnectionState.setText("?");
				edGMOConnectionState.setTextColor(Color.GRAY);
				edGMOLocationState.setText("?");
				edGMOLocationState.setTextColor(Color.GRAY);
				edGMOAlertState.setText("?");
				edGMOAlertState.setTextColor(Color.GRAY);
			}
			//.
			btnGMODataStream.setVisibility((Object.DataStreamComponents != null) ? View.VISIBLE : View.GONE);
			//.
			_UpdateObjectModelPanel();
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, getString(R.string.SErrorOfUpdatingObjectInfo)+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private boolean flUpdatingObjectModelPanel = false;
	
	private void _UpdateObjectModelPanel() throws Exception {
		flUpdatingObjectModelPanel = true; 
		try {
			TextView lbGMOModelTitle = (TextView)findViewById(R.id.lbGMOModelTitle);
			//.
			LinearLayout UnknownModelLayout = (LinearLayout)findViewById(R.id.UnknownModelLayout);
			LinearLayout GMOTrackLogger1BusinessModelLayout = (LinearLayout)findViewById(R.id.GMOTrackLogger1BusinessModelLayout);
			LinearLayout GMO1GeoLogAndroidBusinessModelLayout = (LinearLayout)findViewById(R.id.GMO1GeoLogAndroidBusinessModelLayout);
			LinearLayout EnforaObjectTrackerBusinessModelLayout = (LinearLayout)findViewById(R.id.EnforaObjectTrackerBusinessModelLayout);
			LinearLayout EnforaMT3000TrackerBusinessModelLayout = (LinearLayout)findViewById(R.id.EnforaMT3000TrackerBusinessModelLayout);
			//.
			UnknownModelLayout.setVisibility(View.GONE);
			GMOTrackLogger1BusinessModelLayout.setVisibility(View.GONE);
			GMO1GeoLogAndroidBusinessModelLayout.setVisibility(View.GONE);
			EnforaObjectTrackerBusinessModelLayout.setVisibility(View.GONE);
			EnforaMT3000TrackerBusinessModelLayout.setVisibility(View.GONE);
			//.
			if ((ObjectModel != null) && (ObjectModel.BusinessModel != null)) {
				lbGMOModelTitle.setText(getString(R.string.SDevice)+ObjectModel.BusinessModel.GetName());
				//.
				switch (ObjectModel.GetID()) {
				case TGeoMonitoredObjectModel.ID: {
					switch (ObjectModel.BusinessModel.GetID()) { 
					case TGMOTrackLogger1BusinessModel.ID: {
						EditText edBatteryCharge = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edBatteryCharge);
						EditText edBatteryVoltage = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edBatteryVoltage);
						EditText edConnectorSignal = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edConnectorSignal);
						EditText edConnectorAccount = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edConnectorAccount);
						EditText edGPSModuleMode = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edGPSModuleMode);
						EditText edGPSModuleStatus = (EditText)findViewById(R.id.GMOTrackLogger1BusinessModel_edGPSModuleStatus);
						//.
						TGeoMonitoredObjectDeviceSchema.TGeoMonitoredObjectDeviceComponent DC = (TGeoMonitoredObjectDeviceSchema.TGeoMonitoredObjectDeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
						//.
						edBatteryCharge.setText(Short.toString(DC.BatteryModule.Charge.GetValue())+" %");
						edBatteryVoltage.setText(Double.toString(DC.BatteryModule.Voltage.GetValue()/100.0)+" v");
						edConnectorSignal.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Signal.GetValue())+" %");
						edConnectorAccount.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Account.GetValue()));
						edGPSModuleMode.setText(R.string.SOn);
						edGPSModuleMode.setTextColor(Color.GREEN);
						if (DC.GPSModule.FixIsAvailable()) {
							edGPSModuleStatus.setText(R.string.SCoordinatesAreAvailable);
							edGPSModuleStatus.setTextColor(Color.GREEN);
						}
						else {
							edGPSModuleStatus.setText(R.string.SCoordinatesAreNotAvailable);
							edGPSModuleStatus.setTextColor(Color.RED);
						}
						//.
						GMOTrackLogger1BusinessModelLayout.setVisibility(View.VISIBLE);
						break; //. >
					}
					}
					break; //. >
				}
					
				case TGeoMonitoredObject1Model.ID: {
					switch (ObjectModel.BusinessModel.GetID()) { 
					case TGMO1GeoLogAndroidBusinessModel.ID: {
						EditText edBatteryCharge = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edBatteryCharge);
						EditText edBatteryVoltage = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edBatteryVoltage);
						EditText edConnectorSignal = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edConnectorSignal);
						EditText edConnectorAccount = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edConnectorAccount);
						EditText edGPSModuleMode = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edGPSModuleMode);
						EditText edGPSModuleStatus = (EditText)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_edGPSModuleStatus);
						Spinner	 spVideoRecorderMode = (Spinner)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_spVideoRecorderMode);
						CheckBox cbVideoRecorderRecording = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderRecording);
						CheckBox cbVideoRecorderActive = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderActive);
						CheckBox cbVideoRecorderSaving = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderSaving);
						CheckBox cbVideoRecorderTransmitting = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderTransmitting);
						CheckBox cbVideoRecorderAudio = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderAudio);
						CheckBox cbVideoRecorderVideo = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbVideoRecorderVideo);
						Button btnShowVideoRecorderVideoPhone = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowVideoRecorderVideoPhone);
						Button btnShowVideoRecorderViewer = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowVideoRecorderViewer);
						Button btnShowVideoRecorderArchive = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowVideoRecorderArchive);
						Button btnShowSensorsModuleStream = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowSensorsModuleStream);
						Button btnShowSensorsModuleMeasurementsArchive = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowSensorsModuleMeasurementsArchive);
						Button btnShowSensorsModuleMeters = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowSensorsModuleMeters);
						Button btnShowControlsModuleStream = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowControlsModuleStream);
						Button btnSendAudioFileMessage = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnSendAudioFileMessage);
						Button btnImportAudioFiles = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnImportAudioFiles);
						CheckBox cbDataStreamerActive = (CheckBox)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_cbDataStreamerActive);
						Button btnDataStreamerStreamDescriptor = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnDataStreamerStreamDescriptor);
						LinearLayout llAlarmModule = (LinearLayout)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_llAlarmModule);
						Button btnShowUserMessagingModuleMessagingPanel = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowUserMessagingModuleMessagingPanel);
						final Button btnShowHideAdditive = (Button)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_btnShowHideAdditive);
						final LinearLayout llAdditive = (LinearLayout)findViewById(R.id.GMO1GeoLogAndroidBusinessModel_llAdditive);
						//.
						final TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent DC = (TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
						//.
						edBatteryCharge.setText(Short.toString(DC.BatteryModule.Charge.GetValue())+" %");
						edBatteryVoltage.setText(Double.toString(DC.BatteryModule.Voltage.GetValue()/100.0)+" v");
						edConnectorSignal.setText(Short.toString(DC.ConnectorModule.ServiceProvider.Signal.GetValue())+" %");
						edConnectorAccount.setText(Short.toString(DC.ConnectorModule.ServiceProvider.Account.GetValue()));
						switch (DC.GPSModule.Mode.GetValue()) {
						
						case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULEMODE_DISABLED:
							edGPSModuleMode.setText(R.string.SDisabled);
							edGPSModuleMode.setTextColor(Color.RED);
							edGPSModuleStatus.setText("?");
							edGPSModuleStatus.setTextColor(Color.GRAY);
							break; //. >
							
						case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULEMODE_ENABLED:
							edGPSModuleMode.setText(R.string.SOn);
							edGPSModuleMode.setTextColor(Color.GREEN);
							//.
							switch (DC.GPSModule.Status.GetValue()) {
							case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULESTATUS_AVAILABLE:
								edGPSModuleStatus.setText(R.string.SCoordinatesGetting);
								edGPSModuleStatus.setTextColor(Color.GREEN);
								break; //. >
								
							case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE:
								edGPSModuleStatus.setText(R.string.SPermanentlyUnavailable);
								edGPSModuleStatus.setTextColor(Color.RED);
								break; //. >
								
							case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE:
								edGPSModuleStatus.setText(R.string.STemporarilyUnavailable);
								edGPSModuleStatus.setTextColor(Color.MAGENTA);
								break; //. >
								
							case com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule.GPSMODULESTATUS_UNKNOWN:
								edGPSModuleStatus.setText("?");
								edGPSModuleStatus.setTextColor(Color.GRAY);
								break; //. >						
							}
							break; //. >
						}
				        String[] SA = new String[6];
				        SA[0] = getString(R.string.SVRModeIsUnknown);
				        SA[1] = getString(R.string.SVRModeStreamH263);
				        SA[2] = getString(R.string.SVRModeStreamH264);
				        SA[3] = getString(R.string.SVRModeMPEG4);
				        SA[4] = getString(R.string.SVRMode3GP);
				        SA[5] = getString(R.string.SVRModeStreamFrame);
				        ArrayAdapter<String> saMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
				        saMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        spVideoRecorderMode.setAdapter(saMode);
				        spVideoRecorderMode.setOnItemSelectedListener(new OnItemSelectedListener() {
				        	
				            @Override
				            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				            	final short Mode;
				            	switch (position) {
				            	
				            	case 1:
				            		Mode = TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1;
				            		break; //. >
				            		
				            	case 2:
				            		Mode = TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1;
				            		break; //. >
				            		
				            	case 3:
				            		Mode = TVideoRecorderModule.MODE_MPEG4;
				            		break; //. >
				            		
				            	case 4:
				            		Mode = TVideoRecorderModule.MODE_3GP;
				            		break; //. >
				            		
				            	case 5:
				            		Mode = TVideoRecorderModule.MODE_FRAMESTREAM;
				            		break; //. >
				            	
			            		default:
			            			Mode = TVideoRecorderModule.MODE_UNKNOWN;				            			
				            		break; //. >
				            	}
				            	//.
				            	if (Mode == DC.VideoRecorderModule.Mode.Value)
				            		return; //. ->
				            	//.
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {
									
									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+3/*Set VideoRecorderModule.Mode*/;
										byte[] Data = TDataConverter.ConvertInt16ToLEByteArray(Mode);
										Object.SetData(DataType, Data);
									}		
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), Mode);
										Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Mode.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
							}
				        });        
				        switch (DC.VideoRecorderModule.Mode.GetValue()) {
				        
				        case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
				            spVideoRecorderMode.setSelection(1);
				        	break; //. >
				        	
				        case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
				            spVideoRecorderMode.setSelection(2);
				        	break; //. >
				        	
				        case TVideoRecorderModule.MODE_MPEG4:
				        	spVideoRecorderMode.setSelection(3);
				        	break; //. >
				        	
				        case TVideoRecorderModule.MODE_3GP:
				        	spVideoRecorderMode.setSelection(4);
				        	break; //. >
				        	
				        case TVideoRecorderModule.MODE_FRAMESTREAM:
				        	spVideoRecorderMode.setSelection(5);
				        	break; //. >
				        default: 
				            spVideoRecorderMode.setSelection(0);
				        }
						cbVideoRecorderRecording.setChecked(DC.VideoRecorderModule.Recording.BooleanValue());
						cbVideoRecorderRecording.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.VideoRecorderModule.Recording.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {

									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+8/*Set VideoRecorderModule.Recording*/;
										byte[] Data = new byte[] {V};
										Object.SetData(DataType, Data);
									}
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										if (V != 0) {
											Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Recording.GetAddressArray(), Value.ToByteArray()); //. Active = true
											Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Active.GetAddressArray(), Value.ToByteArray());
										}
										else {
											Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Active.GetAddressArray(), Value.ToByteArray());
											Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Recording.GetAddressArray(), Value.ToByteArray()); //. Active = true
										}
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderActive.setChecked(DC.VideoRecorderModule.Active.BooleanValue());
						cbVideoRecorderSaving.setChecked(DC.VideoRecorderModule.Saving.BooleanValue());
						cbVideoRecorderSaving.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.VideoRecorderModule.Saving.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {
									
									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+7/*Set VideoRecorderModule.Saving*/;
										byte[] Data = new byte[] {V};
										Object.SetData(DataType, Data);
									}
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Saving.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderTransmitting.setChecked(DC.VideoRecorderModule.Transmitting.BooleanValue());
						cbVideoRecorderTransmitting.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.VideoRecorderModule.Transmitting.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {

									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+6/*Set VideoRecorderModule.Transmitting*/;
										byte[] Data = new byte[] {V};
										Object.SetData(DataType, Data);
									}
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Transmitting.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderAudio.setChecked(DC.VideoRecorderModule.Audio.BooleanValue());
						cbVideoRecorderAudio.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.VideoRecorderModule.Audio.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {
									
									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+4/*Set VideoRecorderModule.Audio*/;
										byte[] Data = new byte[] {V};
										Object.SetData(DataType, Data);
									}
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Audio.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderVideo.setChecked(DC.VideoRecorderModule.Video.BooleanValue());
						cbVideoRecorderVideo.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.VideoRecorderModule.Video.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {

									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+5/*Set VideoRecorderModule.Video*/;
										byte[] Data = new byte[] {V};
										Object.SetData(DataType, Data);
									}
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Video.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						btnShowVideoRecorderVideoPhone.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, TVideoRecorderServerVideoPhoneCallPanel.class);
			    	        	intent.putExtra("Name",Object.Name);
			    	        	intent.putExtra("idTComponent",SpaceDefines.idTCoComponent);
			    	        	intent.putExtra("idComponent",Object.ID);
			    	        	//.
					            startActivity(intent);
							}
						});
						btnShowVideoRecorderViewer.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								TGeoScopeServerInfo.TInfo ServersInfo;
					    		try {
									ServersInfo = Server.Info.GetInfo();
									if (!ServersInfo.IsGeographProxyServerValid()) 
										throw new Exception(TCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographProxyServer)); //. =>
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, TVideoRecorderServerViewer.class);
				    	        	intent.putExtra("Name",Object.Name);
				    	        	intent.putExtra("GeographProxyServerAddress",ServersInfo.GeographProxyServerAddress);
				    	        	intent.putExtra("GeographProxyServerPort",ServersInfo.GeographProxyServerPort);
				    	        	intent.putExtra("UserID",Object.Server.User.UserID);
				    	        	intent.putExtra("UserPassword",Object.Server.User.UserPassword);
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										intent.putExtra("ParametersType", TVideoRecorderServerViewer.PARAMETERS_TYPE_OID);
										intent.putExtra("ObjectID", ObjectID);
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										intent.putExtra("ParametersType", TVideoRecorderServerViewer.PARAMETERS_TYPE_OIDX);
										intent.putExtra("ObjectIndex", ObjectIndex);
						        		break; //. >
						        	}
				    	        	intent.putExtra("flAudio",DC.VideoRecorderModule.Audio.BooleanValue());
				    	        	intent.putExtra("flVideo",DC.VideoRecorderModule.Video.BooleanValue());
				    	        	intent.putExtra("ConnectionType", TVideoRecorderServerViewer.CONNECTION_TYPE_TCP);
				    	        	//.
						            startActivity(intent);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowVideoRecorderViewer.setEnabled((DC.VideoRecorderModule.Mode.GetValue() == TVideoRecorderModule.MODE_FRAMESTREAM) && DC.VideoRecorderModule.Active.BooleanValue());
						btnShowVideoRecorderArchive.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					    		try {
					    			TGeoScopeServerInfo.TInfo ServersInfo = Server.Info.GetInfo();
									if (!ServersInfo.IsGeographDataServerValid()) 
										throw new Exception(TCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographDataServer)); //. =>
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, TVideoRecorderServerArchive.class);
				    	        	intent.putExtra("GeographDataServerAddress",ServersInfo.GeographDataServerAddress);
				    	        	intent.putExtra("GeographDataServerPort",ServersInfo.GeographDataServerPort);
				    	        	intent.putExtra("UserID",Server.User.UserID);
				    	        	intent.putExtra("UserPassword",Server.User.UserPassword);
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										intent.putExtra("ParametersType", TVideoRecorderServerArchive.PARAMETERS_TYPE_OID);
										intent.putExtra("ObjectID", ObjectID);
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										intent.putExtra("ParametersType", TVideoRecorderServerArchive.PARAMETERS_TYPE_OIDX);
										intent.putExtra("ObjectIndex", ObjectIndex);
						        		break; //. >
						        	}
						            startActivity(intent);
									//. TGettingCurrentLocation GCL = new TGettingCurrentLocation(ServersInfo.GeographProxyServerAddress,ServersInfo.GeographProxyServerPort, Reflector.Server.User.UserID,Reflector.Server.User.UserPassword, Object);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowSensorsModuleStream.setEnabled(DC.SensorsModule.SensorsDataValue.Value != null);
						btnShowSensorsModuleStream.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					            try {
									if ((DC.SensorsModule.SensorsDataValue.Value == null) || (DC.SensorsModule.SensorsDataValue.Value.length == 0)) {
								    	Toast.makeText(TCoGeoMonitorObjectPanel.this, R.string.SThereAreNoSensors, Toast.LENGTH_LONG).show();
										return; //. ->
									}
									com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.TModel Model = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.TModel(DC.SensorsModule.SensorsDataValue.Value);
									if (Model.Stream == null)
										return; //. ->
									//.
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.class);
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.PARAMETERS_TYPE_OID);
										intent.putExtra("ObjectID", ObjectID);
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.PARAMETERS_TYPE_OIDX);
										intent.putExtra("ObjectIndex", ObjectIndex);
						        		break; //. >
						        	}
						            startActivity(intent);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowSensorsModuleMeasurementsArchive.setEnabled(true);
						btnShowSensorsModuleMeasurementsArchive.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					    		try {
					    			TGeoScopeServerInfo.TInfo ServersInfo = Server.Info.GetInfo();
									if (!ServersInfo.IsGeographDataServerValid()) 
										throw new Exception(TCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographDataServer)); //. =>
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, TSensorsModuleMeasurementsArchive.class);
				    	        	intent.putExtra("GeographDataServerAddress",ServersInfo.GeographDataServerAddress);
				    	        	intent.putExtra("GeographDataServerPort",ServersInfo.GeographDataServerPort);
				    	        	intent.putExtra("UserID",Server.User.UserID);
				    	        	intent.putExtra("UserPassword",Server.User.UserPassword);
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										intent.putExtra("ParametersType", TSensorsModuleMeasurementsArchive.PARAMETERS_TYPE_OID);
										intent.putExtra("ObjectID", ObjectID);
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										intent.putExtra("ParametersType", TSensorsModuleMeasurementsArchive.PARAMETERS_TYPE_OIDX);
										intent.putExtra("ObjectIndex", ObjectIndex);
						        		break; //. >
						        	}
						            startActivity(intent);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowSensorsModuleMeters.setEnabled(DC.SensorsModule.SensorsDataValue.Value != null);
						btnShowSensorsModuleMeters.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					            try {
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Meters.TSensorsMetersPanel.class);
						            long _ObjectID = 0;
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										_ObjectID = ObjectID;
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										_ObjectID = Component.CoGeoMonitorObjects.Items[ObjectIndex].ID;
						        		break; //. >
						        	}
									intent.putExtra("ObjectID", _ObjectID);
						            startActivity(intent);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowControlsModuleStream.setEnabled(DC.SensorsModule.SensorsDataValue.Value != null);
						btnShowControlsModuleStream.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
					            try {
									if ((DC.ControlsModule.ControlsDataValue.Value == null) || (DC.ControlsModule.ControlsDataValue.Value.length == 0)) {
								    	Toast.makeText(TCoGeoMonitorObjectPanel.this, R.string.SThereAreNoControls, Toast.LENGTH_LONG).show();
										return; //. ->
									}
									com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.TModel Model = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.TModel(DC.ControlsModule.ControlsDataValue.Value);
									if (Model.ControlStream == null)
										return; //. ->
									//.
						            Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TDataStreamPropsPanel.class);
						        	switch (ParametersType) {
						        	
						        	case PARAMETERS_TYPE_OID:
										intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TDataStreamPropsPanel.PARAMETERS_TYPE_OID);
										intent.putExtra("ObjectID", ObjectID);
						        		break; //. >
						        		
						        	case PARAMETERS_TYPE_OIDX:
										intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TDataStreamPropsPanel.PARAMETERS_TYPE_OIDX);
										intent.putExtra("ObjectIndex", ObjectIndex);
						        		break; //. >
						        	}
				    	        	intent.putExtra("DataStreamDescriptorData",Model.ControlStream.ToByteArray());
						            startActivity(intent);
								} catch (Exception E) {
							    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
							}
						});
						btnShowControlsModuleStream.setEnabled((DC.ControlsModule.ControlsDataValue.Value != null) && (DC.ControlsModule.ControlsDataValue.Value.length > 0));
						btnSendAudioFileMessage.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
						    	AlertDialog.Builder alert = new AlertDialog.Builder(TCoGeoMonitorObjectPanel.this);
						    	//.
						    	alert.setTitle(R.string.SPlayAudioMessage);
						    	alert.setMessage(R.string.SEnterIDOfTheMessage);
						    	//.
						    	final EditText input = new EditText(TCoGeoMonitorObjectPanel.this);
						    	alert.setView(input);
						    	//.
						    	alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
						    		@Override
						        	public void onClick(DialogInterface dialog, int whichButton) {
						    			//. hide keyboard
						        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						        		//.
						        		final String FileMessageID = input.getText().toString();
						        		try {
								    		TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this,getString(R.string.SWaitAMoment)) {
								    			
								    			@Override
								    			public void Process() throws Exception {
													TAudioFileMessageValue Value = new TAudioFileMessageValue();
													Value.TimeStamp = OleDate.UTCCurrentTimestamp();
													try {
														Value.FileID = Integer.parseInt(FileMessageID);
														Value.FileName = null;
													}
													catch (NumberFormatException NFE) {
														Value.FileID = 0;
														Value.FileName = FileMessageID;
													}
													Value.DestinationID = 1;
													Value.Volume = 100; //. %
													Value.RepeatCount = -1; //. minus means that there is no waiting for play finish
													Value.RepeatInterval = 0;
													Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.AudioModule.AudioFileMessageValue.GetAddressArray(), Value.ToByteArray());
								    			}
								    			
								    			@Override 
								    			public void DoOnCompleted() throws Exception {
								    			}
								    			
								    			@Override
								    			public void DoOnException(Exception E) {
								    				Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								    			}
								    		};
								    		Processing.Start();
						        		}
						        		catch (Exception E) {
						        			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
						        			return; //. ->
						    		    }
						          	}
						    	});
						    	//.
						    	alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
						    		@Override
						    		public void onClick(DialogInterface dialog, int whichButton) {
						    		}
						    	});
						    	//.
						    	alert.show();    
							}
						});
						btnImportAudioFiles.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TCoGeoMonitorObjectPanel.this, ".ZIP", new TFileSystemFileSelector.OpenDialogListener() {
						        	
						            @Override
						            public void OnSelectedFile(String fileName) {
					                    final File ChosenFile = new File(fileName);
					                    //.
										try {
								    		TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this,getString(R.string.SWaitAMoment)) {
								    			
								    			@Override
								    			public void Process() throws Exception {
													TAudioFilesValue Value = new TAudioFilesValue();
													Value.Timestamp = OleDate.UTCCurrentTimestamp();
													Value.Value = TFileSystem.File_ToByteArray(ChosenFile.getAbsolutePath());
													Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.AudioModule.AudioFilesValue.GetAddressArray(), Value.ToByteArray());
								    			}
								    			
								    			@Override 
								    			public void DoOnCompleted() throws Exception {
								        			Toast.makeText(TCoGeoMonitorObjectPanel.this, R.string.SAudioFilesHaveBeenImportedToTheDevice, Toast.LENGTH_LONG).show();  						
								    			}
								    			
								    			@Override
								    			public void DoOnException(Exception E) {
								    				Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								    			}
								    		};
								    		Processing.Start();
										}
										catch (Throwable E) {
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
						        			Toast.makeText(TCoGeoMonitorObjectPanel.this, S, Toast.LENGTH_LONG).show();  						
										}
						            }

									@Override
									public void OnCancel() {
									}
						        });
						    	FileSelector.show();    	
							}
						});
						cbDataStreamerActive.setChecked(DC.DataStreamerModule.ActiveValue.BooleanValue());
						cbDataStreamerActive.setOnClickListener(new OnClickListener() {
							
				            @Override
				            public void onClick(View v) {
								if (flUpdatingObjectModelPanel)
									return; //. ->
				                boolean checked = ((CheckBox)v).isChecked();
								//.
								if (checked == DC.DataStreamerModule.ActiveValue.BooleanValue())
									return; //. ->
								//.
								final byte V = (byte)(checked ? 1 : 0); 
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {

									@Override
									public void Process() throws Exception {
										TComponentTimestampedBooleanValue Value = new TComponentTimestampedBooleanValue();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), V);
										try {
											Object.GeographServerObjectController().Component_WriteDeviceCUAC(DC.DataStreamerModule.ActiveValue.GetAddressArray(), Value.ToByteArray());
										}
										catch (OperationException OE) {
											switch (OE.Code) {
											
											case TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerIsDisabled:
												throw new Exception(context.getString(R.string.SDataStreamerIsDisabled)); //. =>
											
											case TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerConfigurationError:
												throw new Exception(context.getString(R.string.SDataStreamerConfigurationError)); //. =>
											
											case TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerChannelsConflictError:
												throw new Exception(context.getString(R.string.SDataStreamerChannelsConflictError)); //. =>
											
											default:
												throw OE; //. =>
											}
										}
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });  
						btnDataStreamerStreamDescriptor.setEnabled(cbDataStreamerActive.isChecked());
						btnDataStreamerStreamDescriptor.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this) {

									private TComponentTimestampedDataValue Value;
									
									@Override
									public void Process() throws Exception {
						        		String Params = "1"; //. get an own streaming component stream descriptor
						        		//.
						        		byte[] _AddressData = Params.getBytes("US-ASCII");
						        		Value = new TComponentTimestampedDataValue();
						        		try {
						        			byte[] Data = Object.GeographServerObjectController().Component_ReadDeviceByAddressDataCUAC(DC.DataStreamerModule.StreamingComponentsValue.GetAddressArray(),_AddressData);
						        			Value.FromByteArray(Data,(new TIndex(0)));
						        		}
						        		catch (OperationException OE) {
						        			switch (OE.Code) {

						        			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
						        				throw new Exception(context.getString(R.string.SUserAccessIsDenied)); //. =>
						        				
						        			default:
						        				throw new OperationException(OE.Code,"error of reading the device, "+OE.getMessage()); //. =>
						        			}
						        		}
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										if (Value.Value != null) {
											TStreamDescriptor StreamDescriptor = new TStreamDescriptor(Value.Value);
											if (StreamDescriptor.Channels_Count() > 0) {
												TChannelIDs Channels = StreamDescriptor.Channels_IDs(); 
												Intent intent = new Intent(TCoGeoMonitorObjectPanel.this, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.class);
									        	switch (ParametersType) {
									        	
									        	case PARAMETERS_TYPE_OID:
													intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.PARAMETERS_TYPE_OID);
													intent.putExtra("ObjectID", ObjectID);
									        		break; //. >
									        		
									        	case PARAMETERS_TYPE_OIDX:
													intent.putExtra("ParametersType", com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel.PARAMETERS_TYPE_OIDX);
													intent.putExtra("ObjectIndex", ObjectIndex);
									        		break; //. >
									        	}
												intent.putExtra("ChannelIDs", Channels.ToByteArray());
												intent.putExtra("DenyOpening", true);
												//.
										        startActivity(intent);
											}
											else
					                			Toast.makeText(TCoGeoMonitorObjectPanel.this, R.string.SNoChannels, Toast.LENGTH_LONG).show();
										}
										else
				                			Toast.makeText(TCoGeoMonitorObjectPanel.this, R.string.SNoChannels, Toast.LENGTH_LONG).show();
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
							}
						});
						boolean flAlarms = ((DC.AlarmModule.AlarmDataValue.Value != null) && (DC.AlarmModule.AlarmDataValue.Value.length > 0));
						if (flAlarms) {
							TAlarmModule.TAlarms Alarms = new TAlarms(DC.AlarmModule.AlarmDataValue.Value);
							//.
							int TableTextSize = 18;
							//.
							llAlarmModule.removeAllViews();
							//. Alarm module header
							TextView lbAlarms = new TextView(this);
							lbAlarms.setBackgroundColor(Color.RED);
							lbAlarms.setText(R.string.SAlarms);
							lbAlarms.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
							lbAlarms.setTextColor(Color.WHITE);
							lbAlarms.setGravity(Gravity.CENTER);
							llAlarmModule.addView(lbAlarms, new LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
							//.
							TableLayout.LayoutParams TLP = new TableLayout.LayoutParams();
							TLP.height = TableRow.LayoutParams.WRAP_CONTENT; 
							TLP.width = TableRow.LayoutParams.MATCH_PARENT;
							//.
							TableLayout tlAlarms = new TableLayout(this);
							//. alarm rows
							Typeface AlarmTF = Typeface.create(Typeface.SERIF, Typeface.BOLD);
							int Cnt = Alarms.Items.length;
							for (int I = 0; I < Cnt; I++) {
								TAlarmModule.TAlarm Alarm = Alarms.Items[I];
								//. row
								TableRow Row = new TableRow(this);
								Row.setBackgroundColor(Color.WHITE);
								//.
								TableRow.LayoutParams RowParams = new TableRow.LayoutParams();
								RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
								RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
								//.
								TableRow.LayoutParams ColParams = new TableRow.LayoutParams();
								ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
								ColParams.width = TableRow.LayoutParams.WRAP_CONTENT;
								//.			
						    	String TS = (new SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.US)).format((new OleDate(Alarm.Timestamp)).GetDateTime());
								TextView Col = new TextView(this);
								Col.setText(TS+": ");
								Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
								Col.setBackgroundColor(Color.WHITE);
								Col.setTextColor(Color.BLACK);
								Col.setGravity(Gravity.LEFT);
								//.
								Row.addView(Col, ColParams);
								//.
								ColParams = new TableRow.LayoutParams();
								ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
								ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
								//.			
								Col = new TextView(this);
								Col.setText(Alarm.ID+" "+"("+Alarm.Value+")");
								Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
								Col.setBackgroundColor(Color.WHITE);
								Col.setTextColor(Alarm.GetSeverityColor());
								Col.setTypeface(AlarmTF);
								Col.setGravity(Gravity.LEFT);
								//.
								Row.addView(Col, ColParams);
								//.
								tlAlarms.addView(Row, RowParams);		
							}
							llAlarmModule.addView(tlAlarms, TLP);
							//.
							llAlarmModule.setVisibility(View.VISIBLE);
						}
						else {
							llAlarmModule.setVisibility(View.GONE);
							llAlarmModule.removeAllViews();
						}
						btnShowUserMessagingModuleMessagingPanel.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this,getString(R.string.SWaitAMoment)) {
									
									@Override
									public void Process() throws Exception {
										TTracker Tracker = TTracker.GetTracker(context.getApplicationContext());
										if (Tracker == null)
											throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
										Tracker.GeoLog.SensorsModule.InternalSensorsModule.UserMessagingModule.StartUserMessagingForObject(Object, Object.Server.User.UserID,"", Tracker.GeoLog.idTOwnerComponent,Tracker.GeoLog.idOwnerComponent);
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
									}
									
									@Override
									public void DoOnException(Exception E) {
										Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
							}
						});
						btnShowHideAdditive.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								llAdditive.setVisibility((llAdditive.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
								if (llAdditive.getVisibility() == View.VISIBLE) {
							    	svRoot.postDelayed(new Runnable() {
							    	    @Override
							    	    public void run() {
							            	try {
												svRoot.fullScroll(View.FOCUS_DOWN);
							            	}
							            	catch (Throwable E) {
							            		TGeoLogApplication.Log_WriteError(E);
							            	}
							    	    }
									},100);
								}
								//.
								btnShowHideAdditive.setVisibility(View.GONE);
							}
						});
						//.
						GMO1GeoLogAndroidBusinessModelLayout.setVisibility(View.VISIBLE);
						break; //. >
					}
					}
					break; //. >
				}
					
				case TEnforaObjectModel.ID: {
					switch (ObjectModel.BusinessModel.GetID()) { 
					case TEnforaObjectTrackerBusinessModel.ID: {
						EditText edBatteryCharge = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edBatteryCharge);
						EditText edBatteryVoltage = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edBatteryVoltage);
						EditText edConnectorSignal = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edConnectorSignal);
						EditText edConnectorAccount = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edConnectorAccount);
						EditText edGPSModuleMode = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edGPSModuleMode);
						EditText edGPSModuleStatus = (EditText)findViewById(R.id.EnforaObjectTrackerBusinessModel_edGPSModuleStatus);
						//.
						TEnforaObjectDeviceSchema.TEnforaObjectDeviceComponent DC = (TEnforaObjectDeviceSchema.TEnforaObjectDeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
						//.
						edBatteryCharge.setText(Short.toString(DC.BatteryModule.Charge.GetValue())+" %");
						edBatteryVoltage.setText(Double.toString(DC.BatteryModule.Voltage.GetValue()/100.0)+" v");
						edConnectorSignal.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Signal.GetValue())+" %");
						edConnectorAccount.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Account.GetValue()));
						edGPSModuleMode.setText(R.string.SOn);
						edGPSModuleMode.setTextColor(Color.GREEN);
						if (DC.GPSModule.FixIsAvailable()) {
							edGPSModuleStatus.setText(R.string.SCoordinatesAreAvailable);
							edGPSModuleStatus.setTextColor(Color.GREEN);
						}
						else {
							edGPSModuleStatus.setText(R.string.SCoordinatesAreNotAvailable);
							edGPSModuleStatus.setTextColor(Color.RED);
						}
						//.
						EnforaObjectTrackerBusinessModelLayout.setVisibility(View.VISIBLE);
						break; //. >
					}
					}
					break; //. >
				}
					
				case TEnforaMT3000ObjectModel.ID: {
					switch (ObjectModel.BusinessModel.GetID()) {
					case TEnforaMT3000TrackerBusinessModel.ID: {
						EditText edBatteryCharge = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edBatteryCharge);
						EditText edBatteryVoltage = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edBatteryVoltage);
						EditText edConnectorSignal = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edConnectorSignal);
						EditText edConnectorAccount = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edConnectorAccount);
						EditText edGPSModuleStatus = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edGPSModuleStatus);
						CheckBox cbStatusStop = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusStop);
						CheckBox cbStatusIdle = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusIdle);
						CheckBox cbStatusMotion = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusMotion);
						CheckBox cbStatusLowFuel = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusLowFuel);
						CheckBox cbStatusLowBattery = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusLowBattery);
						CheckBox cbStatusGPS = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusGPS);
						CheckBox cbStatusMalfunction = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbStatusMalfunction);
						EditText edStatusMalfunction = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edStatusMalfunction);
						CheckBox cbIgnition = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbIgnition);
						CheckBox cbTowAlert = (CheckBox)findViewById(R.id.EnforaMT3000TrackerBusinessModel_cbTowAlert);
						EditText edSpeedometer = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edSpeedometer);
						EditText edOdometer = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edOdometer);
						EditText edTachometer = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edTachometer);
						EditText edAccelerometer = (EditText)findViewById(R.id.EnforaMT3000TrackerBusinessModel_edAccelerometer);
						//.
						TEnforaMT3000ObjectDeviceSchema.TEnforaMT3000ObjectDeviceComponent DC = (TEnforaMT3000ObjectDeviceSchema.TEnforaMT3000ObjectDeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
						//.
						edBatteryCharge.setText(Short.toString(DC.BatteryModule.Charge.GetValue())+" %");
						edBatteryVoltage.setText(Double.toString(DC.BatteryModule.Voltage.GetValue()/100.0)+" v");
						edConnectorSignal.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Signal.GetValue())+" %");
						edConnectorAccount.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Account.GetValue()));
						if (DC.GPSModule.IsActive.BooleanValue()) {
							edGPSModuleStatus.setText(R.string.SActive);
							edGPSModuleStatus.setTextColor(Color.GREEN);
						}
						else {
							edGPSModuleStatus.setText(R.string.SInactive);
							edGPSModuleStatus.setTextColor(Color.RED);
						}
						cbStatusStop.setChecked(DC.StatusModule.IsStop.BooleanValue());
						cbStatusIdle.setChecked(DC.StatusModule.IsIdle.BooleanValue());
						cbStatusMotion.setChecked(DC.StatusModule.IsMotion.BooleanValue());
						cbStatusLowFuel.setChecked(DC.OBDIIModule.FuelModule.IsLow.BooleanValue());
						cbStatusLowBattery.setChecked(DC.OBDIIModule.BatteryModule.IsLow.BooleanValue());
						cbStatusGPS.setChecked(DC.GPSModule.IsActive.BooleanValue());
						cbStatusMalfunction.setChecked(DC.StatusModule.IsMIL.BooleanValue());
						String S = DC.OBDIIModule.MILAlertModule.AlertCodes.GetValue();
						if (!S.equals("")) {
							edStatusMalfunction.setText(S);
							edStatusMalfunction.setTextColor(Color.RED);
						}
						else {
							edStatusMalfunction.setText(R.string.SNoMalfunction);
							edStatusMalfunction.setTextColor(Color.GREEN);
						}
						cbIgnition.setChecked(DC.IgnitionModule.Value.BooleanValue());
						cbTowAlert.setChecked(DC.TowAlertModule.Value.BooleanValue());
						edSpeedometer.setText(Double.toString((int)DC.OBDIIModule.SpeedometerModule.Value.GetValue())+" Km/h");
						edSpeedometer.setTextColor(Color.GREEN);
						edOdometer.setText(Double.toString((int)DC.OBDIIModule.OdometerModule.Value.GetValue())+" Km");
						edOdometer.setTextColor(Color.GREEN);
						edTachometer.setText(Integer.toString(DC.OBDIIModule.TachometerModule.Value.GetValue())+" RPM");
						edTachometer.setTextColor(Color.GREEN);
						edAccelerometer.setText(Double.toString(DC.AccelerometerModule.Value.GetValue())+" mG");
						edAccelerometer.setTextColor(Color.GREEN);
						//.
						EnforaMT3000TrackerBusinessModelLayout.setVisibility(View.VISIBLE);
						break; //. >					
					}
					}
				}
				}
			}
			else
				lbGMOModelTitle.setText(R.string.SUnknownDevice);
		}
		finally {
			flUpdatingObjectModelPanel = false;
		}
	}
	
	public void ShowCurrentLocation() {
		TAsyncProcessing Processing = new TAsyncProcessing(TCoGeoMonitorObjectPanel.this,getString(R.string.SWaitAMoment)) {
			
			private TXYCoord Crd = null;

			@Override
			public void Process() throws Exception {
				Crd = Object.GetComponentLocation(TCoGeoMonitorObjectPanel.this);
				//.
				Thread.sleep(100);
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
				if (Crd != null) {
					Intent intent = new Intent(TCoGeoMonitorObjectPanel.this,TReflector.class);
					intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATION);
					intent.putExtra("LocationXY", Crd.ToByteArray());
					TCoGeoMonitorObjectPanel.this.startActivity(intent);
				}
			}
			
			@Override
			public void DoOnException(Exception E) {
		    	Toast.makeText(TCoGeoMonitorObjectPanel.this, getString(R.string.SErrorOfGettingCurrentGMOPosition)+E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		};
		Processing.Start();
	}
	
	private void ShowUser() {
		if (ObjectModel == null)
			return; //. ->
		long UserID = ObjectModel.ObjectUserID();
		if (UserID == 0)
			return; //. ->
		//.
    	Intent intent = new Intent(this, TUserPanel.class);
    	int ComponentID = 0;
    	if (Component != null)
    		ComponentID = Component.ID;
		intent.putExtra("ComponentID", ComponentID);
    	intent.putExtra("UserID",UserID);
    	startActivity(intent);
	}
	
	private void ShowHistory() {
		Calendar c = Calendar.getInstance();
		DatePickerDialog DateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {      

			private boolean flCalledOnce = false;
			
			@Override
			public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
				//. workaround for the picker bug (called twice)
				if (flCalledOnce)
					return; //. ->
				flCalledOnce = true;
				//.
				AddTrack_Date_Year = year;
				AddTrack_Date_Month = monthOfYear+1;
				AddTrack_Date_Day = dayOfMonth;
				//.
        		OleDate Day = new OleDate(AddTrack_Date_Year,AddTrack_Date_Month,AddTrack_Date_Day, 0,0,0,0);
        		try {
        			ShowHistory((int)OleDate.LocalTimeToUTC(Day.toDouble())-1.0/*cover any time-zone*/, (short)3);
        		}
        		catch (Exception E) {
        			Toast.makeText(TCoGeoMonitorObjectPanel.this, getString(R.string.SErrorOfObjectTrackAdding)+E.getMessage(), Toast.LENGTH_SHORT).show();
        		}
			}
		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
		DateDialog.show();
	}
	
	private void ShowHistory(double DayDate, short DaysCount) {
		try {
			TGeoScopeServerInfo.TInfo ServersInfo = Server.Info.GetInfo();
			if (!ServersInfo.IsGeographDataServerValid()) 
				throw new Exception(TCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographDataServer)); //. =>
			//.
	    	Intent intent = new Intent(this, TObjectModelHistoryPanel.class);
	    	//.
	    	intent.putExtra("ObjectID", (long)Object.ID);
	    	//.
	    	intent.putExtra("DayDate", DayDate);
	    	intent.putExtra("DaysCount", DaysCount);
	    	//.
	    	intent.putExtra("GeographDataServerAddress",ServersInfo.GeographDataServerAddress);
	    	intent.putExtra("GeographDataServerPort",ServersInfo.GeographDataServerPort);
	    	intent.putExtra("UserID",Server.User.UserID);
	    	intent.putExtra("UserPassword",Server.User.UserPassword);
	    	//.
	    	startActivity(intent);
		} catch (Exception E) {
	    	Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    	return; //. ->
		}
	}
	
	private void AddTrack() {
		Calendar c = Calendar.getInstance();
		DatePickerDialog DateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {                

			private boolean flCalledOnce = false;
			
			@Override
			public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
				//. workaround for the picker bug (called twice)
				if (flCalledOnce)
					return; //. ->
				flCalledOnce = true;
				//.
				AddTrack_Date_Year = year;
				AddTrack_Date_Month = monthOfYear+1;
				AddTrack_Date_Day = dayOfMonth;
				//.
        		ColorPicker ColorDialog = new ColorPicker(TCoGeoMonitorObjectPanel.this, new ColorPicker.OnColorChangedListener() {
        			@Override
        			public void colorChanged(int color) {
        				AddTrack_Color = color;
        				//.
                		OleDate Day = new OleDate(AddTrack_Date_Year,AddTrack_Date_Month,AddTrack_Date_Day, 0,0,0,0);
                		try {
                			AddTrack(Object.ID,Day.toDouble(),AddTrack_Color);
                		}
                		catch (Exception E) {
                			Toast.makeText(TCoGeoMonitorObjectPanel.this, getString(R.string.SErrorOfObjectTrackAdding)+E.getMessage(), Toast.LENGTH_SHORT).show();
                		}
        			}
        		},Color.RED);    
        		ColorDialog.show();
			}
		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
		DateDialog.show();
	}
	
	private void AddTrack(long pObjectID, double pTrackDay, int pTrackColor) {
		if (Component == null)
			return; //. ->
		final long ObjectID = pObjectID;
		final double TrackDay = pTrackDay;
		final int TrackColor = pTrackColor;
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SLoadingTheObjectTrack)) {
			
			private byte[] TrackData;
			@Override
			public void Process() throws Exception {
				TrackData = Component.ObjectTracks.GetTrackData(ObjectID,TrackDay,TrackColor);
			}
			@Override 
			public void DoOnCompleted() throws Exception {
    			Component.ObjectTracks.AddNewTrack(TrackData,ObjectID,TrackDay,TrackColor);
    			//.
    			Component.StartUpdatingSpaceImage();
    			//.
    			TCoGeoMonitorObjectPanel.this.finish();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}

	private void ShowDataStream() throws Exception {
		if (Object.DataStreamComponents == null)
			return; //. >
		TComponentDescriptor DataStreamComponent = Object.DataStreamComponents[0]; 
		TComponentFunctionality CF = Server.User.Space.TypesSystem.TComponentFunctionality_Create(DataStreamComponent.idTComponent, DataStreamComponent.idComponent);
		try {
			TComponentFunctionality.TPropsPanel PropsPanel = CF.TPropsPanel_Create(this);
			if (PropsPanel != null)
				startActivity(PropsPanel.PanelActivity);
		}
		finally {
			CF.Release();
		}
	}
}
