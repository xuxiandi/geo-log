package com.geoscope.GeoLog.DEVICE.AlarmModule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

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

	public interface IAlarmer {
		
		public boolean IsAlarm();
		public void ToXMLSerializer(XmlSerializer Serializer) throws Exception;
	}
	
	public static class TAlarmer implements IAlarmer {
		
		protected TAlarmModule AlarmModule;
		//.
		public int 			AlarmLevel = -1; //. unknown
		public double		AlarmTimestamp = 0.0;
		public String  		AlarmSeverity = "";
		public String  		AlarmID = "";
		public String  		AlarmValue = "";
		public String  		AlarmNotification = "";
		
		public TAlarmer(TAlarmModule pAlarmModule) {
			AlarmModule = pAlarmModule;
		}
		
		public synchronized void DoOnValue(Object Value) {
		}

		@Override
		public boolean IsAlarm() {
			return (AlarmLevel > 0);
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
	    	//. AlarmSeverity
	        Serializer.startTag("", "Severity");
	        Serializer.text(AlarmSeverity);
	        Serializer.endTag("", "Severity");
	    	//. AlarmValue
	        Serializer.startTag("", "Value");
	        Serializer.text(AlarmValue);
	        Serializer.endTag("", "Value");
	    	//. AlarmNotification
	        Serializer.startTag("", "Notification");
	        Serializer.text(AlarmNotification);
	        Serializer.endTag("", "Notification");
		}		
	}
	
	public static class TBatteryLevelTrigger {
		
		public static class TLevelAlarmer extends TAlarmer {
			
			public static final double AL1_ValueThreshold = 20.0; //. %
			public static final double AL2_ValueThreshold = 10.0; //. %
			public static final double AL3_ValueThreshold =  5.0; //. %
			
			
			public TLevelAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				Double V = (Double)Value;
				//.
				int _AlarmLevel = 0;
				if (V < AL3_ValueThreshold) 
					_AlarmLevel = 3;
				else
					if (V < AL2_ValueThreshold) 
						_AlarmLevel = 2;
					else
						if (V < AL1_ValueThreshold) 
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
	}
	
	public static class TCellularSignalTrigger {
		
		public static class TSignalAlarmer extends TAlarmer {
			
			public static final double AL1_ValueThreshold = 10.0; //. %
			public static final double AL2_ValueThreshold =  5.0; //. %
			
			
			public TSignalAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			public synchronized void DoOnValue(Object Value) {
				Double V = (Double)Value;
				//.
				int _AlarmLevel = 0;
				if (V < AL2_ValueThreshold) 
					_AlarmLevel = 2;
				else
					if (V < AL1_ValueThreshold) 
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
			return (AlarmLevel > 0);
		}
		
		@Override
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
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
	        Serializer.startTag("", "Value");
	        Serializer.text(AlarmDataType.GetValueAndUnitString(AlarmModule.Device.context));
	        Serializer.endTag("", "Value");
	    	//. AlarmNotification
	        Serializer.startTag("", "Notification");
	        Serializer.text(AlarmNotification);
	        Serializer.endTag("", "Notification");
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
			public static final Double AL1_ValueThreshold = 10.0;
			
			public static String GetTypeID() {
				return TypeID;
			}
			
			
			public TLSAlarmer(TAlarmModule pAlarmModule) {
				super(pAlarmModule);
			}
			
			@Override
			protected synchronized void DoOnValue(TDataType DataType) {
				Double V = (Double)DataType.GetContainerTypeValue();
				int _AlarmLevel = ((V < AL1_ValueThreshold) ? 1 : 0);
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
            	BA = Alarmers.ToByteArray();
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
