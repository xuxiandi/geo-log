/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICEModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.BatteryModule.TBatteryModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ControlModule.TControlModule;
import com.geoscope.GeoLog.DEVICE.FileSystemModule.TFileSystemModule;
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIModule;
import com.geoscope.GeoLog.DEVICE.GPOModule.TGPOModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TRollingLogFile;

/**
 *
 * @author ALXPONOM
 */
public class TDEVICEModule extends TModule 
{
	public static final String ProgramFolder = TGeoLogInstallator.ProgramFolder;
	public static final String ProgramLogFolder = ProgramFolder+"/"+"Log";
	public static final String ProfileFolder = ProgramFolder+"/"+"PROFILEs"+"/"+"Default";
	public static final String DeviceFileName = "Device.xml";
	public static final String DeviceFolder = ProfileFolder+"/"+"Device";
	public static final String DeviceLogFileName = "Device.log";
	
	public static final int DEVICEModuleState_Initializing = 0;
    public static final int DEVICEModuleState_Running = 1;
    public static final int DEVICEModuleState_Finalizing = 2;

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
    
    public int State;
    //.
	public boolean flEnabled = false;
	//.
    public int 		UserID = 2;
    public String 	UserPassword = "ra3tkq";
    public int ObjectID = 0;
    public int idGeographServerObject = 0;
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
    public TLANModule       		LANModule				= null;
    //.
    public boolean flUserInteractive = false;
    //.
    public TBackupMonitor BackupMonitor;
    
	public String ModuleFile() {
		return TDEVICEModule.ProfileFolder+"/"+TDEVICEModule.DeviceFileName;		
	}
	
    public TDEVICEModule(Context pcontext) throws Exception
    {
    	super(null);
    	//.
        State = DEVICEModuleState_Initializing;
        //.
        context = pcontext;
        //.
        TGeoLogInstallator.CheckInstallation(context);
        //.
        Log = new TRollingLogFile(ProgramLogFolder+"/"+DeviceLogFileName);
        //.
    	try {
			LoadConfiguration();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SDeviceConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//. initialization
		if (flEnabled) {
	        //. creating components
	        MovementDetectorModule 	= new TMovementDetectorModule(this);
	        ConnectorModule 		= new TConnectorModule(this);
	        GPSModule 				= new TGPSModule(this);
	        GPIModule 				= new TGPIModule(this);
	        GPOModule 				= new TGPOModule(this);
	        BatteryModule 			= new TBatteryModule(this);
	        VideoRecorderModule 	= new TVideoRecorderModule(this);
	        FileSystemModule 		= new TFileSystemModule(this);
	        ControlModule 			= new TControlModule(this);
	        LANModule 				= new TLANModule(this);
	        //. start server connection
	        if (ConnectorModule.flServerConnectionEnabled)
	        	ConnectorModule.StartConnection();
			//.
	    	/*///? IntentFilter Filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    	Filter.addAction(Intent.ACTION_SCREEN_OFF);
	    	context.registerReceiver(EventReceiver,Filter);*/         
	        //.
	        BackupMonitor = new TBackupMonitor(this);
	        //.
	        State = DEVICEModuleState_Running;
	        //.
		}
		//.
        Log.WriteInfo("Device", "started.");
    }
    
    public void Destroy() throws Exception
    {
        State = DEVICEModuleState_Finalizing;
        //.
        if (ConnectorModule != null)
        {
            //. send outgoing operations that remain in queue
            if (ConnectorModule.flProcessing) {
                int Timeout = 5/*seconds*/*10;
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
            //.
            ConnectorModule.StopConnection();
        }      
        //. 
        if (BackupMonitor != null) {
        	BackupMonitor.Destroy();
        	BackupMonitor = null;
        }
    	//.
    	/*///? if (EventReceiver != null) {
    		context.unregisterReceiver(EventReceiver);
    		EventReceiver = null;
    	}*/
        //. save configuration
        if (flEnabled)
        	SaveConfiguration();
        //.
        if (LANModule != null)
        {
            LANModule.Destroy();
            LANModule = null;
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
        //.
        if (ConnectorModule != null)
        {
            ConnectorModule.Destroy();
            ConnectorModule = null;
        }      
        if (MovementDetectorModule != null)
        {
        	MovementDetectorModule.Destroy();
        	MovementDetectorModule = null;
        }
        //.
        if (Log != null) {
    		Log.WriteInfo("Device", "stopped.");
            //.
        	Log.Destroy();
        	Log = null;
        }
    }
    
    @Override
    public synchronized void LoadConfiguration() throws Exception {
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
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return; 
    }
    
    @Override
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
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
    }
    
    @Override
	public synchronized void SaveConfiguration() throws Exception {
		String CFN = ModuleFile();
		String TCFN = CFN+".tmp";
	    XmlSerializer Serializer = Xml.newSerializer();
	    FileWriter writer = new FileWriter(TCFN);
	    try {
	        Serializer.setOutput(writer);
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //.
	        SaveConfigurationTo(Serializer);
	        if (BatteryModule != null)
	        	BatteryModule.SaveConfigurationTo(Serializer);
	        if (ConnectorModule != null)
	        	ConnectorModule.SaveConfigurationTo(Serializer);
	        if (GPSModule != null)
	        	GPSModule.SaveConfigurationTo(Serializer);
	        if (GPIModule != null)
	        	GPIModule.SaveConfigurationTo(Serializer);
	        if (GPOModule != null)
	        	GPOModule.SaveConfigurationTo(Serializer);
	        if (VideoRecorderModule != null)
	        	VideoRecorderModule.SaveConfigurationTo(Serializer);
	        if (FileSystemModule != null)
	        	FileSystemModule.SaveConfigurationTo(Serializer);
	        if (ControlModule != null)
	        	ControlModule.SaveConfigurationTo(Serializer);
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
    
    public class TBackupMonitor {
    	
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
        	private Object ProcessSignal = new Object();
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
						synchronized (ProcessSignal) {
							ProcessSignal.wait(BackupInterval);
						}
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
				synchronized (ProcessSignal) {
					ProcessSignal.notify();
				}
			}
			
			public void Process() {
				synchronized (this) {
					ImmediateProcessCounter++;
				}
				//.
				synchronized (ProcessSignal) {
					ProcessSignal.notify();
				}
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
}
