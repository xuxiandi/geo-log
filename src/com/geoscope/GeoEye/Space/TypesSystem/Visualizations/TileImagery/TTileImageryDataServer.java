package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Random;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.GeoLog.Utils.TUpdater;
import com.geoscope.Utils.TDataConverter;

public class TTileImageryDataServer extends TTileImageryServer {

	public static final int DefaultPort = 5555;
	public static final int ServerReadWriteTimeout = 1000*60; //. Seconds
	
	public static final short SERVICE_NONE          = 0;
	public static final short SERVICE_TILESERVER    = 1;
	public static final short SERVICE_TILESERVER_V1 = 2;
	//.
	public static final int SERVICE_TILESERVER_COMMAND_GETDATA = 0;
	public static final int SERVICE_TILESERVER_COMMAND_GETSERVERDATA = 1;
	public static final int SERVICE_TILESERVER_COMMAND_GETPROVIDERDATA = 2;
	public static final int SERVICE_TILESERVER_COMMAND_GETCOMPILATIONDATA = 3;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES = 4;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V1 = 5;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS = 6;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V1 = 7;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V2 = 11;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V2 = 8;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V3 = 9;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V4 = 10;
	public static final int SERVICE_TILESERVER_COMMAND_SETTILES = 12;
	public static final int SERVICE_TILESERVER_COMMAND_RESETTILES = 13;
	public static final int SERVICE_TILESERVER_COMMAND_RESETTILES_V1 = 14;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_NOTFOUND              = -2;
	public static final int MESSAGE_UNKNOWNSERVICE        = -10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
	public static final int MESSAGE_WRONGPARAMETERS       = -15;
	
	public static void CheckMessage(int Message) throws Exception {
		if (Message >= 0)
			return; //. ->
		switch (Message) {
		
		case MESSAGE_ERROR:
			throw new Exception("error"); //. =>
			
		case MESSAGE_NOTFOUND:
			throw new Exception("data is not found"); //. =>
			
		case MESSAGE_UNKNOWNSERVICE:
			throw new Exception("unknown service"); //. =>
			
		case MESSAGE_AUTHENTICATIONFAILED:
			throw new Exception("authentication is failed"); //. =>
			
		case MESSAGE_ACCESSISDENIED:
			throw new Exception("access is denied"); //. =>
			
		case MESSAGE_TOOMANYCLIENTS:
			throw new Exception("too many clients"); //. =>
			
		case MESSAGE_UNKNOWNCOMMAND:
			throw new Exception("unknown command"); //. =>
			
		case MESSAGE_WRONGPARAMETERS:
			throw new Exception("wrong parameters"); //. =>
			
		default:
			throw new Exception("unknown error, code: "+Integer.toString(Message)); //. =>
		}
	}

	public static class TGetTilesParams {
		public int TilesCount;
	}
	
	public static class TGetTilesByTimestampParams {
		public int TilesCount;
	}
	
	public static class TTileTimestampDescriptor {
		public int 		X;
		public int 		Y;
		public double 	Timestamp = 0.0;
	}
	
	public static class TTileDescriptor {
		public int 		X;
		public int 		Y;
		public double 	Timestamp = 0.0;
		public byte[] 	Data = null;
	}
	
	private static Random rnd = new Random();
	
	private Context context;
	//.
	public String 	ServerAddress;
	public int		ServerPort = DefaultPort;
	//.
	private int 	UserID;
	private String 	UserPassword;
	//.
    private Socket 		Connection;
    public InputStream 	ConnectionInputStream;
    public OutputStream ConnectionOutputStream;
	
	public TTileImageryDataServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		context = pcontext;
		//.
		ServerAddress = pServerAddress;
		if (pServerPort > 0)
			ServerPort = pServerPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
	}
	
	private void InputStream_ReadData(InputStream in, byte[] Data, int DataSize) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
        }
	}

	private void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
        int StartIdx = Offset;
        byte[] UserPasswordArray;
        UserPasswordArray = UserPassword.getBytes("windows-1251");
        //.
        if (UserPasswordArray.length > 0)
        {
            int UserPasswordArrayIdx = 0;
            for (int I = StartIdx; I < (StartIdx+Size); I++)
            {
                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
                UserPasswordArrayIdx++;
                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
                	UserPasswordArrayIdx = 0;
            }
        }
	}
	
	private void Connect(int Command) throws Exception {
        Connection = new Socket(ServerAddress,ServerPort); 
        Connection.setSoTimeout(ServerReadWriteTimeout);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
        //. send login info
        String UserIDStr = Integer.toString(UserID);
        int UserIDStrSize = 2*UserIDStr.length(); //. UCS2(UTF-16) size
        int UserIDStr1Length = 16;
        StringBuilder SB = new StringBuilder(UserIDStr1Length);
        SB.append(UserID);
        final char[] CharSet = new char[] {'!','@','#','$','%','^','&','*','(',')'};
        while (SB.length() < UserIDStr1Length) 
        	SB.append(CharSet[rnd.nextInt(CharSet.length)]);
        String UserIDStr1 = SB.toString();
        int UserIDStr1Size = 2*UserIDStr1.length(); //. UCS2(UTF-16) size
    	byte[] LoginBuffer = new byte[2/*SizeOf(Service)*/+4/*SizeOf(UserIDStrSize)*/+UserIDStrSize+4/*SizeOf(UserIDStr1Size)*/+UserIDStr1Size+4/*SizeOf(Command)*/];
    	int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_TILESERVER_V1);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStrSize);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr.getBytes("UTF-16LE");
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStr1Size);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr1.getBytes("UTF-16LE");
		Buffer_Encrypt(BA,0,BA.length,UserPassword);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Command);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		//.
		ConnectionOutputStream.write(LoginBuffer);
	}

	private void Disconnect() throws IOException {
        //. close connection gracefully
		try {
	        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_DISCONNECT);
	        ConnectionOutputStream.write(BA);
	        ConnectionOutputStream.flush();
		}
		catch (Exception E) {}
        //.
        ConnectionOutputStream.close();
        ConnectionInputStream.close();
        Connection.close();
	}
	
	public byte[] GetData() throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETDATA);
		try {
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. get data size
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			int DataSize = Descriptor;
			//.
			byte[] Result = new byte[DataSize];
        	InputStream_ReadData(ConnectionInputStream, Result,Result.length);
        	return Result; //. ->
		}
		finally {
			Disconnect();
		}
	}
	
	public byte[] GetCompilationData(int SID, int PID, int CID) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETCOMPILATIONDATA);
		try {
			byte[] Params = new byte[8];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			//. get data size
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			int DataSize = Descriptor;
			//.
			byte[] Result = new byte[DataSize];
        	InputStream_ReadData(ConnectionInputStream, Result,Result.length);
        	return Result; //. ->
		}
		finally {
			Disconnect();
		}
	}
	
	public TTileTimestampDescriptor[] GetTilesTimestamps(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V1);
		try {
			byte[] Params = new byte[48];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Xmn);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Xmx);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Ymn);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Ymx);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			int ExceptTilesDescriptor = 0;
			if (ExceptTiles != null)
				ExceptTilesDescriptor = ExceptTiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(ExceptTilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (ExceptTilesDescriptor > 0)
				ConnectionOutputStream.write(ExceptTiles);
			//. check response
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//.
			TTileTimestampDescriptor[] Result = new TTileTimestampDescriptor[Descriptor];
			byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/];
			for (int I = 0; I < Descriptor; I++) {
	        	InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length);
	        	Result[I] = new TTileTimestampDescriptor();
	        	Idx = 0;
	        	Result[I].X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
	        	Result[I].Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
	        	Result[I].Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
			}
			return Result;
		}
		finally {
			Disconnect();
		}
	}
	
	public TTileTimestampDescriptor[] GetTilesTimestampsByTimestamp(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, double HistoryTime, byte[] ExceptTiles, TCanceller Canceller) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V2);
		try {
			byte[] Params = new byte[56];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Xmn);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Xmx);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Ymn);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertInt32ToBEByteArray(Ymx);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertDoubleToBEByteArray(HistoryTime);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			int ExceptTilesDescriptor = 0;
			if (ExceptTiles != null)
				ExceptTilesDescriptor = ExceptTiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(ExceptTilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (ExceptTilesDescriptor > 0)
				ConnectionOutputStream.write(ExceptTiles);
			//. check response
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//.
			TTileTimestampDescriptor[] Result = new TTileTimestampDescriptor[Descriptor];
			byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/];
			for (int I = 0; I < Descriptor; I++) {
	        	InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length);
	        	Result[I] = new TTileTimestampDescriptor();
	        	Idx = 0;
	        	Result[I].X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
	        	Result[I].Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
	        	Result[I].Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
			}
			return Result;
		}
		finally {
			Disconnect();
		}
	}
	
	public TGetTilesParams GetTiles_Begin(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETTILES_V3);
		byte[] Params = new byte[48];
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
		System.arraycopy(BA,0, Params,0, BA.length);
		ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
		//. check login
		byte[] DecriptorBA = new byte[4];
		ConnectionInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
		//. send parameters
		int Idx = 0;
		BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmn);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmx);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymn);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymx);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		int ExceptTilesDescriptor = 0;
		if (ExceptTiles != null)
			ExceptTilesDescriptor = ExceptTiles.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ExceptTilesDescriptor);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		ConnectionOutputStream.write(Params);
		if (ExceptTilesDescriptor > 0)
			ConnectionOutputStream.write(ExceptTiles);
		//. check response
		ConnectionInputStream.read(DecriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
		//.
		TGetTilesParams Result = new TGetTilesParams();
		Result.TilesCount = Descriptor;
		return Result;
	}
	
	public TTileDescriptor GetTiles_Read(TGetTilesParams Params) throws Exception {
		TTileDescriptor Result = new TTileDescriptor();
		byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/+4/*SizeOf(TileSize)*/];
    	InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length);
    	int Idx = 0;
    	Result.X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
    	int TileSize = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 4;
    	if (TileSize > 0) {
    		Result.Data = new byte[TileSize];
        	InputStream_ReadData(ConnectionInputStream, Result.Data,Result.Data.length);
    	}
    	else
    		Result.Data = null;
    	return Result;
	}
	
	public void GetTiles_End(TGetTilesParams Params) throws Exception {
		Disconnect();
	}
	
	public TGetTilesByTimestampParams GetTilesByTimestamp_Begin(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, double HistoryTime, byte[] ExceptTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_GETTILES_V4);
		byte[] Params = new byte[56];
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
		System.arraycopy(BA,0, Params,0, BA.length);
		ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
		//. check login
		byte[] DecriptorBA = new byte[4];
		ConnectionInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
		//. send parameters
		int Idx = 0;
		BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmn);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmx);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymn);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymx);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
		BA = TDataConverter.ConvertDoubleToBEByteArray(HistoryTime);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		int ExceptTilesDescriptor = 0;
		if (ExceptTiles != null)
			ExceptTilesDescriptor = ExceptTiles.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ExceptTilesDescriptor);
		System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
		ConnectionOutputStream.write(Params);
		if (ExceptTilesDescriptor > 0)
			ConnectionOutputStream.write(ExceptTiles);
		//. check response
		ConnectionInputStream.read(DecriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
		//.
		TGetTilesByTimestampParams Result = new TGetTilesByTimestampParams();
		Result.TilesCount = Descriptor;
		return Result;
	}
	
	public TTileDescriptor GetTilesByTimestamp_Read(TGetTilesByTimestampParams Params) throws Exception {
		TTileDescriptor Result = new TTileDescriptor();
		byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/+4/*SizeOf(TileSize)*/];
    	InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length);
    	int Idx = 0;
    	Result.X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
    	int TileSize = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 4;
    	if (TileSize > 0) {
    		Result.Data = new byte[TileSize];
        	InputStream_ReadData(ConnectionInputStream, Result.Data,Result.Data.length);
    	}
    	else
    		Result.Data = null;
    	return Result;
	}
	
	public void GetTilesByTimestamp_End(TGetTilesByTimestampParams Params) throws Exception {
		Disconnect();
	}
		
	public double SetTiles(int SID, int PID, int CID, int Level, int SecurityFileID, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_SETTILES);
		try {
			byte[] Params = new byte[24];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(SecurityFileID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			int TilesDescriptor = 0;
			if (Tiles != null)
				TilesDescriptor = Tiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(TilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (TilesDescriptor > 0)
				ConnectionOutputStream.write(Tiles);
			//. check response
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
        	InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	

	public double ReSetTiles(int SID, int PID, int CID, int Level, int SecurityFileID, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_RESETTILES);
		try {
			byte[] Params = new byte[24];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(SecurityFileID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			int TilesDescriptor = 0;
			if (Tiles != null)
				TilesDescriptor = Tiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(TilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (TilesDescriptor > 0)
				ConnectionOutputStream.write(Tiles);
			//. check response
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
        	InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	

	public double ReSetTilesV1(int SID, int PID, int CID, int Level, int SecurityFileID, double ReSetInterval, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_COMMAND_RESETTILES_V1);
		try {
			byte[] Params = new byte[32];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(Level);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(SecurityFileID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; //. SizeOf(Int64)
			BA = TDataConverter.ConvertDoubleToBEByteArray(ReSetInterval);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += 8; 
			int TilesDescriptor = 0;
			if (Tiles != null)
				TilesDescriptor = Tiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(TilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (TilesDescriptor > 0)
				ConnectionOutputStream.write(Tiles);
			//. check response
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
        	InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	
}
