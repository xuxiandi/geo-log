package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Color;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000.TEnforaMT3000ObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaObject.TEnforaObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject.TGeoMonitoredObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentElement;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;
import com.jcraft.jzlib.ZInputStream;


public class TObjectModel {

	public static TObjectModel GetObjectModel(int pID) throws Exception {
		switch (pID)
		{
		case TGeoMonitoredObjectModel.ID: 
			return (new TGeoMonitoredObjectModel());
		
			case TGeoMonitoredObject1Model.ID: 
				return (new TGeoMonitoredObject1Model());
			
			case TEnforaObjectModel.ID: 
				return (new TEnforaObjectModel());
				
			case TEnforaMT3000ObjectModel.ID: 
				return (new TEnforaMT3000ObjectModel());
				
			default:
				return null; //. ->
		}
	}
	
	public static TObjectModel GetObjectModel(int pID, TGeographServerObjectController pObjectController, boolean pflFreeObjectController) throws Exception {
		TObjectModel Result = GetObjectModel(pID);
		if (Result != null) 
			Result.SetObjectController(pObjectController,pflFreeObjectController);	
		return Result;
	}
	
	public static class THistoryRecord {
		
		public static final int SEVERITY_INFO 		= 0;
		public static final int SEVERITY_MINOR 		= 1;
		public static final int SEVERITY_MAJOR 		= 2;
		public static final int SEVERITY_CRITICAL 	= 3;
		//.
		public static String 	SEVERITY_ToString(int Value) {
			switch (Value) {
			
			case SEVERITY_INFO:
				return "info"; //. ->

			case SEVERITY_MINOR:
				return "minor"; //. ->

			case SEVERITY_MAJOR:
				return "major"; //. ->
				
			case SEVERITY_CRITICAL:
				return "critical"; //. ->
				
			default:
				return "?"; //. ->
			}
		}
		//.
		public static int 		SEVERITY_ToColor(int Value) {
			switch (Value) {
			
			case SEVERITY_INFO:
				return Color.TRANSPARENT; //. ->

			case SEVERITY_MINOR:
				return Color.YELLOW; //. ->

			case SEVERITY_MAJOR:
				return Color.RED; //. ->
				
			case SEVERITY_CRITICAL:
				return Color.MAGENTA; //. ->
				
			default:
				return Color.TRANSPARENT; //. ->
			}
		}
		
		
		public double 	Timestamp;
		public long		UserID;
		public int 		Severity;
		//.
		public TXYCoord XY = null;
		
		public THistoryRecord(double pTimestamp, long pUserID, int pSeverity) {
			Timestamp = pTimestamp;
			UserID = pUserID;
			Severity = pSeverity;
		}
		
		public THistoryRecord(double pTimestamp, long pUserID) {
			this(pTimestamp,pUserID, SEVERITY_INFO);
		}
		
		public String GetString(int Level) {
			return null;
		}
		
		public int GetSeverityColor() {
			return SEVERITY_ToColor(Severity);
		}
		
		public String GetSeverityString() {
			return SEVERITY_ToString(Severity);
		}
	}
	
	public static class TEventRecord extends THistoryRecord {
		
		public int 		Tag;
		public String 	Message;
		public String 	Info;
		public byte[] 	Extra;
		
		public TEventRecord(double pTimestamp, long pUserID, int pSeverity, int pTag, String pMessage, String pInfo, byte[] pExtra) {
			super(pTimestamp,pUserID, pSeverity);
			Tag = pTag;
			Message = pMessage;
			Info = pInfo;
			Extra = pExtra;
		}

		public TEventRecord(double pTimestamp, long pUserID, int pSeverity, String pMessage) {
			this(pTimestamp, pUserID, pSeverity, 0, pMessage, null, null);
		}
		
	    @Override
		public String GetString(int Level) {
	    	switch (Level) {
	    	
	    	case 1: 
	    		return Message; //. ->
	    		
	    	case 2:
		    	StringBuilder SB = new StringBuilder()
		    	.append("Timestamp: "+(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Timestamp)).GetDateTime())+", ")
		    	.append("Severity: "+SEVERITY_ToString(Severity)+", ")
		    	.append("Message: "+Message+", ")
		    	.append("Info: "+Info);
				return SB.toString();
				
			default:
				return ""; //. ->
	    	}
		}
	}
	
	public static class TGeoLocationRecord extends THistoryRecord {
		
	    public static final double 	UnavailableFixPrecision = 1000000000.0;
	    public static final double 	UnknownFixPrecision = -1000000000.0;
	    
	    public static class TSpeedColorer {
	    	
	    	public static final double 	Interval0_SpeedLimit = 5.0; //. Km/h
	    	public static final int		Interval0_LowSpeedColor = 0xFF000100;
	    	public static final int		Interval0_HighSpeedColor = Color.GREEN;
	    	//.
	    	public static final double 	Interval1_SpeedLimit = 60.0; //. Km/h
	    	public static final int		Interval1_LowSpeedColor = Color.BLUE;
	    	public static final int		Interval1_HighSpeedColor = Color.RED;
	    	  
	    	  
	    	private int InterpolateColor(int C1, int C2, float proportion) {
	    	    float[] hsva = new float[3];
	    	    float[] hsvb = new float[3];
	    	    Color.colorToHSV(C1, hsva);
	    	    Color.colorToHSV(C2, hsvb);
	    	    for (int i = 0; i < 3; i++) 
	    	    	hsvb[i] = (hsva[i]+((hsvb[i]-hsva[i])*proportion));
	    	    return Color.HSVToColor(hsvb);
	    	}

	    	public int GetColor(double Speed) {
		    	if (Speed < Interval0_SpeedLimit) 
		    		return InterpolateColor(Interval0_LowSpeedColor,Interval0_HighSpeedColor, (float)(Speed/Interval0_SpeedLimit)); //. ->
		    	//.
		    	if (Speed > Interval1_SpeedLimit) 
		    		Speed = Interval1_SpeedLimit;
			    return InterpolateColor(Interval1_LowSpeedColor,Interval1_HighSpeedColor, (float)((Speed-Interval0_SpeedLimit)/(Interval1_SpeedLimit-Interval0_SpeedLimit)));
	    	}
	    }
	    
	    public static TSpeedColorer SpeedColorer = new TSpeedColorer();
	    //.
	    public double Latitude;
	    public double Longitude;
	    public double Altitude;
	    public double Speed;
	    public double Bearing;
	    public double Precision;
	    
	    public TGeoLocationRecord(double pTimestamp, long pUserID, double pLatitude, double pLongitude, double pAltitude, double pSpeed, double pBearing, double pPrecision) {
			super(pTimestamp,pUserID);
	    	Latitude = pLatitude;
	    	Longitude = pLongitude;
	    	Altitude = pAltitude;
	    	Speed = pSpeed;
	    	Bearing = pBearing;
	    	Precision = pPrecision;
	    }

	    @Override
		public String GetString(int Level) {
	    	StringBuilder SB = new StringBuilder()
	    	.append("Timestamp: "+(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(Timestamp)).GetDateTime())+", ")
	    	.append("Latitude: "+Double.toString(Latitude)+", ")
	    	.append("Longitude: "+Double.toString(Longitude)+", ")
	    	.append("Altitude: "+Double.toString(Altitude)+", ")
	    	.append("Speed: "+Double.toString(Speed)+", ")
	    	.append("Bearing: "+Double.toString(Bearing)+", ")
	    	.append("Precision: "+Double.toString(Precision));
			return SB.toString();
		}

	    public boolean IsAvailable() {
	        return ((Precision != UnknownFixPrecision) && (Precision != UnavailableFixPrecision));
	    }
	}
	

	public TGeographServerObjectController 	ObjectController = null;
	protected boolean 						flFreeObjectController = false;
	//.
	public TComponentSchema ObjectSchema = null;
	public TComponentSchema ObjectDeviceSchema = null;
	//.
	public TObjectBusinessModel BusinessModel = null;
	
	public TObjectModel() throws Exception {
		BusinessModel = null;
		//.
		CreateSchemas();
	}
	
	public TObjectModel(TGeographServerObjectController pObjectController, boolean pflFreeObjectController) throws Exception {
		this();
		SetObjectController(pObjectController,pflFreeObjectController);	
		//.
		CreateSchemas();
	}
	
	public TObjectModel(TGeographServerObjectController pObjectController) throws Exception {
		this(pObjectController, false);
	}
	
	protected void CreateSchemas() throws Exception {
	}
	
	public void Destroy() {
		if (BusinessModel != null)
		{
			BusinessModel.Destroy();
			BusinessModel = null;
		};
		if (ObjectDeviceSchema != null)
		{
			ObjectDeviceSchema.Destroy();
			ObjectDeviceSchema = null;
		};
		if (ObjectSchema != null)
		{
			ObjectSchema.Destroy();
			ObjectSchema = null;
		};
		if (flFreeObjectController && (ObjectController != null)) {
			try {
				ObjectController.Destroy();
			} catch (IOException E) {}
			ObjectController = null;
		}
	}

	public int GetID() {
		return 0;
	}
	
	public String GetName() {
		return "";
	}
	
	public boolean SetBusinessModel(int BusinessModelID) {
		if (BusinessModel != null)
		{
			BusinessModel.Destroy();
			BusinessModel = null;
		};
		return false;
	}	
	
	public void SetObjectController(TGeographServerObjectController pObjectController, boolean pflFreeObjectController) {
		ObjectController = pObjectController;
		flFreeObjectController = pflFreeObjectController;
	}
	
	public void SetObjectController(TGeographServerObjectController pObjectController) {
		SetObjectController(pObjectController, false);
	}
	
	public long ObjectUserID() {
		return 0;
	}
	
    public int ObjectDatumID() {
    	return 0;
    }
    
    public static class TObjectHistoryRecords {
    
    	public ArrayList<THistoryRecord> ObjectModelRecords;
    	public ArrayList<THistoryRecord> BusinessModelRecords;
    	
    	public TObjectHistoryRecords(ArrayList<THistoryRecord> pObjectModelRecords, ArrayList<THistoryRecord> pBusinessModelRecords) {
    		ObjectModelRecords = pObjectModelRecords;
    		BusinessModelRecords = pBusinessModelRecords;
    	}
    	
    	public int BusinessModelRecords_GetNearestItemToTimestamp(double Timestamp) {
    		int Result = -1;
    		int Cnt = BusinessModelRecords.size();
    		double MinDistance = Double.MAX_VALUE;
    		for (int I = 0; I < Cnt; I++) {
    			double Distance = Math.abs(BusinessModelRecords.get(I).Timestamp-Timestamp);
    			if (Distance < MinDistance) {
    				MinDistance = Distance;
    				Result = I;
    			}
    		}
    		return Result;
    	}
    }
    
	public TObjectHistoryRecords History_GetRecords(double DayDate, short DaysCount, Context context) throws Exception {
		ArrayList<THistoryRecord> ObjectModelRecords = new ArrayList<THistoryRecord>(1024);
		ArrayList<THistoryRecord> BusinessModelRecords = new ArrayList<THistoryRecord>(1024);
		//.
		byte[] HistoryData = ObjectController.ObjectOperation_GetDaysLogData(DayDate,DaysCount, (short)1/*ZLIB zipped XML format*/);
		int Idx = 0;
		for (int I = 0; I < DaysCount; I++) {
			int DayDataSize = TDataConverter.ConvertLEByteArrayToInt32(HistoryData, Idx); Idx += 4; //. SizeOf(DayDataSize)
			if (DayDataSize <= 0)
				continue; //. ^
			byte[] DayData;
			ByteArrayInputStream BIS = new ByteArrayInputStream(HistoryData, Idx,DayDataSize);
			try {
				ZInputStream ZIS = new ZInputStream(BIS);
				try {
					byte[] Buffer = new byte[8192];
					int ReadSize;
					ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
					try {
						while ((ReadSize = ZIS.read(Buffer)) > 0) 
							BOS.write(Buffer, 0,ReadSize);
						//.
						DayData = BOS.toByteArray();
					}
					finally {
						BOS.close();
					}
				}
				finally {
					ZIS.close();
				}
			}
			finally {
				BIS.close();
			}
			Idx += DayDataSize;
			//.
	    	Document XmlDoc;
			BIS = new ByteArrayInputStream(DayData);
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
			
			case 0:
				try {
					NodeList RecordsNode = TMyXML.SearchNode(RootNode,"OperationsLog").getChildNodes();
					//.
					TIndex AddressIndex = new TIndex();
					int Cnt = RecordsNode.getLength();
					for (int J = 0; J < Cnt; J++) {
						Node RecordNode = RecordsNode.item(J);
						//.
						if (RecordNode.getLocalName() != null) {
							String NodeName = RecordNode.getLocalName();
							if (NodeName.equals("SET") || NodeName.equals("GET")) {
								boolean flSetOperation = NodeName.equals("SET"); 
								int[] ElementAddress = TComponent.GetAddressFromString(TMyXML.SearchNode(RecordNode,"Component").getFirstChild().getNodeValue());
								AddressIndex.Reset();
								TComponentElement ObjectModelElement = null;
								if (ElementAddress[0] == 1) 
									ObjectModelElement = ObjectSchema.RootComponent.GetComponentElement(ElementAddress, AddressIndex);
								else {
									if (ElementAddress[0] == 2) 
										ObjectModelElement = ObjectDeviceSchema.RootComponent.GetComponentElement(ElementAddress, AddressIndex);
								}
								double Timestamp = 0.0;
								long UserID = 0;
								if (ObjectModelElement != null) {
									try {
										Timestamp = Double.parseDouble(TMyXML.SearchNode(RecordNode,"Time").getFirstChild().getNodeValue());
										Node UserNode = TMyXML.SearchNode(RecordNode,"User");
										if (UserNode != null)
											UserID = Long.parseLong(UserNode.getFirstChild().getNodeValue());
									}
									catch (NumberFormatException NFE) {
									}
									//.
									try {
										ObjectModelElement.FromXMLNodeByAddress(ElementAddress,AddressIndex, RecordNode);
										//.
										THistoryRecord Record = ObjectModelElement.ToHistoryRecord(Timestamp, UserID, flSetOperation, context);
										if (Record != null)
											ObjectModelRecords.add(Record);
										//.
										if (BusinessModel != null) {
											Record = BusinessModel.GetBusinessHistoryRecord(ObjectModelElement, Timestamp, UserID, flSetOperation, context);
											if (Record != null)
												BusinessModelRecords.add(Record);
										}
									}
									catch (Exception E) {
									}
								}
							}
						}
					}
				}
				catch (Exception E) {
	    			throw new Exception("error of log data parsing: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown log data version, version: "+Integer.toString(Version)); //. =>
			}
		}
		return new TObjectHistoryRecords(ObjectModelRecords,BusinessModelRecords);
	}
	
	public void Sensors_Channel_SetProfile(int ChannelID, byte[] Profile) throws IOException, Exception {
	}
	
	public byte[] Sensors_Channel_GetProfile(int ChannelID) throws Exception {
		return null;
	}
	
	public void Sensors_Channels_SetProfile(byte[] Profile) throws IOException, Exception {
	}
	
	public byte[] Sensors_Channels_GetProfile() throws Exception {
		return null;
	}
	
	public static class TSensorChannelStatus {
		
		public boolean Enabled;
		public boolean Active;
		
		public int FromByteArray(byte[] BA, int Idx) {
			Enabled = (BA[Idx] != 0); Idx++;
			Active = (BA[Idx] != 0); Idx++;
			//.
			return Idx;
		}
	}
	
	public TSensorChannelStatus[] Sensors_Channels_GetStatus(int[] ChannelIDs) throws Exception {
		return null;
	}
	
	public void Sensors_Meter_SetProfile(String MeterID, byte[] Profile) throws IOException, Exception {
	}
	
	public byte[] Sensors_Meter_GetProfile(String MeterID) throws Exception {
		return null;
	}
	
	public TChannelIDs Sensors_Meter_GetChannels(String MeterID) throws Exception {
		return null;
	}
	
	public TSensorMeterInfo[] Sensors_Meters_GetList() throws Exception {
		return null;
	}
	
	public void Sensors_Meters_SetActive(String MeterIDs, boolean flActive) throws IOException, Exception {
	}
	
	public void Sensors_Meters_ValidateActivity(String MeterIDs) throws IOException, Exception {
	}
	
	public TSensorMeasurementDescriptor[] Sensors_Measurements_GetList(double BeginTimestamp, double EndTimestamp, String GeographDataServerAddress, int GeographDataServerPort, Context context, TCanceller Canceller) throws Exception {
		return null;
	}
	
	public boolean UserMessaging_IsSupported() {
		return false;
	}

	public void UserMessaging_Start(TCoGeoMonitorObject Object, Context context) throws Exception {
	}

	public void UserMessaging_Stop() throws Exception {
	}

	public boolean UserVideoPhone_IsSupported() {
		return false;
	}

	public void UserVideoPhone_Start(TCoGeoMonitorObject Object, Context context) throws Exception {
	}

	public void UserVideoPhone_Stop() throws Exception {
	}
}
