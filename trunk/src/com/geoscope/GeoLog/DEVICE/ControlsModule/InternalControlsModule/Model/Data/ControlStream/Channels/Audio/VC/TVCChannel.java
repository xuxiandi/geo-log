package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDataContainerType;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.TStreamChannel;

public class TVCChannel extends TStreamChannel {

	public static final String TypeID = "Audio.VC";
		
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	public static final int SET_VOICECOMMANDS_ID 	= 1;
	public static final int DO_VOICECOMMAND_ID 		= 2;
	

	protected TDataType SetVoiceCommands;
	protected TDataType DoVoiceCommand;
	
	public TVCChannel(TInternalControlsModule pInternalControlsModule, int pID) throws Exception {
		super(pInternalControlsModule, pID, TMyProfile.class);
		//.
		Kind = TChannel.CHANNEL_KIND_INOUT;
		DataFormat = 0;
		Name = "Audio voice commands";
		Info = "";
		Size = 1024*1024*1;
		Configuration = "";
		Parameters = "";
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

	public void DataType_SetValue(TDataType DataType) throws Exception {
		switch (DataType.ID) {
		
		case SET_VOICECOMMANDS_ID:
			TTimestampedDataContainerType TimestampedData = (TTimestampedDataContainerType)DataType.ContainerType;
			if (TimestampedData.Value.Value.length > 0) 
				InternalControlsModule.ControlsModule.Device.AudioModule.AudioFiles_FromByteArray(TimestampedData.Value.Value);
			break; //. >

		case DO_VOICECOMMAND_ID:
			TimestampedData = (TTimestampedDataContainerType)DataType.ContainerType;
			if (TimestampedData.Value.Value.length > 0) 
				InternalControlsModule.ControlsModule.Device.AudioModule.AudioFileMessageValue.FromByteArray(TimestampedData.Value.Value, new TIndex());
			break; //. >
			
		default:
			throw new Exception("unknown action ID"); //. =>
		}
	}
}
