package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.GeoEye.R;

public class TUserStatusDataType extends TDataType {

	public static String ID() {
		return "UserStatus";
	}

	public static final short USERSTATUS_UNKNOWN		= 0;
	public static final short USERSTATUS_OFFLINE		= 1;
	public static final short USERSTATUS_ONLINE			= 2;	
	public static final short USERSTATUS_CALLING		= 3;	
	public static final short USERSTATUS_NOTAVAILABLE	= 4;
	public static final short USERSTATUS_AVAILABLE		= 5;
	public static final short USERSTATUS_COMPOSING		= 6;
	public static final short USERSTATUS_IDLE			= 7;
	public static final short USERSTATUS_IGNORING		= 8;
	public static final short USERSTATUS_CLOSING		= 9;
	//.
	public static String USERSTATUS(short UserStatus, Context context) {
		switch (UserStatus) {
		
		case USERSTATUS_UNKNOWN:
			return "?"; //. ->
		
		case USERSTATUS_OFFLINE:
			return context.getString(R.string.SOffline1); //. ->
		
		case USERSTATUS_ONLINE:
			return context.getString(R.string.SOnline1); //. ->
		
		case USERSTATUS_CALLING:
			return context.getString(R.string.SCalling1); //. ->
		
		case USERSTATUS_NOTAVAILABLE:
			return context.getString(R.string.SNotAvailable1); //. ->
		
		case USERSTATUS_AVAILABLE:
			return context.getString(R.string.SAvailable2); //. ->
		
		case USERSTATUS_COMPOSING:
			return context.getString(R.string.SComposing); //. ->
		
		case USERSTATUS_IDLE:
			return context.getString(R.string.SIdle1); //. ->
		
		case USERSTATUS_IGNORING:
			return context.getString(R.string.SIgnore); //. ->
			
		case USERSTATUS_CLOSING:
			return context.getString(R.string.SClosing); //. ->
		
		default:
			return null; //. ->
		}
	}
	
	
	public TUserStatusDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserStatusDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public TTimestampedInt16ContainerType.TValue ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedInt16ContainerType)
			return ((TTimestampedInt16ContainerType)ContainerType).Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
