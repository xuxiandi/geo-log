package com.geoscope.Classes.Data.Stream.Channel;

public class TChannelDescriptor extends TChannel {

	public String TypeID;
	
	public TChannelDescriptor(String pTypeID) {
		super();
		//.
		TypeID = pTypeID;
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
