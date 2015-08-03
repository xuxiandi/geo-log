package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL;

import java.util.Hashtable;

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
	
	public static class ChannelNotFoundError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public ChannelNotFoundError() {
			super("");
		}
	}
	
	public static class ChannelNotActiveError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public ChannelNotActiveError() {
			super("");
		}
	}
	
	public static class ChannelLockedError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public ChannelLockedError() {
			super("");
		}
	}
	
	public static final int COMMAND_ID = 1;
	
	private static Hashtable<Integer, Boolean> ChannelLockTable = new Hashtable<Integer, Boolean>();  

	public synchronized void LockChannel(int ChannelID) throws ChannelLockedError {
		if (ChannelLockTable.get(ChannelID) != null)
			throw new ChannelLockedError(); //. =>
		ChannelLockTable.put(ChannelID, true);
	}
	
	public synchronized void UnlockChannel(int ChannelID) throws ChannelLockedError {
		ChannelLockTable.remove(ChannelID);
	}
	

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
			if (Channel == null) 
				throw new ChannelNotFoundError(); //. =>
			com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel SourceChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)Channel.SourceChannel_Get();
			if (SourceChannel == null) 
				throw new ChannelNotFoundError(); //. =>
			if (!SourceChannel.IsActive()) 
				throw new ChannelNotActiveError(); //. =>
			//.
			LockChannel(ChannelID);
			try {
				SourceChannel.SetBitrate(Bitrate);
			}
			finally {
				UnlockChannel(ChannelID);
			}
			break; //. >
			
		case 2:
			ChannelID = Integer.parseInt(Command[1]);
			double BitrateMultiplier = Double.parseDouble(Command[2]);
			//.
			Channel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel)InternalControlsModule.ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
			if (Channel == null) 
				throw new ChannelNotFoundError(); //. =>
			SourceChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel)Channel.SourceChannel_Get();
			if (SourceChannel == null) 
				throw new ChannelNotFoundError(); //. =>
			if (!SourceChannel.IsActive()) 
				throw new ChannelNotActiveError(); //. =>
			//.
			LockChannel(ChannelID);
			try {
				SourceChannel.SetBitrate((int)(SourceChannel.GetBitrate()*BitrateMultiplier));
			}
			finally {
				UnlockChannel(ChannelID);
			}
			break; //. >
		}
	}
}
