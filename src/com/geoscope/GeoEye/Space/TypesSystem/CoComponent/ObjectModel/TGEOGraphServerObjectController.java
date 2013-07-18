package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TMessage;
import com.geoscope.Utils.TDataConverter;

public class TGEOGraphServerObjectController {

	public static final int ConnectionTimeout = 1000*5; //. seconds
	
	private int ObjectID;
	private int 	UserID;
	private String 	UserPassword;
	private String 	ServerAddress;
	private int 	ServerPort;
	//.
	private Socket 			Connection = null;
	private InputStream 	ConnectionInputStream;
	private OutputStream 	ConnectionOutputStream; 
	//.
	private short NextOperationSession = 1;
	
	public TGEOGraphServerObjectController(int pObjectID, int pUserID, String pUserPassword, String pServerAddress, int pServerPort) throws IOException {
		ObjectID = pObjectID;
		UserID = pUserID;
		UserPassword = pUserPassword;
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
		//.
		Connect();
	}
	
	public void Destroy() throws IOException {
		Disconnect();
	}

	private void Connect() throws IOException {
		Connection = new Socket(ServerAddress,ServerPort); 
		Connection.setSoTimeout(ConnectionTimeout);
		Connection.setKeepAlive(true);
		Connection.setSendBufferSize(8192);
		ConnectionInputStream = Connection.getInputStream();
		ConnectionOutputStream = Connection.getOutputStream();
	}
	
	private void Disconnect() throws IOException {
		if (Connection != null) {
			ConnectionInputStream.close();
			ConnectionOutputStream.close();
			Connection.close();
			Connection = null;
		}
	}
	
	private short GetOperationSession() {
		short Result = NextOperationSession;
		NextOperationSession++;
		if (NextOperationSession > 30000) 
			NextOperationSession = 1;
		return Result;
	}
	
	private static class TOperationExecuteResult {
		public byte[] Data; 
		public int Origin;
		public int DataSize;
	}
	
	private TOperationExecuteResult Operation_Execute(short OperationSession, byte MessageEncryption, byte MessagePacking, byte[] Data) throws Exception {
		  //. encode message
		  TGeographServerServiceOperation.EncodeMessage(OperationSession,MessagePacking,UserID,UserPassword,MessageEncryption,new TMessage(Data));
		  int DataSize = Data.length;
		  //. send operation message
		  byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
		  TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream, BA);
		  TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream, Data);
		  ConnectionOutputStream.flush();
		  //. waiting for and get a response message
		  byte[] ResponseMessageSizeArray = new byte[4];
		  TGeographServerServiceOperation.Connection_ReadData(ConnectionInputStream,ResponseMessageSizeArray);
		  int ResponseMessageSize = TDataConverter.ConvertBEByteArrayToInt32(ResponseMessageSizeArray,0);
		  byte[] ResponseMessage = new byte[ResponseMessageSize];
		  TGeographServerServiceOperation.Connection_ReadData(ConnectionInputStream,ResponseMessage);
		  //. decode message
		  TOperationExecuteResult Response = new TOperationExecuteResult();
          TOperationSession ResponseSession = new TOperationSession();
          TMessage _ResponseMessage = new TMessage(ResponseMessage);
          TIndex ResponseMessageOrigin = new TIndex();
          TGeographServerServiceOperation.DecodeMessage(UserID,UserPassword, /*out*/ ResponseSession, _ResponseMessage, ResponseMessageOrigin);
          if (ResponseSession.ID != OperationSession)
        	  throw new Exception("wrong response session while executting an operation"); //. =>
          //.
          Response.Data = _ResponseMessage.Array; 
          Response.Origin = ResponseMessageOrigin.Value;
          Response.DataSize = Response.Data.length-(2/*SizeOf(Session)*/+4/*SizeOf(CRC)*/);
          //.
          return Response;
	}
	
	public static class TGetComponentDataResult {
		public int ResultCode;
		public byte[] Value;
	}
	
	public TGetComponentDataResult DeviceOperation_GetComponentDataCommand2(byte[] Address) throws Exception {
		short SID = TDeviceGetComponentDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//. execute
		TOperationExecuteResult ExecuteResult = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, ExecuteResult.Origin); ExecuteResult.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode); //. =>
		//. get result out-data
		TGetComponentDataResult Result = new TGetComponentDataResult();
		Result.ResultCode = ResultCode;
		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/];
		System.arraycopy(ExecuteResult.Data,ExecuteResult.Origin, Result.Value,0, Result.Value.length);
		//.
		return Result;
	}	

	public TGetComponentDataResult DeviceOperation_AddressDataGetComponentDataCommand1(byte[] Address, byte[] AddressData) throws Exception {
		short SID = TDeviceGetComponentDataByAddressDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		int AddressDataSize = 0;
		if (AddressData != null)
			AddressDataSize = AddressData.length; 
		BA = TDataConverter.ConvertInt32ToBEByteArray(AddressDataSize);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		if (AddressDataSize > 0) {
			System.arraycopy(AddressData,0, Data,Idx, AddressDataSize); Idx += AddressDataSize;			
		}
		//. execute
		TOperationExecuteResult ExecuteResult = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, ExecuteResult.Origin); ExecuteResult.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode); //. =>
		//. get result out-data
		TGetComponentDataResult Result = new TGetComponentDataResult();
		Result.ResultCode = ResultCode;
		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/];
		System.arraycopy(ExecuteResult.Data,ExecuteResult.Origin, Result.Value,0, Result.Value.length);
		//.
		return Result;
	}
	
	public int DeviceOperation_SetComponentDataCommand2(byte[] Address, byte[] Value) throws Exception {
		short SID = TDeviceSetComponentDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+Value.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;			
		//. execute
		TOperationExecuteResult Result = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertBEByteArrayToInt32(Result.Data, Result.Origin); Result.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode); //. =>
		//.
		return ResultCode;
	}

	public int DeviceOperation_AddressDataSetComponentDataCommand2(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
		short SID = TDeviceSetComponentDataByAddressDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length+Value.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		int AddressDataSize = 0;
		if (AddressData != null)
			AddressDataSize = AddressData.length; 
		BA = TDataConverter.ConvertInt32ToBEByteArray(AddressDataSize);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		if (AddressDataSize > 0) {
			System.arraycopy(AddressData,0, Data,Idx, AddressDataSize); Idx += AddressDataSize;			
		}
		//.
		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;			
		//. execute
		TOperationExecuteResult Result = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertBEByteArrayToInt32(Result.Data, Result.Origin); Result.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode); //. =>
		//.
		return ResultCode;
	}
}
