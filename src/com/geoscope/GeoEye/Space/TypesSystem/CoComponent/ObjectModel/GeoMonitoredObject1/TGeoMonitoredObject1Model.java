package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels.TGMO1GeoLogAndroidBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;

public class TGeoMonitoredObject1Model extends TObjectModel
{
	public static final int 	ID = 101;
	public static final String	Name = "Geo.Log";
	
	public static final int LANConnectionTimeout = 1000*30; //. seconds

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
	
	public TGeoMonitoredObject1Model() throws Exception {
		super();
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
	
	public TLANConnectionStartHandler TLANConnectionStartHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionStarter(Object);
	}

	public TLANConnectionStopHandler TLANConnectionStopHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionStopper(Object);
	}
}
