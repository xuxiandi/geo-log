package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeSpaceDataServer;

public class TComponentStreamServer extends TGeoScopeSpaceDataServer {

	public static final short SERVICE_COMPONENTSTREAMSERVER		= 3;
	public static final short SERVICE_COMPONENTSTREAMSERVER_V1	= 4;
	//. ComponentStreamServer commands
	public static final int SERVICE_COMPONENTSTREAMSERVER_COMMAND_GETCOMPONENTSTREAM = 0;
	
	public TComponentStreamServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword);
	}
	
	public int ComponentStreamServer_GetComponentStream_Begin(int idTComponent, int idComponent) throws Exception {
		Connect(SERVICE_COMPONENTSTREAMSERVER_V1,SERVICE_COMPONENTSTREAMSERVER_COMMAND_GETCOMPONENTSTREAM);
		byte[] Params = new byte[12];
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(idTComponent);
		System.arraycopy(BA,0, Params,0, BA.length);
		BA = TDataConverter.ConvertInt32ToBEByteArray(idComponent);
		System.arraycopy(BA,0, Params,4, BA.length);
		ConnectionOutputStream.write(Params);
		//. check login
		byte[] DescriptorBA = new byte[4];
		ConnectionInputStream.read(DescriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		//. get items count
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		return Descriptor;
	}	

	public static class TComponentStream {
		public String 	DataID;
		public long 	DataSize;
	}
	
	public TComponentStream ComponentStreamServer_GetComponentStream_Read(String ComponentStreamDataID, RandomAccessFile ComponentStream, TCanceller Canceller, TProgressor Progressor) throws Exception {
		TComponentStream Result = new TComponentStream();
		//. DataID string
		byte[] DescriptorBA = new byte[4];
		ConnectionInputStream.read(DescriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		byte[] BA = new byte[Descriptor];
		String DataID = "";
		if (Descriptor > 0) {
			TNetworkConnection.InputStream_ReadData(ConnectionInputStream, BA,BA.length, context);
        	DataID = new String(BA,"windows-1251");
		}
    	Result.DataID = DataID; 
		if (!DataID.equals(ComponentStreamDataID))
			ComponentStream.setLength(0); //. reset component stream
		//. Data size
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		int DataSize = Descriptor;
		Result.DataSize = DataSize;
		//.
		if (ComponentStream.getFilePointer() > DataSize) 
			throw new Exception("incorrect Data offset"); //. =>
		//. send offset
		Descriptor = (int)ComponentStream.getFilePointer();
		DescriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
		ConnectionOutputStream.write(DescriptorBA);
		//. Data actual size
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		int ActualDataSize = Descriptor;
		//.
		int MaxReadBufferSize = 1024*1024;
		int MinReadBufferSize = 8192;
		int ReadBufferSize = (ActualDataSize/100);
		if (ReadBufferSize > MaxReadBufferSize)
			ReadBufferSize = MaxReadBufferSize;
		else
			if (ReadBufferSize < MinReadBufferSize)
				ReadBufferSize = MinReadBufferSize;
		//.
		byte[] ReadBuffer = new byte[ReadBufferSize]; 
		int Portion = ReadBufferSize;
		while (ActualDataSize > 0) {
			if (Canceller != null)
				Canceller.Check();
			//.
			if (Portion > ActualDataSize) 
				Portion = ActualDataSize;
			int BytesRead = ConnectionInputStream.read(ReadBuffer);
			if (BytesRead <= 0)
				throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
			//.
			ComponentStream.write(ReadBuffer, 0,BytesRead);
			//.
			ActualDataSize -= BytesRead;
			//.
			if (Progressor != null)
				Progressor.DoOnProgress((int)(100*ComponentStream.getFilePointer()/DataSize));
		}
		return Result;
	}
	
	public void ComponentStreamServer_GetComponentStream_End() throws IOException {
		Disconnect();
	}
}
