package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpConnection;
import org.w3c.dom.Document;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.Notification.Style;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources.Theme;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Shader.TileMode;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.util.Xml.Encoding;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geoscope.GeoEye.TReflector.TWorkSpace.TButtons.TButton;
import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.TSpaceContextStorage;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingCommandMessage;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingCommandResponseMessage;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUserDataFile;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TElectedPlaces;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStrucStack;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerArchive;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHint;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHints;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections.TSpaceReflections;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryDataServer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoEye.UserAgentService.TUserAgentService;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule.TServerSaver;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.TrackerService.TTrackerService;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.GeoLog.Utils.TProgressor;
import com.geoscope.GeoLog.Utils.TUpdater;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.TFileSystem;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

@SuppressLint("HandlerLeak")
@SuppressWarnings("unused")
public class TReflector extends Activity implements OnTouchListener {

	public static final String ProgramName = "Geo.Log";
	//.
	public static final String ProgramVersion = "v2.250913";
	//.
	public static final String ProgramBaseFolder = TSpaceContextStorage.DevicePath();
	//.
	public static final String ProgramFolderName = ProgramName;
	public static final String ProgramFolder = ProgramBaseFolder+"/"+ProgramFolderName;
	//.
	public static final String ProfileFolder = ProgramFolder+"/"+"PROFILEs"+"/"+"Default";
	//.
	public static final String HelpFolderName = "HELP";
	public static final String HelpPath = ProgramFolderName+"/"+HelpFolderName;
	public static final String HelpFolder = ProgramFolder+"/"+HelpFolderName;
	public static final String HelpVersionFileName = "Version.txt";
	public static final String HelpFileName = "help.html";
	//.
	public static final String TempFolderName = "TEMP";
	public static final String TempFolder = ProgramFolder+"/"+TempFolderName;
	//.
	private static final int MaxLastWindowsCount = 10;
	private static int ShowLogoCount = 3;
	// .
	private static TReflector Reflector;
	
	public static synchronized TReflector GetReflector() {
		return Reflector;
	}
	
	public static synchronized void SetReflector(TReflector pReflector) {
		Reflector = pReflector;
	}

	public static synchronized void ClearReflector(TReflector pReflector) {
		if (Reflector == pReflector)
			Reflector = null;
	}

	public static class TReflectorConfiguration {

		public static final String ConfigurationFileName = "GeoEye.Configuration";
		public static final String LastConfigurationFilePrefix = "last";
		public static final int ConfigurationFileVersion = 1;
		public static final String ReflectionWindowFileName = "ReflectionWindow.dat";
		public static final String ReflectionWindowDisabledLaysFileName = "ReflectionWindow_DisabledLays.dat";

		private Context context;
		private TReflector Reflector;
		//.
		public boolean flChanged = false;
		//.
		public String 	ServerAddress = "89.108.122.51";
		public int 		ServerPort = 80;
		//.
		public int 		UserID = 2;
		public String 	UserName = "";
		public String 	UserPassword = "ra3tkq";
		//.
		public int GeoSpaceID = 88;
		// .
		public int 		ReflectionWindow_ViewMode = VIEWMODE_TILES;
		public byte[] 	ReflectionWindowData = null;
		public int[] 	ReflectionWindow_DisabledLaysIDs = null;
		public boolean 	ReflectionWindow_flShowHints = true;
		public String 	ReflectionWindow_ViewMode_Tiles_Compilation = "";
		public int 		ReflectionWindow_NavigationMode = NAVIGATION_MODE_MULTITOUCHING1;
		// . GeoLog data
		public boolean 	GeoLog_flEnabled = false;
		public boolean 	GeoLog_flServerConnection = true;
		public String 	GeoLog_ServerAddress = "89.108.122.51";
		public int 		GeoLog_ServerPort = 8282;
		public int 		GeoLog_ObjectID = 0;
		public String 	GeoLog_ObjectName = "";
		public int 		GeoLog_QueueTransmitInterval = 0;
		public boolean 	GeoLog_flSaveQueue = true;
		public int 		GeoLog_GPSModuleProviderReadInterval = 0;
		public int 		GeoLog_GPSModuleMapID = 6;
		public boolean 	GeoLog_VideoRecorderModuleEnabled = true;
		public boolean 	GeoLog_flHide = false;

		public TReflectorConfiguration(Context pcontext, TReflector pReflector) {
			context = pcontext;
			Reflector = pReflector;
		}

		public void LoadReflectionWindowDisabledLays() throws IOException {
			String FN = TReflector.ProfileFolder + "/"
					+ ReflectionWindowDisabledLaysFileName;
			File F = new File(FN);
			if (F.exists()) {
				long FileSize = F.length();
				FileInputStream FIS = new FileInputStream(FN);
				try {
					ReflectionWindow_DisabledLaysIDs = new int[(int) (FileSize / 4)];
					for (int I = 0; I < ReflectionWindow_DisabledLaysIDs.length; I++) {
						byte[] BA = new byte[4];
						FIS.read(BA);
						ReflectionWindow_DisabledLaysIDs[I] = TDataConverter
								.ConvertBEByteArrayToInt32(BA, 0);
					}
				} finally {
					FIS.close();
				}
			} else
				ReflectionWindow_DisabledLaysIDs = null;
		}

		private boolean _Load(String FN) throws Exception {
			File F = new File(FN);
			// .
			byte[] XML;
			long FileSize = F.length();
			if (FileSize == 0)
				return false; // . ->
			FileInputStream FIS = new FileInputStream(FN);
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
			int Version = Integer.parseInt(XmlDoc.getDocumentElement()
					.getElementsByTagName("Version").item(0).getFirstChild()
					.getNodeValue());
			NodeList NL;
			switch (Version) {
			case 1:
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ServerAddress");
				ServerAddress = NL.item(0).getFirstChild().getNodeValue();
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ServerPort");
				ServerPort = Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserID");
				UserID = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserName");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						UserName = N.getNodeValue();
				}
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"UserPassword");
				UserPassword = NL.item(0).getFirstChild().getNodeValue();
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoSpaceID");
				GeoSpaceID = Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_ViewMode");
				if (NL.getLength() > 0)
					ReflectionWindow_ViewMode = Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_flShowHints");
				if (NL.getLength() > 0)
					ReflectionWindow_flShowHints = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_ViewMode_Tiles_Compilation");
				if ((NL.getLength() > 0)
						&& (NL.item(0).getFirstChild() != null))
					ReflectionWindow_ViewMode_Tiles_Compilation = NL.item(0)
							.getFirstChild().getNodeValue();
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_NavigationMode");
				if (NL.getLength() > 0)
					ReflectionWindow_NavigationMode = Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_flEnabled");
				GeoLog_flEnabled = (Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue()) != 0);
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_flServerConnection");
				GeoLog_flServerConnection = (Integer.parseInt(NL.item(0)
						.getFirstChild().getNodeValue()) != 0);
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_ServerAddress");
				GeoLog_ServerAddress = NL.item(0).getFirstChild()
						.getNodeValue();
				GeoLog_ServerAddress = ServerAddress; // . override address
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_ServerPort");
				GeoLog_ServerPort = Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_ObjectID");
				GeoLog_ObjectID = Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_ObjectName");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						GeoLog_ObjectName = N.getNodeValue();
				}
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_flHide");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						GeoLog_flHide = (Integer.parseInt(N.getNodeValue()) != 0);
				}
				// .
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					if (Tracker.GeoLog.ConnectorModule != null) {
						GeoLog_QueueTransmitInterval = (int) (Tracker.GeoLog.ConnectorModule.TransmitInterval / 1000);
						GeoLog_flSaveQueue = Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled;
					}
					// .
					if (Tracker.GeoLog.GPSModule != null) {
						GeoLog_GPSModuleProviderReadInterval = (int) (Tracker.GeoLog.GPSModule.Provider_ReadInterval / 1000);
						GeoLog_GPSModuleMapID = Tracker.GeoLog.GPSModule.MapID;
					}
					// .
					if (Tracker.GeoLog.VideoRecorderModule != null)
						GeoLog_VideoRecorderModuleEnabled = Tracker.GeoLog.VideoRecorderModule.flEnabled;
				}
				break; // . >
			default:
				throw new Exception("unknown configuration version, version: "
						+ Integer.toString(Version)); // . =>
			}
			flChanged = false;
			// . load reflection window
			FN = TReflector.ProfileFolder + "/" + ReflectionWindowFileName;
			F = new File(FN);
			if (F.exists()) {
				FileSize = F.length();
				FIS = new FileInputStream(FN);
				try {
					ReflectionWindowData = new byte[(int) FileSize];
					FIS.read(ReflectionWindowData);
				} finally {
					FIS.close();
				}
			} else
				ReflectionWindowData = null;
			// . load reflection window disabled lays
			LoadReflectionWindowDisabledLays();
			// .
			return true;
		}

		public void Load() throws Exception {
			int TryCount = 3;
			int SleepTime = 1000;
			for (int I = 0; I < TryCount; I++) {
				try {
					String FN = TReflector.ProfileFolder + "/" + ConfigurationFileName;
					if (!_Load(FN))
						break; // . >
					return; // . ->
				} catch (Exception E) {
					Thread.sleep(SleepTime);
				}
			}
			String FN = TReflector.ProfileFolder+"/"+ConfigurationFileName+"."+LastConfigurationFilePrefix;
			if (_Load(FN))
				return; // . ->
			throw new Exception(context.getString(R.string.SErrorOfConfigurationLoading)+ConfigurationFileName); // . =>
		}

		public void SaveReflectionWindowDisabledLays() throws IOException {
			String FN;
			TSpaceLays Lays = Reflector.ReflectionWindow.getLays();
			if (Lays != null) {
				ReflectionWindow_DisabledLaysIDs = Lays.GetDisabledLaysIDs();
				// .
				FN = TReflector.ProfileFolder + "/"
						+ ReflectionWindowDisabledLaysFileName;
				if (ReflectionWindow_DisabledLaysIDs != null) {
					String TFN = FN+".tmp";
					FileOutputStream FOS = new FileOutputStream(TFN);
					try {
						for (int I = 0; I < ReflectionWindow_DisabledLaysIDs.length; I++) {
							byte[] BA = TDataConverter
									.ConvertInt32ToBEByteArray(ReflectionWindow_DisabledLaysIDs[I]);
							FOS.write(BA);
						}
					} finally {
						FOS.close();
					}
					File TF = new File(TFN);
					File F = new File(FN);
					TF.renameTo(F);
				} else {
					File F = new File(FN);
					F.delete();
				}
			}
		}

		public void Save() throws IOException {
			//. save reflection window disabled lays
			SaveReflectionWindowDisabledLays();
			//. save reflection window
			ReflectionWindowData = Reflector.ReflectionWindow.GetWindow().ToByteArray();
			String FN = TReflector.ProfileFolder+"/"+ReflectionWindowFileName;
			if (ReflectionWindowData != null) {
				String TFN = FN+".tmp";
				FileOutputStream FOS = new FileOutputStream(TFN);
				try {
					FOS.write(ReflectionWindowData);
				} finally {
					FOS.close();
				}
				File TF = new File(TFN);
				File F = new File(FN);
				TF.renameTo(F);
			} else {
				File F = new File(FN);
				F.delete();
			}
			//.
			if (flChanged) {
				FN = ProfileFolder + "/" + ConfigurationFileName;
				String TFN = FN+".tmp";
			    XmlSerializer serializer = Xml.newSerializer();
			    FileWriter writer = new FileWriter(TFN);
				try {
					String S;
					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", "ROOT");
					// .
					serializer.startTag("", "Version");
					serializer.text(Integer.toString(ConfigurationFileVersion));
					serializer.endTag("", "Version");
					// .
					serializer.startTag("", "ServerAddress");
					serializer.text(ServerAddress);
					serializer.endTag("", "ServerAddress");
					// .
					serializer.startTag("", "ServerPort");
					serializer.text(Integer.toString(ServerPort));
					serializer.endTag("", "ServerPort");
					// .
					serializer.startTag("", "UserID");
					serializer.text(Integer.toString(UserID));
					serializer.endTag("", "UserID");
					// .
					serializer.startTag("", "UserName");
					serializer.text(UserName);
					serializer.endTag("", "UserName");
					// .
					serializer.startTag("", "UserPassword");
					serializer.text(UserPassword);
					serializer.endTag("", "UserPassword");
					// .
					serializer.startTag("", "GeoSpaceID");
					serializer.text(Integer.toString(GeoSpaceID));
					serializer.endTag("", "GeoSpaceID");
					// .
					serializer.startTag("", "ReflectionWindow_ViewMode");
					serializer.text(Integer.toString(ReflectionWindow_ViewMode));
					serializer.endTag("", "ReflectionWindow_ViewMode");
					// .
					if (ReflectionWindow_flShowHints)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "ReflectionWindow_flShowHints");
					serializer.text(S);
					serializer.endTag("", "ReflectionWindow_flShowHints");
					// .
					serializer.startTag("",
							"ReflectionWindow_ViewMode_Tiles_Compilation");
					serializer.text(ReflectionWindow_ViewMode_Tiles_Compilation);
					serializer.endTag("",
							"ReflectionWindow_ViewMode_Tiles_Compilation");
					// .
					serializer.startTag("", "ReflectionWindow_NavigationMode");
					serializer.text(Integer.toString(ReflectionWindow_NavigationMode));
					serializer.endTag("", "ReflectionWindow_NavigationMode");
					// .
					if (GeoLog_flEnabled)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flEnabled");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flEnabled");
					// .
					if (GeoLog_flServerConnection)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flServerConnection");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flServerConnection");
					//.
					serializer.startTag("", "GeoLog_ServerAddress");
					serializer.text(GeoLog_ServerAddress);
					serializer.endTag("", "GeoLog_ServerAddress");
					//.
					serializer.startTag("", "GeoLog_ServerPort");
					serializer.text(Integer.toString(GeoLog_ServerPort));
					serializer.endTag("", "GeoLog_ServerPort");
					//.
					serializer.startTag("", "GeoLog_ObjectID");
					serializer.text(Integer.toString(GeoLog_ObjectID));
					serializer.endTag("", "GeoLog_ObjectID");
					//.
					serializer.startTag("", "GeoLog_ObjectName");
					serializer.text(GeoLog_ObjectName);
					serializer.endTag("", "GeoLog_ObjectName");
					//.
					if (GeoLog_flHide)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flHide");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flHide");
					//.
					serializer.endTag("", "ROOT");
					serializer.endDocument();
				} finally {
					writer.close();
				}
				File TF = new File(TFN);
				File F = new File(FN);
				if (F.exists()) {
					File LFN = new File(ProfileFolder+"/"+ConfigurationFileName+"."+LastConfigurationFilePrefix);
					F.renameTo(LFN);
				}
				TF.renameTo(F);
				flChanged = false;		
			}
		}

		public void Validate() throws Exception {
			Reflector.Server.SetServerAddress(ServerAddress,ServerPort);
			Reflector.InitializeUser();
			// .
			Reflector.CoGeoMonitorObjects = new TReflectorCoGeoMonitorObjects(Reflector);
			if (Reflector.CoGeoMonitorObjectsLocationUpdating != null) {
				Reflector.CoGeoMonitorObjectsLocationUpdating.Cancel();
				Reflector.CoGeoMonitorObjectsLocationUpdating = null;
			}
			Reflector.CoGeoMonitorObjectsLocationUpdating = Reflector.new TCoGeoMonitorObjectsLocationUpdating(Reflector);
			// . validate tracker
			try {
				TTrackerService _Service = TTrackerService.GetService();
				if (_Service != null)
					_Service.SetServicing(false);
				//. Set tracker configuration as well
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					Tracker.GeoLog.Stop();
					//.
					//. not needed, managed at the tracker panel Tracker.GeoLog.flEnabled = this.GeoLog_flEnabled;
					Tracker.GeoLog.UserID = this.UserID;
					Tracker.GeoLog.UserPassword = this.UserPassword;
					Tracker.GeoLog.ObjectID = this.GeoLog_ObjectID;
					//.
					if (Tracker.GeoLog.ConnectorModule != null) {
						Tracker.GeoLog.ConnectorModule.flServerConnectionEnabled = this.GeoLog_flServerConnection;
						Tracker.GeoLog.ConnectorModule.ServerAddress = this.ServerAddress;
						Tracker.GeoLog.ConnectorModule.ServerPort = this.GeoLog_ServerPort;
						Tracker.GeoLog.ConnectorModule.TransmitInterval = this.GeoLog_QueueTransmitInterval * 1000;
						Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled = this.GeoLog_flSaveQueue;
					}
					if (Tracker.GeoLog.GPSModule != null) {
						Tracker.GeoLog.GPSModule.Provider_ReadInterval = this.GeoLog_GPSModuleProviderReadInterval * 1000;
						Tracker.GeoLog.GPSModule.MapID = this.GeoLog_GPSModuleMapID;
					}
					if (Tracker.GeoLog.VideoRecorderModule != null)
						Tracker.GeoLog.VideoRecorderModule.flEnabled = this.GeoLog_VideoRecorderModuleEnabled;
					// .
					Tracker.GeoLog.SaveProfile();
				}
				// .
				TTracker.FreeTracker();
				TTracker.CreateTracker(context);
				// . start tracker service if needed
				if (_Service != null)
					_Service.SetServicing(true);
			} catch (Exception E) {
				Toast.makeText(context, E.getMessage(), Toast.LENGTH_SHORT)
						.show();
			}
			// .
			Reflector.StartUpdatingSpaceImage();
		}
	}

	private class TUserIncomingMessageReceiver extends TGeoScopeServerUser.TIncomingMessages.TReceiver {
		
		private TGeoScopeServerUser MyUser;
		
		public TUserIncomingMessageReceiver(TGeoScopeServerUser pUser) throws Exception {
			MyUser = pUser;
			//.
			MyUser.IncomingMessages.AddReceiver(this,true,false);
		}
		
		public void Destroy() {
			MyUser.IncomingMessages.RemoveReceiver(this);
		}
		
		@Override
		public boolean DoOnCommand(TGeoScopeServerUser User, TIncomingCommandMessage Message) {
			if (Message instanceof TGeoScopeServerUser.TLocationCommandMessage) {
				try {
					final TGeoScopeServerUser.TLocationCommandMessage _Message = (TGeoScopeServerUser.TLocationCommandMessage)Message;
					String _UserText;
					if (Message.Sender != null)
						_UserText = Message.Sender.UserName+"\n"+"  "+Message.Sender.UserFullName;
					else
						_UserText = "? (ID: "+Integer.toString(Message.SenderID)+")";
					final String UserText = _UserText; 
				    new AlertDialog.Builder(TReflector.this)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle(R.string.SConfirmation)
			        .setMessage(getString(R.string.SUser)+UserText+"\n"+getString(R.string.SHaveSentToYouANewPlace)+"\n"+"  "+_Message.Location.Name+"\n"+getString(R.string.SDoYouWantToAddPlace))
				    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
				    	
				    	public void onClick(DialogInterface dialog, int id) {
							try {
								ElectedPlaces.AddPlace(_Message.Location);
								_Message.SetAsProcessed();
								//.
								SetReflectionWindowByLocation(_Message.Location);
								//.
								Toast.makeText(TReflector.this, getString(R.string.SPlace1)+"'"+_Message.Location.Name+"'"+getString(R.string.SHasBeenAddedToYourList), Toast.LENGTH_SHORT).show();
							} catch (Exception E) {
								Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
							}
				    	}
				    })
				    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
				    	
				    	public void onClick(DialogInterface dialog, int id) {
							try {
								_Message.SetAsProcessed();
							} catch (Exception E) {
								Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
							}
				    	}
				    })
				    .show();
					//.
					return true; //. ->
				} catch (Exception E) {
					Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
			else
				if (Message instanceof TGeoScopeServerUser.TGeoMonitorObjectCommandMessage) {
					try {
						final TGeoScopeServerUser.TGeoMonitorObjectCommandMessage _Message = (TGeoScopeServerUser.TGeoMonitorObjectCommandMessage)Message;
						String _UserText;
						if (Message.Sender != null)
							_UserText = Message.Sender.UserName+"\n"+"  "+Message.Sender.UserFullName;
						else
							_UserText = "? (ID: "+Integer.toString(Message.SenderID)+")";
						final String UserText = _UserText;
					    new AlertDialog.Builder(TReflector.this)
				        .setIcon(android.R.drawable.ic_dialog_alert)
				        .setTitle(R.string.SConfirmation)
				        .setMessage(getString(R.string.SUser)+UserText+"\n"+getString(R.string.SHaveSentToYouANewObject)+"\n"+"  "+_Message.CoGeoMonitorObject.Name+"\n"+getString(R.string.SDoYouWantToAddObject))
					    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
					    	
					    	public void onClick(DialogInterface dialog, int id) {
								try {
									TReflectorCoGeoMonitorObject Item = _Message.CoGeoMonitorObject;
									//.
									Item.flEnabled = false;
									Item.Prepare(TReflector.this);
									//.
									CoGeoMonitorObjects.AddItem(Item);
									_Message.SetAsProcessed();
									//.
									Toast.makeText(TReflector.this, getString(R.string.SObject)+"'"+_Message.CoGeoMonitorObject.Name+"'"+getString(R.string.SHasBeenAddedToYourList1), Toast.LENGTH_SHORT).show();
								} catch (Exception E) {
									Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
					    	}
					    })
					    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
					    	
					    	public void onClick(DialogInterface dialog, int id) {
								try {
									_Message.SetAsProcessed();
								} catch (Exception E) {
									Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
					    	}
					    })
					    .show();
						//.
						return true; //. ->
					} catch (Exception E) {
						Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			return false;
		}

		@Override
		public boolean DoOnCommandResponse(TGeoScopeServerUser User, TIncomingCommandResponseMessage Message) {
			return false;
		}
		
		@Override
		public boolean DoOnMessage(TGeoScopeServerUser User, TIncomingMessage Message) {
			TUserChatPanel UCP = TUserChatPanel.Panels.get(Message.SenderID);
			if (UCP == null) {
	        	Intent intent = new Intent(TReflector.this, TUserChatPanel.class);
	        	//.
	        	intent.putExtra("UserID",Message.Sender.UserID);
	        	intent.putExtra("UserName",Message.Sender.UserName);
	        	intent.putExtra("UserFullName",Message.Sender.UserFullName);
	        	intent.putExtra("UserContactInfo",Message.Sender.UserContactInfo);
	        	//.
	        	intent.putExtra("MessageID",Message.ID);
	        	//.
	        	startActivity(intent);
			}
			else 
				UCP.ReceiveMessage(Message);
			return true; 
		}
	}
	
	public static class TWorkSpace extends ImageView {

		public static float LeftGroupButtonXFitFactor = 1/10.0F;
		//.
		public static int UpdateTransitionInterval = 50; //. milliseconds 
		public static int UpdateTransitionStep = 25; //. % 
		
		public static class TButtons {

			public static class TButton {

				public static final int STYLE_RECTANGLE 	= 0;
				public static final int STYLE_ELLIPSE 		= 1;
				
				public static final int STATUS_UP 			= 0;
				public static final int STATUS_DOWN 		= 1;
				
				public static String 	FontName = "Serif";
				public static float 	FontSize = 24.0F;

				public static class TStateColorProvider {
					
					public int GetStateColor() {
						return Color.BLACK;
					}
				}
				
				public int GroupID;
				public int Style = STYLE_RECTANGLE;
				//.
				public float 	Left;
				public float 	Top;
				public float 	Width;
				public float 	Height;
				public String Name;
				public boolean flEnabled = true;
				public int 					TextColor = Color.RED;
				public TStateColorProvider 	StateColorProvider = null;
				public int Status;

				public TButton(int pGroupID, int pStyle, float pLeft, float pTop, float pWidth,float pHeight, String pName, int pTextColor) {
					GroupID = pGroupID;
					Style = pStyle;
					Left = pLeft;
					Top = pTop;
					Width = pWidth;
					Height = pHeight;
					Name = pName;
					TextColor = pTextColor;
					Status = STATUS_UP;
				}

				public TButton(int pGroupID, float pLeft, float pTop, float pWidth,float pHeight, String pName, int pTextColor) {
					this(pGroupID,STYLE_ELLIPSE, pLeft,pTop, pWidth,pHeight, pName, pTextColor);
				}

				public void SetStateColorProvider(TStateColorProvider pStateColorProvider) {
					StateColorProvider = pStateColorProvider;
				}
				
				public void SetStatus(int pStatus) {
					if (!flEnabled)
						return; //. ->
					Status = pStatus;
				}
				
				public void SetStyle(int pStyle) {
					Style = pStyle;
				}
			}

			protected static RectF Extent = new RectF();
			
			public TWorkSpace WorkSpace;
			public TButton[] Items;
			public Paint paint = new Paint();
			public int DownButtonIndex = -1;
			public long DownButtons_Time = Calendar.getInstance().getTime().getTime();

			public TButtons(TWorkSpace pWorkSpace) {
				WorkSpace = pWorkSpace;
				Items = new TButton[0];
				//.
				Typeface tf = Typeface.create(TButton.FontName,Typeface.BOLD);
		   		paint.setTypeface(tf);
				paint.setAntiAlias(true);
		   	}

			public void SetButtons(TButton[] Buttons) {
				Items = Buttons;
			}

			public int GetValidItemCount(int GroupID) {
				int Result = 0;
				for (int I = 0; I < Items.length; I++) 
					if ((Items[I] != null) && (Items[I].GroupID == GroupID))
						Result++;
				return Result;
			}
			
			public void Draw(Canvas canvas) {
				for (int I = 0; I < Items.length; I++) {
					TButton Item = Items[I];
					if (Item == null)
						continue; //. ^
					switch (Item.Style) {
						
						case TButton.STYLE_RECTANGLE:
							Extent.left = Item.Left;
							Extent.right = Item.Left + Item.Width;
							Extent.top = Item.Top;
							Extent.bottom = Item.Top + Item.Height;
							if (Item.flEnabled) 
								if (Item.Status == TButton.STATUS_DOWN) {
									paint.setColor(Color.RED);
									paint.setAlpha(100);
								} else {
									paint.setColor(Color.GRAY);
									paint.setAlpha(128);
								}
							else {
									paint.setColor(Color.DKGRAY);
									paint.setAlpha(128);
							}
							paint.setStrokeWidth(0);
							paint.setStyle(Paint.Style.FILL);
							canvas.drawRect(Extent, paint);
							//.
							paint.setStrokeWidth(WorkSpace.Reflector.metrics.density*1.0F);
							paint.setStyle(Paint.Style.STROKE);
							paint.setColor(Color.LTGRAY);
							canvas.drawRect(Extent, paint);
							break; //. >
							
						case TButton.STYLE_ELLIPSE:
							Extent.left = Item.Left;
							Extent.right = Item.Left + Item.Width;
							Extent.top = Item.Top;
							Extent.bottom = Item.Top + Item.Height;
							//.
							if (Item.flEnabled) 
								if (Item.Status == TButton.STATUS_DOWN) {
									paint.setColor(Color.RED);
									paint.setAlpha(100);
								} else {
									paint.setColor(Color.GRAY);
									paint.setAlpha(128);
								}
							else {
									paint.setColor(Color.DKGRAY);
									paint.setAlpha(128);
							}
							paint.setStrokeWidth(0.0F);
							paint.setStyle(Paint.Style.FILL);
							canvas.drawOval(Extent, paint);
							//.
							paint.setStrokeWidth(WorkSpace.Reflector.metrics.density*1.0F);
							paint.setStyle(Paint.Style.STROKE);
							paint.setColor(Color.LTGRAY);
							canvas.drawOval(Extent, paint);
							break; //. >
					}
					if (Item.flEnabled) 
						if (Item.Status == TButton.STATUS_DOWN)
							paint.setColor(Color.WHITE);
						else {
							if (Item.StateColorProvider != null)
								paint.setColor(Item.StateColorProvider.GetStateColor());
							else
								paint.setColor(Item.TextColor);
						}
					else
						paint.setColor(Color.GRAY);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(TButton.FontSize*WorkSpace.Reflector.metrics.density);
					String S = Item.Name;
					Rect bounds = new Rect();
					paint.getTextBounds(S, 0,S.length(), bounds);
					canvas.drawText(S, Item.Left+(Item.Width-paint.measureText(S))/2.0F, Item.Top+(Item.Height+bounds.height())/2.0F, paint);
				}
			}

			public int GetItemAt(double pX, double pY) {
				for (int I = 0; I < Items.length; I++)
					if ((Items[I] != null) && (((Items[I].Left <= pX) && (pX <= (Items[I].Left + Items[I].Width)))
							&& ((Items[I].Top <= pY) && (pY <= (Items[I].Top + Items[I].Height)))))
						return I; // . ->
				return -1;
			}

			public void SetDownButton(int pDownButtonIndex) {
				Items[pDownButtonIndex].SetStatus(TButton.STATUS_DOWN);
				DownButtonIndex = pDownButtonIndex;
				// .
				DownButtons_Time = Calendar.getInstance().getTime().getTime();
				//.
				WorkSpace.invalidate();
			}

			public void ClearDownButton() {
				Items[DownButtonIndex].SetStatus(TButton.STATUS_UP);
				DownButtonIndex = -1;
				// .
				DownButtons_Time = Calendar.getInstance().getTime().getTime();
				//.
				WorkSpace.invalidate();
			}
		}

		public static class TNavigationArrows {
			
			public static float ArrowSize = 48.0F;
			
			public static double ScalingDelta = 8.0;
			public static double RotatingDelta = Math.PI/96.0F;
			
			protected static class TArrow {

				public static final int STATUS_UP = 0;
				public static final int STATUS_DOWN = 1;
				//.
				public static final int ProcessDelay = 333; //. ms
				public static final int RepeatingDelay = 25; //. ms

				public static final float ArrowUnitFactor = 1.0F/9;
				
				protected static Paint paint = new Paint();
				protected static Paint arrowpaint = new Paint();
				protected static float[] Frame = new float[16];
				
				protected TNavigationArrows Arrows;
				//.
				public float 	Left;
				public float 	Top;
				public float 	Width;
				public float 	Height;
				public boolean flEnabled = true;
				public int Status;
				private int BackgroundColor = Color.GRAY;
				//.
				private Timer 			Handler = null;
				private THandlerTask 	HandlerTask = null;

				public TArrow(TNavigationArrows pArrows, float pLeft, float pTop, float pWidth, float pHeight, int pBackgroundColor) {
					Arrows = pArrows;
					//.
					Left = pLeft;
					Top = pTop;
					Width = pWidth;
					Height = pHeight;
					//.
					BackgroundColor = pBackgroundColor;
					//.
					Status = STATUS_UP;
				}
				
				public void Destroy() {
					StopHandler();
				}

				public boolean SetStatus(int pStatus) {
					if (!flEnabled)
						return false; //. ->
					if (Status == pStatus)
						return false; //. ->
					Status = pStatus;
					//.
					if (Status == STATUS_DOWN)
						StartHandler();
					else
						StopHandler();
					//.
					return true;
				}
				
				private void StartHandler() {
					StopHandler();
					//.
					Process();
					//.
					HandlerTask = new THandlerTask();
			        Handler = new Timer();
			        Handler.schedule(HandlerTask,ProcessDelay,RepeatingDelay);
				}
				
				private void StopHandler() {
					if (Handler != null) {
						Handler.cancel();
						Handler = null;
					}
					if (HandlerTask != null) {
						HandlerTask.Destroy();
						HandlerTask = null;
					}
				}
				
				private boolean HandlerIsStarted() {
					return (Handler != null);
				}
				
			    private class THandlerTask extends TimerTask {
			    	
			    	public static final int MESSAGE_PROCESS = 1;
			    	
			        private final Handler MessageHandler = new Handler() {
			            @Override
			            public void handleMessage(Message msg) {
			            	switch (msg.what) {
			            	
			            	case MESSAGE_PROCESS:
			            		if (HandlerIsStarted())
			            			Process();
			            		break; //. >
			            	}
			            }
			        };
			        
			        public void Destroy() {
			        	MessageHandler.removeMessages(MESSAGE_PROCESS);
			        }

			        public void run() {
		            	MessageHandler.obtainMessage(MESSAGE_PROCESS).sendToTarget();
			        }
			    }
			    
			    protected void Process() {
			    }
			    
				protected void Draw(Canvas canvas) {
					if (flEnabled) 
						if (Status == STATUS_DOWN) {
							paint.setColor(Color.RED);
							paint.setAlpha(100);
						} else {
							paint.setColor(BackgroundColor);
							paint.setAlpha(128);
						}
					else {
							paint.setColor(Color.DKGRAY);
							paint.setAlpha(64);
					}
					canvas.drawRect(Left, Top,Left+Width, Top+Height, paint);
					paint.setStrokeWidth(Arrows.WorkSpace.Reflector.metrics.density*0.5F);
					paint.setColor(BackgroundColor);
					paint.setAlpha(196);
					Frame[0] = Left;
					Frame[1] = Top;
					Frame[2] = Left+Width;
					Frame[3] = Top;
					Frame[4] = Left+Width;
					Frame[5] = Top;
					Frame[6] = Left+Width;
					Frame[7] = Top+Height;
					Frame[8] = Left+Width;
					Frame[9] = Top+Height;
					Frame[10] = Left;
					Frame[11] = Top+Height;
					Frame[12] = Left;
					Frame[13] = Top+Height;
					Frame[14] = Left;
					Frame[15] = Top;
					canvas.drawLines(Frame, paint);
				}
				
				public boolean DoOnPointerDown(double pX, double pY) {
					if (((Left <= pX) && (pX <= (Left + Width))) && ((Top <= pY) && (pY <= (Top + Height)))) 
						return SetStatus(STATUS_DOWN); //. ->
					else 
						return SetStatus(STATUS_UP); //. ->
				}
				
				public boolean Release() {
					return SetStatus(STATUS_UP);
				}
			}
			
			private static class TScalingUpArrow extends TArrow {
				
				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xDBDBDB;
				
				public TScalingUpArrow(TNavigationArrows pArrows, float pLeft, float pTop, float pWidth, float pHeight) { 
					super(pArrows, pLeft,pTop,pWidth,pHeight, BackgroundColor);
				}
				
				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					//.
					float Unit = Height*ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					//.
					float X = Left+Width/2.0F;
					float ArrowR = Width/4.0F;
					float Shift = (float)(Unit/(2.0*Math.sqrt(2.0)));
					Frame[0] = X;
					Frame[1] = Top+Height-Unit;
					Frame[2] = X;
					Frame[3] = Top+Unit;
					Frame[4] = Frame[2]+Shift;
					Frame[5] = Frame[3]-Shift;					
					Frame[6] = Frame[2]-ArrowR;
					Frame[7] = Frame[3]+ArrowR;
					Frame[8] = Frame[2]-Shift;
					Frame[9] = Frame[3]-Shift;					
					Frame[10] = Frame[2]+ArrowR;
					Frame[11] = Frame[3]+ArrowR;
					canvas.drawLines(Frame, 0,12, arrowpaint);
				}
				
				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX,pY); 
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_SCALING;
					return Result;
				}

				@Override
			    protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					//.
					double Scale = (1.0+Arrows.WorkSpace.Reflector.ScaleCoef*(-ScalingDelta)/Arrows.WorkSpace.Reflector.ReflectionWindow.getHeight());
					Arrows.WorkSpace.Reflector.NavigationTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
						Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
							Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale), (float) (1.0 / Scale), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					//.
					Arrows.WorkSpace.Update();
			    }
			}

			private static class TScalingDownArrow extends TArrow {
				
				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xDBDBDB;
				
				public TScalingDownArrow(TNavigationArrows pArrows, float pLeft, float pTop, float pWidth, float pHeight) { 
					super(pArrows, pLeft,pTop,pWidth,pHeight, BackgroundColor);
				}
				
				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					//.
					float Unit = Height*ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					//.
					float X = Left+Width/2.0F;
					float ArrowR = Width/4.0F;
					float Shift = (float)(Unit/(2.0*Math.sqrt(2.0)));
					Frame[0] = X;
					Frame[1] = Top+Unit;
					Frame[2] = X;
					Frame[3] = Top+Height-Unit;
					Frame[4] = Frame[2]+Shift;
					Frame[5] = Frame[3]+Shift;					
					Frame[6] = Frame[2]-ArrowR;
					Frame[7] = Frame[3]-ArrowR;
					Frame[8] = Frame[2]-Shift;
					Frame[9] = Frame[3]+Shift;					
					Frame[10] = Frame[2]+ArrowR;
					Frame[11] = Frame[3]-ArrowR;
					canvas.drawLines(Frame, 0,12, arrowpaint);
				}
				
				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX,pY); 
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_SCALING;
					return Result;
				}

				@Override
			    protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					//.
					double Scale = (1.0+Arrows.WorkSpace.Reflector.ScaleCoef*ScalingDelta/Arrows.WorkSpace.Reflector.ReflectionWindow.getHeight());
					Arrows.WorkSpace.Reflector.NavigationTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
						Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
							Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix.postScale((float) Scale, (float) Scale, Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale), (float) (1.0 / Scale), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					//.
					Arrows.WorkSpace.Update();
			    }
			}

			private static class TRotatingUpArrow extends TArrow {
				
				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xBABABA;
				
				public TRotatingUpArrow(TNavigationArrows pArrows, float pLeft, float pTop, float pWidth, float pHeight) { 
					super(pArrows, pLeft,pTop,pWidth,pHeight, BackgroundColor);
				}
				
				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					//.
					float Unit = Height*ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					//.
					float X0 = Left+Width-Unit;
					float X1 = Left+Unit;
					float ArrowR = Width/4.0F;
					float Shift = Unit/2.0F;
					Frame[0] = X0;
					Frame[1] = Top+Height-Unit;
					Frame[2] = X1;
					Frame[3] = Top+Unit;
					Frame[4] = Frame[2];
					Frame[5] = Frame[3]-Shift;					
					Frame[6] = Frame[2];
					Frame[7] = Frame[3]+ArrowR;
					Frame[8] = Frame[2]-Shift;
					Frame[9] = Frame[3];					
					Frame[10] = Frame[2]+ArrowR;
					Frame[11] = Frame[3];
					canvas.drawLines(Frame, 0,12, arrowpaint);
				}
				
				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX,pY); 
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					return Result;
				}

				@Override
			    protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					//.
					double Gamma = RotatingDelta;
					Arrows.WorkSpace.Reflector.NavigationTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
						Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
							Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix.postRotate((float) (Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					//.
					Arrows.WorkSpace.Update();
			    }
			}

			private static class TRotatingDownArrow extends TArrow {
				
				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xBABABA;
				
				public TRotatingDownArrow(TNavigationArrows pArrows, float pLeft, float pTop, float pWidth, float pHeight) { 
					super(pArrows, pLeft,pTop,pWidth,pHeight, BackgroundColor);
				}
				
				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					//.
					float Unit = Height*ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					//.
					float X0 = Left+Width-Unit;
					float X1 = Left+Unit;
					float ArrowR = Width/4.0F;
					float Shift = Unit/2.0F;
					Frame[0] = X0;
					Frame[1] = Top+Unit;
					Frame[2] = X1;
					Frame[3] = Top+Height-Unit;
					Frame[4] = Frame[2];
					Frame[5] = Frame[3]+Shift;					
					Frame[6] = Frame[2];
					Frame[7] = Frame[3]-ArrowR;
					Frame[8] = Frame[2]-Shift;
					Frame[9] = Frame[3];					
					Frame[10] = Frame[2]+ArrowR;
					Frame[11] = Frame[3];
					canvas.drawLines(Frame, 0,12, arrowpaint);
				}
				
				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX,pY); 
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					return Result;
				}

				@Override
			    protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					//.
					double Gamma = -RotatingDelta;
					Arrows.WorkSpace.Reflector.NavigationTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
						Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
							Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix.postRotate((float) (-Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix.postRotate((float) (Gamma * 180.0 / Math.PI), Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd, Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					//.
					Arrows.WorkSpace.Update();
			    }
			}

			public TWorkSpace WorkSpace;
			//.
			private TArrow[] Items;
			//.
			public TArrow DownArrow;
			
			public TNavigationArrows(TWorkSpace pWorkSpace) {
				WorkSpace = pWorkSpace;
				//.
				Items = null;
				DownArrow = null;
			}
			
			public void Destroy() {
				ClearItems();
			}

			public void Prepare(float Width, float Height) {
				ClearItems();
				//.
				Items = new TArrow[4];
				float ArrowWidth = ArrowSize*WorkSpace.Reflector.metrics.density; 
				float ArrowHeight = ArrowWidth;
				float X = Width-ArrowWidth;
				float Y = Height/2.0F-2.0F*ArrowHeight;
				Items[0] = new TRotatingUpArrow(this, X,Y, ArrowWidth,ArrowHeight); Y+=ArrowHeight;
				Items[1] = new TScalingUpArrow(this, X,Y, ArrowWidth,ArrowHeight); Y+=ArrowHeight;
				Items[2] = new TScalingDownArrow(this, X,Y, ArrowWidth,ArrowHeight); Y+=ArrowHeight;
				Items[3] = new TRotatingDownArrow(this, X,Y, ArrowWidth,ArrowHeight); 
			}
			
			public void ClearItems() {
				if (Items == null)
					return; //. ->
				for (int I = 0; I < Items.length; I++)
					Items[I].Destroy();
				Items = null;
			}
			
			public void Draw(Canvas canvas) {
				if (Items == null)
					return; //. ->
				for (int I = 0; I < Items.length; I++)
					Items[I].Draw(canvas);
			}
			
			public TArrow GetItemAt(double pX, double pY) {
				for (int I = 0; I < Items.length; I++)
					if (((Items[I].Left <= pX) && (pX <= (Items[I].Left + Items[I].Width)))
							&& ((Items[I].Top <= pY) && (pY <= (Items[I].Top + Items[I].Height))))
						return Items[I]; // . ->
				return null;
			}

			public TArrow DoOnPointerDown(double pX, double pY) {
				TArrow Result = null;
				if (Items == null)
					return Result; //. ->
				for (int I = 0; I < Items.length; I++)
					if (Items[I].DoOnPointerDown(pX,pY))
						Result = Items[I];
				DownArrow = Result;
				if (Result != null)
					WorkSpace.invalidate();
				return Result;
			}	
			
			public boolean Release() {
				boolean Result = false; 
				if (DownArrow == null)
					return Result; //. ->
				Result = DownArrow.Release();
				DownArrow = null;
				if (Result)
					WorkSpace.invalidate();
				return Result;
			}
			
			public boolean ReleaseAll() {
				boolean Result = false;
				if (Items == null)
					return Result; //. ->
				for (int I = 0; I < Items.length; I++)
					Result |= Items[I].Release();
				if (Result)
					WorkSpace.invalidate();
				return Result;
			}
		}
		
		private TReflector Reflector = null;
		// .
		public int Width = 0;
		public int Height = 0;
		private Paint paint = new Paint();
		private Paint transitionpaint = new Paint();
		private Paint SelectedObjPaint = new Paint();
		private Paint DelimiterPaint;
		private Paint CenterMarkPaint = new Paint();
		private Bitmap BackgroundImage = null;
		private int		CurrentImageID = 0;
		public TButtons Buttons = new TButtons(this);
		private TNavigationArrows NavigationArrows;
		private int 		UpdateTransitionFactor = 0;
		private Timer		UpdateTransitionHandler = null;
		private TimerTask	UpdateTransitionHandlerTask = null;

		public TWorkSpace(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public TWorkSpace(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TWorkSpace(Context context) {
			super(context);
		}
		
		public void Initialize(TReflector pReflector) {
			Reflector = pReflector;
			//.
			setScaleType(ScaleType.MATRIX);
			//.
			SelectedObjPaint.setColor(Color.RED);
			SelectedObjPaint.setStrokeWidth(2.0F * Reflector.metrics.density);
			//.
			switch (Reflector.NavigationMode) {
			
			case NAVIGATION_MODE_NATIVE:
				DelimiterPaint = new Paint();
				DelimiterPaint.setColor(Color.RED);
				DelimiterPaint.setStrokeWidth(1.0F * Reflector.metrics.density);
				DelimiterPaint.setAlpha(160);
				break; //. >
				
			case NAVIGATION_MODE_ARROWS:
				NavigationArrows = new TNavigationArrows(this);
				break; //. >
				
			case NAVIGATION_MODE_MULTITOUCHING1:
				DelimiterPaint = new Paint();
				DelimiterPaint.setColor(Color.RED);
				DelimiterPaint.setStrokeWidth(1.0F * Reflector.metrics.density);
				DelimiterPaint.setAlpha(160);
				break; //. >
				
			}
			//. 
			CenterMarkPaint.setColor(Color.RED);
			CenterMarkPaint.setStrokeWidth(2.0F * Reflector.metrics.density);
		}

		public void Finalize() {
			UpdateTransition_Stop();
			//.
			if (BackgroundImage != null)
				BackgroundImage.recycle();
			//.
			if (NavigationArrows != null) {
				NavigationArrows.Destroy();
				NavigationArrows = null;
			}
		}
		
		public void Reinitialize(TReflector pReflector) {
			Finalize();
			//.
			Initialize(pReflector);
			//.
			onSizeChanged(Width,Height, 0,0);
		}
		
		public void Update(boolean flTransition) {
			if (flTransition) 
				UpdateTransition_StartIfNotStarted();
			else {
				UpdateTransition_Stop();
				postInvalidate();
			}
		}				
		
		public void Update() {
			Update(false);
		}				
		
		private synchronized void UpdateTransition_Start(boolean flCancel) {
			if (flCancel) 
				UpdateTransition_Stop();
			else
				if (UpdateTransitionHandler != null)
					return; //. ->
			//.	
			UpdateTransitionFactor = 0;
			//.
			UpdateTransitionHandlerTask  = new TimerTask() {
				@Override
				public void run() {
					synchronized (TWorkSpace.this) {
						UpdateTransitionFactor += UpdateTransitionStep;
						if (UpdateTransitionFactor >= 100) {
							UpdateTransition_Stop();							
						}
					}
					//.
					TWorkSpace.this.postInvalidate();
				}
			};
			//.
			UpdateTransitionHandler = new Timer();
			UpdateTransitionHandler.schedule(UpdateTransitionHandlerTask, UpdateTransitionInterval,UpdateTransitionInterval);
		}
		
		private synchronized void UpdateTransition_Start() {
			UpdateTransition_Start(true);
		}
		
		private synchronized void UpdateTransition_StartIfNotStarted() {
			UpdateTransition_Start(false);
		}
		
		private synchronized void UpdateTransition_Stop() {
			if (UpdateTransitionHandler != null) {
				UpdateTransitionHandler.cancel();
				UpdateTransitionHandler = null;
			}
			if (UpdateTransitionHandlerTask != null) {
				UpdateTransitionHandlerTask.cancel();
				UpdateTransitionHandlerTask = null;
			}
			UpdateTransitionFactor = 0;
		}
		
		private synchronized boolean UpdateTransition_IsActive() {
			return (UpdateTransitionHandlerTask != null);
		}
		
		private synchronized int UpdateTransition_GetFactor() {
			return UpdateTransitionFactor;
		}
		
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			//.
			if ((w*h) <= 0)
				return; //. ->
			//.
			if (Reflector == null)
				return; //. ->
			//.
			Width = w;
			Height = h;
			setMinimumWidth(Width);
			setMinimumHeight(Height);
			// .
			if (BackgroundImage != null)
				BackgroundImage.recycle();
			BackgroundImage = BackgroundImage_ReCreate(Width, Height);
			//.
			if (NavigationArrows != null)
				NavigationArrows.Prepare(w,h);
			// . align buttons
			float YStep = ((h+0.0F)/Buttons.GetValidItemCount(BUTTONS_GROUP_LEFT));
			float Y = 0;
			//.
			float XStep = YStep; 
			int LeftGroupButtonStyle = TButton.STYLE_ELLIPSE;
			float XS = Width*LeftGroupButtonXFitFactor;
			if (XS < XStep) {
				XStep = XS;
				LeftGroupButtonStyle = TButton.STYLE_RECTANGLE;			
			}
			//.	
			for (int I = 0; I < Buttons.Items.length; I++) 
				Buttons.Items[I].Width = XStep;
			for (int I = 0; I < Buttons.Items.length; I++)
				if (Buttons.Items[I].GroupID == BUTTONS_GROUP_LEFT)
					Buttons.Items[I].Style = LeftGroupButtonStyle;
			//.
			Buttons.Items[BUTTON_UPDATE].Top = Y+(1.0F*Reflector.metrics.density);
			Buttons.Items[BUTTON_UPDATE].Height = YStep-(1.0F*Reflector.metrics.density);
			Y += YStep;
			Buttons.Items[BUTTON_SHOWREFLECTIONPARAMETERS].Top = Y;
			Buttons.Items[BUTTON_SHOWREFLECTIONPARAMETERS].Height = YStep;
			Y += YStep;
			///? Buttons.Items[BUTTON_SUPERLAYS].Top = Y;
			//. Buttons.Items[BUTTON_SUPERLAYS].Height = YStep;
			//. Y += YStep;
			Buttons.Items[BUTTON_OBJECTS].Top = Y;
			Buttons.Items[BUTTON_OBJECTS].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_ELECTEDPLACES].Top = Y;
			Buttons.Items[BUTTON_ELECTEDPLACES].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_MAPOBJECTSEARCH].Top = Y;
			Buttons.Items[BUTTON_MAPOBJECTSEARCH].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_PREVWINDOW].Top = Y;
			Buttons.Items[BUTTON_PREVWINDOW].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_EDITOR].Top = Y;
			Buttons.Items[BUTTON_EDITOR].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_USERSEARCH].Top = Y;
			Buttons.Items[BUTTON_USERSEARCH].Height = YStep;
			Y += YStep;
			if (!Reflector.Configuration.GeoLog_flHide) {
				Buttons.Items[BUTTON_TRACKER].Top = Y;
				Buttons.Items[BUTTON_TRACKER].Height = YStep-(1.0F*Reflector.metrics.density);
				Y += YStep;
			}
			//.
			Reflector.RotatingZoneWidth = XStep;
			Buttons.Items[BUTTON_COMPASS].Left = Width-Reflector.RotatingZoneWidth+2.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Top = 2.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Width = Reflector.RotatingZoneWidth-4.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Height = Reflector.RotatingZoneWidth-4.0F*Reflector.metrics.density;
			// .
			Reflector.SpaceImage.DoOnResize(Width, Height);
			// .
			Reflector.ReflectionWindow.Resize(Width, Height);
			Reflector.RecalculateAndUpdateCurrentSpaceImage();
			// .
			Reflector.StartUpdatingSpaceImage(1000);
		}

		protected void DrawOnCanvas(Canvas canvas,
				int TransitionFactor,
				boolean flDrawBackground,
				boolean flDrawImage, 
				boolean flDrawHints,
				boolean flDrawObjectTracks, 
				boolean flDrawSelectedObject,
				boolean flDrawGeoMonitorObjects, 
				boolean flDrawControls) {
			try {
				//. get window
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				RW.MultiplyByMatrix(Reflector.ReflectionWindowTransformatrix);
				//. draw background
				if (flDrawBackground)
					canvas.drawBitmap(BackgroundImage, 0, 0, paint);
				//.
				if (flDrawImage) 
					switch (Reflector.GetViewMode()) {
					
					case VIEWMODE_REFLECTIONS:
						Reflector.SpaceReflections.ReflectionWindow_DrawOnCanvas(RW, canvas,paint);
						// .
						synchronized (Reflector.SpaceImage) {
							if (Reflector.SpaceImage.flResultBitmap) {
								canvas.save();
								try {
									canvas.concat(Reflector.SpaceImage.ResultBitmapTransformatrix);
									canvas.drawBitmap(Reflector.SpaceImage.ResultBitmap, 0,0, paint);
								}
								finally {
									canvas.restore();
								}
							}
							if (Reflector.SpaceImage.flSegments) {
								canvas.save();
								try {
									canvas.concat(Reflector.SpaceImage.SegmentsTransformatrix);
									int SX;
									Bitmap Segment;
									for (int X = 0; X < Reflector.SpaceImage.DivX; X++) {
										SX = X * Reflector.SpaceImage.SegmentWidth;
										for (int Y = 0; Y < Reflector.SpaceImage.DivY; Y++) {
											Segment = Reflector.SpaceImage.Segments[X][Y];
											if (Segment != null)
												canvas.drawBitmap(Segment, SX,Y*Reflector.SpaceImage.SegmentHeight, paint);
										}
									}
								}
								finally {
									canvas.restore();
								}
							}
						}
						break; // . >

					case VIEWMODE_TILES:
						if (TransitionFactor == 0) {
							CurrentImageID++;
							//. draw image
							try {
								Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(RW, CurrentImageID,canvas,paint,null, null);
							} catch (TTimeLimit.TimeIsExpiredException TEE) {}
							//.
							if (Reflector.SpaceTileImagery_flUseResultImage)
								synchronized (Reflector.SpaceImage) {
									if (Reflector.SpaceImage.flResultBitmap) {
										canvas.save();
										try {
											canvas.concat(Reflector.SpaceImage.ResultBitmapTransformatrix);
											canvas.drawBitmap(Reflector.SpaceImage.ResultBitmap, 0,0, paint);
										}
										finally {
											canvas.restore();
										}
									}
								}
						}
						else {
							transitionpaint.setAlpha((int)(255.0*TransitionFactor/100.0));
							//. draw transition image
							try {
								Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(RW, CurrentImageID,canvas,paint,transitionpaint, null);
							} catch (TTimeLimit.TimeIsExpiredException TEE) {}
							//.
							if (Reflector.SpaceTileImagery_flUseResultImage)
								synchronized (Reflector.SpaceImage) {
									if (Reflector.SpaceImage.flResultBitmap) {
										canvas.save();
										try {
											canvas.concat(Reflector.SpaceImage.ResultBitmapTransformatrix);
											canvas.drawBitmap(Reflector.SpaceImage.ResultBitmap, 0,0, transitionpaint);
										}
										finally {
											canvas.restore();
										}
									}
								}
						}
						break; // . >
					}
				//. draw space image hints
				if (flDrawHints) {
					if (Reflector.Configuration.ReflectionWindow_flShowHints) 
						Reflector.SpaceHints.DrawOnCanvas(RW, Reflector.DynamicHintVisibleFactor, canvas);
				}
				//.
				canvas.save();
				try {
					canvas.concat(Reflector.NavigationTransformatrix);
					//. draw tracks
					if (flDrawObjectTracks) 
						Reflector.ObjectTracks.DrawOnCanvas(canvas);
					//. draw selected object
					if (flDrawSelectedObject) {
						if ((Reflector.SelectedObj != null) && (Reflector.SelectedObj.ScreenNodes != null))
							canvas.drawLines(Reflector.SelectedObj.ScreenNodes, SelectedObjPaint);
					}
					//. draw monitor objects
					if (flDrawGeoMonitorObjects) 
						Reflector.CoGeoMonitorObjects.DrawOnCanvas(canvas);
				}
				finally {
					canvas.restore();
				}
				//. draw controls
				if (flDrawControls) {
					switch (Reflector.NavigationMode) {
					
					case NAVIGATION_MODE_NATIVE:
						//. draw navigation delimiters
						float X = Width-Reflector.RotatingZoneWidth;
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						X = Width-(Reflector.RotatingZoneWidth+Reflector.ScalingZoneWidth);
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						break; //. >
						
					case NAVIGATION_MODE_ARROWS:
						NavigationArrows.Draw(canvas);
						break; //. >

					case NAVIGATION_MODE_MULTITOUCHING1:
						//. draw rotation delimiter
						X = Width-Reflector.RotatingZoneWidth;
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						break; //. >						
					}
					//. draw buttons
					Buttons.Draw(canvas);
					//.
					ShowTitle(canvas);
					ShowStatus(canvas);
					//.
					if (ShowLogoCount > 0) {
						ShowLogo(canvas);
						ShowLogoCount--;
					} else
						ShowCenterMark(canvas);
				}
			} catch (Throwable TE) {
				TDEVICEModule.Log_WriteCriticalError(TE);
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			//.
			if (Reflector == null)
				return; //. ->
			//.
			int TransitionFactor = UpdateTransition_GetFactor();
			DrawOnCanvas(canvas, TransitionFactor, true, true, true, true, true, true, true);
		}

		public Bitmap BackgroundImage_ReCreate(int Width, int Height) {
			Bitmap Result = Bitmap.createBitmap(Width, Height,
					Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(Result);
			Paint _paint = new Paint();
			_paint.setColor(Color.GRAY);
			canvas.drawRect(0, 0, Result.getWidth(), Result.getHeight(), _paint);
			_paint.setColor(Color.DKGRAY);
			_paint.setStrokeWidth(1.0F * Reflector.metrics.density);
			float MeshStep = 5.0F * Reflector.metrics.density;
			float X = 0;
			float Y = 0;
			float W = Result.getWidth();
			float H = Result.getHeight();
			int CntX = (int) (W / MeshStep) + 1;
			int CntY = (int) (H / MeshStep) + 1;
			for (int I = 0; I < CntY; I++) {
				canvas.drawLine(0, Y, W, Y, _paint);
				Y += MeshStep;
			}
			for (int I = 0; I < CntX; I++) {
				canvas.drawLine(X, 0, X, H, _paint);
				X += MeshStep;
			}
			return Result;
		}

		public void ShowCenterMark(Canvas canvas) {
			float X = (Width / 2);
			float Y = (Height / 2);
			int R = 8;
			canvas.drawLine(X, Y - R, X, Y + R, CenterMarkPaint);
			canvas.drawLine(X - R, Y, X + R, Y, CenterMarkPaint);
		}

		private void ShowLogo(Canvas canvas) {
			Paint _paint = new Paint();
			String S = "   GeoLog  " + ProgramVersion + "   ";
			_paint.setTextSize(28.0F * Reflector.metrics.density);
			float W = _paint.measureText(S);
			float H = _paint.getTextSize();
			float Left = ((Width - W) / 2.0F);
			float Top = ((Height - H) / 2.0F);
			_paint.setColor(Color.WHITE);
			_paint.setAlpha(100);
			canvas.drawRect(Left, Top, Left + W, Top + H, _paint);
			_paint.setColor(Color.BLUE);
			_paint.setStyle(Paint.Style.FILL);
			_paint.setAntiAlias(true);
			canvas.drawText(S, Left, Top + H - 4, _paint);
		}

		private Paint ShowTitle_Paint = new Paint();

		private void ShowTitle(Canvas canvas) {
			String S = null;
			if (!(Reflector.ReflectionWindow.ActualityInterval.IsEndTimestampInfinite() || Reflector.ReflectionWindow.ActualityInterval.IsEndTimestampMax())) {
				OleDate TS = new OleDate(Reflector.ReflectionWindow.ActualityInterval.GetEndTimestamp());
				String TSS = Integer.toString(TS.year)+"/"+Integer.toString(TS.month)+"/"+Integer.toString(TS.date)+" "+Integer.toString(TS.hrs)+":"+Integer.toString(TS.min)+":"+Integer.toString(TS.sec);
				S = getContext().getString(R.string.STimestamp)+TSS;
			}
			if (S == null)
				return; // . ->
			ShowTitle_Paint.setTextSize(16.0F * Reflector.metrics.density);
			float W = ShowTitle_Paint.measureText(S);
			float H = ShowTitle_Paint.getTextSize();
			float Left = ((Width - W) / 2.0F);
			float Top = 0;
			ShowTitle_Paint.setAntiAlias(true);
			ShowTitle_Paint.setStyle(Paint.Style.FILL);
			ShowTitle_Paint.setColor(Color.BLACK);
			canvas.drawText(S, Left + 1, Top + H - 4 + 1, ShowTitle_Paint);
			ShowTitle_Paint.setColor(Color.RED);
			canvas.drawText(S, Left, Top + H - 4, ShowTitle_Paint);
		}

		private Paint ShowStatus_Paint = new Paint();
		
		private void ShowStatus(Canvas canvas) {
			String S = null;
			int ProgressSummaryValue = -1;
			int ProgressValue = -1;
			int ProgressPercentage = -1;
			//.
			TSpaceImageUpdating SpaceImageUpdating = Reflector.GetSpaceImageUpdating(); 
			if (SpaceImageUpdating != null) {
				S = getContext().getString(R.string.SImageUpdating);
				ProgressSummaryValue = SpaceImageUpdating.ImageProgressor.GetSummaryValue();
				ProgressValue = SpaceImageUpdating.ImageProgressor.GetProgressValue();
				ProgressPercentage = SpaceImageUpdating.ImageProgressor.ProgressPercentage();
			}
			else
				if (Reflector.flOffline)
					S = getContext().getString(R.string.SOfflineMode);
			//.
			if (S == null)
				return; // . ->
			if (ProgressPercentage > 0) 
				S += Integer.toString(ProgressPercentage)+"%"+" ("+Integer.toString(ProgressValue)+"/"+Integer.toString(ProgressSummaryValue)+") ";
			ShowStatus_Paint.setTextSize(16.0F * Reflector.metrics.density);
			float W = ShowStatus_Paint.measureText(S);
			float H = ShowStatus_Paint.getTextSize();
			float Left = ((Width - W) / 2.0F);
			float Top = (Height - H);
			int AlphaFactor = 200;
			ShowStatus_Paint.setAntiAlias(true);
			ShowStatus_Paint.setColor(Color.GRAY);
			ShowStatus_Paint.setAlpha(AlphaFactor);
			canvas.drawRect(Left, Top, Left + W, Top + H, ShowStatus_Paint);
			if (ProgressPercentage > 0) {
				ShowStatus_Paint.setColor(Color.WHITE);
				float PW = W*ProgressPercentage/100.0F;
				canvas.drawRect(Left, Top, Left + PW, Top + H, ShowStatus_Paint);
			}
			ShowStatus_Paint.setStyle(Paint.Style.FILL);
			ShowStatus_Paint.setColor(Color.BLACK);
			canvas.drawText(S, Left + 1, Top + H - 4 + 1, ShowStatus_Paint);
			ShowStatus_Paint.setColor(Color.RED);
			canvas.drawText(S, Left, Top + H - 4, ShowStatus_Paint);
		}
	}

	public class TSpaceImageCaching extends TCancelableThread {
		
		public static final int 	CachingDelay = 100; //. ms
		public static final double 	CachingFactor = 1.0/8;
		
		private TReflector Reflector;
		//.
		private boolean flCancelThread = false;
		//.
		private TReflectionWindowStruc ReflectionWindowToCache;
		private TAutoResetEvent	StartSignal = new TAutoResetEvent();

		public TSpaceImageCaching(TReflector pReflector) {
			Reflector = pReflector;
			// .
			_Thread = new Thread(this);
			_Thread.setPriority(Thread.MIN_PRIORITY);
			_Thread.start();
		}
		
		@Override
		public void CancelAndWait() {
			flCancelThread = true;
			Cancel();
    		StartSignal.Set();
			//.
			super.Wait();
		}

		@Override
		public void run() {
			try {
				while (!flCancelThread) {
					StartSignal.WaitOne();
					//.
					if (flCancelThread)
						return; //. ->
					//.
					TReflectionWindowStruc RW;
					synchronized (this) { 
						RW = ReflectionWindowToCache;
					}
					if (RW != null) {
						if (Reflector.Server.Info.flInitialized || Reflector.flOffline) {
							try {
								//.
								TRWLevelTileContainer[] LevelTileContainers = null;
								//. caching
								switch (GetViewMode()) {
								case VIEWMODE_REFLECTIONS:
									Reflector.SpaceReflections.CheckInitialized();
									//.
									Reflector.SpaceReflections.CacheReflectionsSimilarTo(RW);
									break; // . >

								case VIEWMODE_TILES:
									Reflector.SpaceTileImagery.CheckInitialized();
									// .
									LevelTileContainers = Reflector.SpaceTileImagery.ActiveCompilationSet_GetLevelTileRange(RW);
									// .
									Reflector.SpaceTileImagery.ActiveCompilationSet_RestoreTiles(LevelTileContainers, Canceller, null);
									break; // . >
								}
								//.
								Thread.sleep(CachingDelay);
							} catch (CancelException CE) {
								Canceller.flCancel = false;
							}
						}
					}
				}
			} catch (InterruptedException E) {
			} catch (CancelException CE) {
			} catch (NullPointerException NPE) { 
				if (Reflector.flVisible) 
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, NPE.getMessage()).sendToTarget();
			} catch (IOException E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			} catch (Throwable E) {
				///- TDEVICEModule.Log_WriteCriticalError(E);
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			}
		}
		
    	public void Start(TReflectionWindowStruc RW) {
    		Canceller.flCancel = true;
    		//.
    		synchronized (this) {
				ReflectionWindowToCache = RW;
			}
    		StartSignal.Set();
    	}
    	
    	public void Stop() {
    		synchronized (this) {
    			if (ReflectionWindowToCache != null) {
    				ReflectionWindowToCache = null;
        			Canceller.flCancel = true;
    			}
    		}
    	}    	

    	public void TryToCacheCurrentWindow() {
    		TReflectionWindowStruc CacheRW;
    		synchronized (this) {
				CacheRW = ReflectionWindowToCache;
			}
    		//.
    		TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
    		RW.MultiplyByMatrix(Reflector.ReflectionWindowTransformatrix);
    		//.
    		if (CacheRW != null) {
    			double Dmin = CacheRW.Container_Xmax-CacheRW.Container_Xmin;
    			double D = CacheRW.Container_Ymax-CacheRW.Container_Ymin;
    			if (D < Dmin)
    				Dmin = D;
    			D = RW.Container_Xmax-RW.Container_Xmin;
    			if (D < Dmin)
    				Dmin = D;
    			D = RW.Container_Ymax-RW.Container_Ymin;
    			if (D < Dmin)
    				Dmin = D;
    			Dmin = Dmin*CachingFactor;
    			//.
    			if (((int)(CacheRW.Container_Xmin/Dmin) == (int)(RW.Container_Xmin/Dmin)) && ((int)(CacheRW.Container_Xmax/Dmin) == (int)(RW.Container_Xmax/Dmin)) && ((int)(CacheRW.Container_Ymin/Dmin) == (int)(RW.Container_Ymin/Dmin)) && ((int)(CacheRW.Container_Ymax/Dmin) == (int)(RW.Container_Ymax/Dmin)))
    				return; //. ->
    			//. check if scaling up then don't cache
    			if (RW.Container_S > CacheRW.Container_S) {
    	    		Canceller.flCancel = true;
    	    		//.
    	    		synchronized (this) {
    					ReflectionWindowToCache = RW;
    				}
    	    		//.
    				return; //. ->
    			}
    			//. start caching
    			Start(RW);
    		}
    		else {
	    		synchronized (this) {
					ReflectionWindowToCache = RW;
				}
    		}
    	}    	
	}
	
	public class TSpaceImageUpdating extends TCancelableThread {

		private class TImageUpdater extends TUpdater {
			@Override
			public boolean Update() {
				Reflector.WorkSpace.Update(true);
				return super.Update();
			}
		}

		public class TImageProgressor extends TProgressor {
			
			public static final int PercentageDelta = 5;
			
			@Override
			public synchronized boolean DoOnProgress(int Percentage) {
				if ((Percentage-this.Percentage) < PercentageDelta)
					return false; //. ->
				if (super.DoOnProgress(Percentage)) {
					Reflector.WorkSpace.Update(true);
					return true; //. ->
				}
				else 
					return false; //. -> 
			}			
		}

		private class TCompilationTilesPreparing {

			private class TCompilationTilesPreparingThread implements Runnable {

				private TTileServerProviderCompilation Compilation;
				private TRWLevelTileContainer LevelTileContainer;
				private TCanceller Canceller;
				private TUpdater Updater;
				private TProgressor Progressor;
				// .
				private Thread _Thread;
				private Throwable _ThreadException = null;

				public TCompilationTilesPreparingThread(TTileServerProviderCompilation pCompilation, TRWLevelTileContainer pLevelTileContainer, TCanceller pCanceller, TUpdater pUpdater, TProgressor pProgressor) {
					Compilation = pCompilation;
					LevelTileContainer = pLevelTileContainer;
					Canceller = pCanceller;
					Updater = pUpdater;
					Progressor = pProgressor;
					// .
					_Thread = new Thread(this);
					_Thread.setPriority(Thread.MIN_PRIORITY);
				}

				@Override
				public void run() {
					try {
						Compilation.PrepareTiles(LevelTileContainer, Canceller,	Updater, Progressor);
					} catch (InterruptedException IE) {
					} catch (Throwable E) {
						_ThreadException = E;
					}
				}

				public void Start() {
					_Thread.start();
				}
				
				public void WaitFor() throws Throwable {
					_Thread.join();
					// .
					if (_ThreadException != null)
						throw _ThreadException; // . =>
				}
			}

			private TCompilationTilesPreparingThread[] Threads;

			public TCompilationTilesPreparing(TTileServerProviderCompilation[] Compilation, TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater, TProgressor Progressor) throws InterruptedException {
				Threads = new TCompilationTilesPreparingThread[Compilation.length];
				for (int I = Compilation.length - 1; I >= 0; I--) 
					Threads[I] = new TCompilationTilesPreparingThread(Compilation[I], LevelTileContainers[I], Canceller, Updater, Progressor);
				//. start 
				for (int I = Compilation.length - 1; I >= 0; I--) 
					Threads[I].Start();
			}

			public void WaitForFinish() throws Throwable {
				for (int I = 0; I < Threads.length; I++) 
					Threads[I].WaitFor();
			}
		}

		public class TActiveCompilationUpLevelsTilesPreparing extends TCancelableThread {

			public static final int LevelStep = 3;
			
			private TRWLevelTileContainer[] LevelTileContainers;

			public TActiveCompilationUpLevelsTilesPreparing(TRWLevelTileContainer[] pLevelTileContainers) {
				LevelTileContainers = pLevelTileContainers;
				// .
				_Thread = new Thread(this);
				_Thread.setPriority(Thread.MIN_PRIORITY);
			}

			public void Start() {
				_Thread.start();
			}
			
			@Override
			public void run() {
				try {
					Reflector.SpaceTileImagery.ActiveCompilationSet_PrepareUpLevelsTiles(LevelTileContainers, LevelStep, Canceller, null);
				} catch (InterruptedException E) {
				} catch (CancelException CE) {
				} catch (NullPointerException NPE) { //. avoid on long operation
				} catch (Throwable E) {
					/*///? avoid on long operation String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,TReflector.this.getString(R.string.SErrorOfGettingUpperLayers)+S).sendToTarget();*/
				}
			}
		}

		private TReflector Reflector;
		private int Delay;
		private boolean flUpdateProxySpace;
		// .
		private TImageUpdater 		ImageUpdater;
		public TImageProgressor 	ImageProgressor;

		public TSpaceImageUpdating(TReflector pReflector, int pDelay,
				boolean pflUpdateProxySpace) throws Exception {
			Reflector = pReflector;
			Delay = pDelay;
			flUpdateProxySpace = pflUpdateProxySpace;
			// .
			ImageUpdater = new TImageUpdater();
			ImageProgressor = new TImageProgressor();
			// .
			_Thread = new Thread(this);
			_Thread.setPriority(Thread.MIN_PRIORITY);
			_Thread.start();
		}

		private void InputStream_ReadData(InputStream in, byte[] Data,
				int DataSize) throws Exception {
			int Size;
			int SummarySize = 0;
			int ReadSize;
			while (SummarySize < DataSize) {
				ReadSize = DataSize - SummarySize;
				Size = in.read(Data, SummarySize, ReadSize);
				if (Size <= 0)
					throw new Exception(
							Reflector
									.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																							// =>
				SummarySize += Size;
			}
		}

		@Override
		public void run() {
			try {
				long LastTime = Calendar.getInstance().getTime().getTime();
				//. check context storage device
				if (flCheckContextStorage) {
					flCheckContextStorage = false;
					//.
					if (!TSpace.Space.Context.Storage.CheckDeviceFillFactor()) {
						// . raise event
						Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY).sendToTarget();
					}
				}
				//. provide servers info
				boolean flServersInfoIsJustInitialized;
				try {
					flServersInfoIsJustInitialized = Reflector.Server.Info.CheckIntialized();
				}
				catch (IOException E) {
					flOffline = true;
					flServersInfoIsJustInitialized = true;
				}
				//.
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				TRWLevelTileContainer[] LevelTileContainers = null;
				//. 
				switch (GetViewMode()) {
				case VIEWMODE_REFLECTIONS:
					Reflector.SpaceReflections.CheckInitialized();
					//.
					Reflector.SpaceReflections.CacheReflectionsSimilarTo(RW);
					break; // . >

				case VIEWMODE_TILES:
					Reflector.SpaceTileImagery.CheckInitialized();
					// .
					LevelTileContainers = Reflector.SpaceTileImagery.ActiveCompilationSet_GetLevelTileRange(RW);
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED,LevelTileContainers).sendToTarget();
					if (LevelTileContainers == null)
						return; // . ->
					//.
					Reflector.SpaceTileImagery.ActiveCompilationSet_RemoveOldTiles(LevelTileContainers, Canceller);
					// .
					Reflector.SpaceTileImagery.ActiveCompilationSet_RestoreTiles(LevelTileContainers, Canceller, null);
					break; // . >
				}
				Reflector.WorkSpace.Update(true);
				//.
				if (!flOffline)
					Reflector.ReflectionWindow.CheckSpaceLays();
				//.
				if (Canceller.flCancel)
					return; // . ->
				while ((Calendar.getInstance().getTime().getTime() - LastTime) < Delay) {
					Thread.sleep(100);
					if (Canceller.flCancel)
						return; // . ->
				}
				//.
				Reflector.SpaceHints.CheckInitialized();
				switch (GetViewMode()) {
				case VIEWMODE_REFLECTIONS:
					int DivX = Reflector.SpaceImage.DivX;
					int DivY = Reflector.SpaceImage.DivY;
					URL url;
					TReflectionWindowStruc Reflection_Window;
					double Reflection_TimeStamp;
					synchronized (Reflector.ReflectionWindow) {
						url = new URL(
								Reflector.ReflectionWindow.PreparePNGImageURL(
										DivX, DivY, 1/* segments order */,
										flUpdateProxySpace));
						Reflection_TimeStamp = OleDate.ToUTCCurrentTime()
								.toDouble();
						Reflection_Window = Reflector.ReflectionWindow
								.GetWindow();
					}
					// .
					HttpURLConnection _Connection = (HttpURLConnection) url
							.openConnection();
					try {
						if (Canceller.flCancel)
							return; // . ->
						_Connection.setAllowUserInteraction(false);
						_Connection.setInstanceFollowRedirects(true);
						_Connection.setRequestMethod("GET");
						_Connection
								.setConnectTimeout(TGeoScopeServer.Connection_ConnectTimeout);
						_Connection
								.setReadTimeout(TGeoScopeServer.Connection_ReadTimeout);
						_Connection.connect();
						if (Canceller.flCancel)
							return; // . ->
						int response = _Connection.getResponseCode();
						if (response != HttpURLConnection.HTTP_OK) {
							String ErrorMessage = _Connection.getResponseMessage();
							byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
							ErrorMessage = new String(ErrorMessageBA,"windows-1251");
							throw new IOException(
									Reflector.getString(R.string.SServerError)
											+ ErrorMessage); //. =>
						}
						if (Canceller.flCancel)
							return; // . ->
						InputStream in = _Connection.getInputStream();
						if (in == null)
							throw new IOException(
									Reflector
											.getString(R.string.SConnectionError)); // .
																					// =>
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							byte[] ImageDataSize = new byte[4]; // . not used
							InputStream_ReadData(in, ImageDataSize,
									ImageDataSize.length);
							// .
							Reflector.SpaceImage.StartSegmenting();
							// .
							byte[] Data = new byte[32768]; // . max segment size
							int DataSize;
							byte SX, SY;
							int Cnt = DivX * DivY;
							for (int I = 0; I < Cnt; I++) {
								DataSize = 2 + 4;
								InputStream_ReadData(in, Data, DataSize);
								int Idx = 0;
								SX = Data[Idx];
								Idx++;
								SY = Data[Idx];
								Idx++;
								DataSize = TDataConverter
										.ConvertBEByteArrayToInt32(Data, Idx);
								Idx += 4;
								if (DataSize > Data.length)
									Data = new byte[DataSize];
								InputStream_ReadData(in, Data, DataSize);
								// .
								if (Canceller.flCancel)
									return; // . ->
								// .
								Reflector.SpaceImage.AddSegment(SX, SY, Data,
										DataSize);
								// .
								if (Canceller.flCancel)
									return; // . ->
								// .
								if (I != (Cnt - 1))
									Reflector.WorkSpace.Update(true);
							}
							Reflector.SpaceImage.FinishSegmenting(
									Reflection_TimeStamp, Reflection_Window);
							// .
							if (Canceller.flCancel)
								return; // . ->
							// . raise event
							Reflector.MessageHandler.obtainMessage(
									TReflector.MESSAGE_UPDATESPACEIMAGE)
									.sendToTarget();
							// . receiving hint's data
							byte[] HintDataSizeBA = new byte[4];
							InputStream_ReadData(in, HintDataSizeBA,
									HintDataSizeBA.length);
							int HintDataSize = TDataConverter
									.ConvertBEByteArrayToInt32(HintDataSizeBA,
											0);
							byte[] HintData = new byte[HintDataSize];
							InputStream_ReadData(in, HintData, HintDataSize);
							HintData = Reflector.SpaceHints
									.UnPackByteArray(HintData);
							Reflector.SpaceHints.ReviseItemsInReflectionWindow(
									Reflection_Window, HintData, Canceller);
							Reflector.SpaceHints.FromByteArray(HintData,
									Canceller);
							Reflector.WorkSpace.Update(true);
						} finally {
							in.close();
						}
					} finally {
						_Connection.disconnect();
					}
					break; // . >

				case VIEWMODE_TILES:
					Reflector.SpaceHints.GetHintsFromServer(Reflector.ReflectionWindow, Canceller);
					Reflector.WorkSpace.Update(true);
					//. prepare progress summary value
					int ProgressSummaryValue = 0;
					for (int I = 0; I < LevelTileContainers.length; I++)
						if (LevelTileContainers[I] != null)
							ProgressSummaryValue += ((LevelTileContainers[I].Xmx-LevelTileContainers[I].Xmn+1)*(LevelTileContainers[I].Ymx-LevelTileContainers[I].Ymn+1));
					ImageProgressor.SetSummaryValue(ProgressSummaryValue);
					//. prepare tiles
					switch (Reflector.SpaceTileImagery.ServerType) {

					case TTileImagery.SERVERTYPE_HTTPSERVER:
						//. sequential preparing in current thread 
						Reflector.SpaceTileImagery.ActiveCompilationSet_PrepareTiles(LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
						break; //. >
						
					case TTileImagery.SERVERTYPE_DATASERVER:
						///* sequential preparing in current thread 
						///* Reflector.SpaceTileImagery.ActiveCompilation_PrepareTiles(
						///*	LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
						//. preparing in seperate threads
						TCompilationTilesPreparing CompilationTilesPreparing = new TCompilationTilesPreparing(Reflector.SpaceTileImagery.ActiveCompilationSet(), LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
						CompilationTilesPreparing.WaitForFinish(); //. waiting for threads to be finished
						break; //. >
						
					default:
						//. sequential preparing in current thread 
						Reflector.SpaceTileImagery.ActiveCompilationSet_PrepareTiles(LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
						break; //. >
					}
					//. draw result image
					if (Reflector.SpaceTileImagery_flUseResultImage)
						Reflector.SpaceImage.ResultBitmap_DrawFromTileImagery(RW, Reflector.SpaceTileImagery);
					// . raise event
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_UPDATESPACEIMAGE).sendToTarget();
					// . prepare up level's tiles 
					if (_SpaceImageUpdating_flPrepareUpLevels) {
						///? _SpaceImageUpdating_flPrepareUpLevels = false;
						// .
						TRWLevelTileContainer[] _LevelTileContainers = new TRWLevelTileContainer[LevelTileContainers.length];
						for (int I = 0; I < _LevelTileContainers.length; I++)
							if (LevelTileContainers[I] != null)
								_LevelTileContainers[I] = new TRWLevelTileContainer(LevelTileContainers[I]);
						// .
						TActiveCompilationUpLevelsTilesPreparing ActiveCompilationUpLevelsTilesPreparing = new TActiveCompilationUpLevelsTilesPreparing(_LevelTileContainers);
						synchronized (Reflector) {
							if (_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing != null)
								_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing.Cancel();
							_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = ActiveCompilationUpLevelsTilesPreparing;
							_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing.Start();
						}
					}
					break; // . >
				}
				// . supply hints with images
				_Thread.setPriority(Thread.MIN_PRIORITY);
				int SupplyCount = 25;
				for (int I = 0; I < SupplyCount; I++) {
					int RC = Reflector.SpaceHints.SupplyHintsWithImageDataFiles(Canceller);
					if (RC == TSpaceHints.SHWIDF_RESULT_NOITEMTOSUPPLY)
						break; // . >
					Reflector.WorkSpace.Update(true);
					if (RC == TSpaceHints.SHWIDF_RESULT_SUPPLIED)
						break; // . >
					// .
					if (Canceller.flCancel)
						return; // . ->
				}
				// .
				synchronized (Reflector) {
					_SpaceImageUpdatingCount++;
				}
			} catch (InterruptedException E) {
			} catch (CancelException CE) {
			} catch (NullPointerException NPE) { 
				if (Reflector.flVisible) 
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, NPE.getMessage()).sendToTarget();
			} catch (IOException E) {
				if (Reflector.Reflection_FirstTryCount > 0) {
					Reflector.Reflection_FirstTryCount--;
					// . wait a moment
					try {
						Thread.sleep(1000*2);
					} catch (Exception Ex) {
						return; // . ->
					}
					// . try to update an image again
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_STARTUPDATESPACEIMAGE).sendToTarget();
				} else {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, Reflector.getString(R.string.SErrorOfUpdatingImage)+S).sendToTarget();
				}
			} catch (Throwable E) {
				///- TDEVICEModule.Log_WriteCriticalError(E);
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			}
		}
	}

	private class TSpaceObjOwnerTypedDataFileNamesLoading extends
			TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflector Reflector;
		// .
		private int OnStartDelay;
		private TSpaceObj SpaceObj;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TSpaceObjOwnerTypedDataFileNamesLoading(TReflector pReflector,
				TSpaceObj pSpaceObj, int pOnStartDelay, int pOnCompletionMessage) {
			Reflector = pReflector;
			// .
			OnStartDelay = pOnStartDelay;
			SpaceObj = pSpaceObj;
			OnCompletionMessage = pOnCompletionMessage;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				if (OnStartDelay > 0)
					Thread.sleep(OnStartDelay);
				if (Canceller.flCancel)
					return; // . ->
				// .
				String URL1 = Server.Address;
				// . add command path
				URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
						+ "/" + Integer.toString(User.UserID);
				String URL2 = "Functionality" + "/"
						+ "VisualizationOwnerDataDocument.dat";
				// . add command parameters
				int WithComponentsFlag = 1;
				URL2 = URL2
						+ "?"
						+ "1"/* command version */
						+ ","
						+ Integer.toString(SpaceObj.ptrObj)
						+ ","
						+ Integer
								.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
						+ ","
						+ Integer
								.toString(SpaceDefines.TYPEDDATAFILE_TYPE_AllName)
						+ "," + Integer.toString(WithComponentsFlag);
				// .
				byte[] URL2_Buffer;
				try {
					URL2_Buffer = URL2.getBytes("windows-1251");
				} catch (Exception E) {
					URL2_Buffer = null;
				}
				byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
				// . encode string
				StringBuffer sb = new StringBuffer();
				for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
					String h = Integer
							.toHexString(0xFF & URL2_EncryptedBuffer[I]);
					while (h.length() < 2)
						h = "0" + h;
					sb.append(h);
				}
				URL2 = sb.toString();
				// .
				String URL = URL1 + "/" + URL2 + ".dat";
				// .
				if (Canceller.flCancel)
					return; // . ->
				// .
				MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
						.sendToTarget();
				try {
					HttpURLConnection Connection = Server.OpenConnection(URL);
					try {
						if (Canceller.flCancel)
							return; // . ->
						// .
						InputStream in = Connection.getInputStream();
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							int RetSize = Connection.getContentLength();
							if (RetSize == 0) {
								SpaceObj.OwnerTypedDataFiles = null;
								return; // . ->
							}
							byte[] Data = new byte[RetSize];
							int Size;
							SummarySize = 0;
							int ReadSize;
							while (SummarySize < Data.length) {
								ReadSize = Data.length - SummarySize;
								Size = in.read(Data, SummarySize, ReadSize);
								if (Size <= 0)
									throw new Exception(
											Reflector
													.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																											// =>
								SummarySize += Size;
								// .
								if (Canceller.flCancel)
									return; // . ->
								// .
								MessageHandler
										.obtainMessage(
												MESSAGE_PROGRESSBAR_PROGRESS,
												(Integer) (100 * SummarySize / Data.length))
										.sendToTarget();
							}
							// .
							SpaceObj.OwnerTypedDataFiles = new TComponentTypedDataFiles(
									Reflector,
									SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
							// .
							int Idx = 0;
							SpaceObj.OwnerType = TDataConverter
									.ConvertBEByteArrayToInt32(Data, Idx);
							Idx += 4;
							SpaceObj.OwnerID = TDataConverter
									.ConvertBEByteArrayToInt32(Data, Idx);
							Idx += 8; // . ID: Int64
							SpaceObj.OwnerCoType = TDataConverter
									.ConvertBEByteArrayToInt32(Data, Idx);
							Idx += 4;
							if (Data.length > Idx)
								SpaceObj.OwnerTypedDataFiles
										.PrepareFromByteArrayV0(Data, Idx);
							// .
							Reflector.MessageHandler.obtainMessage(
									OnCompletionMessage, SpaceObj)
									.sendToTarget();
						} finally {
							in.close();
						}
					} finally {
						Connection.disconnect();
					}
				} finally {
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
							.sendToTarget();
				}
			} catch (InterruptedException E) {
			} catch (NullPointerException NPE) { 
        		if (Reflector.flVisible) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E).sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					Exception E = (Exception) msg.obj;
					Toast.makeText(
							TReflector.this,
							Reflector.getString(R.string.SErrorOfDataLoading)
									+ E.getMessage(), Toast.LENGTH_LONG).show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_SHOW:
					try {
						progressDialog = new ProgressDialog(Reflector);
						progressDialog.setMessage(Reflector
								.getString(R.string.SLoading));
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setIndeterminate(false);
						progressDialog.setCancelable(true);
						progressDialog
								.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface arg0) {
										Cancel();
									}
								});
						progressDialog.show();
					} catch (Exception EE) {
						Toast.makeText(TReflector.this, EE.getMessage(),
								Toast.LENGTH_LONG).show();
					}
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_HIDE:
					progressDialog.dismiss();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_PROGRESS:
					progressDialog.setProgress((Integer) msg.obj);
					// .
					break; // . >
				}
			}
		};
	}

	private class TComponentTypedDataFileNamesLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflector Reflector;
		// .
		private int ComponentType;
		private int ComponentID;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileNamesLoading(TReflector pReflector,
				int pComponentType, int pComponentID, int pOnCompletionMessage) {
			Reflector = pReflector;
			ComponentType = pComponentType;
			ComponentID = pComponentID;
			OnCompletionMessage = pOnCompletionMessage;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		public String PrepareURL() {
			String URL1 = Server.Address;
			// . add command path
			URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
					+ "/" + Integer.toString(User.UserID);
			String URL2 = "Functionality" + "/" + "ComponentDataDocument.dat";
			// . add command parameters
			int WithComponentsFlag = 1;
			URL2 = URL2
					+ "?"
					+ "1"/* command version */
					+ ","
					+ Integer.toString(ComponentType)
					+ ","
					+ Integer.toString(ComponentID)
					+ ","
					+ Integer
							.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
					+ ","
					+ Integer.toString(SpaceDefines.TYPEDDATAFILE_TYPE_AllName)
					+ "," + Integer.toString(WithComponentsFlag);
			// .
			byte[] URL2_Buffer;
			try {
				URL2_Buffer = URL2.getBytes("windows-1251");
			} catch (Exception E) {
				URL2_Buffer = null;
			}
			byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
			// . encode string
			StringBuffer sb = new StringBuffer();
			for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
				String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
				while (h.length() < 2)
					h = "0" + h;
				sb.append(h);
			}
			URL2 = sb.toString();
			// .
			String URL = URL1 + "/" + URL2 + ".dat";
			return URL;
		}

		@Override
		public void run() {
			try {
				String URL = PrepareURL();
				// .
				if (Canceller.flCancel)
					return; // . ->
				// .
				MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
						.sendToTarget();
				try {
					HttpURLConnection Connection = Server.OpenConnection(URL);
					try {
						if (Canceller.flCancel)
							return; // . ->
						// .
						InputStream in = Connection.getInputStream();
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							int RetSize = Connection.getContentLength();
							if (RetSize == 0) {
								Reflector.MessageHandler.obtainMessage(
										OnCompletionMessage, null)
										.sendToTarget();
								return; // . ->
							}
							byte[] Data = new byte[RetSize];
							int Size;
							SummarySize = 0;
							int ReadSize;
							while (SummarySize < Data.length) {
								ReadSize = Data.length - SummarySize;
								Size = in.read(Data, SummarySize, ReadSize);
								if (Size <= 0)
									throw new Exception(
											Reflector
													.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																											// =>
								SummarySize += Size;
								// .
								if (Canceller.flCancel)
									return; // . ->
								// .
								MessageHandler
										.obtainMessage(
												MESSAGE_PROGRESSBAR_PROGRESS,
												(Integer) (100 * SummarySize / Data.length))
										.sendToTarget();
							}
							// .
							TComponentTypedDataFiles OwnerTypedDataFiles = new TComponentTypedDataFiles(
									Reflector,
									SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
							OwnerTypedDataFiles.PrepareFromByteArrayV0(Data);
							// .
							Reflector.MessageHandler.obtainMessage(
									OnCompletionMessage, OwnerTypedDataFiles)
									.sendToTarget();
						} finally {
							in.close();
						}
					} finally {
						Connection.disconnect();
					}
				} finally {
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
							.sendToTarget();
				}
			} catch (InterruptedException E) {
			} catch (NullPointerException NPE) { 
        		if (Reflector.flVisible) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E).sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					Exception E = (Exception) msg.obj;
					Toast.makeText(
							TReflector.this,
							Reflector.getString(R.string.SErrorOfDataLoading)
									+ E.getMessage(), Toast.LENGTH_SHORT)
							.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_SHOW:
					progressDialog = new ProgressDialog(Reflector);
					progressDialog.setMessage(Reflector
							.getString(R.string.SLoading));
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setIndeterminate(false);
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
					progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
							Reflector.getString(R.string.SCancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Cancel();
								}
							});
					// .
					progressDialog.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_HIDE:
					progressDialog.dismiss();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_PROGRESS:
					progressDialog.setProgress((Integer) msg.obj);
					// .
					break; // . >
				}
			}
		};
	}

	private class TComponentTypedDataFileLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflector Reflector;
		// .
		private TComponentTypedDataFile ComponentTypedDataFile;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileLoading(TReflector pReflector,
				TComponentTypedDataFile pComponentTypedDataFile,
				int pOnCompletionMessage) {
			Reflector = pReflector;
			ComponentTypedDataFile = pComponentTypedDataFile;
			OnCompletionMessage = pOnCompletionMessage;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				switch (ComponentTypedDataFile.DataComponentType) {

				case SpaceDefines.idTDATAFile:
					TGeoScopeServerInfo.TInfo ServersInfo = Reflector.Server.Info.GetInfo();
					TComponentStreamServer CSS = new TComponentStreamServer(Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Reflector.User.UserID, Reflector.User.UserPassword);
					try {
						String CFN = TTypesSystem.TypesSystem.SystemTDATAFile.Context_GetFolder()+"/"+ComponentTypedDataFile.FileName();
						//.
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
						try {
							CSS.ComponentStreamServer_GetComponentStream_Begin(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							try {
								File CF = new File(CFN);
								RandomAccessFile ComponentStream = new RandomAccessFile(CF,"rw");
								try {
									ComponentStream.seek(ComponentStream.length());
									//.
									CSS.ComponentStreamServer_GetComponentStream_Read(Integer.toString(ComponentTypedDataFile.DataComponentID),ComponentStream, Canceller, new TProgressor() {
										@Override
										public synchronized boolean DoOnProgress(int Percentage) {
											MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,Percentage).sendToTarget();
											return true;
										}
									});
								}
								finally {
									ComponentStream.close();
								}
							}
							finally {
								CSS.ComponentStreamServer_GetComponentStream_End();						
							}
						}
						finally {
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
						}
						//.
						ComponentTypedDataFile.PrepareFullFromFile(CFN);
						//.
						Reflector.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
					}
					finally {
						CSS.Destroy();
					}
					break; //. >

				default:
					String URL1 = Server.Address;
					// . add command path
					URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
							+ "/" + Integer.toString(User.UserID);
					String URL2 = "Functionality" + "/"
							+ "ComponentDataDocument.dat";
					// . add command parameters
					int WithComponentsFlag = 0;
					URL2 = URL2
							+ "?"
							+ "1"/* command version */
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataComponentType)
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataComponentID)
							+ ","
							+ Integer
									.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
							+ ","
							+ Integer.toString(ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
							+ "," + Integer.toString(WithComponentsFlag);
					// .
					byte[] URL2_Buffer;
					try {
						URL2_Buffer = URL2.getBytes("windows-1251");
					} catch (Exception E) {
						URL2_Buffer = null;
					}
					byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
					// . encode string
					StringBuffer sb = new StringBuffer();
					for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
						String h = Integer
								.toHexString(0xFF & URL2_EncryptedBuffer[I]);
						while (h.length() < 2)
							h = "0" + h;
						sb.append(h);
					}
					URL2 = sb.toString();
					// .
					String URL = URL1 + "/" + URL2 + ".dat";
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
							.sendToTarget();
					try {
						HttpURLConnection Connection = Server.OpenConnection(URL);
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							InputStream in = Connection.getInputStream();
							try {
								if (Canceller.flCancel)
									return; // . ->
								// .
								int RetSize = Connection.getContentLength();
								if (RetSize == 0) {
									ComponentTypedDataFile.Data = null;
									return; // . ->
								}
								byte[] Data = new byte[RetSize];
								int Size;
								SummarySize = 0;
								int ReadSize;
								while (SummarySize < Data.length) {
									ReadSize = Data.length - SummarySize;
									Size = in.read(Data, SummarySize, ReadSize);
									if (Size <= 0)
										throw new Exception(
												Reflector
														.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																												// =>
									SummarySize += Size;
									// .
									if (Canceller.flCancel)
										return; // . ->
									// .
									MessageHandler
											.obtainMessage(
													MESSAGE_PROGRESSBAR_PROGRESS,
													(Integer) (100 * SummarySize / Data.length))
											.sendToTarget();
								}
								// .
								ComponentTypedDataFile.PrepareFromByteArrayV0(Data);
								// .
								Reflector.MessageHandler
										.obtainMessage(OnCompletionMessage,
												ComponentTypedDataFile)
										.sendToTarget();
							} finally {
								in.close();
							}
						} finally {
							Connection.disconnect();
						}
					} finally {
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
								.sendToTarget();
					}
					break; //. >
				}
			} catch (InterruptedException E) {
			} catch (CancelException E) {
			} catch (NullPointerException NPE) { 
        		if (Reflector.flVisible) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E).sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					Exception E = (Exception) msg.obj;
					Toast.makeText(
							TReflector.this,
							Reflector.getString(R.string.SErrorOfDataLoading)
									+ E.getMessage(), Toast.LENGTH_SHORT)
							.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_SHOW:
					progressDialog = new ProgressDialog(Reflector);
					progressDialog.setMessage(Reflector
							.getString(R.string.SLoading));
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setIndeterminate(false);
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
					progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
							Reflector.getString(R.string.SCancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Cancel();
								}
							});
					// .
					progressDialog.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_HIDE:
					progressDialog.dismiss();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_PROGRESS:
					progressDialog.setProgress((Integer) msg.obj);
					// .
					break; // . >
				}
			}
		};
	}

	private class TCoGeoMonitorObjectsLocationUpdating extends
			TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_UPDATESPACEIMAGE = 1;
		private static final int MESSAGE_STATUS_ALARM = 2;

		private TReflector Reflector;

		public TCoGeoMonitorObjectsLocationUpdating(TReflector pReflector) {
			Reflector = pReflector;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				Thread.sleep(3000);
				// .
				while (!Canceller.flCancel) {
					try {
						if ((!Reflector.flOffline) && Reflector.flVisible) {
							boolean flAlarm = false;
							boolean flUpdate = false;
							for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
								if (Reflector.CoGeoMonitorObjects.Items[I].flEnabled
										&& Reflector.CoGeoMonitorObjects.Items[I].flStatusIsEnabled) {
									try {
										int Mask = Reflector.CoGeoMonitorObjects.Items[I]
												.UpdateStatus();
										if (Mask > 0) {
											if ((Mask & TReflectorCoGeoMonitorObject.STATUS_flAlarm_Mask) == TReflectorCoGeoMonitorObject.STATUS_flAlarm_Mask)
												flAlarm = true;
											flUpdate = true;
										}
									} catch (Exception E) {
										MessageHandler.obtainMessage(
												MESSAGE_SHOWEXCEPTION, E)
												.sendToTarget();
									}
								}
							}
							if (flAlarm)
								MessageHandler.obtainMessage(MESSAGE_STATUS_ALARM,
										null).sendToTarget();
							if (flUpdate)
								Reflector.WorkSpace.Update();
							// .
							if (Canceller.flCancel)
								return; // . ->
							// .
							boolean flUpdateImage = false;
							for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
								if (Reflector.CoGeoMonitorObjects.Items[I].flEnabled) {
									try {
										flUpdateImage = (Reflector.CoGeoMonitorObjects.Items[I].UpdateVisualizationLocation(Reflector) || flUpdateImage);
									} catch (Exception E) {
										MessageHandler.obtainMessage(
												MESSAGE_SHOWEXCEPTION, E)
												.sendToTarget();
									}
								}
							}
							if (flUpdateImage) {
								Reflector.WorkSpace.Update();
								// .
								TSpaceImageUpdating SIU = Reflector
										.GetSpaceImageUpdating();
								if (SIU != null)
									SIU.Join();
								MessageHandler.obtainMessage(
										MESSAGE_UPDATESPACEIMAGE, null)
										.sendToTarget();
							}
							// .
							if (Canceller.flCancel)
								return; // . ->
						}
						// .
						Thread.sleep(Reflector.CoGeoMonitorObjects.GetUpdateInterval()*1000);
						// .
						if (Canceller.flCancel)
							return; // . ->
					} catch (InterruptedException E) {
						return; // . ->
					} catch (NullPointerException NPE) { 
		        		if (Reflector.flVisible) 
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
					} catch (Exception E) {
						MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E).sendToTarget();
						//.
						try {
							Thread.sleep(Reflector.CoGeoMonitorObjects.GetUpdateInterval()*1000);
							// .
							if (Canceller.flCancel)
								return; // . ->
						} catch (InterruptedException E1) {
							return; // . ->
						}
					}
				}
			} catch (InterruptedException E) {
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					Exception E = (Exception) msg.obj;
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Toast.makeText(
							TReflector.this,
							Reflector
									.getString(R.string.SErrorOfUpdatingCurrentPosition)
									+ S, Toast.LENGTH_SHORT)
							.show();
					// .
					break; // . >

				case MESSAGE_UPDATESPACEIMAGE:
					Reflector.StartUpdatingSpaceImage(true);
					// .
					break; // . >

				case MESSAGE_STATUS_ALARM:
					for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
						if (Reflector.CoGeoMonitorObjects.Items[I].Status_flAlarm) {
							PlayAlarmSound();
							// .
							Toast.makeText(
									TReflector.this,
									Reflector.getString(R.string.SAlarm)
											+ Reflector.CoGeoMonitorObjects.Items[I].LabelText,
									Toast.LENGTH_LONG).show();
						}
					}
					// .
					break; // . >
				}
			}
		};
	}

	public static final int MODE_NONE 		= 0;
	public static final int MODE_BROWSING 	= 1;
	public static final int MODE_EDITING 	= 2;
	//.
	public static final int VIEWMODE_NONE 			= 0;
	public static final int VIEWMODE_REFLECTIONS 	= 1;
	public static final int VIEWMODE_TILES 			= 2;
	//.
	public final static int NAVIGATION_MODE_NATIVE 			= 0;
	public final static int NAVIGATION_MODE_ARROWS 			= 1;
	public final static int NAVIGATION_MODE_MULTITOUCHING 	= 2;
	public final static int NAVIGATION_MODE_MULTITOUCHING1 	= 3;
	//.
	public final static int NAVIGATION_TYPE_NONE 						= 1;
	public final static int NAVIGATION_TYPE_MOVING 						= 2;
	public final static int NAVIGATION_TYPE_SCALING 					= 3;
	public final static int NAVIGATION_TYPE_ROTATING 					= 4;
	public final static int NAVIGATION_TYPE_TRANSFORMATING				= 5;
	public final static int NAVIGATION_TYPE_SCALETRANSFORMATING			= 6;
	//.
	public static final int 	MESSAGE_SHOWEXCEPTION 											= 0;
	private static final int 	MESSAGE_STARTUPDATESPACEIMAGE 									= 1;
	private static final int 	MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED			= 2;
	private static final int 	MESSAGE_UPDATESPACEIMAGE 										= 3;
	private static final int 	MESSAGE_SELECTEDOBJ_SET 										= 4;
	private static final int 	MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED 			= 5;
	private static final int 	MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED 					= 6;
	private static final int 	MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED 	= 7;
	private static final int 	MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED 		= 8;
	private static final int 	MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY 							= 9;
	// .
	private static final int REQUEST_SHOW_TRACKER 							= 1;
	private static final int REQUEST_EDIT_REFLECTOR_CONFIGURATION 			= 2;
	private static final int REQUEST_OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE 	= 3;
	private static final int REQUEST_OPEN_USERSEARCH 						= 4;
	//.
	private static final int BUTTONS_COUNT = 10;
	//.
	private static final int BUTTONS_GROUP_LEFT 	= 1;
	private static final int BUTTONS_GROUP_RIGHT 	= 2;
	//.
	private static final int BUTTON_UPDATE 						= 0;
	private static final int BUTTON_SHOWREFLECTIONPARAMETERS 	= 1;
	private static final int BUTTON_ELECTEDPLACES 				= 2;
	private static final int BUTTON_OBJECTS 					= 3;
	private static final int BUTTON_MAPOBJECTSEARCH 			= 4;
	private static final int BUTTON_PREVWINDOW 					= 5;
	private static final int BUTTON_EDITOR 						= 6;
	private static final int BUTTON_USERSEARCH 					= 7;
	private static final int BUTTON_TRACKER 					= 8;
	private static final int BUTTON_COMPASS 					= 9;

	private static boolean flCheckContextStorage = true;
	
	public TReflectorConfiguration Configuration;
	//.
	public TGeoScopeServer 					Server;
	public TGeoScopeServerUser 				User;
	public TUserIncomingMessageReceiver 	UserIncomingMessageReceiver;
	public int 								UserIncomingMessages_LastCheckInterval;
	//.
	public TReflectionWindow 	ReflectionWindow;
	private Matrix 				ReflectionWindowTransformatrix = new Matrix();
	//.
	private int Reflection_FirstTryCount = 3;
	//.
	public boolean flFullScreen;
	//.
	public DisplayMetrics metrics;
	// .
	public TWorkSpace WorkSpace;
	// .
	public int Mode = MODE_BROWSING;
	// .
	public int ViewMode = VIEWMODE_NONE;
	// .
	protected TSpaceReflections SpaceReflections;
	//.
	protected TTileImagery 		SpaceTileImagery;
	protected boolean			SpaceTileImagery_flUseResultImage = false;
	//.
	protected TSpaceHints 		SpaceHints;
	//. result image
	protected TSpaceImage 		SpaceImage;
	//.
	public boolean 	flVisible = false;
	public boolean 	flRunning = false;
	public boolean 	flOffline = false;
	private boolean flEnabled = true;
	//.
	public int 			NavigationMode = NAVIGATION_MODE_MULTITOUCHING1;
	private int 		NavigationType = NAVIGATION_TYPE_NONE;
	protected Matrix 	NavigationTransformatrix = new Matrix();
	private TTimeLimit 	NavigationDrawingTimeLimit = new TTimeLimit(100/* milliseconds */);
	//.
	private TXYCoord Pointer_Down_StartPos;
	private TXYCoord Pointer_LastPos;
	//.
	private TXYCoord Pointer1_Down_StartPos;
	private TXYCoord Pointer1_LastPos;
	//.
	private TXYCoord Pointer2_Down_StartPos;
	private TXYCoord Pointer2_LastPos;
	//.
	private float ScalingZoneWidth;
	private float RotatingZoneWidth;
	private int SelectShiftFactor = 10;
	private double ScaleCoef = 3.0;
	public int VisibleFactor = 16;
	//.
	public int 		DynamicHintVisibleFactor = 2*2;
	public double 	DynamicHintVisibility = 1.0;
	//.
	public TElectedPlaces ElectedPlaces = null;
	public TReflectionWindowStrucStack LastWindows;
	private TReflectionWindow.TObjectAtPositionGetting ObjectAtPositionGetting = null;
	//.
	public TSpaceObj SelectedObj = null;
	//.
	public TCancelableThread 	SelectedComponentTypedDataFileNamesLoading = null;
	public AlertDialog 			SelectedComponentTypedDataFileNames_SelectorPanel = null;
	public TCancelableThread 	SelectedComponentTypedDataFileLoading = null;
	// .
	private TSpaceImageCaching 												_SpaceImageCaching = null;
	//.
	private TSpaceImageUpdating 											_SpaceImageUpdating = null;
	private int 															_SpaceImageUpdatingCount = 0;
	private boolean 														_SpaceImageUpdating_flPrepareUpLevels = true;
	private TSpaceImageUpdating.TActiveCompilationUpLevelsTilesPreparing 	_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = null;
	//.
	public TReflectorCoGeoMonitorObjects 			CoGeoMonitorObjects;
	private TCoGeoMonitorObjectsLocationUpdating 	CoGeoMonitorObjectsLocationUpdating;
	// .
	public TReflectorObjectTracks ObjectTracks;
	// .
	public MediaPlayer _MediaPlayer = null;

	public final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_SHOWEXCEPTION:
				String EStr = (String) msg.obj;
				Toast.makeText(TReflector.this,
						TReflector.this.getString(R.string.SError) + EStr,
						Toast.LENGTH_SHORT).show();
				// .
				break; // . >

			case MESSAGE_STARTUPDATESPACEIMAGE:
				StartUpdatingSpaceImage();
				// .
				break; // . >

			case MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED:
				TRWLevelTileContainer[] LevelTileContainers = (TRWLevelTileContainer[])msg.obj;
				WorkSpace_Buttons_Update(LevelTileContainers);
				//.
				break; // . >

			case MESSAGE_UPDATESPACEIMAGE:
				synchronized (TReflector.this) {
					_SpaceImageUpdating = null;
				}
				// .
				RecalculateAndUpdateCurrentSpaceImage();
				//. validate space window update subscription if the window is changed
				try {
					ReflectionWindow.UpdateSubscription_Validate();
				}
				catch (Exception E) {
					Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_SHORT).show();
				}
				// . add new window to last windows
				TReflectionWindowStruc RWS = ReflectionWindow.GetWindow();
				LastWindows.Push(RWS);
				// /test
				/*
				 * ActivityManager activityManager = (ActivityManager)
				 * getSystemService(ACTIVITY_SERVICE); MemoryInfo mi = new
				 * MemoryInfo(); activityManager.getMemoryInfo(mi);
				 * Toast.makeText(TReflector.this,
				 * "FreeMem: "+Integer.toString((
				 * int)(mi.availMem/(1024*1024)))+", Cached: "
				 * +Reflections.GetCachedItemsCount(),
				 * Toast.LENGTH_LONG).show();
				 */
				//.
				break; // . >

			case MESSAGE_SELECTEDOBJ_SET:
				SelectedObj = (TSpaceObj) msg.obj;
				if (SelectedObj == null)
					return; // . ->
				// .
				RecalculateAndUpdateCurrentSpaceImage();
				// .
				if (SelectedComponentTypedDataFileNamesLoading != null)
					SelectedComponentTypedDataFileNamesLoading.Cancel();
				SelectedComponentTypedDataFileNamesLoading = TReflector.this.new TSpaceObjOwnerTypedDataFileNamesLoading(
						TReflector.this, SelectedObj, 2000,
						MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED);
				// .
				break; // . >

			case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED:
				TSpaceObj Obj = (TSpaceObj) msg.obj;
				if ((Obj.OwnerTypedDataFiles != null)
						&& (Obj.OwnerTypedDataFiles.Items != null)) {
					String Hint = null;
					// . look for first DocumentName that will be a object name
					for (int I = 0; I < Obj.OwnerTypedDataFiles.Items.length; I++)
						if (Obj.OwnerTypedDataFiles.Items[I].DataType == SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName) {
							Hint = Obj.OwnerTypedDataFiles.Items[I].DataName;
							break; // . >
						}
					// .
					if (Obj.OwnerTypedDataFiles.Items.length == 1) {
						TComponentTypedDataFile ComponentTypedDataFile = Obj.OwnerTypedDataFiles.Items[0];
						// .
						if (SelectedComponentTypedDataFileLoading != null)
							SelectedComponentTypedDataFileLoading.Cancel();
						SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(
								TReflector.this, ComponentTypedDataFile,
								MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
						SelectedComponentTypedDataFileNames_SelectorPanel = null;
						if (Hint != null)
							Toast.makeText(TReflector.this,
									Hint,
									Toast.LENGTH_LONG).show();
					} else {
						SelectedComponentTypedDataFileNames_SelectorPanel = ComponentTypedDataFiles_CreateSelectorPanel(
								Obj.OwnerTypedDataFiles, TReflector.this);
						SelectedComponentTypedDataFileNames_SelectorPanel
								.show();
					}
				}
				// .
				break; // . >

			case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED:
				TComponentTypedDataFiles OwnerTypedDataFiles = (TComponentTypedDataFiles) msg.obj;
				if (OwnerTypedDataFiles != null) {
					if ((OwnerTypedDataFiles != null)
							&& (OwnerTypedDataFiles.Items != null)) {
						String Hint = null;
						// . look for first DocumentName that will be a object name
						for (int I = 0; I < OwnerTypedDataFiles.Items.length; I++)
							if (OwnerTypedDataFiles.Items[I].DataType == SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName) {
								Hint = OwnerTypedDataFiles.Items[I].DataName;
								break; // . >
							}
						// .
						if (OwnerTypedDataFiles.Items.length == 1) {
							TComponentTypedDataFile ComponentTypedDataFile = OwnerTypedDataFiles.Items[0];
							// .
							if (SelectedComponentTypedDataFileLoading != null)
								SelectedComponentTypedDataFileLoading.Cancel();
							SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(
									TReflector.this, ComponentTypedDataFile,
									MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED);
							SelectedComponentTypedDataFileNames_SelectorPanel = null;
							if (Hint != null)
								Toast.makeText(TReflector.this,
										Hint,
										Toast.LENGTH_LONG).show();
						} else {
							SelectedComponentTypedDataFileNames_SelectorPanel = ComponentTypedDataFiles_CreateSelectorPanel(
									OwnerTypedDataFiles, TReflector.this);
							SelectedComponentTypedDataFileNames_SelectorPanel
									.show();
						}
					}
				}
				// .
				break; // . >

			case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED:
			case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED:
				TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile) msg.obj;
				if (ComponentTypedDataFile != null)
					ComponentTypedDataFile_Open(ComponentTypedDataFile);
				// .
				break; // . >
				
			case MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY:
			    new AlertDialog.Builder(TReflector.this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.SAlert)
		        .setMessage(TReflector.this.getString(R.string.SDiskFillingIsNearToCapacity)+" - "+Integer.toString((int)(100.0*TSpace.Space.Context.Storage.DeviceFillFactor()))+" %"+"\n"+TReflector.this.getString(R.string.SDoYouWantToClearSomeContextData))
			    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(TReflector.this, TReflectorConfigurationPanel.class);
						startActivityForResult(intent, REQUEST_EDIT_REFLECTOR_CONFIGURATION);
			    	}
			    })
			    .setNegativeButton(R.string.SNo, null)
			    .show();
				break; // . >
			}
		}
	};

	protected BroadcastReceiver EventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				//.
				return; //. ->
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				//.
				return; // . ->
			}
		}
	};

	private static int CreateCount = 0;
	
	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context context = getApplicationContext();
		//.
		if (CreateCount == 0) {
			//. process pre-initialization
			try {
				TFileSystem.TExternalStorage.WaitForMounted();
			} catch (Exception E) {
				Toast.makeText(this,R.string.SExternalStorageIsNotMounted,Toast.LENGTH_LONG).show();
				finish();
				return; // . ->
			}
			//.
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy); 		
			}
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			//.
			try {
				TGeoLogInstallator.CheckInstallation(context);
			} catch (IOException E) {
				Toast.makeText(
						this,
						getString(R.string.SErrorOfProgramInstalling)
								+ E.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return; // . ->
			}
			//. start tracker service
			try {
				TTracker.CreateTracker(this);
			} catch (Exception E) {
				Toast.makeText(
						this,
						getString(R.string.SErrorOfTrackerCreating)
								+ E.getMessage(), Toast.LENGTH_LONG).show();
			}
			//.
			Intent TrackerServiceLauncher = new Intent(context, TTrackerService.class);
			context.startService(TrackerServiceLauncher);
			//. start server user-agent service
			try {
				TUserAgent.CreateUserAgent(this);
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return; // . ->
			}
			Intent UserAgentServiceLauncher = new Intent(context, TUserAgentService.class);
			context.startService(UserAgentServiceLauncher);
		}
		// .
		super.onCreate(savedInstanceState);
		//.
		Display display = getWindowManager().getDefaultDisplay();
		if ((android.os.Build.VERSION.SDK_INT < 14) || ViewConfiguration.get(this).hasPermanentMenuKey()) { 
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
			flFullScreen = true; 
		}
		else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
			flFullScreen = false;
		}
		// .
		metrics = context.getResources().getDisplayMetrics();
		// .
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED,
					LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
		//.
		Configuration = new TReflectorConfiguration(this,this);
		try {
			Configuration.Load();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; // . ->
		}
		//. Initialize User
		try {
			Server = TUserAgent.GetUserAgent().Server;
			//.
			InitializeUser();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; // . ->
		}
		//.
		double Xc = 317593.059;
		double Yc = -201347.576;
		TReflectionWindowStruc RW = new TReflectionWindowStruc(Xc - 10,
				Yc + 10, Xc + 10, Yc + 10, Xc + 10, Yc - 10, Xc - 10, Yc - 10,
				0, 0, 320, 240, TReflectionWindowActualityInterval.NullTimestamp, TReflectionWindowActualityInterval.MaxTimestamp);
		if (Configuration.ReflectionWindowData != null) {
			try {
				RW.FromByteArrayV1(Configuration.ReflectionWindowData);
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
				finish();
				return; //. ->
			}
		}
		try {
			ReflectionWindow = new TReflectionWindow(this, RW);
			ReflectionWindow.Normalize();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
		}
		// .
		Pointer_Down_StartPos = new TXYCoord();
		Pointer_LastPos = new TXYCoord();
		// .
		Pointer1_Down_StartPos = new TXYCoord();
		Pointer1_LastPos = new TXYCoord();
		// .
		Pointer2_Down_StartPos = new TXYCoord();
		Pointer2_LastPos = new TXYCoord();
		// .
		try {
			ElectedPlaces = new TElectedPlaces();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		// .
		LastWindows = new TReflectionWindowStrucStack(MaxLastWindowsCount);
		// .
		setContentView(R.layout.reflector);
		//.
		NavigationMode = Configuration.ReflectionWindow_NavigationMode;
		//.
		ScalingZoneWidth = 48.0F * metrics.density;
		RotatingZoneWidth = 42.0F * metrics.density;
		// .
		WorkSpace = (TWorkSpace)findViewById(R.id.ivWorkSpace);
		WorkSpace.Initialize(this);
		//.
		WorkSpace_Buttons_Recreate(false);
		//.
    	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		WorkSpace.setOnTouchListener(this);
		//.
		try {
			SpaceReflections = new TSpaceReflections(this);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
		}
		try {
			SpaceTileImagery = new TTileImagery(this,Configuration.ReflectionWindow_ViewMode_Tiles_Compilation);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
		}
		try {
			SpaceHints = new TSpaceHints(this);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
		}
		SpaceImage = new TSpaceImage(this, 16, 1);
		//.
		ViewMode = Configuration.ReflectionWindow_ViewMode;
		//.
		_SpaceImageCaching = new TSpaceImageCaching(this);
		// .
		ObjectTracks = new TReflectorObjectTracks(this);
		// .
		CoGeoMonitorObjects = new TReflectorCoGeoMonitorObjects(this);
		if (CoGeoMonitorObjectsLocationUpdating != null) {
			CoGeoMonitorObjectsLocationUpdating.Cancel();
			CoGeoMonitorObjectsLocationUpdating = null;
		}
		CoGeoMonitorObjectsLocationUpdating = new TCoGeoMonitorObjectsLocationUpdating(
				this);
		// .
		IntentFilter ScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(EventReceiver, ScreenOffFilter);
		//.
		UserIncomingMessages_LastCheckInterval = User.IncomingMessages.SetMediumCheckInterval();
		User.IncomingMessages.Check();
		//.
		SetReflector(this);
		//.
		StartUpdatingSpaceImage();
		//.
		CreateCount++;
	}

	@Override
	public void onDestroy() {
		ClearReflector(this);
		//.
		User.IncomingMessages.SetCheckInterval(UserIncomingMessages_LastCheckInterval);
		//.
		if (EventReceiver != null) {
			unregisterReceiver(EventReceiver);
			EventReceiver = null;
		}
		//.
		if (_SpaceImageUpdating != null) {
			_SpaceImageUpdating.Cancel();
			_SpaceImageUpdating = null;
		}
		//.
		if (_SpaceImageCaching != null) {
			_SpaceImageCaching.Cancel();
			_SpaceImageCaching = null;
		}
		// .
		TSpaceImageUpdating.TActiveCompilationUpLevelsTilesPreparing ActiveCompilationUpLevelsTilesPreparing;
		synchronized (this) {
			ActiveCompilationUpLevelsTilesPreparing = _SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing;
			_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = null;
		}
		if (ActiveCompilationUpLevelsTilesPreparing != null)
			ActiveCompilationUpLevelsTilesPreparing.Cancel();
		// .
		if (ObjectAtPositionGetting != null) {
			ObjectAtPositionGetting.Cancel();
			ObjectAtPositionGetting = null;
		}
		// .
		if (SelectedComponentTypedDataFileNamesLoading != null) {
			SelectedComponentTypedDataFileNamesLoading.Cancel();
			SelectedComponentTypedDataFileNamesLoading = null;
		}
		// .
		if (SelectedComponentTypedDataFileLoading != null) {
			SelectedComponentTypedDataFileLoading.Cancel();
			SelectedComponentTypedDataFileLoading = null;
		}
		// .
		if (CoGeoMonitorObjectsLocationUpdating != null) {
			CoGeoMonitorObjectsLocationUpdating.Cancel();
			CoGeoMonitorObjectsLocationUpdating = null;
		}
		//.
		if (ReflectionWindow != null) 
			try {
				ReflectionWindow.UpdateSubscription_Free();
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		//.
		if (WorkSpace != null) {
			WorkSpace.Finalize();
			WorkSpace = null;
		}
		// .
		if (SpaceImage != null) {
			SpaceImage.Destroy();
			SpaceImage = null;
		}
		if (SpaceHints != null) {
			try {
				SpaceHints.Destroy();
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			SpaceHints = null;
		}
		if (SpaceTileImagery != null) {
			try {
				SpaceTileImagery.Destroy();
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			SpaceTileImagery = null;
		}
		if (SpaceReflections != null) {
			try {
				SpaceReflections.Destroy();
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			SpaceReflections = null;
		}
		//.
		try {
			FinalizeUser();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
		/*///- using server user-agent 
		if (Server != null) {
			try {
				Server.Destroy();
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}*/
		Server = null;
		// .
		if (Configuration != null)
			try {
				Configuration.Save();
				Configuration = null;
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		super.onDestroy();
		// .
		System.gc(); // . collect garbage
	}

	@Override
	protected void onStart() {
		super.onStart();
		flRunning = true;	
	}

	@Override
	protected void onStop() {
		if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
			_MediaPlayer.stop();
		// .
		flRunning = false;
		super.onStop();
	}
	
	public void onResume() {
		super.onResume();
		flVisible = true;
        //. start tracker position fixing immediately if it is in impulse mode
        TTracker Tracker = TTracker.GetTracker();
    	if ((Tracker != null) && (Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode) 
			Tracker.GeoLog.GPSModule.LocationMonitor.flProcessImmediately = true;
	}

	public void onPause() {
	    flVisible = false;
		super.onPause();
	}
	
	private void InitializeUser() throws Exception {
		User = Server.InitializeUser(Configuration.UserID,Configuration.UserPassword);
		//. add receiver
		UserIncomingMessageReceiver = new TUserIncomingMessageReceiver(User);
	}
	
	private void FinalizeUser() throws IOException {
		//. remove receiver
		if (UserIncomingMessageReceiver != null) {
			UserIncomingMessageReceiver.Destroy();
			UserIncomingMessageReceiver = null;
		}
		//.
		if (User != null) {
			if (User.IncomingMessages != null)
				User.IncomingMessages.Save();
			User = null;
		}
	}

	public void Finish() {
		finish();
		/*
		 * ///? //. onDestroy(); //. TTracker.FreeTracker(); //. System.exit(0);
		 */
	}

	public synchronized int GetViewMode() {
		return ViewMode;
	}

	public void SetViewMode(int pViewMode) {
		if (pViewMode == ViewMode)
			return; // . ->
		// .
		synchronized (this) {
			ViewMode = pViewMode;
		}
		Configuration.ReflectionWindow_ViewMode = ViewMode;
		Configuration.flChanged = true;
		//.
		StartUpdatingSpaceImage();
	}

	public synchronized int GetNavigationMode() {
		return NavigationMode;
	}

	public void SetNavigationMode(int pNavigationMode) {
		if (pNavigationMode == NavigationMode)
			return; // . ->
		// .
		synchronized (this) {
			NavigationMode = pNavigationMode;
		}
		WorkSpace.Reinitialize(this);
		//.
		Configuration.ReflectionWindow_NavigationMode = NavigationMode;
		Configuration.flChanged = true;
		// .
		StartUpdatingSpaceImage();
	}

	public void ViewMode_Tiles_SetActiveCompilation(
			TTileImagery.TTileServerProviderCompilationDescriptors pDescriptors) {
		if (SpaceTileImagery != null) {
			CancelUpdatingSpaceImage();
			// .
			SpaceTileImagery.SetActiveCompilationSet(pDescriptors);
			// .
			Configuration.ReflectionWindow_ViewMode_Tiles_Compilation = pDescriptors.ToString();
			Configuration.flChanged = true;
			// .
			StartUpdatingSpaceImage();
		}
	}

	@Override
	public boolean onTouch(View pView, MotionEvent pEvent) {
		switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN:
			Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
			break; // . >

		case MotionEvent.ACTION_POINTER_DOWN:
			switch (pEvent.getPointerCount()) {
			
			case 1:
				Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
				break; //. >

			case 2:
				Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
				Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
				break; //. >
				
			case 3:
				Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
				Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
				Pointer2_Down(pEvent.getX(2), pEvent.getY(2));
				break; //. >
			}
			break; // . >
			
		case MotionEvent.ACTION_UP:
			Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
			break; // . >
			
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL:
			switch (pEvent.getPointerCount()) {
			
			case 1:
				Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
				break; //. >

			case 2:
				Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
				Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
				break; //. >
				
			case 3:
				Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
				Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
				Pointer2_Up(pEvent.getX(2), pEvent.getY(2));
				break; //. >
			}
			break; // . >

		case MotionEvent.ACTION_MOVE:
			switch (pEvent.getPointerCount()) {
			
			case 1:
				Pointer0_Move((ImageView) pView, pEvent.getX(0), pEvent.getY(0));
				break; //. >

			case 2:
				Pointer0_Move((ImageView) pView, pEvent.getX(0), pEvent.getY(0));
				Pointer1_Move((ImageView) pView, pEvent.getX(1), pEvent.getY(1));
				break; //. >
				
			case 3:
				Pointer0_Move((ImageView) pView, pEvent.getX(0), pEvent.getY(0));
				Pointer1_Move((ImageView) pView, pEvent.getX(1), pEvent.getY(1));
				Pointer2_Move((ImageView) pView, pEvent.getX(2), pEvent.getY(2));
				break; //. >
			}
			break; // . >

		default:
			return false; // . ->
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reflector_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		
		case R.id.UserPanel:
			intent = new Intent(this, TMyUserPanel.class);
			startActivity(intent);
			// .
			return true; // . >

		case R.id.ReflectorConfiguration:
			intent = new Intent(this, TReflectorConfigurationPanel.class);
			startActivityForResult(intent, REQUEST_EDIT_REFLECTOR_CONFIGURATION);
			// .
			return true; // . >

		case R.id.ReflectorObjectTracks:
			ObjectTracks.CreateTracksSelectorPanel(this).show();
			// .
			return true; // . >

		case R.id.Reflector_Help:
            intent = new Intent(this, TReflectorHelpPanel.class);
            startActivity(intent);
			//.
			return true; // . >

		case R.id.ExitProgram:
			Finish();
			// .
			return true; // . >
		}

		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_SHOW_TRACKER:
			break; // . >

		case REQUEST_EDIT_REFLECTOR_CONFIGURATION:
			break; // . >

		case REQUEST_OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE:
			/*
			 * ///- if (SelectedComponentTypedDataFileNames_SelectorPanel !=
			 * null) { if ((SelectedObj.OwnerTypedDataFiles.Items != null) &&
			 * (SelectedObj.OwnerTypedDataFiles.Items.length == 1)) {
			 * SelectedComponentTypedDataFileNames_SelectorPanel.dismiss();
			 * SelectedComponentTypedDataFileNames_SelectorPanel = null; } }
			 */
			break; // . >
			
		case REQUEST_OPEN_USERSEARCH:
			if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	TGeoScopeServerUser.TUserDescriptor User = new TGeoScopeServerUser.TUserDescriptor();
                	User.UserID = extras.getInt("UserID");
                	User.UserIsDisabled = extras.getBoolean("UserIsDisabled");
                	User.UserIsOnline = extras.getBoolean("UserIsOnline");
                	User.UserName = extras.getString("UserName");
                	User.UserFullName = extras.getString("UserFullName");
                	User.UserContactInfo = extras.getString("UserContactInfo");
                	//.
                	Intent intent = new Intent(TReflector.this, TUserPanel.class);
                	intent.putExtra("UserID",User.UserID);
                	startActivity(intent);
            	}
			}
			break; // . >
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void WorkSpace_Buttons_Recreate(boolean flReinitializeWorkSpace) {
		TWorkSpace.TButtons.TButton[] Buttons = new TWorkSpace.TButtons.TButton[BUTTONS_COUNT];
		float ButtonWidth = 36.0F * metrics.density;
		float ButtonHeight = 64.0F * metrics.density;
		float X = 1.0F * metrics.density; 
		float Y = 0.0F * metrics.density;
		Buttons[BUTTON_UPDATE] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "!", Color.YELLOW); 
		Y += ButtonHeight;
		Buttons[BUTTON_SHOWREFLECTIONPARAMETERS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "=", Color.YELLOW);
		Y += ButtonHeight;
		Buttons[BUTTON_OBJECTS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "O", Color.GREEN);
		Y += ButtonHeight;
		Buttons[BUTTON_ELECTEDPLACES] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "*", Color.GREEN);
		Y += ButtonHeight;
		Buttons[BUTTON_MAPOBJECTSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "?", Color.GREEN);
		Y += ButtonHeight;
		Buttons[BUTTON_PREVWINDOW] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "<<", Color.GREEN);
		Y += ButtonHeight;
		Buttons[BUTTON_EDITOR] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "+", Color.RED);
		Buttons[BUTTON_EDITOR].flEnabled = false;
		Y += ButtonHeight;
		Buttons[BUTTON_USERSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "U", Color.WHITE);
		Y += ButtonHeight;
		if (!Configuration.GeoLog_flHide) {
			final int ActiveColor = Color.CYAN; 
			final int PassiveColor = Color.BLACK;
			Buttons[BUTTON_TRACKER] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X,Y, ButtonWidth,ButtonHeight, "@", ActiveColor);
			Buttons[BUTTON_TRACKER].SetStateColorProvider(new TWorkSpace.TButtons.TButton.TStateColorProvider() {
				@Override
				public int GetStateColor() {
					if (TTracker.TrackerIsEnabled())
						return ActiveColor; //. ->
					else
						return PassiveColor; //. ->
				};
			});  
			Y += ButtonHeight;
		}
		Buttons[BUTTON_COMPASS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_RIGHT, TButton.STYLE_ELLIPSE, WorkSpace.Width-RotatingZoneWidth+2.0F*metrics.density,2.0F*metrics.density, RotatingZoneWidth-4.0F*metrics.density, RotatingZoneWidth-4.0F*metrics.density, "N", Color.BLUE);
		WorkSpace.Buttons.SetButtons(Buttons);
		//.
		if (flReinitializeWorkSpace) 
			WorkSpace.Reinitialize(this);
	}
	
	public void WorkSpace_Buttons_Update(TRWLevelTileContainer[] LevelTileContainers) {
		boolean flUserDrawable = false;
		if (LevelTileContainers != null)
			for (int I = 0; I < LevelTileContainers.length; I++)
				if ((LevelTileContainers[I] != null) && (LevelTileContainers[I].TileLevel.IsUserDrawable())) {
					flUserDrawable = true;
					break; //. >
				}
		if (WorkSpace != null)
			WorkSpace.Buttons.Items[BUTTON_EDITOR].flEnabled = flUserDrawable;
	}
		
	public synchronized boolean IsUpdatingSpaceImage() {
		return (_SpaceImageUpdating != null);
	}

	public synchronized void StartUpdatingSpaceImage(int Delay,
			boolean flUpdateProxySpace) {
		CancelUpdatingSpaceImage();
		// .
		try {
			_SpaceImageUpdating = new TSpaceImageUpdating(this, Delay,
					flUpdateProxySpace);
		} catch (Exception E) {
		}
		if (WorkSpace != null)
			WorkSpace.invalidate();
	}

	public void StartUpdatingSpaceImage(int Delay) {
		StartUpdatingSpaceImage(Delay, false);
	}

	public void StartUpdatingSpaceImage(boolean flUpdateProxySpace) {
		StartUpdatingSpaceImage(0, flUpdateProxySpace);
	}

	public void StartUpdatingSpaceImage() {
		StartUpdatingSpaceImage(0, false);
	}
	
	public void PostStartUpdatingSpaceImage() {
		MessageHandler.obtainMessage(MESSAGE_STARTUPDATESPACEIMAGE).sendToTarget();
	}

	public void StartUpdatingCurrentSpaceImage() {
		try {
			SpaceImage.GrayScale();
			if (WorkSpace != null)
				WorkSpace.invalidate();
			// .
			StartUpdatingSpaceImage();
		} catch (Throwable E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, getString(R.string.SErrorOfUpdatingImage) + S,
					Toast.LENGTH_LONG).show();
		}
	}

	public synchronized void CancelUpdatingSpaceImage() {
		if (_SpaceImageUpdating != null) {
			_SpaceImageUpdating.Cancel();
			_SpaceImageUpdating = null;
		}
	}

	public synchronized void CancelAndWaitUpdatingSpaceImage() {
		if (_SpaceImageUpdating != null) {
			_SpaceImageUpdating.CancelAndWait();
			_SpaceImageUpdating = null;
		}
	}

	public synchronized TSpaceImageUpdating GetSpaceImageUpdating() {
		return _SpaceImageUpdating;
	}

	public void RecalculateAndUpdateCurrentSpaceImage() {
		RecalculateSpaceImage();
		//.
		if (WorkSpace != null)
			WorkSpace.Update();
	}

	public void RecalculateSpaceImage() {
		SelectedObj_PrepareScreenNodes();
		ObjectTracks.RecalculateScreenNodes();
		CoGeoMonitorObjects.RecalculateVisualizationScreenLocation();
	}

	public void SetReflectionWindow(TReflectionWindowStruc RWS, boolean flUpdate) {
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		//.
		ReflectionWindow.Assign(RWS);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}
	
	public void SetReflectionWindow(TReflectionWindowStruc RWS) {
		SetReflectionWindow(RWS,true);
	}
	
	public void MoveReflectionWindow(TXYCoord Position, boolean flUpdate) {
		int RW_Xmd, RW_Ymd;
		TXYCoord Pmd;
		synchronized (ReflectionWindow) {
			RW_Xmd = ReflectionWindow.Xmd;
			RW_Ymd = ReflectionWindow.Ymd;
			Pmd = ReflectionWindow.ConvertToScreen(Position.X, Position.Y);
		}
		double dX = (RW_Xmd - Pmd.X);
		double dY = (RW_Ymd - Pmd.Y);
		//.
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate((float) dX,
					(float) dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate((float) dX,
						(float) dY);
		}
		//.
		ReflectionWindow.SetReflection(Position.X, Position.Y);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void MoveReflectionWindow(TXYCoord Position) {
		MoveReflectionWindow(Position,true);
	}
	
	public void RotateReflectionWindow(double Angle, boolean flUpdate) {
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postRotate(
					(float) (-Angle * 180.0 / Math.PI),
					ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postRotate(
						(float) (-Angle * 180.0 / Math.PI),
						ReflectionWindow.Xmd, ReflectionWindow.Ymd);
		}
		//.
		ReflectionWindow.RotateReflection(Angle);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void RotateReflectionWindow(double Angle) {
		RotateReflectionWindow(Angle,true);
	}
	
	public void TransformReflectionWindow(TReflectionWindowStruc RW, boolean flUpdate) {
		int RW_Xmd, RW_Ymd;
		TXYCoord Pmd;
		double ScaleFactor;
		double Alpha;
		synchronized (ReflectionWindow) {
			ReflectionWindow.ActualityInterval.Set(RW.BeginTimestamp,RW.EndTimestamp);
			//.
			RW_Xmd = ReflectionWindow.Xmd;
			RW_Ymd = ReflectionWindow.Ymd;
			Pmd = ReflectionWindow.ConvertToScreen((RW.X0 + RW.X2) / 2.0,
					(RW.Y0 + RW.Y2) / 2.0);
			ScaleFactor = RW.Scale() / ReflectionWindow.Scale();
			if ((ReflectionWindow.X1 - ReflectionWindow.X0) != 0) {
				Alpha = Math.atan((ReflectionWindow.Y1 - ReflectionWindow.Y0)
						/ (ReflectionWindow.X1 - ReflectionWindow.X0));
				if (((ReflectionWindow.X1 - ReflectionWindow.X0) < 0)
						&& ((ReflectionWindow.Y1 - ReflectionWindow.Y0) > 0))
					Alpha = Alpha + Math.PI;
				else if (((ReflectionWindow.X1 - ReflectionWindow.X0) < 0)
						&& ((ReflectionWindow.Y1 - ReflectionWindow.Y0) < 0))
					Alpha = Alpha + Math.PI;
				else if (((ReflectionWindow.X1 - ReflectionWindow.X0) > 0)
						&& ((ReflectionWindow.Y1 - ReflectionWindow.Y0) < 0))
					Alpha = Alpha + 2 * Math.PI;
			} else {
				if ((ReflectionWindow.Y1 - ReflectionWindow.Y0) >= 0)
					Alpha = Math.PI / 2;
				else
					Alpha = -Math.PI / 2;
			}
		}
		double dX = (RW_Xmd - Pmd.X);
		double dY = (RW_Ymd - Pmd.Y);
		double Betta, Gamma;
		if ((RW.X1 - RW.X0) != 0) {
			Betta = Math.atan((RW.Y1 - RW.Y0) / (RW.X1 - RW.X0));
			if (((RW.X1 - RW.X0) < 0) && ((RW.Y1 - RW.Y0) > 0))
				Betta = Betta + Math.PI;
			else if (((RW.X1 - RW.X0) < 0) && ((RW.Y1 - RW.Y0) < 0))
				Betta = Betta + Math.PI;
			else if (((RW.X1 - RW.X0) > 0) && ((RW.Y1 - RW.Y0) < 0))
				Betta = Betta + 2 * Math.PI;
		} else {
			if ((RW.Y1 - RW.Y0) >= 0)
				Betta = Math.PI / 2;
			else
				Betta = -Math.PI / 2;
		}
		;
		Gamma = (Betta - Alpha);
		//.
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate((float) dX,
					(float) dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate((float) dX,
						(float) dY);
			SpaceImage.ResultBitmapTransformatrix.postScale(
					(float) ScaleFactor, (float) ScaleFactor,
					ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postScale(
						(float) ScaleFactor, (float) ScaleFactor,
						ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			SpaceImage.ResultBitmapTransformatrix.postRotate(
					(float) (Gamma * 180.0 / Math.PI), ReflectionWindow.Xmd,
					ReflectionWindow.Ymd);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postRotate(
						(float) (Gamma * 180.0 / Math.PI),
						ReflectionWindow.Xmd, ReflectionWindow.Ymd);
		}
		ReflectionWindowTransformatrix.postRotate(
				-(float) (Gamma * 180.0 / Math.PI), ReflectionWindow.Xmd,
				ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postScale((float) (1.0 / ScaleFactor),
				(float) (1.0 / ScaleFactor), ReflectionWindow.Xmd,
				ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postTranslate(-(float) dX, -(float) dY);
		//.
		ReflectionWindow
				.MultiplyReflectionByMatrix(ReflectionWindowTransformatrix);
		//.
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void TransformReflectionWindow(TReflectionWindowStruc RW) {
		TransformReflectionWindow(RW,true);
	}
	
	public void TranslateReflectionWindow(float dX, float dY, boolean flUpdate) {
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate(dX, dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate(dX, dY);
		}
		//.
		ReflectionWindow.PixShiftReflection(dX, dY);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}
	
	public void TranslateReflectionWindow(float dX, float dY) {
		TranslateReflectionWindow(dX,dY,true);
	}
	
	public void SetReflectionWindowByLocation(TLocation Location) {
		if (ReflectionWindow.IsValid())
			TransformReflectionWindow(Location.RW);
		else
			SetReflectionWindow(Location.RW);
	}	

	public double ReflectionWindowToTheNorthPoleAlignAngle() throws Exception {
		TGeoCoord GCRD,GCRD1;
		double Lat,Long, Lat1,Long1;
		TXYCoord Crd;
		double X0,Y0, X1,Y1;
		double Geo_X0,Geo_Y0, Geo_X1,Geo_Y1;
		double ALFA,BETTA,GAMMA;
		//.
		synchronized (ReflectionWindow) {
			Geo_X0 = ReflectionWindow.Xcenter;
			Geo_Y0 = ReflectionWindow.Ycenter;
			X1 = (ReflectionWindow.X1+ReflectionWindow.X2)/2.0;
			Y1 = (ReflectionWindow.Y1+ReflectionWindow.Y2)/2.0;
		}
		//.
		GCRD = ConvertXYCoordinatesToGeo(Geo_X0,Geo_Y0);
		Crd = ConvertGeoCoordinatesToXY(GCRD.Latitude,GCRD.Longitude+(1.0/*Grad*/));
		//.
		Geo_X1 = Crd.X;
		Geo_Y1 = Crd.Y;
		X0 = Geo_X0;
		Y0 = Geo_Y0;
		if ((Geo_X1-Geo_X0) != 0)
			  ALFA = Math.atan((Geo_Y1-Geo_Y0)/(Geo_X1-Geo_X0));
		 else
		  if ((Geo_Y1-Geo_Y0) >= 0)
			  ALFA = Math.PI/2;
		  else 
			  ALFA = -Math.PI/2;
		if ((X1-X0) != 0)
			  BETTA = Math.atan((Y1-Y0)/(X1-X0));
		 else
			  if ((Y1-Y0) >= 0)
				  BETTA = Math.PI/2;
			  else 
				  BETTA = -Math.PI/2;
		GAMMA = (ALFA-BETTA);
		if ((Geo_X1-Geo_X0)*(X1-X0) < 0)
			  if ((Geo_Y1-Geo_Y0)*(Y1-Y0) >= 0)
				  GAMMA = GAMMA-Math.PI;
			  else 
				  GAMMA = GAMMA+Math.PI;
		GAMMA = -GAMMA;
		if (GAMMA < -Math.PI)
			  GAMMA = GAMMA+2*Math.PI;
		 else
			  if (GAMMA > Math.PI)
			    GAMMA = GAMMA-2*Math.PI;
		return GAMMA;
	}
	
    private class TReflectionWindowToNorthPoleAlignning extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_ROTATION 				= 1;
    	private static final int MESSAGE_ROTATIONISDONE 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 5;
    	
        private ProgressDialog progressDialog; 
    	
    	public TReflectionWindowToNorthPoleAlignning() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				double Angle;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				Angle = ReflectionWindowToTheNorthPoleAlignAngle();
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
				while (Math.abs(Angle) > Math.PI/32) {
	    			MessageHandler.obtainMessage(MESSAGE_ROTATION,((Double)Math.PI/32.0*(Angle/Math.abs(Angle)))).sendToTarget();
	    			Thread.sleep(20);
					//.
					Angle = Angle-Math.PI/32.0*(Angle/Math.abs(Angle));
				};
    			MessageHandler.obtainMessage(MESSAGE_ROTATIONISDONE,Angle).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
			catch (CancelException CE) {
			}
        	catch (NullPointerException NPE) { 
        		if (!TReflector.this.isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_ROTATION:
	            	double Angle = (Double)msg.obj;
	            	RotateReflectionWindow(Angle,false);
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ROTATIONISDONE:
	            	Angle = (Double)msg.obj;
	            	RotateReflectionWindow(Angle);
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflector.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflector.this);    
	            	progressDialog.setMessage(TReflector.this.getString(R.string.SAligningToTheNorthPole));    
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
	            	if (flRunning)
	            		progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }

	public void ShowPrevWindow() {
		TReflectionWindowStruc CurrentRWS = null;
		if (!IsUpdatingSpaceImage())
			CurrentRWS = LastWindows.Pop(); // . skip current window
		TReflectionWindowStruc RWS = LastWindows.Pop();
		if (RWS != null)
			TransformReflectionWindow(RWS);
		else if (CurrentRWS != null)
			LastWindows.Push(CurrentRWS);
	}

	public void ClearReflections(boolean flUpdateImage) throws IOException {
		if (SpaceReflections != null)
			SpaceReflections.Clear();
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ClearHints(boolean flUpdateImage) throws IOException {
		if (SpaceHints != null)
			SpaceHints.ClearItems();
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ClearTileImagery(boolean flUpdateImage) throws IOException {
		if (SpaceTileImagery != null)
			SpaceTileImagery.ActiveCompilationSet_DeleteAll();
		//.
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ClearReflectionsAndHints(boolean flUpdateImage) throws IOException {
		if (SpaceReflections != null)
			SpaceReflections.Clear();
		if (SpaceHints != null)
			SpaceHints.ClearItems();
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ClearVisualizations(boolean flUpdateImage) throws IOException {
		if (SpaceReflections != null)
			SpaceReflections.Clear();
		if (SpaceTileImagery != null)
			SpaceTileImagery.ActiveCompilationSet_DeleteAll();
		if (SpaceHints != null)
			SpaceHints.ClearItems();
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ResetVisualizations(boolean flUpdateImage) throws IOException {
		if (SpaceReflections != null)
			SpaceReflections.Clear();
		if (SpaceTileImagery != null)
			SpaceTileImagery.ActiveCompilationSet_ResetHistoryTiles();
		if (SpaceHints != null)
			SpaceHints.ClearItems();
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	private void SelectedObj_PrepareScreenNodes() {
		if (SelectedObj == null)
			return; // . ->
		if (SelectedObj.Nodes != null) {
			TXYCoord[] ScrNodes = ReflectionWindow
					.ConvertNodesToScreen(SelectedObj.Nodes);
			float[] pts = new float[ScrNodes.length << 2];
			int Idx = 0;
			for (int I = 0; I < (ScrNodes.length - 1); I++) {
				pts[Idx] = (float) ScrNodes[I].X;
				Idx++;
				pts[Idx] = (float) ScrNodes[I].Y;
				Idx++;
				pts[Idx] = (float) ScrNodes[I + 1].X;
				Idx++;
				pts[Idx] = (float) ScrNodes[I + 1].Y;
				Idx++;
			}
			pts[Idx] = (float) ScrNodes[(ScrNodes.length - 1)].X;
			Idx++;
			pts[Idx] = (float) ScrNodes[(ScrNodes.length - 1)].Y;
			Idx++;
			pts[Idx] = (float) ScrNodes[0].X;
			Idx++;
			pts[Idx] = (float) ScrNodes[0].Y;
			Idx++;
			SelectedObj.ScreenNodes = pts;
		} else
			SelectedObj.ScreenNodes = null;
	}

	private void SelectedObj_Clear() {
		SelectedObj = null;
	}

	private void SelectedObj_CancelProcessing() {
		if (SelectedComponentTypedDataFileNamesLoading != null)
			SelectedComponentTypedDataFileNamesLoading.Cancel();
		if (SelectedComponentTypedDataFileLoading != null)
			SelectedComponentTypedDataFileLoading.Cancel();
	}

	public byte[] GetVisualizationOwnerDataDocument(int ptrObj, int Format,
			int DataType, boolean flWithComponents) throws Exception,
			IOException {
		String URL1 = Server.Address;
		// . add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
				+ "/" + Integer.toString(User.UserID);
		String URL2 = "Functionality" + "/"
				+ "VisualizationOwnerDataDocument.dat";
		// . add command parameters
		int WithComponentsFlag;
		if (flWithComponents)
			WithComponentsFlag = 1;
		else
			WithComponentsFlag = 0;
		URL2 = URL2 + "?" + "1"/* command version */+ ","
				+ Integer.toString(ptrObj) + "," + Integer.toString(Format)
				+ "," + Integer.toString(DataType) + ","
				+ Integer.toString(WithComponentsFlag);
		// .
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		// . encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		// .
		String URL = URL1 + "/" + URL2 + ".dat";
		// .
		// .
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; // . ->
				byte[] Data = new byte[RetSize];
				int Size;
				int SummarySize = 0;
				int ReadSize;
				while (SummarySize < Data.length) {
					ReadSize = Data.length - SummarySize;
					Size = in.read(Data, SummarySize, ReadSize);
					if (Size <= 0)
						throw new Exception(
								getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																						// =>
					SummarySize += Size;
				}
				// .
				return Data; // . ->
			} finally {
				in.close();
			}
		} finally {
			Connection.disconnect();
		}
	}

	public byte[] GetComponentDataDocument(int idTComponent, int idComponent,
			int Format, int DataType, boolean flWithComponents)
			throws Exception, IOException {
		String URL1 = Server.Address;
		// . add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
				+ "/" + Integer.toString(User.UserID);
		String URL2 = "Functionality" + "/" + "ComponentDataDocument.dat";
		// . add command parameters
		int WithComponentsFlag;
		if (flWithComponents)
			WithComponentsFlag = 1;
		else
			WithComponentsFlag = 0;
		URL2 = URL2 + "?" + "1"/* command version */+ ","
				+ Integer.toString(idTComponent) + ","
				+ Integer.toString(idComponent) + ","
				+ Integer.toString(Format) + "," + Integer.toString(DataType)
				+ "," + Integer.toString(WithComponentsFlag);
		// .
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		// . encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		// .
		String URL = URL1 + "/" + URL2 + ".dat";
		// .
		// .
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; // . ->
				byte[] Data = new byte[RetSize];
				int Size;
				int SummarySize = 0;
				int ReadSize;
				while (SummarySize < Data.length) {
					ReadSize = Data.length - SummarySize;
					Size = in.read(Data, SummarySize, ReadSize);
					if (Size <= 0)
						throw new Exception(
								getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																						// =>
					SummarySize += Size;
				}
				// .
				return Data; // . ->
			} finally {
				in.close();
			}
		} finally {
			Connection.disconnect();
		}
	}

	public byte[] GetCoGeoMonitorObjectData(int idCoComponent, int DataType)
			throws Exception, IOException {
		String URL1 = Server.Address;
		// . add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
				+ "/" + Integer.toString(User.UserID);
		String URL2 = "TypesSystem" + "/"
				+ Integer.toString(SpaceDefines.idTCoComponent) + "/"
				+ "TypedCo" + "/"
				+ Integer.toString(SpaceDefines.idTCoGeoMonitorObject) + "/"
				+ Integer.toString(idCoComponent) + "/" + "Data.dat";
		// . add command parameters
		URL2 = URL2 + "?" + "1"/* command version */+ ","
				+ Integer.toString(DataType);
		// .
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		// . encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		// .
		String URL = URL1 + "/" + URL2 + ".dat";
		// .
		// .
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; // . ->
				byte[] Data = new byte[RetSize];
				int Size;
				int SummarySize = 0;
				int ReadSize;
				while (SummarySize < Data.length) {
					ReadSize = Data.length - SummarySize;
					Size = in.read(Data, SummarySize, ReadSize);
					if (Size <= 0)
						throw new Exception(
								getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																						// =>
					SummarySize += Size;
				}
				// .
				return Data; // . ->
			} finally {
				in.close();
			}
		} finally {
			Connection.disconnect();
		}
	}

	public AlertDialog ComponentTypedDataFiles_CreateSelectorPanel(
			TComponentTypedDataFiles pComponentTypedDataFiles,
			Activity ParentActivity) {
		final TComponentTypedDataFiles ComponentTypedDataFiles = pComponentTypedDataFiles;
		final CharSequence[] _items = new CharSequence[ComponentTypedDataFiles.Items.length];
		for (int I = 0; I < ComponentTypedDataFiles.Items.length; I++)
			_items[I] = ComponentTypedDataFiles.Items[I].DataName
					+ "("
					+ SpaceDefines
							.TYPEDDATAFILE_TYPE_String(ComponentTypedDataFiles.Items[I].DataType)
					+ ")";
		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		builder.setTitle(R.string.SFiles);
		builder.setNegativeButton(getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {

					private TComponentTypedDataFiles _ComponentTypedDataFiles = ComponentTypedDataFiles;

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						TComponentTypedDataFile ComponentTypedDataFile = _ComponentTypedDataFiles.Items[arg1];
						if (ComponentTypedDataFile.IsLoaded()) {
							ComponentTypedDataFile_Open(ComponentTypedDataFile);
						} else {
							if (SelectedComponentTypedDataFileLoading != null)
								SelectedComponentTypedDataFileLoading.Cancel();
							SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(
									TReflector.this, ComponentTypedDataFile,
									MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
						}
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

	public void ComponentTypedDataFile_Open(
			TComponentTypedDataFile ComponentTypedDataFile) {
		Intent intent = null;
		switch (ComponentTypedDataFile.DataType) {

		case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
			try {
				File F = ComponentTypedDataFile.GetFile();
				byte[] Data = new byte[(int)F.length()];
				FileInputStream FIS = new FileInputStream(F);
				try {
					FIS.read(Data);
				}
				finally {
					FIS.close();
				}				
				String Text = new String(Data,"windows-1251");
				byte[] TextData = Text.getBytes("utf-16");
				// .
				File TempFile = ComponentTypedDataFile.GetTempFile();
				FileOutputStream fos = new FileOutputStream(TempFile);
				try {
					fos.write(TextData, 0, TextData.length);
				} finally {
					fos.close();
				}
				// . open appropriate extent
				intent = new Intent();
				intent.setDataAndType(Uri.fromFile(TempFile), "text/plain");
			} catch (Exception E) {
				Toast.makeText(
						TReflector.this,
						getString(R.string.SErrorOfPreparingDataFile)
								+ ComponentTypedDataFile.FileName(),
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			break; // . >

		case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
			try {
				// . open appropriate extent
				intent = new Intent();
				intent.setDataAndType(
						Uri.fromFile(ComponentTypedDataFile.GetFile()),
						"image/*");
			} catch (Exception E) {
				Toast.makeText(
						TReflector.this,
						getString(R.string.SErrorOfPreparingDataFile)
								+ ComponentTypedDataFile.FileName(),
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			break; // . >

		case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
			try {
				// . open appropriate extent
				intent = new Intent();
				intent.setDataAndType(
						Uri.fromFile(ComponentTypedDataFile.GetFile()),
						"audio/*");
			} catch (Exception E) {
				Toast.makeText(
						TReflector.this,
						getString(R.string.SErrorOfPreparingDataFile)
								+ ComponentTypedDataFile.FileName(),
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			break; // . >

		case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
			try {
				// . open appropriate extent
				intent = new Intent();
				intent.setDataAndType(
						Uri.fromFile(ComponentTypedDataFile.GetFile()),
						"video/*");
			} catch (Exception E) {
				Toast.makeText(
						TReflector.this,
						getString(R.string.SErrorOfPreparingDataFile)
								+ ComponentTypedDataFile.FileName(),
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			break; // . >

		default:
			Toast.makeText(TReflector.this, R.string.SUnknownDataFileFormat,
					Toast.LENGTH_SHORT).show();
			return; // . ->
		}
		intent.setAction(android.content.Intent.ACTION_VIEW);
		startActivityForResult(intent, REQUEST_OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE);
	}

	public void ShowEditor() {
		Intent intent = new Intent(this, TReflectionWindowEditorPanel.class);
		intent.putExtra("AskForLastDrawingFile", true);
		startActivity(intent);
	}

	private void DoOnPointerClick(double X, double Y) {
		try {
			SelectedObj_CancelProcessing();
			// .
			SelectedObj_Clear();
			SpaceHints.UnSelectAll();
			CoGeoMonitorObjects.UnSelectAll();
			// .
			boolean flSelected = false;
			// .
			int idxCoGeoMonitorObject = CoGeoMonitorObjects.Select((float) X,
					(float) Y);
			if (idxCoGeoMonitorObject != -1) {
				flSelected = true;
				WorkSpace.invalidate();
				// .
				Intent intent = new Intent(this,
						TReflectorCoGeoMonitorObjectPanel.class);
				intent.putExtra("Index", idxCoGeoMonitorObject);
				startActivity(intent);
			}
			//.
			if (!flSelected) {
				TSpaceHint Hint = SpaceHints.Select(ReflectionWindow.GetWindow(), VisibleFactor, (float) X, (float) Y);
				if (Hint != null) {
					flSelected = true;
					WorkSpace.invalidate();
					// .
					if (SelectedComponentTypedDataFileNamesLoading != null)
						SelectedComponentTypedDataFileNamesLoading.Cancel();
					if (Hint.InfoComponent_ID != 0)
						SelectedComponentTypedDataFileNamesLoading = new TComponentTypedDataFileNamesLoading(
								this, Hint.InfoComponent_Type,
								Hint.InfoComponent_ID,
								MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED);
					else
						Toast.makeText(this, Hint.InfoString, Toast.LENGTH_LONG).show();
				}
			}
			// .
			if (ObjectAtPositionGetting != null) {
				ObjectAtPositionGetting.Cancel();
				ObjectAtPositionGetting = null;
			}
			if (!flSelected)
				ObjectAtPositionGetting = ReflectionWindow.new TObjectAtPositionGetting(
						ReflectionWindow, X, Y, true, MESSAGE_SELECTEDOBJ_SET);
		} catch (Exception E) {
			Toast.makeText(
					this,
					getString(R.string.SErrorOfGettingObjectByCurrentPosition)
							+ E.getMessage(), Toast.LENGTH_SHORT).show();
			return; // . ->
		}
	}

	private void Pointer0_Down(double X, double Y) {
		if (!flEnabled)
			return; // . ->
		int idxButton = WorkSpace.Buttons.GetItemAt(X, Y);
		if (idxButton != -1) 
			WorkSpace.Buttons.SetDownButton(idxButton);
		//.
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		//.
		switch (NavigationMode) {
		
		case NAVIGATION_MODE_NATIVE:
			if (X < (WorkSpace.Width - (RotatingZoneWidth + ScalingZoneWidth)))
				NavigationType = NAVIGATION_TYPE_MOVING;
			else if (X < (WorkSpace.Width - RotatingZoneWidth))
				NavigationType = NAVIGATION_TYPE_SCALING;
			else if (X >= (WorkSpace.Width - RotatingZoneWidth))
				NavigationType = NAVIGATION_TYPE_ROTATING;
			else
				NavigationType = NAVIGATION_TYPE_NONE;
			break; //. >
			
		case NAVIGATION_MODE_ARROWS:
			TWorkSpace.TNavigationArrows.TArrow Arrow = WorkSpace.NavigationArrows.DoOnPointerDown(X,Y);
			if (Arrow == null)
				NavigationType = NAVIGATION_TYPE_MOVING;
			break; //. >

		case NAVIGATION_MODE_MULTITOUCHING:
			NavigationType = NAVIGATION_TYPE_MOVING;
			break; //. >
			
		case NAVIGATION_MODE_MULTITOUCHING1:
			if (X >= (WorkSpace.Width - RotatingZoneWidth))
				NavigationType = NAVIGATION_TYPE_ROTATING;
			else
				NavigationType = NAVIGATION_TYPE_MOVING;
			break; //. >
		}
		//.
		Pointer_Down_StartPos.X = X;
		Pointer_Down_StartPos.Y = Y;
		Pointer_LastPos.X = X;
		Pointer_LastPos.Y = Y;
		// .
		SelectedObj_CancelProcessing();
		// .
		if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
			_MediaPlayer.stop();
	}

	private void Pointer0_Up(double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		int idxDownButton = WorkSpace.Buttons.DownButtonIndex;
		if ((idxDownButton != -1)) {
			boolean flPlayGeoLog = ((Calendar.getInstance().getTime().getTime() - WorkSpace.Buttons.DownButtons_Time) > 7000);
			WorkSpace.Buttons.ClearDownButton();
			// .
			int idxButton = WorkSpace.Buttons.GetItemAt(X, Y);
			if ((idxButton == idxDownButton) && (WorkSpace.Buttons.Items[idxButton].flEnabled)) {
				switch (idxButton) {
				case BUTTON_UPDATE:
					StartUpdatingCurrentSpaceImage();
					if (flPlayGeoLog) {
						File GF = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/"
								+ "Geo.Log"
								+ "/"
								+ "Lib"
								+ "/"
								+ "GeoLog.dat");
						if (GF.exists()) {
							Intent intent = new Intent();
							intent.setDataAndType(Uri.fromFile(GF), "audio/*");
							intent.setAction(android.content.Intent.ACTION_VIEW);
							startActivityForResult(intent, 0);
						}
					}
					break; // . >

				case BUTTON_SHOWREFLECTIONPARAMETERS:
					Intent intent = ReflectionWindow
							.CreateConfigurationPanel(this);
					startActivity(intent);
					break; // . >

				/*
				 * ///? case BUTTON_SUPERLAYS:
				 * ReflectionWindow.getLays().SuperLays
				 * .CreateSelectorPanel(this).show(); break; //. >
				 */

				case BUTTON_OBJECTS:
					intent = new Intent(this,
							TReflectorCoGeoMonitorObjectsPanel.class);
					startActivity(intent);
					break; // . >

				case BUTTON_USERSEARCH:
					intent = new Intent(this, TUserListPanel.class);
			    	intent.putExtra("Mode",TUserListPanel.MODE_UNKNOWN);    	
					startActivityForResult(intent, REQUEST_OPEN_USERSEARCH);
					// .
					break; // . >

				case BUTTON_TRACKER:
					intent = new Intent(this, TTrackerPanel.class);
					startActivityForResult(intent, REQUEST_SHOW_TRACKER);
					// .
					break; // . >

				case BUTTON_MAPOBJECTSEARCH:
					intent = new Intent(this, TMapObjectsPanel.class);
					startActivity(intent);
					// .
					break; // . >

				case BUTTON_ELECTEDPLACES:
					intent = new Intent(this,
							TReflectorElectedPlacesPanel.class);
					startActivity(intent);
					// .
					break; // . >

				case BUTTON_PREVWINDOW:
					ShowPrevWindow();
					// .
					break; // . >

				case BUTTON_EDITOR:
					if (GetViewMode() == VIEWMODE_TILES)
						ShowEditor();
					else
						Toast.makeText(this,
								R.string.SImageEditingAvailableInTileModeOnly,
								Toast.LENGTH_LONG).show();
					// .
					break; // . >
					
				case BUTTON_COMPASS:
					new TReflectionWindowToNorthPoleAlignning();
					//.
					break; // . >
				}
			}
			return; // . ->
		}
		//.
		try {
			NavigationTransformatrix.reset();
			//.
			if ((WorkSpace.NavigationArrows != null) && (WorkSpace.NavigationArrows.DownArrow != null)) 
				WorkSpace.NavigationArrows.Release();
			else {
				double dX = X - Pointer_Down_StartPos.X;
				double dY = Y - Pointer_Down_StartPos.Y;
				// .
				if ((Math.abs(dX) < SelectShiftFactor) && (Math.abs(dY) < SelectShiftFactor)) {
					NavigationType = NAVIGATION_TYPE_NONE;
					DoOnPointerClick(Pointer_Down_StartPos.X,Pointer_Down_StartPos.Y);
					return; // . ->
				};
			}
			//.
			if (NavigationType != NAVIGATION_TYPE_NONE) {
				NavigationType = NAVIGATION_TYPE_NONE;
				// .
				ReflectionWindow.MultiplyReflectionByMatrix(ReflectionWindowTransformatrix);
				ReflectionWindowTransformatrix.reset();
				//.
				///? SpaceImage.ResetResultBitmap();
				//.
				switch (NavigationMode) {
				
				case NAVIGATION_MODE_NATIVE:
				case NAVIGATION_MODE_MULTITOUCHING:
				case NAVIGATION_MODE_MULTITOUCHING1:
					_SpaceImageCaching.Stop();
					break; //. >
				}
				//.
				RecalculateSpaceImage();
			}
		} finally {
			StartUpdatingSpaceImage(1000);
		}
	}

	private void Pointer0_Move(ImageView view, double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		if ((WorkSpace.NavigationArrows != null) && (WorkSpace.NavigationArrows.DownArrow != null)) {
			if (WorkSpace.NavigationArrows.DownArrow != WorkSpace.NavigationArrows.GetItemAt(X,Y)) {
				WorkSpace.NavigationArrows.Release();
				//.
				WorkSpace.NavigationArrows.DoOnPointerDown(X,Y);
			}
			return; //. ->
		}
		//.
		if (WorkSpace.Buttons.DownButtonIndex != -1) {
			int idxButton = WorkSpace.Buttons.GetItemAt(X, Y);
			if (idxButton != WorkSpace.Buttons.DownButtonIndex) 
				WorkSpace.Buttons.ClearDownButton();
			return; // . ->
		}
		// .
		/*
		 * ///? if ((NavigationType == TNavigationItem.NAVIGATIONTYPE_NONE) &&
		 * ((Math.abs(X-Pointer_Down_StartPos.X) < SelectShiftFactor) &&
		 * (Math.abs(Y-Pointer_Down_StartPos.Y) < SelectShiftFactor))) return ;
		 * //. ->
		 */
		// .
		CancelUpdatingSpaceImage();
		// .
		double dX = X - Pointer_LastPos.X;
		double dY = Y - Pointer_LastPos.Y;
		// .
		switch (NavigationType) {
		
		case NAVIGATION_TYPE_MOVING: {
			NavigationTransformatrix.postTranslate((float) dX, (float) dY);
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postTranslate((float) dX,
						(float) dY);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postTranslate((float) dX,
							(float) dY);
			}
			ReflectionWindowTransformatrix.postTranslate(-(float) dX,
					-(float) dY);
			break; // . >
		}

		case NAVIGATION_TYPE_SCALING: {
			double Scale = (1.0 + ScaleCoef * dY / ReflectionWindow.getHeight());
			NavigationTransformatrix.postScale((float) Scale, (float) Scale,
					ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale,
						(float) Scale, ReflectionWindow.Xmd,
						ReflectionWindow.Ymd);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postScale((float) Scale,
							(float) Scale, ReflectionWindow.Xmd,
							ReflectionWindow.Ymd);
			}
			ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
					(float) (1.0 / Scale), ReflectionWindow.Xmd,
					ReflectionWindow.Ymd);
			break; // . >
		}

		case NAVIGATION_TYPE_ROTATING: {
			double dX0, dY0;
			dX0 = (Pointer_LastPos.X - ReflectionWindow.Xmd);
			dY0 = (Pointer_LastPos.Y - ReflectionWindow.Ymd);
			double dX1, dY1;
			dX1 = (X - ReflectionWindow.Xmd);
			dY1 = (Y - ReflectionWindow.Ymd);
			double Alpha = Math.atan(dY0 / dX0);
			double Betta = Math.atan(dY1 / dX1);
			double Gamma = -(Betta - Alpha);
			// .
			NavigationTransformatrix.postRotate(
					(float) (-Gamma * 180.0 / Math.PI), ReflectionWindow.Xmd,
					ReflectionWindow.Ymd);
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postRotate(
						(float) (-Gamma * 180.0 / Math.PI),
						ReflectionWindow.Xmd, ReflectionWindow.Ymd);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postRotate(
							(float) (-Gamma * 180.0 / Math.PI),
							ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			}
			ReflectionWindowTransformatrix.postRotate(
					(float) (Gamma * 180.0 / Math.PI), ReflectionWindow.Xmd,
					ReflectionWindow.Ymd);
			break; // . >
		}

		case NAVIGATION_TYPE_SCALETRANSFORMATING: {
			dX = (Pointer_LastPos.X-Pointer1_LastPos.X);
			dY = (Pointer_LastPos.Y-Pointer1_LastPos.Y);
			double L0Qd = dX*dX+dY*dY;  
			dX = (X-Pointer1_LastPos.X);
			dY = (Y-Pointer1_LastPos.Y);
			double LQd = dX*dX+dY*dY;  
			if ((L0Qd == 0) || (LQd == 0))
				break; //. >
			double Scale = (LQd/L0Qd);
			NavigationTransformatrix.postScale((float) Scale, (float) Scale,
					ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale,
						(float) Scale, ReflectionWindow.Xmd,
						ReflectionWindow.Ymd);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postScale((float) Scale,
							(float) Scale, ReflectionWindow.Xmd,
							ReflectionWindow.Ymd);
			}
			ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
					(float) (1.0 / Scale), ReflectionWindow.Xmd,
					ReflectionWindow.Ymd);
			break; //. >
		}
		}
		if (NavigationType != NAVIGATION_TYPE_NONE) {
			///-- SpaceImage.ResetResultBitmap();
			view.invalidate();
			switch (NavigationMode) {
			
			case NAVIGATION_MODE_NATIVE:
			case NAVIGATION_MODE_MULTITOUCHING:
			case NAVIGATION_MODE_MULTITOUCHING1:
				_SpaceImageCaching.TryToCacheCurrentWindow();
				break; //. >
			}
		}
		//.
		Pointer_LastPos.X = X;
		Pointer_LastPos.Y = Y;
	}

	private void Pointer1_Down(double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		switch (NavigationMode) {
		
		case NAVIGATION_MODE_MULTITOUCHING1:
			if (NavigationType != NAVIGATION_TYPE_ROTATING)
				NavigationType = NAVIGATION_TYPE_SCALETRANSFORMATING;
			break; //. >
		}
		//.
		Pointer1_Down_StartPos.X = X;
		Pointer1_Down_StartPos.Y = Y;
		Pointer1_LastPos.X = X;
		Pointer1_LastPos.Y = Y;
	}

	private void Pointer1_Up(double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		switch (NavigationMode) {
		
		case NAVIGATION_MODE_MULTITOUCHING:
			if (NavigationType != NAVIGATION_TYPE_NONE) {
				NavigationType = NAVIGATION_TYPE_NONE;
				//.
				_SpaceImageCaching.Stop();
			}
			break; //. >
			
		case NAVIGATION_MODE_MULTITOUCHING1:
			if (NavigationType != NAVIGATION_TYPE_NONE) {
				NavigationType = NAVIGATION_TYPE_NONE;
				//.
				_SpaceImageCaching.Stop();
			}
			break; //. >
		}
	}

	private void Pointer1_Move(ImageView view, double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		switch (NavigationMode) {
		
		case NAVIGATION_MODE_MULTITOUCHING1:
			switch (NavigationType) {
			
			case NAVIGATION_TYPE_SCALETRANSFORMATING: {
				double dX = (Pointer1_LastPos.X-Pointer_LastPos.X);
				double dY = (Pointer1_LastPos.Y-Pointer_LastPos.Y);
				double L0Qd = dX*dX+dY*dY;  
				dX = (X-Pointer_LastPos.X);
				dY = (Y-Pointer_LastPos.Y);
				double LQd = dX*dX+dY*dY;  
				if ((L0Qd == 0) || (LQd == 0))
					break; //. >
				double Scale = (LQd/L0Qd);
				NavigationTransformatrix.postScale((float) Scale, (float) Scale,
						ReflectionWindow.Xmd, ReflectionWindow.Ymd);
				synchronized (SpaceImage) {
					SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale,
							(float) Scale, ReflectionWindow.Xmd,
							ReflectionWindow.Ymd);
					if (SpaceImage.flSegments)
						SpaceImage.SegmentsTransformatrix.postScale((float) Scale,
								(float) Scale, ReflectionWindow.Xmd,
								ReflectionWindow.Ymd);
				}
				ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
						(float) (1.0 / Scale), ReflectionWindow.Xmd,
						ReflectionWindow.Ymd);
				break; //. >
			}
			}
			break; //. >
		}
		if (NavigationType != NAVIGATION_TYPE_NONE) {
			view.invalidate();
			_SpaceImageCaching.TryToCacheCurrentWindow();
		}
		// .
		Pointer1_LastPos.X = X;
		Pointer1_LastPos.Y = Y;
	}

	private void Pointer2_Down(double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		Pointer2_Down_StartPos.X = X;
		Pointer2_Down_StartPos.Y = Y;
		Pointer2_LastPos.X = X;
		Pointer2_LastPos.Y = Y;
	}

	private void Pointer2_Up(double X, double Y) {
		if (!flEnabled)
			return; // . ->
	}

	private void Pointer2_Move(ImageView view, double X, double Y) {
		if (!flEnabled)
			return; // . ->
		//.
		Pointer2_LastPos.X = X;
		Pointer2_LastPos.Y = Y;
	}

	public TGeoCoord ConvertXYCoordinatesToGeo(double X, double Y) throws Exception { 
		TGeoCoord C = new TGeoCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(SpaceDefines.idTGeoSpace) + "/" + "Co" + "/" + Integer.toString(Configuration.GeoSpaceID) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "4"/* command version */+ "," + Double.toString(X) + "," + Double.toString(Y);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1 + "/" + URL2 + ".dat";
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(URL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TGeoCoord();
					int Idx = 0;
					C.Latitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Longitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}

	public TXYCoord ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude) throws Exception { 
		TXYCoord C = new TXYCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(SpaceDefines.idTGeoSpace) + "/" + "Co" + "/" + Integer.toString(Configuration.GeoSpaceID) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "1"/* command version */+ "," + Integer.toString(DatumID) + "," + Double.toString(Latitude) + "," + Double.toString(Longitude);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1 + "/" + URL2 + ".dat";
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(URL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}

	public TXYCoord ConvertGeoCoordinatesToXY(double Latitude, double Longitude) throws Exception { 
		TXYCoord C = new TXYCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(SpaceDefines.idTGeoSpace) + "/" + "Co" + "/" + Integer.toString(Configuration.GeoSpaceID) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "2"/* command version */+ "," + Double.toString(Latitude) + "," + Double.toString(Longitude);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1 + "/" + URL2 + ".dat";
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(URL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}

	public void Tracker_ShowCurrentLocation() {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(
					this,
					R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable,
					Toast.LENGTH_SHORT).show();
			return; // . ->
		}
		TGPSFixValue Fix;
		TXYCoord Crd = new TXYCoord();
		try {
			Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
			if (!Fix.IsSet()) {
				Toast.makeText(this, R.string.SCurrentPositionIsUnavailable,
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			if (Fix.IsEmpty()) {
				Toast.makeText(this, R.string.SCurrentPositionIsUnknown,
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			Crd = ConvertGeoCoordinatesToXY(TTracker.DatumID, Fix.Latitude,
					Fix.Longitude);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			return; // . ->
		}
		MoveReflectionWindow(Crd);
		// .
		if (!Fix.IsAvailable())
			Toast.makeText(this, R.string.SNoCurrentPositionSetByLastCoords,
					Toast.LENGTH_LONG).show();
	}

	public void PlayAlarmSound() {
		try {
			if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
				_MediaPlayer.stop();
			_MediaPlayer = new MediaPlayer();
			// .
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			_MediaPlayer.setDataSource(this, alert);
			final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audio.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				_MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				_MediaPlayer.setLooping(true);
				_MediaPlayer.prepare();
				_MediaPlayer.start();
			}
		} catch (Exception E) {
		}
		;
	}
}