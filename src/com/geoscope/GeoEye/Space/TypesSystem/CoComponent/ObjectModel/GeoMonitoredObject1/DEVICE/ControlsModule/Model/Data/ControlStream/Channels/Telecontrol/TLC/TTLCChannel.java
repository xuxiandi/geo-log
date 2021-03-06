package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Telecontrol.TLC;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TTLCChannel extends TStreamChannel {

	public static final String TypeID = "Telecontrol.TLC";
	
	public static final int DescriptorSize = 4;
	
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		int ID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += DescriptorSize;
		TDataType DataType = DataTypes.GetItemByID((short)ID);
		Idx = DataType.ContainerType.FromByteArray(BA, Idx);
		return Idx;
	}
	
	private byte[] DataType_ToByteArray(TDataType DataType) throws IOException {
		int DataSize = 4/*SizeOf(ID)*/+DataType.ContainerType.ByteArraySize();
		byte[] Result = new byte[DescriptorSize+DataSize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(DataSize); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = TDataConverter.ConvertInt32ToLEByteArray(DataType.ID); 
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += DescriptorSize;
		BA = DataType.ContainerType.ToByteArray();
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public byte[] DoOnData(TDataType DataType) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		//.
		WaitForConnection();
		//.
		return ProcessCommand(BA);
	}
}
