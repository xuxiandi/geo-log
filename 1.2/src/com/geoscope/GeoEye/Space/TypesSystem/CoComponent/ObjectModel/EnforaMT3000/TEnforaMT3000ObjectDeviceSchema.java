package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.EnforaMT3000;

import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleThresholdsValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentDoubleValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt32Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt32Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TEnforaMT3000ObjectDeviceSchema extends TComponentSchema {

	public class TEnforaMT3000ObjectDeviceComponent extends TComponent {

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
	
		public class TBatteryModule extends TComponent
		{
			public TComponentTimestampedInt16Value		Voltage;
			public TComponentTimestampedInt16Value		Charge;
			public TComponentTimestampedBooleanValue	IsExternalPower;
			public TComponentTimestampedBooleanValue	IsLowPowerMode;
			
			public TBatteryModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"BatteryModule");
				//.
				Voltage			= new TComponentTimestampedInt16Value	(this,1,"Voltage");
				Charge			= new TComponentTimestampedInt16Value	(this,2,"Charge");
				IsExternalPower = new TComponentTimestampedBooleanValue	(this,3,"IsExternalPower");
				IsLowPowerMode	= new TComponentTimestampedBooleanValue	(this,4,"IsLowPowerMode");
			}
		}
	
		public class TConnectionModule extends TComponent
		{
			public class TServiceProvider extends TComponent
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
			
			public TServiceProvider						ServiceProvider;
			public TComponentInt16Value					CheckPointInterval;
			public TComponentDoubleValue 				LastCheckpointTime;
			public TComponentTimestampedBooleanValue	IsOnline;
			//. public TComponentTimestampedUInt16Data		SignalValue;
			
			public TConnectionModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ConnectionModule");
				//.
				ServiceProvider	=		new TServiceProvider					(this,1);	
				CheckPointInterval =	new TComponentInt16Value				(this,2,"CheckPointInterval");	
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
			
			public TComponentInt32Value					DatumID;
			public TComponentInt16Value					DistanceThreshold;
			public TGPSFixDataValue						GPSFixData;
			public TMapPOIComponent						MapPOI;
			public TFixMarkComponent					FixMark;
			public TComponentTimestampedBooleanValue	IsActive;
			
			public TGPSModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPSModule");
				//.
				DatumID 			= new TComponentInt32Value				(this,1,"DatumID");
				DistanceThreshold 	= new TComponentInt16Value				(this,2,"DistanceThreshold");	
				GPSFixData 			= new TGPSFixDataValue					(this,3,"GPSFixData");
				MapPOI				= new TMapPOIComponent					(this,4);
				FixMark				= new TFixMarkComponent					(this,5);
				IsActive			= new TComponentTimestampedBooleanValue	(this,6,"IsActive");
			}
		}
		
		public class TAccelerometerModule extends TComponent
		{
			public TComponentTimestampedDoubleValue		Value;
			public TComponentTimestampedDoubleThresholdsValue		Thresholds;
			public TComponentTimestampedBooleanValue	IsCalibrated;

			public TAccelerometerModule(TComponent pOwner, int pID) throws Exception
			{
				super(pOwner,pID,"AccelerometerModule");
				//.
				Value			= new TComponentTimestampedDoubleValue	(this,1,"Value");
				Thresholds		= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",6);
				IsCalibrated	= new TComponentTimestampedBooleanValue	(this,3,"IsCalibrated");
				//.
				double[] DefaultThresholds = new double[] {150,250,350, -380,-400,-500};
				Thresholds.SetThresholds(DefaultThresholds);
			}
		}

		public class TIgnitionModule extends TComponent
		{
			public TComponentTimestampedBooleanValue Value;

			public TIgnitionModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"IgnitionModule");
				//.
				Value = new TComponentTimestampedBooleanValue(this,1,"Value");
			}
		}

		public class TTowAlertModule extends TComponent
		{
			public TComponentTimestampedBooleanValue Value;

			public TTowAlertModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"TowAlertModule");
				//.
				Value = new TComponentTimestampedBooleanValue(this,1,"Value");
			}
		}

		public class TOBDIIModule extends TComponent
		{
			public class TStateModule extends TComponent
			{
				public TComponentTimestampedBooleanValue	IsPresented;
				public TComponentTimestampedInt16Value		Protocol;
				public TComponentTimestampedANSIStringValue	VIN;
				public TComponentTimestampedInt32Value		EnforaPKG;

				public TStateModule(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"StateModule");
					//.
					IsPresented = new TComponentTimestampedBooleanValue		(this,1,"IsPresented");
					Protocol	= new TComponentTimestampedInt16Value		(this,2,"Protocol");
					VIN			= new TComponentTimestampedANSIStringValue	(this,3,"VIN");
					EnforaPKG	= new TComponentTimestampedInt32Value		(this,4,"EnforaPKG");
				}
			}

			public class TBatteryModule extends TComponent
			{
				public TComponentTimestampedDoubleValue				Value;
				public TComponentTimestampedDoubleThresholdsValue	Thresholds;
				public TComponentTimestampedBooleanValue			IsLow;

				public TBatteryModule(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"BatteryModule");
					//.
					Value		= new TComponentTimestampedDoubleValue				(this,1,"Value");
					Thresholds	= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",1);
					IsLow		= new TComponentTimestampedBooleanValue 			(this,3,"IsLow");
				}
			}

			public class TFuelModule extends TComponent
			{
				public TComponentTimestampedDoubleValue				Value;
				public TComponentTimestampedDoubleThresholdsValue	Thresholds;
				public TComponentTimestampedBooleanValue			IsLow;

				public TFuelModule(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"FuelModule");
					//.
					Value		= new TComponentTimestampedDoubleValue				(this,1,"Value");
					Thresholds	= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",1);
					IsLow		= new TComponentTimestampedBooleanValue 			(this,3,"IsLow");
				}
			}

			public class TTachometerModule extends TComponent
			{
				public TComponentTimestampedInt32Value				Value;
				public TComponentTimestampedDoubleThresholdsValue	Thresholds;

				public TTachometerModule(TComponent pOwner, int pID) throws Exception
				{
					super(pOwner,pID,"TachometerModule");
					//.
					Value		= new TComponentTimestampedInt32Value				(this,1,"Value");
					Thresholds	= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",3);
					//.
					double[] DefaultThresholds = new double[] {1000,2000,3000};
					Thresholds.SetThresholds(DefaultThresholds);
				}
			}

			public class TSpeedometerModule extends TComponent
			{
				public TComponentTimestampedDoubleValue				Value;
				public TComponentTimestampedDoubleThresholdsValue	Thresholds;

				public TSpeedometerModule(TComponent pOwner, int pID) throws Exception
				{
					super(pOwner,pID,"SpeedometerModule");
					//.
					Value		= new TComponentTimestampedDoubleValue				(this,1,"Value");
					Thresholds	= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",3);
					//.
					double[] DefaultThresholds = new double[] {40,60,100};
					Thresholds.SetThresholds(DefaultThresholds);
				}
			}

			public class TMILAlertModule extends TComponent
			{
				public TComponentTimestampedANSIStringValue AlertCodes;

				public TMILAlertModule(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"MILAlertModule");
					//.
					AlertCodes = new TComponentTimestampedANSIStringValue(this,1,"AlertCodes");
				}
			}

			public class TOdometerModule extends TComponent
			{
				public TComponentTimestampedDoubleValue				Value;
				public TComponentTimestampedDoubleThresholdsValue	Thresholds;

				public TOdometerModule(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"OdometerModule");
					//.
					Value		= new TComponentTimestampedDoubleValue				(this,1,"Value");
					Thresholds	= new TComponentTimestampedDoubleThresholdsValue	(this,2,"Thresholds",1);
				}
			}

			public TStateModule			StateModule;
			public TBatteryModule		BatteryModule;
			public TFuelModule			FuelModule;
			public TTachometerModule	TachometerModule;
			public TSpeedometerModule	SpeedometerModule;
			public TMILAlertModule		MILAlertModule;
			public TOdometerModule		OdometerModule;

			public TOBDIIModule(TComponent pOwner, int pID) throws Exception
			{
				super(pOwner,pID,"OBDIIModule");
				//.
				StateModule			= new TStateModule			(this,1);
				BatteryModule		= new TBatteryModule		(this,2);
				FuelModule			= new TFuelModule			(this,3);
				TachometerModule	= new TTachometerModule		(this,4);
				SpeedometerModule	= new TSpeedometerModule	(this,5);
				MILAlertModule		= new TMILAlertModule		(this,6);
				OdometerModule		= new TOdometerModule		(this,7);
			}
		}
		
		public class TStatusModule extends TComponent
		{
			public TComponentTimestampedBooleanValue	IsStop;
			public TComponentTimestampedBooleanValue	IsIdle;
			public TComponentTimestampedBooleanValue	IsMotion;
			public TComponentTimestampedBooleanValue	IsMIL;

			public TStatusModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"StatusModule");
				//.
				IsStop		= new TComponentTimestampedBooleanValue	(this,1,"IsStop");
				IsIdle		= new TComponentTimestampedBooleanValue	(this,2,"IsIdle");
				IsMotion	= new TComponentTimestampedBooleanValue	(this,3,"IsMotion");
				IsMIL		= new TComponentTimestampedBooleanValue	(this,4,"IsMIL");
			}
		}

		public TDeviceDescriptor	DeviceDescriptor;
		public TControlModule		ControlModule;
		public TBatteryModule		BatteryModule;
		public TConnectionModule 	ConnectionModule;
		public TGPSModule 			GPSModule;
		public TAccelerometerModule	AccelerometerModule;
		public TIgnitionModule		IgnitionModule;
		public TTowAlertModule		TowAlertModule;
		public TOBDIIModule			OBDIIModule;
		public TStatusModule		StatusModule;
		
		public TEnforaMT3000ObjectDeviceComponent() throws Exception {
			super(null,2,"EnforaMT3000ObjectDeviceComponent");
			//.
			//. components
			DeviceDescriptor	= new TDeviceDescriptor		(this,1);
			ControlModule		= new TControlModule		(this,2);
			BatteryModule 		= new TBatteryModule		(this,3);
			ConnectionModule 	= new TConnectionModule		(this,4);
			GPSModule 			= new TGPSModule			(this,5);
			AccelerometerModule = new TAccelerometerModule  (this,6);
			IgnitionModule		= new TIgnitionModule		(this,7);
			TowAlertModule		= new TTowAlertModule		(this,8);
			OBDIIModule			= new TOBDIIModule			(this,9);
			StatusModule		= new TStatusModule			(this,10);
		}
	}
	
	public TEnforaMT3000ObjectDeviceSchema(TObjectModel pObjectModel) throws Exception {
		super(pObjectModel);
		RootComponent = new TEnforaMT3000ObjectDeviceComponent();
	}
}
