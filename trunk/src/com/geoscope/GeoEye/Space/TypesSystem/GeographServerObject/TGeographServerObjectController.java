package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TMessage;

public class TGeographServerObjectController extends TGeographServerClient {

	public TGeographServerObjectController(Context pcontext, int pObjectID, int pUserID, String pUserPassword, String pServerAddress, int pServerPort) throws Exception {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, 0, pObjectID);
		//.
		Connect();
	}
	
	@Override
	public void Destroy() throws IOException {
		Disconnect();
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
		  byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(DataSize);
		  TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream, BA);
		  TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream, Data);
		  ConnectionOutputStream.flush();
		  //. waiting for and get a response message
		  byte[] ResponseMessageSizeArray = new byte[4];
		  TGeographServerServiceOperation.Connection_ReadData(Connection,ConnectionInputStream,ResponseMessageSizeArray,context);
		  int ResponseMessageSize = TDataConverter.ConvertLEByteArrayToInt32(ResponseMessageSizeArray,0);
		  byte[] ResponseMessage = new byte[ResponseMessageSize];
		  TGeographServerServiceOperation.Connection_ReadData(Connection,ConnectionInputStream,ResponseMessage,context);
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
	
	private static class TGetComponentDataResult {
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
		byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//. execute
		TOperationExecuteResult ExecuteResult = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertLEByteArrayToInt32(ExecuteResult.Data, ExecuteResult.Origin); ExecuteResult.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
		//. get result out-data
		TGetComponentDataResult Result = new TGetComponentDataResult();
		Result.ResultCode = ResultCode;
		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/];
		System.arraycopy(ExecuteResult.Data,ExecuteResult.Origin, Result.Value,0, Result.Value.length);
		//.
		return Result;
	}	

	@Override
    public byte[] Component_ReadDeviceCUAC(byte[] Address) throws Exception {
    	Operation_Cancel();
    	//.
    	TGetComponentDataResult ValueResult = DeviceOperation_GetComponentDataCommand2(Address);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,context); //. =>
		return ValueResult.Value;
    }
    
	public TGetComponentDataResult DeviceOperation_AddressDataGetComponentDataCommand1(byte[] Address, byte[] AddressData) throws Exception {
		short SID = TDeviceGetComponentDataByAddressDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		int AddressDataSize = 0;
		if (AddressData != null)
			AddressDataSize = AddressData.length; 
		BA = TDataConverter.ConvertInt32ToLEByteArray(AddressDataSize);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		if (AddressDataSize > 0) {
			System.arraycopy(AddressData,0, Data,Idx, AddressDataSize); Idx += AddressDataSize;			
		}
		//. execute
		TOperationExecuteResult ExecuteResult = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertLEByteArrayToInt32(ExecuteResult.Data, ExecuteResult.Origin); ExecuteResult.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
		//. get result out-data
		TGetComponentDataResult Result = new TGetComponentDataResult();
		Result.ResultCode = ResultCode;
		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/];
		System.arraycopy(ExecuteResult.Data,ExecuteResult.Origin, Result.Value,0, Result.Value.length);
		//.
		return Result;
	}
	
	@Override
    public byte[] Component_ReadDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData) throws Exception {
    	Operation_Cancel();
    	//.
    	TGetComponentDataResult ValueResult = DeviceOperation_AddressDataGetComponentDataCommand1(Address,AddressData);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,context); //. =>
		return ValueResult.Value;
    }
    
	public int DeviceOperation_SetComponentDataCommand2(byte[] Address, byte[] Value) throws Exception {
		short SID = TDeviceSetComponentDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+Value.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;			
		//. execute
		TOperationExecuteResult Result = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertLEByteArrayToInt32(Result.Data, Result.Origin); Result.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
		//.
		return ResultCode;
	}

	@Override
    public void Component_WriteDeviceCUAC(byte[] Address, byte[] Value) throws Exception {
    	Operation_Cancel();
    	//.
    	int ResultCode = DeviceOperation_SetComponentDataCommand2(Address,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
    }
    
	public int DeviceOperation_AddressDataSetComponentDataCommand2(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
		short SID = TDeviceSetComponentDataByAddressDataServiceOperation.SID; //. this operation SID
		short OperationSession = GetOperationSession();
		int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length+Value.length; //. this operation message size
		byte[] Data = new byte[DataSize]; 
		int Origin = TGeographServerServiceOperation.MessageOrigin;                             
		//. fill message for in-data
		int Idx = Origin;
		byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(SID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(ObjectID);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		//.
		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
		//.
		int AddressDataSize = 0;
		if (AddressData != null)
			AddressDataSize = AddressData.length; 
		BA = TDataConverter.ConvertInt32ToLEByteArray(AddressDataSize);
		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
		if (AddressDataSize > 0) {
			System.arraycopy(AddressData,0, Data,Idx, AddressDataSize); Idx += AddressDataSize;			
		}
		//.
		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;			
		//. execute
		TOperationExecuteResult Result = Operation_Execute(OperationSession,TGeographServerServiceOperation.EncryptionMethod_None,TGeographServerServiceOperation.PackingMethod_None, Data);
		//. get result code
		int ResultCode = TDataConverter.ConvertLEByteArrayToInt32(Result.Data, Result.Origin); Result.Origin += 4/*SizeOf(ResultCode)*/;
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
		//.
		return ResultCode;
	}

	@Override
    public void Component_WriteDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	Operation_Cancel();
    	//.
    	int ResultCode = DeviceOperation_AddressDataSetComponentDataCommand2(Address,AddressData,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
    }
}
