package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt166DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSModuleModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSModuleStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.MovementDetectorModule.TMovementDetectorModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;



/**
 *
 * @author AlxPonom
 */
@SuppressLint("HandlerLeak")
public class TGPSModule extends TModule implements Runnable 
{
	public static final int DatumID = 23; //. WGS-84
	//. Mode
    public static final short GPSMODULEMODE_DISABLED      = 0;
    public static final short GPSMODULEMODE_ENABLED       = 1;
    //. Status
    public static final short GPSMODULESTATUS_PERMANENTLYUNAVAILABLE      = -2;
    public static final short GPSMODULESTATUS_TEMPORARILYUNAVAILABLE      = -1;
    public static final short GPSMODULESTATUS_UNKNOWN                     = 0;
    public static final short GPSMODULESTATUS_AVAILABLE                   = 1;
	
	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"GPSModule";
	}
	
	public static String MapPOIComponentFolder() {
		return Folder()+"/"+"MapPOIComponent";
	}
	
    private static final int MinTimeBetweenFixSignals 	= 1; //. seconds
    private static final int WaitForFixInterval 		= 1000*900; //. seconds
    private static final int MaxLocationObtainingTime 	= 1000*(5*60); //. seconds
    //.
    private static final long	LocationManager_MinUpdateTime 		= 0; //. seconds, each fix is accepted
    private static final float 	LocationManager_MinUpdateDistance 	= 0.0F; //. meters, each fix is accepted 
    
    public static class FixTimeoutException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public FixTimeoutException(String Reason) {
			super(Reason);
		}
    }
    
    public class TMyLocationListener implements LocationListener,NmeaListener {

    	public static final double MovementFixSpeedLimit = 1.0; //. Km/h
    	///- public static final int SkipFixCount = 2;
    	///- public static final int SkipUnavailableFixCount = 59;

    	public class LocationProviderIsDisabledException extends IOException {
    		
			private static final long serialVersionUID = 1L;

			public LocationProviderIsDisabledException() {
    			super(Device.context.getString(R.string.SGPSModuleIsDisabled));
    		}
    	}
    	
		private TGPSModule GPSModule;
		public LocationManager LocationMgr;
		@SuppressWarnings("unused")
		private boolean flInpulseMode;
        public String	ProviderName;
        public int		ProviderStatus; //. Unknown status
        public long		ProviderStatusTime;
		private OleDate FixOleDateTime = new OleDate();
        private int FixCount = 0;
        private long MovementFixTime = 0;
		private TAutoResetEvent	_CurrentFixSignal = new TAutoResetEvent();
		private TGPSFixValue 	_CurrentFix = new TGPSFixValue();
		///- private double		 _CurrentFixPrecision = TGPSFixValue.UnknownFixPrecision;	
		///- private TGPSFixValue _Fix = new TGPSFixValue();
        ///- public int SkipFixCounter = SkipFixCount;
        ///- public int SkipUnavailableFixCounter = SkipUnavailableFixCount;

		
		private TMyLocationListener(TGPSModule pGPSModule, LocationManager pLocationManager, String pProviderName, boolean pflInpulseMode) {
			GPSModule = pGPSModule;
			LocationMgr = pLocationManager;
			ProviderName = pProviderName;
			flInpulseMode = pflInpulseMode;
			//.
	        ProviderStatus = -1; //. Unknown status
	        ProviderStatusTime = System.currentTimeMillis();
			//.
	        SetCurrentFixAsUnavailable();
		}

		public void Start() {
			ProviderStatus = -1; //. Unknown status
	        ProviderStatusTime = System.currentTimeMillis();
	        //.
			if (LocationMgr.isProviderEnabled(ProviderName))
				onProviderEnabled(ProviderName);
			else
				onProviderDisabled(ProviderName);
	        //.
	        ///- SkipFixCounter = SkipFixCount;
			///- _CurrentFixPrecision = TGPSFixValue.UnknownFixPrecision;	
			LocationMgr.requestLocationUpdates(ProviderName, LocationManager_MinUpdateTime, LocationManager_MinUpdateDistance, this);
			///- LocationMgr.addNmeaListener(this);
		}
		
		public void Stop() {
	    	///- LocationMgr.removeNmeaListener(this);
	    	LocationMgr.removeUpdates(this);
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			synchronized (this) {
				if (ProviderStatus != LocationProvider.OUT_OF_SERVICE) {
					ProviderStatus = LocationProvider.OUT_OF_SERVICE;
					ProviderStatusTime = System.currentTimeMillis();
					//.
					TGPSModule.this.SetMode(TGPSModule.GPSMODULEMODE_DISABLED);
				}
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			synchronized (this) {
				if (ProviderStatus != LocationProvider.TEMPORARILY_UNAVAILABLE) {
					ProviderStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
					ProviderStatusTime = System.currentTimeMillis();
					//.
					TGPSModule.this.SetMode(TGPSModule.GPSMODULEMODE_ENABLED);
					TGPSModule.this.SetStatus(TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE);
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			synchronized (this) {
				if (ProviderStatus != status) {
					ProviderStatus = status;
					ProviderStatusTime = System.currentTimeMillis();
					//.
					switch (ProviderStatus) {
					case LocationProvider.OUT_OF_SERVICE:
						TGPSModule.this.SetStatus(TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE);
						break; //. >
						
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						if (!(GPSModule.flImpulseMode && ((GPSModule.LocationMonitor != null) && GPSModule.LocationMonitor.flNeedToSleep)))
							TGPSModule.this.SetStatus(TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE);
						break; //. >
						
					case LocationProvider.AVAILABLE:
						TGPSModule.this.SetStatus(TGPSModule.GPSMODULESTATUS_AVAILABLE);
						break; //. >
					}
				}
			}
			//.
			if (GPSModule.LocationMonitor == null) {
				if (ProviderStatus != LocationProvider.AVAILABLE) {
					SetCurrentFixAsUnavailable();
				}
			}
		}
		
		public synchronized int GetProviderStatus() {
			return ProviderStatus;
		}

		public synchronized long GetProviderStatusTime() {
			return ProviderStatusTime;
		}

		@Override
		public void onLocationChanged(Location location) {
			try {
				synchronized (this) {
					if (ProviderStatus != LocationProvider.AVAILABLE) {
						ProviderStatus = LocationProvider.AVAILABLE;
						ProviderStatusTime = System.currentTimeMillis();
						//.
						TGPSModule.this.SetStatus(TGPSModule.GPSMODULESTATUS_AVAILABLE);
					}
				}
				//.
	            FixOleDateTime.SetTimeStamp(location.getTime());
	            double Speed = location.getSpeed()*3.6;
	            synchronized (_CurrentFix) {
	            	_CurrentFix.setValues(OleDate.UTCCurrentTimestamp(),FixOleDateTime.toDouble(), location.getLatitude(), location.getLongitude(), location.getAltitude(), Speed, location.getBearing(), location.getAccuracy());
					_CurrentFixSignal.Set();
					//.
	                FixCount++;
				}
	            //.
	            synchronized (this) {
	            	if (Speed > MovementFixSpeedLimit)
	            		MovementFixTime = System.currentTimeMillis();
				}
			}
			catch (Exception E) {
			}
		}

		@Override
		public void onNmeaReceived(long arg0, String arg1) {
			/*///- if (TGPSFixParser.Parse(arg1,_Fix)) {
				flFixIsAvailable = (_Fix.Precision != TGPSFixValue.UnavailableFixPrecision);
				//.
				if (flFixIsAvailable) {
					SkipUnavailableFixCounter = SkipUnavailableFixCount;
					if (SkipFixCounter > 0) {
						SkipFixCounter--;
						return; //. ->
					}
					//.
					if (_CurrentFixPrecision == TGPSFixValue.UnknownFixPrecision)
						return; //. ->
					_Fix.Precision = _CurrentFixPrecision;
				}
				else {
					if (SkipUnavailableFixCounter > 0) {
						SkipUnavailableFixCounter--;
						return; //. ->
					}
					//.
					if (flInpulseMode)
						return; //. ->
				}
				//.
	            synchronized (_CurrentFix) {
	            	_CurrentFix.Assign(_Fix);
					_CurrentFixSignal.Set();
					//.
	                if (flFixIsAvailable)
	                	FixCount++;
				}
	            return; //. ->
			}
			//.
			_CurrentFixPrecision = TGPSFixParser.ParsePrecision(arg1,_CurrentFixPrecision);*/
		}
		
		public void SetCurrentFixAsUnavailable() {
			synchronized (_CurrentFix) {
				_CurrentFix.SetFixAsUnAvailable(OleDate.UTCCurrentTimestamp(),GetUnavailableFixTimestamp());
				_CurrentFixSignal.Set();
			}
		}
		
		private void GetCurrentFix(TGPSFixValue _Fix) {
			synchronized (_CurrentFix) {
				_Fix.Assign(_CurrentFix);
			}
		}
		
		private boolean FetchCurrentFix(TGPSFixValue _Fix) {
			boolean Result = false;
			synchronized (_CurrentFix) {
				if (_CurrentFix.IsSet()) {
					_Fix.Assign(_CurrentFix);
					_CurrentFix.ClearSetFlag();
					//.
					Result = true;
				}
			}
			return Result; 
		}
		
		public double CurrentFixSpeed() {
			synchronized (_CurrentFix) {
				return _CurrentFix.Speed;
			}
		}
		
		public int GetFixCount() {
			synchronized (_CurrentFix) {
				return FixCount;
			}
		}
		
		public synchronized long GetMovementFixTime() {
			return MovementFixTime;
		}
		
		public double GetUnavailableFixTimestamp() {
			OleDate FixOleDateTime = new OleDate();
			Location Fix = LocationMgr.getLastKnownLocation(ProviderName);
			if (Fix != null) {
				FixOleDateTime.SetTimeStamp(Fix.getTime());
				return FixOleDateTime.toDouble()+1.0/(24*3600)/*+1 second*/; //. =>
			}
			else
				return OleDate.UTCCurrentTimestamp();
		}
	}
	
    public class TLocationMonitor extends TCancelableThread {
    	
    	public static final int STATE_SLEEPING		= 1;
    	public static final int STATE_PROCESSING 	= 2;
    	//.
    	public static final int 	Processing_WaitInterval = 900; //. ms
    	//.
    	public static final int 	NeedToSleepMinInterval 						= 1000*30/*seconds*/;
        private static final int 	MovementDetectingInterval 					= 1000*60; //. seconds
    	public static final int 	ActiveProviderDelayBeforeSleep 				= 1000*5; //. seconds
    	public static final double 	MovementFix_ActiveProviderDelayBeforeSleep 	= 1000*60; //. seconds
    	
    	
    	private TGPSModule GPSModule;
    	//. 
    	private int 	WaitInterval;
    	//.
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	//.
    	private int State = STATE_SLEEPING;
    	//.
    	private boolean flNeedToSleep;
    	
    	private TLocationMonitor(TGPSModule pGPSModule) {
    		super();
    		//.
    		GPSModule = pGPSModule;
    		//.
			flNeedToSleep = (GPSModule.Provider_ReadInterval >= NeedToSleepMinInterval);
			//.
			WaitInterval = GPSModule.Provider_ReadInterval;
			//.
    		_Thread = new Thread(this);
    		//.
    		Start();
    		//.
    		Process();
    	}
    	
    	private void SetWaitInterval(int pWaitInterval) {
    		WaitInterval = pWaitInterval;
    	}
    	
		public void Start() {
    		_Thread.start();
		}
		
		public void Process() {
			ProcessSignal.Set();
		}

		@Override
		public void Cancel() {
			super.Cancel();
			//.
			ProcessSignal.Set();
		}

		@Override
		public void CancelAndWait() throws InterruptedException {
			Cancel();
			// .
			Wait();
		}
		
		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					ProcessSignal.WaitOne(WaitInterval);
					if (Canceller.flCancel)
						return; //. ->
					//.
		        	DoProcess();
				}
			}
			catch (InterruptedException IE) {
			}
			catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
			}
		}
		
    	private static final int MESSAGE_CONNECT 	= 1;
    	private static final int MESSAGE_DISCONNECT = 2;
    	
	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_CONNECT:
		    			if (Canceller.flCancel)
		    				return; //. ->
		    			//.
						TLocationMonitor.this.GPSModule.Connect();
		            	break; //. >

		            case MESSAGE_DISCONNECT:
		    			if (Canceller.flCancel)
		    				return; //. ->
		    			//.
		    			TLocationMonitor.this.GPSModule.Disconnect();
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
	    
		private void DoProcess() {
			int _State;
			synchronized (this) {
				_State = State;
			}
			switch (_State) {
			case STATE_SLEEPING:
				try {
					//. connect
		        	MessageHandler.obtainMessage(MESSAGE_CONNECT).sendToTarget();
					//.
					///- State_Processing_FixCount = LocationMonitor.GPSModule.MyLocationListener.GetFixCount();
					///- State_Processing_Timestamp = NowTicks;
					synchronized (this) {
						State = STATE_PROCESSING;
					}
					//.
					SetWaitInterval(Processing_WaitInterval);
				} catch (Exception E) {
					LocationMonitor.GPSModule.SetProcessException(E);
				}
				break; //. >
				
			case STATE_PROCESSING:
				long NowTicks = System.currentTimeMillis();
				//.
				///- int FixCount = LocationMonitor.GPSModule.MyLocationListener.GetFixCount();
				///- int FixProcessedCount = (FixCount-State_Processing_FixCount);
				if (/*///- (FixProcessedCount > 0) && */((LocationMonitor.GPSModule.MyLocationListener.GetProviderStatus() == LocationProvider.AVAILABLE) && ((NowTicks-LocationMonitor.GPSModule.MyLocationListener.GetProviderStatusTime()) > ActiveProviderDelayBeforeSleep))) {
					TMovementDetectorModule MovementDetector = LocationMonitor.GPSModule.Device.MovementDetectorModule;
					boolean MovementDetectorIsActive = MovementDetector.IsPresent() && MovementDetector.IsActive(MovementDetectingInterval);
					if (
							!DestinationChannel_Active() &&
							!LocationMonitor.GPSModule.Device.flUserInteractive &&
							(flNeedToSleep && ((!MovementDetectorIsActive) || (!MovementDetector.IsMovementDetected(MovementDetectingInterval)))) &&
							((!LocationMonitor.GPSModule.flIgnoreImpulseModeSleepingOnMovement) || ((NowTicks-LocationMonitor.GPSModule.MyLocationListener.GetMovementFixTime()) > MovementFix_ActiveProviderDelayBeforeSleep))
						){
						try {
							//. disconnect
				        	MessageHandler.obtainMessage(MESSAGE_DISCONNECT).sendToTarget();
							//.
							synchronized (this) {
								State = STATE_SLEEPING;
							}
							//.
							SetWaitInterval(GPSModule.Provider_ReadInterval);
						} catch (Exception E) {
							LocationMonitor.GPSModule.SetProcessException(E);
						}
					}
				}
				else {
					if ((LocationMonitor.GPSModule.MyLocationListener.GetProviderStatus() != LocationProvider.AVAILABLE) && ((NowTicks-LocationMonitor.GPSModule.MyLocationListener.GetProviderStatusTime()) > MaxLocationObtainingTime)) {
						try {
							//. disconnect
				        	MessageHandler.obtainMessage(MESSAGE_DISCONNECT).sendToTarget();
							//.
							LocationMonitor.GPSModule.MyLocationListener.SetCurrentFixAsUnavailable();
							//.
							synchronized (this) {
								State = STATE_SLEEPING;
							}
							//.
							SetWaitInterval(GPSModule.Provider_ReadInterval);
						} catch (Exception E) {
							LocationMonitor.GPSModule.SetProcessException(E);
						}
					}
				}
				break; //. >
			}
		}
		
		public synchronized int GetState() {
			return State;
		}
    }
    
    public class TMapPOIConfiguration {
    	public int 		Image_ResX = 640;
    	public int 		Image_ResY = 480;
    	public int 		Image_Quality = 75;
    	public String	Image_Format = "jpg";
    	public int 		MediaFragment_Audio_SampleRate = -1;
    	public int 		MediaFragment_Audio_BitRate = -1;
    	public int 		MediaFragment_Video_ResX = 640;
    	public int 		MediaFragment_Video_ResY = 480;
    	public int 		MediaFragment_Video_FrameRate = 10;
    	public int 		MediaFragment_Video_BitRate = -1;
    	public int 		MediaFragment_MaxDuration = -1;
    	public String	MediaFragment_Format = "3gp";
    }
    
	public TComponentTimestampedInt16Value	Mode;
	public TComponentTimestampedInt16Value 	Status;
	//. virtual values
	public TGPSModuleConfigurationDataValue	ConfigurationDataValue;
	//.
    public int Provider_ReadInterval = 5*1000; //. milliseconds
    public int 					MapID = 0;
    public TMapPOIConfiguration MapPOIConfiguration;
	//.
    public boolean 				flImpulseMode = false;
    public boolean				flIgnoreImpulseModeSleepingOnMovement = true;
	public boolean				flProcessingIsDisabled = false;
    private	LocationManager 	MyLocationManager;
    private TMyLocationListener MyLocationListener = null;
    public TLocationMonitor 	LocationMonitor = null;
    //.
    private Thread m_thread;
    public boolean flConnected = false;
    public boolean flTerminated = false;
    public boolean flProcessing = false;
    private Exception ProcessException = null;
    public TGPSFixValue LastFix;
    public TGPSFixValue CurrentFix;
    //.
    public boolean flGPSFixing = false;
    public TComponentInt16Value Threshold = null;
    //.
    private TGPSChannel 									DestinationChannel = null;
    private TTimestampedInt16ContainerType.TValue 			DestinationChannel_GPSMode;
    private TTimestampedInt16ContainerType.TValue 			DestinationChannel_GPSStatus;
    private TTimestampedInt166DoubleContainerType.TValue 	DestinationChannel_GPSFix;
    
    
    public TGPSModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
    	//. 
		F = new File(MapPOIComponentFolder());
		if (!F.exists()) 
			F.mkdirs();
        //.
        MapPOIConfiguration = new TMapPOIConfiguration();
        //.
        Mode 	= new TComponentTimestampedInt16Value();
        Status	= new TComponentTimestampedInt16Value();
    	//. virtual values
        ConfigurationDataValue = new TGPSModuleConfigurationDataValue(this);
        //.
        LastFix = new TGPSFixValue();
        CurrentFix = new TGPSFixValue();
        Threshold = new TComponentInt16Value();
    	flProcessingIsDisabled = false;
    	flImpulseMode = false;
    	//.
        Threshold.SetValue((short)100); //. default, meters
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SGPSModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
        //.
        if (Provider_ReadInterval < 0) 
        	flProcessingIsDisabled = true;
        else
        	flImpulseMode = (Provider_ReadInterval > 0);
    }
    
    public void Destroy() throws Exception
    {
    	Stop();
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
        if (IsEnabled()) {
            MyLocationManager = (LocationManager)Device.context.getSystemService(Activity.LOCATION_SERVICE);
    		MyLocationListener = new TMyLocationListener(this,MyLocationManager,LocationManager.GPS_PROVIDER,flImpulseMode);
            //.
    		if (flImpulseMode)
    			LocationMonitor = new TLocationMonitor(this);
    		else
    			Connect();
            //.
    		if (!flProcessingIsDisabled) {
    	        flTerminated = false;
    	        m_thread = new Thread(this);
    	        m_thread.start();
    		}
        }
    }
    
    @Override
    public void Stop() throws Exception {
        Terminate();
        //.
        if (LocationMonitor != null) {
        	LocationMonitor.Destroy();
        	LocationMonitor = null;
        }
        Disconnect();
    	//.
    	super.Stop();
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
				Node node;
				try {
					node = RootNode.getElementsByTagName("Provider_ReadInterval").item(0).getFirstChild();
					if (node != null)
						Provider_ReadInterval = Integer.parseInt(node.getNodeValue());
				}
				catch (Exception E) {}
				try {
					node = RootNode.getElementsByTagName("IgnoreImpulseModeSleepingOnMovement").item(0).getFirstChild();
					if (node != null)
						flIgnoreImpulseModeSleepingOnMovement = (Integer.parseInt(node.getNodeValue()) != 0);
				}
				catch (Exception E) {}
				try {
					node = RootNode.getElementsByTagName("Threshold").item(0).getFirstChild();
					if (node != null) {
						int _Threshold = Integer.parseInt(node.getNodeValue());
						Threshold.SetValue((short)_Threshold);
					}
				}
				catch (Exception E) {}
				node = RootNode.getElementsByTagName("MapID").item(0).getFirstChild();
				if (node != null)
					MapID = Integer.parseInt(node.getNodeValue());
				//.
				node = RootNode.getElementsByTagName("IResX").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.Image_ResX = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("IResY").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.Image_ResY = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("IQuality").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.Image_Quality = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("IFormat").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.Image_Format = node.getNodeValue();
				node = RootNode.getElementsByTagName("MFSampleRate").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Audio_SampleRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFABitRate").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Audio_BitRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFResX").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Video_ResX = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFResY").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Video_ResY = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFFrameRate").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Video_FrameRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFVBitRate").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Video_BitRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFMaxDuration").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_MaxDuration = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("MFFormat").item(0).getFirstChild();
				if (node != null)
					MapPOIConfiguration.MediaFragment_Format = node.getNodeValue();
				//.
		        ///? load LastFix.Assign(GPSLastFix);
		        CurrentFix.Assign(LastFix);
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
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "GPSModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.startTag("", "Provider_ReadInterval");
        Serializer.text(Integer.toString(Provider_ReadInterval));
        Serializer.endTag("", "Provider_ReadInterval");
        //.
        int V = 0;
        if (flIgnoreImpulseModeSleepingOnMovement)
        	V = 1;
        Serializer.startTag("", "IgnoreImpulseModeSleepingOnMovement");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "IgnoreImpulseModeSleepingOnMovement");
        //. 
        Serializer.startTag("", "Threshold");
        Serializer.text(Integer.toString(Threshold.GetValue()));
        Serializer.endTag("", "Threshold");
        //. 
        Serializer.startTag("", "MapID");
        Serializer.text(Integer.toString(MapID));
        Serializer.endTag("", "MapID");
        //.
        Serializer.startTag("", "MapPOI");
        //. 
        Serializer.startTag("", "Image");
        //. 
        Serializer.startTag("", "IResX");
        Serializer.text(Integer.toString(MapPOIConfiguration.Image_ResX));
        Serializer.endTag("", "IResX");
        //. 
        Serializer.startTag("", "IResY");
        Serializer.text(Integer.toString(MapPOIConfiguration.Image_ResY));
        Serializer.endTag("", "IResY");
        //. 
        Serializer.startTag("", "IQuality");
        Serializer.text(Integer.toString(MapPOIConfiguration.Image_Quality));
        Serializer.endTag("", "IQuality");
        //. 
        Serializer.startTag("", "IFormat");
        Serializer.text(MapPOIConfiguration.Image_Format);
        Serializer.endTag("", "IFormat");
        //.
        Serializer.endTag("", "Image");
        //.
        Serializer.startTag("", "MediaFragment");
        //.
        Serializer.startTag("", "Audio");
        //. 
        Serializer.startTag("", "MFSampleRate");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Audio_SampleRate));
        Serializer.endTag("", "MFSampleRate");
        //. 
        Serializer.startTag("", "MFABitRate");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Audio_BitRate));
        Serializer.endTag("", "MFABitRate");
        //.
        Serializer.endTag("", "Audio");
        //.
        Serializer.startTag("", "Video");
        //. 
        Serializer.startTag("", "MFResX");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Video_ResX));
        Serializer.endTag("", "MFResX");
        //. 
        Serializer.startTag("", "MFResY");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Video_ResY));
        Serializer.endTag("", "MFResY");
        //. 
        Serializer.startTag("", "MFFrameRate");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Video_FrameRate));
        Serializer.endTag("", "MFFrameRate");
        //. 
        Serializer.startTag("", "MFVBitRate");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_Video_BitRate));
        Serializer.endTag("", "MFVBitRate");
        //.
        Serializer.endTag("", "Video");
        //. 
        Serializer.startTag("", "MFMaxDuration");
        Serializer.text(Integer.toString(MapPOIConfiguration.MediaFragment_MaxDuration));
        Serializer.endTag("", "MFMaxDuration");
        //. 
        Serializer.startTag("", "MFFormat");
        Serializer.text(MapPOIConfiguration.MediaFragment_Format);
        Serializer.endTag("", "MFFormat");
        //.
        Serializer.endTag("", "MediaFragment");
        //.
        Serializer.endTag("", "MapPOI");
        //.
        ///? save GPSLastFix.Assign(CurrentFix);
        //. 
        Serializer.endTag("", "GPSModule");
    }
    
    public synchronized void Connect() throws IOException,Exception 
    {
    	if (flConnected)
    		return; //. ->
    	//.
    	CurrentFixIsOverThresholdWithGoodAccuracy_flLastFixSpeedIsZero = false;
    	//.
    	MyLocationListener.Start();
		//.
		flConnected = true;
    }
    
    public synchronized void Disconnect() 
    {
    	if (!flConnected)
    		return; //. ->
    	//.
    	MyLocationListener.Stop();
    	//.
		flConnected = false;
    }
    
    public void Process() {
    	if (flImpulseMode) 
    		LocationMonitor.Process();
    }
    
    public void ProcessImmediately() {
    	if (flImpulseMode) 
    		LocationMonitor.Process();
    }
    
    private synchronized void SetProcessException(Exception E)
    {
        ProcessException = E;
    }
    
    public synchronized Exception GetProcessException()
    {
        if (ProcessException == null)
            return null; //. ->
        Exception R = ProcessException;
        ProcessException = null;
        return R;
    }
    
    public double GetDistance(double StartLat, double StartLong,  double EndLat, double EndLong)
    /* COPYRIGHT DelphiWorld */
    {
        double fPhimean;
        double fdLambda;
        double fdPhi; 
        double fAlpha;
        double fRho;
        double fNu;
        double fR;
        double fz;
        double fTemp;
        double Distance;
        //. consts
        double D2R = 0.017453;
        double a = 6378137.0;
        double e2 = 0.006739496742337; 
        if (Double.isNaN(StartLat) || Double.isNaN(StartLong) || Double.isNaN(EndLat) || Double.isNaN(EndLong))
                return Double.NaN; //. ->
        
        fdLambda = (StartLong - EndLong) * D2R;
        fdPhi = (StartLat - EndLat) * D2R;
        fPhimean = ((StartLat + EndLat) / 2.0) * D2R;
        fTemp = 1 - e2 * (Math.pow(Math.sin(fPhimean), 2));
        fRho = (a * (1 - e2)) / Math.pow(fTemp, 1.5);
        fNu = a / (Math.sqrt(1 - e2 * (Math.sin(fPhimean) * Math.sin(fPhimean))));
        fz = Math.sqrt(Math.pow(Math.sin(fdPhi / 2.0), 2) + Math.cos(EndLat * D2R) * Math.cos(StartLat * D2R) * Math.pow(Math.sin(fdLambda / 2.0), 2));
        fz = 2 * Math.asin(fz);
        fAlpha = Math.cos(EndLat * D2R) * Math.sin(fdLambda) * 1 / Math.sin(fz);
        if (fAlpha > 1.0) //. workaround on a case like fAlpha = 1.0000000002
        	fAlpha = 1.0;
        else
            if (fAlpha < -1.0)
            	fAlpha = -1.0;
        fAlpha = Math.asin(fAlpha);
        fR = (fRho * fNu) / ((fRho * Math.pow(Math.sin(fAlpha), 2)) + (fNu * Math.pow(Math.cos(fAlpha), 2)));
        Distance = (fz * fR);
        //.
        return Distance;
    }
    
    public synchronized void AssignCurrentFix(TGPSFixValue Fix) {
    	if (Fix.TimeStamp > CurrentFix.TimeStamp)
    		CurrentFix.Assign(Fix);
    }
    
    public synchronized TGPSFixValue GetCurrentFix() {
    	return (TGPSFixValue)CurrentFix.getValue();
    }

    public TGPSFixValue ObtainCurrentFix(TCanceller Canceller, TProgressor Progressor, boolean flRaiseExceptionOnTimeout) throws Exception {
		if (MyLocationListener.GetProviderStatus() == LocationProvider.OUT_OF_SERVICE)
			throw MyLocationListener.new LocationProviderIsDisabledException(); //. => 
		//.
		if (Progressor != null)
			Progressor.DoOnProgress(0);
    	//. waiting for fix
    	if (LocationMonitor != null) {
    		int FixIndex = MyLocationListener.GetFixCount();
    		//. start obtaining fix immediately if needed
    		LocationMonitor.Process();
    		//.
    		int MaxTime = MaxLocationObtainingTime;
    		long LastTimeTicks = System.currentTimeMillis();
    		while (MyLocationListener.GetFixCount() == FixIndex) {
    			try {
					Thread.sleep(100);
				} catch (InterruptedException E) {
					throw E; //. =>
				}
				if (MyLocationListener.GetProviderStatus() == LocationProvider.OUT_OF_SERVICE)
					throw MyLocationListener.new LocationProviderIsDisabledException(); //. => 
				long Time = (System.currentTimeMillis()-LastTimeTicks); 
    			if (Time > MaxTime)
    				if (flRaiseExceptionOnTimeout)
    					throw new FixTimeoutException(Device.context.getString(R.string.STimeoutIsExpired)); //. =>
    				else
    					break; //. >
    			//.
				if (Canceller != null)
					Canceller.Check();
    			//.
    			if (Progressor != null)
    				Progressor.DoOnProgress((int)(100.0*Time/MaxTime));
    		}
    	}
		//.
		if (Progressor != null)
			Progressor.DoOnProgress(100);
    	//.
		TGPSFixValue Result = new TGPSFixValue();
		MyLocationListener.GetCurrentFix(Result);
		AssignCurrentFix(Result);
    	return Result;
    }

    @SuppressWarnings("unused")
	private synchronized void SetCurrentFixPrecision(double Precision)
    {
        CurrentFix.setPrecision(Precision);
    }

    public synchronized TGPSFixValue TakeFixPoint()
    {
        TGPSFixValue R = (TGPSFixValue)CurrentFix.getValue();
        LastFix.setValue(CurrentFix);
        return R;
    }
    
    private synchronized boolean CurrentFixIsAcceptableByTime()
    {
        if (!CurrentFix.IsAvailable() || !LastFix.IsAvailable())
            return false; //. ->
        return ((int)((CurrentFix.TimeStamp-LastFix.TimeStamp)*24*3600) >= MinTimeBetweenFixSignals);
    }
    
    @SuppressWarnings("unused")
	private double AjustThresholdByFixSpeed(double ThresholdValue, double FixSpeed)
    {
        double Result = ThresholdValue;
        if ((FixSpeed >= 2.0) && (FixSpeed <= 5.0))
            Result = 7;
        else
        if ((FixSpeed > 5.0) && (FixSpeed <= 30.0))
            Result = 15;
        else
        if ((FixSpeed > 30.0) && (FixSpeed <= 70.0))
            Result = 70;
        else
        if ((FixSpeed > 70.0) && (FixSpeed <= 150.0))
            Result = 100;
        else
        if ((FixSpeed > 150.0) && (FixSpeed <= 300.0))
            Result = 170;            
        //.
        if (Result < ThresholdValue)
            Result = ThresholdValue;
        return Result;
    }
    
    private boolean CurrentFixIsOverThresholdWithGoodAccuracy_flLastFixSpeedIsZero = false;
    
    private synchronized boolean CurrentFixIsOverThresholdWithGoodAccuracy()
    {
        if (!LastFix.IsAvailable() || !CurrentFix.IsAvailable())
            return false; //. ->
        double ThresholdValue = Threshold.GetValue();
        if (ThresholdValue == 0.0) //. special case
        	return true; //. ->
        //.
        boolean flSpeedIsZero = (CurrentFix.Speed < 0.001);  
        if (CurrentFixIsOverThresholdWithGoodAccuracy_flLastFixSpeedIsZero && flSpeedIsZero) 
            return false; //. ->
        CurrentFixIsOverThresholdWithGoodAccuracy_flLastFixSpeedIsZero = flSpeedIsZero;
        //.
        double Distance = GetDistance(LastFix.Latitude,LastFix.Longitude, CurrentFix.Latitude,CurrentFix.Longitude); 
        if (Distance < LastFix.Precision)
            return false; //. ->
        if (Distance < CurrentFix.Precision)
            return false; //. ->
        ///* to-do more effective ThresholdValue = AjustThresholdByFixSpeed(ThresholdValue,CurrentFix.Speed);
        if (ThresholdValue < CurrentFix.Precision)
            return false; //. ->
        return (Distance >= ThresholdValue);
    }
    
    public void run() 
    {
        TGPSFixValue _Fix = new TGPSFixValue();
        //. notify that fix is unavailable
        /*///- TGPSFixValue UnavailableFix = new TGPSFixValue();
        UnavailableFix.Precision = TGPSFixValue.UnavailableFixPrecision;
        DoOnFixIsArrived(UnavailableFix);*/
        //.
        LastFix.SetFixAsUnknown();
        while (!flTerminated)
        {
            SetProcessException(null);
            try
            {
                ///- now in main thread Connect();
                try
                {
                    flGPSFixing = false;
                    flProcessing = true;
                    try
                    {
                    	CurrentFix.ClearSetFlag();
                        while (!flTerminated) 
                        {
                        	if (MyLocationListener.FetchCurrentFix(_Fix)) {
                        		AssignCurrentFix(_Fix);
                        		if (_Fix.IsAvailable()) //. is fix available
                        		{
                        			flGPSFixing = true;
                        			//.
                    				if (LastFix.IsAvailable())
                    				{
                    					if (CurrentFixIsAcceptableByTime() && CurrentFixIsOverThresholdWithGoodAccuracy())
                    						DoOnFixIsArrived(TakeFixPoint());
                    				}
                    				else
                    					DoOnFixIsArrived(TakeFixPoint());
                        		}
                        		else
                        		{
                        			flGPSFixing = false;
                        			//.
                        			if (LastFix.IsAvailable() || !LastFix.IsSet() || LastFix.IsUnknown())
                        				DoOnFixIsArrived(TakeFixPoint());
                        		}
                        		//. process for destination channel if it is active
                        		if (DestinationChannel_Active())
                        			DestinationChannel_ProcessForFix(_Fix);
                        	}
                        	else {
                				synchronized (MyLocationListener) {
                					if (MyLocationListener.ProviderStatus == LocationProvider.OUT_OF_SERVICE)
                						throw MyLocationListener.new LocationProviderIsDisabledException(); //. => 
                				}
                				//. wait for a next fix ...
            					MyLocationListener._CurrentFixSignal.WaitOne(WaitForFixInterval);
                        	}
                        }
                    }
                    finally
                    {
                        flProcessing = false;
                        flGPSFixing = false;
                        //. notify that fix becomes unavailable
                        if (CurrentFix.IsAvailable())
                        {
                            DoOnFixIsArrived(TakeFixPoint());
                            //.
                            TGPSFixValue UnavailableFix = (TGPSFixValue)CurrentFix.getValue();
                            UnavailableFix.SetFixAsUnAvailable(OleDate.UTCCurrentTimestamp(),CurrentFix.TimeStamp);
                            DoOnFixIsArrived(UnavailableFix);
                        }
                    }   
                }
                finally
                {
                	///- now in main thread Disconnect();
                }
            }
            catch (InterruptedException E) {
            	return; //. ->
            }
            catch (Throwable E)
            {
            	//. log errors
            	if (!(E instanceof TMyLocationListener.LocationProviderIsDisabledException))
            		Device.Log.WriteError("GPSModule",E.getMessage());
            	if (!(E instanceof Exception))
            		TGeoLogApplication.Log_WriteCriticalError(E);
            	//.
            	SetProcessException(new Exception(E.getMessage()));
            	//.
                try {
                	Thread.sleep(5000); //. time to wait and take ProcessException
                }
                catch (InterruptedException E1) {
                	return; //. ->
                }
            }
        }
    }
    
    public void Terminate() 
    {
        if (m_thread == null)
            return; //. ->
        flTerminated = true;
        try
        {
            m_thread.interrupt();
            m_thread.join();
            m_thread = null;
        }
        catch (InterruptedException E) {}
    }
    
    public int GetMode() {
    	return Mode.GetValue();
    }
    
    public void SetMode(short pMode) 
    {
    	if (!(Device.ConnectorModule.flServerConnectionEnabled || Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled))
    		return; //. ->
    	if (pMode == Mode.GetValue())
    		return; //. ->
        Mode.SetValue(OleDate.UTCCurrentTimestamp(),pMode);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetGPSModuleModeSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetGPSModuleModeSO)SO).setValue(Mode);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
		//. process for destination channel if it is active
        try
        {
            if (DestinationChannel_Active())
            	DestinationChannel_ProcessForMode(pMode);
        }
        catch (Exception E) {}
    }
    
    public int GetStatus() {
    	return Status.GetValue();
    }
    
    public void SetStatus(short pStatus) 
    {
    	if ((Device.ConnectorModule == null) || (!(Device.ConnectorModule.flServerConnectionEnabled || Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled)))
    		return; //. ->
    	if (pStatus == Status.GetValue())
    		return; //. ->
        Status.SetValue(OleDate.UTCCurrentTimestamp(),pStatus);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetGPSModuleStatusSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetGPSModuleStatusSO)SO).setValue(Status);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
		//. process for destination channel if it is active
        try
        {
            if (DestinationChannel_Active())
            	DestinationChannel_ProcessForStatus(pStatus);
        }
        catch (Exception E) {}
    }
    
    public void DoOnFixIsArrived(TGPSFixValue fix)
    {
    	if (!(Device.ConnectorModule.flServerConnectionEnabled || Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled))
    		return; //. ->
        TObjectSetGPSFixSO SO = new TObjectSetGPSFixSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        SO.setValue(fix);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        }
        catch (Exception E) {}
    }
    
    public void DestinationChannel_Set(final TGPSChannel pDestinationChannel) {
    	synchronized (this) {
        	if (DestinationChannel != null)
        		return; //. ->
        	//.
        	DestinationChannel = pDestinationChannel;
        	//.
        	DestinationChannel_GPSMode = new TTimestampedInt16ContainerType.TValue();
        	DestinationChannel_GPSStatus = new TTimestampedInt16ContainerType.TValue();
        	DestinationChannel_GPSFix = new TTimestampedInt166DoubleContainerType.TValue();
    	}
    	//.
    	pDestinationChannel.DestinationChannel_PacketSubscribersItemsNotifier_Set(new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscribers.TItemsNotifier() {
			
    		private TGPSChannel GPSChannel = pDestinationChannel;
    			
    		@Override
			protected void DoOnSubscribed(final com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber Subscriber) {
				try {
					final TTimestampedInt16ContainerType.TValue GPSMode = new TTimestampedInt16ContainerType.TValue();
					final TTimestampedInt16ContainerType.TValue GPSStatus = new TTimestampedInt16ContainerType.TValue();
					//.
	    			GPSMode.Timestamp = OleDate.UTCCurrentTimestamp();
	    			GPSMode.Value = Mode.GetValue();
	    			//.
	    			GPSStatus.Timestamp = OleDate.UTCCurrentTimestamp();
	    			GPSStatus.Value = Status.GetValue();
	    			//.
	    			Thread Processing = new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
				    			synchronized (GPSChannel) {
				        			GPSChannel.GPSMode.SetContainerTypeValue(GPSMode);
				        			GPSChannel.DoOnData(GPSChannel.GPSMode, Subscriber);
				        			//.
				        			GPSChannel.GPSStatus.SetContainerTypeValue(GPSStatus);
				        			GPSChannel.DoOnData(GPSChannel.GPSStatus, Subscriber);
								}
							}
							catch (Throwable T) {
							}								
						}
					});
					Processing.start();
				} catch (Exception E) {
				}
			};
		});
    	//.
    	ProcessImmediately();
    }
    
    public synchronized void DestinationChannel_Clear() {
    	DestinationChannel = null;
    	//.
    	Process();
    }
    
    public synchronized TGPSChannel DestinationChannel_Get() {
    	return DestinationChannel;
    }
    
    public synchronized boolean DestinationChannel_Active() {
    	return ((DestinationChannel != null) && DestinationChannel.DestinationChannel_IsConnected());
    }
    
    public void DestinationChannel_ProcessForMode(short pMode) throws Exception {
    	TGPSChannel GPSChannel = null;
    	synchronized (this) {
        	if (DestinationChannel_Active()) 
        		GPSChannel = DestinationChannel_Get();
    	}
    	//.
		if (GPSChannel != null) {
			DestinationChannel_GPSMode.Timestamp = OleDate.UTCCurrentTimestamp();
			DestinationChannel_GPSMode.Value = pMode;
			//.
			synchronized (GPSChannel) {
    			GPSChannel.GPSMode.SetContainerTypeValue(DestinationChannel_GPSMode);
    			GPSChannel.DoOnData(GPSChannel.GPSMode);
			}
		}
    }
    
    public void DestinationChannel_ProcessForStatus(short pStatus) throws Exception {
    	TGPSChannel GPSChannel = null;
    	synchronized (this) {
        	if (DestinationChannel_Active()) 
        		GPSChannel = DestinationChannel_Get();
    	}
    	//.
		if (GPSChannel != null) {
			DestinationChannel_GPSStatus.Timestamp = OleDate.UTCCurrentTimestamp();
			DestinationChannel_GPSStatus.Value = pStatus;
			//.
			synchronized (GPSChannel) {
    			GPSChannel.GPSStatus.SetContainerTypeValue(DestinationChannel_GPSStatus);
    			GPSChannel.DoOnData(GPSChannel.GPSStatus);
			}
		}
    }
    
    public void DestinationChannel_ProcessForFix(TGPSFixValue pGPSFix) throws Exception {
    	TGPSChannel GPSChannel = null;
    	synchronized (this) {
        	if (DestinationChannel_Active()) 
        		GPSChannel = DestinationChannel_Get();
    	}
    	//.
		if (GPSChannel != null) {
			DestinationChannel_GPSFix.Timestamp = pGPSFix.TimeStamp;
			DestinationChannel_GPSFix.Value = DatumID;
			DestinationChannel_GPSFix.Value1 = pGPSFix.Latitude;
			DestinationChannel_GPSFix.Value2 = pGPSFix.Longitude;
			DestinationChannel_GPSFix.Value3 = pGPSFix.Altitude;
			DestinationChannel_GPSFix.Value4 = pGPSFix.Speed;
			DestinationChannel_GPSFix.Value5 = pGPSFix.Bearing;
			DestinationChannel_GPSFix.Value6 = pGPSFix.Precision;
			//.
			synchronized (GPSChannel) {
    			GPSChannel.GPSFix.SetContainerTypeValue(DestinationChannel_GPSFix);
    			GPSChannel.DoOnData(GPSChannel.GPSFix);
			}
		}
    }
}
