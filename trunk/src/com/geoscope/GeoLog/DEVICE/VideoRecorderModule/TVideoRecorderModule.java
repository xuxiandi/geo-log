package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerVideoPhoneServer;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TLoadVideoRecorderConfigurationSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderActiveFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderAudioFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderRecordingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderSavingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderTransmittingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderVideoFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
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
            	TSensorsModuleMeasurements.ValidateMeasurements();
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
                	break; //. >

                case MESSAGE_OPERATION_ERROR: 
                	Exception E = (Exception)msg.obj;
                	//.
            		Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleSettingConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
                	break; //. >
                	
                case MESSAGE_SETSAVINGSERVER_OPERATION_COMPLETED:
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
        						double NowTime = OleDate.UTCCurrentTimestamp();
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
			if (MeasurementsTransferProcess != null)
				MeasurementsTransferProcess.StartProcess();
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
    
	public String Measurements_GetList() {
		return TSensorsModuleMeasurements.GetMeasurementsList((short)1);
	}
	
	public String Measurements_GetList(double BeginTimestamp, double EndTimestamp) {
		return TSensorsModuleMeasurements.GetMeasurementsList(BeginTimestamp,EndTimestamp, (short)1);
	}
	
	public void Measurements_Delete(String MeasurementID) throws IOException {
		TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
	}
	
	public long Measurement_GetSize(String MeasurementID) throws IOException {
		return TSensorsModuleMeasurements.GetMeasurementSize(MeasurementID);
	}
	
	public byte[] Measurement_GetData(String MeasurementID) throws IOException {
		return TSensorsModuleMeasurements.GetMeasurementData(MeasurementID);
	}
	
	/* public byte[] Measurement_GetDataFragment(String MeasurementID, TMeasurementDescriptor CurrentMeasurement, double StartTimestamp, double FinishTimestamp, boolean flDescriptor, boolean flAudio, boolean flVideo) throws Exception {
		return TVideoRecorderMeasurements.GetMeasurementDataFragment(MeasurementID, CurrentMeasurement, StartTimestamp,FinishTimestamp, flDescriptor,flAudio,flVideo);
	} */
}
