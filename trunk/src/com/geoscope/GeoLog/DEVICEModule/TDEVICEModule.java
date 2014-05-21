/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICEModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.BatteryModule.TBatteryModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ControlModule.TControlModule;
import com.geoscope.GeoLog.DEVICE.FileSystemModule.TFileSystemModule;
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIModule;
import com.geoscope.GeoLog.DEVICE.GPOModule.TGPOModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.OSModule.TOSModule;
import com.geoscope.GeoLog.DEVICE.SensorModule.TSensorModule;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TRollingLogFile;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.TFileSystem;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

/**
 *
 * @author ALXPONOM
 */
@SuppressLint("SimpleDateFormat")
public class TDEVICEModule extends TModule 
{
	public static final String ObjectBusinessModel = "101.2";
		
	public static final String ProgramFolder = TGeoLogInstallator.ProgramFolder;
	public static final String ProgramLogFolder = ProgramFolder+"/"+"Log";
	public static final String ProfileFolder = ProgramFolder+"/"+"PROFILEs"+"/"+"Default";
	public static final String DeviceFolder = ProfileFolder+"/"+"Device";
	public static final String DeviceOldFileName = "Device.xml";
	public static final String DeviceFileName = "Data.xml";
	public static final String DeviceLogFileName = "Device.log";
	
    public static void Log_WriteCriticalError(Throwable E) {
    	if (E instanceof CancelException)
    		return; //. ->
        try {
        	String LogFolder = ProgramLogFolder; File LF = new File(LogFolder); LF.mkdirs();
        	PrintWriter PW = new PrintWriter(new FileWriter(LogFolder+"/"+(new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss")).format(new Date())+"."+"log", true));
            try {
                E.printStackTrace(PW);
                PW.flush();
            }
            finally {
            	PW.close();
            }
        } catch (Throwable TE) {
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
		File F = new File(DeviceFolder);
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
        Log = new TRollingLogFile(ProgramLogFolder+"/"+DeviceLogFileName);
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
        ModuleState = MODULE_STATE_FINALIZING;
        //. stopping
        Stop();
    	//.
    	/*///? if (EventReceiver != null) {
    		context.unregisterReceiver(EventReceiver);
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
			//.
	    	/*///? IntentFilter Filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    	Filter.addAction(Intent.ACTION_SCREEN_OFF);
	    	context.registerReceiver(EventReceiver,Filter);*/
			//.
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
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("GPSModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		
		case 1:
			try {
				NodeList NL;
				Node node = RootNode.getElementsByTagName("flEnabled").item(0).getFirstChild();
				if (node != null)
					flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
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
	        if (ControlModule != null)
	        	ControlModule.SaveProfileTo(Serializer);
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
    	public static final int MaxItemErrors = 10;
    	public static final int StreamingAttemptSleepTime = 1000*60; //. seconds
    	
    	public static class TItem {
    		public int idTComponent;
    		public int idComponent;
    		public String FileName;
    		//.
    		public int ErrorCount = 0;
    		//.
    		public long TransmittedSize = 0;
    	}
    	
    	public static final int StreamSignalTimeout = 1000*60; //. seconds
    	
    	public static final int ConnectTimeout = 1000*600; //. seconds
    	
    	public static final short SERVICE_SETCOMPONENTSTREAM_V2 = 3;
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

    	public static class StreamingErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public int Code;
			
			public StreamingErrorException(int pCode) {
    			super("Error: "+Integer.toString(pCode));
    			Code = pCode;
    		}

			public StreamingErrorException(int pCode, String pMessage) {
    			super(pMessage);
    			Code = pCode;
    		}
    	}
    	
    	private TDEVICEModule DEVICEModule;
        //.
    	public boolean flEnabledStreaming = true;
    	//.
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
        public int			ConnectionType = CONNECTION_TYPE_SECURE_SSL;
        private Socket 		Connection;
        public InputStream 	ConnectionInputStream;
        public OutputStream ConnectionOutputStream;
    	//.
    	private ArrayList<TItem> Items = new ArrayList<TItem>();
    	
    	public TComponentFileStreaming(TDEVICEModule pDEVICEModule, boolean pflEnabledStreaming) throws Exception {
    		DEVICEModule = pDEVICEModule;
    		flEnabledStreaming = pflEnabledStreaming;
    		//.
    		Load();
    	}
    	
    	public void Destroy() {
    		Stop();
    	}
    	
    	private synchronized void Load() throws Exception {
			Items.clear();
    		String FN = TDEVICEModule.DeviceFolder+"/"+ItemsFileName;
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
    					NodeList ItemChildsNode = ItemNode.getChildNodes();
    					//.
    					TItem Item = new TItem();
    					Item.idTComponent = Integer.parseInt(ItemChildsNode.item(0).getFirstChild().getNodeValue());
    					Item.idComponent = Integer.parseInt(ItemChildsNode.item(1).getFirstChild().getNodeValue());
    					Item.FileName = ItemChildsNode.item(2).getFirstChild().getNodeValue();
    					Item.ErrorCount = Integer.parseInt(ItemChildsNode.item(3).getFirstChild().getNodeValue());
    					//.
    					Items.add(Item);
    				}
    			}
    			break; //. >
    		default:
    			throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
    		}
    	}
    	
    	public synchronized void Save() throws Exception {
    	    String FN = TDEVICEModule.DeviceFolder+"/"+ItemsFileName;
            File F = new File(FN);
            if (Items.size() == 0) {
            	F.delete();
            	return; //. ->
            }
    	    if (!F.exists()) 
    	    	F.getParentFile().mkdirs();
    	    String TFN = FN+".tmp";
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
    	            		serializer.text(Integer.toString(Item.idComponent));
    	            		serializer.endTag("", "idComponent");
    	            		//. FileName
    	            		serializer.startTag("", "FileName");
    	            		serializer.text(Item.FileName);
    	            		serializer.endTag("", "FileName");
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
    	
    	public synchronized void AddItem(int pidTComponent, int pidComponent, String pFileName) throws Exception {
    		TItem NewItem = new TItem();
    		NewItem.idTComponent = pidTComponent;
    		NewItem.idComponent = pidComponent;
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
    	
    	public synchronized void RemoveLastItem() throws Exception {
    		TItem LastItem = GetLastItem();
    		if (LastItem == null)
    			return; //. ->
    		RemoveItem(LastItem);
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
    	
    	public void Start() {
    		if (!flEnabledStreaming)
    			return; //. ->
    		Canceller.flCancel = false;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Stop() {
    		if (_Thread != null) {
        		CancelAndWait();
        		_Thread = null;
    		}
    	}
    	
    	public boolean IsStarted() {
    		return (_Thread != null);
    	}
    	
    	public boolean IsStreaming() {
    		return flStreaming;
    	}
    	
    	public void SetEnabledStreaming(boolean pflEnabledStreaming) throws Exception {
    		if (pflEnabledStreaming == flEnabledStreaming)
    			return; //. ->
    		flEnabledStreaming = pflEnabledStreaming;
    		DEVICEModule.SaveProfile();
    		if (flEnabledStreaming) {
    			if (!IsStarted())
    				Start();
    		}
    		else {
    			if (IsStarted())
    				Stop();
    		}
    	}
    	
		@Override
		public void run() {
			byte[] TransferBuffer = new byte[1024*64];
			flStreaming = true;
			try {
				try {
					while (!Canceller.flCancel) {
						if (ServerAddress != null) {
							//. streaming ...
							TItem StreamItem = GetLastItem();
							while (StreamItem != null) {
								flStreamingComponent = true;
								try {
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
											catch (SocketException SE) {
												break; //. >
											}
											catch (Exception E) {
												DEVICEModule.Log.WriteWarning("DEVICEModule.ComponentFileStreaming","Failed attempt to stream file: "+StreamItem.FileName+", Component("+Integer.toString(StreamItem.idTComponent)+";"+Integer.toString(StreamItem.idComponent)+")"+", "+E.getMessage());
												//.
												StreamItem.ErrorCount++;
												if (StreamItem.ErrorCount < MaxItemErrors) { 
													Save();
													//.
													Thread.sleep(StreamingAttemptSleepTime);
													//.
													if (Canceller.flCancel)
														return; //. ->
													//.
													continue; //. ^
												}
												else {
													String S = E.getMessage();
													if (S == null)
														S = E.getClass().getName();
													DEVICEModule.Log.WriteError("DEVICEModule.ComponentFileStreaming","Streaming has been cancelled after attempt errors, file: "+StreamItem.FileName+", Component("+Integer.toString(StreamItem.idTComponent)+";"+Integer.toString(StreamItem.idComponent)+")"+", "+S);
												}
											}
											//.
											RemoveItem(StreamItem);
											//.
											break; //. >
										}
									}
									catch (Throwable TE) {
						            	//. log errors
										String S = TE.getMessage();
										if (S == null)
											S = TE.getClass().getName();
										DEVICEModule.Log.WriteError("DEVICEModule.ComponentFileStreaming",S);
						            	if (!(TE instanceof Exception))
						            		TDEVICEModule.Log_WriteCriticalError(TE);
									}
								}
								finally {
									flStreamingComponent = false;
								}
								//. next one
								StreamItem = GetLastItem();
							}
						}
						else {
							if ((DEVICEModule.ConnectorModule != null) && (DEVICEModule.ConnectorModule.flProcessing)) {
								ServerAddress = DEVICEModule.ConnectorModule.GetGeographDataServerAddress();
								ServerPort = DEVICEModule.ConnectorModule.GetGeographDataServerPort();
							}
						}
						//.
						StreamSignal.WaitOne(1000);/////////////StreamSignalTimeout);
					}
				}
				catch (InterruptedException E) {
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
		
	    private void Connect() throws Exception {
	    	switch (ConnectionType) {
	    	
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
	    		throw new Exception("unknown connection type, type: "+Integer.toString(ConnectionType)); //. =>
	    	}
	        Connection.setSoTimeout(ConnectTimeout);
	        Connection.setKeepAlive(true);
	        Connection.setSendBufferSize(10000);
	        ConnectionInputStream = Connection.getInputStream();
	        ConnectionOutputStream = Connection.getOutputStream();
	    }
	    
	    private void Disconnect(boolean flDisconnectGracefully) throws IOException {
	    	if (flDisconnectGracefully) {
		        //. close connection gracefully
		        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_DISCONNECT);
		        ConnectionOutputStream.write(BA);
		        ConnectionOutputStream.flush();
	    	}
	        //.
	        ConnectionOutputStream.close();
	        ConnectionInputStream.close();
	        Connection.close();
	    }
	    
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
		
	    private void Login(int idTComponent, int idComponent) throws Exception {
	    	byte[] LoginBuffer = new byte[24];
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_SETCOMPONENTSTREAM_V2);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(DEVICEModule.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idTComponent);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idComponent);
			System.arraycopy(BA,0, LoginBuffer,14, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,12);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,22, BA.length);
			Buffer_Encrypt(LoginBuffer,10,14,DEVICEModule.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(DEVICEModule.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
		private void StreamItem(TItem Item, byte[] TransferBuffer) throws Exception {
			synchronized (this) {
				Item.TransmittedSize = 0;
			}
			boolean flDisconnect = true;
			Connect();
			try {
				Login(Item.idTComponent,Item.idComponent);
				//.
				File F = new File(Item.FileName);
				byte[] FileNameBA = F.getName().getBytes("windows-1251");
				byte[] DecriptorBA = new byte[4];
				int Descriptor = FileNameBA.length;
				DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				//.
				if (Descriptor > 0)  
					ConnectionOutputStream.write(FileNameBA);
				//. get the temporary file size on the server side 
				ConnectionInputStream.read(DecriptorBA);
				int ServerItemSize = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
				//. send file offset
				Descriptor = ServerItemSize; 
				DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				//. send file size
				Descriptor = (int)F.length()-ServerItemSize; 
				DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				//.
				if (Descriptor > 0) {
					FileInputStream FIS = new FileInputStream(F);
					try {
						if (ServerItemSize > 0) {
							FIS.skip(ServerItemSize);
							synchronized (this) {
								Item.TransmittedSize += ServerItemSize;
							}
						}
						try {
							while (Descriptor > 0) {
								int BytesRead = FIS.read(TransferBuffer);
								ConnectionOutputStream.write(TransferBuffer,0,BytesRead);
								//.
								synchronized (this) {
									Item.TransmittedSize += BytesRead;
								}
								//.
								Descriptor -= BytesRead;
								//. check for unexpected result
								if ((Descriptor > 0) && (ConnectionInputStream.available() >= 4)) {
									ConnectionInputStream.read(DecriptorBA);
									int _Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
									if (_Descriptor < 0) { 
										flDisconnect = false;
										throw new StreamingErrorException(_Descriptor); //. =>
									}
									else
										throw new StreamingErrorException(MESSAGE_ERROR,"unknown message during transmission"); //. =>
								}
								//.
								if (Canceller.flCancel) {
									Disconnect(false);
									flDisconnect = false;
									//.
									throw new CancelException(); //. =>
								}
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
					//. check result
					ConnectionInputStream.read(DecriptorBA);
					Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
					if (Descriptor != MESSAGE_OK) {
						flDisconnect = false;
						throw new StreamingErrorException(Descriptor); //. =>
					}
				}
			}
			finally {
				if (flDisconnect)
					Disconnect();
			}
		}
    }
}
