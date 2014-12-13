package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserMessageDataType;

public class TTimestampedTypedTaggedDataContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedTypedTaggedData";
	}
	
	public static class TValue {
		
		public double 	Timestamp;
		public short 	ValueType;
		public int		ValueTag;
		public byte[] 	Value;
		
		public TValue() {
		}

		public TValue(double pTimestamp, short pValueType, int pValueTag, byte[] pValue) {
			Timestamp = pTimestamp;
			ValueType = pValueType;
			ValueTag = pValueTag;
			Value = pValue;
		}
		
		public TValue Clone() {
			byte[] _Value;
			if (Value != null) {
				_Value = new byte[Value.length];
				System.arraycopy(Value,0, _Value,0, Value.length);
			}
			else
				_Value = null;
			return (new TValue(Timestamp, ValueType, ValueTag, _Value));
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedTypedTaggedDataContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TTimestampedTypedTaggedDataContainerType Result = new TTimestampedTypedTaggedDataContainerType();
		Result.Value.Timestamp = Value.Timestamp;
		Result.Value.ValueType = Value.ValueType;
		Result.Value.ValueTag = Value.ValueTag;
		Result.Value.Value = Value.Value;
		return Result;
	}
	
	@Override
	public String GetID() {
		return ID();
	}

	@Override
	public TDataType GetDataType(String DataTypeID, TChannel pChannel) {
		if (TUserMessageDataType.ID().equals(DataTypeID))
			return new TUserMessageDataType(this, pChannel); //. -> 
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
		if (Value.Value == null)
			return null; //. ->
		return (Short.toString(Value.ValueType)+":"+new String(Value.Value)+":"+Integer.toString(Value.ValueTag));
	}
	
	@Override
	public int ByteArraySize() {
		int Size = 8/*SizeOf(Timestamp)*/+2/*SizeOf(ValueType)*/+4/*SizeOf(ValueTag)*/+4/*SizeOf(ValueSize)*/;
		if (Value.Value != null)
			Size += Value.Value.length;
		return Size;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Timestamp)
		Value.ValueType = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(ValueType)
		Value.ValueTag = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(ValueTag)
		int ValueSize = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(ValueSize)
		if (ValueSize > 0) {
			Value.Value = new byte[ValueSize];
			System.arraycopy(BA,Idx, Value.Value,0, ValueSize); Idx += ValueSize;
		}
		else
			Value.Value = null;
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt16ToLEByteArray(Value.ValueType);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToLEByteArray(Value.ValueTag);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		int ValueSize = ((Value.Value != null) ? Value.Value.length : 0);
		BA = TDataConverter.ConvertInt32ToLEByteArray(ValueSize);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (ValueSize > 0) {
			System.arraycopy(Value.Value,0, Result,Idx, ValueSize); Idx += ValueSize;
		}
		return Result;
	}
}
