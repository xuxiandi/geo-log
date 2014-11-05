package com.geoscope.Classes.Data.Stream.Channel;

import android.content.Context;


public class TDataType {

	public static class WrongContainerTypeException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public WrongContainerTypeException() {
			super("");
		}
	}

	
	public TContainerType ContainerType;
	//.
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
	
	public TDataType(TContainerType pContainerType, String pTypeID) {
		ContainerType = pContainerType;
		TypeID = pTypeID;
	}
	
	public TDataType(TContainerType pContainerType, String pTypeID, int pID, String pName, String pInfo, String pValueUnit) {
		ContainerType = pContainerType;
		TypeID = pTypeID;
		ID = (short)pID;
		Name = pName;
		Info = pInfo;
		ValueUnit = pValueUnit;
	}
	
	public String GetContainerTypeID() {
		return ContainerType.GetID();
	}

	public String GetName() {
		if (Name.length() > 0)
			return Name; //. ->
		else
			return TypeID; //. ->
	}

	public String GetValueString(Context context) throws WrongContainerTypeException {
		return ContainerType.GetValueString(context);
	}
	
	public String GetValueUnit(Context context) {
		return ValueUnit;
	}
}
