package com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer;


public class TGeographDataServerClient {

	public static final short SERVICE_SETVIDEORECORDERDATA_V1 = 1;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_UNKNOWNSERVICE        = 10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	public static final int MESSAGE_SAVINGDATAERROR       = -101;

	public static class TServerInfo {
	}	
}
