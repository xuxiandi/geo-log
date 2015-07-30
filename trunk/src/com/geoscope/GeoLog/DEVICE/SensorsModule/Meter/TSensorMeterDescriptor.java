package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter;

public class TSensorMeterDescriptor {
	
	public String ID;
	//.
	public String TypeID;
	public String ContainerTypeID;
	//.
	public String LocationID;
	//.
	public String Name;
	public String Info;
	//.
	public String Configuration = "";
	public String Parameters = "";
	
	public TSensorMeterDescriptor() {
		ID = "";
		//.
		TypeID = "";
		ContainerTypeID = "";
		//.
		LocationID = "";
		//.
		Name = "";
		Info = "";
	}
	
	public TSensorMeterDescriptor(String pID, String pTypeID, String pContainerTypeID, String pLocationID, String pName, String pInfo) {
		ID = pID;
		//.
		TypeID = pTypeID;
		ContainerTypeID = pContainerTypeID;
		//.
		LocationID = pLocationID;
		//.
		Name = pName;
		Info = pInfo;
	}
}