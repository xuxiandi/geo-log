package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;

public class T3DoubleContainerType extends TContainerType {
	
	public static String ID() {
		return "3Double";
	}

	public static class TValue {
		
		public double Value;
		public double Value1;
		public double Value2;

		public TValue() {
		}

		public TValue(double pValue, double pValue1, double pValue2) {
			Value = pValue;
			Value1 = pValue1;
			Value2 = pValue2;
		}
	}

	
	public TValue Value = new TValue();
	
	public T3DoubleContainerType() {
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
		return String.format(Locale.ENGLISH,"%.2f",Value.Value)+", "+String.format(Locale.ENGLISH,"%.2f",Value.Value1)+", "+String.format(Locale.ENGLISH,"%.2f",Value.Value2);
	}
	
	@Override
	public int ByteArraySize() {
		return 3*8; //. SizeOf(Value)+SizeOf(Value1)+SizeOf(Value2)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Value = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value)
		Value.Value1 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value1)
		Value.Value2 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value2)
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
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value2);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		return Result;
	}
}
