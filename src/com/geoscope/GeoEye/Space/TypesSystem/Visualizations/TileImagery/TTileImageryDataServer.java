package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TUpdater;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Server.TGeoScopeSpaceDataServer;

public class TTileImageryDataServer extends TGeoScopeSpaceDataServer {

	public static final short SERVICE_TILESERVER    = 1;
	public static final short SERVICE_TILESERVER_V1 = 2;
	//.
	public static final int SERVICE_TILESERVER_COMMAND_GETDATA 					= 0;
	public static final int SERVICE_TILESERVER_COMMAND_GETSERVERDATA 			= 1;
	public static final int SERVICE_TILESERVER_COMMAND_GETPROVIDERDATA 			= 2;
	public static final int SERVICE_TILESERVER_COMMAND_GETCOMPILATIONDATA 		= 3;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES 				= 4;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V1 				= 5;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS 		= 6;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V1 	= 7;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V2 	= 11;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V2 				= 8;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V3 				= 9;
	public static final int SERVICE_TILESERVER_COMMAND_GETTILES_V4 				= 10;
	public static final int SERVICE_TILESERVER_COMMAND_SETTILES 				= 12;
	public static final int SERVICE_TILESERVER_COMMAND_RESETTILES 				= 13;
	public static final int SERVICE_TILESERVER_COMMAND_RESETTILES_V1 			= 14;
	public static final int SERVICE_TILESERVER_COMMAND_RESETTILES_V2 			= 15;

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
	
	public static class TTilesPlace {

		public String Name = "";
		//.
		public double X0;
		public double Y0;
		public double X1;
		public double Y1;
		public double X2;
		public double Y2;
		public double X3;
		public double Y3;
		//.
		public double Timestamp = 0.0;
		
		public TTilesPlace(String pName, TReflectionWindowStruc RW) {
			Name = pName;
			//.
			X0 = RW.X0; Y0 = RW.Y0;
			X1 = RW.X1; Y1 = RW.Y1;
			X2 = RW.X2; Y2 = RW.Y2;
			X3 = RW.X3; Y3 = RW.Y3;
			//.
			Timestamp = RW.BeginTimestamp;
		}
		
		public int ByteArraySize() {
			return (8*8+8+2/*SizeOf(Name)*/+Name.length()/*one-byte char length (ANSI)*/);
		}
		
		public int ToByteArray(byte[] BA, int Idx) throws IOException {
			byte[] _BA = TDataConverter.ConvertDoubleToBEByteArray(X0);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			_BA = TDataConverter.ConvertDoubleToBEByteArray(Y0);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			//.
			_BA = TDataConverter.ConvertDoubleToBEByteArray(X1);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			_BA = TDataConverter.ConvertDoubleToBEByteArray(Y1);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			//.
			_BA = TDataConverter.ConvertDoubleToBEByteArray(X2);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			_BA = TDataConverter.ConvertDoubleToBEByteArray(Y2);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			//.
			_BA = TDataConverter.ConvertDoubleToBEByteArray(X3);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			_BA = TDataConverter.ConvertDoubleToBEByteArray(Y3);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			//.
			_BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			//.
			short SS = (short)Name.length();
			_BA = TDataConverter.ConvertInt16ToBEByteArray(SS);
			System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			if (SS > 0) {
				_BA = Name.getBytes("windows-1251");
				System.arraycopy(_BA,0, BA,Idx, _BA.length); Idx += _BA.length;
			}
			//.
			return Idx;
		}
		
		public byte[] ToByteArray() throws IOException {
			byte[] Result = new byte[ByteArraySize()];
			ToByteArray(Result, 0);
			return Result;
		}
	}
	
	public static class TTilesFile {

		public int 			SID;
		public int 			PID;
		public int 			CID;
		public int 			Level;
		public int 			SecurityFileID; 
		public double 		ReSetInterval;
		public TTilesPlace 	Place;
		public byte[] 		Tiles;
		
		public TTilesFile(int pSID, int pPID, int pCID, int pLevel, int pSecurityFileID, double pReSetInterval, TTilesPlace pPlace, byte[] pTiles) {
			SID = pSID;
			PID = pPID;
			CID = pCID;
			Level = pLevel;
			SecurityFileID = pSecurityFileID;
			ReSetInterval = pReSetInterval;
			Place = pPlace;
			Tiles = pTiles;
		}
		
		public void SaveToFile(String pFileName) throws IOException {
			short Version = 1;
			byte[] BA;
			byte[] BA64 = new byte[4]; //. extension to Int64
			FileOutputStream FOS = new FileOutputStream(pFileName);
			try {
				FOS.write(TDataConverter.ConvertInt16ToBEByteArray(Version));
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(SID)); FOS.write(BA64);
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(PID));
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(CID));
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(Level));
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(SecurityFileID)); FOS.write(BA64);
				FOS.write(TDataConverter.ConvertDoubleToBEByteArray(ReSetInterval));
				//.
				BA = Place.ToByteArray();
				int PlaceSize = 0;
				if (BA != null)
					PlaceSize = BA.length;
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(PlaceSize));
				if (PlaceSize > 0) 
					FOS.write(BA);
				//.
				int TilesSize = 0;
				if (Tiles != null)
					TilesSize = Tiles.length;
				FOS.write(TDataConverter.ConvertInt32ToBEByteArray(TilesSize));
				if (TilesSize > 0) 
					FOS.write(Tiles);
			}
			finally {
				FOS.close();
			}
		}
	}
	
	public TTileImageryDataServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword);
	}
	
	public byte[] GetData() throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETDATA);
		try {
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. get data size
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			int DataSize = Descriptor;
			//.
			byte[] Result = new byte[DataSize];
        	TNetworkConnection.InputStream_ReadData(ConnectionInputStream, Result,Result.length, context);
        	return Result; //. ->
		}
		finally {
			Disconnect();
		}
	}
	
	public byte[] GetCompilationData(int SID, int PID, int CID) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETCOMPILATIONDATA);
		try {
			byte[] Params = new byte[8];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. send parameters
			int Idx = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(PID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(CID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			//. get data size
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			int DataSize = Descriptor;
			//.
			byte[] Result = new byte[DataSize];
        	TNetworkConnection.InputStream_ReadData(ConnectionInputStream, Result,Result.length, context);
        	return Result; //. ->
		}
		finally {
			Disconnect();
		}
	}
	
	public TTileTimestampDescriptor[] GetTilesTimestamps(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, byte[] ExceptTiles, TCanceller Canceller) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V1);
		try {
			byte[] Params = new byte[48];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//.
			TTileTimestampDescriptor[] Result = new TTileTimestampDescriptor[Descriptor];
			byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/];
			for (int I = 0; I < Descriptor; I++) {
				TNetworkConnection.InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length, context);
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
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETTILESTIMESTAMPS_V2);
		try {
			byte[] Params = new byte[56];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//.
			TTileTimestampDescriptor[] Result = new TTileTimestampDescriptor[Descriptor];
			byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/];
			for (int I = 0; I < Descriptor; I++) {
				TNetworkConnection.InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length, context);
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
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETTILES_V3);
		byte[] Params = new byte[48];
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
		System.arraycopy(BA,0, Params,0, BA.length);
		ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
		//. check login
		byte[] DescriptorBA = new byte[4];
		ConnectionInputStream.read(DescriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		//.
		TGetTilesParams Result = new TGetTilesParams();
		Result.TilesCount = Descriptor;
		return Result;
	}
	
	public TTileDescriptor GetTiles_Read(TGetTilesParams Params) throws Exception {
		TTileDescriptor Result = new TTileDescriptor();
		byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/+4/*SizeOf(TileSize)*/];
		TNetworkConnection.InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length, context);
    	int Idx = 0;
    	Result.X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
    	int TileSize = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 4;
    	if (TileSize > 0) {
    		Result.Data = new byte[TileSize];
    		TNetworkConnection.InputStream_ReadData(ConnectionInputStream, Result.Data,Result.Data.length, context);
    	}
    	else
    		Result.Data = null;
    	return Result;
	}
	
	public void GetTiles_End(TGetTilesParams Params) throws Exception {
		Disconnect();
	}
	
	public TGetTilesByTimestampParams GetTilesByTimestamp_Begin(int SID, int PID, int CID, int Level, int Xmn, int Xmx, int Ymn, int Ymx, double HistoryTime, byte[] ExceptTiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_GETTILES_V4);
		byte[] Params = new byte[56];
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
		System.arraycopy(BA,0, Params,0, BA.length);
		ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
		//. check login
		byte[] DescriptorBA = new byte[4];
		ConnectionInputStream.read(DescriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		//.
		TGetTilesByTimestampParams Result = new TGetTilesByTimestampParams();
		Result.TilesCount = Descriptor;
		return Result;
	}
	
	public TTileDescriptor GetTilesByTimestamp_Read(TGetTilesByTimestampParams Params) throws Exception {
		TTileDescriptor Result = new TTileDescriptor();
		byte[] ResultData = new byte[8/*SizeOf(X)*/+8/*SizeOf(Y)*/+8/*SizeOf(Timestamp)*/+4/*SizeOf(TileSize)*/];
		TNetworkConnection.InputStream_ReadData(ConnectionInputStream, ResultData,ResultData.length, context);
    	int Idx = 0;
    	Result.X = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Y = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 8; //. SizeOf(Int64)
    	Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(ResultData,Idx); Idx += 8; 
    	int TileSize = TDataConverter.ConvertBEByteArrayToInt32(ResultData,Idx); Idx += 4;
    	if (TileSize > 0) {
    		Result.Data = new byte[TileSize];
    		TNetworkConnection.InputStream_ReadData(ConnectionInputStream, Result.Data,Result.Data.length, context);
    	}
    	else
    		Result.Data = null;
    	return Result;
	}
	
	public void GetTilesByTimestamp_End(TGetTilesByTimestampParams Params) throws Exception {
		Disconnect();
	}
		
	public double SetTiles(int SID, int PID, int CID, int Level, int SecurityFileID, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_SETTILES);
		try {
			byte[] Params = new byte[24];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
			TNetworkConnection.InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length, context);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	

	public double ReSetTiles(int SID, int PID, int CID, int Level, int SecurityFileID, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_RESETTILES);
		try {
			byte[] Params = new byte[24];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
			TNetworkConnection.InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length, context);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	

	public double ReSetTilesV1(int SID, int PID, int CID, int Level, int SecurityFileID, double ReSetInterval, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_RESETTILES_V1);
		try {
			byte[] Params = new byte[32];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
			TNetworkConnection.InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length, context);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	

	public double ReSetTilesV2(int SID, int PID, int CID, int Level, int SecurityFileID, double ReSetInterval, TTilesPlace TilesPlace, byte[] Tiles) throws Exception {
		Connect(SERVICE_TILESERVER_V1,SERVICE_TILESERVER_COMMAND_RESETTILES_V2);
		try {
			byte[] Params = new byte[32+TilesPlace.ByteArraySize()];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SID);
			System.arraycopy(BA,0, Params,0, BA.length);
			ConnectionOutputStream.write(Params,0,8/*SizeOf(SID)*/);
			//. check login
			byte[] DescriptorBA = new byte[4];
			ConnectionInputStream.read(DescriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
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
			Idx = TilesPlace.ToByteArray(Params, Idx);
			int TilesDescriptor = 0;
			if (Tiles != null)
				TilesDescriptor = Tiles.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(TilesDescriptor);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			ConnectionOutputStream.write(Params);
			if (TilesDescriptor > 0)
				ConnectionOutputStream.write(Tiles);
			//. check response
			ConnectionInputStream.read(DescriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
			CheckMessage(Descriptor);
			//. get timestamp
			byte[] TimestampBA = new byte[8];
			TNetworkConnection.InputStream_ReadData(ConnectionInputStream, TimestampBA,TimestampBA.length, context);
        	double Timestamp = TDataConverter.ConvertBEByteArrayToDouble(TimestampBA,0);
			return Timestamp;
		}
		finally {
			Disconnect();
		}
	}	
}
