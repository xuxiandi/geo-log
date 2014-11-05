package com.geoscope.Classes.Data.Stream.Channel;

import com.geoscope.Classes.Data.Stream.Channel.DataTypes.T2DoubleDataType;
import com.geoscope.Classes.Data.Stream.Channel.DataTypes.T3DoubleDataType;
import com.geoscope.Classes.Data.Stream.Channel.DataTypes.TDoubleDataType;
import com.geoscope.Classes.Data.Stream.Channel.DataTypes.TTimestampedDoubleDataType;

public class TDataType extends TDataContainerType {

	public static TDataType GetDataType(String ContainerTypeID) {
		if (TDoubleDataType.ContainerTypeID().equals(ContainerTypeID))
			return (new TDoubleDataType()); //. -> 
		if (T2DoubleDataType.ContainerTypeID().equals(ContainerTypeID))
			return (new T2DoubleDataType()); //. -> 
		if (T3DoubleDataType.ContainerTypeID().equals(ContainerTypeID))
			return (new T3DoubleDataType()); //. -> 
		if (TTimestampedDoubleDataType.ContainerTypeID().equals(ContainerTypeID))
			return (new TTimestampedDoubleDataType()); //. -> 
		return null;
	}
	
	
	public String TypeID = "";
	//.
	public short ID = 0;
	//.
	public int Index = 0;
	//.
	public String Name = "";
	public String Info = "";
	//.
	public String ValueUnit = "";
	
	public TDataType() {
	}
	
	public TDataType(String pTypeID, int pID, String pName, String pInfo, String pValueUnit) {
		TypeID = pTypeID;
		ID = (short)pID;
		Name = pName;
		Info = pInfo;
		ValueUnit = pValueUnit;
	}
	
	public String GetName() {
		if (Name.length() > 0)
			return Name; //. ->
		else
			return TypeID; //. ->
	}

	public String GetValueUnit() {
		return ValueUnit;
	}
}
