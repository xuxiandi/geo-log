package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;

public class TTimestampedDataContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedData";
	}
	
	public static class TValue {
		
		public double 	Timestamp;
		public byte[] 	Value;
		
		public TValue() {
		}

		public TValue(double pTimestamp, byte[] pValue) {
			Timestamp = pTimestamp;
			Value = pValue;
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedDataContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TTimestampedDataContainerType Result = new TTimestampedDataContainerType();
		Result.Value.Timestamp = Value.Timestamp;
		Result.Value.Value = Value.Value;
		return Result;
	}
	
	@Override
	public String GetID() {
		return ID();
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
		return (new String(Value.Value));
	}
	
	@Override
	public int ByteArraySize() {
		int Size = 8/*SizeOf(Timestamp)*/+4/*SizeOf(ValueSize)*/;
		if (Value.Value != null)
			Size += Value.Value.length;
		return Size;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Timestamp)
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
		int ValueSize = ((Value.Value != null) ? Value.Value.length : 0);
		BA = TDataConverter.ConvertInt32ToLEByteArray(ValueSize);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (ValueSize > 0) {
			System.arraycopy(Value.Value,0, Result,Idx, ValueSize); Idx += ValueSize;
		}
		return Result;
	}
}
