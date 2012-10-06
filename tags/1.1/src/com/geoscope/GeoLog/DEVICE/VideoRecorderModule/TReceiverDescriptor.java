package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

public class TReceiverDescriptor {

	public static final int RECEIVER_NATIVE = 0;
	public static final int RECEIVER_VLC = 1;
	public static final int RECEIVER_DEFAULT = RECEIVER_NATIVE;
	//. 
	public static final int RECEIVER_NATIVE_AUDIOPORT = 2011;
	public static final int RECEIVER_NATIVE_VIDEOPORT = 2012;
	
	public int 		ReceiverType = RECEIVER_DEFAULT;
	public String 	Address;
	public int		AudioPort;
	public int		VideoPort;
}
