package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Audio.VC;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDataContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TVCChannel extends TStreamChannel {

	public static final String TypeID = "Audio.VC";
	
	public static final int DescriptorSize = com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel.DescriptorSize;
	
	public static final int SET_VOICECOMMANDS_ID 	= com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel.SET_VOICECOMMANDS_ID;
	public static final int DO_VOICECOMMAND_ID 		= com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel.DO_VOICECOMMAND_ID;

	
	private TDataType SetVoiceCommands;
	private TDataType DoVoiceCommand;
	
	public TVCChannel() {
		super();
		//.
		DataTypes = new TDataTypes();
		//.
		SetVoiceCommands 	= DataTypes.AddItem(new TDataType(new TTimestampedDataContainerType(),	"SetVoiceCommands",	this, SET_VOICECOMMANDS_ID, "","", ""));
		DoVoiceCommand 		= DataTypes.AddItem(new TDataType(new TTimestampedDataContainerType(), 	"DoVoiceCommand", 	this, DO_VOICECOMMAND_ID, 	"","", ""));
	}
	
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
	
	private void DoOnData(TDataType DataType) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		//.
		WaitForConnection();
		//.
		ProcessCommand(BA);
	}
	
	public void SetVoiceCommands(byte[] BA) throws Exception {
		synchronized (SetVoiceCommands) {
			TTimestampedDataContainerType CT = (TTimestampedDataContainerType)SetVoiceCommands.ContainerType; 
			CT.Value.Timestamp = OleDate.UTCCurrentTimestamp();
			CT.Value.Value = BA;
			//.
			DoOnData(SetVoiceCommands);
		}
	}
	
	public void DoVoiceCommand(byte[] BA) throws Exception {
		synchronized (DoVoiceCommand) {
			TTimestampedDataContainerType CT = (TTimestampedDataContainerType)DoVoiceCommand.ContainerType; 
			CT.Value.Timestamp = OleDate.UTCCurrentTimestamp();
			CT.Value.Value = BA;
			//.
			DoOnData(DoVoiceCommand);
		}
	}
}
