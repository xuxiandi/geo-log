package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

public class TSavingServerDescriptor {

	public static final int SAVINGSERVER_NATIVEFTP = 0;
	//. 
	public static final int SAVINGSERVER_NATIVEFTP_PORT = 5000;
	
	public int 		ServerType = SAVINGSERVER_NATIVEFTP;
	public String 	Address;
	public int		Port = SAVINGSERVER_NATIVEFTP_PORT;
	public String	BaseFolder = "";
}
