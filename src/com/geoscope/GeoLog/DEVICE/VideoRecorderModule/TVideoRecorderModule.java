package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerVideoPhoneServer;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer.TGeographDataServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TLoadVideoRecorderConfigurationSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderActiveFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderAudioFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderRecordingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderSavingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderTransmittingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderVideoFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.TMediaFrameServer;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TVideoRecorderModule extends TModule {

	public static final String FolderName = "VideoRecorderModule"; 

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+FolderName;
	}
	
	public static final String VideoPhoneProfileFileName = "VideoPhone.xml";
	
	public static final short MODE_UNKNOWN					= 0;
	public static final short MODE_H264STREAM1_AMRNBSTREAM1 = 1;
	public static final short MODE_MPEG4					= 2;
	public static final short MODE_3GP						= 3;
	public static final short MODE_H263STREAM1_AMRNBSTREAM1 = 4;
	public static final short MODE_FRAMESTREAM 				= 5;

	public static final boolean 	RecorderIsHidden = false;
	public static final int 		RecorderWatcherInterval = 1000*60; //. seconds
	public static final int 		RecorderMeasurementFlushCounter = 2; //. minutes
	public static final int 		RecorderMeasurementCheckCounter = 1; //. minutes
	
	public static final int MESSAGE_OPERATION_COMPLETED 					= 1;
    public static final int MESSAGE_OPERATION_ERROR 						= 2;
	public static final int MESSAGE_CONFIGURATION_RECEIVED 					= 3;
	public static final int MESSAGE_SETSAVINGSERVER_OPERATION_COMPLETED 	= 4;
    public static final int MESSAGE_UPDATERECORDERSTATE 					= 5;
    public static final int MESSAGE_CHECKRECORDERMEASUREMENT 				= 6;
    public static final int MESSAGE_RESTARTRECORDER 						= 7;
	
    public static class TAudioNotifier extends TModule.TAudioNotifier {
    
    	@Override
    	protected String GetNotificationFolder() {
    		return TGeoLogApplication.Resources_GetCurrentFolder()+"/"+TGeoLogApplication.Resource_AudiosFolder+"/"+TDEVICEModule.DeviceFolderName+"/"+TVideoRecorderModule.FolderName; 
    	}
    	
    	public void Notification_RecordingIsStarted() throws Exception {
    		PlayNotification("RecordingIsStarted.aac");
    	}

    	public void Notification_RecordingIsFinished() throws Exception {
    		PlayNotification("RecordingIsFinished.aac");
    	}

    	public void Notification_VoiceActivatedRecordingIsStarted() throws Exception {
    		PlayNotification("VoiceRecordingIsStarted.aac");
    	}

    	public void Notification_VoiceActivatedRecordingIsFinished() throws Exception {
    		PlayNotification("VoiceRecordingIsFinished.aac");
    	}

    	public void Notification_TapActivatedRecordingIsStarted() throws Exception {
    		PlayNotification("TapRecordingIsStarted.aac");
    	}

    	public void Notification_TapActivatedRecordingIsFinished() throws Exception {
    		PlayNotification("TapRecordingIsFinished.aac");
    	}
    }
    
    public class TMeasurementConfiguration {
        public double MaxDuration = (1.0/(24*60))*60; //. minutes
        public double LifeTime  = 2.0; //. days
        public double AutosaveInterval = 1000; //. days
    }
    
    public class TCameraConfiguration {
        public int Camera_Audio_Source = 0;
        public int Camera_Audio_SampleRate = -1;
    	public int Camera_Audio_BitRate = -1;
    	public int Camera_Video_Source = 0;
    	public int Camera_Video_ResX = 640;
    	public int Camera_Video_ResY = 480;
    	public int Camera_Video_FrameRate = 10;
    	public int Camera_Video_BitRate = -1;
    }
    
    public static class TServerSaver implements Runnable {
    	
    	public static final String FolderName = "ServerSaver"; 

    	public static String Folder() {
    		return TVideoRecorderModule.Folder()+"/"+FolderName;
    	}
    	
    	public static class TMeasurements {
    		
    		public static final String FileName = "Measurements.xml";
    		
    		
    		private String Folder;
    		//.
    		private ArrayList<String> 	Items = new ArrayList<String>();
    		private int					Items_SummaryCount;
    		private int					Items_RemovedCount;
    		
    		public TMeasurements(String pFolder) throws Exception {
    			Folder = pFolder;
    			//.
    			Load();
    		}
    		
    		private synchronized void Clear() {
    			Items.clear();
    			Items_SummaryCount = 0;
    			Items_RemovedCount = 0;
    		}
    		
    		private synchronized void Load() throws Exception {
    			Clear();
    			//.
    			String FN = Folder+"/"+FileName;
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
				Element RootNode = XmlDoc.getDocumentElement();
				int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
    			switch (Version) {
    			
    			case 1:
					NodeList ItemsNode = TMyXML.SearchNode(RootNode,"Items").getChildNodes();
					int Cnt = ItemsNode.getLength();
					for (int I = 0; I < Cnt; I++) {
						Node ItemNode = ItemsNode.item(I);
						String MeasurementID = ItemNode.getFirstChild().getNodeValue();
						//.
						Items.add(MeasurementID);
						Items_SummaryCount++;
    				}
    				break; //. >
    				
    			default:
    				throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
    			}
    		}
    		
			public void Save() throws Exception {
    			String FN = Folder+"/"+FileName;
	            File F = new File(FN);
	            if (Items.size() == 0) {
	            	F.delete();
	            	return; //. ->
	            }
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
	                	int Cnt = Items.size();
	                	for (int I = 0; I < Cnt; I++) {
	    	            	serializer.startTag("", "Item"+Integer.toString(I));
    	            		serializer.text(Items.get(I));
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
			
			public synchronized void AddItem(String MeasurementID, boolean flSave) throws Exception {
				if (!ItemExists(MeasurementID)) {
					Items.add(MeasurementID);
					Items_SummaryCount++;
					//.
					if (flSave)
						Save();
				}
			}

			public synchronized void RemoveItem(String MeasurementID) throws Exception {
				int Cnt = Items.size();
				for (int I = 0; I < Cnt; I++) 
					if (Items.get(I).equals(MeasurementID)) {
						Items.remove(I);
						Items_RemovedCount++;
						//.
						Save();
						//.
						return; //. ->
					}
			}
			
			public synchronized boolean ItemExists(String MeasurementID) {
				int Cnt = Items.size();
				for (int I = 0; I < Cnt; I++)
					if (Items.get(I).equals(MeasurementID))
						return true; //. ->
				return false;
			}
			
			public synchronized int Count() {
				return Items.size();
			}
			
			public synchronized ArrayList<String> GetItems() {
				return (new ArrayList<String>(Items));
			}
			
			public synchronized double ProgressPercentage() {
				if (Items_SummaryCount == 0)
					return 100.0; //. ->
				return (100.0*Items_RemovedCount/Items_SummaryCount);
			}
    	}
    	
    	public static class TScheduler {
    		
    		public static final String FileName = "Scheduler.xml";
    		
    		public static class TDailyPlan {
    			
				public static final long DayInMs = 24*3600*1000;
				
    			public static class TItem {
    				
    				private TDailyPlan Plan;
    				//.
    				public boolean flEnabled = true;
    				//.
    				public double 	DayTime = 0.0;
    				private long 	DayTimeInMs = 0;
    				//.
    				private Runnable Process;
    				
    				public TItem(TDailyPlan pPlan) {
    					Plan = pPlan;
    					//.
    					Process = new Runnable() {
							
    						private TItem Item = TItem.this;
    						
							@Override
							public void run() {
								Plan.Scheduler.ServerSaver.StartProcess();
								//.
	    						DayTimeInMs += DayInMs;
	        					Plan.ProcessHandler.postDelayed(Item.Process, DayTimeInMs-System.currentTimeMillis());
							}
						};
    				}
    				
    				public void FromXMLNode(Node node) {
    					flEnabled = (Integer.parseInt(TMyXML.SearchNode(node,"Enabled").getFirstChild().getNodeValue()) != 0);
    					DayTime = Double.parseDouble(TMyXML.SearchNode(node,"DayTime").getFirstChild().getNodeValue());
    				}
    				
    				public void ToXMLSerializer(XmlSerializer serializer) throws Exception {
    	                serializer.startTag("", "Enabled");
    	                int V = 0;
    	                if (flEnabled)
    	                	V = 1;
    	                serializer.text(Integer.toString(V));
    	                serializer.endTag("", "Enabled");
    	                //.
    	                serializer.startTag("", "DayTime");
    	                serializer.text(Double.toString(DayTime));
    	                serializer.endTag("", "DayTime");
    				}
    				
    				public void Attach() {
    					Calendar c = Calendar.getInstance();
    					int Hours = (int)(DayTime*24.0);
    					c.set(Calendar.HOUR_OF_DAY, Hours);
    					int Mins = (int)((DayTime*24.0-Hours)*60.0);
    					c.set(Calendar.MINUTE, Mins);
    					long ProcessTimeInMs = c.getTimeInMillis();
    					long Delay = ProcessTimeInMs-System.currentTimeMillis();
    					if (Delay < 0) {
    						Delay += DayInMs;
    						ProcessTimeInMs += DayInMs;
    					}
    					//.
    					DayTimeInMs = ProcessTimeInMs;
    					Plan.ProcessHandler.postDelayed(Process, Delay);
    				}
    				
    				public void Detach() {
    					Plan.ProcessHandler.removeCallbacks(Process);
    				}
    				
    				public void SetEnabled(boolean pflEnabled) throws Exception {
    					if (pflEnabled != flEnabled) {
        					if (flEnabled)
        						Detach();
        					flEnabled = pflEnabled;
        					if (flEnabled)
        						Attach();
        					//.
        					Plan.Scheduler.Save();
    					}
    				}
    				
    				public void SetDayTime(double pDayTime) throws Exception {
    					if (pDayTime != DayTime) { 
        					if (flEnabled)
        						Detach();
        					DayTime = pDayTime;
        					if (flEnabled)
        						Attach();
        					//.
        					Plan.Scheduler.Save();
    					}
    				}
    			}
    			
    			private TScheduler Scheduler;
    			//.
    			public ArrayList<TItem> Items = new ArrayList<TItem>();
    			//.
        	    private Handler ProcessHandler = new Handler();
    			
    			public TDailyPlan(TScheduler pScheduler) {
    				Scheduler = pScheduler;
    			}
    			
    			public void Destroy() {
    				Stop();
    			}
    			
    			private void Clear() {
    				Stop();
    				Items.clear();
    			}
    			
        		private void FromXMLNode(Node node) throws Exception {
        			Clear();
        			//.
					NodeList ItemsNode = TMyXML.SearchNode(node,"Items").getChildNodes();
					int Cnt = ItemsNode.getLength();
					for (int I = 0; I < Cnt; I++) {
						Node ItemNode = ItemsNode.item(I);
						//.
						TItem Item = new TItem(this);
						Item.FromXMLNode(ItemNode);
						//.
						Items.add(Item);
    				}
        		}
        		
    			private void ToXMLSerializer(XmlSerializer serializer) throws Exception {
	                serializer.startTag("", "Items");
	                	int Cnt = Items.size();
	                	for (int I = 0; I < Cnt; I++) {
	    	            	serializer.startTag("", "Item"+Integer.toString(I));
	    	            	Items.get(I).ToXMLSerializer(serializer);
	    	            	serializer.endTag("", "Item"+Integer.toString(I));
	                	}
	                serializer.endTag("", "Items");
    			}
    			
    			public void Start() {
    				int Cnt = Items.size();
    				for (int I =0; I < Cnt; I++) {
    					TItem Item = Items.get(I);
    					if (Item.flEnabled)
    						Item.Attach();
    				}
    			}
    			
    			public void Stop() {
    				int Cnt = Items.size();
    				for (int I =0; I < Cnt; I++) { 
    					TItem Item = Items.get(I);
    					if (Item.flEnabled)
    						Item.Detach();
    				}
    			}
    		}
    		
    		private TServerSaver ServerSaver;
    		//.
    		private String Folder;
    		//.
    		public TDailyPlan Plan;
    		
    		public TScheduler(TServerSaver pServerSaver) throws Exception {
    			ServerSaver = pServerSaver;
    			Folder = TServerSaver.Folder();
    			//.
    			Plan = new TDailyPlan(this);
    			//.
    			Load();
    			//.
    			Plan.Start();
    		}
    		
    		public void Destroy() {
    			if (Plan != null) {
    				Plan.Destroy();
    				Plan = null;
    			}
    		}

    		private void Load() throws Exception {
    			String FN = Folder+"/"+FileName;
    			File F = new File(FN);
    			if (!F.exists()) {
    				//. default items
    				TDailyPlan.TItem Item = new TDailyPlan.TItem(Plan);
    				Item.DayTime = 10/24.0;
    				Plan.Items.add(Item);
    				Item = new TDailyPlan.TItem(Plan);
    				Item.DayTime = 14/24.0;
    				Plan.Items.add(Item);
    				Item = new TDailyPlan.TItem(Plan);
    				Item.DayTime = 18/24.0;
    				Plan.Items.add(Item);
    				Item = new TDailyPlan.TItem(Plan);
    				Item.DayTime = 20/24.0;
    				Plan.Items.add(Item);
    				return; //. ->
    			}
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
				Element RootNode = XmlDoc.getDocumentElement();
				int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
    			switch (Version) {
    			
    			case 1:
    				Node DailyPlanNode = TMyXML.SearchNode(RootNode,"DailyPlan");
    				if (DailyPlanNode != null) 
    					Plan.FromXMLNode(DailyPlanNode);
    				break; //. >
    				
    			default:
    				throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
    			}
    		}
    		
			protected void Save() throws Exception {
    			String FN = Folder+"/"+FileName;
	            File F = new File(FN);
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
	    	        //. DailyPlan
	                serializer.startTag("", "DailyPlan");
	                Plan.ToXMLSerializer(serializer);
	                serializer.endTag("", "DailyPlan");
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
    	}
    	
    	public static final int CONNECTION_TYPE_PLAIN 		= 0;
    	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
    	
    	public static final int ConnectTimeout = 1000*10; //. seconds
    	
    	private class SavingDataErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public SavingDataErrorException() {
    			super();
    		}
    	}
    	
    	private TVideoRecorderModule VideoRecorderModule;
    	//.
        public String 	ServerAddress = "127.0.0.1";
        public int		ServerPort = 5000;
    	protected int 	SecureServerPortShift = 2;
        protected int	SecureServerPort() {
        	return (ServerPort+SecureServerPortShift);
        }
    	//.
    	private TMeasurements Measurements;
        //.
        public int			ConnectionType = (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
        private Socket 		Connection;
        public InputStream 	ConnectionInputStream;
        public OutputStream ConnectionOutputStream;
        //.
    	protected Thread _Thread;
    	private boolean flCancel = false;
    	//.
    	public boolean 			flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	//.
    	private String MeasurementsIDsToProcess = "";
    	//.
    	public TScheduler Scheduler;
    	
    	public TServerSaver(TVideoRecorderModule pVideoRecorderModule, TSavingServerDescriptor ServerDescriptor) throws Exception {
    		VideoRecorderModule = pVideoRecorderModule;
    		//.
    		ServerAddress = ServerDescriptor.Address;
    		ServerPort = ServerDescriptor.Port;
        	//. 
    		File F = new File(Folder());
    		if (!F.exists()) 
    			F.mkdirs();
    		//.
    		Measurements = new TMeasurements(Folder());
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    		//.
    		Scheduler = new TScheduler(this);
    	}
    	
    	public void Destroy() throws IOException {
    		if (Scheduler != null) {
    			Scheduler.Destroy();
    			Scheduler = null;
    		}
    		//.
    		CancelAndWait();
    	}
    	
		@Override
		public void run() {
			flProcessing = true;
			try {
				try {
					byte[] TransferBuffer = new byte[1024*64];
					while (!flCancel) {
						//. transferring the measurements ...
						try {
							while (true) {
								ArrayList<String> ItemsToSave = Measurements.GetItems();
								int Cnt = ItemsToSave.size();
								if (Cnt == 0)
									break; //. ->
								for (int I = 0; I < Cnt; I++) {
									String MeasurementID = ItemsToSave.get(I);
									try {
										ProcessMeasurement(MeasurementID,TransferBuffer);
										//.
										Measurements.RemoveItem(MeasurementID);
									}
									catch (SavingDataErrorException E) {
										String S = E.getMessage();
										if (S == null)
											S = E.getClass().getName();
										VideoRecorderModule.Device.Log.WriteError("VideoRecorderModule.ServerSaver, error of saving measurement: "+MeasurementID,S);
									}
									//.
									if (flCancel)
										return; //. ->
								}
							}
						}
						catch (Throwable TE) {
			            	//. log errors
							String S = TE.getMessage();
							if (S == null)
								S = TE.getClass().getName();
							VideoRecorderModule.Device.Log.WriteError("VideoRecorderModule.ServerSaver",S);
			            	if (!(TE instanceof Exception))
			            		TGeoLogApplication.Log_WriteCriticalError(TE);
						}
						//. waiting for the signal
						ProcessSignal.WaitOne((int)(VideoRecorderModule.MeasurementConfiguration.AutosaveInterval*1000*3600*24));
						if (flCancel)
							return; //. ->
						//. collect measurements for transfer
						String _MeasurementsIDsToProcess;
						synchronized (this) {
							_MeasurementsIDsToProcess = MeasurementsIDsToProcess;
							MeasurementsIDsToProcess = "";
						}
						try {
							if (_MeasurementsIDsToProcess.equals("")) {
								File[] _Measurements = TVideoRecorderMeasurements.GetMeasurementsFolderList();
								for (int I = 0; I < _Measurements.length; I++) 
									if (_Measurements[I] != null) {
										String MeasurementID = _Measurements[I].getName();
										TMeasurementDescriptor CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
										if (!((CurrentMeasurement != null) && CurrentMeasurement.ID.equals(MeasurementID)))
											Measurements.AddItem(MeasurementID, false);
									}
								Measurements.Save();
							}
							else {
								String[] _Measurements = _MeasurementsIDsToProcess.split(",");
								for (int I = 0; I < _Measurements.length; I++) 
									if (_Measurements[I] != null) {
										String MeasurementID = _Measurements[I];
										TMeasurementDescriptor CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
										if (!((CurrentMeasurement != null) && CurrentMeasurement.ID.equals(MeasurementID))) 
											Measurements.AddItem(MeasurementID, false);
									}
								Measurements.Save();
							}
						}
						catch (Throwable TE) {
			            	//. log errors
							String S = TE.getMessage();
							if (S == null)
								S = TE.getClass().getName();
							VideoRecorderModule.Device.Log.WriteError("VideoRecorderModule.ServerSaver",S);
			            	if (!(TE instanceof Exception))
			            		TGeoLogApplication.Log_WriteCriticalError(TE);
						}
					}

				}
				catch (InterruptedException E) {
				}
			}
			finally {
				flProcessing = false;
			}
		}
		
		public void StartProcess(String MIDs) {
			synchronized (this) {
				MeasurementsIDsToProcess = MIDs;
			}
			ProcessSignal.Set();
		}
		
		public void StartProcess() {
			StartProcess("");		
		}
		
		public double ProcessProgressPercentage() {
			return Measurements.ProgressPercentage();
		}
		
		public int MeasurementsCount() {
			return Measurements.Count();
		}
		
    	public void Join() {
    		try {
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
		
		public void CancelAndWait() throws IOException {
    		Cancel();
    		try {
    			if (_Thread != null)
    				_Thread.join();
			} catch (InterruptedException e) {
			}
    	}
		
		private final int Connect_TryCount = 3;
		
	    private void Connect() throws Exception
	    {
			int TryCounter = Connect_TryCount;
			while (true) {
				try {
					try {
						//. connect
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
				    	    SSLContext sslContext = SSLContext.getInstance( "SSL" );
				    	    sslContext.init( null, _TrustAllCerts, new java.security.SecureRandom());
				    	    //. create a ssl socket factory with our all-trusting manager
				    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
				    		break; //. >
				    		
				    	default:
				    		throw new Exception("unknown connection type"); //. =>
				    	}
				        Connection.setSoTimeout(ConnectTimeout);
				        Connection.setKeepAlive(true);
				        Connection.setSendBufferSize(10000);
				        ConnectionInputStream = Connection.getInputStream();
				        ConnectionOutputStream = Connection.getOutputStream();
						break; //. >
					} catch (SocketTimeoutException STE) {
						throw new IOException(VideoRecorderModule.Device.context.getString(R.string.SConnectionTimeoutError)); //. =>
					} catch (ConnectException CE) {
						throw new ConnectException(VideoRecorderModule.Device.context.getString(R.string.SNoServerConnection)); //. =>
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.toString();
						throw new Exception(VideoRecorderModule.Device.context.getString(R.string.SHTTPConnectionError)+S); //. =>
					}
				}
				catch (Exception E) {
					TryCounter--;
					if (TryCounter == 0)
						throw E; //. =>
				}
			}
	    }
	    
	    private void Disconnect(boolean flDisconnectGracefully) throws IOException
	    {
	    	if (flDisconnectGracefully) {
		        //. close connection gracefully
		        byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(TGeographDataServerClient.MESSAGE_DISCONNECT);
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
		
	    private void Login() throws Exception {
	    	byte[] LoginBuffer = new byte[20];
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(TGeographDataServerClient.SERVICE_SETSENSORDATA_V1);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(VideoRecorderModule.Device.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray((int)VideoRecorderModule.Device.idGeographServerObject);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,18, BA.length);
			Buffer_Encrypt(LoginBuffer,10,10,VideoRecorderModule.Device.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != TGeographDataServerClient.MESSAGE_OK)
				throw new Exception(VideoRecorderModule.Device.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
		private void ProcessMeasurement(String MeasurementID, byte[] TransferBuffer) throws Exception {
			File[] MeasurementContent = TVideoRecorderMeasurements.GetMeasurementFolderContent(MeasurementID);
			if (MeasurementContent == null)
				return; //. ->
			ArrayList<File> ContentFiles = new ArrayList<File>(MeasurementContent.length);
			for (int I = 0; I < MeasurementContent.length; I++)
				if (!MeasurementContent[I].isDirectory() && (MeasurementContent[I].length() > 0)) 
					ContentFiles.add(MeasurementContent[I]);
			//.
			boolean flDisconnect = true;
			Connect();
			try {
				Login();
				//.
				byte[] DataIDBA = MeasurementID.getBytes("windows-1251");
				int Descriptor = DataIDBA.length;
				byte[] DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				if (Descriptor > 0)  
					ConnectionOutputStream.write(DataIDBA);
				//.
				int ContentFilesCount = ContentFiles.size(); 
				DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(ContentFilesCount);
				ConnectionOutputStream.write(DecriptorBA);
				//.
				for (int I = 0; I < ContentFilesCount; I++) {
					File ContentFile = ContentFiles.get(I);
					//.
					byte[] FileNameBA = ContentFile.getName().getBytes("windows-1251");
					Descriptor = FileNameBA.length;
					DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
					ConnectionOutputStream.write(DecriptorBA);
					if (Descriptor > 0)  
						ConnectionOutputStream.write(FileNameBA);
					//.
					long ContentFileSize = ContentFile.length(); 
					byte[] ContentFileSizeBA = TDataConverter.ConvertInt64ToLEByteArray(ContentFileSize);
					ConnectionOutputStream.write(ContentFileSizeBA);
					//.
					if (ContentFileSize > 0) {
						FileInputStream FIS = new FileInputStream(ContentFile);
						try {
							while (ContentFileSize > 0) {
								int BytesRead = FIS.read(TransferBuffer);
								ConnectionOutputStream.write(TransferBuffer,0,BytesRead);
								//.
								ContentFileSize -= BytesRead;
								//.
								if ((ContentFileSize > 0) && (ConnectionInputStream.available() >= 4)) {
									ConnectionInputStream.read(DecriptorBA);
									Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
									if (Descriptor == TGeographDataServerClient.MESSAGE_SAVINGDATAERROR) { 
										flDisconnect = false;
										throw new SavingDataErrorException(); //. =>
									}
								}
								//.
								if (flCancel) {
									Disconnect(false);
									flDisconnect = false;
									//.
									throw new InterruptedException(); //. =>
								}
							}
						}
						finally {
							FIS.close();
						}
						//. check ok
						ConnectionInputStream.read(DecriptorBA);
						Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
						if (Descriptor != TGeographDataServerClient.MESSAGE_OK) {
							flDisconnect = false;
							throw new SavingDataErrorException(); //. =>
						}
					}
				}
				flDisconnect = false;
				//. remove transferred measurement
				TVideoRecorderMeasurements.DeleteMeasurement(MeasurementID);
			}
			finally {
				if (flDisconnect)
					Disconnect();
			}
		}
    }
    
	public String 	Profile_Name = "";
	public boolean	Profile_flDefault = true;
	//.
	public TMeasurementConfiguration 	MeasurementConfiguration;
	public TCameraConfiguration 		CameraConfiguration;
	//.
	public TComponentTimestampedInt16Value 		Mode;
	public TVideoRecorderActiveValue 			Active;
	public TVideoRecorderRecordingValue 		Recording;
	public TComponentTimestampedBooleanValue 	Audio;
	public TComponentTimestampedBooleanValue 	Video;
	public TComponentTimestampedBooleanValue 	Transmitting;
	public TComponentTimestampedBooleanValue 	Saving;
	public TComponentTimestampedANSIStringValue	SDP;
	public TComponentTimestampedANSIStringValue	Receivers;
	public TComponentTimestampedANSIStringValue	SavingServer;
	//. virtual values
	public TVideoRecorderConfigurationDataValue	ConfigurationDataValue;
	//.
	public TMediaFrameServer MediaFrameServer = null;
	//.
    private Timer RecorderWatcher = null;
    private TServerSaver ServerSaver = null;

    public TVideoRecorderModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
        MeasurementConfiguration 	= new TMeasurementConfiguration();
        CameraConfiguration 		= new TCameraConfiguration();
        //.
        Mode 			= new TComponentTimestampedInt16Value();
        Active 			= new TVideoRecorderActiveValue(this);
        Recording 		= new TVideoRecorderRecordingValue(this);
        Audio 			= new TComponentTimestampedBooleanValue();
        Video			= new TComponentTimestampedBooleanValue();
        Transmitting	= new TComponentTimestampedBooleanValue();
        Saving			= new TComponentTimestampedBooleanValue();
        SDP 			= new TComponentTimestampedANSIStringValue();
        Receivers 		= new TComponentTimestampedANSIStringValue();
        SavingServer	= new TComponentTimestampedANSIStringValue();
    	//. virtual values
        ConfigurationDataValue = new TVideoRecorderConfigurationDataValue(this);
        //.
        MediaFrameServer = new TMediaFrameServer(this);
		//. workaround for ClassNotFoundException of TVideoRecorderServerVideoPhone class 
        TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() throws Exception {
    	Stop();
    	//.
    	if (MediaFrameServer != null) {
    		MediaFrameServer.Destroy();
    		MediaFrameServer = null;
    	}
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
        if (IsEnabled()) {
            try {
            	TVideoRecorderMeasurements.ValidateMeasurements();
            }
            catch (Exception E) {
                Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderMeasurementsCheckingFailed)+E.getMessage(), Toast.LENGTH_LONG).show();
            }
            //.
        	UpdateRecorderState();
			//.
        	StartReceivingConfiguration();
        	//.
            RecorderWatcher = new Timer();
            RecorderWatcher.schedule(new TRecorderWatcherTask(),RecorderWatcherInterval,RecorderWatcherInterval);
        }
    }
    
    @Override
    public void Stop() throws Exception {
    	if (ServerSaver != null) {
    		ServerSaver.Destroy();
    		ServerSaver = null;
    	}
    	//.
    	if (RecorderWatcher != null) {
    		RecorderWatcher.cancel();
    		RecorderWatcher = null;
    	}
    	//.
    	super.Stop();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String PFN = ModuleFile();
		if (LoadProfileFromFile(PFN))
			Profile_flDefault = true;
    }
    
    public synchronized boolean LoadProfileFromFile(String PFN) throws Exception {
		File F = new File(PFN);
		if (!F.exists()) 
			return false; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(PFN);
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
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("VideoRecorderModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
				Node node = RootNode.getElementsByTagName("flVideoRecorderModuleIsEnabled").item(0).getFirstChild();
				if (node != null)
					flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("Name").item(0).getFirstChild();
				if (node != null)
					Profile_Name = node.getNodeValue();
				node = RootNode.getElementsByTagName("Mode").item(0).getFirstChild();
				if (node != null)
					Mode.SetValue(OleDate.UTCCurrentTimestamp(),Short.parseShort(node.getNodeValue()));
				node = RootNode.getElementsByTagName("Active").item(0).getFirstChild();
				if (node != null)
					Active.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("Recording").item(0).getFirstChild();
				if (node != null)
					Recording.FromString(node.getNodeValue());
				NodeList NL = RootNode.getElementsByTagName("Audio");
				for (int I = 0; I < NL.getLength(); I++) {
					node = NL.item(I).getFirstChild();
					if ((node != null) && (node.getNodeValue() != null)) { 
						Audio.FromString(node.getNodeValue());
						break; //. >
					}
				}
				NL = RootNode.getElementsByTagName("Video");
				for (int I = 0; I < NL.getLength(); I++) {
					node = NL.item(I).getFirstChild();
					if ((node != null) && (node.getNodeValue() != null)) { 
						Video.FromString(node.getNodeValue());						
						break; //. >
					}
				}
				node = RootNode.getElementsByTagName("Transmitting").item(0).getFirstChild();
				if (node != null)
					Transmitting.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("Saving").item(0).getFirstChild();
				if (node != null)
					Saving.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("SDP").item(0).getFirstChild();
				if (node != null)
					SDP.SetValue(node.getNodeValue());
				node = RootNode.getElementsByTagName("Receivers").item(0).getFirstChild();
				if (node != null)
					Receivers.SetValue(node.getNodeValue());
				node = RootNode.getElementsByTagName("SavingServer").item(0).getFirstChild();
				if (node != null)
					SavingServer.SetValue(node.getNodeValue());
				//. measurement configuration
				node = RootNode.getElementsByTagName("MaxDuration").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.MaxDuration = Double.parseDouble(node.getNodeValue());
				node = RootNode.getElementsByTagName("LifeTime").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.LifeTime = Double.parseDouble(node.getNodeValue());
				node = RootNode.getElementsByTagName("AutosaveInterval").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.AutosaveInterval = Double.parseDouble(node.getNodeValue());
				//. camera configuration
				CameraConfiguration.Camera_Audio_Source = MediaRecorder.AudioSource.DEFAULT;
				node = RootNode.getElementsByTagName("AudioSource").item(0);
				if (node != null) {
					node = node.getFirstChild();
					if (node != null)
						CameraConfiguration.Camera_Audio_Source = Integer.parseInt(node.getNodeValue());
				}
				node = RootNode.getElementsByTagName("SampleRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Audio_SampleRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("ABitRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Audio_BitRate = Integer.parseInt(node.getNodeValue());
				CameraConfiguration.Camera_Video_Source = MediaRecorder.VideoSource.DEFAULT;
				node = RootNode.getElementsByTagName("VideoSource").item(0);
				if (node != null) {
					node = node.getFirstChild();
					if (node != null)
						CameraConfiguration.Camera_Video_Source = Integer.parseInt(node.getNodeValue());
				}
				node = RootNode.getElementsByTagName("ResX").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_ResX = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("ResY").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_ResY = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("FrameRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_FrameRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("VBitRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_BitRate = Integer.parseInt(node.getNodeValue());
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return true; 
    }
    
    public synchronized boolean LoadVideoPhoneProfile() throws Exception {
		String PFN = Folder()+"/"+VideoPhoneProfileFileName;
		if (LoadProfileFromFile(PFN)) {
			if (CameraConfiguration.Camera_Audio_Source == MediaRecorder.AudioSource.DEFAULT)
				CameraConfiguration.Camera_Audio_Source = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
			//.
			Profile_flDefault = false;
			return true; //. ->
		}
		else
			return false; //. ->			
    }
        
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
    	if (!Profile_flDefault)
    		LoadProfile();
		int Version = 1;
        Serializer.startTag("", "VideoRecorderModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //.
        int V = 0;
        if (flEnabled)
        	V = 1;
        Serializer.startTag("", "flVideoRecorderModuleIsEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flVideoRecorderModuleIsEnabled");
        //. 
        Serializer.startTag("", "Name");
        Serializer.text(Profile_Name);
        Serializer.endTag("", "Name");
        //. 
        Serializer.startTag("", "Mode");
        Serializer.text(Short.toString(Mode.GetValue()));
        Serializer.endTag("", "Mode");
        //. 
        Serializer.startTag("", "Active");
        Serializer.text(Active.ToString());
        Serializer.endTag("", "Active");
        //. 
        Serializer.startTag("", "Recording");
        Serializer.text(Recording.ToString());
        Serializer.endTag("", "Recording");
        //. 
        Serializer.startTag("", "Audio");
        Serializer.text(Audio.ToString());
        Serializer.endTag("", "Audio");
        //. 
        Serializer.startTag("", "Video");
        Serializer.text(Video.ToString());
        Serializer.endTag("", "Video");
        //. 
        Serializer.startTag("", "Transmitting");
        Serializer.text(Transmitting.ToString());
        Serializer.endTag("", "Transmitting");
        //. 
        Serializer.startTag("", "Saving");
        Serializer.text(Saving.ToString());
        Serializer.endTag("", "Saving");
        //. 
        Serializer.startTag("", "SDP");
        Serializer.text(SDP.GetValue());
        Serializer.endTag("", "SDP");
        //. 
        Serializer.startTag("", "Receivers");
        Serializer.text(Receivers.GetValue());
        Serializer.endTag("", "Receivers");
        //. 
        Serializer.startTag("", "SavingServer");
        Serializer.text(SavingServer.GetValue());
        Serializer.endTag("", "SavingServer");
        //. 
        Serializer.startTag("", "Measurement");
        //.
        Serializer.startTag("", "MaxDuration");
        Serializer.text(Double.toString(MeasurementConfiguration.MaxDuration));
        Serializer.endTag("", "MaxDuration");
        //.
        Serializer.startTag("", "LifeTime");
        Serializer.text(Double.toString(MeasurementConfiguration.LifeTime));
        Serializer.endTag("", "LifeTime");
        //.
        Serializer.startTag("", "AutosaveInterval");
        Serializer.text(Double.toString(MeasurementConfiguration.AutosaveInterval));
        Serializer.endTag("", "AutosaveInterval");
        //.
        Serializer.endTag("", "Measurement");
        //. 
        Serializer.startTag("", "Camera");
        //.
        Serializer.startTag("", "Audio");
        //.
        Serializer.startTag("", "SampleRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Audio_SampleRate));
        Serializer.endTag("", "SampleRate");
        //.
        Serializer.startTag("", "ABitRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Audio_BitRate));
        Serializer.endTag("", "ABitRate");
        //.
        Serializer.endTag("", "Audio");
        //.
        Serializer.startTag("", "Video");
        //.
        Serializer.startTag("", "ResX");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_ResX));
        Serializer.endTag("", "ResX");
        //.
        Serializer.startTag("", "ResY");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_ResY));
        Serializer.endTag("", "ResY");
        //.
        Serializer.startTag("", "FrameRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_FrameRate));
        Serializer.endTag("", "FrameRate");
        //.
        Serializer.startTag("", "VBitRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_BitRate));
        Serializer.endTag("", "VBitRate");
        //.
        Serializer.endTag("", "Video");
        //.
        Serializer.endTag("", "Camera");
        //.
        Serializer.endTag("", "VideoRecorderModule");
    }
    
    private void StartReceivingConfiguration() {
		TLoadVideoRecorderConfigurationSO GSO = new TLoadVideoRecorderConfigurationSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        try {
        	Device.ConnectorModule.OutgoingGetComponentDataOperationsQueue.AddNewOperation(GSO);
        } 
        catch (Exception E) {
			Toast.makeText(Device.context, Device.context.getString(R.string.SError)+E.toString(), Toast.LENGTH_LONG).show();
			return; //. ->
        }    	
    }
    
    public boolean Security_UserAccessCodeIsActive() {
    	return ((!Device.AudioModule.UserAccessKey.IsNull()) || (!Device.VideoModule.UserAccessKey.IsNull()));
    }
    
    public TReceiverDescriptor GetReceiverDescriptor() {
    	String S = Receivers.GetValue();
    	if ((S == null) || (S.equals("")))
    		return null; //. ->
    	String[] SA = S.split(";");
    	if (SA.length < 1)
    		return null; //. ->
    	String RS = SA[0];
    	SA = RS.split(",");
    	if (SA.length < 2)
    		return null; //. ->
    	TReceiverDescriptor Result = new TReceiverDescriptor();
    	if (!SA[0].equals(""))
    		Result.Address = SA[0];
    	else
    		if (Device.ConnectorModule != null)
    			Result.Address = Device.ConnectorModule.ServerAddress;
    		else
    			return null; //. ->
    	Result.ReceiverType = Integer.parseInt(SA[1]);
    	switch (Result.ReceiverType) {
    	case TReceiverDescriptor.RECEIVER_NATIVE: 
    		Result.AudioPort = TReceiverDescriptor.RECEIVER_NATIVE_AUDIOPORT; 
    		Result.VideoPort = TReceiverDescriptor.RECEIVER_NATIVE_VIDEOPORT; 
    		break; //. 
    	}
    	if (SA.length > 2)
    		Result.AudioPort = Integer.parseInt(SA[2]);
    	if (SA.length > 3)
    		Result.VideoPort = Integer.parseInt(SA[3]);
    	//.
    	return Result;
    }
    
    public TSavingServerDescriptor GetSavingServerDescriptor() {
    	String S = SavingServer.GetValue();
    	if ((S == null) || (S.equals("")))
    		return null; //. ->
    	String[] SA = S.split(";");
    	if (SA.length < 1)
    		return null; //. ->
    	String RS = SA[0];
    	SA = RS.split(",");
    	if (SA.length < 2)
    		return null; //. ->
    	TSavingServerDescriptor Result = new TSavingServerDescriptor();
    	if (!SA[0].equals(""))
    		Result.Address = SA[0];
    	else
    		if (Device.ConnectorModule != null)
    			Result.Address = Device.ConnectorModule.ServerAddress;
    		else
    			return null; //. ->
    	Result.ServerType = Integer.parseInt(SA[1]);
    	switch (Result.ServerType) {
    	case TSavingServerDescriptor.SAVINGSERVER_NATIVEFTP: 
    		Result.Port = TSavingServerDescriptor.SAVINGSERVER_NATIVEFTP_PORT; 
    		break; //. 
    	}
    	if (SA.length > 2)
    		Result.Port = Integer.parseInt(SA[2]);
    	if (SA.length > 3)
    		Result.BaseFolder = SA[3];
    	//.
    	return Result;
    }
    
    public Handler CompletionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
            	if (!flEnabled) {
            		if (Active.BooleanValue())
            			DeActivate();
            		return; //. ->
            	}
                switch (msg.what) {
                
                case MESSAGE_CONFIGURATION_RECEIVED:
                case MESSAGE_OPERATION_COMPLETED:
                	try {
    					SaveProfile();
    				} catch (Exception E) {
    	        		Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleLocalConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
    				}
    				//.
                	UpdateRecorderState();
    				//.
    		        if ((msg.what == MESSAGE_CONFIGURATION_RECEIVED) && (flEnabled) && (ServerSaver == null)) {
    		        	TSavingServerDescriptor SD = GetSavingServerDescriptor();
    		        	if (SD != null) 
    		        		synchronized (this) {
    			        		ServerSaver = new TServerSaver(TVideoRecorderModule.this, SD);
    						}
    		        }
                	break; //. >

                case MESSAGE_OPERATION_ERROR: 
                	Exception E = (Exception)msg.obj;
                	//.
            		Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleSettingConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
                	break; //. >
                	
                case MESSAGE_SETSAVINGSERVER_OPERATION_COMPLETED:
                	if (flEnabled) {
                		try {
                			synchronized (this) {
                        		if (ServerSaver != null)
                        			ServerSaver.Destroy();
            		        	TSavingServerDescriptor SD = GetSavingServerDescriptor();
            		        	if (SD != null)
            		        		ServerSaver = new TServerSaver(TVideoRecorderModule.this,SD);
    						}
        				}
        				catch (Exception E1) {
        					Toast.makeText(Device.context, Device.context.getString(R.string.SErrorOfCreatingTransmissionService)+E1.getMessage(), Toast.LENGTH_LONG).show();
        				}
                	}
                	break; //. >
                	
                case MESSAGE_UPDATERECORDERSTATE:
                	UpdateRecorderState();
                	break; //. >
                	
                case MESSAGE_CHECKRECORDERMEASUREMENT:
                	TVideoRecorder VideoRecorder = TVideoRecorder.GetVideoRecorder();
        			if ((VideoRecorder != null) && VideoRecorder.IsRecording() && VideoRecorder.flSaving) 
        				try {
        					TMeasurementDescriptor CurrentMeasurement = VideoRecorder.Recording_GetMeasurementDescriptor();
        					if ((CurrentMeasurement != null) && CurrentMeasurement.IsStarted()) {
        						double NowTime = TVideoRecorderMeasurements.GetCurrentTime();
        						if ((NowTime-CurrentMeasurement.StartTimestamp) > MeasurementConfiguration.MaxDuration)
        							ReinitializeRecorder();
        					}
        				}
        				catch (Exception E1) {
        					Toast.makeText(Device.context, Device.context.getString(R.string.SMeasurementCheckingError)+E1.getMessage(), Toast.LENGTH_LONG).show();
        				}
                	break; //. >
                	
                case MESSAGE_RESTARTRECORDER: 
    				try {
    					ReinitializeRecorder();
    				}
    				catch (Exception E1) {
    					Toast.makeText(Device.context, Device.context.getString(R.string.SMeasurementRestartError)+E1.getMessage(), Toast.LENGTH_LONG).show();
    				}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
    
    public void UpdateRecorderState() {
    	if (!flEnabled) 
    		return; //. ->
    	//.
    	TVideoRecorder VideoRecorder = TVideoRecorder.GetVideoRecorder();
    	if (Active.BooleanValue() && (VideoRecorder != null)) {
    		boolean flRestart = false;
    		boolean flStop = false;
			if (Recording.BooleanValue() && VideoRecorder.IsRecording()) {
				flRestart = (!((Mode.GetValue() == VideoRecorder.Mode) && (Transmitting.BooleanValue() == VideoRecorder.flTransmitting) && (Saving.BooleanValue() == VideoRecorder.flSaving) && (Audio.BooleanValue() == VideoRecorder.flAudio) && (Video.BooleanValue() == VideoRecorder.flVideo)));
			}
			else
				if (Recording.BooleanValue())
					flRestart = true;
				else
					if (VideoRecorder.IsRecording())
						flStop = true;
			if (flRestart) {
				boolean flSetTransmitting = ((Transmitting.BooleanValue() != VideoRecorder.flTransmitting) && (Mode.GetValue() == VideoRecorder.Mode) && (Saving.BooleanValue() == VideoRecorder.flSaving) && (Audio.BooleanValue() == VideoRecorder.flAudio) && (Video.BooleanValue() == VideoRecorder.flVideo));
				if (flSetTransmitting) {
					if (Transmitting.BooleanValue()) {
						if (Device.idGeographServerObject != 0)
							VideoRecorder.StartTransmitting(Device.idGeographServerObject);
					}
					else
						VideoRecorder.FinishTransmitting();
				}
				else {
    				TReceiverDescriptor RD = GetReceiverDescriptor();
            		if (RD != null) {
        				TVideoRecorderPanel.flHidden = RecorderIsHidden;
            	    	if (TVideoRecorderPanel.flHidden) {
            				TReflector RFL = TReflector.GetReflector();
            				if (RFL != null) {
                				TReflectorComponent Reflector = RFL.Component;
                	        	if (((Reflector != null) && Reflector.flVisible) || Video.BooleanValue())
                	        		TVideoRecorderPanel.flHidden = false;
            				}
            	    	}
            	    	//.
            			if (VideoRecorder.RestartRecording(RD, Mode.GetValue(), Transmitting.BooleanValue(), Saving.BooleanValue(), Audio.BooleanValue(),Video.BooleanValue(), Recording.flPreview) && (!TVideoRecorderPanel.flHidden));
            		}
				}
			}
			else
    			if (flStop)
    				FinishRecorder();
    	}
    	else
    		if (Active.BooleanValue()) 
    	    	if (Recording.flPreview || !MediaFrameServer.H264EncoderServer_IsAvailable()) {
        			if (!TVideoRecorderPanel.flStarting) {
                		TVideoRecorderPanel.flStarting = true;
                		//.
        				TVideoRecorderPanel.flHidden = RecorderIsHidden;
            	    	if (TVideoRecorderPanel.flHidden) {
            				TReflector RFL = TReflector.GetReflector();
            				if (RFL != null) {
                				TReflectorComponent Reflector = RFL.Component;
                	        	if (((Reflector != null) && Reflector.flVisible) || Video.BooleanValue())
                	        		TVideoRecorderPanel.flHidden = false;
            				}
            	    	}
            			//. start recorder with preview
                		Intent intent = new Intent(Device.context,TVideoRecorderPanel.class);
                		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                		Device.context.startActivity(intent);
        			}
    	    	}
    	    	else
            		new TVideoRecorder(Device.context, this); //. start recorder silently
    		else {
    			if (VideoRecorder != null)
    				VideoRecorder.Destroy();
    			//.
    	    	TVideoRecorderPanel VideoRecorderPanel = TVideoRecorderPanel.GetVideoRecorderPanel();
    			if (VideoRecorderPanel != null)
    				VideoRecorderPanel.finish();
    		}
    }
    
    public void PostUpdateRecorderState() {
    	CompletionHandler.obtainMessage(MESSAGE_UPDATERECORDERSTATE).sendToTarget();
    }
    
    public void FinishRecorder() {
    	TVideoRecorder VideoRecorder = TVideoRecorder.GetVideoRecorder();
    	if (VideoRecorder != null)
    		VideoRecorder.StopRecording();
		//.
		/*///??? synchronized (this) {
			if (ServerSaver != null)
				ServerSaver.StartProcess();
		}*/
    }
    
    public void ReinitializeRecorder() {
    	TVideoRecorder VideoRecorder = TVideoRecorder.GetVideoRecorder();
    	if ((VideoRecorder != null) && VideoRecorder.IsRecording())
    		FinishRecorder();
    	UpdateRecorderState();
    }
    
    public void PostRestartRecorder() {
    	CompletionHandler.obtainMessage(MESSAGE_RESTARTRECORDER).sendToTarget();
    }
    
    public synchronized TServerSaver GetServerSaver() {
    	return ServerSaver;
    }
    
    private class TRecorderWatcherTask extends TimerTask
    {
    	private int TicksCounter = 0;
    	
        public void UpdateRecorderState() {
        	CompletionHandler.obtainMessage(MESSAGE_UPDATERECORDERSTATE).sendToTarget();
        }
        
        public void FlashRecorderMeasurement() {
        	try {
        		TVideoRecorder.VideoRecorder_FlashMeasurement();
			} catch (Exception E) {
	        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_ERROR,new Exception(Device.context.getString(R.string.SMeasurementUpdatingError)+E.getMessage())).sendToTarget();        	
			}
        }
        
        public void CheckRecorderMeasurement() {
        	CompletionHandler.obtainMessage(MESSAGE_CHECKRECORDERMEASUREMENT).sendToTarget();
        }
        
        public void run() {
        	UpdateRecorderState();
        	//.
        	if ((TicksCounter % RecorderMeasurementFlushCounter) == 0)
        		FlashRecorderMeasurement();
        	//.
        	if ((TicksCounter % RecorderMeasurementCheckCounter) == 0)
        		CheckRecorderMeasurement();
        	//.
        	TicksCounter++;
        }
    }
    
    public void Activate() {
    	SetActive(true);
    	PostUpdateRecorderState();
    }
    
    public void DeActivate() {
    	SetActive(false);
    	PostUpdateRecorderState();
    }

    public void SetMode(short pMode, boolean flGlobal, boolean flPostProcess)
    {
        Mode.SetValue(OleDate.UTCCurrentTimestamp(),pMode);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderModeSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderModeSO)SO).setValue(Mode);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetMode(short pMode) {
        SetMode(pMode,true,true);
    }
    public void SetActive(boolean flActive, boolean flGlobal, boolean flPostProcess)
    {
    	if (flActive == Active.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flActive)
    		V = 1;
        Active.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderActiveFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderActiveFlagSO)SO).setValue(Active);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetActive(boolean flActive) {
    	SetActive(flActive,true,true);
    }
    
    public void CancelRecording(boolean flGlobal, boolean flPostProcess) {
    	SetActive(false, flGlobal,false);
    	SetRecording(false, flGlobal,flPostProcess, false);
    	//.
    	if (flPostProcess) {
        	//. to avoid connector queue loosing on crash
        	Device.BackupMonitor.BackupImmediate();
        	//.
        	PostUpdateRecorderState();
    	}
    }
    
    public void CancelRecording() {
    	CancelRecording(true,true);
    }
    
    public void SetupRecording(boolean flPreview) {
    	CancelRecording(true,false);
    	//.
    	SetRecording(true, true,false, flPreview);
    	SetActive(true);
    	//. to avoid connector queue loosing on crash
    	Device.BackupMonitor.BackupImmediate();
    	//.
    	PostUpdateRecorderState();
    }
    
    public void CancelRecordingLocally() {
    	CancelRecording(false,false);
    }
    
    public void SetupRecordingLocally(short pMode, boolean pflAudio, boolean pflVideo, boolean pflTransmitting, boolean pflSaving, boolean pflPreview) {
    	boolean flGlobal = false;
    	//.
    	CancelRecording(flGlobal,false);
    	//.
    	SetMode(pMode,flGlobal,false);
    	SetAudio(pflAudio,flGlobal,false);
    	SetVideo(pflVideo,flGlobal,false);
    	SetTransmitting(pflTransmitting,flGlobal,false);
    	SetSaving(pflSaving,flGlobal,false);
    	//.
    	SetRecording(true, flGlobal,false, pflPreview);
    	SetActive(true,flGlobal,false);
    	//. to avoid the connector queue loosing on crash
    	Device.BackupMonitor.BackupImmediate();
    	//.
    	PostUpdateRecorderState();
    }
    
    public void SetRecording(boolean flTrue, boolean flGlobal, boolean flPostProcess, boolean flPreview)
    {
    	if (flTrue == Recording.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Recording.SetValue(OleDate.UTCCurrentTimestamp(),V);
    	Recording.flPreview = flPreview;
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderRecordingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderRecordingFlagSO)SO).setValue(Recording);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetRecording(boolean flTrue, boolean flPreview) {
    	SetRecording(flTrue,true,true, flPreview);
    }
    
    public void SetAudio(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Audio.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Audio.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderAudioFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderAudioFlagSO)SO).setValue(Audio);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetAudio(boolean flTrue) {
    	SetAudio(flTrue,true,true);
    }
    
    public void SetVideo(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Video.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Video.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderVideoFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderVideoFlagSO)SO).setValue(Video);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetVideo(boolean flTrue) {
    	SetVideo(flTrue,true,true);
    }
    
    public void SetTransmitting(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Transmitting.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Transmitting.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderTransmittingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderTransmittingFlagSO)SO).setValue(Transmitting);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetTransmitting(boolean flTrue) {
    	SetTransmitting(flTrue,true,true);
    }
    
    public void SetSaving(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Saving.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Saving.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderSavingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderSavingFlagSO)SO).setValue(Saving);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetSaving(boolean flTrue) {
    	SetSaving(flTrue,true,true);
    }

    public void SetRecorderState(boolean Recording, boolean flPreview) {
		SetActive(Recording);
		SetRecording(Recording, flPreview);
		PostUpdateRecorderState();
    }
    
    public void SetRecorderState(boolean Recording) {
    	SetRecorderState(Recording, false);
    }
    
    public boolean IsRecording() {
    	return Recording.BooleanValue();
    }
    
    public void ShowPropsPanel(Context context) {
    	Intent intent = new Intent(Device.context,TVideoRecorderPropsPanel.class);
    	context.startActivity(intent);
    }
    
    public void ShowServerSaverPanel(Context context) {
    	Intent intent = new Intent(Device.context,TVideoRecorderServerSaverPanel.class);
    	context.startActivity(intent);
    }
    
	public String Measurements_GetList() {
		return TVideoRecorderMeasurements.GetMeasurementsList();
	}
	
	public String Measurements_GetList(double BeginTimestamp, double EndTimestamp) {
		return TVideoRecorderMeasurements.GetMeasurementsList(BeginTimestamp,EndTimestamp);
	}
	
	public void Measurements_Delete(String MeasurementID) throws IOException {
		TVideoRecorderMeasurements.DeleteMeasurement(MeasurementID);
	}
	
	public int Measurement_GetSize(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return TVideoRecorderMeasurements.GetMeasurementSize(MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	public byte[] Measurement_GetData(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return TVideoRecorderMeasurements.GetMeasurementData(MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	/* public byte[] Measurement_GetDataFragment(String MeasurementID, TMeasurementDescriptor CurrentMeasurement, double StartTimestamp, double FinishTimestamp, boolean flDescriptor, boolean flAudio, boolean flVideo) throws Exception {
		return TVideoRecorderMeasurements.GetMeasurementDataFragment(MeasurementID, CurrentMeasurement, StartTimestamp,FinishTimestamp, flDescriptor,flAudio,flVideo);
	} */
}
