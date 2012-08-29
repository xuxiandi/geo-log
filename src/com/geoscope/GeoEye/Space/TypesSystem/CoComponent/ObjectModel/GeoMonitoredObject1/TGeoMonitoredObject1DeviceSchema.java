package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentDoubleValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt32Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentShortValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleArrayValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedShortValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TGeoMonitoredObject1DeviceSchema extends TComponentSchema {

	public class TGeoMonitoredObject1DeviceComponent extends TComponent {

		public class TDeviceDescriptor extends TComponent
		{
			public TComponentInt32Value			Vendor;
			public TComponentInt32Value			Model;
			public TComponentInt32Value			SerialNumber;
			public TComponentDoubleValue		ProductionDate;
			public TComponentInt32Value			HWVersion;
			public TComponentInt32Value			SWVersion;
			public TComponentANSIStringValue	FOTA;
			
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
				FOTA			= new TComponentANSIStringValue	(this,7,"FOTA");
			}
		}
		
		public class TBatteryModule extends TComponent
		{
			public TComponentTimestampedShortValue	Voltage;
			public TComponentTimestampedShortValue	Charge;
			
			public TBatteryModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"BatteryModule");
				//.
				Voltage = new TComponentTimestampedShortValue(this,1,"Voltage");
				Charge	= new TComponentTimestampedShortValue(this,2,"Charge");
			}
		}
	
		public class TConnectionModule extends TComponent
		{
			public class TServiceProvider extends TComponent
			{
				public TComponentShortValue				ProviderID;
				public TComponentDoubleValue			Number;
				public TComponentTimestampedShortValue	Account;
				public TComponentTimestampedShortValue	Signal;

				public TServiceProvider(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"ServiceProvider");
					//.
					ProviderID	= new TComponentShortValue				(this,1,"ProviderID");			
					Number		= new TComponentDoubleValue				(this,2,"Number");				
					Account		= new TComponentTimestampedShortValue	(this,3,"Account");	
					Signal		= new TComponentTimestampedShortValue	(this,4,"Signal");	
				}
			}
			
			public TServiceProvider						ServiceProvider;
			public TComponentShortValue					CheckPointInterval;
			public TComponentDoubleValue 				LastCheckpointTime;
			public TComponentTimestampedBooleanValue	IsOnline;
			//. public TComponentTimestampedUInt16Data		SignalValue;
			
			public TConnectionModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ConnectionModule");
				//.
				ServiceProvider	=		new TServiceProvider					(this,1);	
				CheckPointInterval =	new TComponentShortValue				(this,2,"CheckPointInterval");	
				LastCheckpointTime =	new TComponentDoubleValue				(this,3,"LastCheckpointTime");		
				IsOnline =				new TComponentTimestampedBooleanValue	(this,4,"IsOnline");				
			}
		}
	
		public class TGPSModule extends TComponent
		{
			public static final double FixIsNotAvailablePrecision = 1000000000.0;

			public class TGPSFixDataValue extends TComponentValue
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
			}
			
			public class TMapPOIComponent extends TComponent
			{

				public class TMapPOIDataValue extends TComponentValue
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
			
			public class TFixMarkComponent extends TComponent
			{

				public class TFixMarkDataValue extends TComponentValue
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
			
			public TComponentTimestampedShortValue	Mode;
			public TComponentTimestampedShortValue	Status;
			public TComponentInt32Value				DatumID;
			public TComponentShortValue				DistanceThreshold;
			public TGPSFixDataValue					GPSFixData;
			public TMapPOIComponent					MapPOI;
			public TFixMarkComponent				FixMark;
			
			public TGPSModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPSModule");
				//.
				Mode				= new TComponentTimestampedShortValue	(this,1,"Mode");
				Status				= new TComponentTimestampedShortValue	(this,2,"Status");
				DatumID 			= new TComponentInt32Value				(this,3,"DatumID");
				DistanceThreshold 	= new TComponentShortValue				(this,2,"DistanceThreshold");	
				GPSFixData 			= new TGPSFixDataValue					(this,5,"GPSFixData");
				MapPOI				= new TMapPOIComponent					(this,6);
				FixMark				= new TFixMarkComponent					(this,7);
			}
		}
		
		public class TGPIModule extends TComponent
		{
			public TComponentTimestampedShortValue	Value;
			
			public TGPIModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPIModule");
				//.
				Value = new TComponentTimestampedShortValue(this,1,"Value");
			}
		}
	
		public class TGPOModule extends TComponent
		{
			public TComponentTimestampedShortValue	Value;
			
			public TGPOModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPOModule");
				//.
				Value = new TComponentTimestampedShortValue(this,1,"Value");
			}
		}
	
		public class TADCModule extends TComponent
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
	
		public class TDACModule extends TComponent
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
	
		public class TVideoRecorderModule extends TComponent
		{
			public TComponentTimestampedShortValue		Mode;
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
				Mode			= new TComponentTimestampedShortValue		(this,1,"Mode");				
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
	
		public class TFileSystemModule extends TComponent
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
	
		public class TControlModule extends TComponent
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
	
		public TDeviceDescriptor	DeviceDescriptor;
		public TBatteryModule		BatteryModule;
		public TConnectionModule 	ConnectionModule;
		public TGPSModule 			GPSModule;
		public TGPIModule			GPIModule;
		public TGPOModule			GPOModule;
		public TADCModule			ADCModule;
		public TDACModule			DACModule;
		public TVideoRecorderModule	VideoRecorderModule;
		public TFileSystemModule	FileSystemModule;
		public TControlModule		ControlModule;
		
		public TGeoMonitoredObject1DeviceComponent() throws Exception {
			super(null,2,"GeoMonitoredObjectDeviceComponent");
			//.
			//. components
			DeviceDescriptor	= new TDeviceDescriptor		(this,1);
			BatteryModule 		= new TBatteryModule		(this,2);
			ConnectionModule 	= new TConnectionModule		(this,3);
			GPSModule 			= new TGPSModule			(this,4);
			GPIModule 			= new TGPIModule			(this,5);
			GPOModule 			= new TGPOModule			(this,6);
			ADCModule			= new TADCModule			(this,7);
			DACModule			= new TDACModule			(this,8);
			VideoRecorderModule	= new TVideoRecorderModule	(this,9);
			FileSystemModule	= new TFileSystemModule		(this,10);
			ControlModule		= new TControlModule		(this,11);
		}
	}
	
	public TGeoMonitoredObject1DeviceSchema(TObjectModel pObjectModel) throws Exception {
		super(pObjectModel);
		RootComponent = new TGeoMonitoredObject1DeviceComponent();
	}
}
