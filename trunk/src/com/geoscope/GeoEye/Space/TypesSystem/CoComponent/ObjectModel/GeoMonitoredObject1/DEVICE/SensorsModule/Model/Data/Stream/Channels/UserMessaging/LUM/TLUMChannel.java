package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt32ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedDataContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedTaggedDataContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDeliveryDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessagingParametersDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserStatusDataType;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TLUMChannel extends TStreamChannel {

	public static final String TypeID = "UserMessaging.LUM";

	public static final int DescriptorSize = 4;

	
	public TDataType	UserMessagingParameters;
	public TDataType	UserStatus;
	public TDataType	UserMessage;
	public TDataType	UserMessageDelivery;
	
	public TDoOnDataHandler OnDataHandler = null;
	
	public TLUMChannel() {
		Enabled = true;
		Kind = TChannel.CHANNEL_KIND_IN;
		DataFormat = 0;
		Name = "user messaging";
		Info = "live user messaging";
		Size = 0;
		Configuration = "";
		Parameters = "";
		//.
		DataTypes = new TDataTypes();
		UserMessagingParameters = DataTypes.AddItem(new TUserMessagingParametersDataType(new TTimestampedTypedDataContainerType(), this, 	1, "","", ""));		
		UserStatus 				= DataTypes.AddItem(new TUserStatusDataType(new TTimestampedInt16ContainerType(), this, 					2, "","", ""));		
		UserMessage 			= DataTypes.AddItem(new TUserMessageDataType(new TTimestampedTypedTaggedDataContainerType(), this, 			3, "","", ""));		
		UserMessageDelivery		= DataTypes.AddItem(new TUserMessageDeliveryDataType(new TTimestampedInt32ContainerType(), this, 			4, "","", ""));
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(Socket Connection, InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		byte[] TransferBuffer = new byte[DescriptorSize];
		int Size;
		int BytesRead,BytesRead1;
		int IdleTimeoutCount = 0; 
		int _StreamingTimeout = StreamingTimeout*IdleTimeoutCounter;
		while (!Canceller.flCancel) {
			try {
				if (Connection != null)
					Connection.setSoTimeout(StreamingTimeout);
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
			//.
			BytesRead1 = 0;
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				if (Connection != null)
					Connection.setSoTimeout(_StreamingTimeout);
				BytesRead1 = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead1 <= 0) 
                	break; //. >
				//. parse and process
            	ParseFromByteArrayAndProcess(TransferBuffer, 0, Size);
            	//.
            	OnProgressHandler.DoOnProgress(BytesRead1, Canceller);
			}
			//.
			if (pInputStreamSize > 0) {
				pInputStreamSize -= (BytesRead+BytesRead1);
				if (pInputStreamSize <= 0)
                	break; //. >
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short ID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; /*SizeOf(ID)*/
		if (DataTypes != null) {
			TDataType DataType = DataTypes.GetItemByID(ID);
			if (DataType != null) {
				Idx = DataType.ContainerType.FromByteArray(BA, Idx);
				//.
				if (OnDataHandler != null)
					OnDataHandler.DoOnData(DataType);
				else
					throw new Exception("DataTypeHandler is not set, TypeID: "+Short.toString(ID)); //. =>
			}
			else
				throw new Exception("DataType is not found, TypeID: "+Short.toString(ID)); //. =>
		}
		return Idx;
	}
}
