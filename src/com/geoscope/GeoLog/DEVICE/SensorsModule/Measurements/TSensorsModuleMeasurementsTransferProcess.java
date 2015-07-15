package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements;

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

import android.os.Handler;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer.TGeographDataServerClient;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;

public class TSensorsModuleMeasurementsTransferProcess implements Runnable {

	public static final String FolderName = "MeasurementsTransferProcess"; 

	public static String Folder() {
		return TSensorsModule.Folder()+"/"+FolderName;
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
					if (Items.size() > 0)
						Items_RemovedCount++;
					else {
		    			Items_SummaryCount = 0;
		    			Items_RemovedCount = 0;
					}
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
				public boolean flEnabled;
				//.
				public double 	DayTime = 0.0;
				private long 	DayTimeInMs = 0;
				//.
				private Runnable Process;
				
				public TItem(TDailyPlan pPlan, boolean pflEnabled) {
					Plan = pPlan;
					flEnabled = pflEnabled;
					//.
					Process = new Runnable() {
						
						private TItem Item = TItem.this;
						
						@Override
						public void run() {
							Plan.Scheduler.TransferProcess.StartProcess();
							//.
    						DayTimeInMs += DayInMs;
        					Plan.ProcessHandler.postDelayed(Item.Process, DayTimeInMs-System.currentTimeMillis());
						}
					};
				}
				
				public TItem(TDailyPlan pPlan) {
					this(pPlan, false);
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
		
		private TSensorsModuleMeasurementsTransferProcess TransferProcess;
		//.
		private String Folder;
		//.
		public TDailyPlan Plan;
		
		public TScheduler(TSensorsModuleMeasurementsTransferProcess pTransferProcess) throws Exception {
			TransferProcess = pTransferProcess;
			Folder = TSensorsModuleMeasurementsTransferProcess.Folder();
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

		private void CreateDefaultItems() {
			double Time0 = 10; //. hours
			double Time1 = 14;
			double Time2 = 18;
			double Time3 = 20;
			//.
			TDailyPlan.TItem Item = new TDailyPlan.TItem(Plan, false);
			Item.DayTime = Time0/24.0;
			Plan.Items.add(Item);
			Item = new TDailyPlan.TItem(Plan, false);
			Item.DayTime = Time1/24.0;
			Plan.Items.add(Item);
			Item = new TDailyPlan.TItem(Plan, false);
			Item.DayTime = Time2/24.0;
			Plan.Items.add(Item);
			Item = new TDailyPlan.TItem(Plan, false);
			Item.DayTime = Time3/24.0;
			Plan.Items.add(Item);
		}
		
		private void Load() throws Exception {
			String FN = Folder+"/"+FileName;
			File F = new File(FN);
			if (!F.exists()) {
				CreateDefaultItems();
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
	
	private TSensorsModule SensorsModule;
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
	private ArrayList<String> Queue = new ArrayList<String>();
	//.
	public TScheduler Scheduler;
	
	public TSensorsModuleMeasurementsTransferProcess(TSensorsModule pSensorsModule, String pServerAddress, int pServerPort) throws Exception {
		SensorsModule = pSensorsModule;
		//.
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
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
									TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.TryWriteLock(TSensorsModuleMeasurements.Domain, MeasurementID, 1000);
									if (MeasurementLock != null)
										try {
											ProcessMeasurement(MeasurementID,TransferBuffer);
											//.
											Measurements.RemoveItem(MeasurementID);
										}
										finally {
											MeasurementLock.WriteUnLock();
										}
								}
								catch (SavingDataErrorException E) {
									String S = E.getMessage();
									if (S == null)
										S = E.getClass().getName();
									SensorsModule.Device.Log.WriteError("SensorsModule.MeasurementsTransferProcess, error of saving measurement: "+MeasurementID,S);
								}
								//.
								if (flCancel)
									return; //. ->
							}
							//.
							if (ProcessSignal.IsSignalled())
								break; //. >
						}
					}
					catch (Throwable TE) {
		            	//. log errors
						String S = TE.getMessage();
						if (S == null)
							S = TE.getClass().getName();
						SensorsModule.Device.Log.WriteError("SensorsModule.MeasurementsTransferProcess",S);
		            	if (!(TE instanceof Exception))
		            		TGeoLogApplication.Log_WriteCriticalError(TE);
					}
					//. waiting for a signal to process
					ProcessSignal.WaitOne();
					if (flCancel)
						return; //. ->
					//. collect measurements for transfer
					String[] _QueueContent = null;
					synchronized (this) {
						int Cnt = Queue.size();
						if (Cnt > 0) {
							_QueueContent = new String[Cnt];
							for (int I = 0; I < Cnt; I++)
								_QueueContent[I] = Queue.get(I);
							Queue.clear();
						}
					}
					if (_QueueContent != null)
						try {
							int CntII = _QueueContent.length;
							for (int II = 0; II < CntII; II++) {
								String QueueItem = _QueueContent[II];
								//.
								if (QueueItem.equals("")) {
									File[] _Measurements = TSensorsModuleMeasurements.GetMeasurementsFolderList();
									int CntI = _Measurements.length;
									if (CntI > 0) {
										for (int I = 0; I < CntI; I++) {
											String MeasurementID = _Measurements[I].getName();
											TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.TryWriteLock(TSensorsModuleMeasurements.Domain, MeasurementID);
											if (MeasurementLock != null)
												try {
													Measurements.AddItem(MeasurementID, false);
												}
												finally {
													MeasurementLock.WriteUnLock();
												}
										}
										Measurements.Save();
									}
								}
								else {
									String[] _Measurements = QueueItem.split(",");
									int CntI = _Measurements.length;
									if (CntI > 0) {
										for (int I = 0; I < CntI; I++) 
											if (_Measurements[I] != null) {
												String MeasurementID = _Measurements[I];
												TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.TryWriteLock(TSensorsModuleMeasurements.Domain, MeasurementID);
												if (MeasurementLock != null)
													try {
														Measurements.AddItem(MeasurementID, false);
													}
													finally {
														MeasurementLock.WriteUnLock();
													}
											}
										Measurements.Save();
									}
								}
							}
						}
						catch (Throwable TE) {
			            	//. log errors
							String S = TE.getMessage();
							if (S == null)
								S = TE.getClass().getName();
							SensorsModule.Device.Log.WriteError("SensorsModule.MeasurementsTransferProcess",S);
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
			Queue.add(MIDs);
		}
		ProcessSignal.Set();
	}
	
	public void StartProcess() {
		StartProcess("");		
	}
	
	public void StopProcess() {
		Measurements.Clear();
		//.
		ProcessSignal.Set();
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
					throw new IOException(SensorsModule.Device.context.getString(R.string.SConnectionTimeoutError)); //. =>
				} catch (ConnectException CE) {
					throw new ConnectException(SensorsModule.Device.context.getString(R.string.SNoServerConnection)); //. =>
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.toString();
					throw new Exception(SensorsModule.Device.context.getString(R.string.SHTTPConnectionError)+S); //. =>
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
		BA = TDataConverter.ConvertInt32ToLEByteArray(SensorsModule.Device.UserID);
		System.arraycopy(BA,0, LoginBuffer,2, BA.length);
		BA = TDataConverter.ConvertInt32ToLEByteArray((int)SensorsModule.Device.idGeographServerObject);
		System.arraycopy(BA,0, LoginBuffer,10, BA.length);
		short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
		BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
		System.arraycopy(BA,0, LoginBuffer,18, BA.length);
		Buffer_Encrypt(LoginBuffer,10,10,SensorsModule.Device.UserPassword);
		//.
		ConnectionOutputStream.write(LoginBuffer);
		byte[] DecriptorBA = new byte[4];
		ConnectionInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
		if (Descriptor != TGeographDataServerClient.MESSAGE_OK)
			throw new Exception(SensorsModule.Device.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
    }
    
	private void ProcessMeasurement(String MeasurementID, byte[] TransferBuffer) throws Exception {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(TSensorsModuleMeasurements.Domain, MeasurementID);
		try {
			File[] MeasurementContent = TSensorsModuleMeasurements.GetMeasurementFolderContent(MeasurementID);
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
				TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
			}
			finally {
				if (flDisconnect)
					Disconnect();
			}
		}
		finally {
			MeasurementLock.WriteUnLock();
		}
	}
}
