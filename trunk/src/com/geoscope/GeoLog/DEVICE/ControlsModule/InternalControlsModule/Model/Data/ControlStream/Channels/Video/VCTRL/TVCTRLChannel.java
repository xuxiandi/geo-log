package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDataContainerType;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.TStreamChannel;

public class TVCTRLChannel extends TStreamChannel {

	public static final String TypeID = "Video.VCTRL";
		
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	public static final int COMMAND_ID = 1;
	

	protected TDataType Command;
	
	public TVCTRLChannel(TInternalControlsModule pInternalControlsModule, int pID) throws Exception {
		super(pInternalControlsModule, pID, "DefaultSpeaker", TMyProfile.class);
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
		Command = DataTypes.AddItem(new TDataType(new TTimestampedDataContainerType(),	"Command",	this, COMMAND_ID, "","", ""));
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}

	public void DataType_SetValue(TDataType DataType) throws Exception {
		switch (DataType.ID) {
		
		case COMMAND_ID:
			TTimestampedDataContainerType TimestampedData = (TTimestampedDataContainerType)DataType.ContainerType;
			if (TimestampedData.Value.Value.length > 0) {
				String _Command = new String(TimestampedData.Value.Value, "utf-8");
				String[] Command = _Command.split(",");
				//.
				ProcessCommand(Command);
			}
			break; //. >
			
		default:
			throw new Exception("unknown action ID"); //. =>
		}
	}
	
	private void ProcessCommand(String[] Command) throws Exception {
		if ((Command == null) || (Command.length == 0))
			return; //. ->
		int Version = Integer.parseInt(Command[0]);
		switch (Version) {
		
		case 1:
			int ChannelID = Integer.parseInt(Command[1]);
			int Bitrate = Integer.parseInt(Command[2]);
			//.
			com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel Channel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel)InternalControlsModule.ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
			if (Channel != null) {
				com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel SourceChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)Channel.SourceChannel_Get();
				SourceChannel.SetBitrate(Bitrate);
			}
			break; //. >
		}
	}
}
