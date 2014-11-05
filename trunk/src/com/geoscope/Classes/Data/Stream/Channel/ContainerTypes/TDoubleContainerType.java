package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;

public class TDoubleContainerType extends TContainerType {
	
	public static String ID() {
		return "Double";
	}
	

	public double Value;
	
	public TDoubleContainerType() {
		super();
	}
	
	public String GetID() {
		return ID();
	}

	@Override
	public void SetValue(Object pValue) {
		Value = (Double)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Double.valueOf(Value);
	}
	
	@Override
	public String GetValueString(Context context) {
		return String.format(Locale.ENGLISH,"%.2f",Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 8; //. SizeOf(Value)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		return TDataConverter.ConvertDoubleToLEByteArray(Value);
	}
}
