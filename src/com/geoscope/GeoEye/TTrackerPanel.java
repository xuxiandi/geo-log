package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

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
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectPanel;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.TUserAccess;
import com.geoscope.GeoLog.DEVICE.AudioModule.VoiceCommandModule.TVoiceCommandModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIJPEGImageSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOITextSO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIDataFileValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIImageValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOITextValue;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule.TUSBPluginModuleConsole;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.TSensorsMetersPanel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPropsPanel;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerFRAME0;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint({"HandlerLeak"})
public class TTrackerPanel extends Activity {

	private static final int CONTROL_METHOD_DEFAULT	= 0;
	private static final int CONTROL_METHOD_VOICE	= 1;
	private static final int CONTROL_METHOD_TAP		= 2;
	
	private static final int CONTROL_COMMAND_SENSORSMODULE_METER_START	= 1;
	private static final int CONTROL_COMMAND_SENSORSMODULE_METER_FINISH = 2;
	
	private static final int UPDATE_CONFIGURATION			= 1;
	private static final int SHOW_NEWPOIPANEL				= 2;
	private static final int SHOW_LASTPOICAMERA 			= 3;
	private static final int SHOW_LASTPOICAMERAANDEDITOR	= 4;
	private static final int SHOW_LASTPOITEXTEDITOR			= 5;
	private static final int SHOW_LASTPOIVIDEOCAMERA		= 6;
	private static final int SHOW_LASTPOIVIDEOCAMERA1 		= 7;
	private static final int SHOW_LASTPOIDRAWINGEDITOR 		= 8;
	private static final int SHOW_LASTPOIIMAGEDRAWINGEDITOR = 9;
	//.
	private static final int POI_SUBMENU_NEWPOI 	= 101; 
	private static final int POI_SUBMENU_ADDTEXT 	= 102; 
	private static final int POI_SUBMENU_ADDIMAGE 	= 103; 
	private static final int POI_SUBMENU_ADDVIDEO 	= 104;
	//.
	private static final int POI_DATAFILE_TYPE_TEXT 		= 1;
	private static final int POI_DATAFILE_TYPE_IMAGE 		= 2;
	private static final int POI_DATAFILE_TYPE_EDITEDIMAGE	= 3;
	private static final int POI_DATAFILE_TYPE_VIDEO 		= 4;
	private static final int POI_DATAFILE_TYPE_DRAWING 		= 5;
	private static final int POI_DATAFILE_TYPE_FILE 		= 6;
    
	public static final int UpdatingInterval = 1000*5; //. seconds
	
	public static final int SensorsModule_ControlMeter_RecordingNotifying_Interval = 1000*5; //. seconds 
	
	public static final int 	Lock_Timeout 		= 1000*30; //. seconds
	public static final int 	Lock_DialogTimeout 	= 1000*5; //. seconds
	//.
	public static final float 	Lock_LowBrightness = 0.01F; 
	
	public static class TControlDescriptor {
		
		public int Command;
		public int Method;
		
		public TControlDescriptor(int pCommand, int pMethod) {
			Command = pCommand;
			Method = pMethod;
		}
		
		public TControlDescriptor(int pCommand) {
			this(pCommand,CONTROL_METHOD_DEFAULT);
		}
	}
	
	public static class TConfiguration {
	
		public static String FileName = "TrackerConfiguration.xml";
		
		
		public static final int CONTROL_NONE 					= 0;
		public static final int CONTROL_2TAPSSTARTMANUALSTOP 	= 1;
		public static final int CONTROL_2TAPSSTART3TAPSSTOP 	= 2;
		
		public static final int NOTIFICATIONS_NONE 						= 0;
		public static final int NOTIFICATIONS_VIBRATION2BITS 			= 1;
		public static final int NOTIFICATIONS_VOICE 					= 2;
		public static final int NOTIFICATIONS_VIBRATION2BITSANDVOICE	= 3;
		
		public static class TGPSModuleConfiguration {
			
			public static final int MapPOI_DataNameMaxSize = 100;
			
			
			public boolean MapPOI_flDataName = false;
		}
		
		public static class TSensorsModuleConfiguration {
			
			public static final String VideoRecorderModuleMeterID = "VideoRecorderModule";
			
			public static final int METERRECORDING_NOTIFICATION_NONE 		= 0;
			public static final int METERRECORDING_NOTIFICATION_BEEPING 	= 1;
			public static final int METERRECORDING_NOTIFICATION_VIBRATING 	= 2;
			
			
			public String	MeterToControl = VideoRecorderModuleMeterID;
			public int 		MeterControl = CONTROL_2TAPSSTART3TAPSSTOP;
			public int 		MeterControlNotifications = NOTIFICATIONS_VOICE;
			public int 		MeterRecordingNotification = METERRECORDING_NOTIFICATION_NONE;
		}
		
		
		public String ConfigurationFileName;
		//.
		public TGPSModuleConfiguration		GPSModuleConfiguration = new TGPSModuleConfiguration();
		public TSensorsModuleConfiguration 	SensorsModuleConfiguration = new TSensorsModuleConfiguration();
		//.
		public boolean flChanged = false;
		
		public TConfiguration() {
			ConfigurationFileName = TReflectorComponent.ProfileFolder()+"/"+FileName;
		}
		
		public void Load() throws Exception {
			//. defaults
			GPSModuleConfiguration.MapPOI_flDataName = false;
			//.
			SensorsModuleConfiguration.MeterControl = TConfiguration.CONTROL_2TAPSSTART3TAPSSTOP;
			SensorsModuleConfiguration.MeterControlNotifications = TConfiguration.NOTIFICATIONS_VOICE;
			//.
			File F = new File(ConfigurationFileName);
			if (F.exists()) {
				if (F.length() == 0)
					return; //. ->
				byte[] BA;
		    	FileInputStream FIS = new FileInputStream(F);
		    	try {
	    			BA = new byte[(int)F.length()];
	    			FIS.read(BA);
		    	}
				finally
				{
					FIS.close(); 
				}
				//.
		    	Document XmlDoc;
				ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
					factory.setNamespaceAware(true);     
					DocumentBuilder builder = factory.newDocumentBuilder(); 			
					XmlDoc = builder.parse(BIS); 
				}
				finally {
					BIS.close();
				}
				Element RootNode = XmlDoc.getDocumentElement();
				int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
				switch (Version) {
				case 1:
					try {
						Node DeviceNode = TMyXML.SearchNode(RootNode,"DEVICE");
						//.
						Node ModuleNode = TMyXML.SearchNode(DeviceNode,"GPSModule");
						if (ModuleNode != null) {
							Node Node = TMyXML.SearchNode(ModuleNode,"MapPOI");
							GPSModuleConfiguration.MapPOI_flDataName = (Integer.parseInt(TMyXML.SearchNode(Node,"DataName").getFirstChild().getNodeValue()) != 0);
						}
						//.
						ModuleNode = TMyXML.SearchNode(DeviceNode,"SensorsModule");
						if (ModuleNode != null) {
							Node ControlNode = TMyXML.SearchNode(ModuleNode,"Control");
							if (ControlNode != null) {
								SensorsModuleConfiguration.MeterToControl = TMyXML.SearchNode(ControlNode,"Meter").getFirstChild().getNodeValue();
								//.
								SensorsModuleConfiguration.MeterControl = Integer.parseInt(TMyXML.SearchNode(ControlNode,"ID").getFirstChild().getNodeValue());
								//.
								SensorsModuleConfiguration.MeterControlNotifications = Integer.parseInt(TMyXML.SearchNode(ControlNode,"Notifications").getFirstChild().getNodeValue());
								//.
								Node node = TMyXML.SearchNode(ControlNode,"RecordingNotification");
								if (node != null)
									SensorsModuleConfiguration.MeterRecordingNotification = Integer.parseInt(node.getFirstChild().getNodeValue());
							}
						}
					}
					catch (Exception E) {
		    			throw new Exception("error of configuration data parsing: "+E.getMessage()); //. =>
					}
					break; //. >
				default:
					throw new Exception("unknown configuration data version, version: "+Integer.toString(Version)); //. =>
				}
			}
			//.
			flChanged = false;
		}

		public void Save() throws Exception {
            File F = new File(ConfigurationFileName);
    	    String TFN = ConfigurationFileName+".tmp";
        	int Version = 1;
    	    XmlSerializer serializer = Xml.newSerializer();
    	    FileWriter writer = new FileWriter(TFN);
    	    try {
    	        serializer.setOutput(writer);
    	        serializer.startDocument("UTF-8",true);
    	        serializer.startTag("", "ROOT");
    	        //.
                serializer.startTag("", "Version");
                serializer.text(Integer.toString(Version));
                serializer.endTag("", "Version");
    	        //. Device
                serializer.startTag("", "DEVICE");
    	        //. GPSModule
                serializer.startTag("", "GPSModule");
    	        //.
                serializer.startTag("", "MapPOI");
    	        //.
                serializer.startTag("", "DataName");
                int V = 0;
                if (GPSModuleConfiguration.MapPOI_flDataName)
                	V = 1;
                serializer.text(Integer.toString(V));
                serializer.endTag("", "DataName");
                //.
                serializer.endTag("", "MapPOI");
                //.
                serializer.endTag("", "GPSModule");
    	        //. 
                serializer.startTag("", "SensorsModule");
    	        //.
                serializer.startTag("", "Control");
    	        //.
                serializer.startTag("", "Meter");
                serializer.text(SensorsModuleConfiguration.MeterToControl);
                serializer.endTag("", "Meter");
    	        //.
                serializer.startTag("", "ID");
                serializer.text(Integer.toString(SensorsModuleConfiguration.MeterControl));
                serializer.endTag("", "ID");
    	        //.
                serializer.startTag("", "Notifications");
                serializer.text(Integer.toString(SensorsModuleConfiguration.MeterControlNotifications));
                serializer.endTag("", "Notifications");
                //.
                serializer.startTag("", "RecordingNotification");
                serializer.text(Integer.toString(SensorsModuleConfiguration.MeterRecordingNotification));
                serializer.endTag("", "RecordingNotification");
                //.
                serializer.endTag("", "Control");
                //.
                serializer.endTag("", "SensorsModule");
                //.
                serializer.endTag("", "DEVICE");
                //.
    	        serializer.endTag("", "ROOT");
    	        serializer.endDocument();
    	    }
    	    finally {
    	    	writer.close();
    	    }
    		File TF = new File(TFN);
    		TF.renameTo(F);
    		//.
    		flChanged = false;
		}
	}
	
    public static class TCurrentFixObtaining extends TCancelableThread {

    	public static class TDoOnFixIsObtainedHandler {
    		
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    		}
    	}
    	
    	private static final int MESSAGE_EXCEPTION = 0;
    	private static final int MESSAGE_COMPLETED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private class TObtainProgressor extends TProgressor {
    		@Override
    		public synchronized boolean DoOnProgress(int Percentage) {
				if (super.DoOnProgress(Percentage)) {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,this.Percentage).sendToTarget();
					return true; //. ->
				}
				else 
					return false; //. -> 
    		}
    	}
    	
    	public Context context;
    	//.
    	private TGPSModule GPSModule;
    	//.
    	private TDoOnFixIsObtainedHandler DoOnFixIsObtainedHandler;
    	//. 
    	private TGPSFixValue CurrentFix = null;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TCurrentFixObtaining(Context pcontext, TGPSModule pGPSModule, TDoOnFixIsObtainedHandler pDoOnFixIsObtainedHandler) {
    		super();
    		//.
    		context = pcontext;
    		GPSModule = pGPSModule;
    		DoOnFixIsObtainedHandler = pDoOnFixIsObtainedHandler;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				CurrentFix = GPSModule.ObtainCurrentFix(Canceller, new TObtainProgressor(), true);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMPLETED,CurrentFix).sendToTarget();
        	}
        	catch (InterruptedException E) {
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
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, context.getString(R.string.SErrorOfGettingCoordinates)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
						//.
		            	TGPSFixValue Fix = (TGPSFixValue)msg.obj;
		            	if (DoOnFixIsObtainedHandler != null)
		            		DoOnFixIsObtainedHandler.DoOnFixIsObtained(Fix);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SCoordinatesGettingForNow));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if (progressDialog.isShowing()) 
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
    
    public static class TCurrentPositionObtaining extends TCancelableThread {

    	public static class TDoOnPositionIsObtainedHandler {
    		
    		public void DoOnPositionIsObtained(TXYCoord Crd) {
    		}
    	}
    	
    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_COMPLETED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private TTrackerPanel context;
    	private TGPSModule GPSModule;
    	private TReflectorComponent Reflector;
    	private TDoOnPositionIsObtainedHandler DoOnPositionIsObtainedHandler;
        private ProgressDialog progressDialog; 
    	
    	public TCurrentPositionObtaining(TTrackerPanel pcontext, TGPSModule pGPSModule, TReflectorComponent pReflector, TDoOnPositionIsObtainedHandler pDoOnPositionIsObtainedHandler) {
    		super();
    		//.
    		context = pcontext;
    		GPSModule = pGPSModule;
    		Reflector = pReflector;
    		DoOnPositionIsObtainedHandler = pDoOnPositionIsObtainedHandler;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			TXYCoord Crd;
    			try {
    		    	TGPSFixValue Fix;
    				Fix = GPSModule.GetCurrentFix();
    				if (!Fix.IsSet()) 
    					throw new Exception(context.getString(R.string.SCurrentPositionIsUnavailable)); //. =>
    				if (Fix.IsEmpty()) 
    					throw new Exception(context.getString(R.string.SCurrentPositionIsUnknown)); //. =>
    				Crd = Reflector.ConvertGeoCoordinatesToXY(TGPSModule.DatumID, Fix.Latitude,Fix.Longitude,Fix.Altitude);
    				//.
    				Thread.sleep(100);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMPLETED,Crd).sendToTarget();
        	}
        	catch (IOException E) {
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
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
						//.
		            	TXYCoord Crd = (TXYCoord)msg.obj;
		            	if (DoOnPositionIsObtainedHandler != null)
		            		DoOnPositionIsObtainedHandler.DoOnPositionIsObtained(Crd);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SWaitAMoment));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(false);
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
		                if (context.flVisible && progressDialog.isShowing()) 
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
    private TTracker Tracker;
    //.
	private Timer Updater;
	private LinearLayout llMainScreen;
	private LinearLayout llLockScreen;
	private TextView lbTitle;
	private ToggleButton tbTrackerIsOn;
	private Button btnLock;
    private EditText edFix;
    private EditText edFixSpeed;
    private EditText edFixPrecision;
    private Button btnObtainCurrentFix;
    private Button btnInterfacePanel;	
    private Button btnShowLocation;	
    private Button btnNewPOI;	
	private CheckBox cbPOIDataName;
    private Button btnAddPOIText;	
    private Button btnAddPOIImage;	
    private Button btnAddPOIVideo;	
    private Button btnAddPOIDrawing;	
    private Button btnAddPOIFile;	
	private ToggleButton tbAlarm;
    private Button btnSensorsModuleChannelsPanel;
    private Button btnSensorsModuleMetersPanel;
    private Button btnSensorsModuleMeasurementsArchive;
    private Button btnSensorsModuleMeasurementsTransferProcessPanel;	
    private CheckBox cbVideoRecorderModuleRecording;
    private Button btnVideoRecorderModulePanel;	
    private CheckBox cbDataStreamerModuleActive;
    private Button btnDataStreamerModulePanel;	
    private EditText edConnectorInfo;
    private Button btnConnectorCommands;	
    private EditText edCheckpoint;
    private EditText edOpQueueTransmitInterval;
    private EditText edPositionReadInterval;
    private CheckBox cbIgnoreImpulseModeSleepingOnMovement;
    private EditText edGeoThreshold;
    private EditText edOpQueue;
    private Button btnOpQueueCommands;	
    private EditText edComponentFileStreaming;
    private ProgressBar pbComponentFileStreaming;
    private Button btnComponentFileStreamingCommands;
    //.
	private TReflectorComponent Component = null;
    //.
    private boolean flVisible = false;
    //.
    private TConfiguration Configuration;
    //.
    private boolean 		flLocked = false;
    private TLockTimer 		Lock_Timer = null;
	private float			Lock_DefaultBrightness;
	private AlertDialog		Lock_Dialog = null;
    //.
    private TVoiceCommandModule.TCommandHandler VoiceCommandHandler = null;
    private TAsyncProcessing 					VoiceCommandHandler_Initializing = null;
	private Vibrator 							Vibrator;
	//.
	private TMovementDetectorModule.THittingDetector HittingDetector = null;
	//.
	private TSensorsModule.TAudioNotifier 		SensorsModule_AudioNotifier = null;
	private boolean 							SensorsModule_ControlMeter_flActive = false;
	private Timer								SensorsModule_ControlMeter_RecordingBeeping = null;
	//.
	private TVideoRecorderModule.TAudioNotifier VideoRecorderModule_AudioNotifier = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
    	Tracker = TTracker.GetTracker();
    	if (Tracker == null) {
			Toast.makeText(this, R.string.STrackerIsNotInitialized, Toast.LENGTH_LONG).show();  						
    		finish();
    		return; //. ->
    	}
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
    	//.
		if (ViewConfiguration.get(this).hasPermanentMenuKey())  
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//.
        setContentView(R.layout.tracker_panel);
        //.
        Configuration = new TConfiguration();
        try {
			Configuration.Load();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
		}
        //.
        llMainScreen = (LinearLayout)findViewById(R.id.llMainScreen);
        llLockScreen = (LinearLayout)findViewById(R.id.llLockScreen);
        //.
        tbTrackerIsOn = (ToggleButton)findViewById(R.id.tbTrackerIsOn);
        tbTrackerIsOn.setTextOn(getString(R.string.STrackerIsON));
        tbTrackerIsOn.setTextOff(getString(R.string.STrackerIsOff));
        tbTrackerIsOn.setChecked(TTracker.TrackerIsEnabled());
        tbTrackerIsOn.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View arg0) {
				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
			}
		});
        tbTrackerIsOn.setOnLongClickListener(new OnLongClickListener() {
        	
			@Override
			public boolean onLongClick(View arg0) {
				final ToggleButton TB = (ToggleButton)arg0;
				if (TUserAccess.UserAccessFileExists()) {
					final TUserAccess AR = new TUserAccess();
					if (AR.AdministrativeAccessPassword != null) {
				    	AlertDialog.Builder alert = new AlertDialog.Builder(TTrackerPanel.this);
				    	//.
				    	alert.setTitle("");
				    	alert.setMessage(R.string.SEnterPassword);
				    	//.
				    	final EditText input = new EditText(TTrackerPanel.this);
				    	alert.setView(input);
				    	//.
				    	alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
				    		
				    		@Override
				        	public void onClick(DialogInterface dialog, int whichButton) {
				    			//. hide keyboard
				        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				        		//.
				        		String Password = input.getText().toString();
				        		if (Password.equals(AR.AdministrativeAccessPassword)) {
				    				EnableDisableTrackerByButton(TB);
				        		}
				        		else
				        			Toast.makeText(TTrackerPanel.this, R.string.SIncorrectPassword, Toast.LENGTH_LONG).show();  						
				          	}
				    	});
				    	//.
				    	alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
				    		
				    		@Override
				    		public void onClick(DialogInterface dialog, int whichButton) {
				    			//. hide keyboard
				        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				    		}
				    	});
				    	//.
				    	alert.show();   
						//.
						return true; // . >
					}
				}
				EnableDisableTrackerByButton(TB);
    	    	return true; //. ->
			}
		});
        btnLock = (Button)findViewById(R.id.btnLock);
        btnLock.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
            	try {
					Lock();
				} catch (InterruptedException IE) {
				}
            }
        });
        //.
        lbTitle = (TextView)findViewById(R.id.lbTitle);
        edFix = (EditText)findViewById(R.id.edFix);
        edFixSpeed = (EditText)findViewById(R.id.edFixSpeed);
        edFixPrecision = (EditText)findViewById(R.id.edFixPrecision);
        btnObtainCurrentFix = (Button)findViewById(R.id.btnObtainCurrentFix);
        btnObtainCurrentFix.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
            	StartObtainingCurrentFix();
            }
        });
        btnInterfacePanel = (Button)findViewById(R.id.btnInterfacePanel);
        btnInterfacePanel.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
	            	ShowInterfacePanel();
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, S, Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnShowLocation = (Button)findViewById(R.id.btnShowLocation);
        btnShowLocation.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
            	StartObtainingCurrentPosition();
            }
        });
        btnNewPOI = (Button)findViewById(R.id.btnNewPOI);
        btnNewPOI.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIPanel.class);
                startActivityForResult(intent,SHOW_NEWPOIPANEL);
            }
        });
        cbPOIDataName = (CheckBox)findViewById(R.id.cbPOIDataName);
        cbPOIDataName.setChecked(Configuration.GPSModuleConfiguration.MapPOI_flDataName);
        cbPOIDataName.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
                //.
                Configuration.GPSModuleConfiguration.MapPOI_flDataName = checked;
                Configuration.flChanged = true;
            }
        });        
        btnAddPOIText = (Button)findViewById(R.id.btnAddPOIText);
        btnAddPOIText.setEnabled(false);
        btnAddPOIText.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOITextPanel.class);
                startActivityForResult(intent,SHOW_LASTPOITEXTEDITOR);
            }
        });
        btnAddPOIImage = (Button)findViewById(R.id.btnAddPOIImage);
        btnAddPOIImage.setEnabled(false);
        btnAddPOIImage.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetImageTempFile(TTrackerPanel.this))); 
      		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
            }
        });
        btnAddPOIImage.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				final CharSequence[] _items;
				_items = new CharSequence[2];
				_items[0] = getString(R.string.SAddImage);
				_items[1] = getString(R.string.STakePictureWithCameraAndEdit);
				AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
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
				      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetImageTempFile(TTrackerPanel.this))); 
				      		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
								break; //. >
								
							case 1: //. take a picture and edit
				      		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetImageTempFile(TTrackerPanel.this))); 
				      		    startActivityForResult(intent, SHOW_LASTPOICAMERAANDEDITOR);    		
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        btnAddPOIVideo = (Button)findViewById(R.id.btnAddPOIVideo);
        btnAddPOIVideo.setEnabled(false);
        btnAddPOIVideo.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
      		    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetVideoTempFile(TTrackerPanel.this))); 
      		    startActivityForResult(intent, SHOW_LASTPOIVIDEOCAMERA1);    		
            }
        });
        btnAddPOIVideo.setOnLongClickListener(new OnLongClickListener() {
        	
			@Override
			public boolean onLongClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIVideoPanel.class);
                startActivityForResult(intent,SHOW_LASTPOIVIDEOCAMERA);
				return true;
			}
        });
        btnAddPOIDrawing = (Button)findViewById(R.id.btnAddPOIDrawing);
        btnAddPOIDrawing.setEnabled(false);
        btnAddPOIDrawing.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				Intent intent = new Intent(TTrackerPanel.this, TDrawingEditor.class);
	    		File F = GetDrawingTempFile(TTrackerPanel.this);
	    		F.delete();
	  		    intent.putExtra("FileName", F.getAbsolutePath()); 
	  		    intent.putExtra("ReadOnly", false); 
	  		    startActivityForResult(intent, SHOW_LASTPOIDRAWINGEDITOR);    		
            }
        });
        btnAddPOIFile = (Button)findViewById(R.id.btnAddPOIFile);
        btnAddPOIFile.setEnabled(false);
        btnAddPOIFile.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TTrackerPanel.this, null, new TFileSystemFileSelector.OpenDialogListener() {
		        	
		            @Override
		            public void OnSelectedFile(String fileName) {
		        		final String SelectedFileName = fileName; 
	                    //.
						try {
			            	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
			            		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
			            			
			            			@Override
			            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
			            	    		EnqueueDataFile(POI_DATAFILE_TYPE_FILE, SelectedFileName,Name);
			            			}
			            		});
			            	else 
			    	    		EnqueueDataFile(POI_DATAFILE_TYPE_FILE, SelectedFileName,null);
						}
						catch (Throwable E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, S, Toast.LENGTH_LONG).show();  						
						}
		            }

					@Override
					public void OnCancel() {
					}
		        });
		    	FileSelector.show();    	
            }
        });
        tbAlarm = (ToggleButton)findViewById(R.id.tbAlarm);
        tbAlarm.setChecked(GetAlarm() > 0);
        tbAlarm.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View arg0) {
				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
			}
		});
        tbAlarm.setOnLongClickListener(new OnLongClickListener() {			
        	
			@Override
			public boolean onLongClick(View arg0) {
    			try {
    				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
    				SetAlarm(((ToggleButton)arg0).isChecked());
    	    	}
    	    	catch (Exception E) {
    	            Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
    	    	}
    	    	return true;
			}
		});
        btnSensorsModuleChannelsPanel = (Button)findViewById(R.id.btnSensorsModuleChannelsPanel);
        btnSensorsModuleChannelsPanel.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
    				Intent intent = new Intent(TTrackerPanel.this, TDataStreamPropsPanel.class);
    		        startActivity(intent);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        }); 
        btnSensorsModuleMetersPanel = (Button)findViewById(R.id.btnSensorsModuleMetersPanel);
        btnSensorsModuleMetersPanel.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
    				Intent intent = new Intent(TTrackerPanel.this, TSensorsMetersPanel.class);
    		        startActivity(intent);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        }); 
        btnSensorsModuleMeasurementsArchive = (Button)findViewById(R.id.btnSensorsModuleMeasurementsArchive);
        btnSensorsModuleMeasurementsArchive.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
    				Intent intent = new Intent(TTrackerPanel.this, TSensorsModuleMeasurementsArchive.class);
    		        startActivity(intent);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        }); 
        btnSensorsModuleMeasurementsTransferProcessPanel = (Button)findViewById(R.id.btnSensorsModuleMeasurementsTransferProcessPanel);
        btnSensorsModuleMeasurementsTransferProcessPanel.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
        			Tracker.GeoLog.SensorsModule.ShowMeasurementsTransferProcessPanel(TTrackerPanel.this);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        });
        cbVideoRecorderModuleRecording =  (CheckBox)findViewById(R.id.cbVideoRecorderModuleRecording);
        cbVideoRecorderModuleRecording.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View arg0) {
				((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			}
		});
        cbVideoRecorderModuleRecording.setOnLongClickListener(new OnLongClickListener() {	
        	
			@Override
			public boolean onLongClick(View arg0) {
            	try {
			    	((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			    	//.	
			    	boolean flSet = ((CheckBox)arg0).isChecked();
			    	//.
					Tracker.GeoLog.VideoRecorderModule.SetRecorderState(flSet, true);
					//.
					if (flSet)
						ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_START,CONTROL_METHOD_DEFAULT);
					else
						ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_FINISH,CONTROL_METHOD_DEFAULT);
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
				}
    	    	return true;
			}
		});
        btnVideoRecorderModulePanel = (Button)findViewById(R.id.btnVideoRecorderModulePanel);
        btnVideoRecorderModulePanel.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
        			Tracker.GeoLog.VideoRecorderModule.ShowPropsPanel(TTrackerPanel.this);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        });
        cbDataStreamerModuleActive =  (CheckBox)findViewById(R.id.cbDataStreamerModuleActive);
        cbDataStreamerModuleActive.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View arg0) {
				((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			}
		});
        cbDataStreamerModuleActive.setOnLongClickListener(new OnLongClickListener() {	
        	
			@Override
			public boolean onLongClick(View arg0) {
            	try {
			    	((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			    	//.	
					Tracker.GeoLog.DataStreamerModule.SetActiveValue(((CheckBox)arg0).isChecked());
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
				}
    	    	return true;
			}
		});
        btnDataStreamerModulePanel = (Button)findViewById(R.id.btnDataStreamerModulePanel);
        btnDataStreamerModulePanel.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
            		if (Tracker.GeoLog.DataStreamerModule.StreamingComponentsCount() == 0) 
            			Tracker.GeoLog.DataStreamerModule.ShowStreamChannelsPanel(TTrackerPanel.this);
            		else
            			Tracker.GeoLog.DataStreamerModule.ShowPropsPanel(TTrackerPanel.this);
            	}
            	catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
            	}
            }
        });
        edConnectorInfo = (EditText)findViewById(R.id.edConnectorInfo);
        btnConnectorCommands = (Button)findViewById(R.id.btnConnectorCommands);
        btnConnectorCommands.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[2];
    			_items[0] = getString(R.string.SReconnect);
    			_items[1] = getString(R.string.SForceReconnect);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					
	    					case 0:
    					    	Tracker.GeoLog.ConnectorModule.Reconnect();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SDone, Toast.LENGTH_SHORT).show();
    	                		break; //. >
    						
	    					case 1:
    					    	Tracker.GeoLog.ConnectorModule.ForceReconnect();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SDone, Toast.LENGTH_SHORT).show();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        edCheckpoint = (EditText)findViewById(R.id.edCheckpoint);
        edOpQueueTransmitInterval = (EditText)findViewById(R.id.edOpQueueTransmitInterval);
        edPositionReadInterval = (EditText)findViewById(R.id.edPositionReadInterval);
        cbIgnoreImpulseModeSleepingOnMovement =  (CheckBox)findViewById(R.id.cbIgnoreImpulseModeSleepingOnMovement);
        cbIgnoreImpulseModeSleepingOnMovement.setOnClickListener(new OnClickListener() {
        	
			@Override
			public void onClick(View arg0) {
				((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			}
		});
        cbIgnoreImpulseModeSleepingOnMovement.setOnLongClickListener(new OnLongClickListener() {	
        	
			@Override
			public boolean onLongClick(View arg0) {
            	try {
			    	((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			    	Tracker.GeoLog.GPSModule.flIgnoreImpulseModeSleepingOnMovement = ((CheckBox)arg0).isChecked();
			    	Tracker.GeoLog.SaveProfile();
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
				}
    	    	return true;
			}
		});
        edGeoThreshold = (EditText)findViewById(R.id.edGeoThreshold);
        edOpQueue = (EditText)findViewById(R.id.edOpQueue);
        btnOpQueueCommands = (Button)findViewById(R.id.btnOpQueueCommands);
        btnOpQueueCommands.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[4];
    			_items[0] = getString(R.string.SContent);
    			_items[1] = getString(R.string.STransmiteImmediately);
    			_items[2] = getString(R.string.SSave);
    			_items[3] = getString(R.string.SClear);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					
	    					case 0:
	    						Intent intent = new Intent(TTrackerPanel.this,TTrackerOSOQueuePanel.class);
	    						TTrackerPanel.this.startActivity(intent);
    	                		break; //. >
    						
	    					case 1:
    					    	Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SImmediateTransmissionStarted, Toast.LENGTH_SHORT).show();
    	                		break; //. >
    						
	    					case 2:
	    					    	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Save();
	    	                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsSavedToDisk, Toast.LENGTH_SHORT).show();
	    						break; //. >
	    						
	    					case 3:
	    		    		    new AlertDialog.Builder(TTrackerPanel.this)
	    		    	        .setIcon(android.R.drawable.ic_dialog_alert)
	    		    	        .setTitle(R.string.SConfirmation)
	    		    	        .setMessage(R.string.SEmptyTheQueue)
	    		    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    		    		    	@Override
	    		    		    	public void onClick(DialogInterface dialog, int id) {
	    		    		    		try {
		        					    	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Clear();
		        					    	Update();
		        	                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsCleared, Toast.LENGTH_SHORT).show();
	    								}
	    								catch (Exception E) {
	    									String S = E.getMessage();
	    									if (S == null)
	    										S = E.getClass().getName();
	    				        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
	    								}
	    		    		    	}
	    		    		    })
	    		    		    .setNegativeButton(R.string.SNo, null)
	    		    		    .show();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        edComponentFileStreaming = (EditText)findViewById(R.id.edComponentFileStreaming);
        pbComponentFileStreaming = (ProgressBar)findViewById(R.id.pbComponentFileStreaming);
        btnComponentFileStreamingCommands = (Button)findViewById(R.id.btnComponentFileStreamingCommands);
        btnComponentFileStreamingCommands.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[5];
    			_items[0] = getString(R.string.SContent);
    			_items[1] = getString(R.string.SSuspend);
    			_items[2] = getString(R.string.SResume);
    			_items[3] = getString(R.string.STransmiteImmediately);
    			_items[4] = getString(R.string.SClear);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					
	    					case 0:
	    						Intent intent = new Intent(TTrackerPanel.this,TTrackerComponentFileStreamingPanel.class);
	    						TTrackerPanel.this.startActivity(intent);
    	                		break; //. >
    						
	    					case 1:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							TAsyncProcessing Processing = new TAsyncProcessing(TTrackerPanel.this,getString(R.string.SWaitAMoment)) {
	    								@Override
	    								public void Process() throws Exception {
	    	    							Tracker.GeoLog.ComponentFileStreaming.SetEnabledStreaming(false);
	    	    							//.
	    	    							Thread.sleep(100);
	    								}
	    								@Override
	    								public void DoOnCompleted() throws Exception {
	        	                			Toast.makeText(TTrackerPanel.this, R.string.SStreamingHasBeenSuspended, Toast.LENGTH_SHORT).show();
	    								}
	    								@Override
	    								public void DoOnException(Exception E) {
	    									Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    								}
	    							};
	    							Processing.Start();
	    						}
    	                		break; //. >
    						
	    					case 2:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							TAsyncProcessing Processing = new TAsyncProcessing(TTrackerPanel.this,getString(R.string.SWaitAMoment)) {
	    								@Override
	    								public void Process() throws Exception {
	    	    							Tracker.GeoLog.ComponentFileStreaming.SetEnabledStreaming(true);
	    	    							//.
	    	    							Thread.sleep(100);
	    								}
	    								@Override
	    								public void DoOnCompleted() throws Exception {
	        	                			Toast.makeText(TTrackerPanel.this, R.string.SStreamingHasBeenResumed, Toast.LENGTH_SHORT).show();
	    								}
	    								@Override
	    								public void DoOnException(Exception E) {
	    									Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    								}
	    							};
	    							Processing.Start();
	    						}
    	                		break; //. >
    						
	    					case 3:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.Process();
    	                			Toast.makeText(TTrackerPanel.this, R.string.SImmediateTransmissionStarted, Toast.LENGTH_SHORT).show();
	    						}
    	                		break; //. >
    						
	    					/*case 4:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.RemoveLastItem();
    					    		UpdateInfo();
	    	                		Toast.makeText(TTrackerPanel.this, R.string.SLastItemHasBeenRemoved, Toast.LENGTH_SHORT).show();
    							}    							
	    						break; //. >*/
	    						
	    					case 4:
	    		    		    new AlertDialog.Builder(TTrackerPanel.this)
	    		    	        .setIcon(android.R.drawable.ic_dialog_alert)
	    		    	        .setTitle(R.string.SConfirmation)
	    		    	        .setMessage(R.string.SEmptyTheQueue)
	    		    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    		    		    	@Override
	    		    		    	public void onClick(DialogInterface dialog, int id) {
	    		    		    		try {
	    		    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    		    							Tracker.GeoLog.ComponentFileStreaming.Clear();
	    		    					    	Update();
	    	    		                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsCleared, Toast.LENGTH_SHORT).show();
	    	    							}    							
	    								}
	    								catch (Exception E) {
	    									String S = E.getMessage();
	    									if (S == null)
	    										S = E.getClass().getName();
	    				        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
	    								}
	    		    		    	}
	    		    		    })
	    		    		    .setNegativeButton(R.string.SNo, null)
	    		    		    .show();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        //.
        EnableDisablePanelItems(TTracker.TrackerIsEnabled());
        //.
        Vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //.
        Initialize();
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
        flExists = true;
	}

    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	Finalize();
    	//.
    	SensorsModule_ControlMeter_RecordingNotifying_Stop();
    	//.
    	Updating_Finish();
    	//.
    	if ((Configuration != null) && Configuration.flChanged) {
    		try {
				Configuration.Save();
			} catch (Exception E) {
    			Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
			}
    		Configuration = null;
    	}
        //.
		super.onDestroy();
	}

    private void Initialize() {
		try {
			if (Tracker.GeoLog.IsEnabled() && TVoiceCommandModule.TRecognizer.Available() && Tracker.GeoLog.AudioModule.VoiceCommandModule.flEnabled)
				VoiceCommandHandler_StartInitializing();
			//.
			if (Tracker.GeoLog.IsEnabled() && Tracker.GeoLog.MovementDetectorModule.HitDetector_flEnabled && ((Configuration.SensorsModuleConfiguration.MeterControl == TConfiguration.CONTROL_2TAPSSTARTMANUALSTOP) || (Configuration.SensorsModuleConfiguration.MeterControl == TConfiguration.CONTROL_2TAPSSTART3TAPSSTOP))) {
		    	HittingDetector = new TMovementDetectorModule.THittingDetector(Tracker.GeoLog.MovementDetectorModule, new TMovementDetectorModule.THittingDetector.TDoOnHitHandler() {

		    		@Override
		    		public void DoOnHit() {
		    			if (TGeoLogApplication.DebugOptions_IsDebugging())
		    				MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,"Tap detected").sendToTarget();
		    		}
		    		
		    		@Override
		    		public void DoOnDoubleHit() {
		    			TAsyncProcessing Processing = new TAsyncProcessing() {
		    				
		    				@Override
		    				public void Process() throws Exception {
			    		    	SensorsModule_ControlMeter_SetActive(true);
								//.
			    				ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_START,CONTROL_METHOD_TAP);
		    				}
		    				
		    				@Override
		    				public void DoOnCompleted() throws Exception {
		    				}
		    				
		    				@Override
		    				public void DoOnException(Exception E) {
		    					String S = E.getMessage();
		    					if (S == null)
		    						S = E.getClass().getName();
		    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
		    				}
		    			};
		    			Processing.Start();
		    		}
		    		
		    		@Override
		    		public void DoOn3Hit() {
		    			if (Configuration.SensorsModuleConfiguration.MeterControl == TConfiguration.CONTROL_2TAPSSTART3TAPSSTOP) {
			    			TAsyncProcessing Processing = new TAsyncProcessing() {
			    				
			    				@Override
			    				public void Process() throws Exception {
			    					SensorsModule_ControlMeter_SetActive(false);
									//.
									ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_FINISH,CONTROL_METHOD_TAP);
			    				}
			    				
			    				@Override
			    				public void DoOnCompleted() throws Exception {
			    				}
			    				
			    				@Override
			    				public void DoOnException(Exception E) {
			    					String S = E.getMessage();
			    					if (S == null)
			    						S = E.getClass().getName();
			    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
			    				}
			    			};
			    			Processing.Start();
		    			}
		    		}
		    	});
		    	//.
    			Toast.makeText(TTrackerPanel.this, R.string.STapControlIsOn, Toast.LENGTH_LONG).show();  						
			}
			//.
			if (Tracker.GeoLog.IsEnabled() && Tracker.GeoLog.IsAudioNotifications() && ((Configuration.SensorsModuleConfiguration.MeterControlNotifications == TConfiguration.NOTIFICATIONS_VOICE) || (Configuration.SensorsModuleConfiguration.MeterControlNotifications == TConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE))) { 
		    	SensorsModule_AudioNotifier = new TSensorsModule.TAudioNotifier(this);
		    	VideoRecorderModule_AudioNotifier = new TVideoRecorderModule.TAudioNotifier();
		    	//.
    			Toast.makeText(TTrackerPanel.this, R.string.SAudioNotificationsAreOn, Toast.LENGTH_LONG).show();  						
			}
		} catch (Exception E) {
			Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    private void Finalize() {
		if (SensorsModule_AudioNotifier != null) {
			SensorsModule_AudioNotifier.Destroy();
			SensorsModule_AudioNotifier = null;
		}
    	//.
		if (VideoRecorderModule_AudioNotifier != null) {
			VideoRecorderModule_AudioNotifier.Destroy();
			VideoRecorderModule_AudioNotifier = null;
		}
    	//.
    	if (HittingDetector != null) {
    		HittingDetector.Destroy();
    		HittingDetector = null;
    	}
    	//.
    	try {
			VoiceCommandHandler_Finalize();
		} catch (InterruptedException IE) {
		}
    }
    
    private void ReInitialize() {
    	Finalize();
    	Initialize();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	//.
        flVisible = true;
        //. start tracker position fixing immediately if it is in impulse mode
    	if ((Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode) 
			Tracker.GeoLog.GPSModule.ProcessImmediately();
        //.
    	WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        Lock_DefaultBrightness = layoutParams.screenBrightness;
        //.
        try {
        	if (flLocked)
        		Lock();
        	else
        		Unlock();
		} catch (InterruptedException IE) {
		}
    }

    @Override
    public void onPause() {
    	super.onPause();
    	//.
    	flVisible = false;
    	//.
    	try {
			Lock_Cancel();
		} catch (InterruptedException IE) {
		}
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tracker_panel_menu, menu);
		//.
		menu.getItem(2/* Debug */).setVisible(TGeoLogApplication.DebugOptions_IsDebugging());
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return (!flLocked);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.CONFIGURATION_MENU:
    		Intent intent = new Intent(this, TTrackerConfigurationPanel.class);
            startActivityForResult(intent,UPDATE_CONFIGURATION);
    		//.
    		return true; //. >
    
        case R.id.LOG_MENU:
    		intent = new Intent(this, TTrackerLogPanel.class);
            startActivity(intent);
    		//.
    		return true; //. >
    
        case R.id.DEBUG_MENU:
    		final CharSequence[] _items;
    		int SelectedIdx = -1;
    		_items = new CharSequence[4];
    		_items[0] = getString(R.string.SPluginsModuleUSBConsole); 
    		_items[1] = getString(R.string.SPluginsModuleBTConsole); 
    		_items[2] = getString(R.string.SPluginsModuleWiFiConsole); 
    		_items[3] = getString(R.string.SVideoRecorderModuleCamera0CodecsConfig); 
    		//.
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.SSelect);
    		builder.setNegativeButton(R.string.SClose,null);
    		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface arg0, int arg1) {
    		    	try {
    		    		switch (arg1) {
    		    		
    		    		case 0: //. PluginsModuleUSBConsole
    		        		Intent intent = new Intent(TTrackerPanel.this, TUSBPluginModuleConsole.class);
    		                startActivity(intent);
        		    		//.
        		    		arg0.dismiss();
        		    		//.
    		    			break; //. >
    		    			
    		    		case 1: //. PluginsModuleBTConsole
    		    			break; //. >
    		    			
    		    		case 2: //. PluginsModuleWIFIConsole
    		    			break; //. >

    		    		case 3: //. VideoRecorderModuleCamera0CodecsConfig
    		    			CameraStreamerFRAME0.flSaveAudioCodecConfig = true;
    		    			CameraStreamerFRAME0.flSaveVideoCodecConfig = true;
        		    		Toast.makeText(TTrackerPanel.this, "Start the video-recorder in \"FRAME\" mode with \"Saving\" option checked. Result config files will be placed in a new measurement folder.", Toast.LENGTH_LONG).show();
        		    		//.
        		    		arg0.dismiss();
        		    		//.
    		    			break; //. >
    		    		}
    		    	}
    		    	catch (Exception E) {
    		    		Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    		//.
    		    		arg0.dismiss();
    		    	}
    			}
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    		//.
    		return true; //. >
    
    	case POI_SUBMENU_NEWPOI:
    		intent = new Intent(this, TTrackerPOIPanel.class);
            startActivityForResult(intent,SHOW_NEWPOIPANEL);
    		//.
    		return true; //. >
    
    	case POI_SUBMENU_ADDTEXT:
    		intent = new Intent(this, TTrackerPOITextPanel.class);
            startActivityForResult(intent,SHOW_LASTPOITEXTEDITOR);
    		//.
    		return true; //. >

    	case POI_SUBMENU_ADDIMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(GetImageTempFile(this))); 
  		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
    		//.
    		return true; //. >

    	case POI_SUBMENU_ADDVIDEO:
    		intent = new Intent(this, TTrackerPOIVideoPanel.class);
            startActivityForResult(intent,SHOW_LASTPOIVIDEOCAMERA);
    		//.
    		return true; //. >
        }
        return false;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		try {
	        if (flLocked) {
	        	if (!Lock_Dialog_Exists())
	        		Lock_Dialog_Show();
			    return false; //. ->
	        }
	        else {
	        	Lock_ResetTimer();
	        	//.
	        	return super.dispatchTouchEvent(ev);
	        }
		} catch (InterruptedException IE) {
			return false; //. ->
		}
    }	
    
    @Override
	public void onBackPressed() {
    	if (flLocked)
    		return; //. ->
    	if (SensorsModule_ControlMeter_flActive) {
        	TSensorMeter ControlMeter = Tracker.GeoLog.SensorsModule.Meters.Items_GetItem(Configuration.SensorsModuleConfiguration.MeterToControl);
        	if ((ControlMeter != null) && ControlMeter.IsActive()) {
    			Toast.makeText(this, R.string.SRecordingIsntFinished, Toast.LENGTH_LONG).show();  						
        		return; //. ->
        	}
    	}
    	//.
		/*if (VoiceCommandHandler_IsExist() || (HittingDetector != null)) {
		    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.SConfirmation)
	        .setMessage(R.string.SCloseThePanel)
		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) {
		    		TTrackerPanel.this.finish();
		    	}
		    })
		    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) {
		    	}
		    })
		    .show();
		}
		else
    		TTrackerPanel.this.finish();*/
    	TTrackerPanel.this.finish();
	}	
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case UPDATE_CONFIGURATION: 
        	if (resultCode == RESULT_OK) 
        		try {
					Configuration.Load();
					//.
					ReInitialize();
				} catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            break; //. >
        
        case SHOW_NEWPOIPANEL: 
        	if (resultCode == RESULT_OK) {  
                btnAddPOIText.setEnabled(true);
                btnAddPOIImage.setEnabled(true);
                btnAddPOIVideo.setEnabled(true);
                btnAddPOIDrawing.setEnabled(true);
                btnAddPOIFile.setEnabled(true);
        	}  
            break; //. >
        
        case SHOW_LASTPOITEXTEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String POIText = extras.getString("Text");
                	try {
                    	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
                    		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(POI_DATAFILE_TYPE_TEXT, POIText,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(POI_DATAFILE_TYPE_TEXT, POIText,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case SHOW_LASTPOICAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetImageTempFile(this);
				if (F.exists()) {
					try {
                    	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
                    		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(POI_DATAFILE_TYPE_IMAGE, F,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(POI_DATAFILE_TYPE_IMAGE, F,null);
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

        case SHOW_LASTPOICAMERAANDEDITOR: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetImageTempFile(this);
				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
					
					private File ImageFile = null;
					
					@Override
					public void Process() throws Exception {
		            	if (F.exists()) {
	            			FileInputStream fs = new FileInputStream(F);
	            			try {
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
	            					float MaxSize = Tracker.GeoLog.GPSModule.MapPOIConfiguration.Image_ResX;
	            					float Scale = MaxSize/ImageMaxSize; 
	            					Matrix matrix = new Matrix();     
	            					matrix.postScale(Scale,Scale);
	            					//.
	            					Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
	            					try {
	            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            						try {
	            							Canceller.Check();
	            							//.
	            							if (!resizedBitmap.compress(CompressFormat.JPEG, 100, bos)) 
	            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
	            							//.
	            							Canceller.Check();
	            							//.
	            							byte[] ImageData = bos.toByteArray();
	            							//.
	            							ImageFile = GetImageTempFile(context);
	            							FileOutputStream FOS = new FileOutputStream(ImageFile);
	            							try {
	            								FOS.write(ImageData);
	            							}
	            							finally {
	            								FOS.close();
	            							}
	            						}
	            						finally {
	            							bos.close();
	            						}
	            					}
	            					finally {
	            						if (resizedBitmap != bitmap)
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
						if (ImageFile != null) {
					    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"TrackerImageDrawing"+"."+TDrawingDefines.FileExtension;
					    	//.
					    	Intent intent = new Intent(TTrackerPanel.this, TDrawingEditor.class);
					    	//.
					    	intent.putExtra("ImageFileName", ImageFile.getAbsolutePath());
					    	//.
					    	intent.putExtra("FileName", FileName); 
					    	intent.putExtra("ReadOnly", false); 
					    	intent.putExtra("SpaceContainersAvailable", false); 
					    	startActivityForResult(intent, SHOW_LASTPOIIMAGEDRAWINGEDITOR);    		
						}
					}
					
					@Override
					public void DoOnException(Exception E) {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
						//.
						Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				};
				PictureProcessing.Start();
        	}  
            break; //. >

        case SHOW_LASTPOIVIDEOCAMERA: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	double Timestamp = OleDate.UTCCurrentTimestamp();
                	String FileName = extras.getString("FileName");
                	try {
                		File F = new File(FileName);
                		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_"+F.getName();
                		File NF = new File(NFN);
                		F.renameTo(NF);
                		FileName = NFN;
                		//.
                		POI_AddDataFile(Timestamp,FileName);
                		//.
                		long DataSize = 0;
                		F = new File(FileName);
                		if (F.exists())
                			DataSize = F.length();
                		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case SHOW_LASTPOIVIDEOCAMERA1: 
        	if (resultCode == RESULT_OK) {  
            	try {
    				final File F = GetVideoTempFile(this);
    				if (F.exists()) {
                    	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
                    		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(POI_DATAFILE_TYPE_VIDEO, F,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(POI_DATAFILE_TYPE_VIDEO, F,null);
    				}
    				else
            			Toast.makeText(this, R.string.SVideoWasNotPrepared, Toast.LENGTH_SHORT).show();  
				}
				catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
			}
            break; //. >

        case SHOW_LASTPOIDRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String DrawingFileName = extras.getString("FileName");
                	try {
                    	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
                    		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(POI_DATAFILE_TYPE_DRAWING, DrawingFileName,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(POI_DATAFILE_TYPE_DRAWING, DrawingFileName,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >
            
        case SHOW_LASTPOIIMAGEDRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String DrawingFileName = extras.getString("FileName");
                	final File F = new File(DrawingFileName);
                	if (F.exists()) {
        				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
        					
        					private File ImageFile;
        					
        					@Override
        					public void Process() throws Exception {
                    	    	FileInputStream FIS = new FileInputStream(F);
                    	    	try {
                            		byte[] DRW = new byte[(int)F.length()];
                        			FIS.read(DRW);
                                	//.
        							TDrawings Drawings = new TDrawings();
        							Drawings.LoadFromByteArray(DRW,0);
        							Bitmap Image = Drawings.ToBitmap(Drawings.GetOptimalSize());
        							//.
            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
            						try {
            							if (!Image.compress(CompressFormat.JPEG, 100, bos)) 
            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
            							byte[] ImageData = bos.toByteArray();
            							//.
            							ImageFile = GetImageTempFile(context);
            							FileOutputStream FOS = new FileOutputStream(ImageFile);
            							try {
            								FOS.write(ImageData);
            							}
            							finally {
            								FOS.close();
            							}
            						}
            						finally {
            							bos.close();
            						}
                    	    	}
                    	    	finally {
                    	    		FIS.close();
                    	    	}
        						//.
        						F.delete();
        					}
        					
        					@Override
        					public void DoOnCompleted() throws Exception {
        						if (Canceller.flCancel)
        							return; //. ->
        						if (!flExists)
        							return; //. ->
                            	//.
        						if (ImageFile.exists()) {
        							try {
        				            	if (Configuration.GPSModuleConfiguration.MapPOI_flDataName) 
        				            		DataFileName_Dialog(TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TOnDataFileNameHandler() {
        				            			
        				            			@Override
        				            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
        				            	    		EnqueueDataFile(POI_DATAFILE_TYPE_EDITEDIMAGE, ImageFile,Name);
        				            			}
        				            		});
        				            	else 
        				    	    		EnqueueDataFile(POI_DATAFILE_TYPE_EDITEDIMAGE, ImageFile,null);
        							}
        							catch (Throwable E) {
        								String S = E.getMessage();
        								if (S == null)
        									S = E.getClass().getName();
        			        			Toast.makeText(TTrackerPanel.this, S, Toast.LENGTH_LONG).show();  						
        							}
        						}
        						else
        		        			Toast.makeText(TTrackerPanel.this, R.string.SImageWasNotPrepared, Toast.LENGTH_SHORT).show();  
        					}
        					
        					@Override
        					public void DoOnException(Exception E) {
        						if (Canceller.flCancel)
        							return; //. ->
        						if (!flExists)
        							return; //. ->
        						//.
        						Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        					}
        				};
        				PictureProcessing.Start();
                	}
                }
			}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static class TLockTimer extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_DOONTIME = 0;
    	
    	private TTrackerPanel Panel;
    	//.
    	private int Interval;
    	//.
        private Object 	Timeouter = new Object();
        private boolean Timeouter_flReset = false;
        
    	
    	public TLockTimer(TTrackerPanel pPanel, int pInterval) {
    		super();
    		//.
    		Panel = pPanel;
    		Interval = pInterval;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Destroy() throws InterruptedException {
    		CancelAndWait();
    	}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					Thread.sleep(1000); //. wait abit to avoid fast resetting on user touching
					//.
					synchronized (Timeouter) {
						Timeouter.wait(Interval);
						if (Timeouter_flReset) {
							Timeouter_flReset = false;
							//.
							continue; //. ^
						}
					}
	    			MessageHandler.obtainMessage(MESSAGE_DOONTIME).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

		public void Reset() {
			synchronized (Timeouter) {
				Timeouter_flReset = true;
				Timeouter.notifyAll();
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
		            	//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(Panel, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DOONTIME:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	if (!Panel.flLocked)
		            		Panel.Lock();
		            	else
		            		if (Panel.Lock_Dialog != null) 
		            			Panel.Lock_Dialog_Dismiss();
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
    
    protected void Lock() throws InterruptedException {
        Lock_CancelTimer();
        //.
        Updating_Finish();
        //. 
        Lock_SetLowBrightness();
        //.
        Lock_ShowLockScreen();
        //.
        flLocked = true;
        //.
        Toast.makeText(this, R.string.STrackerPanelIsLockedWithLowBrightness, Toast.LENGTH_LONG).show();
    }
    
    protected void Unlock() throws InterruptedException {
    	flLocked = false;
    	//.
    	Lock_HideLockScreen();
    	//.
    	Lock_RestoreBrightness();
        //.
        Updating_Start();
        //.
        Lock_SetupTimer(Lock_Timeout);
    }
    
    protected void Lock_Cancel() throws InterruptedException {
    	Lock_CancelTimer();
    }
    
    protected void Lock_SetupTimer(int Interval) throws InterruptedException {
    	Lock_CancelTimer();
    	//.
    	Lock_Timer = new TLockTimer(this, Interval); 
    }
    
    protected void Lock_CancelTimer() throws InterruptedException {
    	if (Lock_Timer != null) {
    		Lock_Timer.Destroy();
    		Lock_Timer = null;
    	}
    }
    
    protected void Lock_ResetTimer() throws InterruptedException {
    	if (Lock_Timer != null)
    		Lock_Timer.Reset();
    }
    
    protected void Lock_SetLowBrightness() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = Lock_LowBrightness; 
        getWindow().setAttributes(layoutParams);
    }
    
    protected void Lock_RestoreBrightness() {
    	WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = Lock_DefaultBrightness; 
        getWindow().setAttributes(layoutParams);
    }
    
    protected void Lock_Dialog_Show() throws InterruptedException {
    	AlertDialog.Builder AB = new AlertDialog.Builder(TTrackerPanel.this);
    	AB.setIcon(android.R.drawable.ic_dialog_alert);
        AB.setTitle(R.string.SConfirmation);
        AB.setMessage(R.string.SUnlockTrackerPanel);
	    AB.setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    	
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	    		try {
		    		Lock_Dialog_Dismiss();
		    		//.
					Unlock();
				} catch (InterruptedException IE) {
				}
	    	}
	    });
	    AB.setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
	    	
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	    		try {
		    		Lock_Dialog_Dismiss();
				} catch (InterruptedException IE) {
				}
	    	}
	    });
	    AB.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				try {
					Lock_Dialog_Dismiss();
				} catch (InterruptedException IE) {
				}
			}
		});
	    Lock_Dialog = AB.show();
	    //.
	    Lock_HideLockScreen();
	    //.
	    Lock_RestoreBrightness();
	    //.
	    Lock_SetupTimer(Lock_DialogTimeout);
    }
    
    protected void Lock_Dialog_Dismiss() throws InterruptedException {
    	Lock_CancelTimer();
    	//.
    	if (Lock_Dialog != null) {
    		Lock_Dialog.dismiss();
    		Lock_Dialog = null;
    	}
    	Lock_SetLowBrightness();
    	//.
    	Lock_ShowLockScreen();
    }
    
    protected boolean Lock_Dialog_Exists() {
    	return (Lock_Dialog != null);
    }
    
    protected void Lock_ShowLockScreen() {
    	llMainScreen.setVisibility(View.GONE);
    	llLockScreen.setVisibility(View.VISIBLE);
		//.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    protected void Lock_HideLockScreen() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//.
    	llLockScreen.setVisibility(View.GONE);
    	llMainScreen.setVisibility(View.VISIBLE);
    }
    
    private void VoiceCommandHandler_StartInitializing() throws Exception {
    	TTrackerPanelVoiceCommands VoiceCommands = TTrackerPanelVoiceCommands.GetInstance("en-us"); 
    	if (VoiceCommands == null)
    		throw new Exception("voice commands initializing error: culture is not found"); //. =>
    	//.
        VoiceCommandHandler = Tracker.GeoLog.AudioModule.VoiceCommandModule.CommandHandler_Create(VoiceCommands.GetCommands(false), new TVoiceCommandModule.TCommandHandler.TDoOnCommandHandler() {
        	@Override
        	public void DoOnCommand(String Command) {
        		try {
					VoiceCommandHandler_DoOnCommand(Command);
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
				}
        	}            	
        });
		VoiceCommandHandler_Initializing = new TAsyncProcessing(TTrackerPanel.this,"Initializing the Voice recognizer ...") {
			@Override
			public void Process() throws Exception {
				try {
					VoiceCommandHandler.Initialize();
				}
				catch (Exception E) {
					throw E; //. =>
				}
				catch (Throwable T) {
					String S = T.getMessage();
					if (S == null)
						S = T.getClass().getName();
					throw new Exception(S); //. =>
				}
			}
			@Override
			public void DoOnCompleted() throws Exception {
			}
			@Override
			public void DoOnException(Exception E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
			}
		};
		VoiceCommandHandler_Initializing.Start();
    }
    
    private void VoiceCommandHandler_Finalize() throws InterruptedException {
    	if (VoiceCommandHandler_Initializing != null) {
    		VoiceCommandHandler_Initializing.CancelAndWait();
    		VoiceCommandHandler_Initializing = null;
    	}
    	if (VoiceCommandHandler != null) {
    		VoiceCommandHandler.Finalize();
    		VoiceCommandHandler = null;
    	}
    }
    
    @SuppressWarnings("unused")
	private boolean VoiceCommandHandler_IsExist() {
    	return (VoiceCommandHandler != null);
    }
    
    private void VoiceCommandHandler_DoOnCommand(String Command) throws Exception {
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_GPSMODULE_POI_ADDIMAGE)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
  		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetImageTempFile(TTrackerPanel.this))); 
  		    startActivityForResult(intent, SHOW_LASTPOICAMERA);		
			return; //. ->
		}
    	//.
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_GPSMODULE_POI_ADDVIDEO)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
  		    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.GetVideoTempFile(TTrackerPanel.this))); 
  		    startActivityForResult(intent, SHOW_LASTPOIVIDEOCAMERA1);	
			return; //. ->
		}
    	//.
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_VIDEORECORDERMODULE_RECORDING_ON)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
			//.
			SensorsModule_ControlMeter_SetActive(true);
			//.
			ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_START,CONTROL_METHOD_VOICE);
			return; //. ->
		}
		//.
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_VIDEORECORDERMODULE_RECORDING_OFF)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
			//.
			SensorsModule_ControlMeter_SetActive(false);
			//.
			ControlCommand_NotifyOnAfterCommand(CONTROL_COMMAND_SENSORSMODULE_METER_FINISH,CONTROL_METHOD_VOICE);
			return; //. ->
		}
    	//.
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_DATASTREAMERMODULE_ACTIVE_ON)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
			Tracker.GeoLog.DataStreamerModule.SetActiveValue(true);
			return; //. ->
		}
    	//.
		if (Command.equals(TTrackerPanelVoiceCommands.COMMAND_DATASTREAMERMODULE_ACTIVE_OFF)) {
			VoiceCommandHandler_NotifyOnBeforeCommand(Command);
			Tracker.GeoLog.DataStreamerModule.SetActiveValue(false);
			return; //. ->
		}
    }
    
    private void VoiceCommandHandler_NotifyOnBeforeCommand(String Command) {
		MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,Command).sendToTarget();
		//.
    	int Duration = 200; //. ms
		Vibrator.vibrate(Duration);
		//.
		PostUpdate();
    }
    
    private void ControlCommand_NotifyOnAfterCommand(int Command, int Method) {
    	MessageHandler.obtainMessage(MESSAGE_DOONAFTERCONTROLCOMMAND,new TControlDescriptor(Command,Method)).sendToTarget();
    }
    
    private void ControlCommand_DoNotifyOnAfterCommand(int Command, int Method) {
    	switch (Command) {
    	
    	case CONTROL_COMMAND_SENSORSMODULE_METER_START:
    		switch (Configuration.SensorsModuleConfiguration.MeterControlNotifications) {
    		
    		case TConfiguration.NOTIFICATIONS_VIBRATION2BITS:
    			TAsyncProcessing Processing = new TAsyncProcessing() {
    				
    				@Override
    				public void Process() throws Exception {
    			    	int Duration = 300; //. ms
    			    	int Pause = 500; //. ms
    			    	Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    				}
    				
    				@Override
    				public void DoOnCompleted() throws Exception {
    				}
    				
    				@Override
    				public void DoOnException(Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
    				}
    			};
    			Processing.Start();
    			break;

    		case TConfiguration.NOTIFICATIONS_VOICE:
    			if (VideoRecorderModule_AudioNotifier != null)
					try {
						switch (Method) {
						
						case CONTROL_METHOD_VOICE:
							VideoRecorderModule_AudioNotifier.Notification_VoiceActivatedRecordingIsStarted();
							break; //. >

						case CONTROL_METHOD_TAP:
							VideoRecorderModule_AudioNotifier.Notification_TapActivatedRecordingIsStarted();
							break; //. >
							
						default:
							VideoRecorderModule_AudioNotifier.Notification_RecordingIsStarted();
							break; //. >
						}
					} catch (Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
					}
    			break;
    			
    		case TConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE:
    			Processing = new TAsyncProcessing() {
    				
    				@Override
    				public void Process() throws Exception {
    			    	int Duration = 300; //. ms
    			    	int Pause = 500; //. ms
    			    	Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    				}
    				
    				@Override
    				public void DoOnCompleted() throws Exception {
    				}
    				
    				@Override
    				public void DoOnException(Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
    				}
    			};
    			Processing.Start();
    			//.
    			if (VideoRecorderModule_AudioNotifier != null)
					try {
						switch (Method) {
						
						case CONTROL_METHOD_VOICE:
							VideoRecorderModule_AudioNotifier.Notification_VoiceActivatedRecordingIsStarted();
							break; //. >

						case CONTROL_METHOD_TAP:
							VideoRecorderModule_AudioNotifier.Notification_TapActivatedRecordingIsStarted();
							break; //. >
							
						default:
							VideoRecorderModule_AudioNotifier.Notification_RecordingIsStarted();
							break; //. >
						}
					} catch (Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
					}
    			break;
    		}
    		break; //. >
    		
    	case CONTROL_COMMAND_SENSORSMODULE_METER_FINISH:
    		switch (Configuration.SensorsModuleConfiguration.MeterControlNotifications) {
    		
    		case TConfiguration.NOTIFICATIONS_VIBRATION2BITS:
    			TAsyncProcessing Processing = new TAsyncProcessing() {
    				
    				@Override
    				public void Process() throws Exception {
    			    	int Duration = 300; //. ms
    			    	int Pause = 500; //. ms
    			    	Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    				}
    				
    				@Override
    				public void DoOnCompleted() throws Exception {
    				}
    				
    				@Override
    				public void DoOnException(Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
    				}
    			};
    			Processing.Start();
    			break;

    		case TConfiguration.NOTIFICATIONS_VOICE:
    			if (VideoRecorderModule_AudioNotifier != null)
					try {
						switch (Method) {
						
						case CONTROL_METHOD_VOICE:
							VideoRecorderModule_AudioNotifier.Notification_VoiceActivatedRecordingIsFinished();
							break; //. >

						case CONTROL_METHOD_TAP:
							VideoRecorderModule_AudioNotifier.Notification_TapActivatedRecordingIsFinished();
							break; //. >
							
						default:
							VideoRecorderModule_AudioNotifier.Notification_RecordingIsFinished();
							break; //. >
						}
					} catch (Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
					}
    			break;
    			
    		case TConfiguration.NOTIFICATIONS_VIBRATION2BITSANDVOICE:
    			Processing = new TAsyncProcessing() {
    				
    				@Override
    				public void Process() throws Exception {
    			    	int Duration = 300; //. ms
    			    	int Pause = 500; //. ms
    			    	Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    					Thread.sleep(Pause);
    					Vibrator.vibrate(Duration);
    				}
    				
    				@Override
    				public void DoOnCompleted() throws Exception {
    				}
    				
    				@Override
    				public void DoOnException(Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
    				}
    			};
    			Processing.Start();
    			//.
    			if (VideoRecorderModule_AudioNotifier != null)
					try {
						switch (Method) {
						
						case CONTROL_METHOD_VOICE:
							VideoRecorderModule_AudioNotifier.Notification_VoiceActivatedRecordingIsFinished();
							break; //. >

						case CONTROL_METHOD_TAP:
							VideoRecorderModule_AudioNotifier.Notification_TapActivatedRecordingIsFinished();
							break; //. >
							
						default:
							VideoRecorderModule_AudioNotifier.Notification_RecordingIsFinished();
							break; //. >
						}
					} catch (Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
    	    			MessageHandler.obtainMessage(MESSAGE_SHOWMESSAGE,S).sendToTarget();
					}
    			break;
    		}
    		break; //. >
    	}
    }
    
    protected void EnableDisableTrackerByButton(ToggleButton TB) {
		try {
			TB.setChecked(!TB.isChecked());
			//.
			boolean flOn = TB.isChecked();
			TTracker.EnableDisableTracker(flOn);
			EnableDisablePanelItems(flOn);
			//.
			Update();
			//.
			if (!flOn) {
				Thread.sleep(333); 
				TTrackerPanel.this.finish();
			}
    	}
    	catch (Exception E) {
            Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    }
    
    protected File GetImageTempFile(Context context) {
    	  return new File(TGeoLogApplication.GetTempFolder(),"Image.jpg");
    }
    
    protected File GetVideoTempFile(Context context) {
  	  return new File(TGeoLogApplication.GetTempFolder(),"Video.3gp");
  }
  
    protected File GetDrawingTempFile(Context context) {
    	return new File(TGeoLogApplication.GetTempFolder(),"Drawing"+"."+TDrawingDefines.FileExtension);
    }

    private static class TOnDataFileNameHandler {
    	
    	public void DoOnDataFileNameHandler(String Name) throws Exception {
    	}
    }
    
    private void DataFileName_Dialog(final int DataNameMaxSize, final TOnDataFileNameHandler OnDataFileNameHandler) {
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		//.
		final AlertDialog dlg = new AlertDialog.Builder(this)
		//.
		.setTitle(R.string.SDataName)
		.setMessage(R.string.SEnterName)
		//.
		.setView(input)
		.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
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
					Toast.makeText(TTrackerPanel.this, E.getMessage(),	Toast.LENGTH_LONG).show();
				}
			}
		})
		//.
		.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// . hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		}).create();
		//.
		input.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick(); 
				return false;
			}
        });        
		// .
		dlg.show();
    }
    
    private void EnqueueDataFile(int DataFileType, Object Data, String DataName) throws Exception {
    	if ((DataName != null) && (DataName.length() > 0))
    		DataName = "@"+TComponentFileStreaming.CheckAndEncodeFileNameString(DataName);
    	else
    		DataName = "";
    	//.
		switch (DataFileType) {
		
		case POI_DATAFILE_TYPE_TEXT: {
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
    		//. last version: POI_AddText(Timestamp,NFN);
    		POI_AddDataFile(Timestamp,NFN);
    		//.
    		Toast.makeText(this, getString(R.string.STextIsAdded)+Integer.toString(TextBA.length), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case POI_DATAFILE_TYPE_IMAGE:
		case POI_DATAFILE_TYPE_EDITEDIMAGE: {
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
	    	//.
	    	File F = (File)Data;
	    	//.
			FileInputStream fs = new FileInputStream(F);
			try
			{
				byte[] PictureBA;
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
						if (resizedBitmap != bitmap)
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
				//. last version: POI_AddImage(Timestamp,NFN);
        		POI_AddDataFile(Timestamp,NFN);
	        	//.
	        	Toast.makeText(this, getString(R.string.SImageIsAdded)+Integer.toString(PictureBA.length), Toast.LENGTH_LONG).show();
			}
			finally
			{
				fs.close();
			}
			break; //. >
		}
			
		case POI_DATAFILE_TYPE_VIDEO: {
	    	File F = (File)Data;
	    	//.
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
			//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(F.getName());
    		File NF = new File(NFN);
    		F.renameTo(NF);
    		String FileName = NFN;
    		//.
    		POI_AddDataFile(Timestamp,FileName);
    		//.
    		long DataSize = 0;
    		F = new File(FileName);
    		if (F.exists())
    			DataSize = F.length();
    		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
			break; //. >
		}

		case POI_DATAFILE_TYPE_DRAWING: {
			String DrawingFileName = (String)Data;
			//.
    		double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TDrawingDefines.FileExtension;
    		File NF = new File(NFN);
    		if (!(new File(DrawingFileName)).renameTo(NF))
    			throw new IOException("could not rename file: "+DrawingFileName); //. =>
    		String FileName = NFN;
    		//.
    		POI_AddDataFile(Timestamp,FileName);
    		//.
    		long DataSize = 0;
    		File F = new File(FileName);
    		if (F.exists())
    			DataSize = F.length();
    		Toast.makeText(this, getString(R.string.SDrawingIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case POI_DATAFILE_TYPE_FILE: {
			String FileName = (String)Data;
			//.
	    	double Timestamp = OleDate.UTCCurrentTimestamp();
			String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(FileName);
			File NF = new File(NFN);
			TFileSystem.CopyFile(new File(FileName), NF);
			FileName = NFN;
			//.
			POI_AddDataFile(Timestamp,FileName);
			//.
			long DataSize = 0;
			File F = new File(FileName);
			if (F.exists())
				DataSize = F.length();
			//.
    		Toast.makeText(this, getString(R.string.SFileIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
			break; //. >
		}
		}
    }
    
    @SuppressWarnings("unused")
	private void POI_AddText(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            TMapPOITextValue MapPOIText = new TMapPOITextValue(Timestamp,FileName);
            TObjectSetGetMapPOITextSO SO = new TObjectSetGetMapPOITextSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIText);
            try {
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingText)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SEmptyText, Toast.LENGTH_LONG).show();  
    }
    
    @SuppressWarnings("unused")
	private void POI_AddImage(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            TMapPOIImageValue MapPOIImage = new TMapPOIImageValue(Timestamp,FileName);
            TObjectSetGetMapPOIJPEGImageSO SO = new TObjectSetGetMapPOIJPEGImageSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIImage);
            try {
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingImage)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SImageIsNull, Toast.LENGTH_LONG).show();  
    }
    
    private void POI_AddDataFile(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            String Params = "1"; //. Version
            byte[] AddressData = Params.getBytes("windows-1251");
			//.
            TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(Timestamp,FileName);
            TObjectSetGetMapPOIDataFileSO SO = new TObjectSetGetMapPOIDataFileSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null, AddressData);
            SO.setValue(MapPOIDataFile);
            try {
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingData)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SEmptyData, Toast.LENGTH_LONG).show();  
    }
    
    public void StartObtainingCurrentFix() {
        if (Tracker.GeoLog.GPSModule == null) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
    	new TCurrentFixObtaining(this, Tracker.GeoLog.GPSModule, new TCurrentFixObtaining.TDoOnFixIsObtainedHandler() {
    		@Override
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    			Update();
    		}
    	});
    }

    public void ShowInterfacePanel() throws Exception {
        if (Tracker.GeoLog.idTOwnerComponent == 0)
        	throw new Exception("device is not connected to the server yet"); //. =>
        if (Tracker.GeoLog.idTOwnerComponent != SpaceDefines.idTCoComponent)
			return; //. ->
        long ObjectID = Tracker.GeoLog.idOwnerComponent;
        if (ObjectID != 0) {
        	Intent intent = new Intent(this, TCoGeoMonitorObjectPanel.class);
    		intent.putExtra("ComponentID", Component.ID);
        	intent.putExtra("ParametersType", TCoGeoMonitorObjectPanel.PARAMETERS_TYPE_OID);
        	intent.putExtra("ObjectID", ObjectID);
        	intent.putExtra("ObjectName", getString(R.string.SMe1));
        	startActivity(intent);
        }
    }
    
    public void StartObtainingCurrentPosition() {
        if (Tracker.GeoLog.GPSModule == null) {
			Toast.makeText(this, R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if (Component == null)
        	return; //. ->
    	try {
        	new TCurrentPositionObtaining(this, Tracker.GeoLog.GPSModule, Component, new TCurrentPositionObtaining.TDoOnPositionIsObtainedHandler() {
        		@Override
        		public void DoOnPositionIsObtained(TXYCoord Crd) {
                	try {
                		Component.MoveReflectionWindow(Crd);
    				} catch (Exception Ex) {
    	                Toast.makeText(TTrackerPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
    				}
                	//.
	                setResult(Activity.RESULT_OK);
            		TTrackerPanel.this.finish();
        		}
        	});
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public TXYCoord ObtainCurrentPosition() throws Exception {
    	TGPSFixValue Fix;
		Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
		if (!Fix.IsSet()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnavailable)); //. =>
		if (Fix.IsEmpty()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnknown)); //. =>
		if (Component != null)
			return Component.ConvertGeoCoordinatesToXY(TGPSModule.DatumID, Fix.Latitude,Fix.Longitude,Fix.Altitude);
		else 
			return null; //. ->
    }
    
    public int GetAlarm() {
    	if (Tracker.GeoLog.GPIModule != null)
    		return (Tracker.GeoLog.GPIModule.GetIntValue() & 3); //. ->
    	else
    		return 0;
    }
    
    public void SetAlarm(boolean flSet) throws Exception {
    	short V;
    	if (flSet)
    		V = 2; //. major alert priority
    	else 
    		V = 0; //. no alert
    	TTracker.GetTracker().GeoLog.GPIModule.SetValue(V);
    }
    
    private void SensorsModule_ControlMeter_SetActive(boolean flActive) throws Exception {
    	if (SensorsModule_ControlMeter_flActive == flActive)
    		return; //. ->
    	if (Tracker.GeoLog.IsEnabled()) {
    		int MetersCount = 0;
        	String[] _MetersToControl = Configuration.SensorsModuleConfiguration.MeterToControl.split(",");
    		int CntI = _MetersToControl.length;
        	for (int I = 0; I < CntI; I++) {
            	TSensorMeter MeterToControl = Tracker.GeoLog.SensorsModule.Meters.Items_GetItem(_MetersToControl[I]);
            	if (MeterToControl != null) {  
            		MeterToControl.SetActive(flActive);
            		MetersCount++;
            	}
        	}
        	if (MetersCount == 0)
        		Tracker.GeoLog.VideoRecorderModule.SetRecorderState(flActive, false);
    	}
    	SensorsModule_ControlMeter_flActive = flActive;
    	//.
    	if (SensorsModule_ControlMeter_flActive) {
    		MessageHandler.obtainMessage(TTrackerPanel.MESSAGE_METER_RECORDING_BEEP).sendToTarget();
    		//.
    		if (Configuration.SensorsModuleConfiguration.MeterRecordingNotification != TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_NONE)
        		SensorsModule_ControlMeter_RecordingNotifying_Start(Configuration.SensorsModuleConfiguration.MeterRecordingNotification);
    	}
    	else
    		SensorsModule_ControlMeter_RecordingNotifying_Stop();
    }
    
    private void SensorsModule_ControlMeter_RecordingNotifying_Start(int NotificationType) {
    	SensorsModule_ControlMeter_RecordingNotifying_Stop();
    	//.
    	SensorsModule_ControlMeter_RecordingBeeping = new Timer();
    	SensorsModule_ControlMeter_RecordingBeeping.schedule(new TMeterRecordingNotifyingTask(this, NotificationType),SensorsModule_ControlMeter_RecordingNotifying_Interval,SensorsModule_ControlMeter_RecordingNotifying_Interval);
    }
    
    private void SensorsModule_ControlMeter_RecordingNotifying_Stop() {
    	if (SensorsModule_ControlMeter_RecordingBeeping != null) {
    		SensorsModule_ControlMeter_RecordingBeeping.cancel();
    		SensorsModule_ControlMeter_RecordingBeeping = null;
    	}
    }
    
    private void EnableDisablePanelItems(boolean flEnabled) {
        btnLock.setEnabled(flEnabled);
    	edFix.setEnabled(flEnabled);
        edFixSpeed.setEnabled(flEnabled);
        edFixPrecision.setEnabled(flEnabled);
        btnObtainCurrentFix.setEnabled(flEnabled);
        btnInterfacePanel.setEnabled(flEnabled);
        btnShowLocation.setEnabled(flEnabled);
        btnNewPOI.setEnabled(flEnabled);
        cbPOIDataName.setEnabled(flEnabled);
        btnAddPOIText.setEnabled(flEnabled);
        btnAddPOIImage.setEnabled(flEnabled);
        btnAddPOIVideo.setEnabled(flEnabled);
        btnAddPOIDrawing.setEnabled(flEnabled);
        btnAddPOIFile.setEnabled(flEnabled);
        tbAlarm.setEnabled(flEnabled);
        cbVideoRecorderModuleRecording.setEnabled(flEnabled);
        btnVideoRecorderModulePanel.setEnabled(flEnabled);
        btnSensorsModuleChannelsPanel.setEnabled(flEnabled);
        btnSensorsModuleMetersPanel.setEnabled(flEnabled);
        btnSensorsModuleMeasurementsArchive.setEnabled(flEnabled);
        btnSensorsModuleMeasurementsTransferProcessPanel.setEnabled(flEnabled);
        cbDataStreamerModuleActive.setEnabled(flEnabled);
        btnDataStreamerModulePanel.setEnabled(flEnabled);
        edConnectorInfo.setEnabled(flEnabled);
        btnConnectorCommands.setEnabled(flEnabled);
        edCheckpoint.setEnabled(flEnabled);
        edOpQueueTransmitInterval.setEnabled(flEnabled);
        edPositionReadInterval.setEnabled(flEnabled);
        cbIgnoreImpulseModeSleepingOnMovement.setEnabled(flEnabled);
        edGeoThreshold.setEnabled(flEnabled);
        edOpQueue.setEnabled(flEnabled);
        btnOpQueueCommands.setEnabled(flEnabled);
        edComponentFileStreaming.setEnabled(flEnabled);
        pbComponentFileStreaming.setEnabled(flEnabled);
        btnComponentFileStreamingCommands.setEnabled(flEnabled);
    }
    
    public static String DoubleToString(double Value) {
        int ValueInteger = (int)Value;
        long ValueDecimals = (int)((Value-ValueInteger)*100000);

        String ValueDecString = String.valueOf(ValueDecimals);
        while(ValueDecString.length()<5) {
            ValueDecString = "0" + ValueDecString;
        }
        return String.valueOf(ValueInteger) + "." + ValueDecString;
    }
    
    private void Updating_Start() {
    	Updating_Finish();
    	//.
        Updater = new Timer();
        Updater.schedule(new TUpdaterTask(this),100,UpdatingInterval);
    }

    private void Updating_Finish() {
        if (Updater != null) {
        	Updater.cancel();
        	Updater = null;
        }
    }

    private void Update() {
    	if (!flVisible)
    		return; //. ->
    	if (Tracker.GeoLog.IsEnabled()) {
            String S;
            //.
            tbAlarm.setChecked(GetAlarm() > 0);
            //.
            cbVideoRecorderModuleRecording.setChecked(Tracker.GeoLog.VideoRecorderModule.Recording.BooleanValue());
            cbDataStreamerModuleActive.setChecked(Tracker.GeoLog.DataStreamerModule.ActiveValue.BooleanValue());
            //. connector info
            if (Tracker.GeoLog.ConnectorModule.flProcessing) {
            	S = getString(R.string.SConnected);
            	if (Tracker.GeoLog.ConnectorModule.IsSecure())
            		S += "("+getString(R.string.SSecure)+")";
            	edConnectorInfo.setText(S);
            	edConnectorInfo.setTextColor(Color.GREEN);
            	if (Tracker.GeoLog.ConnectorModule.flReconnect) {
            		edConnectorInfo.setText(R.string.SReconnect);
                	edConnectorInfo.setTextColor(Color.RED);
            	}
            }
            else
            {
            	if (Tracker.GeoLog.ConnectorModule.flServerConnectionEnabled) {
            		edConnectorInfo.setText(R.string.SNoConnection);
                	edConnectorInfo.setTextColor(Color.RED);
                	if (Tracker.GeoLog.ConnectorModule.IsPaused()) {
                		edConnectorInfo.setText(R.string.SPause);
                    	edConnectorInfo.setTextColor(Color.YELLOW);
                	}
            	}
            	else {
            		edConnectorInfo.setText(R.string.SDisabledConnection);
                	edConnectorInfo.setTextColor(Color.GRAY);
            	}
            	//.
        		Exception E = Tracker.GeoLog.ConnectorModule.GetProcessException();
                if (E != null)
                    Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            }
            //. GPS module info
            if (Tracker.GeoLog.GPSModule.flProcessing) {
                if (Tracker.GeoLog.GPSModule.flGPSFixing) {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecsAgo);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecsAgo);
                    lbTitle.setText(getString(R.string.SGeoPosition)+S);
                    S = DoubleToString(fix.Latitude)+"; "+DoubleToString(fix.Longitude)+"; "+DoubleToString(fix.Altitude);
                    edFix.setText(S);
                    edFix.setTextColor(Color.GREEN);
                    edFixSpeed.setText(Integer.toString((int)fix.Speed));
                    edFixSpeed.setTextColor(Color.GREEN);
                    edFixPrecision.setText(Integer.toString((int)fix.Precision));
                    edFixPrecision.setTextColor(Color.GREEN);
                    ///? btnObtainCurrentFix.setEnabled(true);
                    ///? btnInterfacePanel.setEnabled(true);
                    ///? btnShowLocation.setEnabled(true);
                    ///? btnNewPOI.setEnabled(true);
                    //.
                }
                else {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecs);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecs);
                    lbTitle.setText(getString(R.string.SGeoPositionIsUnknown)+S);
                    S = getString(R.string.SLastCoordinates)+DoubleToString(fix.Latitude)+"; "+DoubleToString(fix.Longitude)+"; "+DoubleToString(fix.Altitude)+")";
                    edFix.setText(S);
                    edFix.setTextColor(Color.RED);
                    edFixSpeed.setText(Integer.toString((int)fix.Speed));
                    edFixSpeed.setTextColor(Color.RED);
                    edFixPrecision.setText(Integer.toString((int)fix.Precision));
                    edFixPrecision.setTextColor(Color.RED);
                    ///? btnObtainCurrentFix.setEnabled(true);
                    ///? btnInterfacePanel.setEnabled(false);
                    ///? btnShowLocation.setEnabled(false);
                    ///? btnNewPOI.setEnabled(false);
                    //.
                }
            }
            else {
                lbTitle.setText(R.string.SGeoCoordinatesIsNull);
                edFix.setText(R.string.SGeoCoordinatesAreNotAvailable);
                edFix.setTextColor(Color.GRAY);
                edFixSpeed.setText("?");
                edFixSpeed.setTextColor(Color.GRAY);
                edFixPrecision.setText("?");
                edFixPrecision.setTextColor(Color.GRAY);
            	//.
        		Exception E = Tracker.GeoLog.GPSModule.GetProcessException();
                if (E != null)
                    Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            }
            edCheckpoint.setText(Short.toString(Tracker.GeoLog.ConnectorModule.CheckpointInterval.Value)+" ");
            edOpQueueTransmitInterval.setText(Integer.toString(Tracker.GeoLog.ConnectorModule.TransmitInterval/1000)+" ");
            edPositionReadInterval.setText(Integer.toString(Tracker.GeoLog.GPSModule.Provider_ReadInterval/1000)+" ");
            cbIgnoreImpulseModeSleepingOnMovement.setChecked(Tracker.GeoLog.GPSModule.flIgnoreImpulseModeSleepingOnMovement);
            edGeoThreshold.setText(Short.toString(Tracker.GeoLog.GPSModule.Threshold.Value)+" ");
            int POC = Tracker.GeoLog.ConnectorModule.PendingOperationsCount();
            if (POC > 0)
            	edOpQueue.setTextColor(Color.RED);
            else
            	edOpQueue.setTextColor(Color.GREEN);
            edOpQueue.setText(Integer.toString(POC)+" ");
            if (Tracker.GeoLog.ComponentFileStreaming != null) {
                TDEVICEModule.TComponentFileStreaming.TItemsStatistics ItemsStatistics = Tracker.GeoLog.ComponentFileStreaming.GetItemsStatistics();
                if (ItemsStatistics.Count > 0) {
                	edComponentFileStreaming.setTextColor(Color.RED);
                	//.
                	if (Tracker.GeoLog.ComponentFileStreaming.flEnabledStreaming)
                		pbComponentFileStreaming.setVisibility(View.VISIBLE);
                	else
                    	pbComponentFileStreaming.setVisibility(View.GONE);
                }
                else {
                	edComponentFileStreaming.setTextColor(Color.GREEN);
                	//.
                	pbComponentFileStreaming.setVisibility(View.GONE);
                }
                String SS = null;
                int Size = (int)(ItemsStatistics.Size/1024);
                if (Size < 1024) {
                	if (Size > 0)
                		SS = Integer.toString(Size)+getString(R.string.SKb);
                }
                else {
                	Size = (int)(Size/1024);
                	SS = Integer.toString(Size)+getString(R.string.SMb);
                }
                if (SS != null)
                	S = Integer.toString(ItemsStatistics.Count)+" ("+SS+")";
                else
                	S = Integer.toString(ItemsStatistics.Count);
                if (!Tracker.GeoLog.ComponentFileStreaming.flEnabledStreaming) {
                	S = S+" "+"/"+getString(R.string.SPause)+"/";
                	edComponentFileStreaming.setTextColor(Color.YELLOW);
                }
            	edComponentFileStreaming.setText(S);
            	if (ItemsStatistics.FileSize > 0)
            		pbComponentFileStreaming.setProgress((int)ItemsStatistics.TransmissionPercentage());
            	else
            		pbComponentFileStreaming.setProgress(0);
            }
            else {
            	edComponentFileStreaming.setTextColor(Color.GRAY);
                edComponentFileStreaming.setText("?");
            }
            //. error handling
            Exception E = Tracker.GeoLog.ConnectorModule.GetProcessOutgoingOperationException();
            if (E != null)
                Toast.makeText(this, getString(R.string.SServerSideError)+E.getMessage(), Toast.LENGTH_LONG).show();
            
    	}
    	else {
        	edConnectorInfo.setText("");
            lbTitle.setText(R.string.SGeoCoordinates);
            edFix.setTextColor(Color.GRAY);
            edFix.setText(R.string.SDisabled1);
            edFixSpeed.setTextColor(Color.GRAY);
            edFixSpeed.setText("?");
            edFixPrecision.setTextColor(Color.GRAY);
            edFixPrecision.setText("?");
            edCheckpoint.setTextColor(Color.GRAY);
            edCheckpoint.setText("?");
            edOpQueueTransmitInterval.setTextColor(Color.GRAY);
            edOpQueueTransmitInterval.setText("?");
            edPositionReadInterval.setTextColor(Color.GRAY);
            edPositionReadInterval.setText("?");
            cbIgnoreImpulseModeSleepingOnMovement.setChecked(false);
            edGeoThreshold.setTextColor(Color.GRAY);
            edGeoThreshold.setText("?");
            edOpQueue.setTextColor(Color.GRAY);
            if (Tracker != null)
            	edOpQueue.setText(Integer.toString(Tracker.GeoLog.ConnectorModule.PendingOperationsCount())+" ");
            else 
            	edOpQueue.setText("?");
            edComponentFileStreaming.setTextColor(Color.GRAY);
            if ((Tracker != null) && (Tracker.GeoLog.ComponentFileStreaming != null)) {
                TDEVICEModule.TComponentFileStreaming.TItemsStatistics ItemsStatistics = Tracker.GeoLog.ComponentFileStreaming.GetItemsStatistics();
                String SS = null;
                int Size = (int)(ItemsStatistics.Size/1024);
                if (Size < 1024) {
                	if (Size > 0)
                		SS = Integer.toString(Size)+getString(R.string.SKb);
                }
                else {
                	Size = (int)(Size/1024);
                	SS = Integer.toString(Size)+getString(R.string.SMb);
                }
                String S;
                if (SS != null)
                	S = Integer.toString(ItemsStatistics.Count)+" ("+SS+")";
                else
                	S = Integer.toString(ItemsStatistics.Count);
                if (!Tracker.GeoLog.ComponentFileStreaming.flEnabledStreaming) {
                	S = S+" "+"/"+getString(R.string.SPause)+"/";
                	edComponentFileStreaming.setTextColor(Color.YELLOW);
                }
            	edComponentFileStreaming.setText(S);
            }
            else 
                edComponentFileStreaming.setText("?");
    	}
    }

    private void PostUpdate() {
    	MessageHandler.obtainMessage(TTrackerPanel.MESSAGE_UPDATEINFO).sendToTarget();
    }
    
	private static final int MESSAGE_UPDATEINFO 				= 1;
	private static final int MESSAGE_SHOWMESSAGE 				= 2;
	private static final int MESSAGE_DOONAFTERCONTROLCOMMAND	= 3;
	private static final int MESSAGE_METER_RECORDING_BEEP		= 4;
	
    private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATEINFO:
                	if (flVisible)
                		Update();  
                	break; //. >

                case MESSAGE_SHOWMESSAGE:
                	if (flVisible) {
                		String Message = (String)msg.obj;
	        			Toast.makeText(TTrackerPanel.this.getApplicationContext(), Message, Toast.LENGTH_LONG).show();  						
                	}
                	break; //. >
                	
                case MESSAGE_DOONAFTERCONTROLCOMMAND:
            		TControlDescriptor CD = (TControlDescriptor)msg.obj;
            		ControlCommand_DoNotifyOnAfterCommand(CD.Command,CD.Method);
                	break; //. >
                	
                case MESSAGE_METER_RECORDING_BEEP:
                	if (SensorsModule_ControlMeter_flActive && (SensorsModule_AudioNotifier != null))
                		SensorsModule_AudioNotifier.Notification_MeterRecordingBeep();
                	break; //. >
               }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };

    private class TUpdaterTask extends TimerTask {
    	
        private TTrackerPanel _TrackerPanel;
        
        public TUpdaterTask(TTrackerPanel pTrackerPanel) {
            _TrackerPanel = pTrackerPanel;
        }
        
        @Override
        public void run() {
        	try {
        		if (flVisible)
        			_TrackerPanel.MessageHandler.obtainMessage(TTrackerPanel.MESSAGE_UPDATEINFO).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }   

    private class TMeterRecordingNotifyingTask extends TimerTask {
    	
        private TTrackerPanel _TrackerPanel;
        //.
        private int NotificationType;
        
        public TMeterRecordingNotifyingTask(TTrackerPanel pTrackerPanel, int pNotificationType) {
            _TrackerPanel = pTrackerPanel;
            NotificationType = pNotificationType;
        }
        
        @Override
        public void run() {
        	try {
            	if (_TrackerPanel.Tracker.GeoLog.IsEnabled() && SensorsModule_ControlMeter_flActive) {
            		int MetersCount = 0;
                	String[] _MetersToControl = Configuration.SensorsModuleConfiguration.MeterToControl.split(",");
            		int CntI = _MetersToControl.length;
                	for (int I = 0; I < CntI; I++) {
                    	TSensorMeter MeterToControl = Tracker.GeoLog.SensorsModule.Meters.Items_GetItem(_MetersToControl[I]);
                    	if (MeterToControl != null)   
                    		MetersCount++;
                	}
                	if ((MetersCount > 0) && (MetersCount == CntI)) 
                		switch (NotificationType) {
                		
						case TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_BEEPING:
	                    	_TrackerPanel.MessageHandler.obtainMessage(TTrackerPanel.MESSAGE_METER_RECORDING_BEEP).sendToTarget();
							break; //. ->
							
						case TConfiguration.TSensorsModuleConfiguration.METERRECORDING_NOTIFICATION_VIBRATING:
					    	int Duration = 50; //. ms
							Vibrator.vibrate(Duration);
							break; //. ->
						}
            	}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }   
}
