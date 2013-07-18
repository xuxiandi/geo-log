package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject;

import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentDoubleArrayValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentDoubleValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt32Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TGeoMonitoredObjectDeviceSchema extends TComponentSchema {

	public class TGeoMonitoredObjectDeviceComponent extends TComponent {

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
		
		public class TBatteryModule extends TComponent
		{
			public TComponentInt16Value	Voltage;
			public TComponentInt16Value	Charge;
			
			public TBatteryModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"BatteryModule");
				//.
				Voltage = new TComponentInt16Value(this,1,"Voltage");
				Charge	= new TComponentInt16Value(this,2,"Charge");
			}
		}
	
		public class TConnectionModule extends TComponent
		{
			public class TServiceProvider extends TComponent
			{
				public TComponentInt16Value				ProviderID;
				public TComponentDoubleValue			Number;
				public TComponentInt16Value	Account;
				public TComponentInt16Value	Signal;

				public TServiceProvider(TComponent pOwner, int pID)
				{
					super(pOwner,pID,"ServiceProvider");
					//.
					ProviderID	= new TComponentInt16Value	(this,1,"ProviderID");			
					Number		= new TComponentDoubleValue	(this,2,"Number");				
					Account		= new TComponentInt16Value	(this,3,"Account");	
					Signal		= new TComponentInt16Value	(this,4,"Signal");	
				}
			}
			
			public TServiceProvider						ServiceProvider;
			public TComponentInt16Value					CheckPointInterval;
			public TComponentDoubleValue 				LastCheckpointTime;
			public TComponentBooleanValue				IsOnline;
			//. public TComponentUInt16Data				SignalValue;
			
			public TConnectionModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ConnectionModule");
				//.
				ServiceProvider	=		new TServiceProvider		(this,1);	
				CheckPointInterval =	new TComponentInt16Value	(this,2,"CheckPointInterval");	
				LastCheckpointTime =	new TComponentDoubleValue	(this,3,"LastCheckpointTime");		
				IsOnline =				new TComponentBooleanValue	(this,4,"IsOnline");				
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
			
			public TComponentInt32Value				DatumID;
			public TComponentInt16Value				DistanceThreshold;
			public TGPSFixDataValue					GPSFixData;
			public TMapPOIComponent					MapPOI;
			public TFixMarkComponent				FixMark;
			
			public TGPSModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPSModule");
				//.
				DatumID 			= new TComponentInt32Value				(this,1,"DatumID");
				GPSFixData 			= new TGPSFixDataValue					(this,2,"GPSFixData");
				DistanceThreshold 	= new TComponentInt16Value				(this,3,"DistanceThreshold");	
				MapPOI				= new TMapPOIComponent					(this,4);
				FixMark				= new TFixMarkComponent					(this,5);
			}
			
			public boolean FixIsAvailable() {
				return GPSFixData.IsAvailable();
			}
		}
		
		public class TGPIModule extends TComponent
		{
			public TComponentInt16Value	Value;
			
			public TGPIModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPIModule");
				//.
				Value = new TComponentInt16Value(this,1,"Value");
			}
		}
	
		public class TGPOModule extends TComponent
		{
			public TComponentInt16Value	Value;
			
			public TGPOModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"GPOModule");
				//.
				Value = new TComponentInt16Value(this,1,"Value");
			}
		}
	
		public class TADCModule extends TComponent
		{
			public static final int ValueSize = 16;

			public TComponentDoubleArrayValue	Value;
			
			public TADCModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"ADCModule");
				//.
				Value = new TComponentDoubleArrayValue(this,1,"Value",ValueSize);
			}
		}
	
		public class TDACModule extends TComponent
		{
			public static final int ValueSize = 16;

			public TComponentDoubleArrayValue	Value;
			
			public TDACModule(TComponent pOwner, int pID)
			{
				super(pOwner,pID,"DACModule");
				//.
				Value = new TComponentDoubleArrayValue(this,1,"Value",ValueSize);
			}
		}
	
		public TConnectionModule 	ConnectionModule;
		public TGPSModule 			GPSModule;
		public TGPIModule			GPIModule;
		public TGPOModule			GPOModule;
		public TADCModule			ADCModule;
		public TDACModule			DACModule;
		public TBatteryModule		BatteryModule;
		public TDeviceDescriptor	DeviceDescriptor;
		
		public TGeoMonitoredObjectDeviceComponent() throws Exception {
			super(TGeoMonitoredObjectDeviceSchema.this,2,"GeoMonitoredObjectDeviceComponent");
			//.
			//. components
			ConnectionModule 	= new TConnectionModule		(this,1);
			GPSModule 			= new TGPSModule			(this,2);
			GPIModule 			= new TGPIModule			(this,3);
			GPOModule 			= new TGPOModule			(this,4);
			ADCModule			= new TADCModule			(this,5);
			DACModule			= new TDACModule			(this,6);
			BatteryModule 		= new TBatteryModule		(this,7);
			DeviceDescriptor	= new TDeviceDescriptor		(this,8);
		}
	}
	
	public TGeoMonitoredObjectDeviceSchema(TObjectModel pObjectModel) throws Exception {
		super(pObjectModel);
		RootComponent = new TGeoMonitoredObjectDeviceComponent();
	}
}
