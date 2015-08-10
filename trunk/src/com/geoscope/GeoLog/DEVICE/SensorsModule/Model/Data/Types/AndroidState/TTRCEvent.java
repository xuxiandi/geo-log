package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Types.AndroidState;

public class TTRCEvent {

	public double 	Timestamp;
	public short 	Type;
	public int 		Tag;
	public String	Message;
	
	public TTRCEvent(double pTimestamp, short pType, int pTag, String pMessage) {
		Timestamp = pTimestamp;
		Type = pType;
		Tag = pTag;
		Message = pMessage;
	}
}
