package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TGEOGraphServerObjectController;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels.TGMO1GeoLogAndroidBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;
import com.geoscope.Utils.TDataConverter;

public class TGeoMonitoredObject1Model extends TObjectModel
{
	public static final int 	ID = 101;
	public static final String	Name = "Geo.Log";
	
	public static final int LANConnectionTimeout = 1000*30; //. seconds

	public class TDeviceConnectionStarter extends TDeviceConnectionStartHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TDeviceConnectionStarter(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStartDeviceConnection(String CUAL, String ServerAddress, int ServerPort, int ConnectionID) throws Exception { 
			ControlModule_DoStartDeviceConnection(Object, CUAL, ServerAddress,ServerPort, ConnectionID);
		}
	}
	
	public class TDeviceConnectionStopper extends TDeviceConnectionStopHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TDeviceConnectionStopper(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStopDeviceConnection(int ConnectionID) throws Exception {
			ControlModule_DoStopDeviceConnection(Object,ConnectionID);
		}
	}

	public class TLANConnectionStarter extends TLANConnectionStartHandler {
	
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionStarter(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStartLANConnection(int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, int ConnectionID) throws Exception { 
			ControlModule_DoStartLANConnection(Object, ConnectionType, Address,Port, ServerAddress,ServerPort, ConnectionID);
		}
	}
	
	public class TLANConnectionStopper extends TLANConnectionStopHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionStopper(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStopLANConnection(int ConnectionID) throws Exception {
			ControlModule_DoStopLANConnection(Object,ConnectionID);
		}
	}

	public static class TVideoRecorderMeasurementDescriptor {
		public String ID;
		public double StartTimestamp;
		public double FinishTimestamp;
		public int AudioSize;
		public int VideoSize;
	}
	
	public TGeoMonitoredObject1Model() throws Exception {
		super();
	}

	public TGeoMonitoredObject1Model(TGEOGraphServerObjectController pObjectController, boolean pflFreeObjectController) throws Exception {
		super(pObjectController,pflFreeObjectController);
	}

	@Override
	protected void CreateSchemas() throws Exception {
		ObjectSchema = new TGeoMonitoredObject1Schema(this);
		ObjectDeviceSchema = new TGeoMonitoredObject1DeviceSchema(this);
	}
	
	@Override
	public boolean SetBusinessModel(int BusinessModelID) {
		boolean Result = super.SetBusinessModel(BusinessModelID);
		switch (BusinessModelID)
		{
			case TGMO1GeoLogAndroidBusinessModel.ID: 
				BusinessModel = new TGMO1GeoLogAndroidBusinessModel(this);
				return true; //. ->

			default:
				return Result; //. ->
		}
	}
	
	@Override
	public int GetID() {
		return ID;
	}
	
	@Override
	public String GetName() {
		return Name;
	}
	
	public void ControlModule_DoStartDeviceConnection(TReflectorCoGeoMonitorObject Object, String CUAL, String ServerAddress, int ServerPort, int ConnectionID) throws Exception {
		String Params = "107,"+"0,"/*Version*/+CUAL+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}

	public void ControlModule_DoStopDeviceConnection(TReflectorCoGeoMonitorObject Object, int ConnectionID) throws Exception {
		String Params = "108,"+Integer.toString(ConnectionID);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	public void ControlModule_DoStartLANConnection(TReflectorCoGeoMonitorObject Object, int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, int ConnectionID) throws Exception {
		String Params = "101,"+Integer.toString(ConnectionType)+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}

	public void ControlModule_DoStopLANConnection(TReflectorCoGeoMonitorObject Object, int ConnectionID) throws Exception {
		String Params = "102,"+Integer.toString(ConnectionID);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	public TDeviceConnectionStartHandler TDeviceConnectionStartHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TDeviceConnectionStarter(Object);
	}

	public TDeviceConnectionStopHandler TDeviceConnectionStopHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TDeviceConnectionStopper(Object);
	}
	
	public TLANConnectionStartHandler TLANConnectionStartHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionStarter(Object);
	}

	public TLANConnectionStopHandler TLANConnectionStopHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionStopper(Object);
	}
	
	public TVideoRecorderMeasurementDescriptor[] VideoRecorder_Measurements_GetList(TReflectorCoGeoMonitorObject Object) throws IOException, Exception {
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*VideoRecorderModule.MeasurementsListValue.ReadDeviceCUAC()*/;
		byte[] Data = Object.GetData(DataType);
		int Idx = 0;
		int DataSize = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4/*SizeOf(DataSize)*/;
		TVideoRecorderMeasurementDescriptor[] Result;
		if (DataSize > 0) {
			String ResultString = new String(Data,Idx,DataSize,"US-ASCII");
			String[] Items = ResultString.split(";");
			Result = new TVideoRecorderMeasurementDescriptor[Items.length];
			for (int I = 0; I < Items.length; I++) {
				String[] Properties = Items[I].split(",");
				Result[I] = new TVideoRecorderMeasurementDescriptor();
				Result[I].ID = Properties[0];
				Result[I].StartTimestamp = Double.parseDouble(Properties[1]);
				Result[I].FinishTimestamp = Double.parseDouble(Properties[2]);
				Result[I].AudioSize = Integer.parseInt(Properties[3]);
				Result[I].VideoSize = Integer.parseInt(Properties[4]);
			}
		}
		else
			Result = new TVideoRecorderMeasurementDescriptor[0];
		return Result;
	}
	
	public void VideoRecorder_Measurements_Delete(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "1,"+MeasurementIDs; //. delete command
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+9/*VideoRecorderModule.MeasurementDataValue.WriteDeviceByAddressDataCUAC(AddressData)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	public void VideoRecorder_Measurements_MoveToDataServer(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "2,"+MeasurementIDs; //. move command
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+9/*VideoRecorderModule.MeasurementDataValue.WriteDeviceByAddressDataCUAC(AddressData)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
}
