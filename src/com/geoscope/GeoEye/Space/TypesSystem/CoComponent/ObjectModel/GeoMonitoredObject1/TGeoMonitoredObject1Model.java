package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.BusinessModels.TGMO1GeoLogAndroidBusinessModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionUDPStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionUDPStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.Utils.OleDate;
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
			ControlModule_DoStartDeviceConnection1(Object, CUAL, ServerAddress,ServerPort, ConnectionID);
		}
	}
	
	public class TDeviceConnectionStopper extends TDeviceConnectionStopHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TDeviceConnectionStopper(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStopDeviceConnection(int ConnectionID) throws Exception {
			ControlModule_DoStopDeviceConnection1(Object,ConnectionID);
		}
	}

	public class TLANConnectionStarter extends TLANConnectionStartHandler {
	
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionStarter(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStartLANConnection(int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, int ConnectionID, String UserAccessKey) throws Exception { 
			ControlModule_DoStartLANConnection1(Object, ConnectionType, Address,Port, ServerAddress,ServerPort, ConnectionID, UserAccessKey);
		}
	}
	
	public class TLANConnectionStopper extends TLANConnectionStopHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionStopper(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStopLANConnection(int ConnectionID, String UserAccessKey) throws Exception {
			ControlModule_DoStopLANConnection1(Object,ConnectionID,UserAccessKey);
		}
	}

	public class TLANConnectionUDPStarter extends TLANConnectionUDPStartHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionUDPStarter(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public String DoStartLANConnection(int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, String DestinationUDPAddress, int DestinationUDPPort, int DestinationUDPProxyType, String AddressData, int ConnectionID, String UserAccessKey) throws Exception { 
			return ControlModule_DoStartLANConnectionUDP1(Object, ConnectionType, Address,Port, ServerAddress,ServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressData, ConnectionID, UserAccessKey);
		}
	}
	
	public class TLANConnectionUDPStopper extends TLANConnectionUDPStopHandler {
		
		private TReflectorCoGeoMonitorObject Object;
		
		public TLANConnectionUDPStopper(TReflectorCoGeoMonitorObject pObject) {
			Object = pObject;
		}
		
		@Override
		public void DoStopLANConnection(int ConnectionID, String UserAccessKey) throws Exception {
			ControlModule_DoStopLANConnectionUDP1(Object,ConnectionID,UserAccessKey);
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

	public TGeoMonitoredObject1Model(TGeographServerClient pObjectController, boolean pflFreeObjectController) throws Exception {
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
	
	@SuppressWarnings("unused")
	private void ControlModule_DoStartDeviceConnection(TReflectorCoGeoMonitorObject Object, String CUAL, String ServerAddress, int ServerPort, int ConnectionID) throws Exception {
		String Params = "107,"+"0,"/*Version*/+CUAL+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		try {
			Object.SetData(DataType, Data);
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error of starting device connection, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}
	}

	private void ControlModule_DoStartDeviceConnection1(TReflectorCoGeoMonitorObject Object, String CUAL, String ServerAddress, int ServerPort, int ConnectionID) throws Exception {
		String Params = "107,"+"0,"/*Version*/+CUAL+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of starting device connection"); //. =>
			}
		}
	}

	@SuppressWarnings("unused")
	private void ControlModule_DoStopDeviceConnection(TReflectorCoGeoMonitorObject Object, int ConnectionID) throws Exception {
		String Params = "108,"+Integer.toString(ConnectionID);
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	private void ControlModule_DoStopDeviceConnection1(TReflectorCoGeoMonitorObject Object, int ConnectionID) throws Exception {
		String Params = "108,"+Integer.toString(ConnectionID);
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of stopping device connection"); //. =>
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void ControlModule_DoStartLANConnection(TReflectorCoGeoMonitorObject Object, int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, int ConnectionID, String UserAccessKey) throws Exception {
		String Params = "";
		switch (ConnectionType) {
		
		case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
			if (UserAccessKey == null)
				Params = "101,"+"0"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
			else
				Params = "101,"+"2"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey;
			break; //. >

		case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
			if (UserAccessKey == null)
				Params = "101,"+"1"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
			else
				Params = "101,"+"3"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey;
			break; //. >
		}
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		try {
			Object.SetData(DataType, Data);
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error of starting LAN connection, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}
	}

	private void ControlModule_DoStartLANConnection1(TReflectorCoGeoMonitorObject Object, int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, int ConnectionID, String UserAccessKey) throws Exception {
		String Params = "";
		switch (ConnectionType) {
		
		case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
			if (UserAccessKey == null)
				Params = "101,"+"0"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
			else
				Params = "101,"+"2"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey;
			break; //. >

		case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
			if (UserAccessKey == null)
				Params = "101,"+"1"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout);
			else
				Params = "101,"+"3"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey;
			break; //. >
		}
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of starting LAN connection"); //. =>
			}
		}
	}

	@SuppressWarnings("unused")
	private void ControlModule_DoStopLANConnection(TReflectorCoGeoMonitorObject Object, int ConnectionID, String UserAccessKey) throws Exception {
		String Params;
		if (UserAccessKey == null)
			Params = "102,"+Integer.toString(ConnectionID);
		else
			Params = "102,"+Integer.toString(ConnectionID)+","+"1"/*Version*/+","+UserAccessKey;
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	private void ControlModule_DoStopLANConnection1(TReflectorCoGeoMonitorObject Object, int ConnectionID, String UserAccessKey) throws Exception {
		String Params;
		if (UserAccessKey == null)
			Params = "102,"+Integer.toString(ConnectionID);
		else
			Params = "102,"+Integer.toString(ConnectionID)+","+"1"/*Version*/+","+UserAccessKey;
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of stopping LAN connection"); //. =>
			}
		}
	}
	
	@SuppressWarnings("unused")
	private String ControlModule_DoStartLANConnectionUDP(TReflectorCoGeoMonitorObject Object, int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, String DestinationUDPAddress, int DestinationUDPPort, int DestinationUDPProxyType, String AddressData, int ConnectionID, String UserAccessKey) throws Exception {
		String Params = "";
		if (AddressData == null)
			AddressData = "0";
		switch (ConnectionType) {
		
		case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
			if (UserAccessKey == null)
				Params = "110,"+"0"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			else
				Params = "110,"+"2"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			break; //. >

		case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
			if (UserAccessKey == null)
				Params = "110,"+"1"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			else
				Params = "110,"+"3"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			break; //. >
		}
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		byte[] OutData;
		try {
			OutData = Object.SetGetData(DataType, Data);
			if (OutData != null)
				return new String(OutData,"US-ASCII"); //. ->
			else
				return null; //. ->
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error of starting LAN connection UDP, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}
	}

	private String ControlModule_DoStartLANConnectionUDP1(TReflectorCoGeoMonitorObject Object, int ConnectionType, String Address, int Port, String ServerAddress, int ServerPort, String DestinationUDPAddress, int DestinationUDPPort, int DestinationUDPProxyType, String AddressData, int ConnectionID, String UserAccessKey) throws Exception {
		String Params = "";
		if (AddressData == null)
			AddressData = "0";
		switch (ConnectionType) {
		
		case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
			if (UserAccessKey == null)
				Params = "110,"+"0"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			else
				Params = "110,"+"2"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			break; //. >

		case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
			if (UserAccessKey == null)
				Params = "110,"+"1"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			else
				Params = "110,"+"3"/*Version*/+","+Address+","+Integer.toString(Port)+","+ServerAddress+","+Integer.toString(ServerPort)+","+Integer.toString(ConnectionID)+","+Integer.toString(LANConnectionTimeout)+","+UserAccessKey+","+DestinationUDPAddress+","+Integer.toString(DestinationUDPPort)+","+Integer.toString(DestinationUDPProxyType)+","+AddressData;
			break; //. >
		}
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		byte[] OutData;
		try {
			OutData = Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
			if (OutData != null) {
				TComponentTimestampedDataValue TimestampedDataValue = new TComponentTimestampedDataValue();
				TIndex Idx = new TIndex(0);
				TimestampedDataValue.FromByteArray(OutData,Idx);
				if (TimestampedDataValue.Value != null)
					return new String(TimestampedDataValue.Value,"US-ASCII"); //. ->
				else
					return null; //. ->
			}
			else
				return null; //. ->
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of starting LAN connection UDP"); //. =>
			}
		}
	}

	@SuppressWarnings("unused")
	private void ControlModule_DoStopLANConnectionUDP(TReflectorCoGeoMonitorObject Object, int ConnectionID, String UserAccessKey) throws Exception {
		String Params;
		if (UserAccessKey == null)
			Params = "111,"+Integer.toString(ConnectionID);
		else
			Params = "111,"+Integer.toString(ConnectionID)+","+"1"/*Version*/+","+UserAccessKey;
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		Object.SetData(DataType, Data);
	}
	
	private void ControlModule_DoStopLANConnectionUDP1(TReflectorCoGeoMonitorObject Object, int ConnectionID, String UserAccessKey) throws Exception {
		String Params;
		if (UserAccessKey == null)
			Params = "111,"+Integer.toString(ConnectionID);
		else
			Params = "111,"+Integer.toString(ConnectionID)+","+"1"/*Version*/+","+UserAccessKey;
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new OperationException(OE.Code,"error of stopping LAN connection UDP"); //. =>
			}
		}
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
	
	public TLANConnectionUDPStartHandler TLANConnectionUDPStartHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionUDPStarter(Object);
	}

	public TLANConnectionUDPStopHandler TLANConnectionUDPStopHandler_Create(TReflectorCoGeoMonitorObject Object) {
		return new TLANConnectionUDPStopper(Object);
	}
	
	@SuppressWarnings("unused")
	private TVideoRecorderMeasurementDescriptor[] VideoRecorder_Measurements_GetList(TReflectorCoGeoMonitorObject Object) throws IOException, Exception {
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*VideoRecorderModule.MeasurementsListValue.ReadDeviceCUAC()*/;
		byte[] Data;
		try {
			Data = Object.GetData(DataType);
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}		
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
	
	public TVideoRecorderMeasurementDescriptor[] VideoRecorder_Measurements_GetList1(TReflectorCoGeoMonitorObject Object) throws IOException, Exception {
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,9,1100});
		TComponentTimestampedANSIStringValue Value = new TComponentTimestampedANSIStringValue();
		try {
			byte[] Data = Object.GeographServerClient().Component_ReadDeviceCUAC(_Address);
			Value.FromByteArray(Data,(new TIndex(0)));
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new Exception("error VideoRecorder_Measurements_GetList(), RC: "+Integer.toString(OE.Code)); //. =>
			}
		}
		//.
		TVideoRecorderMeasurementDescriptor[] Result;
		if (Value.Value != null) {
			String[] Items = Value.Value.split(";");
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
	
	@SuppressWarnings("unused")
	private void VideoRecorder_Measurements_Delete(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "1,"+MeasurementIDs; //. delete command
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+9/*VideoRecorderModule.MeasurementDataValue.WriteDeviceByAddressDataCUAC(AddressData)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		try {
			Object.SetData(DataType, Data);
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}
	}
	
	public void VideoRecorder_Measurements_Delete1(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "1,"+MeasurementIDs; //. delete command
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,9,1101});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			TComponentTimestampedDataValue V = new TComponentTimestampedDataValue();
			V.Timestamp = OleDate.UTCCurrentTimestamp();
			V.Value = null;
			Object.GeographServerClient().Component_WriteDeviceByAddressDataCUAC(_Address,_AddressData, V.ToByteArray());
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new Exception("error VideoRecorder_Measurements_Delete1(), RC: "+Integer.toString(OE.Code)); //. =>
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void VideoRecorder_Measurements_MoveToDataServer(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "2,"+MeasurementIDs; //. move command
		int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+9/*VideoRecorderModule.MeasurementDataValue.WriteDeviceByAddressDataCUAC(AddressData)*/;
		byte[] Data = Params.getBytes("US-ASCII");
		try {
			Object.SetData(DataType, Data);
		}
		catch (Exception E) {
			String ES = E.getMessage();
			String RCPrefix = "RC: ";
			int RCP = ES.indexOf(RCPrefix);
			if (RCP >= 0) {
				String RCS  = ES.substring(RCP+RCPrefix.length());
				int RC = Integer.parseInt(RCS);
				switch (RC) {

				case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
					throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

				default:
					throw new Exception("error, RC: "+Integer.toString(RC)); //. =>
				}
			}
			else
				throw E; //. =>
		}
	}

	public void VideoRecorder_Measurements_MoveToDataServer1(TReflectorCoGeoMonitorObject Object, String MeasurementIDs) throws IOException, Exception {
		String Params = "2,"+MeasurementIDs; //. move command
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,9,1101});
		byte[] _AddressData = Params.getBytes("US-ASCII");
		try {
			TComponentTimestampedDataValue V = new TComponentTimestampedDataValue();
			V.Timestamp = OleDate.UTCCurrentTimestamp();
			V.Value = null;
			Object.GeographServerClient().Component_WriteDeviceByAddressDataCUAC(_Address,_AddressData, V.ToByteArray());
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied:
				throw new Exception(Object.Server.context.getString(R.string.SUserAccessIsDenied)); //. =>

			default:
				throw new Exception("error VideoRecorder_Measurements_MoveToDataServer1(), RC: "+Integer.toString(OE.Code)); //. =>
			}
		}
	}
}
