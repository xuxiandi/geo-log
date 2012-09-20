package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources.Theme;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TElectedPlaces;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStrucStack;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHint;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Hints.TSpaceHints;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections.TSpaceReflections;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.TrackerService.TTrackerService;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.GeoLog.Utils.TUpdater;


@SuppressLint("HandlerLeak")
@SuppressWarnings("unused")
public class TReflector extends Activity implements OnTouchListener {

	public static final String ProgramVersion = "v2.240812";
	//.
	public static final String ProfileFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Geo.Log"+"/"+"PROFILEs"+"/"+"Default";
	public static final String SpaceContextFolder = ProfileFolder+"/"+"CONTEXT"+"/"+"Space";
	public static final String TypesSystemContextFolder = SpaceContextFolder+"/"+"TypesSystem";
	private static final int MaxLastWindowsCount = 10;
	private static int ShowLogoCount = 3;
	//.
	public static TReflector MyReflector;
	
	public class TReflectorConfiguration {

		private static final String ConfigurationFileName = "GeoEye.Configuration";
		private static final int 	ConfigurationFileVersion = 1;
		private static final String ReflectionWindowFileName = "ReflectionWindow.dat";
		private static final String ReflectionWindowDisabledLaysFileName = "ReflectionWindow_DisabledLays.dat";
		
		private TReflector Reflector;
		//.
		public String 	ServerAddress = "http://89.108.122.51";
		public int		ServerPort = 80;
		public int 		UserID = 2;
		public String 	UserPassword = "ra3tkq";
		public int 		GeoSpaceID = 88;
		//.
		public int		ReflectionWindow_ViewMode = VIEWMODE_TILES;
		public byte[] 	ReflectionWindowData = null;
		public int[] 	ReflectionWindow_DisabledLaysIDs = null;
		public boolean 	ReflectionWindow_flShowHints = true;
		public String 	ReflectionWindow_ViewMode_Tiles_Compilation = null;
		//. GeoLog data
		public boolean 	GeoLog_flEnabled = false;
		public boolean 	GeoLog_flServerConnection = true;
		public String 	GeoLog_ServerAddress = "89.108.122.51";
		public int 		GeoLog_ServerPort = 8282;
		public int 		GeoLog_ObjectID = 0;
		public int 		GeoLog_QueueTransmitInterval = 0;
		public boolean 	GeoLog_flSaveQueue = true;
		public int 		GeoLog_GPSModuleProviderReadInterval = 0;
		public int 		GeoLog_GPSModuleMapID = 6;
		public boolean 	GeoLog_VideoRecorderModuleEnabled = true;
		
		public TReflectorConfiguration(TReflector pReflector) {
			Reflector = pReflector;
		}
		
		public void LoadReflectionWindowDisabledLays() throws IOException {
			String FN = TReflector.ProfileFolder+"/"+ReflectionWindowDisabledLaysFileName;
			File F = new File(FN);
			if (F.exists()) { 
		    	long FileSize = F.length();
		    	FileInputStream FIS = new FileInputStream(FN);
		    	try {
					ReflectionWindow_DisabledLaysIDs = new int[(int)(FileSize/4)];
					for (int I = 0; I < ReflectionWindow_DisabledLaysIDs.length; I++) {
						byte[] BA = new byte[4];
						FIS.read(BA);
						ReflectionWindow_DisabledLaysIDs[I] = TDataConverter.ConvertBEByteArrayToInt32(BA,0);
					}
				}
				finally
				{
					FIS.close();
				}
			}
			else
				ReflectionWindow_DisabledLaysIDs = null;
		}
		
		private void _Load() throws Exception {
			String FN = TReflector.ProfileFolder+"/"+ConfigurationFileName;
			File F = new File(FN);
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
			NodeList NL;
			switch (Version) {
			case 1:
				NL = XmlDoc.getDocumentElement().getElementsByTagName("ServerAddress");
				ServerAddress = NL.item(0).getFirstChild().getNodeValue();
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("ServerPort");
				ServerPort = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserID");
				UserID = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserPassword");
				UserPassword = NL.item(0).getFirstChild().getNodeValue();
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoSpaceID");
				GeoSpaceID = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("ReflectionWindow_ViewMode");
				if (NL.getLength() > 0)
					ReflectionWindow_ViewMode = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("ReflectionWindow_flShowHints");
				if (NL.getLength() > 0)
					ReflectionWindow_flShowHints = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("ReflectionWindow_ViewMode_Tiles_Compilation");
				if ((NL.getLength() > 0) && (NL.item(0).getFirstChild() != null))
					ReflectionWindow_ViewMode_Tiles_Compilation = NL.item(0).getFirstChild().getNodeValue();
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_flEnabled");
				GeoLog_flEnabled = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_flServerConnection");
				GeoLog_flServerConnection = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
				//.				
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_ServerAddress");
				GeoLog_ServerAddress = NL.item(0).getFirstChild().getNodeValue();
				GeoLog_ServerAddress = ServerAddress; //. override address 
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_ServerPort");
				GeoLog_ServerPort = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("GeoLog_ObjectID");
				GeoLog_ObjectID = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
				//.
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					if (Tracker.GeoLog.ConnectorModule != null) {
						GeoLog_QueueTransmitInterval = (int)(Tracker.GeoLog.ConnectorModule.TransmitInterval/1000);
						GeoLog_flSaveQueue = Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled;
					}
					//.		
					if (Tracker.GeoLog.GPSModule != null) {
						GeoLog_GPSModuleProviderReadInterval = (int)(Tracker.GeoLog.GPSModule.Provider_ReadInterval/1000);
						GeoLog_GPSModuleMapID = Tracker.GeoLog.GPSModule.MapID;
					}
					//.
					if (Tracker.GeoLog.VideoRecorderModule != null)
						GeoLog_VideoRecorderModuleEnabled = Tracker.GeoLog.VideoRecorderModule.flEnabled;
				}
				break; //. >
			default:
				throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
			}
			//. load reflection window
			FN = TReflector.ProfileFolder+"/"+ReflectionWindowFileName;
			F = new File(FN);
			if (F.exists()) { 
		    	FileSize = F.length();
		    	FIS = new FileInputStream(FN);
		    	try {
						ReflectionWindowData = new byte[(int)FileSize];
						FIS.read(ReflectionWindowData);
		    	}
				finally
				{
					FIS.close(); 
				}
			}
			else
				ReflectionWindowData = null;
			//. load reflection window disabled lays
			LoadReflectionWindowDisabledLays();
		}
		
		public void Load() throws Exception {
			int TryCount = 12000;
			int SleepTime = 5000;
			for (int I = 0; I < TryCount; I++) {
				try {
					_Load();
					return; //. ->
				}
				catch (Exception E) {
					Thread.sleep(SleepTime);
				}
			}
			throw new Exception(Reflector.getString(R.string.SErrorOfConfigurationLoading)+ConfigurationFileName); //. =>
		}
		
		public void SaveReflectionWindowDisabledLays() throws IOException {
			String FN;
			TSpaceLays Lays = Reflector.ReflectionWindow.getLays();
			if (Lays != null) {
				ReflectionWindow_DisabledLaysIDs = Lays.GetDisabledLaysIDs();
				//.
				FN = TReflector.ProfileFolder+"/"+ReflectionWindowDisabledLaysFileName;
				if (ReflectionWindow_DisabledLaysIDs != null)
				{
					FileOutputStream FOS = new FileOutputStream(FN);
	                try
	                {
						for (int I = 0; I < ReflectionWindow_DisabledLaysIDs.length; I++) {
							byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(ReflectionWindow_DisabledLaysIDs[I]);
							FOS.write(BA);
						}
	                }
	                finally
	                {
	                	FOS.close();
	                }
				}
				else {
					File F = new File(FN);
					F.delete();
				}
			}
		}
		
		public void Save() throws IOException {
			String FN;
			//. save reflection window disabled lays
			SaveReflectionWindowDisabledLays();
			//. save reflection window
			ReflectionWindowData = Reflector.ReflectionWindow.GetWindow().ToByteArray();
			//.
			FN = TReflector.ProfileFolder+"/"+ReflectionWindowFileName;
			ReflectionWindowData = Reflector.ReflectionWindow.GetWindow().ToByteArray();
			if (ReflectionWindowData != null)
			{
				FileOutputStream FOS = new FileOutputStream(FN);
                try
                {
					FOS.write(ReflectionWindowData);
                }
                finally
                {
                	FOS.close();
                }
			}
			else {
				File F = new File(FN);
				F.delete();
			}
			//.
			FN = ProfileFolder+"/"+ConfigurationFileName;
			File F = new File(FN);
			if (!F.exists()) {
				F.getParentFile().mkdirs();
				F.createNewFile();
			}
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(FN);
		    try {
		    	String S;
		        serializer.setOutput(writer);
		        serializer.startDocument("UTF-8",true);
		        serializer.startTag("", "ROOT");
		        //.
	            serializer.startTag("", "Version");
	            serializer.text(Integer.toString(ConfigurationFileVersion));
	            serializer.endTag("", "Version");
		        //.
	            serializer.startTag("", "ServerAddress");
	            serializer.text(ServerAddress);
	            serializer.endTag("", "ServerAddress");
		        //.
	            serializer.startTag("", "ServerPort");
	            serializer.text(Integer.toString(ServerPort));
	            serializer.endTag("", "ServerPort");
		        //.
	            serializer.startTag("", "UserID");
	            serializer.text(Integer.toString(UserID));
	            serializer.endTag("", "UserID");
		        //.
	            serializer.startTag("", "UserPassword");
	            serializer.text(UserPassword);
	            serializer.endTag("", "UserPassword");
		        //.
	            serializer.startTag("", "GeoSpaceID");
	            serializer.text(Integer.toString(GeoSpaceID));
	            serializer.endTag("", "GeoSpaceID");
	            //.
	            serializer.startTag("", "ReflectionWindow_ViewMode");
	            serializer.text(Integer.toString(ReflectionWindow_ViewMode));
	            serializer.endTag("", "ReflectionWindow_ViewMode");
	            //.
		        if (ReflectionWindow_flShowHints)
		        	S = "1";
		        else
		        	S = "0";
	            serializer.startTag("", "ReflectionWindow_flShowHints");
	            serializer.text(S);
	            serializer.endTag("", "ReflectionWindow_flShowHints");
	            //.
	            serializer.startTag("", "ReflectionWindow_ViewMode_Tiles_Compilation");
	            serializer.text(ReflectionWindow_ViewMode_Tiles_Compilation);
	            serializer.endTag("", "ReflectionWindow_ViewMode_Tiles_Compilation");
	            //.
		        if (GeoLog_flEnabled)
		        	S = "1";
		        else
		        	S = "0";
	            serializer.startTag("", "GeoLog_flEnabled");
	            serializer.text(S);
	            serializer.endTag("", "GeoLog_flEnabled");
	            //.
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
		        serializer.endTag("", "ROOT");
		        serializer.endDocument();
		    }
		    finally {
		    	writer.close();
		    }
		}
		
		public void Validate() throws Exception {
			Reflector.ServerAddress = ServerAddress;
			Reflector.User = new TReflectorUser(UserID,UserPassword);
			//.
			Reflector.CoGeoMonitorObjects = new TReflectorCoGeoMonitorObjects(Reflector);
			if (Reflector.CoGeoMonitorObjectsLocationUpdating != null) {
				Reflector.CoGeoMonitorObjectsLocationUpdating.Cancel();
				Reflector.CoGeoMonitorObjectsLocationUpdating = null;
			}
			Reflector.CoGeoMonitorObjectsLocationUpdating = (TReflector.this).new TCoGeoMonitorObjectsLocationUpdating(Reflector); 
			//. validate tracker
			try {
				TTrackerService _Service = TTrackerService.GetService();
				if (_Service != null)
					_Service.SetServicing(false);
				//. Set tracker configuration as well
				TTracker Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					Tracker.GeoLog.flEnabled = Reflector.Configuration.GeoLog_flEnabled;
					Tracker.GeoLog.UserID = Reflector.Configuration.UserID;
					Tracker.GeoLog.UserPassword = Reflector.Configuration.UserPassword;
					Tracker.GeoLog.ObjectID = Reflector.Configuration.GeoLog_ObjectID;
					//.
					Tracker.GeoLog.SaveConfiguration();
				}
				//.
				TTracker.FreeTracker();
				TTracker.CreateTracker(Reflector);
				//.
				Tracker = TTracker.GetTracker();
				if (Tracker != null) {
					if (Tracker.GeoLog.ConnectorModule != null) {
						Tracker.GeoLog.ConnectorModule.flServerConnectionEnabled = Reflector.Configuration.GeoLog_flServerConnection;
						Tracker.GeoLog.ConnectorModule.ServerAddress = Reflector.Configuration.ServerAddress;
						Tracker.GeoLog.ConnectorModule.ServerPort = Reflector.Configuration.GeoLog_ServerPort;
						Tracker.GeoLog.ConnectorModule.TransmitInterval = Reflector.Configuration.GeoLog_QueueTransmitInterval*1000;
						Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled = Reflector.Configuration.GeoLog_flSaveQueue;
					}
					if (Tracker.GeoLog.GPSModule != null) {
						Tracker.GeoLog.GPSModule.Provider_ReadInterval = Reflector.Configuration.GeoLog_GPSModuleProviderReadInterval*1000;
						Tracker.GeoLog.GPSModule.MapID = Reflector.Configuration.GeoLog_GPSModuleMapID;
					}
					if (Tracker.GeoLog.VideoRecorderModule != null)
						Tracker.GeoLog.VideoRecorderModule.flEnabled = Reflector.Configuration.GeoLog_VideoRecorderModuleEnabled;
					//.
					Tracker.GeoLog.SaveConfiguration();
				}
				//.
				TTracker.FreeTracker();
				TTracker.CreateTracker(Reflector);
				//. start tracker service if needed
				if (_Service != null)
					_Service.SetServicing(true);
			} catch (Exception E) {
	            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			//.
			Reflector.StartUpdatingSpaceImage();
		}
	}
	
	public class TWorkSpace extends ImageView {

    	public class TButton {
    		
    		public static final int STATUS_UP	= 0;
    		public static final int STATUS_DOWN = 1;
    		
    		public int Left;
    		public int Top;
    		public int Width;
    		public int Height;
    		public String 	Name;
    		public int		TextColor = Color.RED;
    		public int 		Status;
    		
    		public TButton(int pLeft, int pTop, int pWidth, int pHeight, String pName, int pTextColor) {
    			Left = pLeft;
    			Top = pTop;
    			Width = pWidth;
    			Height = pHeight;
    			Name = pName;
    			TextColor = pTextColor;
    			Status = STATUS_UP;
    		}
    		
    		public void SetStatus(int pStatus) {
    			Status = pStatus;
    		}
    	}
    	
    	public class TButtons {
    		
    		public TWorkSpace WorkSpace;
    		public TButton[] Items;
            public Paint paint = new Paint();
            public int  DownButtonIndex = -1;
    		public long	DownButtons_Time = Calendar.getInstance().getTime().getTime();
    		
    		public TButtons(TWorkSpace pWorkSpace) {
    			WorkSpace = pWorkSpace;
    			Items = new TButton[0];
    		}
    		
    		public void SetButtons(TButton[] Buttons) {
    			Items = Buttons;
    		}
    		
    		public void Draw(Canvas canvas) {
                float[] Frame = new float[16];
                for (int I = 0; I < Items.length; I++)
                {
                	TButton Item = Items[I]; 
                	if (Item.Status == TButton.STATUS_DOWN) {
                		paint.setColor(Color.RED);
                		paint.setAlpha(100);
                	}
                	else {
                		paint.setColor(Color.GRAY);
                		paint.setAlpha(120);
                	}
            		canvas.drawRect(Item.Left,Item.Top, Item.Left+Item.Width,Item.Top+Item.Height, paint);
        			paint.setStrokeWidth(0.5F);
        			paint.setColor(Color.WHITE);
        			Frame[0] = Item.Left; Frame[1] = Item.Top; Frame[2] = Item.Left+Item.Width; Frame[3] = Item.Top; 
        			Frame[4] = Item.Left+Item.Width; Frame[5] = Item.Top; Frame[6] = Item.Left+Item.Width; Frame[7] = Item.Top+Item.Height;
        			Frame[8] = Item.Left+Item.Width; Frame[9] = Item.Top+Item.Height; Frame[10] = Item.Left; Frame[11] = Item.Top+Item.Height;
        			Frame[12] = Item.Left; Frame[13] = Item.Top+Item.Height; Frame[14] = Item.Left; Frame[15] = Item.Top; 
                    canvas.drawLines(Frame, paint);
                	if (Item.Status == TButton.STATUS_DOWN) 
                        paint.setColor(Color.WHITE);
                	else
                		paint.setColor(Item.TextColor);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setAntiAlias(true);
                    paint.setTextSize(24);
                    String S = Item.Name;
                    float W = paint.measureText(S);
                    canvas.drawText(S, Item.Left+(Item.Width-W)/2.0F,Item.Top+(Item.Height+paint.getTextSize())/2F, paint);
                }
    		}
    		
    		public int GetItemAt(double pX, double pY) {
                for (int I = 0; I < Items.length; I++)
                	if ( ((Items[I].Left <= pX) && (pX <= (Items[I].Left+Items[I].Width))) && ((Items[I].Top <= pY) && (pY <= (Items[I].Top+Items[I].Height))) ) 
                		return I; //. ->
                return -1;
    		}
    		
    		public void SetDownButton(int pDownButtonIndex) {
    			Items[pDownButtonIndex].SetStatus(TButton.STATUS_DOWN);
    			DownButtonIndex = pDownButtonIndex;
    			//.
    			DownButtons_Time = Calendar.getInstance().getTime().getTime();
    		}

    		public void ClearDownButton() {
    			Items[DownButtonIndex].SetStatus(TButton.STATUS_UP);
    			DownButtonIndex = -1;
    			//.
    			DownButtons_Time = Calendar.getInstance().getTime().getTime();
    		}
    	}
    			
		private TReflector Reflector;
		//.
		public int Width;
		public int Height;
		private Paint paint = new Paint();
		private Paint SelectedObjPaint = new Paint();
		private Paint DelimiterPaint = new Paint();
		private Paint CenterMarkPaint = new Paint();
		private Bitmap BackgroundBitmap = null;
		public TButtons Buttons;
		
		public TWorkSpace(TReflector pReflector) {
			super(pReflector);
			//.
			Reflector = pReflector;
			setScaleType(ScaleType.MATRIX);
			//.
			SelectedObjPaint.setColor(Color.RED);
			SelectedObjPaint.setStrokeWidth(2.0F);
			//.
			DelimiterPaint.setColor(Color.RED);
			DelimiterPaint.setStrokeWidth(0.8F);
            //.
			CenterMarkPaint.setColor(Color.RED);
			CenterMarkPaint.setStrokeWidth(2.0F);
			//.
			Buttons = new TButtons(this);
		}
		
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            //.
        	Width = w;
        	Height = h;
        	setMinimumWidth(Width);
        	setMinimumHeight(Height);
        	//.
        	if (BackgroundBitmap != null)
        		BackgroundBitmap.recycle();
        	BackgroundBitmap = BackgroundBitmap_ReCreate(Width,Height);
        	//. align buttons
        	double YStep = (h/Buttons.Items.length);
        	double Y = 0;
        	for (int I = 0; I < Buttons.Items.length; I++) {
        		Buttons.Items[I].Top = (int)Y;
        		if (I < Buttons.Items.length-1)
        			Buttons.Items[I].Height = (int)YStep;
        		else
        			Buttons.Items[I].Height = Height-Buttons.Items[I].Top;
        		Y += YStep;
        	}
        	//.
        	Reflector.SpaceImage.DoOnResize(Width,Height);
        	//.
        	Reflector.ReflectionWindow.Resize(Width,Height);
        	Reflector.RecalculateAndUpdateCurrentSpaceImage();
        	//.
        	Reflector.StartUpdatingSpaceImage(1000);
        }
	
        
        protected void DrawOnCanvas(Canvas canvas, boolean flDrawBackground, boolean flDrawImage, boolean flDrawHints, boolean flDrawObjectTracks, boolean flDrawSelectedObject, boolean flDrawGeoMonitorObjects, boolean flDrawControls) {
        	try {
            	super.onDraw(canvas);
            	//. draw background
        		if (flDrawBackground)
        			canvas.drawBitmap(BackgroundBitmap, 0,0, paint);
            	//.
            	TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
            	RW.MultiplyByMatrix(ReflectionWindowTransformatrix);
            	//. draw image
            	if (flDrawImage) {
                	switch (GetViewMode()) {
                	case VIEWMODE_REFLECTIONS: 
                		SpaceReflections.ReflectionWindow_DrawOnCanvas(RW, canvas);
                		//.
                		synchronized (Reflector.SpaceImage) {
                			if (Reflector.SpaceImage.flResultBitmap) {
                        		canvas.setMatrix(Reflector.SpaceImage.ResultBitmapTransformatrix);
            					canvas.drawBitmap(Reflector.SpaceImage.ResultBitmap, 0,0, paint);
                			}
                			if (Reflector.SpaceImage.flSegments) {
                        		int SX;
                        		Bitmap Segment;
                        		canvas.setMatrix(Reflector.SpaceImage.SegmentsTransformatrix);
                    			for (int X = 0; X < Reflector.SpaceImage.DivX; X++) {
                    				SX = X*Reflector.SpaceImage.SegmentWidth;
                    				for (int Y = 0; Y < Reflector.SpaceImage.DivY; Y++) {
                    					Segment = Reflector.SpaceImage.Segments[X][Y];
                    					if (Segment != null)
                    						canvas.drawBitmap(Segment, SX,Y*Reflector.SpaceImage.SegmentHeight, paint);
                    				}
                    			}
                			}
                		}
                		break; //. >
                		
                	case VIEWMODE_TILES:
                		try {
            			    SpaceTileImagery.ActiveCompilation_ReflectionWindow_DrawOnCanvas(RW, canvas, null);
                		}
                		catch (TTimeLimit.TimeIsExpiredException TEE) {
                		}
                		break; //. >
                	}
            	}
        		//. draw space image hints
            	if (flDrawHints) {
            		if (Reflector.Configuration.ReflectionWindow_flShowHints) {
                    	canvas.setMatrix(null);
                		Reflector.SpaceHints.DrawOnCanvas(RW,Reflector.DynamicHintVisibleFactor,canvas);
            		}
            	}
            	//.
            	canvas.setMatrix(NavigationTransformatrix);
            	//. draw tracks
            	if (flDrawObjectTracks) {
                	Reflector.ObjectTracks.DrawOnCanvas(canvas);
            	}
            	//. draw selected object
            	if (flDrawSelectedObject) {
                	if ((Reflector.SelectedObj != null) && (Reflector.SelectedObj.ScreenNodes != null)) 
                		canvas.drawLines(Reflector.SelectedObj.ScreenNodes, SelectedObjPaint);
            	}
            	//. draw monitor objects
            	if (flDrawGeoMonitorObjects) {
                	Reflector.CoGeoMonitorObjects.DrawOnCanvas(canvas);
            	}
            	//. restore transformatrix
        		canvas.setMatrix(null);
        		//. draw controls
        		if (flDrawControls) {
            		//. draw buttons
            		Buttons.Draw(canvas);
                	//.
                	if (ShowLogoCount > 0) {
                		ShowLogo(canvas);
                		ShowLogoCount--;
                	}
                	else
            			ShowCenterMark(canvas);
                	//. draw navigation delimiters
                	int X = Width-Reflector.RotatingZoneWidth; 
            		canvas.drawLine(X,0, X,Height, DelimiterPaint);
                	X = Width-(Reflector.RotatingZoneWidth+Reflector.ScalingZoneWidth); 
            		canvas.drawLine(X,0, X,Height, DelimiterPaint);
            		//.
            		ShowStatus(canvas);
        		}
        	}
        	catch (Throwable TE) {
        		TDEVICEModule.Log_WriteCriticalError(TE);
        	}
        }

        @Override
        protected void onDraw(Canvas canvas) {
        	DrawOnCanvas(canvas, true,true,true,true,true,true,true);
        }
	
        public Bitmap BackgroundBitmap_ReCreate(int Width, int Height)
        {
        	Bitmap Result = Bitmap.createBitmap(Width, Height, Bitmap.Config.RGB_565);
        	Canvas canvas = new Canvas(Result);	
        	Paint _paint = new Paint();
        	_paint.setColor(Color.GRAY);
        	canvas.drawRect(0,0, Result.getWidth(),Result.getHeight(), _paint);
        	_paint.setColor(Color.DKGRAY);
            _paint.setStrokeWidth(1.0F);
        	int MeshStep = 5;
        	int X = 0;
        	int Y = 0;
        	int W = Result.getWidth();
        	int H = Result.getHeight();
        	int CntX = (int)(W/MeshStep)+1; 
        	int CntY = (int)(H/MeshStep)+1;
        	for (int I = 0; I < CntY; I++) {
        		canvas.drawLine(0,Y, W,Y, _paint);
        		Y += MeshStep;
        	}
        	for (int I = 0; I < CntX; I++) {
        		canvas.drawLine(X,0, X,H, _paint);
        		X += MeshStep;
        	}
        	return Result;
        }
        
        public void ShowCenterMark(Canvas canvas) {
        	int X = (int)(Width/2);
        	int Y = (int)(Height/2);
        	int R = 8;
    		canvas.drawLine(X,Y-R, X,Y+R, CenterMarkPaint);
    		canvas.drawLine(X-R,Y, X+R,Y, CenterMarkPaint);
        }
        
        private void ShowLogo(Canvas canvas) {
        	Paint _paint = new Paint();
            String S = "   GeoLog  "+ProgramVersion+"   ";
            _paint.setTextSize(28);
            float W = _paint.measureText(S);
            float H = _paint.getTextSize();
            int Left = (int)((Width-W)/2);
            int Top = (int)((Height-H)/2);
			_paint.setColor(Color.WHITE);
        	_paint.setAlpha(100);
    		canvas.drawRect(Left,Top, Left+W,Top+H, _paint);
        	_paint.setColor(Color.BLUE);
            _paint.setStyle(Paint.Style.FILL);
            _paint.setAntiAlias(true);
            canvas.drawText(S, Left,Top+H-4, _paint);
        }
        
        private Paint ShowStatus_Paint = new Paint();
        
        private void ShowStatus(Canvas canvas) {
            String S = null; 
            //.
            if (IsUpdatingSpaceImage())
            	S = getContext().getString(R.string.SImageUpdating);
            //.
            if (S == null)
            	return; //. ->
            ShowStatus_Paint.setTextSize(16);
            float W = ShowStatus_Paint.measureText(S);
            float H = ShowStatus_Paint.getTextSize();
            int Left = (int)((Width-W)/2);
            int Top = (int)(Height-H);
            ShowStatus_Paint.setAntiAlias(true);
            ShowStatus_Paint.setColor(Color.GRAY);
            ShowStatus_Paint.setAlpha(100);
    		canvas.drawRect(Left,Top, Left+W,Top+H, ShowStatus_Paint);
            ShowStatus_Paint.setStyle(Paint.Style.FILL);
            ShowStatus_Paint.setColor(Color.BLACK);
            canvas.drawText(S, Left+1,Top+H-4+1, ShowStatus_Paint);
            ShowStatus_Paint.setColor(Color.RED);
            canvas.drawText(S, Left,Top+H-4, ShowStatus_Paint);
        }
    }

    private class TSpaceImageUpdating extends TCancelableThread {

    	private class TImageUpdater extends TUpdater {
    	
    		@Override
    		public void Update() {
				Reflector.WorkSpace.postInvalidate();
    		}
    	}
    	
    	private class TCompilationTilesPreparing {
    		
    		private class TCompilationTilesPreparingThread implements Runnable{
    			
    			private TTileServerProviderCompilation Compilation;
    			private TRWLevelTileContainer LevelTileContainer;
    			private TCanceller Canceller;
    			private TUpdater Updater;
    			//.
    	    	private Thread 		_Thread;
    	    	private Throwable	_ThreadException = null;
    			
    			public TCompilationTilesPreparingThread(TTileServerProviderCompilation pCompilation, TRWLevelTileContainer pLevelTileContainer, TCanceller pCanceller, TUpdater pUpdater) {
    				Compilation = pCompilation;
    				LevelTileContainer = pLevelTileContainer;
    				Canceller = pCanceller;
    				Updater = pUpdater;
    				//.
    	    		_Thread = new Thread(this);
    	    		_Thread.setPriority(3);
    	    		_Thread.start();
    			}
    			
    	    	@Override
    	        public void run() {
    	    		try {
						Compilation.PrepareTiles(LevelTileContainer, Canceller,Updater);
					} catch (Throwable E) {
						_ThreadException = E;
					}    	    	
				}
    	    	
    			public void WaitFor() throws Throwable {
    	    		_Thread.join();
    	    		//.
    	    		if (_ThreadException != null)
    	    			throw _ThreadException; //. =>
    			}
    		}
    		
    		private TCompilationTilesPreparingThread[] Threads;
    		
    		public TCompilationTilesPreparing(TTileServerProviderCompilation[] Compilation, TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater) throws InterruptedException {
    			Threads = new TCompilationTilesPreparingThread[Compilation.length];
    			for (int I = Compilation.length-1; I >= 0; I--) {
    				Threads[I] = new TCompilationTilesPreparingThread(Compilation[I], LevelTileContainers[I], Canceller, Updater);
    				//.
    				Thread.sleep(10);
    			}
    		}
    		
			public void WaitForFinish() throws Throwable {
    			for (int I = 0; I < Threads.length; I++)
    				Threads[I].WaitFor();
			}
    	}
    	
		public class TActiveCompilationUpLevelsTilesPreparing extends TCancelableThread {
			
			private TRWLevelTileContainer[] LevelTileContainers;
			
			public TActiveCompilationUpLevelsTilesPreparing(TRWLevelTileContainer[] pLevelTileContainers) {
				LevelTileContainers = pLevelTileContainers;
				//.
	    		_Thread = new Thread(this);
	    		_Thread.start();
			}
			
	    	@Override
	        public void run() {
	    		try {
            		Reflector.SpaceTileImagery.ActiveCompilation_PrepareUpLevelsTiles(LevelTileContainers, Canceller,null);
				} catch (Throwable E) {
	        		TDEVICEModule.Log_WriteCriticalError(E);
	        		String S = E.getMessage();
	        		if (S == null)
	        			S = E.getClass().getName();
	    			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,TReflector.this.getString(R.string.SErrorOfGettingUpperLayers)+S).sendToTarget();
				}    	    	
			}
		}
		
    	private TReflector Reflector;
    	private int Delay;
    	private boolean flUpdateProxySpace;
    	//.
    	private TImageUpdater ImageUpdater;
    	
    	public TSpaceImageUpdating(TReflector pReflector, int pDelay, boolean pflUpdateProxySpace) throws Exception {
    		Reflector = pReflector;
    		Delay = pDelay;
    		flUpdateProxySpace = pflUpdateProxySpace;
    		//.
    		ImageUpdater = new TImageUpdater();
    		//.
    		_Thread = new Thread(this);
    		_Thread.setPriority(Thread.NORM_PRIORITY);
    		_Thread.start();
    	}

    	private void InputStream_ReadData(InputStream in, byte[] Data, int DataSize) throws Exception {
            int Size;
            int SummarySize = 0;
            int ReadSize;
            while (SummarySize < DataSize) {
                ReadSize = DataSize-SummarySize;
                Size = in.read(Data,SummarySize,ReadSize);
                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
                SummarySize += Size;
            }
    	}

    	@Override
        public void run() {
        	try {
        		long LastTime = Calendar.getInstance().getTime().getTime();
        		//.
        		TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
        		TRWLevelTileContainer[] LevelTileContainers = null;
    			//. cache and draw reflections
            	switch (GetViewMode()) {
            	case VIEWMODE_REFLECTIONS: 
    				Reflector.SpaceReflections.CacheReflectionsSimilarTo(RW);
            		break; //. >
            		
            	case VIEWMODE_TILES:
            		Reflector.SpaceTileImagery.ActiveCompilation_CheckInitialized();
            		//.
            		LevelTileContainers = Reflector.SpaceTileImagery.ActiveCompilation_GetLevelTileRange(RW);
            		if (LevelTileContainers == null)
            			return; //. ->
        			//.
            		Reflector.SpaceTileImagery.ActiveCompilation_RemoveOldTiles(LevelTileContainers, Canceller);
            		//.
					Reflector.SpaceTileImagery.ActiveCompilation_RestoreTiles(LevelTileContainers, Canceller,null);
            		break; //. >
            	}
        		Reflector.WorkSpace.postInvalidate();
        		//.
        		Reflector.ReflectionWindow.CheckSpaceLays();
				//.
        		if (Canceller.flCancel)
        			return; //. ->
    			while ((Calendar.getInstance().getTime().getTime()-LastTime) < Delay) {
            		Thread.sleep(100);
            		if (Canceller.flCancel)
            			return; //. ->
    			}
    			//.
            	switch (GetViewMode()) {
            	case VIEWMODE_REFLECTIONS: 
        			int DivX = Reflector.SpaceImage.DivX; 
        			int DivY = Reflector.SpaceImage.DivY;
        			URL url;
        			TReflectionWindowStruc Reflection_Window;
        			double Reflection_TimeStamp;
        			synchronized (Reflector.ReflectionWindow) {
            			url = new URL(Reflector.ReflectionWindow.PreparePNGImageURL(DivX,DivY,1/*segments order*/,flUpdateProxySpace));
            			Reflection_TimeStamp = OleDate.ToUTCCurrentTime().toDouble();
            			Reflection_Window = Reflector.ReflectionWindow.GetWindow(); 
					}
        			//.
                    HttpURLConnection _HTTPConnection = (HttpURLConnection)url.openConnection();
                    try {
                		if (Canceller.flCancel)
                			return; //. ->
                        _HTTPConnection.setAllowUserInteraction(false);
                        _HTTPConnection.setInstanceFollowRedirects(true);
                        _HTTPConnection.setRequestMethod("GET");
                        _HTTPConnection.setConnectTimeout(HttpConnection_ConnectTimeout);
                        _HTTPConnection.setReadTimeout(HttpConnection_ReadTimeout);
            			_HTTPConnection.connect();
                		if (Canceller.flCancel)
                			return; //. ->
            			int response = _HTTPConnection.getResponseCode();
            			if (response != HttpURLConnection.HTTP_OK) 
                        	throw new IOException(Reflector.getString(R.string.SServerError)+_HTTPConnection.getResponseMessage());                          
                		if (Canceller.flCancel)
                			return; //. ->
            			InputStream in = _HTTPConnection.getInputStream();
        				if (in == null)
        					throw new IOException(Reflector.getString(R.string.SConnectionError)); //. =>
            			try {
            				if (Canceller.flCancel)  
            					return; //. ->
            				//.
            				byte[] ImageDataSize = new byte[4]; //. not used
            				InputStream_ReadData(in, ImageDataSize,ImageDataSize.length);
            				//.
            				Reflector.SpaceImage.StartSegmenting(); 
            				//.
            				byte[] Data = new byte[32768]; //. max segment size
            				int DataSize;
            				byte SX,SY;
            				int Cnt = DivX*DivY;
            				for (int I = 0; I < Cnt; I++) {
                				DataSize = 2+4;
                				InputStream_ReadData(in, Data,DataSize);
                				int Idx = 0;
                				SX = Data[Idx]; Idx++;
                				SY = Data[Idx]; Idx++;
                				DataSize = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx += 4;
                				if (DataSize > Data.length)
                					Data = new byte[DataSize];
                				InputStream_ReadData(in, Data,DataSize);
                				//.
                        		if (Canceller.flCancel)
                        			return; //. ->
                				//.
                				Reflector.SpaceImage.AddSegment(SX,SY, Data,DataSize);
                				//.
                        		if (Canceller.flCancel)
                        			return; //. ->
                				//.
                				if (I != (Cnt-1))
                        			Reflector.WorkSpace.postInvalidate();
            				}
        					Reflector.SpaceImage.FinishSegmenting(Reflection_TimeStamp,Reflection_Window);
            				//.
                    		if (Canceller.flCancel)
                    			return; //. ->
                			//. raise event
                			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_UPDATESPACEIMAGE).sendToTarget();
                    		//. receiving hint's data
            				byte[] HintDataSizeBA = new byte[4]; 
            				InputStream_ReadData(in, HintDataSizeBA,HintDataSizeBA.length);
            				int HintDataSize = TDataConverter.ConvertBEByteArrayToInt32(HintDataSizeBA,0); 
            				byte[] HintData = new byte[HintDataSize]; 
            				InputStream_ReadData(in, HintData,HintDataSize);
            				HintData = Reflector.SpaceHints.UnPackByteArray(HintData);
            				Reflector.SpaceHints.ReviseItemsInReflectionWindow(Reflection_Window,HintData,Canceller);
            				Reflector.SpaceHints.FromByteArray(HintData,Canceller);
                			Reflector.WorkSpace.postInvalidate();
            			}
            			finally {
            				in.close();
            			}
                    }
                    finally {
                    	_HTTPConnection.disconnect();
                    }
            		break; //. >
            		
            	case VIEWMODE_TILES: 
            		Reflector.SpaceHints.GetHintsFromServer(Reflector.ReflectionWindow, Canceller);
            		Reflector.WorkSpace.postInvalidate();
            		//. 
            		Reflector.SpaceTileImagery.ActiveCompilation_PrepareTiles(LevelTileContainers, Canceller,ImageUpdater);
            		///? TCompilationTilesPreparing CompilationTilesPreparing = new TCompilationTilesPreparing(Reflector.SpaceTileImagery.ActiveCompilation(), LevelTileContainers, Canceller, ImageUpdater);
            		///? CompilationTilesPreparing.WaitForFinish(); //. waiting for threads to finish
            		//. raise event
        			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_UPDATESPACEIMAGE).sendToTarget();
        			//. prepare up level's tiles once when program is just started
    				if (_SpaceImageUpdating_flPrepareUpLevels) {
    					_SpaceImageUpdating_flPrepareUpLevels = false;
    					//.
    	        		TRWLevelTileContainer[] _LevelTileContainers = new TRWLevelTileContainer[LevelTileContainers.length];
    	        		for (int I = 0; I < _LevelTileContainers.length; I++)
    	        			_LevelTileContainers[I] = new TRWLevelTileContainer(LevelTileContainers[I]);
    					//.
    	        		TActiveCompilationUpLevelsTilesPreparing ActiveCompilationUpLevelsTilesPreparing = new TActiveCompilationUpLevelsTilesPreparing(_LevelTileContainers);
    	            	synchronized (Reflector) {
    	            		if (_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing != null)
    	            			_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing.Cancel();
    	            		_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = ActiveCompilationUpLevelsTilesPreparing;
    	        		}
    				}
            		break; //. >
            	}
        		//. supply hints with images
        		_Thread.setPriority(Thread.MIN_PRIORITY);
				int SupplyCount = 25; 
				for (int I = 0; I < SupplyCount; I++) {
					int RC = Reflector.SpaceHints.SupplyHintsWithImageDataFiles(Canceller);
        			if (RC == TSpaceHints.SHWIDF_RESULT_NOITEMTOSUPPLY)
        				break; //. >
            		Reflector.WorkSpace.postInvalidate();
        			if (RC == TSpaceHints.SHWIDF_RESULT_SUPPLIED)
        				break; //. >
    				//.
            		if (Canceller.flCancel)
            			return; //. ->
				}
				//.
				synchronized (Reflector) {
					_SpaceImageUpdatingCount++;
				}
    		}	    	
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
        	catch (IOException E) {
        		if (Reflector.Reflection_FirstTryCount > 0) { 
        			Reflector.Reflection_FirstTryCount--;
        			//. wait a moment
        			try {
        				Thread.sleep(1000*2);
        			}
        			catch (Exception Ex) {
        				return; //. ->
        			}
        			//. try to update an image again
        			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_STARTUPDATESPACEIMAGE).sendToTarget(); 
        		}
        		else {
            		String S = E.getMessage();
            		if (S == null)
            			S = E.getClass().getName();
        			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,Reflector.getString(R.string.SErrorOfUpdatingImage)+S).sendToTarget();
        		}
        	}
        	catch (Throwable E) {
        		///- TDEVICEModule.Log_WriteCriticalError(E);
        		String S = E.getMessage();
        		if (S == null)
        			S = E.getClass().getName();
    			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,Reflector.getString(R.string.SErrorOfUpdatingImage)+S).sendToTarget();
        	}
        }
    }
    
    private class TSpaceObjOwnerTypedDataFileNamesLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TReflector Reflector;
    	//. 
    	private int OnStartDelay;
    	private TSpaceObj SpaceObj;
    	private int OnCompletionMessage;
    	//.
        int SummarySize = 0;
        private ProgressDialog progressDialog; 
    	
    	public TSpaceObjOwnerTypedDataFileNamesLoading(TReflector pReflector, TSpaceObj pSpaceObj, int pOnStartDelay, int pOnCompletionMessage) {
    		Reflector = pReflector;
    		//. 
    		OnStartDelay = pOnStartDelay;
    		SpaceObj = pSpaceObj;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				if (OnStartDelay > 0)
					Thread.sleep(OnStartDelay);
				if (Canceller.flCancel)
					return; //. ->
				//.
				String URL1 = ServerAddress;
				//. add command path
				URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
				String URL2 = "Functionality"+"/"+"VisualizationOwnerDataDocument.dat";
				//. add command parameters
				int WithComponentsFlag = 1;
				URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(SpaceObj.ptrObj)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_TYPE_AllName)+","+Integer.toString(WithComponentsFlag);
				//.
				byte[] URL2_Buffer;
				try {
					URL2_Buffer = URL2.getBytes("windows-1251");
				} 
				catch (Exception E) {
					URL2_Buffer = null;
				}
				byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
				//. encode string
		        StringBuffer sb = new StringBuffer();
		        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
		            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
		            while (h.length() < 2) 
		            	h = "0" + h;
		            sb.append(h);
		        }
				URL2 = sb.toString();
				//.
				String URL = URL1+"/"+URL2+".dat";
				//.
				if (Canceller.flCancel)
					return; //. ->
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				HttpURLConnection HttpConnection = OpenHttpConnection(URL);
    				try {
    					if (Canceller.flCancel)
    						return; //. ->
    					//.
    					InputStream in = HttpConnection.getInputStream();
    					try {
			    			if (Canceller.flCancel)
			    				return; //. ->
			                //.
    						int RetSize = HttpConnection.getContentLength();
    						if (RetSize == 0) {
    							SpaceObj.OwnerTypedDataFiles = null;
    							return; //. ->
    						}
    						byte[] Data = new byte[RetSize];
    			            int Size;
    			            SummarySize = 0;
    			            int ReadSize;
    			            while (SummarySize < Data.length)
    			            {
    			                ReadSize = Data.length-SummarySize;
    			                Size = in.read(Data,SummarySize,ReadSize);
    			                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
    			                SummarySize += Size;
    			                //.
    			    			if (Canceller.flCancel)
    			    				return; //. ->
    			    			//.
    			    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(100*SummarySize/Data.length)).sendToTarget();
    			            }
    			            //.
    			    		SpaceObj.OwnerTypedDataFiles = new TComponentTypedDataFiles(Reflector,SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
    			    		//.
    			    		int Idx = 0;
    			    		SpaceObj.OwnerType = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4;
    			    		SpaceObj.OwnerID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. ID: Int64
    			    		SpaceObj.OwnerCoType = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4;
    			    		if (Data.length > Idx)
    			    			SpaceObj.OwnerTypedDataFiles.PrepareFromByteArrayV0(Data,Idx);
    			    		//.
    		    			Reflector.MessageHandler.obtainMessage(OnCompletionMessage,SpaceObj).sendToTarget();
    					}
    					finally {
    						in.close();
    					}                
    				}
    				finally {
    					HttpConnection.disconnect();
    				}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
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
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflector.this, Reflector.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	try {
		            	progressDialog = new ProgressDialog(Reflector);    
		            	progressDialog.setMessage(Reflector.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	progressDialog.show();
	            	}
	            	catch (Exception EE) {
		                Toast.makeText(TReflector.this, EE.getMessage(), Toast.LENGTH_LONG).show();
	            	}
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
	    };
    }
    
    private class TComponentTypedDataFileNamesLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TReflector Reflector;
    	//. 
    	private int ComponentType;
    	private int ComponentID;
    	private int OnCompletionMessage;
    	//.
        int SummarySize = 0;
        private ProgressDialog progressDialog; 
    	
    	public TComponentTypedDataFileNamesLoading(TReflector pReflector, int pComponentType, int pComponentID, int pOnCompletionMessage) {
    		Reflector = pReflector;
    		ComponentType = pComponentType;
    		ComponentID = pComponentID;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public String PrepareURL() {
			String URL1 = ServerAddress;
			//. add command path
			URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
			String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
			//. add command parameters
			int WithComponentsFlag = 1;
			URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(ComponentType)+","+Integer.toString(ComponentID)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_TYPE_AllName)+","+Integer.toString(WithComponentsFlag);
			//.
			byte[] URL2_Buffer;
			try {
				URL2_Buffer = URL2.getBytes("windows-1251");
			} 
			catch (Exception E) {
				URL2_Buffer = null;
			}
			byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
			//. encode string
	        StringBuffer sb = new StringBuffer();
	        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
	            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
	            while (h.length() < 2) 
	            	h = "0" + h;
	            sb.append(h);
	        }
			URL2 = sb.toString();
			//.
			String URL = URL1+"/"+URL2+".dat";
			return URL;
    	}

		@Override
		public void run() {
			try {
				String URL = PrepareURL();
				//.
				if (Canceller.flCancel)
					return; //. ->
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				HttpURLConnection HttpConnection = OpenHttpConnection(URL);
    				try {
    					if (Canceller.flCancel)
    						return; //. ->
    					//.
    					InputStream in = HttpConnection.getInputStream();
    					try {
			    			if (Canceller.flCancel)
			    				return; //. ->
			                //.
    						int RetSize = HttpConnection.getContentLength();
    						if (RetSize == 0) {
        		    			Reflector.MessageHandler.obtainMessage(OnCompletionMessage,null).sendToTarget();
    							return; //. ->
    						}
    						byte[] Data = new byte[RetSize];
    			            int Size;
    			            SummarySize = 0;
    			            int ReadSize;
    			            while (SummarySize < Data.length)
    			            {
    			                ReadSize = Data.length-SummarySize;
    			                Size = in.read(Data,SummarySize,ReadSize);
    			                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
    			                SummarySize += Size;
    			                //.
    			    			if (Canceller.flCancel)
    			    				return; //. ->
    			    			//.
    			    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(100*SummarySize/Data.length)).sendToTarget();
    			            }
    			            //.
    			            TComponentTypedDataFiles OwnerTypedDataFiles = new TComponentTypedDataFiles(Reflector,SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION);
    			            OwnerTypedDataFiles.PrepareFromByteArrayV0(Data);
    			            //.
    		    			Reflector.MessageHandler.obtainMessage(OnCompletionMessage,OwnerTypedDataFiles).sendToTarget();
    					}
    					finally {
    						in.close();
    					}                
    				}
    				finally {
    					HttpConnection.disconnect();
    				}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
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
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflector.this, Reflector.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(Reflector);    
	            	progressDialog.setMessage(Reflector.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
	            	progressDialog.setIndeterminate(false); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, Reflector.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
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
    
    private class TComponentTypedDataFileLoading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TReflector Reflector;
    	//. 
    	private TComponentTypedDataFile ComponentTypedDataFile;
    	private int OnCompletionMessage;
    	//.
        int SummarySize = 0;
        private ProgressDialog progressDialog; 
    	
    	public TComponentTypedDataFileLoading(TReflector pReflector, TComponentTypedDataFile pComponentTypedDataFile, int pOnCompletionMessage) {
    		Reflector = pReflector;
    		ComponentTypedDataFile = pComponentTypedDataFile;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				String URL1 = ServerAddress;
				//. add command path
				URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
				String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
				//. add command parameters
				int WithComponentsFlag = 0;
				URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(ComponentTypedDataFile.DataComponentType)+","+Integer.toString(ComponentTypedDataFile.DataComponentID)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)+","+Integer.toString(ComponentTypedDataFile.DataType)+","+Integer.toString(WithComponentsFlag);
				//.
				byte[] URL2_Buffer;
				try {
					URL2_Buffer = URL2.getBytes("windows-1251");
				} 
				catch (Exception E) {
					URL2_Buffer = null;
				}
				byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
				//. encode string
		        StringBuffer sb = new StringBuffer();
		        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
		            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
		            while (h.length() < 2) 
		            	h = "0" + h;
		            sb.append(h);
		        }
				URL2 = sb.toString();
				//.
				String URL = URL1+"/"+URL2+".dat";
				//.
				if (Canceller.flCancel)
					return; //. ->
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				HttpURLConnection HttpConnection = OpenHttpConnection(URL);
    				try {
    					if (Canceller.flCancel)
    						return; //. ->
    					//.
    					InputStream in = HttpConnection.getInputStream();
    					try {
			    			if (Canceller.flCancel)
			    				return; //. ->
			                //.
    						int RetSize = HttpConnection.getContentLength();
    						if (RetSize == 0) {
    							ComponentTypedDataFile.Data = null;
    							return; //. ->
    						}
    						byte[] Data = new byte[RetSize];
    			            int Size;
    			            SummarySize = 0;
    			            int ReadSize;
    			            while (SummarySize < Data.length)
    			            {
    			                ReadSize = Data.length-SummarySize;
    			                Size = in.read(Data,SummarySize,ReadSize);
    			                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
    			                SummarySize += Size;
    			                //.
    			    			if (Canceller.flCancel)
    			    				return; //. ->
    			    			//.
    			    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(100*SummarySize/Data.length)).sendToTarget();
    			            }
    			            //.
    			            ComponentTypedDataFile.PrepareFromByteArrayV0(Data);
    			            //.
    		    			Reflector.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
    					}
    					finally {
    						in.close();
    					}                
    				}
    				finally {
    					HttpConnection.disconnect();
    				}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
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
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflector.this, Reflector.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(Reflector);    
	            	progressDialog.setMessage(Reflector.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
	            	progressDialog.setIndeterminate(false); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, Reflector.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
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
    
    private class TCoGeoMonitorObjectsLocationUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_UPDATESPACEIMAGE = 1;
    	private static final int MESSAGE_STATUS_ALARM = 2;
    	
    	private TReflector Reflector;
    	
    	public TCoGeoMonitorObjectsLocationUpdating(TReflector pReflector) {
    		Reflector = pReflector;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				Thread.sleep(3000);
				//. 
				while (!Canceller.flCancel) {
					try {
						boolean flAlarm = false;
						boolean flPostInvalidate = false;
						for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
							if (Reflector.CoGeoMonitorObjects.Items[I].flEnabled && Reflector.CoGeoMonitorObjects.Items[I].flStatusIsEnabled) {
								try {
									int Mask = Reflector.CoGeoMonitorObjects.Items[I].UpdateStatus();
									if (Mask > 0) {
										if ((Mask & TReflectorCoGeoMonitorObject.STATUS_flAlarm_Mask) == TReflectorCoGeoMonitorObject.STATUS_flAlarm_Mask)
											flAlarm = true;
										flPostInvalidate = true;
									}
								}
						        catch (Exception E) {
						    		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
						        }
							}
						}
						if (flAlarm)
							MessageHandler.obtainMessage(MESSAGE_STATUS_ALARM,null).sendToTarget();
						if (flPostInvalidate)
							Reflector.WorkSpace.postInvalidate();
						//.
						if (Canceller.flCancel)
							return; //. ->
						//.
						boolean flUpdateImage = false;
						for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
							if (Reflector.CoGeoMonitorObjects.Items[I].flEnabled) {
								try {
									flUpdateImage = (flUpdateImage || Reflector.CoGeoMonitorObjects.Items[I].UpdateVisualizationLocation());
								}
					        	catch (Exception E) {
					        		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
					        	}
							}
						}
						if (flUpdateImage) {
							Reflector.WorkSpace.postInvalidate();
							//.
							TSpaceImageUpdating SIU = Reflector.GetSpaceImageUpdating();
							if (SIU != null)
								SIU.Join();
			    			MessageHandler.obtainMessage(MESSAGE_UPDATESPACEIMAGE,null).sendToTarget();
						}
						//.
						if (Canceller.flCancel)
							return; //. ->
						//.
						Thread.sleep(Reflector.CoGeoMonitorObjects.GetUpdateInterval()*1000);
						//.
						if (Canceller.flCancel)
							return; //. ->
		        	}
		        	catch (InterruptedException E) {
		        		return; //. ->
		        	}
		        	catch (Exception E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
						//.
		    			try {
		    				Thread.sleep(Reflector.CoGeoMonitorObjects.GetUpdateInterval()*1000);
							//.
							if (Canceller.flCancel)
								return; //. ->
		    			}
				        catch (InterruptedException E1) {
				        	return; //. ->
				        }
		        	}
				}
			}
			catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
			}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflector.this, Reflector.getString(R.string.SErrorOfUpdatingCurrentPosition)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            
	            case MESSAGE_UPDATESPACEIMAGE:
					Reflector.StartUpdatingSpaceImage(true);
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_STATUS_ALARM:
					for (int I = 0; I < Reflector.CoGeoMonitorObjects.Items.length; I++) {
		            	if (Reflector.CoGeoMonitorObjects.Items[I].Status_flAlarm) {
		            		PlayAlarmSound();
		            		//.
		            		Toast.makeText(TReflector.this, Reflector.getString(R.string.SAlarm)+Reflector.CoGeoMonitorObjects.Items[I].LabelText, Toast.LENGTH_LONG).show();
		            	}
					}
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
    
	public static final int MODE_NONE		= 0; 
	public static final int MODE_BROWSING	= 1; 
	public static final int MODE_EDITING 	= 2;
	//.
    public static final int VIEWMODE_NONE 			= 0;
    public static final int VIEWMODE_REFLECTIONS 	= 1;
    public static final int VIEWMODE_TILES 			= 2;
    //.
    private static final int MESSAGE_SHOWEXCEPTION = 0;
    private static final int MESSAGE_STARTUPDATESPACEIMAGE = 1;
    private static final int MESSAGE_UPDATESPACEIMAGE = 2;
    private static final int MESSAGE_SELECTEDOBJ_SET = 3;
    private static final int MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED = 4;
    private static final int MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED = 5;
    private static final int MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED = 6;
    private static final int MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED = 7;
    //.
    private static final int SHOW_TRACKER = 1;
    private static final int EDIT_REFLECTOR_CONFIGURATION = 2;
    private static final int OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE = 3;
    //.
    private static final int BUTTONS_COUNT = 8;
    //.
    private static final int BUTTON_UPDATE = 0;
    private static final int BUTTON_SHOWREFLECTIONPARAMETERS = 1;
    private static final int BUTTON_OBJECTS = 2;
    private static final int BUTTON_ELECTEDPLACES = 3;
    private static final int BUTTON_MAPOBJECTSEARCH = 4;
    private static final int BUTTON_PREVWINDOW = 5;
    private static final int BUTTON_EDITOR = 6;
    private static final int BUTTON_TRACKER = 7;
    
    private static final int HttpConnection_ConnectTimeout = 1000*30/*seconds*/;
    private static final int HttpConnection_ReadTimeout = 1000*30/*seconds*/;
    
	public String ServerAddress;
	public TReflectorUser User;
	public TReflectorConfiguration Configuration;
	public TReflectionWindow 	ReflectionWindow;
	private Matrix				ReflectionWindowTransformatrix = new Matrix();
	private int					Reflection_FirstTryCount = 3;
	
    public TWorkSpace WorkSpace;
    //.
    public int Mode = MODE_BROWSING;
    //.
    public int ViewMode = VIEWMODE_NONE; 
    //.
	protected TSpaceReflections 	SpaceReflections;
	protected TTileImagery 				SpaceTileImagery;
	protected TSpaceHints  				SpaceHints;
	protected TSpaceReflectionImage 	SpaceImage;
	//.	
	private boolean flEnabled = true;
	private int			NavigationType = TNavigationItem.NAVIGATIONTYPE_NONE;
	protected Matrix	NavigationTransformatrix = new Matrix();
	private TTimeLimit	NavigationDrawingTimeLimit = new TTimeLimit(100/*milliseconds*/);
	private TXYCoord Pointer_Down_StartPos;
	private TXYCoord Pointer_LastPos;
	private int ScalingZoneWidth = 48;
	private int RotatingZoneWidth = 42;
	private int SelectShiftFactor = 10;
	private double ScaleCoef = 3.0;
	public int VisibleFactor = 16;                                                                                                                                                 
	public int DynamicHintVisibleFactor = 10*10;                                                                                                                                                 
	public double DynamicHintVisibility = 1.0;
	public TElectedPlaces ElectedPlaces = null;
	public TReflectionWindowStrucStack LastWindows;
    private TReflectionWindow.TObjectAtPositionGetting ObjectAtPositionGetting = null;
    public TSpaceObj 								SelectedObj = null;
    public TCancelableThread						SelectedComponentTypedDataFileNamesLoading = null;
    public AlertDialog 								SelectedComponentTypedDataFileNames_SelectorPanel = null;
    public TCancelableThread 						SelectedComponentTypedDataFileLoading = null;
    //.
    private TSpaceImageUpdating 											_SpaceImageUpdating = null;
    private int																_SpaceImageUpdatingCount = 0;
    private boolean															_SpaceImageUpdating_flPrepareUpLevels = true;
    private TSpaceImageUpdating.TActiveCompilationUpLevelsTilesPreparing	_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = null;	
    public TReflectorCoGeoMonitorObjects CoGeoMonitorObjects;
    private TCoGeoMonitorObjectsLocationUpdating CoGeoMonitorObjectsLocationUpdating;
    //.
    public TReflectorObjectTracks ObjectTracks;
	//.
	public MediaPlayer _MediaPlayer = null; 

    public final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_SHOWEXCEPTION:
            	String EStr = (String)msg.obj;
                Toast.makeText(TReflector.this, TReflector.this.getString(R.string.SError)+EStr, Toast.LENGTH_SHORT).show();
            	//.
            	break; //. >

            case MESSAGE_STARTUPDATESPACEIMAGE:
            	StartUpdatingSpaceImage();
            	//.
            	break; //. >
            	
            case MESSAGE_UPDATESPACEIMAGE:
            	synchronized (TReflector.this) {
                	_SpaceImageUpdating = null;
				}
            	//.
            	RecalculateAndUpdateCurrentSpaceImage();
            	//. add new window to last windows
            	TReflectionWindowStruc RWS = ReflectionWindow.GetWindow();
            	LastWindows.Push(RWS);
            	///test
            	/*ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE); 
            	MemoryInfo mi = new MemoryInfo(); 
            	activityManager.getMemoryInfo(mi);             	
                Toast.makeText(TReflector.this, "FreeMem: "+Integer.toString((int)(mi.availMem/(1024*1024)))+", Cached: "+Reflections.GetCachedItemsCount(), Toast.LENGTH_LONG).show();*/
            	//.
            	break; //. >

            case MESSAGE_SELECTEDOBJ_SET:
        		SelectedObj = (TSpaceObj)msg.obj;
        		if (SelectedObj == null)
        			return; //. ->
        		//.
        		RecalculateAndUpdateCurrentSpaceImage();
            	//.
            	if (SelectedComponentTypedDataFileNamesLoading != null)
            		SelectedComponentTypedDataFileNamesLoading.Cancel();
            	SelectedComponentTypedDataFileNamesLoading = TReflector.this.new TSpaceObjOwnerTypedDataFileNamesLoading(TReflector.this, SelectedObj, 2000, MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED);
            	//.
            	break; //. >
            	
            case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILENAMES_LOADED:
            	TSpaceObj Obj = (TSpaceObj)msg.obj;
            	if ((Obj.OwnerTypedDataFiles != null) && (Obj.OwnerTypedDataFiles.Items != null)) {
            		//. look for first DocumentName that will be a object name
            		for (int I = 0; I < Obj.OwnerTypedDataFiles.Items.length; I++)
            			if (Obj.OwnerTypedDataFiles.Items[I].DataType == SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName) {
            	            Toast.makeText(TReflector.this, Obj.OwnerTypedDataFiles.Items[I].DataName, Toast.LENGTH_LONG).show();
            	            break; //. >
            			}
            		//.
            		if (Obj.OwnerTypedDataFiles.Items.length == 1) { 
        				TComponentTypedDataFile ComponentTypedDataFile = Obj.OwnerTypedDataFiles.Items[0];
        				//. convert from Name data to full data
        				ComponentTypedDataFile.DataType = ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
        				//.
        				if (SelectedComponentTypedDataFileLoading != null)
        					SelectedComponentTypedDataFileLoading.Cancel();
        				SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(TReflector.this,ComponentTypedDataFile,MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
        				SelectedComponentTypedDataFileNames_SelectorPanel = null;
            		}
        			else {
        				SelectedComponentTypedDataFileNames_SelectorPanel = ComponentTypedDataFiles_CreateSelectorPanel(Obj.OwnerTypedDataFiles,TReflector.this);
        				SelectedComponentTypedDataFileNames_SelectorPanel.show();
        			}
            	}
            	//.
            	break; //. >
            	
            case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED:
            	TComponentTypedDataFiles OwnerTypedDataFiles = (TComponentTypedDataFiles)msg.obj;
            	if (OwnerTypedDataFiles != null) {
                	if ((OwnerTypedDataFiles != null) && (OwnerTypedDataFiles.Items != null)) {
                		//. look for first DocumentName that will be a object name
                		for (int I = 0; I < OwnerTypedDataFiles.Items.length; I++)
                			if (OwnerTypedDataFiles.Items[I].DataType == SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName) {
                	            Toast.makeText(TReflector.this, OwnerTypedDataFiles.Items[I].DataName, Toast.LENGTH_LONG).show();
                	            break; //. >
                			}
                		//.
                		if (OwnerTypedDataFiles.Items.length == 1) { 
            				TComponentTypedDataFile ComponentTypedDataFile = OwnerTypedDataFiles.Items[0];
            				//. convert from Name data to full data
            				ComponentTypedDataFile.DataType = ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
            				//.
            				if (SelectedComponentTypedDataFileLoading != null)
            					SelectedComponentTypedDataFileLoading.Cancel();
            				SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(TReflector.this,ComponentTypedDataFile,MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED);
            				SelectedComponentTypedDataFileNames_SelectorPanel = null;
                		}
            			else {
            				SelectedComponentTypedDataFileNames_SelectorPanel = ComponentTypedDataFiles_CreateSelectorPanel(OwnerTypedDataFiles,TReflector.this);
            				SelectedComponentTypedDataFileNames_SelectorPanel.show();
            			}
                	}
            	}
            	//.
            	break; //. >
            	
            case MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED:
            case MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILE_LOADED:
            	TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile)msg.obj; 
            	if (ComponentTypedDataFile != null)
            		ComponentTypedDataFile_Open(ComponentTypedDataFile);
            	//.
            	break; //. >
            }
        }
    };
	
    protected BroadcastReceiver EventReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
    			TReflector.this.finish();
    			//.
    	    	return; //. ->
    		}
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
    			//.
    	    	return; //. ->
    		}
    	}
    };
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    	//.
        super.onCreate(savedInstanceState);
    	//. 
    	Context context = getApplicationContext();
    	//.
        try {
			TGeoLogInstallator.CheckInstallation(context);
		} catch (IOException E) {
            Toast.makeText(this, getString(R.string.SErrorOfProgramInstalling)+E.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return; //. ->
		}
    	//. start tracker service
    	try { 
    		TTracker.CreateTracker(this);
    	}
    	catch (Exception E) {
            Toast.makeText(this, getString(R.string.SErrorOfTrackerCreating)+E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	//.
		Intent serviceLauncher = new Intent(context, TTrackerService.class);
		context.startService(serviceLauncher);
        //.
        Configuration = new TReflectorConfiguration(this);
        try {
        	Configuration.Load();
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
        //.
        ServerAddress = Configuration.ServerAddress+":"+Integer.toString(Configuration.ServerPort);
        //.
        User = new TReflectorUser(Configuration.UserID,Configuration.UserPassword);
        //.
        double Xc = 317593.059;
        double Yc = -201347.576;
        TReflectionWindowStruc RW = new TReflectionWindowStruc(Xc-10,Yc+10,Xc+10,Yc+10,Xc+10,Yc-10,Xc-10,Yc-10,0,0,320,240);
        if (Configuration.ReflectionWindowData != null)
        {
        	try {
        		RW.FromByteArray(Configuration.ReflectionWindowData);
        	}
        	catch (Exception E) {
                Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
        	}
        }
    	try {
    		ReflectionWindow = new TReflectionWindow(this,RW);
    		ReflectionWindow.Normalize();
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
		//.
		Pointer_Down_StartPos = new TXYCoord();
		Pointer_LastPos = new TXYCoord();
		//.
		try {
			ElectedPlaces = new TElectedPlaces();
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	//.
    	LastWindows = new TReflectionWindowStrucStack(MaxLastWindowsCount);
		//.
        setContentView(R.layout.reflector);
        //.
		WorkSpace = new TWorkSpace(this);
		TWorkSpace.TButton[] Buttons = new TWorkSpace.TButton[BUTTONS_COUNT];
		int ButtonWidth = 36;
		int ButtonHeight = 64;
		int Y = 0;
		Buttons[BUTTON_UPDATE] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"!",Color.YELLOW); Y += ButtonHeight; 
		Buttons[BUTTON_SHOWREFLECTIONPARAMETERS] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"~",Color.YELLOW); Y += ButtonHeight; 
		///? Buttons[BUTTON_SUPERLAYS] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"=",Color.YELLOW); Y += ButtonHeight; 
		Buttons[BUTTON_OBJECTS] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"O",Color.GREEN); Y += ButtonHeight;
		Buttons[BUTTON_ELECTEDPLACES] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"*",Color.GREEN); Y += ButtonHeight;
		Buttons[BUTTON_MAPOBJECTSEARCH] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"?",Color.GREEN); Y += ButtonHeight;
		Buttons[BUTTON_PREVWINDOW] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"<<",Color.GREEN); Y += ButtonHeight;
		Buttons[BUTTON_EDITOR] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"+",Color.RED); Y += ButtonHeight;
		Buttons[BUTTON_TRACKER] = WorkSpace.new TButton(0,Y,ButtonWidth,ButtonHeight,"@",Color.CYAN); Y += ButtonHeight;
		WorkSpace.Buttons.SetButtons(Buttons);
        setContentView(WorkSpace);
    	WorkSpace.setOnTouchListener(this);
    	//.
    	try { 
    		SpaceReflections = new TSpaceReflections(this);
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	try { 
        	SpaceTileImagery = new TTileImagery(this,Configuration.ReflectionWindow_ViewMode_Tiles_Compilation);
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	try { 
        	SpaceHints = new TSpaceHints(this);
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	SpaceImage = new TSpaceReflectionImage(this,16,1);
    	//.
    	SetViewMode(Configuration.ReflectionWindow_ViewMode);
    	//.
    	ObjectTracks = new TReflectorObjectTracks(this);
    	//.
		CoGeoMonitorObjects = new TReflectorCoGeoMonitorObjects(this);
		if (CoGeoMonitorObjectsLocationUpdating != null) {
			CoGeoMonitorObjectsLocationUpdating.Cancel();
			CoGeoMonitorObjectsLocationUpdating = null;
		}
		CoGeoMonitorObjectsLocationUpdating = new TCoGeoMonitorObjectsLocationUpdating(this);
		//.
    	IntentFilter ScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);  
    	registerReceiver(EventReceiver,ScreenOffFilter);         
		//.
		System.gc(); //. collect garbage
    	MyReflector = this;
    }

    @Override
    public void onDestroy() {
    	MyReflector = null;
    	//.
    	if (EventReceiver != null) {
    		unregisterReceiver(EventReceiver);
    		EventReceiver = null;
    	}
    	//.
    	if (_SpaceImageUpdating != null) {
    		_SpaceImageUpdating.CancelAndWait();
    		_SpaceImageUpdating = null;
    	}
    	//.
    	TSpaceImageUpdating.TActiveCompilationUpLevelsTilesPreparing ActiveCompilationUpLevelsTilesPreparing;
    	synchronized (this) {
        	ActiveCompilationUpLevelsTilesPreparing = _SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing;
        	_SpaceImageUpdating_TActiveCompilationUpLevelsTilesPreparing = null;
		}
    	if (ActiveCompilationUpLevelsTilesPreparing != null) 
    		ActiveCompilationUpLevelsTilesPreparing.CancelAndWait();
    	//.
    	if (ObjectAtPositionGetting != null) {
    		ObjectAtPositionGetting.CancelAndWait();
    		ObjectAtPositionGetting = null;
    	}
    	//.
    	if (SelectedComponentTypedDataFileNamesLoading != null)
    	{
    		SelectedComponentTypedDataFileNamesLoading.CancelAndWait();
    		SelectedComponentTypedDataFileNamesLoading = null;
    	}
    	//.
    	if (SelectedComponentTypedDataFileLoading != null) {
    		SelectedComponentTypedDataFileLoading.CancelAndWait();
    		SelectedComponentTypedDataFileLoading = null;
    	}
    	//.
		if (CoGeoMonitorObjectsLocationUpdating != null) {
			CoGeoMonitorObjectsLocationUpdating.CancelAndWait();
			CoGeoMonitorObjectsLocationUpdating = null;
		}
		//.
		if (SpaceImage != null) {
			SpaceImage.Destroy();
			SpaceImage = null;
		}
		if (SpaceHints != null) {
    		try {
    			SpaceHints.Destroy();
        	}
    		catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
    		}
			SpaceHints = null;
		}
		if (SpaceTileImagery != null) {
    		try {
    			SpaceTileImagery.Destroy();
        	}
    		catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
    		}
			SpaceTileImagery = null;
		}
		if (SpaceReflections != null) {
    		try {
    			SpaceReflections.Destroy();
        	}
    		catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
    		}
			SpaceReflections = null;
		}
    	//.
    	if (Configuration != null)
    		try {
    			Configuration.Save();
    			Configuration = null;
        	}
    		catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
    		}
        super.onDestroy();
        //.
		System.gc(); //. collect garbage
    }
    
    @Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
			_MediaPlayer.stop();
		//.	
		super.onStop();
	}

	public void Finish() {
    	finish();
    	/*///? //.
		onDestroy();
		//.
		TTracker.FreeTracker();
    	//.
    	System.exit(0);*/
    }
	
	public synchronized int GetViewMode() {
		return ViewMode;
	}
	
	public void SetViewMode(int pViewMode) {
		if (pViewMode == ViewMode)
			return; //. ->
    	//.
    	synchronized (this) {
    		ViewMode = pViewMode;
		}
		Configuration.ReflectionWindow_ViewMode = ViewMode;
		//.
		StartUpdatingSpaceImage();
	}
	
	public void ViewMode_Tiles_SetActiveCompilation(TTileImagery.TTileServerProviderCompilationDescriptors pDescriptors) {
		if (SpaceTileImagery != null) {
			CancelUpdatingSpaceImage();
			//.
			SpaceTileImagery.SetActiveCompilation(pDescriptors);
			//.
			Configuration.ReflectionWindow_ViewMode_Tiles_Compilation = pDescriptors.ToString();
			//.
			StartUpdatingSpaceImage();
		}
	}
    
	@Override
	public boolean onTouch(View pView, MotionEvent pEvent) {
		switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_DOWN:
			Pointer0_Down(pEvent.getX(0),pEvent.getY(0));
			break; //. >
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			Pointer0_Up(pEvent.getX(0),pEvent.getY(0));
			break; //. >
			
		case MotionEvent.ACTION_MOVE:
			Pointer0_Move((ImageView)pView, pEvent.getX(0),pEvent.getY(0));
			break; //. >
			
		default:
			return false; //. ->
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
        case R.id.Reflector_UpdateImage:
    		StartUpdatingSpaceImage();
        	//.
            return true; //. >
        
        case R.id.ReflectorCoGeoMonitorObjects:
        	try {
            	intent = new Intent(this, TReflectorCoGeoMonitorObjectsPanel.class);
            	startActivity(intent);
        	}
        	catch (Exception E) {
                Toast.makeText(this, getString(R.string.SError)+E.getMessage(), Toast.LENGTH_LONG).show();
        	}
        	//.
            return true; //. >
        

    	case R.id.ReflectorObjectTracks:
    		ObjectTracks.CreateTracksSelectorPanel(this).show();
    		//.
    		return true; //. >
    		
        case R.id.TrackerPanel:
            intent = new Intent(this, TTrackerPanel.class);
            startActivityForResult(intent,SHOW_TRACKER);
    		//.
    		return true; //. >
    
    	case R.id.ReflectorConfiguration:
            intent = new Intent(this, TReflectorConfigurationPanel.class);
            startActivityForResult(intent,EDIT_REFLECTOR_CONFIGURATION);
    		//.
    		return true; //. >
    
    	case R.id.ExitProgram:
    		Finish();
        	//.
        	return true; //. >
    	}
    
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        
        case SHOW_TRACKER:
            if (resultCode == Activity.RESULT_OK) 
            	Tracker_ShowCurrentLocation();
        	//.
            break; //. >

        case EDIT_REFLECTOR_CONFIGURATION: 
            break; //. >
        
    	case OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE:
    		/*///- if (SelectedComponentTypedDataFileNames_SelectorPanel != null) {
    			if ((SelectedObj.OwnerTypedDataFiles.Items != null) && (SelectedObj.OwnerTypedDataFiles.Items.length == 1)) {
    				SelectedComponentTypedDataFileNames_SelectorPanel.dismiss();
    				SelectedComponentTypedDataFileNames_SelectorPanel = null;
    			}
    		}*/
    		break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean ImageIsNotUpdated() {
    	return (SpaceImage.IsSegmenting());
    }
    
    public synchronized boolean IsUpdatingSpaceImage() {
    	return (_SpaceImageUpdating != null);
    }
    
    public synchronized void StartUpdatingSpaceImage(int Delay, boolean flUpdateProxySpace) {
    	CancelUpdatingSpaceImage();
    	//.
    	try {
    		_SpaceImageUpdating = new TSpaceImageUpdating(this,Delay,flUpdateProxySpace);
    	}
    	catch (Exception E) {    		
    	}
    	WorkSpace.invalidate();
    }
    
    public void StartUpdatingSpaceImage(int Delay) {
    	StartUpdatingSpaceImage(Delay,false);
    }

    public void StartUpdatingSpaceImage(boolean flUpdateProxySpace) {
    	StartUpdatingSpaceImage(0,flUpdateProxySpace);
    }
    
    public void StartUpdatingSpaceImage() {
    	StartUpdatingSpaceImage(0,false);
    }
    
    private void StartUpdatingCurrentSpaceImage() {
    	try {
        	SpaceImage.GrayScale();
        	WorkSpace.invalidate();
        	//.
        	StartUpdatingSpaceImage();
    	}
    	catch (Throwable E) {
    		String S = E.getMessage();
    		if (S == null)
    			S = E.getClass().getName();
            Toast.makeText(this, getString(R.string.SErrorOfUpdatingImage)+S, Toast.LENGTH_LONG).show();
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
    	WorkSpace.postInvalidate();
    }
    
    public void RecalculateSpaceImage() {
		SelectedObj_PrepareScreenNodes();
		ObjectTracks.RecalculateScreenNodes();
		CoGeoMonitorObjects.RecalculateVisualizationScreenLocation();
    }
    
    public void MoveReflectionWindow(TXYCoord Position) {
    	int RW_Xmd,RW_Ymd;
    	TXYCoord Pmd;
    	synchronized (ReflectionWindow) {
    		RW_Xmd = ReflectionWindow.Xmd;
    		RW_Ymd = ReflectionWindow.Ymd;
        	Pmd = ReflectionWindow.ConvertToScreen(Position.X,Position.Y);
		}
    	double dX = (RW_Xmd-Pmd.X);
    	double dY = (RW_Ymd-Pmd.Y);
		//.
		NavigationTransformatrix.reset();
    	ReflectionWindowTransformatrix.reset();
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate((float)dX,(float)dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate((float)dX,(float)dY);
		} 
		//.
		ReflectionWindow.SetReflection(Position.X,Position.Y);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		StartUpdatingSpaceImage();
    }
    
    public void TransformReflectionWindow(TReflectionWindowStruc RW) {
    	int RW_Xmd,RW_Ymd;
    	TXYCoord Pmd;
    	double ScaleFactor;
    	double Alpha;
    	synchronized (ReflectionWindow) {
    		RW_Xmd = ReflectionWindow.Xmd;
    		RW_Ymd = ReflectionWindow.Ymd;
        	Pmd = ReflectionWindow.ConvertToScreen((RW.X0+RW.X2)/2.0,(RW.Y0+RW.Y2)/2.0);
        	ScaleFactor = RW.Scale()/ReflectionWindow.Scale();
    		if ((ReflectionWindow.X1-ReflectionWindow.X0) != 0)
    		{
    			Alpha = Math.atan((ReflectionWindow.Y1-ReflectionWindow.Y0)/(ReflectionWindow.X1-ReflectionWindow.X0));
    			if (((ReflectionWindow.X1-ReflectionWindow.X0) < 0) && ((ReflectionWindow.Y1-ReflectionWindow.Y0) > 0)) Alpha = Alpha+Math.PI; else
    				if (((ReflectionWindow.X1-ReflectionWindow.X0) < 0) && ((ReflectionWindow.Y1-ReflectionWindow.Y0) < 0)) Alpha = Alpha+Math.PI; else
    				if (((ReflectionWindow.X1-ReflectionWindow.X0) > 0) && ((ReflectionWindow.Y1-ReflectionWindow.Y0) < 0)) Alpha = Alpha+2*Math.PI;
    		}
    		else
    		{
    			if ((ReflectionWindow.Y1-ReflectionWindow.Y0) >= 0) Alpha = Math.PI/2; else Alpha = -Math.PI/2;
    		};
		}
    	double dX = (RW_Xmd-Pmd.X);
    	double dY = (RW_Ymd-Pmd.Y);
    	double Betta,Gamma;
		if ((RW.X1-RW.X0) != 0)
		{
			Betta = Math.atan((RW.Y1-RW.Y0)/(RW.X1-RW.X0));
			if (((RW.X1-RW.X0) < 0) && ((RW.Y1-RW.Y0) > 0)) Betta = Betta+Math.PI; else
				if (((RW.X1-RW.X0) < 0) && ((RW.Y1-RW.Y0) < 0)) Betta = Betta+Math.PI; else
				if (((RW.X1-RW.X0) > 0) && ((RW.Y1-RW.Y0) < 0)) Betta = Betta+2*Math.PI;
		}
		else
		{
			if ((RW.Y1-RW.Y0) >= 0) Betta = Math.PI/2; else Betta = -Math.PI/2;
		};
		Gamma = (Betta-Alpha);
    	//.
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate((float)dX,(float)dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate((float)dX,(float)dY);
			SpaceImage.ResultBitmapTransformatrix.postScale((float)ScaleFactor,(float)ScaleFactor,ReflectionWindow.Xmd,ReflectionWindow.Ymd);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postScale((float)ScaleFactor,(float)ScaleFactor,ReflectionWindow.Xmd,ReflectionWindow.Ymd);
			SpaceImage.ResultBitmapTransformatrix.postRotate((float)(Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postRotate((float)(Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
		} 
		ReflectionWindowTransformatrix.postRotate(-(float)(Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postScale((float)(1.0/ScaleFactor),(float)(1.0/ScaleFactor),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
		ReflectionWindowTransformatrix.postTranslate(-(float)dX,-(float)dY);
		//.
		ReflectionWindow.MultiplyReflectionByMatrix(ReflectionWindowTransformatrix);
		//.
		NavigationTransformatrix.reset();
    	ReflectionWindowTransformatrix.reset();
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		StartUpdatingSpaceImage();
    }
    
    public void TranslateReflectionWindow(float dX, float dY) {
		NavigationTransformatrix.reset();
    	ReflectionWindowTransformatrix.reset();
		synchronized (SpaceImage) {
			SpaceImage.ResultBitmapTransformatrix.postTranslate(dX,dY);
			if (SpaceImage.flSegments)
				SpaceImage.SegmentsTransformatrix.postTranslate(dX,dY);
		} 
		//.
		ReflectionWindow.PixShiftReflection(dX,dY);
		//.
		RecalculateAndUpdateCurrentSpaceImage();
		//.
		StartUpdatingSpaceImage();
    }
    
    public void ShowPrevWindow() {
    	TReflectionWindowStruc CurrentRWS = null;
    	if (!IsUpdatingSpaceImage())
    		CurrentRWS = LastWindows.Pop(); //. skip current window
    	TReflectionWindowStruc RWS = LastWindows.Pop();
    	if (RWS != null)
    		TransformReflectionWindow(RWS);
    	else
    		if (CurrentRWS != null)
    			LastWindows.Push(CurrentRWS);
    }
    
    public void ClearReflections() throws IOException {
    	if (SpaceReflections != null)
    		SpaceReflections.Clear();
    	//.
    	StartUpdatingCurrentSpaceImage();
    }
    
    public void ClearHints() throws IOException {
    	if (SpaceHints != null) 
    		SpaceHints.Clear();
    	//.
    	StartUpdatingCurrentSpaceImage();
    }
    
    public void ClearTileImagery() throws IOException {
    	if (SpaceTileImagery != null)
    		SpaceTileImagery.ActiveCompilation_DeleteAllTiles();
    	//.
    	StartUpdatingCurrentSpaceImage();
    }
    
    public void ClearReflectionsAndHints() throws IOException {
    	if (SpaceReflections != null)
    		SpaceReflections.Clear();
    	if (SpaceHints != null) 
    		SpaceHints.Clear();
    	//.
    	StartUpdatingCurrentSpaceImage();
    }
    
    public void ClearVisualizations() throws IOException {
    	if (SpaceReflections != null)
    		SpaceReflections.Clear();
    	if (SpaceTileImagery != null)
    		SpaceTileImagery.ActiveCompilation_DeleteAllTiles();
    	if (SpaceHints != null) 
    		SpaceHints.Clear();
    	//.
    	StartUpdatingCurrentSpaceImage();
    }
    
    private void SelectedObj_PrepareScreenNodes() {
    	if (SelectedObj == null)
    		return; //. ->
    	if (SelectedObj.Nodes != null) {
    		TXYCoord[] ScrNodes = ReflectionWindow.ConvertNodesToScreen(SelectedObj.Nodes);
    		float[] pts = new float[ScrNodes.length << 2];
    		int Idx = 0;
    		for (int I = 0; I < (ScrNodes.length-1); I++) {
    			pts[Idx] = (float)ScrNodes[I].X; Idx++;
    			pts[Idx] = (float)ScrNodes[I].Y; Idx++;
    			pts[Idx] = (float)ScrNodes[I+1].X; Idx++;
    			pts[Idx] = (float)ScrNodes[I+1].Y; Idx++;
    		}
			pts[Idx] = (float)ScrNodes[(ScrNodes.length-1)].X; Idx++;
			pts[Idx] = (float)ScrNodes[(ScrNodes.length-1)].Y; Idx++;
			pts[Idx] = (float)ScrNodes[0].X; Idx++;
			pts[Idx] = (float)ScrNodes[0].Y; Idx++;
    		SelectedObj.ScreenNodes = pts;
    	}
    	else
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
    
    public HttpURLConnection OpenHttpConnection(String urlString) throws IOException
    {
        int response = -1;
        //.       
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
        //.         
        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException(getString(R.string.SNoHTTPConnection));
        //.
        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection)conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.setUseCaches(false);
            httpConn.setConnectTimeout(HttpConnection_ConnectTimeout);
            httpConn.setReadTimeout(HttpConnection_ReadTimeout);
            httpConn.connect(); 
            //.
            response = httpConn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) 
            	throw new IOException(getString(R.string.SServerError)+httpConn.getResponseMessage());                          
        }
        catch (SocketTimeoutException STE)
        {
            throw new IOException(getString(R.string.SConnectionTimeoutError));            
        }
        catch (Exception E)
        {
        	String S = E.getMessage();
        	if (S == null)
        		S = E.toString();
            throw new IOException(getString(R.string.SHTTPConnectionError)+S);            
        }
        return httpConn;     
    }
    
    public byte[] GetVisualizationOwnerDataDocument(int ptrObj, int Format, int DataType, boolean flWithComponents) throws Exception,IOException {
		String URL1 = ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "Functionality"+"/"+"VisualizationOwnerDataDocument.dat";
		//. add command parameters
		int WithComponentsFlag;
		if (flWithComponents)
			WithComponentsFlag = 1;
		else 
			WithComponentsFlag = 0;
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(ptrObj)+","+Integer.toString(Format)+","+Integer.toString(DataType)+","+Integer.toString(WithComponentsFlag);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		//.
		HttpURLConnection HttpConnection = OpenHttpConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				int RetSize = HttpConnection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				byte[] Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
	            //.
	            return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
    }
    
    public byte[] GetComponentDataDocument(int idTComponent, int idComponent, int Format, int DataType, boolean flWithComponents) throws Exception,IOException {
		String URL1 = ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
		//. add command parameters
		int WithComponentsFlag;
		if (flWithComponents)
			WithComponentsFlag = 1;
		else 
			WithComponentsFlag = 0;
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(idTComponent)+","+Integer.toString(idComponent)+","+Integer.toString(Format)+","+Integer.toString(DataType)+","+Integer.toString(WithComponentsFlag);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		//.
		HttpURLConnection HttpConnection = OpenHttpConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				int RetSize = HttpConnection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				byte[] Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
	            //.
	            return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
    }
    
    public byte[] GetCoGeoMonitorObjectData(int idCoComponent, int DataType) throws Exception,IOException {
		String URL1 = ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"TypedCo"+"/"+Integer.toString(SpaceDefines.idTCoGeoMonitorObject)+"/"+Integer.toString(idCoComponent)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(DataType);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		//.
		HttpURLConnection HttpConnection = OpenHttpConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				int RetSize = HttpConnection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				byte[] Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
	            //.
	            return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
    }
    
	public AlertDialog ComponentTypedDataFiles_CreateSelectorPanel(TComponentTypedDataFiles pComponentTypedDataFiles, Activity ParentActivity) {
		final TComponentTypedDataFiles ComponentTypedDataFiles = pComponentTypedDataFiles; 
		final CharSequence[] _items = new CharSequence[ComponentTypedDataFiles.Items.length];
		for (int I = 0; I < ComponentTypedDataFiles.Items.length; I++) 
			_items[I] = ComponentTypedDataFiles.Items[I].DataName+"("+SpaceDefines.TYPEDDATAFILE_TYPE_String(ComponentTypedDataFiles.Items[I].DataType)+")";
		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		builder.setTitle(R.string.SFiles);
		builder.setNegativeButton(getString(R.string.SCancel),null);
		builder.setSingleChoiceItems(_items, -1, new DialogInterface.OnClickListener() {
			
			private TComponentTypedDataFiles _ComponentTypedDataFiles = ComponentTypedDataFiles;
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				TComponentTypedDataFile ComponentTypedDataFile = _ComponentTypedDataFiles.Items[arg1];
				if (ComponentTypedDataFile.Data != null) {
	        		ComponentTypedDataFile_Open(ComponentTypedDataFile);					
				}
				else {
					//. convert from Name data to full data
					ComponentTypedDataFile.DataType = ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
					//.
					if (SelectedComponentTypedDataFileLoading != null)
						SelectedComponentTypedDataFileLoading.Cancel();
					SelectedComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(TReflector.this,ComponentTypedDataFile,MESSAGE_SELECTEDOBJ_OWNER_TYPEDDATAFILE_LOADED);
				}
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}

	public void ComponentTypedDataFile_Open(TComponentTypedDataFile ComponentTypedDataFile) {
	    Intent intent = null;
		switch (ComponentTypedDataFile.DataType) {
		
		case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
			try {
				if (ComponentTypedDataFile.Data == null)
					return; //. ->
				String Text = new String(ComponentTypedDataFile.Data,"windows-1251");
				byte[] TextData = Text.getBytes("utf-16");
				//.
				File TempFile = ComponentTypedDataFile.GetTempFile(); 
				FileOutputStream fos = new FileOutputStream(TempFile);
				try {
					fos.write(TextData,0,TextData.length);
				}
				finally {
					fos.close();
				}
				//. open appropriate extent
				intent = new Intent();   
	            intent.setDataAndType(Uri.fromFile(TempFile), "text/plain");   
			}
		    catch (Exception E) {
		    	Toast.makeText(TReflector.this, getString(R.string.SErrorOfPreparingDataFile)+ComponentTypedDataFile.FileName(), Toast.LENGTH_SHORT).show();
		    	return; //. ->
		    }
			break; //. >
			
		case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
			try {
				//. open appropriate extent
	            intent = new Intent();   
	            intent.setDataAndType(Uri.fromFile(ComponentTypedDataFile.CreateTempFile()), "image/*");   
			}
		    catch (Exception E) {
		    	Toast.makeText(TReflector.this, getString(R.string.SErrorOfPreparingDataFile)+ComponentTypedDataFile.FileName(), Toast.LENGTH_SHORT).show();
		    	return; //. ->
		    }
			break; //. >
			
		case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
			try {
				//. open appropriate extent
	            intent = new Intent();   
	            intent.setDataAndType(Uri.fromFile(ComponentTypedDataFile.CreateTempFile()), "audio/*");   
			}
		    catch (Exception E) {
		    	Toast.makeText(TReflector.this, getString(R.string.SErrorOfPreparingDataFile)+ComponentTypedDataFile.FileName(), Toast.LENGTH_SHORT).show();
		    	return; //. ->
		    }
			break; //. >
			
		case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
			try {
				//. open appropriate extent
	            intent = new Intent();   
	            intent.setDataAndType(Uri.fromFile(ComponentTypedDataFile.CreateTempFile()), "video/*");   
			}
		    catch (Exception E) {
		    	Toast.makeText(TReflector.this, getString(R.string.SErrorOfPreparingDataFile)+ComponentTypedDataFile.FileName(), Toast.LENGTH_SHORT).show();
		    	return; //. ->
		    }
			break; //. >
			
		default: 
			Toast.makeText(TReflector.this, R.string.SUnknownDataFileFormat, Toast.LENGTH_SHORT).show();
			return; //. ->
		}
        intent.setAction(android.content.Intent.ACTION_VIEW);   
        startActivityForResult(intent,OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE);              
	}
	
	public void ShowEditor() {
		Intent intent = new Intent(this, TReflectionWindowEditorPanel.class);
        startActivity(intent);
	}
	
	private void DoOnPointerClick(double X, double Y) {
    	try {
    		SelectedObj_CancelProcessing();
    		//. 
    		SelectedObj_Clear();
    		SpaceHints.UnSelectAll();
    		CoGeoMonitorObjects.UnSelectAll();
    		//.
    		boolean flSelected = false;
    		//.
    		int idxCoGeoMonitorObject = CoGeoMonitorObjects.Select((float)X,(float)Y);
    		if (idxCoGeoMonitorObject != -1) {
    			flSelected = true;
    			WorkSpace.invalidate();
    			//.
            	Intent intent = new Intent(this, TReflectorCoGeoMonitorObjectPanel.class);
            	intent.putExtra("Index", idxCoGeoMonitorObject);
            	startActivity(intent);
    		}
    		//.
    		TSpaceHint Hint = SpaceHints.Select(ReflectionWindow.GetWindow(), VisibleFactor, (float)X,(float)Y);
    		if (Hint != null) {
    			flSelected = true;
    			WorkSpace.invalidate();
    			//.
    			if (SelectedComponentTypedDataFileNamesLoading != null)
    				SelectedComponentTypedDataFileNamesLoading.Cancel();
    			if (Hint.InfoComponent_ID != 0)
    				SelectedComponentTypedDataFileNamesLoading = new TComponentTypedDataFileNamesLoading(this,Hint.InfoComponent_Type,Hint.InfoComponent_ID,MESSAGE_SELECTEDHINT_INFOCOMPONENT_TYPEDDATAFILENAMES_LOADED);
    			else 
    				Toast.makeText(this, Hint.InfoString, Toast.LENGTH_LONG).show();
    		}
        	//.
    		if (ObjectAtPositionGetting != null) {
    			ObjectAtPositionGetting.Cancel();
    			ObjectAtPositionGetting = null;
    		}
    		if (!flSelected) 
    			ObjectAtPositionGetting = ReflectionWindow.new TObjectAtPositionGetting(ReflectionWindow, X,Y, true, MESSAGE_SELECTEDOBJ_SET);
    	}
    	catch (Exception E) {
            Toast.makeText(this, getString(R.string.SErrorOfGettingObjectByCurrentPosition)+E.getMessage(), Toast.LENGTH_SHORT).show();
            return; //. ->
    	}
    }
    
    private void Pointer0_Down(double X, double Y)
	{
		if (!flEnabled)
			return; //. ->
		int idxButton = WorkSpace.Buttons.GetItemAt(X,Y);
		if (idxButton != -1) {
			WorkSpace.Buttons.SetDownButton(idxButton);
			WorkSpace.postInvalidate();
		}
		if (X < (WorkSpace.Width-(RotatingZoneWidth+ScalingZoneWidth))) NavigationType = TNavigationItem.NAVIGATIONTYPE_MOVING; else
			if (X < (WorkSpace.Width-RotatingZoneWidth)) NavigationType = TNavigationItem.NAVIGATIONTYPE_SCALING; else
			if (X >= (WorkSpace.Width-RotatingZoneWidth)) NavigationType = TNavigationItem.NAVIGATIONTYPE_ROTATING; else 
				NavigationType = TNavigationItem.NAVIGATIONTYPE_NONE; 
		//.
		NavigationTransformatrix.reset();
		ReflectionWindowTransformatrix.reset();
		//.
		Pointer_Down_StartPos.X = X; Pointer_Down_StartPos.Y = Y;
		Pointer_LastPos.X = X; Pointer_LastPos.Y = Y;
		//.
		SelectedObj_CancelProcessing();
		//.
		if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
			_MediaPlayer.stop();
	}

	private void Pointer0_Up(double X, double Y)
	{
		if (!flEnabled)
			return; //. ->
		int idxDownButton = WorkSpace.Buttons.DownButtonIndex;
		if ((idxDownButton != -1)) {
			boolean flPlayGeoLog = ((Calendar.getInstance().getTime().getTime()-WorkSpace.Buttons.DownButtons_Time) > 7000);
			WorkSpace.Buttons.ClearDownButton();
			WorkSpace.invalidate();
			//.
			int idxButton = WorkSpace.Buttons.GetItemAt(X,Y);
			if (idxButton == idxDownButton) {
				switch (idxButton) {
				case BUTTON_UPDATE:
					StartUpdatingCurrentSpaceImage();
					if (flPlayGeoLog) {
				        File GF = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Geo.Log"+"/"+"Lib"+"/"+"GeoLog.dat");
				        if (GF.exists()) {
							Intent intent = new Intent();
					        intent.setDataAndType(Uri.fromFile(GF), "audio/*");   
					        intent.setAction(android.content.Intent.ACTION_VIEW);   
					        startActivityForResult(intent,0);              
				        }
					}
					break; //. >

				case BUTTON_SHOWREFLECTIONPARAMETERS:
		            Intent intent = ReflectionWindow.CreateConfigurationPanel(this);
		            startActivity(intent);
					break; //. >

				/*///? case BUTTON_SUPERLAYS:
		            ReflectionWindow.getLays().SuperLays.CreateSelectorPanel(this).show();
					break; //. >*/
					
				case BUTTON_OBJECTS:
					intent = new Intent(this, TReflectorCoGeoMonitorObjectsPanel.class);
	            	startActivity(intent);
					break; //. >

				case BUTTON_TRACKER:
		            intent = new Intent(this, TTrackerPanel.class);
		            startActivityForResult(intent,SHOW_TRACKER);
		    		//.
					break; //. >

				case BUTTON_MAPOBJECTSEARCH:
		            intent = new Intent(this, TMapObjectsPanel.class);
		            startActivity(intent);
		    		//.
					break; //. >

				case BUTTON_ELECTEDPLACES:
		            intent = new Intent(this, TReflectorElectedPlacesPanel.class);
		            startActivity(intent);
		    		//.
					break; //. >

				case BUTTON_PREVWINDOW:
					ShowPrevWindow();
		    		//.
					break; //. >
					
				case BUTTON_EDITOR:
					if (GetViewMode() == VIEWMODE_TILES) 
						ShowEditor();
					else
			            Toast.makeText(this, R.string.SImageEditingAvailableInTileModeOnly, Toast.LENGTH_LONG).show();
		    		//.
					break; //. >
				}
			}
			return; //. ->
		}
		try {
			NavigationTransformatrix.reset();
			//.
			double dX = X-Pointer_Down_StartPos.X;
			double dY = Y-Pointer_Down_StartPos.Y;
			//.
			if ((Math.abs(dX) < SelectShiftFactor) && (Math.abs(dY) < SelectShiftFactor)) 
			{
				NavigationType = TNavigationItem.NAVIGATIONTYPE_NONE;
				DoOnPointerClick(Pointer_Down_StartPos.X,Pointer_Down_StartPos.Y);
				return ; //. ->
			};
			//.
			if (NavigationType != TNavigationItem.NAVIGATIONTYPE_NONE)
			{
				NavigationType = TNavigationItem.NAVIGATIONTYPE_NONE;
				//.
				ReflectionWindow.MultiplyReflectionByMatrix(ReflectionWindowTransformatrix);
				ReflectionWindowTransformatrix.reset();
				//.
				SpaceImage.ResetResultBitmap();
				//.
				RecalculateSpaceImage();
			};
		}
		finally {
			StartUpdatingSpaceImage(1000);
		}
	}
	
	private void Pointer0_Move(ImageView view, double X, double Y)
	{
		if (!flEnabled)
			return; //. ->
		//.
		if (WorkSpace.Buttons.DownButtonIndex != -1) {
			int idxButton = WorkSpace.Buttons.GetItemAt(X,Y);
			if (idxButton != WorkSpace.Buttons.DownButtonIndex) {
				WorkSpace.Buttons.ClearDownButton();
				WorkSpace.invalidate();
			}
			return; //. ->
		} 
		//. 
		/*///? if ((NavigationType == TNavigationItem.NAVIGATIONTYPE_NONE) && ((Math.abs(X-Pointer_Down_StartPos.X) < SelectShiftFactor) && (Math.abs(Y-Pointer_Down_StartPos.Y) < SelectShiftFactor))) 
			return ; //. ->*/
		//.
		CancelUpdatingSpaceImage();
		//.
		double dX = X-Pointer_LastPos.X;
		double dY = Y-Pointer_LastPos.Y;
		//.
		switch (NavigationType)
		{
			case TNavigationItem.NAVIGATIONTYPE_MOVING:
			{
				NavigationTransformatrix.postTranslate((float)dX,(float)dY);
				synchronized (SpaceImage) {
					SpaceImage.ResultBitmapTransformatrix.postTranslate((float)dX,(float)dY);
					if (SpaceImage.flSegments)
						SpaceImage.SegmentsTransformatrix.postTranslate((float)dX,(float)dY);
				} 
				ReflectionWindowTransformatrix.postTranslate(-(float)dX,-(float)dY);
				break; //. ->
			}

			case TNavigationItem.NAVIGATIONTYPE_SCALING:
			{
				double Scale = (1.0+ScaleCoef*dY/ReflectionWindow.getHeight());
				NavigationTransformatrix.postScale((float)Scale,(float)Scale,ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				synchronized (SpaceImage) {
					SpaceImage.ResultBitmapTransformatrix.postScale((float)Scale,(float)Scale,ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (SpaceImage.flSegments)
						SpaceImage.SegmentsTransformatrix.postScale((float)Scale,(float)Scale,ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				}
				ReflectionWindowTransformatrix.postScale((float)(1.0/Scale),(float)(1.0/Scale),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				break; //. ->
			}

			case TNavigationItem.NAVIGATIONTYPE_ROTATING:
			{
				double dX0,dY0;
				dX0 = (Pointer_LastPos.X-ReflectionWindow.Xmd);
				dY0 = (Pointer_LastPos.Y-ReflectionWindow.Ymd);
				double dX1,dY1;
				dX1 = (X-ReflectionWindow.Xmd);
				dY1 = (Y-ReflectionWindow.Ymd);
				double Alpha = Math.atan(dY0/dX0);
				double Betta = Math.atan(dY1/dX1); 
				double Gamma = -(Betta-Alpha);
				//.
				NavigationTransformatrix.postRotate((float)(-Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				synchronized (SpaceImage) {
					SpaceImage.ResultBitmapTransformatrix.postRotate((float)(-Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
					if (SpaceImage.flSegments)
						SpaceImage.SegmentsTransformatrix.postRotate((float)(-Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				}
				ReflectionWindowTransformatrix.postRotate((float)(Gamma*180.0/Math.PI),ReflectionWindow.Xmd,ReflectionWindow.Ymd);
				break; //. ->
			}
		};
		if (NavigationType != TNavigationItem.NAVIGATIONTYPE_NONE) {
			///-- SpaceImage.ResetResultBitmap();
			view.invalidate();
		}
		//.
		Pointer_LastPos.X = X; Pointer_LastPos.Y = Y;
	}
	
	public TXYCoord ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude) throws Exception {
		TXYCoord C = new TXYCoord();
		//.
		String URL1 = ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTGeoSpace)+"/"+"Co"+"/"+Integer.toString(Configuration.GeoSpaceID)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(DatumID)+","+Double.toString(Latitude)+","+Double.toString(Longitude);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		try {
			HttpURLConnection HttpConnection = OpenHttpConnection(URL);
			try {
				InputStream in = HttpConnection.getInputStream();
				try {
					byte[] Data = new byte[2*8/*SizeOf(Double)*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException(getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); 
				}
				finally {
					in.close();
				}                
			}
			finally {
				HttpConnection.disconnect();
			}
		} 
		catch (IOException E) {
			throw new Exception(E.getMessage());
		}
		return C;
	}
	
    public void Tracker_ShowCurrentLocation() {
    	if (!TTracker.TrackerIsEnabled()) {
            Toast.makeText(this, R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable, Toast.LENGTH_SHORT).show();
            return; //. ->
    	}
    	TGPSFixValue Fix;
    	TXYCoord Crd = new TXYCoord();
    	try {
    		Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
    		if (!Fix.IsSet()) {
                Toast.makeText(this, R.string.SCurrentPositionIsUnavailable, Toast.LENGTH_SHORT).show();
                return; //. ->
    		}
    		if (Fix.IsEmpty()) {
                Toast.makeText(this, R.string.SCurrentPositionIsUnknown, Toast.LENGTH_SHORT).show();
                return; //. ->
    		}
    		Crd = ConvertGeoCoordinatesToXY(TTracker.DatumID,Fix.Latitude,Fix.Longitude);
    	}
    	catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
            return; //. ->
    	}
    	MoveReflectionWindow(Crd);
		//.
		if (!Fix.IsAvailable())
            Toast.makeText(this, R.string.SNoCurrentPositionSetByLastCoords, Toast.LENGTH_LONG).show();
    }
    
    public void PlayAlarmSound() {
    	try {
    		if ((_MediaPlayer != null) && _MediaPlayer.isPlaying())
    			_MediaPlayer.stop();
    		_MediaPlayer = new MediaPlayer();
    		//.
        	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        	_MediaPlayer.setDataSource(this,alert);
        	final AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        	if (audio.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
        		_MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        		_MediaPlayer.setLooping(true);
        		_MediaPlayer.prepare();
        		_MediaPlayer.start();
        	}
    	}
    	catch (Exception E) {};
    }
}