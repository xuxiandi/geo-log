package com.geoscope.GeoEye;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
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
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerArchive;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerVideoPhoneCallPanel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerViewer;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels.TGMO1GeoLogAndroidBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;

@SuppressLint("HandlerLeak")
public class TReflectorCoGeoMonitorObjectPanel extends Activity {

	public static final int MESSAGE_UPDATE = 1;
	
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
    	private byte[] 			ObjectData = null;
    	private TObjectModel	ObjectModel = null;
    	
        private ProgressDialog progressDialog; 
    	
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
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				ObjectData = TReflectorCoGeoMonitorObjectPanel.this.Object.GetData(0);
	    				//.
	    				byte[] ObjectModelData = TReflectorCoGeoMonitorObjectPanel.this.Object.GetData(1000001);
	    				if (ObjectModelData != null) {
	    					int Idx = 0;
	    					int ObjectModelID = TDataConverter.ConvertBEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
	    					int BusinessModelID = TDataConverter.ConvertBEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
	    					//.
	    					if (ObjectModelID != 0) {
	    						ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
	    						if (ObjectModel != null) {
	    							TGeographServerClient GSC = TReflectorCoGeoMonitorObjectPanel.this.Object.GeographServerClient();
	    							synchronized (GSC) {
	    								boolean flKeepConnectionLast = GSC.KeepConnection();
	    								try {
		    								GSC.Connect();
		    								try {
		    									byte[] ObjectSchemaData = GSC.Component_ReadAllCUAC(new int[] {1}/*object side*/);
		    									if (ObjectSchemaData != null)
		    										ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
		    									//.
		    									byte[] ObjectDeviceSchemaData = GSC.Component_ReadAllCUAC(new int[] {2/*device side*/});
		    									if (ObjectDeviceSchemaData != null)
		    										ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
		    								}
	    									finally {
			    								GSC.Disconnect();
	    									}
	    								}
	    								finally {
	    									GSC.Connection_flKeepAlive = flKeepConnectionLast;
	    								}
									}
	    							//.
	    							ObjectModel.SetBusinessModel(BusinessModelID);
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
		                Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, Reflector.getString(R.string.SErrorOfLoadingObjectData)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TReflectorCoGeoMonitorObjectPanel.this.ObjectData = ObjectData;
		            	//.
	    				if (TReflectorCoGeoMonitorObjectPanel.this.ObjectModel != null) {
	    					TReflectorCoGeoMonitorObjectPanel.this.ObjectModel.Destroy();
	    					TReflectorCoGeoMonitorObjectPanel.this.ObjectModel = null;
	    				}
		            	TReflectorCoGeoMonitorObjectPanel.this.ObjectModel = ObjectModel;
		            	//.
		            	TReflectorCoGeoMonitorObjectPanel.this._Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TReflectorCoGeoMonitorObjectPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TReflectorCoGeoMonitorObjectPanel.this);    
		            	progressDialog.setMessage(Reflector.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TReflectorCoGeoMonitorObjectPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, Reflector.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TReflectorCoGeoMonitorObjectPanel.this.finish();
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
    
	public boolean flExists = false;
	//.
	private TReflector Reflector;
	private int								ObjectIndex = -1;
	private TReflectorCoGeoMonitorObject 	Object = null;
	private byte[] 				ObjectData = null;
	private TObjectModel		ObjectModel = null;
	private TUpdating 			Updating = null;
	private Timer 				Updater = null;

	public boolean 	flVisible = false;
	
	private EditText edGMOName;
	private EditText edGMOConnectionState;
	private EditText edGMOLocationState;
	private EditText edGMOAlertState;
	private Button btnGMOUpdateInfo;
	private Button btnGMOShowPosition;
	private Button btnGMOAddTrack;
	//.
	private int AddTrack_Date_Year;
	private int AddTrack_Date_Month;
	private int AddTrack_Date_Day;
	private int AddTrack_Color;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.GetReflector();
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ObjectIndex = extras.getInt("Index");
        	Object = Reflector.CoGeoMonitorObjects.Items[ObjectIndex]; 
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.reflector_gmo_panel);
        //.
        edGMOName = (EditText)findViewById(R.id.edGMOName);
        edGMOConnectionState = (EditText)findViewById(R.id.edGMOConnectionState);
        edGMOLocationState = (EditText)findViewById(R.id.edGMOLocationState);
        edGMOAlertState = (EditText)findViewById(R.id.edGMOAlertState);
        //.
        btnGMOUpdateInfo = (Button)findViewById(R.id.btnGMOUpdateInfo);
        btnGMOUpdateInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Update(true,false);
            }
        });
        //.
        btnGMOShowPosition = (Button)findViewById(R.id.btnGMOShowPosition);
        btnGMOShowPosition.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ShowCurrentPosition();
            	finish();
            }
        });
        //.
        btnGMOAddTrack = (Button)findViewById(R.id.btnGMOAddTrack);
        btnGMOAddTrack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddTrack();
            }
        });
        //.
        flExists = true;
        //.
        _Update();
        Update(true,true);
        //.
        int UpdateInterval = Reflector.CoGeoMonitorObjects.GetUpdateInterval()*1000;
        Updater = new Timer();
        Updater.schedule(new TUpdaterTask(this),UpdateInterval,UpdateInterval);
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		if (ObjectModel != null) {
			ObjectModel.Destroy();
			ObjectModel = null;
		}
		if (Updater != null) {
			Updater.cancel();
			Updater = null;
		}
		if (Updating != null) 
			Updating.Cancel();
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
	
	private void Update(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
		if (Updating != null)
			return; //. ->
		Updating = new TUpdating(pflShowProgress,pflClosePanelOnCancel);
	}
	
	private void _Update() {
		try {
			edGMOName.setText(Object.LabelText);
			if (ObjectData != null) {
				boolean IsOnline = (ObjectData[0] > 0);
				boolean FixIsAvailable = (ObjectData[1] > 0);
				int UserAlert = TDataConverter.ConvertBEByteArrayToInt32(ObjectData,2);
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
			_UpdateObjectModelPanel();
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, getString(R.string.SErrorOfUpdatingObjectInfo)+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private boolean flUpdatingObjectModelPanel = false;
	
	private void _UpdateObjectModelPanel() {
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
						//.
						final TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent DC = (TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
						//.
						edBatteryCharge.setText(Short.toString(DC.BatteryModule.Charge.GetValue())+" %");
						edBatteryVoltage.setText(Double.toString(DC.BatteryModule.Voltage.GetValue()/100.0)+" v");
						edConnectorSignal.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Signal.GetValue())+" %");
						edConnectorAccount.setText(Short.toString(DC.ConnectionModule.ServiceProvider.Account.GetValue()));
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {
									
									@SuppressWarnings("unused")
									public void Process0() throws Exception {
										int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+3/*Set VideoRecorderModule.Mode*/;
										byte[] Data = TDataConverter.ConvertInt16ToBEByteArray(Mode);
										Object.SetData(DataType, Data);
									}		
									
									@Override
									public void Process() throws Exception {
										TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value();
										Value.SetValue(OleDate.UTCCurrentTimestamp(), Mode);
										Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Mode.GetAddressArray(), Value.ToByteArray());
									}
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
						cbVideoRecorderRecording.setOnClickListener(new OnClickListener(){
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {

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
											Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Recording.GetAddressArray(), Value.ToByteArray()); //. Active = true
											Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Active.GetAddressArray(), Value.ToByteArray());
										}
										else {
											Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Active.GetAddressArray(), Value.ToByteArray());
											Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Recording.GetAddressArray(), Value.ToByteArray()); //. Active = true
										}
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderActive.setChecked(DC.VideoRecorderModule.Active.BooleanValue());
						cbVideoRecorderSaving.setChecked(DC.VideoRecorderModule.Saving.BooleanValue());
						cbVideoRecorderSaving.setOnClickListener(new OnClickListener(){
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {
									
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
										Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Saving.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderTransmitting.setChecked(DC.VideoRecorderModule.Transmitting.BooleanValue());
						cbVideoRecorderTransmitting.setOnClickListener(new OnClickListener(){
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {

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
										Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Transmitting.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderAudio.setChecked(DC.VideoRecorderModule.Audio.BooleanValue());
						cbVideoRecorderAudio.setOnClickListener(new OnClickListener(){
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {
									
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
										Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Audio.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						cbVideoRecorderVideo.setChecked(DC.VideoRecorderModule.Video.BooleanValue());
						cbVideoRecorderVideo.setOnClickListener(new OnClickListener(){
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
								TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectPanel.this) {

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
										Object.GeographServerClient().Component_WriteDeviceCUAC(DC.VideoRecorderModule.Video.GetAddressArray(), Value.ToByteArray());
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										Update(true,false);
									}
									
									@Override
									public void DoOnException(Exception E) {
			                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Processing.Start();
				            }
				        });        
						btnShowVideoRecorderVideoPhone.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
					            Intent intent = new Intent(TReflectorCoGeoMonitorObjectPanel.this, TVideoRecorderServerVideoPhoneCallPanel.class);
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
									ServersInfo = Reflector.Server.Info.GetInfo();
									if (!ServersInfo.IsGeographProxyServerValid()) 
										throw new Exception(TReflectorCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographProxyServer)); //. =>
								} catch (Exception E) {
							    	Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
					            Intent intent = new Intent(TReflectorCoGeoMonitorObjectPanel.this, TVideoRecorderServerViewer.class);
			    	        	intent.putExtra("Name",Object.Name);
			    	        	intent.putExtra("GeographProxyServerAddress",ServersInfo.GeographProxyServerAddress);
			    	        	intent.putExtra("GeographProxyServerPort",ServersInfo.GeographProxyServerPort);
			    	        	intent.putExtra("UserID",Reflector.Server.User.UserID);
			    	        	intent.putExtra("UserPassword",Reflector.Server.User.UserPassword);
			    	        	intent.putExtra("ObjectIndex",ObjectIndex);
			    	        	intent.putExtra("flAudio",DC.VideoRecorderModule.Audio.BooleanValue());
			    	        	intent.putExtra("flVideo",DC.VideoRecorderModule.Video.BooleanValue());
					            startActivity(intent);
							}
						});
						btnShowVideoRecorderViewer.setEnabled((DC.VideoRecorderModule.Mode.GetValue() == TVideoRecorderModule.MODE_FRAMESTREAM) && DC.VideoRecorderModule.Active.BooleanValue());
						btnShowVideoRecorderArchive.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								TGeoScopeServerInfo.TInfo ServersInfo;
					    		try {
									ServersInfo = Reflector.Server.Info.GetInfo();
									if (!ServersInfo.IsGeographDataServerValid()) 
										throw new Exception(TReflectorCoGeoMonitorObjectPanel.this.getString(R.string.SInvalidGeographDataServer)); //. =>
								} catch (Exception E) {
							    	Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
							    	return; //. ->
								}
					            Intent intent = new Intent(TReflectorCoGeoMonitorObjectPanel.this, TVideoRecorderServerArchive.class);
			    	        	intent.putExtra("GeographDataServerAddress",ServersInfo.GeographDataServerAddress);
			    	        	intent.putExtra("GeographDataServerPort",ServersInfo.GeographDataServerPort);
			    	        	intent.putExtra("UserID",Reflector.Server.User.UserID);
			    	        	intent.putExtra("UserPassword",Reflector.Server.User.UserPassword);
			    	        	intent.putExtra("ObjectIndex",ObjectIndex);
					            startActivity(intent);
								//. TGettingCurrentLocation GCL = new TGettingCurrentLocation(ServersInfo.GeographProxyServerAddress,ServersInfo.GeographProxyServerPort, Reflector.Server.User.UserID,Reflector.Server.User.UserPassword, Reflector.CoGeoMonitorObjects.Items[ObjectIndex]);
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
	
	public void ShowCurrentPosition() {
		try {
			TXYCoord C = Object.GetComponentLocation(this);
			Reflector.MoveReflectionWindow(C);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, getString(R.string.SErrorOfGettingCurrentGMOPosition)+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private void AddTrack() {
		Calendar c = Calendar.getInstance();
		DatePickerDialog DateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {                
			@Override
			public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
				AddTrack_Date_Year = year;
				AddTrack_Date_Month = monthOfYear+1;
				AddTrack_Date_Day = dayOfMonth;
				//.
        		ColorPicker ColorDialog = new ColorPicker(TReflectorCoGeoMonitorObjectPanel.this, new ColorPicker.OnColorChangedListener() {
        			@Override
        			public void colorChanged(int color) {
        				AddTrack_Color = color;
        				//.
                		OleDate Day = new OleDate(AddTrack_Date_Year,AddTrack_Date_Month,AddTrack_Date_Day, 0,0,0);
                		try {
                			AddTrack(Object.ID,Day.toDouble(),AddTrack_Color);
                		}
                		catch (Exception E) {
                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, getString(R.string.SErrorOfObjectTrackAdding)+E.getMessage(), Toast.LENGTH_SHORT).show();
                		}
        			}
        		},Color.RED);    
        		ColorDialog.show();
			}
		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
		DateDialog.show();
	}
	
	private void AddTrack(int pObjectID, double pTrackDay, int pTrackColor) {
		final int ObjectID = pObjectID;
		final double TrackDay = pTrackDay;
		final int TrackColor = pTrackColor;
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SLoadingTheObjectTrack)) {
			
			private byte[] TrackData;
			@Override
			public void Process() throws Exception {
				TrackData = Reflector.ObjectTracks.GetTrackData(ObjectID,TrackDay,TrackColor);
			}
			@Override 
			public void DoOnCompleted() throws Exception {
    			Reflector.ObjectTracks.AddNewTrack(TrackData,ObjectID,TrackDay,TrackColor);
    			//.
    			Reflector.StartUpdatingSpaceImage();
    			//.
    			TReflectorCoGeoMonitorObjectPanel.this.finish();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
    private final Handler UpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATE:
                	if (flVisible)
                		Update(false,false);  
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };

    private class TUpdaterTask extends TimerTask {
    	
        private TReflectorCoGeoMonitorObjectPanel _Panel;
        
        public TUpdaterTask(TReflectorCoGeoMonitorObjectPanel pPanel) {
            _Panel = pPanel;
        }
        
        @Override
		public void run() {
        	try {
            	_Panel.UpdateHandler.obtainMessage(TReflectorCoGeoMonitorObjectPanel.MESSAGE_UPDATE).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }   
}
