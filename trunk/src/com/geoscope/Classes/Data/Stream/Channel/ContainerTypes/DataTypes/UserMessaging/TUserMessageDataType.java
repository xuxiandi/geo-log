package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedDataContainerType;

public class TUserMessageDataType extends TDataType {

	public static String ID() {
		return "UserMessage";
	}
	
	public static final short TYPE_UNKNOWN 			= 0;
	public static final short TYPE_STRING_UTF8 		= 1;
	public static final short TYPE_XML 				= 2;
	public static final short TYPE_IMAGE_JPG 		= 1001;
	public static final short TYPE_IMAGE_PNG 		= 1002;
	public static final short TYPE_IMAGE_DRW 		= 1003;
	public static final short TYPE_AUDIO_WAV 		= 2001;
	public static final short TYPE_AUDIO_MP3 		= 2002;
	public static final short TYPE_VIDEO_3GP 		= 3001;
	public static final short TYPE_VIDEO_MP4 		= 3002;
	

	public TUserMessageDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserMessageDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public TTimestampedTypedDataContainerType.TValue ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedTypedDataContainerType)
			return ((TTimestampedTypedDataContainerType)ContainerType).Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
