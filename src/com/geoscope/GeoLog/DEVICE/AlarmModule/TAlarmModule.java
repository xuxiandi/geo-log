package com.geoscope.GeoLog.DEVICE.AlarmModule;

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
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetAlarmDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TAlarmModule extends TModule {

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"AlarmModule";
	}
	
	public static final String DefaultProfileFileName = "Profile";
	
	public static class TProfile {
		
		private String ProfileFileName;
		//.
		public byte[] SourceByteArray = null;
		//.
		public Node TriggersNode = null;
		
		public TProfile(String pProfileFileName) throws Exception {
			ProfileFileName = pProfileFileName;
			//.
			LoadProfile();
		}
		
		public boolean Exists() {
			return (TriggersNode != null);
		}
		
		public void Clear() {
			SourceByteArray = null;
			TriggersNode = null;
		}
		
		private void LoadProfile() throws Exception {
			File F = new File(ProfileFileName);
			if (F.exists()) { 
		    	FileInputStream FIS = new FileInputStream(F);
		    	try {
		    			byte[] BA = new byte[(int)F.length()];
		    			FIS.read(BA);
		    			//.
		    			FromByteArray(BA);
		    	}
				finally
				{
					FIS.close(); 
				}
			}
			else
				Clear();
		}
		
		public void SaveProfile() throws Exception {
			File F = new File(ProfileFileName);
			if (SourceByteArray != null) {
				FileOutputStream FOS = new FileOutputStream(F);
		        try
		        {
		        	byte[] BA = SourceByteArray;
		        	FOS.write(BA);
		        }
		        finally
		        {
		        	FOS.close();
		        }
			}
			else
				F.delete();
		}	
		
		public void FromByteArray(byte[] BA) throws Exception {
			if ((BA == null) || (BA.length == 0)) {
				Clear();
				return; //. ->
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
			FromXMLNode(RootNode);
			SourceByteArray = BA;
		}		

		private void FromXMLNode(Node ANode) throws Exception {
			int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
					TriggersNode = TMyXML.SearchNode(ANode,"Triggers");
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing profile: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown profile version, version: "+Integer.toString(Version)); //. =>
			}
		}
		
		public Node GetTriggerAlarmerNode(String TriggerTypeID, String TypeID) {
			if (TriggersNode == null)
				return null; //. ->
			//.
			NodeList TriggersNodeList = TriggersNode.getChildNodes();
			int TCnt = TriggersNodeList.getLength();
			for (int I = 0; I < TCnt; I++) {
				Node TriggerNode = TriggersNodeList.item(I);
				//.
				if (TriggerNode.getLocalName() != null) {
					String _TriggerTypeID = TMyXML.SearchNode(TriggerNode,"TypeID").getFirstChild().getNodeValue();
					if (_TriggerTypeID.equals(TriggerTypeID)) {
						NodeList AlarmersNode = TMyXML.SearchNode(TriggerNode,"Alarmers").getChildNodes();
						int ACnt = AlarmersNode.getLength();
						for (int J = 0; J < ACnt; J++) {
							Node AlarmerNode = AlarmersNode.item(J);
							//.
							if (AlarmerNode.getLocalName() != null) {
								String _TypeID = TMyXML.SearchNode(AlarmerNode,"TypeID").getFirstChild().getNodeValue();
								//.
								if (_TypeID.equals(TypeID)) 
									return AlarmerNode; //. -> 
							}
						}
					}
				}
			}
			return null;
		}
	}
	
	public static class TAlarmLevel {
		
		public static class TThreshold {
			
			public TThreshold() {
			}
			
			public boolean IsSignalling(Object pValue) {
				return false;
			}

			public void FromXMLNode(Node ANode) throws Exception {
			}
		}
		
		
		public int 			Level = 0;
		public TThreshold	Threshold = null;
		public String		Severity = "";
		public String  		ID = "";
		public String  		Notification = "";

		public TAlarmLevel(int pLevel) {
			Level = pLevel;
		}
		
		public void FromXMLNode(Node ANode, Class<?> TThresholdClass) throws Exception {
			Node ThresholdNode = TMyXML.SearchNode(ANode,"Threshold");
			if (ThresholdNode != null) {
				Threshold = (TThreshold)TThresholdClass.newInstance();
				//.
				Threshold.FromXMLNode(ThresholdNode);
			}
			else
				Threshold = null;
			//.
			Node SeverityNode = TMyXML.SearchNode(ANode,"Severity");
			if (SeverityNode != null) {
				Node ValueNode = SeverityNode.getFirstChild();
				if (ValueNode != null)
					Severity = ValueNode.getNodeValue();
			}
			//.
			Node IDNode = TMyXML.SearchNode(ANode,"ID");
			if (IDNode != null) {
				Node ValueNode = IDNode.getFirstChild();
				if (ValueNode != null)
					ID = ValueNode.getNodeValue();
			}
			//.
			Node NotificationNode = TMyXML.SearchNode(ANode,"Notification");
			if (NotificationNode != null) {
				Node ValueNode = NotificationNode.getFirstChild();
				if (ValueNode != null)
					Notification = ValueNode.getNodeValue();
			}
		}
		
		public boolean Threshold_IsSignalling(Object pValue) {
			return ((Threshold != null) && Threshold.IsSignalling(pValue));
		}
	}
	
	public static class TAlarmLevels {
		
		public ArrayList<TAlarmLevel> Items = new ArrayList<TAlarmLevel>();

		public TAlarmLevels(Node ANode, Class<?> TThresholdClass) throws Exception {
			FromXMLNode(ANode, TThresholdClass);
		}
		
		public void FromXMLNode(Node ANode, Class<?> TThresholdClass) throws Exception {
			Items.clear();
			//.
			NodeList ItemsNodeList = ANode.getChildNodes();
			int Level = 1;
			int Cnt = ItemsNodeList.getLength();
			for (int I = 0; I < Cnt; I++) {
				Node ItemNode = ItemsNodeList.item(I);
				//.
				if (ItemNode.getLocalName() != null) {
					TAlarmLevel Item = new TAlarmLevel(Level); Level++;
					Item.FromXMLNode(ItemNode, TThresholdClass);
					Items.add(Item);
				}
			}
		}
		
		public int Count() {
			return Items.size();
		}
		                    
		public TAlarmLevel GetItemByLevel(int Level) {
			return Items.get(Level-1);
		}
		
		public TAlarmLevel GetSignallingLevel(Object pValue) {
			int Cnt = Items.size();
			for (int I = (Cnt-1); I >= 0; I--) {
				TAlarmLevel Item = Items.get(I);
				if (Item.Threshold_IsSignalling(pValue))
					return Item; //. ->
			}
			return null;
		}
	}
	
	public interface IAlarmer {
		
		public String GetTriggerTypeID();
		public String GetTypeID();
		//.
		public void LoadProfile(Node ANode) throws Exception;
		//.
		public void 	SetEnabled(boolean pflEnabled);
		public boolean 	IsEnabled();
		//.
		public void 	ResetAlarm();
		public boolean 	IsAlarm();
		public void 	AlarmToXMLSerializer(XmlSerializer Serializer) throws Exception;
		//.
		public void DoOnValue(Object Value);
	}
	
	public static class TAlarmer implements IAlarmer {
		
		public static TAlarmer GetAlarmer(TAlarmModule AlarmModule, String TriggerTypeID, String TypeID) throws Exception {
			if (TBatteryLevelTrigger.TriggerTypeID.equals(TriggerTypeID))
				return TBatteryLevelTrigger.GetAlarmer(AlarmModule, TypeID); //. =>
			if (TCellularSignalTrigger.TriggerTypeID.equals(TriggerTypeID))
				return TCellularSignalTrigger.GetAlarmer(AlarmModule, TypeID); //. =>
			else 
				return null; //. ->
		}
		
		
		protected TAlarmModule AlarmModule;
		//.
		public boolean flEnabled = true;
		//.
		public int 			AlarmLevel = -1; //. unknown
		public double		AlarmTimestamp = 0.0;
		public String  		AlarmSeverity = "";
		public String  		AlarmID = "";
		public String  		AlarmValue = "";
		public String  		AlarmInfo = "";
		public String  		AlarmNotification = "";
		
		public TAlarmer(TAlarmModule pAlarmModule) throws Exception {
			AlarmModule = pAlarmModule;
			//.
			Node MyNode = AlarmModule.Profile.GetTriggerAlarmerNode(GetTriggerTypeID(), GetTypeID()); 
			if (MyNode != null)
				LoadProfile(MyNode);
		}
		
		@Override
		public String GetTriggerTypeID() {
			return null;
		}

		@Override
		public String GetTypeID() {
			return null;
		}
		

		@Override
		public void SetEnabled(boolean pflEnabled) {
			flEnabled = pflEnabled;
		}

		@Override
		public boolean IsEnabled() {
			return flEnabled;
		}
		
		@Override
		public void LoadProfile(Node ANode) throws Exception {
			Node _Node = TMyXML.SearchNode(ANode,"Enabled");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					SetEnabled(Integer.parseInt(ValueNode.getNodeValue()) != 0);
			}
			//.
			ResetAlarm();
		}
		
		@Override
		public void ResetAlarm() {
			AlarmLevel = -1; //. unknown
		}
		
		@Override
		public boolean IsAlarm() {
			return (AlarmLevel > 0);
		}
		
		@Override
		public synchronized void AlarmToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. AlarmTimestamp
	        Serializer.startTag("", "Timestamp");
	        Serializer.text(Double.toString(AlarmTimestamp));
	        Serializer.endTag("", "Timestamp");
	    	//. AlarmID
	        Serializer.startTag("", "ID");
	        Serializer.text(AlarmID);
	        Serializer.endTag("", "ID");
	    	//. AlarmSeverity
	        Serializer.startTag("", "Severity");
	        Serializer.text(AlarmSeverity);
	        Serializer.endTag("", "Severity");
	    	//. AlarmValue
	        if (AlarmValue.length() > 0) {
		        Serializer.startTag("", "Value");
		        Serializer.text(AlarmValue);
		        Serializer.endTag("", "Value");
	        }
	    	//. AlarmInfo
	        if (AlarmInfo.length() > 0) {
		        Serializer.startTag("", "Info");
		        Serializer.text(AlarmInfo);
		        Serializer.endTag("", "Info");
	        }
	    	//. AlarmNotification
	        if (AlarmNotification.length() > 0) {
		        Serializer.startTag("", "Notification");
		        Serializer.text(AlarmNotification);
		        Serializer.endTag("", "Notification");
	        }
		}

		@Override
		public synchronized void DoOnValue(Object Value) {
		}
	}
	
	public static class TProfilableAlarmer extends TAlarmer {
		
		protected TAlarmLevels AlarmLevels;
		//.
		protected String ValueUnit = "";
		
		public TProfilableAlarmer(TAlarmModule pAlarmModule) throws Exception {
			super(pAlarmModule);
		}
		
		@Override
		public synchronized void DoOnValue(Object Value) {
			if (!IsEnabled() || (AlarmLevels == null))
				return; //. ->
			//.
			TAlarmLevel SignallingLevel = AlarmLevels.GetSignallingLevel(Value);
			if (SignallingLevel != null) {
				if (SignallingLevel.Level != AlarmLevel) {
					AlarmLevel = SignallingLevel.Level;
					//.
					AlarmTimestamp = OleDate.UTCCurrentTimestamp();
					AlarmSeverity = SignallingLevel.Severity;
					AlarmID = SignallingLevel.ID;
					AlarmValue = String.format("%.1f",(Double)Value)+" "+ValueUnit;
					AlarmNotification = SignallingLevel.Notification;
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
			}
			else
				if (AlarmLevel != 0) {
					AlarmLevel = 0;
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
		}
	}

	public static class TBatteryLevelTrigger {
		
		public static final String TriggerTypeID = "BatteryLevel";
		
		public static TAlarmer GetAlarmer(TAlarmModule AlarmModule, String TypeID) throws Exception {
			if (TLevelAlarmer.TypeID.equals(TypeID))
				return (new TLevelAlarmer(AlarmModule)); //. ->
			if (TLevelProfilableAlarmer.TypeID.equals(TypeID))
				return (new TLevelProfilableAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TLevelAlarmer extends TAlarmer {
			
			public static final String TypeID = "Default";
			
			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static final double AL1_DefaultThreshold = 20.0; //. %
			public static final double AL2_DefaultThreshold = 10.0; //. %
			public static final double AL3_DefaultThreshold =  5.0; //. %

			
			public double AL1_Threshold = AL1_DefaultThreshold; 
			public double AL2_Threshold = AL2_DefaultThreshold; 
			public double AL3_Threshold = AL3_DefaultThreshold; 
			
			public TLevelAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				if (!IsEnabled())
					return; //. ->
				Double V = (Double)Value;
				//.
				int _AlarmLevel = 0;
				if (V < AL3_Threshold) 
					_AlarmLevel = 3;
				else
					if (V < AL2_Threshold) 
						_AlarmLevel = 2;
					else
						if (V < AL1_Threshold) 
							_AlarmLevel = 1;
				//.
				if (_AlarmLevel != AlarmLevel) {
					AlarmLevel = _AlarmLevel;
					//.
					switch (AlarmLevel) {
					
					case 1:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Minor";
						AlarmID = "BatteryLevelLow";
						AlarmValue = String.format("%.1f",V)+" %";
						AlarmNotification = "Visual";
						break; //. >

					case 2:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Major";
						AlarmID = "BatteryLevelMajorLow";
						AlarmValue = String.format("%.1f",V)+" %";
						AlarmNotification = "Visual";
						break; //. >

					case 3:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Critical";
						AlarmID = "BatteryLevelCriticalLow";
						AlarmValue = String.format("%.1f",V)+" %";
						AlarmNotification = "Visual";
						break; //. >
					}
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
			}
		}

		public static class TLevelProfilableAlarmer extends TProfilableAlarmer {
			
			public static final String TypeID = "Profilable";
			
			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TAlarmLevelThreshold extends TAlarmLevel.TThreshold {
				
				private double Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Double)pValue) < Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Double.parseDouble(ValueNode.getNodeValue());
					else
						Value = -Double.MIN_VALUE;
				}
			}
			
			
			public TLevelProfilableAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
				ValueUnit = "%";
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TAlarmLevelThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
	}
	
	public static class TCellularSignalTrigger {
		
		public static final String TriggerTypeID = "CellularSignal";
		
		public static TAlarmer GetAlarmer(TAlarmModule AlarmModule, String TypeID) throws Exception {
			if (TSignalAlarmer.TypeID.equals(TypeID))
				return (new TSignalAlarmer(AlarmModule)); //. ->
			if (TSignalProfilableAlarmer.TypeID.equals(TypeID))
				return (new TSignalProfilableAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TSignalAlarmer extends TAlarmer {
			
			public static final String TypeID = "Default";
			
			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static final double AL1_DefaultThreshold = 10.0; //. %
			public static final double AL2_DefaultThreshold =  5.0; //. %
			
			
			public double AL1_Threshold; 
			public double AL2_Threshold; 
			
			public TSignalAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				AL1_Threshold = AL1_DefaultThreshold; 
				AL2_Threshold = AL2_DefaultThreshold;
				//.
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) {
					Node AlarmLevelNode = TMyXML.SearchNode(AlarmLevelsNode,"AL1");
					if (AlarmLevelNode != null) {
						Node ThresholdNode = TMyXML.SearchNode(AlarmLevelNode,"Threshold");
						if (ThresholdNode != null) {
							Node ValueNode = ThresholdNode.getFirstChild();
							if (ValueNode != null)
								AL1_Threshold = Double.parseDouble(ValueNode.getNodeValue());
						}
					}
					AlarmLevelNode = TMyXML.SearchNode(AlarmLevelsNode,"AL2");
					if (AlarmLevelNode != null) {
						Node ThresholdNode = TMyXML.SearchNode(AlarmLevelNode,"Threshold");
						if (ThresholdNode != null) {
							Node ValueNode = ThresholdNode.getFirstChild();
							if (ValueNode != null)
								AL2_Threshold = Double.parseDouble(ValueNode.getNodeValue());
						}
					}
				}
				super.LoadProfile(ANode);
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				if (!IsEnabled())
					return; //. ->
				Double V = (Double)Value;
				//.
				int _AlarmLevel = 0;
				if (V < AL2_Threshold) 
					_AlarmLevel = 2;
				else
					if (V < AL1_Threshold) 
						_AlarmLevel = 1;
				//.
				if (_AlarmLevel != AlarmLevel) {
					AlarmLevel = _AlarmLevel;
					//.
					switch (AlarmLevel) {
					
					case 1:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Major";
						AlarmID = "CellularSignalMajorLow";
						AlarmValue = String.format("%.1f",V)+" %";
						AlarmNotification = "Visual";
						break; //. >

					case 2:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Critical";
						AlarmID = "CellularSignalCriticalLow";
						AlarmValue = String.format("%.1f",V)+" %";
						AlarmNotification = "Visual";
						break; //. >
					}
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
			}
		}
		
		public static class TSignalProfilableAlarmer extends TProfilableAlarmer {
			
			public static final String TypeID = "Profilable";
			
			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TAlarmLevelThreshold extends TAlarmLevel.TThreshold {
				
				private double Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Double)pValue) < Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Double.parseDouble(ValueNode.getNodeValue());
					else
						Value = -Double.MIN_VALUE;
				}
			}
			
			
			public TSignalProfilableAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
				ValueUnit = "%";
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TAlarmLevelThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
	}
	
	public static class TChannelDataTypeAlarmer extends TDataType.TDataTrigger.TAlarmer implements IAlarmer {
	
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String TriggerTypeID, String TypeID) throws Exception {
			if (TInt32ValueDataTypeTrigger.TriggerTypeID.equals(TriggerTypeID))
				return TInt32ValueDataTypeTrigger.GetAlarmer(AlarmModule, TypeID); //. =>
			if (TDoubleValueDataTypeTrigger.TriggerTypeID.equals(TriggerTypeID))
				return TDoubleValueDataTypeTrigger.GetAlarmer(AlarmModule, TypeID); //. =>
			if (TLightSensorDataTypeTrigger.TriggerTypeID.equals(TriggerTypeID))
				return TLightSensorDataTypeTrigger.GetAlarmer(AlarmModule, TypeID); //. =>
			else 
				return null; //. ->
		}
		

		protected TAlarmModule AlarmModule;
		
		public TChannelDataTypeAlarmer(TAlarmModule pAlarmModule) throws Exception {
			AlarmModule = pAlarmModule;
			//.
			Node MyNode = AlarmModule.Profile.GetTriggerAlarmerNode(GetTriggerTypeID(), GetTypeID()); 
			if (MyNode != null)
				LoadProfile(MyNode);
		}

		@Override
		public String GetTriggerTypeID() {
			return null;
		}

		@Override
		public String GetTypeID() {
			return null;
		}

		@Override
		public void SetEnabled(boolean pflEnabled) {
			flEnabled = pflEnabled;
		}

		@Override
		public boolean IsEnabled() {
			return flEnabled;
		}
		
		@Override
		public void LoadProfile(Node ANode) throws Exception {
			Node _Node = TMyXML.SearchNode(ANode,"Enabled");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					SetEnabled(Integer.parseInt(ValueNode.getNodeValue()) != 0);
			}
			//.
			ResetAlarm();
		}
		
		@Override
		public void ResetAlarm() {
			AlarmLevel = -1; //. unknown
		}
		
		@Override
		public boolean IsAlarm() {
			return (AlarmLevel > 0);
		}
		
		@Override
		public synchronized void AlarmToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. AlarmTimestamp
	        Serializer.startTag("", "Timestamp");
	        Serializer.text(Double.toString(AlarmTimestamp));
	        Serializer.endTag("", "Timestamp");
	    	//. AlarmSeverity
	        Serializer.startTag("", "Severity");
	        Serializer.text(AlarmSeverity);
	        Serializer.endTag("", "Severity");
	    	//. AlarmID
	        Serializer.startTag("", "ID");
	        Serializer.text(AlarmID);
	        Serializer.endTag("", "ID");
	    	//. AlarmValue
	        String AV = AlarmDataType.GetValueAndUnitString(AlarmModule.Device.context);
	        if (AV.length() > 0) {
		        Serializer.startTag("", "Value");
		        Serializer.text(AV);
		        Serializer.endTag("", "Value");
	        }
	    	//. AlarmInfo
	        if (AlarmInfo.length() > 0) {
		        Serializer.startTag("", "Info");
		        Serializer.text(AlarmInfo);
		        Serializer.endTag("", "Info");
	        }
	    	//. AlarmNotification
	        if (AlarmNotification.length() > 0) {
		        Serializer.startTag("", "Notification");
		        Serializer.text(AlarmNotification);
		        Serializer.endTag("", "Notification");
	        }
	    	//. AlarmSourceChannelID
	        String CID = AlarmDataType.Channel.GetTypeID();
	        if (CID.length() > 0) {
		        Serializer.startTag("", "ChannelID");
		        Serializer.text(CID);
		        Serializer.endTag("", "ChannelID");
	        }
	    	//. AlarmDataTypeID
	        if (AlarmDataType.TypeID.length() > 0) {
		        Serializer.startTag("", "DataTypeID");
		        Serializer.text(AlarmDataType.TypeID);
		        Serializer.endTag("", "DataTypeID");
	        }
		}

		@Override
		public void DoOnValue(Object Value) {
		}
	}
	
	public static class TChannelDataTypeProfilableAlarmer extends TChannelDataTypeAlarmer {
		
		protected TAlarmLevels AlarmLevels;
		
		public TChannelDataTypeProfilableAlarmer(TAlarmModule pAlarmModule) throws Exception {
			super(pAlarmModule);
		}
		
		@Override
		protected synchronized void DoOnDataTypeValue(TDataType DataType) {
			if (!IsEnabled() || (AlarmLevels == null))
				return; //. ->
			//.
			Object Value = DataType.GetContainerTypeValue();
			//.
			TAlarmLevel SignallingLevel = AlarmLevels.GetSignallingLevel(Value);
			if (SignallingLevel != null) {
				if (SignallingLevel.Level != AlarmLevel) {
					AlarmLevel = SignallingLevel.Level;
					//.
					AlarmTimestamp = OleDate.UTCCurrentTimestamp();
					AlarmSeverity = SignallingLevel.Severity;
					AlarmID = SignallingLevel.ID;
					AlarmDataType = DataType.Clone();
					AlarmNotification = SignallingLevel.Notification;
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
			}
			else
				if (AlarmLevel != 0) {
					AlarmLevel = 0;
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
		}
	}

	public static class TInt32ValueDataTypeTrigger {
		
		public static final String TriggerTypeID = "Int32Value";
		
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String TypeID) throws Exception {
			if (TInt32ValueProfilableAAAlarmer.TypeID.equals(TypeID))
				return (new TInt32ValueProfilableAAAlarmer(AlarmModule)); //. ->
			if (TInt32ValueProfilableBAAlarmer.TypeID.equals(TypeID))
				return (new TInt32ValueProfilableBAAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TInt32ValueProfilableAAAlarmer extends TChannelDataTypeProfilableAlarmer {

			public static final String TypeID = "ProfilableAA"; //. profilable, alarm on above

			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TInt32ValueThreshold extends TAlarmLevel.TThreshold {
				
				private int Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Integer)pValue) > Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Integer.parseInt(ValueNode.getNodeValue());
					else
						Value = 0;
				}
			}
			
			
			public TInt32ValueProfilableAAAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TInt32ValueThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
		
		public static class TInt32ValueProfilableBAAlarmer extends TChannelDataTypeProfilableAlarmer {

			public static final String TypeID = "ProfilableBA"; //. profilable, alarm on below

			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TInt32ValueThreshold extends TAlarmLevel.TThreshold {
				
				private int Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Integer)pValue) < Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Integer.parseInt(ValueNode.getNodeValue());
					else
						Value = 0;
				}
			}
			
			
			public TInt32ValueProfilableBAAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TInt32ValueThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
	}

	public static class TDoubleValueDataTypeTrigger {
		
		public static final String TriggerTypeID = "DoubleValue";
		
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String TypeID) throws Exception {
			if (TDoubleValueProfilableAAAlarmer.TypeID.equals(TypeID))
				return (new TDoubleValueProfilableAAAlarmer(AlarmModule)); //. ->
			if (TDoubleValueProfilableBAAlarmer.TypeID.equals(TypeID))
				return (new TDoubleValueProfilableBAAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TDoubleValueProfilableAAAlarmer extends TChannelDataTypeProfilableAlarmer {

			public static final String TypeID = "ProfilableAA"; //. profilable, alarm on above

			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TDoubleValueThreshold extends TAlarmLevel.TThreshold {
				
				private double Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Double)pValue) > Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Double.parseDouble(ValueNode.getNodeValue());
					else
						Value = 0.0;
				}
			}
			
			
			public TDoubleValueProfilableAAAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TDoubleValueThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
		
		public static class TDoubleValueProfilableBAAlarmer extends TChannelDataTypeProfilableAlarmer {

			public static final String TypeID = "ProfilableBA"; //. profilable, alarm on below

			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static class TDoubleValueThreshold extends TAlarmLevel.TThreshold {
				
				private double Value;

				@Override
				public boolean IsSignalling(Object pValue) {
					return (((Double)pValue) < Value);
				}

				@Override
				public void FromXMLNode(Node ANode) throws Exception {
					Node ValueNode = ANode.getFirstChild();
					if (ValueNode != null)
						Value = Double.parseDouble(ValueNode.getNodeValue());
					else
						Value = 0.0;
				}
			}
			
			
			public TDoubleValueProfilableBAAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) 
					AlarmLevels = new TAlarmLevels(AlarmLevelsNode, TDoubleValueThreshold.class);
				else
					AlarmLevels = null;
				super.LoadProfile(ANode);
			}
		}
	}

	public static class TLightSensorDataTypeTrigger {
		
		public static final String TriggerTypeID = "LightSensorDarkness";
		
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String TypeID) throws Exception {
			if (TLSAlarmer.TypeID.equals(TypeID))
				return (new TLSAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TLSAlarmer extends TChannelDataTypeAlarmer {

			public static final String TypeID = "Default";

			public static String TriggerTypeID() {
				return TriggerTypeID;
			}
			
			public static final Double AL1_DefaultThreshold = 10.0; //. %

			
			public Double AL1_Threshold;
			
			public TLSAlarmer(TAlarmModule pAlarmModule) throws Exception {
				super(pAlarmModule);
			}
			
			@Override
			public String GetTriggerTypeID() {
				return TriggerTypeID();
			}

			@Override
			public String GetTypeID() {
				return TypeID;
			}
			
			@Override
			public void LoadProfile(Node ANode) throws Exception {
				AL1_Threshold = AL1_DefaultThreshold; 
				//.
				Node AlarmLevelsNode = TMyXML.SearchNode(ANode,"AlarmLevels");
				if (AlarmLevelsNode != null) {
					Node AlarmLevelNode = TMyXML.SearchNode(AlarmLevelsNode,"AL1");
					if (AlarmLevelNode != null) {
						Node ThresholdNode = TMyXML.SearchNode(AlarmLevelNode,"Threshold");
						if (ThresholdNode != null) {
							Node ValueNode = ThresholdNode.getFirstChild();
							if (ValueNode != null)
								AL1_Threshold = Double.parseDouble(ValueNode.getNodeValue());
						}
					}
				}
				super.LoadProfile(ANode);
			}
			
			@Override
			protected synchronized void DoOnDataTypeValue(TDataType DataType) {
				if (!IsEnabled())
					return; //. ->
				Double V = (Double)DataType.GetContainerTypeValue();
				int _AlarmLevel = ((V < AL1_Threshold) ? 1 : 0);
				if (_AlarmLevel != AlarmLevel) {
					AlarmLevel = _AlarmLevel;
					//.
					switch (AlarmLevel) {
					
					case 1:
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmSeverity = "Minor";
						AlarmID = "LightLevelDark";
						AlarmDataType = DataType.Clone();
						AlarmNotification = "Visual";
						break; //. >
					}
					//. update result AlarmData
					AlarmModule.Alarmers_CommitAlarmData();
				}
			}
		}
	}

	public static class TAlarmers {
		
		private TAlarmModule AlarmModule;
		//.
		private ArrayList<Object> Items = new ArrayList<Object>();
		
		public TAlarmers(TAlarmModule pAlarmModule) {
			AlarmModule = pAlarmModule;
		}
		
		public synchronized void Add(Object Item) {
			Items.add(Item);
		}
		
		public synchronized void Clear() {
			Items.clear();
		}

		public synchronized void FromXMLNode(Node ANode) throws Exception {
			Items.clear();
			//.
			if (ANode == null)
				return; //. ->
			//.
			NodeList TriggersNode = ANode.getChildNodes();
			int TCnt = TriggersNode.getLength();
			for (int I = 0; I < TCnt; I++) {
				Node TriggerNode = TriggersNode.item(I);
				//.
				if (TriggerNode.getLocalName() != null) {
					String TriggerTypeID = TMyXML.SearchNode(TriggerNode,"TypeID").getFirstChild().getNodeValue();
					//.
					NodeList AlarmersNode = TMyXML.SearchNode(TriggerNode,"Alarmers").getChildNodes();
					int ACnt = AlarmersNode.getLength();
					for (int J = 0; J < ACnt; J++) {
						Node AlarmerNode = AlarmersNode.item(J);
						//.
						if (AlarmerNode.getLocalName() != null) {
							String TypeID = TMyXML.SearchNode(AlarmerNode,"TypeID").getFirstChild().getNodeValue();
							//.
							Object Alarmer = TAlarmer.GetAlarmer(AlarmModule, TriggerTypeID, TypeID);
							if (Alarmer != null)
								Add(Alarmer);
							else {
								Alarmer = TChannelDataTypeAlarmer.GetAlarmer(AlarmModule, TriggerTypeID, TypeID);
								if (Alarmer != null)
									Add(Alarmer);
							}
						}
					}
				}
			}
		}
		
	    public synchronized byte[] AlarmsToByteArray() throws Exception {
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
		        //. 
		        if (!AlarmsToXMLSerializer(Serializer))
		        	return null; //. ->
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
	    
		public synchronized boolean AlarmsToXMLSerializer(XmlSerializer Serializer) throws Exception {
			int Version = 1;
	        //. Version
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	        //. Alarms
	        Serializer.startTag("", "Alarms");
	        int Idx = 0;
	        int Cnt = Items.size();
	        for (int I = 0; I < Cnt; I++) {
	        	Object AnAlarmer = Items.get(I);
	        	//.
	        	synchronized (AnAlarmer) {
        			IAlarmer Alarmer = (IAlarmer)AnAlarmer;
					if (Alarmer.IsAlarm()) {
			        	String ItemNodeName = "A"+Integer.toString(Idx);
			            Serializer.startTag("", ItemNodeName);
			            //.
			            Alarmer.AlarmToXMLSerializer(Serializer);
			            //.
			            Serializer.endTag("", ItemNodeName);
			            //.
			            Idx++;
					}
				}
	        }
	        Serializer.endTag("", "Alarms");
	        //.
	        return (Idx > 0);
		}		
		
		public synchronized Object GetItem(String TriggerTypeID, String TypeID) {
	        int Cnt = Items.size();
	        for (int I = 0; I < Cnt; I++) {
	        	Object AnAlarmer = Items.get(I);
	        	IAlarmer Alarmer = (IAlarmer)AnAlarmer;
	        	if (Alarmer.GetTriggerTypeID().equals(TriggerTypeID) && Alarmer.GetTypeID().equals(TypeID))
	        		return AnAlarmer; //. ->
	        }
	        return null;
		}
	}
	
	
	public TProfile Profile;
	//.
	private TAlarmers Alarmers;
	
    public TAlarmModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
		//.
		Alarmers = new TAlarmers(this);
        //.
        Profile = new TProfile(Folder()+"/"+DefaultProfileFileName);
    }
    
    public void Destroy() {
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
        if (IsEnabled())
    		try {
    			Initialize();
    		}
    		catch (Exception E) {
        		Device.Log.WriteError("AlarmModule","initialization error: "+E.getMessage());
    		}
    }
    
    public void Initialize() throws Exception {
    	Alarmers_Build();
    }
    
    public void Alarmers_Build() throws Exception {
    	Alarmers.Clear();
    	//.
    	if (Profile.Exists()) {
        	Alarmers.FromXMLNode(Profile.TriggersNode);
        	//. TBatteryLevelTrigger
        	if (Device.BatteryModule != null) {
    			Object Alarmer = Alarmers.GetItem(TBatteryLevelTrigger.TLevelProfilableAlarmer.TriggerTypeID(), TBatteryLevelTrigger.TLevelProfilableAlarmer.TypeID);
    			if (Alarmer instanceof TBatteryLevelTrigger.TLevelProfilableAlarmer) 
        			Device.BatteryModule.SetBatteryLevelAlarmer((TBatteryLevelTrigger.TLevelProfilableAlarmer)Alarmer);
    			else {
        			Alarmer = Alarmers.GetItem(TBatteryLevelTrigger.TLevelAlarmer.TriggerTypeID(), TBatteryLevelTrigger.TLevelAlarmer.TypeID);
        			if (Alarmer instanceof TBatteryLevelTrigger.TLevelAlarmer)
            			Device.BatteryModule.SetBatteryLevelAlarmer((TAlarmer)Alarmer);
        			else
        				Device.BatteryModule.SetBatteryLevelAlarmer(null);
    			}
        	}
        	//. TCellularSignalTrigger
        	if ((Device.ConnectorModule != null) && (Device.ConnectorModule.ConnectorStateListener != null)) {
            	Object Alarmer = Alarmers.GetItem(TCellularSignalTrigger.TSignalProfilableAlarmer.TriggerTypeID(), TCellularSignalTrigger.TSignalProfilableAlarmer.TypeID);
    			if (Alarmer instanceof TCellularSignalTrigger.TSignalProfilableAlarmer)
    				Device.ConnectorModule.ConnectorStateListener.SetCellularSignalAlarmer((TAlarmer)Alarmer);
    			else {
                	Alarmer = Alarmers.GetItem(TCellularSignalTrigger.TSignalAlarmer.TriggerTypeID(), TCellularSignalTrigger.TSignalAlarmer.TypeID);
        			if (Alarmer instanceof TCellularSignalTrigger.TSignalAlarmer)
        				Device.ConnectorModule.ConnectorStateListener.SetCellularSignalAlarmer((TAlarmer)Alarmer);
        			else 
        				Device.ConnectorModule.ConnectorStateListener.SetCellularSignalAlarmer(null);
    			}
        	}
    	}
    	else {
        	if (Device.BatteryModule != null) {
        		TBatteryLevelTrigger.TLevelAlarmer BatteryLevelAlarmer  = new TBatteryLevelTrigger.TLevelAlarmer(this);
    			Alarmers.Add(BatteryLevelAlarmer);
    			Device.BatteryModule.SetBatteryLevelAlarmer(BatteryLevelAlarmer);
        	}
        	//.
        	if ((Device.ConnectorModule != null) && (Device.ConnectorModule.ConnectorStateListener != null)) {
        		TCellularSignalTrigger.TSignalAlarmer CellularSignalAlarmer  = new TCellularSignalTrigger.TSignalAlarmer(this);
    			Alarmers.Add(CellularSignalAlarmer);
    			Device.ConnectorModule.ConnectorStateListener.SetCellularSignalAlarmer(CellularSignalAlarmer);
        	}
    	}
    	//.
    	if ((Device.SensorsModule != null) && (Device.SensorsModule.InternalSensorsModule != null) && (Device.SensorsModule.InternalSensorsModule.Model != null)) {
    		TStreamDescriptor Stream = Device.SensorsModule.InternalSensorsModule.Model.Stream;
    		int CC = Stream.Channels.size();
    		for (int C = 0; C < CC; C++) {
    			TChannel Channel = Stream.Channels.get(C);
    			if (Channel.DataTypes != null) {
    				int DTC = Channel.DataTypes.Count();
    				for (int I = 0; I < DTC; I++) {
    					TDataType DataType = Channel.DataTypes.GetItemByIndex(I);
    					TDataType.TDataTriggers Triggers = DataType.Triggers_Get();
    					if (Triggers != null) {
    						ArrayList<TDataType.TDataTrigger> Items = Triggers.GetItems();
    						int TC = Items.size();
    						for (int T = 0; T < TC; T++) {
    							TDataType.TDataTrigger Trigger = Items.get(T);
    							//.
    							Object Alarmer;
    					    	if (Profile.Exists()) 
        							Alarmer = Alarmers.GetItem(Trigger.TypeID, Trigger.HandlerTypeID);
    					    	else { 
    					    		Alarmer = TChannelDataTypeAlarmer.GetAlarmer(this, Trigger.TypeID, Trigger.HandlerTypeID);
    								Alarmers.Add(Alarmer);
    					    	}
    					    	//.
    							if (Alarmer instanceof TChannelDataTypeAlarmer) 
    								Trigger.SetHandler((TChannelDataTypeAlarmer)Alarmer);
    						}
    					}
    				}
    			}
    		}
    		//. start InternalSensorsModule permanently
    		//. Device.SensorsModule.InternalSensorsModule.flStartOnDeviceStart = true;
    		//. Device.SensorsModule.InternalSensorsModule.Start();
    	}
    }
    
    private void Alarmers_CommitAlarmData() {
    	if (Device.ModuleState == MODULE_STATE_RUNNING)
    		Alarmers_SendAlarmData();
    	else
    		Alarmers_PostSendingAlarmData();
    }
    
    private void Alarmers_SendAlarmData() {
    	try {
    		double TS;
    		byte[] BA;
    		synchronized (this) {
            	TS = OleDate.UTCCurrentTimestamp();
            	BA = Alarmers.AlarmsToByteArray();
            	Thread.sleep(10); //. it allows UTCCurrentTimestamp() to change 
			}
    		//.
        	TAlarmDataValue AlarmDataValue = new TAlarmDataValue();
        	AlarmDataValue.SetValue(TS,BA);
            //.
            TObjectSetComponentDataServiceOperation SO = new TObjectSetAlarmDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
            SO.setValue(AlarmDataValue);
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
    	}
    	catch (Exception E) {
    		Device.Log.WriteError("AlarmModule","sending alarm data error: "+E.getMessage());
    	}
    }

    private void Alarmers_PostSendingAlarmData() {
		MessageHandler.obtainMessage(MESSAGE_ALARMERS_SENDALARMDATA).sendToTarget();
    }
    
    public static final int MESSAGE_ALARMERS_SENDALARMDATA = 1;
    
	public final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_ALARMERS_SENDALARMDATA:
                	if (Device.ModuleState == MODULE_STATE_RUNNING)
                		Alarmers_SendAlarmData();
                	break; //. >

                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };    
}
