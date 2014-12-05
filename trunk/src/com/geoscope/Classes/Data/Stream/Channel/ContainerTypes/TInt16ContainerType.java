package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryHealthDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryPlugTypeDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryStatusDataType;

public class TInt16ContainerType extends TContainerType {
	
	public static String ID() {
		return "Int16";
	}
	

	public short Value;
	
	public TInt16ContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TInt16ContainerType Result = new TInt16ContainerType();
		Result.Value = Value;
		return Result;
	}
	
	@Override
	public String GetID() {
		return ID();
	}

	@Override
	public TDataType GetDataType(String DataTypeID, TChannel pChannel) {
		if (TBatteryHealthDataType.ID().equals(DataTypeID))
			return new TBatteryHealthDataType(this, pChannel); //. -> 
		if (TBatteryStatusDataType.ID().equals(DataTypeID))
			return new TBatteryStatusDataType(this, pChannel); //. -> 
		if (TBatteryPlugTypeDataType.ID().equals(DataTypeID))
			return new TBatteryPlugTypeDataType(this, pChannel); //. -> 
		else
			return super.GetDataType(DataTypeID, pChannel);  //. ->
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
