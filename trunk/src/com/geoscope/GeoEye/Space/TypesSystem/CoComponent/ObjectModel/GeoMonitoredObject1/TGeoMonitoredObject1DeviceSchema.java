package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Color;
import android.util.Base64;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TEventRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TGeoLocationRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentDoubleValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt32Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleArrayValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16ArrayValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioFileMessageValue;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioFilesValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.UserAgentModule.TUserDataValue;
import com.geoscope.GeoLog.DEVICE.UserAgentModule.TUserIDValue;

public class TGeoMonitoredObject1DeviceSchema extends TComponentSchema {

	public static class TGeoMonitoredObject1DeviceComponent extends TComponent {

		public class TDeviceDescriptor extends TComponent
		{
			public TComponentInt32Value			Vendor;
			public TComponentInt32Value			Model;
			public TComponentInt32Value			SerialNumber;
			public TComponentDoubleValue		ProductionDate;
			public TComponentInt32Value			HWVersion;
			public TComponentInt32Value			SWVersion;
			
			public TDeviceDescriptor(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"DeviceDescriptor");
				//.
				Vendor			= new TComponentInt32Value		(this,1,"Vendor");
				Model			= new TComponentInt32Value		(this,2,"Model");
				SerialNumber	= new TComponentInt32Value		(this,3,"SerialNumber");
				ProductionDate	= new TComponentDoubleValue		(this,4,"ProductionDate");
				HWVersion		= new TComponentInt32Value		(this,5,"HWVersion");
				SWVersion		= new TComponentInt32Value		(this,6,"SWVersion");
			}
		}
		
		public static class TBatteryModule extends TComponent
		{
			public TComponentTimestampedInt16Value	Voltage;
			public TComponentTimestampedInt16Value	Charge;
			
			public TBatteryModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"BatteryModule");
				//.
				Voltage = new TComponentTimestampedInt16Value(this,1,"Voltage");
				Charge	= new TComponentTimestampedInt16Value(this,2,"Charge");
			}
		}
	
		public static class TConnectorModule extends TComponent
		{
			public static class TServiceProvider extends TComponent
			{
				public TComponentInt16Value				ProviderID;
				public TComponentDoubleValue			Number;
				public TComponentTimestampedInt16Value	Account;
				public TComponentTimestampedInt16Value	Signal;

				public TServiceProvider(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"ServiceProvider");
					//.
					ProviderID	= new TComponentInt16Value				(this,1,"ProviderID");			
					Number		= new TComponentDoubleValue				(this,2,"Number");				
					Account		= new TComponentTimestampedInt16Value	(this,3,"Account");	
					Signal		= new TComponentTimestampedInt16Value	(this,4,"Signal");	
				}
			}
			
			public static class TLastCheckpointTimeValue extends TComponentDoubleValue {
	
				public TLastCheckpointTimeValue(TComponent pOwner, int pID, String pName) {
					super(pOwner,pID,pName);
				}
	
			    @Override
			    public synchronized THistoryRecord ToHistoryRecord(double pTimestamp, long pUserID, boolean flSetOperation, Context context) {
			    	TEventRecord Result = new TEventRecord(pTimestamp,pUserID, THistoryRecord.SEVERITY_INFO, "Checkpoint");
			    	return Result;
			    }
			}
			
			public TServiceProvider						ServiceProvider;
			public TComponentInt16Value					CheckPointInterval;
			public TLastCheckpointTimeValue 			LastCheckpointTime;
			public TComponentTimestampedBooleanValue	IsOnline;
			//. public TComponentTimestampedUInt16Data		SignalValue;
			
			public TConnectorModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ConnectorModule");
				//.
				ServiceProvider	=		new TServiceProvider					(this,1);	
				CheckPointInterval =	new TComponentInt16Value				(this,2,"CheckPointInterval");	
				LastCheckpointTime =	new TLastCheckpointTimeValue			(this,3,"LastCheckpointTime");		
				IsOnline =				new TComponentTimestampedBooleanValue	(this,4,"IsOnline");				
			}
		}
	
		public static class TGPSModule extends TComponent
		{
			public static final double FixIsNotAvailablePrecision = 1000000000.0;

			public static class TGPSFixDataValue extends TComponentValue
			{
				public double Timestamp;
				public double Latitude;
				public double Longitude;
				public double Altitude;
				public double Speed;
				public double Bearing;
				public double Precision;
	
				public TGPSFixDataValue(TComponent pOwner, int pID, String pName) 
				{
					super(pOwner,pID,pName);
					//.
					Precision = FixIsNotAvailablePrecision;
				}
	
				public synchronized boolean IsAvailable()
				{
					return (Precision == FixIsNotAvailablePrecision);
				}
	
				public synchronized boolean IsNull()
				{
					return ((Latitude == 0.0) && (Longitude == 0.0) && (Altitude == 0.0));
				}

				@Override
			    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
				{
					if ((Idx.Value+56) > BA.length) 
						return; //. -> 
					Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Latitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Longitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Altitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Speed = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Bearing = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
					Precision = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
				}

			    @Override
			    public synchronized void FromXMLNode(Node node) {
			    	Node ValueNode = TMyXML.SearchNode(node,Name);
					Timestamp = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Timestamp").getFirstChild().getNodeValue());
					Latitude = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Latitude").getFirstChild().getNodeValue());
					Longitude = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Longitude").getFirstChild().getNodeValue());
					Altitude = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Altitude").getFirstChild().getNodeValue());
					Speed = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Speed").getFirstChild().getNodeValue());
					Bearing = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Bearing").getFirstChild().getNodeValue());
					Precision = Double.parseDouble(TMyXML.SearchNode(ValueNode,"Precision").getFirstChild().getNodeValue());
			    }

			    @Override
			    public synchronized THistoryRecord ToHistoryRecord(double pTimestamp, long pUserID, boolean flSetOperation, Context context) {
			    	TGeoLocationRecord Result = new TGeoLocationRecord(Timestamp,pUserID, Latitude,Longitude,Altitude,Speed,Bearing,Precision);
			    	return Result;
			    }
			}
			
			public static class TMapPOIComponent extends TComponent
			{

				public static class TMapPOIDataValue extends TComponentValue
				{
					public double 	Timestamp;
					public int	 	MapID;
					public int 		POIID;
					public int 		POIType;
					public byte[] 	POINameArray;
					public boolean	flPrivate;
	
					public TMapPOIDataValue(TComponent pOwner, int pID, String pName) {
						super(pOwner,pID,pName);
					}
	
					@Override
				    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
					{
						if ((Idx.Value+8+4+4+4+256+1) > BA.length) return; //. -> 
						Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
						MapID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
						POIID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
						POIType = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
						byte NameLength = BA[Idx.Value]; Idx.Value += 1;
						if (NameLength > 0)
						{
							if ((BA.length-Idx.Value) < NameLength)
								NameLength = (byte)(BA.length-Idx.Value);
							if (NameLength > 0)
							{
								POINameArray = new byte[NameLength];
								System.arraycopy(BA,Idx.Value,POINameArray,0,NameLength); 
							}
							else
								POINameArray = null;
						}
						else
							POINameArray = null;
						Idx.Value += 255;
						flPrivate = (BA[Idx.Value] != 0); Idx.Value++;
					}
				}

				public TMapPOIDataValue Data;
				//. public TPOIImageValue		JPEGImage;
				//. public TPOITextValue		Text;
				//. public TPOIDataFileValue	DataFile;

				public TMapPOIComponent(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"MapPOI");
					Data				= new TMapPOIDataValue		(this,1,"Data");
					//. referencing (virtual) values
					//. JPEGImage 			= new TPOIImageValue		(this,1000);
					//. Text 				= new TPOITextValue			(this,1001);
					//. DataFile			= new TPOIDataFileValue		(this,1002);
				}
			}
			
			public static class TFixMarkComponent extends TComponent
			{

				public static class TFixMarkDataValue extends TComponentValue
				{
					public double 	Timestamp;
					public int 		ObjID;
					public int 		ID;
	
					public TFixMarkDataValue(TComponent pOwner, int pID, String pName)
					{
						super(pOwner,pID,pName);
					}
	
					@Override
				    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
					{
						if ((Idx.Value+8+4+4) > BA.length) return; //. -> 
						Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
						ObjID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
						ID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
					}
				}

				public TFixMarkDataValue Data;
				//. public TFixMarkImageValue JPEGImage;
				
				public TFixMarkComponent(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"FixMark");
					//.
					Data				= new TFixMarkDataValue			(this,1,"Data");
					//. referencing (virtual) values
					///. JPEGImage 			= new TFixMarkImageValue		(this,1000);
				}
			}
			
			public TComponentTimestampedInt16Value	Mode;
			public TComponentTimestampedInt16Value	Status;
			public TComponentInt32Value				DatumID;
			public TComponentInt16Value				DistanceThreshold;
			public TGPSFixDataValue					GPSFixData;
			public TMapPOIComponent					MapPOI;
			public TFixMarkComponent				FixMark;
			
			public TGPSModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPSModule");
				//.
				Mode				= new TComponentTimestampedInt16Value	(this,1,"Mode");
				Status				= new TComponentTimestampedInt16Value	(this,2,"Status");
				DatumID 			= new TComponentInt32Value				(this,3,"DatumID");
				DistanceThreshold 	= new TComponentInt16Value				(this,2,"DistanceThreshold");	
				GPSFixData 			= new TGPSFixDataValue					(this,5,"GPSFixData");
				MapPOI				= new TMapPOIComponent					(this,6);
				FixMark				= new TFixMarkComponent					(this,7);
			}
		}
		
		public static class TGPIModule extends TComponent
		{
			public TComponentTimestampedInt16Value	Value;
			
			public TGPIModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPIModule");
				//.
				Value = new TComponentTimestampedInt16Value(this,1,"Value");
			}
		}
	
		public static class TGPOModule extends TComponent
		{
			public TComponentTimestampedInt16Value	Value;
			
			public TGPOModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPOModule");
				//.
				Value = new TComponentTimestampedInt16Value(this,1,"Value");
			}
		}
	
		public static class TADCModule extends TComponent
		{
			public static final int ValueSize = 16;

			public TComponentTimestampedDoubleArrayValue	Value;
			
			public TADCModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ADCModule");
				//.
				Value = new TComponentTimestampedDoubleArrayValue(this,1,"Value",ValueSize);
			}
		}
	
		public static class TDACModule extends TComponent
		{
			public static final int ValueSize = 16;

			public TComponentTimestampedDoubleArrayValue	Value;
			
			public TDACModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"DACModule");
				//.
				Value = new TComponentTimestampedDoubleArrayValue(this,1,"Value",ValueSize);
			}
		}
	
		public static class TVideoRecorderModule extends TComponent
		{
			public TComponentTimestampedInt16Value		Mode;
			public TComponentTimestampedBooleanValue	Active;
			public TComponentTimestampedBooleanValue	Recording;
			public TComponentTimestampedBooleanValue	Audio;
			public TComponentTimestampedBooleanValue	Video;
			public TComponentTimestampedBooleanValue	Transmitting;
			public TComponentTimestampedBooleanValue	Saving;
			public TComponentTimestampedANSIStringValue	SDP;
			public TComponentTimestampedANSIStringValue	Receivers;
			public TComponentTimestampedANSIStringValue	SavingServer;
			
			public TVideoRecorderModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"VideoRecorderModule");
				//.
				Mode			= new TComponentTimestampedInt16Value		(this,1,"Mode");				
				Active			= new TComponentTimestampedBooleanValue		(this,2,"Active");				
				Recording		= new TComponentTimestampedBooleanValue		(this,3,"Recording");			
				Audio			= new TComponentTimestampedBooleanValue		(this,4,"Audio");				
				Video			= new TComponentTimestampedBooleanValue		(this,5,"Video");				
				Transmitting	= new TComponentTimestampedBooleanValue		(this,6,"Transmitting");		
				Saving			= new TComponentTimestampedBooleanValue		(this,7,"Saving");				
				SDP				= new TComponentTimestampedANSIStringValue	(this,8,"SDP");					
				Receivers		= new TComponentTimestampedANSIStringValue	(this,9,"Receivers");			
				SavingServer	= new TComponentTimestampedANSIStringValue	(this,10,"SavingServer");		
			}
		}
	
		public static class TFileSystemModule extends TComponent
		{
			//. virtual values
			//. public TFileSystemDataValue	FileSystemDataValue;
			
			public TFileSystemModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"FileSystemModule");
				//.
				//. referencing (virtual) values
				//. FileSystemDataValue	= new TFileSystemDataValue(this,1000);	
			}
		}
	
		public static class TControlModule extends TComponent
		{
			//. virtual values
			//. public TControlDataValue		ControlDataValue;
			//. public TControlCommandResponse	ControlCommandResponse;
			
			public TControlModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ControlModule");
				//.
				//. referencing (virtual) values
				//. ControlDataValue		= new TControlDataValue			(this,1000);	
				//. ControlCommandResponse	= new TControlCommandResponse	(this,1001);
			}
		}
	
        public static class TSensorModule extends TComponent 
        {
            //. virtual values
            //. public TSensorDataValue SensorDataValue;

            public TSensorModule(TComponent pOwner, int pID)
            {
            	super(pOwner,pID,"SensorModule");
            	//. virtual values
                //. SensorDataValue = new TSensorDataValue(this, 1000);
            }
        }

        public static class TAudioModule extends TComponent
        {
            public static final int SourcesCount = 2;
            public static final int DestinationsCount = 4;

            //. virtual values
            public TComponentTimestampedInt16ArrayValue SourcesSensitivitiesValue;
            public TComponentTimestampedInt16ArrayValue DestinationsVolumesValue;
            public TAudioFilesValue       				AudioFilesValue;
            public TAudioFileMessageValue               AudioFileMessageValue;

            public TAudioModule(TComponent pOwner, int pID)
            {
            	super(pOwner, pID, "AudioModule");
                //. virtual values
                SourcesSensitivitiesValue   = new TComponentTimestampedInt16ArrayValue  (this, 1001, "SourcesSensitivities", SourcesCount);     SourcesSensitivitiesValue.flVirtualValue = true;
                DestinationsVolumesValue    = new TComponentTimestampedInt16ArrayValue  (this, 1002, "DestinationsVolumes", DestinationsCount); DestinationsVolumesValue.flVirtualValue = true;
                AudioFilesValue             = new TAudioFilesValue	   					(this, 1101); 
                AudioFileMessageValue       = new TAudioFileMessageValue                (this, 1102);
            }
        }
        
        public static class TVideoModule extends TComponent
        {
            public TVideoModule(TComponent pOwner, int pID)
            {
            	super(pOwner,pID,"VideoModule");
            }
        }

        public static class TOSModule extends TComponent
        {
            public TOSModule(TComponent pOwner, int pID)
            {
            	super(pOwner,pID,"OSModule");
            }
        }

        public static class TDataStreamerModule extends TComponent
        {
            public TComponentTimestampedDataValue 		StreamingComponentsValue;
            public TComponentTimestampedBooleanValue	ActiveValue;

            public TDataStreamerModule(TComponent pOwner, int pID)
            {
            	super(pOwner, pID, "DataStreamerModule");
                //. 
            	StreamingComponentsValue   	= new TComponentTimestampedDataValue	(this, 1, "StreamingComponents");     
            	ActiveValue					= new TComponentTimestampedBooleanValue	(this, 2, "Active");     
            }
        }
        
		public static class TControlsModule extends TComponent
		{
			//. values
			public TComponentTimestampedDataValue ControlsDataValue;
			
			public TControlsModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ControlsModule");
				//. values
				ControlsDataValue	= new TComponentTimestampedDataValue(this,1,"ControlsDataValue",false);	
			}
		}
	
		public static class TSensorsModule extends TComponent
		{
			//. values
			public TComponentTimestampedDataValue SensorsDataValue;
			
			public TSensorsModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"SensorsModule");
				//. values
				SensorsDataValue	= new TComponentTimestampedDataValue(this,1,"SensorsDataValue",false);	
			}
		}
	
		public static class TPluginsModule extends TComponent
		{
			//. values
			public TComponentTimestampedDataValue PluginsDataValue;
			
			public TPluginsModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"PluginsModule");
                //. values
				PluginsDataValue	= new TComponentTimestampedDataValue(this,1,"PluginsDataValue",false); 
			}
		}
	
		public static class TAlarmModule extends TComponent
		{
			public static class TAlarm {
			
				public double 	Timestamp;
				public String 	Severity = "";
				public String 	ID = "";
				public String 	Value = "";
				public String 	ChannelID = "";
				public String 	DataTypeID = "";
				public String 	Info = "";
				public String 	Notification = "";

				public void FromXMLNode(Node ANode) throws Exception {
					Node _Node = TMyXML.SearchNode(ANode,"Timestamp");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							Timestamp = Double.parseDouble(ValueNode.getNodeValue());
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"Severity");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							Severity = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"ID");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							ID = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"Value");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							Value = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"Info");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							Info = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"Notification");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							Notification = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"ChannelID");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							ChannelID = ValueNode.getNodeValue();
					}
					//.
					_Node = TMyXML.SearchNode(ANode,"DataTypeID");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							DataTypeID = ValueNode.getNodeValue();
					}
				}
				
				public int GetSeverityColor() {
					if (Severity.equals("Normal"))
						return Color.GREEN; //. ->
					if (Severity.equals("Minor"))
						return Color.MAGENTA; //. ->
					if (Severity.equals("Major"))
						return Color.MAGENTA; //. ->
					if (Severity.equals("Critical"))
						return Color.RED; //. ->
					else
						return Color.RED; //. ->
				}
			}
			
			public static class TAlarms {
				
				public TAlarm[] Items;

				public TAlarms(byte[] BA) throws Exception {
					FromByteArray(BA);
				}
				
				public TAlarms(String Base64String) throws Exception {
					FromBase64String(Base64String);
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
				
				public void FromBase64String(String S) throws Exception {
					FromByteArray(Base64.decode(S, Base64.NO_WRAP));
				}
				
				public void FromXMLNode(Node ANode) throws Exception {
					int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
					switch (Version) {
					case 1:
						try {
							NodeList AlarmsNode = TMyXML.SearchNode(ANode,"Alarms").getChildNodes();
							int Cnt = AlarmsNode.getLength();
							Items = new TAlarm[Cnt];
							for (int I = 0; I < Cnt; I++) {
								Node AlarmNode = AlarmsNode.item(I);
								//.
								Items[I] = new TAlarm();
								Items[I].FromXMLNode(AlarmNode);
							}
						}
						catch (Exception E) {
			    			throw new Exception("error of parsing alarm data: "+E.getMessage()); //. =>
						}
						break; //. >
					default:
						throw new Exception("unknown alarm data version, version: "+Integer.toString(Version)); //. =>
					}
				}
			}
			//. values
			public TComponentTimestampedDataValue AlarmDataValue;
			
			public TAlarmModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"AlarmModule");
                //. values
				AlarmDataValue	= new TComponentTimestampedDataValue(this,1,"AlarmDataValue",false); 
			}
		}
	
		public static class TUserAgentModule extends TComponent
		{
			//. values
			public TUserIDValue		UserIDValue;
			public TUserDataValue	UserDataValue;
			
			public TUserAgentModule(TComponent pOwner, int pID) 
			{
				super(pOwner,pID,"UserAgentModule");
                //. values
				UserIDValue		= new TUserIDValue		(this,1);
				UserDataValue	= new TUserDataValue	(this,2);
			}
		}
	

		public TDeviceDescriptor	DeviceDescriptor;
		public TBatteryModule		BatteryModule;
		public TConnectorModule 	ConnectorModule;
		public TGPSModule 			GPSModule;
		public TGPIModule			GPIModule;
		public TGPOModule			GPOModule;
		public TADCModule			ADCModule;
		public TDACModule			DACModule;
		public TVideoRecorderModule	VideoRecorderModule;
		public TFileSystemModule	FileSystemModule;
		public TControlModule		ControlModule;
		public TSensorModule		SensorModule;
		public TAudioModule			AudioModule;
		public TVideoModule			VideoModule;
		public TOSModule			OSModule;
		public TDataStreamerModule	DataStreamerModule;
		public TControlsModule		ControlsModule;
		public TSensorsModule		SensorsModule;
		public TPluginsModule		PluginsModule;
		public TAlarmModule			AlarmModule;
		public TUserAgentModule		UserAgentModule;
		
		public TGeoMonitoredObject1DeviceComponent(TGeoMonitoredObject1DeviceSchema pSchema) throws Exception {
			super(pSchema,2,"GeoMonitoredObjectDeviceComponent");
			//.
			//. components
			DeviceDescriptor	= new TDeviceDescriptor		(this,1);
			BatteryModule 		= new TBatteryModule		(this,2);
			ConnectorModule 	= new TConnectorModule		(this,3);
			GPSModule 			= new TGPSModule			(this,4);
			GPIModule 			= new TGPIModule			(this,5);
			GPOModule 			= new TGPOModule			(this,6);
			ADCModule			= new TADCModule			(this,7);
			DACModule			= new TDACModule			(this,8);
			VideoRecorderModule	= new TVideoRecorderModule	(this,9);
			FileSystemModule	= new TFileSystemModule		(this,10);
			ControlModule		= new TControlModule		(this,11);
			SensorModule		= new TSensorModule			(this,12);
			AudioModule			= new TAudioModule			(this,13);
			VideoModule			= new TVideoModule			(this,14);
			OSModule			= new TOSModule				(this,15);
			//. TaskModule		= new TTaskModule			(this,16);
			DataStreamerModule	= new TDataStreamerModule	(this,17);
			ControlsModule		= new TControlsModule		(this,18);
			SensorsModule		= new TSensorsModule		(this,19);
			PluginsModule		= new TPluginsModule		(this,20);
			AlarmModule			= new TAlarmModule			(this,21);
			UserAgentModule		= new TUserAgentModule		(this,22);
		}
	}
	
	public TGeoMonitoredObject1DeviceSchema(TObjectModel pObjectModel) throws Exception {
		super(pObjectModel);
		RootComponent = new TGeoMonitoredObject1DeviceComponent(this);
	}
}
