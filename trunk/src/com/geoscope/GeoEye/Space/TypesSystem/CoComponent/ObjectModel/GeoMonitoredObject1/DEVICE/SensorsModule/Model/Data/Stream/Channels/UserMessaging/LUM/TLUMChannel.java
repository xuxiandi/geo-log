package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TLUMChannel extends TStreamChannel {

	public static final String TypeID = "UserMessaging.LUM";

	public static final int DescriptorSize = 4;
	
	public static class TDoOnDataHandler {
		
		public void DoOnData(TDataType DataType) {
		}
	}
	
	
	public TDoOnDataHandler OnDataHandler = null;
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(Socket Connection, InputStream pInputStream, OutputStream pOutputStream, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws IOException {
		byte[] TransferBuffer = new byte[DescriptorSize];
		int Size;
		int BytesRead;
		int IdleTimeoutCount = 0; 
		Connection.setSoTimeout(StreamingTimeout);
		while (!Canceller.flCancel) {
			try {
                BytesRead = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (BytesRead <= 0) 
                	break; //. >
				IdleTimeoutCount = 0;
			}
			catch (SocketTimeoutException E) {
				IdleTimeoutCount++;
				if (IdleTimeoutCount >= IdleTimeoutCounter) {
					IdleTimeoutCount = 0;
					OnIdleHandler.DoOnIdle(Canceller);
				}
				//.
				continue; //. ^
			}
			if (BytesRead != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				BytesRead = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead <= 0) 
                	break; //. >
				//. parse and process
                try {
                	ParseFromByteArrayAndProcess(TransferBuffer, 0);
                }
                catch (Exception E) {
                	break; //. >
                }
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		short ID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += DescriptorSize;
		if (DataTypes != null) {
			TDataType DataType = DataTypes.GetItemByID(ID);
			if (DataType != null) {
				Idx = DataType.ContainerType.FromByteArray(BA, Idx);
				//.
				if (OnDataHandler != null)
					OnDataHandler.DoOnData(DataType);
			}
		}
		return Idx;
	}
}
