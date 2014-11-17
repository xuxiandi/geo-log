package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
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
	
	@Override
	public TContainerType Clone() {
		TInt32ContainerType Result = new TInt32ContainerType();
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
