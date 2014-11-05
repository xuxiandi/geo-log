package com.geoscope.Classes.Data.Stream.Channel;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.T2DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.T3DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TDoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedDoubleContainerType;

public class TContainerType {

	public static TContainerType GetInstance(String ContainerTypeID) {
		if (TDoubleContainerType.ID().equals(ContainerTypeID))
			return (new TDoubleContainerType()); //. -> 
		if (T2DoubleContainerType.ID().equals(ContainerTypeID))
			return (new T2DoubleContainerType()); //. -> 
		if (T3DoubleContainerType.ID().equals(ContainerTypeID))
			return (new T3DoubleContainerType()); //. -> 
		if (TTimestampedDoubleContainerType.ID().equals(ContainerTypeID))
			return (new TTimestampedDoubleContainerType()); //. -> 
		return null;
	}
	
	public static String ID() {
		return "";
	}


	public String GetID() {
		return null;
	}

	public TDataType GetDataType(String DataTypeID) {
		return new TDataType(this, DataTypeID);
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
