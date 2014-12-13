package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt32ContainerType;

public class TUserMessageDeliveryDataType extends TDataType {

	public static String ID() {
		return "UserMessageDelivery";
	}
	
	
	public TUserMessageDeliveryDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserMessageDeliveryDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public TTimestampedInt32ContainerType.TValue ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedInt32ContainerType)
			return ((TTimestampedInt32ContainerType)ContainerType).Value.Clone(); //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
