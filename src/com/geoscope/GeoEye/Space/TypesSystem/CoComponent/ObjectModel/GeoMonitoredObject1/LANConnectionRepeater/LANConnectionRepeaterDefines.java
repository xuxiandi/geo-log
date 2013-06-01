package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

public class LANConnectionRepeaterDefines {

	public static final short SERVICE_NONE							= 0;
	public static final short SERVICE_LANCONNECTION_DESTINATION		= 7;
	public static final short SERVICE_LANCONNECTION_DESTINATION_V1	= 8;

	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_UNKNOWNSERVICE        = -10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	//. custom error messages
	public static final int MESSAGE_LANCONNECTIONISNOTFOUND       = -101;
	
	public static final int CONNECTIONTYPE_NORMAL 		= 0;
	public static final int CONNECTIONTYPE_PACKETTED 	= 1;
	
	public static final int ServerReadWriteTimeout = 1000*60; //. Seconds
}
