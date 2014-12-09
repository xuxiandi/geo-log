package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserStatusDataType;

public class TTimestampedInt16ContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedInt16";
	}
	
	public static class TValue {
		
		public double 	Timestamp;
		public short 	Value;
		
		public TValue() {
		}

		public TValue(double pTimestamp, short pValue) {
			Timestamp = pTimestamp;
			Value = pValue;
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedInt16ContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TTimestampedInt16ContainerType Result = new TTimestampedInt16ContainerType();
		Result.Value.Timestamp = Value.Timestamp;
		Result.Value.Value = Value.Value;
		return Result;
	}
	
	@Override
	public String GetID() {
		return ID();
	}

	@Override
	public TDataType GetDataType(String DataTypeID, TChannel pChannel) {
		if (TUserStatusDataType.ID().equals(DataTypeID))
			return new TUserStatusDataType(this, pChannel); //. -> 
		else
			return super.GetDataType(DataTypeID, pChannel);  //. ->
	}
	
	@Override
	public void SetValue(Object pValue) {
		Value = (TValue)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Value;
	}
	
	@Override
	public String GetValueString(Context context) {
		return Short.toString(Value.Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 8/*SizeOf(Timestamp)*/+2/*SizeOf(Value)*/;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Timestamp)
		Value.Value = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt16ToLEByteArray(Value.Value);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
