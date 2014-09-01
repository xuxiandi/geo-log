package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.IOException;
import java.io.InterruptedIOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeSpaceDataServer;

public class TDataStreamServer extends TGeoScopeSpaceDataServer {

	public static final short SERVICE_COMPONENTSTREAMSERVER		= 3;
	public static final short SERVICE_COMPONENTSTREAMSERVER_V1	= 4;
	//. ComponentStreamServer commands
	public static final int SERVICE_COMPONENTSTREAMSERVER_COMMAND_GETDATASTREAM      	= 1;
	public static final int SERVICE_COMPONENTSTREAMSERVER_COMMAND_GETDATASTREAM_V1		= 2;
	
	public static class TStreamReadHandler {
		
		public void DoOnRead(byte[] Buffer, int BufferSize, TCanceller Canceller) {
		}
	}
	
	public static class TStreamReadIdleHandler {
		
		public void DoOnIdle(TCanceller Canceller) {
		}
	}
	
	public TDataStreamServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword);
	}
	
	public void ComponentStreamServer_GetDataStreamV1_Begin(int idTComponent, long idComponent) throws Exception {
		Connect(SERVICE_COMPONENTSTREAMSERVER_V1,SERVICE_COMPONENTSTREAMSERVER_COMMAND_GETDATASTREAM_V1);
		byte[] Params = new byte[12];
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(idTComponent);
		System.arraycopy(BA,0, Params,0, BA.length);
		BA = TDataConverter.ConvertInt64ToLEByteArray(idComponent);
		System.arraycopy(BA,0, Params,4, BA.length);
		ConnectionOutputStream.write(Params);
		//. check login
		byte[] DescriptorBA = new byte[4];
		ConnectionInputStream.read(DescriptorBA);
		int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
	}	

	public void ComponentStreamServer_GetDataStreamV1_Read(int StreamChannelID, int Timeout, TCanceller Canceller, TStreamReadHandler StreamReadHandler, TStreamReadIdleHandler StreamReadIdleHandler) throws Exception {
		int Descriptor;
		long Descriptor64;
		byte[] DescriptorBA = new byte[4];
		byte[] Descriptor64BA = new byte[8];
		//. send stream channel ID
		Descriptor = StreamChannelID;
		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
		ConnectionOutputStream.write(DescriptorBA);
		//. check result
		ConnectionInputStream.read(DescriptorBA);
		Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
		CheckMessage(Descriptor);
		//.
		long StreamChannelPosition = -1; //. unknown position
		while (!Canceller.flCancel) {
			//. get stream size
			ConnectionInputStream.read(Descriptor64BA);
			Descriptor64 = TDataConverter.ConvertLEByteArrayToInt64(Descriptor64BA,0);
			CheckMessage(Descriptor64);
			long StreamChannelSize = Descriptor64;
			//.
			if (StreamChannelPosition < 0) 
				StreamChannelPosition = StreamChannelSize;
			//. send stream position
			if (!Canceller.flCancel) {
				Descriptor64 = StreamChannelPosition;
				Descriptor64BA = TDataConverter.ConvertInt64ToLEByteArray(Descriptor64);
				ConnectionOutputStream.write(Descriptor64BA);
			}
			else {
			    Descriptor64 = -1; //. Exit marker
				Descriptor64BA = TDataConverter.ConvertInt64ToLEByteArray(Descriptor64);
				ConnectionOutputStream.write(Descriptor64BA);
				return; //. ->
			}
		    //. stream actual size to load
			ConnectionInputStream.read(Descriptor64BA);
			Descriptor64 = TDataConverter.ConvertLEByteArrayToInt64(Descriptor64BA,0);
			CheckMessage(Descriptor64);
			long ActualStreamChannelSize = Descriptor64;
			//.
	        Connection.setSoTimeout(Timeout);
			//.
			double CheckPointInterval = (1.0/(3600.0*24))*300;
			double CheckPointBaseTime = OleDate.UTCCurrentTimestamp();
			int ReadBufferSize = 1024*1024;
			byte[] ReadBuffer = new byte[ReadBufferSize];
			while (true) {
				try {
					int BytesRead = ConnectionInputStream.read(ReadBuffer);
					if (BytesRead <= 0)
						throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
					//.
				    StreamChannelPosition += BytesRead;
				    ActualStreamChannelSize -= BytesRead;
				    //.
				    if (StreamReadHandler != null)
				    	StreamReadHandler.DoOnRead(ReadBuffer,BytesRead, Canceller);
				    else
				    	Thread.sleep(100);
				    //.
				    if (!Canceller.flCancel && ((OleDate.UTCCurrentTimestamp()-CheckPointBaseTime) > CheckPointInterval)) {
		        		Descriptor = 0; //. checkpoint descriptor
		        		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
		        		ConnectionOutputStream.write(DescriptorBA);
				    	//.
				    	CheckPointBaseTime = OleDate.UTCCurrentTimestamp();
				    }
				}
		        catch (InterruptedIOException IIOE) {
				    if (StreamReadIdleHandler != null)
				    	StreamReadIdleHandler.DoOnIdle(Canceller);
		        }
	            if (Canceller.flCancel) {
	        		Descriptor = -1; //. end of reading stream descriptor
	        		DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
	        		ConnectionOutputStream.write(DescriptorBA);
	        		return; //. ->
	            }
			}
		}
	}
	
	public void ComponentStreamServer_GetDataStreamV1_End() throws IOException {
		Disconnect();
	}
}
