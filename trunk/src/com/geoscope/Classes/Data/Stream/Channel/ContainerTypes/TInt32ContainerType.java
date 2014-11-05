package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryHealthDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryPlugTypeDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.TBatteryStatusDataType;

public class TInt32ContainerType extends TContainerType {
	
	public static String ID() {
		return "Int32";
	}
	

	public int Value;
	
	public TInt32ContainerType() {
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
		Value = (Integer)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Integer.valueOf(Value);
	}
	
	@Override
	public String GetValueString(Context context) {
		return Integer.toString(Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 4; //. SizeOf(Value)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		return TDataConverter.ConvertInt32ToLEByteArray(Value);
	}
}
