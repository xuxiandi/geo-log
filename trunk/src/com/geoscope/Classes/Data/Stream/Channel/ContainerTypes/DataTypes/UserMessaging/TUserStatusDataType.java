package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;

public class TUserStatusDataType extends TDataType {

	public static String ID() {
		return "UserStatus";
	}

	public static final short USERSTATUS_UNKNOWN		= 0;
	public static final short USERSTATUS_OFFLINE		= 1;
	public static final short USERSTATUS_ONLINE			= 2;	
	public static final short USERSTATUS_COMPOSING		= 3;
	public static final short USERSTATUS_IDLE			= 4;
	public static final short USERSTATUS_BUSY			= 5;
	public static final short USERSTATUS_AWAY			= 6;
	
	
	public TUserStatusDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserStatusDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public int ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedInt16ContainerType)
			return ((TTimestampedInt16ContainerType)ContainerType).Value.Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
