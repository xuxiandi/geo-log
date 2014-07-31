package com.geoscope.GeoEye.Space.TypesSystem.GeographServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.content.Context;

import com.geoscope.Classes.IO.Log.TDataConverter;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TMessage;

public class TGeographServerClient {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static final int DefaultPort = 8283;
	//.
	public static final int ConnectionTimeout = 30; //. seconds
	public static final int ServerReadWriteTimeout = 30; //. Seconds
		
	private static short NextOperationSession = 1;
	
	protected short GetOperationSession() {
		short Result = NextOperationSession;
		NextOperationSession++;
		if (NextOperationSession > 30000)
			NextOperationSession = 1;
		return Result;
	}
	
	protected static byte[] GetAddressArray(short[] Address) throws IOException {
		byte[] Result = new byte[2/*SizeOf(AddressSize)*/+(Address.length << 1)];
		TDataConverter.ConvertInt16ToBEByteArray((short)Address.length, Result, 0);
		for (int I = 0; I < Address.length; I++) 
			TDataConverter.ConvertInt16ToBEByteArray(Address[I], Result, 2+(I << 1));
		return Result;
	}

	public static byte[] GetAddressArray(int[] Address) throws IOException {
		byte[] Result = new byte[2/*SizeOf(AddressSize)*/+(Address.length << 1)];
		TDataConverter.ConvertInt16ToBEByteArray((short)Address.length, Result, 0);
		for (int I = 0; I < Address.length; I++) 
			TDataConverter.ConvertInt16ToBEByteArray((short)(Address[I] & 0xFFFF), Result, 2+(I << 1));
		return Result;
	}

	protected Context context;
	//.
	protected String 	ServerAddress;
	protected int		ServerPort;
	protected int 		SecureServerPortShift = 2;
    protected int		SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
	//.
	protected int 	UserID;
	protected String 	UserPassword;
    //.
	protected int idGeoGraphServerObject;
    //.
	protected int ObjectID;
    //.
    public int				ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
	protected Socket		Connection = null;
	protected InputStream 	ConnectionInputStream = null;
	protected OutputStream 	ConnectionOutputStream = null;
	public boolean 			Connection_flKeepAlive = false;
    
    public TGeographServerClient(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeoGraphServerObject, int pObjectID) {
    	context = pcontext;
    	//.
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
    
    public void Destroy() throws IOException {
    }

	public boolean KeepConnection() {
		boolean R = Connection_flKeepAlive;
		Connection_flKeepAlive = true;
		return R;
	}
	
	private void PlainConnect() throws IOException {
        Connection = new Socket(ServerAddress,ServerPort); 
        Connection.setSoTimeout(ConnectionTimeout*1000);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
	}
	
    private void SecureSSLConnect() throws Exception {
		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
	        @Override
	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
	        }
	        @Override
	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
	        }
	        @Override
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    } };
	    //. install the all-trusting trust manager
	    SSLContext sslContext = SSLContext.getInstance( "SSL" );
	    sslContext.init( null, _TrustAllCerts, new java.security.SecureRandom());
	    //. create a ssl socket factory with our all-trusting manager
	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
        Connection.setSoTimeout(ConnectionTimeout*1000);
        Connection.setKeepAlive(true);
        Connection.setSendBufferSize(10000);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
    }
    
	private final int Connect_TryCount = 3;
	
    public void Connect() throws Exception {
		int TryCounter = Connect_TryCount;
		while (true) {
			try {
				try {
					//. connect
			    	switch (ConnectionType()) {
			    	
			    	case CONNECTION_TYPE_PLAIN:
			    		PlainConnect();
			    		break; //. >
			    		
			    	case CONNECTION_TYPE_SECURE_SSL:
			    		SecureSSLConnect();
			    		break; //. >
			    		
			    	default:
			    		throw new Exception("unknown connection type"); //. =>
			    	}
					break; //. >
				} catch (SocketTimeoutException STE) {
					throw new IOException(context.getString(R.string.SConnectionTimeoutError)); //. =>
				} catch (ConnectException CE) {
					throw new ConnectException(context.getString(R.string.SNoServerConnection)); //. =>
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.toString();
					throw new Exception(context.getString(R.string.SHTTPConnectionError)+S); //. =>
				}
			}
			catch (Exception E) {
				TryCounter--;
				if (TryCounter == 0)
					throw E; //. =>
			}
		}
    }
    
	public void Disconnect() throws IOException {
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
	
	public void Cancel() throws IOException {
    	if (Connection != null) 
    		Connection.close();
	}
	
    protected void Operation_Start() throws Exception {
    	if (!Connection_flKeepAlive)
    		Connect();
    }
    
    protected void Operation_Finish() throws Exception {
    	if (!Connection_flKeepAlive)
    		Disconnect();
    }
    
    public void Operation_Cancel() throws IOException {
    	if (!Connection_flKeepAlive)
    		Cancel();
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
        	TGeographServerServiceOperation.Connection_ReadData(Connection,ConnectionInputStream,Descriptor,ServerReadWriteTimeout,context);
        	DataSize = TDataConverter.ConvertBEByteArrayToInt32(Descriptor,0);
    	}
    	while (DataSize == 0);
    	Message.Array = new byte[DataSize];
    	TGeographServerServiceOperation.Connection_ReadData(Connection,ConnectionInputStream,Message.Array,ServerReadWriteTimeout,context);
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
    
    private synchronized TValueResult ObjectOperation_GetComponentDataCommand2(byte[] Address) throws Exception {
    	
    	TValueResult Result = new TValueResult();
    	
    	byte MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        //.
    	byte MessageEncryption = TGeographServerServiceOperation.MessageDefaultEncryption;
    	if (ConnectionType() == CONNECTION_TYPE_SECURE_SSL)
    		MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;

    	short SID = (short)5503; //. this operation SID
    	
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
    
    public byte[] Component_ReadAllCUAC(byte[] Address) throws Exception {
    	Operation_Cancel();
    	//.
    	TValueResult ValueResult = ObjectOperation_GetComponentDataCommand2(Address);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,context); //. =>
		return ValueResult.Value;
    }
    
    public byte[] Component_ReadAllCUAC(int[] Address) throws Exception {
		return Component_ReadAllCUAC(TGeographServerClient.GetAddressArray(Address));
    }
    
    private synchronized TValueResult DeviceOperation_GetComponentDataCommand2(byte[] Address) throws Exception {
    	
    	TValueResult Result = new TValueResult();
    	
    	byte MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        //.
    	byte MessageEncryption = TGeographServerServiceOperation.MessageDefaultEncryption;
    	if (ConnectionType() == CONNECTION_TYPE_SECURE_SSL)
    		MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;

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
    
    public byte[] Component_ReadDeviceCUAC(byte[] Address) throws Exception {
    	Operation_Cancel();
    	//.
    	TValueResult ValueResult = DeviceOperation_GetComponentDataCommand2(Address);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,context); //. =>
		return ValueResult.Value;
    }
    
    public byte[] Component_ReadDeviceCUAC(int[] Address) throws Exception {
		return Component_ReadDeviceCUAC(TGeographServerClient.GetAddressArray(Address));
    }
    
    private synchronized TValueResult DeviceOperation_AddressDataGetComponentDataCommand1(byte[] Address, byte[] AddressData) throws Exception {
    	
    	TValueResult Result = new TValueResult();
    	
    	byte MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        //.
    	byte MessageEncryption = TGeographServerServiceOperation.MessageDefaultEncryption;
    	if (ConnectionType() == CONNECTION_TYPE_SECURE_SSL)
    		MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;

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
    
    public byte[] Component_ReadDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData) throws Exception {
    	Operation_Cancel();
    	//.
    	TValueResult ValueResult = DeviceOperation_AddressDataGetComponentDataCommand1(Address,AddressData);
		if (ValueResult.ResultCode < 0)
			throw new OperationException(ValueResult.ResultCode,context); //. =>
		return ValueResult.Value;
    }
    
    public byte[] Component_ReadDeviceByAddressDataCUAC(int[] Address, byte[] AddressData) throws Exception {
		return Component_ReadDeviceByAddressDataCUAC(TGeographServerClient.GetAddressArray(Address),AddressData);
    }
    
    private synchronized int DeviceOperation_SetComponentDataCommand2(byte[] Address, byte[] Value) throws Exception {
    	
    	byte MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        if (Value.length > TGeographServerServiceOperation.MessageSizeForPacking) {
            if (Value.length > TGeographServerServiceOperation.MessageSizeForNoPacking)
                MessagePacking = TGeographServerServiceOperation.MessageNoPacking;
            else
                MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        }
        //.
    	byte MessageEncryption = TGeographServerServiceOperation.MessageDefaultEncryption;
    	if (ConnectionType() == CONNECTION_TYPE_SECURE_SSL)
    		MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;
    	//.
    	short SID = (short)30503; //. this operation SID
    	//.
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
    
    public void Component_WriteDeviceCUAC(byte[] Address, byte[] Value) throws Exception {
    	Operation_Cancel();
    	//.
    	int ResultCode = DeviceOperation_SetComponentDataCommand2(Address,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
    }
    
    public void Component_WriteDeviceCUAC(int[] Address, byte[] Value) throws Exception {
    	Component_WriteDeviceCUAC(TGeographServerClient.GetAddressArray(Address), Value);
    }
    
    private synchronized int DeviceOperation_AddressDataSetComponentDataCommand2(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	
    	byte MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        if (Value.length > TGeographServerServiceOperation.MessageSizeForPacking) {
            if (Value.length > TGeographServerServiceOperation.MessageSizeForNoPacking)
                MessagePacking = TGeographServerServiceOperation.MessageNoPacking;
            else
                MessagePacking = TGeographServerServiceOperation.MessageNormalPacking;
        }
        //.
    	byte MessageEncryption = TGeographServerServiceOperation.MessageDefaultEncryption;
    	if (ConnectionType() == CONNECTION_TYPE_SECURE_SSL)
    		MessageEncryption = TGeographServerServiceOperation.EncryptionMethod_SimpleByPassword;

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
    
    public void Component_WriteDeviceByAddressDataCUAC(byte[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	Operation_Cancel();
    	//.
    	int ResultCode = DeviceOperation_AddressDataSetComponentDataCommand2(Address,AddressData,Value);
		if (ResultCode < 0)
			throw new OperationException(ResultCode,context); //. =>
    }

    public void Component_WriteDeviceByAddressDataCUAC(int[] Address, byte[] AddressData, byte[] Value) throws Exception {
    	Component_WriteDeviceByAddressDataCUAC(TGeographServerClient.GetAddressArray(Address),AddressData, Value);
    }
}

