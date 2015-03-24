package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

public class TSensorMeterDescriptor {
	
	public String ID;
	//.
	public String TypeID;
	public String ContainerTypeID;
	//.
	public String Name;
	public String Info;
	
	public TSensorMeterDescriptor() {
		ID = "";
		//.
		TypeID = "";
		ContainerTypeID = "";
		//.
		Name = "";
		Info = "";
	}
	
	public TSensorMeterDescriptor(String pID, String pTypeID, String pContainerTypeID, String pName, String pInfo) {
		ID = pID;
		//.
		TypeID = pTypeID;
		ContainerTypeID = pContainerTypeID;
		//.
		Name = pName;
		Info = pInfo;
	}
}