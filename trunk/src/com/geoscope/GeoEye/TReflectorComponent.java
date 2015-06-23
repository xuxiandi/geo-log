package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Number.Real.TReal48;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.Classes.MultiThreading.TUpdater;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.TReflectorComponent.TObjectCreationGalleryOverlay.TItems.TItem;
import com.geoscope.GeoEye.TReflectorComponent.TWorkSpace.TButtons.TButton;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TGeoLocation;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TOwnerSpaceObj;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStrucStack;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentDescriptor;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.BaseVisualizationFunctionality.TBase2DVisualizationFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingCommandMessage;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingCommandResponseMessage;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectPanel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectTrack;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjects;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TGeoDatum;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TGeoSpaceFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHint;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHints;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections.TSpaceReflections;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileLevel.TGetTilesResult;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;
import com.geoscope.GeoEye.Space.URLs.TURLFolderListComponent;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoEye.UserAgentService.TUserAgentService;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.TUserAccess;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.TrackerService.TTrackerService;

@SuppressLint("HandlerLeak")
public class TReflectorComponent extends TUIComponent {

	public static final String ProfileFolder() {
		return TGeoLogApplication.ProfileFolder();
	}

	public static final String URLListFolder = ProfileFolder()+"/"+"URLs";
	
	public static int NextID = 0;
	//.
	public static synchronized int GetNextID() {
		NextID++;
		return NextID;
	}
	
	private static ArrayList<TReflectorComponent> ComponentsList = new ArrayList<TReflectorComponent>();

	public static synchronized TReflectorComponent GetComponent(int ComponentID) {
		int Cnt = ComponentsList.size();
		for (int I = 0; I < Cnt; I++) {
			TReflectorComponent Component = ComponentsList.get(I);
			if (Component.ID == ComponentID)
				return Component; //. ->
		}
		return null;	
	}

	public static synchronized TReflectorComponent GetAComponent() {
		if (ComponentsList.size() > 0)
			return ComponentsList.get(0); //. ->
		else
			return null; //. ->
	}
	
	public static synchronized int ComponentsCount() {
		return ComponentsList.size();
	}

	private static synchronized void _AddComponent(TReflectorComponent pComponent) {
		ComponentsList.add(pComponent);
	}

	private static synchronized void _RemoveComponent(TReflectorComponent pComponent) {
		ComponentsList.remove(pComponent);
	}

	private static final int MaxLastWindowsCount = 10;

	public static class TReflectorConfiguration {

		public static final String ConfigurationFileName = "GeoEye.Configuration";
		public static final String LastConfigurationFilePrefix = "last";
		public static final int ConfigurationFileVersion = 1;
		public static final String ReflectionWindowFileName = "ReflectionWindow.dat";
		public static final String ReflectionWindowDisabledLaysFileName = "ReflectionWindow_DisabledLays.dat";

		private Context context;
		private TReflectorComponent Reflector;
		// .
		public boolean flChanged = false;
		// .
		public boolean Application_flQuitAbility = true;
		// .
		public String ServerAddress = "89.108.122.51";
		public int ServerPort = 80;
		// .
		public long		UserID = 2;
		public String 	UserName = "";
		public String 	UserPassword = "ra3tkq";
		public boolean 	flUserSession = false;
		public boolean 	flSecureConnections = false;
		// .
		public int GeoSpaceID = 88;
		public int GeoSpaceDatumID = 23; // . WGS-84
		// .
		public int ReflectionWindow_ViewMode = VIEWMODE_TILES;
		public byte[] ReflectionWindowData = null;
		public int[] ReflectionWindow_DisabledLaysIDs = null;
		public boolean ReflectionWindow_flShowHints = true;
		public String ReflectionWindow_ViewMode_Tiles_Compilation = "";
		public int ReflectionWindow_NavigationMode = NAVIGATION_MODE_MULTITOUCHING1;
		public boolean ReflectionWindow_flLargeControlButtons = false;
		public boolean ReflectionWindow_flTMSOption = false;
		// . GeoLog data
		public boolean GeoLog_flEnabled = false;
		public boolean GeoLog_flAudioNotifications = false;
		public boolean GeoLog_flServerConnection = true;
		public String GeoLog_ServerAddress = "89.108.122.51";
		public int GeoLog_ServerPort = 8282;
		public int GeoLog_ObjectID = 0;
		public String GeoLog_ObjectName = "";
		public int GeoLog_QueueTransmitInterval = 0;
		public boolean GeoLog_flSaveQueue = true;
		public int GeoLog_GPSModuleProviderReadInterval = 0;
		public int GeoLog_GPSModuleMapID = 6;
		public boolean GeoLog_VideoRecorderModuleEnabled = true;
		public boolean GeoLog_VoiceCommandModuleEnabled = false;
		public boolean 	GeoLog_MovementDetectorModuleHitDetectorEnabled = false;
		public double 	GeoLog_MovementDetectorModuleHitDetectorThreshold = TMovementDetectorModule.THittingDetector.Threshold_VeryHard;
		public boolean GeoLog_flHide = false;

		public TReflectorConfiguration(Context pcontext, TReflectorComponent pReflector) {
			context = pcontext;
			Reflector = pReflector;
		}

		private void LoadReflectionWindowDisabledLays() throws IOException {
			String FN = ProfileFolder() + "/"
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
								.ConvertLEByteArrayToInt32(BA, 0);
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
				try {
					NL = XmlDoc.getDocumentElement().getElementsByTagName(
							"Application_flQuitAbility");
					Application_flQuitAbility = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				} catch (Exception E) {
					Application_flQuitAbility = true;
				}
				// .
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
				UserID = Long.parseLong(NL.item(0).getFirstChild()
						.getNodeValue());
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"UserName");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						UserName = N.getNodeValue();
					else
						UserName = "";
				} else
					UserName = "";
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"UserPassword");
				UserPassword = NL.item(0).getFirstChild().getNodeValue();
				// .
				try {
					NL = XmlDoc.getDocumentElement().getElementsByTagName(
							"flUserSession");
					flUserSession = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				} catch (Exception E) {
					flUserSession = false;
				}
				// .
				try {
					NL = XmlDoc.getDocumentElement().getElementsByTagName(
							"flSecureConnections");
					flSecureConnections = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				} catch (Exception E) {
					flSecureConnections = false;
				}
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
				else
					ReflectionWindow_ViewMode = VIEWMODE_TILES;
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_flShowHints");
				if (NL.getLength() > 0)
					ReflectionWindow_flShowHints = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				else
					ReflectionWindow_flShowHints = true;
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_ViewMode_Tiles_Compilation");
				if ((NL.getLength() > 0)
						&& (NL.item(0).getFirstChild() != null))
					ReflectionWindow_ViewMode_Tiles_Compilation = NL.item(0)
							.getFirstChild().getNodeValue();
				else
					ReflectionWindow_ViewMode_Tiles_Compilation = "";
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_NavigationMode");
				if (NL.getLength() > 0)
					ReflectionWindow_NavigationMode = Integer.parseInt(NL
							.item(0).getFirstChild().getNodeValue());
				else
					ReflectionWindow_NavigationMode = NAVIGATION_MODE_MULTITOUCHING1;
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_flLargeControlButtons");
				if (NL.getLength() > 0)
					ReflectionWindow_flLargeControlButtons = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				else
					ReflectionWindow_flLargeControlButtons = false;
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"ReflectionWindow_flTMSOption");
				if (NL.getLength() > 0)
					ReflectionWindow_flTMSOption = (Integer.parseInt(NL.item(0)
							.getFirstChild().getNodeValue()) != 0);
				else
					ReflectionWindow_flTMSOption = false;
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_flEnabled");
				GeoLog_flEnabled = (Integer.parseInt(NL.item(0).getFirstChild()
						.getNodeValue()) != 0);
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_flAudioNotifications");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						GeoLog_flAudioNotifications = (Integer.parseInt(N.getNodeValue()) != 0);
					else
						GeoLog_flAudioNotifications = false;
				} 
				else
					GeoLog_flAudioNotifications = false;
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
					else
						GeoLog_ObjectName = "";
				} else
					GeoLog_ObjectName = "";
				// .
				NL = XmlDoc.getDocumentElement().getElementsByTagName(
						"GeoLog_flHide");
				if (NL.getLength() > 0) {
					Node N = NL.item(0).getFirstChild();
					if (N != null)
						GeoLog_flHide = (Integer.parseInt(N.getNodeValue()) != 0);
					else
						GeoLog_flHide = false;
				} 
				else
					GeoLog_flHide = false;
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
					//.
					if (Tracker.GeoLog.VideoRecorderModule != null)
						GeoLog_VideoRecorderModuleEnabled = Tracker.GeoLog.VideoRecorderModule.flEnabled;
					//.
					if ((Tracker.GeoLog.AudioModule != null) && (Tracker.GeoLog.AudioModule.VoiceCommandModule != null))
						GeoLog_VoiceCommandModuleEnabled = Tracker.GeoLog.AudioModule.VoiceCommandModule.flEnabled;
					//.
					if (Tracker.GeoLog.MovementDetectorModule != null) {
						GeoLog_MovementDetectorModuleHitDetectorEnabled = Tracker.GeoLog.MovementDetectorModule.HitDetector_flEnabled;
						GeoLog_MovementDetectorModuleHitDetectorThreshold = Tracker.GeoLog.MovementDetectorModule.HitDetector_Threshold;
					}
				}
				break; // . >
			default:
				throw new Exception("unknown configuration version, version: "
						+ Integer.toString(Version)); // . =>
			}
			flChanged = false;
			// . load reflection window
			FN = ProfileFolder() + "/" + ReflectionWindowFileName;
			F = new File(FN);
			if (F.exists()) {
				FileSize = F.length();
				if (FileSize > 0) {
					FIS = new FileInputStream(FN);
					try {
						ReflectionWindowData = new byte[(int) FileSize];
						FIS.read(ReflectionWindowData);
					} finally {
						FIS.close();
					}
				}
				else 
					ReflectionWindowData = null;
			} else
				ReflectionWindowData = null;
			// . load reflection window disabled lays
			LoadReflectionWindowDisabledLays();
			// .
			return true;
		}

		public synchronized void Load() throws Exception {
			int TryCount = 3;
			int SleepTime = 1000;
			for (int I = 0; I < TryCount; I++) {
				try {
					String FN = ProfileFolder() + "/"
							+ ConfigurationFileName;
					if (!_Load(FN))
						break; // . >
					return; // . ->
				} catch (Exception E) {
					Thread.sleep(SleepTime);
				}
			}
			String FN = ProfileFolder() + "/"
					+ ConfigurationFileName + "." + LastConfigurationFilePrefix;
			if (_Load(FN))
				return; // . ->
			throw new Exception(
					context.getString(R.string.SErrorOfConfigurationLoading)
							+ ConfigurationFileName); // . =>
		}

		private void SaveReflectionWindowDisabledLays() throws IOException {
			String FN;
			TReflectorSpaceLays Lays = Reflector.ReflectionWindow.getLays();
			if (Lays != null) {
				ReflectionWindow_DisabledLaysIDs = Lays.GetDisabledLaysIDs();
				// .
				FN = ProfileFolder() + "/"
						+ ReflectionWindowDisabledLaysFileName;
				if (ReflectionWindow_DisabledLaysIDs != null) {
					String TFN = FN + ".tmp";
					FileOutputStream FOS = new FileOutputStream(TFN);
					try {
						for (int I = 0; I < ReflectionWindow_DisabledLaysIDs.length; I++) {
							byte[] BA = TDataConverter
									.ConvertInt32ToLEByteArray(ReflectionWindow_DisabledLaysIDs[I]);
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

		public synchronized void Save() throws IOException {
			// . save reflection window disabled lays
			SaveReflectionWindowDisabledLays();
			// . save reflection window
			ReflectionWindowData = Reflector.ReflectionWindow.GetWindow()
					.ToByteArray();
			String FN = ProfileFolder() + "/"
					+ ReflectionWindowFileName;
			if (ReflectionWindowData != null) {
				String TFN = FN + ".tmp";
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
			// .
			if (flChanged) {
				FN = ProfileFolder() + "/" + ConfigurationFileName;
				String TFN = FN + ".tmp";
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
					serializer.startTag("", "Application_flQuitAbility");
					int IV = 0;
					if (Application_flQuitAbility)
						IV = 1;
					serializer.text(Integer.toString(IV));
					serializer.endTag("", "Application_flQuitAbility");
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
					serializer.text(Long.toString(UserID));
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
					serializer.startTag("", "flUserSession");
					IV = 0;
					if (flUserSession)
						IV = 1;
					serializer.text(Integer.toString(IV));
					serializer.endTag("", "flUserSession");
					// .
					serializer.startTag("", "flSecureConnections");
					IV = 0;
					if (flSecureConnections)
						IV = 1;
					serializer.text(Integer.toString(IV));
					serializer.endTag("", "flSecureConnections");
					// .
					serializer.startTag("", "GeoSpaceID");
					serializer.text(Integer.toString(GeoSpaceID));
					serializer.endTag("", "GeoSpaceID");
					// .
					serializer.startTag("", "ReflectionWindow_ViewMode");
					serializer
							.text(Integer.toString(ReflectionWindow_ViewMode));
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
					serializer
							.text(ReflectionWindow_ViewMode_Tiles_Compilation);
					serializer.endTag("",
							"ReflectionWindow_ViewMode_Tiles_Compilation");
					// .
					serializer.startTag("", "ReflectionWindow_NavigationMode");
					serializer.text(Integer
							.toString(ReflectionWindow_NavigationMode));
					serializer.endTag("", "ReflectionWindow_NavigationMode");
					// .
					serializer.startTag("", "ReflectionWindow_flLargeControlButtons");
					if (ReflectionWindow_flLargeControlButtons)
						S = "1";
					else
						S = "0";
					serializer.text(S);
					serializer.endTag("", "ReflectionWindow_flLargeControlButtons");
					// .
					serializer.startTag("", "ReflectionWindow_flTMSOption");
					if (ReflectionWindow_flTMSOption)
						S = "1";
					else
						S = "0";
					serializer.text(S);
					serializer.endTag("", "ReflectionWindow_flTMSOption");
					// .
					if (GeoLog_flEnabled)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flEnabled");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flEnabled");
					// .
					if (GeoLog_flAudioNotifications)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flAudioNotifications");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flAudioNotifications");
					// .
					if (GeoLog_flServerConnection)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flServerConnection");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flServerConnection");
					// .
					serializer.startTag("", "GeoLog_ServerAddress");
					serializer.text(GeoLog_ServerAddress);
					serializer.endTag("", "GeoLog_ServerAddress");
					// .
					serializer.startTag("", "GeoLog_ServerPort");
					serializer.text(Integer.toString(GeoLog_ServerPort));
					serializer.endTag("", "GeoLog_ServerPort");
					// .
					serializer.startTag("", "GeoLog_ObjectID");
					serializer.text(Integer.toString(GeoLog_ObjectID));
					serializer.endTag("", "GeoLog_ObjectID");
					// .
					serializer.startTag("", "GeoLog_ObjectName");
					serializer.text(GeoLog_ObjectName);
					serializer.endTag("", "GeoLog_ObjectName");
					// .
					if (GeoLog_flHide)
						S = "1";
					else
						S = "0";
					serializer.startTag("", "GeoLog_flHide");
					serializer.text(S);
					serializer.endTag("", "GeoLog_flHide");
					// .
					serializer.endTag("", "ROOT");
					serializer.endDocument();
				} finally {
					writer.close();
				}
				File TF = new File(TFN);
				File F = new File(FN);
				if (F.exists()) {
					File LFN = new File(ProfileFolder() + "/"
							+ ConfigurationFileName + "."
							+ LastConfigurationFilePrefix);
					F.renameTo(LFN);
				}
				TF.renameTo(F);
				flChanged = false;
			}
		}

		public void Validate(boolean flUpdateButtons, boolean flUpdateOptionsMenu) throws Exception {
			TServerConnection.flSecureConnection = flSecureConnections;
			//. validate the Reflector
			try {
				Reflector.ButtonsStyle = BUTTONS_STYLE_BRIEF;
				if (ReflectionWindow_flLargeControlButtons)
					Reflector.ButtonsStyle = BUTTONS_STYLE_NORMAL;
				//.
	    		if (flUpdateButtons)
	    			Reflector.WorkSpace_Buttons_Recreate(true);
	    		if (flUpdateOptionsMenu)
	    			Reflector.ParentActivity.invalidateOptionsMenu();
				//.
				Reflector.Server.SetServerAddress(ServerAddress, ServerPort);
				Reflector.InitializeUser(flUserSession);
			} catch (Exception E) {
				Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
			//. validate user-agent
			try {
				TUserAgentService _Service = TUserAgentService.GetService();
				if (_Service != null)
					_Service.Validate();
			} catch (Exception E) {
				Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
			//. validate tracker
			try {
				// . set tracker configuration as well
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					Tracker.GeoLog.Stop();
					//.
					//. not needed, managed at the tracker panel: Tracker.GeoLog.flEnabled = this.GeoLog_flEnabled;
					Tracker.GeoLog.flAudioNotifications = this.GeoLog_flAudioNotifications;
					Tracker.GeoLog.UserID = (int)this.UserID;
					Tracker.GeoLog.UserPassword = this.UserPassword;
					Tracker.GeoLog.ObjectID = this.GeoLog_ObjectID;
					// .
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
					if ((Tracker.GeoLog.AudioModule != null) && (Tracker.GeoLog.AudioModule.VoiceCommandModule != null))
						Tracker.GeoLog.AudioModule.VoiceCommandModule.flEnabled = this.GeoLog_VoiceCommandModuleEnabled;
					if (Tracker.GeoLog.MovementDetectorModule != null) {
						Tracker.GeoLog.MovementDetectorModule.HitDetector_flEnabled = this.GeoLog_MovementDetectorModuleHitDetectorEnabled;
						Tracker.GeoLog.MovementDetectorModule.HitDetector_Threshold = this.GeoLog_MovementDetectorModuleHitDetectorThreshold;
					}
					// .
					Tracker.GeoLog.SaveProfile();
				}
				// . restart tracker
				TTrackerService _Service = TTrackerService.GetService();
				if (_Service != null)
					_Service.SetServicing(false);
				TTracker.RestartTracker(context);
				if (_Service != null)
					_Service.SetServicing(true);
			} catch (Exception E) {
				Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
			//.
			Reflector.CoGeoMonitorObjects = new TCoGeoMonitorObjects(Reflector);
			Reflector.CoGeoMonitorObjects_LocationUpdating_Intialize();
			//.
			Reflector.StartUpdatingSpaceImage();
		}
	}

	private class TUserIncomingMessageReceiver extends
			TGeoScopeServerUser.TIncomingMessages.TReceiver {

		private TGeoScopeServerUser MyUser;

		private TComponentServiceOperation ServiceOperation = null;

		public TUserIncomingMessageReceiver(TGeoScopeServerUser pUser)
				throws Exception {
			MyUser = pUser;
			// .
			MyUser.IncomingMessages.AddReceiver(this, true, false);
		}

		public void Destroy() {
			ServiceOperation_Cancel();
			// .
			if (MyUser.IncomingMessages != null)
				MyUser.IncomingMessages.RemoveReceiver(this);
		}

		@Override
		public boolean DoOnCommand(final TGeoScopeServerUser User,
				TIncomingCommandMessage Message) {
			if (Message instanceof TGeoScopeServerUser.TLocationCommandMessage) {
				try {
					final TGeoScopeServerUser.TLocationCommandMessage _Message = (TGeoScopeServerUser.TLocationCommandMessage) Message;
					String _UserText;
					if (Message.Sender != null)
						_UserText = Message.Sender.UserName + "\n" + "  "
								+ Message.Sender.UserFullName;
					else
						_UserText = "? (ID: "
								+ Long.toString(Message.SenderID) + ")";
					final String UserText = _UserText;
					new AlertDialog.Builder(context)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.SConfirmation)
							.setMessage(
									context.getString(R.string.SUser)
											+ UserText
											+ "\n"
											+ context.getString(R.string.SHaveSentToYouANewPlace)
											+ "\n"
											+ "  "
											+ _Message.Location.Name
											+ "\n"
											+ context.getString(R.string.SDoYouWantToAddPlace))
							.setPositiveButton(R.string.SYes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												ElectedPlaces
														.AddPlace(_Message.Location);
												_Message.SetAsProcessed();
												// .
												SetReflectionWindowByLocation(_Message.Location);
												// .
												Toast.makeText(
														context,
														context.getString(R.string.SPlace1)
																+ "'"
																+ _Message.Location.Name
																+ "'"
																+ context.getString(R.string.SHasBeenAddedToYourList),
														Toast.LENGTH_SHORT)
														.show();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									})
							.setNegativeButton(R.string.SNo,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												_Message.SetAsProcessed();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									}).show();
					// .
					return true; // . ->
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			} else if (Message instanceof TGeoScopeServerUser.TGeoMonitorObjectCommandMessage) {
				try {
					final TGeoScopeServerUser.TGeoMonitorObjectCommandMessage _Message = (TGeoScopeServerUser.TGeoMonitorObjectCommandMessage) Message;
					String _UserText;
					if (Message.Sender != null)
						_UserText = Message.Sender.UserName + "\n" + "  "
								+ Message.Sender.UserFullName;
					else
						_UserText = "? (ID: "
								+ Long.toString(Message.SenderID) + ")";
					final String UserText = _UserText;
					new AlertDialog.Builder(context)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.SConfirmation)
							.setMessage(
									context.getString(R.string.SUser)
											+ UserText
											+ "\n"
											+ context.getString(R.string.SHaveSentToYouANewObject)
											+ "\n"
											+ "  "
											+ _Message.CoGeoMonitorObject.Name
											+ "\n"
											+ context.getString(R.string.SDoYouWantToAddObject))
							.setPositiveButton(R.string.SYes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												TCoGeoMonitorObject Item = _Message.CoGeoMonitorObject;
												// .
												Item.flEnabled = false;
												Item.Prepare(TReflectorComponent.this);
												// .
												CoGeoMonitorObjects
														.AddItem(Item);
												_Message.SetAsProcessed();
												// .
												Toast.makeText(
														context,
														context.getString(R.string.SObject)
																+ "'"
																+ _Message.CoGeoMonitorObject.Name
																+ "'"
																+ context.getString(R.string.SHasBeenAddedToYourList1),
														Toast.LENGTH_SHORT)
														.show();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									})
							.setNegativeButton(R.string.SNo,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												_Message.SetAsProcessed();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									}).show();
					// .
					return true; // . ->
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			} else if (Message instanceof TGeoScopeServerUser.TURLCommandMessage) {
				try {
					final TGeoScopeServerUser.TURLCommandMessage _Message = (TGeoScopeServerUser.TURLCommandMessage) Message;
					String _UserText;
					if (Message.Sender != null)
						_UserText = Message.Sender.UserName + "\n" + "  "
								+ Message.Sender.UserFullName;
					else
						_UserText = "? (ID: "
								+ Long.toString(Message.SenderID) + ")";
					final String UserText = _UserText;
					new AlertDialog.Builder(context)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.SConfirmation)
							.setMessage(
									context.getString(R.string.SUser)
											+ UserText
											+ "\n"
											+ context.getString(R.string.SHaveSentToYouANewBookmark)
											+ "\n"
											+ "  "
											+ _Message.URLName
											+ "\n"
											+ context.getString(R.string.SDoYouWantToAddIt))
							.setPositiveButton(R.string.SYes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												TURLFolderListComponent.Components_AddNewURL(URLListFolder, _Message.URLName,_Message.URLData, User);
												//.
												_Message.SetAsProcessed();
												// .
												Toast.makeText(
														context,
														context.getString(R.string.SBookmark)
																+ "'"
																+ _Message.URLName
																+ "'"
																+ context.getString(R.string.SHasBeenAddedToYourList1),
														Toast.LENGTH_SHORT)
														.show();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									})
							.setNegativeButton(R.string.SNo,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												_Message.SetAsProcessed();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									}).show();
					// .
					return true; // . ->
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			} else if (Message instanceof TGeoScopeServerUser.TUserTaskStatusCommandMessage) {
				try {
					final TGeoScopeServerUser.TUserTaskStatusCommandMessage _Message = (TGeoScopeServerUser.TUserTaskStatusCommandMessage) Message;
					String _UserText;
					if (Message.Sender != null)
						_UserText = Message.Sender.UserName + "\n" + "  "
								+ Message.Sender.UserFullName;
					else
						_UserText = "? (ID: "
								+ Long.toString(Message.SenderID) + ")";
					final String UserText = _UserText;
					new AlertDialog.Builder(context)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.SConfirmation)
							.setMessage(
									context.getString(R.string.SUser)
											+ UserText
											+ "\n"
											+ context.getString(R.string.SNotifiesThatTaskStatusHasBeenChangedTo)
											+ "\n"
											+ " "
											+ TTaskStatusValue
													.Status_String(
															_Message.UserTaskStatusDescriptor.Status,
															context)
											+ "\n"
											+ context.getString(R.string.SDoYouWantToSeeThatTask))
							.setPositiveButton(R.string.SYes,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												Tasks_OpenTaskPanel(_Message);
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									})
							.setNegativeButton(R.string.SNo,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												_Message.SetAsProcessed();
											} catch (Exception E) {
												Toast.makeText(context,
														E.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											}
										}
									}).show();
					// .
					return true; // . ->
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
			return false;
		}

		@Override
		public boolean DoOnCommandResponse(TGeoScopeServerUser User,
				TIncomingCommandResponseMessage Message) {
			return false;
		}

		@Override
		public boolean DoOnMessage(TGeoScopeServerUser User,
				TIncomingMessage Message) {
			TUserChatPanel UCP = TUserChatPanel.Panels.get(Message.SenderID);
			if (UCP == null) {
				Intent intent = new Intent(context,
						TUserChatPanel.class);
				// .
				intent.putExtra("UserID", Message.Sender.UserID);
				intent.putExtra("UserName", Message.Sender.UserName);
				intent.putExtra("UserFullName", Message.Sender.UserFullName);
				intent.putExtra("UserContactInfo",
						Message.Sender.UserContactInfo);
				// .
				intent.putExtra("MessageID", Message.ID);
				// .
				ParentActivity.startActivity(intent);
			} else
				UCP.ReceiveMessage(Message);
			return true;
		}

		private void ServiceOperation_Cancel() {
			if (ServiceOperation != null) {
				ServiceOperation.Cancel();
				ServiceOperation = null;
			}
		}

		private void Tasks_OpenTaskPanel(
				TGeoScopeServerUser.TUserTaskStatusCommandMessage pMessage)
				throws Exception {
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker == null)
				throw new Exception(
						context.getString(R.string.STrackerIsNotInitialized)); // . =>
			final TGeoScopeServerUser.TUserTaskStatusCommandMessage Message = pMessage;
			ServiceOperation_Cancel();
			ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskData(
					Tracker.GeoLog.UserID,
					Message.UserTaskStatusDescriptor.idTask,
					new TTaskDataValue.TTaskDataIsReceivedHandler() {
						@Override
						public void DoOnTaskDataIsReceived(byte[] TaskData) {
							Task_OnDataIsReceivedForOpening(TaskData);
							// .
							Message.SetAsProcessed();
						}
					}, new TTaskDataValue.TExceptionHandler() {
						@Override
						public void DoOnException(Exception E) {
							Task_DoOnException(E);
						}
					});
			// .
			MessageHandler.obtainMessage(MESSAGE_LOADINGPROGRESSBAR_SHOW)
					.sendToTarget();
		}

		private void Task_OnDataIsReceivedForOpening(byte[] TaskData) {
			MessageHandler.obtainMessage(MESSAGE_LOADINGPROGRESSBAR_HIDE)
					.sendToTarget();
			MessageHandler.obtainMessage(MESSAGE_OPENUSERTASKPANEL, TaskData)
					.sendToTarget();
		}

		private void Task_DoOnException(Exception E) {
			MessageHandler.obtainMessage(MESSAGE_LOADINGPROGRESSBAR_HIDE)
					.sendToTarget();
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
					.sendToTarget();
		}
	}

	public static class TViewLayer extends SurfaceView implements OnTouchListener {
		
		public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {

			public SurfaceHolder _SurfaceHolder = null;

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				if (SurfaceUpdating != null) {
					SurfaceUpdating.Destroy();
					SurfaceUpdating = null;
				}
				// .
				_SurfaceHolder = holder;
				if (flTransparent)
					_SurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
				//.
				SurfaceUpdating = new TSurfaceUpdating();
				// .
				DoOnSizeChanged(width,height);
				// .
				SurfaceUpdating.Start();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				_SurfaceHolder = holder;
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (SurfaceUpdating != null) {
					SurfaceUpdating.Destroy();
					SurfaceUpdating = null;
				}
				_SurfaceHolder = null;
			}
		}

		private static final int MESSAGE_DRAW = 1;

		private final Handler SurfaceMessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_DRAW:
						DoDraw();
						break; // . >
					}
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};

		public class TSurfaceUpdating implements Runnable {

			private Thread _Thread;
			private boolean flCancel = false;
			public boolean flProcessing = false;
			private TAutoResetEvent ProcessSignal = new TAutoResetEvent();

			public TSurfaceUpdating() {
				_Thread = new Thread(this);
			}

			public void Destroy() {
				CancelAndWait();
			}

			public void Start() {
				_Thread.start();
			}

			@Override
			public void run() {
				try {
					flProcessing = true;
					try {
						while (!flCancel) {
							ProcessSignal.WaitOne();
							if (flCancel)
								return; // . ->
							//.
							try {
								DoDraw();
							}
							catch (Exception E) {
							}
							//.
							TGeoLogApplication.Instance().GarbageCollector.Start();
						}
					} finally {
						flProcessing = false;
					}
				} catch (Throwable T) {
				}
			}

			public void StartUpdate() {
				ProcessSignal.Set();
			}

			public void Join() {
				try {
					if (_Thread != null)
						_Thread.join();
				} catch (Exception E) {
				}
			}

			public void Cancel() {
				flCancel = true;
				// .
				ProcessSignal.Set();
				// .
				if (_Thread != null)
					_Thread.interrupt();
			}

			public void CancelAndWait() {
				Cancel();
				Join();
			}
		}

		protected TReflectorComponent Reflector = null;
		//.
		private TSurfaceHolderCallbackHandler 	SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
		private TSurfaceUpdating 				SurfaceUpdating = null;
		//.
		public int Width = 0;
		public int Height = 0;
		//. 
		protected boolean flTransparent = false;
		
		public TViewLayer(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public TViewLayer(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TViewLayer(Context context) {
			super(context);
		}
		
		public void Initialize(TReflectorComponent pReflector) throws Exception {
			Reflector = pReflector;
			//.
			SurfaceHolder sh = getHolder();
			sh.addCallback(SurfaceHolderCallbackHandler);
			//.
			setOnTouchListener(this);
			//.
			setVisibility(View.VISIBLE);
		}

		public void Finalize() {
			setVisibility(View.GONE);
			clearAnimation();
			//.
			SurfaceHolder sh = getHolder();
			sh.removeCallback(SurfaceHolderCallbackHandler);
		}

		public void Reinitialize(TReflectorComponent pReflector) throws Exception {
			Finalize();
			//.
			Initialize(pReflector);
			//.
			DoOnSizeChanged(Width, Height);
		}

		protected void DoOnSizeChanged(int w, int h) {
			Width = w;
			Height = h;
			//.
			setMinimumWidth(Width);
			setMinimumHeight(Height);
		}

		public void DoDraw() {
			if (SurfaceHolderCallbackHandler._SurfaceHolder == null)
				return; // . ->
			Canvas canvas = SurfaceHolderCallbackHandler._SurfaceHolder.lockCanvas();
			if (canvas == null)
				return; // . ->
			try {
				DoOnDraw(canvas, null/* DrawCanceller */, null/* DrawTimeLimit */);
			} finally {
				SurfaceHolderCallbackHandler._SurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}

		public void Draw() {
			StartDraw();
		}

		public void StartDraw() {
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
		}

		public void PostDraw() {
			SurfaceMessageHandler.obtainMessage(MESSAGE_DRAW).sendToTarget();
		}

		protected void DoOnDraw(Canvas canvas, TCanceller Canceller, TTimeLimit TimeLimit) {
		}
		
		@Override
		public boolean onTouch(View pView, MotionEvent pEvent) {
			try {
				switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {

				case MotionEvent.ACTION_DOWN:
					Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
					break; // . >

				case MotionEvent.ACTION_POINTER_DOWN:
					switch (pEvent.getPointerCount()) {

					case 1:
						Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
						break; // . >

					case 2:
						Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
						Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
						break; // . >

					case 3:
						Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
						Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
						Pointer2_Down(pEvent.getX(2), pEvent.getY(2));
						break; // . >
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
						break; // . >

					case 2:
						Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
						Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
						break; // . >

					case 3:
						Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
						Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
						Pointer2_Up(pEvent.getX(2), pEvent.getY(2));
						break; // . >
					}
					break; // . >

				case MotionEvent.ACTION_MOVE:
					switch (pEvent.getPointerCount()) {

					case 1:
						Pointer0_Move(pEvent.getX(0),pEvent.getY(0));
						break; // . >

					case 2:
						Pointer0_Move(pEvent.getX(0),pEvent.getY(0));
						Pointer1_Move(pEvent.getX(1),pEvent.getY(1));
						break; // . >

					case 3:
						Pointer0_Move(pEvent.getX(0),pEvent.getY(0));
						Pointer1_Move(pEvent.getX(1),pEvent.getY(1));
						Pointer2_Move(pEvent.getX(2),pEvent.getY(2));
						break; // . >
					}
					break; // . >

				default:
					return false; // . ->
				}
				return true; // . ->
			} catch (Throwable E) {
				TGeoLogApplication.Log_WriteError(E);
				// .
				return false; // . ->
			}
		}

		protected void Pointer0_Down(double X, double Y) {
		}
		
		protected void Pointer0_Up(double X, double Y) {
		}
		
		protected void Pointer0_Move(double X, double Y) {
		}
		
		protected void Pointer1_Down(double X, double Y) {
		}
		
		protected void Pointer1_Up(double X, double Y) {
		}
		
		protected void Pointer1_Move(double X, double Y) {
		}
		
		protected void Pointer2_Down(double X, double Y) {
		}
		
		protected void Pointer2_Up(double X, double Y) {
		}

		protected void Pointer2_Move(double X, double Y) {
		}
	}
	
	public static class TWorkSpace extends TViewLayer {

		public static float LeftGroupButtonXFitFactorForBriefMode = 1 / 10.0F;
		public static float LeftGroupButtonXFitFactorForNormalMode = 1.1F / 5.0F;
		// .
		public static int UpdateTransitionInterval = 50; // . milliseconds
		public static int UpdateTransitionStep = 25; // . %

		public static class TButtons {

			public static class TButton {

				public static final int STYLE_RECTANGLE = 0;
				public static final int STYLE_ELLIPSE = 1;

				public static final int STATUS_UP = 0;
				public static final int STATUS_DOWN = 1;

				public static String FontName = "Serif";
				public static float FontSize = 24.0F;

				public static class TStateColorProvider {

					public int GetStateColor() {
						return Color.BLACK;
					}
				}

				public int GroupID;
				public int Style = STYLE_RECTANGLE;
				// .
				public float Left;
				public float Top;
				public float Width;
				public float Height;
				public String Name;
				public boolean flEnabled = true;
				public int TextColor = Color.RED;
				public TStateColorProvider StateColorProvider = null;
				public int Status;

				public TButton(int pGroupID, int pStyle, float pLeft,
						float pTop, float pWidth, float pHeight, String pName,
						int pTextColor) {
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

				public TButton(int pGroupID, float pLeft, float pTop,
						float pWidth, float pHeight, String pName,
						int pTextColor) {
					this(pGroupID, STYLE_ELLIPSE, pLeft, pTop, pWidth, pHeight,
							pName, pTextColor);
				}

				public void SetStateColorProvider(
						TStateColorProvider pStateColorProvider) {
					StateColorProvider = pStateColorProvider;
				}

				public void SetStatus(int pStatus) {
					if (!flEnabled)
						return; // . ->
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
			public long DownButtons_Time = System.currentTimeMillis();

			public TButtons(TWorkSpace pWorkSpace) {
				WorkSpace = pWorkSpace;
				Items = new TButton[0];
				// .
				Typeface tf = Typeface.create(TButton.FontName, Typeface.BOLD);
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
						continue; // . ^
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
						// .
						paint.setStrokeWidth(WorkSpace.Reflector.metrics.density * 1.0F);
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(Color.LTGRAY);
						canvas.drawRect(Extent, paint);
						break; // . >

					case TButton.STYLE_ELLIPSE:
						Extent.left = Item.Left;
						Extent.right = Item.Left + Item.Width;
						Extent.top = Item.Top;
						Extent.bottom = Item.Top + Item.Height;
						// .
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
						// .
						paint.setStrokeWidth(WorkSpace.Reflector.metrics.density*1.0F);
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(Color.LTGRAY);
						canvas.drawOval(Extent, paint);
						break; // . >
					}
					if (Item.flEnabled)
						if (Item.Status == TButton.STATUS_DOWN)
							paint.setColor(Color.WHITE);
						else {
							if (Item.StateColorProvider != null)
								paint.setColor(Item.StateColorProvider
										.GetStateColor());
							else
								paint.setColor(Item.TextColor);
						}
					else
						paint.setColor(Color.GRAY);
					//.
					float TextSize = TButton.FontSize*WorkSpace.Reflector.metrics.density;
					if (TextSize > Item.Height)
						TextSize = Item.Height*0.75F;
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(TextSize);
					String S = Item.Name;
					Rect bounds = new Rect();
					paint.getTextBounds(S, 0, S.length(), bounds);
					canvas.drawText(S, Item.Left+(Item.Width-paint.measureText(S))/2.0F, Item.Top+(Item.Height+bounds.height())/2.0F, paint);
				}
			}

			public int GetItemAt(double pX, double pY) {
				for (int I = 0; I < Items.length; I++)
					if ((Items[I] != null)
							&& (((Items[I].Left <= pX) && (pX <= (Items[I].Left + Items[I].Width))) && ((Items[I].Top <= pY) && (pY <= (Items[I].Top + Items[I].Height)))))
						return I; // . ->
				return -1;
			}

			public void SetDownButton(int pDownButtonIndex) {
				Items[pDownButtonIndex].SetStatus(TButton.STATUS_DOWN);
				DownButtonIndex = pDownButtonIndex;
				// .
				DownButtons_Time = System.currentTimeMillis();
				// .
				WorkSpace.Draw();
			}

			public void ClearDownButton() {
				Items[DownButtonIndex].SetStatus(TButton.STATUS_UP);
				DownButtonIndex = -1;
				// .
				DownButtons_Time = System.currentTimeMillis();
				// .
				WorkSpace.Draw();
			}
		}

		public static class TNavigationArrows {

			public static float ArrowSize = 48.0F;

			public static double ScalingDelta = 8.0;
			public static double RotatingDelta = Math.PI / 96.0F;

			protected static class TArrow {

				public static final int STATUS_UP = 0;
				public static final int STATUS_DOWN = 1;
				// .
				public static final int ProcessDelay = 333; // . ms
				public static final int RepeatingDelay = 25; // . ms

				public static final float ArrowUnitFactor = 1.0F / 9;

				protected static Paint paint = new Paint();
				protected static Paint arrowpaint = new Paint();
				protected static float[] Frame = new float[16];

				protected TNavigationArrows Arrows;
				// .
				public float Left;
				public float Top;
				public float Width;
				public float Height;
				public boolean flEnabled = true;
				public int Status;
				private int BackgroundColor = Color.GRAY;
				// .
				private Timer Handler = null;
				private THandlerTask HandlerTask = null;

				public TArrow(TNavigationArrows pArrows, float pLeft,
						float pTop, float pWidth, float pHeight,
						int pBackgroundColor) {
					Arrows = pArrows;
					// .
					Left = pLeft;
					Top = pTop;
					Width = pWidth;
					Height = pHeight;
					// .
					BackgroundColor = pBackgroundColor;
					// .
					Status = STATUS_UP;
				}

				public void Destroy() {
					StopHandler();
				}

				public boolean SetStatus(int pStatus) {
					if (!flEnabled)
						return false; // . ->
					if (Status == pStatus)
						return false; // . ->
					Status = pStatus;
					// .
					if (Status == STATUS_DOWN)
						StartHandler();
					else
						StopHandler();
					// .
					return true;
				}

				private void StartHandler() {
					StopHandler();
					// .
					Process();
					// .
					HandlerTask = new THandlerTask();
					Handler = new Timer();
					Handler.schedule(HandlerTask, ProcessDelay, RepeatingDelay);
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
							try {
								switch (msg.what) {

								case MESSAGE_PROCESS:
									if (HandlerIsStarted())
										Process();
									break; // . >
								}
							} catch (Throwable E) {
								TGeoLogApplication.Log_WriteError(E);
							}
						}
					};

					public void Destroy() {
						MessageHandler.removeMessages(MESSAGE_PROCESS);
					}

					public void run() {
						try {
							MessageHandler.obtainMessage(MESSAGE_PROCESS)
									.sendToTarget();
						} catch (Throwable E) {
							TGeoLogApplication.Log_WriteError(E);
						}
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
					canvas.drawRect(Left, Top, Left + Width, Top + Height,
							paint);
					paint.setStrokeWidth(Arrows.WorkSpace.Reflector.metrics.density * 0.5F);
					paint.setColor(BackgroundColor);
					paint.setAlpha(196);
					Frame[0] = Left;
					Frame[1] = Top;
					Frame[2] = Left + Width;
					Frame[3] = Top;
					Frame[4] = Left + Width;
					Frame[5] = Top;
					Frame[6] = Left + Width;
					Frame[7] = Top + Height;
					Frame[8] = Left + Width;
					Frame[9] = Top + Height;
					Frame[10] = Left;
					Frame[11] = Top + Height;
					Frame[12] = Left;
					Frame[13] = Top + Height;
					Frame[14] = Left;
					Frame[15] = Top;
					canvas.drawLines(Frame, paint);
				}

				public boolean DoOnPointerDown(double pX, double pY) {
					if (((Left <= pX) && (pX <= (Left + Width)))
							&& ((Top <= pY) && (pY <= (Top + Height))))
						return SetStatus(STATUS_DOWN); // . ->
					else
						return SetStatus(STATUS_UP); // . ->
				}

				public boolean Release() {
					return SetStatus(STATUS_UP);
				}
			}

			private static class TScalingUpArrow extends TArrow {

				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xDBDBDB;

				public TScalingUpArrow(TNavigationArrows pArrows, float pLeft,
						float pTop, float pWidth, float pHeight) {
					super(pArrows, pLeft, pTop, pWidth, pHeight,
							BackgroundColor);
				}

				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					// .
					float Unit = Height * ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					// .
					float X = Left + Width / 2.0F;
					float ArrowR = Width / 4.0F;
					float Shift = (float) (Unit / (2.0 * Math.sqrt(2.0)));
					Frame[0] = X;
					Frame[1] = Top + Height - Unit;
					Frame[2] = X;
					Frame[3] = Top + Unit;
					Frame[4] = Frame[2] + Shift;
					Frame[5] = Frame[3] - Shift;
					Frame[6] = Frame[2] - ArrowR;
					Frame[7] = Frame[3] + ArrowR;
					Frame[8] = Frame[2] - Shift;
					Frame[9] = Frame[3] - Shift;
					Frame[10] = Frame[2] + ArrowR;
					Frame[11] = Frame[3] + ArrowR;
					canvas.drawLines(Frame, 0, 12, arrowpaint);
				}

				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX, pY);
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_SCALING;
					return Result;
				}

				@Override
				protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					// .
					double Scale = (1.0 + Arrows.WorkSpace.Reflector.ScaleCoef
							* (-ScalingDelta)
							/ Arrows.WorkSpace.Reflector.ReflectionWindow
									.getHeight());
					// .
					// Arrows.WorkSpace.Reflector.NavigationTransformatrix.postScale((float)
					// Scale, (float) Scale,
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					if (Arrows.WorkSpace.Reflector.SpaceImage != null)
						synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
							Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix
									.postScale(
											(float) Scale,
											(float) Scale,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
							if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
								Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix
										.postScale(
												(float) Scale,
												(float) Scale,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix
							.postScale(
									(float) (1.0 / Scale),
									(float) (1.0 / Scale),
									Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
									Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					// .
					Arrows.WorkSpace.Update();
					//.
					Arrows.WorkSpace.Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
				}
			}

			private static class TScalingDownArrow extends TArrow {

				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xDBDBDB;

				public TScalingDownArrow(TNavigationArrows pArrows,
						float pLeft, float pTop, float pWidth, float pHeight) {
					super(pArrows, pLeft, pTop, pWidth, pHeight,
							BackgroundColor);
				}

				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					// .
					float Unit = Height * ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					// .
					float X = Left + Width / 2.0F;
					float ArrowR = Width / 4.0F;
					float Shift = (float) (Unit / (2.0 * Math.sqrt(2.0)));
					Frame[0] = X;
					Frame[1] = Top + Unit;
					Frame[2] = X;
					Frame[3] = Top + Height - Unit;
					Frame[4] = Frame[2] + Shift;
					Frame[5] = Frame[3] + Shift;
					Frame[6] = Frame[2] - ArrowR;
					Frame[7] = Frame[3] - ArrowR;
					Frame[8] = Frame[2] - Shift;
					Frame[9] = Frame[3] + Shift;
					Frame[10] = Frame[2] + ArrowR;
					Frame[11] = Frame[3] - ArrowR;
					canvas.drawLines(Frame, 0, 12, arrowpaint);
				}

				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX, pY);
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_SCALING;
					return Result;
				}

				@Override
				protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					// .
					double Scale = (1.0 + Arrows.WorkSpace.Reflector.ScaleCoef
							* ScalingDelta
							/ Arrows.WorkSpace.Reflector.ReflectionWindow
									.getHeight());
					// .
					// Arrows.WorkSpace.Reflector.NavigationTransformatrix.postScale((float)
					// Scale, (float) Scale,
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					if (Arrows.WorkSpace.Reflector.SpaceImage != null)
						synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
							Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix
									.postScale(
											(float) Scale,
											(float) Scale,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
							if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
								Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix
										.postScale(
												(float) Scale,
												(float) Scale,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix
							.postScale(
									(float) (1.0 / Scale),
									(float) (1.0 / Scale),
									Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
									Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					// .
					Arrows.WorkSpace.Update();
					//.
					Arrows.WorkSpace.Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
				}
			}

			private static class TRotatingUpArrow extends TArrow {

				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xBABABA;

				public TRotatingUpArrow(TNavigationArrows pArrows, float pLeft,
						float pTop, float pWidth, float pHeight) {
					super(pArrows, pLeft, pTop, pWidth, pHeight,
							BackgroundColor);
				}

				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					// .
					float Unit = Height * ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					// .
					float X0 = Left + Width - Unit;
					float X1 = Left + Unit;
					float ArrowR = Width / 4.0F;
					float Shift = Unit / 2.0F;
					Frame[0] = X0;
					Frame[1] = Top + Height - Unit;
					Frame[2] = X1;
					Frame[3] = Top + Unit;
					Frame[4] = Frame[2];
					Frame[5] = Frame[3] - Shift;
					Frame[6] = Frame[2];
					Frame[7] = Frame[3] + ArrowR;
					Frame[8] = Frame[2] - Shift;
					Frame[9] = Frame[3];
					Frame[10] = Frame[2] + ArrowR;
					Frame[11] = Frame[3];
					canvas.drawLines(Frame, 0, 12, arrowpaint);
				}

				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX, pY);
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					return Result;
				}

				@Override
				protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					// .
					double Gamma = RotatingDelta;
					// .
					// Arrows.WorkSpace.Reflector.NavigationTransformatrix.postRotate((float)
					// (-Gamma * 180.0 / Math.PI),
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					if (Arrows.WorkSpace.Reflector.SpaceImage != null)
						synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
							Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix
									.postRotate(
											(float) (-Gamma * 180.0 / Math.PI),
											Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
							if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
								Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix
										.postRotate(
												(float) (-Gamma * 180.0 / Math.PI),
												Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix
							.postRotate(
									(float) (Gamma * 180.0 / Math.PI),
									Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
									Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					// .
					Arrows.WorkSpace.Update();
					//.
					Arrows.WorkSpace.Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
				}
			}

			private static class TRotatingDownArrow extends TArrow {

				public static final int BackgroundColor = Color.DKGRAY;
				public static final int ArrowColor = 0xBABABA;

				public TRotatingDownArrow(TNavigationArrows pArrows,
						float pLeft, float pTop, float pWidth, float pHeight) {
					super(pArrows, pLeft, pTop, pWidth, pHeight,
							BackgroundColor);
				}

				@Override
				public void Draw(Canvas canvas) {
					super.Draw(canvas);
					// .
					float Unit = Height * ArrowUnitFactor;
					arrowpaint.setAntiAlias(true);
					arrowpaint.setColor(ArrowColor);
					arrowpaint.setStrokeWidth(Unit);
					arrowpaint.setAlpha(196);
					// .
					float X0 = Left + Width - Unit;
					float X1 = Left + Unit;
					float ArrowR = Width / 4.0F;
					float Shift = Unit / 2.0F;
					Frame[0] = X0;
					Frame[1] = Top + Unit;
					Frame[2] = X1;
					Frame[3] = Top + Height - Unit;
					Frame[4] = Frame[2];
					Frame[5] = Frame[3] + Shift;
					Frame[6] = Frame[2];
					Frame[7] = Frame[3] - ArrowR;
					Frame[8] = Frame[2] - Shift;
					Frame[9] = Frame[3];
					Frame[10] = Frame[2] + ArrowR;
					Frame[11] = Frame[3];
					canvas.drawLines(Frame, 0, 12, arrowpaint);
				}

				@Override
				public boolean DoOnPointerDown(double pX, double pY) {
					boolean Result = super.DoOnPointerDown(pX, pY);
					if (Result)
						Arrows.WorkSpace.Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					return Result;
				}

				@Override
				protected void Process() {
					Arrows.WorkSpace.Reflector.CancelUpdatingSpaceImage();
					// .
					double Gamma = -RotatingDelta;
					// .
					// Arrows.WorkSpace.Reflector.NavigationTransformatrix.postRotate((float)
					// (-Gamma * 180.0 / Math.PI),
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
					// Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					if (Arrows.WorkSpace.Reflector.SpaceImage != null)
						synchronized (Arrows.WorkSpace.Reflector.SpaceImage) {
							Arrows.WorkSpace.Reflector.SpaceImage.ResultBitmapTransformatrix
									.postRotate(
											(float) (-Gamma * 180.0 / Math.PI),
											Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
											Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
							if (Arrows.WorkSpace.Reflector.SpaceImage.flSegments)
								Arrows.WorkSpace.Reflector.SpaceImage.SegmentsTransformatrix
										.postRotate(
												(float) (-Gamma * 180.0 / Math.PI),
												Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
												Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
						}
					Arrows.WorkSpace.Reflector.ReflectionWindowTransformatrix
							.postRotate(
									(float) (Gamma * 180.0 / Math.PI),
									Arrows.WorkSpace.Reflector.ReflectionWindow.Xmd,
									Arrows.WorkSpace.Reflector.ReflectionWindow.Ymd);
					// .
					Arrows.WorkSpace.Update();
					//.
					Arrows.WorkSpace.Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
				}
			}

			public TWorkSpace WorkSpace;
			// .
			private TArrow[] Items;
			// .
			public TArrow DownArrow;

			public TNavigationArrows(TWorkSpace pWorkSpace) {
				WorkSpace = pWorkSpace;
				// .
				Items = null;
				DownArrow = null;
			}

			public void Destroy() {
				ClearItems();
			}

			public void Prepare(float Width, float Height) {
				ClearItems();
				// .
				Items = new TArrow[4];
				float ArrowWidth = ArrowSize
						* WorkSpace.Reflector.metrics.density;
				float ArrowHeight = ArrowWidth;
				float X = Width - ArrowWidth;
				float Y = Height / 2.0F - 2.0F * ArrowHeight;
				Items[0] = new TRotatingUpArrow(this, X, Y, ArrowWidth,
						ArrowHeight);
				Y += ArrowHeight;
				Items[1] = new TScalingUpArrow(this, X, Y, ArrowWidth,
						ArrowHeight);
				Y += ArrowHeight;
				Items[2] = new TScalingDownArrow(this, X, Y, ArrowWidth,
						ArrowHeight);
				Y += ArrowHeight;
				Items[3] = new TRotatingDownArrow(this, X, Y, ArrowWidth,
						ArrowHeight);
			}

			public void ClearItems() {
				if (Items == null)
					return; // . ->
				for (int I = 0; I < Items.length; I++)
					Items[I].Destroy();
				Items = null;
			}

			public void Draw(Canvas canvas) {
				if (Items == null)
					return; // . ->
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
					return Result; // . ->
				for (int I = 0; I < Items.length; I++)
					if (Items[I].DoOnPointerDown(pX, pY))
						Result = Items[I];
				DownArrow = Result;
				if (Result != null)
					WorkSpace.Draw();
				return Result;
			}

			public boolean Release() {
				boolean Result = false;
				if (DownArrow == null)
					return Result; // . ->
				Result = DownArrow.Release();
				DownArrow = null;
				if (Result)
					WorkSpace.Draw();
				return Result;
			}

			public boolean ReleaseAll() {
				boolean Result = false;
				if (Items == null)
					return Result; // . ->
				for (int I = 0; I < Items.length; I++)
					Result |= Items[I].Release();
				if (Result)
					WorkSpace.Draw();
				return Result;
			}
		}

		public static class TDialogPanel {
			
			public static final float OwnerWidthFit = 0.50F; //. %
			public static final float DefaultHeight = 36.0F;
			//.
			public static final int BackgroundColor = Color.LTGRAY;
			public static final int BackgroundTransparency = 200;
			public static final int SelectedBackgroundColor = Color.RED;
			public static final int SelectedBackgroundTransparency = 255;
			
			public static class TButton {
				
				public static class TClickHandler {
					
					public void DoOnClick() {
					}
				}
				
				
				private TDialogPanel Panel;
				//.
				private String Text;
				//.
				private TClickHandler ClickHandler;
				
				public TButton(TDialogPanel pPanel, String pText, TClickHandler pClickHandler) {
					Panel = pPanel;
					Text = pText;
					ClickHandler = pClickHandler;
				}
				
				public void DrawOnCanvas(Canvas canvas, RectF rect, int BackgroundColor, int BackgroundTransparency) {
					//. background
					Panel.paint.setStyle(Paint.Style.FILL);
					Panel.paint.setColor(BackgroundColor);
					Panel.paint.setAlpha(BackgroundTransparency);
					canvas.drawRoundRect(rect, Panel.RoundRectRadius,Panel.RoundRectRadius, Panel.paint);
					//.
					Panel.paint.setStyle(Paint.Style.STROKE);
					Panel.paint.setStrokeWidth(1.5F*Panel.WorkSpace.Reflector.metrics.density);
					Panel.paint.setColor(Color.WHITE);
					Panel.paint.setAlpha(255);
					canvas.drawRoundRect(rect, Panel.RoundRectRadius,Panel.RoundRectRadius, Panel.paint);
					//. text
					Panel.paint.setStyle(Paint.Style.FILL);
					Panel.paint.setColor(Color.BLACK);
					Panel.paint.setTextSize(Panel.ButtonTextSize);
					float TW = Panel.paint.measureText(Text);
					float TH = Panel.paint.getTextSize();
					float X = rect.left+((rect.right-rect.left)-TW)/2.0F;
					float Y = rect.bottom-((rect.bottom-rect.top)-TH)/2.0F;
					canvas.drawText(Text, X,Y, Panel.paint);
				}
				
				public void Click() {
					ClickHandler.DoOnClick();
				}
			}
			
			
			public TWorkSpace WorkSpace;
			// .
			private ArrayList<TButton> 	Buttons = new ArrayList<TButton>();
			private TButton				Buttons_Selected = null;
			//.
			protected float ButtonTextSize;
			protected float RoundRectRadius;
			protected Paint paint = new Paint();

			public TDialogPanel(TWorkSpace pWorkSpace) {
				WorkSpace = pWorkSpace;
				//.
				RoundRectRadius = 8.0F*WorkSpace.Reflector.metrics.density;
			}

			public void AddButton(String ButtonText, TButton.TClickHandler ClickHandler) {
				TButton Button = new TButton(this, ButtonText, ClickHandler);
				Buttons.add(Button);
			}
			
			public void SetSelectedButton(TButton Button) {
				if (Button != Buttons_Selected) {
					Buttons_Selected = Button;
					WorkSpace.Draw();
				}
			}
			
			public void ClearSelectedButton() {
				if (Buttons_Selected != null) {
					Buttons_Selected = null;
					WorkSpace.Draw();
				}
			}
			
			public TButton GetSelectedButton() {
				return Buttons_Selected;
			}
			
			private RectF GetViewRect() {
				float Width = WorkSpace.Width*OwnerWidthFit;
				float X = (WorkSpace.Width-Width)/2.0F;
				return (new RectF(X,0, X+Width,DefaultHeight*WorkSpace.Reflector.metrics.density));
			}
			
			public void DrawOnCanvas(Canvas canvas, RectF rect) {
				ButtonTextSize = rect.height()*0.74F;
				//.
				int Cnt = Buttons.size();
				float ButtonWidth = rect.width()/Cnt;
				for (int I = 0; I < Cnt; I++) {
					RectF ButtonRect = new RectF(rect.left+I*ButtonWidth, rect.top, rect.left+(I+1)*ButtonWidth, rect.bottom);
					TButton Button = Buttons.get(I);
					if (Button != Buttons_Selected) 
						Button.DrawOnCanvas(canvas, ButtonRect, BackgroundColor,BackgroundTransparency);
					else
						Button.DrawOnCanvas(canvas, ButtonRect, SelectedBackgroundColor,SelectedBackgroundTransparency);
				}
			}
			
			public void Draw(Canvas canvas) {
				DrawOnCanvas(canvas, GetViewRect());
			}			
			
			public TButton GetButtonAtPosition(double X, double Y) {
				RectF rect = GetViewRect();
				//.
				int Cnt = Buttons.size();
				float ButtonWidth = rect.width()/Cnt;
				for (int I = 0; I < Cnt; I++) {
					RectF ButtonRect = new RectF(rect.left+I*ButtonWidth, rect.top, rect.left+(I+1)*ButtonWidth, rect.bottom);
					if (ButtonRect.contains((float)X,(float)Y))
						return Buttons.get(I); //. ->
				}
				return null;
			}
		}
		
		private Paint paint = new Paint();
		private Paint transitionpaint = new Paint();
		private Paint DelimiterPaint;
		private Paint CenterMarkPaint = new Paint();
		private Bitmap BackgroundImage = null;
		private int CurrentImageID = 0;
		public TButtons Buttons = new TButtons(this);
		private TNavigationArrows NavigationArrows;
		public TDialogPanel DialogPanel = null; 
		private int UpdateTransitionFactor = 0;
		private Timer UpdateTransitionHandler = null;
		private TimerTask UpdateTransitionHandlerTask = null;
		//.
		private TXYCoord Pointer_Down_StartPos = new TXYCoord();
		private TXYCoord Pointer_LastPos = new TXYCoord();
		//.
		private TXYCoord Pointer1_Down_StartPos = new TXYCoord();
		private TXYCoord Pointer1_LastPos = new TXYCoord();
		//.
		private TXYCoord Pointer2_Down_StartPos = new TXYCoord(); 
		private TXYCoord Pointer2_LastPos = new TXYCoord();
		//.
		public int SelectShiftFactor = 10;
		//.
		public float ScalingZoneWidth;
		public float RotatingZoneWidth;

		public TWorkSpace(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public TWorkSpace(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TWorkSpace(Context context) {
			super(context);
		}

		@Override
		public void Initialize(TReflectorComponent pReflector) throws Exception {
			super.Initialize(pReflector);
			//.
			paint.setAntiAlias(false);
			paint.setDither(true);
			paint.setFilterBitmap(false);
			//.
			switch (Reflector.NavigationMode) {

			case NAVIGATION_MODE_NATIVE:
				DelimiterPaint = new Paint();
				DelimiterPaint.setColor(Color.RED);
				DelimiterPaint.setStrokeWidth(1.0F * Reflector.metrics.density);
				DelimiterPaint.setAlpha(160);
				break; // . >

			case NAVIGATION_MODE_ARROWS:
				NavigationArrows = new TNavigationArrows(this);
				break; // . >

			case NAVIGATION_MODE_MULTITOUCHING1:
				DelimiterPaint = new Paint();
				DelimiterPaint.setColor(Color.RED);
				DelimiterPaint.setStrokeWidth(1.0F * Reflector.metrics.density);
				DelimiterPaint.setAlpha(160);
				break; // . >

			}
			// .
			CenterMarkPaint.setColor(Color.RED);
			CenterMarkPaint.setStrokeWidth(2.0F * Reflector.metrics.density);
			CenterMarkPaint.setStyle(Paint.Style.STROKE);
			//.
			ScalingZoneWidth = 48.0F * Reflector.metrics.density;
			RotatingZoneWidth = 42.0F * Reflector.metrics.density;
		}

		@Override
		public void Finalize() {
			UpdateTransition_Stop();
			// .
			if (BackgroundImage != null)
				BackgroundImage.recycle();
			// .
			if (NavigationArrows != null) {
				NavigationArrows.Destroy();
				NavigationArrows = null;
			}
			//.
			super.Finalize();
		}

		public void DialogPanel_Set(TDialogPanel pDialogPanel) {
			DialogPanel = pDialogPanel;
		}
		
		public void DialogPanel_Clear() {
			DialogPanel = null;
		}
		
		public void Update(boolean flTransition) {
			if (flTransition)
				UpdateTransition_StartIfNotStarted();
			else {
				UpdateTransition_Stop();
				StartDraw();
			}
		}

		public void Update() {
			Update(false);
		}

		private synchronized void UpdateTransition_Start(boolean flCancel) {
			if (flCancel)
				UpdateTransition_Stop();
			else if (UpdateTransitionHandler != null)
				return; // . ->
			// .
			UpdateTransitionFactor = 0;
			// .
			UpdateTransitionHandlerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						synchronized (TWorkSpace.this) {
							UpdateTransitionFactor += UpdateTransitionStep;
							if (UpdateTransitionFactor >= 100) {
								UpdateTransition_Stop();
							}
						}
						// .
						TWorkSpace.this.StartDraw();
					} catch (Throwable E) {
						TGeoLogApplication.Log_WriteError(E);
					}
				}
			};
			// .
			UpdateTransitionHandler = new Timer();
			UpdateTransitionHandler.schedule(UpdateTransitionHandlerTask,
					UpdateTransitionInterval, UpdateTransitionInterval);
		}

		@SuppressWarnings("unused")
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

		@SuppressWarnings("unused")
		private synchronized boolean UpdateTransition_IsActive() {
			return (UpdateTransitionHandlerTask != null);
		}

		private synchronized int UpdateTransition_GetFactor() {
			return UpdateTransitionFactor;
		}

		@Override
		protected void DoOnSizeChanged(int w, int h) {
			super.DoOnSizeChanged(w,h);
			//.
			if ((w*h) <= 0)
				return; // . ->
			// .
			if (Reflector == null)
				return; // . ->
			// .
			if (BackgroundImage != null)
				BackgroundImage.recycle();
			BackgroundImage = BackgroundImage_ReCreate(Width, Height);
			// .
			if (NavigationArrows != null)
				NavigationArrows.Prepare(w, h);
			// . align buttons
			float YStep = ((h+0.0F)/Buttons.GetValidItemCount(BUTTONS_GROUP_LEFT));
			float Y = 0;
			// .
			float XStep = YStep;
			switch (Reflector.ButtonsStyle) {
			
			case BUTTONS_STYLE_BRIEF:
				int LeftGroupButtonStyle = TButton.STYLE_ELLIPSE;
				float XS = Width*LeftGroupButtonXFitFactorForBriefMode;
				if (XS < XStep) {
					XStep = XS;
					LeftGroupButtonStyle = TButton.STYLE_RECTANGLE;
				}
				//.
				for (int I = 0; I < Buttons.Items.length; I++)
					if (Buttons.Items[I] != null) {
						Buttons.Items[I].Width = XStep;
						if (Buttons.Items[I].GroupID == BUTTONS_GROUP_LEFT)
							Buttons.Items[I].Style = LeftGroupButtonStyle;
					}
				break; //. >
				
			case BUTTONS_STYLE_NORMAL:
				LeftGroupButtonStyle = TButton.STYLE_RECTANGLE;
				XS = Width*LeftGroupButtonXFitFactorForNormalMode;
				//.
				for (int I = 0; I < Buttons.Items.length; I++)
					if (Buttons.Items[I] != null) 
						if (Buttons.Items[I].GroupID == BUTTONS_GROUP_LEFT) {
							Buttons.Items[I].Style = LeftGroupButtonStyle;
							Buttons.Items[I].Width = XS;
						}
						else 
							Buttons.Items[I].Width = XStep;
					
				break; //. >
			}
			//.
			Buttons.Items[BUTTON_URLS].Top = Y+(1.0F*Reflector.metrics.density);
			Buttons.Items[BUTTON_URLS].Height = YStep-(1.0F*Reflector.metrics.density);
			Y += YStep;
			Buttons.Items[BUTTON_UPDATE].Top = Y;
			Buttons.Items[BUTTON_UPDATE].Height = YStep;
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
			Buttons.Items[BUTTON_CREATINGGALLERY].Top = Y;
			Buttons.Items[BUTTON_CREATINGGALLERY].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_EDITOR].Top = Y;
			Buttons.Items[BUTTON_EDITOR].Height = YStep;
			Y += YStep;
			Buttons.Items[BUTTON_USERSEARCH].Top = Y;
			Buttons.Items[BUTTON_USERSEARCH].Height = YStep;
			Y += YStep;
			if (((Reflector.Reason == REASON_UNKNOWN) || (Reflector.Reason == REASON_MAIN) || (Reflector.Reason == REASON_USERPROFILECHANGED)) && !Reflector.Configuration.GeoLog_flHide) {
				Buttons.Items[BUTTON_TRACKER].Top = Y;
				Buttons.Items[BUTTON_TRACKER].Height = YStep-(1.0F*Reflector.metrics.density);
				Y += YStep;
			}
			//.
			RotatingZoneWidth = XStep;
			//.
			Buttons.Items[BUTTON_COMPASS].Left = Width-RotatingZoneWidth+2.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Top = 2.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Width = RotatingZoneWidth-4.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_COMPASS].Height = RotatingZoneWidth-4.0F*Reflector.metrics.density;
			//.
			Buttons.Items[BUTTON_MYUSERPANEL].Left = Width-RotatingZoneWidth+2.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_MYUSERPANEL].Width = RotatingZoneWidth-4.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_MYUSERPANEL].Height = RotatingZoneWidth-4.0F*Reflector.metrics.density;
			Buttons.Items[BUTTON_MYUSERPANEL].Top = Height-Buttons.Items[BUTTON_MYUSERPANEL].Height-2.0F*Reflector.metrics.density;
			//.
			if (Reflector.SpaceImage != null)
				Reflector.SpaceImage.DoOnResize(Width, Height);
			//.
			Reflector.ReflectionWindow.Resize(Width, Height);
			//. Reflector.ResetNavigationAndUpdateCurrentSpaceImage();
			//.
			Reflector.PostStartUpdatingSpaceImage();
		}

		private void DrawOnCanvas(Canvas canvas, int TransitionFactor,
				boolean flDrawBackground, boolean flDrawImage,
				boolean flDrawHints, boolean flDrawObjectTracks,
				boolean flDrawSelectedObject, boolean flDrawEditingObject, boolean flDrawGeoMonitorObjects,
				boolean flDrawControls, TCanceller Canceller,
				TTimeLimit TimeLimit) {
			try {
				// . get window
				TReflectionWindowStruc RW = Reflector.ReflectionWindow
						.GetWindow();
				RW.MultiplyByMatrix(Reflector.ReflectionWindowTransformatrix);
				// . draw background
				if (flDrawBackground)
					canvas.drawBitmap(BackgroundImage, 0, 0, paint);
				// .
				if (flDrawImage)
					switch (Reflector.GetViewMode()) {

					case VIEWMODE_REFLECTIONS:
						Reflector.SpaceReflections
								.ReflectionWindow_DrawOnCanvas(RW, canvas,
										paint);
						//.
						if (Reflector.SpaceImage != null)
							synchronized (Reflector.SpaceImage) {
								if (Reflector.SpaceImage.flResultBitmap) {
									canvas.save();
									try {
										canvas.concat(Reflector.SpaceImage.ResultBitmapTransformatrix);
										canvas.drawBitmap(
												Reflector.SpaceImage.ResultBitmap,
												0, 0, paint);
									} finally {
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
											SX = X
													* Reflector.SpaceImage.SegmentWidth;
											for (int Y = 0; Y < Reflector.SpaceImage.DivY; Y++) {
												Segment = Reflector.SpaceImage.Segments[X][Y];
												if (Segment != null)
													canvas.drawBitmap(
															Segment,
															SX,
															Y
																	* Reflector.SpaceImage.SegmentHeight,
															paint);
											}
										}
									} finally {
										canvas.restore();
									}
								}
							}
						break; // . >

					case VIEWMODE_TILES:
						// .
						if (TransitionFactor == 0) {
							CurrentImageID++;
							// . draw image
							try {
								Reflector.SpaceTileImagery
										.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(
												RW, CurrentImageID, canvas,
												paint, null, Canceller,
												TimeLimit);
							} catch (TTimeLimit.TimeIsExpiredException TEE) {
							}
							// .
							if (Reflector.SpaceTileImagery_flUseResultTilesSet) {
								float ReflectorSpaceImageResultBitmapTransformatrixScale = 1.0F;
								if (Reflector.SpaceImage != null)
									synchronized (Reflector.SpaceImage) {
										ReflectorSpaceImageResultBitmapTransformatrixScale = Reflector.SpaceImage.ResultBitmapTransformatrix
												.mapRadius(1.0F);
									}
								if (ReflectorSpaceImageResultBitmapTransformatrixScale < 1.0F) {
									Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_ResultLevelTileContainer_DrawOnCanvas(RW, CurrentImageID, canvas, paint,null, Canceller, TimeLimit);
									Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_PartialResultLevelTileContainer_DrawOnCanvas(RW, CurrentImageID, canvas, paint,null, Canceller, TimeLimit);
								}
							}
						} else {
							transitionpaint
									.setAlpha((int) (255.0 * TransitionFactor / 100.0));
							// . draw transition image
							try {
								Reflector.SpaceTileImagery
										.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(
												RW, CurrentImageID, canvas,
												paint, transitionpaint,
												Canceller, TimeLimit);
							} catch (TTimeLimit.TimeIsExpiredException TEE) {
							}
							// .
							if (Reflector.SpaceTileImagery_flUseResultTilesSet) {
								float ReflectorSpaceImageResultBitmapTransformatrixScale = 1.0F;
								if (Reflector.SpaceImage != null)
									synchronized (Reflector.SpaceImage) {
										ReflectorSpaceImageResultBitmapTransformatrixScale = Reflector.SpaceImage.ResultBitmapTransformatrix
												.mapRadius(1.0F);
									}
								if (ReflectorSpaceImageResultBitmapTransformatrixScale < 1.0F) {
									Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_ResultLevelTileContainer_DrawOnCanvas(RW, CurrentImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);
									Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_PartialResultLevelTileContainer_DrawOnCanvas(RW, CurrentImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);
								}
							}
						}
						break; // . >
					}
				// . draw space image hints
				if (flDrawHints) {
					if (Reflector.Configuration.ReflectionWindow_flShowHints)
						Reflector.SpaceHints.DrawOnCanvas(RW,
								Reflector.DynamicHintVisibleFactor, canvas);
				}
				// .
				// . draw tracks
				if (flDrawObjectTracks)
					Reflector.ObjectTracks.DrawOnCanvas(RW, canvas);
				//. draw selected object
				if (flDrawSelectedObject) {
					if (Reflector.SelectedObj != null)
						Reflector.SelectedObj.DrawOnCanvas(RW, canvas);
				}
				//.
				if (flDrawEditingObject) {
					if (Reflector.EditingObj != null)
						Reflector.EditingObj.DrawOnCanvas(RW, canvas);
				}
				// . draw monitor objects
				if (flDrawGeoMonitorObjects)
					Reflector.CoGeoMonitorObjects.DrawOnCanvas(RW, canvas);
				// . draw with NavigationTransformatrix
				/*
				 * synchronized (Reflector.NavigationTransformatrix) {
				 * canvas.save(); try {
				 * canvas.concat(Reflector.NavigationTransformatrix); //.
				 * .................. } finally { canvas.restore(); } }
				 */
				// .
				// . draw controls
				if (!Reflector.WorkSpaceOverlay_Active()  && flDrawControls) {
					switch (Reflector.NavigationMode) {

					case NAVIGATION_MODE_NATIVE:
						// . draw navigation delimiters
						float X = Width - RotatingZoneWidth;
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						X = Width
								- (RotatingZoneWidth + ScalingZoneWidth);
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						break; // . >

					case NAVIGATION_MODE_ARROWS:
						NavigationArrows.Draw(canvas);
						break; // . >

					case NAVIGATION_MODE_MULTITOUCHING1:
						// . draw rotation delimiter
						X = Width - RotatingZoneWidth;
						canvas.drawLine(X, 0, X, Height, DelimiterPaint);
						break; // . >
					}
					//. draw buttons
					if (Buttons != null)
						Buttons.Draw(canvas);
					//.
					ShowTitle(canvas);
					ShowStatus(canvas);
					//. draw dialog panel
					if (DialogPanel != null)
						DialogPanel.Draw(canvas);
					//.
					ShowCenterMark(canvas);
				}
			} catch (CancelException CE) {
			} catch (TimeIsExpiredException TES) {
			} catch (Throwable TE) {
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null)
					Tracker.GeoLog.Log.WriteError(
							"UserAgent.Reflector.WorkSpace.DrawOnCanvas()",
							TE.getMessage());
			}
		}

		@Override
		protected void DoOnDraw(Canvas canvas, TCanceller Canceller, TTimeLimit TimeLimit) {
			if (Reflector == null)
				return; // . ->
			// .
			int TransitionFactor = UpdateTransition_GetFactor();
			DrawOnCanvas(canvas, TransitionFactor, true, true, true, true,
					true, true, true, true, Canceller, TimeLimit);
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
			if (Reflector.Reason == REASON_MONITORGEOLOCATION) {
				R <<= 2;
				canvas.drawCircle(X,Y, R, CenterMarkPaint);
			}
		}

		@SuppressWarnings("unused")
		private void ShowLogo(Canvas canvas) {
			Paint _paint = new Paint();
			String S = "   GeoLog  " + TReflector.ProgramVersion + "   ";
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
			if (!(Reflector.ReflectionWindow.ActualityInterval
					.IsEndTimestampInfinite() || Reflector.ReflectionWindow.ActualityInterval
					.IsEndTimestampMax())) {
				OleDate TS = new OleDate(OleDate.UTCToLocalTime(Reflector.ReflectionWindow.ActualityInterval.GetEndTimestamp()));
				String TSS = Integer.toString(TS.year) + "/"
						+ Integer.toString(TS.month) + "/"
						+ Integer.toString(TS.date) + " "
						+ Integer.toString(TS.hrs) + ":"
						+ Integer.toString(TS.min) + ":"
						+ Integer.toString(TS.sec);
				S = getContext().getString(R.string.STimestamp) + TSS;
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
			// .
			TSpaceImageUpdating SpaceImageUpdating = Reflector.GetSpaceImageUpdating();
			if ((SpaceImageUpdating != null) && !SpaceImageUpdating.flDone) {
				S = getContext().getString(R.string.SImageUpdating);
				ProgressSummaryValue = SpaceImageUpdating.ImageProgressor
						.GetSummaryValue();
				ProgressValue = SpaceImageUpdating.ImageProgressor
						.GetProgressValue();
				ProgressPercentage = SpaceImageUpdating.ImageProgressor
						.ProgressPercentage();
			} else if (Reflector.flOffline)
				S = getContext().getString(R.string.SOfflineMode);
			// .
			if (S == null)
				return; // . ->
			if (ProgressPercentage > 0)
				S += Integer.toString(ProgressPercentage) + "%" + " ("
						+ Integer.toString(ProgressValue) + "/"
						+ Integer.toString(ProgressSummaryValue) + ") ";
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
				float PW = W * ProgressPercentage / 100.0F;
				canvas.drawRect(Left, Top, Left + PW, Top + H, ShowStatus_Paint);
			}
			ShowStatus_Paint.setStyle(Paint.Style.FILL);
			ShowStatus_Paint.setColor(Color.BLACK);
			canvas.drawText(S, Left + 1, Top + H - 4 + 1, ShowStatus_Paint);
			ShowStatus_Paint.setColor(Color.RED);
			canvas.drawText(S, Left, Top + H - 4, ShowStatus_Paint);
		}
		
		private void DoOnPointerClick(double X, double Y) {
			try {
				Reflector.SelectedObj_CancelProcessing();
				// .
				Reflector.SelectedObj_Clear();
				Reflector.SpaceHints.UnSelectAll();
				Reflector.CoGeoMonitorObjects.UnSelectAll();
				// .
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				// .
				boolean flSelected = false;
				// .
				int idxCoGeoMonitorObject = Reflector.CoGeoMonitorObjects.Select(RW,
						(float) X, (float) Y);
				if (idxCoGeoMonitorObject != -1) {
					flSelected = true;
					Draw();
					// .
					Intent intent = new Intent(Reflector.context, TCoGeoMonitorObjectPanel.class);
					intent.putExtra("ComponentID", Reflector.ID);
	            	intent.putExtra("ParametersType", TCoGeoMonitorObjectPanel.PARAMETERS_TYPE_OIDX);
					intent.putExtra("ObjectIndex", idxCoGeoMonitorObject);
					Reflector.ParentActivity.startActivity(intent);
				}
				// .
				if (!flSelected) {
					TSpaceHint Hint = Reflector.SpaceHints.Select(RW, Reflector.VisibleFactor,
							(float) X, (float) Y);
					if (Hint != null) {
						flSelected = true;
						Draw();
						// .
						if (Reflector.SelectedComponentTypedDataFileNamesLoading != null)
							Reflector.SelectedComponentTypedDataFileNamesLoading.Cancel();
						if (Hint.InfoComponent_ID != 0)
							Reflector.SelectedComponentTypedDataFileNamesLoading = Reflector.new TComponentTypedDataFileNamesLoading(
									Reflector, Hint.InfoComponent_Type,
									Hint.InfoComponent_ID,
									MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED);
						else
							Toast.makeText(Reflector.context, Hint.InfoString, Toast.LENGTH_LONG)
									.show();
					}
				}
				// .
				if (Reflector.ObjectAtPositionGetting != null) {
					Reflector.ObjectAtPositionGetting.Cancel();
					Reflector.ObjectAtPositionGetting = null;
				}
				if (!flSelected)
					Reflector.ObjectAtPositionGetting = Reflector.ReflectionWindow.new TObjectAtPositionGetting(
							Reflector.ReflectionWindow, X, Y, true, MESSAGE_SELECTEDOBJ_SET);
			} catch (Exception E) {
				Toast.makeText(
						Reflector.context,
						Reflector.context.getString(R.string.SErrorOfGettingObjectByCurrentPosition)
								+ E.getMessage(), Toast.LENGTH_SHORT).show();
				return; // . ->
			}
		}

		@Override
		protected void Pointer0_Down(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			try {
				if (DialogPanel != null) {
					TDialogPanel.TButton Button = DialogPanel.GetButtonAtPosition(X,Y);
					DialogPanel.SetSelectedButton(Button);
					if (Button != null) 
						return; // . ->
				}
				//.
				if ((Reflector.EditingObj != null) && (Reflector.EditingObj.CheckEditingMode(X,Y) != TEditableObj.EDITINGMODE_NONE)) {
					Draw();
					return; // . ->
				}
				//.
				int idxButton = Buttons.GetItemAt(X, Y);
				if (idxButton != -1) {
					Buttons.SetDownButton(idxButton);
					return; // . ->
				}
				// .
				Reflector.ReflectionWindowTransformatrix.reset();
				// .
				switch (Reflector.NavigationMode) {

				case NAVIGATION_MODE_NATIVE:
					if (X < (Width - (RotatingZoneWidth + ScalingZoneWidth)))
						Reflector.NavigationType = NAVIGATION_TYPE_MOVING;
					else if (X < (Width - RotatingZoneWidth))
						Reflector.NavigationType = NAVIGATION_TYPE_SCALING;
					else if (X >= (Width - RotatingZoneWidth))
						Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					else
						Reflector.NavigationType = NAVIGATION_TYPE_NONE;
					break; // . >

				case NAVIGATION_MODE_ARROWS:
					TWorkSpace.TNavigationArrows.TArrow Arrow = NavigationArrows
							.DoOnPointerDown(X, Y);
					if (Arrow == null)
						Reflector.NavigationType = NAVIGATION_TYPE_MOVING;
					break; // . >

				case NAVIGATION_MODE_MULTITOUCHING:
					Reflector.NavigationType = NAVIGATION_TYPE_MOVING;
					break; // . >

				case NAVIGATION_MODE_MULTITOUCHING1:
					if (X >= (Width - RotatingZoneWidth))
						Reflector.NavigationType = NAVIGATION_TYPE_ROTATING;
					else
						Reflector.NavigationType = NAVIGATION_TYPE_MOVING;
					break; // . >
				}
			}
			finally {
				Pointer_Down_StartPos.X = X;
				Pointer_Down_StartPos.Y = Y;
				Pointer_LastPos.X = X;
				Pointer_LastPos.Y = Y;
			}
			//.
			Reflector.SelectedObj_CancelProcessing();
			//.
			if ((Reflector._MediaPlayer != null) && Reflector._MediaPlayer.isPlaying())
				Reflector._MediaPlayer.stop();
		}

		@Override
		protected void Pointer0_Up(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			//.
			if (DialogPanel != null) {
				TDialogPanel.TButton Button = DialogPanel.GetButtonAtPosition(X,Y);
				TDialogPanel.TButton SelectedButton = DialogPanel.GetSelectedButton();
				if (Button == SelectedButton) {
					if (Button != null) {
						try {
							Button.Click();
						}
						finally {
							if (DialogPanel != null) 
								DialogPanel.ClearSelectedButton();
						}
						return; // . ->
					}
				}
				else
					if (SelectedButton != null) { 
						DialogPanel.ClearSelectedButton();
						return; // . ->
					}
			}
			//.
			if ((Reflector.EditingObj != null) && Reflector.EditingObj.IsEditing()) {
				Reflector.EditingObj.StopEditing();
				Draw();
				return; // . ->
			}
			//.
			int idxDownButton = Buttons.DownButtonIndex;
			if (idxDownButton != -1) {
				boolean flPlayGeoLog = ((System.currentTimeMillis() - Buttons.DownButtons_Time) > 7000);
				Buttons.ClearDownButton();
				// .
				int idxButton = Buttons.GetItemAt(X, Y);
				if ((idxButton == idxDownButton)
						&& (Buttons.Items[idxButton].flEnabled)) {
					switch (idxButton) {
					
					case BUTTON_URLS:
						Intent intent = new Intent(Reflector.context, TReflectorURLListPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						intent.putExtra("URLListFolder", URLListFolder);
						Reflector.ParentActivity.startActivity(intent);
						//.
						break; //. >

					case BUTTON_UPDATE:
						Reflector.StartUpdatingCurrentSpaceImage();
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
								intent = new Intent();
								intent.setDataAndType(Uri.fromFile(GF), "audio/*");
								intent.setAction(android.content.Intent.ACTION_VIEW);
								Reflector.ParentActivity.startActivityForResult(intent, 0);
							}
						}
						break; // . >

					case BUTTON_SHOWREFLECTIONPARAMETERS:
						intent = Reflector.ReflectionWindow.CreateConfigurationPanel(Reflector.ParentActivity);
						Reflector.ParentActivity.startActivity(intent);
						break; // . >

					/*
					 * ///? case BUTTON_SUPERLAYS:
					 * ReflectionWindow.getLays().SuperLays
					 * .CreateSelectorPanel(this).show(); break; //. >
					 */

					case BUTTON_OBJECTS:
						intent = new Intent(Reflector.context, TReflectorCoGeoMonitorObjectsPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						Reflector.ParentActivity.startActivity(intent);
						break; // . >

					case BUTTON_USERSEARCH:
						intent = new Intent(Reflector.context, TUserListPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						intent.putExtra("Mode", TUserListComponent.MODE_UNKNOWN);
						Reflector.ParentActivity.startActivityForResult(intent, REQUEST_OPEN_USERSEARCH);
						//.
						break; // . >

					case BUTTON_TRACKER:
						intent = new Intent(Reflector.context, TTrackerPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						Reflector.ParentActivity.startActivityForResult(intent, REQUEST_SHOW_TRACKER);
						//.
						break; // . >

					case BUTTON_MAPOBJECTSEARCH:
						intent = new Intent(Reflector.context, TReflectorMapObjectsPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						Reflector.ParentActivity.startActivity(intent);
						//.
						break; // . >

					case BUTTON_ELECTEDPLACES:
						intent = new Intent(Reflector.context, TReflectorElectedPlacesPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						Reflector.ParentActivity.startActivity(intent);
						//.
						break; // . >

					case BUTTON_PREVWINDOW:
						Reflector.ShowPrevWindow();
						// .
						break; // . >

					case BUTTON_CREATINGGALLERY:
						try {
							Reflector.ObjectCreatingGallery_Start();
						} catch (Exception E) {
							Toast.makeText(Reflector.context, E.getMessage(), Toast.LENGTH_LONG).show();
						}
						//.
						break; // . >

					case BUTTON_EDITOR:
						if (Reflector.GetViewMode() == VIEWMODE_TILES)
							Reflector.ShowEditor();
						else
							Toast.makeText(Reflector.context,
									R.string.SImageEditingAvailableInTileModeOnly,
									Toast.LENGTH_LONG).show();
						//.
						break; // . >

					case BUTTON_COMPASS:
						Reflector.new TReflectionWindowToNorthPoleAlignning();
						//.
						break; // . >

					case BUTTON_MYUSERPANEL:
						intent = new Intent(Reflector.context, TMyUserPanel.class);
						intent.putExtra("ComponentID", Reflector.ID);
						Reflector.ParentActivity.startActivity(intent);
						//.
						break; // . >
					}
				}
				return; // . ->
			}
			// .
			try {
				if ((NavigationArrows != null)
						&& (NavigationArrows.DownArrow != null))
					NavigationArrows.Release();
				else {
					double dX = X - Pointer_Down_StartPos.X;
					double dY = Y - Pointer_Down_StartPos.Y;
					// .
					if ((Math.abs(dX) < SelectShiftFactor)
							&& (Math.abs(dY) < SelectShiftFactor)) {
						Reflector.NavigationType = NAVIGATION_TYPE_NONE;
						DoOnPointerClick(Pointer_Down_StartPos.X,
								Pointer_Down_StartPos.Y);
						return; // . ->
					}
					;
				}
				// .
				if (Reflector.NavigationType != NAVIGATION_TYPE_NONE) {
					Reflector.NavigationType = NAVIGATION_TYPE_NONE;
					// .
					Reflector.ReflectionWindow
							.MultiplyReflectionByMatrix(Reflector.ReflectionWindowTransformatrix);
					Reflector.ReflectionWindowTransformatrix.reset();
					//.
					///? if (SpaceImage != null)
					///?	SpaceImage.ResetResultBitmap();
					// .
					switch (Reflector.NavigationMode) {

					case NAVIGATION_MODE_NATIVE:
					case NAVIGATION_MODE_MULTITOUCHING:
					case NAVIGATION_MODE_MULTITOUCHING1:
						Reflector._SpaceImageCaching.Stop();
						break; // . >
					}
					// .
					// . ResetNavigationTransformatrix();
				}
			} finally {
				Reflector.StartUpdatingSpaceImage(1000);
			}
		}

		@Override
		protected void Pointer0_Move(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			//.
			try {
				if (DialogPanel != null) {
					TDialogPanel.TButton Button = DialogPanel.GetButtonAtPosition(X,Y);
					TDialogPanel.TButton SelectedButton = DialogPanel.GetSelectedButton();
					if (SelectedButton != null) {
						if (Button != SelectedButton)  
							DialogPanel.ClearSelectedButton();
						return; // . ->
					}						
				}
				//.
				if ((Reflector.EditingObj != null) && Reflector.EditingObj.IsEditing()) {
					Reflector.EditingObj.ProcessEditingPoint(X,Y);
					Draw();
					return; // . ->
				}
				//.
				if ((NavigationArrows != null)
						&& (NavigationArrows.DownArrow != null)) {
					if (NavigationArrows.DownArrow != NavigationArrows
							.GetItemAt(X, Y)) {
						NavigationArrows.Release();
						// .
						NavigationArrows.DoOnPointerDown(X, Y);
					}
					return; // . ->
				}
				//.
				if (Buttons.DownButtonIndex != -1) {
					int idxButton = Buttons.GetItemAt(X, Y);
					if (idxButton != Buttons.DownButtonIndex)
						Buttons.ClearDownButton();
					return; // . ->
				}
				//.
				/*
				 * ///? if ((NavigationType == TNavigationItem.NAVIGATIONTYPE_NONE) &&
				 * ((Math.abs(X-Pointer_Down_StartPos.X) < SelectShiftFactor) &&
				 * (Math.abs(Y-Pointer_Down_StartPos.Y) < SelectShiftFactor))) return ;
				 * //. ->
				 */
				// .
				Reflector.CancelUpdatingSpaceImage();
				//.
				double dX = X - Pointer_LastPos.X;
				double dY = Y - Pointer_LastPos.Y;
				// .
				switch (Reflector.NavigationType) {

				case NAVIGATION_TYPE_MOVING: {
					// . NavigationTransformatrix.postTranslate((float) dX, (float) dY);
					if (Reflector.SpaceImage != null)
						synchronized (Reflector.SpaceImage) {
							Reflector.SpaceImage.ResultBitmapTransformatrix.postTranslate((float) dX,
									(float) dY);
							if (Reflector.SpaceImage.flSegments)
								Reflector.SpaceImage.SegmentsTransformatrix.postTranslate((float) dX,
										(float) dY);
						}
					Reflector.ReflectionWindowTransformatrix.postTranslate(-(float) dX,
							-(float) dY);
					break; // . >
				}

				case NAVIGATION_TYPE_SCALING: {
					double Scale = (1.0 + Reflector.ScaleCoef * dY / Reflector.ReflectionWindow.getHeight());
					// . NavigationTransformatrix.postScale((float) Scale, (float)
					// Scale, ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (Reflector.SpaceImage != null)
						synchronized (Reflector.SpaceImage) {
							Reflector.SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale,
									(float) Scale, Reflector.ReflectionWindow.Xmd,
									Reflector.ReflectionWindow.Ymd);
							if (Reflector.SpaceImage.flSegments)
								Reflector.SpaceImage.SegmentsTransformatrix.postScale((float) Scale,
										(float) Scale, Reflector.ReflectionWindow.Xmd,
										Reflector.ReflectionWindow.Ymd);
						}
					Reflector.ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
							(float) (1.0 / Scale), Reflector.ReflectionWindow.Xmd,
							Reflector.ReflectionWindow.Ymd);
					break; // . >
				}

				case NAVIGATION_TYPE_ROTATING: {
					double dX0, dY0;
					dX0 = (Pointer_LastPos.X - Reflector.ReflectionWindow.Xmd);
					dY0 = (Pointer_LastPos.Y - Reflector.ReflectionWindow.Ymd);
					double dX1, dY1;
					dX1 = (X - Reflector.ReflectionWindow.Xmd);
					dY1 = (Y - Reflector.ReflectionWindow.Ymd);
					double Alpha = Math.atan(dY0 / dX0);
					double Betta = Math.atan(dY1 / dX1);
					double Gamma = -(Betta - Alpha);
					// .
					// . NavigationTransformatrix.postRotate((float) (-Gamma * 180.0 /
					// Math.PI), ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (Reflector.SpaceImage != null)
						synchronized (Reflector.SpaceImage) {
							Reflector.SpaceImage.ResultBitmapTransformatrix.postRotate(
									(float) (-Gamma * 180.0 / Math.PI),
									Reflector.ReflectionWindow.Xmd, Reflector.ReflectionWindow.Ymd);
							if (Reflector.SpaceImage.flSegments)
								Reflector.SpaceImage.SegmentsTransformatrix.postRotate(
										(float) (-Gamma * 180.0 / Math.PI),
										Reflector.ReflectionWindow.Xmd, Reflector.ReflectionWindow.Ymd);
						}
					Reflector.ReflectionWindowTransformatrix.postRotate(
							(float) (Gamma * 180.0 / Math.PI), Reflector.ReflectionWindow.Xmd,
							Reflector.ReflectionWindow.Ymd);
					break; // . >
				}

				case NAVIGATION_TYPE_SCALETRANSFORMATING: {
					dX = (Pointer_LastPos.X - Pointer1_LastPos.X);
					dY = (Pointer_LastPos.Y - Pointer1_LastPos.Y);
					double L0Qd = dX * dX + dY * dY;
					dX = (X - Pointer1_LastPos.X);
					dY = (Y - Pointer1_LastPos.Y);
					double LQd = dX * dX + dY * dY;
					if ((L0Qd == 0) || (LQd == 0))
						break; // . >
					double Scale = (LQd / L0Qd);
					// . NavigationTransformatrix.postScale((float) Scale, (float)
					// Scale, ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (Reflector.SpaceImage != null)
						synchronized (Reflector.SpaceImage) {
							Reflector.SpaceImage.ResultBitmapTransformatrix.postScale((float) Scale,
									(float) Scale, Reflector.ReflectionWindow.Xmd,
									Reflector.ReflectionWindow.Ymd);
							if (Reflector.SpaceImage.flSegments)
								Reflector.SpaceImage.SegmentsTransformatrix.postScale((float) Scale,
										(float) Scale, Reflector.ReflectionWindow.Xmd,
										Reflector.ReflectionWindow.Ymd);
						}
					Reflector.ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
							(float) (1.0 / Scale), Reflector.ReflectionWindow.Xmd,
							Reflector.ReflectionWindow.Ymd);
					break; // . >
				}
				}
				if (Reflector.NavigationType != NAVIGATION_TYPE_NONE) {
					///-- if (SpaceImage != null)
					///-- 	SpaceImage.ResetResultBitmap();
					Draw();
					switch (Reflector.NavigationMode) {

					case NAVIGATION_MODE_NATIVE:
					case NAVIGATION_MODE_MULTITOUCHING:
					case NAVIGATION_MODE_MULTITOUCHING1:
						Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
						break; // . >
					}
				}
			}
			finally {
				Pointer_LastPos.X = X;
				Pointer_LastPos.Y = Y;
			}
		}

		@Override
		protected void Pointer1_Down(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			// .
			switch (Reflector.NavigationMode) {

			case NAVIGATION_MODE_MULTITOUCHING1:
				if (Reflector.NavigationType != NAVIGATION_TYPE_ROTATING)
					Reflector.NavigationType = NAVIGATION_TYPE_SCALETRANSFORMATING;
				break; // . >
			}
			// .
			Pointer1_Down_StartPos.X = X;
			Pointer1_Down_StartPos.Y = Y;
			Pointer1_LastPos.X = X;
			Pointer1_LastPos.Y = Y;
		}

		@Override
		protected void Pointer1_Up(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			// .
			switch (Reflector.NavigationMode) {

			case NAVIGATION_MODE_MULTITOUCHING:
				if (Reflector.NavigationType != NAVIGATION_TYPE_NONE) {
					Reflector.NavigationType = NAVIGATION_TYPE_NONE;
					// .
					Reflector._SpaceImageCaching.Stop();
				}
				break; // . >

			case NAVIGATION_MODE_MULTITOUCHING1:
				if (Reflector.NavigationType != NAVIGATION_TYPE_NONE) {
					Reflector.NavigationType = NAVIGATION_TYPE_NONE;
					// .
					Reflector._SpaceImageCaching.Stop();
				}
				break; // . >
			}
		}

		@Override
		protected void Pointer1_Move(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			// .
			switch (Reflector.NavigationMode) {

			case NAVIGATION_MODE_MULTITOUCHING1:
				switch (Reflector.NavigationType) {

				case NAVIGATION_TYPE_SCALETRANSFORMATING: {
					double dX = (Pointer1_LastPos.X - Pointer_LastPos.X);
					double dY = (Pointer1_LastPos.Y - Pointer_LastPos.Y);
					double L0Qd = dX * dX + dY * dY;
					dX = (X - Pointer_LastPos.X);
					dY = (Y - Pointer_LastPos.Y);
					double LQd = dX * dX + dY * dY;
					if ((L0Qd == 0) || (LQd == 0))
						break; // . >
					double Scale = (LQd / L0Qd);
					// . NavigationTransformatrix.postScale((float) Scale, (float)
					// Scale, ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (Reflector.SpaceImage != null)
						synchronized (Reflector.SpaceImage) {
							Reflector.SpaceImage.ResultBitmapTransformatrix.postScale(
									(float) Scale, (float) Scale, Reflector.ReflectionWindow.Xmd,
									Reflector.ReflectionWindow.Ymd);
							if (Reflector.SpaceImage.flSegments)
								Reflector.SpaceImage.SegmentsTransformatrix.postScale(
										(float) Scale, (float) Scale,
										Reflector.ReflectionWindow.Xmd, Reflector.ReflectionWindow.Ymd);
						}
					Reflector.ReflectionWindowTransformatrix.postScale((float) (1.0 / Scale),
							(float) (1.0 / Scale), Reflector.ReflectionWindow.Xmd,
							Reflector.ReflectionWindow.Ymd);
					break; // . >
				}
				}
				break; // . >
			}
			if (Reflector.NavigationType != NAVIGATION_TYPE_NONE) {
				Draw();
				Reflector._SpaceImageCaching.TryToCacheCurrentWindow();
			}
			// .
			Pointer1_LastPos.X = X;
			Pointer1_LastPos.Y = Y;
		}

		@Override
		protected void Pointer2_Down(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			// .
			Pointer2_Down_StartPos.X = X;
			Pointer2_Down_StartPos.Y = Y;
			Pointer2_LastPos.X = X;
			Pointer2_LastPos.Y = Y;
		}

		@Override
		protected void Pointer2_Up(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
		}

		@Override
		protected void Pointer2_Move(double X, double Y) {
			if (!Reflector.flEnabled)
				return; // . ->
			// .
			Pointer2_LastPos.X = X;
			Pointer2_LastPos.Y = Y;
		}
	}

	public class TSpaceImageCaching extends TCancelableThread {

		public static final int CachingDelay = 100; // . ms
		public static final double CachingFactor = 1.0 / 8;

		private TReflectorComponent Reflector;
		// .
		private TReflectionWindowStruc ReflectionWindowToCache;
		private TAutoResetEvent StartSignal = new TAutoResetEvent();
		private TCanceller ProcessingCanceller = new TCanceller();

		public TSpaceImageCaching(TReflectorComponent pReflector) {
    		super();
    		//.
			Reflector = pReflector;
			// .
			_Thread = new Thread(this);
			_Thread.setPriority(Thread.MIN_PRIORITY);
			_Thread.start();
		}

		@Override
		public void CancelAndWait() throws InterruptedException {
			Cancel();
			StartSignal.Set();
			// .
			super.Wait();
		}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					StartSignal.WaitOne();
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					TReflectionWindowStruc RW;
					synchronized (this) {
						RW = ReflectionWindowToCache;
					}
					if (RW != null) {
						if (Reflector.Server.Info.flInitialized
								|| Reflector.flOffline) {
							ProcessingCanceller.Reset();
							try {
								TRWLevelTileContainer[] LevelTileContainers = null;
								//. caching
								switch (GetViewMode()) {
								case VIEWMODE_REFLECTIONS:
									Reflector.SpaceReflections.CheckInitialized();
									Canceller.Check();
									ProcessingCanceller.Check();
									//.
									Reflector.SpaceReflections.CacheReflectionsSimilarTo(RW);
									break; //. >

								case VIEWMODE_TILES:
									Reflector.SpaceTileImagery.CheckInitialized();
									Canceller.Check();
									ProcessingCanceller.Check();
									//.
									LevelTileContainers = Reflector.SpaceTileImagery.ActiveCompilationSet_GetLevelTileRange(RW);
									Canceller.Check();
									ProcessingCanceller.Check();
									//.
									Reflector.SpaceTileImagery.ActiveCompilationSet_RestoreTiles(LevelTileContainers,ProcessingCanceller, null);
									break; //. >
								}
								//.
								Thread.sleep(CachingDelay);
							} catch (CancelException CE) {
								if (CE.Canceller != ProcessingCanceller)
									throw CE; //. =>
							}
						}
					}
				}
			} catch (InterruptedException E) {
			} catch (CancelException CE) {
			} catch (NullPointerException NPE) {
				if (Reflector.flVisible)
					Reflector.MessageHandler.obtainMessage(
							TReflectorComponent.MESSAGE_SHOWEXCEPTION, NPE.getMessage())
							.sendToTarget();
			} catch (IOException E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(
						TReflectorComponent.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			} catch (Throwable E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(
						TReflectorComponent.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			}
		}

		public void Start(TReflectionWindowStruc RW) {
			ProcessingCanceller.Cancel();
			//.
			synchronized (this) {
				ReflectionWindowToCache = RW;
			}
			StartSignal.Set();
		}

		public void Start() {
			Start(Reflector.ReflectionWindow.GetWindow());
		}
		
		public void Stop() {
			ProcessingCanceller.Cancel();
			//.
			synchronized (this) {
				if (ReflectionWindowToCache != null) {
					ReflectionWindowToCache = null;
				}
			}
		}

		public void TryToCacheCurrentWindow() {
			TReflectionWindowStruc CacheRW;
			synchronized (this) {
				CacheRW = ReflectionWindowToCache;
			}
			// .
			TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
			RW.MultiplyByMatrix(Reflector.ReflectionWindowTransformatrix);
			// .
			if (CacheRW != null) {
				double Dmin = CacheRW.Container_Xmax - CacheRW.Container_Xmin;
				double D = CacheRW.Container_Ymax - CacheRW.Container_Ymin;
				if (D < Dmin)
					Dmin = D;
				D = RW.Container_Xmax - RW.Container_Xmin;
				if (D < Dmin)
					Dmin = D;
				D = RW.Container_Ymax - RW.Container_Ymin;
				if (D < Dmin)
					Dmin = D;
				Dmin = Dmin * CachingFactor;
				// .
				if (((int) (CacheRW.Container_Xmin / Dmin) == (int) (RW.Container_Xmin / Dmin))
						&& ((int) (CacheRW.Container_Xmax / Dmin) == (int) (RW.Container_Xmax / Dmin))
						&& ((int) (CacheRW.Container_Ymin / Dmin) == (int) (RW.Container_Ymin / Dmin))
						&& ((int) (CacheRW.Container_Ymax / Dmin) == (int) (RW.Container_Ymax / Dmin)))
					return; // . ->
				// . check if scaling up then don't cache
				if (RW.Container_S > CacheRW.Container_S) {
					ProcessingCanceller.Cancel();
					// .
					synchronized (this) {
						ReflectionWindowToCache = RW;
					}
					// .
					return; // . ->
				}
				// . start caching
				Start(RW);
			} else {
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
				if ((Percentage - this.Percentage) < PercentageDelta)
					return false; // . ->
				if (super.DoOnProgress(Percentage)) {
					Reflector.WorkSpace.Update(true);
					return true; // . ->
				} else
					return false; // . ->
			}
		}

		private class TCompilationTilesPreparing {

			private class TCompilationTilesPreparingThread implements Runnable {

				private int Index;
				private TTileServerProviderCompilation Compilation;
				private TRWLevelTileContainer LevelTileContainer;
				private TCanceller Canceller;
				private TUpdater Updater;
				private TProgressor Progressor;
				// .
				private Thread _Thread;
				private Throwable _ThreadException = null;

				public TCompilationTilesPreparingThread(
						int pIndex,
						TTileServerProviderCompilation pCompilation,
						TRWLevelTileContainer pLevelTileContainer,
						TCanceller pCanceller, TUpdater pUpdater,
						TProgressor pProgressor) {
					Index = pIndex;
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
						Result[Index] = Compilation.PrepareTiles(LevelTileContainer, Canceller,	Updater, Progressor);
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
			//.
			public TGetTilesResult[] Result = null;

			public TCompilationTilesPreparing(
					TTileServerProviderCompilation[] Compilation,
					TRWLevelTileContainer[] LevelTileContainers,
					TCanceller Canceller, TUpdater Updater,
					TProgressor Progressor) throws InterruptedException {
				Result = new TGetTilesResult[Compilation.length];
				Threads = new TCompilationTilesPreparingThread[Compilation.length];
				for (int I = Compilation.length - 1; I >= 0; I--)
					Threads[I] = new TCompilationTilesPreparingThread(
							I, Compilation[I], LevelTileContainers[I], Canceller,
							Updater, Progressor);
				// . start
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

			public TActiveCompilationUpLevelsTilesPreparing(TCancelableThread pParentThread, TRWLevelTileContainer[] pLevelTileContainers) {
	    		super(pParentThread);
	    		//.
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
				} catch (NullPointerException NPE) { //. avoid this on long operation
				} catch (Throwable E) {
					/* avoid on long operation String S = E.getMessage();
					if (S == null) S = E.getClass().getName();
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,TReflector.this.getString(R.string.SErrorOfGettingUpperLayers)+S).sendToTarget();
					 */
				}
			}
		}

		public class TActiveCompilationUpDownLevelsTilesRemoving extends TCancelableThread {

			private TRWLevelTileContainer[] LevelTileContainers;
			private TGetTilesResult[] GetTilesResult;
			
			public TActiveCompilationUpDownLevelsTilesRemoving(TRWLevelTileContainer[] pLevelTileContainers, TGetTilesResult[] pGetTilesResult) {
	    		super();
	    		//.
				LevelTileContainers = pLevelTileContainers;
				GetTilesResult = pGetTilesResult;
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
					Reflector.SpaceTileImagery.ActiveCompilationSet_RemoveUpDownLevelsTiles(LevelTileContainers, GetTilesResult, Canceller, null);
				} catch (InterruptedException E) {
				} catch (CancelException CE) {
				} catch (NullPointerException NPE) { //. avoid this on long operation
				} catch (Throwable E) {
					/* avoid on long operation String S = E.getMessage();
					if (S == null) S = E.getClass().getName();
					Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,TReflector.this.getString(R.string.SErrorOfGettingUpperLayers)+S).sendToTarget();
					 */
				}
			}
		}

		private TReflectorComponent Reflector;
		//.
		public boolean flDone = false;
		//.
		private int Delay;
		private boolean flUpdateProxySpace;
		// .
		private TImageUpdater ImageUpdater;
		public TImageProgressor ImageProgressor;
		//.
		private boolean 														flPrepareUpLevels = true;
		private boolean 														flRemoveUpDownLevels = true;

		public TSpaceImageUpdating(TReflectorComponent pReflector, int pDelay, boolean pflUpdateProxySpace) throws Exception {
    		super();
    		//.
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

		@Override
		public void run() {
			try {
				long LastTime = System.currentTimeMillis();
				// . check context storage device
				if (flCheckContextStorage) {
					flCheckContextStorage = false;
					// .
					if (!Reflector.User.Space.Context.Storage.CheckDeviceFillFactor()) {
						// . raise event
						Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY).sendToTarget();
					}
				}
				// . provide servers info
				try {
					Reflector.Server.CheckInitialized();
				} catch (IOException E) {
				}
				flOffline = (!Reflector.Server.flOnline);
				// .
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				TRWLevelTileContainer[] LevelTileContainers = null;
				//. pre-processing
				switch (GetViewMode()) {
				
				case VIEWMODE_REFLECTIONS:
					Reflector.SpaceReflections.CheckInitialized();
					//.
					Reflector.SpaceReflections.CacheReflectionsSimilarTo(RW);
					break; //. >

				case VIEWMODE_TILES:
					Reflector.SpaceTileImagery.CheckInitialized();
					//.
					LevelTileContainers = Reflector.SpaceTileImagery.ActiveCompilationSet_GetLevelTileRange(RW);
					Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED,	LevelTileContainers).sendToTarget();
					if (LevelTileContainers == null)
						return; // . ->
					//.
					Reflector.SpaceTileImagery.ActiveCompilationSet_ReleaseTiles(LevelTileContainers, Canceller);
					//.
					Reflector.SpaceTileImagery.ActiveCompilationSet_RestoreTiles(LevelTileContainers, Canceller, null);
					break; //. >
				}
				Reflector.WorkSpace.Update(true);
				//.
				if (!flOffline)
					Reflector.ReflectionWindow.CheckSpaceLays();
				//.
				if (Canceller.flCancel)
					return; //. ->
				while ((System.currentTimeMillis() - LastTime) < Delay) {
					Thread.sleep(100);
					if (Canceller.flCancel)
						return; // . ->
				}
				//.
				Reflector.SpaceHints.CheckInitialized();
				//.
				switch (GetViewMode()) {
				
				case VIEWMODE_REFLECTIONS:
					Reflector.SpaceHints.GetHintsFromServer(Reflector.ReflectionWindow, Canceller);
					Reflector.WorkSpace.Update(true);
					//.
					if (Reflector.SpaceImage != null)
						Reflector.SpaceImage.GetSegmentsFromServer(Reflector.ReflectionWindow, flUpdateProxySpace, Canceller, ImageUpdater, null);
					//. Done.
					flDone = true;
					//. raise event
					Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_UPDATESPACEIMAGE).sendToTarget();
					break; //. >

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
					TGetTilesResult[] PrepareTilesResult = null;
					try {
						switch (Reflector.SpaceTileImagery.ServerType) {

						case TTileImagery.SERVERTYPE_HTTPSERVER:
							// . sequential preparing in current thread
							PrepareTilesResult = Reflector.SpaceTileImagery.ActiveCompilationSet_PrepareTiles(LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
							break; // . >

						case TTileImagery.SERVERTYPE_DATASERVER:
							//. sequential preparing in current thread Reflector.SpaceTileImagery.ActiveCompilation_PrepareTiles(LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
							//. preparing in separate threads
							TCompilationTilesPreparing CompilationTilesPreparing = new TCompilationTilesPreparing(Reflector.SpaceTileImagery.ActiveCompilationSet(), LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
							CompilationTilesPreparing.WaitForFinish(); //. waiting for threads to be finished
							PrepareTilesResult = CompilationTilesPreparing.Result; 
							break; //. >

						default:
							//. sequential preparing in current thread
							PrepareTilesResult = Reflector.SpaceTileImagery.ActiveCompilationSet_PrepareTiles(LevelTileContainers, Canceller, ImageUpdater, ImageProgressor);
							break; //. >
						}
						//. prepare result composition
						if (Reflector.SpaceTileImagery_flUseResultTilesSet) {
							Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_SetResultLevelTileContainer(LevelTileContainers);
							Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_ClearPartialResultLevelTileContainer();
							//.
							if (Reflector.SpaceImage != null)
								Reflector.SpaceImage.ResultBitmap_Reset();
						}
					}
					catch (InterruptedException IE) {
						//. prepare partial result composition
						if (Reflector.SpaceTileImagery_flUseResultTilesSet) 
							Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_SetPartialResultLevelTileContainer(LevelTileContainers);
						//.
						throw IE; //. =>
					}
					catch (CancelException CE) {
						//. prepare partial result composition
						if (Reflector.SpaceTileImagery_flUseResultTilesSet) 
							Reflector.SpaceTileImagery.ActiveCompilationSet_ReflectionWindow_SetPartialResultLevelTileContainer(LevelTileContainers);
						//.
						throw CE; //. =>
					}
					//. Done.
					flDone = true;
					//. raise event
					if (!Canceller.flCancel) 
						Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_UPDATESPACEIMAGE).sendToTarget();
					//.
					TRWLevelTileContainer[] _LevelTileContainers = new TRWLevelTileContainer[LevelTileContainers.length];
					for (int I = 0; I < _LevelTileContainers.length; I++)
						if (LevelTileContainers[I] != null)
							_LevelTileContainers[I] = new TRWLevelTileContainer(LevelTileContainers[I]);
					//. remove up level's tiles if they are updated as new
					if (flRemoveUpDownLevels && (PrepareTilesResult != null)) 
						Reflector.SpaceTileImagery.ActiveCompilationSet_RemoveUpLevelsTiles(_LevelTileContainers, PrepareTilesResult, Canceller, null);
					Canceller.Check();
					//. prepare upper level's tiles
					if (flPrepareUpLevels) {
						//. flPrepareUpLevels = false;
						//.
						TActiveCompilationUpLevelsTilesPreparing _ActiveCompilationUpLevelsTilesPreparing = new TActiveCompilationUpLevelsTilesPreparing(this, _LevelTileContainers);
						_ActiveCompilationUpLevelsTilesPreparing.Start();
					}
					break; // . >
				}
				//. supply hints with images
				int SupplyCount = 25;
				for (int I = 0; I < SupplyCount; I++) {
					int RC = Reflector.SpaceHints.SupplyHintsWithImageDataFiles(Canceller);
					if (RC == TSpaceHints.SHWIDF_RESULT_NOITEMTOSUPPLY)
						break; //. >
					Reflector.WorkSpace.Update(true);
					if (RC == TSpaceHints.SHWIDF_RESULT_SUPPLIED)
						break; //. >
					//.
					if (Canceller.flCancel)
						return; //. ->
				}
				
			} catch (InterruptedException E) {
			} catch (CancelException CE) {
			} catch (NullPointerException NPE) {
				if (Reflector.flVisible)
					Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_SHOWEXCEPTION, NPE.getMessage()).sendToTarget();
			} catch (IOException E) {
				if (Reflector.Reflection_FirstTryCount > 0) {
					Reflector.Reflection_FirstTryCount--;
					//. wait a moment
					try {
						Thread.sleep(1000 * 2);
					} catch (Exception Ex) {
						return; // . ->
					}
					//. try to update an image again
					Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_STARTUPDATESPACEIMAGE).sendToTarget();
				} else {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_SHOWEXCEPTION, Reflector.context.getString(R.string.SErrorOfUpdatingImage)+S).sendToTarget();
				}
			} catch (Throwable E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Reflector.MessageHandler.obtainMessage(TReflectorComponent.MESSAGE_SHOWEXCEPTION, S).sendToTarget();
			}
		}
	}

	private class TSpaceObjOwnerTypedDataFileNamesLoading extends
			TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflectorComponent Reflector;
		// .
		private int OnStartDelay;
		private TOwnerSpaceObj OwnerSpaceObj;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TSpaceObjOwnerTypedDataFileNamesLoading(TReflectorComponent pReflector,
				TOwnerSpaceObj pOwnerSpaceObj, int pOnStartDelay, int pOnCompletionMessage) {
    		super();
    		//.
			Reflector = pReflector;
			// .
			OnStartDelay = pOnStartDelay;
			OwnerSpaceObj = pOwnerSpaceObj;
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
						+ "/" + Long.toString(User.UserID);
				String URL2 = "Functionality" + "/"
						+ "VisualizationOwnerDataDocument.dat";
				// . add command parameters
				int WithComponentsFlag = 1;
				URL2 = URL2
						+ "?"
						+ "1"/* command version */
						+ ","
						+ Long.toString(OwnerSpaceObj.ptrObj)
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
								OwnerSpaceObj.OwnerTypedDataFiles = null;
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
											Reflector.context
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
							int Idx = 0;
							OwnerSpaceObj.OwnerType = TDataConverter
									.ConvertLEByteArrayToInt32(Data, Idx);
							Idx += 4;
							OwnerSpaceObj.OwnerID = TDataConverter
									.ConvertLEByteArrayToInt64(Data, Idx);
							Idx += 8; // . ID: Int64
							OwnerSpaceObj.OwnerCoType = TDataConverter
									.ConvertLEByteArrayToInt32(Data, Idx);
							Idx += 4;
							if (Data.length > Idx) {
								OwnerSpaceObj.OwnerTypedDataFiles = new TComponentTypedDataFiles(
										Reflector.context,
										SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
								OwnerSpaceObj.OwnerTypedDataFiles.FromByteArrayV0(
										Data, Idx);
							} else {
								OwnerSpaceObj.OwnerTypedDataFiles = null;
								return; // . ->
							}
							// .
							Reflector.MessageHandler.obtainMessage(
									OnCompletionMessage, OwnerSpaceObj)
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
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, NPE)
						.sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel || !Reflector.flVisible)
							break; // . >
						Exception E = (Exception) msg.obj;
						Toast.makeText(
								Reflector.context,
								Reflector.context
										.getString(R.string.SErrorOfDataLoading)
										+ E.getMessage(), Toast.LENGTH_LONG)
								.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_SHOW:
						try {
							progressDialog = new ProgressDialog(Reflector.context);
							progressDialog.setMessage(Reflector.context
									.getString(R.string.SLoading));
							progressDialog
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							progressDialog.setIndeterminate(false);
							progressDialog.setCancelable(true);
							progressDialog
									.setOnCancelListener(new OnCancelListener() {
										@Override
										public void onCancel(
												DialogInterface arg0) {
											Cancel();
										}
									});
							progressDialog.show();
						} catch (Exception EE) {
							Toast.makeText(context, EE.getMessage(),
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
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};
	}

	private class TComponentTypedDataFileNamesLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflectorComponent Reflector;
		// .
		private int ComponentType;
		private int ComponentID;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileNamesLoading(TReflectorComponent pReflector,
				int pComponentType, int pComponentID, int pOnCompletionMessage) {
    		super();
    		//.
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
					+ "/" + Long.toString(User.UserID);
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
											Reflector.context
													.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																											// //
																											// =>
								SummarySize += Size;
								// .
								if (Canceller.flCancel)
									return; // . ->
								// .
								// .
								// MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,
								// (Integer) (100 * SummarySize /
								// Data.length)).sendToTarget();
							}
							// .
							TComponentTypedDataFiles OwnerTypedDataFiles = new TComponentTypedDataFiles(
									Reflector.context,
									SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
							OwnerTypedDataFiles.FromByteArrayV0(Data);
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
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, NPE)
						.sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel || !Reflector.flVisible)
							break; // . >
						Exception E = (Exception) msg.obj;
						Toast.makeText(
								context,
								Reflector.context
										.getString(R.string.SErrorOfDataLoading)
										+ E.getMessage(), Toast.LENGTH_SHORT)
								.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_SHOW:
						progressDialog = new ProgressDialog(Reflector.context);
						progressDialog.setMessage(Reflector.context
								.getString(R.string.SLoading));
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setIndeterminate(true);
						progressDialog.setCancelable(true);
						progressDialog
								.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface arg0) {
										Cancel();
									}
								});
						progressDialog.setButton(
								ProgressDialog.BUTTON_NEGATIVE,
								Reflector.context.getString(R.string.SCancel),
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
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};
	}

	private class TComponentTypedDataFileLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TReflectorComponent Reflector;
		// .
		private TComponentTypedDataFile ComponentTypedDataFile;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileLoading(TReflectorComponent pReflector,
				TComponentTypedDataFile pComponentTypedDataFile,
				int pOnCompletionMessage) {
    		super();
    		//.
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
					TGeoScopeServerInfo.TInfo ServersInfo = Reflector.Server.Info
							.GetInfo();
					TComponentStreamServer CSS = new TComponentStreamServer(
							Reflector.context, ServersInfo.SpaceDataServerAddress,
							ServersInfo.SpaceDataServerPort,
							Reflector.User.UserID, Reflector.User.UserPassword);
					try {
						String CFN = TTypesSystem.TypesSystem.SystemTDATAFile
								.Context_GetFolder()
								+ "/"
								+ ComponentTypedDataFile.FileName();
						// .
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
								.sendToTarget();
						try {
							CSS.ComponentStreamServer_GetComponentStream_Begin(
									ComponentTypedDataFile.DataComponentType,
									ComponentTypedDataFile.DataComponentID);
							try {
								File CF = new File(CFN);
								RandomAccessFile ComponentStream = new RandomAccessFile(
										CF, "rw");
								try {
									ComponentStream.seek(ComponentStream
											.length());
									// .
									CSS.ComponentStreamServer_GetComponentStream_Read(
											Long.toString(ComponentTypedDataFile.DataComponentID),
											ComponentStream, Canceller,
											new TProgressor() {
												@Override
												public synchronized boolean DoOnProgress(
														int Percentage) {
													MessageHandler
															.obtainMessage(
																	MESSAGE_PROGRESSBAR_PROGRESS,
																	Percentage)
															.sendToTarget();
													return true;
												}
											});
								} finally {
									ComponentStream.close();
								}
							} finally {
								CSS.ComponentStreamServer_GetComponentStream_End();
							}
						} finally {
							MessageHandler.obtainMessage(
									MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
						}
						//.
						ComponentTypedDataFile.PrepareAsFullFromFile(CFN);
						//.
						Canceller.Check();
						//.
						Reflector.MessageHandler.obtainMessage(
								OnCompletionMessage, ComponentTypedDataFile)
								.sendToTarget();
					} finally {
						CSS.Destroy();
					}
					break; // . >

				default:
					String URL1 = Server.Address;
					// . add command path
					URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
							+ "/" + Long.toString(User.UserID);
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
							+ Long.toString(ComponentTypedDataFile.DataComponentID)
							+ ","
							+ Integer
									.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataType
											+ SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
							+ "," + Integer.toString(WithComponentsFlag);
					// .
					byte[] URL2_Buffer;
					try {
						URL2_Buffer = URL2.getBytes("windows-1251");
					} catch (Exception E) {
						URL2_Buffer = null;
					}
					byte[] URL2_EncryptedBuffer = User
							.EncryptBufferV2(URL2_Buffer);
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
						HttpURLConnection Connection = Server
								.OpenConnection(URL);
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							InputStream in = Connection.getInputStream();
							try {
								Canceller.Check();
								//.
								int RetSize = Connection.getContentLength();
								if (RetSize > 0) {
									byte[] Data = new byte[RetSize];
									int Size;
									SummarySize = 0;
									int ReadSize;
									while (SummarySize < Data.length) {
										ReadSize = Data.length - SummarySize;
										Size = in.read(Data, SummarySize, ReadSize);
										if (Size <= 0)
											throw new Exception(
													Reflector.context
															.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
																													// =>
										SummarySize += Size;
										//.
										Canceller.Check();
										//.
										MessageHandler
												.obtainMessage(
														MESSAGE_PROGRESSBAR_PROGRESS,
														(Integer) (100 * SummarySize / Data.length))
												.sendToTarget();
									}
									//.
									ComponentTypedDataFile.FromByteArrayV0(Data);
								}
								else {
									ComponentTypedDataFile.DataType += SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
									ComponentTypedDataFile.Data = null;
								}
								//.
								Canceller.Check();
								//.
								Reflector.MessageHandler.obtainMessage(
										OnCompletionMessage,
										ComponentTypedDataFile).sendToTarget();
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
					break; // . >
				}
			} catch (InterruptedException E) {
			} catch (CancelException E) {
			} catch (NullPointerException NPE) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, NPE)
						.sendToTarget();
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel || !Reflector.flVisible)
							break; // . >
						Exception E = (Exception) msg.obj;
						Toast.makeText(
								context,
								Reflector.context
										.getString(R.string.SErrorOfDataLoading)
										+ E.getMessage(), Toast.LENGTH_SHORT)
								.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_SHOW:
						progressDialog = new ProgressDialog(Reflector.context);
						progressDialog.setMessage(Reflector.context
								.getString(R.string.SLoading));
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setIndeterminate(false);
						progressDialog.setCancelable(true);
						progressDialog
								.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface arg0) {
										Cancel();
									}
								});
						progressDialog.setButton(
								ProgressDialog.BUTTON_NEGATIVE,
								Reflector.context.getString(R.string.SCancel),
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
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};
	}

	private class TCoGeoMonitorObjectsLocationUpdating extends
			TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_UPDATESPACEIMAGE = 1;
		private static final int MESSAGE_STATUS_ALARM = 2;

		private TReflectorComponent Reflector;

		public TCoGeoMonitorObjectsLocationUpdating(TReflectorComponent pReflector) {
    		super();
    		//.
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
											if ((Mask & TCoGeoMonitorObject.STATUS_flAlarm_Mask) == TCoGeoMonitorObject.STATUS_flAlarm_Mask)
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
								MessageHandler.obtainMessage(
										MESSAGE_STATUS_ALARM, null)
										.sendToTarget();
							if (flUpdate)
								Reflector.WorkSpace.Update();
							// .
							if (Canceller.flCancel)
								return; // . ->
							// .
							TReflectionWindowStruc RW = Reflector.ReflectionWindow
									.GetWindow();
							boolean flUpdateImage = false;
							for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
								if (Reflector.CoGeoMonitorObjects.Items[I].flEnabled) {
									try {
										flUpdateImage = (Reflector.CoGeoMonitorObjects.Items[I]
												.UpdateVisualizationLocation(
														RW, Reflector) || flUpdateImage);
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
								TSpaceImageUpdating SIU = Reflector.GetSpaceImageUpdating();
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
						Thread.sleep(Reflector.CoGeoMonitorObjects
								.GetUpdateInterval() * 1000);
						// .
						if (Canceller.flCancel)
							return; // . ->
					} catch (InterruptedException E) {
						return; // . ->
					} catch (NullPointerException NPE) {
						if (Reflector.flVisible)
							MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
									NPE).sendToTarget();
					} catch (Exception E) {
						MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
								.sendToTarget();
						// .
						try {
							Thread.sleep(Reflector.CoGeoMonitorObjects
									.GetUpdateInterval() * 1000);
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
				try {
					switch (msg.what) {

					case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
							break; // . >
						Exception E = (Exception) msg.obj;
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
						Toast.makeText(
								context,
								Reflector.context
										.getString(R.string.SErrorOfUpdatingCurrentPosition)
										+ S, Toast.LENGTH_SHORT).show();
						// .
						break; // . >

					case MESSAGE_UPDATESPACEIMAGE:
						if (Canceller.flCancel)
							break; // . >
						// .
						Reflector.StartUpdatingSpaceImage(true);
						// .
						break; // . >

					case MESSAGE_STATUS_ALARM:
						if (Canceller.flCancel)
							break; // . >
						// .
						for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
							if (Reflector.CoGeoMonitorObjects.Items[I].Status_flAlarm) {
								MediaPlayer_PlayAlarmSound();
								// .
								Toast.makeText(
										context,
										Reflector.context.getString(R.string.SAlarm)
												+ Reflector.CoGeoMonitorObjects.Items[I].LabelText(),
										Toast.LENGTH_LONG).show();
							}
						}
						// .
						break; // . >
					}
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};
	} 

	public static class TObjectCreationGalleryOverlay extends TViewLayer {

		public static final String ItemsFileName = "ObjectCreationGallery.xml";
		
		public static final int SetupImagePrototypeTypeID = SpaceDefines.idTCoComponent;
		public static final int SetupImagePrototypeID = 1740; //. do not modify
		
		public static final int MODE_UNKNOWN 			= 0;
		public static final int MODE_WORKING 			= 1;
		public static final int MODE_EDITING_REMOVE 	= 2;
		public static final int MODE_EDITING_REPLACE 	= 3;
		
		public static final int Working_Color = Color.DKGRAY;
		public static final int Working_Transparency = 200;
		//.
		public static final int Editing_Color = Color.RED;
		public static final int Editing_Transparency = 100;
		
		public static final float DefaultHeaderHeight = 64.0F;

		public static final int ItemsColumnCount = 5;
		
		public static class TGalleryButton {
			
			public static class TClickHandler {
				
				public void DoOnClick() {
				}
			}
			
			//.
			private TObjectCreationGalleryOverlay Gallery;
			//.
			private String Text;
			//.
			private Paint paint = new Paint();
			//.
			private TClickHandler ClickHandler = null;
			//.
			private RectF rect = null;
			//.
			private boolean flSelectable = false;
			private boolean flSelected = false;
			
			public TGalleryButton(TObjectCreationGalleryOverlay pGallery, String pText, TClickHandler pClickHandler) {
				Gallery = pGallery;
				Text = pText;
				ClickHandler = pClickHandler;
				flSelectable = (ClickHandler != null); 
			}

			public void DrawOnCanvas(Canvas canvas, RectF pRect) {
				if (flSelected) { 
					paint.setStyle(Paint.Style.FILL);
					paint.setAlpha(Working_Transparency);
					paint.setColor(Color.RED);
					canvas.drawRect(pRect, paint);
				}
				//. draw button border
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(Gallery.Reflector.metrics.density*1.0F);
				paint.setColor(Color.LTGRAY);
				canvas.drawRect(pRect, paint);
				//. draw button text
				canvas.save();
				try {
					canvas.clipRect(pRect);
					//.
					paint.setStyle(Paint.Style.FILL);
					paint.setColor(Color.WHITE);
					paint.setTextSize(pRect.height()*0.50F);
					float TW = paint.measureText(Text);
					float TH = paint.getTextSize();
					float X = pRect.left+((pRect.right-pRect.left)-TW)/2.0F;
					float Y = pRect.top+((pRect.bottom-pRect.top)+TH)/2.0F;
					canvas.drawText(Text, X,Y, paint);
				}
				finally {
					canvas.restore();
				}
				//.
				rect = pRect;
			}
			
			public boolean PositionIsVisible(RectF pRect, double PX, double PY) {
				return (pRect.contains((float)PX,(float)PY));
			}
			
			public boolean CheckSelected(double PX, double PY) {
				flSelected = (flSelectable && (rect != null) && PositionIsVisible(rect, PX,PY)); 
				return flSelected;			
			}
			
			public void ClearSelected() {
				flSelected = false;
			}
			
			public void Click() {
				ClickHandler.DoOnClick();
			}
		}
		
		@SuppressWarnings("serial")
		public static class TItems extends ArrayList<TItems.TItem> {
			
			public static final int 	ItemMaxSize = 256;
			public static final float 	ItemPadding = 5.0F;
			
			public static class TItem {

				public static final float CaptionFontSize = 18.0F;
				
				
				private TItems Items;
				//.
				public int Index;
				//.
				public int 	idTComponent;
				public long idComponent;
				//.
				public String Name = "";
				//.
				public TComponentDescriptor Visualization = null;
				public TSpaceObj 			Visualization_Obj = null;
				public boolean 				Visualization_flSetup = false;
				//.
				private Paint ItemPaint = new Paint();
				private Paint CaptionPaint = new Paint();

				public TItem() {
				}
				
				public TItem(TItems pItems) {
					this();
					//.
					SetItems(pItems);
				}

				public TItem(int pidTComponent, long pidComponent, String pName) {
					this();
					//.
					idTComponent = pidTComponent;
					idComponent = pidComponent;
					Name = pName;
				}
				
				public void SetItems(TItems pItems) {
					Items = pItems;
					//.
					if (Items != null) {
						CaptionPaint.setColor(Color.YELLOW);
						CaptionPaint.setTextSize(CaptionFontSize*Items.Reflector.metrics.density);
					}
				}
				
				public void DrawOnCanvas(Canvas canvas, RectF rect, float Padding, boolean flSelected) {
					//. background
					ItemPaint.setStyle(Paint.Style.FILL);
					if (!flSelected) { 
						ItemPaint.setAlpha(Working_Transparency);
						ItemPaint.setColor(Color.GRAY);
					}
					else { 
						ItemPaint.setAlpha(255);
						ItemPaint.setColor(Color.RED);
					}
					canvas.drawRect(rect, ItemPaint);
					//.
					ItemPaint.setStyle(Paint.Style.STROKE);
					ItemPaint.setStrokeWidth(1.5F*Items.Reflector.metrics.density);
					ItemPaint.setColor(Color.WHITE);
					ItemPaint.setAlpha(Working_Transparency);
					canvas.drawRect(rect, ItemPaint);
					//.
					rect.left += Padding;
					rect.top += Padding;
					rect.right -= Padding;
					rect.bottom -= Padding;
					//. image
					if ((Visualization_Obj != null) && (Visualization_Obj.Container_Image != null)) {
						ItemPaint.setAlpha(255);
						Rect ImageRect = new Rect(0,0, Visualization_Obj.Container_Image.getWidth(),Visualization_Obj.Container_Image.getHeight());
						canvas.drawBitmap(Visualization_Obj.Container_Image, ImageRect, rect, ItemPaint);
					}
					else {
						ItemPaint.setAlpha(Working_Transparency);
						Rect ImageRect = new Rect(0,0, Items.HourGlassImage.getWidth(),Items.HourGlassImage.getHeight());
						canvas.drawBitmap(Items.HourGlassImage, ImageRect, rect, ItemPaint);
					}
					//. caption
					float TW = CaptionPaint.measureText(Name);
					float TH = CaptionPaint.getTextSize();
					float X = rect.left+((rect.right-rect.left)-TW)/2.0F;
					float Y = rect.bottom-(TH/2.0F);
					canvas.drawText(Name, X,Y, CaptionPaint);
				}
			}
			
			
			private TReflectorComponent Reflector;
			private String FileName;
			//.
			public float CellSize;
			//.
			protected Bitmap HourGlassImage;
			
			public TItems(TReflectorComponent pReflector, String pFileName) throws Exception {
				Reflector = pReflector;
				FileName = pFileName;
				//.
				HourGlassImage = BitmapFactory.decodeResource(Reflector.context.getResources(), R.drawable.hourglass, TBitmapDecodingOptions.GetBitmapFactoryOptions());
				//.
				Load();
			}
			
			protected void Load() throws Exception {
				clear();
				//.
				File F = new File(FileName);
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
							NodeList ItemsNode = TMyXML.SearchNode(RootNode,"Items").getChildNodes();
							int Cnt = ItemsNode.getLength();
							for (int I = 0; I < Cnt; I++) {
								Node ItemNode = ItemsNode.item(I);
								//.
								if (ItemNode.getLocalName() != null) {
									TItem Item = new TItem(this);
									//.
									Item.idTComponent = Integer.parseInt(TMyXML.SearchNode(ItemNode,"idTComponent").getFirstChild().getNodeValue());
									Item.idComponent = Long.parseLong(TMyXML.SearchNode(ItemNode,"idComponent").getFirstChild().getNodeValue());
									//.
									Item.Name = TMyXML.SearchNode(ItemNode,"Name").getFirstChild().getNodeValue();
									//.
									Item.Index = size(); 
									add(Item);
								}
							}
						}
						catch (Exception E) {
			    			throw new Exception("error of items data parsing: "+E.getMessage()); //. =>
						}
						break; //. >
					default:
						throw new Exception("unknown items data version, version: "+Integer.toString(Version)); //. =>
					}
				}
			}
			
			protected void Save() throws Exception {
	            File F = new File(FileName);
	            if (size() == 0) {
	            	F.delete();
	            	return; //. ->
	            }
	    	    String TFN = FileName+".tmp";
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
	                	int Cnt = size();
	                	for (int I = 0; I < Cnt; I++) {
	                		TItem Item = get(I);
	    	            	serializer.startTag("", "Item"+Integer.toString(I));
	    	            		//. idTComponent
	    	            		serializer.startTag("", "idTComponent");
	    	            		serializer.text(Integer.toString(Item.idTComponent));
	    	            		serializer.endTag("", "idTComponent");
	    	            		//. idComponent
	    	            		serializer.startTag("", "idComponent");
	    	            		serializer.text(Long.toString(Item.idComponent));
	    	            		serializer.endTag("", "idComponent");
	    	            		//. Name
	    	            		serializer.startTag("", "Name");
	    	            		serializer.text(Item.Name);
	    	            		serializer.endTag("", "Name");
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
			
			protected void Add(TItem Item) throws Exception {
				Item.SetItems(this);
				Item.Index = size(); 
				add(Item);
				//.
				Save();
			}
			
			public void Remove(TItems.TItem Item) throws Exception {
				remove(Item);
				Item.Index = -1;
				Item.SetItems(null);
				//. 
				UpdateItemIndexes();
				//.
				Save();
			}
			
			public void Replace(int Index, TItems.TItem Item) throws Exception {
				remove(Item);
				add(Index, Item);
				//. 
				UpdateItemIndexes();
				//.
				Save();
			}
			
			private void UpdateItemIndexes() {
				int Cnt = size();
				for (int I = 0; I < Cnt; I++)
					get(I).Index = I;
			}
			
			public void DrawOnCanvas(Canvas canvas, RectF rect, TItem SelectedItem) {
				float Padding = ItemPadding*Reflector.metrics.density; 
				int CntX = ItemsColumnCount;
				CellSize = rect.width()/CntX;
				int CntY = (int)(rect.height()/CellSize);
				int ItemIndex = 0;
				int ItemsCount = size();
				for (int Y = 0; Y < CntY; Y++) 
					for (int X = 0; X < CntX; X++) {
						RectF ItemRect = new RectF(rect.left+X*CellSize,rect.top+Y*CellSize, rect.left+(X+1)*CellSize,rect.top+(Y+1)*CellSize);
						TItem Item = this.get(ItemIndex);
						Item.DrawOnCanvas(canvas, ItemRect, Padding, (Item == SelectedItem));
						ItemIndex++;
						if (ItemIndex >= ItemsCount)
							return; //. ->
					}
			}

			public TItem GetItemAtPosition(RectF rect, double PX, double PY) {
				int CntX = ItemsColumnCount;
				float CellSize = rect.width()/CntX;
				int CntY = (int)(rect.height()/CellSize);
				int ItemIndex = 0;
				int ItemsCount = size();
				for (int Y = 0; Y < CntY; Y++) 
					for (int X = 0; X < CntX; X++) {
						RectF ItemRect = new RectF(rect.left+X*CellSize,rect.top+Y*CellSize, rect.left+(X+1)*CellSize,rect.top+(Y+1)*CellSize);
						if (ItemRect.contains((float)PX,(float)PY))
							return this.get(ItemIndex); //. ->
						ItemIndex++;
						if (ItemIndex >= ItemsCount)
							return null; //. ->
					}
				return null; //. ->
			}
		}
		
		private static class TItemsLoading extends TCancelableThread {

			private static final int MESSAGE_SHOWEXCEPTION 	= 0;
			private static final int MESSAGE_START 			= 1;
			private static final int MESSAGE_FINISH 		= 2;
			private static final int MESSAGE_LISTLOADED		= 3;
			private static final int MESSAGE_ITEMLOADED		= 4;

			private static class TItemSpaceObj {
				
				public TItems.TItem Item;
				//.
				public TComponentDescriptor Visualization;
				public TSpaceObj 			Visualization_Obj;
				
				public TItemSpaceObj(TItems.TItem pItem, TComponentDescriptor pVisualization, TSpaceObj pVisualizationObj) {
					Item = pItem;
					Visualization = pVisualization;
					Visualization_Obj = pVisualizationObj;
				}
			}
			
			private TObjectCreationGalleryOverlay Gallery;
			//.
			private TItems Items;
			private String ItemsFileName;

			public TItemsLoading(TObjectCreationGalleryOverlay pGallery, TItems pItems, String pItemsFileName) {
	    		super();
	    		//.
				Gallery = pGallery;
				Items = pItems;
				ItemsFileName = pItemsFileName;
				// .
				_Thread = new Thread(this);
				_Thread.start();
			}

			@Override
			public void run() {
				try {
					MessageHandler.obtainMessage(MESSAGE_START, null).sendToTarget();
					try {
						if (Items == null) {
							Items = new TItems(Gallery.Reflector, ItemsFileName);
							MessageHandler.obtainMessage(MESSAGE_LISTLOADED, Items).sendToTarget();
						}
						//.
						Canceller.Check();
						//.
						int Cnt = Items.size();
						for (int I = 0; I < Cnt; I++) {
							TItems.TItem Item = Items.get(I);
							try {
								TComponentFunctionality CF = Gallery.Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(Item.idTComponent,Item.idComponent);
								try {
									TComponentDescriptor Visualization = Item.Visualization; 
									if (Visualization == null)
										Visualization = CF.GetVisualizationComponent();
									//.
									if (Visualization != null) {
										TBase2DVisualizationFunctionality VF = (TBase2DVisualizationFunctionality)Gallery.Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(Visualization.idTComponent,Visualization.idComponent);
										if (VF != null) 
											try {
												TSpaceObj Visualization_Obj = VF.GetObj(TItems.ItemMaxSize);
												TItemSpaceObj ISO = new TItemSpaceObj(Item, Visualization, Visualization_Obj);
												MessageHandler.obtainMessage(MESSAGE_ITEMLOADED, ISO).sendToTarget();
											}
											finally {
												VF.Release();
											}
									}
								}
								finally {
									CF.Release();
								}
							} catch (Exception E) {
								MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, new Exception(E.getMessage())).sendToTarget();
							}
							//.
							Canceller.Check();
						}
					}
					finally {
						MessageHandler.obtainMessage(MESSAGE_FINISH, null).sendToTarget();
					}
				} catch (InterruptedException E) {
				} catch (CancelException CE) {
				} catch (Throwable E) {
					MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, new Exception(E.getMessage())).sendToTarget();
				}
			}

			private final Handler MessageHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					try {
						switch (msg.what) {

						case MESSAGE_SHOWEXCEPTION:
							if (Canceller.flCancel)
								break; // . >
							Exception E = (Exception)msg.obj;
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
							Toast.makeText(Gallery.Reflector.context, S, Toast.LENGTH_LONG).show();
							// .
							break; // . >

						case MESSAGE_START:
							Gallery.PostDraw();
							//.
							break; // . >
							
						case MESSAGE_FINISH:
							Gallery.ItemsLoading = null;
							Gallery.PostDraw();
							//.
							break; // . >
							
						case MESSAGE_LISTLOADED:
							TItems Items = (TItems)msg.obj;
							//.
							Gallery.Items = Items;
							Gallery.PostDraw();
							//.
							break; // . >
							
						case MESSAGE_ITEMLOADED:
							TItemSpaceObj ISO = (TItemSpaceObj)msg.obj;
							//.
							ISO.Item.Visualization = ISO.Visualization;
							ISO.Item.Visualization_Obj = ISO.Visualization_Obj;
							Gallery.PostDraw();
							//.
							break; // . >
						}
					} catch (Throwable E) {
						TGeoLogApplication.Log_WriteError(E);
					}
				}
			};
		} 

		private static class TItemLoading extends TCancelableThread {

			public static class TDoOnItemLoadedHandler {
			
				public void DoOnItemLoaded(TItems.TItem Item) {
				}
			}
			
			private static final int MESSAGE_SHOWEXCEPTION 	= 0;
			private static final int MESSAGE_START 			= 1;
			private static final int MESSAGE_FINISH 		= 2;
			private static final int MESSAGE_ITEMLOADED		= 3;

			private static class TItemSpaceObj {
				
				public TItems.TItem Item;
				//.
				public TComponentDescriptor Visualization;
				public TSpaceObj 			Visualization_Obj;
				
				public TItemSpaceObj(TItems.TItem pItem, TComponentDescriptor pVisualization, TSpaceObj pVisualizationObj) {
					Item = pItem;
					Visualization = pVisualization;
					Visualization_Obj = pVisualizationObj;
				}
			}
			
			private TObjectCreationGalleryOverlay Gallery;
			//.
			private TItems.TItem Item;
			//.
			private TDoOnItemLoadedHandler DoOnItemLoadedHandler;
			//.
		    private ProgressDialog 	progressDialog = null; 

			public TItemLoading(TObjectCreationGalleryOverlay pGallery, TItems.TItem pItem, TDoOnItemLoadedHandler pDoOnItemLoadedHandler) {
	    		super();
	    		//.
				Gallery = pGallery;
				Item = pItem;
				DoOnItemLoadedHandler = pDoOnItemLoadedHandler;
				// .
				_Thread = new Thread(this);
				_Thread.start();
			}

			@Override
			public void run() {
				try {
					MessageHandler.obtainMessage(MESSAGE_START, null).sendToTarget();
					try {
						try {
							TComponentFunctionality CF = Gallery.Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(Item.idTComponent,Item.idComponent);
							try {
								TComponentDescriptor Visualization = Item.Visualization; 
								if (Visualization == null)
									Visualization = CF.GetVisualizationComponent();
								//.
								Canceller.Check();
								//.
								if (Visualization != null) {
									TBase2DVisualizationFunctionality VF = (TBase2DVisualizationFunctionality)Gallery.Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(Visualization.idTComponent,Visualization.idComponent);
									if (VF != null) 
										try {
											TSpaceObj Visualization_Obj = VF.GetObj(TItems.ItemMaxSize);
											//.
											Canceller.Check();
											//.
											TItemSpaceObj ISO = new TItemSpaceObj(Item, Visualization, Visualization_Obj);
											MessageHandler.obtainMessage(MESSAGE_ITEMLOADED, ISO).sendToTarget();
										}
										finally {
											VF.Release();
										}
								}
							}
							finally {
								CF.Release();
							}
						} catch (CancelException CE) {
						} catch (Exception E) {
							MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, new Exception(E.getMessage())).sendToTarget();
						}
					}
					finally {
						MessageHandler.obtainMessage(MESSAGE_FINISH, null).sendToTarget();
					}
				} catch (Throwable E) {
					MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, new Exception(E.getMessage())).sendToTarget();
				}
			}

			private final Handler MessageHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					try {
						switch (msg.what) {

						case MESSAGE_SHOWEXCEPTION:
							if (Canceller.flCancel)
								break; // . >
							Exception E = (Exception)msg.obj;
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
							Toast.makeText(Gallery.Reflector.context, S, Toast.LENGTH_LONG).show();
							// .
							break; // . >

						case MESSAGE_START:
                        	progressDialog = new ProgressDialog(Gallery.Reflector.context);
                        	progressDialog.setMessage(Gallery.Reflector.context.getString(R.string.SLoading));    
                        	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
                        	progressDialog.setIndeterminate(true); 
                        	progressDialog.setCancelable(true);
                        	progressDialog.setOnCancelListener(new OnCancelListener() {
            					@Override
            					public void onCancel(DialogInterface arg0) {
            						Cancel();
            					}
            				});
                        	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, Gallery.Reflector.context.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
                        		@Override 
                        		public void onClick(DialogInterface dialog, int which) { 
            						Cancel();
                        		} 
                        	}); 
                        	//.
                        	progressDialog.show(); 	            	
							//.
							break; // . >
							
						case MESSAGE_FINISH:
		                	try {
		                    	if ((progressDialog != null) && progressDialog.isShowing())
		                    		progressDialog.dismiss(); 
		                	}
		                	catch (Exception Ex) {
		                	}
							//.
							break; // . >
							
						case MESSAGE_ITEMLOADED:
							TItemSpaceObj ISO = (TItemSpaceObj)msg.obj;
							//.
							ISO.Item.Visualization = ISO.Visualization;
							ISO.Item.Visualization_Obj = ISO.Visualization_Obj;
							//.
							if (DoOnItemLoadedHandler != null) 
								DoOnItemLoadedHandler.DoOnItemLoaded(ISO.Item);
							//.
							break; // . >
						}
					} catch (Throwable E) {
						TGeoLogApplication.Log_WriteError(E);
					}
				}
			};
		} 

		private static TItems.TItem SetupFromImageItem = null;

		
		private boolean flInitialized = false;
		//.
		private int Mode = MODE_UNKNOWN;
		//.
		public float HeaderHeight;
		//.
		private TGalleryButton 	TitleButton;
		private TGalleryButton 	SetupFromImageFileButton;
		private TGalleryButton 	ConfigurationButton;
		//.
		private TGalleryButton 	SelectedButton = null;
		//.
		private Paint BackgroundPaint = new Paint();
		//.
		private TItems 			Items = null;
		private TItemsLoading 	ItemsLoading = null;
		//.
		private TItemLoading ItemLoading = null;
		//.
		private TItems.TItem 		SelectedItem = null;
		private TAsyncProcessing 	SelectedItemPressing = null;
		//.
		private TItems.TItem 	DraggingItem = null;
		private TXYCoord 		DraggingItem_Pos = new TXYCoord();
		//.
		private TXYCoord Pointer_LastPos = new TXYCoord();
		
		
		public TObjectCreationGalleryOverlay(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			flTransparent = true;
		}

		public TObjectCreationGalleryOverlay(Context context, AttributeSet attrs) {
			super(context, attrs);
			flTransparent = true;
		}

		public TObjectCreationGalleryOverlay(Context context) {
			super(context);
			flTransparent = true;
		}
		
		@Override
		public void Initialize(TReflectorComponent pReflector) throws Exception {
			super.Initialize(pReflector);
			//.
			setZOrderOnTop(true);
			//.
			HeaderHeight = DefaultHeaderHeight*Reflector.metrics.density;
			//.
			TitleButton = new TGalleryButton(this,Reflector.context.getString(R.string.SNewObjectGallery), null);
			SetupFromImageFileButton = new TGalleryButton(this,Reflector.context.getString(R.string.SFromImageFile), new TGalleryButton.TClickHandler() {
				
				@Override
				public void DoOnClick() {
					SetupFromImageFile();
				}				
			});
			ConfigurationButton = new TGalleryButton(this,Reflector.context.getString(R.string.SConfiguration1), new TGalleryButton.TClickHandler() {
				
				@Override
				public void DoOnClick() {
					ShowConfigurationMenu();				
				}				
			});
			//.
			BackgroundPaint = new Paint();
			//.
			SelectedItem = null;
			DraggingItem = null;
			//.
			SetMode(MODE_WORKING);
			//.
			if (ItemsLoading != null)
				ItemsLoading.Cancel();
			ItemsLoading = new TItemsLoading(this, Items,ProfileFolder()+"/"+ItemsFileName);
			//.
			flInitialized = true;
		}
		
		@Override
		public void Finalize() {
			flInitialized = false;
			//.
			if (SelectedItemPressing != null) {
				SelectedItemPressing.Cancel();
				SelectedItemPressing = null;
			}
			//.
			if (ItemsLoading != null) {
				ItemsLoading.Cancel();
				ItemsLoading = null;
			}
			//.
			if (ItemLoading != null) {
				ItemLoading.Cancel();
				ItemLoading = null;
			}
			//.
			Mode = MODE_UNKNOWN;
			//.
			super.Finalize();
		}
		
		@Override
		protected void DoOnSizeChanged(int w, int h) {
			super.DoOnSizeChanged(w,h);
			//.
			PostDraw();
		}
		
		@Override
		protected void DoOnDraw(Canvas canvas, TCanceller Canceller, TTimeLimit TimeLimit) {
			//. clear
			canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
			//. draw background
			canvas.drawRect(0,0, Width,Height, BackgroundPaint);
			//.
			float Y = 0.0F;
			float ButtonHeight = HeaderHeight/2.0F;
			//. Title button
			RectF ButtonRect = new RectF(0,Y, Width,Y+ButtonHeight);
			Y += ButtonHeight;
			TitleButton.DrawOnCanvas(canvas, ButtonRect);
			//. SetupFromImageFile button
			ButtonRect = new RectF(0,Y, Width/2.0F,Y+ButtonHeight);
			SetupFromImageFileButton.DrawOnCanvas(canvas, ButtonRect);
			//. Configuration button
			ButtonRect = new RectF(Width/2.0F,Y, Width,Y+ButtonHeight);
			ConfigurationButton.DrawOnCanvas(canvas, ButtonRect);
			Y += ButtonHeight;
			//. Items
			if (Items != null) {
				RectF ItemsRect = new RectF(0,Y, Width,Height);
				Items.DrawOnCanvas(canvas, ItemsRect, SelectedItem);
			}
			if (DraggingItem != null) {
				float ItemSizeHalf = Items.CellSize/2.0F; 
				RectF ItemRect = new RectF((float)(DraggingItem_Pos.X-ItemSizeHalf),(float)(DraggingItem_Pos.Y-ItemSizeHalf), (float)(DraggingItem_Pos.X+ItemSizeHalf),(float)(DraggingItem_Pos.Y+ItemSizeHalf));
				DraggingItem.DrawOnCanvas(canvas, ItemRect, 0.0F, true);
			}
		}
		
		@Override
		protected void Pointer0_Down(double X, double Y) {
			if (!flInitialized) {
				Reflector.WorkSpace.Pointer0_Down(X,Y);
				return; //. ->
			}
			//.
			try {
				final double _X = X;
				final double _Y = Y;
				//.
				SelectedButton = Buttons_CheckSelected(_X,_Y);
				if (SelectedButton != null) {
					Draw();
					return; //. ->
				}
				//.
				TItems.TItem _SelectedItem = Items_GetItemAtPosition(_X,_Y);
				if (_SelectedItem != SelectedItem) {
					SelectedItem = _SelectedItem;
					Draw();
					//.
					if (SelectedItemPressing != null) {
						SelectedItemPressing.Cancel();
						SelectedItemPressing = null;
					}
					//.
					switch (Mode) {
					
					case MODE_WORKING:
					case MODE_EDITING_REMOVE:
						SelectedItemPressing = new TAsyncProcessing() {

							private Vibrator vibe = (Vibrator)Reflector.context.getSystemService(Context.VIBRATOR_SERVICE) ;
							
							@Override
							public void Process() throws Exception {
								//. long click delay
								Thread.sleep(500); 
								vibe.vibrate(100);
								Thread.sleep(200); 
							}

							@Override
							public void DoOnCompleted() throws Exception {
								SelectedItemPressing = null;
								//.
								TObjectCreationGalleryOverlay.this.DoOnChoice(_X,_Y);
							}
						};
						SelectedItemPressing.Start();
						break; //. >
					}
				}
			}
			finally {
				Pointer_LastPos.X = X;			
				Pointer_LastPos.Y = Y;			
			}
		}

		@Override
		protected void Pointer0_Up(double X, double Y) {
			if (!flInitialized) {
				Reflector.WorkSpace.Pointer0_Up(X,Y);
				return; //. ->
			}
			//.
			if (DraggingItem != null) {
				TItems.TItem Item = DraggingItem; 
				DraggingItem = null;
				//.
				TItems.TItem UnderItem = Items_GetItemAtPosition(X,Y);
				if (UnderItem != null) { 
		    		try {
						Items_Replace(UnderItem.Index, Item);
		    		}
		    		catch (Exception E) {
						Toast.makeText(Reflector.context, E.getMessage(),	Toast.LENGTH_LONG).show();
		    		}
					return; //. ->
				}
				else
					Draw();		    			
			}
			//.
			TGalleryButton _SelectedButton = Buttons_CheckSelected(X,Y);
			if (_SelectedButton == SelectedButton) {
				if (_SelectedButton != null) {
					try {
						_SelectedButton.Click();
					}
					finally {
						Buttons_ClearSelected();
						Draw();
					}
					return; // . ->
				}
			}
			else
				if ((SelectedButton != null) || (_SelectedButton != null)){ 
					Buttons_ClearSelected();
					Draw();
					return; // . ->
				}
			//.
			if (SelectedItem != null) {
				SelectedItem = null;
				Draw();
				//.
				if (SelectedItemPressing != null) {
					SelectedItemPressing.Cancel();
					SelectedItemPressing = null;
				}
			}
		}
		
		@Override
		protected void Pointer0_Move(double X, double Y) {
			if (!flInitialized) {
				Reflector.WorkSpace.Pointer0_Move(X,Y);
				return; //. ->
			}
			//.
			try {
				TGalleryButton _SelectedButton = Buttons_CheckSelected(X,Y);
				if (SelectedButton != null) {
					if (_SelectedButton != SelectedButton) {
						Buttons_ClearSelected();
						Draw();
					}
					return; // . ->
				}
				else 
					if (_SelectedButton != null) {
						Buttons_ClearSelected();
						Draw();
					}
				//.
				if (DraggingItem != null) {
					DraggingItem_Pos.X = X; DraggingItem_Pos.Y = Y;
					Draw();
					return; // . ->
				}
				//.
				TItems.TItem _SelectedItem = Items_GetItemAtPosition(X,Y);
				if (_SelectedItem != SelectedItem) {
					switch (Mode) {
					
					case MODE_WORKING:
						SelectedItem = null;
						Draw();
						//.
						if (SelectedItemPressing != null) {
							SelectedItemPressing.Cancel();
							SelectedItemPressing = null;
						}
						break; //. >

					case MODE_EDITING_REPLACE:
						DraggingItem = SelectedItem;
						DraggingItem_Pos.X = X; DraggingItem_Pos.Y = Y;
						//.
						SelectedItem = null;
						Draw();
						//.
						if (SelectedItemPressing != null) {
							SelectedItemPressing.Cancel();
							SelectedItemPressing = null;
						}
						break; //. >
					}
				}
			}
			finally {
				Pointer_LastPos.X = X;			
				Pointer_LastPos.Y = Y;			
			}
		}
		
		public TGalleryButton Buttons_CheckSelected(double PX, double PY) {
			Buttons_ClearSelected();
			//. Title button
			if (TitleButton.CheckSelected(PX,PY)) 
				return TitleButton; //. -> 				
			//. SetupFromImageFile button
			if (SetupFromImageFileButton.CheckSelected(PX,PY)) 
				return SetupFromImageFileButton; //. -> 				
			//. Configuration button
			if (ConfigurationButton.CheckSelected(PX,PY)) 
				return ConfigurationButton; //. -> 				
			//.
			return null;
		}
		
		public void Buttons_ClearSelected() {
			TitleButton.ClearSelected();
			SetupFromImageFileButton.ClearSelected();
			ConfigurationButton.ClearSelected();
		}
		
		private void ShowConfigurationMenu() {
    		final CharSequence[] _items;
			_items = new CharSequence[4];
			_items[0] = getContext().getString(R.string.SAddNewPrototype);
			_items[1] = getContext().getString(R.string.SReplaceItem);
			_items[2] = getContext().getString(R.string.SRemoveItem);
			_items[3] = getContext().getString(R.string.SBackToWorkMode);
    		AlertDialog.Builder builder = new AlertDialog.Builder(Reflector.context);
    		builder.setTitle(R.string.SMenu);
    		builder.setNegativeButton(Reflector.context.getString(R.string.SCancel),null);
    		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface arg0, int arg1) {
                	try {
    					switch (arg1) {
    					
    					case 0:
    						AlertDialog.Builder alert = new AlertDialog.Builder(Reflector.context);
    						//.
    						alert.setTitle("");
    						alert.setMessage(R.string.SNewPrototype);
    						//.
    			        	LayoutInflater factory = LayoutInflater.from(Reflector.context);
    			        	View layout = factory.inflate(R.layout.componentprototype_dialog_layout, null);
    						final EditText edComponent = (EditText)layout.findViewById(R.id.edComponent);
    						final EditText edComponentName = (EditText)layout.findViewById(R.id.edComponentName);
    			        	alert.setView(layout);		    						
    						//.
    						alert.setPositiveButton(R.string.SOk,new DialogInterface.OnClickListener() {
    							
    									@Override
    									public void onClick(DialogInterface dialog, int whichButton) {
    										//. hide keyboard
    										InputMethodManager imm = (InputMethodManager)Reflector.context.getSystemService(Context.INPUT_METHOD_SERVICE);
    										imm.hideSoftInputFromWindow(edComponent.getWindowToken(), 0);
    										//.
    										try {
    											String CID = edComponent.getText().toString();
    											String[] SA = CID.split(":");
    											if (SA.length != 2)
    												throw new Exception(Reflector.context.getString(R.string.SIncorrectComponentID)); //. =>
    											int idTComponent = Integer.parseInt(SA[0]);
    											long idComponent = Long.parseLong(SA[1]);
    											String Name = edComponentName.getText().toString();
    											final TItems.TItem Item = new TItems.TItem(idTComponent,idComponent, Name);
    											//. update the new item
    											TAsyncProcessing Updating = new TAsyncProcessing(Reflector.context, Reflector.context.getString(R.string.SWaitAMoment)) {

    												@Override
    												public void Process() throws Exception {
		    											TComponentFunctionality CF = Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(Item.idTComponent,Item.idComponent);
		    											try {
		    												TComponentDescriptor VD = CF.GetVisualizationComponent();
		    												if (VD != null) {
		    													TBase2DVisualizationFunctionality VF = (TBase2DVisualizationFunctionality)Reflector.User.Space.TypesSystem.TComponentFunctionality_Create(VD.idTComponent,VD.idComponent);
		    													if (VF != null) 
		    														try {
		    															Item.Visualization_Obj = VF.GetObj(TItems.ItemMaxSize);
		    														}
		    														finally {
		    															VF.Release();
		    														}
		    												}
		    											}
		    											finally {
		    												CF.Release();
		    											}
    													//.
    													Thread.sleep(100);
    												}

    												@Override
    												public void DoOnCompleted() throws Exception {
		    											Items_Add(Item);
		    											//.
		    				    						SetMode(MODE_EDITING_REPLACE);
    												}

    												@Override
    												public void DoOnException(Exception E) {
    													Toast.makeText(Reflector.context, E.getMessage(),	Toast.LENGTH_LONG).show();
    												}
    											};
    											Updating.Start();
    										} catch (Exception E) {
    											Toast.makeText(Reflector.context, E.getMessage(), Toast.LENGTH_LONG).show();
    										}
    									}
    								});
    						// .
    						alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
    							
    									@Override
    									public void onClick(DialogInterface dialog, int whichButton) {
    										//. hide keyboard
    										InputMethodManager imm = (InputMethodManager)Reflector.context.getSystemService(Context.INPUT_METHOD_SERVICE);
    										imm.hideSoftInputFromWindow(edComponent.getWindowToken(), 0);
    									}
    								});
    						// .
    						alert.show();
	                		break; //. >
						
    					case 1:
    						SetMode(MODE_EDITING_REPLACE);
		        			Toast.makeText(Reflector.context, R.string.SUseDraggingToReplaceItem, Toast.LENGTH_LONG).show();  						
	                		break; //. >
	                		
    					case 2:
    						SetMode(MODE_EDITING_REMOVE);
		        			Toast.makeText(Reflector.context, R.string.SLongPressOnItemToRemoveIt, Toast.LENGTH_LONG).show();  						
	                		break; //. >
	                		
    					case 3:
    						SetMode(MODE_WORKING);
	                		break; //. >
    					}
					}
					catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(Reflector.context, S, Toast.LENGTH_LONG).show();  						
					}
					//.
					arg0.dismiss();
    			}
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
		}
		
		private void SetupFromImageFile() {
			String BaseFolder = null; 
			File RF = new File(TGeoLogApplication.Resources_GetCurrentFolder()+"/"+TGeoLogApplication.Resource_ImagesFolder);
			if (RF.exists())
				BaseFolder = RF.getAbsolutePath();
			//.
			TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(Reflector.context, BaseFolder, ".BMP,.PNG,.JPG,.JPEG", new TFileSystemFileSelector.OpenDialogListener() {
	        	
	            @Override
	            public void OnSelectedFile(String fileName) {
                    File ChosenFile = new File(fileName);
                    //.
					try {
						FileInputStream FIS = new FileInputStream(ChosenFile);
						try
						{
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inDither=false;
							options.inPurgeable=true;
							options.inInputShareable=true;
							options.inTempStorage=new byte[1024*256]; 							
							Rect rect = new Rect();
							final Bitmap Image = BitmapFactory.decodeFileDescriptor(FIS.getFD(), rect, options);
							//.
							if (SetupFromImageItem == null) {
								TItems.TItem Item = new TItems.TItem();
								//.
								Item.idTComponent = SetupImagePrototypeTypeID;
								Item.idComponent = SetupImagePrototypeID;
								//.
								Item.Visualization_flSetup = true;
								//.
								if (ItemLoading != null)
									ItemLoading.Cancel();
								ItemLoading = new TItemLoading(TObjectCreationGalleryOverlay.this, Item, new TItemLoading.TDoOnItemLoadedHandler() {
									@Override
									public void DoOnItemLoaded(TItem Item) {
										SetupFromImageItem = Item;
										//.
										SetupFromImageFile_DoOnItemLoaded(SetupFromImageItem, Image);	
									}
								});
							}
							else
								SetupFromImageFile_DoOnItemLoaded(SetupFromImageItem, Image);								
						}
						finally
						{
							FIS.close();
						}
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(Reflector.context, S, Toast.LENGTH_LONG).show();  						
					}
	            }

				@Override
				public void OnCancel() {
				}
	        });
			FileSelector.show();
		}
		
		private void SetupFromImageFile_DoOnItemLoaded(TItem Item, Bitmap Image) {
			//. setup
			if (Item.Visualization_Obj != null) 
				try {
					Item.Visualization_Obj.Container_Image = Image;
					//.
					Item.Visualization_Obj.Nodes = new TXYCoord[4];
					float W = Item.Visualization_Obj.Container_Image.getWidth();
					float H = Item.Visualization_Obj.Container_Image.getHeight();
					float WH = W/2.0F;
					float HH = H/2.0F;
					//.
					Item.Visualization_Obj.Nodes[0] = new TXYCoord(-WH,-HH);
					Item.Visualization_Obj.Nodes[1] = new TXYCoord(+WH,-HH);
					Item.Visualization_Obj.Nodes[2] = new TXYCoord(+WH,+HH);
					Item.Visualization_Obj.Nodes[3] = new TXYCoord(-WH,+HH);
					//.
					Item.Visualization_Obj.Width = new TReal48(H);
					//.
					float MaxSize = W;
					if (H > MaxSize)
						MaxSize = H;
					Reflector.ObjectCreatingGallery_StartCreatingObject(Item, Width/2.0,Height/2.0, MaxSize);
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(Reflector.context, S, Toast.LENGTH_LONG).show();  						
				}
		}
		
		public TItems.TItem Items_GetItemAtPosition(double PX, double PY) {
			//. skip Header
			float Y = HeaderHeight;
			//. Items
			if (Items != null) {
				RectF ItemsRect = new RectF(0,Y, Width,Height);
				return Items.GetItemAtPosition(ItemsRect, PX,PY); //. ->
			}
			else
				return null; //. ->
		}
		
		public void Items_Add(TItems.TItem Item) throws Exception {
			Items.Add(Item);
			//.
			Draw();
		}
		
		public void Items_Remove(TItems.TItem Item) throws Exception {
			Items.Remove(Item);
			//.
			Draw();
		}
		
		public void Items_Replace(int Index, TItems.TItem Item) throws Exception {
			Items.Replace(Index, Item);
			//.
			Draw();
		}
		
		public void SetMode(int pMode) {
			if (Mode != pMode) {
				Mode = pMode;
				//.
				switch (Mode) {
				
				case MODE_WORKING:
					BackgroundPaint.setColor(Working_Color);
					BackgroundPaint.setAlpha(Working_Transparency);
					break; //. 
					
				case MODE_EDITING_REMOVE:
				case MODE_EDITING_REPLACE:
					BackgroundPaint.setColor(Editing_Color);
					BackgroundPaint.setAlpha(Editing_Transparency);
					break; //. 
				}
				//.
				PostDraw();
			}
		}
		
		private void DoOnChoice(double X, double Y) throws Exception {
			if (SelectedItem != null) {
				switch (Mode) {
				
				case MODE_WORKING:
					Reflector.ObjectCreatingGallery_StartCreatingObject(SelectedItem, X,Y, Items.CellSize);
					break; //. 
					
				case MODE_EDITING_REMOVE:
					final TItems.TItem _SelectedItem = SelectedItem;
	    		    new AlertDialog.Builder(Reflector.context)
	    	        .setIcon(android.R.drawable.ic_dialog_alert)
	    	        .setTitle(R.string.SConfirmation)
	    	        .setMessage(R.string.SRemoveQuestion)
	    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    		    	
	    		    	@Override
	    		    	public void onClick(DialogInterface dialog, int id) {
	    		    		try {
	    		    			Items_Remove(_SelectedItem);
	    		    		}
	    		    		catch (Exception E) {
								Toast.makeText(Reflector.context, E.getMessage(),	Toast.LENGTH_LONG).show();
	    		    		}
	    		    	}
	    		    })
	    		    .setNegativeButton(R.string.SNo, null)
	    		    .show();
					break; //. 
				}
			}
		}
	}
	
	public static class TDrawableObj {
		
		public TSpaceObj Obj;
		//.
		protected float LineWidth;
		//.
		protected Paint ThePaint;
		//.
		protected Matrix ImageMatrix = new Matrix();
		
		public TDrawableObj(TSpaceObj pObj, float pLineWidth) {
			Obj = pObj;
			LineWidth = pLineWidth;
			//.
			ThePaint = new Paint();
			ThePaint.setColor(Color.WHITE);
			ThePaint.setStrokeWidth(LineWidth);
		}
		
		public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
			if (Obj.Nodes != null) {
				TXYCoord[] ScrNodes = RW.ConvertNodesToScreen(Obj.Nodes);
				//.
				if (Obj.Container_Image != null) {
					ImageMatrix.reset();
					//.
					double X0 = ScrNodes[0].X; double Y0 = ScrNodes[0].Y;
					double X1 = ScrNodes[1].X; double Y1 = ScrNodes[1].Y;
					double X3 = ScrNodes[3].X; double Y3 = ScrNodes[3].Y;
					//.
					double diffX1X0 = X1-X0;
					double diffY1Y0 = Y1-Y0;
					double Alpha;
					if ((diffX1X0 > 0) && (diffY1Y0 >= 0))
						Alpha = 2*Math.PI+Math.atan(-diffY1Y0/diffX1X0);
					else
						if ((diffX1X0 < 0) && (diffY1Y0 > 0))
							Alpha = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
						else
							if ((diffX1X0 < 0) && (diffY1Y0 <= 0))
								Alpha = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
							else
								if ((diffX1X0 > 0) && (diffY1Y0 < 0))
									Alpha = Math.atan(-diffY1Y0/diffX1X0);
								else
									if (diffY1Y0 > 0)
										Alpha = 3.0*Math.PI/2.0;
									else Alpha = Math.PI/2.0;
					double Rotation = -Alpha;
					ImageMatrix.postRotate((float)(Rotation*180.0/Math.PI),0.0F,0.0F);
					//.
					float ImageScaleX = (float)(Math.sqrt(Math.pow(X1-X0,2)+Math.pow(Y1-Y0,2))/Obj.Container_Image.getWidth()); 
					float ImageScaleY = (float)(Math.sqrt(Math.pow(X3-X0,2)+Math.pow(Y3-Y0,2))/Obj.Container_Image.getHeight()); 
					ImageMatrix.postScale(ImageScaleX,ImageScaleY, 0.0F,0.0F);
					//.
					ImageMatrix.postTranslate((float)X0,(float)Y0);
		    		//.
		    		canvas.save();
		    		try {
				    	canvas.concat(ImageMatrix);
						canvas.drawBitmap(Obj.Container_Image, 0,0, ThePaint);
		    		}
		    		finally {
		    			canvas.restore();
		    		}
				}
			}
		}	
	}

	public static class TSelectableObj extends TDrawableObj {
		
		private static final int BodyTransparency = 80;
		private static final int BorderTransparency = 128;
		
		
		private Path ObjPath = new Path();
		
		public TSelectableObj(TSpaceObj pObj, float pLineWidth) {
			super(pObj,pLineWidth);
			//.
			ThePaint.setColor(Color.RED);
		}

		@Override
		public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
			if (Obj.Nodes != null) {
				TXYCoord[] ScrNodes = RW.ConvertNodesToScreen(Obj.Nodes);
				int Cnt = ScrNodes.length;
				//.
				boolean flAtLeastOneNodeVisibleInWindow = false;
				for (int I = 0; I < Cnt; I++) { 
					TXYCoord Node = ScrNodes[I];
					if (RW.IsScreenNodeVisible(Node.X,Node.Y)) {
						flAtLeastOneNodeVisibleInWindow = true;
						break; //. >
					}
				}
				//.
				ObjPath.reset();
				TXYCoord Node0 = ScrNodes[0];
				ObjPath.moveTo((float)Node0.X-LineWidth/2.0F,(float)Node0.Y);
				for (int I = 1; I < Cnt; I++) {
					TXYCoord Node = ScrNodes[I];
					ObjPath.lineTo((float)Node.X,(float)Node.Y);
				}
				ObjPath.lineTo((float)Node0.X,(float)Node0.Y-LineWidth/2.0F);
				//.
				if (flAtLeastOneNodeVisibleInWindow) {
					ThePaint.setStyle(Paint.Style.FILL);
					ThePaint.setAlpha(BodyTransparency);
					canvas.drawPath(ObjPath, ThePaint);
				}
				ThePaint.setStyle(Paint.Style.STROKE);
				ThePaint.setAlpha(BorderTransparency);
				canvas.drawPath(ObjPath, ThePaint);
			} 
		}	
	}
	
	public static class TEditableObj extends TDrawableObj {
		
		public static final int ImageTransparency = 175;
		
		public static class TTransformatrix {
			
			public double Xbind;
			public double Ybind;
			public double Scale;
			public double Rotation;
			public double TranslateX;
			public double TranslateY;
			
			public TTransformatrix(double pXbind, double pYbind, double pScale, double pRotation, double pTranslateX, double pTranslateY) {
				Xbind = pXbind;
				Ybind = pYbind;
				Scale = pScale;
				Rotation = pRotation;
				TranslateX = pTranslateX;
				TranslateY = pTranslateY;
			}

			public TTransformatrix(double pXbind, double pYbind) {
				this(pXbind,pYbind, 1.0, 0.0, 0.0,0.0);
			}
		}
		
		public static final int EDITINGMODESCOUNT = 4;
		//.
		public static final int EDITINGMODE_NONE 		= 0;
		public static final int EDITINGMODE_MOVING 		= 1;
		public static final int EDITINGMODE_SCALING		= 2;
		public static final int EDITINGMODE_ROTATING 	= 3;
		
		private class THandle {
			
		    public boolean 	Enabled = true;
		    public double 	R;
		    public double 	dR;
		    public int 		Color;
		}
		
		private class THandles {
			
			public THandle[] Items;
			//.
			private Paint ItemsPaint;
			
			public THandles() {
				Items = new THandle[EDITINGMODESCOUNT];
				//.
				THandle Handle = new THandle();
				Handle.Enabled = true;
				Handle.Color = Color.TRANSPARENT;
				Items[EDITINGMODE_NONE] = Handle;
				//.
				Handle = new THandle();
				Handle.Enabled = true;
				Handle.Color = Color.TRANSPARENT;
				Items[EDITINGMODE_MOVING] = Handle;
				//.
				Handle = new THandle();
				Handle.Enabled = true;
				Handle.dR = 8.0F*Reflector.metrics.density;
				Handle.Color = Color.RED;
				Items[EDITINGMODE_SCALING] = Handle;
				//.
				Handle = new THandle();
				Handle.Enabled = true;
				Handle.dR = Items[EDITINGMODE_SCALING].dR*3;
				Handle.Color = Color.RED;
				Items[EDITINGMODE_ROTATING] = Handle;
				//.
				ItemsPaint = new Paint();
			}

			public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
				Items[EDITINGMODE_MOVING].R = MaxScreenRadius;
				Items[EDITINGMODE_SCALING].R = Items[EDITINGMODE_MOVING].R+Items[EDITINGMODE_SCALING].dR;
				Items[EDITINGMODE_ROTATING].R = Items[EDITINGMODE_SCALING].R;
				//.
				ItemsPaint.setStyle(Paint.Style.STROKE);
				ItemsPaint.setColor(Items[EDITINGMODE_SCALING].Color);
				ItemsPaint.setStrokeWidth((float)(Items[EDITINGMODE_SCALING].dR*2));
				ItemsPaint.setAlpha(150);
				canvas.drawCircle((float)BindMarkerX,(float)BindMarkerY, (float)Items[EDITINGMODE_SCALING].R, ItemsPaint);
				//.
				ItemsPaint.setStyle(Paint.Style.FILL);
				ItemsPaint.setColor(Items[EDITINGMODE_ROTATING].Color);
				ItemsPaint.setStrokeWidth(0);
				float _X = (float)(BindMarkerX+Items[EDITINGMODE_ROTATING].R*Math.cos(-Transformatrix.Rotation));
				float _Y = (float)(BindMarkerY+Items[EDITINGMODE_ROTATING].R*Math.sin(-Transformatrix.Rotation));
				ItemsPaint.setAlpha(200);
				canvas.drawCircle((float)_X,(float)_Y, (float)Items[EDITINGMODE_ROTATING].dR, ItemsPaint);
			}
			
			public int CheckEditingMode(double pX, double pY) {
				int Result = EDITINGMODE_NONE;
				//.
				double _X = (float)(BindMarkerX+Items[EDITINGMODE_ROTATING].R*Math.cos(-Transformatrix.Rotation));
				double _Y = (float)(BindMarkerY+Items[EDITINGMODE_ROTATING].R*Math.sin(-Transformatrix.Rotation));
				double _R = (float)(Math.sqrt(Math.pow(pX-BindMarkerX,2)+Math.pow(pY-BindMarkerY,2)));
				//.
				if (Items[EDITINGMODE_SCALING].Enabled && ((Items[EDITINGMODE_SCALING].R-Items[EDITINGMODE_SCALING].dR <= _R) && (_R <= Items[EDITINGMODE_SCALING].R+Items[EDITINGMODE_SCALING].dR)))
					Result = EDITINGMODE_SCALING;
				//.
				if (Items[EDITINGMODE_MOVING].Enabled && (_R < Items[EDITINGMODE_MOVING].R)) 
					Result = EDITINGMODE_MOVING;
				else {
					_R = (float)Math.sqrt(Math.pow(pX-_X,2)+Math.pow(pY-_Y,2));
					if (Items[EDITINGMODE_ROTATING].Enabled && (_R <= Items[EDITINGMODE_ROTATING].dR))
						Result = EDITINGMODE_ROTATING;
				}
				return Result;
			}
		}
		
		public boolean flSetup = false;
		//.
		public int 	idTComponent = 0;
		public long idComponent = 0;
		//.
		private TReflectorComponent Reflector;
		//.
		protected double X = 0.0; 
		protected double Y = 0.0; 
		protected double MaxViewSize = 0.0; 
		//.
		private boolean flSetupView = false;
		//.
		public TTransformatrix Transformatrix = null;
		//.
		private THandles Handles;
		//.
		protected double BindMarkerX;
		protected double BindMarkerY;
		//.
		protected double MaxScreenRadius;
		//.
		protected int 	EditingMode = EDITINGMODE_NONE;
		public boolean	EditingIsFinished = false;
		//.
		protected double LastEditingPointX = 0.0;
		protected double LastEditingPointY = 0.0;
		
		public TEditableObj(TSpaceObj pObj, boolean pflSetup, int pidTComponent, long pidComponent, TReflectorComponent pReflector, boolean pflSetupView, double pX, double pY, double pMaxViewSize) throws Exception {
			super(pObj,0.0F);
			flSetup = pflSetup;
			idTComponent = pidTComponent;
			idComponent = pidComponent;
			Reflector = pReflector;
			flSetupView = pflSetupView;
			X = pX;
			Y = pY;
			MaxViewSize = pMaxViewSize;
			//.
			ThePaint.setAlpha(ImageTransparency);
			//.
			Handles = new THandles();
			//.
			Initialize();
		}
		
		public TEditableObj(TSpaceObj pObj, boolean pflSetup, TReflectorComponent pReflector, boolean pflSetupView, double pX, double pY, double pMaxViewSize) throws Exception {
			this(pObj, pflSetup, 0,0, pReflector, pflSetupView, pX,pY, pMaxViewSize);
		}
		
		public TEditableObj(TSpaceObj pObj, TReflectorComponent pReflector) throws Exception {
			this(pObj, false, 0,0, pReflector, false, 0.0,0.0, 0.0);
		}
		
		private void Initialize() throws Exception {
			if (Obj.Nodes.length != 4)
				throw new Exception("incorrect object container"); //. =>
			TXYCoord ObjCenter = Obj.Nodes_AveragePoint();
			if (ObjCenter == null)
				throw new Exception("could not get object center position"); //. =>
			//.
			if (flSetupView) {
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				double MaxLength;
				double LengthX = Math.pow(Obj.Nodes[1].X-Obj.Nodes[0].X,2)+Math.pow(Obj.Nodes[1].Y-Obj.Nodes[0].Y,2);
				double LengthY = Math.pow(Obj.Nodes[3].X-Obj.Nodes[0].X,2)+Math.pow(Obj.Nodes[3].Y-Obj.Nodes[0].Y,2);
				if (LengthX > LengthY)
					MaxLength = Math.sqrt(LengthX);
				else
					MaxLength = Math.sqrt(LengthY);
				double Scale = MaxViewSize/(RW.Scale()*MaxLength);
				//.
				double ALFA,BETTA,GAMMA;
				if ((Obj.Nodes[1].X-Obj.Nodes[0].X) != 0.0)
					ALFA = Math.atan((Obj.Nodes[1].Y-Obj.Nodes[0].Y)/(Obj.Nodes[1].X-Obj.Nodes[0].X));
				else 
				    if ((Obj.Nodes[1].Y-Obj.Nodes[0].Y) >= 0.0)
			    		ALFA = Math.PI/2.0;
		    	    else 
		    	    	ALFA = -Math.PI/2.0;
				if ((RW.X1-RW.X0) != 0)
					BETTA = Math.atan((RW.Y1-RW.Y0)/(RW.X1-RW.X0));
				else
				    if ((RW.Y1-RW.Y0) >= 0.0)
				    	BETTA = Math.PI/2.0;
				    else 
				    	BETTA = -Math.PI/2.0;
				GAMMA = (ALFA-BETTA);
				if ((Obj.Nodes[1].X-Obj.Nodes[0].X)*(RW.X1-RW.X0) < 0.0)
				    if ((Obj.Nodes[1].Y-Obj.Nodes[0].Y)*(RW.Y1-RW.Y0) >= 0.0)
				    	GAMMA = GAMMA-Math.PI;
				    else 
				    	GAMMA = GAMMA+Math.PI;
				double Rotation = -GAMMA;
				//.
				double TranslateX = X-ObjCenter.X;
				double TranslateY = Y-ObjCenter.Y;
				//.
				Transformatrix = new TEditableObj.TTransformatrix(ObjCenter.X,ObjCenter.Y, Scale, Rotation, TranslateX,TranslateY);
			}
			else { 
				X = ObjCenter.X;
				Y = ObjCenter.Y;
				//.
				Transformatrix = new TEditableObj.TTransformatrix(ObjCenter.X,ObjCenter.Y);
			}
		}
		
		@Override
		public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
			if (Obj.Nodes != null) {
				//. transforming
				int Cnt = Obj.Nodes.length;
				TXYCoord[] Nodes = new TXYCoord[Cnt];
				for (int I = 0; I < Cnt; I++) {
					TXYCoord N = Obj.Nodes[I];
				    double X = Transformatrix.Xbind+(N.X-Transformatrix.Xbind)*Transformatrix.Scale*Math.cos(Transformatrix.Rotation)+(N.Y-Transformatrix.Ybind)*Transformatrix.Scale*(-Math.sin(Transformatrix.Rotation))+Transformatrix.TranslateX;
				    double Y = Transformatrix.Ybind+(N.X-Transformatrix.Xbind)*Transformatrix.Scale*Math.sin(Transformatrix.Rotation)+(N.Y-Transformatrix.Ybind)*Transformatrix.Scale*Math.cos(Transformatrix.Rotation)+Transformatrix.TranslateY;
					Nodes[I] = new TXYCoord(X,Y);
				}
				//.
				TXYCoord[] ScrNodes = RW.ConvertNodesToScreen(Nodes);
				//.
				if (Obj.Container_Image != null) {
					ImageMatrix.reset();
					//.
					double X0 = ScrNodes[0].X; double Y0 = ScrNodes[0].Y;
					double X1 = ScrNodes[1].X; double Y1 = ScrNodes[1].Y;
					double X3 = ScrNodes[3].X; double Y3 = ScrNodes[3].Y;
					//.
					float ImageWidth = Obj.Container_Image.getWidth();
					float ImageHeight = Obj.Container_Image.getHeight();
					ImageMatrix.postTranslate(0.0F,-ImageHeight/2.0F);
					//.
					double diffX1X0 = X1-X0;
					double diffY1Y0 = Y1-Y0;
					double Alpha;
					if ((diffX1X0 > 0) && (diffY1Y0 >= 0))
						Alpha = 2*Math.PI+Math.atan(-diffY1Y0/diffX1X0);
					else
						if ((diffX1X0 < 0) && (diffY1Y0 > 0))
							Alpha = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
						else
							if ((diffX1X0 < 0) && (diffY1Y0 <= 0))
								Alpha = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
							else
								if ((diffX1X0 > 0) && (diffY1Y0 < 0))
									Alpha = Math.atan(-diffY1Y0/diffX1X0);
								else
									if (diffY1Y0 > 0)
										Alpha = 3.0*Math.PI/2.0;
									else Alpha = Math.PI/2.0;
					double Rotation = -Alpha;
					ImageMatrix.postRotate((float)(Rotation*180.0/Math.PI), 0.0F,0.0F);
					//.
					float ImageScaleX = (float)(Math.sqrt(Math.pow(X1-X0,2)+Math.pow(Y1-Y0,2))/ImageWidth); 
					float ImageScaleY = (float)(Math.sqrt(Math.pow(X3-X0,2)+Math.pow(Y3-Y0,2))/ImageHeight); 
					ImageMatrix.postScale(ImageScaleX,ImageScaleY, 0.0F,0.0F);
					//.
					ImageMatrix.postTranslate((float)((X0+X3)/2.0),(float)((Y0+Y3)/2.0));
		    		//.
		    		canvas.save();
		    		try {
				    	canvas.concat(ImageMatrix);
						canvas.drawBitmap(Obj.Container_Image, 0,0, ThePaint);
		    		}
		    		finally {
		    			canvas.restore();
		    		}
				}
				//.
				if (!EditingIsFinished) {
					TXYCoord BM = RW.ConvertToScreen(X,Y);
					BindMarkerX = (float)BM.X;
					BindMarkerY = (float)BM.Y;
					MaxScreenRadius = (float)(Obj.Nodes_GetMaxRadius(Transformatrix.Xbind,Transformatrix.Ybind)*RW.Scale()*Transformatrix.Scale);
					Handles.DrawOnCanvas(RW, canvas);
				}
			} 
		}	

		public int CheckEditingMode(double pX, double pY) {
			if (!EditingIsFinished) {
				EditingMode = Handles.CheckEditingMode(pX,pY);
				//.
				LastEditingPointX = pX;
				LastEditingPointY = pY;
			}
			return EditingMode;
		}
		
		public void StopEditing() {
			if (EditingMode != EDITINGMODE_NONE) {
				EditingMode = EDITINGMODE_NONE;
			}
		}
		
		public boolean IsEditing() {
			return (EditingMode != EDITINGMODE_NONE); 
		}
		
		public void ProcessEditingPoint(double pX, double pY) {
			if (EditingIsFinished) 
				return; //. ->
			switch (EditingMode) {
			
			case EDITINGMODE_MOVING:
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				TXYCoord LC = RW.ConvertToReal(LastEditingPointX,LastEditingPointY);
				TXYCoord NC = RW.ConvertToReal(pX,pY);
				double dX = NC.X-LC.X; double dY = NC.Y-LC.Y;
				X += dX; Y += dY;
				Transformatrix.TranslateX += dX; Transformatrix.TranslateY += dY;
				break; //. >

			case EDITINGMODE_SCALING:
				double LR = Math.sqrt(Math.pow(LastEditingPointX-BindMarkerX,2)+Math.pow(LastEditingPointY-BindMarkerY,2));
				double R = Math.sqrt(Math.pow(pX-BindMarkerX,2)+Math.pow(pY-BindMarkerY,2));
				if (LR != 0.0) 
					Transformatrix.Scale *= (R/LR);
				break; //. >
				
			case EDITINGMODE_ROTATING:
				double GAMMA;
				if ((pX-BindMarkerX) != 0.0) {
				    GAMMA = Math.atan((pY-BindMarkerY)/(pX-BindMarkerX));
				    if ((pX-BindMarkerX) < 0.0) 
				    	GAMMA = GAMMA+Math.PI;
				}
				else 
				    if ((pY-BindMarkerY) >= 0.0)
				    	GAMMA = Math.PI/2;
				    else 
				    	GAMMA = -Math.PI/2;
				Transformatrix.Rotation = -GAMMA;
				break; //. >
			}
			//.
			LastEditingPointX = pX;
			LastEditingPointY = pY;
		}
	}

	public static final int REASON_UNKNOWN 				= 0;
	public static final int REASON_MAIN 				= 1;
	public static final int REASON_USERPROFILECHANGED 	= 2;
	public static final int REASON_SHOWLOCATION 		= 3;
	public static final int REASON_SHOWLOCATIONWINDOW 	= 4;
	public static final int REASON_SHOWGEOLOCATION 		= 5;
	public static final int REASON_SHOWGEOLOCATION1 	= 6;
	public static final int REASON_MONITORGEOLOCATION 	= 7;
	// .
	public static final int MODE_NONE 		= 0;
	public static final int MODE_BROWSING 	= 1;
	public static final int MODE_EDITING 	= 2;
	// .
	public static final int VIEWMODE_NONE 			= 0;
	public static final int VIEWMODE_REFLECTIONS 	= 1;
	public static final int VIEWMODE_TILES 			= 2;
	// .
	public final static int NAVIGATION_MODE_NATIVE 			= 0;
	public final static int NAVIGATION_MODE_ARROWS 			= 1;
	public final static int NAVIGATION_MODE_MULTITOUCHING 	= 2;
	public final static int NAVIGATION_MODE_MULTITOUCHING1 	= 3;
	// .
	public final static int NAVIGATION_TYPE_NONE 				= 1;
	public final static int NAVIGATION_TYPE_MOVING 				= 2;
	public final static int NAVIGATION_TYPE_SCALING 			= 3;
	public final static int NAVIGATION_TYPE_ROTATING 			= 4;
	public final static int NAVIGATION_TYPE_TRANSFORMATING 		= 5;
	public final static int NAVIGATION_TYPE_SCALETRANSFORMATING = 6;
	// .
	public static final int  MESSAGE_SHOWEXCEPTION 											= 0;
	private static final int MESSAGE_STARTUPDATESPACEIMAGE 									= 1;
	private static final int MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED 			= 2;
	private static final int MESSAGE_UPDATESPACEIMAGE 										= 3;
	private static final int MESSAGE_SELECTEDOBJ_SET 										= 4;
	private static final int MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED 			= 5;
	private static final int MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED 				= 6;
	private static final int MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED 	= 7;
	private static final int MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED 		= 8;
	private static final int MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY 							= 9;
	private static final int MESSAGE_OPENUSERTASKPANEL 										= 10;
	private static final int MESSAGE_LOADINGPROGRESSBAR_SHOW 								= 11;
	private static final int MESSAGE_LOADINGPROGRESSBAR_HIDE 								= 12;
	private static final int MESSAGE_LOADINGPROGRESSBAR_PROGRESS 							= 13;
	// .
	public static final int REQUEST_SHOW_TRACKER 							= 1;
	public static final int REQUEST_EDIT_REFLECTOR_CONFIGURATION 			= 2;
	public static final int REQUEST_OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE 	= 3;
	public static final int REQUEST_OPEN_USERSEARCH 						= 4;
	// .
	private static final int BUTTONS_GROUP_LEFT	 	= 1;
	private static final int BUTTONS_GROUP_RIGHT	= 2;
	//.
	private static final int BUTTONS_STYLE_BRIEF 	= 1;
	private static final int BUTTONS_STYLE_NORMAL 	= 2;
	// .
	private static final int BUTTONS_COUNT = 13;
	// .
	private static final int BUTTON_URLS 						= 0;
	private static final int BUTTON_UPDATE 						= 1;
	private static final int BUTTON_SHOWREFLECTIONPARAMETERS 	= 2;
	private static final int BUTTON_ELECTEDPLACES 				= 3;
	private static final int BUTTON_OBJECTS 					= 4;
	private static final int BUTTON_MAPOBJECTSEARCH 			= 5;
	private static final int BUTTON_PREVWINDOW 					= 6;
	private static final int BUTTON_CREATINGGALLERY 			= 7;
	private static final int BUTTON_EDITOR 						= 8;
	private static final int BUTTON_USERSEARCH 					= 9;
	private static final int BUTTON_TRACKER 					= 10;
	private static final int BUTTON_COMPASS 					= 11;
	private static final int BUTTON_MYUSERPANEL 				= 12;

	private static boolean flCheckContextStorage = true;
	// .
	private static TReflectionWindowStrucStack MyLastWindows = null;
	
	
	public boolean 	flExists = false;
	public boolean 	flVisible = false;
	public boolean 	flRunning = false;
	public boolean 	flOffline = false;
	private boolean flEnabled = true;
	//.
	public int ID = 0;
	//. Start reason
	public int Reason = REASON_MAIN;
	//.
	public TReflectorConfiguration Configuration;
	//.
	public TGeoScopeServer 				Server;
	public TGeoScopeServerUser 			User;
	public TUserIncomingMessageReceiver UserIncomingMessageReceiver;
	public int 							UserIncomingMessages_LastCheckInterval;
	//.
	public TReflectionWindow 	ReflectionWindow;
	private Matrix 				ReflectionWindowTransformatrix = new Matrix();
	//.
	private int Reflection_FirstTryCount = 3;
	//.
	public DisplayMetrics metrics;
	//.
	public int ButtonsStyle = BUTTONS_STYLE_BRIEF;
	//.
	public Activity ParentActivity;
	//.
	public RelativeLayout ParentLayout;
	//.
	public Context context;
	//.
	public TWorkSpace 	WorkSpace = null;
	public TViewLayer 	WorkSpaceOverlay = null;
	public boolean 		WorkSpaceOverlay_Active() {
		return (WorkSpaceOverlay != null);
	}
	//. Mode
	public int Mode = MODE_BROWSING;
	//. View mode
	public int ViewMode = VIEWMODE_NONE;
	//.
	protected TSpaceReflections SpaceReflections;
	//.
	public TTileImagery SpaceTileImagery;
	protected boolean 	SpaceTileImagery_flUseResultTilesSet = true;
	//.
	public TSpaceHints SpaceHints;
	//. result image
	protected TReflectorSpaceImage SpaceImage;
	//.
	private TSpaceImageCaching _SpaceImageCaching = null;
	//.
	private TSpaceImageUpdating _SpaceImageUpdating = null;
	//. Navigation mode and type
	public int 	NavigationMode = NAVIGATION_MODE_MULTITOUCHING1;
	//.
	private int 	NavigationType = NAVIGATION_TYPE_NONE;
	public boolean 	IsNavigating() {
		return (NavigationType != NAVIGATION_TYPE_NONE);
	}
	//. protected Matrix NavigationTransformatrix = new Matrix();
	//.
	private double 	ScaleCoef = 3.0;
	//.
	public int 		VisibleFactor = 16;
	// .
	public int 		DynamicHintVisibleFactor = 2 * 2;
	public double 	DynamicHintVisibility = 1.0;
	// .
	public TReflectorElectedPlaces ElectedPlaces = null;
	public TReflectionWindowStrucStack LastWindows;
	private TReflectionWindow.TObjectAtPositionGetting ObjectAtPositionGetting = null;
	// .
	public TSelectableObj SelectedObj = null;
	//.
	public TEditableObj EditingObj = null;
	//.
	public TCancelableThread 	SelectedComponentTypedDataFileNamesLoading = null;
	public AlertDialog 			SelectedComponentTypedDataFileNames_SelectorPanel = null;
	public TCancelableThread 	SelectedComponentTypedDataFileLoading = null;
	//.
	public TCoGeoMonitorObjects 					CoGeoMonitorObjects;
	private TCoGeoMonitorObjectsLocationUpdating 	CoGeoMonitorObjects_LocationUpdating = null;
	private void 									CoGeoMonitorObjects_LocationUpdating_Intialize() {
		CoGeoMonitorObjects_LocationUpdating_Finalize();
		//.
		CoGeoMonitorObjects_LocationUpdating = new TCoGeoMonitorObjectsLocationUpdating(this);
		
	}
	private void 									CoGeoMonitorObjects_LocationUpdating_Finalize() {
		if (CoGeoMonitorObjects_LocationUpdating != null) {
			CoGeoMonitorObjects_LocationUpdating.Cancel();
			CoGeoMonitorObjects_LocationUpdating = null;
		}
	}
	// .
	public TReflectorObjectTracks 					ObjectTracks;
	private TAsyncProcessing						ObjectTracks_TrackAdding = null;
	//.
	public MediaPlayer _MediaPlayer = null;
	//.
	private ProgressDialog progressDialog;
	//.
	public final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					if (!flExists)
						break; // . >
					String EStr = (String) msg.obj;
					Toast.makeText(context,
							context.getString(R.string.SError) + EStr,
							Toast.LENGTH_SHORT).show();
					// .
					break; // . >

				case MESSAGE_STARTUPDATESPACEIMAGE:
					if (!flExists)
						return; // . ->
					// .
					StartUpdatingSpaceImage();
					// .
					break; // . >

				case MESSAGE_VIEWMODE_TILES_LEVELTILECONTAINERSARECHANGED:
					if (!flExists)
						return; // . ->
					// .
					TRWLevelTileContainer[] LevelTileContainers = (TRWLevelTileContainer[]) msg.obj;
					WorkSpace_Buttons_Update(LevelTileContainers);
					// .
					break; // . >

				case MESSAGE_UPDATESPACEIMAGE:
					if (!flExists)
						return; // . ->
					//. check the EditingObj for completion
					if ((EditingObj != null) && EditingObj.EditingIsFinished) 
						EditingObj_Clear();
					//.
					ResetNavigationAndUpdateCurrentSpaceImage();
					//. validate space window update subscription if the window is changed
					try {
						if (flVisible)
							ReflectionWindow.UpdateSubscription_Validate();
					} catch (Exception E) {
						Toast.makeText(context, E.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
					//. add new window to last windows
					TReflectionWindowStruc RWS = ReflectionWindow.GetWindow();
					LastWindows.Push(RWS);
					// .
					break; // . >

				case MESSAGE_SELECTEDOBJ_SET:
					if (!flExists)
						return; // . ->
					if (msg.obj == null)
						return; // . ->
					//.
					SelectedObj_Set(new TSelectableObj(new TOwnerSpaceObj((TSpaceObj)msg.obj), 5.0F*metrics.density));
					//.
					ResetNavigationAndUpdateCurrentSpaceImage();
					//.
					if (SelectedComponentTypedDataFileNamesLoading != null)
						SelectedComponentTypedDataFileNamesLoading.Cancel();
					SelectedComponentTypedDataFileNamesLoading = TReflectorComponent.this.new TSpaceObjOwnerTypedDataFileNamesLoading(
							TReflectorComponent.this, (TOwnerSpaceObj)SelectedObj.Obj, 2000,
							MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED);
					// .
					break; // . >

				case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED:
					if (!flExists)
						return; // . ->
					// .
					TOwnerSpaceObj Obj = (TOwnerSpaceObj) msg.obj;
					if ((Obj.OwnerTypedDataFiles != null)
							&& (Obj.OwnerTypedDataFiles.Items != null)) {
						String Hint = null;
						// . look for first DocumentName that will be an object
						// name
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
									TReflectorComponent.this, ComponentTypedDataFile,
									MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
							SelectedComponentTypedDataFileNames_SelectorPanel = null;
							if (Hint != null)
								Toast.makeText(context, Hint,
										Toast.LENGTH_LONG).show();
						} else {
							Intent intent = new Intent(context, TComponentTypedDataFilesPanel.class);
							intent.putExtra("ComponentID", ID);
							intent.putExtra("DataFiles", Obj.OwnerTypedDataFiles.ToByteArrayV0());
							intent.putExtra("AutoStart", false);
							// .
							ParentActivity.startActivity(intent);
						}
					}
					// .
					break; // . >

				case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED:
					if (!flExists)
						return; // . ->
					// .
					TComponentTypedDataFiles OwnerTypedDataFiles = (TComponentTypedDataFiles) msg.obj;
					if (OwnerTypedDataFiles != null) {
						if ((OwnerTypedDataFiles != null)
								&& (OwnerTypedDataFiles.Items != null)) {
							String Hint = null;
							// . look for first DocumentName that will be a
							// object name
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
									SelectedComponentTypedDataFileLoading
											.Cancel();
								SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(
										TReflectorComponent.this,
										ComponentTypedDataFile,
										MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED);
								SelectedComponentTypedDataFileNames_SelectorPanel = null;
								if (Hint != null)
									Toast.makeText(context, Hint,
											Toast.LENGTH_LONG).show();
							} else {
								Intent intent = new Intent(context, TComponentTypedDataFilesPanel.class);
								intent.putExtra("ComponentID", ID);
								intent.putExtra("DataFiles", OwnerTypedDataFiles.ToByteArrayV0());
								intent.putExtra("AutoStart", false);
								//.
								ParentActivity.startActivity(intent);
							}
						}
					}
					// .
					break; // . >

				case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED:
				case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED:
					if (!flExists)
						return; // . ->
					//.
					try {
						TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile) msg.obj;
						if (ComponentTypedDataFile != null)
							ComponentTypedDataFile.Open(User, ParentActivity);
					} catch (Exception E) {
						Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
						return; // . ->
					}
					//.
					break; // . >

				case MESSAGE_CONTEXTSTORAGE_NEARTOCAPACITY:
					if (!flExists)
						return; // . ->
					// .
					new AlertDialog.Builder(context)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.SAlert)
							.setMessage(
									context
											.getString(R.string.SDiskFillingIsNearToCapacity)
											+ " - "
											+ Integer
													.toString((int) (100.0 * User.Space.Context.Storage
															.DeviceFillFactor()))
											+ " %"
											+ "\n"
											+ context
													.getString(R.string.SDoYouWantToClearSomeContextData))
							.setPositiveButton(R.string.SYes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Intent intent = new Intent(context, TReflectorConfigurationPanel.class);
											intent.putExtra("ComponentID", ID);
											ParentActivity.startActivityForResult(intent, REQUEST_EDIT_REFLECTOR_CONFIGURATION);
										}
									}).setNegativeButton(R.string.SNo, null)
							.show();
					break; // . >

				case MESSAGE_OPENUSERTASKPANEL:
					if (!flExists)
						break; // . >
					byte[] TaskData = (byte[]) msg.obj;
					// .
					try {
						TTracker Tracker = TTracker.GetTracker();
						if (Tracker == null)
							throw new Exception(
									context.getString(R.string.STrackerIsNotInitialized)); // .
																					// =>
						Intent intent = new Intent(context, TUserTaskPanel.class);
						intent.putExtra("ComponentID", TReflectorComponent.this.ID);
						intent.putExtra("UserID", (long)Tracker.GeoLog.UserID);
						intent.putExtra("flOriginator", true);
						intent.putExtra("TaskData", TaskData);
						ParentActivity.startActivity(intent);
					} catch (Exception Ex) {
						Toast.makeText(context, Ex.getMessage(),
								Toast.LENGTH_LONG).show();
						ParentActivity.finish();
					}
					break; // . >

				case MESSAGE_LOADINGPROGRESSBAR_SHOW:
					progressDialog = new ProgressDialog(context);
					progressDialog.setMessage(context
							.getString(R.string.SLoading));
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progressDialog.setIndeterminate(true);
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
						}
					});
					progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
							context.getString(R.string.SCancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					// .
					progressDialog.show();
					// .
					break; // . >

				case MESSAGE_LOADINGPROGRESSBAR_HIDE:
					if (flVisible && progressDialog.isShowing())
						progressDialog.dismiss();
					// .
					break; // . >

				case MESSAGE_LOADINGPROGRESSBAR_PROGRESS:
					progressDialog.setProgress((Integer) msg.obj);
					// .
					break; // . >
				}
			} catch (Throwable E) {
				TGeoLogApplication.Log_WriteError(E);
			}
		}
	};

	public TReflectorComponent(Activity pParentActivity, RelativeLayout pParentLayout, Intent Parameters) throws Exception {
		ID = GetNextID();
		//.
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		context = ParentActivity;
		//.
		Bundle extras;
		if (Parameters != null)
			extras = Parameters.getExtras();
		else
			extras = null;
		//.
		if (extras != null) 
			Reason = extras.getInt("Reason");
		//.
		metrics = context.getResources().getDisplayMetrics();
		//.
		Configuration = new TReflectorConfiguration(context, this);
		Configuration.Load();
		// . Initialize User
		try {
			Server = TUserAgent.GetUserAgent().Server;
			// . re-initialize server info later
			Server.Info.Finalize();
			// .
			InitializeUser(Configuration.flUserSession);
		} catch (Exception E) {
			throw E; //. =>
		}
		// .
		double Xc = 317593.059;
		double Yc = -201347.576;
		TReflectionWindowStruc RW = new TReflectionWindowStruc(Xc - 10,
				Yc + 10, Xc + 10, Yc + 10, Xc + 10, Yc - 10, Xc - 10, Yc - 10,
				0, 0, 320, 240,
				TReflectionWindowActualityInterval.NullTimestamp,
				TReflectionWindowActualityInterval.MaxTimestamp);
		if (Configuration.ReflectionWindowData != null) {
			RW.FromByteArrayV1(Configuration.ReflectionWindowData);
		}
		//.
		ReflectionWindow = new TReflectionWindow(this, RW);
		ReflectionWindow.Normalize();
		//.
		ElectedPlaces = new TReflectorElectedPlaces();
		// .
		if (MyLastWindows == null)
			LastWindows = new TReflectionWindowStrucStack(MaxLastWindowsCount);
		else
			LastWindows = MyLastWindows;
		// .
		NavigationMode = Configuration.ReflectionWindow_NavigationMode;
		//.
		ButtonsStyle = BUTTONS_STYLE_BRIEF;
		if (Configuration.ReflectionWindow_flLargeControlButtons)
			ButtonsStyle = BUTTONS_STYLE_NORMAL;
		//.
		ParentActivity = pParentActivity;
		//.
		ParentLayout = pParentLayout;
		//.
		WorkSpace = (TWorkSpace) ParentLayout.findViewById(R.id.ivWorkSpace);
		WorkSpace.Initialize(this);
		WorkSpace_Buttons_Recreate(false);
		//.
		SpaceReflections = new TSpaceReflections(this);
		//.
		SpaceTileImagery = new TTileImagery(this, Configuration.ReflectionWindow_ViewMode_Tiles_Compilation);
		// . load TileImagery data from file
		SpaceTileImagery.Data.CheckInitialized();
		//.
		SpaceHints = new TSpaceHints(this);
		//.
		SpaceImage = new TReflectorSpaceImage(this, 16, 1);
		//.
		ViewMode = Configuration.ReflectionWindow_ViewMode;
		//.
		_SpaceImageCaching = new TSpaceImageCaching(this);
		//.
		ObjectTracks = new TReflectorObjectTracks(this);
		//.
		CoGeoMonitorObjects = new TCoGeoMonitorObjects(this);
		//.
		UserIncomingMessages_LastCheckInterval = User.IncomingMessages
				.SetMediumCheckInterval();
		User.IncomingMessages.Check();
		//. change the view according to the "start reason"
		switch (Reason) {

		case TReflectorComponent.REASON_SHOWLOCATION:
			if (extras != null)
				try {
					byte[] LocationXY_BA = extras.getByteArray("LocationXY");
					TXYCoord LocationXY = new TXYCoord();
					LocationXY.FromByteArray(LocationXY_BA);
					//.
					double Timestamp = extras.getDouble("Timestamp");
					//.
					if (Timestamp > 0.0) 
	    	            ReflectionWindow.SetActualityTime(Timestamp, false);
					MoveReflectionWindow(LocationXY);
				} catch (Exception E) {
					throw E; //. =>
				}
			break; // . >

		case TReflectorComponent.REASON_SHOWLOCATIONWINDOW:
			if (extras != null)
				try {
					byte[] LocationWindow_BA = extras
							.getByteArray("LocationWindow");
					TLocation LocationWindow = new TLocation();
					LocationWindow.FromByteArrayV2(LocationWindow_BA, 0);
					// . set location
					SetReflectionWindowByLocation(LocationWindow);
				} catch (Exception E) {
					throw E; //. =>
				}
			break; // . >

		case TReflectorComponent.REASON_SHOWGEOLOCATION:
			if (extras != null)
				try {
					byte[] GeoLocation_BA = extras.getByteArray("GeoLocation");
					if (GeoLocation_BA != null) {
						final TGeoCoord GeoLocation = new TGeoCoord();
						GeoLocation.FromByteArray(GeoLocation_BA, 0);
						// . set location
						TAsyncProcessing Processing = new TAsyncProcessing(context,
								context.getString(R.string.SWaitAMoment)) {

							private TXYCoord LocationXY = null;

							@Override
							public void Process() throws Exception {
								LocationXY = ConvertGeoCoordinatesToXY(
										GeoLocation.Datum, GeoLocation.Latitude,
										GeoLocation.Longitude, GeoLocation.Altitude);
								// .
								Thread.sleep(100);
							}

							@Override
							public void DoOnCompleted() throws Exception {
								MoveReflectionWindow(LocationXY);
							}

							@Override
							public void DoOnException(Exception E) {
								Toast.makeText(context, E.getMessage(),
										Toast.LENGTH_LONG).show();
							}
						};
						Processing.Start();
					}
				} catch (Exception E) {
					throw E; //. =>
				}
			break; // . >

		case TReflectorComponent.REASON_SHOWGEOLOCATION1:
			if (extras != null)
				try {
					byte[] GeoLocation_BA = extras.getByteArray("GeoLocation");
					if (GeoLocation_BA != null) {
						final TGeoLocation GeoLocation = new TGeoLocation();
						GeoLocation.FromByteArray(GeoLocation_BA, 0);
						// . set location
						TAsyncProcessing Processing = new TAsyncProcessing(context,
								context.getString(R.string.SWaitAMoment)) {

							private TXYCoord LocationXY = null;

							@Override
							public void Process() throws Exception {
								LocationXY = ConvertGeoCoordinatesToXY(
										GeoLocation.Datum, GeoLocation.Latitude,
										GeoLocation.Longitude, GeoLocation.Altitude);
								// .
								Thread.sleep(100);
							}

							@Override
							public void DoOnCompleted() throws Exception {
								if (GeoLocation.Timestamp > 0.0) 
									ReflectionWindow.SetActualityTime(GeoLocation.Timestamp, false);
								MoveReflectionWindow(LocationXY);
							}

							@Override
							public void DoOnException(Exception E) {
								Toast.makeText(context, E.getMessage(),
										Toast.LENGTH_LONG).show();
							}
						};
						Processing.Start();
					}
				} catch (Exception E) {
					throw E; //. =>
				}
			break; // . >
		}
		//.
		_SpaceImageCaching.Start();
		//.
		_AddComponent(this);
		//.
		flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
		flExists = false;
		//.
		_RemoveComponent(this);
		//.
		if (ObjectCreatingGallery_Active()) {
			ObjectCreatingGallery_Stop();
			return; //. ->
		}
		//.
		if ((User != null) && (User.IncomingMessages != null))
			User.IncomingMessages.SetCheckInterval(UserIncomingMessages_LastCheckInterval);
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
		//.
		if (ObjectAtPositionGetting != null) {
			ObjectAtPositionGetting.Cancel();
			ObjectAtPositionGetting = null;
		}
		//.
		if (SelectedComponentTypedDataFileNamesLoading != null) {
			SelectedComponentTypedDataFileNamesLoading.Cancel();
			SelectedComponentTypedDataFileNamesLoading = null;
		}
		//.
		if (SelectedComponentTypedDataFileLoading != null) {
			SelectedComponentTypedDataFileLoading.Cancel();
			SelectedComponentTypedDataFileLoading = null;
		}
		//.
		if (ObjectTracks_TrackAdding != null) {
			ObjectTracks_TrackAdding.Cancel();
			ObjectTracks_TrackAdding = null;
		}
		//.
		if (ReflectionWindow != null) 
			ReflectionWindow.UpdateSubscription_Free();
		//.
		if (WorkSpace != null) {
			WorkSpace.Finalize();
			WorkSpace = null;
		}
		//.
		if (SpaceImage != null) {
			SpaceImage.Destroy();
			SpaceImage = null;
		}
		if (SpaceHints != null) {
			SpaceHints.Destroy();
			SpaceHints = null;
		}
		if (SpaceTileImagery != null) {
			SpaceTileImagery.Destroy();
			SpaceTileImagery = null;
		}
		if (SpaceReflections != null) {
			SpaceReflections.Destroy();
			SpaceReflections = null;
		}
		//.
		MyLastWindows = LastWindows;
		//.
		FinalizeUser();
		Server = null;
		// .
		if ((Configuration != null) && ((Reason == REASON_UNKNOWN) || (Reason == REASON_MAIN) || (Reason == REASON_USERPROFILECHANGED))) 
			Configuration.Save();
	}

	@Override
	public void Start() {
		DoOnStart();
		//.
		DoOnResume();
	}

	@Override
	public void Stop() {
		DoOnPause();
		//.
		DoOnStop();
	}
	
	@Override
	public void Resume() {
		DoOnResume();
	}
	
	@Override
	public void Pause() {
		DoOnPause();
	}
	
	@Override
	public void Show() {
		ParentLayout.setVisibility(View.VISIBLE);
		//.
		if (WorkSpace != null)
			WorkSpace.setVisibility(View.VISIBLE);
		if (WorkSpaceOverlay != null)
			WorkSpaceOverlay.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void Hide() {
		if (WorkSpaceOverlay != null)
			WorkSpaceOverlay.setVisibility(View.GONE);
		if (WorkSpace != null)
			WorkSpace.setVisibility(View.GONE);
		//.
		ParentLayout.setVisibility(View.GONE);
	}
	
	@Override
	public boolean IsVisible() {
		return ParentLayout.isShown();
	}
	
	public void DoOnStart() {
		flRunning = true;
	}

	public void DoOnStop() {
		flRunning = false;
		//.
		MediaPlayer_Finalize();
	}
	
	public void DoOnResume() {
		CoGeoMonitorObjects_LocationUpdating_Intialize();
		//.
		PostStartUpdatingSpaceImage();
		//.
		flVisible = true;
	}
	
	public void DoOnPause() {
		flVisible = false;
		//.
		CancelUpdatingSpaceImage();
		// . cancel ReflectionWindow subscription for updates
		try {
			ReflectionWindow.UpdateSubscription_Free();
		} catch (Exception E) {
			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		CoGeoMonitorObjects_LocationUpdating_Finalize();
	}
	
	public void DoOnBackPressed() {
		if (ObjectCreatingGallery_Active()) {
			ObjectCreatingGallery_Stop();
			return; //. ->
		}	
		//.
		ParentActivity.finish();
	}

	private void InitializeUser(boolean flUserSession) throws Exception {
		User = Server.InitializeUser(Configuration.UserID,
				Configuration.UserPassword, flUserSession);
		// . add receiver
		UserIncomingMessageReceiver = new TUserIncomingMessageReceiver(User);
	}

	private void FinalizeUser() throws IOException {
		// . remove receiver
		if (UserIncomingMessageReceiver != null) {
			UserIncomingMessageReceiver.Destroy();
			UserIncomingMessageReceiver = null;
		}
		// .
		if (User != null) {
			if (User.IncomingMessages != null)
				User.IncomingMessages.Save();
			User = null;
		}
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
			Configuration.ReflectionWindow_ViewMode_Tiles_Compilation = pDescriptors
					.ToString();
			Configuration.flChanged = true;
			// .
			StartUpdatingSpaceImage();
		}
	}

	public synchronized int GetNavigationMode() {
		return NavigationMode;
	}

	public void SetNavigationMode(int pNavigationMode) throws Exception {
		if (pNavigationMode == NavigationMode)
			return; // . ->
		// .
		synchronized (this) {
			NavigationMode = pNavigationMode;
		}
		WorkSpace.Reinitialize(this);
		// .
		Configuration.ReflectionWindow_NavigationMode = NavigationMode;
		Configuration.flChanged = true;
		// .
		StartUpdatingSpaceImage();
	}

	public void WorkSpace_Buttons_Recreate(boolean flReinitializeWorkSpace) throws Exception {
		TWorkSpace.TButtons.TButton[] Buttons = new TWorkSpace.TButtons.TButton[BUTTONS_COUNT];
		float ButtonWidth = 36.0F * metrics.density;
		float ButtonHeight = 64.0F * metrics.density;
		float X = 1.0F * metrics.density;
		float Y = 0.0F * metrics.density;
		switch (ButtonsStyle) {
			
		case BUTTONS_STYLE_BRIEF:
			Buttons[BUTTON_URLS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "B", Color.BLUE);
			Y += ButtonHeight;
			Buttons[BUTTON_UPDATE] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "!", Color.YELLOW);
			Y += ButtonHeight;
			Buttons[BUTTON_SHOWREFLECTIONPARAMETERS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "=", Color.YELLOW);
			Y += ButtonHeight;
			Buttons[BUTTON_OBJECTS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "O", Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_ELECTEDPLACES] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "*", Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_MAPOBJECTSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "?", Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_PREVWINDOW] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "<", Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_CREATINGGALLERY] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "G", Color.RED);
			Y += ButtonHeight;
			Buttons[BUTTON_EDITOR] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "+", Color.RED);
			Buttons[BUTTON_EDITOR].flEnabled = false;
			Y += ButtonHeight;
			Buttons[BUTTON_USERSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "U", Color.WHITE);
			Y += ButtonHeight;
			if (((Reason == REASON_UNKNOWN) || (Reason == REASON_MAIN) || (Reason == REASON_USERPROFILECHANGED)) && !Configuration.GeoLog_flHide) {
				final int ActiveColor = Color.CYAN;
				final int PassiveColor = Color.BLACK;
				Buttons[BUTTON_TRACKER] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, X, Y, ButtonWidth, ButtonHeight, "@", ActiveColor);
				Buttons[BUTTON_TRACKER].SetStateColorProvider(new TWorkSpace.TButtons.TButton.TStateColorProvider() {
					
					@Override
					public int GetStateColor() {
						if (TTracker.TrackerIsEnabled())
							return ActiveColor; // . ->
						else
							return PassiveColor; // . ->
					};
				});
				Y += ButtonHeight;
			}
			break; //. >

		case BUTTONS_STYLE_NORMAL:
			Buttons[BUTTON_URLS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, ParentActivity.getString(R.string.SBookmarks), Color.BLUE);
			Y += ButtonHeight;
			Buttons[BUTTON_UPDATE] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SUpdate1), Color.YELLOW);
			Y += ButtonHeight;
			Buttons[BUTTON_SHOWREFLECTIONPARAMETERS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SViewConfiguration), Color.YELLOW);
			Y += ButtonHeight;
			Buttons[BUTTON_OBJECTS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SObjects), Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_ELECTEDPLACES] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SPlaces), Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_MAPOBJECTSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SSearch2), Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_PREVWINDOW] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SBack), Color.GREEN);
			Y += ButtonHeight;
			Buttons[BUTTON_CREATINGGALLERY] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SGallery), Color.RED);
			Y += ButtonHeight;
			Buttons[BUTTON_EDITOR] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SDrawing2), Color.RED);
			Buttons[BUTTON_EDITOR].flEnabled = false;
			Y += ButtonHeight;
			Buttons[BUTTON_USERSEARCH] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.SFindUser), Color.WHITE);
			Y += ButtonHeight;
			if (((Reason == REASON_UNKNOWN) || (Reason == REASON_MAIN) || (Reason == REASON_USERPROFILECHANGED)) && !Configuration.GeoLog_flHide) {
				final int ActiveColor = Color.CYAN;
				final int PassiveColor = Color.BLACK;
				Buttons[BUTTON_TRACKER] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_LEFT, TWorkSpace.TButtons.TButton.STYLE_RECTANGLE, X, Y, ButtonWidth, ButtonHeight, context.getString(R.string.STracker), ActiveColor);
				Buttons[BUTTON_TRACKER].SetStateColorProvider(new TWorkSpace.TButtons.TButton.TStateColorProvider() {
					
					@Override
					public int GetStateColor() {
						if (TTracker.TrackerIsEnabled())
							return ActiveColor; // . ->
						else
							return PassiveColor; // . ->
					};
				});
				Y += ButtonHeight;
			}
			break; //. >
		}
		Buttons[BUTTON_COMPASS] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_RIGHT, TButton.STYLE_ELLIPSE, WorkSpace.Width-WorkSpace.RotatingZoneWidth+2.0F*metrics.density, 2.0F * metrics.density, WorkSpace.RotatingZoneWidth-4.0F*metrics.density, WorkSpace.RotatingZoneWidth-4.0F*metrics.density, "N", Color.BLUE);
		Buttons[BUTTON_MYUSERPANEL] = new TWorkSpace.TButtons.TButton(BUTTONS_GROUP_RIGHT, TButton.STYLE_ELLIPSE, WorkSpace.Width-WorkSpace.RotatingZoneWidth+2.0F*metrics.density, 2.0F*metrics.density, WorkSpace.RotatingZoneWidth-4.0F*metrics.density, WorkSpace.RotatingZoneWidth-4.0F*metrics.density, context.getString(R.string.SI1), Color.CYAN);
		WorkSpace.Buttons.SetButtons(Buttons);
		// .
		if (flReinitializeWorkSpace)
			WorkSpace.Reinitialize(this);
	}

	public void WorkSpace_Buttons_Update(
			TRWLevelTileContainer[] LevelTileContainers) {
		boolean flUserDrawable = false;
		if (LevelTileContainers != null)
			for (int I = 0; I < LevelTileContainers.length; I++)
				if ((LevelTileContainers[I] != null)
						&& (LevelTileContainers[I].TileLevel.IsUserDrawable())) {
					flUserDrawable = true;
					break; // . >
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
			_SpaceImageUpdating = new TSpaceImageUpdating(this, Delay, flUpdateProxySpace);
		} catch (Exception E) {
		}
		if (WorkSpace != null)
			WorkSpace.StartDraw();
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
		MessageHandler.obtainMessage(MESSAGE_STARTUPDATESPACEIMAGE)
				.sendToTarget();
	}

	public void StartUpdatingCurrentSpaceImage() {
		try {
			if (SpaceImage != null)
				SpaceImage.GrayScale();
			if (WorkSpace != null)
				WorkSpace.Draw();
			// .
			StartUpdatingSpaceImage();
		} catch (Throwable E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(context, context.getString(R.string.SErrorOfUpdatingImage) + S,
					Toast.LENGTH_LONG).show();
		}
	}

	public synchronized void CancelUpdatingSpaceImage() {
		if (_SpaceImageUpdating != null) {
			_SpaceImageUpdating.CancelByCanceller();
			_SpaceImageUpdating = null;
		}
	}

	public synchronized void CancelAndWaitUpdatingSpaceImage()
			throws InterruptedException {
		if (_SpaceImageUpdating != null) {
			_SpaceImageUpdating.CancelByCancellerAndWait();
			_SpaceImageUpdating = null;
		}
	}

	public synchronized TSpaceImageUpdating GetSpaceImageUpdating() {
		return _SpaceImageUpdating;
	}

	public void ResetNavigationAndUpdateCurrentSpaceImage() {
		// . ResetNavigationTransformatrix();
		// .
		if (WorkSpace != null)
			WorkSpace.Update();
	}

	/*
	 * public void ResetNavigationTransformatrix() { synchronized
	 * (NavigationTransformatrix) { NavigationTransformatrix.reset(); } }
	 */

	public void SetReflectionWindow(TReflectionWindowStruc RWS, boolean flUpdate) {
		ReflectionWindowTransformatrix.reset();
		// .
		ReflectionWindow.Assign(RWS);
		// .
		ResetNavigationAndUpdateCurrentSpaceImage();
		// .
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void SetReflectionWindow(TReflectionWindowStruc RWS) {
		SetReflectionWindow(RWS, true);
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
		// .
		ReflectionWindowTransformatrix.reset();
		//.
		if (SpaceImage != null)
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postTranslate((float) dX,
						(float) dY);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postTranslate((float) dX,
							(float) dY);
			}
		// .
		ReflectionWindow.SetReflection(Position.X, Position.Y);
		// .
		ResetNavigationAndUpdateCurrentSpaceImage();
		// .
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void MoveReflectionWindow(TXYCoord Position) {
		MoveReflectionWindow(Position, true);
	}

	public void RotateReflectionWindow(double Angle, boolean flUpdate) {
		ReflectionWindowTransformatrix.reset();
		//.
		if (SpaceImage != null)
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postRotate(
						(float) (-Angle * 180.0 / Math.PI), ReflectionWindow.Xmd,
						ReflectionWindow.Ymd);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postRotate(
							(float) (-Angle * 180.0 / Math.PI),
							ReflectionWindow.Xmd, ReflectionWindow.Ymd);
			}
		// .
		ReflectionWindow.RotateReflection(Angle);
		// .
		ResetNavigationAndUpdateCurrentSpaceImage();
		// .
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void RotateReflectionWindow(double Angle) {
		RotateReflectionWindow(Angle, true);
	}

	public void TransformReflectionWindow(TReflectionWindowStruc RW,
			boolean flUpdate) {
		int RW_Xmd, RW_Ymd;
		TXYCoord Pmd;
		double ScaleFactor;
		double Alpha;
		synchronized (ReflectionWindow) {
			ReflectionWindow.ActualityInterval.Set(RW.BeginTimestamp,
					RW.EndTimestamp);
			// .
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
		if (SpaceImage != null)
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
		//.
		ReflectionWindowTransformatrix.postRotate(
				-(float) (Gamma * 180.0 / Math.PI), ReflectionWindow.Xmd,
				ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postScale((float) (1.0 / ScaleFactor),
				(float) (1.0 / ScaleFactor), ReflectionWindow.Xmd,
				ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postTranslate(-(float) dX, -(float) dY);
		// .
		ReflectionWindow
				.MultiplyReflectionByMatrix(ReflectionWindowTransformatrix);
		// .
		ReflectionWindowTransformatrix.reset();
		// .
		ResetNavigationAndUpdateCurrentSpaceImage();
		// .
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void TransformReflectionWindow(TReflectionWindowStruc RW) {
		TransformReflectionWindow(RW, true);
	}

	public void TranslateReflectionWindow(float dX, float dY, boolean flUpdate) {
		ReflectionWindowTransformatrix.reset();
		//.
		if (SpaceImage != null)
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postTranslate(dX, dY);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postTranslate(dX, dY);
			}
		//.
		ReflectionWindow.PixShiftReflection(dX, dY);
		//.
		ResetNavigationAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void TranslateReflectionWindow(float dX, float dY) {
		TranslateReflectionWindow(dX, dY, true);
	}

	public void ScaleReflectionWindow(float Scale, boolean flUpdate) {
		ReflectionWindowTransformatrix.reset();
		//.
		if (SpaceImage != null)
			synchronized (SpaceImage) {
				SpaceImage.ResultBitmapTransformatrix.postScale(Scale,Scale, SpaceImage.Width()/2.0F, SpaceImage.Height()/2.0F);
				if (SpaceImage.flSegments)
					SpaceImage.SegmentsTransformatrix.postScale(Scale,Scale, SpaceImage.Width()/2.0F, SpaceImage.Height()/2.0F);
			}
		//.
		ReflectionWindow.ChangeScaleReflection(Scale);
		//.
		ResetNavigationAndUpdateCurrentSpaceImage();
		//.
		if (flUpdate)
			StartUpdatingSpaceImage();
	}

	public void ScaleReflectionWindow(float Scale) {
		ScaleReflectionWindow(Scale, true);
	}

	public void SetReflectionWindowByLocation(TLocation Location) {
		if (ReflectionWindow.IsValid())
			TransformReflectionWindow(Location.RW);
		else
			SetReflectionWindow(Location.RW);
	}

	public double ReflectionWindowToTheNorthPoleAlignAngle() throws Exception {
		TGeoCoord GCRD;
		TXYCoord Crd;
		double X0, Y0, X1, Y1;
		double Geo_X0, Geo_Y0, Geo_X1, Geo_Y1;
		double ALFA, BETTA, GAMMA;
		// .
		synchronized (ReflectionWindow) {
			Geo_X0 = ReflectionWindow.Xcenter;
			Geo_Y0 = ReflectionWindow.Ycenter;
			X1 = (ReflectionWindow.X1 + ReflectionWindow.X2) / 2.0;
			Y1 = (ReflectionWindow.Y1 + ReflectionWindow.Y2) / 2.0;
		}
		// .
		GCRD = ConvertXYCoordinatesToGeo(Geo_X0, Geo_Y0, Configuration.GeoSpaceDatumID);
		Crd = ConvertGeoCoordinatesToXY(GCRD.Datum, GCRD.Latitude,
				GCRD.Longitude + (1.0/* Grad */), 0.0/* Altitude */);
		// .
		Geo_X1 = Crd.X;
		Geo_Y1 = Crd.Y;
		X0 = Geo_X0;
		Y0 = Geo_Y0;
		if ((Geo_X1 - Geo_X0) != 0)
			ALFA = Math.atan((Geo_Y1 - Geo_Y0) / (Geo_X1 - Geo_X0));
		else if ((Geo_Y1 - Geo_Y0) >= 0)
			ALFA = Math.PI / 2;
		else
			ALFA = -Math.PI / 2;
		if ((X1 - X0) != 0)
			BETTA = Math.atan((Y1 - Y0) / (X1 - X0));
		else if ((Y1 - Y0) >= 0)
			BETTA = Math.PI / 2;
		else
			BETTA = -Math.PI / 2;
		GAMMA = (ALFA - BETTA);
		if ((Geo_X1 - Geo_X0) * (X1 - X0) < 0)
			if ((Geo_Y1 - Geo_Y0) * (Y1 - Y0) >= 0)
				GAMMA = GAMMA - Math.PI;
			else
				GAMMA = GAMMA + Math.PI;
		GAMMA = -GAMMA;
		if (GAMMA < -Math.PI)
			GAMMA = GAMMA + 2 * Math.PI;
		else if (GAMMA > Math.PI)
			GAMMA = GAMMA - 2 * Math.PI;
		return GAMMA;
	}

	private class TReflectionWindowToNorthPoleAlignning extends
			TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_ROTATION = 1;
		private static final int MESSAGE_ROTATIONISDONE = 2;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 3;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 4;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 5;

		private ProgressDialog progressDialog;

		public TReflectionWindowToNorthPoleAlignning() {
    		super();
    		//.
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				double Angle;
				MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
						.sendToTarget();
				try {
					Angle = ReflectionWindowToTheNorthPoleAlignAngle();
					// .
					Thread.sleep(100);
				} finally {
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
							.sendToTarget();
				}
				// .
				if (Canceller.flCancel)
					return; // . ->
				// .
				while (Math.abs(Angle) > Math.PI / 32) {
					MessageHandler
							.obtainMessage(
									MESSAGE_ROTATION,
									((Double) Math.PI / 32.0 * (Angle / Math
											.abs(Angle)))).sendToTarget();
					Thread.sleep(20);
					// .
					Angle = Angle - Math.PI / 32.0 * (Angle / Math.abs(Angle));
				}
				;
				MessageHandler.obtainMessage(MESSAGE_ROTATIONISDONE, Angle)
						.sendToTarget();
			} catch (InterruptedException E) {
			} catch (CancelException CE) {
			} catch (NullPointerException NPE) {
				if (TReflectorComponent.this.flVisible)
					MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, NPE)
							.sendToTarget();
			} catch (Exception E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_ROTATION:
						if (Canceller.flCancel)
							break; // . >
						// .
						double Angle = (Double) msg.obj;
						RotateReflectionWindow(Angle, false);
						// .
						break; // . >

					case MESSAGE_ROTATIONISDONE:
						if (Canceller.flCancel)
							break; // . >
						// .
						Angle = (Double) msg.obj;
						RotateReflectionWindow(Angle);
						// .
						break; // . >

					case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
							break; // . >
						Exception E = (Exception) msg.obj;
						Toast.makeText(context, E.getMessage(),
								Toast.LENGTH_LONG).show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_SHOW:
						progressDialog = new ProgressDialog(context);
						progressDialog.setMessage(context
								.getString(R.string.SAligningToTheNorthPole));
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
						// .
						progressDialog.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_HIDE:
						if (flRunning)
							progressDialog.dismiss();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_PROGRESS:
						progressDialog.setProgress((Integer) msg.obj);
						// .
						break; // . >
					}
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};
	}

	public void ShowPrevWindow() {
		TReflectionWindowStruc CurrentRWS = ReflectionWindow.GetWindow();
		while (true) {
			TReflectionWindowStruc LastRWS = LastWindows.Pop(); // . skip
																// current
																// window
			if (LastRWS == null)
				return; // . ->
			if (!LastRWS.IsEqualTo(CurrentRWS)) {
				TransformReflectionWindow(LastRWS);
				return; // . ->
			}
		}
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
		// .
		if (flUpdateImage)
			StartUpdatingCurrentSpaceImage();
	}

	public void ClearReflectionsAndHints(boolean flUpdateImage)
			throws IOException {
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

	private void SelectedObj_Set(TSelectableObj pSelectedObj) {
		SelectedObj = pSelectedObj;
	}

	private void SelectedObj_Clear() {
		SelectedObj = null;
	}

	public boolean SelectedObj_Exists() {
		return (SelectedObj != null);
	}
	
	private void SelectedObj_CancelProcessing() {
		if (SelectedComponentTypedDataFileNamesLoading != null)
			SelectedComponentTypedDataFileNamesLoading.Cancel();
		if (SelectedComponentTypedDataFileLoading != null)
			SelectedComponentTypedDataFileLoading.Cancel();
	}

	private void EditingObj_Set(TEditableObj pEditingObj) {
		EditingObj = pEditingObj;
	}
	
	private void EditingObj_Clear() {
		EditingObj = null;
	}
	
	public byte[] GetVisualizationOwnerDataDocument(int ptrObj, int Format,
			int DataType, boolean flWithComponents) throws Exception,
			IOException {
		String URL1 = Server.Address;
		// . add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
				+ "/" + Long.toString(User.UserID);
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
								context.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
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
				+ "/" + Long.toString(User.UserID);
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
								context.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
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
				+ "/" + Long.toString(User.UserID);
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
								context.getString(R.string.SConnectionIsClosedUnexpectedly)); // .
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

	@SuppressWarnings("unused")
	private AlertDialog ComponentTypedDataFiles_CreateSelectorPanel(
			TComponentTypedDataFiles pComponentTypedDataFiles,
			Activity ParentActivity) {
		final TComponentTypedDataFiles ComponentTypedDataFiles = pComponentTypedDataFiles;
		final CharSequence[] _items = new CharSequence[ComponentTypedDataFiles.Items.length];
		for (int I = 0; I < ComponentTypedDataFiles.Items.length; I++)
			_items[I] = ComponentTypedDataFiles.Items[I].DataName
					+ "("
					+ SpaceDefines.TYPEDDATAFILE_TYPE_String(
							ComponentTypedDataFiles.Items[I].DataType, context)
					+ ")";
		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		builder.setTitle(R.string.SFiles);
		builder.setNegativeButton(context.getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {

					private TComponentTypedDataFiles _ComponentTypedDataFiles = ComponentTypedDataFiles;

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							TComponentTypedDataFile ComponentTypedDataFile = _ComponentTypedDataFiles.Items[arg1];
							if (ComponentTypedDataFile.IsLoaded()) {
								ComponentTypedDataFile.Open(User, TReflectorComponent.this.ParentActivity);
							} else {
								if (SelectedComponentTypedDataFileLoading != null)
									SelectedComponentTypedDataFileLoading.Cancel();
								SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(
										TReflectorComponent.this, ComponentTypedDataFile,
										MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
							}
						} catch (Exception E) {
							Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
							return; // . ->
						}
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

	public void ShowEditor() {
		Intent intent = new Intent(context, TReflectionWindowEditorPanel.class);
		intent.putExtra("ComponentID", ID);
		intent.putExtra("AskForLastDrawings", true);
		intent.putExtra("AskForUncommittedDrawings", true);
		ParentActivity.startActivity(intent);
	}

	public TGeoCoord ConvertXYCoordinatesToGeo(double X, double Y, int DatumID) throws Exception {
		TGeoSpaceFunctionality GSF = (TGeoSpaceFunctionality) (User.Space.TypesSystem.SystemTGeoSpace.TComponentFunctionality_Create(Configuration.GeoSpaceID));
		try {
			return GSF.ConvertXYCoordinatesToGeo(X, Y, DatumID);
		} finally {
			GSF.Release();
		}
	}

	public TGeoCoord ConvertXYCoordinatesToGeo(double X, double Y) throws Exception {
		return ConvertXYCoordinatesToGeo(X,Y, Configuration.GeoSpaceDatumID);
	}
	
	public TXYCoord ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude, double Altitude) throws Exception {
		TGeoSpaceFunctionality GSF = (TGeoSpaceFunctionality)User.Space.TypesSystem.SystemTGeoSpace.TComponentFunctionality_Create(Configuration.GeoSpaceID);
		try {
			return GSF.ConvertGeoCoordinatesToXY(DatumID, Latitude,Longitude,Altitude);
		} finally {
			GSF.Release();
		}
	}

	public TXYCoord ConvertGeoCoordinatesToXY(double Latitude, double Longitude, double Altitude) throws Exception {
		return ConvertGeoCoordinatesToXY(Configuration.GeoSpaceDatumID, Latitude,Longitude,Altitude);
	}
	
	public TXYCoord ConvertGeoCoordinatesToXY(TGeoCoord GeoCoord) throws Exception {
		return ConvertGeoCoordinatesToXY(GeoCoord.Datum, GeoCoord.Latitude,	GeoCoord.Longitude, GeoCoord.Altitude);
	}
	
	public double GetGeoDistance(double Latitude, double Longitude, double Latitude1, double Longitude1) {
		TGeoDatum Datum = TGeoDatum.GetDatumByID(Configuration.GeoSpaceDatumID);
		return Datum.GetDistance(Latitude,Longitude, Latitude1,Longitude1);
	}

	public void Tracker_ShowCurrentLocation() {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(
					context,
					R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable,
					Toast.LENGTH_SHORT).show();
			return; // . ->
		}
		TGPSFixValue Fix;
		TXYCoord Crd = new TXYCoord();
		try {
			Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
			if (!Fix.IsSet()) {
				Toast.makeText(context, R.string.SCurrentPositionIsUnavailable,
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			if (Fix.IsEmpty()) {
				Toast.makeText(context, R.string.SCurrentPositionIsUnknown,
						Toast.LENGTH_SHORT).show();
				return; // . ->
			}
			Crd = ConvertGeoCoordinatesToXY(TTracker.DatumID, Fix.Latitude,
					Fix.Longitude, Fix.Altitude);
		} catch (Exception E) {
			Toast.makeText(context, E.getMessage(), Toast.LENGTH_SHORT).show();
			return; // . ->
		}
		MoveReflectionWindow(Crd);
		// .
		if (!Fix.IsAvailable())
			Toast.makeText(context, R.string.SNoCurrentPositionSetByLastCoords,
					Toast.LENGTH_LONG).show();
	}

	private void ObjectCreatingGallery_Start() throws Exception {
		WorkSpaceOverlay = (TObjectCreationGalleryOverlay)ParentLayout.findViewById(R.id.ivObjectCreatingGalleryOverlay);
		WorkSpaceOverlay.Initialize(this);
		WorkSpace.PostDraw();
	}
	
	private void ObjectCreatingGallery_Stop() {
		if (WorkSpaceOverlay instanceof TObjectCreationGalleryOverlay) {
			WorkSpaceOverlay.Finalize();
			WorkSpaceOverlay = null;
			//.
			WorkSpace.requestFocus();
			WorkSpace.PostDraw();
		}	
	}
	
	private boolean ObjectCreatingGallery_Active() {
		return (WorkSpaceOverlay instanceof TObjectCreationGalleryOverlay);
	}
	
	private void ObjectCreatingGallery_StartCreatingObject(TObjectCreationGalleryOverlay.TItems.TItem Prototype, double X, double Y, float MaxSize) throws Exception {
		ObjectCreatingGallery_Stop();
		//.
		TReflectionWindowStruc RW = ReflectionWindow.GetWindow();
		TXYCoord Position = RW.ConvertToReal(X,Y);
		if (Position == null)
			throw new Exception("unknown space position"); //. =>
		EditingObj_Set(new TEditableObj(Prototype.Visualization_Obj,Prototype.Visualization_flSetup, Prototype.idTComponent,Prototype.idComponent, this, true, Position.X,Position.Y, MaxSize));
		//.
		TWorkSpace.TDialogPanel DialogPanel = new TWorkSpace.TDialogPanel(WorkSpace);
		DialogPanel.AddButton(context.getString(R.string.SOk1), new TWorkSpace.TDialogPanel.TButton.TClickHandler() {

			@Override
			public void DoOnClick() {
				try {
					ObjectCreatingGallery_CommitCreatingObject();
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		DialogPanel.AddButton(context.getString(R.string.SCancel1), new TWorkSpace.TDialogPanel.TButton.TClickHandler() {

			@Override
			public void DoOnClick() {
				ObjectCreatingGallery_CancelCreatingObject();
			}
		});
		WorkSpace.DialogPanel_Set(DialogPanel);
		//.
		WorkSpace.DoDraw(); //. update the EditingObj view
		//.
		WorkSpace.Pointer0_Down(X,Y);
	}
	
	private void ObjectCreatingGallery_FinishCreatingObject() {
		WorkSpace.DialogPanel_Clear();
		if (EditingObj != null) 
			EditingObj.EditingIsFinished = true;
		//.
		WorkSpace.DoDraw(); 
	}
	
	private void ObjectCreatingGallery_CancelCreatingObject() {
		WorkSpace.DialogPanel_Clear();
		EditingObj_Clear();
		//.
		WorkSpace.DoDraw(); 
	}
	
	private void ObjectCreatingGallery_CommitCreatingObject() throws Exception {
		TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(context, null, new TFileSystemFileSelector.OpenDialogListener() {
        	
            @Override
            public void OnSelectedFile(String fileName) {
                final File ChosenFile = new File(fileName);
                //.
        		AlertDialog.Builder DataNameDialog = new AlertDialog.Builder(context);
        		// .
        		DataNameDialog.setTitle(R.string.SDataName);
        		DataNameDialog.setMessage(R.string.SEnterName);
        		// .
        		final EditText input = new EditText(context);
        		input.setInputType(InputType.TYPE_CLASS_TEXT);
        		DataNameDialog.setView(input);
        		// .
        		DataNameDialog.setCancelable(false);
        		DataNameDialog.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
        			
        					@Override
        					public void onClick(DialogInterface dialog, int whichButton) {
        						//. hide keyboard
        						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        						//.
        						try {
        							String Name = input.getText().toString();
        							int DataNameMaxSize = TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize;
                    				if (Name.length() > DataNameMaxSize)
                    					Name = Name.substring(0,DataNameMaxSize);
                    				final String DataName; 
                    		    	if ((Name != null) && (Name.length() > 0))
                    		    		DataName = "@"+TComponentFileStreaming.CheckAndEncodeFileNameString(Name);
                    		    	else
                    		    		DataName = "";
                    				//. creating ...
                    		    	ObjectCreatingGallery_StartCreatingObject(ChosenFile,DataName);
        						} catch (Exception E) {
        							Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
        						}
        					}
        				});
        		// .
        		DataNameDialog.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
        			
        					@Override
        					public void onClick(DialogInterface dialog, int whichButton) {
        						// . hide keyboard
        						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        						//.
        						ObjectCreatingGallery_CancelCreatingObject();
        					}
        				});
        		// .
        		DataNameDialog.show();
            }

			@Override
			public void OnCancel() {
				ObjectCreatingGallery_CancelCreatingObject();
			}
        });
    	FileSelector.show();    	
    	//.
		Toast.makeText(context, R.string.SSelectAFileToLoad, Toast.LENGTH_LONG).show();
	}
	
	private void ObjectCreatingGallery_StartCreatingObject(final File DataFile, final String DataName) {
		TAsyncProcessing Creating = new TAsyncProcessing(context,context.getString(R.string.SWaitAMoment)) {

			private String 	DataFileName;
			private  long 	DataFileSize;
			
			@Override
			public void Process() throws Exception {
		    	TTracker Tracker = TTracker.GetTracker();
		    	if (Tracker == null)
		    		throw new Exception(context.getString(R.string.STrackerIsNotInitialized)); //. =>
		    	//.
		    	TBase2DVisualizationFunctionality.TData VD = null;
				TBase2DVisualizationFunctionality.TTransformatrix VT = null;
				if (EditingObj.flSetup) {
					TXYCoord P0 = EditingObj.Obj.Nodes[0];
					TXYCoord P1 = EditingObj.Obj.Nodes[1];
					TXYCoord P2 = EditingObj.Obj.Nodes[2];
					TXYCoord P3 = EditingObj.Obj.Nodes[3];
					//.
	                VD = new TBase2DVisualizationFunctionality.TData();
	                VD.Width = Math.sqrt(Math.pow(P3.X-P0.X,2)+Math.pow(P3.Y-P0.Y,2))*EditingObj.Transformatrix.Scale;
	                VD.Nodes = new TBase2DVisualizationFunctionality.TData.TNode[2];
	                VD.Nodes[0] = new TBase2DVisualizationFunctionality.TData.TNode((P0.X+P3.X)/2.0,(P0.Y+P3.Y)/2.0);
	                VD.Nodes[1] = new TBase2DVisualizationFunctionality.TData.TNode((P1.X+P2.X)/2.0,(P1.Y+P2.Y)/2.0);
					int Cnt = VD.Nodes.length;
					for (int I = 0; I < Cnt; I++) {
						TBase2DVisualizationFunctionality.TData.TNode N = VD.Nodes[I];
						//.
					    double X = EditingObj.Transformatrix.Xbind+(N.X-EditingObj.Transformatrix.Xbind)*EditingObj.Transformatrix.Scale*Math.cos(EditingObj.Transformatrix.Rotation)+(N.Y-EditingObj.Transformatrix.Ybind)*EditingObj.Transformatrix.Scale*(-Math.sin(EditingObj.Transformatrix.Rotation))+EditingObj.Transformatrix.TranslateX;
					    double Y = EditingObj.Transformatrix.Ybind+(N.X-EditingObj.Transformatrix.Xbind)*EditingObj.Transformatrix.Scale*Math.sin(EditingObj.Transformatrix.Rotation)+(N.Y-EditingObj.Transformatrix.Ybind)*EditingObj.Transformatrix.Scale*Math.cos(EditingObj.Transformatrix.Rotation)+EditingObj.Transformatrix.TranslateY;
					    //.
					    N.X = X; N.Y = Y;
					}
					String ContentType = "png";
					byte[] ContentData;
					ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
					try {
						EditingObj.Obj.Container_Image.compress(CompressFormat.PNG, 0, BOS);
						ContentData = BOS.toByteArray(); 
					}
					finally {
						BOS.close();
					}
	                VD.Content = new TBase2DVisualizationFunctionality.TData.TContent(ContentType,ContentData);
				}
				else 
					VT = new TBase2DVisualizationFunctionality.TTransformatrix(EditingObj.Transformatrix.Xbind,EditingObj.Transformatrix.Ybind, EditingObj.Transformatrix.Scale, EditingObj.Transformatrix.Rotation, EditingObj.Transformatrix.TranslateX,EditingObj.Transformatrix.TranslateY);
				//.
				TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(EditingObj.idTComponent,EditingObj.idComponent);
				if (CF == null)
					return; // . ->
				try {
					if (VD != null)
    					CF.VisualizationData = VD;
					else
						if (VT != null)
							CF.VisualizationTransformatrix = VT;
					//.
					long idClone = CF.Clone();
					//.
			    	DataFileName = DataFile.getAbsolutePath();
					if ((DataFileName != null) && (DataFileName.length() > 0)) {
    					TComponentFunctionality CCF = User.Space.TypesSystem.TComponentFunctionality_Create(CF.idTComponent(),idClone);
    					if (CCF == null)
    						throw new Exception("could not get a clone functionality"); //. => 
    					try {
    						long idDATAFile = CCF.GetComponent(SpaceDefines.idTDATAFile);
    						if (idDATAFile == 0)
    							throw new Exception("unable to get TDATAFile component of the clone"); //. =>
    						//. prepare and send datafile
    				    	double Timestamp = OleDate.UTCCurrentTimestamp();
    						String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(DataFileName);
    						File NF = new File(NFN);
    						TFileSystem.CopyFile(new File(DataFileName), NF);
            				DataFileName = NFN;
    						DataFileSize = NF.length();
    						try {
    							Tracker.GeoLog.ComponentFileStreaming.AddItem(SpaceDefines.idTDATAFile,idDATAFile, DataFileName);
    						} catch (Exception E) {
    					    	File F = new File(DataFileName);
    					    	F.delete();
    					    	//.
    				    		throw E; //. =>
    						}
    					} finally {
    						CCF.Release();
    					}
					}
				} finally {
					CF.Release();
				}
				//.
				Thread.sleep(100);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				ObjectCreatingGallery_FinishCreatingObject();
				//.
				StartUpdatingSpaceImage(2000); //. update with delay to allow the server to update the imagery
				//.
				Toast.makeText(context, context.getString(R.string.SObjectHasBeenCreated)+"\n"+context.getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
			}

			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
			}
		};
		Creating.Start();
	}
	
	private static final int ObjectEditing_ImageMaxSize = 256;
	
	private void ObjectEditing_StartEditingObject(final TSpaceObj Obj, final boolean flSetup, final Bitmap SetupImage) throws Exception {
		TAsyncProcessing Processing = new TAsyncProcessing(context,context.getString(R.string.SWaitAMoment)) {

			private TSpaceObj 	EditObj;
			private TXYCoord 	EditObjCenter;
			private float 		EditObjMaxSize;
			
			@Override
			public void Process() throws Exception {
				TBase2DVisualizationFunctionality VF = (TBase2DVisualizationFunctionality)User.Space.TypesSystem.TComponentFunctionality_Create(Obj.idTObj,Obj.idObj);
				if (VF != null) 
					try {
						VF.Context_RemoveObj(); //. remove the object from context to reload data
						//.
						EditObj = VF.GetObj(ObjectEditing_ImageMaxSize);
						//.
						EditObjCenter = EditObj.Nodes_AveragePoint();
						//.
						if (flSetup) {
							EditObj.Container_Image = SetupImage;
							//.
							EditObj.Nodes = new TXYCoord[4];
							float W = EditObj.Container_Image.getWidth();
							float H = EditObj.Container_Image.getHeight();
							float WH = W/2.0F;
							float HH = H/2.0F;
							//.
							EditObj.Nodes[0] = new TXYCoord(-WH,-HH);
							EditObj.Nodes[1] = new TXYCoord(+WH,-HH);
							EditObj.Nodes[2] = new TXYCoord(+WH,+HH);
							EditObj.Nodes[3] = new TXYCoord(-WH,+HH);
							//.
							EditObj.Width = new TReal48(H);
							//.
							EditObjMaxSize = W;
							if (H > EditObjMaxSize)
								EditObjMaxSize = H;
						}
					}
					finally {
						VF.Release();
					}
				//.
				Thread.sleep(100);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (flSetup)
					EditingObj_Set(new TEditableObj(EditObj, flSetup, TReflectorComponent.this, true, EditObjCenter.X,EditObjCenter.Y, EditObjMaxSize));
				else
					EditingObj_Set(new TEditableObj(EditObj, TReflectorComponent.this));
				//.
				final TWorkSpace.TDialogPanel DialogPanel = new TWorkSpace.TDialogPanel(WorkSpace);
				DialogPanel.AddButton(context.getString(R.string.SOk1), new TWorkSpace.TDialogPanel.TButton.TClickHandler() {

					@Override
					public void DoOnClick() {
						try {
							ObjectEditing_CommitEditingObject();
						} catch (Exception E) {
							Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});
				DialogPanel.AddButton(context.getString(R.string.SCancel1), new TWorkSpace.TDialogPanel.TButton.TClickHandler() {

					@Override
					public void DoOnClick() {
						ObjectEditing_CancelEditingObject();
					}
				});
				//.
				WorkSpace.DialogPanel_Set(DialogPanel);
				//.
				WorkSpace.DoDraw(); //. update the EditingObj view
			}

			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
	private void ObjectEditing_FinishEditingObject() {
		WorkSpace.DialogPanel_Clear();
		if (EditingObj != null) 
			EditingObj.EditingIsFinished = true;
		//.
		WorkSpace.DoDraw(); 
	}
	
	private void ObjectEditing_CancelEditingObject() {
		WorkSpace.DialogPanel_Clear();
		EditingObj_Clear();
		//.
		WorkSpace.DoDraw(); 
	}
	
	private void ObjectEditing_CommitEditingObject() throws Exception {
		TAsyncProcessing Processing = new TAsyncProcessing(context,context.getString(R.string.SWaitAMoment)) {

			@Override
			public void Process() throws Exception {
		    	TTracker Tracker = TTracker.GetTracker();
		    	if (Tracker == null)
		    		throw new Exception(context.getString(R.string.STrackerIsNotInitialized)); //. =>
		    	//.
		    	TBase2DVisualizationFunctionality.TData VD = null;
				TBase2DVisualizationFunctionality.TTransformatrix VT = null;
				if (EditingObj.flSetup) {
					TXYCoord P0 = EditingObj.Obj.Nodes[0];
					TXYCoord P1 = EditingObj.Obj.Nodes[1];
					TXYCoord P2 = EditingObj.Obj.Nodes[2];
					TXYCoord P3 = EditingObj.Obj.Nodes[3];
					//.
	                VD = new TBase2DVisualizationFunctionality.TData();
	                VD.Width = Math.sqrt(Math.pow(P3.X-P0.X,2)+Math.pow(P3.Y-P0.Y,2))*EditingObj.Transformatrix.Scale;
	                VD.Nodes = new TBase2DVisualizationFunctionality.TData.TNode[2];
	                VD.Nodes[0] = new TBase2DVisualizationFunctionality.TData.TNode((P0.X+P3.X)/2.0,(P0.Y+P3.Y)/2.0);
	                VD.Nodes[1] = new TBase2DVisualizationFunctionality.TData.TNode((P1.X+P2.X)/2.0,(P1.Y+P2.Y)/2.0);
					int Cnt = VD.Nodes.length;
					for (int I = 0; I < Cnt; I++) {
						TBase2DVisualizationFunctionality.TData.TNode N = VD.Nodes[I];
						//.
					    double X = EditingObj.Transformatrix.Xbind+(N.X-EditingObj.Transformatrix.Xbind)*EditingObj.Transformatrix.Scale*Math.cos(EditingObj.Transformatrix.Rotation)+(N.Y-EditingObj.Transformatrix.Ybind)*EditingObj.Transformatrix.Scale*(-Math.sin(EditingObj.Transformatrix.Rotation))+EditingObj.Transformatrix.TranslateX;
					    double Y = EditingObj.Transformatrix.Ybind+(N.X-EditingObj.Transformatrix.Xbind)*EditingObj.Transformatrix.Scale*Math.sin(EditingObj.Transformatrix.Rotation)+(N.Y-EditingObj.Transformatrix.Ybind)*EditingObj.Transformatrix.Scale*Math.cos(EditingObj.Transformatrix.Rotation)+EditingObj.Transformatrix.TranslateY;
					    //.
					    N.X = X; N.Y = Y;
					}
					String ContentType = "png";
					byte[] ContentData;
					ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
					try {
						EditingObj.Obj.Container_Image.compress(CompressFormat.PNG, 0, BOS);
						ContentData = BOS.toByteArray(); 
					}
					finally {
						BOS.close();
					}
	                VD.Content = new TBase2DVisualizationFunctionality.TData.TContent(ContentType,ContentData);
				}
				else 
					VT = new TBase2DVisualizationFunctionality.TTransformatrix(EditingObj.Transformatrix.Xbind,EditingObj.Transformatrix.Ybind, EditingObj.Transformatrix.Scale, EditingObj.Transformatrix.Rotation, EditingObj.Transformatrix.TranslateX,EditingObj.Transformatrix.TranslateY);
				//.
				TBase2DVisualizationFunctionality VF = (TBase2DVisualizationFunctionality)User.Space.TypesSystem.TComponentFunctionality_Create(EditingObj.Obj.idTObj,EditingObj.Obj.idObj);
				if (VF != null)
					try {
						VF.SetObj(EditingObj.Obj);
						try {
							if (VD != null)
		    					VF.Setup(VD);
							else
								if (VT != null)
									VF.Transform(VT);
						}
						finally {
							//. clear data of the object
							VF.ClearObj();
						}
					} finally {
						VF.Release();
					}
				//.
				Thread.sleep(100);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				ObjectEditing_FinishEditingObject();
				//.
				SelectedObj_Clear();
				//.
				StartUpdatingSpaceImage(2000); //. update with delay to allow the server to update the imagery
			}

			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
	public void ShowConfiguration() {
		if (TUserAccess.UserAccessFileExists()) {
			final TUserAccess AR = new TUserAccess();
			if (AR.AdministrativeAccessPassword != null) {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				// .
				alert.setTitle("");
				alert.setMessage(R.string.SEnterPassword);
				// .
				final EditText input = new EditText(context);
				alert.setView(input);
				// .
				alert.setPositiveButton(R.string.SOk,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// . hide keyboard
								InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(
										input.getWindowToken(), 0);
								// .
								String Password = input.getText()
										.toString();
								if (Password.equals(AR.AdministrativeAccessPassword)) {
									Intent intent = new Intent(context, TReflectorConfigurationPanel.class);
									intent.putExtra("ComponentID", ID);
									ParentActivity.startActivityForResult(intent, REQUEST_EDIT_REFLECTOR_CONFIGURATION);
								} else
									Toast.makeText(context,
											R.string.SIncorrectPassword,
											Toast.LENGTH_LONG).show();
							}
						});
				// .
				alert.setNegativeButton(R.string.SCancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// . hide keyboard
								InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(
										input.getWindowToken(), 0);
							}
						});
				// .
				alert.show();
				// .
				return; // . >
			}
		}
		Intent intent = new Intent(context, TReflectorConfigurationPanel.class);
		intent.putExtra("ComponentID", ID);
		ParentActivity.startActivityForResult(intent, REQUEST_EDIT_REFLECTOR_CONFIGURATION);
	}

	public void ShowSelectedComponentMenu() {
		final CharSequence[] _items;
		int SelectedIdx = -1;
		_items = new CharSequence[5];
		_items[0] = context.getString(R.string.SEdit); 
		_items[1] = context.getString(R.string.SSetup); 
		_items[2] = context.getString(R.string.SRemove); 
		_items[3] = context.getString(R.string.SAddObjectToGallery); 
		_items[4] = context.getString(R.string.SGetThObjectDescriptor); 
		//.
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.SSelectedObjectMenu);
		builder.setNegativeButton(R.string.SClose,null);
		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
		    	try {
		    		switch (arg1) {
		    		
		    		case 0: //. edit component
		    			try {
		    				EditSelectedComponent();
		    			} catch (Exception E) {
		    				Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    			//.
    		    		arg0.dismiss();
    		    		//.
		    			break; //. >
		    			
		    		case 1: //. setup component
		    			try {
		    				SetupSelectedComponent();
		    			} catch (Exception E) {
		    				Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    			//.
    		    		arg0.dismiss();
    		    		//.
		    			break; //. >
		    			
		    		case 2: //. remove component
		    			RemoveSelectedComponent();
		    			//.
    		    		arg0.dismiss();
    		    		//.
		    			break; //. >
		    			
		    		case 3: //. add component to the Gallery
		    			AddSelectedComponentToGallery();
		    			//.
    		    		arg0.dismiss();
    		    		//.
		    			break; //. >

		    		case 4: //. show component descriptor
		    			ShowSelectedComponentDescriptor();
		    			//.
    		    		arg0.dismiss();
    		    		//.
		    			break; //. >
		    		}
		    	}
		    	catch (Exception E) {
		    		Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		    		//.
		    		arg0.dismiss();
		    	}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void FindComponent() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		// .
		alert.setTitle(R.string.SFindComponent);
		alert.setMessage(R.string.SEnterComponent);
		// .
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);
		// .
		alert.setPositiveButton(R.string.SOk,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// . hide keyboard
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						// .
						try {
							String CID = input.getText().toString();
							String[] SA = CID.split(":");
							if (SA.length != 2)
								throw new Exception(
										context
												.getString(R.string.SIncorrectComponentID)); // .
																								// =>
							int idTComponent = Integer.parseInt(SA[0]);
							long idComponent = Long.parseLong(SA[1]);
							// .
							TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(idTComponent,idComponent);
							if (CF == null)
								return; // . ->
							try {
								TComponentFunctionality.TPropsPanel PropsPanel = CF
										.TPropsPanel_Create(context);
								if (PropsPanel != null)
									ParentActivity.startActivity(PropsPanel.PanelActivity);
							} finally {
								CF.Release();
							}
						} catch (Exception E) {
							Toast.makeText(context, E.getMessage(),
									Toast.LENGTH_LONG).show();
						}
					}
				});
		// .
		alert.setNegativeButton(R.string.SCancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// . hide keyboard
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				});
		// .
		alert.show();
	}

	private void EditSelectedComponent() throws Exception {
		if (SelectedObj == null) {
			Toast.makeText(context, R.string.SObjectIsNotSelected, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		if (SelectedObj.Obj.Nodes.length != 4)
			throw new Exception(context.getString(R.string.SIncorrectObjectForEditing)); //. =>
		//.
		ObjectEditing_StartEditingObject(SelectedObj.Obj, false,null);
	}

	private void SetupSelectedComponent() throws Exception {
		if (SelectedObj == null) {
			Toast.makeText(context, R.string.SObjectIsNotSelected, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		if (SelectedObj.Obj.Nodes.length != 4)
			throw new Exception(context.getString(R.string.SIncorrectObjectForEditing)); //. =>
		if (SelectedObj.Obj.idTObj != SpaceDefines.idTPictureVisualization)
			throw new Exception(context.getString(R.string.SUnableToSetupForThisTypeOfVisualization)); //. =>
		//.
		String BaseFolder = null; 
		File RF = new File(TGeoLogApplication.Resources_GetCurrentFolder()+"/"+TGeoLogApplication.Resource_ImagesFolder);
		if (RF.exists())
			BaseFolder = RF.getAbsolutePath();
		//.
		TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(context, BaseFolder, ".BMP,.PNG,.JPG,.JPEG", new TFileSystemFileSelector.OpenDialogListener() {
        	
            @Override
            public void OnSelectedFile(String fileName) {
                File ChosenFile = new File(fileName);
                //.
				try {
					FileInputStream FIS = new FileInputStream(ChosenFile);
					try
					{
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inDither=false;
						options.inPurgeable=true;
						options.inInputShareable=true;
						options.inTempStorage=new byte[1024*256]; 							
						Rect rect = new Rect();
						final Bitmap Image = BitmapFactory.decodeFileDescriptor(FIS.getFD(), rect, options);
						//.
						ObjectEditing_StartEditingObject(SelectedObj.Obj, true,Image);
					}
					finally
					{
						FIS.close();
					}
				}
				catch (Throwable E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(context, S, Toast.LENGTH_LONG).show();  						
				}
            }

			@Override
			public void OnCancel() {
			}
        });
    	FileSelector.show();    	
	}
	
	private void RemoveSelectedComponent() {
		if (SelectedObj == null) {
			Toast.makeText(context, R.string.SObjectIsNotSelected, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		TOwnerSpaceObj OwnerSpaceObj = (TOwnerSpaceObj)SelectedObj.Obj; 
		if (OwnerSpaceObj.OwnerID == 0) {
			Toast.makeText(context, R.string.SSelectedObjectIsNotLoaded, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		final int idTComponent = OwnerSpaceObj.OwnerType;
		final long idComponent = OwnerSpaceObj.OwnerID;
		//.
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		//.
		alert.setTitle(R.string.SRemoval);
		alert.setMessage(R.string.SRemoveSelectedComponent);
		//.
		alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				TAsyncProcessing Removing = new TAsyncProcessing(context, context.getString(R.string.SRemoving)) {

					@Override
					public void Process() throws Exception {
						TTypeFunctionality TF = User.Space.TypesSystem.TTypeFunctionality_Create(idTComponent);
						if (TF != null)
							try {
								TF.DestroyInstance(idComponent);
							} finally {
								TF.Release();
							}
							else
								throw new Exception("there is no functionality for type, idType = "+Integer.toString(idTComponent)); //. =>
					}

					@Override
					public void DoOnCompleted() throws Exception {
						SelectedObj_Clear();
						//.
						StartUpdatingSpaceImage();
						//.
						Toast.makeText(context, R.string.SObjectHasBeenRemoved, Toast.LENGTH_LONG).show();
					}
					
					@Override
					public void DoOnException(Exception E) {
						Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
					}
				};
				Removing.Start();
			}
		});
		//.
		alert.setNegativeButton(R.string.SCancel, null);
		//.
		alert.show();
	}

	private void AddSelectedComponentToGallery() {
		if (SelectedObj == null) {
			Toast.makeText(context, R.string.SObjectIsNotSelected, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		TOwnerSpaceObj OwnerSpaceObj = (TOwnerSpaceObj)SelectedObj.Obj; 
		if (OwnerSpaceObj.OwnerID == 0) {
			Toast.makeText(context, R.string.SSelectedObjectIsNotLoaded, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		final int idTComponent = OwnerSpaceObj.OwnerType;
		final long idComponent = OwnerSpaceObj.OwnerID;
		//.
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		// .
		alert.setTitle(R.string.SAddingObject);
		alert.setMessage(R.string.SEnterNameOfObject);
		// .
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);
		// .
		alert.setPositiveButton(R.string.SOk,
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						//. hide keyboard
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						//.
						final String ComponentName = input.getText().toString();
						//.
						TAsyncProcessing Processing = new TAsyncProcessing(context, context.getString(R.string.SAdding)) {

							@Override
							public void Process() throws Exception {
								String ItemsFile = ProfileFolder()+"/"+TObjectCreationGalleryOverlay.ItemsFileName;
								TObjectCreationGalleryOverlay.TItems GalleryItems = new TObjectCreationGalleryOverlay.TItems(TReflectorComponent.this,ItemsFile);
								TObjectCreationGalleryOverlay.TItems.TItem NewItem = new TObjectCreationGalleryOverlay.TItems.TItem(idTComponent,idComponent, ComponentName);
								GalleryItems.Add(NewItem);
								//.
								Thread.sleep(100);
							}

							@Override
							public void DoOnCompleted() throws Exception {
								//. reset the gallery loaded items
								TObjectCreationGalleryOverlay GalleryOverlay = (TObjectCreationGalleryOverlay)ParentLayout.findViewById(R.id.ivObjectCreatingGalleryOverlay);
								GalleryOverlay.Items = null; 
								//.
								Toast.makeText(context, R.string.SSelectedObjectHasBeenAddedToGallery, Toast.LENGTH_LONG).show();
							}
							
							@Override
							public void DoOnException(Exception E) {
								Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
							}
						};
						Processing.Start();
					}
				});
		// .
		alert.setNegativeButton(R.string.SCancel,
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// . hide keyboard
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				});
		// .
		alert.show();
	}
	
	private void ShowSelectedComponentDescriptor() {
		if (SelectedObj == null) {
			Toast.makeText(context, R.string.SObjectIsNotSelected, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		TOwnerSpaceObj OwnerSpaceObj = (TOwnerSpaceObj)SelectedObj.Obj; 
		if (OwnerSpaceObj.OwnerID == 0) {
			Toast.makeText(context, R.string.SSelectedObjectIsNotLoaded, Toast.LENGTH_LONG).show();
			return; //. ->
		}
		final int idTComponent = OwnerSpaceObj.OwnerType;
		final long idComponent = OwnerSpaceObj.OwnerID;
		//.
		Toast.makeText(context, Integer.toString(idTComponent)+":"+Long.toString(idComponent), Toast.LENGTH_LONG).show();
	}
	
	public void ObjectTracks_Clear() {
		ObjectTracks.Clear();
		//.
		StartUpdatingCurrentSpaceImage();
	}
	
	public void ObjectTracks_AddTrack(final byte[] TrackData) {
		ObjectTracks_TrackAdding = new TAsyncProcessing() {

			private TCoGeoMonitorObjectTrack Track = new TCoGeoMonitorObjectTrack();
			
			@Override
			public void Process() throws Exception {
				Track = new TCoGeoMonitorObjectTrack();
				Track.FromByteArray1(TrackData, Canceller);
				ObjectTracks.AddNewTrack(Track);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (!flExists)
					return; //. ->
				//.
				StartUpdatingCurrentSpaceImage();
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
			}
		};
		ObjectTracks_TrackAdding.Start();
	}
	
	public void MediaPlayer_PlayAlarmSound() {
		try {
			if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
				_MediaPlayer.stop();
			_MediaPlayer = new MediaPlayer();
			// .
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			_MediaPlayer.setDataSource(context, alert);
			final AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			if (audio.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				_MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				_MediaPlayer.setLooping(true);
				_MediaPlayer.prepare();
				_MediaPlayer.start();
			}
		} catch (Exception E) {
		}
	}

	public void MediaPlayer_Finalize() {
		if (_MediaPlayer != null) {
			if (_MediaPlayer.isPlaying()) 
				_MediaPlayer.stop();
			_MediaPlayer = null;
		}
	}
}