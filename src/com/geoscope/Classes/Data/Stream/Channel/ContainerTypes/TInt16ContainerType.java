package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryHealthDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryPlugTypeDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryStatusDataType;

public class TInt16ContainerType extends TContainerType {
	
	public static String ID() {
		return "Int16";
	}
	

	public short Value;
	
	public TInt16ContainerType() {
		super();
	}
	
	public String GetID() {
		return ID();
	}

	@Override
	public TDataType GetDataType(String DataTypeID) {
		if (TBatteryHealthDataType.ID().equals(DataTypeID))
			return new TBatteryHealthDataType(this); //. -> 
		if (TBatteryStatusDataType.ID().equals(DataTypeID))
			return new TBatteryStatusDataType(this); //. -> 
		if (TBatteryPlugTypeDataType.ID().equals(DataTypeID))
			return new TBatteryPlugTypeDataType(this); //. -> 
		else
			return super.GetDataType(DataTypeID);  //. ->
	}
	
	@Override
	public void SetValue(Object pValue) {
		Value = (Short)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Short.valueOf(Value);
	}
	
	@Override
	public String GetValueString(Context context) {
		return Short.toString(Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 2; //. SizeOf(Value)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		return TDataConverter.ConvertInt16ToLEByteArray(Value);
	}
}
