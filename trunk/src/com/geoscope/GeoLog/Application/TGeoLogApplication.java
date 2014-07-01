package com.geoscope.GeoLog.Application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Xml;

import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoEye.UserAgentService.TUserAgentService;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.TrackerService.TTrackerService;
import com.geoscope.Utils.TFileSystem;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

public class TGeoLogApplication {

	public static final String	ApplicationBasePath = TGeoLogInstallator.ProgramInstallationPath;
	public static final String 	ApplicationFolderName = TGeoLogInstallator.ProgramFolderName;
	public static final String 	ApplicationFolder = TGeoLogInstallator.ProgramFolder;
	
	public static final String 	LogFolder = ApplicationFolder+"/"+"Log";

	public static final String 		Profiles_Folder = ApplicationFolder+"/"+"PROFILEs";
	//.
	public static final String 		Profiles_Default = "Default";
	
	public static ArrayList<String>	Profiles_GetNames() {
		ArrayList<String> Result = new ArrayList<String>();
		File PF = new File(Profiles_Folder);
		if (!PF.exists())
			return Result; //. ->
		File[] Files = PF.listFiles();
		for (int I = 0; I < Files.length; I++) 
			if (Files[I].isDirectory()) 
				Result.add(Files[I].getName());
		return Result;
		
	}
	//.
	public static String 			Profiles_Clone(String ProfileToClone, String CloneName) throws IOException {
		File DestFolder = new File(Profiles_Folder+"/"+CloneName);
		if (DestFolder.exists())
			return CloneName; //. ->
		File SrcFolder = new File(Profiles_Folder+"/"+ProfileToClone);
		TFileSystem.CopyFolder(SrcFolder, DestFolder, new String[] {SpaceContextFolder});
		return CloneName; 
	}
	//.
	public static void 				Profiles_Remove(String ProfileName) throws IOException {
		if (!ProfileName.equals(Profiles_Default)) {
			File ProfileFolder = new File(Profiles_Folder+"/"+ProfileName);
			TFileSystem.RemoveFolder(ProfileFolder);
		}
	}
	//.
	public static synchronized String 	ProfileName() {
		try {
			TCurrentProfileFile CurrentProfileFile = new TCurrentProfileFile(); 
			if (CurrentProfileFile.CurrentProfileName != null)
				return CurrentProfileFile.CurrentProfileName;
			else
				return Profiles_Default; //. ->
		}
		catch (Exception E) {
			return Profiles_Default; //. ->
		}
	}
	//.
	public static synchronized void 	SetProfileName(String Value, Context context) throws Exception {
		TCurrentProfileFile CurrentProfileFile = new TCurrentProfileFile(); 
		CurrentProfileFile.CurrentProfileName = Value;
		CurrentProfileFile.Save();
		//.
		Intent intent = new Intent(context, TReflector.class);
		intent.setAction("com.geoscope.geolog.action.newprofile");
		intent.putExtra("ProfileName", Value);
		PendingIntent _PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    	AlarmManager AM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	AM.set(AlarmManager.RTC, System.currentTimeMillis()+500, _PendingIntent);
    	//.
    	System.exit(2);
	}
	//.
	public static String 		ProfileFolder() {
		return (Profiles_Folder+"/"+ProfileName());
	}
	
	public static class TCurrentProfileFile {
		
		public static final String CurrentProfileFile = Profiles_Folder+"/"+"Current";

		public String CurrentProfileName = null;
		
		public TCurrentProfileFile() throws Exception {
			Load();
		}
		
		public void Load() throws Exception {
			File CPF = new File(CurrentProfileFile);
			if (CPF.exists() && CPF.isFile()) {
				byte[] XML;
				long FileSize = CPF.length();
				if (FileSize != 0) {
					FileInputStream FIS = new FileInputStream(CPF);
					try {
						XML = new byte[(int) FileSize];
						FIS.read(XML);
					} finally {
						FIS.close();
					}
					Document XmlDoc;
					ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory
								.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						XmlDoc = builder.parse(BIS);
					} finally {
						BIS.close();
					}
					int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
					NodeList NL;
					switch (Version) {
					case 1:
						NL = XmlDoc.getDocumentElement().getElementsByTagName("CurrentProfileName");
						String _CurrentProfileName = NL.item(0).getFirstChild().getNodeValue();
						String _CurrentProfileFolder = Profiles_Folder+"/"+_CurrentProfileName;
						File _CPF = new File(_CurrentProfileFolder);
						if (_CPF.exists() && _CPF.isDirectory())
							CurrentProfileName = _CurrentProfileName;
						break; // . >
						
					default:
						throw new Exception("unknown current profile file version, version: "+Integer.toString(Version)); // . =>
					}
				}
			}
		}
		
		public void Save() throws Exception {
			int CurrentProfileFileVersion = 1;
			String CPFN = CurrentProfileFile;
			String TCPFN = CPFN+".tmp";
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(TCPFN);
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", true);
				serializer.startTag("", "ROOT");
				//.
				serializer.startTag("", "Version");
				serializer.text(Integer.toString(CurrentProfileFileVersion));
				serializer.endTag("", "Version");
				//.
				serializer.startTag("", "CurrentProfileName");
				if (CurrentProfileName != null)
					serializer.text(CurrentProfileName);
				serializer.endTag("", "CurrentProfileName");
				//.
				serializer.endTag("", "ROOT");
				serializer.endDocument();
			} finally {
				writer.close();
			}
			File TCPF = new File(TCPFN);
			File CPF = new File(CPFN);
			TCPF.renameTo(CPF);
		}
	}
	
	public static final String 		SpaceContextFolder = "CONTEXT";
	
	private static TGeoLogApplication _Instance = null;
	
	public static void InitializeInstance() {
		_Instance = new TGeoLogApplication();
	}
	
	public static void FinalizeInstance() {
		if (_Instance != null) {
			_Instance.Destroy();
			_Instance = null;
		}
	}
	
	public static TGeoLogApplication Instance() {
		if (_Instance == null)
			_Instance = new TGeoLogApplication();
		return _Instance;
	}
	
    public class TGarbageCollector implements Runnable {
    	
        private static final int GarbageCollectingMinInterval = 1000*10/*seconds*/;
        
    	private Thread _Thread;
    	private boolean flCancel = false;
    	public boolean flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	
    	public TGarbageCollector() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Destroy() {
    		CancelAndWait();
    	}
    	
		@Override
		public void run() {
			try {
				flProcessing = true;
				try {
					while (!flCancel) {
						ProcessSignal.WaitOne();
						if (flCancel)
							return; //. ->
						//.
						Collect();
						//.
						Thread.sleep(GarbageCollectingMinInterval);
					}
				}
				finally {
					flProcessing = false;
				}
			}
			catch (Throwable E) {
			}
		}
		
		public void Collect() {
			System.gc();		
		}
		
		public void Start() {
			ProcessSignal.Set();
		}
		
    	public void Join() {
    		try {
    			if (_Thread != null)
    				_Thread.join();
    		}
    		catch (Exception E) {}
    	}

		public void Cancel() {
			flCancel = true;
			//.
			ProcessSignal.Set();
    		//.
    		if (_Thread != null)
    			_Thread.interrupt();
		}
		
		public void CancelAndWait() {
    		Cancel();
    		Join();
		}
    }
    
    
    public TGarbageCollector GarbageCollector;
    
	public TGeoLogApplication() {
		GarbageCollector = new TGarbageCollector();
	}
	
	public void Destroy() {
		if (GarbageCollector != null) {
			GarbageCollector.Destroy();
			GarbageCollector = null;
		}
	}
	
	public synchronized void StartServices(Context context) throws Exception {
		//. start server user-agent service
		if (TUserAgent.GetUserAgent() == null)
			TUserAgent.CreateUserAgent(context);
		Intent UserAgentServiceLauncher = new Intent(context, TUserAgentService.class);
		context.startService(UserAgentServiceLauncher);
		//. start tracker service
		if (TTracker.GetTracker() == null)
			TTracker.CreateTracker(context);
		Intent TrackerServiceLauncher = new Intent(context, TTrackerService.class);
		context.startService(TrackerServiceLauncher);
	}
	
	public synchronized void StopServices(Context context) {
		TTrackerService TrackerService = TTrackerService.GetService();
		if (TrackerService != null)
			TrackerService.StopServicing();
		Intent TrackerServiceLauncher = new Intent(context, TTrackerService.class);
		context.stopService(TrackerServiceLauncher);
		//.
		TUserAgentService UserAgentService = TUserAgentService.GetService();
		if (UserAgentService != null)
			UserAgentService.StopServicing();
		Intent UserAgentServiceLauncher = new Intent(context, TUserAgentService.class);
		context.stopService(UserAgentServiceLauncher);
	}
	
	public synchronized void Terminate(Context context) {
		StopServices(context);
		//.
		System.exit(0);
	}
	
	
}
