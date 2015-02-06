/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICEModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.Log.TRollingLogFile;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines.TTypedDataFile;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ADCModule.TADCModule;
import com.geoscope.GeoLog.DEVICE.AlarmModule.TAlarmModule;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.BatteryModule.TBatteryModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ControlModule.TControlModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.DACModule.TDACModule;
import com.geoscope.GeoLog.DEVICE.DataStreamerModule.TDataStreamerModule;
import com.geoscope.GeoLog.DEVICE.FileSystemModule.TFileSystemModule;
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIModule;
import com.geoscope.GeoLog.DEVICE.GPOModule.TGPOModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.OSModule.TOSModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.SensorModule.TSensorModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract.TStreamer.TBuffer;

/**
 *
 * @author ALXPONOM
 */
@SuppressLint("SimpleDateFormat")
public class TDEVICEModule extends TModule 
{
	public static final String ObjectBusinessModel = "101.2";
		
	public static String GetTempFolder() {
		return TGeoLogApplication.GetTempFolder();
	}
	//.
	public static String GetLogFolder() {
		return TGeoLogApplication.GetLogFolder();
	}
	//.
	public static final String ProfileFolder() {
		return TGeoLogApplication.ProfileFolder();
	}
	//.
	public static final String DeviceFolderName = "Device";
	//.
	public static String DeviceFolder() {
		return ProfileFolder()+"/"+DeviceFolderName;
	}
	//.
	public static final String 	DeviceOldFileName = "Device.xml";
	public static final String 	DeviceFileName = "Data.xml";
	public static final String 	DeviceLogFileName = "Device.log";
	
	public static class TSensorMeasurementDescriptor {
		
		public static final int LOCATION_DEVICE = 0;
		public static final int LOCATION_SERVER = 1;
		public static final int LOCATION_CLIENT = 2;
		
		public static class TLocationUpdater {
			
			public void DoOnLocationUpdated(String MeasurementID, int Location) {
			}
		}
		
		private static final double MaxMeasurementIDDeviation = 1.0/(24.0*3600.0);
		//.
		public static boolean IDsAreTheSame(String ID, String ID1) {
			return (Math.abs(Double.parseDouble(ID)-Double.parseDouble(ID1)) < MaxMeasurementIDDeviation);
		}

		
		public String ID = "";
		//.
		public String TypeID = ""; 				//. media type
		public String ContainerTypeID = ""; 	//. container(format) type
		//.
		public double StartTimestamp = 0.0;
		public double FinishTimestamp = 0.0;
		//.
		public int Location = LOCATION_DEVICE;

		public TSensorMeasurementDescriptor() {
		}

		public TSensorMeasurementDescriptor(String pID) {
			ID = pID;
		}
		
		public boolean IsStarted() {
			return (StartTimestamp != 0.0);
		}

		public boolean IsFinished() {
			return (FinishTimestamp != 0.0);
		}
		
		public boolean IsValid() {
			return (IsStarted() && IsFinished());
		}
		
		public double Duration() {
			return (FinishTimestamp-StartTimestamp);
		}

		public int DurationInMs() {
			return (int)(Duration()*24.0*3600.0*1000.0);
		}

		public long DurationInNs() {
			return (long)(Duration()*24.0*3600.0*1000000000.0);
		}
	}
	
	
    public int 		UserID = 2;
    public String 	UserPassword = "ra3tkq";
    //.
    public int idTOwnerComponent = 0;
    public int idOwnerComponent = 0;
    //.
    public int idGeographServerObject = 0;
    public int ObjectID = 0;
    //.
    public TRollingLogFile Log;
    //.
    public Context context;
    //. components
    public TMovementDetectorModule 	MovementDetectorModule	= null;
    public TConnectorModule 		ConnectorModule			= null;
    public TGPSModule       		GPSModule				= null;
    public TGPIModule       		GPIModule				= null;
    public TGPOModule       		GPOModule				= null;
    public TADCModule 				ADCModule				= null;
    public TDACModule				DACModule 				= null;
    public TBatteryModule   		BatteryModule			= null;
    public TVideoRecorderModule 	VideoRecorderModule		= null;
    public TFileSystemModule		FileSystemModule		= null;
    public TControlModule			ControlModule			= null;
    public TSensorModule			SensorModule			= null;
    public TLANModule       		LANModule				= null;
    public TAudioModule				AudioModule				= null;
    public TVideoModule				VideoModule				= null;
    public TOSModule				OSModule				= null;
    public TTaskModule				TaskModule				= null;
    public TDataStreamerModule		DataStreamerModule		= null;
    public TControlsModule			ControlsModule			= null;
    public TSensorsModule			SensorsModule			= null;
    public TPluginsModule			PluginsModule			= null;
    public TAlarmModule				AlarmModule				= null;
    //.
    public boolean flUserInteractive = false;
    //.
    public TBackupMonitor BackupMonitor = null;
    //.
    public boolean 					flComponentFileStreaming = true;
    public TComponentFileStreaming 	ComponentFileStreaming;
    
    @SuppressLint("NewApi")
	public TDEVICEModule(Context pcontext) throws Exception
    {
    	super(null);
    	flEnabled = false;
    	//.
        ModuleState = MODULE_STATE_INITIALIZING;
        //.
		TFileSystem.TExternalStorage.WaitForMounted();
        //.
		File F = new File(DeviceFolder());
		if (!F.exists()) 
			F.mkdirs();
		//.
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 		 
		}
		//.
        context = pcontext;
        //.
        TGeoLogInstallator.CheckInstallation(context);
        //.
        Log = new TRollingLogFile(GetLogFolder()+"/"+DeviceLogFileName);
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SDeviceConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
        //. creating components
        MovementDetectorModule 	= new TMovementDetectorModule(this);
        GPSModule 				= new TGPSModule(this);
        GPIModule 				= new TGPIModule(this);
        GPOModule 				= new TGPOModule(this);
        ADCModule				= new TADCModule(this);
        DACModule				= new TDACModule(this);
        BatteryModule 			= new TBatteryModule(this);
        VideoRecorderModule 	= new TVideoRecorderModule(this);
        FileSystemModule 		= new TFileSystemModule(this);
        ControlModule 			= new TControlModule(this);
        SensorModule 			= new TSensorModule(this);
        LANModule 				= new TLANModule(this);
        AudioModule				= new TAudioModule(this);
        VideoModule				= new TVideoModule(this);
        OSModule				= new TOSModule(this);
        TaskModule				= new TTaskModule(this);
        DataStreamerModule		= new TDataStreamerModule(this);
        ControlsModule			= new TControlsModule(this);
        SensorsModule			= new TSensorsModule(this);
        PluginsModule			= new TPluginsModule(this);
        AlarmModule				= new TAlarmModule(this);
        ConnectorModule 		= new TConnectorModule(this); //. must be at end to be started at the end
        //.
        ComponentFileStreaming = new TComponentFileStreaming(this,flComponentFileStreaming);
        //.
        ModuleState = MODULE_STATE_INITIALIZED;
		Log.WriteInfo("Device", "initialized.");
		//. starting
        Start();
    }
    
    public void Destroy() throws Exception
    {
        //. stopping
        Stop();
        //.
        ModuleState = MODULE_STATE_FINALIZING;
    	//.
    	/*///? if (EventReceiver != null) {
    		context.getApplicationContext().unregisterReceiver(EventReceiver);
    		EventReceiver = null;
    	}*/
        //. save profile
        if (IsEnabled())
        	SaveProfile();
        //.
        if (ComponentFileStreaming != null) {
        	ComponentFileStreaming.Destroy();
        	ComponentFileStreaming = null;
        }
        //.
        if (ConnectorModule != null)
        {
            ConnectorModule.Destroy();
            ConnectorModule = null;
        }     
        if (AlarmModule != null) {
        	AlarmModule.Destroy();
        	AlarmModule = null;
        }
        if (PluginsModule != null) {
        	PluginsModule.Destroy();
        	PluginsModule = null;
        }
        if (SensorsModule != null) {
        	SensorsModule.Destroy();
        	SensorsModule = null;
        }
        if (ControlsModule != null) {
        	ControlsModule.Destroy();
        	ControlsModule = null;
        }
        if (DataStreamerModule != null) {
        	DataStreamerModule.Destroy();
        	DataStreamerModule = null;
        }
        if (TaskModule != null) {
        	TaskModule.Destroy();
        	TaskModule = null;
        }
        if (OSModule != null) {
        	OSModule.Destroy();
        	OSModule = null;
        }
        if (VideoModule != null) {
        	VideoModule.Destroy();
        	VideoModule = null;
        }
        if (AudioModule != null) {
        	AudioModule.Destroy();
        	AudioModule = null;
        }
        if (LANModule != null)
        {
            LANModule.Destroy();
            LANModule = null;
        }
        if (SensorModule != null) {
        	SensorModule.Destroy();
        	SensorModule = null;
        }
        if (ControlModule != null) {
        	ControlModule.Destroy();
        	ControlModule = null;
        }
        if (FileSystemModule != null) {
        	FileSystemModule.Destroy();
        	FileSystemModule = null;
        }
        if (VideoRecorderModule != null) {
        	VideoRecorderModule.Destroy();
        	VideoRecorderModule = null;
        }
        if (BatteryModule != null)
        {
        	BatteryModule.Destroy();
        	BatteryModule = null;
        }
        if (DACModule != null) 
        {
        	DACModule.Destroy();
        	DACModule = null;
        }
        if (ADCModule != null)
        {
        	ADCModule.Destroy();
        	ADCModule = null;
        }
        if (GPOModule != null)
        {
            GPOModule.Destroy();
            GPOModule = null;
        }
        if (GPIModule != null)
        {
            GPIModule.Destroy();
            GPIModule = null;
        }
        if (GPSModule != null)
        {
            GPSModule.Destroy();
            GPSModule = null;
        }
        if (MovementDetectorModule != null)
        {
        	MovementDetectorModule.Destroy();
        	MovementDetectorModule = null;
        }
        //.
        ModuleState = MODULE_STATE_FINALIZED;
        if (Log != null) {
    		Log.WriteInfo("Device", "finalized.");
            //.
        	Log.Destroy();
        	Log = null;
        }
    }
    
    @Override
    public void Start() throws Exception {
    	if (ModuleState == MODULE_STATE_RUNNING)
    		return; //. ->
    	//.
		if (IsEnabled()) {
	        //. start 
			super.Start();
			//. build and send controls/sensors schemas
			ControlsModule.Model_BuildAndPublish();
			SensorsModule.Model_BuildAndPublish();
			//.
	    	/*///? IntentFilter Filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    	Filter.addAction(Intent.ACTION_SCREEN_OFF);
	    	context.getApplicationContext().registerReceiver(EventReceiver,Filter);*/
			//.
			if (ComponentFileStreaming.flEnabledStreaming)
				ComponentFileStreaming.Start();
	        //.
	        BackupMonitor = new TBackupMonitor(this);
	        //.
	        ModuleState = MODULE_STATE_RUNNING;
	        Log.WriteInfo("Device", "started.");
		}
    }
    
    @Override
    public void Stop() throws Exception {
    	if (ModuleState != MODULE_STATE_RUNNING)
    		return; //. ->
    	//.
        if ((ConnectorModule != null) && ConnectorModule.IsEnabled())
        {
            //. send outgoing operations that remain in queue
            if (ConnectorModule.flProcessing) {
                int Timeout = 1/*seconds*/*10;
                for (int I = 0; I < Timeout; I++)
                {
                    if (!(ConnectorModule.flProcessingOperation || (ConnectorModule.OutgoingSetComponentDataOperationsQueue.QueueCount() > 0)))
                        break; //. >
                    //.
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (Exception E) {}
                }
            }
        }
        //. 
        if (BackupMonitor != null) {
        	BackupMonitor.Destroy();
        	BackupMonitor = null;
        }
        //.
		ComponentFileStreaming.Stop();
        //.
    	super.Stop();
    	//.
        ModuleState = MODULE_STATE_NOTRUNNING;
		Log.WriteInfo("Device", "stopped.");
    }
    
    public void SetEnabled(boolean pflEnable) throws Exception {
		flEnabled = pflEnable;
		//.
		SaveProfile();
		Restart();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
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
				NodeList NL;
				Node node = RootNode.getElementsByTagName("flEnabled").item(0).getFirstChild();
				if (node != null)
					flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("flAudioNotifications").item(0).getFirstChild();
				if (node != null)
					flAudioNotifications = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("UserID").item(0).getFirstChild();
				if (node != null)
					UserID = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("UserPassword").item(0).getFirstChild();
				if (node != null)
					UserPassword = node.getNodeValue();
				node = RootNode.getElementsByTagName("ObjectID").item(0).getFirstChild();
				if (node != null)
					ObjectID = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("idGeographServerObject").item(0).getFirstChild();
				if (node != null)
					idGeographServerObject = Integer.parseInt(node.getNodeValue());
				NL = RootNode.getElementsByTagName("flComponentFileStreaming");
				if (NL != null) {
					node = NL.item(0).getFirstChild();
					if (node != null)
						flComponentFileStreaming = (Integer.parseInt(node.getNodeValue()) != 0);
				}
			}
			catch (Exception E) {
			}
			break; //. >
			
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        int V = 0;
        if (flEnabled)
        	V = 1;
        Serializer.startTag("", "flEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flEnabled");
        //. 
        V = 0;
        if (flAudioNotifications)
        	V = 1;
        Serializer.startTag("", "flAudioNotifications");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flAudioNotifications");
        //.
        Serializer.startTag("", "UserID");
        Serializer.text(Integer.toString(UserID));
        Serializer.endTag("", "UserID");
        //.
        Serializer.startTag("", "UserPassword");
        Serializer.text(UserPassword);
        Serializer.endTag("", "UserPassword");
        //.
        Serializer.startTag("", "ObjectID");
        Serializer.text(Integer.toString(ObjectID));
        Serializer.endTag("", "ObjectID");
        //.
        Serializer.startTag("", "idGeographServerObject");
        Serializer.text(Integer.toString(idGeographServerObject));
        Serializer.endTag("", "idGeographServerObject");
        //.
        Serializer.startTag("", "ComponentFileStreaming");
        	V = 1;
        	if ((ComponentFileStreaming != null) && (!ComponentFileStreaming.flEnabledStreaming))
        		V = 0;
        	Serializer.startTag("", "flComponentFileStreaming");
        	Serializer.text(Integer.toString(V));
        	Serializer.endTag("", "flComponentFileStreaming");
        	//.
        Serializer.endTag("", "ComponentFileStreaming");
    }
    
    @Override
	public synchronized void SaveProfile() throws Exception {
		String CFN = ModuleFile();
		String TCFN = CFN+".tmp";
	    XmlSerializer Serializer = Xml.newSerializer();
	    FileWriter writer = new FileWriter(TCFN);
	    try {
	        Serializer.setOutput(writer);
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //.
	        SaveProfileTo(Serializer);
	        if (BatteryModule != null)
	        	BatteryModule.SaveProfileTo(Serializer);
	        if (MovementDetectorModule != null)
	        	MovementDetectorModule.SaveProfileTo(Serializer);
	        if (ConnectorModule != null)
	        	ConnectorModule.SaveProfileTo(Serializer);
	        if (GPSModule != null)
	        	GPSModule.SaveProfileTo(Serializer);
	        if (GPIModule != null)
	        	GPIModule.SaveProfileTo(Serializer);
	        if (GPOModule != null)
	        	GPOModule.SaveProfileTo(Serializer);
	        if (VideoRecorderModule != null)
	        	VideoRecorderModule.SaveProfileTo(Serializer);
	        if (FileSystemModule != null)
	        	FileSystemModule.SaveProfileTo(Serializer);
	        if (DataStreamerModule != null)
	        	DataStreamerModule.SaveProfileTo(Serializer);
	        if (ControlModule != null)
	        	ControlModule.SaveProfileTo(Serializer);
	        if (AudioModule != null)
	        	AudioModule.SaveProfileTo(Serializer);
	        //.
	        Serializer.endTag("", "ROOT");
	        Serializer.endDocument();
	    }
	    finally {
	    	writer.close();
	    }
		File TF = new File(TCFN);
		File F = new File(CFN);
		TF.renameTo(F);
	}
	
    @SuppressWarnings("deprecation")
	public String GetStateInfo() {
    	//. memory state
        StringBuffer memoryInfo = new StringBuffer();
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        memoryInfo.append("\nTotal Available Memory : ").append(outInfo.availMem >> 20).append("M");
        memoryInfo.append("\nIn low memory situation: ").append(outInfo.lowMemory);
        memoryInfo.append("\n");
        memoryInfo.append("\nProcess processors: ").append(Runtime.getRuntime().availableProcessors());
        memoryInfo.append("\nProcess Memory: ").append(Runtime.getRuntime().maxMemory() >> 20).append("M");
        memoryInfo.append("\nProcess Free Memory: ").append((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().freeMemory()) >> 20).append("M");
        memoryInfo.append("\n");
        StatFs SDstat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        memoryInfo.append("\nSD card size: ").append(((long)SDstat.getBlockCount()*(long)SDstat.getBlockSize())/(1024*1024)).append("M");
        memoryInfo.append("\nSD card free size: ").append(((long)SDstat.getAvailableBlocks()*(long)SDstat.getBlockSize())/(1024*1024)).append("M");
        memoryInfo.append("\n");
        TConnectionRepeater.TRepeatersStatistic RepeatersStatistic = TConnectionRepeater.Repeaters_GetStatistics(); 
        memoryInfo.append("\nLAN repeaters: ");
        memoryInfo.append("\n  Device connections: ").append(RepeatersStatistic.DeviceConnections);
        memoryInfo.append("\n  Local connections: ").append(RepeatersStatistic.LocalConnections);
        memoryInfo.append("\n  Remote connections: ").append(RepeatersStatistic.Connections-RepeatersStatistic.DeviceConnections-RepeatersStatistic.LocalConnections);
        memoryInfo.append("\n    Summary: ").append(RepeatersStatistic.Connections);
        //.
        return memoryInfo.toString();
    }
    
    protected BroadcastReceiver EventReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
    			flUserInteractive = true;
    			//.
    	    	return; //. ->
    		}
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
    			flUserInteractive = false;
    			//.
    	    	return; //. ->
    		}
    	}
    };
    
    public static class TBackupMonitor {
    	
    	public static final int BackupInterval = 1000*300/*seconds*/;
    	public static final int ImmediateBackupCounter = 12; 
    	public static final int ConnectorModule_OutgoingSetComponentDataOperationsQueue_ChangesCountThreshold = 50; 
    	
    	private int TickCount = 0;
    	private TDEVICEModule DEVICEModule;
    	private TBackingUp BackingUp;
    	
    	private TBackupMonitor(TDEVICEModule pDEVICEModule) {
    		DEVICEModule = pDEVICEModule;
    		//.
    		BackingUp = new TBackingUp(this);
    	}
    	
    	private void Destroy() {
    		if (BackingUp != null) {
    			BackingUp.Cancel();
    			BackingUp = null;
    		}
    	}
    	
    	public void BackupImmediate() {
    		BackingUp.Process();
    	}
    	
    	public class TBackingUp implements Runnable {

    		private TBackupMonitor BackupMonitor;
    		private Thread _Thread;
        	private boolean flCancel = false;
        	private int ImmediateProcessCounter = 0;
        	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    		//.
    		private int ConnectorModule_OutgoingSetComponentDataOperationsQueue_ChangesCount = 0;
    		private boolean ConnectorModule_OutgoingSetComponentDataOperationsQueue_flEmpty = false;
    		
    		public TBackingUp(TBackupMonitor pBackupMonitor) {
    			BackupMonitor = pBackupMonitor;
    			_Thread = new Thread(this);
    			_Thread.start();
    		}
    		
			@Override
			public void run() {
				try {
					while (!flCancel) {
						ProcessSignal.WaitOne(BackupInterval);
						if (flCancel)
							return; //. ->
						//.
						boolean flProcessImmediate;
						synchronized (this) {
							flProcessImmediate = (ImmediateProcessCounter > 0);
						}
						//.
						ProcessForConnectorModuleBackup(flProcessImmediate);
						//.
						synchronized (this) {
							if (ImmediateProcessCounter > 0)
								ImmediateProcessCounter--;
						}
						//.
						if (!flProcessImmediate)
							BackupMonitor.TickCount++;
					}
				}
				catch (InterruptedException IE) {
				}
				catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
				}
			}
			
			public void Cancel() {
				flCancel = true;
				//.
				ProcessSignal.Set();
			}
			
			public void Process() {
				synchronized (this) {
					ImmediateProcessCounter++;
				}
				//.
				ProcessSignal.Set();			
			}
			
			private void ProcessForConnectorModuleBackup(boolean flImmediateProcess) {
				boolean flBackup = flImmediateProcess;
				int ChangesCount = BackupMonitor.DEVICEModule.ConnectorModule.OutgoingSetComponentDataOperationsQueue.GetQueueChangesCount();
				if (!flBackup) {
					int ChangesDelta = ChangesCount-ConnectorModule_OutgoingSetComponentDataOperationsQueue_ChangesCount; 
					flBackup = ((ChangesDelta >= ConnectorModule_OutgoingSetComponentDataOperationsQueue_ChangesCountThreshold) || (((BackupMonitor.TickCount % TBackupMonitor.ImmediateBackupCounter) == 0) && (ChangesDelta > 0)));
					
				}
				if (flBackup) {
					try {
						byte[] QueueData = BackupMonitor.DEVICEModule.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Saving_ToByteArray();
						if ((QueueData != null) || !ConnectorModule_OutgoingSetComponentDataOperationsQueue_flEmpty)
							BackupMonitor.DEVICEModule.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Save(QueueData);
						//.
						ConnectorModule_OutgoingSetComponentDataOperationsQueue_flEmpty = (QueueData == null);
						ConnectorModule_OutgoingSetComponentDataOperationsQueue_ChangesCount = ChangesCount;
					} catch (Throwable E) {
					}
				}
			}
    	}
    }
    
    public static class TComponentFileStreaming extends TCancelableThread {
    	
    	public static final int CONNECTION_TYPE_PLAIN 		= 0;
    	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
    	
    	public static final String ItemsFileName = "ComponentFileStreaming.xml"; 
    	public static final int StreamingAttemptSleepTime = 1000*60; //. seconds
    	
    	public static class TItem {
    		
    		public int idTComponent;
    		public long idComponent;
    		//.
    		public String 	FileName = null;
    		public short 	FileCRCVersion = 0;
    		public int 		FileCRC = 0;
    		//.
    		public int ErrorCount = 0;
    		//.
    		public long TransmittedSize = 0;

    	    private TTypedDataFile TypedDataFile = null;
    	    
    	    public synchronized TTypedDataFile GetTypedDataFile() {
    	    	if ((FileName == null) || (!FileName.contains(".")))
    				return null; //. ->
    	    	if (TypedDataFile == null)
    	    		TypedDataFile = new TTypedDataFile(FileName);
    	    	return TypedDataFile; 
    	    }
    	}
    	
    	public static final int StreamSignalTimeout = 1000*60; //. seconds
    	
    	public static final int ConnectTimeout = 1000*5; //. seconds
    	public static final int CommittingTimeout = 1000*60; //. seconds
    	
    	public static final short SERVICE_SETCOMPONENTSTREAM_V2V2 = 9;
    	//.
    	public static final int MESSAGE_DISCONNECT = 0;
    	//. error messages
    	public static final int MESSAGE_OK                    = 0;
    	public static final int MESSAGE_ERROR                 = -1;
    	public static final int MESSAGE_UNKNOWNSERVICE        = 10;
    	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
    	public static final int MESSAGE_ACCESSISDENIED        = -12;
    	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
    	public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
    	public static final int MESSAGE_WRONGPARAMETERS       = -15;
    	public static final int MESSAGE_SAVINGDATAERROR       = -101;
    	//.
    	public static String	MESSAGE_GetString(int Code) {
    		switch (Code) {
    		
    		case MESSAGE_OK:
    			return "OK"; //. ->
    			
    		case MESSAGE_ERROR:
    			return "?"; //. ->

    		case MESSAGE_UNKNOWNSERVICE:
    			return "unknown service"; //. ->
    			
    		case MESSAGE_AUTHENTICATIONFAILED:
    			return "authentication failed"; //. ->
    			
    		case MESSAGE_ACCESSISDENIED:
    			return "access denied"; //. ->
    			
    		case MESSAGE_TOOMANYCLIENTS:
    			return "too many clients"; //. ->
    			
    		case MESSAGE_UNKNOWNCOMMAND:
    			return "unknown command"; //. ->
    			
    		case MESSAGE_WRONGPARAMETERS:
    			return "wrong parameters"; //. ->
    			
    		case MESSAGE_SAVINGDATAERROR:
    			return "data saving error"; //. ->

    		default:
    			return "Code: "+Integer.toString(Code); //. ->
    		}
    	}
    	
    	public static class StreamingErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public long Code;
			
			public StreamingErrorException(int pCode) {
    			super("Error: "+MESSAGE_GetString(pCode));
    			Code = pCode;
    		}

			public StreamingErrorException(long pCode) {
    			super("Error: "+Long.toString(pCode));
    			Code = pCode;
    		}

			public StreamingErrorException(int pCode, String pMessage) {
    			super(pMessage);
    			Code = pCode;
    		}

			public StreamingErrorException(long pCode, String pMessage) {
    			super(pMessage);
    			Code = pCode;
    		}
			
			public int GetCode() {
				return (int)Code;
			}
    	}
    	
    	private TDEVICEModule DEVICEModule;
        //.
    	public boolean flEnabledStreaming = true;
    	//.
    	private Boolean 		flStarted = false;
    	public boolean 			flStreaming = false;
    	private TAutoResetEvent StreamSignal = new TAutoResetEvent();
    	public boolean 			flStreamingComponent = false;
    	//.
        public String 	ServerAddress = null;
        public int		ServerPort = 5000;
    	protected int 	SecureServerPortShift = 2;
    	protected int	SecureServerPort() {
        	return (ServerPort+SecureServerPortShift);
        }
        //.
        public int			ConnectionType() {
        	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
        }
        private Socket 		Connection = null;
        public InputStream 	ConnectionInputStream = null;
        public OutputStream ConnectionOutputStream = null;
    	//.
    	private ArrayList<TItem> Items = new ArrayList<TItem>();
    	
    	public TComponentFileStreaming(TDEVICEModule pDEVICEModule, boolean pflEnabledStreaming) throws Exception {
    		DEVICEModule = pDEVICEModule;
    		flEnabledStreaming = pflEnabledStreaming;
    		//.
    		Load();
    	}
    	
    	public void Destroy() throws InterruptedException {
    		Stop();
    	}
    	
    	@Override
    	public void Cancel() {
    		super.Cancel();
    		//.
    		try {
    			if (Connection != null) {
            		synchronized (Connection) {
        				if (ConnectionInputStream != null) 
        					ConnectionInputStream.close();
        				if (ConnectionOutputStream != null) 
        					ConnectionOutputStream.close();
        			}
    			}
    		}
    		catch (Exception E) {}
    	}
    	
    	private synchronized void Load() throws Exception {
			Items.clear();
    		String FN = TDEVICEModule.DeviceFolder()+"/"+ItemsFileName;
    		File F = new File(FN);
    		if (!F.exists()) 
    			return; //. ->
    		//.
    		byte[] XML;
        	long FileSize = F.length();
        	FileInputStream FIS = new FileInputStream(FN);
        	try {
        		XML = new byte[(int)FileSize];
        		FIS.read(XML);
        	}
        	finally {
        		FIS.close();
        	}
        	Document XmlDoc;
    		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
    		try {
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
    			factory.setNamespaceAware(true);     
    			DocumentBuilder builder = factory.newDocumentBuilder(); 			
    			XmlDoc = builder.parse(BIS); 
    		}
    		finally {
    			BIS.close();
    		}
    		int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
    		switch (Version) {

    		case 1:
    			NodeList NL = XmlDoc.getDocumentElement().getElementsByTagName("Items");
    			if (NL != null) {
    				NodeList ItemsNode = NL.item(0).getChildNodes();
    				for (int I = 0; I < ItemsNode.getLength(); I++) {
    					Node ItemNode = ItemsNode.item(I);
    					NodeList ItemChilds = ItemNode.getChildNodes();
    					Node ValueNode;
    					//.
    					TItem Item = new TItem();
    					Item.idTComponent = Integer.parseInt(ItemChilds.item(0).getFirstChild().getNodeValue());
    					Item.idComponent = Long.parseLong(ItemChilds.item(1).getFirstChild().getNodeValue());
    					//.
    					ValueNode = ItemChilds.item(2).getFirstChild();
    					if (ValueNode != null)
    						Item.FileName = ValueNode.getNodeValue();
    					//.
    					Item.ErrorCount = Integer.parseInt(ItemChilds.item(3).getFirstChild().getNodeValue());
    					//.
    					if (Item.FileName != null)
    						Items.add(Item);
    				}
    			}
    			break; //. >

    		case 2:
    			NL = XmlDoc.getDocumentElement().getElementsByTagName("Items");
    			if (NL != null) {
    				NodeList ItemsNode = NL.item(0).getChildNodes();
    				for (int I = 0; I < ItemsNode.getLength(); I++) {
    					Node ItemNode = ItemsNode.item(I);
    					NodeList ItemChilds = ItemNode.getChildNodes();
    					Node ValueNode;
    					//.
    					TItem Item = new TItem();
    					Item.idTComponent = Integer.parseInt(ItemChilds.item(0).getFirstChild().getNodeValue());
    					Item.idComponent = Long.parseLong(ItemChilds.item(1).getFirstChild().getNodeValue());
    					//.
    					ValueNode = ItemChilds.item(2).getFirstChild();
    					if (ValueNode != null)
    						Item.FileName = ValueNode.getNodeValue();
    					Item.FileCRCVersion = Short.parseShort(ItemChilds.item(3).getFirstChild().getNodeValue());
    					Item.FileCRC = Integer.parseInt(ItemChilds.item(4).getFirstChild().getNodeValue());
    					//.
    					Item.ErrorCount = Integer.parseInt(ItemChilds.item(5).getFirstChild().getNodeValue());
    					//.
    					if (Item.FileName != null)
    						Items.add(Item);
    				}
    			}
    			break; //. >
    		default:
    			throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
    		}
    	}
    	
    	public synchronized void Save() throws Exception {
    	    String FN = TDEVICEModule.DeviceFolder()+"/"+ItemsFileName;
            File F = new File(FN);
            if (Items.size() == 0) {
            	F.delete();
            	return; //. ->
            }
    	    if (!F.exists()) 
    	    	F.getParentFile().mkdirs();
    	    String TFN = FN+".tmp";
        	int Version = 2;
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
    	        //. Items
                serializer.startTag("", "Items");
                	for (int I = 0; I < Items.size(); I++) {
                		TItem Item = Items.get(I);
    	            	serializer.startTag("", "Item"+Integer.toString(I));
    	            		//. idTComponent
    	            		serializer.startTag("", "idTComponent");
    	            		serializer.text(Integer.toString(Item.idTComponent));
    	            		serializer.endTag("", "idTComponent");
    	            		//. idComponent
    	            		serializer.startTag("", "idComponent");
    	            		serializer.text(Long.toString(Item.idComponent));
    	            		serializer.endTag("", "idComponent");
    	            		//. FileName
    	            		serializer.startTag("", "FileName");
    	            		serializer.text(Item.FileName);
    	            		serializer.endTag("", "FileName");
    	            		//. FileCRCVersion
    	            		serializer.startTag("", "FileCRCVersion");
    	            		serializer.text(Integer.toString(Item.FileCRCVersion));
    	            		serializer.endTag("", "FileCRCVersion");
    	            		//. FileCRC
    	            		serializer.startTag("", "FileCRC");
    	            		serializer.text(Integer.toString(Item.FileCRC));
    	            		serializer.endTag("", "FileCRC");
    	            		//. ErrorCount
    	            		serializer.startTag("", "ErrorCount");
    	            		serializer.text(Integer.toString(Item.ErrorCount));
    	            		serializer.endTag("", "ErrorCount");
    	            	serializer.endTag("", "Item"+Integer.toString(I));
                	}
                serializer.endTag("", "Items");
                //.
    	        serializer.endTag("", "ROOT");
    	        serializer.endDocument();
    	    }
    	    finally {
    	    	writer.close();
    	    }
    		File TF = new File(TFN);
    		TF.renameTo(F);
    	}	
    	
    	public synchronized void Clear() throws Exception {
        	for (int I = 0; I < Items.size(); I++) {
        		TItem Item = Items.get(I);
        		File F = new File(Item.FileName);
        		F.delete();
        	}
        	//.
    		Items.clear();
    		//.
    		Save();
    	}
    	
    	public synchronized void AddItem(int pidTComponent, long pidComponent, String pFileName) throws Exception {
    		TItem NewItem = new TItem();
    		NewItem.idTComponent = pidTComponent;
    		NewItem.idComponent = pidComponent;
    		//.
    		NewItem.FileName = pFileName;
    		//.
    		Items.add(0,NewItem);
    		//.
    		Save();
    		//.
    		Process();
    	}
    	
    	private synchronized void RemoveItem(TItem Item) throws Exception {
    		File F = new File(Item.FileName);
    		F.delete();
    		//.
    		Items.remove(Item);
    		//.
    		Save();
    	}
    	
    	public synchronized void RemoveItem(int ItemHashCode) throws Exception {
        	for (int I = 0; I < Items.size(); I++) {
        		TItem Item = Items.get(I);
        		if (Item.hashCode() == ItemHashCode) {
        			RemoveItem(Item);
            		//.
            		return; //. ->
        		}
        	}
    	}
    	
    	public synchronized void RemoveLastItem() throws Exception {
    		TItem LastItem = GetLastItem();
    		if (LastItem == null)
    			return; //. ->
    		RemoveItem(LastItem);
    	}
    	
    	public synchronized TItem[] GetItems() {
    		int ItemsCount = Items.size(); 
    		TItem[] Result = new TItem[ItemsCount]; 
        	for (int I = 0; I < ItemsCount; I++) 
        		Result[ItemsCount-I-1] = Items.get(I);
        	return Result;
    	}
    	
    	private synchronized TItem GetLastItem() {
    		if (Items.size() > 0)
    			return Items.get(Items.size()-1); //. ->
    		else
    			return null; //. ->
    	}
    	
    	public static class TItemsStatistics {
    		public int Count = 0;
    		public long Size = 0;
    	}
    	
    	public synchronized TItemsStatistics GetItemsStatistics() {
    		TItemsStatistics Result = new TItemsStatistics();
    		for (int I = 0; I < Items.size(); I++) {
    			TItem Item = Items.get(I);
    			File F = new File(Item.FileName);
    			Result.Size += (F.length()-Item.TransmittedSize);
    			Result.Count++;
    		}
    		return Result;
    	}
    	
    	public synchronized int GetItemsCount() {
    		return Items.size();
    	}
    	
    	public void SupplyItemsWithFileInfo() throws Exception {
    		short CRCVersion = 1;
    		ArrayList<TItem> ItemsToProcess = null;
    		synchronized (this) {
	    		for (int I = 0; I < Items.size(); I++) {
    				TItem Item = Items.get(I);
    				//. check for File CRC
					if (Item.FileCRCVersion != CRCVersion) {
						if (ItemsToProcess == null)
							ItemsToProcess = new ArrayList<TItem>();
						//.
						ItemsToProcess.add(Item); 
    				}			
    			}
			}
			if (ItemsToProcess != null) {
				for (int I = 0; I < ItemsToProcess.size(); I++) {
    				TItem Item = ItemsToProcess.get(I);
    				//. supply item with File CRC
					if (Item.FileCRCVersion != CRCVersion) {
						Item.FileCRC = TFileSystem.File_GetCRCV1(Item.FileName);
						Item.FileCRCVersion = CRCVersion;
					}
	    		}
				Save();
			}
    	}
    	
    	public void Start() {
    		synchronized (flStarted) {
        		Canceller.flCancel = false;
        		//.
        		_Thread = new Thread(this);
        		_Thread.start();
        		//.
        		flStarted = true;
    		}
    	}
    	
    	public void Stop() throws InterruptedException {
    		synchronized (flStarted) {
        		if (_Thread != null) {
        			Cancel();
        			Process();
            		Wait();
            		//.
            		flStarted = false;
            		//.
            		_Thread = null;
        		}
    		}
    	}
    	
    	public boolean IsStarted() {
    		synchronized (flStarted) {
        		return flStarted; //. ->
    		}
    	}
    	
    	public boolean IsStreaming() {
    		return flStreaming;
    	}
    	
    	public boolean IsEnabledStreaming() {
    		return flEnabledStreaming;
    	}
    	
    	public void SetEnabledStreaming(boolean pflEnabledStreaming) throws Exception {
    		if (pflEnabledStreaming == flEnabledStreaming)
    			return; //. ->
    		if (pflEnabledStreaming) {
    			if (!IsStarted())
    				Start();
    		}
    		else {
    			if (IsStarted())
    				Stop();
    		}
    		flEnabledStreaming = pflEnabledStreaming;
			DEVICEModule.SaveProfile();
    	}
    	
		@Override
		public void run() {
	        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
	        //.
			byte[] TransferBuffer = new byte[1024*64];
			flStreaming = true;
			try {
				try {
					while (!Canceller.flCancel) {
						if (ServerAddress != null) {
							//. streaming ...
							TItem StreamItem = GetLastItem();
							while (StreamItem != null) {
								try {
									SupplyItemsWithFileInfo();
									//.
									flStreamingComponent = true;
									try {
										while (!Canceller.flCancel) {
											try {
												StreamItem(StreamItem,TransferBuffer);
											}
											catch (InterruptedException E) {
												return; //. ->
											}
											catch (CancelException CE) {
												return; //. ->
											}
											catch (SocketTimeoutException STE) {
												break; //. >
											}
											catch (IOException IOE) {
												break; //. >
											}
											catch (Exception E) {
												String S = E.getMessage();
												if (S == null)
													S = E.getClass().getName();
												DEVICEModule.Log.WriteWarning("DEVICEModule.ComponentFileStreaming","Failed attempt to stream file: "+StreamItem.FileName+", Component("+Integer.toString(StreamItem.idTComponent)+";"+Long.toString(StreamItem.idComponent)+")"+", "+S);
												//.
												boolean flRetry = true;
												if (E instanceof StreamingErrorException) {
													StreamingErrorException SEE = (StreamingErrorException)E;
													switch (SEE.GetCode()) {
													
													case MESSAGE_UNKNOWNSERVICE:
													case MESSAGE_UNKNOWNCOMMAND:
													case MESSAGE_WRONGPARAMETERS:
													case MESSAGE_SAVINGDATAERROR:
														flRetry = false;
														break; //. >
													}
												}
												//.
												if (flRetry) {
													StreamItem.ErrorCount++;
													Save();
													//.
													Thread.sleep(StreamingAttemptSleepTime);
													//.
													if (Canceller.flCancel)
														return; //. ->
													//.
													continue; //. ^
												}
											}
											//.
											RemoveItem(StreamItem);
											//. next one
											StreamItem = GetLastItem();
											//.
											break; //. >
										}
										if (Canceller.flCancel)
											return; //. ->
										}
									finally {
										flStreamingComponent = false;
									}
								}
								catch (InterruptedException E) {
									return; //. ->
								}
								catch (CancelException CE) {
									return; //. ->
								}
								catch (Throwable TE) {
					            	//. log errors
									String S = TE.getMessage();
									if (S == null)
										S = TE.getClass().getName();
									DEVICEModule.Log.WriteError("DEVICEModule.ComponentFileStreaming",S);
					            	if (!(TE instanceof Exception))
					            		TGeoLogApplication.Log_WriteCriticalError(TE);
								}
							}
						}
						else {
							if ((DEVICEModule.ConnectorModule != null) && (DEVICEModule.ConnectorModule.flProcessing)) {
								ServerAddress = DEVICEModule.ConnectorModule.GetGeographDataServerAddress();
								ServerPort = DEVICEModule.ConnectorModule.GetGeographDataServerPort();
							}
						}
						//.
						StreamSignal.WaitOne(StreamSignalTimeout);
					}
				}
				catch (InterruptedException E) {
				}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
			}
			finally {
				flStreaming = false;
			}
		}
		
		public void Process() {
			StreamSignal.Set();
		}
		
		public boolean IsStreamingComponent() {
			return flStreamingComponent;
		}
		
		private final int Connect_TryCount = 3;
		
	    private void Connect() throws Exception {
			int TryCounter = Connect_TryCount;
			while (true) {
				try {
					try {
						//. connect
				    	switch (ConnectionType()) {
				    	
				    	case CONNECTION_TYPE_PLAIN:
				            Connection = new Socket(ServerAddress,ServerPort); 
				    		break; //. >
				    		
				    	case CONNECTION_TYPE_SECURE_SSL: 
				    		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
				    	        @Override
				    	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public X509Certificate[] getAcceptedIssuers() {
				    	            return null;
				    	        }
				    	    } };
				    	    //. install the all-trusting trust manager
				    	    SSLContext sslContext = SSLContext.getInstance("SSL");
				    	    sslContext.init( null, _TrustAllCerts, new SecureRandom());
				    	    //. create a ssl socket factory with our all-trusting manager
				    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
				    		break; //. >
				    		
				    	default:
				    		throw new Exception("unknown connection type, type: "+Integer.toString(ConnectionType())); //. =>
				    	}
				        Connection.setSoTimeout(ConnectTimeout);
				        //.
				        synchronized (Connection) {
					        ConnectionInputStream = Connection.getInputStream();
					        ConnectionOutputStream = Connection.getOutputStream();
						}
						break; //. >
					} catch (SocketTimeoutException STE) {
						throw new IOException(DEVICEModule.context.getString(R.string.SConnectionTimeoutError)); //. =>
					} catch (ConnectException CE) {
						throw new ConnectException(DEVICEModule.context.getString(R.string.SNoServerConnection)); //. =>
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.toString();
						throw new Exception(DEVICEModule.context.getString(R.string.SHTTPConnectionError)+S); //. =>
					}
				}
				catch (Exception E) {
					Canceller.Check();
					//.
					TryCounter--;
					if (TryCounter == 0)
						throw E; //. =>
				}
			}
	    }
	    
	    private void Disconnect(boolean flDisconnectGracefully) throws IOException {
	    	if (flDisconnectGracefully) {
		        //. close connection gracefully
		        byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(MESSAGE_DISCONNECT);
		        ConnectionOutputStream.write(BA);
		        ConnectionOutputStream.flush();
	    	}
	        //.
	    	try {
		    	if (Connection != null) {
			    	synchronized (Connection) {
			    		if (ConnectionOutputStream != null) {
					        ConnectionOutputStream.close();
					        ConnectionOutputStream = null;
			    		}
				        //.
			    		if (ConnectionInputStream != null) {
					        ConnectionInputStream.close();
					        ConnectionInputStream = null;
			    		}
					}
			    	//.
			        Connection.close();
			        Connection = null;
		    	}
	    	}
	    	catch (Exception E) {}
	    }
	    
	    @SuppressWarnings("unused")
		private void Disconnect() throws IOException {
	    	Disconnect(true);
	    }
	    
		private short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
	        int CRC = 0;
	        int V;
	        int Idx  = Offset;
	        while (Idx < (Offset+Size))
	        {
	            V = (int)(buffer[Idx] & 0x000000FF);
	            CRC = (((CRC+V) << 1)^V);
	            //.
	            Idx++;
	        }
	        return (short)CRC;
		}
		
		private void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
	        int StartIdx = Offset;
	        byte[] UserPasswordArray;
	        UserPasswordArray = UserPassword.getBytes("windows-1251");
	        //.
	        if (UserPasswordArray.length > 0)
	        {
	            int UserPasswordArrayIdx = 0;
	            for (int I = StartIdx; I < (StartIdx+Size); I++)
	            {
	                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
	                UserPasswordArrayIdx++;
	                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
	                	UserPasswordArrayIdx = 0;
	            }
	        }
		}
		
	    private void Login(int idTComponent, long idComponent) throws Exception {
	    	byte[] LoginBuffer = new byte[24];
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SERVICE_SETCOMPONENTSTREAM_V2V2);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(DEVICEModule.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(idTComponent);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray((int)idComponent);
			System.arraycopy(BA,0, LoginBuffer,14, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,12);
			BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,22, BA.length);
			Buffer_Encrypt(LoginBuffer,10,14,DEVICEModule.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
		private void StreamItem(TItem Item, byte[] TransferBuffer) throws Exception {
			synchronized (this) {
				Item.TransmittedSize = 0;
			}
			//.
			boolean flDisconnectGracefully = false;
			Connect();
			try {
				Login(Item.idTComponent,Item.idComponent);
				//.	
				File F = new File(Item.FileName);
				byte[] FileNameBA = F.getName().getBytes("windows-1251");
				byte[] DecriptorBA = new byte[4];
				int Descriptor = FileNameBA.length;
				DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				//.
				if (Descriptor > 0)  
					ConnectionOutputStream.write(FileNameBA);
				//. get the temporary file size on the server side 
				byte[] Decriptor64BA = new byte[8];
				ConnectionInputStream.read(Decriptor64BA);
				long Descriptor64 = TDataConverter.ConvertLEByteArrayToInt64(Decriptor64BA,0);
				if (Descriptor64 < 0) 
					throw new StreamingErrorException(Descriptor64); //. =>
				long ServerItemSize = Descriptor64;
				long SizeToStream = F.length()-ServerItemSize;
				//. send file offset
				if (SizeToStream > 0) {
					Descriptor64 = ServerItemSize; 
					Decriptor64BA = TDataConverter.ConvertInt64ToLEByteArray(Descriptor64);
					ConnectionOutputStream.write(Decriptor64BA);
				}
				else { //. unexpected EOF, try again
					Descriptor64 = -1; //. reset temp file message
					Decriptor64BA = TDataConverter.ConvertInt64ToLEByteArray(Descriptor64);
					ConnectionOutputStream.write(Decriptor64BA);
					//. check result
					ConnectionInputStream.read(DecriptorBA);
					Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
					if (Descriptor != MESSAGE_OK) 
						throw new StreamingErrorException(Descriptor); //. =>
					else
						throw new IOException("Error of streaming the file: "+Item.FileName+", unexpected EOF, restarting it again ..."); //. =>
				}
				//. send file size
				Descriptor64 = SizeToStream; 
				Decriptor64BA = TDataConverter.ConvertInt64ToLEByteArray(Descriptor64);
				ConnectionOutputStream.write(Decriptor64BA);
				//.
				FileInputStream FIS = new FileInputStream(F);
				try {
					if (ServerItemSize > 0) {
						FIS.skip(ServerItemSize);
						synchronized (this) {
							Item.TransmittedSize += ServerItemSize;
						}
					}
					try {
						while (SizeToStream > 0) {
							int BytesRead = FIS.read(TransferBuffer);
							if (BytesRead == -1)
								throw new IOException("unexpected end of stream"); //. =>
							//.
							ConnectionOutputStream.write(TransferBuffer,0,BytesRead);
							ConnectionOutputStream.flush();
							//.
							synchronized (this) {
								Item.TransmittedSize += BytesRead;
							}
							//.
							SizeToStream -= BytesRead;
							//. check for unexpected result
							if ((SizeToStream > 0) && (ConnectionInputStream.available() >= 4)) {
								ConnectionInputStream.read(DecriptorBA);
								Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
								if (Descriptor < 0) 
									throw new StreamingErrorException(Descriptor); //. =>
								else
									throw new StreamingErrorException(MESSAGE_ERROR,"unknown message during transmission"); //. =>
							}
							//.
							Canceller.Check(); 
						}
					}
					catch (Exception E) {
						synchronized (this) {
							Item.TransmittedSize = 0;
						}
						throw E; //. =>
					}
				}
				finally {
					FIS.close();
				}
				//. write File CRC
				byte[] CRCData = new byte[2/*SizeOf(Item.FileCRCVersion)*/+4/*SizeOf(Item.FileCRC)*/];
				byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(Item.FileCRCVersion);
				int Idx = 0;
				System.arraycopy(BA,0, CRCData,Idx, BA.length); Idx += BA.length; 
				BA = TDataConverter.ConvertInt32ToLEByteArray(Item.FileCRC);
				System.arraycopy(BA,0, CRCData,Idx, BA.length); Idx += BA.length; 
				ConnectionOutputStream.write(CRCData);
				//. check committing result
		        Connection.setSoTimeout(CommittingTimeout);
				ConnectionInputStream.read(DecriptorBA);
				Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
				if (Descriptor != MESSAGE_OK) 
					throw new StreamingErrorException(Descriptor); //. =>
				//.
				flDisconnectGracefully = true;
			}
			finally {
				Disconnect(flDisconnectGracefully);
			}
		}
    }

    public static class TComponentDataStreamingAbstract extends TCancelableThread {
    	
    	public static final int CONNECTION_TYPE_PLAIN 		= 0;
    	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
    	
    	public static class TStreamer {
    		
    		public static String TypeID() {
    			return "";
    		}
    		
    		public static class TBuffer {
    			
    			public byte[] 	Data;
    			public int 		Size;
    			
    			public TBuffer(int Capacity) {
    				SetCapacity(Capacity);
    				Size = 0;
    			}
    			
    			public void SetCapacity(int Capacity) {
    				Data = new byte[Capacity];
    			}
    			
    			public int Capacity() {
    				return Data.length;
    			}
    		}
    		
    		public static class TOutputStream extends ByteArrayOutputStream {
    			
    			private TStreamer Streamer;
    			
    			public TOutputStream(TStreamer pStreamer, int Capacity) {
    				super(Capacity);
    				Streamer = pStreamer;
    			}
    			
    			@Override
    			public void flush() throws IOException {
    				super.flush();
    				//.
    				byte[] BA = toByteArray();
    				Streamer.Streaming_SetData(BA,BA.length);
    				//.
    				super.reset();
    			}
    		}
    		
    		public static class TBufferHandler {
    			
    			public void DoOnBuffer(TBuffer StreamingBuffer) {
    			}
    		}
    	    	    		
    		protected TDEVICEModule Device;
    		//.
    		public int idTComponent;
    		public long idComponent;
    		//.
    		public int ChannelID;
    		//.
    		public String Configuration;
    		public String Parameters;
    		//.
    		public short DataSizeDescriptorLength;
    		//.
    		private TBuffer 		StreamingBuffer;
    		private int				StreamingBuffer_InitCapacity;
        	private TAutoResetEvent StreamingBuffer_ProcessSignal = new TAutoResetEvent();
        	public TOutputStream	StreamingBuffer_OutputStream;
        	public TBufferHandler 	StreamingBuffer_Handler = null;
    		
    		public TStreamer(TDEVICEModule pDevice, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters, int pDataSizeDescriptorLength, int pStreamingBuffer_InitCapacity) throws Exception {
    			Device = pDevice;
    			//.
    			idTComponent = pidTComponent;
    			idComponent = pidComponent;
    			//.
    			ChannelID = pChannelID;
    			//.
    			Configuration = pConfiguration;
    			//.
    			Parameters = pParameters;
    			//.
    			DataSizeDescriptorLength = (short)pDataSizeDescriptorLength;
    			//.
    			StreamingBuffer_InitCapacity = pStreamingBuffer_InitCapacity;
    			StreamingBuffer = new TBuffer(StreamingBuffer_InitCapacity);
    			StreamingBuffer_OutputStream = new TOutputStream(this,StreamingBuffer_InitCapacity);
    			//.
    			ParseConfiguration();
    		}
    		
    		public void Destroy() throws Exception {
    			Stop();
    		}
    		
    	    public void ParseConfiguration() throws Exception {
    	    }

    		public void Start() {
    		}
    		
    		public void Stop() throws Exception {
    		}
    		
    		public void SetStreamingHandler(TBufferHandler pBufferHandler) {
				synchronized (StreamingBuffer) {
					StreamingBuffer_Handler = pBufferHandler;
				}
    		}
    		
        	public int Streaming_Start() {
    			return StreamingBuffer_InitCapacity;
        	}

        	public void Streaming_Stop() {
        	}
        	
        	public boolean Streaming_SourceIsActive() {
        		return true;
        	}
        	
        	protected void Streaming_SetData(byte[] Data, int Size) {
				synchronized (StreamingBuffer) {
					if (StreamingBuffer.Capacity() < Size) 
						StreamingBuffer.SetCapacity(Size << 1);
					System.arraycopy(Data,0, StreamingBuffer.Data,0, Size);
					StreamingBuffer.Size = Size;
					//.
					if (StreamingBuffer_Handler != null)
						StreamingBuffer_Handler.DoOnBuffer(StreamingBuffer);
					else
						StreamingBuffer_ProcessSignal.Set();
				}
        	}
        	
        	public boolean Streaming_GetBuffer(TBuffer Buffer, TCanceller Canceller) throws Exception {
        		while (true) {
        			if (StreamingBuffer_ProcessSignal.WaitOne(100)) {
        				synchronized (StreamingBuffer) {
							if (StreamingBuffer.Size > Buffer.Capacity())
								Buffer.SetCapacity(StreamingBuffer.Size);
							System.arraycopy(StreamingBuffer.Data,0, Buffer.Data,0, StreamingBuffer.Size);
							Buffer.Size = StreamingBuffer.Size;
						}
        				return true; //. ->
        			}
        			else {
        				if (!Streaming_SourceIsActive() || Canceller.flCancel)
        					return false; //. ->
        			}
        		}
        	}
    	}
    	
    	public static final int StreamingConnectionTimeout = 1000*5; //. seconds
    	
    	public static final int ConnectTimeout = 1000*5; //. seconds
    	
    	public static final int MESSAGE_DISCONNECT = 0;


    	protected TDEVICEModule DEVICEModule;
    	//.
    	private Boolean 		flStarted = false;
    	public boolean 			flStreaming = false;
    	//.
        public String 	ServerAddress = null;
        public int		ServerPort = 0;
    	protected int 	SecureServerPortShift = 2;
    	protected int	SecureServerPort() {
        	return (ServerPort+SecureServerPortShift);
        }
        //.
        public int			ConnectionType() {
        	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
        }
        protected Socket 	Connection = null;
        public InputStream 	ConnectionInputStream = null;
        public OutputStream ConnectionOutputStream = null;
    	//.
    	protected TStreamer Streamer;
    	
    	public TComponentDataStreamingAbstract(TDEVICEModule pDEVICEModule, TStreamer pStreamer) {
    		DEVICEModule = pDEVICEModule;
    		Streamer = pStreamer;
    	}
    	
    	public void Destroy() throws InterruptedException {
    		Stop();
    	}
    	
    	@Override
    	public void Cancel() {
    		super.Cancel();
    		//.
    		try {
    			if (Connection != null) {
            		synchronized (Connection) {
        				if (ConnectionInputStream != null) 
        					ConnectionInputStream.close();
        				if (ConnectionOutputStream != null) 
        					ConnectionOutputStream.close();
        			}
    			}
    		}
    		catch (Exception E) {}
    	}
    	
    	public void Start() {
    		synchronized (flStarted) {
        		Canceller.flCancel = false;
        		//.
        		_Thread = new Thread(this);
        		_Thread.start();
        		//.
        		flStarted = true;
    		}
    	}
    	
    	public void Stop() throws InterruptedException {
    		synchronized (flStarted) {
        		if (_Thread != null) {
        			Cancel();
            		Wait();
            		//.
            		flStarted = false;
            		//.
            		_Thread = null;
        		}
    		}
    	}
    	
    	public boolean IsStarted() {
    		synchronized (flStarted) {
        		return flStarted; //. ->
    		}
    	}
    	
    	public boolean IsStreaming() {
    		return flStreaming;
    	}
    	
    	protected void GetServerInfo() {
    	}
    	
		@Override
		public void run() {
			try {
		        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		        //.
				while (!Canceller.flCancel) {
					if (ServerAddress != null) {
						//. streaming ...
						try {
							try {
								if (Streamer.Streaming_SourceIsActive())
									Streaming();
							}
							catch (InterruptedException IE) {
								return; //. ->
							}
							catch (CancelException CE) {
								return; //. ->
							}
							catch (Exception E) {
								String S = E.getMessage();
								if (S == null)
									S = E.getClass().getName();
								DEVICEModule.Log.WriteWarning("DEVICEModule.ComponentDataStreaming","Failed attempt to stream, Component("+Integer.toString(Streamer.idTComponent)+";"+Long.toString(Streamer.idComponent)+")"+", "+S);
							}
						}
						catch (Throwable TE) {
			            	//. log errors
							String S = TE.getMessage();
							if (S == null)
								S = TE.getClass().getName();
							DEVICEModule.Log.WriteError("DEVICEModule.ComponentDataStreaming",S);
			            	if (!(TE instanceof Exception))
			            		TGeoLogApplication.Log_WriteCriticalError(TE);
						}
					}
					else {
						if ((DEVICEModule.ConnectorModule != null) && (DEVICEModule.ConnectorModule.flProcessing)) 
							GetServerInfo();
					}
					//.
					Thread.sleep(StreamingConnectionTimeout);
				}
			}
			catch (InterruptedException E) {
			}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
		
		private final int Connect_TryCount = 3;
		
	    protected void Connect() throws Exception {
			int TryCounter = Connect_TryCount;
			while (true) {
				try {
					try {
						//. connect
				    	switch (ConnectionType()) {
				    	
				    	case CONNECTION_TYPE_PLAIN:
				            Connection = new Socket(ServerAddress,ServerPort); 
				    		break; //. >
				    		
				    	case CONNECTION_TYPE_SECURE_SSL: 
				    		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
				    	        @Override
				    	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public X509Certificate[] getAcceptedIssuers() {
				    	            return null;
				    	        }
				    	    } };
				    	    //. install the all-trusting trust manager
				    	    SSLContext sslContext = SSLContext.getInstance("SSL");
				    	    sslContext.init( null, _TrustAllCerts, new SecureRandom());
				    	    //. create a ssl socket factory with our all-trusting manager
				    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
				    		break; //. >
				    		
				    	default:
				    		throw new Exception("unknown connection type, type: "+Integer.toString(ConnectionType())); //. =>
				    	}
				        Connection.setTcpNoDelay(true);
				        Connection.setSoTimeout(ConnectTimeout);
				        //.
				        synchronized (Connection) {
					        ConnectionInputStream = Connection.getInputStream();
					        ConnectionOutputStream = Connection.getOutputStream();
						}
						break; //. >
					} catch (SocketTimeoutException STE) {
						throw new IOException(DEVICEModule.context.getString(R.string.SConnectionTimeoutError)); //. =>
					} catch (ConnectException CE) {
						throw new ConnectException(DEVICEModule.context.getString(R.string.SNoServerConnection)); //. =>
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.toString();
						throw new Exception(DEVICEModule.context.getString(R.string.SHTTPConnectionError)+S); //. =>
					}
				}
				catch (Exception E) {
					Canceller.Check();
					//.
					TryCounter--;
					if (TryCounter == 0)
						throw E; //. =>
				}
			}
	    }
	    
	    protected void Disconnect(boolean flDisconnectGracefully) throws IOException {
	    	if (flDisconnectGracefully) {
		        //. close connection gracefully
		        byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(MESSAGE_DISCONNECT);
		        ConnectionOutputStream.write(BA);
		        ConnectionOutputStream.flush();
	    	}
	        //.
	    	try {
		    	if (Connection != null) {
			    	synchronized (Connection) {
			    		if (ConnectionOutputStream != null) {
					        ConnectionOutputStream.close();
					        ConnectionOutputStream = null;
			    		}
				        //.
			    		if (ConnectionInputStream != null) {
					        ConnectionInputStream.close();
					        ConnectionInputStream = null;
			    		}
					}
			    	//.
			        Connection.close();
			        Connection = null;
		    	}
	    	}
	    	catch (Exception E) {}
	    }
	    
		protected void Disconnect() throws IOException {
	    	Disconnect(true);
	    }
	    
		protected short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
	        int CRC = 0;
	        int V;
	        int Idx  = Offset;
	        while (Idx < (Offset+Size))
	        {
	            V = (int)(buffer[Idx] & 0x000000FF);
	            CRC = (((CRC+V) << 1)^V);
	            //.
	            Idx++;
	        }
	        return (short)CRC;
		}
		
		protected void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
	        int StartIdx = Offset;
	        byte[] UserPasswordArray;
	        UserPasswordArray = UserPassword.getBytes("windows-1251");
	        //.
	        if (UserPasswordArray.length > 0)
	        {
	            int UserPasswordArrayIdx = 0;
	            for (int I = StartIdx; I < (StartIdx+Size); I++)
	            {
	                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
	                UserPasswordArrayIdx++;
	                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
	                	UserPasswordArrayIdx = 0;
	            }
	        }
		}
		
		protected void Streaming() throws Exception {
		}
    }
    
    public static class TComponentDataStreaming extends TComponentDataStreamingAbstract {
    	
    	public static final short SERVICE_SETDATASTREAM_V2 = 5;
        //. error messages
        public static final int MESSAGE_OK                    = 0;
        public static final int MESSAGE_ERROR                 = -1;
        public static final int MESSAGE_UNKNOWNSERVICE        = 10;
        public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
        public static final int MESSAGE_ACCESSISDENIED        = -12;
        public static final int MESSAGE_TOOMANYCLIENTS        = -13;
        public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
        public static final int MESSAGE_WRONGPARAMETERS       = -15;
        public static final int MESSAGE_SAVINGDATAERROR       = -101;
    	//.
    	public static String	MESSAGE_GetString(int Code) {
    		switch (Code) {
    		
    		case MESSAGE_OK:
    			return "OK"; //. ->
    			
    		case MESSAGE_ERROR:
    			return "?"; //. ->

    		case MESSAGE_UNKNOWNSERVICE:
    			return "unknown service"; //. ->
    			
    		case MESSAGE_AUTHENTICATIONFAILED:
    			return "authentication failed"; //. ->
    			
    		case MESSAGE_ACCESSISDENIED:
    			return "access denied"; //. ->
    			
    		case MESSAGE_TOOMANYCLIENTS:
    			return "too many clients"; //. ->
    			
    		case MESSAGE_UNKNOWNCOMMAND:
    			return "unknown command"; //. ->
    			
    		case MESSAGE_WRONGPARAMETERS:
    			return "wrong parameters"; //. ->
    			
    		case MESSAGE_SAVINGDATAERROR:
    			return "data saving error"; //. ->

    		default:
    			return "code: "+Integer.toString(Code); //. ->
    		}
    	}
        
    	public static class StreamingErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public int Code;
			
			public StreamingErrorException(int pCode) {
    			super("Error: "+MESSAGE_GetString(pCode));
    			Code = pCode;
    		}

			public StreamingErrorException(int pCode, String pMessage) {
    			super(pMessage);
    			Code = pCode;
    		}
    	}
    	

        private Exception StreamingHandlerException;
        
    	public TComponentDataStreaming(TDEVICEModule pDEVICEModule, TStreamer pStreamer) {
    		super(pDEVICEModule,pStreamer);
    		//.
    		ServerPort = 5000;
    	}
    	
	    @Override
    	protected void GetServerInfo() {
			ServerAddress = DEVICEModule.ConnectorModule.GetGeographDataServerAddress();
			ServerPort = DEVICEModule.ConnectorModule.GetGeographDataServerPort();
    	}
    	
	    private void Login(int idTComponent, long idComponent) throws Exception {
	    	byte[] LoginBuffer = new byte[24];
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SERVICE_SETDATASTREAM_V2);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(DEVICEModule.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(idTComponent);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			BA = TDataConverter.ConvertInt64ToLEByteArray(idComponent);
			System.arraycopy(BA,0, LoginBuffer,14, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,12);
			BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,22, BA.length);
			Buffer_Encrypt(LoginBuffer,10,14,DEVICEModule.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
	    @Override
		protected void Streaming() throws Exception {
			flStreaming = true;
			try {
				boolean flDisconnectGracefully = false;
				Connect();
				try {
					Login(Streamer.idTComponent,Streamer.idComponent);
					//.
					int Descriptor;
					byte[] DescriptorBA = new byte[4];
					//. send the stream channel ID
					DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Streamer.ChannelID);
					ConnectionOutputStream.write(DescriptorBA);
					//. send the DataSizeDescriptorLength
					byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(Streamer.DataSizeDescriptorLength);
					ConnectionOutputStream.write(BA);
					//. check result
					ConnectionInputStream.read(DescriptorBA);
					Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
					if (Descriptor != MESSAGE_OK)
						throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
					//. set streaming handler
					StreamingHandlerException = null;
					Streamer.SetStreamingHandler(new TStreamer.TBufferHandler() {
						@Override
						public void DoOnBuffer(TBuffer StreamingBuffer) {
							try {
						        Connection.setSendBufferSize(StreamingBuffer.Size);
								ConnectionOutputStream.write(StreamingBuffer.Data, 0,StreamingBuffer.Size);
								ConnectionOutputStream.flush();
							}
							catch (Exception E) {
								synchronized (TComponentDataStreaming.this) {
									StreamingHandlerException = E;									
								}
							}
						}
					});
					//.
					@SuppressWarnings("unused")
					int StreamingBufferCapacity = Streamer.Streaming_Start();
					try {
						//. last version: TStreamer.TBuffer StreamingBuffer = new TStreamer.TBuffer(StreamingBufferCapacity);
						//.
						try {
							while (!Canceller.flCancel && Streamer.Streaming_SourceIsActive()) {
								/* last version: if (!Streamer.Streaming_GetBuffer(StreamingBuffer, Canceller)) 
									break; //. >
								//.
						        Connection.setSendBufferSize(StreamingBuffer.Size);
								ConnectionOutputStream.write(StreamingBuffer.Data, 0,StreamingBuffer.Size);
								ConnectionOutputStream.flush();*/
								Thread.sleep(1000);
								//. check for streaming handler exception
								synchronized (TComponentDataStreaming.this) {
									if (StreamingHandlerException != null)							 {
										Exception E = StreamingHandlerException;
										StreamingHandlerException = null;
										//.
										throw E; //. =>
									}
								}
								//. check for unexpected result
								if (ConnectionInputStream.available() >= 4) {
									ConnectionInputStream.read(DescriptorBA);
									int _Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
									if (_Descriptor < 0)  
										throw new StreamingErrorException(_Descriptor); //. =>
									else
										throw new StreamingErrorException(MESSAGE_ERROR,"unknown message during transmission"); //. =>
								}
							}
							//. send the "Exit" marker and disconnect
							if (Streamer.DataSizeDescriptorLength > 0) {
								byte[] ExitMarker = new byte[Streamer.DataSizeDescriptorLength];
								ConnectionOutputStream.write(ExitMarker);
								ConnectionOutputStream.flush();
							}
						}
						catch (InterruptedException IE) {
							//. send the "Exit" marker and disconnect
							if (Streamer.DataSizeDescriptorLength > 0) {
								byte[] ExitMarker = new byte[Streamer.DataSizeDescriptorLength];
								ConnectionOutputStream.write(ExitMarker);
								ConnectionOutputStream.flush();
							}
							//.
							throw IE; //. =>
						}
					}
					finally {
						Streamer.Streaming_Stop();
					}
				}
				finally {
					Disconnect(flDisconnectGracefully);
				}
			}
			finally {
				flStreaming = false;
			}
		}
    }
    
    public static class TComponentDataStreamingUDP extends TComponentDataStreamingAbstract {
    	
    	public static final short SERVICE_SETDATASTREAM = 11;
        //. error messages
        public static final int MESSAGE_OK                    = 0;
        public static final int MESSAGE_ERROR                 = -1;
        public static final int MESSAGE_UNKNOWNSERVICE        = 10;
        public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
        public static final int MESSAGE_ACCESSISDENIED        = -12;
        public static final int MESSAGE_TOOMANYCLIENTS        = -13;
        public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
        public static final int MESSAGE_WRONGPARAMETERS       = -15;
        public static final int MESSAGE_SAVINGDATAERROR       = -101;
    	//.
    	public static String	MESSAGE_GetString(int Code) {
    		switch (Code) {
    		
    		case MESSAGE_OK:
    			return "OK"; //. ->
    			
    		case MESSAGE_ERROR:
    			return "?"; //. ->

    		case MESSAGE_UNKNOWNSERVICE:
    			return "unknown service"; //. ->
    			
    		case MESSAGE_AUTHENTICATIONFAILED:
    			return "authentication failed"; //. ->
    			
    		case MESSAGE_ACCESSISDENIED:
    			return "access denied"; //. ->
    			
    		case MESSAGE_TOOMANYCLIENTS:
    			return "too many clients"; //. ->
    			
    		case MESSAGE_UNKNOWNCOMMAND:
    			return "unknown command"; //. ->
    			
    		case MESSAGE_WRONGPARAMETERS:
    			return "wrong parameters"; //. ->
    			
    		case MESSAGE_SAVINGDATAERROR:
    			return "data saving error"; //. ->

    		default:
    			return "code: "+Integer.toString(Code); //. ->
    		}
    	}
    	        
    	public static class StreamingErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public int Code;
			
			public StreamingErrorException(int pCode) {
    			super("Error: "+MESSAGE_GetString(pCode));
    			Code = pCode;
    		}

			public StreamingErrorException(int pCode, String pMessage) {
    			super(pMessage);
    			Code = pCode;
    		}
    	}
    	
    	public static final int MTU_MAX_SIZE = 1500;
    	
    	private long SessionKey;
    	//.
        private String 	UDPServerAddress;
        private int 	UDPServerPort;
        //.
        private Exception StreamingHandlerException;
        
    	public TComponentDataStreamingUDP(TDEVICEModule pDEVICEModule, TStreamer pStreamer) {
    		super(pDEVICEModule,pStreamer);
    		//.
    		ServerPort = 2010;
    	}
    	
	    @Override
    	protected void GetServerInfo() {
			ServerAddress = DEVICEModule.ConnectorModule.GetGeographProxyServerAddress();
			ServerPort = DEVICEModule.ConnectorModule.GetGeographProxyServerPort();
    	}
    	
	    private void Login(int idTComponent, long idComponent) throws Exception {
	    	byte[] LoginBuffer = new byte[24];
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SERVICE_SETDATASTREAM);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(DEVICEModule.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(idTComponent);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			BA = TDataConverter.ConvertInt64ToLEByteArray(idComponent);
			System.arraycopy(BA,0, LoginBuffer,14, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,12);
			BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,22, BA.length);
			Buffer_Encrypt(LoginBuffer,10,14,DEVICEModule.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
	    @Override
		protected void Streaming() throws Exception {
			flStreaming = true;
			try {
				boolean flDisconnectGracefully = false;
				Connect();
				try {
					Login(Streamer.idTComponent,Streamer.idComponent);
					//.
					int Descriptor;
					byte[] DescriptorBA = new byte[4];
					//. send the stream channel ID
					DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Streamer.ChannelID);
					ConnectionOutputStream.write(DescriptorBA);
					//. send the DataSizeDescriptorLength
					byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(Streamer.DataSizeDescriptorLength);
					ConnectionOutputStream.write(BA);
					//. check result
					ConnectionInputStream.read(DescriptorBA);
					Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
					if (Descriptor != MESSAGE_OK)
						throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
					//. getSessionKey
					BA = new byte[8];
					ConnectionInputStream.read(BA);
					SessionKey = TDataConverter.ConvertLEByteArrayToInt64(BA,0);
					//. get UDP server instance info
					ConnectionInputStream.read(DescriptorBA);
					Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
					BA = new byte[Descriptor];
					ConnectionInputStream.read(BA);
		        	String UDPServerInfo = new String(BA,"windows-1251");
		        	String[] SA = UDPServerInfo.split(":"); 
		        	if (SA.length == 2) {
		        		UDPServerAddress = SA[0];
		        		UDPServerPort = Integer.parseInt(SA[1]);
		        		//.
		        		if ((UDPServerAddress == null) || (UDPServerAddress.length() == 0))
		        			UDPServerAddress = ServerAddress;
		        	}
		        	else
		        		throw new Exception("incorrect UDP server info: "+UDPServerInfo); //. =>
					//.
					final DatagramSocket UDPSocket = new DatagramSocket(TConnectionUDPRepeater.GetUDPLocalPort(),InetAddress.getByName("0.0.0.0"));
					try {
						final byte[] SendPacketBuffer = new byte[MTU_MAX_SIZE];
						int PacketHeaderSize = 0;
						short Version = 1;
						BA = TDataConverter.ConvertInt16ToLEByteArray(Version);
						System.arraycopy(BA,0, SendPacketBuffer,PacketHeaderSize, BA.length); PacketHeaderSize += BA.length;
						BA = TDataConverter.ConvertInt64ToLEByteArray(SessionKey);
						System.arraycopy(BA,0, SendPacketBuffer,PacketHeaderSize, BA.length); PacketHeaderSize += BA.length;
						final int SendPacketHeaderSize = PacketHeaderSize;
						//.
						final int MaxPacketBodySize = SendPacketBuffer.length-PacketHeaderSize; 
						//.
						final DatagramPacket SendPacket = new DatagramPacket(SendPacketBuffer,SendPacketBuffer.length, InetAddress.getByName(UDPServerAddress),UDPServerPort);
						//. set streaming handler
						StreamingHandlerException = null;
						Streamer.SetStreamingHandler(new TStreamer.TBufferHandler() {
							@Override
							public void DoOnBuffer(TBuffer StreamingBuffer) {
								try {
									if (StreamingBuffer.Size <= MaxPacketBodySize) {
										System.arraycopy(StreamingBuffer.Data,0, SendPacketBuffer,SendPacketHeaderSize, StreamingBuffer.Size);
										SendPacket.setLength(SendPacketHeaderSize+StreamingBuffer.Size);
										//.
										UDPSocket.send(SendPacket); 				
									}
								}
								catch (Exception E) {
									synchronized (TComponentDataStreamingUDP.this) {
										StreamingHandlerException = E;									
									}
								}
							}
						});
						//.
						@SuppressWarnings("unused")
						int StreamingBufferCapacity = Streamer.Streaming_Start();
						try {
							//. last version: TStreamer.TBuffer StreamingBuffer = new TStreamer.TBuffer(StreamingBufferCapacity);
							double CheckPointInterval = (1.0/(3600.0*24))*300;
							double CheckPointBaseTime = OleDate.UTCCurrentTimestamp();
							try {
								while (!Canceller.flCancel && Streamer.Streaming_SourceIsActive()) {
									/* last version: if (!Streamer.Streaming_GetBuffer(StreamingBuffer, Canceller)) 
										break; //. >
									//.
									if (StreamingBuffer.Size <= MaxPacketBodySize) {
										System.arraycopy(StreamingBuffer.Data,0, SendPacketBuffer,PacketHeaderSize, StreamingBuffer.Size);
										SendPacket.setLength(PacketHeaderSize+StreamingBuffer.Size);
										//.
										UDPSocket.send(SendPacket); 				
									}*/
									Thread.sleep(1000);
									//. check for streaming handler exception
									synchronized (TComponentDataStreamingUDP.this) {
										if (StreamingHandlerException != null)							 {
											Exception E = StreamingHandlerException;
											StreamingHandlerException = null;
											//.
											throw E; //. =>
										}
									}
								    //. checkpoint
								    if (!Canceller.flCancel && ((OleDate.UTCCurrentTimestamp()-CheckPointBaseTime) > CheckPointInterval)) {
						        		Descriptor = 0; //. checkpoint descriptor
						        		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
						        		ConnectionOutputStream.write(DescriptorBA);
										ConnectionOutputStream.flush();
								    	//.
								    	CheckPointBaseTime = OleDate.UTCCurrentTimestamp();
								    }
									//. check for unexpected result
									if (ConnectionInputStream.available() >= 4) {
										ConnectionInputStream.read(DescriptorBA);
										int _Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
										if (_Descriptor < 0)  
											throw new StreamingErrorException(_Descriptor); //. =>
										else
											throw new StreamingErrorException(MESSAGE_ERROR,"unknown message during transmission"); //. =>
									}
								}
								//. send the "Exit" marker and disconnect
								if (Streamer.DataSizeDescriptorLength > 0) {
					        		Descriptor = -1; //. Exit marker
					        		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
					        		ConnectionOutputStream.write(DescriptorBA);
									ConnectionOutputStream.flush();
								}
							}
							catch (InterruptedException IE) {
								//. send the "Exit" marker and disconnect
								if (Streamer.DataSizeDescriptorLength > 0) {
					        		Descriptor = -1; //. Exit marker
					        		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
					        		ConnectionOutputStream.write(DescriptorBA);
									ConnectionOutputStream.flush();
								}
								//.
								throw IE; //. >
							}
						}
						finally {
							Streamer.Streaming_Stop();
						}
					}
					finally {
						UDPSocket.close();
					}
				}
				finally {
					Disconnect(flDisconnectGracefully);
				}
			}
			finally {
				flStreaming = false;
			}
		}
    }
    
    public TComponentDataStreamingAbstract TComponentDataStreaming_Create(TComponentDataStreamingAbstract.TStreamer Streamer) {
    	return (new TComponentDataStreaming(this, Streamer));
    }

    public TComponentDataStreamingAbstract TComponentDataStreamingUDP_Create(TComponentDataStreamingAbstract.TStreamer Streamer) {
    	return (new TComponentDataStreamingUDP(this, Streamer));
    }
}
