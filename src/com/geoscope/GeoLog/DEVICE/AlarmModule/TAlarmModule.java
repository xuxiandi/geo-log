package com.geoscope.GeoLog.DEVICE.AlarmModule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetAlarmDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TAlarmModule extends TModule {

	public interface IAlarmer {
		
		public boolean IsAlarm();
		public void ToXMLSerializer(XmlSerializer Serializer) throws Exception;
	}
	
	public static class TAlarmer implements IAlarmer {
		
		protected TAlarmModule AlarmModule;
		//.
		public boolean 		flAlarm = false;
		public double		AlarmTimestamp = 0.0;
		public String  		AlarmID = "";
		public String  		AlarmValue = "";
		
		public TAlarmer(TAlarmModule pAlarmModule) {
			AlarmModule = pAlarmModule;
		}
		
		public synchronized void DoOnValue(Object Value) {
		}

		@Override
		public boolean IsAlarm() {
			return flAlarm;
		}
		
		@Override
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. AlarmTimestamp
	        Serializer.startTag("", "Timestamp");
	        Serializer.text(Double.toString(AlarmTimestamp));
	        Serializer.endTag("", "Timestamp");
	    	//. AlarmID
	        Serializer.startTag("", "ID");
	        Serializer.text(AlarmID);
	        Serializer.endTag("", "ID");
	    	//. AlarmValue
	        Serializer.startTag("", "Value");
	        Serializer.text(AlarmValue);
	        Serializer.endTag("", "Value");
		}		
	}
	
	public static class TBatteryLevelTrigger {
		
		public static class TLevelAlarmer extends TAlarmer {
			
			public static final double ValueThreshold = 70.0; //. %
			
			
			public TLevelAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				Double V = (Double)Value;
				boolean _flAlarm = (V < ValueThreshold);
				if (_flAlarm != flAlarm) {
					flAlarm = _flAlarm;
					if (flAlarm) {
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmID = "BatteryLevelLow";
						AlarmValue = Double.toString(V)+"%";
					}
					//. update result AlarmData
					AlarmModule.Alarmers_SendAlarmData();
				}
			}
		}
	}
	
	public static class TCellularSignalTrigger {
		
		public static class TSignalAlarmer extends TAlarmer {
			
			public static final double ValueThreshold = 50.0; //. %
			
			
			public TSignalAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				Double V = (Double)Value;
				boolean _flAlarm = (V < ValueThreshold);
				if (_flAlarm != flAlarm) {
					flAlarm = _flAlarm;
					if (flAlarm) {
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmID = "CellularSignalLow";
						AlarmValue = Double.toString(V)+" %";
					}
					//. update result AlarmData
					AlarmModule.Alarmers_SendAlarmData();
				}
			}
		}
	}
	
	private static class TChannelDataTypeAlarmer extends TDataType.TDataTrigger.TAlarmer implements IAlarmer {
	
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String TriggerTypeID, String HandlerTypeID) {
			if (TLightSensorDataTypeTrigger.TypeID.equals(TriggerTypeID))
				return TLightSensorDataTypeTrigger.GetAlarmer(AlarmModule, HandlerTypeID); //. =>
			else 
				return null; //. ->
		}
		
		protected TAlarmModule AlarmModule;
		
		public TChannelDataTypeAlarmer(TAlarmModule pAlarmModule) {
			AlarmModule = pAlarmModule;
		}

		@Override
		public boolean IsAlarm() {
			return flAlarm;
		}
		
		@Override
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. AlarmTimestamp
	        Serializer.startTag("", "Timestamp");
	        Serializer.text(Double.toString(AlarmTimestamp));
	        Serializer.endTag("", "Timestamp");
	    	//. AlarmID
	        Serializer.startTag("", "ID");
	        Serializer.text(AlarmID);
	        Serializer.endTag("", "ID");
	    	//. AlarmValue
	        Serializer.startTag("", "Value");
	        Serializer.text(AlarmDataType.GetValueAndUnitString(AlarmModule.Device.context));
	        Serializer.endTag("", "Value");
	    	//. AlarmSourceChannelID
	        Serializer.startTag("", "ChannelID");
	        Serializer.text(AlarmDataType.Channel.GetTypeID());
	        Serializer.endTag("", "ChannelID");
	    	//. AlarmDataTypeID
	        Serializer.startTag("", "DataTypeID");
	        Serializer.text(AlarmDataType.TypeID);
	        Serializer.endTag("", "DataTypeID");
		}		
	}
	
	private static class TLightSensorDataTypeTrigger {
		
		public static String TypeID = "LightSensorDarkness";
		
		public static TChannelDataTypeAlarmer GetAlarmer(TAlarmModule AlarmModule, String HadlerTypeID) {
			if (TLSAlarmer.TypeID.equals(HadlerTypeID))
				return (new TLSAlarmer(AlarmModule)); //. ->
			else
				return null; //. ->
		}
		
		public static class TLSAlarmer extends TChannelDataTypeAlarmer {

			public static String TypeID = "Default";
			//.
			public static final Double ValueThreshold = 10.0;
			
			public static String GetTypeID() {
				return TypeID;
			}
			
			
			public TLSAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			protected synchronized void DoOnValue(TDataType DataType) {
				Double V = (Double)DataType.GetContainerTypeValue();
				boolean _flAlarm = (V < ValueThreshold);
				if (_flAlarm != flAlarm) {
					flAlarm = _flAlarm;
					if (flAlarm) {
						AlarmTimestamp = OleDate.UTCCurrentTimestamp();
						AlarmID = "LightLevelDark";
						AlarmDataType = DataType.Clone();
					}
					//. update result AlarmData
					AlarmModule.Alarmers_SendAlarmData();
				}
			}
		}
	}

	private static class TAlarmers {
		
		private ArrayList<Object> Items = new ArrayList<Object>();
		
		public TAlarmers() {
		}
		
		public void Add(Object Item) {
			Items.add(Item);
		}
		
		public void Clear() {
			Items.clear();
		}

	    public byte[] ToByteArray() throws Exception {
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
		        //. 
		        if (!ToXMLSerializer(Serializer))
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
	    
		public synchronized boolean ToXMLSerializer(XmlSerializer Serializer) throws Exception {
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
			            Alarmer.ToXMLSerializer(Serializer);
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
	}
	
	
	private TAlarmers Alarmers = new TAlarmers();
	
    public TAlarmModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
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
    
    private void Alarmers_Build() throws Exception {
    	Alarmers.Clear();
    	//.
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
    							TChannelDataTypeAlarmer Alarmer = TChannelDataTypeAlarmer.GetAlarmer(this, Trigger.TypeID, Trigger.HandlerTypeID);
    							if (Alarmer != null) {
    								Alarmers.Add(Alarmer);
    								Trigger.SetHandler(Alarmer);
    							}
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
    
    private void Alarmers_SendAlarmData() {
    	try {
        	TAlarmDataValue AlarmDataValue = new TAlarmDataValue();
        	AlarmDataValue.SetValue(OleDate.UTCCurrentTimestamp(),Alarmers.ToByteArray());
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
}
