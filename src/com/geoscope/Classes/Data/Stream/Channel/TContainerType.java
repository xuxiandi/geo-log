 package com.geoscope.Classes.Data.Stream.Channel;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.T2DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.T3DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TDoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt32ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestamped6DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt166DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;

public class TContainerType {

	public static TContainerType GetInstance(String ContainerTypeID) {
		if (TInt16ContainerType.ID().equals(ContainerTypeID))
			return (new TInt16ContainerType()); //. -> 
		if (TInt32ContainerType.ID().equals(ContainerTypeID))
			return (new TInt32ContainerType()); //. -> 
		if (TDoubleContainerType.ID().equals(ContainerTypeID))
			return (new TDoubleContainerType()); //. -> 
		if (T2DoubleContainerType.ID().equals(ContainerTypeID))
			return (new T2DoubleContainerType()); //. -> 
		if (T3DoubleContainerType.ID().equals(ContainerTypeID))
			return (new T3DoubleContainerType()); //. -> 
		if (TTimestampedInt16ContainerType.ID().equals(ContainerTypeID))
			return (new TTimestampedInt16ContainerType()); //. -> 
		if (TTimestampedDoubleContainerType.ID().equals(ContainerTypeID))
			return (new TTimestampedDoubleContainerType()); //. -> 
		if (TTimestamped6DoubleContainerType.ID().equals(ContainerTypeID))
			return (new TTimestamped6DoubleContainerType()); //. -> 
		if (TTimestampedInt166DoubleContainerType.ID().equals(ContainerTypeID))
			return (new TTimestampedInt166DoubleContainerType()); //. -> 
		return null;
	}
	
	public static String ID() {
		return "";
	}


	public TContainerType Clone() {
		return null;
	}
	
	public String GetID() {
		return null;
	}

	public TDataType GetDataType(String DataTypeID, TChannel pChannel) {
		return new TDataType(this, DataTypeID, pChannel);
	}
	
	public void SetValue(Object pValue) {
	}
	
	public Object GetValue() {
		return null;
	}
	
	public String GetValueString(Context context) {
		return "";
	}
	
	public int ByteArraySize() {
		return 0;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		return Idx;
	}
	
	public byte[] ToByteArray() throws IOException {
		return null;
	}
}
