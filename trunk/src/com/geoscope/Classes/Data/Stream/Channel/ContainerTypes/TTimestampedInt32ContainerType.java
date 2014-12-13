package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDeliveryDataType;

public class TTimestampedInt32ContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedInt32";
	}
	
	public static class TValue {
		
		public double 	Timestamp;
		public int 		Value;
		
		public TValue() {
		}

		public TValue(double pTimestamp, int pValue) {
			Timestamp = pTimestamp;
			Value = pValue;
		}

		
		public TValue Clone() {
			return (new TValue(Timestamp, Value));
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedInt32ContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TTimestampedInt32ContainerType Result = new TTimestampedInt32ContainerType();
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
		if (TUserMessageDeliveryDataType.ID().equals(DataTypeID))
			return new TUserMessageDeliveryDataType(this, pChannel); //. -> 
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
		return Integer.toString(Value.Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 8/*SizeOf(Timestamp)*/+4/*SizeOf(Value)*/;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Timestamp)
		Value.Value = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Value.Value);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
