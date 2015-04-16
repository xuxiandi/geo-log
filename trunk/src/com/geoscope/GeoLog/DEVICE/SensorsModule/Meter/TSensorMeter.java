package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserDataFile;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.TMeasurement;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;

public class TSensorMeter extends TCancelableThread {

	public static final int STATUS_ERROR 		= -1;
	public static final int STATUS_NOTRUNNING 	= 0;
	public static final int STATUS_RUNNING 		= 1;
	//.
	public static String STATUS_GetString(int Status, Context context) {
		switch (Status) {
		
		case STATUS_ERROR:
			return context.getString(R.string.SError1); //. ->

		case STATUS_NOTRUNNING:
			return context.getString(R.string.SNotRunning); //. ->
			
		case STATUS_RUNNING:
			return context.getString(R.string.SRunning); //. ->
			
		default:
			return "?"; //. ->
		}
	}
	
	public static class TProfile {
		
		public boolean flEnabled;
		//.
		public boolean flActive;
		//.
		public volatile double MeasurementMaxDuration;
		public volatile double MeasurementLifeTime;
		public volatile double MeasurementAutosaveInterval;
		//.
		public boolean flCreateDataFile;
		
		public TProfile() {
			SetDefaults();
		}
		
		private void SetDefaults() {
			flEnabled = true;
			//.
			flActive = false;
			//. Measurement 
			MeasurementMaxDuration = (1.0/(24.0*60.0))*60; //. minutes
			MeasurementLifeTime = 1.0*2; //. days
			MeasurementAutosaveInterval = -1.0;
			//.
			flCreateDataFile = false;
		}
		
		protected synchronized void FromXMLNode(Node ANode) throws Exception {
			try {
				SetDefaults();
				//.
    			Node node = TMyXML.SearchNode(ANode,"Enabled").getFirstChild();
    			if (node != null)
    				flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				//.
    			node = TMyXML.SearchNode(ANode,"Active").getFirstChild();
    			if (node != null)
    				flActive = (Integer.parseInt(node.getNodeValue()) != 0);
    			//. Measurement 
    			Node MeasurementNode = TMyXML.SearchNode(ANode,"Measurement");
    			if (MeasurementNode != null) {
	    			node = TMyXML.SearchNode(MeasurementNode,"MaxDuration").getFirstChild();
	    			if (node != null)
	    				MeasurementMaxDuration = Double.parseDouble(node.getNodeValue());
	    			node = TMyXML.SearchNode(MeasurementNode,"LifeTime").getFirstChild();
	    			if (node != null)
	    				MeasurementLifeTime = Double.parseDouble(node.getNodeValue());
	    			node = TMyXML.SearchNode(MeasurementNode,"AutosaveInterval").getFirstChild();
	    			if (node != null)
	    				MeasurementAutosaveInterval = Double.parseDouble(node.getNodeValue());
    			}
    			//.
    			node = TMyXML.SearchNode(ANode,"CreateDataFile");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null) 
        				flCreateDataFile = (Integer.parseInt(node.getNodeValue()) != 0);
    			}
			}
			catch (Exception E) {
    			throw new Exception("error of parsing the meter profile: "+E.getMessage()); //. =>
			}
		}
		
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
	        Serializer.startTag("", "Enabled");
	        String SV;
	        if (flEnabled)
	        	SV = "1";
	        else
	        	SV = "0";
	        Serializer.text(SV);
	        Serializer.endTag("", "Enabled");
	        //. 
	        Serializer.startTag("", "Active");
	        if (flActive)
	        	SV = "1";
	        else
	        	SV = "0";
	        Serializer.text(SV);
	        Serializer.endTag("", "Active");
	        //. Measurement 
	        Serializer.startTag("", "Measurement");
	        Serializer.startTag("", "MaxDuration");
	        Serializer.text(Double.toString(MeasurementMaxDuration));
	        Serializer.endTag("", "MaxDuration");
	        Serializer.startTag("", "LifeTime");
	        Serializer.text(Double.toString(MeasurementLifeTime));
	        Serializer.endTag("", "LifeTime");
	        Serializer.startTag("", "AutosaveInterval");
	        Serializer.text(Double.toString(MeasurementAutosaveInterval));
	        Serializer.endTag("", "AutosaveInterval");
	        Serializer.endTag("", "Measurement");
	        //. 
	        Serializer.startTag("", "CreateDataFile");
	        if (flCreateDataFile)
	        	SV = "1";
	        else
	        	SV = "0";
	        Serializer.text(SV);
	        Serializer.endTag("", "CreateDataFile");
		}
		
		public void FromByteArray(byte[] BA) throws Exception {
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
			FromXMLNode(RootNode);
		}
		
	    public byte[] ToByteArray() throws Exception {
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
		        //.
				int Version = 1;
		        //. Version
		        Serializer.startTag("", "Version");
		        Serializer.text(Integer.toString(Version));
		        Serializer.endTag("", "Version");
		        //. 
		        ToXMLSerializer(Serializer);
		        //.
		        Serializer.endTag("", "ROOT");
		        Serializer.endDocument();
		        //.
				return BOS.toByteArray(); //. ->
		    }
		    finally {
		    	BOS.close();
		    }
	    }
	}
	
	protected TSensorsModule SensorsModule;
	//.
	public TSensorMeterDescriptor Descriptor;
	//.
	private String 		ProfileFolder;
	private String 		ProfileFile;
	protected TProfile 	Profile;
	//.
	private int Status = STATUS_NOTRUNNING;
	//.
	private TSensorMeasurement Measurement = null;
	//.
	private Thread OnMeasurementFinishProcessing = null;
	
	public TSensorMeter(TSensorsModule pSensorsModule, TSensorMeterDescriptor pDescriptor, Class<?> ProfileClass, String pProfileFolder) throws Exception {
		super();
		//.
		SensorsModule = pSensorsModule;
		//.
		Descriptor = pDescriptor;
		//.
		Profile = (TProfile)ProfileClass.newInstance();
		ProfileFolder = pProfileFolder;
		ProfileFile = ProfileFolder+"/"+Descriptor.ID+".xml";
	}
	
	public String GetTypeID() {
		return "";
	}
	
	public void Initialize() throws Exception {
		LoadProfile();
	}
	
	public void Finalize() throws InterruptedException {
		Finish();
	}
	
	public void SetProfile(byte[] pProfile, boolean flSave) throws Exception {
		Profile.FromByteArray(pProfile);
		//.
		if (flSave)
			SaveProfile();
		//. restart
		Finish();
		if (Profile.flEnabled && Profile.flActive)
			Start();
	}
	
	public void SetProfile(byte[] pProfile) throws Exception {
		SetProfile(pProfile,true);
	}
	
	public byte[] GetProfile() throws Exception {
		return Profile.ToByteArray();
	}
	
	protected void LoadProfile() throws Exception {
		byte[] ProfileBA;
		File PF = new File(ProfileFile);
		if (!PF.exists())
			return; //. ->
		FileInputStream FIS = new FileInputStream(PF);
		try {
			ProfileBA = new byte[(int)PF.length()];
			FIS.read(ProfileBA);
		}
		finally {
			FIS.close();
		}
		//.
		SetProfile(ProfileBA,false);
	}
	
	public void SaveProfile() throws Exception {
		FileOutputStream FOS = new FileOutputStream(ProfileFile);
		try {
			FOS.write(Profile.ToByteArray());
		}
		finally {
			FOS.close();
		}
	}
	
	public void SetEnabled(boolean flEnabled) throws Exception {
		Profile.flEnabled = flEnabled;
		//.
		SaveProfile();
		//. restart
		Finish();
		if (Profile.flEnabled && Profile.flActive)
			Start();
	}
	
	public boolean IsEnabled() {
		return Profile.flEnabled;
	}
	
	public void SetActive(boolean flActive) throws Exception {
		Profile.flActive = flActive;
		//.
		SaveProfile();
		//. restart
		Finish();
		if (Profile.flEnabled && Profile.flActive)
			Start();
	}
	
	public boolean IsActive() {
		return Profile.flActive;
	}
	
	public synchronized void SetStatus(int Value) {
		Status = Value;
	}
	
	public synchronized int GetStatus() {
		return Status;
	}
	
	public void Start() {
		_Thread = new Thread(this);
		_Thread.start();
	}

	public void Finish() throws InterruptedException {
		CancelAndWait();
		//.
		Reset();
	}
	
	public void Restart() throws InterruptedException {
		Finish();
		Start();
	}

	@Override
	public void run() {
		try {
			DoOnStart();
			try {
				DoProcess();
			}
			finally {
				DoOnFinish();
			}
		}
		catch (InterruptedException IE) {
		} 
		catch (CancelException CE) {
		}
    	catch (Throwable E) {
			SetStatus(STATUS_ERROR);
			//.
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			SensorsModule.Device.Log.WriteError("Sensors meter: "+GetTypeID(),S);
    	}
	}
	
	protected void DoOnStart() throws Exception {
		SetStatus(STATUS_RUNNING);
		//.
		OnMeasurementFinishProcessing = null;
	}
	
	protected void DoOnFinish() throws Exception {
		if (OnMeasurementFinishProcessing != null) 
			OnMeasurementFinishProcessing.join();
		//.
		SetStatus(STATUS_NOTRUNNING);
	}
	
	protected void DoProcess() throws Exception {
	}
	
	protected synchronized void SetMeasurement(TSensorMeasurement Value) {
		Measurement = Value;
	}
	
	public synchronized TSensorMeasurement GetMeasurement() {
		return Measurement;
	}

	private void CreateMeasurementAsDataFile(TSensorMeasurement Measurement) throws Exception {
		String DataName = Measurement.Descriptor.TypeID()+"("+OleDate.Format("yyyy-MM-dd HH:mm:ss",OleDate.UTCToLocalTime(Measurement.Descriptor.StartTimestamp))+")"; 
		//.
    	if ((DataName != null) && (DataName.length() > 0))
    		DataName = "@"+TComponentFileStreaming.EncodeFileNameString(DataName);
    	else
    		DataName = "";
    	//.
    	byte[] Data = Measurement.ToByteArray();
    	double Timestamp = OleDate.UTCCurrentTimestamp();
		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Measurement.Descriptor.GUID+DataName+TSensorMeasurement.DataFileFormat;
		File NF = new File(NFN);
		FileOutputStream FOS = new FileOutputStream(NF);
		try {
			FOS.write(Data);
		}
		finally {
			FOS.close();
		}
		//. prepare and send data-file
    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(Timestamp,NFN);
    	DataFile.Create(SensorsModule.Device);
	}
	
	protected void DoOnMeasurementFinish(final TSensorMeasurement Measurement) throws Exception {
		if (OnMeasurementFinishProcessing != null)
			OnMeasurementFinishProcessing.join();
		OnMeasurementFinishProcessing = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if (Profile.flCreateDataFile)
						CreateMeasurementAsDataFile(Measurement);
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					SensorsModule.Device.Log.WriteError("Sensors meter: "+GetTypeID(),S);
				}
			}
		});
		OnMeasurementFinishProcessing.start();
	}
	
    public void Measurements_RemoveOld(ArrayList<String> MIDs) throws Exception {
    	double MinTimestamp = OleDate.UTCCurrentTimestamp()-Profile.MeasurementLifeTime;
    	for (int I = 0; I < MIDs.size(); I++) {
    		String MeasurementID = MIDs.get(I); 
			TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, TSensorsModuleMeasurements.DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
			if (Measurement.Descriptor.IsTypeOf(GetTypeID())) {
				if (Measurement.Descriptor.IsValid()) {
					if (Measurement.Descriptor.FinishTimestamp < MinTimestamp)
						TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
				}
				else
					if (Measurement.Descriptor.IsStarted()) {
						if (Measurement.Descriptor.StartTimestamp < MinTimestamp)
							TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
					}
			}
    	}
    }
}