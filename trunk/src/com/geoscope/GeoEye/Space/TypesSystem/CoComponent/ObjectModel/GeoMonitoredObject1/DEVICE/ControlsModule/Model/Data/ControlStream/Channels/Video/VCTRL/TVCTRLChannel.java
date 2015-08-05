package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDataContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TVCTRLChannel extends TStreamChannel {

	public static final String TypeID = "Video.VCTRL";
	
	public static final int DescriptorSize = com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.DescriptorSize;
	
	public static final int COMMAND_ID 	= com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.COMMAND_ID;

	
	private TDataType Command;
	
	public TVCTRLChannel() {
		super();
		//.
		DataTypes = new TDataTypes();
		//.
		Command = DataTypes.AddItem(new TDataType(new TTimestampedDataContainerType(),	"Command",	this, COMMAND_ID, "","", ""));
		//.
		Hidden = true;
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	protected void CheckCommandResult(int Descriptor) throws Exception {
		switch (Descriptor) {
		
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELNOTFOUND:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelNotFoundError(); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELNOTACTIVE:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelNotActiveError(); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELLOCKED:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelLockedError(); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_SOURCENOTAVAILABLE:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.SourceNotAvailableError(); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_SOURCENONEXCLUSIVEACCESS:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.SourceNonExclusiveAccessError(); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_VALUEOUTOFRANGE:
			throw new com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError(); //. => 
					
		default:
			super.CheckCommandResult(Descriptor); 
			break; //. =>
		}
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
	
	private byte[] DoOnData(TDataType DataType) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		//.
		WaitForConnection();
		//.
		return ProcessCommand(BA);
	}
	
	public byte[] DoCommand(String pCommand) throws Exception {
		byte[] BA = pCommand.getBytes("utf-8");
		synchronized (Command) {
			TTimestampedDataContainerType CT = (TTimestampedDataContainerType)Command.ContainerType; 
			CT.Value.Timestamp = OleDate.UTCCurrentTimestamp();
			CT.Value.Value = BA;
			//.
			return DoOnData(Command); //. ->
		}
	}

	public void Ping() throws Exception {
		DoCommand("0"); //. ping command
	}
	
	public void SetChannelBitrate(int ChannelID, int Bitrate) throws Exception {
		DoCommand("1"+","+Integer.toString(ChannelID)+","+Integer.toString(Bitrate)); 
	}

	public void MultiplyChannelBitrate(int ChannelID, double Multiplier) throws Exception {
		DoCommand("2"+","+Integer.toString(ChannelID)+","+Double.toString(Multiplier)); 
	}

	public static class TPacketsBufferInfo {
		
		public int BuffersCount;
		public int PendingPackets;
		
		public TPacketsBufferInfo(byte[] BA) throws IOException {
			BuffersCount = TDataConverter.ConvertLEByteArrayToInt32(BA, 0);
			PendingPackets = TDataConverter.ConvertLEByteArrayToInt32(BA, 4);
		}
	}
	
	public TPacketsBufferInfo GetChannelSubscriberPacketsBufferInfo(int ChannelID, String UserAccessKey) throws Exception {
		byte[] Response = DoCommand("3"+","+Integer.toString(ChannelID)+","+UserAccessKey);
		return (new TPacketsBufferInfo(Response));
	}
}
