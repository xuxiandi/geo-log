package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedTaggedDataContainerType;

public class TUserMessageDataType extends TDataType {

	public static String ID() {
		return "UserMessage";
	}
	
	public static final short TYPE_UNKNOWN 			= 0;
	public static final short TYPE_OPENSESSION 		= 1;
	public static final short TYPE_CLOSESESSION 	= 2;
	public static final short TYPE_OPENSESSION_XML 	= 3;
	public static final short TYPE_CLOSESESSION_XML = 4;
	public static final short TYPE_ERROR_INT32 		= 5;
	public static final short TYPE_ERROR_XML 		= 6;
	public static final short TYPE_TEXT_UTF8 		= 1001;
	public static final short TYPE_TEXT_XML 		= 1002;
	public static final short TYPE_IMAGE_JPG 		= 2001;
	public static final short TYPE_IMAGE_PNG 		= 2002;
	public static final short TYPE_IMAGE_DRW 		= 2003;
	public static final short TYPE_AUDIO_WAV 		= 3001;
	public static final short TYPE_AUDIO_MP3 		= 3002;
	public static final short TYPE_VIDEO_3GP 		= 4001;
	public static final short TYPE_VIDEO_MP4 		= 4002;
	//.
	public static boolean TYPE_OPENSESSION(short Type) {
		return ((Type == TYPE_OPENSESSION) || (Type == TYPE_OPENSESSION_XML));
	}
	//.
	public static boolean TYPE_CLOSESESSION(short Type) {
		return ((Type == TYPE_CLOSESESSION) || (Type == TYPE_CLOSESESSION_XML));
	}
	//.
	public static boolean TYPE_ERROR(short Type) {
		return ((Type == TYPE_ERROR_INT32) || (Type == TYPE_ERROR_XML));
	}
	//.
	public static boolean TYPE_IMAGE(short Type) {
		return ((Type == TYPE_IMAGE_JPG) || (Type == TYPE_IMAGE_PNG) || (Type == TYPE_IMAGE_DRW));
	}
	

	public TUserMessageDataType() {
		super();
	}
	
	public TUserMessageDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserMessageDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public TTimestampedTypedTaggedDataContainerType.TValue ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedTypedTaggedDataContainerType)
			return ((TTimestampedTypedTaggedDataContainerType)ContainerType).Value.Clone(); //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
