package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TMessage;
import com.geoscope.Utils.TDataConverter;

public class TGeographServerClient {

	public static final int DefaultPort = 8283;
	public static final int ServerReadWriteTimeout = 1000*30; //. Seconds
		
	private static short NextOperationSession = 1;
	
	private short GetOperationSession() {
		short Result = NextOperationSession;
		NextOperationSession++;
		if (NextOperationSession > 30000)
			NextOperationSession = 1;
		return Result;
	}
	
	public static byte[] GetAddressArray(short[] Address) throws IOException {
		byte[] Result = new byte[Address.length << 1];
		for (int I = 0; I < Address.length; I++) 
			TDataConverter.ConvertInt16ToBEByteArray(Address[I], Result, (I << 1));
		return Result;
	}

	private String 	ServerAddress;
	private int		ServerPort;
	//.
    private int 	UserID;
    private String 	UserPassword;
    //.
    @SuppressWarnings("unused")
	private int idGeoGraphServerObject;
    //.
    private int ObjectID;
    //.
    private Socket			Connection = null;
    private InputStream 	ConnectionInputStream = null;
    private OutputStream 	ConnectionOutputStream = null;
    
    public TGeographServerClient(String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeoGraphServerObject, int pObjectID) {
    	ServerAddress = pServerAddress;
    	ServerPort = pServerPort;
    	//.
    	UserID = pUserID;
    	UserPassword = pUserPassword;
    	//.
    	idGeoGraphServerObject = pidGeoGraphServerObject;
    	//.
    	ObjectID = pObjectID;
    }
    
    public void Destroy() {
    }

    private void Operation_Start() throws IOException {
        Connection = new Socket(ServerAddress,ServerPort); 
        Connection.setSoTimeout(ServerReadWriteTimeout);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
    }
    
    private void Operation_Finish() throws IOException {
    	if (ConnectionOutputStream != null) {
            //. close connection gracefully
    		try {
    	        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(0);
    	        ConnectionOutputStream.write(BA);
    	        ConnectionOutputStream.flush();
    		}
    		catch (Exception E) {}
            //.
            ConnectionOutputStream.close();
            ConnectionOutputStream = null;
    	}
    	if (ConnectionInputStream != null) {
    		ConnectionInputStream.close();
    		ConnectionInputStream = null;
    	}
    	if (Connection != null) {
    		Connection.close();
    		Connection = null;
    	}
    }

    private static class TExecuteResult {

    	byte[] 	Data;
    	int		DataSize;
    }
    
    private TExecuteResult Operation_Execute(TOperationSession OperationSession, byte MessageEncryption, byte MessagePacking, byte[] Data, TIndex Origin, int DataSize) throws Exception {
    	TExecuteResult Result = new TExecuteResult();
    	//. encode message
    	TMessage Message = new TMessage(Data);
    	TGeographServerServiceOperation.EncodeMessage(OperationSession.ID,MessagePacking,UserID,UserPassword,MessageEncryption,Message);
    	//. send operation message
    	byte[] Descriptor = TDataConverter.ConvertInt32ToBEByteArray(Message.Array.length);
    	TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream,Descriptor);
    	TGeographServerServiceOperation.Connection_WriteData(ConnectionOutputStream,Message.Array);
    	//. waiting for and get a response message
    	do {
        	TGeographServerServiceOperation.Connection_ReadData(ConnectionInputStream,Descriptor,Descriptor.length);
        	DataSize = TDataConverter.ConvertBEByteArrayToInt32(Descriptor,0);
    	}
    	while (DataSize == 0);
    	Message.Array = new byte[DataSize];
    	TGeographServerServiceOperation.Connection_ReadData(ConnectionInputStream,Message.Array,Message.Array.length);
    	//. decode message
    	TGeographServerServiceOperation.DecodeMessage(UserID,UserPassword,OperationSession, Message,Origin);
    	//.
    	Result.Data = Message.Array;
    	Result.DataSize = Message.Array.length-(2/*SizeOf(Session)*/+4/*SizeOf(CRC)*/);
    	//.
    	return Result;
    }
    
    private static class TValueResult {
    	
    	public int 		ResultCode;
    	public byte[] 	Value = null;
    }
    
    public TValueResult DeviceOperation_GetComponentDataCommand2(byte[] Address) throws Exception {
    	
    	TValueResult Result = new TValueResult();
    	
    	byte MessagePacking = TGeographServerServiceOperation.PackingMethod_ZLIBZIP;
    	byte MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;
    	short SID = (short)35103; //. this operation SID
    	
    	TOperationSession OperationSession = new TOperationSession(GetOperationSession());
    	int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4; //. this operation message size
    	byte[] Data = new byte[DataSize];
    	TIndex Origin = new TIndex(TGeographServerServiceOperation.MessageOrigin);
    	int Idx;
    	int ResultCode;

    	Operation_Start();
    	try {
    		//. fill message for in-data
    		Idx = Origin.Value;
    		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		//.
    		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
    		//. execute
    		TExecuteResult ExecuteResult = Operation_Execute(OperationSession,MessageEncryption,MessagePacking, Data,Origin,DataSize);
    		//. get result code
    		ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, Origin.Value); Origin.Value += 4/*SizeOf(ResultCode)*/; 
    		if (ResultCode >= 0) {
        		//. get result out-data
        		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/]; 
        		System.arraycopy(ExecuteResult.Data,Origin.Value, Result.Value,0, Result.Value.length);
    		}
    		//.
    		Result.ResultCode = ResultCode;
    	}
    	finally {
    		Operation_Finish();
    	}
    	//.
    	return Result;
    }
    
    public byte[] ReadDeviceCUAC(byte[] Address) throws Exception {
    	TValueResult ValueResult = DeviceOperation_GetComponentDataCommand2(Address);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,""); //. =>
		return ValueResult.Value;
    }
    
    public byte[] ReadDeviceCUAC(short[] Address) throws Exception {
		return ReadDeviceCUAC(TGeographServerClient.GetAddressArray(Address));
    }
    
    public TValueResult DeviceOperation_AddressDataGetComponentDataCommand1(byte[] Address, byte[] AddressData) throws Exception {
    	
    	TValueResult Result = new TValueResult();
    	
    	byte MessagePacking = TGeographServerServiceOperation.PackingMethod_ZLIBZIP;
    	byte MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;
    	short SID = (short)35104; //. this operation SID
    	
    	TOperationSession OperationSession = new TOperationSession(GetOperationSession());
    	int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length; //. this operation message size
    	byte[] Data = new byte[DataSize];
    	TIndex Origin = new TIndex(TGeographServerServiceOperation.MessageOrigin);
    	int Idx;
    	int ResultCode;

    	Operation_Start();
    	try {
    		//. fill message for in-data
    		Idx = Origin.Value;
    		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		//.
    		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
    		//.
    		BA = TDataConverter.ConvertInt32ToBEByteArray(AddressData.length);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		if (AddressData.length > 0) {
        		System.arraycopy(AddressData,0, Data,Idx, AddressData.length); Idx += AddressData.length;
    		}
    		//. execute
    		TExecuteResult ExecuteResult = Operation_Execute(OperationSession,MessageEncryption,MessagePacking, Data,Origin,DataSize);
    		//. get result code
    		ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, Origin.Value); Origin.Value += 4/*SizeOf(ResultCode)*/; 
    		if (ResultCode >= 0) {
        		//. get result out-data
        		Result.Value = new byte[ExecuteResult.DataSize-4/*SizeOf(ResultCode)*/]; 
        		System.arraycopy(ExecuteResult.Data,Origin.Value, Result.Value,0, Result.Value.length);
    		}
    		//.
    		Result.ResultCode = ResultCode;
    	}
    	finally {
    		Operation_Finish();
    	}
    	//.
    	return Result;
    }
    
    public byte[] ReadDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData) throws Exception {
    	TValueResult ValueResult = DeviceOperation_AddressDataGetComponentDataCommand1(Address,AddressData);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,""); //. =>
		return ValueResult.Value;
    }
    
    public byte[] ReadDeviceByAddressDataCUAC(short[] Address, byte[] AddressData) throws Exception {
		return ReadDeviceByAddressDataCUAC(TGeographServerClient.GetAddressArray(Address),AddressData);
    }
    
    public int DeviceOperation_SetComponentDataCommand2(byte[] Address, byte[] Value) throws Exception {
    	
    	byte MessagePacking = TGeographServerServiceOperation.PackingMethod_ZLIBZIP;
    	byte MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;
    	short SID = (short)30503; //. this operation SID
    	
    	TOperationSession OperationSession = new TOperationSession(GetOperationSession());
    	int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+Value.length; //. this operation message size
    	byte[] Data = new byte[DataSize];
    	TIndex Origin = new TIndex(TGeographServerServiceOperation.MessageOrigin);
    	int Idx;
    	int ResultCode;

    	Operation_Start();
    	try {
    		//. fill message for in-data
    		Idx = Origin.Value;
    		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		//.
    		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
    		//.
    		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;
    		//. execute
    		TExecuteResult ExecuteResult = Operation_Execute(OperationSession,MessageEncryption,MessagePacking, Data,Origin,DataSize);
    		//. get result code
    		ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, Origin.Value); Origin.Value += 4/*SizeOf(ResultCode)*/; 
    	}
    	finally {
    		Operation_Finish();
    	}
    	//.
    	return ResultCode;
    }
    
    public void WriteDeviceCUAC(byte[] Address, byte[] Value) throws Exception {
    	int ResultCode = DeviceOperation_SetComponentDataCommand2(Address,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,""); //. =>
    }
    
    public void WriteDeviceCUAC(short[] Address, byte[] Value) throws Exception {
    	WriteDeviceCUAC(TGeographServerClient.GetAddressArray(Address), Value);
    }
    
    public int DeviceOperation_AddressDataSetComponentDataCommand2(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	
    	byte MessagePacking = TGeographServerServiceOperation.PackingMethod_ZLIBZIP;
    	byte MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;
    	short SID = (short)30506; //. this operation SID
    	
    	TOperationSession OperationSession = new TOperationSession(GetOperationSession());
    	int DataSize = TGeographServerServiceOperation.MessageProtocolSize+6+Address.length+4+AddressData.length+Value.length; //. this operation message size
    	byte[] Data = new byte[DataSize];
    	TIndex Origin = new TIndex(TGeographServerServiceOperation.MessageOrigin);
    	int Idx;
    	int ResultCode;

    	Operation_Start();
    	try {
    		//. fill message for in-data
    		Idx = Origin.Value;
    		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		BA = TDataConverter.ConvertInt32ToBEByteArray(ObjectID);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		//.
    		System.arraycopy(Address,0, Data,Idx, Address.length); Idx += Address.length;
    		//.
    		BA = TDataConverter.ConvertInt32ToBEByteArray(AddressData.length);
    		System.arraycopy(BA,0, Data,Idx, BA.length); Idx += BA.length;
    		if (AddressData.length > 0) {
        		System.arraycopy(AddressData,0, Data,Idx, AddressData.length); Idx += AddressData.length;
    		}
    		//.
    		System.arraycopy(Value,0, Data,Idx, Value.length); Idx += Value.length;
    		//. execute
    		TExecuteResult ExecuteResult = Operation_Execute(OperationSession,MessageEncryption,MessagePacking, Data,Origin,DataSize);
    		//. get result code
    		ResultCode = TDataConverter.ConvertBEByteArrayToInt32(ExecuteResult.Data, Origin.Value); Origin.Value += 4/*SizeOf(ResultCode)*/; 
    	}
    	finally {
    		Operation_Finish();
    	}
    	//.
    	return ResultCode;
    }
    
    public void WriteDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	int ResultCode = DeviceOperation_AddressDataSetComponentDataCommand2(Address,AddressData,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,""); //. =>
    }

    public void WriteDeviceByAddressDataCUAC(short[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	WriteDeviceByAddressDataCUAC(TGeographServerClient.GetAddressArray(Address),AddressData, Value);
    }
}

