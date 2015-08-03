package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDataContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCanceller;
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
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TCanceller Canceller) throws Exception {
		while (!Canceller.flCancel) {
			Thread.sleep(100);
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		int ID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += DescriptorSize;
		TDataType DataType = DataTypes.GetItemByID((short)ID);
		Idx = DataType.ContainerType.FromByteArray(BA, Idx);
		return Idx;
	}

	@Override
	protected void CheckCommandResult(int Descriptor) throws Exception {
		switch (Descriptor) {
		
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELNOTFOUND:
			throw new Exception("Channel not found"); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELNOTACTIVE:
			throw new Exception("Channel not active"); //. => 
					
		case com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.RESULT_CHANNELLOCKED:
			throw new Exception("Channel locked"); //. => 
					
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
	
	private void DoOnData(TDataType DataType) throws Exception {
		byte[] BA = DataType_ToByteArray(DataType);
		//.
		WaitForConnection();
		//.
		ProcessCommand(BA);
	}
	
	public void DoCommand(String pCommand) throws Exception {
		byte[] BA = pCommand.getBytes("utf-8");
		synchronized (Command) {
			TTimestampedDataContainerType CT = (TTimestampedDataContainerType)Command.ContainerType; 
			CT.Value.Timestamp = OleDate.UTCCurrentTimestamp();
			CT.Value.Value = BA;
			//.
			DoOnData(Command);
		}
	}
}
