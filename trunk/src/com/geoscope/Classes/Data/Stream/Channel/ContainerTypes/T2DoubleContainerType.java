package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;

public class T2DoubleContainerType extends TContainerType {
	
	public static String ID() {
		return "2Double";
	}

	public static class TValue {
		
		public double Value;
		public double Value1;

		public TValue() {
		}

		public TValue(double pValue, double pValue1) {
			Value = pValue;
			Value1 = pValue1;
		}
	}

	
	public TValue Value = new TValue();
	
	public T2DoubleContainerType() {
		super();
	}

	@Override
	public TContainerType Clone() {
		T2DoubleContainerType Result = new T2DoubleContainerType();
		Result.Value.Value = Value.Value;
		Result.Value.Value1 = Value.Value1;
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
		return String.format(Locale.getDefault(),"%.2f",Value.Value)+", "+String.format(Locale.getDefault(),"%.2f",Value.Value1);
	}
	
	@Override
	public int ByteArraySize() {
		return 2*8; //. SizeOf(Value)+SizeOf(Value1)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Value = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value)
		Value.Value1 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value1)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value1);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		return Result;
	}
}
