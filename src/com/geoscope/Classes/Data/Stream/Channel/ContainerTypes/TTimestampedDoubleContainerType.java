package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;

public class TTimestampedDoubleContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedDouble";
	}
	
	public static class TValue {
		
		public double Timestamp;
		public double Value;
		
		public TValue() {
		}

		public TValue(double pTimestamp, double pValue) {
			Timestamp = pTimestamp;
			Value = pValue;
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedDoubleContainerType() {
		super();
	}
	
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
		return String.format(Locale.ENGLISH,"%.2f",Value.Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 8/*SizeOf(Timestamp)*/+8/*SizeOf(Value)*/;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
		Value.Value = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[8/*SizeOf(Timestamp)*/+8/*SizeOf(Value)*/];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
}
