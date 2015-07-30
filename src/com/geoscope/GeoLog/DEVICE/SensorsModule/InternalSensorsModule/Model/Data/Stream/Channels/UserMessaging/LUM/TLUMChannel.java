package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM;

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
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel;

public class TLUMChannel extends TStreamChannel {

	public static final String TypeID = "UserMessaging.LUM";
	
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	
	public TDataType	UserMessagingParameters;
	public TDataType	UserStatus;
	public TDataType	UserMessage;
	public TDataType	UserMessageDelivery;
	
	public TLUMChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, "Default", TMyProfile.class);
		//.
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "user messaging";
		Info = "live user messaging";
		Size = 8192;
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
	public void StartSource() {
	}

	@Override
	public void StopSource() {
	}


	@Override
	public boolean IsActive() {
		return true;
	}
}
